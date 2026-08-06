package com.first.game.tools

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * Пакует нарезанные ассеты в атласы, которые грузит игра.
 *
 * Каждая папка внутри assets_src становится отдельным атласом: ui, cards, vfx.
 * Пустые папки пропускаются, поэтому команду можно запускать в любой момент —
 * соберётся то, что уже нарисовано.
 *
 * Запуск: ./gradlew tools:packAtlases
 */

private class AtlasSpec(
    val sourceDir: String,
    val name: String,
    val maxSize: Int,
    /**
     * Наибольшая сторона спрайта в атласе. Исходники приходят от модели крупными —
     * карта 1024×1536, урна 1024×1024, — а в игре они рисуются во много раз мельче.
     * Без нормализации три таких объекта занимают целую страницу атласа каждый.
     */
    val maxSprite: Int,
    /**
     * Отдельные пределы по префиксу имени. Иконки рисуются на экране мелкими,
     * и держать их в атласе наравне с портретами — впустую занятая страница.
     *
     * Обратный случай — растягиваемые панели: модальное окно и фон руки занимают
     * почти весь экран, и общий предел в 384 px означал бы увеличение втрое при
     * отрисовке. Их потолок задаётся по тому, насколько крупно они реально рисуются.
     */
    val spriteCaps: List<Pair<String, Int>> = emptyList(),
    /**
     * Растягиваемые панели: из них в атлас берётся только кант с углами, а однородная
     * середина сжимается до узкой полоски. Панель рисуется во весь экран, и хранить
     * её целиком бессмысленно вдвойне: середина всё равно растягивается девятипатчем,
     * а общий предел в 384 px размывал бы кант при отрисовке втрое крупнее.
     */
    val compact: List<Compact> = emptyList(),
    /**
     * Спрайты, которым нужна круглая обрезка. Портреты рисуются внутри круглой рамы,
     * и квадратная картинка торчит из неё углами.
     */
    val circular: List<String> = emptyList(),
)

/**
 * Растягиваемая панель: какую долю исходника занимает кант с углами и до какого
 * размера этот кант нужно привести в атласе.
 *
 * Разделять эти два числа обязательно. [sourceFraction] должна захватить кант
 * целиком, иначе в углы попадёт только тёмный внешний край и они не состыкуются
 * с полосами. [targetBorder] задаётся по тому, насколько крупно панель рисуется:
 * кант один к одному становится неподвижным углом девятипатча, и для панели руки
 * высотой около 200 единиц кант в 96 px съел бы её целиком.
 */
private class Compact(val name: String, val sourceFraction: Float, val targetBorder: Int)

private val ATLASES = listOf(
    AtlasSpec(
        "ui", "ui", 2048, maxSprite = 384,
        // Панель руки не сжимается в компактный девятипатч: у неё видимая
        // текстура дерева, а сжатие заменяет середину усреднённым градиентом.
        // Вместо этого держим её крупной и растягиваем мягко — см. Theme.panel.
        spriteCaps = listOf("icon_" to 192, "panel_wood" to 768),
        circular = listOf("portrait_player", "portrait_ai"),
        compact = listOf(
            Compact("modal_frame", sourceFraction = 0.18f, targetBorder = 110),
            Compact("panel_stone", sourceFraction = 0.20f, targetBorder = 56),
        ),
    ),
    AtlasSpec("cards", "cards", 2048, maxSprite = 768),
    AtlasSpec("vfx", "vfx", 1024, maxSprite = 256),
)

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val sourceRoot = File(args.getOrElse(0) { "../assets_src" })
    val outputDir = File(args.getOrElse(1) { "assets/atlas" })
    outputDir.mkdirs()

    for (spec in ATLASES) {
        val input = File(sourceRoot, spec.sourceDir)
        val images = input.listFiles { file -> file.isFile && file.name.endsWith(".png") }.orEmpty()
        if (images.isEmpty()) {
            println("${spec.name}: пропущен, в ${input.path} нет PNG")
            continue
        }

        // Нормализуем размеры во временную папку, оригиналы не трогаем.
        val staging = File(outputDir, ".staging-${spec.name}")
        staging.deleteRecursively()
        staging.mkdirs()
        var resized = 0
        for (file in images) {
            val plain = ImageIO.read(file)
            val name = file.nameWithoutExtension
            val compact = spec.compact.firstOrNull { it.name == name }
            if (compact != null) {
                ImageIO.write(compactNinePatch(plain, compact), "png", File(staging, file.name))
                resized++
                continue
            }
            // Круглая обрезка идёт до нормализации размера, иначе портрет попадёт
            // в атлас в исходном разрешении и вытеснит остальное на вторую страницу.
            val source = if (name in spec.circular) circularCrop(plain) else plain
            val cap = spec.spriteCaps.firstOrNull { (prefix, _) -> file.name.startsWith(prefix) }?.second
                ?: spec.maxSprite
            val longest = maxOf(source.width, source.height)
            if (longest <= cap) {
                file.copyTo(File(staging, file.name), overwrite = true)
            } else {
                val factor = cap.toDouble() / longest
                val width = (source.width * factor).roundToInt().coerceAtLeast(1)
                val height = (source.height * factor).roundToInt().coerceAtLeast(1)
                val scaled = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                val g = scaled.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                g.drawImage(source, 0, 0, width, height, null)
                g.dispose()
                ImageIO.write(scaled, "png", File(staging, file.name))
                resized++
            }
        }

        val settings = TexturePacker.Settings().apply {
            maxWidth = spec.maxSize
            maxHeight = spec.maxSize
            paddingX = 4
            paddingY = 4
            // Дублируем крайний пиксель в отступ, иначе на границах спрайтов
            // при линейной фильтрации появляется полоска соседа.
            duplicatePadding = true
            edgePadding = true
            bleed = true
            // Прозрачные поля не срезаем: код позиционирует спрайты по их полному размеру.
            stripWhitespaceX = false
            stripWhitespaceY = false
            filterMin = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            filterMag = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            // Размеры ассетов не степени двойки, а мипмапы для них ломают WebGL1.
            pot = false
            useIndexes = false
        }

        TexturePacker.process(settings, staging.absolutePath, outputDir.absolutePath, spec.name)
        staging.deleteRecursively()
        val atlas = File(outputDir, "${spec.name}.atlas")
        val pages = outputDir.listFiles { file -> file.name.startsWith("${spec.name}") && file.name.endsWith(".png") }
            .orEmpty().size
        println("${spec.name}: ${images.size} спрайтов (уменьшено $resized) → ${atlas.name}, страниц $pages")
    }

    val total = outputDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    println("Готово: ${outputDir.absolutePath}, суммарно ${total / 1024} КБ")
}

/**
 * Собирает из панели компактный девятипатч и приводит его к экранному размеру.
 *
 * Растягивается только середина, а она у наших панелей почти однородна: у
 * `modal_frame` разброс яркости 3.6 из 255. Хранить её целиком — платить атласом
 * за пустоту и получать разводы при отрисовке во весь экран.
 *
 * Полосы между углами усредняются вдоль оси растяжения, иначе неровность исходника
 * размазывается в штрихи. Середина заполняется градиентом между внутренними кромками
 * полос: плоская заливка давала бы видимый стык там, где кант переходит в поле.
 */
private fun compactNinePatch(source: BufferedImage, spec: Compact): BufferedImage {
    val short = minOf(source.width, source.height)
    val border = (short * spec.sourceFraction).roundToInt().coerceIn(8, short / 2 - 4)
    // Из b / (2b + c) = 0.45 следует c = 0.222 * b: кант занимает 45% стороны спрайта,
    // и это же число задаётся в Theme.stretchable как линия разреза.
    val centre = (border * 0.222f).roundToInt().coerceAtLeast(4)
    val side = border * 2 + centre
    val far = border + centre
    val out = BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB)

    // Углы копируются как есть — только они и не растягиваются.
    val g = out.createGraphics()
    g.drawImage(source, 0, 0, border, border, 0, 0, border, border, null)
    g.drawImage(source, far, 0, side, border, source.width - border, 0, source.width, border, null)
    g.drawImage(source, 0, far, border, side, 0, source.height - border, border, source.height, null)
    g.drawImage(
        source, far, far, side, side,
        source.width - border, source.height - border, source.width, source.height, null,
    )
    g.dispose()

    val innerX = source.width / 4 until source.width * 3 / 4
    val innerY = source.height / 4 until source.height * 3 / 4
    var topInner = 0
    var bottomInner = 0
    var leftInner = 0
    var rightInner = 0
    for (i in 0 until border) {
        val top = average(source, innerX, i..i)
        val bottom = average(source, innerX, (source.height - border + i).let { it..it })
        val left = average(source, i..i, innerY)
        val right = average(source, (source.width - border + i).let { it..it }, innerY)
        for (k in border until far) {
            out.setRGB(k, i, top)
            out.setRGB(k, far + i, bottom)
            out.setRGB(i, k, left)
            out.setRGB(far + i, k, right)
        }
        if (i == border - 1) { topInner = top; leftInner = left }
        if (i == 0) { bottomInner = bottom; rightInner = right }
    }

    // Середина: билинейный переход между внутренними кромками четырёх полос.
    val cornerTopLeft = mix(topInner, leftInner, 0.5f)
    val cornerTopRight = mix(topInner, rightInner, 0.5f)
    val cornerBottomLeft = mix(bottomInner, leftInner, 0.5f)
    val cornerBottomRight = mix(bottomInner, rightInner, 0.5f)
    for (y in border until far) {
        val v = if (centre > 1) (y - border).toFloat() / (centre - 1) else 0f
        val leftEdge = mix(cornerTopLeft, cornerBottomLeft, v)
        val rightEdge = mix(cornerTopRight, cornerBottomRight, v)
        for (x in border until far) {
            val u = if (centre > 1) (x - border).toFloat() / (centre - 1) else 0f
            out.setRGB(x, y, mix(leftEdge, rightEdge, u))
        }
    }

    // Кант должен стать targetBorder пикселей: он один к одному превращается
    // в неподвижный угол при отрисовке.
    if (spec.targetBorder >= border) return out
    val finalSide = (side.toFloat() * spec.targetBorder / border).roundToInt().coerceAtLeast(16)
    val scaled = BufferedImage(finalSide, finalSide, BufferedImage.TYPE_INT_ARGB)
    val sg = scaled.createGraphics()
    sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    sg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    sg.drawImage(out, 0, 0, finalSide, finalSide, null)
    sg.dispose()
    return scaled
}

/** Линейная смесь двух ARGB-цветов. */
private fun mix(first: Int, second: Int, ratio: Float): Int {
    fun channel(shift: Int): Int {
        val a = (first ushr shift) and 0xFF
        val b = (second ushr shift) and 0xFF
        return (a + (b - a) * ratio).roundToInt().coerceIn(0, 255)
    }
    return (channel(24) shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}

/**
 * Обрезает спрайт по вписанной окружности.
 *
 * Кромка сглаживается на пару пикселей: жёсткий круг на портрете даёт ступенчатый
 * край, который в круглой раме особенно заметен.
 */
private fun circularCrop(source: BufferedImage): BufferedImage {
    val size = minOf(source.width, source.height)
    val out = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val offsetX = (source.width - size) / 2
    val offsetY = (source.height - size) / 2
    val radius = size / 2f
    val feather = (size * 0.006f).coerceAtLeast(1.5f)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = x + 0.5f - radius
            val dy = y + 0.5f - radius
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val edge = ((radius - distance) / feather).coerceIn(0f, 1f)
            if (edge <= 0f) continue
            val argb = source.getRGB(x + offsetX, y + offsetY)
            val alpha = (((argb ushr 24) and 0xFF) * edge).roundToInt().coerceIn(0, 255)
            out.setRGB(x, y, (alpha shl 24) or (argb and 0x00FFFFFF))
        }
    }
    return out
}

/** Средний цвет прямоугольной области вместе с прозрачностью. */
private fun average(image: BufferedImage, xs: IntRange, ys: IntRange): Int {
    var a = 0L
    var r = 0L
    var g = 0L
    var b = 0L
    var n = 0L
    for (y in ys) for (x in xs) {
        val argb = image.getRGB(x, y)
        a += (argb ushr 24) and 0xFF
        r += (argb ushr 16) and 0xFF
        g += (argb ushr 8) and 0xFF
        b += argb and 0xFF
        n++
    }
    if (n == 0L) return 0
    return (((a / n).toInt() and 0xFF) shl 24) or
        (((r / n).toInt() and 0xFF) shl 16) or
        (((g / n).toInt() and 0xFF) shl 8) or
        ((b / n).toInt() and 0xFF)
}
