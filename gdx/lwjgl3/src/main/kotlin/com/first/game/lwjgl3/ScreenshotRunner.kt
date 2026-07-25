package com.first.game.lwjgl3

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.utils.ScreenUtils

/**
 * Обёртка над игрой, снимающая кадры в PNG и закрывающая приложение.
 *
 * Нужна для проверки экранов из командной строки: системный скриншот требует
 * отдельных прав, а этот путь работает всегда и одинаково на любой машине.
 */
class ScreenshotRunner(
    private val delegate: ApplicationListener,
    private val outputDir: String,
    private val frames: Map<Int, String>,
    private val exitAfterLastFrame: Boolean = true,
) : ApplicationListener by delegate {

    private var frame = 0

    override fun render() {
        delegate.render()
        frames[frame]?.let { name ->
            val raw: Pixmap = ScreenUtils.getFrameBufferPixmap(
                0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight,
            )
            // OpenGL отдаёт кадр снизу вверх — переворачиваем построчно.
            val flipped = Pixmap(raw.width, raw.height, raw.format)
            for (row in 0 until raw.height) {
                flipped.drawPixmap(raw, 0, row, 0, raw.height - row - 1, raw.width, 1)
            }
            PixmapIO.writePNG(Gdx.files.absolute("$outputDir/$name.png"), flipped)
            raw.dispose()
            flipped.dispose()
            Gdx.app.log("screenshot", "$outputDir/$name.png")
        }
        frame++
        if (exitAfterLastFrame && frames.isNotEmpty() && frame > frames.keys.max()) {
            Gdx.app.exit()
        }
    }
}
