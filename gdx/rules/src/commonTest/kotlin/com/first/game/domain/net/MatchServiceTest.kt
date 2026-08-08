package com.first.game.domain.net

import com.first.game.domain.Command
import com.first.game.domain.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Сервис глазами сервера: строки на входе, строки на выходе.
 *
 * Партия здесь играется так же, как её будет играть настоящий сервер: каждое
 * место решает **по своему виду**, а не по полному состоянию — иначе тест не
 * заметил бы, что вид непригоден для игры.
 */
class MatchServiceTest {

    private val seed = "проверочная партия"

    @Test
    fun `партия целиком проходит через сервис`() {
        var state = MatchService.newMatch(seed).state
        var version = 0

        while (!MatchService.isOver(state)) {
            val seat = assertNotNull(MatchService.actingSeat(state), "некому ходить")
            val view = StateCodec.decode(assertNotNull(MatchService.viewFor(state, seat)))
            // В своей перспективе игрок всегда YOU — на этом стоит игровой экран.
            assertEquals(Side.YOU, view.actingSide)

            val result = MatchService.apply(
                stateRaw = state,
                seat = seat,
                commandRaw = CommandCodec.encode(Duel.firstMove(view)),
                seed = "$seed#${++version}",
            )
            assertTrue(result.ok, "ход места $seat отбит: ${result.error}")
            assertTrue(result.events.isNotEmpty(), "ход прошёл, но показать нечего")
            assertNotNull(MatchService.eventsFor(result.events, seat))
            state = result.state
            assertTrue(version < 500, "партия не заканчивается")
        }

        val winner = assertNotNull(MatchService.winnerSeat(state), "партия кончилась без победителя")
        assertNull(MatchService.actingSeat(state), "в законченной партии ходить некому")
        // Победитель видит победу своей стороны, проигравший — чужой.
        val winnerView = StateCodec.decode(assertNotNull(MatchService.viewFor(state, winner)))
        val loserView = StateCodec.decode(assertNotNull(MatchService.viewFor(state, winner.other)))
        assertEquals(Side.YOU, winnerView.outcome?.winner)
        assertEquals(Side.AI, loserView.outcome?.winner)
    }

    @Test
    fun `виды двух мест согласованы между собой`() {
        val state = MatchService.newMatch(seed).state
        val full = StateCodec.decode(state)
        val viewA = StateCodec.decode(assertNotNull(MatchService.viewFor(state, Seat.A)))
        val viewB = StateCodec.decode(assertNotNull(MatchService.viewFor(state, Seat.B)))

        // Своя рука — настоящая, чужая — той же длины, но скрытая.
        assertEquals(full.you.hand, viewA.you.hand)
        assertEquals(full.ai.hand, viewB.you.hand)
        assertEquals(viewA.you.hand.size, viewB.ai.hand.size)
        assertEquals(viewB.you.hand.size, viewA.ai.hand.size)
        assertNotEquals(viewB.you.hand, viewA.ai.hand, "рука места B уехала месту A как есть")

        // Чей ход — вопрос без перспективы: ровно одно место ходит первым.
        assertNotEquals(viewA.turn, viewB.turn)
    }

    @Test
    fun `чужим ходом сходить нельзя`() {
        val state = MatchService.newMatch(seed).state
        val idle = assertNotNull(MatchService.actingSeat(state)).other

        val result = MatchService.apply(state, idle, "play;0", "$seed#1")
        assertFalse(result.ok)
        assertEquals(MatchError.NOT_YOUR_TURN, result.error)
        assertEquals(state, result.state, "отбитый ход не должен менять состояние")
    }

    @Test
    fun `невозможный ход отличается от чужого`() {
        val state = MatchService.newMatch(seed).state
        val seat = assertNotNull(MatchService.actingSeat(state))

        val result = MatchService.apply(state, seat, CommandCodec.encode(Command.PlayCard(99)), "$seed#1")
        assertFalse(result.ok)
        assertEquals(MatchError.ILLEGAL_COMMAND, result.error)
        assertEquals(state, result.state)
    }

    @Test
    fun `битые данные не роняют сервис`() {
        val state = MatchService.newMatch(seed).state
        val seat = assertNotNull(MatchService.actingSeat(state))

        assertEquals(MatchError.BAD_STATE, MatchService.apply("мусор", seat, "play;0", seed).error)
        assertEquals(MatchError.BAD_COMMAND, MatchService.apply(state, seat, "сдаться", seed).error)
        assertNull(MatchService.viewFor("мусор", Seat.A))
        assertNull(MatchService.eventsFor("ЧТОТО;YOU", Seat.A))
        assertFalse(MatchService.isOver("мусор"))
        assertNull(MatchService.actingSeat("мусор"))
    }

    @Test
    fun `в законченной партии ходов больше не принимают`() {
        var state = MatchService.newMatch(seed).state
        var version = 0
        while (!MatchService.isOver(state)) {
            val seat = assertNotNull(MatchService.actingSeat(state))
            val view = StateCodec.decode(assertNotNull(MatchService.viewFor(state, seat)))
            state = MatchService.apply(
                state, seat, CommandCodec.encode(Duel.firstMove(view)), "$seed#${++version}",
            ).state
        }

        val result = MatchService.apply(state, Seat.A, "play;0", "$seed#последний")
        assertFalse(result.ok)
        assertEquals(MatchError.MATCH_FINISHED, result.error)
    }

    @Test
    fun `зерно из строки устойчиво и различает версии`() {
        assertEquals(123L, MatchService.seedOf("123"))
        assertEquals(MatchService.seedOf("матч#7"), MatchService.seedOf("матч#7"))
        assertNotEquals(MatchService.seedOf("матч#7"), MatchService.seedOf("матч#8"))
        // Разное зерно — разная раздача, иначе сервер выдавал бы один расклад.
        assertNotEquals(MatchService.newMatch("матч#1").state, MatchService.newMatch("матч#2").state)
    }
}
