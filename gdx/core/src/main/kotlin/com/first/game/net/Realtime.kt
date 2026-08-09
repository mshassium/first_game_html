package com.first.game.net

import com.badlogic.gdx.Gdx

/** Строка вида партии, прилетевшая из базы. */
data class RealtimeRow(
    val version: Int,
    val seat: String,
    val state: String,
    val events: String,
    val deadline: String?,
)

/**
 * Поток изменений из Supabase.
 *
 * Протокол — Phoenix: соединение открывается, в него уходит `phx_join` с
 * описанием подписки, дальше сервер шлёт `postgres_changes` на каждое изменение.
 * Библиотеки Supabase сюда не годятся — они не живут в веб-сборке, поэтому
 * обмен собран руками. Кадры простые, а протокол проверен скриптом
 * `server/checks/realtime-smoke.mjs`.
 *
 * Что важно и неочевидно:
 * - в `phx_join` обязателен токен игрока: без него действует анонимная роль,
 *   политика базы не срабатывает и не приходит вообще ничего;
 * - фильтр по игроку — только экономия трафика, чужие строки не пришли бы и без
 *   него: их отсекает база;
 * - без heartbeat раз в 25 секунд сервер молча закрывает соединение.
 */
class Realtime(private val playerId: String) {

    /** Пришла новая строка вида партии. */
    var onRow: (RealtimeRow) -> Unit = {}

    /** Связь ожила или пропала: экран показывает это игроку. */
    var onLive: (Boolean) -> Unit = {}

    var subscribed = false
        private set

    private var socket: Socket? = null
    private var heartbeat = 0f
    private var retryIn = 0f
    private var attempts = 0
    private var closed = false
    private var refCounter = 0

    val connected: Boolean get() = socket?.connected == true

    fun connect() {
        closed = false
        val next = Sockets.open() ?: run {
            Gdx.app.log("net", "сокеты недоступны, работаем опросом")
            onLive(false)
            return
        }
        socket = next
        subscribed = false

        next.onOpen = {
            attempts = 0
            join(next)
        }
        next.onText = { text -> handle(text) }
        next.onClose = { reason ->
            subscribed = false
            onLive(false)
            if (!closed) {
                // Пауза перед переподключением растёт, чтобы не долбить сервер:
                // одна, две, четыре секунды и так до полуминуты.
                retryIn = minOf(30f, 1f * (1 shl minOf(attempts, 5)))
                attempts++
                Gdx.app.log("net", "сокет закрыт ($reason), переподключение через ${retryIn.toInt()} с")
            }
        }
        next.connect(NetConfig.REALTIME_URL)
    }

    fun close() {
        closed = true
        subscribed = false
        socket?.close()
        socket = null
    }

    fun update(delta: Float) {
        if (closed) return

        if (retryIn > 0f) {
            retryIn -= delta
            if (retryIn <= 0f) connect()
            return
        }

        if (connected) {
            heartbeat += delta
            if (heartbeat >= HEARTBEAT_SECONDS) {
                heartbeat = 0f
                socket?.send("""{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"${nextRef()}"}""")
            }
        }
    }

    private fun join(socket: Socket) {
        val filter = "player_id=eq.$playerId"
        val payload = """
            {"topic":"$TOPIC","event":"phx_join","ref":"${nextRef()}","payload":{
              "config":{"postgres_changes":[
                {"event":"*","schema":"public","table":"match_views","filter":"$filter"}
              ]},
              "access_token":"${Auth.accessToken.orEmpty()}"
            }}
        """.trimIndent().replace("\n", "").replace("  ", "")
        socket.send(payload)
    }

    private fun handle(text: String) {
        val frame = Json.parse(text) ?: return
        when (frame.str("event")) {
            "phx_reply" -> {
                val status = frame.obj("payload").str("status")
                if (frame.str("topic") == TOPIC && status == "ok") {
                    subscribed = true
                    onLive(true)
                } else if (status == "error") {
                    Gdx.app.error("net", "подписка отклонена: $text")
                    subscribed = false
                    onLive(false)
                }
            }

            "postgres_changes" -> {
                val record = frame.obj("payload").obj("data").obj("record") ?: return
                onRow(
                    RealtimeRow(
                        version = record.int("version", -1),
                        seat = record.str("seat").orEmpty(),
                        state = record.str("state").orEmpty(),
                        events = record.str("events").orEmpty(),
                        deadline = record.str("deadline"),
                    ),
                )
            }

            "system" -> {
                if (frame.obj("payload").str("status") == "error") {
                    Gdx.app.error("net", "поток изменений: $text")
                    subscribed = false
                    onLive(false)
                }
            }
        }
    }

    private fun nextRef(): String = (++refCounter).toString()

    private companion object {
        const val TOPIC = "realtime:match"

        /** Сервер закрывает молчащее соединение примерно через минуту. */
        const val HEARTBEAT_SECONDS = 25f
    }
}
