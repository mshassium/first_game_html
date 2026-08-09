package com.first.game.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import com.first.game.assets.Assets
import com.first.game.audio.SoundManager
import com.first.game.i18n.Strings

/** Одно поле формы. */
data class FormField(
    val label: String,
    val initial: String = "",
    val maxLength: Int = 24,
    /** Ввод скрывается точками: пароль соперник может подсмотреть через плечо. */
    val secret: Boolean = false,
)

/**
 * Форма ввода в стиле игры: ник, название комнаты, пароль, код.
 *
 * Раньше здесь открывалось системное поле ввода. Оно работало, но выглядело
 * чужеродно — в браузере это вообще запрос браузера поверх игры, — а главное,
 * его отмену некуда было передать: игрок, не желавший ставить пароль, просто
 * упирался в диалог, который нельзя пропустить.
 *
 * Поэтому форма своя: несколько полей разом (название и пароль — одно окно),
 * подтверждение по Enter, отмена по Esc, и необязательное поле можно оставить
 * пустым — кнопка подтверждения от этого не гаснет.
 */
class FormDialog(
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
        stage.keyboardFocus = null
    }

    /**
     * @param confirm подпись кнопки подтверждения
     * @param onConfirm получает значения полей в том же порядке
     */
    fun show(
        title: String,
        fields: List<FormField>,
        confirm: String = Strings["online.enter"],
        onConfirm: (List<String>) -> Unit,
    ) {
        close()

        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight
        val group = Group()
        group.setBounds(0f, 0f, worldWidth, worldHeight)

        // Затемнение перехватывает касания: пока форма открыта, стол не трогаем.
        val dim = Image(theme.dim(0.82f))
        dim.setBounds(0f, 0f, worldWidth, worldHeight)
        dim.addListener(object : InputListener() {
            override fun touchDown(e: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) = true
        })
        group.addActor(dim)

        val panelWidth = (worldWidth * 0.62f).coerceIn(320f, 720f)
        val gap = worldHeight * 0.022f
        val rowHeight = (worldHeight * 0.085f).coerceIn(40f, 64f)
        val capSize = (worldHeight * 0.05f).coerceIn(24f, 38f)

        // Содержимое меряет себя само, и уже под него подгоняется рамка: считать
        // высоту руками — значит каждый раз промахиваться на высоту подписей.
        val content = Table()
        content.top()

        content.add(Label(title, theme.title).apply {
            color = Palette.GOLD_LIGHT
            setAlignment(Align.center)
        }).width(panelWidth * 0.86f).padBottom(gap * 0.6f).row()

        val inputs = fields.map { field ->
            content.add(Label(field.label, theme.bodyMuted).apply { setAlignment(Align.left) })
                .width(panelWidth * 0.78f).left().padBottom(gap * 0.2f).row()

            val input = TextField(field.initial, theme.textField).apply {
                // По центру: у резной рамки внутренние поля берутся из девятипатча
                // и равны толщине канта, поэтому прижатый влево текст ложился
                // прямо на завиток угла.
                setAlignment(Align.center)
                maxLength = field.maxLength
                // Каретка в конец: иначе она стоит перед подставленным текстом,
                // и первый же символ уходит в начало строки.
                setCursorPosition(field.initial.length)
                if (field.secret) {
                    isPasswordMode = true
                    setPasswordCharacter('•')
                }
            }
            content.add(input).size(panelWidth * 0.78f, rowHeight).padBottom(gap * 0.6f).row()
            input
        }

        val values = { inputs.map { it.text.trim() } }

        val buttons = Table()
        buttons.add(
            menuButton(theme, assets, sound, confirm, null, capSize, panelWidth * 0.37f) {
                val result = values()
                close()
                onConfirm(result)
            },
        ).size(panelWidth * 0.37f, rowHeight).padRight(gap * 0.6f)
        buttons.add(
            menuButton(theme, assets, sound, Strings["common.back"], null, capSize, panelWidth * 0.37f) { close() },
        ).size(panelWidth * 0.37f, rowHeight)
        content.add(buttons).padTop(gap * 0.2f).row()

        content.pack()
        val panelHeight = content.prefHeight + gap * 2.4f
        val frame = Image(theme.modalFrame)
        frame.setBounds(
            (worldWidth - panelWidth) / 2f,
            (worldHeight - panelHeight) / 2f,
            panelWidth, panelHeight,
        )
        group.addActor(frame)
        content.setBounds(frame.x, frame.y + gap * 1.2f, panelWidth, content.prefHeight)
        group.addActor(content)

        // Enter подтверждает, Esc закрывает — привычнее, чем целиться в кнопку.
        group.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean = when (keycode) {
                Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                    val result = values()
                    close()
                    onConfirm(result)
                    true
                }

                Input.Keys.ESCAPE, Input.Keys.BACK -> {
                    close()
                    true
                }

                else -> false
            }
        })

        group.color.a = 0f
        group.addAction(Actions.fadeIn(0.15f))
        stage.addActor(group)
        stage.keyboardFocus = inputs.firstOrNull()
        current = group
    }
}
