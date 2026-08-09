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
import com.first.game.SaveGame
import com.first.game.SavedGame
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
import com.first.game.ui.ForbidBanner
import com.first.game.ui.GlowActor
import com.first.game.ui.Palette
import com.first.game.ui.drawCover
import ktx.app.KtxScreen

/**
 * Игровой экран: стол, рука и все анимации.
 *
 * Разделение обязанностей строгое: движок считает правила и отдаёт события,
 * экран их проигрывает и перерисовывает доску. Никакой игровой логики здесь нет.
 */
class GameScreen(
    private val game: FirstGame,
    private val autoPlay: Boolean = false,
    /** Отложенная партия: экран открывается сразу в ней, без раздачи. */
    private val resumed: SavedGame? = null,
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

    /** Всплывающие окна: их затемнение перехватывает касания по всей площади. */
    private val dialogGroup = Group()

    /**
     * Слой поверх диалогов. Кнопка правил живёт здесь, потому что забыть эффект
     * карты проще всего именно в момент выбора цели — тогда правила и нужны.
     * Порядок слоёв задан один раз и не зависит от того, кто когда добавился.
     */
    private val topGroup = Group()

    private var elapsedSeconds = 0f
    private var aiDelay = 0f
    private val hudOverlay = HudOverlay()
    private var hudHint: Group? = null
    private var choiceDialog: Group? = null
    private var pauseDialog: Group? = null
    private var menuButton: com.badlogic.gdx.scenes.scene2d.ui.Button? = null
    private var rulesButton: TextButton? = null
    private val overlay = com.first.game.ui.Overlay(stage, theme, assets, game.sound)
    private var resultDialogShown = false

    /**
     * Отложенные изменения доски.
     *
     * Движок применяет весь ход разом и отдаёт финальное состояние, а экран
     * проигрывает события по очереди. Если рисовать прямо из состояния, любое
     * изменение видно с первого кадра: взятая карта появляется в руке ещё до того,
     * как прилетит из колоды, а разыгранная исчезает раньше своего полёта.
     *
     * Поэтому вся пачка событий резервируется заранее, и каждое отпускается ровно
     * тогда, когда его анимация отыграла. Доска рисуется из состояния с поправкой
     * на эти списки.
     */
    private val pendingHandOps = mutableListOf<HandOp>()

    /** Карты руки игрока в порядке раскладки: события адресуют их по индексу. */
    private val handActors = mutableListOf<CardActor>()

    private val pendingSpace = mutableListOf<Pair<Side, Letter>>()
    private val leavingSpace = mutableListOf<Pair<Side, Letter>>()
    private val pendingDiscard = mutableListOf<Pair<Side, Letter>>()

    /**
     * Длящиеся эффекты в том виде, в каком они уже показаны на столе.
     *
     * Из состояния их взять нельзя по той же причине, что и руку: движок применяет
     * ход целиком, и печать запрета появилась бы в тот кадр, когда карту F только
     * понесли на стол, а исчезла бы раньше, чем запрещённая карта дёрнулась и
     * улетела в сброс. Ключ — сторона, на которой эффект висит.
     */
    private val shownForbid = mutableMapOf<Side, Letter>()

    /** Печать появилась только что — при сборке доски ей добавляется «хлопок». */
    private var forbidPopOn: Side? = null

    init {
        val start = engine.newGame()
        state = resumed?.state ?: start.state
        elapsedSeconds = resumed?.elapsedSeconds ?: 0f
        // Отложенная партия открывается без событий, поэтому висящие эффекты
        // берутся прямо из состояния: показывать их нечему было бы.
        for (side in Side.entries) {
            state.traps.forbidOn(side)?.let { shownForbid[side] = it }
        }
        stage.addActor(boardGroup)
        stage.addActor(cardGroup)
        stage.addActor(fxGroup)
        stage.addActor(uiGroup)
        stage.addActor(dialogGroup)
        stage.addActor(topGroup)
        boardGroup.addActor(BoardBackground())
        uiGroup.addActor(hudOverlay)
        addMenuButton()
        addRulesButton()
        placeHud()
        stage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                game.sound.unlock()
                if (!director.isIdle) director.skip()
                return false
            }
        })
        // Продолженная партия не переигрывает раздачу: доска собирается сразу.
        if (resumed == null) enqueue(start.events)
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
                showPauseDialog()
            }
        })
        menuButton = button
        uiGroup.addActor(button)
        placeMenuButton()
    }

    /**
     * Кнопка правил рядом с колодой.
     *
     * Новички забывают способности карт, а окно правил до сих пор жило только
     * в меню — чтобы его открыть, приходилось выходить из партии.
     */
    private fun addRulesButton() {
        val button = TextButton(Strings["menu.rules"], theme.buttonCompact)
        button.label.setFontScale(RULES_BUTTON_SCALE)
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.sound.play(SoundManager.Sfx.UI_CLICK)
                overlay.showRules()
            }
        })
        rulesButton = button
        topGroup.addActor(button)
        placeRulesButton()
    }

    /** Границы HUD нужны не для отрисовки, а чтобы он ловил касания по счётчикам. */
    private fun placeHud() {
        hudOverlay.setBounds(layout.hud.x, layout.hud.y, layout.hud.width, layout.hud.height)
    }

    /**
     * Описание счётчика под его иконкой.
     *
     * Значки в верхней строке ничего не поясняют сами по себе, а новичку неочевидно,
     * где чья колода и что означает дробь.
     */
    private fun showHudHint(key: String, anchor: Rectangle) {
        hideHudHint()
        val width = (layout.worldWidth * 0.22f).coerceIn(200f, 340f)
        val pad = width * 0.07f
        val label = overlay.wrapped(Strings[key])
        // Подсказка служебная и висит поверх стола: шрифт мельче, чем в правилах.
        label.setFontScale(HUD_HINT_FONT)
        label.setWidth(width - pad * 2f)
        val height = label.prefHeight + pad * 2f

        val x = (anchor.x + anchor.width / 2f - width / 2f)
            .coerceIn(pad, layout.worldWidth - width - pad)
        val y = (anchor.y - height - pad * 0.5f).coerceAtLeast(pad)

        val group = Group()
        group.setBounds(x, y, width, height)
        val panel = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.panelStone)
        panel.setBounds(0f, 0f, width, height)
        group.addActor(panel)
        label.setBounds(pad, pad, width - pad * 2f, height - pad * 2f)
        group.addActor(label)
        group.color.a = 0f
        group.addAction(
            Actions.sequence(
                Actions.fadeIn(0.12f),
                Actions.delay(HUD_HINT_TIME),
                Actions.fadeOut(0.2f),
                Actions.run { hideHudHint() },
            ),
        )
        topGroup.addActor(group)
        hudHint = group
    }

    private fun hideHudHint() {
        hudHint?.remove()
        hudHint = null
    }

    private fun placeRulesButton() {
        val button = rulesButton ?: return
        val rect = layout.rulesButton
        button.setBounds(rect.x, rect.y, rect.width, rect.height)
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
        game.sound.update(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            // Открытые правила закрываются первым нажатием, дальше спрашиваем о выходе.
            when {
                overlay.isOpen -> overlay.close()
                pauseDialog != null -> dismissPauseDialog()
                else -> showPauseDialog()
            }
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
        placeRulesButton()
        placeHud()
        hideHudHint()
        choiceDialog?.let { it.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight) }
    }

    /**
     * Сворачивание приложения — на телефоне игру чаще всего закрывают именно так,
     * и без сохранения здесь партия просто пропадёт.
     */
    override fun pause() {
        if (!state.isOver) SaveGame.save(state, elapsedSeconds)
    }

    override fun dispose() {
        stage.dispose()
    }

    // ------------------------------------------------------------ игровой цикл

    /** Двигает партию вперёд, когда анимации отыграли и решение за нами или за ИИ. */
    private fun advance(delta: Float) {
        if (!director.isIdle) return

        if (state.isOver) {
            if (!resultDialogShown) {
                SaveGame.clear()
                showResultDialog()
            }
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
        // Резервируем всю пачку сразу: иначе пересборка доски между событиями
        // покажет то, что случится только через несколько шагов.
        events.forEach(::reserve)
        var index = 0
        while (index < events.size) {
            // Раздача в начале партии идёт одним залпом: по карте за раз это
            // семь одинаковых перелётов подряд, и рука всё это время пустая.
            if (events[index] is GameEvent.CardDealt) {
                val batch = mutableListOf<GameEvent.CardDealt>()
                while (index < events.size && events[index] is GameEvent.CardDealt) {
                    batch += events[index] as GameEvent.CardDealt
                    index++
                }
                director.enqueue { done ->
                    dealHand(batch) {
                        batch.forEach(::release)
                        syncBoard()
                        delay(BEAT_PLAY, done)
                    }
                }
                continue
            }
            val event = events[index]
            director.enqueue { done ->
                animate(event) {
                    release(event)
                    // Доска догоняет состояние сразу после события, а не после всей
                    // пачки: иначе карта, которую только что разыграли, продолжает
                    // висеть в руке до конца хода и выглядит задвоенной.
                    syncBoard()
                    // Пауза после каждого события. Без неё ход проигрывается одним
                    // непрерывным потоком: взял карту, разыграл, сработала способность,
                    // сбросил лишнее — и глазу негде разделить их.
                    delay(beatAfter(event), done)
                }
            }
            index++
        }
    }

    /** Событие ещё не проиграно: его изменения на доске не показываем. */
    private fun reserve(event: GameEvent) = shift(event, reserve = true)

    /** Событие отыграло: изменение можно показывать. */
    private fun release(event: GameEvent) = shift(event, reserve = false)

    private fun shift(event: GameEvent, reserve: Boolean) {
        // Отпускаются события в том же порядке, в каком резервировались, поэтому
        // снимаем первое совпадение: оно и есть самое раннее неотыгранное.
        fun hand(side: Side, op: HandOp) {
            if (side != Side.YOU) return
            if (reserve) pendingHandOps += op else pendingHandOps.remove(op)
        }

        fun zone(list: MutableList<Pair<Side, Letter>>, side: Side, letter: Letter) {
            if (reserve) list += side to letter else list.remove(side to letter)
        }

        when (event) {
            is GameEvent.CardDealt -> hand(event.side, HandOp.Added(event.letter))
            is GameEvent.CardDrawn -> hand(event.side, HandOp.Added(event.letter))
            is GameEvent.CardRecovered -> hand(event.side, HandOp.Added(event.letter))

            is GameEvent.CardPlayed -> {
                hand(event.side, HandOp.Removed(event.handIndex, event.letter))
                zone(pendingSpace, event.side, event.letter)
            }

            is GameEvent.HandOverflow -> {
                hand(event.side, HandOp.Removed(event.handIndex, event.letter))
                zone(pendingDiscard, event.side, event.letter)
            }

            is GameEvent.CardForbidden -> {
                hand(event.side, HandOp.Removed(event.handIndex, event.letter))
                zone(pendingDiscard, event.side, event.letter)
                // Печать снимается только после того, как карта дёрнулась и улетела.
                if (!reserve) shownForbid.remove(event.side)
            }

            is GameEvent.TrapTriggered -> {
                hand(event.side, HandOp.Removed(event.handIndex, event.letter))
                zone(pendingDiscard, event.side, event.letter)
            }

            is GameEvent.CardStolen -> {
                zone(leavingSpace, event.victim, event.letter)
                zone(pendingDiscard, event.victim, event.letter)
            }

            // Флаг снимается вместе с картой, которая его держала.
            is GameEvent.ForbidBroken -> if (!reserve) shownForbid.remove(event.on)

            // Запрет ложится на оппонента того, кто разыграл F, а печать встаёт
            // на его карту F — то есть в SPACE наложившей стороны.
            is GameEvent.ForbidSet -> if (!reserve) {
                shownForbid[event.by.other] = event.letter
                forbidPopOn = event.by.other
            }

            else -> Unit
        }
    }

    /**
     * Рука игрока такой, какой её уже показали: без ещё летящих и с ещё не улетевшими.
     *
     * Состояние движка — итог всей пачки, поэтому неотыгранные операции откатываются
     * с конца: индекс ушедшей карты задан относительно руки, какой она была перед
     * этим уходом, и вернуть её на место можно только после более поздних откатов.
     */
    private fun visualHand(): List<Letter> =
        state.you.hand.toMutableList().apply {
            for (op in pendingHandOps.asReversed()) {
                when (op) {
                    is HandOp.Added -> if (isNotEmpty()) removeAt(lastIndex)
                    is HandOp.Removed -> add(op.index.coerceIn(0, size), op.letter)
                }
            }
        }

    private fun visualSpace(side: Side): List<Letter> =
        state.side(side).space.toMutableList().apply {
            pendingSpace.filter { it.first == side }.forEach { remove(it.second) }
            addAll(leavingSpace.filter { it.first == side }.map { it.second })
        }

    private fun visualDiscard(side: Side): List<Letter> =
        state.side(side).discard.toMutableList().apply {
            pendingDiscard.filter { it.first == side }.forEach { remove(it.second) }
        }

    /**
     * Стартовая раздача: все карты вылетают из колоды разом и приземляются вместе.
     *
     * Рубашками вверх — до приземления игрок не должен знать свою руку; открывается
     * она целиком в момент, когда доска пересобирается.
     */
    private fun dealHand(batch: List<GameEvent.CardDealt>, done: () -> Unit) {
        game.sound.play(SoundManager.Sfx.CARD_DRAW)
        val duration = director.duration(DEAL_TIME)
        val from = layout.deck
        val yourCards = batch.filter { it.side == Side.YOU }
        val slots = layout.handSlots(yourCards.size)

        var pending = 0
        var yourIndex = 0
        for (event in batch) {
            val to = if (event.side == Side.YOU) {
                slots.getOrElse(yourIndex++) { handTarget(Side.YOU) }
            } else {
                handTarget(Side.AI)
            }
            val ghost = CardActor(assets, null, faceUp = false)
            ghost.setBounds(from.x, from.y, from.width, from.height, centerOrigin = true)
            fxGroup.addActor(ghost)
            pending++
            ghost.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.moveTo(to.x, to.y, duration, Interpolation.swing),
                        Actions.sizeTo(to.width, to.height, duration, Interpolation.sine),
                    ),
                    Actions.run {
                        ghost.remove()
                        if (--pending == 0) done()
                    },
                ),
            )
        }
        if (pending == 0) done()
    }

    // ------------------------------------------------------------- анимации

    /**
     * Пауза после события, в секундах до умножения на настройку скорости.
     *
     * Срабатывания способностей держатся заметно дольше перемещений: это самое
     * непонятное на экране, и его нужно успеть прочитать.
     */
    private fun beatAfter(event: GameEvent): Float = when (event) {
        is GameEvent.ForbidSet,
        is GameEvent.ForbidBroken,
        is GameEvent.TrapSet,
        is GameEvent.TrapTriggered,
        is GameEvent.CardStolen,
        is GameEvent.CardRecovered,
        is GameEvent.CardForbidden,
        is GameEvent.EffectFizzled,
        is GameEvent.TurnSkipped -> BEAT_ABILITY
        is GameEvent.CardPlayed -> BEAT_PLAY
        is GameEvent.GameStarted, is GameEvent.GameEnded -> 0f
        else -> BEAT_SHORT
    }

    private fun animate(event: GameEvent, done: () -> Unit) {
        when (event) {
            is GameEvent.GameStarted -> animateDice(event, done)

            is GameEvent.CardDealt -> flyCard(
                from = deckRect(event.side),
                to = handArrivalSlot(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DRAW,
                seconds = 0.34f,
                done = done,
            )

            is GameEvent.CardDrawn -> flyCard(
                from = deckRect(event.side),
                to = handArrivalSlot(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DRAW,
                seconds = 0.38f,
                done = done,
            )

            is GameEvent.CardPlayed -> flyFromHand(
                side = event.side,
                handIndex = event.handIndex,
                to = spaceSlotFor(event.side, event.letter),
                letter = event.letter,
                sound = SoundManager.Sfx.CARD_PLACE,
                seconds = 0.55f,
                done = {
                    impact(spaceSlotFor(event.side, event.letter), event.letter)
                    done()
                },
            )

            is GameEvent.CardForbidden -> {
                game.sound.play(SoundManager.Sfx.FORBID_TRIGGER)
                // Печать отработала и сейчас пропадёт: без вспышки её исчезновение
                // происходит где-то сбоку от летящей карты и остаётся незамеченным.
                flashBanner(event.side)
                flyFromHand(
                    side = event.side,
                    handIndex = event.handIndex,
                    to = discardTarget(event.side),
                    letter = event.letter,
                    sound = null,
                    seconds = 0.75f,
                    shake = true,
                    done = done,
                )
            }

            is GameEvent.ForbidSet -> {
                game.sound.play(SoundManager.Sfx.FORBID_CAST)
                pulse(spaceTarget(event.by.other), Palette.school(Letter.F), done)
            }

            is GameEvent.CardRecovered -> flyCard(
                from = discardTarget(event.side),
                to = handArrivalSlot(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.RECOVER,
                seconds = 0.7f,
                done = done,
            )

            is GameEvent.CardStolen -> {
                game.sound.play(SoundManager.Sfx.STEAL)
                val victim = spaceSlotFor(event.victim, event.letter)
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
                        val slash = director.duration(0.8f)
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
                    from = spaceSlotFor(event.victim, event.letter),
                    to = discardTarget(event.victim),
                    letter = event.letter,
                    sound = null,
                    seconds = 0.7f,
                    done = done,
                )
            }

            is GameEvent.TrapSet -> {
                game.sound.play(SoundManager.Sfx.TRAP_SET)
                pulse(handRect(event.on), Palette.school(Letter.T), done)
            }

            is GameEvent.TrapTriggered -> {
                game.sound.play(SoundManager.Sfx.TRAP_SNAP)
                flyFromHand(
                    side = event.side,
                    handIndex = event.handIndex,
                    to = discardTarget(event.side),
                    letter = event.letter.takeIf { event.side == Side.YOU },
                    sound = null,
                    seconds = 0.6f,
                    shake = true,
                    done = done,
                )
            }

            is GameEvent.HandOverflow -> flyFromHand(
                side = event.side,
                handIndex = event.handIndex,
                to = discardTarget(event.side),
                letter = event.letter.takeIf { event.side == Side.YOU },
                sound = SoundManager.Sfx.CARD_DISCARD,
                seconds = 0.45f,
                done = done,
            )

            // Карту F унесли в сброс — вместе с ней срывается и её флаг.
            is GameEvent.ForbidBroken -> {
                game.sound.play(SoundManager.Sfx.FORBID_TRIGGER)
                flashBanner(event.on)
                delay(0.3f, done)
            }

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

    /**
     * Прячет карту доски, стоящую в заданном месте.
     *
     * Возвращать видимость не нужно: сразу после анимации доска пересобирается.
     */
    private fun hideCardAt(rect: Rectangle) {
        val centerX = rect.x + rect.width / 2f
        val centerY = rect.y + rect.height / 2f
        val reach = rect.width * 0.6f
        cardGroup.children
            .filterIsInstance<CardActor>()
            .firstOrNull {
                it.isVisible &&
                    kotlin.math.abs(it.x + it.width / 2f - centerX) < reach &&
                    kotlin.math.abs(it.y + it.height / 2f - centerY) < reach
            }
            ?.isVisible = false
    }

    /**
     * Полёт карты, покидающей руку игрока.
     *
     * Стартовая точка берётся у карты с индексом из события, а не у первой карты
     * с такой буквой: одинаковых букв в руке бывает несколько, и поиск по букве
     * уносил со стола не ту карту, на которую нажали.
     */
    private fun flyFromHand(
        side: Side,
        handIndex: Int,
        to: Rectangle,
        letter: Letter?,
        sound: SoundManager.Sfx?,
        seconds: Float,
        shake: Boolean = false,
        done: () -> Unit,
    ) {
        val card = handCard(side, handIndex)
        val from = card?.let { Rectangle(it.x, it.y, it.width, it.height) } ?: handTarget(side)
        // Прячем именно этого актёра: в руке карты наезжают друг на друга, и поиск
        // по координате точки вылета может попасть в соседнюю.
        card?.isVisible = false
        flyCard(from, to, letter, sound, seconds, shake, hideSource = false, done = done)
    }

    /** Ghost-карта, летящая между зонами. Сами актёры доски не двигаются. */
    private fun flyCard(
        from: Rectangle,
        to: Rectangle,
        letter: Letter?,
        sound: SoundManager.Sfx?,
        seconds: Float,
        shake: Boolean = false,
        hideSource: Boolean = true,
        done: () -> Unit,
    ) {
        sound?.let(game.sound::play)
        val duration = director.duration(seconds)
        // Карта на доске, стоящая в точке вылета, прячется: без этого во время
        // полёта видны обе — оригинал в руке и летящий призрак.
        if (hideSource) hideCardAt(from)
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
        val duration = director.duration(0.45f)
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
        // Единственный показ того, какая именно школа сработала: держим дольше вспышки.
        val duration = director.duration(0.8f)
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
        // Пульс — единственный показ запрета и ловушки, поэтому он длиннее полёта карты.
        val duration = director.duration(0.55f)
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

    /**
     * Вспышка на месте флага запрета, висящего на стороне [victim].
     *
     * Флаг исчезает молча, при следующей пересборке доски, — а исчезает он ровно
     * тогда, когда взгляд занят летящей в сброс картой. Вспышка привязывает одно
     * к другому: стало видно, из-за чего карта улетела.
     */
    private fun flashBanner(victim: Side) {
        val rect = forbidBannerRect(victim)
        val letter = shownForbid[victim] ?: Letter.F
        val region = assets.vfxRegion("fx_burst_star") ?: return
        val duration = director.duration(0.5f)
        addGlow(
            region,
            rect.x + rect.width / 2f,
            rect.y + rect.height / 2f,
            rect.width * 3f,
            1f,
            Palette.rgba(Palette.school(letter), 1f).lerp(Color.WHITE, 0.5f),
        ) { actor ->
            actor.setScale(0.4f)
            actor.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.scaleTo(1.3f, 1.3f, duration, Interpolation.pow3Out),
                        Actions.fadeOut(duration, Interpolation.pow2In),
                    ),
                    Actions.run { actor.remove() },
                ),
            )
        }
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
        addForbidBanners()
        addHandCards()

        // Колода одна на обе стороны: рисуем её, пока хоть у кого-то остались карты.
        val deckAlive = Side.entries.any { state.side(it).deck.isNotEmpty() }
        if (deckAlive) {
            val rect = layout.deck
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
        val space = visualSpace(side)
        val slots = layout.spaceSlots(zone)
        Letter.ALL.forEachIndexed { index, letter ->
            val count = space.count { it == letter }
            if (count == 0) return@forEachIndexed
            val slot = slots[index]
            val card = CardActor(assets, letter)
            card.stackCount = count
            card.setBounds(slot.x, slot.y, slot.width, slot.height, centerOrigin = true)
            cardGroup.addActor(card)
        }
    }

    /** Флаги запрета — единственное, что висит на столе между ходами. */
    private fun addForbidBanners() {
        for (side in Side.entries) {
            shownForbid[side]?.let { addForbidBanner(side, it) }
        }
    }

    /**
     * Флаг у карты F стороны, наложившей запрет на [victim].
     *
     * Свою названную букву игрок видит, чужую — нет: скрытая информация здесь
     * часть правил (01-rules-spec §5.1), а не недоделка интерфейса.
     */
    private fun addForbidBanner(victim: Side, letter: Letter) {
        val rect = forbidBannerRect(victim)
        val banner = ForbidBanner(assets, letter.takeIf { victim == Side.AI }) { elapsedSeconds }
        banner.setBounds(rect.x, rect.y, rect.width, rect.height)
        banner.setOrigin(rect.width / 2f, rect.height)
        cardGroup.addActor(banner)
        if (forbidPopOn == victim) {
            forbidPopOn = null
            unfurl(banner)
        }
    }

    /**
     * Разворачивание флага: он появляется между ходами, и без движения его не заметить.
     * Растёт от кольца вниз — точка отсчёта у актёра стоит на верхней кромке.
     */
    private fun unfurl(actor: Actor) {
        actor.setScale(1f, 0f)
        actor.color.a = 0f
        actor.addAction(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, director.duration(0.4f), Interpolation.swingOut),
                Actions.fadeIn(director.duration(0.25f)),
            ),
        )
    }

    private fun addHandCards() {
        // Индекс карты уходит в команду движку, поэтому важно: отложенная рука
        // совпадает с настоящей, когда все анимации отыграли, — а разыграть карту
        // раньше и нельзя, обработчик клика ждёт простоя режиссёра.
        val hand = visualHand()
        val slots = layout.handSlots(hand.size)
        val playable = state.turn == Side.YOU && state.phase == Phase.AWAITING_PLAY && !state.isOver
        handActors.clear()
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
            handActors += card
            cardGroup.addActor(card)
        }
    }

    // ---------------------------------------------------------------- диалоги

    /**
     * Выход из партии: отложить или прервать.
     *
     * Без этого кнопка меню молча обрывала матч, и вернуться к нему было нельзя.
     * Третий пункт обязателен: кнопку нажимают и по ошибке.
     */
    private fun showPauseDialog() {
        if (pauseDialog != null || state.isOver) return
        val dialog = Group()
        dialog.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)

        val dim = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.dim(0.72f))
        dim.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)
        dialog.addActor(dim)

        val panelWidth = minOf(layout.worldWidth * 0.58f, layout.worldHeight * 0.9f)
        val buttonHeight = (layout.worldHeight * 0.085f).coerceIn(44f, 70f)
        val gap = layout.worldHeight * 0.02f
        val innerWidth = panelWidth * 0.8f

        // Заголовок и текст меряем до раскладки: высота панели считается по ним,
        // иначе кнопки вылезают за рамку, как только текст переносится на две строки.
        val title = Label(Strings["pause.title"], theme.title)
        title.setAlignment(com.badlogic.gdx.utils.Align.center)
        val body = overlay.wrapped(Strings["pause.body"])
        body.setAlignment(com.badlogic.gdx.utils.Align.center)
        body.setWidth(innerWidth)

        val panelHeight = gap * 3f + title.prefHeight + gap + body.prefHeight +
            gap * 1.6f + buttonHeight * 3f + gap * 2f + gap * 2.4f
        val panelX = (layout.worldWidth - panelWidth) / 2f
        val panelY = (layout.worldHeight - panelHeight) / 2f

        val frame = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.modalFrame)
        frame.setBounds(panelX, panelY, panelWidth, panelHeight)
        dialog.addActor(frame)

        title.setBounds(panelX, panelY + panelHeight - gap * 3f - title.prefHeight, panelWidth, title.prefHeight)
        dialog.addActor(title)

        body.setBounds(
            panelX + (panelWidth - innerWidth) / 2f, title.y - gap - body.prefHeight,
            innerWidth, body.prefHeight,
        )
        dialog.addActor(body)

        val actions = listOf<Pair<String, () -> Unit>>(
            "pause.save" to {
                SaveGame.save(state, elapsedSeconds)
                game.showMenu()
            },
            "pause.abandon" to {
                SaveGame.clear()
                game.showMenu()
            },
            "pause.resume" to { dismissPauseDialog() },
        )
        var buttonY = body.y - gap * 1.6f - buttonHeight
        for ((key, action) in actions) {
            val button = TextButton(Strings[key], theme.buttonCompact)
            button.label.setFontScale(PAUSE_BUTTON_SCALE)
            button.setBounds(panelX + (panelWidth - innerWidth) / 2f, buttonY, innerWidth, buttonHeight)
            button.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    game.sound.play(SoundManager.Sfx.UI_CLICK)
                    action()
                }
            })
            dialog.addActor(button)
            buttonY -= buttonHeight + gap
        }

        dialog.color.a = 0f
        dialog.addAction(Actions.fadeIn(0.18f))
        dialogGroup.addActor(dialog)
        pauseDialog = dialog
    }

    private fun dismissPauseDialog() {
        pauseDialog?.remove()
        pauseDialog = null
    }

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
        dialogGroup.addActor(dialog)
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
        val dim = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.dim(0.86f))
        dim.setBounds(0f, 0f, layout.worldWidth, layout.worldHeight)
        dialog.addActor(dim)

        // Рамка вокруг результата: без неё текст висит прямо на доске и выглядит недоделанным.
        val panelWidth = minOf(layout.worldWidth * 0.52f, layout.worldHeight * 0.95f)
        val panelHeight = layout.worldHeight * 0.46f
        val panelX = (layout.worldWidth - panelWidth) / 2f
        val panelY = (layout.worldHeight - panelHeight) / 2f
        val frame = com.badlogic.gdx.scenes.scene2d.ui.Image(theme.modalFrame)
        frame.setBounds(panelX, panelY, panelWidth, panelHeight)
        dialog.addActor(frame)

        val title = Label(Strings[if (won) "result.victory" else "result.defeat"], theme.titleLarge)
        title.setAlignment(com.badlogic.gdx.utils.Align.center)
        title.color = if (won) Palette.GOLD_LIGHT else Palette.TEXT_MUTED
        title.setBounds(panelX, panelY + panelHeight * 0.62f, panelWidth, title.prefHeight)
        dialog.addActor(title)

        val reasonKey = when (outcome.reason) {
            EndReason.FIRST_SET -> "result.firstSet"
            EndReason.FIVE_OF_A_KIND -> "result.fiveOfAKind"
            EndReason.DECK_OUT -> "result.deckOut"
        }
        val reason = Label(Strings[reasonKey], theme.body)
        reason.setAlignment(com.badlogic.gdx.utils.Align.center)
        reason.wrap = true
        reason.setBounds(
            panelX + panelWidth * 0.08f,
            panelY + panelHeight * 0.44f,
            panelWidth * 0.84f,
            reason.prefHeight,
        )
        dialog.addActor(reason)

        val buttonWidth = panelWidth * 0.38f
        val buttonHeight = (layout.worldHeight * 0.085f).coerceIn(44f, 70f)
        val buttonY = panelY + panelHeight * 0.13f
        val gap = panelWidth * 0.06f

        dialog.addActor(
            resultButton(Strings["common.newGame"], panelX + panelWidth / 2f - buttonWidth - gap / 2f, buttonY, buttonWidth, buttonHeight) {
                game.startGame()
            },
        )
        dialog.addActor(
            resultButton(Strings["common.menu"], panelX + panelWidth / 2f + gap / 2f, buttonY, buttonWidth, buttonHeight) {
                game.showMenu()
            },
        )

        dialog.color.a = 0f
        dialog.addAction(Actions.fadeIn(0.3f))
        dialogGroup.addActor(dialog)
    }

    private fun resultButton(
        text: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        action: () -> Unit,
    ): TextButton {
        val button = TextButton(text, theme.button)
        button.setBounds(x, y, width, height)
        // Высота подписи задаётся явно: по умолчанию Label просит
        // `capHeight - descent * 2`, у заголовочного шрифта это 71 при кнопке около 60 —
        // бокс не влезает и распирает таблицу. Плюс подъём прописных, которые Label
        // центрует ниже середины своего бокса (см. Theme.capSink).
        val label = button.label
        button.clearChildren()
        label.setAlignment(com.badlogic.gdx.utils.Align.center)
        button.add(label)
            .height(height * 0.5f)
            .expand()
            .center()
            .padBottom(theme.capSink(label) * 2f)
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.sound.play(SoundManager.Sfx.UI_CLICK)
                action()
            }
        })
        return button
    }

    // ------------------------------------------------------------- координаты

    /**
     * Куда прилетает сброшенная карта.
     *
     * Панель сброса — это целый прямоугольник, и полёт в неё растягивал карту до
     * её габаритов: в портрете карта на мгновение раздувалась во всю ширину экрана.
     * Летим в карточное пятно внутри панели.
     */
    private fun discardTarget(side: Side): Rectangle {
        val panel = discardRect(side)
        val height = minOf(layout.cardHeight * 0.55f, panel.height * 0.7f)
        val width = height * (2f / 3f)
        return Rectangle(
            panel.x + panel.width / 2f - width / 2f,
            panel.y + (panel.height - height) / 2f,
            width, height,
        )
    }

    /** Колода одна на обе стороны, поэтому сторона на её положение не влияет. */
    private fun deckRect(side: Side): Rectangle = layout.deck

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

    /**
     * Гнездо буквы в зоне SPACE — туда карта и приземлится после пересборки доски.
     * Полёт в центр зоны выглядел как «карта уехала не туда, а потом прыгнула».
     */
    private fun spaceSlotFor(side: Side, letter: Letter): Rectangle {
        val zone = if (side == Side.AI) layout.aiSpace else layout.youSpace
        val index = Letter.ALL.indexOf(letter)
        return layout.spaceSlots(zone).getOrNull(index) ?: spaceTarget(side)
    }

    /**
     * Куда приземляется приходящая в руку карта: последнее гнездо текущей раскладки.
     *
     * Точнее не получится — движок к моменту анимации уже применил всю пачку
     * событий, и промежуточных размеров руки не осталось.
     */
    private fun handArrivalSlot(side: Side): Rectangle {
        if (side == Side.AI) return handTarget(side)
        val hand = state.you.hand
        if (hand.isEmpty()) return handTarget(side)
        return layout.handSlots(hand.size).last()
    }

    /** Место флага: полоса слева от гнезда F в SPACE стороны, наложившей запрет. */
    private fun forbidBannerRect(victim: Side): Rectangle {
        val caster = victim.other
        return layout.forbidBannerSlot(if (caster == Side.AI) layout.aiSpace else layout.youSpace)
    }

    /**
     * Карта руки игрока по её месту в руке; null — если карты там уже нет.
     *
     * Рука ИИ не разложена по карте на карту, поэтому для неё показывать нечего.
     */
    private fun handCard(side: Side, index: Int): CardActor? =
        if (side == Side.AI) null else handActors.getOrNull(index)?.takeIf { it.isVisible }


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
            // Резной разделитель обозначает границу сторон там, где зоны идут
            // одна над другой. В ландшафте они и так разнесены, там его нет.
            if (layout.divider.width > 0f) {
                assets.uiRegion("divider_ornament")?.let { ornament ->
                    val rect = layout.divider
                    // Рисуем в натуральных пропорциях по центру: растянутый во всю
                    // ширину орнамент вырождается в бледную черту.
                    val height = rect.height
                    val width = minOf(rect.width, height * ornament.regionWidth / ornament.regionHeight)
                    batch.draw(
                        ornament, rect.x + (rect.width - width) / 2f, rect.y, width, height,
                    )
                }
            }
            batch.setColor(Color.WHITE)
        }

        /** Полноэкранный фон стола под всей раскладкой. */
        private fun drawTable(batch: Batch) {
            val texture = assets.background(
                if (layout.portrait) "bg_table_portrait" else "bg_table_landscape",
            ) ?: return
            batch.setColor(Color.WHITE)
            batch.drawCover(texture, layout.worldWidth, layout.worldHeight)
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

        /**
         * Границы счётчиков в координатах сцены, заполняются при отрисовке.
         *
         * Позиции считаются по ширине чисел, а она меняется на ходу, поэтому
         * готовых прямоугольников заранее нет — запоминаем те, что нарисовали.
         */
        private val counterBounds = linkedMapOf<String, Rectangle>()

        init {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    val stageX = this@HudOverlay.x + x
                    val stageY = this@HudOverlay.y + y
                    val hit = counterBounds.entries.firstOrNull { it.value.contains(stageX, stageY) }
                        ?: return
                    game.sound.play(SoundManager.Sfx.UI_CLICK)
                    showHudHint("hud.hint.${hit.key}", hit.value)
                }
            })
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            val body = assets.bodyFont
            val title = assets.titleFont
            val hud = layout.hud

            val turnText = when {
                state.isOver -> Strings["hud.gameOver"]
                state.turn == Side.YOU -> Strings["hud.yourTurn"]
                else -> Strings["hud.opponentTurn"]
            }
            title.color = if (state.turn == Side.YOU) Palette.GOLD_LIGHT else Palette.TEXT_MUTED
            title.draw(batch, turnText, hud.x + 16f, hud.y + hud.height * 0.78f)
            title.color = Color.WHITE

            body.color = Palette.TEXT
            val iconSize = hud.height * 0.52f
            val capTop = hud.y + hud.height * 0.74f
            // В libGDX y при отрисовке — верх прописных, а не базовая линия. Середина
            // цифр лежит на capHeight/2 ниже, по ней и центруется иконка.
            val iconY = capTop - body.capHeight / 2f - iconSize / 2f

            val timer = formatTime(elapsedSeconds)
            glyphs.setText(body, timer)
            var x = hud.x + hud.width - glyphs.width - hud.height * 1.35f
            body.draw(batch, timer, x, capTop)
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
            counterBounds.clear()
            for ((icon, value) in counters) {
                val start = x
                assets.icon(icon)?.let {
                    batch.setColor(Color.WHITE)
                    batch.draw(it, x, iconY, iconSize, iconSize)
                    x += iconSize + gap * 0.4f
                }
                body.draw(batch, value, x, capTop)
                glyphs.setText(body, value)
                x += glyphs.width + gap * 1.6f
                // Зона нажатия — иконка вместе со своим числом, на всю высоту строки.
                counterBounds[icon] = Rectangle(start, hud.y, x - start, hud.height)
            }

            drawDiscard(batch, layout.aiDiscard, Side.AI)
            drawDiscard(batch, layout.youDiscard, Side.YOU)
            body.color = Color.WHITE
        }

        /**
         * Сброс стороны. Панель стоит вплотную к своему ряду, поэтому подпись
         * короткая: чей это сброс, видно по положению, а не по тексту.
         *
         * Форма панели зависит от ориентации — в landscape она узкая и высокая,
         * в portrait широкая и низкая, — поэтому миниатюры раскладываются в сетку,
         * число колонок в которой считается от пропорций самой панели.
         */
        private fun drawDiscard(batch: Batch, rect: Rectangle, side: Side) {
            val body = assets.bodyFont
            val discard = visualDiscard(side)
            val padX = rect.width * 0.07f
            val padY = rect.height * 0.09f

            val captionHeight = rect.height * 0.16f
            val captionScale = (captionHeight * 0.62f / body.capHeight).coerceAtMost(0.8f)
            body.data.setScale(captionScale)
            body.color = Palette.TEXT_MUTED
            glyphs.setText(body, Strings["hud.discard"])
            body.draw(
                batch, glyphs,
                rect.x + (rect.width - glyphs.width) / 2f,
                rect.y + rect.height - padY,
            )
            body.data.setScale(1f)

            val contentTop = rect.y + rect.height - captionHeight - padY
            val contentHeight = (contentTop - rect.y - padY).coerceAtLeast(16f)

            val entries = Letter.ALL.map { it to discard.count { card -> card == it } }.filter { it.second > 0 }
            if (entries.isEmpty()) {
                body.color = Palette.TEXT
                return
            }

            // В узкой высокой панели миниатюры идут в два ряда, в широкой низкой — в один.
            // Урны здесь больше нет: она занимала половину панели и ничего не сообщала,
            // а место нужно самим сброшенным картам.
            val available = rect.width - padX * 2f
            val rows = if (rect.height > rect.width) 2 else 1
            val columns = ((entries.size + rows - 1) / rows).coerceAtLeast(1)
            val gap = 0.12f
            val byWidth = available / (columns + (columns - 1) * gap)
            val byHeight = contentHeight / (rows + (rows - 1) * gap) * (2f / 3f)
            val width = minOf(byWidth, byHeight).coerceAtLeast(8f)
            val height = width * (3f / 2f)

            // Сетка миниатюр центруется в панели, как и подпись над ней.
            val usedRows = ((entries.size + columns - 1) / columns).coerceAtLeast(1)
            val gridWidth = columns * width + (columns - 1) * width * gap
            val gridHeight = usedRows * height + (usedRows - 1) * height * gap
            val gridX = rect.x + (rect.width - gridWidth) / 2f
            val gridTop = contentTop - (contentHeight - gridHeight) / 2f

            for ((index, entry) in entries.withIndex()) {
                val (letter, count) = entry
                val column = index % columns
                val row = index / columns
                val x = gridX + column * width * (1f + gap)
                val y = gridTop - height - row * height * (1f + gap)
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
            }
            body.color = Palette.TEXT
        }


        private fun formatTime(seconds: Float): String {
            val total = seconds.toInt()
            return "%02d:%02d".format(total / 60, total % 60)
        }
    }

    /**
     * Отложенное изменение руки игрока.
     *
     * Хранится позиция, а не только буква: одинаковых букв в руке бывает несколько,
     * и по букве нельзя понять, какая из них ушла — а от этого зависит и порядок
     * оставшихся карт, и то, какая карта вылетит в анимации.
     */
    private sealed interface HandOp {
        val letter: Letter

        /** Карта пришла в руку. Движок всегда кладёт её в конец. */
        data class Added(override val letter: Letter) : HandOp

        /** Карта ушла из руки с места [index] — индекс в руке до этого события. */
        data class Removed(val index: Int, override val letter: Letter) : HandOp
    }

    private companion object {
        const val WORLD_WIDTH = 1280f
        const val WORLD_HEIGHT = 720f
        const val AI_THINK_TIME = 0.65f

        /** Паузы между событиями, в секундах до умножения на настройку скорости. */
        const val BEAT_SHORT = 0.12f
        const val BEAT_PLAY = 0.22f
        const val BEAT_ABILITY = 0.5f

        /** Длительность стартовой раздачи: все карты летят одновременно. */
        const val DEAL_TIME = 0.6f
        const val HOVER_LIFT = 14f

        /** Масштаб шрифта в подсказке счётчика относительно обычного текста. */
        const val HUD_HINT_FONT = 0.72f

        /** Сколько держится подсказка по счётчику, прежде чем растаять. */
        const val HUD_HINT_TIME = 4f

        /** Подписи на кнопках паузы длинные, поэтому мельче обычных. */
        const val PAUSE_BUTTON_SCALE = 0.72f

        /** Подпись на кнопке правил мельче обычной: кнопка узкая и служебная. */
        const val RULES_BUTTON_SCALE = 0.62f

    }
}
