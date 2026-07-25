package com.first.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.first.game.AnimationSpeed
import com.first.game.FirstGame
import com.first.game.GamePrefs
import com.first.game.audio.SoundManager
import com.first.game.domain.Letter
import com.first.game.domain.ai.Difficulty
import com.first.game.i18n.Strings
import com.first.game.ui.CardActor
import com.first.game.ui.Palette
import ktx.app.KtxScreen

/** Главное меню: логотип, веер карт, кнопки. Правила и настройки открываются поверх. */
class MenuScreen(private val game: FirstGame) : KtxScreen {

    private val stage = Stage(ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT), game.batch)
    private val theme = game.theme
    private var overlay: Group? = null
    private val root = Table()

    init {
        stage.addActor(root)
        stage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                game.sound.unlock()
                return false
            }
        })
        buildMenu()
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        game.sound.playMusic(SoundManager.Track.MENU)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        buildMenu()
    }

    override fun dispose() = stage.dispose()

    private fun buildMenu() {
        root.clear()
        root.setFillParent(true)
        root.center()

        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight
        // Веер, заголовок и четыре кнопки должны помещаться по высоте целиком.
        val cardHeight = (worldHeight * 0.17f).coerceIn(56f, 130f)
        val cardWidth = cardHeight / 1.5f
        val buttonHeight = (worldHeight * 0.085f).coerceIn(44f, 70f)
        val buttonWidth = (worldWidth * 0.26f).coerceIn(200f, 380f)

        val fan = Table()
        Letter.ALL.forEachIndexed { index, letter ->
            val card = CardActor(game.assets, letter)
            card.setSize(cardWidth, cardHeight)
            card.setOrigin(cardWidth / 2f, cardHeight / 2f)
            card.rotation = (index - 2) * 5f
            // Едва заметное дыхание: карты по очереди приподнимаются.
            card.addAction(
                Actions.forever(
                    Actions.sequence(
                        Actions.delay(index * 0.35f),
                        Actions.moveBy(0f, 8f, 0.9f, Interpolation.sine),
                        Actions.moveBy(0f, -8f, 0.9f, Interpolation.sine),
                        Actions.delay((Letter.ALL.size - index) * 0.35f),
                    ),
                ),
            )
            fan.add(card).size(cardWidth, cardHeight).padLeft(if (index == 0) 0f else cardWidth * 0.12f)
        }

        val title = Label(Strings["app.title"], theme.titleLarge).apply {
            color = Palette.GOLD_LIGHT
            setAlignment(Align.center)
        }
        val subtitle = Label(Strings["app.subtitle"], theme.bodyMuted).apply { setAlignment(Align.center) }

        val gap = worldHeight * 0.016f
        root.add(fan).padBottom(gap).row()
        root.add(title).padBottom(2f).row()
        root.add(subtitle).padBottom(gap).row()

        val buttons = listOf<Pair<String, () -> Unit>>(
            Strings["menu.play"] to { game.startGame() },
            Strings["menu.rules"] to { showRules() },
            Strings["menu.settings"] to { showSettings() },
            "${Strings["menu.language"]}: ${Strings.language.label}" to { toggleLanguage() },
        )
        for ((text, action) in buttons) {
            root.add(menuButton(text, action)).size(buttonWidth, buttonHeight).padBottom(gap).row()
        }
    }

    private fun menuButton(text: String, action: () -> Unit): TextButton {
        val button = TextButton(text, theme.button)
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.sound.play(SoundManager.Sfx.UI_CLICK)
                action()
            }
        })
        return button
    }

    private fun toggleLanguage() {
        val next = Strings.nextLanguage()
        GamePrefs.language = next
        Strings.load(next)
        buildMenu()
    }

    // ------------------------------------------------------------- оверлеи

    private fun showOverlay(build: (Table) -> Unit) {
        closeOverlay()
        val group = Group()
        group.setBounds(0f, 0f, stage.viewport.worldWidth, stage.viewport.worldHeight)

        val dim = Image(theme.dim(0.85f))
        dim.setBounds(0f, 0f, group.width, group.height)
        group.addActor(dim)

        val content = Table()
        content.setBounds(
            group.width * 0.08f,
            group.height * 0.08f,
            group.width * 0.84f,
            group.height * 0.84f,
        )
        content.top()
        build(content)
        group.addActor(content)

        group.color.a = 0f
        group.addAction(Actions.fadeIn(0.2f))
        stage.addActor(group)
        overlay = group
    }

    private fun closeOverlay() {
        overlay?.remove()
        overlay = null
    }

    private fun showRules(): Unit = showOverlay { content ->
        content.add(Label(Strings["rules.title"], theme.title)).padBottom(16f).row()
        val sections = listOf(
            "rules.goal.title" to "rules.goal.body",
            "rules.turn.title" to "rules.turn.body",
        )
        for ((titleKey, bodyKey) in sections) {
            content.add(Label(Strings[titleKey], theme.bodyBold)).left().padTop(8f).row()
            content.add(wrapped(Strings[bodyKey], content.width)).left().row()
        }
        content.add(Label(Strings["rules.cards.title"], theme.bodyBold)).left().padTop(8f).row()
        for (letter in Letter.ALL) {
            val label = wrapped(Strings["rules.card.${letter.name}"], content.width)
            label.color = Palette.school(letter)
            content.add(label).left().row()
        }
        content.add(Label(Strings["rules.limits.title"], theme.bodyBold)).left().padTop(8f).row()
        content.add(wrapped(Strings["rules.limits.body"], content.width)).left().row()
        content.add(menuButton(Strings["common.back"]) { closeOverlay() }).padTop(16f).row()
    }

    private fun showSettings(): Unit = showOverlay { content ->
        content.add(Label(Strings["settings.title"], theme.title)).padBottom(16f).row()

        content.add(
            menuButton(difficultyLabel()) {
                GamePrefs.difficulty = nextDifficulty()
                showSettings()
            },
        ).padBottom(8f).row()

        content.add(
            menuButton(speedLabel()) {
                GamePrefs.animationSpeed = nextSpeed()
                showSettings()
            },
        ).padBottom(8f).row()

        content.add(
            menuButton("${Strings["settings.music"]}: ${percent(GamePrefs.musicVolume)}") {
                GamePrefs.musicVolume = cycleVolume(GamePrefs.musicVolume)
                game.sound.applyVolumes()
                showSettings()
            },
        ).padBottom(8f).row()

        content.add(
            menuButton("${Strings["settings.sfx"]}: ${percent(GamePrefs.sfxVolume)}") {
                GamePrefs.sfxVolume = cycleVolume(GamePrefs.sfxVolume)
                showSettings()
            },
        ).padBottom(8f).row()

        content.add(menuButton(Strings["common.back"]) { closeOverlay() }).padTop(16f).row()
    }

    private fun wrapped(text: String, width: Float): Label {
        val label = Label(text, theme.body)
        label.wrap = true
        label.width = width
        label.setAlignment(Align.left)
        return label
    }

    private fun difficultyLabel(): String {
        val key = when (GamePrefs.difficulty) {
            Difficulty.EASY -> "difficulty.easy"
            Difficulty.NORMAL -> "difficulty.normal"
            Difficulty.HARD -> "difficulty.hard"
        }
        return "${Strings["settings.difficulty"]}: ${Strings[key]}"
    }

    private fun nextDifficulty(): Difficulty {
        val values = Difficulty.entries
        return values[(values.indexOf(GamePrefs.difficulty) + 1) % values.size]
    }

    private fun speedLabel(): String =
        "${Strings["settings.speed"]}: ${Strings[GamePrefs.animationSpeed.labelKey]}"

    private fun nextSpeed(): AnimationSpeed {
        val values = AnimationSpeed.entries
        return values[(values.indexOf(GamePrefs.animationSpeed) + 1) % values.size]
    }

    private fun cycleVolume(current: Float): Float {
        val steps = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        val index = steps.indexOfFirst { it >= current - 0.01f }.coerceAtLeast(0)
        return steps[(index + 1) % steps.size]
    }

    private fun percent(value: Float): String =
        if (value <= 0f) Strings["common.off"] else "${(value * 100).toInt()}%"

    private companion object {
        const val WORLD_WIDTH = 1280f
        const val WORLD_HEIGHT = 720f
    }
}
