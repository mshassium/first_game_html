package com.first.game.tools

import java.awt.image.BufferedImage
import java.io.File
import java.util.ArrayDeque
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Режет сгенерированный лист ассетов на отдельные PNG с прозрачным фоном.
 *
 * Модель рисует элементы на сплошном, но неравномерном фоне (у нас — бежевый градиент
 * с тенями под объектами). Поэтому фон не «выбивается по цвету», а заливается от краёв
 * холста с допуском относительно соседнего пикселя: так уходит и градиент, и мягкая тень,
 * а объект остаётся целым.
 *
 * Дальше лист разбивается на связные области, они сортируются в порядке чтения
 * (сверху вниз, слева направо) и получают имена из пресета.
 *
 * Запуск:
 *   ./gradlew tools:sliceSheet -Psheet="assets_src/cards/Панели и кнопки.png" -Ppreset=panels
 *   ./gradlew tools:sliceSheet -Psheet=... -Pnames=a,b,c   (свой порядок имён)
 *   ./gradlew tools:sliceSheet -Psheet=... -Pdry           (только отчёт, без записи)
 *   ./gradlew tools:sliceSheet -Psheet=card.png -Pnames=card_F -Psolid   (одиночный объект)
 *
 * Если у картинки уже есть альфа, фон не вырезается повторно — она только обрезается
 * по содержимому. Поведение можно задать явно: -Pkeepalpha или -Pcutbg.
 */

/** Готовые раскладки листов: имена в порядке чтения. */
private val PRESETS: Map<String, List<String>> = mapOf(
    // Лист панелей и кнопок: 4 панели (2×2), 5 широких кнопок, 3 круглые.
    // Порядок имён выверен по замеру яркости и насыщенности: «up» — самый светлый
    // вариант, «down» — самый тёмный, «disabled» — наименее насыщенный.
    "panels" to listOf(
        "panel_wood",
        "panel_stone",
        "modal_frame",
        "panel_parchment",
        "btn_primary_up",
        "btn_secondary_down",
        "btn_primary_down",
        "btn_secondary_up",
        "btn_primary_disabled",
        "btn_round_up",
        "btn_round_disabled",
        "btn_round_down",
    ),
    // Лист доски: слот под карту, урна сброса, стопка колоды.
    "board" to listOf("slot_card", "discard_urn", "deck_stack"),
    // Лист кубиков: грани 1..6.
    "dice" to listOf("die_1", "die_2", "die_3", "die_4", "die_5", "die_6"),
    // Лист иконок 4×4. Имена выверены по тому, что модель нарисовала на самом деле:
    // вместо трёх полос меню она дала раскрытую книгу, вместо косого креста — мечи,
    // а на шестой позиции оказалась перечёркнутая книга. Иконки меню и закрытия
    // остались недорисованными, их нужно догенерировать отдельным листом.
    "icons" to listOf(
        "icon_settings", "icon_sound_on", "icon_sound_off", "icon_music_on",
        "icon_music_off", "icon_rules_off", "icon_rules", "icon_duel",
        "icon_restart", "icon_back", "icon_hourglass", "icon_deck",
        "icon_hand", "icon_speed", "icon_lang", "icon_info",
    ),
    // Листы эффектов: рисуются на чёрном, вырезаются по яркости.
    "vfx_neutral" to listOf(
        "fx_glow_soft", "fx_burst_star", "fx_ring_rune", "fx_shockwave",
        "fx_spark", "fx_dust", "fx_smoke_puff", "fx_lightray",
    ),
    "vfx_schools" to listOf(
        "fx_chain_link", "fx_leaf", "fx_ember", "fx_claw_slash", "fx_snare_jaws", "fx_crack",
    ),
)

private class SlicerOptions(
    val sheet: File,
    val outputDir: File,
    val names: List<String>,
    /** Допуск заливки фона: насколько пиксель может отличаться от соседнего. */
    val tolerance: Int,
    /** Минимальная доля площади листа, ниже которой область считается мусором. */
    val minAreaFraction: Double,
    /** Фон чёрный (листы VFX) — тогда прозрачность берётся из яркости. */
    val blackBackground: Boolean,
    /** На листе один объект: все найденные куски объединяются в один. */
    val single: Boolean,
    /** Объект сплошной: дыры внутри контура заливаются обратно. */
    val solid: Boolean,
    /** У картинки уже есть альфа — берём её, а не вырезаем фон заново. */
    val keepAlpha: Boolean?,
    val dryRun: Boolean,
)

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val arguments = args.toArgMap()

    val sheet = File(arguments["sheet"] ?: error("Не указан -Psheet=<путь к листу>"))
    require(sheet.isFile) { "Лист не найден: ${sheet.absolutePath}" }

    val preset = arguments["preset"]
    val names = arguments["names"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?: preset?.let { PRESETS[it] ?: error("Неизвестный пресет '$it'. Доступны: ${PRESETS.keys}") }
        ?: emptyList()

    val options = SlicerOptions(
        sheet = sheet,
        outputDir = File(arguments["out"] ?: "assets_src/ui"),
        names = names,
        tolerance = arguments["tolerance"]?.toIntOrNull() ?: 14,
        minAreaFraction = arguments["minArea"]?.toDoubleOrNull() ?: 0.002,
        blackBackground = preset?.startsWith("vfx") == true || arguments.containsKey("black"),
        single = arguments.containsKey("single") || names.size == 1,
        solid = arguments.containsKey("solid"),
        keepAlpha = when {
            arguments.containsKey("keepalpha") -> true
            arguments.containsKey("cutbg") -> false
            else -> null // определим по самой картинке
        },
        dryRun = arguments.containsKey("dry"),
    )

    slice(options)
}

private fun slice(options: SlicerOptions) {
    val image = ImageIO.read(options.sheet)
    println("Лист: ${options.sheet.name}, ${image.width}×${image.height}")

    val keepAlpha = options.keepAlpha ?: hasUsefulAlpha(image)
    val alpha = when {
        keepAlpha -> {
            println("Прозрачность уже есть — фон не вырезаем, только обрезаем по содержимому")
            alphaFromChannel(image)
        }
        options.blackBackground -> alphaFromLuminance(image)
        else -> alphaFromBorderFlood(image, options.tolerance)
    }

    var pieces = findPieces(alpha, image.width, image.height, options.minAreaFraction)
        .sortedInReadingOrder()

    if (options.single && pieces.size > 1) {
        // Заливка фона могла просочиться внутрь объекта через мягкий край и разрезать
        // его на куски — собираем их обратно в один прямоугольник.
        println("Куски объединены в один объект: было ${pieces.size}")
        pieces = listOf(pieces.reduce { a, b ->
            Piece(min(a.minX, b.minX), min(a.minY, b.minY), max(a.maxX, b.maxX), max(a.maxY, b.maxY), a.area + b.area)
        })
    }

    if (options.solid) {
        // Объект сплошной: всё, что дальше скругления от края рамки, обязано быть непрозрачным.
        pieces.forEach { fillHoles(alpha, image.width, it) }
    }

    println("Найдено элементов: ${pieces.size}")
    if (options.names.isNotEmpty() && options.names.size != pieces.size) {
        println(
            "ВНИМАНИЕ: имён ${options.names.size}, а элементов ${pieces.size}. " +
                "Имена не назначаются — сверьте отчёт ниже и передайте -Pnames=... в нужном порядке."
        )
    }
    val useNames = options.names.size == pieces.size

    if (!options.dryRun) options.outputDir.mkdirs()

    pieces.forEachIndexed { index, piece ->
        val name = if (useNames) options.names[index] else "piece_%02d".format(index + 1)
        val cropped = crop(image, alpha, piece)
        val size = "${cropped.width}×${cropped.height}"
        val position = "x=${piece.minX} y=${piece.minY}"
        if (options.dryRun) {
            println("  %2d  %-24s %-14s %s".format(index + 1, name, size, position))
        } else {
            val file = File(options.outputDir, "$name.png")
            ImageIO.write(cropped, "png", file)
            println("  %2d  %-24s %-14s %s → %s".format(index + 1, name, size, position, file.name))
        }
    }

    if (options.dryRun) println("Пробный прогон: файлы не записаны.")
    else println("Готово: ${options.outputDir.absolutePath}")
}

// ------------------------------------------------------------------ прозрачность

/**
 * Заливка фона от краёв холста. Пиксель попадает в фон, если он близок к тому пикселю,
 * из которого мы в него пришли, — так гладкий градиент и тени уходят целиком,
 * а на резкой границе объекта заливка останавливается.
 */
private fun alphaFromBorderFlood(image: BufferedImage, tolerance: Int): FloatArray {
    val width = image.width
    val height = image.height
    val background = BooleanArray(width * height)
    val queue = ArrayDeque<Int>()

    fun push(x: Int, y: Int, from: Int) {
        val index = y * width + x
        if (background[index]) return
        if (colorDistance(image.getRGB(x, y), from) > tolerance) return
        background[index] = true
        queue.addLast(index)
    }

    for (x in 0 until width) {
        push(x, 0, image.getRGB(x, 0))
        push(x, height - 1, image.getRGB(x, height - 1))
    }
    for (y in 0 until height) {
        push(0, y, image.getRGB(0, y))
        push(width - 1, y, image.getRGB(width - 1, y))
    }

    while (queue.isNotEmpty()) {
        val index = queue.removeFirst()
        val x = index % width
        val y = index / width
        val rgb = image.getRGB(x, y)
        if (x > 0) push(x - 1, y, rgb)
        if (x < width - 1) push(x + 1, y, rgb)
        if (y > 0) push(x, y - 1, rgb)
        if (y < height - 1) push(x, y + 1, rgb)
    }

    // Смягчаем край: пиксель на границе фона и объекта получает промежуточную альфу,
    // иначе вырезанный объект выглядит «вырубленным ножницами».
    val alpha = FloatArray(width * height)
    for (index in alpha.indices) alpha[index] = if (background[index]) 0f else 1f
    return feather(alpha, width, height)
}

/**
 * Картинка уже с вырезанным фоном, если углы прозрачны и прозрачного заметно много.
 * Тогда вырезать фон повторно нельзя: заливка пойдёт по чёрным пикселям под альфой
 * и съест тёмные части самого объекта.
 */
private fun hasUsefulAlpha(image: BufferedImage): Boolean {
    if (!image.colorModel.hasAlpha()) return false
    val corners = listOf(
        image.getRGB(0, 0), image.getRGB(image.width - 1, 0),
        image.getRGB(0, image.height - 1), image.getRGB(image.width - 1, image.height - 1),
    )
    if (corners.any { (it ushr 24) and 0xFF > 8 }) return false

    var transparent = 0
    var total = 0
    var y = 0
    while (y < image.height) {
        var x = 0
        while (x < image.width) {
            if ((image.getRGB(x, y) ushr 24) and 0xFF < 8) transparent++
            total++
            x += 4
        }
        y += 4
    }
    return transparent.toDouble() / total > 0.05
}

/** Берём готовый альфа-канал как есть. */
private fun alphaFromChannel(image: BufferedImage): FloatArray {
    val alpha = FloatArray(image.width * image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            alpha[y * image.width + x] = ((image.getRGB(x, y) ushr 24) and 0xFF) / 255f
        }
    }
    return alpha
}

/** Для листов на чёрном фоне: прозрачность равна яркости, цвет остаётся исходным. */
private fun alphaFromLuminance(image: BufferedImage): FloatArray {
    val alpha = FloatArray(image.width * image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val rgb = image.getRGB(x, y)
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            alpha[y * image.width + x] = (max(r, max(g, b)) / 255f).coerceIn(0f, 1f)
        }
    }
    return alpha
}

/** Одно размытие 3×3 по маске — убирает ступеньку на контуре. */
private fun feather(alpha: FloatArray, width: Int, height: Int): FloatArray {
    val result = FloatArray(alpha.size)
    for (y in 0 until height) {
        for (x in 0 until width) {
            var sum = 0f
            var count = 0
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until width && ny in 0 until height) {
                        sum += alpha[ny * width + nx]
                        count++
                    }
                }
            }
            result[y * width + x] = sum / count
        }
    }
    return result
}

/**
 * Возвращает непрозрачность внутренним пикселям объекта.
 *
 * Отступ от границы рамки оставляем прозрачным как есть — там живут скруглённые углы,
 * которые нужно сохранить. Всё, что глубже, заливается: дыр внутри цельного предмета быть не может.
 */
private fun fillHoles(alpha: FloatArray, imageWidth: Int, piece: Piece) {
    val margin = max(4, (min(piece.maxX - piece.minX, piece.maxY - piece.minY) * 0.05f).toInt())
    for (y in piece.minY + margin..piece.maxY - margin) {
        for (x in piece.minX + margin..piece.maxX - margin) {
            val index = y * imageWidth + x
            if (alpha[index] < 1f) alpha[index] = 1f
        }
    }
}

private fun colorDistance(a: Int, b: Int): Int {
    val dr = abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF))
    val dg = abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF))
    val db = abs((a and 0xFF) - (b and 0xFF))
    return max(dr, max(dg, db))
}

// --------------------------------------------------------------------- разбиение

private class Piece(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int, var area: Int) {
    val centerY: Int get() = (minY + maxY) / 2
    val height: Int get() = maxY - minY + 1
}

/** Связные области непрозрачных пикселей. */
private fun findPieces(
    alpha: FloatArray,
    width: Int,
    height: Int,
    minAreaFraction: Double,
): List<Piece> {
    val visited = BooleanArray(width * height)
    val pieces = mutableListOf<Piece>()
    val minArea = (width.toLong() * height * minAreaFraction).toInt()
    val queue = ArrayDeque<Int>()

    for (start in 0 until width * height) {
        if (visited[start] || alpha[start] < 0.35f) continue
        visited[start] = true
        queue.addLast(start)

        val piece = Piece(width, height, 0, 0, 0)
        while (queue.isNotEmpty()) {
            val index = queue.removeFirst()
            val x = index % width
            val y = index / width
            piece.minX = min(piece.minX, x)
            piece.minY = min(piece.minY, y)
            piece.maxX = max(piece.maxX, x)
            piece.maxY = max(piece.maxY, y)
            piece.area++

            for (dy in -1..1) {
                for (dx in -1..1) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx !in 0 until width || ny !in 0 until height) continue
                    val neighbour = ny * width + nx
                    if (visited[neighbour] || alpha[neighbour] < 0.35f) continue
                    visited[neighbour] = true
                    queue.addLast(neighbour)
                }
            }
        }
        if (piece.area >= minArea) pieces += piece
    }
    return pieces
}

/** Сверху вниз, внутри ряда — слева направо. Ряд определяется по перекрытию по вертикали. */
private fun List<Piece>.sortedInReadingOrder(): List<Piece> {
    if (isEmpty()) return this
    val byTop = sortedBy { it.minY }
    val medianHeight = map { it.height }.sorted()[size / 2]
    val rows = mutableListOf<MutableList<Piece>>()

    for (piece in byTop) {
        val row = rows.lastOrNull()
        if (row != null && abs(piece.centerY - row.first().centerY) < medianHeight * 0.6) {
            row += piece
        } else {
            rows += mutableListOf(piece)
        }
    }
    return rows.flatMap { row -> row.sortedBy { it.minX } }
}

/** Вырезает элемент с наложенной альфой и обрезает пустые поля. */
private fun crop(image: BufferedImage, alpha: FloatArray, piece: Piece): BufferedImage {
    val width = piece.maxX - piece.minX + 1
    val height = piece.maxY - piece.minY + 1
    val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val sourceX = piece.minX + x
            val sourceY = piece.minY + y
            val a = alpha[sourceY * image.width + sourceX]
            if (a <= 0.004f) continue
            val rgb = image.getRGB(sourceX, sourceY) and 0xFFFFFF
            val alphaByte = (a * 255f).toInt().coerceIn(0, 255)
            result.setRGB(x, y, (alphaByte shl 24) or rgb)
        }
    }
    return result
}

private fun Array<String>.toArgMap(): Map<String, String> = buildMap {
    for (argument in this@toArgMap) {
        val clean = argument.removePrefix("--")
        val separator = clean.indexOf('=')
        if (separator > 0) put(clean.substring(0, separator), clean.substring(separator + 1))
        else put(clean, "")
    }
}
