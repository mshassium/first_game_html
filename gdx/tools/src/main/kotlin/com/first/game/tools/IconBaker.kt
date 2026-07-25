package com.first.game.tools

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RadialGradientPaint
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Печёт иконки приложения: печать Ордена — золотое кольцо с пятью самоцветами школ
 * на тёмном дереве.
 *
 * Иконка временная, как и вся программная графика: когда по docs/gdx/05-prompt-book.md
 * будет сгенерирован ассет U-52, достаточно заменить файлы.
 *
 * Запуск: ./gradlew tools:bakeIcons
 */

private val SCHOOL_COLORS = listOf(
    Color(0x9C, 0xC8, 0xFF), // F
    Color(0xA9, 0xFF, 0xCF), // I
    Color(0xFF, 0xD1, 0x95), // R
    Color(0xFF, 0x9A, 0xA4), // S
    Color(0xC6, 0xB3, 0xFF), // T
)

private val WOOD_DARK = Color(0x2A, 0x1C, 0x12)
private val WOOD = Color(0x4A, 0x32, 0x20)
private val BRONZE = Color(0x6B, 0x54, 0x33)
private val GOLD = Color(0xC9, 0xA2, 0x4A)
private val GOLD_LIGHT = Color(0xF2, 0xDF, 0xA6)

/** Android mipmap-плотности и размер иконки в пикселях. */
private val ANDROID_DENSITIES = mapOf(
    "mdpi" to 48,
    "hdpi" to 72,
    "xhdpi" to 96,
    "xxhdpi" to 144,
    "xxxhdpi" to 192,
)

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val androidRes = File(args.getOrElse(0) { "android/src/main/res" })
    val storeDir = File(args.getOrElse(1) { "../assets_src/branding" })
    storeDir.mkdirs()

    for ((density, size) in ANDROID_DENSITIES) {
        val dir = File(androidRes, "mipmap-$density").apply { mkdirs() }
        ImageIO.write(icon(size, rounded = true), "png", File(dir, "ic_launcher.png"))
        ImageIO.write(icon(size, rounded = true, circular = true), "png", File(dir, "ic_launcher_round.png"))
    }
    ImageIO.write(icon(1024, rounded = false), "png", File(storeDir, "app_icon_1024.png"))
    ImageIO.write(icon(512, rounded = true), "png", File(storeDir, "app_icon_512.png"))

    println("Иконки готовы: ${androidRes.absolutePath}, ${storeDir.absolutePath}")
}

private fun icon(size: Int, rounded: Boolean, circular: Boolean = false): BufferedImage {
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

    val s = size.toFloat()

    // Подложка: тёмное дерево.
    val shape = when {
        circular -> Ellipse2D.Float(0f, 0f, s, s)
        rounded -> RoundRectangle2D.Float(0f, 0f, s, s, s * 0.22f, s * 0.22f)
        else -> RoundRectangle2D.Float(0f, 0f, s, s, 0f, 0f)
    }
    g.clip = shape
    g.paint = GradientPaint(0f, 0f, WOOD, 0f, s, WOOD_DARK)
    g.fill(shape)

    // Мягкое золотое свечение из центра.
    g.paint = RadialGradientPaint(
        Point2D.Float(s / 2f, s / 2f),
        s * 0.5f,
        floatArrayOf(0f, 1f),
        arrayOf(Color(GOLD.red, GOLD.green, GOLD.blue, 90), Color(GOLD.red, GOLD.green, GOLD.blue, 0)),
    )
    g.fill(shape)

    // Кольцо печати.
    val ringRadius = s * 0.30f
    g.stroke = BasicStroke(s * 0.055f)
    g.color = BRONZE
    g.draw(Ellipse2D.Float(s / 2f - ringRadius, s / 2f - ringRadius, ringRadius * 2, ringRadius * 2))
    g.stroke = BasicStroke(s * 0.028f)
    g.color = GOLD
    g.draw(Ellipse2D.Float(s / 2f - ringRadius, s / 2f - ringRadius, ringRadius * 2, ringRadius * 2))

    // Пять самоцветов по кругу — по числу школ магии.
    val gemRadius = s * 0.052f
    SCHOOL_COLORS.forEachIndexed { index, color ->
        val angle = index / 5.0 * 2 * Math.PI - Math.PI / 2
        val cx = (s / 2f + Math.cos(angle) * ringRadius).toFloat()
        val cy = (s / 2f + Math.sin(angle) * ringRadius).toFloat()
        g.color = color.darker()
        g.fill(Ellipse2D.Float(cx - gemRadius, cy - gemRadius, gemRadius * 2, gemRadius * 2))
        g.color = color
        g.fill(Ellipse2D.Float(cx - gemRadius * 0.72f, cy - gemRadius * 0.72f, gemRadius * 1.44f, gemRadius * 1.44f))
        g.color = Color(255, 255, 255, 150)
        g.fill(Ellipse2D.Float(cx - gemRadius * 0.36f, cy - gemRadius * 0.5f, gemRadius * 0.5f, gemRadius * 0.4f))
    }

    // Буква F в центре — та же гарнитура, что и на картах.
    val fontFile = File("../assets_src/fonts/CinzelDecorative-Bold.ttf")
    if (fontFile.isFile) {
        val font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(s * 0.34f)
        g.font = font
        val metrics = g.fontMetrics
        val text = "F"
        val textWidth = metrics.stringWidth(text)
        val x = (s - textWidth) / 2f
        val y = s / 2f + metrics.ascent / 2f - metrics.descent / 4f
        g.color = Color(0, 0, 0, 160)
        g.drawString(text, x + s * 0.012f, y + s * 0.012f)
        g.paint = GradientPaint(0f, y - metrics.ascent, GOLD_LIGHT, 0f, y, GOLD)
        g.drawString(text, x, y)
    }

    g.dispose()
    return image
}
