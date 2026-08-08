package com.first.game.domain

import com.first.game.domain.Fixtures.has
import com.first.game.domain.Letter.R
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WinConditionTest {

    private val engine = Fixtures.engine()

    @Test
    fun `полный набор F-I-R-S-T приносит победу независимо от порядка`() {
        val start = Fixtures.state(youHand = "F", youSpace = "TSRI")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Outcome(Side.YOU, EndReason.FIRST_SET), played.state.outcome)
        assertEquals(Phase.GAME_OVER, played.state.phase)
        assertTrue(played.events.has<GameEvent.GameEnded>())
    }

    @Test
    fun `пять одинаковых букв приносят победу`() {
        val start = Fixtures.state(youHand = "R", youSpace = "RRRR")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Outcome(Side.YOU, EndReason.FIVE_OF_A_KIND), played.state.outcome)
    }

    @Test
    fun `победа фиксируется до модалки выбора эффекта`() {
        val start = Fixtures.state(youHand = "F", youSpace = "ISRT")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Phase.GAME_OVER, played.state.phase)
        assertNull(played.state.pending, "лишний выбор игроку не показываем")
    }

    @Test
    fun `победа ИИ фиксируется так же`() {
        val start = Fixtures.state(turn = Side.AI, aiHand = "T", aiSpace = "FIRS")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Outcome(Side.AI, EndReason.FIRST_SET), played.state.outcome)
    }

    @Test
    fun `пустая колода при обязательном доборе — поражение владельца колоды`() {
        val start = Fixtures.state(youHand = "T", aiDeck = emptyList())
        val played = engine.apply(start, Command.PlayCard(0))

        // Ход перешёл к ИИ, добирать нечем.
        assertEquals(Outcome(Side.YOU, EndReason.DECK_OUT), played.state.outcome)
    }

    @Test
    fun `после завершения партии команды игнорируются`() {
        val start = Fixtures.state(youHand = "F", youSpace = "TSRI")
        val finished = engine.apply(start, Command.PlayCard(0)).state

        val after = engine.apply(finished, Command.PlayCard(0))

        assertEquals(finished, after.state)
        assertTrue(after.events.isEmpty())
    }

    @Test
    fun `запрещённая карта не приносит победу`() {
        val start = Fixtures.state(
            youHand = "R",
            youSpace = "RRRR",
            youDeck = List(10) { R },
            traps = Traps(forbidOnYou = R),
        )
        val played = engine.apply(start, Command.PlayCard(0))

        assertNull(played.state.outcome, "карта ушла в сброс, набор не собран")
        assertEquals(4, played.state.you.space.size)
    }
}
