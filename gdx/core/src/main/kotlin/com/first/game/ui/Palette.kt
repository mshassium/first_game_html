package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.first.game.domain.Letter

/**
 * Палитра из style bible (docs/gdx/03-art-style-bible.md §3).
 * Цвета школ сохранены из HTML-версии — это узнаваемость игры.
 */
object Palette {

    val SHADOW: Color = Color.valueOf("0C1014")
    val WOOD_DARK: Color = Color.valueOf("2A1C12")
    val WOOD: Color = Color.valueOf("4A3220")
    val WOOD_LIGHT: Color = Color.valueOf("7A5636")
    val BRONZE: Color = Color.valueOf("6B5433")
    val GOLD: Color = Color.valueOf("C9A24A")
    val GOLD_LIGHT: Color = Color.valueOf("F2DFA6")
    val PARCHMENT: Color = Color.valueOf("E8DCC0")
    val STONE: Color = Color.valueOf("3D4450")
    val STONE_DARK: Color = Color.valueOf("232935")

    val TEXT: Color = Color.valueOf("F2EEE4")
    val TEXT_MUTED: Color = Color.valueOf("B9AE96")

    private val SCHOOL = mapOf(
        Letter.F to Color.valueOf("9CC8FF"),
        Letter.I to Color.valueOf("A9FFCF"),
        Letter.R to Color.valueOf("FFD195"),
        Letter.S to Color.valueOf("FF9AA4"),
        Letter.T to Color.valueOf("C6B3FF"),
    )

    fun school(letter: Letter): Color = SCHOOL.getValue(letter)

    /** Затемнённый вариант цвета школы — для теней и подложек. */
    fun schoolDark(letter: Letter): Color = Color(school(letter)).mul(0.45f, 0.45f, 0.45f, 1f)

    fun rgba(color: Color, alpha: Float): Color = Color(color.r, color.g, color.b, alpha)
}
