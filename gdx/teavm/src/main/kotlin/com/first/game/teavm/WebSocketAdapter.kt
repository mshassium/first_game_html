package com.first.game.teavm

import com.badlogic.gdx.Gdx
import com.first.game.net.Socket
import org.teavm.jso.dom.events.Event
import org.teavm.jso.websocket.WebSocket

/**
 * Веб-сокет браузера через TeaVM.
 *
 * Здесь всё уже в одном потоке — JavaScript однопоточный, — но колбэки всё
 * равно проходят через `postRunnable`: так десктоп и веб ведут себя одинаково,
 * и сцена не меняется из середины отрисовки.
 */
class WebSocketAdapter : Socket {

    override var onOpen: () -> Unit = {}
    override var onText: (String) -> Unit = {}
    override var onClose: (String) -> Unit = {}

    private var socket: WebSocket? = null
    private var open = false

    override val connected: Boolean get() = open

    override fun connect(url: String) {
        val created = WebSocket(url)
        socket = created

        created.onOpen { _: Event ->
            open = true
            Gdx.app.postRunnable { onOpen() }
        }
        created.onMessage { event ->
            val text = event.dataAsString
            Gdx.app.postRunnable { onText(text) }
        }
        created.onClose { _: Event ->
            open = false
            Gdx.app.postRunnable { onClose("соединение закрыто") }
        }
        created.onError { _: Event ->
            open = false
            Gdx.app.postRunnable { onClose("ошибка соединения") }
        }
    }

    override fun send(text: String) {
        if (open) socket?.send(text)
    }

    override fun close() {
        open = false
        socket?.close()
        socket = null
    }
}
