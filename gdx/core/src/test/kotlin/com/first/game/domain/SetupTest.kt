package com.first.game.domain

import com.first.game.domain.Fixtures.count
import com.first.game.domain.Fixtures.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SetupTest {

    @Test
    fun `колода состоит из 50 карт по 10 каждой буквы`() {
        val result = Fixtures.engine().newGame()
        for (side in listOf(Side.YOU, Side.AI)) {
            val state = result.state.side(side)
            assertEquals(Rules.DECK_SIZE, state.totalCards, "всего карт у $side")
            for (letter in Letter.ALL) {
                val count = (state.deck + state.hand + state.space + state.discard).count { it == letter }
                assertEquals(Rules.CARDS_PER_LETTER, count, "карт $letter у $side")
            }
        }
    }

    @Test
    fun `после раздачи по 5 карт в руке и 45 в колоде`() {
        val result = Fixtures.engine(42).newGame()
        assertEquals(Rules.START_HAND, result.state.you.hand.size)
        assertEquals(Rules.START_HAND, result.state.ai.hand.size)
        assertEquals(45, result.state.you.deck.size)
        assertEquals(45, result.state.ai.deck.size)
        assertEquals(10, result.events.count<GameEvent.CardDealt>())
    }

    @Test
    fun `кубики никогда не равны и больший определяет первого игрока`() {
        repeat(50) { seed ->
            val result = Fixtures.engine(seed.toLong()).newGame()
            val started = result.events.first<GameEvent.GameStarted>()
            assertNotEquals(started.youRoll, started.aiRoll, "сид $seed")
            val expected = if (started.youRoll > started.aiRoll) Side.YOU else Side.AI
            assertEquals(expected, started.firstPlayer, "сид $seed")
            assertEquals(expected, result.state.turn)
        }
    }

    @Test
    fun `первый игрок не берёт карту в свой первый ход, второй берёт`() {
        val engine = Fixtures.engine(7)
        val start = engine.newGame()
        val first = start.state.turn

        // Первый ход: добора не было, в руке ровно стартовые пять.
        assertEquals(Rules.START_HAND, start.state.side(first).hand.size)
        assertTrue(start.events.none { it is GameEvent.CardDrawn })

        val afterPlay = engine.apply(start.state, Command.PlayCard(0))
        val stateAfter = resolveChoices(engine, afterPlay)

        // Оппонент вступил в ход и добрал карту.
        assertEquals(Rules.START_HAND + 1, stateAfter.side(first.other).hand.size)
    }

    @Test
    fun `партия стартует в фазе розыгрыша`() {
        val result = Fixtures.engine().newGame()
        assertEquals(Phase.AWAITING_PLAY, result.state.phase)
        assertEquals(null, result.state.pending)
        assertEquals(null, result.state.outcome)
    }

    /** Отвечает на все возникшие выборы первым вариантом. */
    private fun resolveChoices(engine: GameEngine, result: EngineResult): GameState {
        var state = result.state
        var guard = 0
        while (state.phase == Phase.AWAITING_CHOICE && guard++ < 10) {
            state = engine.apply(state, Command.ChooseOption(0)).state
        }
        return state
    }
}
