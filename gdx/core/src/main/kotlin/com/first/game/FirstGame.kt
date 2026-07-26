package com.first.game

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.first.game.assets.Assets
import com.first.game.audio.SoundManager
import com.first.game.i18n.Strings
import com.first.game.screens.GameScreen
import com.first.game.screens.MenuScreen
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
        if (bootToGame) startGame() else showMenu()
    }

    fun showMenu() = swapTo(MenuScreen(this))

    fun startGame() = swapTo(GameScreen(this, autoPlay))

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
