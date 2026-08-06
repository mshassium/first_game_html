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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
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
        when (game.bootOverlay) {
            "rules" -> showRules()
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
        game.assets.background("bg_menu")?.let { texture ->
            game.batch.projectionMatrix = stage.viewport.camera.combined
            game.batch.begin()
            game.batch.setColor(1f, 1f, 1f, 1f)
            game.batch.draw(texture, 0f, 0f, stage.viewport.worldWidth, stage.viewport.worldHeight)
            game.batch.end()
        }

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

        val buttons = listOf<Triple<String, String, () -> Unit>>(
            Triple(Strings["menu.play"], "duel") { game.startGame() },
            Triple(Strings["menu.rules"], "rules") { showRules() },
            Triple(Strings["menu.settings"], "settings") { showSettings() },
            // Подпись — только название языка: что это переключатель, говорит иконка флажков.
            Triple(Strings.language.label, "lang") { toggleLanguage() },
        )
        for ((text, icon, action) in buttons) {
            root.add(menuButton(text, icon, action))
                .size(buttonWidth, buttonHeight).padBottom(gap).row()
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
            // Подпись приподнята: у прописных букв нет нижних выносов, и геометрический
            // центр бокса зрительно читается как посадка на нижний кант.
            val textLayer = Table().apply {
                add(label).height(size).expand().center().padBottom(size * LABEL_LIFT)
            }
            button.stack(textLayer, iconLayer).grow()
        } else {
            // Кнопки настроек без иконок: подпись просто по центру.
            button.pad(0f, size * 0.5f, 0f, size * 0.5f)
            button.add(label).height(size).expandX()
        }

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

    /**
     * Оверлей поверх меню: затемнение, рамка, заголовок и прокручиваемое содержимое.
     *
     * Ширина ячеек задаётся явно. Без этого Table укладывает переносимый текст
     * по его минимальной ширине — то есть в один символ на строку.
     */
    private fun showOverlay(title: String, build: (Table, Float) -> Unit) {
        closeOverlay()
        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight

        val group = Group()
        group.setBounds(0f, 0f, worldWidth, worldHeight)

        val dim = Image(theme.dim(0.88f))
        dim.setBounds(0f, 0f, worldWidth, worldHeight)
        group.addActor(dim)

        val panelWidth = minOf(worldWidth * 0.62f, worldHeight * 1.15f)
        val panelHeight = worldHeight * 0.86f
        val panelX = (worldWidth - panelWidth) / 2f
        val panelY = (worldHeight - panelHeight) / 2f

        val frame = Image(theme.modalFrame)
        frame.setBounds(panelX, panelY, panelWidth, panelHeight)
        group.addActor(frame)

        val innerPad = panelWidth * 0.08f
        val contentWidth = panelWidth - innerPad * 2f

        val header = Label(title, theme.title)
        header.setAlignment(Align.center)
        header.setBounds(panelX, panelY + panelHeight - innerPad - header.prefHeight, panelWidth, header.prefHeight)
        group.addActor(header)

        val body = Table()
        body.top()
        build(body, contentWidth)

        val scroll = object : ScrollPane(body, ScrollPane.ScrollPaneStyle()) {
            /**
             * Шаг колеса по умолчанию — около 22% высоты области за щелчок. Панель
             * правил невысокая, и такой шаг проматывает почти экран разом: текст
             * перескакивает, а не едет.
             */
            override fun getMouseWheelY(): Float = super.getMouseWheelY() * WHEEL_STEP_SCALE
        }
        scroll.setFadeScrollBars(false)
        scroll.setScrollingDisabled(true, false)
        scroll.setOverscroll(false, false)
        val scrollTop = header.y - innerPad * 0.4f
        scroll.setBounds(panelX + innerPad, panelY + innerPad, contentWidth, scrollTop - panelY - innerPad)
        group.addActor(scroll)
        stage.scrollFocus = scroll

        // Круглая кнопка закрытия в углу панели.
        game.assets.icon("close")?.let { icon ->
            val close = com.badlogic.gdx.scenes.scene2d.ui.Button(
                com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(game.assets.roundButtonUp),
                com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(game.assets.roundButtonDown),
            )
            close.add(Image(icon)).grow()
            val size = worldHeight * 0.085f
            close.setBounds(
                panelX + panelWidth - size * 0.55f,
                panelY + panelHeight - size * 0.55f,
                size,
                size,
            )
            close.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    game.sound.play(SoundManager.Sfx.UI_CLICK)
                    closeOverlay()
                }
            })
            group.addActor(close)
        }

        group.color.a = 0f
        group.addAction(Actions.fadeIn(0.2f))
        stage.addActor(group)
        overlay = group
    }

    private fun closeOverlay() {
        overlay?.remove()
        overlay = null
    }

    private fun showRules(): Unit = showOverlay(Strings["rules.title"]) { content, width ->
        val gap = stage.viewport.worldHeight * 0.012f

        fun section(titleKey: String, bodyKey: String) {
            content.add(Label(Strings[titleKey], theme.bodyBold).apply { color = Palette.GOLD_LIGHT })
                .width(width).left().padTop(gap * 1.6f).row()
            content.add(wrapped(Strings[bodyKey])).width(width).left().padTop(gap * 0.4f).row()
        }

        section("rules.goal.title", "rules.goal.body")
        section("rules.turn.title", "rules.turn.body")

        content.add(Label(Strings["rules.cards.title"], theme.bodyBold).apply { color = Palette.GOLD_LIGHT })
            .width(width).left().padTop(gap * 1.6f).row()
        for (letter in Letter.ALL) {
            val row = Table()
            val card = CardActor(game.assets, letter)
            val cardHeight = width * 0.13f
            row.add(card).size(cardHeight * (2f / 3f), cardHeight).padRight(gap * 1.5f).top()
            row.add(wrapped(Strings["rules.card.${letter.name}"])).width(width - cardHeight * (2f / 3f) - gap * 1.5f).left()
            content.add(row).width(width).left().padTop(gap).row()
        }

        section("rules.limits.title", "rules.limits.body")
        content.add(Label("", theme.body)).height(gap * 3f).row()
    }

    private fun showSettings(): Unit = showOverlay(Strings["settings.title"]) { content, width ->
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
    private fun wrapped(text: String): Label {
        val label = Label(text, theme.body)
        label.wrap = true
        label.setAlignment(Align.topLeft)
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

        /** Доля от штатного шага колеса в прокручиваемых панелях правил и настроек. */
        const val WHEEL_STEP_SCALE = 0.45f

        /** Насколько иконка кнопки отступает от края, в долях своего размера. */
        const val ICON_INSET = 0.35f

        /** Насколько подпись приподнята над геометрическим центром, в долях иконки. */
        const val LABEL_LIFT = 0.12f
    }
}
