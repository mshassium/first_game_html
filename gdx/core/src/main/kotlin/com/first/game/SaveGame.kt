package com.first.game

import com.badlogic.gdx.Gdx
import com.first.game.domain.GameState
import com.first.game.domain.net.StateCodec

/** Отложенная партия: состояние и сколько она уже длится. */
data class SavedGame(val state: GameState, val elapsedSeconds: Float)

/**
 * Сохранение незаконченной партии в настройки.
 *
 * Само состояние кодирует [StateCodec] — тот же код, которым пользуется сервер
 * мультиплеера. Здесь остаётся только то, чего в состоянии нет: сколько партия
 * длится. Формат версионирован: при смене модели старое сохранение отбрасывается,
 * а не роняет игру.
 *
 * Чего здесь нет: состояния генератора случайных чисел — `kotlin.random.Random`
 * его наружу не отдаёт. При продолжении колода тасуется от нового зерна. На уже
 * разложенные карты это не влияет, они лежат в состоянии; воспроизвести партию
 * по сиду после сохранения нельзя.
 */
object SaveGame {

    private const val VERSION = 2
    private const val KEY = "savedGame"

    private val prefs by lazy { Gdx.app.getPreferences("first-save") }

    val exists: Boolean get() = prefs.getString(KEY, "").isNotBlank()

    fun clear() {
        prefs.remove(KEY)
        prefs.flush()
    }

    fun save(state: GameState, elapsedSeconds: Float) {
        // Законченную партию сохранять незачем: продолжать в ней нечего.
        if (state.isOver) {
            clear()
            return
        }
        prefs.putString(KEY, encode(state, elapsedSeconds))
        prefs.flush()
    }

    fun load(): SavedGame? {
        val raw = prefs.getString(KEY, "")
        if (raw.isBlank()) return null
        return decode(raw).also { if (it == null) clear() }
    }

    /** Своя обёртка вокруг состояния: версия сохранения, время партии, само состояние. */
    internal fun encode(state: GameState, elapsedSeconds: Float): String =
        "$VERSION\n$elapsedSeconds\n${StateCodec.encode(state)}"

    /** Битое или чужое сохранение превращается в null, а не в исключение. */
    internal fun decode(raw: String): SavedGame? = runCatching {
        val lines = raw.split('\n')
        require(lines.size >= 2 + StateCodec.LINES) { "сохранение короче ожидаемого" }
        require(lines[0].toIntOrNull() == VERSION) { "чужая версия сохранения: ${lines[0]}" }
        SavedGame(
            state = StateCodec.decodeLines(lines.drop(2)),
            elapsedSeconds = lines[1].toFloatOrNull() ?: 0f,
        )
    }.getOrNull()
}
