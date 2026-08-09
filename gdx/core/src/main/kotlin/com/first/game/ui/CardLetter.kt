package com.first.game.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout

/**
 * Буква карты по центру круглого места: медальона на карте и флага чужого запрета.
 *
 * Центрировать строку по её габаритам мало: у Cinzel Decorative, которым печётся
 * `card_letter`, заглавная R с длинным росчерком. Он занимает правую треть глифа
 * (видимая ширина 156 единиц при advance 94), поэтому по габаритам в середину
 * медальона садится хвост, а тело буквы уезжает влево и выбивается из ряда
 * с F, I, S, T. Поправка ставит по центру именно тело.
 *
 * Все места, где рисуется буква карты, должны звать эту функцию — иначе R снова
 * разъедется между экраном стола и флагом запрета.
 */
fun BitmapFont.drawCardLetter(
    batch: Batch,
    layout: GlyphLayout,
    text: String,
    centerX: Float,
    centerY: Float,
) {
    layout.setText(this, text)
    draw(
        batch, layout,
        centerX - layout.width / 2f + opticalShift(text),
        // BitmapFont рисует от верха строки, а нам задан её центр.
        centerY + layout.height / 2f,
    )
}

/**
 * Насколько сдвинуть букву вправо, чтобы по центру оказалось её тело, а не габариты.
 *
 * Величина в долях высоты прописной, поэтому не зависит от размера карты.
 * Измерена по атласу шрифта; перемерить после `./gradlew tools:bakeFonts`:
 * `python3 tools/artgen/measure_card_letters.py`.
 */
private fun BitmapFont.opticalShift(text: String): Float =
    if (text == TAILED_LETTER) capHeight * TAIL_SHIFT else 0f

/** Единственная буква с росчерком; у F, I, S, T тело совпадает с габаритами. */
private const val TAILED_LETTER = "R"

private const val TAIL_SHIFT = 0.246f
