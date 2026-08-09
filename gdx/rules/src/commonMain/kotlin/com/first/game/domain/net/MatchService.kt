package com.first.game.domain.net

import com.first.game.domain.EndReason
import com.first.game.domain.GameEngine
import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.Outcome
import com.first.game.domain.Phase
import com.first.game.domain.SeededRng
import com.first.game.domain.Side

/** Почему команда не была применена. */
enum class MatchError {
    /** Состояние в хранилище не разбирается. */
    BAD_STATE,

    /** Клиент прислал не команду. */
    BAD_COMMAND,

    /** Партия уже закончена. */
    MATCH_FINISHED,

    /** Сейчас ход другого места. */
    NOT_YOUR_TURN,

    /** Команда допустима по форме, но невозможна по правилам. */
    ILLEGAL_COMMAND,
}

/**
 * Результат работы сервиса. [state] и [events] — в системе координат места A,
 * то есть ровно то, что сервер кладёт в хранилище. Раздача по местам — [viewFor].
 */
data class MatchResult(
    val ok: Boolean,
    val error: MatchError?,
    val state: String,
    val events: String,
) {
    companion object {
        fun failure(error: MatchError, state: String = ""): MatchResult =
            MatchResult(ok = false, error = error, state = state, events = "")
    }
}

/**
 * Партия глазами сервера: строки на входе, строки на выходе.
 *
 * Серверная функция не знает правил вообще — она проверяет права доступа, зовёт
 * этот сервис и раскладывает результат по двум видам. Всё остальное считает
 * [GameEngine], тот же самый, что и в одиночной игре.
 *
 * Генератор случайных чисел воссоздаётся на каждый вызов: между вызовами живёт
 * только строка состояния, а `kotlin.random.Random` своё состояние наружу не
 * отдаёт. Поэтому сервер обязан подмешивать в зерно номер версии — иначе две
 * команды подряд дали бы один и тот же расклад.
 */
object MatchService {

    /** Новая партия: бросок кубиков, перемешивание колод, раздача. */
    fun newMatch(seed: String): MatchResult {
        val result = GameEngine(SeededRng(seedOf(seed))).newGame()
        return MatchResult(
            ok = true,
            error = null,
            state = StateCodec.encode(result.state),
            events = EventCodec.encode(result.events),
        )
    }

    /**
     * Применить команду места [seat].
     *
     * Порядок проверок важен: сначала то, что можно решить по состоянию, и только
     * потом сам движок. Так «не твой ход» отличается от «так ходить нельзя» —
     * первое бывает от гонки клиентов, второе означает баг клиента или попытку
     * сжульничать, и сервер такое логирует.
     */
    fun apply(stateRaw: String, seat: Seat, commandRaw: String, seed: String): MatchResult {
        val state = StateCodec.decodeOrNull(stateRaw)
            ?: return MatchResult.failure(MatchError.BAD_STATE)
        if (state.isOver) return MatchResult.failure(MatchError.MATCH_FINISHED, stateRaw)

        val command = CommandCodec.decodeOrNull(commandRaw)
            ?: return MatchResult.failure(MatchError.BAD_COMMAND, stateRaw)
        if (state.actingSide != seat.side) {
            return MatchResult.failure(MatchError.NOT_YOUR_TURN, stateRaw)
        }

        val result = GameEngine(SeededRng(seedOf(seed))).apply(state, command)
        // Невозможную команду движок отбивает молча: состояние прежнее, событий нет.
        if (result.events.isEmpty()) {
            return MatchResult.failure(MatchError.ILLEGAL_COMMAND, stateRaw)
        }

        return MatchResult(
            ok = true,
            error = null,
            state = StateCodec.encode(result.state),
            events = EventCodec.encode(result.events),
        )
    }

    /**
     * Завершить партию не по правилам, а по внешней причине: вышло время хода
     * или игрок сдался.
     *
     * Без этого партия заканчивалась только в базе: строка состояния оставалась
     * «идущей», версия не росла, и соперник об окончании не узнавал вовсе —
     * ждал хода, которого уже не будет.
     */
    fun finish(stateRaw: String, winner: Seat, reason: EndReason): MatchResult {
        val state = StateCodec.decodeOrNull(stateRaw)
            ?: return MatchResult.failure(MatchError.BAD_STATE)
        if (state.isOver) return MatchResult.failure(MatchError.MATCH_FINISHED, stateRaw)

        val outcome = Outcome(winner.side, reason)
        val finished = state.copy(phase = Phase.GAME_OVER, pending = null, outcome = outcome)
        return MatchResult(
            ok = true,
            error = null,
            state = StateCodec.encode(finished),
            events = EventCodec.encode(listOf(GameEvent.GameEnded(outcome))),
        )
    }

    /** Состояние в перспективе места [seat], без скрытой информации. */
    fun viewFor(stateRaw: String, seat: Seat): String? =
        StateCodec.decodeOrNull(stateRaw)
            ?.let { Redact.state(Mirror.forSeat(it, seat), Side.YOU) }
            ?.let(StateCodec::encode)

    /** События в перспективе места [seat], без скрытой информации. */
    fun eventsFor(eventsRaw: String, seat: Seat): String? =
        EventCodec.decodeOrNull(eventsRaw)
            ?.let { Redact.events(Mirror.forSeat(it, seat), Side.YOU) }
            ?.let(EventCodec::encode)

    fun isOver(stateRaw: String): Boolean = StateCodec.decodeOrNull(stateRaw)?.isOver == true

    /** Победитель как место в комнате. null, пока партия идёт. */
    fun winnerSeat(stateRaw: String): Seat? =
        StateCodec.decodeOrNull(stateRaw)?.outcome?.let { Seat.of(it.winner) }

    /** Чьей команды сейчас ждут. null, если партия закончена или состояние битое. */
    fun actingSeat(stateRaw: String): Seat? =
        StateCodec.decodeOrNull(stateRaw)?.takeIf { !it.isOver }?.let { Seat.of(it.actingSide) }

    /**
     * Зерно из строки: число берётся как есть, иначе считается устойчивый хеш
     * (FNV-1a). Устойчивый — значит одинаковый на JVM и в JS, иначе партия,
     * посчитанная сервером, не воспроизвелась бы в тестах на другой платформе.
     */
    internal fun seedOf(raw: String): Long {
        raw.toLongOrNull()?.let { return it }
        var hash = FNV_OFFSET
        for (char in raw) {
            hash = hash xor char.code.toLong()
            hash *= FNV_PRIME
        }
        return hash
    }

    private const val FNV_OFFSET = -3750763034362895579L
    private const val FNV_PRIME = 1099511628211L
}

/** Состояние без скрытой информации — то, что видит место [seat]. */
fun GameState.viewFor(seat: Seat): GameState = Redact.state(Mirror.forSeat(this, seat), Side.YOU)
