package com.first.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.first.game.FirstGame
import com.first.game.audio.SoundManager
import com.first.game.i18n.Strings
import com.first.game.net.Auth
import com.first.game.net.NetResult
import com.first.game.net.RoomInfo
import com.first.game.net.RoomsApi
import com.first.game.ui.Overlay
import com.first.game.ui.Palette
import com.first.game.ui.drawCover
import com.first.game.ui.menuButton
import ktx.app.KtxScreen

/**
 * Список комнат: точка входа в сетевую игру.
 *
 * Экран сам приводит игрока в порядок — заводит анонимного пользователя, при
 * первом входе спрашивает имя — и только потом показывает комнаты. Если у
 * игрока осталась незаконченная партия, список вообще не открывается: он
 * возвращается за стол.
 */
class OnlineScreen(private val game: FirstGame) : KtxScreen {

    private val stage = Stage(ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT), game.batch)
    private val theme = game.theme
    private val overlay = Overlay(stage, theme, game.assets, game.sound)
    private val root = Table()

    private var rooms: List<RoomInfo> = emptyList()
    private var message: String = ""
    private var busy = true
    private var refreshTimer = 0f

    init {
        stage.addActor(root)
        build()
        connect()
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        game.sound.playMusic(SoundManager.Track.MENU)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.sound.update(delta)

        game.assets.background("bg_menu")?.let { texture ->
            game.batch.projectionMatrix = stage.viewport.camera.combined
            game.batch.begin()
            game.batch.setColor(1f, 1f, 1f, 1f)
            game.batch.drawCover(texture, stage.viewport.worldWidth, stage.viewport.worldHeight)
            game.batch.end()
        }

        // Список живёт своей жизнью: комнаты появляются и исчезают, пока игрок
        // на него смотрит.
        if (!busy && !overlay.isOpen) {
            refreshTimer += delta
            if (refreshTimer >= REFRESH_SECONDS) {
                refreshTimer = 0f
                loadRooms(silent = true)
            }
        }

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        build()
    }

    override fun dispose() = stage.dispose()

    // ------------------------------------------------------------------- вход

    private fun connect() {
        Auth.load()
        Auth.signIn { ok ->
            if (!ok) {
                busy = false
                message = Strings["online.error.offline"]
                build()
                return@signIn
            }
            if (Auth.nickname == null) askNickname() else resumeOrList()
        }
    }

    /**
     * Незаконченная партия важнее списка: игрок мог закрыть игру посреди хода,
     * и соперник всё это время ждёт.
     */
    private fun resumeOrList() {
        busy = true
        build()
        game.resumeNetGame(
            onNothing = { loadRooms(silent = false) },
            onFail = { code ->
                busy = false
                message = errorText(code)
                build()
            },
        )
    }

    private fun loadRooms(silent: Boolean) {
        if (!silent) {
            busy = true
            build()
        }
        RoomsApi.list { result ->
            busy = false
            when (result) {
                is NetResult.Ok -> {
                    rooms = result.value
                    message = if (rooms.isEmpty()) Strings["online.empty"] else ""
                }
                is NetResult.Fail -> if (!silent) message = errorText(result.code)
            }
            build()
        }
    }

    // ------------------------------------------------------------------ ввод

    private fun askNickname() {
        input(Strings["online.nickname.title"], Auth.nickname ?: "", Strings["online.nickname.hint"]) { name ->
            val trimmed = name.trim()
            if (trimmed.length < 2) return@input askNickname()
            busy = true
            build()
            RoomsApi.setNickname(trimmed) { result ->
                busy = false
                when (result) {
                    is NetResult.Ok -> {
                        Auth.nickname = result.value
                        resumeOrList()
                    }
                    is NetResult.Fail -> {
                        message = errorText(result.code)
                        build()
                    }
                }
            }
        }
    }

    private fun createRoom() {
        input(Strings["online.create.title"], defaultRoomName(), Strings["online.create.name"]) { name ->
            input(Strings["online.create.title"], "", Strings["online.create.password"]) { password ->
                busy = true
                build()
                RoomsApi.create(name.trim().ifBlank { defaultRoomName() }, password.trim()) { result ->
                    busy = false
                    when (result) {
                        is NetResult.Ok -> game.showLobby(result.value)
                        is NetResult.Fail -> {
                            message = errorText(result.code)
                            build()
                        }
                    }
                }
            }
        }
    }

    private fun joinByCode() {
        input(Strings["online.join_by_code"], "", Strings["online.code"]) { code ->
            val trimmed = code.trim().uppercase()
            if (trimmed.isEmpty()) return@input
            join(roomId = null, code = trimmed, locked = true)
        }
    }

    private fun join(roomId: String?, code: String?, locked: Boolean, password: String? = null) {
        busy = true
        build()
        RoomsApi.join(roomId, code, password) { result ->
            busy = false
            when (result) {
                is NetResult.Ok -> game.startNetGame(result.value)
                is NetResult.Fail -> {
                    // Замок виден заранее, но пароль спрашиваем только когда
                    // сервер его действительно потребовал: так не приходится
                    // угадывать, есть он у комнаты или нет.
                    if (result.code == "wrong_password" && locked) {
                        askPassword(roomId, code, retry = password != null)
                    } else {
                        message = errorText(result.code)
                        build()
                    }
                }
            }
        }
    }

    private fun askPassword(roomId: String?, code: String?, retry: Boolean) {
        val title = if (retry) Strings["online.error.wrong_password"] else Strings["online.password.title"]
        input(title, "", Strings["online.password"]) { password ->
            if (password.isBlank()) return@input
            join(roomId, code, locked = true, password = password)
        }
    }

    /**
     * Ввод текста системным полем.
     *
     * Своей клавиатуры в игре нет, а libGDX умеет открыть родное поле ввода на
     * каждой платформе: на десктопе окно, в браузере запрос, на телефоне —
     * экранную клавиатуру.
     */
    private fun input(title: String, initial: String, hint: String, onText: (String) -> Unit) {
        Gdx.input.getTextInput(
            object : Input.TextInputListener {
                override fun input(text: String) {
                    Gdx.app.postRunnable { onText(text) }
                }

                override fun canceled() = Unit
            },
            title, initial, hint,
        )
    }

    // -------------------------------------------------------------- раскладка

    private fun build() {
        root.clear()
        root.setFillParent(true)
        root.top()

        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight
        val gap = worldHeight * 0.016f
        val capSize = (worldHeight * 0.05f).coerceIn(26f, 40f)
        val rowHeight = (worldHeight * 0.09f).coerceIn(44f, 74f)
        val listWidth = (worldWidth * 0.62f).coerceIn(320f, 760f)

        root.add(Label(Strings["online.title"], theme.titleLarge).apply {
            color = Palette.GOLD_LIGHT
        }).padTop(gap * 2f).row()

        val hint = when {
            busy -> "…"
            message.isNotEmpty() -> message
            else -> Auth.nickname?.let { "${Strings["hud.you"]}: $it" } ?: ""
        }
        root.add(Label(hint, theme.bodyMuted).apply { setAlignment(Align.center) })
            .padTop(gap * 0.5f).padBottom(gap).row()

        val list = Table()
        for (room in rooms.take(MAX_ROWS)) {
            val lock = if (room.hasPassword) "  ${Strings["online.locked"]}" else ""
            val caption = "${room.name} — ${room.host}$lock"
            list.add(
                menuButton(theme, game.assets, game.sound, caption, null, capSize, listWidth) {
                    join(room.id, room.code, room.hasPassword)
                },
            ).size(listWidth, rowHeight).padBottom(gap * 0.6f).row()
        }
        root.add(list).row()

        val actions = Table()
        val buttonWidth = listWidth * 0.48f
        actions.add(
            menuButton(theme, game.assets, game.sound, Strings["online.create"], "duel", capSize, buttonWidth) { createRoom() },
        ).size(buttonWidth, rowHeight).padRight(gap)
        actions.add(
            menuButton(theme, game.assets, game.sound, Strings["online.join_by_code"], null, capSize, buttonWidth) { joinByCode() },
        ).size(buttonWidth, rowHeight).row()
        root.add(actions).padTop(gap).row()

        val bottom = Table()
        bottom.add(
            menuButton(theme, game.assets, game.sound, Strings["online.refresh"], "restart", capSize, buttonWidth) {
                loadRooms(silent = false)
            },
        ).size(buttonWidth, rowHeight).padRight(gap)
        bottom.add(
            menuButton(theme, game.assets, game.sound, Strings["common.back"], null, capSize, buttonWidth) { game.showMenu() },
        ).size(buttonWidth, rowHeight).row()
        root.add(bottom).padTop(gap * 0.6f).row()
    }

    private fun defaultRoomName(): String = Auth.nickname ?: Strings["online.title"]

    private companion object {
        const val WORLD_WIDTH = 1280f
        const val WORLD_HEIGHT = 720f

        /** Больше всё равно не помещается на экране без прокрутки. */
        const val MAX_ROWS = 5

        const val REFRESH_SECONDS = 5f
    }
}

/**
 * Текст ошибки по коду от сервера.
 *
 * Показываются не все коды: `stale_version`, `not_your_turn`, `match_finished`
 * и `illegal_command` клиент разбирает сам — просит свежий вид и продолжает,
 * игроку про них знать незачем. Незнакомый код превращается в общую фразу,
 * чтобы на экране не появилось служебное слово.
 */
internal fun errorText(code: String): String {
    val key = "online.error.$code"
    val known = Strings.has(key)
    return if (known) Strings[key] else Strings["online.error.server"]
}
