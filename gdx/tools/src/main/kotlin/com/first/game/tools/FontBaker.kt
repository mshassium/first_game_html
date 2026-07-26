package com.first.game.tools

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Печёт растровые шрифты (.fnt + .png в формате AngelCode BMFont) из TTF.
 *
 * Зачем свой инструмент вместо gdx-freetype: freetype не работает в TeaVM, а тащить
 * два разных пути загрузки шрифтов на разные платформы дороже, чем один раз испечь
 * растр. Плюс здесь же накладываются эффекты — золотой градиент, обводка и тень,
 * которые в рантайме стоили бы шейдера.
 *
 * Запуск: ./gradlew tools:bakeFonts
 */

private const val LATIN = " !\"#$%&'()*+,-./0123456789:;<=>?@" +
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
private const val CYRILLIC = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
private const val EXTRA = "«»—–…×✦№°•→←"

private val FULL_CHARSET = (LATIN + CYRILLIC + EXTRA).toSortedSet().joinToString("")
private val CARD_CHARSET = " FIRST0123456789×".toSortedSet().joinToString("")

/** Описание одного растрового шрифта. */
private data class FontSpec(
    val name: String,
    val source: String,
    val size: Int,
    val charset: String = FULL_CHARSET,
    val fillTop: Color,
    val fillBottom: Color,
    val outlineColor: Color? = null,
    val outlineWidth: Float = 0f,
    val shadowOffset: Int = 0,
    val shadowAlpha: Int = 170,
    val pageSize: Int = 512,
)

private val SPECS = listOf(
    // Буквы карт: крупные, «резные», тонируются в цвет школы прямо в движке.
    FontSpec(
        name = "card_letter", source = "CinzelDecorative-Bold.ttf", size = 128, charset = CARD_CHARSET,
        fillTop = Color(0xF6, 0xE6, 0xB8), fillBottom = Color(0xC0, 0x93, 0x3C),
        outlineColor = Color(0x1A, 0x10, 0x08), outlineWidth = 5f, shadowOffset = 4, pageSize = 1024,
    ),
    // Заголовки и кнопки.
    FontSpec(
        name = "title_large", source = "Forum-Regular.ttf", size = 56,
        fillTop = Color(0xF2, 0xDF, 0xA6), fillBottom = Color(0xC9, 0xA2, 0x4A),
        outlineColor = Color(0x20, 0x15, 0x10), outlineWidth = 3f, shadowOffset = 3, pageSize = 1024,
    ),
    FontSpec(
        name = "title", source = "Forum-Regular.ttf", size = 36,
        fillTop = Color(0xF2, 0xDF, 0xA6), fillBottom = Color(0xC9, 0xA2, 0x4A),
        outlineColor = Color(0x20, 0x15, 0x10), outlineWidth = 2.5f, shadowOffset = 2, pageSize = 512,
    ),
    // Основной текст: лог боя, правила, подписи.
    FontSpec(
        name = "body", source = "PT_Sans-Web-Regular.ttf", size = 28,
        fillTop = Color(0xF2, 0xEE, 0xE4), fillBottom = Color(0xD3, 0xCB, 0xB8),
        outlineColor = Color(0x14, 0x10, 0x0C), outlineWidth = 2f, shadowOffset = 1, pageSize = 512,
    ),
    // Вариант для светлого пергамента: тёмные чернила со светлой обводкой.
    // Обычный body испечён под тёмный фон, и на пергаменте его тёмная обводка
    // сливается с тёмной заливкой — буквы читаются как пятна.
    FontSpec(
        name = "body_ink", source = "PT_Sans-Web-Regular.ttf", size = 28,
        fillTop = Color(0x3A, 0x28, 0x18), fillBottom = Color(0x24, 0x18, 0x0E),
        outlineColor = Color(0xF0, 0xE4, 0xC8), outlineWidth = 1.6f, shadowOffset = 0, pageSize = 512,
    ),
    FontSpec(
        name = "body_bold", source = "PT_Sans-Web-Bold.ttf", size = 28,
        fillTop = Color(0xFF, 0xFA, 0xEE), fillBottom = Color(0xDE, 0xD5, 0xC0),
        outlineColor = Color(0x14, 0x10, 0x0C), outlineWidth = 2f, shadowOffset = 1, pageSize = 512,
    ),
)

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val sourceDir = File(args.getOrElse(0) { "../assets_src/fonts" })
    val outputDir = File(args.getOrElse(1) { "assets/fonts" })
    outputDir.mkdirs()

    for (spec in SPECS) {
        val ttf = File(sourceDir, spec.source)
        require(ttf.isFile) { "Не найден шрифт ${ttf.absolutePath}" }
        val pages = bake(spec, ttf, outputDir)
        println("${spec.name}: ${spec.charset.length} символов, страниц $pages")
    }
    println("Готово: ${outputDir.absolutePath}")
}

/** Отрисованный глиф вместе с метриками BMFont. */
private class Glyph(
    val char: Char,
    val image: BufferedImage?,
    val xOffset: Int,
    val yOffset: Int,
    val xAdvance: Int,
) {
    var x = 0
    var y = 0
}

private fun bake(spec: FontSpec, ttf: File, outputDir: File): Int {
    val baseFont = Font.createFont(Font.TRUETYPE_FONT, ttf).deriveFont(spec.size.toFloat())
    val frc = FontRenderContext(AffineTransform(), true, true)
    val metrics = probeMetrics(baseFont)
    val padding = kotlin.math.ceil(spec.outlineWidth).toInt() + spec.shadowOffset + 2

    val glyphs = spec.charset.map { char -> renderGlyph(char, baseFont, frc, spec, padding, metrics.ascent) }

    // Полочная упаковка: символы одного кегля близки по высоте, потерь почти нет.
    val page = BufferedImage(spec.pageSize, spec.pageSize, BufferedImage.TYPE_INT_ARGB)
    val pageGraphics = page.createGraphics()
    var penX = 1
    var penY = 1
    var shelfHeight = 0
    for (glyph in glyphs.sortedByDescending { it.image?.height ?: 0 }) {
        val image = glyph.image ?: continue
        if (penX + image.width + 1 > spec.pageSize) {
            penX = 1
            penY += shelfHeight + 1
            shelfHeight = 0
        }
        check(penY + image.height + 1 <= spec.pageSize) {
            "Шрифт ${spec.name} не помещается на страницу ${spec.pageSize}px — увеличьте pageSize"
        }
        pageGraphics.drawImage(image, penX, penY, null)
        glyph.x = penX
        glyph.y = penY
        penX += image.width + 1
        shelfHeight = maxOf(shelfHeight, image.height)
    }
    pageGraphics.dispose()

    val pngName = "${spec.name}.png"
    ImageIO.write(page, "png", File(outputDir, pngName))
    File(outputDir, "${spec.name}.fnt").writeText(buildFnt(spec, glyphs, metrics, pngName, padding))
    return 1
}

private class FontMetrics(val ascent: Int, val descent: Int, val lineHeight: Int)

private fun probeMetrics(font: Font): FontMetrics {
    val probe = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val graphics = probe.createGraphics()
    graphics.font = font
    val metrics = graphics.fontMetrics
    val result = FontMetrics(metrics.ascent, metrics.descent, metrics.height)
    graphics.dispose()
    return result
}

private fun renderGlyph(
    char: Char,
    font: Font,
    frc: FontRenderContext,
    spec: FontSpec,
    padding: Int,
    ascent: Int,
): Glyph {
    val vector: GlyphVector = font.createGlyphVector(frc, char.toString())
    val advance = vector.getGlyphMetrics(0).advanceX
    val outline = vector.outline
    val bounds = outline.bounds

    if (bounds.width <= 0 || bounds.height <= 0) {
        // Пробел и прочие невидимые символы: метрики есть, картинки нет.
        return Glyph(char, null, 0, 0, Math.round(advance))
    }

    val width = bounds.width + padding * 2
    val height = bounds.height + padding * 2
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    // Переносим глиф так, чтобы его левый верхний угол оказался в (padding, padding).
    graphics.translate(padding - bounds.x, padding - bounds.y)

    if (spec.shadowOffset > 0) {
        val shadow = graphics.create() as java.awt.Graphics2D
        shadow.translate(spec.shadowOffset, spec.shadowOffset)
        shadow.color = Color(0, 0, 0, spec.shadowAlpha)
        shadow.fill(outline)
        shadow.dispose()
    }

    if (spec.outlineColor != null && spec.outlineWidth > 0f) {
        graphics.color = spec.outlineColor
        graphics.stroke = BasicStroke(spec.outlineWidth * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graphics.draw(outline)
    }

    graphics.paint = GradientPaint(
        0f, bounds.y.toFloat(), spec.fillTop,
        0f, (bounds.y + bounds.height).toFloat(), spec.fillBottom,
    )
    graphics.fill(outline)
    graphics.dispose()

    return Glyph(
        char = char,
        image = image,
        xOffset = bounds.x - padding,
        yOffset = ascent + bounds.y - padding,
        xAdvance = Math.round(advance),
    )
}

private fun buildFnt(
    spec: FontSpec,
    glyphs: List<Glyph>,
    metrics: FontMetrics,
    pngName: String,
    padding: Int,
): String = buildString {
    val lineHeight = metrics.lineHeight + padding
    appendLine(
        "info face=\"${spec.name}\" size=${spec.size} bold=0 italic=0 charset=\"\" unicode=1 " +
            "stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1"
    )
    appendLine(
        "common lineHeight=$lineHeight base=${metrics.ascent} scaleW=${spec.pageSize} " +
            "scaleH=${spec.pageSize} pages=1 packed=0"
    )
    appendLine("page id=0 file=\"$pngName\"")
    appendLine("chars count=${glyphs.size}")
    for (glyph in glyphs.sortedBy { it.char.code }) {
        val image = glyph.image
        appendLine(
            "char id=${glyph.char.code} x=${glyph.x} y=${glyph.y} " +
                "width=${image?.width ?: 0} height=${image?.height ?: 0} " +
                "xoffset=${glyph.xOffset} yoffset=${glyph.yOffset} " +
                "xadvance=${glyph.xAdvance} page=0 chnl=15"
        )
    }
    appendLine("kernings count=0")
}
