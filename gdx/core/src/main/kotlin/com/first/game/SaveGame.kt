package com.first.game

import com.badlogic.gdx.Gdx
import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.PendingChoice
import com.first.game.domain.Phase
import com.first.game.domain.Side
import com.first.game.domain.SideState
import com.first.game.domain.Traps

/** Отложенная партия: состояние и сколько она уже длится. */
data class SavedGame(val state: GameState, val elapsedSeconds: Float)

/**
 * Сохранение незаконченной партии в настройки.
 *
 * Состояние — плоские перечисления и списки букв, поэтому кодируется строкой,
 * а не тащит за собой библиотеку сериализации. Формат версионирован: при смене
 * модели старое сохранение просто отбрасывается, а не роняет игру.
 *
 * Чего здесь нет: состояния генератора случайных чисел — `kotlin.random.Random`
 * его наружу не отдаёт. При продолжении колода тасуется от нового зерна. На уже
 * разложенные карты это не влияет, они лежат в состоянии; воспроизвести партию
 * по сиду после сохранения нельзя.
 */
object SaveGame {

    private const val VERSION = 1
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
        return runCatching { decode(raw) }.getOrNull().also { if (it == null) clear() }
    }

    // --- кодирование ---------------------------------------------------------

    internal fun encode(state: GameState, elapsedSeconds: Float): String = buildString {
        append(VERSION).append('\n')
        append(encodeSide(state.you)).append('\n')
        append(encodeSide(state.ai)).append('\n')
        append(state.turn).append(';').append(state.firstPlayer).append(';')
            .append(state.firstTurnDone).append(';').append(state.turnNumber).append('\n')
        append(state.traps.forbidOnYou ?: "").append(';')
            .append(state.traps.forbidOnAi ?: "").append(';')
            .append(state.traps.trapsOnYou).append(';')
            .append(state.traps.trapsOnAi).append('\n')
        append(state.phase).append('\n')
        append(encodePending(state.pending)).append('\n')
        append(elapsedSeconds)
    }

    private fun encodeSide(side: SideState): String = listOf(
        side.deck, side.hand, side.space, side.discard,
    ).joinToString(";") { pile -> pile.joinToString(",") { it.name } }

    private fun encodePending(pending: PendingChoice?): String {
        if (pending == null) return ""
        val options = pending.options.joinToString(",") { "${it.index}:${it.letter}" }
        return "${pending.side};${pending.kind};$options"
    }

    // --- разбор --------------------------------------------------------------

    internal fun decode(raw: String): SavedGame? {
        val lines = raw.split('\n')
        if (lines.size < 7 || lines[0].toIntOrNull() != VERSION) return null

        val you = decodeSide(lines[1])
        val ai = decodeSide(lines[2])
        val head = lines[3].split(';')
        val traps = lines[4].split(';')

        return SavedGame(
            state = GameState(
                you = you,
                ai = ai,
                turn = Side.valueOf(head[0]),
                firstPlayer = Side.valueOf(head[1]),
                firstTurnDone = head[2].toBooleanStrict(),
                turnNumber = head[3].toInt(),
                traps = Traps(
                    forbidOnYou = traps[0].takeIf { it.isNotEmpty() }?.let { Letter.valueOf(it) },
                    forbidOnAi = traps[1].takeIf { it.isNotEmpty() }?.let { Letter.valueOf(it) },
                    trapsOnYou = traps[2].toInt(),
                    trapsOnAi = traps[3].toInt(),
                ),
                phase = Phase.valueOf(lines[5]),
                pending = decodePending(lines[6]),
                outcome = null,
            ),
            elapsedSeconds = lines.getOrNull(7)?.toFloatOrNull() ?: 0f,
        )
    }

    private fun decodeSide(line: String): SideState {
        val piles = line.split(';').map { pile ->
            if (pile.isEmpty()) emptyList() else pile.split(',').map { Letter.valueOf(it) }
        }
        return SideState(deck = piles[0], hand = piles[1], space = piles[2], discard = piles[3])
    }

    private fun decodePending(line: String): PendingChoice? {
        if (line.isBlank()) return null
        val parts = line.split(';')
        val options = parts[2].split(',').filter { it.isNotEmpty() }.map {
            val (index, letter) = it.split(':')
            ChoiceOption(index.toInt(), Letter.valueOf(letter))
        }
        return PendingChoice(Side.valueOf(parts[0]), ChoiceKind.valueOf(parts[1]), options)
    }
}
