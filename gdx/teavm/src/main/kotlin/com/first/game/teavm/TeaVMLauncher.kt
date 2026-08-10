package com.first.game.teavm

import com.first.game.FirstGame
import com.first.game.net.AppProfile
import com.first.game.net.Sockets
import com.first.game.ui.SoftKeyboards
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

    /** Значение параметра строки запроса или null. */
    private fun queryValue(query: String, name: String): String? = query
        .removePrefix("?")
        .split('&')
        .firstOrNull { it.startsWith("$name=") }
        ?.substringAfter('=')
        ?.takeIf { it.isNotEmpty() }

    @JvmStatic
    fun main(args: Array<String>) {
        // Отладочные ключи приходят строкой запроса: ?net=guest&profile=b.
        // Системных свойств в браузере нет, а проверять сетевые экраны в вебе
        // как-то надо.
        val query = org.teavm.jso.browser.Window.current().location.search.orEmpty()

        // Ставим до старта приложения: слушатели должны существовать раньше,
        // чем пользователь сделает первое касание.
        WebBrowserHooks.installAudioResume()
        WebBrowserHooks.installOrientationLock()
        // Клавиатуру веб-бэкенд libGDX не показывает вовсе, поэтому на телефоне
        // поля ввода без этого моста бесполезны. На десктопе клавиатура и так
        // физическая — там ввод идёт обычным путём, через события клавиш, и
        // подменять его незачем; ?keyboard=1 включает мост принудительно, иначе
        // проверить его с настольного браузера нечем.
        if (WebKeyboard.isTouchDevice() || queryValue(query, "keyboard") == "1") {
            WebKeyboard.install()
            SoftKeyboards.instance = WebKeyboardAdapter()
        }
        // Постоянное соединение для сетевой игры: у libGDX своего сокета нет,
        // в вебе за него отвечает сокет браузера.
        Sockets.factory = { WebSocketAdapter() }

        val config = WebApplicationConfiguration().apply {
            width = 0
            height = 0
            // Без этого canvas рисуется в CSS-пикселях: на экране с плотностью 2
            // игра считается в половинном разрешении, а браузер растягивает результат.
            usePhysicalPixels = true
            preloadListener = WebAssetPreloadListener { }
        }
        val net = queryValue(query, "net")?.takeIf { it == "host" || it == "guest" }
        queryValue(query, "profile")?.let { AppProfile.suffix = it }

        // Второй слушатель — экран загрузки. Без него бэкенд показывает свой:
        // логотип libGDX и белую полосу на чёрном.
        WebApplication(
            CrashReporter(FirstGame(bootNet = net, bootToOnline = queryValue(query, "boot") == "online")),
            WebLoadingScreen(),
            config,
        )
    }
}
