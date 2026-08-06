package com.first.game.teavm

import com.first.game.FirstGame
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import com.github.xpenatan.gdx.teavm.backends.web.WebAssetPreloadListener

/**
 * Точка входа веб-сборки.
 *
 * Нулевые размеры означают «растянуть canvas по окну», поэтому игра одинаково
 * работает и на телефоне, и на десктопе.
 *
 * ВАЖНО: [WebApplicationConfiguration.preloadListener] обязателен. Внутри
 * WebApplication.init() загрузка ассетов запускается только при непустом слушателе;
 * без него бэкенд бесконечно крутит пустой кадр и экран остаётся чёрным.
 * Сам слушатель ничего делать не обязан — важен факт его наличия.
 */
object TeaVMLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // Ставим до старта приложения: слушатели должны существовать раньше,
        // чем пользователь сделает первое касание.
        WebBrowserHooks.installAudioResume()
        WebBrowserHooks.installOrientationLock()

        val config = WebApplicationConfiguration().apply {
            width = 0
            height = 0
            // Без этого canvas рисуется в CSS-пикселях: на экране с плотностью 2
            // игра считается в половинном разрешении, а браузер растягивает результат.
            usePhysicalPixels = true
            preloadListener = WebAssetPreloadListener { }
        }
        WebApplication(FirstGame(), config)
    }
}
