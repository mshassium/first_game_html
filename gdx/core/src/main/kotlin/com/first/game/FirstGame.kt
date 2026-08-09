package com.first.game

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.first.game.assets.Assets
import com.first.game.audio.SoundManager
import com.first.game.i18n.Strings
import com.first.game.net.Auth
import com.first.game.net.Http
import com.first.game.net.MatchClient
import com.first.game.net.NetConfig
import com.first.game.net.NetResult
import com.first.game.net.RoomsApi
import com.first.game.net.Realtime
import com.first.game.net.RoomInfo
import com.first.game.net.Sockets
import com.first.game.net.obj
import com.first.game.net.str
import com.first.game.screens.GameScreen
import com.first.game.screens.LobbyScreen
import com.first.game.screens.MenuScreen
import com.first.game.screens.OnlineScreen
import com.first.game.ui.Theme

/**
 * @param bootToGame сразу открыть игровой экран, минуя меню — нужно для снятия
 * скриншотов и быстрой ручной проверки стола.
 * @param autoPlay обе стороны ведёт ИИ: дымовой прогон всех анимаций без участия человека.
 */
class FirstGame(
    private val bootToGame: Boolean = false,
    private val autoPlay: Boolean = false,
    /** Открыть оверлей меню сразу: "rules" или "settings". Для проверки экранов. */
    val bootOverlay: String? = null,
    /** Принудительная скорость анимаций на этот запуск. Для проверки экранов. */
    private val forcedSpeed: AnimationSpeed? = null,
    /**
     * Сетевой сценарий без участия человека: "host" заводит комнату и ждёт,
     * "guest" входит в первую открытую. Нужен, чтобы проверить сетевой стол
     * двумя окнами на одной машине.
     */
    private val bootNet: String? = null,
    /** Открыть сразу список комнат — для проверки сетевых экранов. */
    private val bootToOnline: Boolean = false,
    /** Крутить таймер хода на живом столе без сетевой партии — для проверки вида. */
    val timerDemo: Boolean = false,
) : Game() {

    lateinit var batch: SpriteBatch private set
    lateinit var assets: Assets private set
    lateinit var theme: Theme private set
    lateinit var sound: SoundManager private set

    override fun create() {
        Gdx.app.logLevel = Application.LOG_INFO
        batch = SpriteBatch()
        assets = Assets().apply { load() }
        theme = Theme(assets)
        sound = SoundManager().apply { load() }
        forcedSpeed?.let { GamePrefs.animationSpeed = it }
        Strings.load(GamePrefs.language)
        when {
            bootNet != null -> startNetScenario(bootNet)
            bootToOnline -> showOnline()
            bootToGame -> startGame()
            else -> showMenu()
        }
    }

    fun showMenu() = swapTo(MenuScreen(this))

    fun startGame() {
        SaveGame.clear()
        swapTo(GameScreen(this, autoPlay))
    }

    /** Открыть отложенную партию. Если сохранение битое, начинается новая. */
    fun continueGame() {
        val saved = SaveGame.load()
        if (saved == null) startGame() else swapTo(GameScreen(this, autoPlay, saved))
    }

    /**
     * Проверочный сценарий: два окна на одной машине играют друг с другом.
     * Ходы делает эвристика ИИ, человек не нужен.
     */
    private fun startNetScenario(role: String) {
        Auth.load()
        Auth.signIn { ok ->
            if (!ok) return@signIn showMenu()
            val nickname = if (role == "host") "Хозяин" else "Гость"
            RoomsApi.setNickname(nickname) {
                Auth.nickname = nickname
                if (role == "host") {
                    RoomsApi.create("проверка стола", null) { result ->
                        when (result) {
                            is NetResult.Ok -> showLobby(result.value)
                            is NetResult.Fail -> showMenu()
                        }
                    }
                } else {
                    RoomsApi.list { result ->
                        // Именно комнату хозяина: в списке могут висеть чужие.
                        val room = (result as? NetResult.Ok)?.value?.firstOrNull { it.host == "Хозяин" }
                        if (room == null) showMenu() else RoomsApi.join(room.id, room.code, null) { joined ->
                            when (joined) {
                                is NetResult.Ok -> startNetGame(joined.value)
                                is NetResult.Fail -> showMenu()
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------ сетевая игра

    fun showOnline() = swapTo(OnlineScreen(this))

    fun showLobby(room: RoomInfo) = swapTo(LobbyScreen(this, room))

    /** Сесть за сетевой стол: состояние придёт с сервера, раздачи здесь нет. */
    fun startNetGame(matchId: String) {
        val realtime = if (Sockets.available) Realtime(Auth.userId) else null
        swapTo(GameScreen(this, autoPlay = bootNet != null, net = MatchClient(matchId, realtime)))
    }

    /**
     * Вернуть игрока в его партию, если она есть.
     *
     * Спрашиваем сервер, а не память: партия могла остаться после закрытия игры
     * на другом устройстве, и соперник всё это время ждёт хода.
     */
    fun resumeNetGame(onNothing: () -> Unit, onFail: (String) -> Unit) {
        Http.get("${NetConfig.API_BASE}/matches/current", Auth.accessToken) { result ->
            when {
                result.status == 0 -> onFail("offline")
                !result.ok -> onFail(result.error ?: "server_error")
                else -> {
                    val match = result.json.obj("match")
                    val id = match.str("matchId")
                    // Законченную партию сервер отдаёт ещё некоторое время: её
                    // показывать незачем, игрок уже видел исход.
                    if (id == null || match.str("status") != "playing") onNothing() else startNetGame(id)
                }
            }
        }
    }

    /** Экраны лёгкие, кэшировать их незачем: предыдущий сразу освобождаем. */
    private fun swapTo(next: Screen) {
        val previous = screen
        setScreen(next)
        previous?.dispose()
    }

    override fun dispose() {
        screen?.dispose()
        assets.dispose()
        sound.dispose()
        batch.dispose()
    }
}
