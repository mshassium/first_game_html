package com.first.game.domain.net

import com.first.game.domain.EndReason
import com.first.game.domain.Fixtures
import com.first.game.domain.GameEvent
import com.first.game.domain.Letter
import com.first.game.domain.Outcome
import com.first.game.domain.Phase
import com.first.game.domain.Side
import com.first.game.domain.Traps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Отражение перспективы. На нём держится то, что игровой экран не отличает
 * сетевую партию от одиночной: каждый клиент видит себя стороной YOU.
 */
class MirrorTest {

    private val base = Fixtures.state(
        turn = Side.YOU,
        youHand = "FIT",
        aiHand = "RS",
        youSpace = "F",
        aiSpace = "TT",
        youDiscard = "I",
    )

    @Test
    fun `двойное отражение возвращает исходное состояние`() {
        val (states, _) = Duel.play()
        for (state in states) {
            assertEquals(state, Mirror.flip(Mirror.flip(state)))
        }
    }

    @Test
    fun `двойное отражение возвращает исходное событие`() {
        val (_, events) = Duel.play()
        for (event in events) {
            assertEquals(event, Mirror.flip(Mirror.flip(event)))
        }
    }

    @Test
    fun `место A получает состояние как есть, место B — отражённым`() {
        val (states, _) = Duel.play()
        val state = states.last()
        assertEquals(state, Mirror.forSeat(state, Seat.A))
        assertEquals(Mirror.flip(state), Mirror.forSeat(state, Seat.B))
    }

    @Test
    fun `после отражения стороны, запреты и ловушки меняются местами`() {
        val state = base.copy(
            traps = Traps(forbidOnYou = Letter.S, forbidOnAi = Letter.T, trapsOnYou = 1, trapsOnAi = 2),
        )
        val flipped = Mirror.flip(state)

        assertEquals(state.you, flipped.ai)
        assertEquals(state.ai, flipped.you)
        assertEquals(state.turn.other, flipped.turn)
        assertEquals(state.firstPlayer.other, flipped.firstPlayer)
        assertEquals(Letter.T, flipped.traps.forbidOnYou)
        assertEquals(Letter.S, flipped.traps.forbidOnAi)
        assertEquals(2, flipped.traps.trapsOnYou)
        assertEquals(1, flipped.traps.trapsOnAi)
        // Фаза и номер хода от перспективы не зависят.
        assertEquals(state.phase, flipped.phase)
        assertEquals(state.turnNumber, flipped.turnNumber)
    }

    @Test
    fun `победа соперника выглядит поражением с другой стороны`() {
        val won = base.copy(
            phase = Phase.GAME_OVER,
            pending = null,
            outcome = Outcome(Side.YOU, EndReason.FIRST_SET),
        )
        val flipped = Mirror.flip(won)
        assertEquals(Side.AI, flipped.outcome?.winner)
        assertEquals(EndReason.FIRST_SET, flipped.outcome?.reason)
        assertNotEquals(won.outcome, flipped.outcome)
    }

    @Test
    fun `броски кубиков переезжают вместе со сторонами`() {
        val started = GameEvent.GameStarted(firstPlayer = Side.YOU, youRoll = 6, aiRoll = 2)
        val flipped = Mirror.flip(started) as GameEvent.GameStarted
        assertEquals(Side.AI, flipped.firstPlayer)
        assertEquals(2, flipped.youRoll)
        assertEquals(6, flipped.aiRoll)
    }

    @Test
    fun `ожидаемый выбор меняет владельца`() {
        val (states, _) = Duel.play()
        val pendingState = states.first { it.pending != null }
        val flipped = Mirror.flip(pendingState)
        assertEquals(pendingState.pending?.side?.other, flipped.pending?.side)
        assertEquals(pendingState.pending?.options, flipped.pending?.options)
    }
}
