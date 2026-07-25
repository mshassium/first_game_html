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
 *   -Dfirst.autoplay=true        — обе стороны ведёт ИИ (дымовой прогон)
 *   -Dfirst.shots=/tmp/shots     — снять кадры в указанную папку и выйти
 *   -Dfirst.size=1280x720        — размер окна
 *   -Dfirst.frames=90,600        — на каких кадрах снимать
 */
fun main() {
    val bootToGame = System.getProperty("first.boot") == "game"
    val autoPlay = System.getProperty("first.autoplay") == "true"
    val shots = System.getProperty("first.shots")
    val (width, height) = parseSize(System.getProperty("first.size"))

    val game = FirstGame(bootToGame, autoPlay)
    val listener: ApplicationListener = if (shots != null) {
        ScreenshotRunner(
            delegate = game,
            outputDir = shots,
            frames = parseFrames(System.getProperty("first.frames")),
        )
    } else {
        game
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
