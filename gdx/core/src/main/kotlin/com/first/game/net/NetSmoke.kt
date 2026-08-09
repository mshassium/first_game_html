package com.first.game.net

import com.badlogic.gdx.Gdx
import com.first.game.domain.EndReason
import com.first.game.domain.Side

/**
 * Сетевой прогон: партия целиком через настоящий сервер, без экрана.
 *
 * Проверяет весь клиентский слой разом — вход, запросы, сокет и приём чужого
 * хода. Первым игроком идёт [MatchClient], то есть ровно тот код, которым потом
 * будет пользоваться игровой экран; второй игрок изображается голыми запросами
 * с собственным токеном — иначе двух игроков в одном процессе не развести,
 * настройки-то одни.
 *
 * Запуск: `./gradlew lwjgl3:run -Pfirst.net=duel`.
 */
class NetSmoke(
    private val log: (String) -> Unit,
    /** После какого своего хода сдаться. Ноль и меньше — играть до конца. */
    private val surrenderAfter: Int = -1,
    private val onDone: (Boolean) -> Unit,
) {

    private var client: MatchClient? = null
    private var realtime: Realtime? = null

    private var guestToken: String? = null
    private var roomCode: String? = null
    private var matchId: String? = null

    private var moves = 0
    private var busy = false
    private var finished = false
    private var sawLiveSocket = false
    private var sawOpponentMove = false
    private var timeout = 180f
    private var guestRetries = 0

    /** Чей ход по последнему виду и сколько секунд с тех пор ничего не менялось. */
    private var waitingForGuest = false
    private var idleSeconds = 0f

    fun start() {
        log("вход анонимным игроком")
        Auth.load()
        Auth.signIn { ok ->
            if (!ok) return@signIn fail("анонимный вход не удался")
            log("вошли, ставим ник")
            RoomsApi.setNickname("Хозяин") { result ->
                when (result) {
                    is NetResult.Fail -> fail("ник не поставился: ${result.code}")
                    is NetResult.Ok -> signInGuest()
                }
            }
        }
    }

    /** Второй игрок: свой анонимный вход мимо [Auth], который один на процесс. */
    private fun signInGuest() {
        Http.post("${NetConfig.SUPABASE_URL}/auth/v1/signup", "{}") { result ->
            val token = result.json.str("access_token") ?: return@post fail("гость не вошёл")
            guestToken = token
            Http.post("${NetConfig.API_BASE}/profile", Json.obj("nickname" to "Гость"), token) { named ->
                if (!named.ok) return@post fail("ник гостя: ${named.error}")
                createRoom()
            }
        }
    }

    private fun createRoom() {
        log("создаём комнату")
        RoomsApi.create("сетевой прогон", password = null) { result ->
            when (result) {
                is NetResult.Fail -> fail("комната не создалась: ${result.code}")
                is NetResult.Ok -> {
                    roomCode = result.value.code
                    log("комната ${result.value.code}")
                    checkListing()
                }
            }
        }
    }

    private fun checkListing() {
        RoomsApi.list { result ->
            when (result) {
                is NetResult.Fail -> fail("список комнат: ${result.code}")
                is NetResult.Ok -> {
                    val mine = result.value.firstOrNull { it.code == roomCode }
                    if (mine == null) return@list fail("своей комнаты нет в списке")
                    log("список получен: ${result.value.size} комнат, своя на месте")
                    joinAsGuest()
                }
            }
        }
    }

    private fun joinAsGuest() {
        val token = guestToken ?: return fail("нет токена гостя")
        log("гость входит по коду")
        Http.post("${NetConfig.API_BASE}/rooms/join", Json.obj("code" to roomCode), token) { result ->
            val id = result.json.str("matchId") ?: return@post fail("вход гостя: ${result.error}")
            matchId = id
            log("партия началась: $id")
            watchMatch(id)
        }
    }

    private fun watchMatch(id: String) {
        val playerId = Auth.userId
        realtime = if (Sockets.available && playerId.isNotEmpty()) Realtime(playerId) else null
        if (realtime == null) log("сокет недоступен, проверяем работу опросом")

        client = MatchClient(id, realtime).apply {
            onStatus = { status ->
                log("связь: $status")
                if (status == NetStatus.LIVE) sawLiveSocket = true
            }
            onError = { code -> log("ошибка сервера: $code") }
            onView = ::handleView
            start()
        }
    }

    private fun handleView(view: MatchView) {
        if (finished) return
        busy = false

        if (view.state.isOver) {
            val outcome = view.state.outcome
            val winner = if (outcome?.winner == Side.YOU) "наша" else "соперника"
            log("партия окончена на ходу $moves, победа: $winner, причина: ${outcome?.reason}")
            if (surrenderAfter > 0) {
                // Сдались мы — значит победа обязана быть у соперника, и причина
                // должна дойти до экрана, а не остаться в базе.
                val correct = outcome?.winner == Side.AI && outcome.reason == EndReason.SURRENDER
                if (!correct) log("ПРОВАЛ: исход после сдачи неверный: $outcome")
                done(correct)
            } else {
                done(sawOpponentMove && (sawLiveSocket || realtime == null))
            }
            return
        }

        if (surrenderAfter > 0 && moves >= surrenderAfter && view.state.actingSide == Side.YOU) {
            log("сдаёмся на ходу $moves")
            client?.surrender()
            return
        }

        idleSeconds = 0f
        // В своей перспективе игрок всегда YOU: ходим, когда ждут нас.
        waitingForGuest = view.state.actingSide != Side.YOU
        if (waitingForGuest) moveAsGuest() else moveOurselves(view)
    }

    private fun moveOurselves(view: MatchView) {
        val client = client ?: return
        moves++
        log("ходим сами по v${view.version}: ${if (view.state.pending != null) "choose" else "play"}")
        if (view.state.pending != null) client.choose(0) else client.play(0)
    }

    /**
     * Ход соперника: если он доедет сам, значит поток изменений работает —
     * запроса на него мы не делаем.
     */
    private fun moveAsGuest() {
        if (busy) return
        val token = guestToken ?: return
        val id = matchId ?: return
        busy = true

        Http.get("${NetConfig.API_BASE}/matches/current", token) { result ->
            val match = result.json.obj("match") ?: run { busy = false; return@get }
            val version = match.int("version", -1)
            val state = match.str("state").orEmpty()
            // Строка 7 состояния — ожидаемый выбор; пустая означает розыгрыш карты.
            val kind = if (state.split('\n').getOrNull(6).orEmpty().isNotEmpty()) "choose" else "play"
            val body = Json.obj("version" to version, "kind" to kind, "index" to 0)
            Http.post("${NetConfig.API_BASE}/matches/$id/command", body, token) { moved ->
                busy = false
                if (moved.ok) {
                    moves++
                    guestRetries = 0
                    sawOpponentMove = true
                    return@post
                }
                // Гость мог взять версию до того, как сервер записал наш ход.
                // Ждать нечего: уведомлений о чужой партии нам не приходит,
                // поэтому просто пробуем ещё раз.
                if (guestRetries++ < 20) moveAsGuest() else fail("ход гостя: ${moved.error}")
            }
        }
    }

    fun update(delta: Float) {
        if (finished) return
        client?.update(delta)

        // Гостя двигаем мы сами, и уведомлений о его партии нам не приходит.
        // Если запрос за него не дошёл, подтолкнуть некому — делаем это по часам.
        idleSeconds += delta
        if (waitingForGuest && !busy && idleSeconds > 3f) {
            idleSeconds = 0f
            moveAsGuest()
        }

        timeout -= delta
        if (timeout <= 0f) fail("партия не закончилась за отведённое время, ходов: $moves")
    }

    private fun fail(reason: String) {
        log("ПРОВАЛ: $reason")
        done(false)
    }

    private fun done(success: Boolean) {
        if (finished) return
        finished = true
        client?.stop()
        onDone(success)
    }

}

/** Обёртка приложения для прогона: держит [NetSmoke] живым и печатает ход дела. */
class NetSmokeApp(private val surrenderAfter: Int = -1) : com.badlogic.gdx.ApplicationAdapter() {

    private var smoke: NetSmoke? = null

    override fun create() {
        smoke = NetSmoke(
            log = { message -> Gdx.app.log("netsmoke", message) },
            surrenderAfter = surrenderAfter,
            onDone = { success ->
                Gdx.app.log("netsmoke", if (success) "СЕТЕВОЙ ПРОГОН ПРОЙДЕН" else "СЕТЕВОЙ ПРОГОН ПРОВАЛЕН")
                Gdx.app.exit()
            },
        ).also { it.start() }
    }

    override fun render() {
        smoke?.update(Gdx.graphics.deltaTime)
    }
}
