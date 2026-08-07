package com.first.game.ui

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.first.game.assets.Assets
import com.first.game.audio.SoundManager
import com.first.game.domain.Letter
import com.first.game.i18n.Strings

/**
 * Модальное окно поверх экрана: затемнение, рамка, заголовок, прокрутка и закрытие.
 *
 * Вынесено из меню, потому что правила теперь открываются и со стола. Держать
 * два описания одних и тех же карт в двух экранах — гарантированный рассинхрон.
 */
class Overlay(
    private val stage: Stage,
    private val theme: Theme,
    private val assets: Assets,
    private val sound: SoundManager,
) {

    private var current: Group? = null

    val isOpen: Boolean get() = current != null

    fun close() {
        current?.remove()
        current = null
    }

    /**
     * Область под содержимое окна и подобранный масштаб шрифта.
     *
     * [scale] нужен страницам, которые обязаны помещаться целиком: их строят
     * дважды — сначала в натуральную величину, потом с уменьшением, если не влезли.
     */
    class Page(val width: Float, val height: Float, val scale: Float)

    fun show(
        title: String,
        /** Содержимое обязано поместиться без прокрутки: подберём масштаб. */
        fitToPage: Boolean = false,
        build: (content: Table, page: Page) -> Unit,
    ) {
        close()
        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight

        val group = Group()
        group.setBounds(0f, 0f, worldWidth, worldHeight)

        val dim = Image(theme.dim(0.88f))
        dim.setBounds(0f, 0f, worldWidth, worldHeight)
        group.addActor(dim)

        // Странице, которая обязана поместиться целиком, отдаём больше места.
        val panelWidth = if (fitToPage) {
            worldWidth * 0.94f
        } else {
            minOf(worldWidth * 0.62f, worldHeight * 1.15f)
        }
        val panelHeight = worldHeight * if (fitToPage) 0.92f else 0.86f
        val panelX = (worldWidth - panelWidth) / 2f
        val panelY = (worldHeight - panelHeight) / 2f

        val frame = Image(theme.modalFrame)
        frame.setBounds(panelX, panelY, panelWidth, panelHeight)
        group.addActor(frame)

        val innerPad = panelWidth * if (fitToPage) 0.045f else 0.08f
        val contentWidth = panelWidth - innerPad * 2f

        val header = Label(title, theme.title)
        header.setAlignment(Align.center)
        header.setBounds(panelX, panelY + panelHeight - innerPad - header.prefHeight, panelWidth, header.prefHeight)
        group.addActor(header)

        val contentHeight = header.y - innerPad * 0.4f - panelY - innerPad

        val body = Table()
        body.top()
        build(body, Page(contentWidth, contentHeight, 1f))
        if (fitToPage) {
            // Подбор в два прохода: после уменьшения шрифта строки переносятся иначе,
            // и одного пересчёта не хватает.
            var scale = 1f
            repeat(2) {
                body.pack()
                val needed = body.prefHeight
                if (needed <= contentHeight) return@repeat
                scale = (scale * contentHeight / needed).coerceAtLeast(MIN_PAGE_SCALE)
                body.clear()
                build(body, Page(contentWidth, contentHeight, scale))
            }
        }

        val scroll = object : ScrollPane(body, ScrollPane.ScrollPaneStyle()) {
            /**
             * Шаг колеса по умолчанию — около 22% высоты области за щелчок. Панель
             * невысокая, и такой шаг проматывает почти экран разом: текст
             * перескакивает, а не едет.
             */
            override fun getMouseWheelY(): Float = super.getMouseWheelY() * WHEEL_STEP_SCALE
        }
        scroll.setFadeScrollBars(false)
        scroll.setScrollingDisabled(true, false)
        scroll.setOverscroll(false, false)
        scroll.setBounds(panelX + innerPad, panelY + innerPad, contentWidth, contentHeight)
        group.addActor(scroll)
        stage.scrollFocus = scroll

        // Круглая кнопка закрытия в углу панели.
        assets.icon("close")?.let { icon ->
            val close = Button(
                TextureRegionDrawable(assets.roundButtonUp),
                TextureRegionDrawable(assets.roundButtonDown),
            )
            close.add(Image(icon)).grow()
            // Размер по меньшей стороне: на вытянутом экране кнопка от высоты
            // выходила больше самой панели. Положение ограничено краями экрана —
            // у широкой панели угол уходил за границу.
            val size = minOf(worldWidth, worldHeight) * 0.085f
            close.setBounds(
                (panelX + panelWidth - size * 0.55f).coerceAtMost(worldWidth - size - size * 0.15f),
                (panelY + panelHeight - size * 0.55f).coerceAtMost(worldHeight - size - size * 0.15f),
                size,
                size,
            )
            close.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    sound.play(SoundManager.Sfx.UI_CLICK)
                    close()
                }
            })
            group.addActor(close)
        }

        group.color.a = 0f
        group.addAction(Actions.fadeIn(0.2f))
        stage.addActor(group)
        current = group
    }

    /**
     * Правила игры. Помещаются целиком, без прокрутки, в любой раскладке.
     *
     * На широком экране текст идёт в две колонки: в ландшафте высоты мало, а
     * ширины вдоволь, и одна колонка заставляла бы ужимать шрифт вдвое.
     */
    fun showRules() = show(Strings["rules.title"], fitToPage = true) { content, page ->
        val twoColumns = page.width > page.height * 1.1f
        val gap = page.height * 0.02f

        if (twoColumns) {
            val columnWidth = (page.width - gap * 2f) / 2f
            val left = Table().apply { top() }
            val right = Table().apply { top() }
            buildSections(left, columnWidth, page.scale, gap)
            buildCards(right, columnWidth, page.scale, gap)
            content.add(left).width(columnWidth).top().padRight(gap * 2f)
            content.add(right).width(columnWidth).top().row()
        } else {
            buildSections(content, page.width, page.scale, gap)
            buildCards(content, page.width, page.scale, gap)
        }
    }

    private fun buildSections(content: Table, width: Float, scale: Float, gap: Float) {
        fun section(titleKey: String, bodyKey: String) {
            content.add(heading(Strings[titleKey], scale)).width(width).left().padTop(gap * 1.2f).row()
            content.add(wrapped(Strings[bodyKey], scale)).width(width).left().padTop(gap * 0.3f).row()
        }
        section("rules.goal.title", "rules.goal.body")
        section("rules.turn.title", "rules.turn.body")
        section("rules.limits.title", "rules.limits.body")
    }

    private fun buildCards(content: Table, width: Float, scale: Float, gap: Float) {
        content.add(heading(Strings["rules.cards.title"], scale)).width(width).left().padTop(gap * 1.2f).row()
        val cardHeight = width * 0.15f * scale
        val cardWidth = cardHeight * (2f / 3f)
        for (letter in Letter.ALL) {
            val row = Table()
            row.add(CardActor(assets, letter)).size(cardWidth, cardHeight).padRight(gap).top()
            row.add(wrapped(Strings["rules.card.${letter.name}"], scale))
                .width(width - cardWidth - gap).left()
            content.add(row).width(width).left().padTop(gap * 0.6f).row()
        }
    }

    private fun heading(text: String, scale: Float): Label =
        Label(text, theme.bodyBold).apply {
            color = Palette.GOLD_LIGHT
            setFontScale(scale)
        }

    fun wrapped(text: String, scale: Float = 1f): Label = Label(text, theme.body).apply {
        wrap = true
        setAlignment(Align.topLeft)
        if (scale != 1f) setFontScale(scale)
    }

    private companion object {
        /** Доля от штатного шага колеса в прокручиваемых панелях. */
        const val WHEEL_STEP_SCALE = 0.45f

        /** Ниже этого шрифт правил не ужимается: дальше он нечитаем. */
        const val MIN_PAGE_SCALE = 0.45f
    }
}
