package com.first.game.net

import com.badlogic.gdx.Gdx
import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.net.EventCodec
import com.first.game.domain.net.StateCodec

/** Партия глазами игрока: состояние, что показать и сколько осталось на ход. */
data class MatchView(
    val version: Int,
    val seat: String,
    val state: GameState,
    val events: List<GameEvent>,
    /** Сколько секунд осталось до просрочки хода на момент получения. */
    val secondsLeft: Float,
)

/** Что сейчас со связью — экран показывает это игроку. */
enum class NetStatus {
    /** Ход соперника прилетает сам. */
    LIVE,

    /** Сокет не поднялся, спрашиваем сервер по таймеру. */
    POLLING,

    /** Сервер не отвечает вовсе. */
    OFFLINE,
}

/**
 * Сетевая партия: отправляет ходы и отдаёт экрану новые состояния.
 *
 * Правил здесь нет — их считает сервер. Клиент шлёт «сыграл карту номер N»
 * и получает готовое состояние с событиями, которые экран проигрывает так же,
 * как в одиночной игре.
 *
 * Новое состояние приходит двумя путями: ответом на свой ход и сообщением по
 * сокету, когда сходил соперник. Оба пути ведут в [accept], а версия партии
 * отсекает повторы — какой бы путь ни оказался быстрее.
 *
 * [update] нужно звать каждый кадр: на нём держатся опрос и heartbeat сокета.
 */
class MatchClient(
    val matchId: String,
    private val realtime: Realtime?,
) {

    /** Новое состояние партии. Ставится экраном. */
    var onView: (MatchView) -> Unit = {}

    /** Изменилось качество связи. */
    var onStatus: (NetStatus) -> Unit = {}

    /** Отказ сервера: код из ответа API. */
    var onError: (String) -> Unit = {}

    var version: Int = -1
        private set

    var seat: String = "A"
        private set

    /** Ник соперника: приходит вместе с партией и показывается на столе. */
    var opponent: String = ""
        private set

    var status: NetStatus = NetStatus.POLLING
        private set

    private var pollTimer = 0f
    private var socketWait = 0f
    private var awaitingReply = false
    private var stopped = false

    /** Последний показанный вид: по нему клиент напоминает о себе после ответа. */
    private var lastView: MatchView? = null
    private var viewChangedWhileBusy = false

    fun start() {
        stopped = false
        realtime?.let { socket ->
            socket.onRow = { row -> accept(row) }
            socket.onLive = { live -> setStatus(if (live) NetStatus.LIVE else NetStatus.POLLING) }
            socket.connect()
        }
        // Первый снимок берём запросом в любом случае: сокет присылает только
        // изменения, а партия к моменту подключения уже идёт.
        refresh()
    }

    fun stop() {
        stopped = true
        realtime?.close()
    }

    fun update(delta: Float) {
        if (stopped) return
        realtime?.update(delta)

        if (status != NetStatus.LIVE) {
            pollTimer += delta
            if (pollTimer >= NetConfig.POLL_SECONDS) {
                pollTimer = 0f
                refresh()
            }
        } else {
            socketWait = 0f
        }

        // Сокет мог подключиться, но так и не подтвердить подписку: тогда через
        // несколько секунд честно переходим на опрос, а не ждём молча.
        if (status == NetStatus.LIVE && realtime?.subscribed == false) {
            socketWait += delta
            if (socketWait >= NetConfig.SOCKET_GRACE_SECONDS) setStatus(NetStatus.POLLING)
        }
    }

    // ------------------------------------------------------------------ ходы

    fun play(handIndex: Int) = command("play", handIndex)

    fun choose(optionIndex: Int) = command("choose", optionIndex)

    fun surrender() = post("$matchId/surrender", "{}")

    /** Попросить сервер засчитать просрочку соперника. Время сверяет он сам. */
    fun claimTimeout() = post("$matchId/claim-timeout", "{}")

    /**
     * Отправка хода.
     *
     * Пока ответ не пришёл, второй ход не отправляется — так гасится двойной тап.
     * Но просто выбросить команду нельзя: за время ожидания состояние могло уйти
     * вперёд по сокету, и тогда отправитель остался бы ждать сигнала, которого
     * уже не будет. Поэтому после ответа клиент напоминает о последнем виде.
     */
    private fun command(kind: String, index: Int) {
        if (awaitingReply) return
        awaitingReply = true
        val body = Json.obj("version" to version, "kind" to kind, "index" to index)
        Http.post("${NetConfig.API_BASE}/matches/$matchId/command", body, Auth.accessToken) { result ->
            awaitingReply = false
            handle(result)
            remindIfMissed()
        }
    }

    /**
     * Повтор последнего вида без событий: показывать заново нечего, они уже
     * проиграны, а вот знать актуальное состояние собеседнику нужно.
     */
    private fun remindIfMissed() {
        if (!viewChangedWhileBusy) return
        viewChangedWhileBusy = false
        lastView?.let { view -> onView(view.copy(events = emptyList())) }
    }

    private fun post(path: String, body: String) {
        Http.post("${NetConfig.API_BASE}/matches/$path", body, Auth.accessToken) { result -> handle(result) }
    }

    /** Спросить сервер о партии: и первый снимок, и запасной путь без сокета. */
    private fun refresh() {
        Http.get("${NetConfig.API_BASE}/matches/current", Auth.accessToken) { result ->
            if (!result.ok) {
                if (result.status == 0) setStatus(NetStatus.OFFLINE)
                return@get
            }
            if (status == NetStatus.OFFLINE) setStatus(NetStatus.POLLING)
            val match = result.json.obj("match") ?: return@get
            match.str("opponent")?.let { opponent = it }
            accept(
                version = match.int("version", -1),
                seat = match.str("seat") ?: seat,
                state = match.str("state").orEmpty(),
                events = match.str("events").orEmpty(),
                deadline = match.str("deadline"),
            )
        }
    }

    private fun handle(result: HttpResult) {
        if (result.ok) {
            if (status == NetStatus.OFFLINE) setStatus(NetStatus.POLLING)
            accept(
                version = result.json.int("version", -1),
                seat = result.json.str("seat") ?: seat,
                state = result.json.str("state").orEmpty(),
                events = result.json.str("events").orEmpty(),
                deadline = result.json.str("deadline"),
            )
            return
        }
        when {
            result.status == 0 -> setStatus(NetStatus.OFFLINE)
            // Отстали от сервера или партия кончилась, пока ход был в пути:
            // не ошибка игрока, просто просим свежий вид с исходом.
            result.error == "stale_version" || result.error == "match_finished" -> refresh()
            else -> onError(result.error ?: "server_error")
        }
    }

    /** Строка из базы, прилетевшая по сокету. */
    private fun accept(row: RealtimeRow) {
        accept(row.version, row.seat, row.state, row.events, row.deadline)
    }

    /**
     * Принять состояние. Повторы и опоздавшие сообщения отбрасываются по версии:
     * ответ на свой ход и уведомление по сокету несут одно и то же.
     */
    private fun accept(version: Int, seat: String, state: String, events: String, deadline: String?) {
        if (version < 0 || version <= this.version) return
        val decoded = StateCodec.decodeOrNull(state) ?: run {
            Gdx.app.error("net", "состояние партии не разбирается")
            return
        }
        this.version = version
        this.seat = seat
        val view = MatchView(
            version = version,
            seat = seat,
            state = decoded,
            events = EventCodec.decodeOrNull(events) ?: emptyList(),
            secondsLeft = secondsUntil(deadline),
        )
        lastView = view
        // Пришло, пока ждали ответа на свой ход: значит собеседник мог не успеть
        // отреагировать, и после ответа ему надо напомнить.
        if (awaitingReply) viewChangedWhileBusy = true
        onView(view)
    }

    private fun setStatus(next: NetStatus) {
        if (status == next) return
        status = next
        onStatus(next)
    }

    /**
     * Сколько осталось до просрочки. Часы игрока и сервера расходятся, поэтому
     * счётчик на экране — оценка; решение о тайм-ауте принимает только сервер.
     */
    private fun secondsUntil(deadline: String?): Float {
        val at = deadline?.let(::parseIsoMillis) ?: return 0f
        return ((at - System.currentTimeMillis()) / 1000f).coerceAtLeast(0f)
    }

    private companion object {
        /**
         * Разбор времени вида `2026-08-09T12:34:56.789Z`.
         *
         * Своими руками, потому что `java.time` в веб-сборке недоступен, а формат
         * задаём мы сами: его отдаёт PostgreSQL всегда в UTC.
         */
        fun parseIsoMillis(text: String): Long? {
            val digits = text.filter { it.isDigit() }
            if (digits.length < 14) return null
            val year = digits.substring(0, 4).toInt()
            val month = digits.substring(4, 6).toInt()
            val day = digits.substring(6, 8).toInt()
            val hour = digits.substring(8, 10).toInt()
            val minute = digits.substring(10, 12).toInt()
            val second = digits.substring(12, 14).toInt()
            val millis = digits.drop(14).take(3).padEnd(3, '0').toIntOrNull() ?: 0
            return (daysFromCivil(year, month, day) * 86_400L + hour * 3600L + minute * 60L + second) * 1000L + millis
        }

        /** Дней от эпохи Unix до даты. Алгоритм Хиннанта, без календаря платформы. */
        fun daysFromCivil(year: Int, month: Int, day: Int): Long {
            val y = if (month <= 2) year - 1 else year
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val doy = (153 * (if (month > 2) month - 3 else month + 9) + 2) / 5 + day - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return era * 146_097L + doe - 719_468L
        }
    }
}
