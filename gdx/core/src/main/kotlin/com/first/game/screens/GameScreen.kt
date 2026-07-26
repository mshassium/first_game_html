package com.first.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.first.game.FirstGame
import com.first.game.GamePrefs
import com.first.game.audio.SoundManager
import com.first.game.domain.ChoiceKind
import com.first.game.domain.Command
import com.first.game.domain.EndReason
import com.first.game.domain.GameEngine
import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Phase
import com.first.game.domain.Rules
import com.first.game.domain.SeededRng
import com.first.game.domain.Side
import com.first.game.domain.ai.aiPolicy
import com.first.game.i18n.Strings
import com.first.game.ui.AnimationDirector
import com.first.game.ui.BoardLayout
import com.first.game.ui.CardActor
import com.first.game.ui.GlowActor
import com.first.game.ui.Palette
import ktx.app.KtxScreen

/**
 * Игровой экран: стол, рука, журнал боя и все анимации.
 *
 * Разделение обязанностей строгое: движок считает правила и отдаёт события,
 * экран их проигрывает и перерисовывает доску. Никакой игровой логики здесь нет.
 */
class GameScreen(
    private val game: FirstGame,
    private val autoPlay: Boolean = false,
) : KtxScreen {

    private val assets = game.assets
    private val theme = game.theme
    private val stage = Stage(ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT), game.batch)

    private val engine = GameEngine(SeededRng(System.currentTimeMillis()))
    private val policy = aiPolicy(GamePrefs.difficulty, SeededRng(System.nanoTime()))

    /** В режиме автоигры за игрока тоже ходит ИИ — так проверяются все анимации подряд. */
    private val autoPolicy = aiPolicy(GamePrefs.difficulty, SeededRng(System.nanoTime() + 17))
    private val director = AnimationDirector { GamePrefs.animationSpeed }

    private var state: GameState
    private var layout = BoardLayout(WORLD_WIDTH, WORLD_HEIGHT)

    private val boardGroup = Group()
    private val cardGroup = Group()
    private val fxGroup = Group()
    private val uiGroup = Group()

    private val logLines = mutableListOf<String>()
    private var elapsedSeconds = 0f
    private var aiDelay = 0f
    private var choiceDialog: Group? = null
    private var menuButton: com.badlogic.gdx.scenes.scene2d.ui.Button? = null
    private var resultDialogShown = false

    init {
        val start = engine.newGame()
        state = start.state
        stage.addActor(boardGroup)
        stage.addActor(cardGroup)
        stage.addActor(fxGroup)
        stage.addActor(uiGroup)
        boardGroup.addActor(BoardBackground())
        uiGroup.addActor(HudOverlay())
        addMenuButton()
        stage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                game.sound.unlock()
                if (!director.isIdle) director.skip()
                return false
            }
        })
        enqueue(start.events)
        syncBoard()
    }

    /**
     * Круглая кнопка возврата в меню. На телефоне клавиши Esc нет, и без неё
     * выйти из партии можно было только системной кнопкой «назад».
     */
    private fun addMenuButton() {
        val icon = assets.icon("menu") ?: return
        val button = com.badlogic.gdx.scenes.scene2d.ui.Button(
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(assets.roundButtonUp),
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(assets.roundButtonDown),
        )
        button.add(com.badlogic.gdx.scenes.scene2d.ui.Image(icon)).grow()
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.sound.play(SoundManager.Sfx.UI_CLICK)
                game.showMenu()
            }
        })
        menuButton = button
        uiGroup.addActor(button)
        placeMenuButton()
    }

    private fun placeMenuButton() {
        val button = menuButton ?: return
        val size = layout.hud.height * 0.86f
        button.setBounds(
            layout.worldWidth - size - layout.hud.height * 0.25f,
            layout.hud.y + (layout.hud.height - size) / 2f,
            size,
            size,
        )
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        game.sound.playMusic(SoundManager.Track.BATTLE)
    }

    override fun render(delta: Float) {
        elapsedSeconds += delta
        Gdx.gl.glClearColor(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.showMenu()
            return
        }

        advance(delta)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        layout = BoardLayout(stage.viewport.worldWidth, stage.viewport.worldHeight)
        syncBoard()
        placeMenuButton()
        choiceDialog?.let { it.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight) }
    }

    override fun dispose() {
        stage.dispose()
    }

    // ------------------------------------------------------------ игровой цикл

    /** Двигает партию вперёд, когда анимации отыграли и решение за нами или за ИИ. */
    private fun advance(delta: Float) {
        if (!director.isIdle) return

        if (state.isOver) {
            if (!resultDialogShown) showResultDialog()
            return
        }

        if (state.actingSide == Side.AI || autoPlay) {
            aiDelay -= delta
            if (aiDelay <= 0f) {
                aiDelay = AI_THINK_TIME * GamePrefs.animationSpeed.factor
                val brain = if (state.actingSide == Side.AI) policy else autoPolicy
                apply(brain.decide(state))
            }
            return
        }

        if (state.phase == Phase.AWAITING_CHOICE && choiceDialog == null) {
            showChoiceDialog()
        }
    }

    private fun apply(command: Command) {
        val result = engine.apply(state, command)
        state = result.state
        enqueue(result.events)
    }

    private fun enqueue(events: List<GameEvent>) {
        for (event in events) {
            // Запись в журнал делаем в момент проигрывания, а не постановки в очередь:
            // иначе текст обгоняет картинку и описывает ещё не показанные ходы.
            director.enqueue { done ->
                logEvent(event)
                animate(event, done)
            }
        }
        director.enqueue { done ->
            syncBoard()
            done()
        }
    }

    // ------------------------------------------------------------- анимации

    private fun animate(event: GameEvent, done: () -> Unit) {
        when (event) {
            is GameEvent.GameStarted -> animateDice(event, done)

            is GameEvent.CardDealt -> flyCard(
                from = deckRect(event.side),
                to = handTarget(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DRAW,
                seconds = 0.28f,
                done = done,
            )

            is GameEvent.CardDrawn -> flyCard(
                from = deckRect(event.side),
                to = handTarget(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DRAW,
                seconds = 0.3f,
                done = done,
            )

            is GameEvent.CardPlayed -> flyCard(
                from = handTarget(event.side),
                to = spaceTarget(event.side),
                letter = event.letter,
                sound = SoundManager.Sfx.CARD_PLACE,
                seconds = 0.45f,
                done = {
                    impact(spaceTarget(event.side), event.letter)
                    done()
                },
            )

            is GameEvent.CardForbidden -> {
                game.sound.play(SoundManager.Sfx.FORBID_TRIGGER)
                flyCard(
                    from = handTarget(event.side),
                    to = discardRect(event.side),
                    letter = event.letter,
                    sound = null,
                    seconds = 0.5f,
                    shake = true,
                    done = done,
                )
            }

            is GameEvent.ForbidSet -> {
                game.sound.play(SoundManager.Sfx.FORBID_CAST)
                pulse(spaceTarget(event.by.other), Palette.school(Letter.F), done)
            }

            is GameEvent.CardRecovered -> flyCard(
                from = discardRect(event.side),
                to = handTarget(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.RECOVER,
                seconds = 0.45f,
                done = done,
            )

            is GameEvent.CardStolen -> {
                game.sound.play(SoundManager.Sfx.STEAL)
                val victim = spaceTarget(event.victim)
                assets.vfxRegion("fx_claw_slash")?.let { region ->
                    addGlow(
                        region,
                        victim.x + victim.width / 2f,
                        victim.y + victim.height / 2f,
                        victim.width * 1.7f,
                        1f,
                        Color.WHITE,
                    ) { actor ->
                        actor.rotation = -18f
                        actor.setScale(0.6f)
                        val slash = director.duration(0.45f)
                        actor.addAction(
                            Actions.sequence(
                                Actions.parallel(
                                    Actions.scaleTo(1.2f, 1.2f, slash * 0.6f, Interpolation.pow3Out),
                                    Actions.sequence(Actions.delay(slash * 0.25f), Actions.fadeOut(slash * 0.75f)),
                                ),
                                Actions.run { actor.remove() },
                            ),
                        )
                    }
                }
                flyCard(
                    from = spaceTarget(event.victim),
                    to = discardRect(event.victim),
                    letter = event.letter,
                    sound = null,
                    seconds = 0.45f,
                    done = done,
                )
            }

            is GameEvent.TrapSet -> {
                game.sound.play(SoundManager.Sfx.TRAP_SET)
                pulse(handRect(event.on), Palette.school(Letter.T), done)
            }

            is GameEvent.TrapTriggered -> {
                game.sound.play(SoundManager.Sfx.TRAP_SNAP)
                flyCard(
                    from = handTarget(event.side),
                    to = discardRect(event.side),
                    letter = event.letter.takeIf { event.side == Side.YOU },
                    sound = null,
                    seconds = 0.4f,
                    shake = true,
                    done = done,
                )
            }

            is GameEvent.HandOverflow -> flyCard(
                from = handTarget(event.side),
                to = discardRect(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DISCARD,
                seconds = 0.35f,
                done = done,
            )

            is GameEvent.TurnBegan -> {
                if (event.side == Side.YOU) game.sound.play(SoundManager.Sfx.TURN_START, pitchVariation = false)
                delay(0.08f, done)
            }

            is GameEvent.GameEnded -> {
                val won = event.outcome.winner == Side.YOU
                game.sound.play(
                    if (won) SoundManager.Sfx.VICTORY else SoundManager.Sfx.DEFEAT,
                    pitchVariation = false,
                )
                delay(0.4f, done)
            }

            else -> done()
        }
    }

    /** Ghost-карта, летящая между зонами. Сами актёры доски не двигаются. */
    private fun flyCard(
        from: Rectangle,
        to: Rectangle,
        letter: Letter?,
        sound: SoundManager.Sfx?,
        seconds: Float,
        shake: Boolean = false,
        done: () -> Unit,
    ) {
        sound?.let(game.sound::play)
        val duration = director.duration(seconds)
        val ghost = CardActor(assets, letter, faceUp = letter != null)
        ghost.setBounds(from.x, from.y, from.width, from.height, centerOrigin = true)
        fxGroup.addActor(ghost)

        val move = Actions.sequence(
            if (shake) {
                Actions.repeat(
                    4,
                    Actions.sequence(
                        Actions.moveBy(8f, 0f, duration * 0.05f),
                        Actions.moveBy(-8f, 0f, duration * 0.05f),
                    ),
                )
            } else {
                Actions.delay(0f)
            },
            Actions.parallel(
                Actions.moveTo(to.x, to.y, duration, Interpolation.swing),
                Actions.sizeTo(to.width, to.height, duration, Interpolation.sine),
            ),
            Actions.run {
                ghost.remove()
                done()
            },
        )
        ghost.addAction(move)
    }

    /** Вспышка, звезда, кольцо и тематический элемент школы на месте приземления карты. */
    private fun impact(target: Rectangle, letter: Letter) {
        val color = Palette.school(letter)
        val duration = director.duration(0.35f)
        val centerX = target.x + target.width / 2f
        val centerY = target.y + target.height / 2f

        // Мягкое свечение цвета школы.
        addGlow(assets.glow, centerX, centerY, target.width * 2.0f, 0.85f, color) { actor ->
            actor.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.fadeOut(duration, Interpolation.pow3Out),
                        Actions.scaleTo(1.3f, 1.3f, duration),
                    ),
                    Actions.run { actor.remove() },
                ),
            )
        }

        // Звезда-вспышка: быстрая, почти белая.
        assets.vfxRegion("fx_burst_star")?.let { region ->
            addGlow(region, centerX, centerY, target.width * 1.7f, 1f, Palette.rgba(color, 1f).lerp(Color.WHITE, 0.5f)) { actor ->
                actor.setScale(0.5f)
                actor.addAction(
                    Actions.sequence(
                        Actions.parallel(
                            Actions.scaleTo(1.15f, 1.15f, duration * 0.6f, Interpolation.pow3Out),
                            Actions.sequence(Actions.delay(duration * 0.25f), Actions.fadeOut(duration * 0.6f)),
                        ),
                        Actions.run { actor.remove() },
                    ),
                )
            }
        }

        // Кольцо ударной волны расходится наружу.
        assets.vfxRegion("fx_shockwave")?.let { region ->
            addGlow(region, centerX, centerY, target.width * 1.2f, 0.9f, color) { actor ->
                actor.setScale(0.35f)
                actor.addAction(
                    Actions.sequence(
                        Actions.parallel(
                            Actions.scaleTo(1.7f, 1.7f, duration * 1.4f, Interpolation.pow2Out),
                            Actions.fadeOut(duration * 1.4f, Interpolation.pow2In),
                        ),
                        Actions.run { actor.remove() },
                    ),
                )
            }
        }

        schoolBurst(letter, centerX, centerY, target.width)
    }

    /**
     * Тематический элемент школы поверх вспышки: у каждой буквы свой почерк,
     * чтобы эффект читался без чтения журнала.
     */
    private fun schoolBurst(letter: Letter, centerX: Float, centerY: Float, cardWidth: Float) {
        val duration = director.duration(0.55f)
        val color = Palette.school(letter)
        when (letter) {
            // Запрет: рунное кольцо раскрывается, от него разлетаются звенья ледяной цепи.
            Letter.F -> {
                assets.vfxRegion("fx_ring_rune")?.let { region ->
                    addGlow(region, centerX, centerY, cardWidth * 1.5f, 0.95f, color) { actor ->
                        actor.setScale(0.4f)
                        actor.addAction(
                            Actions.sequence(
                                Actions.parallel(
                                    Actions.scaleTo(1.1f, 1.1f, duration, Interpolation.pow3Out),
                                    Actions.rotateBy(90f, duration),
                                    Actions.sequence(Actions.delay(duration * 0.4f), Actions.fadeOut(duration * 0.6f)),
                                ),
                                Actions.run { actor.remove() },
                            ),
                        )
                    }
                }
                assets.vfxRegion("fx_chain_link")?.let { region ->
                    repeat(3) { index ->
                        val angle = -30f + index * 30f
                        addGlow(region, centerX, centerY, cardWidth * 0.42f, 1f, Color.WHITE) { actor ->
                            actor.rotation = angle
                            actor.addAction(
                                Actions.sequence(
                                    Actions.parallel(
                                        Actions.moveBy(
                                            MathUtils.cosDeg(angle - 90f) * cardWidth * 0.7f,
                                            MathUtils.sinDeg(angle - 90f) * cardWidth * 0.7f,
                                            duration, Interpolation.pow2Out,
                                        ),
                                        Actions.fadeOut(duration, Interpolation.pow2In),
                                    ),
                                    Actions.run { actor.remove() },
                                ),
                            )
                        }
                    }
                }
            }
            // Прирост: листья поднимаются вверх.
            Letter.I -> assets.vfxRegion("fx_leaf")?.let { region ->
                repeat(5) { index ->
                    val offsetX = MathUtils.random(-0.45f, 0.45f) * cardWidth
                    addGlow(region, centerX + offsetX, centerY, cardWidth * 0.3f, 1f, Color.WHITE) { actor ->
                        actor.rotation = MathUtils.random(-40f, 40f)
                        actor.addAction(
                            Actions.sequence(
                                Actions.delay(index * duration * 0.08f),
                                Actions.parallel(
                                    Actions.moveBy(offsetX * 0.4f, cardWidth * 1.1f, duration, Interpolation.sineOut),
                                    Actions.rotateBy(MathUtils.random(-60f, 60f), duration),
                                    Actions.fadeOut(duration, Interpolation.pow2In),
                                ),
                                Actions.run { actor.remove() },
                            ),
                        )
                    }
                }
            }
            // Возврат: угольки взлетают из-под карты.
            Letter.R -> assets.vfxRegion("fx_ember")?.let { region ->
                repeat(6) { index ->
                    val offsetX = MathUtils.random(-0.4f, 0.4f) * cardWidth
                    addGlow(region, centerX + offsetX, centerY - cardWidth * 0.3f, cardWidth * 0.22f, 1f, Color.WHITE) { actor ->
                        actor.addAction(
                            Actions.sequence(
                                Actions.delay(index * duration * 0.06f),
                                Actions.parallel(
                                    Actions.moveBy(offsetX * 0.5f, cardWidth * 1.3f, duration, Interpolation.sineOut),
                                    Actions.fadeOut(duration, Interpolation.pow2In),
                                ),
                                Actions.run { actor.remove() },
                            ),
                        )
                    }
                }
            }
            // Кража: след когтей наискось по карте.
            Letter.S -> assets.vfxRegion("fx_claw_slash")?.let { region ->
                addGlow(region, centerX, centerY, cardWidth * 1.6f, 1f, Color.WHITE) { actor ->
                    actor.rotation = -18f
                    actor.setScale(0.7f)
                    actor.addAction(
                        Actions.sequence(
                            Actions.parallel(
                                Actions.scaleTo(1.25f, 1.25f, duration * 0.7f, Interpolation.pow3Out),
                                Actions.sequence(Actions.delay(duration * 0.2f), Actions.fadeOut(duration * 0.8f)),
                            ),
                            Actions.run { actor.remove() },
                        ),
                    )
                }
            }
            // Ловушка: челюсти капкана схлопываются.
            Letter.T -> assets.vfxRegion("fx_snare_jaws")?.let { region ->
                addGlow(region, centerX, centerY, cardWidth * 1.5f, 1f, Color.WHITE) { actor ->
                    actor.setScale(1.35f)
                    actor.addAction(
                        Actions.sequence(
                            Actions.parallel(
                                Actions.scaleTo(0.95f, 0.95f, duration * 0.45f, Interpolation.pow3In),
                                Actions.sequence(Actions.delay(duration * 0.45f), Actions.fadeOut(duration * 0.55f)),
                            ),
                            Actions.run { actor.remove() },
                        ),
                    )
                }
            }
        }
    }

    /** Кладёт светящийся элемент по центру и отдаёт его на настройку анимации. */
    private inline fun addGlow(
        region: com.badlogic.gdx.graphics.g2d.TextureRegion,
        centerX: Float,
        centerY: Float,
        width: Float,
        alpha: Float,
        color: Color,
        configure: (GlowActor) -> Unit,
    ) {
        val height = width * region.regionHeight / region.regionWidth
        val actor = GlowActor(region)
        actor.setBounds(centerX - width / 2f, centerY - height / 2f, width, height)
        actor.setOrigin(width / 2f, height / 2f)
        actor.color = Palette.rgba(color, alpha)
        fxGroup.addActor(actor)
        configure(actor)
    }

    private fun pulse(target: Rectangle, color: Color, done: () -> Unit) {
        val glow = com.badlogic.gdx.scenes.scene2d.ui.Image(assets.glow)
        glow.color = Palette.rgba(color, 0f)
        glow.setBounds(target.x, target.y - target.height * 0.2f, target.width, target.height * 1.4f)
        fxGroup.addActor(glow)
        val duration = director.duration(0.3f)
        glow.addAction(
            Actions.sequence(
                Actions.alpha(0.8f, duration, Interpolation.fade),
                Actions.alpha(0f, duration, Interpolation.fade),
                // Удаляем внутри run: снятый со сцены актёр больше не тикает,
                // и всё, что стоит в очереди после removeActor, не выполнится.
                Actions.run {
                    glow.remove()
                    done()
                },
            ),
        )
    }

    private fun animateDice(event: GameEvent.GameStarted, done: () -> Unit) {
        game.sound.play(SoundManager.Sfx.DICE_ROLL, pitchVariation = false)
        val duration = director.duration(1.1f)
        val size = layout.cardHeight * 0.8f
        val centerY = layout.worldHeight / 2f
        val dice = listOf(event.youRoll, event.aiRoll).mapIndexed { index, value ->
            val image = com.badlogic.gdx.scenes.scene2d.ui.Image(assets.dieFace(value))
            image.setBounds(
                layout.worldWidth / 2f + (if (index == 0) -size * 1.2f else size * 0.2f),
                centerY + size,
                size,
                size,
            )
            fxGroup.addActor(image)
            image
        }
        dice.forEachIndexed { index, image ->
            image.addAction(
                Actions.sequence(
                    Actions.delay(duration * 0.1f * index),
                    Actions.moveTo(image.x, centerY, duration * 0.6f, Interpolation.bounceOut),
                    Actions.delay(duration * 0.5f),
                    Actions.fadeOut(duration * 0.3f),
                    Actions.run {
                        image.remove()
                        if (index == dice.lastIndex) done()
                    },
                ),
            )
        }
    }

    private fun delay(seconds: Float, done: () -> Unit) {
        val duration = director.duration(seconds)
        if (duration <= 0f) {
            done()
            return
        }
        stage.addAction(Actions.sequence(Actions.delay(duration), Actions.run { done() }))
    }

    // ------------------------------------------------------------- отрисовка

    /** Пересобирает карты на столе под текущее состояние. */
    private fun syncBoard() {
        cardGroup.clear()

        addSpaceCards(Side.AI)
        addSpaceCards(Side.YOU)
        addHandCards()

        // Колода — нарисованная стопка, если она есть; иначе рубашка одной карты.
        for (side in listOf(Side.AI, Side.YOU)) {
            if (state.side(side).deck.isEmpty()) continue
            val rect = deckRect(side)
            val stack = assets.uiRegion("deck_stack")
            if (stack != null) {
                val image = com.badlogic.gdx.scenes.scene2d.ui.Image(stack)
                val height = rect.width * stack.regionHeight / stack.regionWidth
                image.setBounds(rect.x, rect.y + (rect.height - height) / 2f, rect.width, height)
                cardGroup.addActor(image)
            } else {
                val back = CardActor(assets, null, faceUp = false)
                back.setBounds(rect.x, rect.y, rect.width, rect.height, centerOrigin = true)
                cardGroup.addActor(back)
            }
        }
    }

    /**
     * Карты ложатся в гнёзда, закреплённые за буквой: пустое гнездо сразу показывает,
     * какой буквы не хватает до набора.
     */
    private fun addSpaceCards(side: Side) {
        val zone = if (side == Side.AI) layout.aiSpace else layout.youSpace
        val space = state.side(side).space
        val slots = layout.spaceSlots(zone)
        Letter.ALL.forEachIndexed { index, letter ->
            val count = space.count { it == letter }
            if (count == 0) return@forEachIndexed
            val slot = slots[index]
            val card = CardActor(assets, letter)
            card.stackCount = count
            card.setBounds(slot.x, slot.y, slot.width, slot.height, centerOrigin = true)
            if (state.traps.forbidOn(side) != null && letter == Letter.F) {
                card.highlight = Palette.school(Letter.F)
            }
            cardGroup.addActor(card)
        }
    }

    private fun addHandCards() {
        val hand = state.you.hand
        val slots = layout.handSlots(hand.size)
        val playable = state.turn == Side.YOU && state.phase == Phase.AWAITING_PLAY && !state.isOver
        hand.forEachIndexed { index, letter ->
            val slot = slots[index]
            val card = CardActor(assets, letter)
            card.setBounds(slot.x, slot.y, slot.width, slot.height, centerOrigin = true)
            card.dimmed = !playable
            if (playable) {
                card.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        if (!director.isIdle) return
                        game.sound.unlock()
                        apply(Command.PlayCard(index))
                    }

                    override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                        if (pointer == -1) card.addAction(Actions.moveBy(0f, HOVER_LIFT, 0.12f))
                    }

                    override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                        if (pointer == -1) card.addAction(Actions.moveBy(0f, -HOVER_LIFT, 0.12f))
                    }
                })
            }
            cardGroup.addActor(card)
        }
    }

    // ---------------------------------------------------------------- диалоги

    private fun showChoiceDialog() {
        val pending = state.pending ?: return
        val titleKey = when (pending.kind) {
            ChoiceKind.FORBID_LETTER -> "choice.forbid"
            ChoiceKind.RECOVER_LETTER -> "choice.recover"
            ChoiceKind.STEAL_TARGET -> "choice.steal"
            ChoiceKind.TRAP_DISCARD -> "choice.trap"
        }

        val dialog = Group()
        dialog.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)

        val dim = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.dim(0.72f))
        dim.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)
        dialog.addActor(dim)

        val title = Label(Strings[titleKey], theme.title)
        title.setAlignment(com.badlogic.gdx.utils.Align.center)
        title.setBounds(0f, layout.worldHeight * 0.62f, layout.worldWidth, title.prefHeight)
        dialog.addActor(title)

        val cardWidth = layout.cardWidth * 0.85f
        val cardHeight = layout.cardHeight * 0.85f
        val gap = cardWidth * 0.18f
        val options = pending.options
        val totalWidth = options.size * cardWidth + (options.size - 1) * gap
        val scale = if (totalWidth > layout.worldWidth * 0.9f) layout.worldWidth * 0.9f / totalWidth else 1f
        var x = (layout.worldWidth - totalWidth * scale) / 2f
        val y = layout.worldHeight * 0.32f

        options.forEachIndexed { index, option ->
            val card = CardActor(assets, option.letter)
            card.setBounds(x, y, cardWidth * scale, cardHeight * scale, centerOrigin = true)
            card.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    dismissChoiceDialog()
                    game.sound.play(SoundManager.Sfx.UI_CLICK)
                    apply(Command.ChooseOption(index))
                }
            })
            dialog.addActor(card)
            x += (cardWidth + gap) * scale
        }

        dialog.color.a = 0f
        dialog.addAction(Actions.fadeIn(director.duration(0.22f)))
        uiGroup.addActor(dialog)
        choiceDialog = dialog
    }

    private fun dismissChoiceDialog() {
        choiceDialog?.remove()
        choiceDialog = null
    }

    private fun showResultDialog() {
        resultDialogShown = true
        val outcome = state.outcome ?: return
        val won = outcome.winner == Side.YOU

        val dialog = Group()
        dialog.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)
        val dim = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.dim(0.8f))
        dim.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)
        dialog.addActor(dim)

        val title = Label(Strings[if (won) "result.victory" else "result.defeat"], theme.titleLarge)
        title.setAlignment(com.badlogic.gdx.utils.Align.center)
        title.color = if (won) Palette.GOLD_LIGHT else Palette.TEXT_MUTED
        title.setBounds(0f, layout.worldHeight * 0.6f, layout.worldWidth, title.prefHeight)
        dialog.addActor(title)

        val reasonKey = when (outcome.reason) {
            EndReason.FIRST_SET -> "result.firstSet"
            EndReason.FIVE_OF_A_KIND -> "result.fiveOfAKind"
            EndReason.DECK_OUT -> "result.deckOut"
        }
        val reason = Label(Strings[reasonKey], theme.body)
        reason.setAlignment(com.badlogic.gdx.utils.Align.center)
        reason.setBounds(0f, layout.worldHeight * 0.52f, layout.worldWidth, reason.prefHeight)
        dialog.addActor(reason)

        val buttonWidth = layout.worldWidth * 0.22f
        val buttonHeight = layout.worldHeight * 0.09f
        val newGame = TextButton(Strings["common.newGame"], theme.button)
        newGame.setBounds(
            layout.worldWidth / 2f - buttonWidth - 8f,
            layout.worldHeight * 0.32f,
            buttonWidth,
            buttonHeight,
        )
        newGame.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = game.startGame()
        })
        dialog.addActor(newGame)

        val menu = TextButton(Strings["common.menu"], theme.button)
        menu.setBounds(layout.worldWidth / 2f + 8f, layout.worldHeight * 0.32f, buttonWidth, buttonHeight)
        menu.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = game.showMenu()
        })
        dialog.addActor(menu)

        dialog.color.a = 0f
        dialog.addAction(Actions.fadeIn(0.3f))
        uiGroup.addActor(dialog)
    }

    // ------------------------------------------------------------- координаты

    private fun deckRect(side: Side): Rectangle =
        if (side == Side.AI) layout.aiDeck else layout.youDeck

    private fun discardRect(side: Side): Rectangle =
        if (side == Side.AI) layout.aiDiscard else layout.youDiscard

    private fun handRect(side: Side): Rectangle =
        if (side == Side.YOU) layout.hand else layout.aiSpace

    /** Куда прилетает или откуда вылетает карта руки. */
    private fun handTarget(side: Side): Rectangle {
        if (side == Side.AI) {
            return Rectangle(
                layout.aiSpace.x + layout.aiSpace.width / 2f - layout.cardWidth / 2f,
                layout.aiSpace.y + layout.aiSpace.height,
                layout.cardWidth,
                layout.cardHeight,
            )
        }
        val hand = layout.hand
        return Rectangle(
            hand.x + hand.width / 2f - layout.cardWidth / 2f,
            hand.y + (hand.height - layout.cardHeight) / 2f,
            layout.cardWidth,
            layout.cardHeight,
        )
    }

    private fun spaceTarget(side: Side): Rectangle {
        val zone = if (side == Side.AI) layout.aiSpace else layout.youSpace
        return Rectangle(
            zone.x + zone.width / 2f - layout.cardWidth / 2f,
            zone.y + (zone.height - layout.cardHeight) / 2f,
            layout.cardWidth,
            layout.cardHeight,
        )
    }

    // ---------------------------------------------------------------- журнал

    private fun logEvent(event: GameEvent) {
        val you = Strings["hud.you"]
        val ai = Strings["hud.ai"]
        fun name(side: Side) = if (side == Side.YOU) you else ai

        val line = when (event) {
            is GameEvent.GameStarted -> Strings["log.gameStarted"]
            is GameEvent.TurnBegan -> Strings.format("log.turnBegan", name(event.side))
            is GameEvent.CardDrawn -> Strings.format("log.drew", name(event.side))
            is GameEvent.HandOverflow -> Strings.format("log.overflow", name(event.side), event.letter.name)
            is GameEvent.CardPlayed -> Strings.format("log.played", name(event.side), event.letter.name)
            is GameEvent.CardForbidden -> Strings.format("log.forbidden", name(event.side), event.letter.name)
            is GameEvent.ForbidSet -> Strings.format("log.forbidSet", name(event.by))
            is GameEvent.CardRecovered -> Strings.format("log.recovered", name(event.side), event.letter.name)
            is GameEvent.CardStolen -> Strings.format("log.stolen", name(event.victim), event.letter.name)
            is GameEvent.TrapSet -> Strings.format("log.trapSet", name(event.on))
            is GameEvent.TrapTriggered -> Strings.format("log.trapTriggered", name(event.side), event.letter.name)
            is GameEvent.EffectFizzled -> Strings.format("log.fizzled", name(event.side), event.letter.name)
            is GameEvent.TurnSkipped -> Strings.format("log.skipped", name(event.side))
            is GameEvent.GameEnded -> Strings["log.ended"]
            else -> null
        } ?: return

        logLines += line
        if (logLines.size > LOG_CAPACITY) logLines.removeAt(0)
    }

    // ------------------------------------------------------------- слои фона

    /** Панели зон и подписи. Отдельный актёр, чтобы не пересобирать их при ресайзе. */
    private inner class BoardBackground : Actor() {
        override fun draw(batch: Batch, parentAlpha: Float) {
            drawTable(batch)
            drawZone(batch, layout.aiSpace, state.turn == Side.AI)
            drawZone(batch, layout.youSpace, state.turn == Side.YOU)

            drawPortrait(batch, layout.aiPortrait, Side.AI)
            drawPortrait(batch, layout.youPortrait, Side.YOU)

            batch.setColor(Color.WHITE)
            theme.panel.draw(batch, layout.hand.x, layout.hand.y, layout.hand.width, layout.hand.height)
            theme.panelStone.draw(
                batch, layout.aiDiscard.x, layout.aiDiscard.y, layout.aiDiscard.width, layout.aiDiscard.height,
            )
            theme.panelStone.draw(
                batch, layout.youDiscard.x, layout.youDiscard.y, layout.youDiscard.width, layout.youDiscard.height,
            )
            if (layout.log.width > 0f) {
                theme.panelParchment.draw(
                    batch, layout.log.x, layout.log.y, layout.log.width, layout.log.height,
                )
            }
            batch.setColor(Color.WHITE)
        }

        /** Полноэкранный фон стола под всей раскладкой. */
        private fun drawTable(batch: Batch) {
            val texture = assets.background(
                if (layout.portrait) "bg_table_portrait" else "bg_table_landscape",
            ) ?: return
            batch.setColor(Color.WHITE)
            batch.draw(texture, 0f, 0f, layout.worldWidth, layout.worldHeight)
        }

        private fun drawZone(batch: Batch, rect: Rectangle, active: Boolean) {
            // Панель полупрозрачна: под ней нарисован стол, и он должен читаться.
            val opacity = if (assets.background("bg_table_landscape") != null) 0.62f else 0.92f
            drawPanel(batch, rect, Palette.rgba(Color.WHITE, opacity))
            // Пустые гнёзда под каждую букву: сразу видно, чего не хватает до набора.
            assets.uiRegion("slot_card")?.let { slot ->
                batch.setColor(1f, 1f, 1f, 0.5f)
                for (position in layout.spaceSlots(rect)) {
                    batch.draw(slot, position.x, position.y, position.width, position.height)
                }
                batch.setColor(Color.WHITE)
            }
            if (active) {
                batch.setColor(Palette.rgba(Palette.GOLD, 0.22f))
                batch.draw(assets.glow, rect.x, rect.y, rect.width, rect.height)
            }
        }

        /** Панель рисуется 9-patch'ем: резные углы держат размер, тянется только середина. */
        /**
         * Портрет стороны в круглой раме. Рама активной стороны подсвечивается —
         * это второй, более заметный указатель на то, чей сейчас ход.
         */
        private fun drawPortrait(batch: Batch, rect: Rectangle, side: Side) {
            val portrait = assets.uiRegion(if (side == Side.YOU) "portrait_player" else "portrait_ai")
            val frame = assets.uiRegion("frame_portrait")
            if (portrait == null && frame == null) return

            val active = state.turn == side && !state.isOver
            if (active) {
                // Пульсация привязана к часам сцены, а не к состоянию — считать нечего.
                val pulse = 0.45f + 0.25f * MathUtils.sin(elapsedSeconds * 3f)
                batch.setColor(Palette.rgba(Palette.GOLD, pulse))
                val halo = rect.width * 0.55f
                batch.draw(
                    assets.glow,
                    rect.x - halo / 2f, rect.y - halo / 2f,
                    rect.width + halo, rect.height + halo,
                )
            }

            // Портрет вписан внутрь кольца, поэтому уменьшаем его на толщину рамы.
            portrait?.let {
                val inset = rect.width * 0.14f
                batch.setColor(if (active) Color.WHITE else Palette.rgba(Color.WHITE, 0.72f))
                batch.draw(
                    it,
                    rect.x + inset, rect.y + inset,
                    rect.width - inset * 2f, rect.height - inset * 2f,
                )
            }
            frame?.let {
                batch.setColor(Color.WHITE)
                batch.draw(it, rect.x, rect.y, rect.width, rect.height)
            }
            batch.setColor(Color.WHITE)
        }

        private fun drawPanel(batch: Batch, rect: Rectangle, color: Color) {
            batch.setColor(color)
            theme.panelStone.draw(batch, rect.x, rect.y, rect.width, rect.height)
        }
    }

    /** HUD, счётчики, сбросы и журнал боя — весь текст экрана. */
    private inner class HudOverlay : Actor() {
        private val glyphs = GlyphLayout()

        override fun draw(batch: Batch, parentAlpha: Float) {
            val body = assets.bodyFont
            val title = assets.titleFont
            val hud = layout.hud

            val turnText = when {
                state.isOver -> Strings["log.ended"]
                state.turn == Side.YOU -> Strings["hud.yourTurn"]
                else -> Strings["hud.opponentTurn"]
            }
            title.color = if (state.turn == Side.YOU) Palette.GOLD_LIGHT else Palette.TEXT_MUTED
            title.draw(batch, turnText, hud.x + 16f, hud.y + hud.height * 0.78f)
            title.color = Color.WHITE

            body.color = Palette.TEXT
            val iconSize = hud.height * 0.52f
            val baseline = hud.y + hud.height * 0.74f
            val iconY = baseline - iconSize * 0.82f

            val timer = formatTime(elapsedSeconds)
            glyphs.setText(body, timer)
            var x = hud.x + hud.width - glyphs.width - hud.height * 1.35f
            body.draw(batch, timer, x, baseline)
            assets.icon("hourglass")?.let {
                batch.setColor(Color.WHITE)
                batch.draw(it, x - iconSize * 1.15f, iconY, iconSize, iconSize)
            }

            // Счётчики руки, колоды и оппонента с иконками перед числами.
            val counters = listOf(
                "hand" to "${state.you.hand.size}/${Rules.HAND_LIMIT}",
                "deck" to "${state.you.deck.size}",
                "duel" to "${state.ai.hand.size} / ${state.ai.deck.size}",
            )
            val gap = iconSize * 0.45f
            var totalWidth = 0f
            for ((icon, value) in counters) {
                glyphs.setText(body, value)
                totalWidth += glyphs.width + gap * 2f + if (assets.icon(icon) != null) iconSize else 0f
            }
            x = hud.x + (hud.width - totalWidth) / 2f
            for ((icon, value) in counters) {
                assets.icon(icon)?.let {
                    batch.setColor(Color.WHITE)
                    batch.draw(it, x, iconY, iconSize, iconSize)
                    x += iconSize + gap * 0.4f
                }
                body.draw(batch, value, x, baseline)
                glyphs.setText(body, value)
                x += glyphs.width + gap * 1.6f
            }

            drawDiscard(batch, layout.aiDiscard, Side.AI)
            drawDiscard(batch, layout.youDiscard, Side.YOU)
            if (layout.log.width > 0f) drawLog(batch)
            body.color = Color.WHITE
        }

        private fun drawDiscard(batch: Batch, rect: Rectangle, side: Side) {
            val body = assets.bodyFont
            val discard = state.side(side).discard
            val padX = rect.width * 0.055f
            val padY = rect.height * 0.16f

            // Подпись держим внутри рамки и уменьшаем — панель низкая,
            // а сверху и снизу у неё резной кант.
            val caption = "${Strings["hud.discard"]} — ${if (side == Side.YOU) Strings["hud.you"] else Strings["hud.ai"]}"
            val captionScale = (rect.height * 0.17f / body.capHeight).coerceAtMost(1f)
            body.data.setScale(captionScale)
            body.color = Palette.TEXT_MUTED
            body.draw(batch, caption, rect.x + padX, rect.y + rect.height - padY * 1.15f)
            body.data.setScale(1f)

            // Урна слева — в неё «падают» карты, дальше идут миниатюры по буквам.
            val height = (rect.height - padY * 3.1f).coerceAtLeast(24f)
            val width = height * (2f / 3f)
            var x = rect.x + padX
            assets.uiRegion("discard_urn")?.let { urn ->
                val urnHeight = height * 0.92f
                val urnWidth = urnHeight * urn.regionWidth / urn.regionHeight
                batch.setColor(Color.WHITE)
                batch.draw(urn, x, rect.y + padY * 0.9f, urnWidth, urnHeight)
                x += urnWidth * 1.1f
            }
            val y = rect.y + padY * 0.9f
            for (letter in Letter.ALL) {
                val count = discard.count { it == letter }
                if (count == 0) continue
                batch.setColor(Color.WHITE)
                batch.draw(assets.cardFace(letter), x, y, width, height)
                if (count > 1) {
                    val scale = height * 0.30f / body.capHeight
                    body.data.setScale(scale)
                    body.color = Palette.GOLD_LIGHT
                    glyphs.setText(body, "×$count")
                    body.draw(batch, glyphs, x + width - glyphs.width, y + glyphs.height + 2f)
                    body.data.setScale(1f)
                }
                x += width * 1.12f
            }
            body.color = Palette.TEXT
        }

        private fun drawLog(batch: Batch) {
            val body = assets.bodyFont
            val rect = layout.log
            // Пергамент светлый, поэтому текст здесь тёмный — иначе не читается.
            val padX = rect.width * 0.10f
            val padTop = rect.height * 0.075f
            val ink = if (theme.parchmentLog) INK else Palette.TEXT
            val inkMuted = if (theme.parchmentLog) INK_MUTED else Palette.TEXT_MUTED

            body.color = inkMuted
            body.draw(batch, Strings["hud.log"], rect.x + padX, rect.y + rect.height - padTop * 1.9f)

            // Строки переносятся по ширине, поэтому высоту каждой меряем, а не считаем
            // одинаковой: иначе длинные записи наезжают друг на друга.
            val width = rect.width - padX * 2f
            val ceiling = rect.y + rect.height - padTop * 2.6f - body.lineHeight
            var y = rect.y + padTop * 1.6f
            body.color = ink
            for (line in logLines.asReversed()) {
                glyphs.setText(body, line, body.color, width, com.badlogic.gdx.utils.Align.left, true)
                if (y + glyphs.height > ceiling) break
                body.draw(batch, glyphs, rect.x + padX, y + glyphs.height)
                y += glyphs.height + 4f
            }
            body.color = Palette.TEXT
        }

        private fun formatTime(seconds: Float): String {
            val total = seconds.toInt()
            return "%02d:%02d".format(total / 60, total % 60)
        }
    }

    private companion object {
        const val WORLD_WIDTH = 1280f
        const val WORLD_HEIGHT = 720f
        const val AI_THINK_TIME = 0.65f
        const val HOVER_LIFT = 14f
        const val LOG_CAPACITY = 40

        /** Чернила по пергаменту. */
        val INK: Color = Color.valueOf("2E2013")
        val INK_MUTED: Color = Color.valueOf("6B5433")
    }
}
