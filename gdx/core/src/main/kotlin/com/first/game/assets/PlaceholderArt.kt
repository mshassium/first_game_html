package com.first.game.assets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.first.game.domain.Letter
import com.first.game.ui.Palette
import kotlin.math.max
import kotlin.math.min

/**
 * Программная заглушка вместо сгенерированных нейросетью ассетов.
 *
 * Рисует те же элементы и в тех же пропорциях, что описаны в docs/gdx/04-asset-list.md,
 * поэтому раскладка, анимации и читаемость проверяются уже сейчас. Как только в
 * gdx/assets/ появятся настоящие атласы, [Assets] возьмёт их вместо заглушек —
 * менять код экранов не придётся.
 */
class PlaceholderArt : Disposable {

    private val textures = mutableListOf<Texture>()

    /** Лицевые стороны карт по буквам. */
    val cardFaces: Map<Letter, TextureRegion> = Letter.ALL.associateWith { letter ->
        region(cardPixmap(letter))
    }

    val cardBack: TextureRegion = region(cardBackPixmap())

    /** Белая точка — универсальная заливка для панелей и затемнений. */
    val white: TextureRegion = region(Pixmap(4, 4, Pixmap.Format.RGBA8888).apply {
        setColor(Color.WHITE)
        fill()
    })

    /** Мягкое круглое свечение, тонируется в коде. */
    val glow: TextureRegion = region(glowPixmap(128))

    /** Панель с рамкой — растягивается как обычный спрайт (заглушка вместо 9-patch). */
    val panel: TextureRegion = region(panelPixmap(WOOD_PANEL, 96, 96))
    val panelStone: TextureRegion = region(panelPixmap(STONE_PANEL, 96, 96))
    val slot: TextureRegion = region(slotPixmap(96, 144))

    val buttonUp: TextureRegion = region(buttonPixmap(bright = false))
    val buttonDown: TextureRegion = region(buttonPixmap(bright = true))

    /** Грани кубика 1..6. */
    val dieFaces: List<TextureRegion> = (1..6).map { region(diePixmap(it, 96)) }

    // ------------------------------------------------------------------ карты

    private fun cardPixmap(letter: Letter): Pixmap {
        val width = CARD_WIDTH
        val height = CARD_HEIGHT
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        val accent = Palette.school(letter)

        // Внешняя рамка «дерева».
        pixmap.fillRoundRect(0, 0, width, height, 18, Palette.WOOD_DARK)
        pixmap.fillRoundRect(3, 3, width - 6, height - 6, 16, Palette.WOOD)
        pixmap.fillRoundRect(7, 7, width - 14, height - 14, 13, Palette.WOOD_LIGHT)
        pixmap.fillRoundRect(10, 10, width - 20, height - 20, 11, Palette.STONE_DARK)

        val innerX = 14
        val innerWidth = width - 28

        // Картуш под букву: 6%..30% высоты.
        val cartoucheTop = (height * 0.06f).toInt()
        val cartoucheHeight = (height * 0.24f).toInt()
        pixmap.fillRoundRect(innerX, cartoucheTop, innerWidth, cartoucheHeight, 8, Palette.STONE)
        pixmap.frameRoundRect(innerX, cartoucheTop, innerWidth, cartoucheHeight, 8, accent, 2)

        // Медальон с иллюстрацией: 30%..72%.
        val medallionTop = (height * 0.30f).toInt()
        val medallionHeight = (height * 0.42f).toInt()
        pixmap.fillRoundRect(innerX, medallionTop, innerWidth, medallionHeight, 10, Palette.schoolDark(letter))
        pixmap.setColor(Palette.rgba(accent, 0.35f))
        pixmap.fillCircle(width / 2, medallionTop + medallionHeight / 2, medallionHeight / 3)
        pixmap.frameRoundRect(innerX, medallionTop, innerWidth, medallionHeight, 10, Palette.BRONZE, 2)

        // Лента названия: 72%..82%.
        val ribbonTop = (height * 0.72f).toInt()
        val ribbonHeight = (height * 0.10f).toInt()
        pixmap.fillRoundRect(innerX, ribbonTop, innerWidth, ribbonHeight, 6, Palette.PARCHMENT)

        // Поле описания: 82%..95%.
        val textTop = (height * 0.82f).toInt()
        val textHeight = (height * 0.13f).toInt()
        pixmap.fillRoundRect(innerX, textTop, innerWidth, textHeight, 6, Palette.STONE_DARK)

        // Самоцвет школы внизу.
        pixmap.setColor(accent)
        pixmap.fillCircle(width / 2, height - 12, 7)
        pixmap.setColor(Palette.GOLD_LIGHT)
        pixmap.fillCircle(width / 2 - 2, height - 14, 2)

        return pixmap
    }

    private fun cardBackPixmap(): Pixmap {
        val pixmap = Pixmap(CARD_WIDTH, CARD_HEIGHT, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        pixmap.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 18, Palette.WOOD_DARK)
        pixmap.fillRoundRect(4, 4, CARD_WIDTH - 8, CARD_HEIGHT - 8, 15, Color.valueOf("1B2440"))
        pixmap.frameRoundRect(10, 10, CARD_WIDTH - 20, CARD_HEIGHT - 20, 12, Palette.BRONZE, 2)

        val centerX = CARD_WIDTH / 2
        val centerY = CARD_HEIGHT / 2
        pixmap.setColor(Palette.BRONZE)
        pixmap.fillCircle(centerX, centerY, 30)
        pixmap.setColor(Palette.GOLD)
        pixmap.fillCircle(centerX, centerY, 24)
        pixmap.setColor(Color.valueOf("1B2440"))
        pixmap.fillCircle(centerX, centerY, 18)
        // Пять самоцветов школ вокруг печати.
        Letter.ALL.forEachIndexed { index, letter ->
            val angle = (index / 5.0 * 2 * Math.PI) - Math.PI / 2
            val x = centerX + (Math.cos(angle) * 42).toInt()
            val y = centerY + (Math.sin(angle) * 42).toInt()
            pixmap.setColor(Palette.school(letter))
            pixmap.fillCircle(x, y, 6)
        }
        return pixmap
    }

    // -------------------------------------------------------------------- UI

    private fun panelPixmap(fill: Color, width: Int, height: Int): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        pixmap.fillRoundRect(0, 0, width, height, 12, Palette.WOOD_DARK)
        pixmap.fillRoundRect(3, 3, width - 6, height - 6, 10, Palette.BRONZE)
        pixmap.fillRoundRect(6, 6, width - 12, height - 12, 8, fill)
        return pixmap
    }

    private fun slotPixmap(width: Int, height: Int): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        pixmap.fillRoundRect(0, 0, width, height, 10, Palette.rgba(Palette.SHADOW, 0.55f))
        pixmap.frameRoundRect(2, 2, width - 4, height - 4, 9, Palette.rgba(Palette.BRONZE, 0.8f), 2)
        return pixmap
    }

    private fun buttonPixmap(bright: Boolean): Pixmap {
        val width = 128
        val height = 44
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        pixmap.fillRoundRect(0, 0, width, height, 12, Palette.WOOD_DARK)
        pixmap.fillRoundRect(2, 2, width - 4, height - 4, 11, if (bright) Palette.GOLD else Palette.BRONZE)
        pixmap.fillRoundRect(5, 5, width - 10, height - 10, 9, if (bright) Palette.WOOD_LIGHT else Palette.WOOD)
        return pixmap
    }

    private fun diePixmap(value: Int, size: Int): Pixmap {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.SourceOver
        pixmap.fillRoundRect(0, 0, size, size, 14, Palette.STONE_DARK)
        pixmap.fillRoundRect(3, 3, size - 6, size - 6, 12, Palette.STONE)
        pixmap.frameRoundRect(3, 3, size - 6, size - 6, 12, Palette.BRONZE, 2)

        val third = size / 4
        val positions: List<Pair<Int, Int>> = when (value) {
            1 -> listOf(2 to 2)
            2 -> listOf(1 to 1, 3 to 3)
            3 -> listOf(1 to 1, 2 to 2, 3 to 3)
            4 -> listOf(1 to 1, 1 to 3, 3 to 1, 3 to 3)
            5 -> listOf(1 to 1, 1 to 3, 2 to 2, 3 to 1, 3 to 3)
            else -> listOf(1 to 1, 1 to 2, 1 to 3, 3 to 1, 3 to 2, 3 to 3)
        }
        for ((cellX, cellY) in positions) {
            pixmap.setColor(Palette.GOLD_LIGHT)
            pixmap.fillCircle(cellX * third, cellY * third, max(3, size / 16))
        }
        return pixmap
    }

    private fun glowPixmap(size: Int): Pixmap {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val center = size / 2f
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = (x - center) / center
                val dy = (y - center) / center
                val distance = min(1.0, Math.sqrt((dx * dx + dy * dy).toDouble()))
                val alpha = ((1.0 - distance) * (1.0 - distance)).toFloat()
                pixmap.setColor(1f, 1f, 1f, alpha)
                pixmap.drawPixel(x, y)
            }
        }
        return pixmap
    }

    private fun region(pixmap: Pixmap): TextureRegion {
        val texture = Texture(pixmap, true)
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        pixmap.dispose()
        textures += texture
        return TextureRegion(texture)
    }

    override fun dispose() {
        textures.forEach { it.dispose() }
        textures.clear()
    }

    private companion object {
        const val CARD_WIDTH = 256
        const val CARD_HEIGHT = 384
        val WOOD_PANEL: Color = Palette.WOOD_DARK
        val STONE_PANEL: Color = Palette.STONE_DARK
    }
}

// ------------------------------------------------------------ помощники Pixmap

private fun Pixmap.fillRoundRect(x: Int, y: Int, width: Int, height: Int, radius: Int, color: Color) {
    setColor(color)
    val r = min(radius, min(width, height) / 2)
    fillRectangle(x + r, y, width - 2 * r, height)
    fillRectangle(x, y + r, width, height - 2 * r)
    fillCircle(x + r, y + r, r)
    fillCircle(x + width - r - 1, y + r, r)
    fillCircle(x + r, y + height - r - 1, r)
    fillCircle(x + width - r - 1, y + height - r - 1, r)
}

private fun Pixmap.frameRoundRect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    radius: Int,
    color: Color,
    thickness: Int,
) {
    setColor(color)
    repeat(thickness) { offset ->
        val r = max(0, radius - offset)
        drawRectangleRounded(x + offset, y + offset, width - offset * 2, height - offset * 2, r)
    }
}

private fun Pixmap.drawRectangleRounded(x: Int, y: Int, width: Int, height: Int, radius: Int) {
    val r = min(radius, min(width, height) / 2)
    drawLine(x + r, y, x + width - r - 1, y)
    drawLine(x + r, y + height - 1, x + width - r - 1, y + height - 1)
    drawLine(x, y + r, x, y + height - r - 1)
    drawLine(x + width - 1, y + r, x + width - 1, y + height - r - 1)
    if (r > 0) {
        drawCircleArcs(x + r, y + r, r)
        drawCircleArcs(x + width - r - 1, y + r, r)
        drawCircleArcs(x + r, y + height - r - 1, r)
        drawCircleArcs(x + width - r - 1, y + height - r - 1, r)
    }
}

private fun Pixmap.drawCircleArcs(centerX: Int, centerY: Int, radius: Int) {
    drawCircle(centerX, centerY, radius)
}
