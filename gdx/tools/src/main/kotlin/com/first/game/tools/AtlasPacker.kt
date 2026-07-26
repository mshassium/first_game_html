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
)

private val ATLASES = listOf(
    AtlasSpec("ui", "ui", 2048, maxSprite = 512),
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
            val source = ImageIO.read(file)
            val longest = maxOf(source.width, source.height)
            if (longest <= spec.maxSprite) {
                file.copyTo(File(staging, file.name), overwrite = true)
            } else {
                val factor = spec.maxSprite.toDouble() / longest
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
