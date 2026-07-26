package com.first.game.tools

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Готовит фоны к сборке: уменьшает и пережимает JPEG.
 *
 * Модель отдаёт фоны примерно в 1672×941 и по 2.5 МБ каждый. В игре они растягиваются
 * вьюпортом на весь экран, мелких деталей на них нет, и разница между 1672 и 1440
 * по ширине не читается — зато вчетверо больший вес критичен для веб-сборки.
 *
 * Запуск: ./gradlew tools:prepareBackgrounds
 */
fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    val sourceDir = File(args.getOrElse(0) { "../assets_src/bg" })
    val outputDir = File(args.getOrElse(1) { "assets/bg" })
    val maxSide = args.getOrNull(2)?.toIntOrNull() ?: 1440
    val quality = args.getOrNull(3)?.toFloatOrNull() ?: 0.82f
    outputDir.mkdirs()

    val sources = sourceDir.listFiles { file -> file.isFile && file.name.endsWith(".jpg") }.orEmpty()
    require(sources.isNotEmpty()) { "Не найдено фонов в ${sourceDir.absolutePath}" }

    for (source in sources.sortedBy { it.name }) {
        val image = ImageIO.read(source)
        val factor = min(1.0, maxSide.toDouble() / maxOf(image.width, image.height))
        val width = (image.width * factor).roundToInt()
        val height = (image.height * factor).roundToInt()

        val scaled = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = scaled.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.drawImage(image, 0, 0, width, height, null)
        graphics.dispose()

        val target = File(outputDir, source.name)
        writeJpeg(scaled, target, quality)
        println(
            "%-26s %d×%d → %d×%d, %d КБ → %d КБ".format(
                source.name, image.width, image.height, width, height,
                source.length() / 1024, target.length() / 1024,
            ),
        )
    }
    val total = outputDir.listFiles().orEmpty().sumOf { it.length() }
    println("Готово: ${outputDir.absolutePath}, суммарно ${total / 1024} КБ")
}

private fun writeJpeg(image: BufferedImage, target: File, quality: Float) {
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    val params = writer.defaultWriteParam.apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = quality
    }
    // Пишем во временный файл: поток поверх существующего не обрезает его,
    // и от прошлой картинки остаётся хвост — размер файла не уменьшается.
    val temporary = File(target.parentFile, "${target.name}.tmp")
    ImageIO.createImageOutputStream(temporary).use { output ->
        writer.output = output
        writer.write(null, IIOImage(image, null, null), params)
    }
    writer.dispose()
    check(!target.exists() || target.delete()) { "Не удалось удалить $target" }
    check(temporary.renameTo(target)) { "Не удалось переименовать $temporary" }
}
