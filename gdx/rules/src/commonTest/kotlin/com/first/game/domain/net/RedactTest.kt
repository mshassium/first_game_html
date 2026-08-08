package com.first.game.domain.net

import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.Fixtures
import com.first.game.domain.GameEvent
import com.first.game.domain.Letter
import com.first.game.domain.PendingChoice
import com.first.game.domain.Phase
import com.first.game.domain.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Скрытая информация. Это главный тест этапа: если сюда просочится чужая карта,
 * весь смысл серверного авторитета пропадает — соперник просто посмотрит ответ
 * сервера в консоли браузера.
 */
class RedactTest {

    private val state = Fixtures.state(
        turn = Side.YOU,
        youHand = "FIT",
        aiHand = "RSTIF",
        youSpace = "F",
        aiSpace = "TT",
        youDiscard = "I",
        aiDiscard = "RS",
    )

    @Test
    fun `своя рука видна, чужая скрыта, размеры сохранены`() {
        val view = Redact.state(state)

        assertEquals(state.you.hand, view.you.hand)
        assertEquals(state.ai.hand.size, view.ai.hand.size)
        assertTrue(view.ai.hand.all { it == Redact.HIDDEN }, "чужая рука не скрыта: ${view.ai.hand}")
    }

    @Test
    fun `обе колоды скрыты — свою знать тоже нельзя`() {
        val view = Redact.state(state)

        assertEquals(state.you.deck.size, view.you.deck.size)
        assertEquals(state.ai.deck.size, view.ai.deck.size)
        assertTrue(view.you.deck.all { it == Redact.HIDDEN }, "своя колода раскрыта")
        assertTrue(view.ai.deck.all { it == Redact.HIDDEN }, "чужая колода раскрыта")
    }

    @Test
    fun `открытые стопки не трогаются`() {
        val view = Redact.state(state)

        assertEquals(state.you.space, view.you.space)
        assertEquals(state.ai.space, view.ai.space)
        assertEquals(state.you.discard, view.you.discard)
        assertEquals(state.ai.discard, view.ai.discard)
    }

    @Test
    fun `свой выбор приходит с вариантами, чужой — без`() {
        val mine = state.copy(
            phase = Phase.AWAITING_CHOICE,
            pending = PendingChoice(
                Side.YOU,
                ChoiceKind.RECOVER_LETTER,
                listOf(ChoiceOption(0, Letter.I)),
            ),
        )
        assertEquals(mine.pending, Redact.state(mine).pending)

        val theirs = mine.copy(
            pending = mine.pending?.copy(side = Side.AI),
        )
        val redacted = Redact.state(theirs).pending
        assertEquals(Side.AI, redacted?.side)
        assertEquals(ChoiceKind.RECOVER_LETTER, redacted?.kind)
        assertEquals(emptyList(), redacted?.options)
    }

    @Test
    fun `выбор по ловушке не раскрывает руку соперника`() {
        // Варианты сброса по ловушке движок строит прямо из руки: без вырезания
        // такой выбор показал бы её целиком, карта за картой.
        val trap = state.copy(
            phase = Phase.AWAITING_CHOICE,
            pending = PendingChoice(
                Side.AI,
                ChoiceKind.TRAP_DISCARD,
                state.ai.hand.mapIndexed { index, letter -> ChoiceOption(index, letter) },
            ),
        )
        val view = Redact.state(trap)
        assertEquals(emptyList(), view.pending?.options)
        assertTrue(
            StateCodec.encode(view).lines().none { line -> line.contains("TRAP_DISCARD;0:") },
            "варианты сброса по ловушке уехали клиенту",
        )
    }

    @Test
    fun `чужие приходы карт обезличены, свои целы`() {
        val events = listOf(
            GameEvent.CardDealt(Side.YOU, Letter.T),
            GameEvent.CardDealt(Side.AI, Letter.T),
            GameEvent.CardDrawn(Side.YOU, Letter.S, deckLeft = 30),
            GameEvent.CardDrawn(Side.AI, Letter.S, deckLeft = 30),
        )
        val view = Redact.events(events)

        assertEquals(Letter.T, (view[0] as GameEvent.CardDealt).letter)
        assertEquals(Redact.HIDDEN, (view[1] as GameEvent.CardDealt).letter)
        assertEquals(Letter.S, (view[2] as GameEvent.CardDrawn).letter)
        assertEquals(Redact.HIDDEN, (view[3] as GameEvent.CardDrawn).letter)
        // Размер колоды скрывать незачем — он и так на экране.
        assertEquals(30, (view[3] as GameEvent.CardDrawn).deckLeft)
    }

    @Test
    fun `события про сброс не редактируются — сброс открыт по правилам`() {
        val events = listOf(
            GameEvent.TrapTriggered(Side.AI, Letter.R, handIndex = 1, trapsLeft = 0),
            GameEvent.HandOverflow(Side.AI, Letter.S, handIndex = 6),
            GameEvent.CardRecovered(Side.AI, Letter.T),
            GameEvent.CardPlayed(Side.AI, Letter.F, handIndex = 0),
        )
        assertEquals(events, Redact.events(events))
    }

    @Test
    fun `в виде партии не остаётся ни одной чужой карты`() {
        val (states, _) = Duel.play()
        for (state in states) {
            for (seat in Seat.entries) {
                val view = state.viewFor(seat)
                val hidden = if (seat == Seat.A) state.ai.hand else state.you.hand
                // Рука соперника целиком заменена заглушкой: восстановить её из
                // вида нельзя даже зная порядок карт.
                assertTrue(
                    view.ai.hand.all { it == Redact.HIDDEN },
                    "место $seat видит чужую руку на ходу ${state.turnNumber}",
                )
                assertEquals(hidden.size, view.ai.hand.size)
            }
        }
    }
}
