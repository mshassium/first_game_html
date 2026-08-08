package com.first.game.domain

import com.first.game.domain.Fixtures.has
import com.first.game.domain.Letter.R
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandLimitTest {

    private val engine = Fixtures.engine()

    @Test
    fun `добор при полной руке сбрасывает последнюю добранную карту`() {
        val start = Fixtures.state(
            youHand = "T",
            aiHand = "FIRSTFI", // ровно 7
            aiDeck = List(10) { R },
        )
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Rules.HAND_LIMIT, played.state.ai.hand.size)
        assertEquals(listOf(R), played.state.ai.discard, "в сброс ушла именно добранная карта")
        assertTrue(played.events.has<GameEvent.HandOverflow>())
    }

    @Test
    fun `рука никогда не превышает лимит`() {
        val start = Fixtures.state(youHand = "IFFFFFF", youDeck = List(10) { R })
        val played = engine.apply(start, Command.PlayCard(0))

        assertTrue(played.state.you.hand.size <= Rules.HAND_LIMIT)
    }

    @Test
    fun `возврат из сброса в полную руку не переполняет её`() {
        val start = Fixtures.state(youHand = "RFFFFFF", youDiscard = "I")
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(0))

        assertEquals(Rules.HAND_LIMIT, chosen.state.you.hand.size)
    }
}
