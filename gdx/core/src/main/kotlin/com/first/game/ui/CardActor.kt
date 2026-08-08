package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.Actor
import com.first.game.assets.Assets
import com.first.game.domain.Letter

/**
 * Карта на столе, в руке или в полёте.
 *
 * Буква рисуется шрифтом поверх картинки, а не «запекается» в текстуру:
 * так она остаётся чёткой на любом разрешении и не мешает локализации.
 */
class CardActor(
    private val assets: Assets,
    var letter: Letter?,
    var faceUp: Boolean = true,
) : Actor() {

    /** Показать «×N» в углу — для стопки одинаковых букв в SPACE. */
    var stackCount: Int = 1

    /**
     * Приглушить недоступную для розыгрыша карту.
     *
     * Гасим яркостью, а не прозрачностью: полупрозрачная карта показывает сквозь
     * себя стол и читается как ошибка отрисовки, а не как «сейчас не твой ход».
     */
    var dimmed: Boolean = false

    private val layout = GlyphLayout()

    override fun draw(batch: Batch, parentAlpha: Float) {
        val region = if (faceUp) letter?.let(assets::cardFace) ?: assets.cardBack else assets.cardBack
        val alpha = color.a * parentAlpha
        val shade = if (dimmed) DIMMED_SHADE else 1f

        batch.setColor(color.r * shade, color.g * shade, color.b * shade, alpha)
        batch.draw(
            region, x, y, originX, originY, width, height, scaleX, scaleY, rotation,
        )

        if (faceUp) {
            val letter = this.letter
            if (letter != null) drawLetter(batch, letter, alpha, shade)
            if (stackCount > 1) drawStackCount(batch, alpha, shade)
        }
        batch.setColor(Color.WHITE)
    }

    private fun drawLetter(batch: Batch, letter: Letter, alpha: Float, shade: Float) {
        val font = assets.cardLetterFont
        val scale = width * LETTER_SCALE / font.capHeight
        font.data.setScale(scale)
        val accent = Palette.school(letter)
        font.color = Color(accent.r * shade, accent.g * shade, accent.b * shade, alpha)
        layout.setText(font, letter.name)
        // Буква садится в пустой бронзовый медальон, нарисованный на карте.
        // Его центр задан в промпт-буке (§2.3) и продублирован здесь: если менять
        // одно, надо менять и другое, иначе буква съедет с медальона.
        val centerX = x + width * MEDALLION_X
        val centerY = y + height * (1f - MEDALLION_Y) + layout.height / 2f
        font.draw(batch, layout, centerX - layout.width / 2f, centerY)
        font.data.setScale(1f)
        font.color = Color.WHITE
    }

    private fun drawStackCount(batch: Batch, alpha: Float, shade: Float) {
        val font = assets.bodyBoldFont
        val scale = width * COUNT_SCALE / font.capHeight
        font.data.setScale(scale)
        font.color = Color(
            Palette.GOLD_LIGHT.r * shade, Palette.GOLD_LIGHT.g * shade, Palette.GOLD_LIGHT.b * shade, alpha,
        )
        layout.setText(font, "×$stackCount")
        // Справа вверху: слева теперь медальон с буквой.
        font.draw(batch, layout, x + width - layout.width - width * 0.06f, y + height - height * 0.03f)
        font.data.setScale(1f)
        font.color = Color.WHITE
    }

    fun setBounds(x: Float, y: Float, width: Float, height: Float, centerOrigin: Boolean) {
        setBounds(x, y, width, height)
        if (centerOrigin) setOrigin(width / 2f, height / 2f)
    }

    private companion object {
        /**
         * Центр медальона под букву: доли ширины и высоты карты от левого верха.
         *
         * Значения измерены по готовым картам, а не взяты из промпта. Модель ставит
         * медальон с разбросом, и вырезание фона у каждой карты срезает свой отступ,
         * поэтому номинал промпта (0.16 / 0.13) на деле даёт 0.13 / 0.09.
         * Перемерить после перегенерации: tools/artgen/measure_medallion.py.
         */
        const val MEDALLION_X = 0.142f
        const val MEDALLION_Y = 0.092f

        /** Высота прописной буквы в долях ширины карты — чуть больше половины медальона. */
        const val LETTER_SCALE = 0.115f
        const val COUNT_SCALE = 0.14f

        /** Насколько темнее рисуется карта, недоступная для розыгрыша. */
        const val DIMMED_SHADE = 0.62f
    }
}
