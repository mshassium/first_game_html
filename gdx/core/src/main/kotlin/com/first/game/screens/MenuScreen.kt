package com.first.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Interpolation
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
import com.first.game.SaveGame
import com.first.game.audio.SoundManager
import com.first.game.domain.Letter
import com.first.game.domain.ai.Difficulty
import com.first.game.i18n.Strings
import com.first.game.ui.CardActor
import com.first.game.ui.drawCover
import com.first.game.ui.Overlay
import com.first.game.ui.Palette
import ktx.app.KtxScreen

/** Главное меню: логотип, веер карт, кнопки. Правила и настройки открываются поверх. */
class MenuScreen(private val game: FirstGame) : KtxScreen {

    private val stage = Stage(ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT), game.batch)
    private val theme = game.theme
    private val overlay = Overlay(stage, theme, game.assets, game.sound)
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
        when (game.bootOverlay) {
            "rules" -> overlay.showRules()
            "settings" -> showSettings()
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        game.sound.playMusic(SoundManager.Track.MENU)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.sound.update(delta)

        // Фон рисуем до сцены — он полноэкранный и ни с чем не взаимодействует.
        menuBackground()?.let { texture ->
            game.batch.projectionMatrix = stage.viewport.camera.combined
            game.batch.begin()
            game.batch.setColor(1f, 1f, 1f, 1f)
            game.batch.drawCover(texture, stage.viewport.worldWidth, stage.viewport.worldHeight)
            game.batch.end()
        }

        stage.act(delta)
        stage.draw()
    }

    /** В вертикальной ориентации свой фон: горизонтальный пришлось бы обрезать втрое. */
    private fun menuBackground() =
        if (stage.viewport.worldHeight > stage.viewport.worldWidth) {
            game.assets.background("bg_menu_portrait") ?: game.assets.background("bg_menu")
        } else {
            game.assets.background("bg_menu")
        }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        buildMenu()
    }

    override fun dispose() = stage.dispose()

    private fun buildMenu() {
        root.clear()
        root.setFillParent(true)
        // Фон нарисован так, что левая треть тёмная и пустая — меню становится там.
        if (game.assets.background("bg_menu") != null) root.left().padLeft(stage.viewport.worldWidth * 0.06f)
        else root.center()

        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight
        // Веер, заголовок и четыре кнопки должны помещаться по высоте целиком.
        val cardHeight = (worldHeight * 0.17f).coerceIn(56f, 130f)
        val cardWidth = cardHeight / 1.5f
        val buttonHeight = (worldHeight * 0.085f).coerceIn(44f, 70f)
        val buttonWidth = (worldWidth * 0.26f).coerceIn(200f, 380f)

        // Эмблема Ордена над заголовком.
        val emblem = game.assets.uiRegion("emblem_first")?.let { region ->
            val height = worldHeight * 0.20f
            Image(region) to (height * region.regionWidth / region.regionHeight to height)
        }

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
        if (emblem != null) {
            val (image, size) = emblem
            root.add(image).size(size.first, size.second).padBottom(gap * 0.5f).row()
        } else {
            root.add(fan).padBottom(gap).row()
        }
        root.add(title).padBottom(2f).row()
        root.add(subtitle).padBottom(gap).row()

        val buttons = mutableListOf<Triple<String, String, () -> Unit>>()
        // «Продолжить» появляется только когда есть что продолжать.
        if (SaveGame.exists) {
            buttons += Triple(Strings["menu.continue"], "restart") { game.continueGame() }
        }
        buttons += listOf(
            Triple(Strings["menu.play"], "duel") { confirmNewGame() },
            Triple(Strings["menu.rules"], "rules") { overlay.showRules() },
            Triple(Strings["menu.settings"], "settings") { showSettings() },
            // Подпись — только название языка: что это переключатель, говорит иконка флажков.
            Triple(Strings.language.label, "lang") { toggleLanguage() },
        )
        for ((text, icon, action) in buttons) {
            root.add(menuButton(text, icon, action))
                .size(buttonWidth, buttonHeight).padBottom(gap).row()
        }
    }

    /**
     * Новая игра поверх отложенной партии спрашивает подтверждение: сохранение
     * одно, и старт новой стирает его безвозвратно.
     */
    private fun confirmNewGame() {
        if (!SaveGame.exists) {
            game.startGame()
            return
        }
        overlay.show(Strings["newgame.title"]) { content, page ->
            val width = page.width
            val gap = stage.viewport.worldHeight * 0.02f
            content.add(overlay.wrapped(Strings["newgame.body"])).width(width).left().padTop(gap).row()
            val buttonHeight = (stage.viewport.worldHeight * 0.085f).coerceIn(44f, 70f)
            for ((key, action) in listOf<Pair<String, () -> Unit>>(
                "newgame.confirm" to { game.startGame() },
                "newgame.cancel" to { overlay.close() },
            )) {
                content.add(menuButton(Strings[key], null, action))
                    .size(width * 0.86f, buttonHeight).padTop(gap * 1.5f).row()
            }
        }
    }

    private fun menuButton(text: String, action: () -> Unit): TextButton = menuButton(text, null, action)

    /**
     * Кнопка меню; [icon] — имя иконки без префикса, если она нарисована.
     *
     * Высота подписи задаётся явно. По умолчанию Label просит
     * `capHeight - descent * 2`, у нашего заголовочного шрифта это 71 px при
     * высоте кнопки 61: бокс не влезает, строка распирает таблицу, содержимое
     * выходит за кнопку — иконка упирается в верхний кант, а текст садится ниже неё.
     * С общей высотой ячеек иконка и прописные буквы центрируются по одной оси.
     */
    private fun menuButton(text: String, icon: String?, action: () -> Unit): TextButton {
        val button = TextButton(text, theme.button)
        val label = button.label
        val size = (stage.viewport.worldHeight * 0.055f).coerceIn(28f, 44f)
        val region = game.assets.icon(icon ?: "")

        button.clearChildren()
        label.setAlignment(Align.center)
        if (region != null) {
            // Иконка и подпись лежат в разных слоях: иконка прижата к левому краю,
            // подпись центрируется по всей ширине кнопки. Если положить их в одну
            // строку таблицы, подпись центруется по остатку после иконки — и уезжает
            // вправо тем сильнее, чем шире иконка.
            button.pad(0f, size * 0.28f, 0f, size * 0.28f)
            // Отступ иконки задаётся внутри её слоя, а не полем кнопки: поле сдвинуло бы
            // и область, по которой центруется подпись.
            val iconLayer = Table().apply {
                add(Image(region)).size(size).expandX().left().padLeft(size * ICON_INSET)
            }
            val textLayer = Table().apply {
                add(label).height(size).expand().center().padBottom(capSink(label) * 2f)
            }
            button.stack(textLayer, iconLayer).grow()
        } else {
            // Кнопки настроек без иконок: подпись просто по центру.
            button.pad(0f, size * 0.5f, 0f, size * 0.5f)
            button.add(label).height(size).expandX().padBottom(capSink(label) * 2f)
        }

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.sound.play(SoundManager.Sfx.UI_CLICK)
                action()
            }
        })
        return button
    }


    private fun capSink(label: Label): Float = theme.capSink(label)

    private fun toggleLanguage() {
        val next = Strings.nextLanguage()
        GamePrefs.language = next
        Strings.load(next)
        buildMenu()
    }

    // ------------------------------------------------------------- оверлеи

    /**
     * Оверлей поверх меню: затемнение, рамка, заголовок и прокручиваемое содержимое.
     *
     * Ширина ячеек задаётся явно. Без этого Table укладывает переносимый текст
     * по его минимальной ширине — то есть в один символ на строку.
     */

    private fun showSettings(): Unit = overlay.show(Strings["settings.title"]) { content, page ->
        val width = page.width
        val buttonWidth = width * 0.86f
        val buttonHeight = (stage.viewport.worldHeight * 0.085f).coerceIn(44f, 70f)
        val gap = stage.viewport.worldHeight * 0.02f

        val rows = listOf<Pair<String, () -> Unit>>(
            difficultyLabel() to { GamePrefs.difficulty = nextDifficulty(); showSettings() },
            speedLabel() to { GamePrefs.animationSpeed = nextSpeed(); showSettings() },
            "${Strings["settings.music"]}: ${percent(GamePrefs.musicVolume)}" to {
                GamePrefs.musicVolume = cycleVolume(GamePrefs.musicVolume)
                game.sound.applyVolumes()
                showSettings()
            },
            "${Strings["settings.sfx"]}: ${percent(GamePrefs.sfxVolume)}" to {
                GamePrefs.sfxVolume = cycleVolume(GamePrefs.sfxVolume)
                showSettings()
            },
        )
        for ((text, action) in rows) {
            content.add(menuButton(text, null, action))
                .size(buttonWidth, buttonHeight).padTop(gap).row()
        }
    }

    /** Ширину переносимой надписи задаёт ячейка таблицы, а не сам актёр. */

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


        /** Насколько иконка кнопки отступает от края, в долях своего размера. */
        const val ICON_INSET = 0.35f
    }
}
