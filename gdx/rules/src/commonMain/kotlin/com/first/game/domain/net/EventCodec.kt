package com.first.game.domain.net

import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.EndReason
import com.first.game.domain.GameEvent
import com.first.game.domain.Letter
import com.first.game.domain.Outcome
import com.first.game.domain.PendingChoice
import com.first.game.domain.Side

/**
 * Строковое представление списка событий.
 *
 * События — это то, что экран проигрывает анимацией; по сети они едут вместе
 * с новым состоянием, иначе ход соперника появился бы на столе рывком.
 *
 * Одно событие — одна строка вида `ТЕГ;поле;поле`. Пустой список кодируется
 * пустой строкой.
 */
object EventCodec {

    fun encode(events: List<GameEvent>): String = events.joinToString("\n", transform = ::encodeOne)

    fun decode(raw: String): List<GameEvent> =
        if (raw.isEmpty()) emptyList() else raw.split('\n').filter { it.isNotEmpty() }.map(::decodeOne)

    fun decodeOrNull(raw: String): List<GameEvent>? = runCatching { decode(raw) }.getOrNull()

    fun encodeOne(event: GameEvent): String = when (event) {
        is GameEvent.GameStarted -> "START;${event.firstPlayer};${event.youRoll};${event.aiRoll}"
        is GameEvent.CardDealt -> "DEAL;${event.side};${event.letter}"
        is GameEvent.TurnBegan -> "TURN;${event.side};${event.turnNumber}"
        is GameEvent.CardDrawn -> "DRAW;${event.side};${event.letter};${event.deckLeft}"
        is GameEvent.HandOverflow -> "OVERFLOW;${event.side};${event.letter};${event.handIndex}"
        is GameEvent.TrapTriggered ->
            "TRAPHIT;${event.side};${event.letter};${event.handIndex};${event.trapsLeft}"
        is GameEvent.TrapFizzled -> "TRAPMISS;${event.side};${event.trapsLeft}"
        is GameEvent.TurnSkipped -> "SKIP;${event.side}"
        is GameEvent.CardPlayed -> "PLAY;${event.side};${event.letter};${event.handIndex}"
        is GameEvent.CardForbidden -> "BLOCKED;${event.side};${event.letter};${event.handIndex}"
        is GameEvent.ForbidSet -> "FORBID;${event.by};${event.letter}"
        is GameEvent.ForbidBroken -> "UNFORBID;${event.on};${event.letter}"
        is GameEvent.CardRecovered -> "RECOVER;${event.side};${event.letter}"
        is GameEvent.CardStolen -> "STEAL;${event.victim};${event.letter};${event.spaceIndex}"
        is GameEvent.TrapSet -> "TRAP;${event.on};${event.trapCount}"
        is GameEvent.EffectFizzled -> "FIZZLE;${event.side};${event.letter}"
        is GameEvent.ChoiceRequired -> {
            val options = event.choice.options.joinToString(",") { "${it.index}:${it.letter}" }
            "CHOICE;${event.choice.side};${event.choice.kind};$options"
        }
        is GameEvent.TurnEnded -> "ENDTURN;${event.side}"
        is GameEvent.GameEnded -> "END;${event.outcome.winner};${event.outcome.reason}"
    }

    fun decodeOne(line: String): GameEvent {
        val p = line.split(';')
        fun side(index: Int) = Side.valueOf(p[index])
        fun letter(index: Int) = Letter.valueOf(p[index])
        fun int(index: Int) = p[index].toInt()

        return when (p[0]) {
            "START" -> GameEvent.GameStarted(side(1), int(2), int(3))
            "DEAL" -> GameEvent.CardDealt(side(1), letter(2))
            "TURN" -> GameEvent.TurnBegan(side(1), int(2))
            "DRAW" -> GameEvent.CardDrawn(side(1), letter(2), int(3))
            "OVERFLOW" -> GameEvent.HandOverflow(side(1), letter(2), int(3))
            "TRAPHIT" -> GameEvent.TrapTriggered(side(1), letter(2), int(3), int(4))
            "TRAPMISS" -> GameEvent.TrapFizzled(side(1), int(2))
            "SKIP" -> GameEvent.TurnSkipped(side(1))
            "PLAY" -> GameEvent.CardPlayed(side(1), letter(2), int(3))
            "BLOCKED" -> GameEvent.CardForbidden(side(1), letter(2), int(3))
            "FORBID" -> GameEvent.ForbidSet(side(1), letter(2))
            "UNFORBID" -> GameEvent.ForbidBroken(side(1), letter(2))
            "RECOVER" -> GameEvent.CardRecovered(side(1), letter(2))
            "STEAL" -> GameEvent.CardStolen(side(1), letter(2), int(3))
            "TRAP" -> GameEvent.TrapSet(side(1), int(2))
            "FIZZLE" -> GameEvent.EffectFizzled(side(1), letter(2))
            "CHOICE" -> GameEvent.ChoiceRequired(
                PendingChoice(
                    side = side(1),
                    kind = ChoiceKind.valueOf(p[2]),
                    options = p[3].split(',').filter { it.isNotEmpty() }.map { option ->
                        val (index, value) = option.split(':')
                        ChoiceOption(index.toInt(), Letter.valueOf(value))
                    },
                ),
            )
            "ENDTURN" -> GameEvent.TurnEnded(side(1))
            "END" -> GameEvent.GameEnded(Outcome(side(1), EndReason.valueOf(p[2])))
            else -> throw IllegalArgumentException("неизвестное событие: ${p[0]}")
        }
    }
}
