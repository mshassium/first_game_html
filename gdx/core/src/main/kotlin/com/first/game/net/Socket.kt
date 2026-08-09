package com.first.game.net

/**
 * Веб-сокет глазами игры.
 *
 * Единственное место сетевого слоя, которое приходится писать под каждую
 * платформу: `Gdx.net` умеет только запросы, а постоянное соединение у libGDX
 * не предусмотрено. На десктопе за этим стоит `java.net.http.WebSocket`, в
 * веб-сборке — сокет браузера через TeaVM.
 *
 * Колбэки зовутся из чужого потока — реализация обязана переносить их в игровой
 * через `Gdx.app.postRunnable`, иначе сцена будет меняться посреди отрисовки.
 */
interface Socket {

    val connected: Boolean

    fun connect(url: String)

    fun send(text: String)

    fun close()

    var onOpen: () -> Unit
    var onText: (String) -> Unit
    var onClose: (reason: String) -> Unit
}

/**
 * Откуда игра берёт сокет. Лаунчер каждой платформы кладёт сюда свою реализацию;
 * если платформа сокетов не умеет, поле остаётся пустым и клиент работает
 * опросом.
 */
object Sockets {
    var factory: (() -> Socket)? = null

    val available: Boolean get() = factory != null

    fun open(): Socket? = factory?.invoke()
}
