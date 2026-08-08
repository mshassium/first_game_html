package com.first.game.domain.net

import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.EndReason
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Outcome
import com.first.game.domain.PendingChoice
import com.first.game.domain.Phase
import com.first.game.domain.Side
import com.first.game.domain.SideState
import com.first.game.domain.Traps

/**
 * Строковое представление состояния партии.
 *
 * Один кодек на всех: им пишется отложенная партия на диск, им же сервер
 * мультиплеера хранит состояние в базе, и им же клиент разбирает присланный вид.
 * Разойтись форматам негде — код общий и компилируется в JVM и в JS.
 *
 * Состояние — плоские перечисления и списки букв, поэтому кодируется строкой,
 * а не тащит за собой библиотеку сериализации: kotlinx.serialization пришлось бы
 * протаскивать ещё и в TeaVM-сборку.
 *
 * Формат (строки разделены `\n`):
 * ```
 * 2                                  версия
 * deck;hand;space;discard            сторона YOU, буквы через запятую
 * deck;hand;space;discard            сторона AI
 * turn;firstPlayer;firstTurnDone;turnNumber
 * forbidOnYou;forbidOnAi;trapsOnYou;trapsOnAi
 * phase
 * side;kind;index:letter,...         ожидаемый выбор; пустая строка, если его нет
 * winner;reason                      исход; пустая строка, если партия идёт
 * ```
 */
object StateCodec {

    const val VERSION = 2

    /** Сколько строк занимает закодированное состояние. */
    const val LINES = 8

    fun encode(state: GameState): String = buildString {
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
        append(encodeOutcome(state.outcome))
    }

    /** Разбор строки. Бросает при чужой версии и при любой порче данных. */
    fun decode(raw: String): GameState = decodeLines(raw.split('\n'))

    /** То же, но битые данные превращаются в null, а не в исключение. */
    fun decodeOrNull(raw: String): GameState? = runCatching { decode(raw) }.getOrNull()

    /**
     * Разбор состояния из готового списка строк — нужен тем, кто хранит его
     * внутри своего формата (например, `SaveGame` дописывает время партии).
     */
    fun decodeLines(lines: List<String>): GameState {
        require(lines.size >= LINES) { "состояние занимает $LINES строк, дано ${lines.size}" }
        require(lines[0].toIntOrNull() == VERSION) { "чужая версия состояния: ${lines[0]}" }

        val head = lines[3].split(';')
        require(head.size >= 4) { "испорченная строка хода" }
        val traps = lines[4].split(';')
        require(traps.size >= 4) { "испорченная строка ловушек" }

        return GameState(
            you = decodeSide(lines[1]),
            ai = decodeSide(lines[2]),
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
            outcome = decodeOutcome(lines[7]),
        )
    }

    // --- стороны -------------------------------------------------------------

    private fun encodeSide(side: SideState): String = listOf(
        side.deck, side.hand, side.space, side.discard,
    ).joinToString(";") { pile -> pile.joinToString(",") { it.name } }

    private fun decodeSide(line: String): SideState {
        val piles = line.split(';')
        require(piles.size >= 4) { "у стороны должно быть четыре стопки" }
        val decoded = piles.map(::decodePile)
        return SideState(deck = decoded[0], hand = decoded[1], space = decoded[2], discard = decoded[3])
    }

    private fun decodePile(pile: String): List<Letter> =
        if (pile.isEmpty()) emptyList() else pile.split(',').map { Letter.valueOf(it) }

    // --- ожидаемый выбор -----------------------------------------------------

    private fun encodePending(pending: PendingChoice?): String {
        if (pending == null) return ""
        val options = pending.options.joinToString(",") { "${it.index}:${it.letter}" }
        return "${pending.side};${pending.kind};$options"
    }

    private fun decodePending(line: String): PendingChoice? {
        if (line.isEmpty()) return null
        val parts = line.split(';')
        require(parts.size >= 3) { "испорченная строка выбора" }
        val options = parts[2].split(',').filter { it.isNotEmpty() }.map { option ->
            val (index, letter) = option.split(':')
            ChoiceOption(index.toInt(), Letter.valueOf(letter))
        }
        return PendingChoice(Side.valueOf(parts[0]), ChoiceKind.valueOf(parts[1]), options)
    }

    // --- исход ---------------------------------------------------------------

    private fun encodeOutcome(outcome: Outcome?): String =
        if (outcome == null) "" else "${outcome.winner};${outcome.reason}"

    private fun decodeOutcome(line: String): Outcome? {
        if (line.isEmpty()) return null
        val parts = line.split(';')
        require(parts.size >= 2) { "испорченная строка исхода" }
        return Outcome(Side.valueOf(parts[0]), EndReason.valueOf(parts[1]))
    }
}
