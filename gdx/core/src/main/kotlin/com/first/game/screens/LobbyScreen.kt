package com.first.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.first.game.FirstGame
import com.first.game.i18n.Strings
import com.first.game.net.NetResult
import com.first.game.net.RoomInfo
import com.first.game.net.RoomsApi
import com.first.game.ui.Palette
import com.first.game.ui.drawCover
import com.first.game.ui.menuButton
import ktx.app.KtxScreen

/**
 * Ожидание соперника в своей комнате.
 *
 * Код комнаты показан крупно: его диктуют другу голосом или пересылают, и он
 * должен читаться с одного взгляда. Комната опрашивается по таймеру — живая
 * подписка ради одного события того не стоит.
 */
class LobbyScreen(private val game: FirstGame, private val room: RoomInfo) : KtxScreen {

    private val stage = Stage(ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT), game.batch)
    private val theme = game.theme
    private val root = Table()

    private var pollTimer = 0f
    private var message = ""
    private var leaving = false

    init {
        stage.addActor(root)
        build()
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
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

        if (!leaving) {
            pollTimer += delta
            if (pollTimer >= POLL_SECONDS) {
                pollTimer = 0f
                checkRoom()
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

    /** Как только сервер посадил гостя и начал партию, уходим за стол. */
    private fun checkRoom() {
        RoomsApi.state(room.id) { result ->
            when (result) {
                is NetResult.Ok -> {
                    val matchId = result.value.matchId
                    if (matchId != null) {
                        game.startNetGame(matchId)
                    } else if (result.value.status == "closed") {
                        message = errorText("room_not_found")
                        build()
                    }
                }
                is NetResult.Fail -> {
                    // Сеть моргнула — не повод выкидывать хозяина из комнаты,
                    // следующий опрос через пару секунд.
                    Gdx.app.log("net", "комната ${room.code}: ${result.code}")
                    if (result.code != "offline") {
                        message = errorText(result.code)
                        build()
                    }
                }
            }
        }
    }

    private fun leave() {
        leaving = true
        RoomsApi.leave(room.id) { game.showOnline() }
    }

    private fun build() {
        root.clear()
        root.setFillParent(true)
        root.center()

        val worldHeight = stage.viewport.worldHeight
        val worldWidth = stage.viewport.worldWidth
        val gap = worldHeight * 0.02f
        val capSize = (worldHeight * 0.05f).coerceIn(26f, 40f)
        val buttonWidth = (worldWidth * 0.3f).coerceIn(240f, 420f)
        val buttonHeight = (worldHeight * 0.085f).coerceIn(44f, 70f)

        root.add(Label(Strings["online.waiting.title"], theme.titleLarge).apply {
            color = Palette.GOLD_LIGHT
            setAlignment(Align.center)
        }).padBottom(gap).row()

        root.add(Label(Strings["online.code"], theme.bodyMuted)).padBottom(gap * 0.3f).row()
        root.add(Label(room.code, theme.titleLarge).apply { color = Palette.GOLD_LIGHT })
            .padBottom(gap).row()

        root.add(Label(message.ifEmpty { Strings["online.waiting.body"] }, theme.body).apply {
            setAlignment(Align.center)
            wrap = true
        }).width(worldWidth * 0.5f).padBottom(gap * 1.5f).row()

        root.add(
            menuButton(theme, game.assets, game.sound, Strings["online.leave"], null, capSize) { leave() },
        ).size(buttonWidth, buttonHeight).row()
    }

    private companion object {
        const val WORLD_WIDTH = 1280f
        const val WORLD_HEIGHT = 720f

        /** Чаще незачем: партия начинается ровно один раз. */
        const val POLL_SECONDS = 2f
    }
}
