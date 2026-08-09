package com.first.game.lwjgl3

import com.badlogic.gdx.Gdx
import com.first.game.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket as JavaWebSocket
import java.time.Duration
import java.util.concurrent.CompletionStage

/**
 * Веб-сокет десктопной сборки на `java.net.http`.
 *
 * Отдельной библиотеки не нужно: клиент есть в JDK начиная с 11, а проект и так
 * собирается под 17. Сообщения приходят в потоке HTTP-клиента, поэтому каждый
 * колбэк переносится в игровой поток — иначе сцена менялась бы посреди кадра.
 */
class DesktopSocket : Socket {

    override var onOpen: () -> Unit = {}
    override var onText: (String) -> Unit = {}
    override var onClose: (String) -> Unit = {}

    private var socket: JavaWebSocket? = null

    @Volatile
    private var open = false

    override val connected: Boolean get() = open

    override fun connect(url: String) {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
            .newWebSocketBuilder()
            .buildAsync(URI.create(url), Listener())
            .whenComplete { created, error ->
                if (error != null) {
                    Gdx.app.postRunnable { onClose(error.message ?: "не удалось подключиться") }
                    return@whenComplete
                }
                socket = created
                open = true
                Gdx.app.postRunnable { onOpen() }
            }
    }

    override fun send(text: String) {
        val current = socket ?: return
        // Отправка асинхронная; ошибку не глотаем — по ней виден обрыв.
        current.sendText(text, true).exceptionally { error ->
            Gdx.app.postRunnable { onClose(error.message ?: "отправка не прошла") }
            null
        }
    }

    override fun close() {
        open = false
        socket?.sendClose(JavaWebSocket.NORMAL_CLOSURE, "выход")
        socket = null
    }

    private inner class Listener : JavaWebSocket.Listener {

        /** Длинное сообщение приходит кусками — собираем до последнего. */
        private val buffer = StringBuilder()

        override fun onOpen(webSocket: JavaWebSocket) {
            webSocket.request(1)
        }

        override fun onText(webSocket: JavaWebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
            buffer.append(data)
            if (last) {
                val message = buffer.toString()
                buffer.setLength(0)
                Gdx.app.postRunnable { this@DesktopSocket.onText(message) }
            }
            webSocket.request(1)
            return null
        }

        override fun onClose(webSocket: JavaWebSocket, status: Int, reason: String): CompletionStage<*>? {
            open = false
            Gdx.app.postRunnable { this@DesktopSocket.onClose(reason.ifBlank { "код $status" }) }
            return null
        }

        override fun onError(webSocket: JavaWebSocket, error: Throwable) {
            open = false
            Gdx.app.postRunnable { this@DesktopSocket.onClose(error.message ?: "ошибка соединения") }
        }
    }
}
