package com.first.game.tools

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.io.File

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
    /** Во сколько раз уменьшить исходники при упаковке. */
    val scale: Float,
)

private val ATLASES = listOf(
    AtlasSpec("ui", "ui", 2048, scale = 1f),
    // Карты приходят от модели в 1024×1536, в игре нужны 512×768.
    AtlasSpec("cards", "cards", 2048, scale = 0.5f),
    AtlasSpec("vfx", "vfx", 1024, scale = 1f),
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
            scale = floatArrayOf(spec.scale)
        }

        TexturePacker.process(settings, input.absolutePath, outputDir.absolutePath, spec.name)
        val atlas = File(outputDir, "${spec.name}.atlas")
        val pages = outputDir.listFiles { file -> file.name.startsWith("${spec.name}") && file.name.endsWith(".png") }
            .orEmpty().size
        println("${spec.name}: ${images.size} спрайтов → ${atlas.name}, страниц $pages")
    }

    val total = outputDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    println("Готово: ${outputDir.absolutePath}, суммарно ${total / 1024} КБ")
}
