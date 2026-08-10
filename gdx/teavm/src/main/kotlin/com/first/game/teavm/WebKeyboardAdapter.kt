package com.first.game.teavm

import com.first.game.ui.SoftKeyboard

/**
 * Клавиатура браузера глазами игры.
 *
 * Вся работа — в [WebKeyboard]: здесь только перевод на язык [SoftKeyboard].
 */
class WebKeyboardAdapter : SoftKeyboard {

    override fun open(initial: String, maxLength: Int, secret: Boolean) =
        WebKeyboard.open(initial, maxLength, secret)

    override fun close() = WebKeyboard.close()

    override val coveredFraction: Float get() = WebKeyboard.coveredFraction().toFloat()

    override fun poll(): String? = if (WebKeyboard.consumeChanged()) WebKeyboard.value() else null

    override fun pollSubmit(): Boolean = WebKeyboard.consumeSubmit()
}
