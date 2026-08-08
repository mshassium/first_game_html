package com.first.game.lwjgl3

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.first.game.FirstGame

/**
 * Точка входа десктопной сборки.
 *
 * Отладочные ключи:
 *   -Dfirst.boot=game            — открыть сразу игровой стол
 *   -Dfirst.boot=loading         — показать экран загрузки веб-сборки
 *   -Dfirst.autoplay=true        — обе стороны ведёт ИИ (дымовой прогон)
 *   -Dfirst.shots=/tmp/shots     — снять кадры в указанную папку и выйти
 *   -Dfirst.size=1280x720        — размер окна
 *   -Dfirst.frames=90,600        — на каких кадрах снимать
 *   -Dfirst.overlay=rules        — сразу открыть оверлей меню (rules | settings)
 *   -Dfirst.speed=instant        — скорость анимаций: normal | fast | instant
 */
fun main() {
    val bootToGame = System.getProperty("first.boot") == "game"
    val autoPlay = System.getProperty("first.autoplay") == "true"
    val shots = System.getProperty("first.shots")
    val (width, height) = parseSize(System.getProperty("first.size"))

    val speed = System.getProperty("first.speed")?.let { name ->
        com.first.game.AnimationSpeed.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
    // Экран загрузки живёт только в вебе и полторы секунды: без этого ключа
    // посмотреть на него нечем.
    val boot: ApplicationListener =
        if (System.getProperty("first.boot") == "loading") {
            LoadingPreview()
        } else {
            FirstGame(bootToGame, autoPlay, System.getProperty("first.overlay"), speed)
        }
    val listener: ApplicationListener = if (shots != null) {
        ScreenshotRunner(
            delegate = boot,
            outputDir = shots,
            frames = parseFrames(System.getProperty("first.frames")),
        )
    } else {
        boot
    }

    Lwjgl3ApplicationConfiguration().apply {
        setTitle("F!RST")
        setWindowedMode(width, height)
        setWindowSizeLimits(800, 450, -1, -1)
        useVsync(true)
        setForegroundFPS(60)
        Lwjgl3Application(listener, this)
    }
}

/** Номера кадров для снимков: "90,260,430" или пусто — набор по умолчанию. */
private fun parseFrames(value: String?): Map<Int, String> {
    val numbers = value?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
        ?: listOf(90, 260, 430, 600, 900, 1400)
    return numbers.withIndex().associate { (index, frame) -> frame to "%02d".format(index + 1) }
}

private fun parseSize(value: String?): Pair<Int, Int> {
    val parts = value?.split("x") ?: return 1280 to 720
    val width = parts.getOrNull(0)?.toIntOrNull() ?: return 1280 to 720
    val height = parts.getOrNull(1)?.toIntOrNull() ?: return 1280 to 720
    return width to height
}
