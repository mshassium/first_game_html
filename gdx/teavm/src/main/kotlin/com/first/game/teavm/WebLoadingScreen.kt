package com.first.game.teavm

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ScreenUtils
import com.first.game.ui.LoadingArt
import com.first.game.ui.Palette
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob

/**
 * Экран загрузки веб-сборки.
 *
 * Штатный экран бэкенда показывает логотип libGDX и белую полосу на чёрном — первое,
 * что видит игрок, не имеет к игре никакого отношения. Рисуем вместо него [LoadingArt].
 *
 * Порядок работы повторяет `WebPreloadApplicationListener`, потому что другого нет:
 * экран должен что-то показывать до того, как загрузились ассеты, из которых он сам
 * собран. Поэтому сперва тянутся два файла для него самого, и лишь потом запускается
 * общая загрузка — её очередь и даёт прогресс.
 */
class WebLoadingScreen : ApplicationAdapter() {

    private lateinit var app: WebApplication
    private lateinit var loader: AssetLoader

    private val art = LoadingArt()
    private var batch: SpriteBatch? = null

    /** Длина очереди на старте: без неё прогресс не с чем сравнивать. */
    private var total = -1

    /** Показанная доля догоняет настоящую, а не прыгает: скачки читаются как подвисание. */
    private var shown = 0f
    private var ready = false

    override fun create() {
        app = WebApplication.get()
        loader = AssetInstance.getLoaderInstance()
        preloadOwn(LoadingArt.BACKGROUND) {
            preloadOwn(LoadingArt.EMBLEM) {
                loader.preload(object : AssetLoaderListener<Void> {
                    override fun onSuccess(url: String?, result: Void?) {
                        total = loader.queue
                    }
                })
            }
        }
    }

    /** Файл, из которого собран сам экран загрузки. Не найдётся — обойдёмся без него. */
    private fun preloadOwn(path: String, next: () -> Unit) {
        loader.loadAsset(
            path, AssetType.Binary, Files.FileType.Internal,
            object : AssetLoaderListener<WebBlob> {
                override fun onSuccess(url: String?, result: WebBlob?) = next()
                override fun onFailure(url: String?) = next()
            },
        )
    }

    override fun render() {
        ScreenUtils.clear(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f, true)
        // Пока очередь не составлена, рисовать нечем: свои файлы ещё в пути.
        if (total < 0) return
        val batch = this.batch ?: SpriteBatch().also {
            this.batch = it
            art.load()
        }

        val progress = if (total == 0) 1f else (total - loader.queue).toFloat() / total
        shown = MathUtils.clamp(
            shown + (progress - shown).coerceAtMost(SPEED * Gdx.graphics.deltaTime), 0f, 1f,
        )

        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        batch.projectionMatrix = batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        batch.begin()
        art.draw(batch, width, height, shown)
        batch.end()

        // Управление отдаётся игре, только когда полоса дошла до конца: оборвать её
        // на середине хуже, чем показать лишние полсекунды.
        if (!ready && progress >= 1f && !loader.isDownloading && shown >= 1f) {
            ready = true
            app.setPreloadReady()
        }
    }

    override fun dispose() {
        batch?.dispose()
        art.dispose()
    }

    private companion object {
        /** Насколько быстро показанная доля догоняет настоящую, долей в секунду. */
        const val SPEED = 0.9f
    }
}
