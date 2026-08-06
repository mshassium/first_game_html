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

    fun show(title: String, build: (content: Table, width: Float) -> Unit) {
        close()
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
             * невысокая, и такой шаг проматывает почти экран разом: текст
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
        assets.icon("close")?.let { icon ->
            val close = Button(
                TextureRegionDrawable(assets.roundButtonUp),
                TextureRegionDrawable(assets.roundButtonDown),
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

    /** Правила игры с карточками способностей. Одинаковы в меню и на столе. */
    fun showRules() = show(Strings["rules.title"]) { content, width ->
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
            val card = CardActor(assets, letter)
            val cardHeight = width * 0.13f
            row.add(card).size(cardHeight * (2f / 3f), cardHeight).padRight(gap * 1.5f).top()
            row.add(wrapped(Strings["rules.card.${letter.name}"]))
                .width(width - cardHeight * (2f / 3f) - gap * 1.5f).left()
            content.add(row).width(width).left().padTop(gap).row()
        }

        section("rules.limits.title", "rules.limits.body")
        content.add(Label("", theme.body)).height(gap * 3f).row()
    }

    fun wrapped(text: String): Label = Label(text, theme.body).apply {
        wrap = true
        setAlignment(Align.topLeft)
    }

    private companion object {
        /** Доля от штатного шага колеса в прокручиваемых панелях. */
        const val WHEEL_STEP_SCALE = 0.45f
    }
}
