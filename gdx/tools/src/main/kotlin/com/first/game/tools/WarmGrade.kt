package com.first.game.tools

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * Тёплая цветокоррекция ассета до целевой температуры набора.
 *
 * Модель регулярно уводит камень в холодный синий, хотя весь принятый набор тёплый
 * (R − B от +21 до +64). Перегенерировать ради этого целый лист дорого, а разница
 * правится линейно: поднимаем красный канал, опускаем синий, зелёный оставляем
 * посередине — и возвращаем исходную яркость, чтобы предмет не «поплыл» по светлоте.
 *
 * Прозрачность не трогаем. Оригиналы лежат в assets_src/raw, так что операция обратима.
 *
 * Запуск:
 *   ./gradlew tools:warmGrade -Pfiles=assets_src/ui/die_1.png,assets_src/ui/die_2.png
 *   ./gradlew tools:warmGrade -Pdir=assets_src/ui -Pprefix=die_ -Ptarget=18
 */

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val arguments = args.toArguments()

    val target = arguments["target"]?.toDoubleOrNull() ?: 18.0
    val files = collectFiles(arguments)
    require(files.isNotEmpty()) { "Не найдено ни одного файла. Укажите -Pfiles=... или -Pdir=... -Pprefix=..." }

    println("Целевая температура R−B: %+.1f".format(target))
    for (file in files) {
        val image = ImageIO.read(file)
        val before = warmth(image)
        if (before >= target) {
            println("  %-22s %+5.1f — уже теплее цели, пропущен".format(file.name, before))
            continue
        }
        val graded = grade(image, target - before)
        ImageIO.write(graded, "png", file)
        println("  %-22s %+5.1f → %+5.1f".format(file.name, before, warmth(graded)))
    }
    println("Готово. Сырьё в assets_src/raw не изменялось.")
}

private fun collectFiles(arguments: Map<String, String>): List<File> {
    arguments["files"]?.let { list ->
        return list.split(",").map { File(it.trim()) }.filter { it.isFile }
    }
    val dir = File(arguments["dir"] ?: return emptyList())
    val prefix = arguments["prefix"] ?: ""
    return dir.listFiles { file ->
        file.isFile && file.name.endsWith(".png") && file.name.startsWith(prefix)
    }.orEmpty().sortedBy { it.name }
}

/** Средняя разница красного и синего по непрозрачным пикселям. */
private fun warmth(image: BufferedImage): Double {
    var red = 0.0
    var blue = 0.0
    var count = 0L
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val pixel = image.getRGB(x, y)
            if ((pixel ushr 24) and 0xFF < 200) continue
            red += (pixel shr 16) and 0xFF
            blue += pixel and 0xFF
            count++
        }
    }
    return if (count == 0L) 0.0 else (red - blue) / count
}

/**
 * Разводит красный и синий на [shift] единиц, сохраняя яркость пикселя.
 * Сдвиг ослабляется на очень тёмных и очень светлых пикселях, иначе тени
 * уходят в бордовый, а блики — в жёлтый.
 */
private fun grade(image: BufferedImage, shift: Double): BufferedImage {
    val result = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val pixel = image.getRGB(x, y)
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha == 0) continue

            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luma = 0.2126 * r + 0.7152 * g + 0.0722 * b

            // Полная сила в средних тонах, мягче в тенях и светах.
            val weight = 1.0 - Math.abs(luma / 255.0 - 0.5) * 1.4
            val amount = shift * weight.coerceIn(0.15, 1.0)

            var newR = r + amount * 0.55
            var newG = g.toDouble()
            var newB = b - amount * 0.45

            // Возвращаем исходную светлоту.
            val newLuma = 0.2126 * newR + 0.7152 * newG + 0.0722 * newB
            if (newLuma > 0.5) {
                val correction = luma / newLuma
                newR *= correction
                newG *= correction
                newB *= correction
            }

            result.setRGB(
                x, y,
                (alpha shl 24) or
                    (newR.roundToInt().coerceIn(0, 255) shl 16) or
                    (newG.roundToInt().coerceIn(0, 255) shl 8) or
                    newB.roundToInt().coerceIn(0, 255),
            )
        }
    }
    return result
}

private fun Array<String>.toArguments(): Map<String, String> = buildMap {
    for (argument in this@toArguments) {
        val separator = argument.indexOf('=')
        if (separator > 0) put(argument.substring(0, separator), argument.substring(separator + 1))
        else put(argument, "")
    }
}
