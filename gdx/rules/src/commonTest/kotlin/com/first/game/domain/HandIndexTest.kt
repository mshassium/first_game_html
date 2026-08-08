package com.first.game.domain

import com.first.game.domain.Fixtures.first
import com.first.game.domain.Letter.R
import com.first.game.domain.Letter.T
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Место карты в руке в событиях ухода.
 *
 * Индекс нужен экрану: одинаковых букв в руке бывает несколько, и без него
 * анимация уносила на стол первую карту такой буквы, а не ту, которую разыграли.
 */
class HandIndexTest {

    private val engine = Fixtures.engine()

    @Test
    fun `разыгранная карта помнит своё место в руке при одинаковых буквах`() {
        val start = Fixtures.state(youHand = "TFFT")
        val played = engine.apply(start, Command.PlayCard(3))

        val event = played.events.first<GameEvent.CardPlayed>()
        assertEquals(T, event.letter)
        assertEquals(3, event.handIndex, "уйти должна четвёртая карта, а не первая такая же")
        assertEquals(Fixtures.cards("TFF"), played.state.you.hand)
    }

    @Test
    fun `запрещённая карта помнит своё место в руке`() {
        val start = Fixtures.state(
            youHand = "IFI",
            youDeck = List(20) { R },
            traps = Traps(forbidOnYou = Letter.I),
        )
        val played = engine.apply(start, Command.PlayCard(2))

        assertEquals(2, played.events.first<GameEvent.CardForbidden>().handIndex)
    }

    @Test
    fun `сброшенная по ловушке карта помнит своё место в руке`() {
        val start = Fixtures.state(youHand = "I", aiHand = "IFI", traps = Traps(trapsOnAi = 1))
        // Ход уходит к ИИ, и на его стороне срабатывает ловушка: выбор делаем за него.
        val played = engine.apply(start, Command.PlayCard(0))
        val options = played.state.pending?.options ?: error("Нет выбора по ловушке")
        val discarded = engine.apply(played.state, Command.ChooseOption(options.lastIndex))

        val event = discarded.events.first<GameEvent.TrapTriggered>()
        assertEquals(Side.AI, event.side)
        assertEquals(options.last().index, event.handIndex)
    }

    @Test
    fun `лишняя карта уходит в сброс со своего места в конце руки`() {
        val start = Fixtures.state(
            youHand = "T",
            aiHand = "FIRSTFI", // ровно 7
            aiDeck = List(10) { R },
        )
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Rules.HAND_LIMIT, played.events.first<GameEvent.HandOverflow>().handIndex)
    }
}
