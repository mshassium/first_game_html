package com.first.game.domain.net

import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.Traps

/**
 * Перевод состояния и событий в перспективу другого места.
 *
 * Сервер считает партию за место A. Клиенту места B всё отражается: его сторона
 * становится [com.first.game.domain.Side.YOU], сторона соперника — [com.first.game.domain.Side.AI].
 * Благодаря этому игровой экран одинаков и в одиночной игре, и в сетевой.
 *
 * Отражение — инволюция: `flip(flip(x)) == x`. Это проверяется тестом, и на этом
 * держится корректность всей раздачи состояний.
 */
object Mirror {

    /** Состояние в перспективе [seat]. Для места A возвращается как есть. */
    fun forSeat(state: GameState, seat: Seat): GameState =
        if (seat == Seat.A) state else flip(state)

    /** События в перспективе [seat]. */
    fun forSeat(events: List<GameEvent>, seat: Seat): List<GameEvent> =
        if (seat == Seat.A) events else events.map(::flip)

    fun flip(state: GameState): GameState = GameState(
        you = state.ai,
        ai = state.you,
        turn = state.turn.other,
        firstPlayer = state.firstPlayer.other,
        firstTurnDone = state.firstTurnDone,
        traps = Traps(
            forbidOnYou = state.traps.forbidOnAi,
            forbidOnAi = state.traps.forbidOnYou,
            trapsOnYou = state.traps.trapsOnAi,
            trapsOnAi = state.traps.trapsOnYou,
        ),
        phase = state.phase,
        pending = state.pending?.let { it.copy(side = it.side.other) },
        outcome = state.outcome?.let { it.copy(winner = it.winner.other) },
        turnNumber = state.turnNumber,
    )

    fun flip(event: GameEvent): GameEvent = when (event) {
        // Броски кубиков меняются местами вместе со сторонами: иначе игрок
        // места B увидел бы на своём портрете чужой результат.
        is GameEvent.GameStarted -> event.copy(
            firstPlayer = event.firstPlayer.other,
            youRoll = event.aiRoll,
            aiRoll = event.youRoll,
        )
        is GameEvent.CardDealt -> event.copy(side = event.side.other)
        is GameEvent.TurnBegan -> event.copy(side = event.side.other)
        is GameEvent.CardDrawn -> event.copy(side = event.side.other)
        is GameEvent.HandOverflow -> event.copy(side = event.side.other)
        is GameEvent.TrapTriggered -> event.copy(side = event.side.other)
        is GameEvent.TrapFizzled -> event.copy(side = event.side.other)
        is GameEvent.TurnSkipped -> event.copy(side = event.side.other)
        is GameEvent.CardPlayed -> event.copy(side = event.side.other)
        is GameEvent.CardForbidden -> event.copy(side = event.side.other)
        is GameEvent.ForbidSet -> event.copy(by = event.by.other)
        is GameEvent.ForbidBroken -> event.copy(on = event.on.other)
        is GameEvent.CardRecovered -> event.copy(side = event.side.other)
        is GameEvent.CardStolen -> event.copy(victim = event.victim.other)
        is GameEvent.TrapSet -> event.copy(on = event.on.other)
        is GameEvent.EffectFizzled -> event.copy(side = event.side.other)
        is GameEvent.ChoiceRequired ->
            event.copy(choice = event.choice.copy(side = event.choice.side.other))
        is GameEvent.TurnEnded -> event.copy(side = event.side.other)
        is GameEvent.GameEnded ->
            event.copy(outcome = event.outcome.copy(winner = event.outcome.winner.other))
    }
}
