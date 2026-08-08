package com.first.game.domain

import com.first.game.domain.Fixtures.first
import com.first.game.domain.Fixtures.has
import com.first.game.domain.Fixtures.optionIndexOf
import com.first.game.domain.Letter.F
import com.first.game.domain.Letter.I
import com.first.game.domain.Letter.R
import com.first.game.domain.Letter.S
import com.first.game.domain.Letter.T
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ForbidTest {

    private val engine = Fixtures.engine()

    @Test
    fun `F требует выбора буквы и вешает запрет на оппонента`() {
        val start = Fixtures.state(youHand = "F")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Phase.AWAITING_CHOICE, played.state.phase)
        assertEquals(ChoiceKind.FORBID_LETTER, played.state.pending?.kind)
        assertEquals(Side.YOU, played.state.pending?.side)
        assertEquals(Letter.ALL.size, played.state.pending?.options?.size)

        val chosen = engine.apply(played.state, Command.ChooseOption(played.state.optionIndexOf(T)))

        assertEquals(T, chosen.state.traps.forbidOnAi)
        assertNull(chosen.state.traps.forbidOnYou)
        assertTrue(chosen.events.has<GameEvent.ForbidSet>())
        assertEquals(Side.AI, chosen.state.turn)
    }

    @Test
    fun `запрещённая карта уходит в свой сброс без эффекта и завершает ход`() {
        val start = Fixtures.state(
            youHand = "I",
            youDeck = List(20) { R },
            traps = Traps(forbidOnYou = I),
        )
        val played = engine.apply(start, Command.PlayCard(0))

        assertTrue(played.state.you.space.isEmpty(), "карта не должна попасть в SPACE")
        assertEquals(listOf(I), played.state.you.discard)
        assertNull(played.state.traps.forbidOnYou, "запрет снимается после срабатывания")
        assertTrue(played.events.has<GameEvent.CardForbidden>())
        assertTrue(played.events.none { it is GameEvent.CardDrawn && it.side == Side.YOU },
            "эффект I не должен сработать")
        assertEquals(Side.AI, played.state.turn)
    }

    @Test
    fun `повторное F перезаписывает запрет, а не добавляет второй`() {
        val start = Fixtures.state(youHand = "F", traps = Traps(forbidOnAi = I))
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(played.state.optionIndexOf(S)))

        assertEquals(S, chosen.state.traps.forbidOnAi)
    }

    @Test
    fun `запрет на букву, которой нет у оппонента, никому не мешает`() {
        val start = Fixtures.state(youHand = "T", traps = Traps(forbidOnYou = S))
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(listOf(T), played.state.you.space)
        assertEquals(S, played.state.traps.forbidOnYou, "запрет висит дальше")
    }
}

class IncreaseTest {

    private val engine = Fixtures.engine()

    @Test
    fun `I добирает карту из своей колоды`() {
        val start = Fixtures.state(youHand = "I", youDeck = List(20) { R })
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(listOf(I), played.state.you.space)
        assertEquals(listOf(R), played.state.you.hand)
        assertEquals(19, played.state.you.deck.size)
    }

    @Test
    fun `пустая колода на эффекте I приводит к поражению`() {
        val start = Fixtures.state(youHand = "I", youDeck = emptyList())
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(Phase.GAME_OVER, played.state.phase)
        assertEquals(Outcome(Side.AI, EndReason.DECK_OUT), played.state.outcome)
    }
}

class RecoverTest {

    private val engine = Fixtures.engine()

    @Test
    fun `R с пустым сбросом просто не срабатывает`() {
        val start = Fixtures.state(youHand = "R")
        val played = engine.apply(start, Command.PlayCard(0))

        assertTrue(played.events.has<GameEvent.EffectFizzled>())
        assertEquals(Phase.AWAITING_PLAY, played.state.phase)
        assertEquals(Side.AI, played.state.turn, "ход всё равно передан")
    }

    @Test
    fun `R предлагает по одному варианту на букву и возвращает выбранную`() {
        val start = Fixtures.state(youHand = "R", youDiscard = "FIT")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(ChoiceKind.RECOVER_LETTER, played.state.pending?.kind)
        assertEquals(listOf(F, I, T), played.state.pending?.options?.map { it.letter })

        val chosen = engine.apply(played.state, Command.ChooseOption(played.state.optionIndexOf(I)))

        assertEquals(listOf(I), chosen.state.you.hand)
        assertEquals(listOf(F, T), chosen.state.you.discard)
        assertTrue(chosen.events.has<GameEvent.CardRecovered>())
    }

    @Test
    fun `R убирает из сброса последнее вхождение буквы`() {
        val start = Fixtures.state(youHand = "R", youDiscard = "FIF")
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(played.state.optionIndexOf(F)))

        assertEquals(listOf(F, I), chosen.state.you.discard)
    }
}

class StealTest {

    private val engine = Fixtures.engine()

    @Test
    fun `S при пустом SPACE оппонента не срабатывает`() {
        val start = Fixtures.state(youHand = "S")
        val played = engine.apply(start, Command.PlayCard(0))

        assertTrue(played.events.has<GameEvent.EffectFizzled>())
        assertEquals(Side.AI, played.state.turn)
    }

    @Test
    fun `S отправляет выбранную карту в сброс владельца, а не в свой`() {
        val start = Fixtures.state(youHand = "S", aiSpace = "FIR")
        val played = engine.apply(start, Command.PlayCard(0))

        assertEquals(ChoiceKind.STEAL_TARGET, played.state.pending?.kind)
        assertEquals(listOf(F, I, R), played.state.pending?.options?.map { it.letter })

        val chosen = engine.apply(played.state, Command.ChooseOption(1))

        assertEquals(listOf(F, R), chosen.state.ai.space)
        assertEquals(listOf(I), chosen.state.ai.discard)
        assertTrue(chosen.state.you.discard.isEmpty(), "карта не переходит вору")
        assertTrue(chosen.events.has<GameEvent.CardStolen>())
    }

    @Test
    fun `украденная карта перестаёт считаться в условии победы`() {
        val start = Fixtures.state(turn = Side.AI, aiHand = "S", youSpace = "FIRS")
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(0))

        assertEquals(3, chosen.state.you.distinctInSpace)
        assertNull(chosen.state.outcome)
    }

    @Test
    fun `кража карты F снимает наложенный ею запрет`() {
        val start = Fixtures.state(
            youHand = "S",
            aiSpace = "F",
            traps = Traps(forbidOnYou = R),
        )
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(0))

        assertNull(chosen.state.traps.forbidOnYou, "карты F не осталось — держать запрет нечему")
        assertTrue(chosen.events.has<GameEvent.ForbidBroken>())
        assertEquals(R, chosen.events.first<GameEvent.ForbidBroken>().letter)
        assertEquals(Side.YOU, chosen.events.first<GameEvent.ForbidBroken>().on)
    }

    @Test
    fun `пока в SPACE осталась вторая F, запрет держится`() {
        val start = Fixtures.state(
            youHand = "S",
            aiSpace = "FF",
            traps = Traps(forbidOnYou = R),
        )
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(0))

        assertEquals(listOf(F), chosen.state.ai.space)
        assertEquals(R, chosen.state.traps.forbidOnYou)
        assertTrue(chosen.events.none { it is GameEvent.ForbidBroken })
    }

    @Test
    fun `кража чужой F не трогает запрет, наложенный другой стороной`() {
        val start = Fixtures.state(
            youHand = "S",
            aiSpace = "F",
            youSpace = "F",
            traps = Traps(forbidOnAi = R),
        )
        val played = engine.apply(start, Command.PlayCard(0))
        val chosen = engine.apply(played.state, Command.ChooseOption(0))

        assertEquals(R, chosen.state.traps.forbidOnAi, "запрет держит карта F в SPACE игрока")
        assertTrue(chosen.events.none { it is GameEvent.ForbidBroken })
    }
}

class TrapTest {

    private val engine = Fixtures.engine()

    @Test
    fun `T вешает ловушку на оппонента`() {
        val start = Fixtures.state(youHand = "T", aiHand = "FIR")
        val played = engine.apply(start, Command.PlayCard(0))

        assertTrue(played.events.has<GameEvent.TrapSet>())
        // Ловушка уже сработала на входе в ход ИИ и ждёт его выбора.
        assertEquals(Phase.AWAITING_CHOICE, played.state.phase)
        assertEquals(ChoiceKind.TRAP_DISCARD, played.state.pending?.kind)
        assertEquals(Side.AI, played.state.pending?.side)
    }

    @Test
    fun `после сброса по ловушке сторона всё равно делает свой ход`() {
        val start = Fixtures.state(youHand = "T", aiHand = "FIR")
        val played = engine.apply(start, Command.PlayCard(0))
        val discarded = engine.apply(played.state, Command.ChooseOption(0))

        assertEquals(Side.AI, discarded.state.turn)
        assertEquals(Phase.AWAITING_PLAY, discarded.state.phase, "ИИ ещё должен разыграть карту")
        assertEquals(1, discarded.state.ai.discard.size)
        assertEquals(0, discarded.state.traps.trapsOnAi)
        assertTrue(discarded.events.has<GameEvent.TrapTriggered>())
    }

    @Test
    fun `две ловушки срабатывают в двух ходах подряд, а не схлопываются в одну`() {
        val start = Fixtures.state(youHand = "T", aiHand = "FIR", traps = Traps(trapsOnAi = 1))
        val played = engine.apply(start, Command.PlayCard(0))
        assertEquals(2, played.state.traps.trapsOnAi)

        val discarded = engine.apply(played.state, Command.ChooseOption(0))
        assertEquals(1, discarded.state.traps.trapsOnAi, "вторая ловушка ждёт следующего хода")
    }

    @Test
    fun `ход пропускается, если ловушка выбила последнюю карту руки`() {
        val start = Fixtures.state(
            youHand = "T",
            aiHand = "",
            aiDeck = listOf(R),
            traps = Traps(trapsOnAi = 1),
        )
        val played = engine.apply(start, Command.PlayCard(0))
        assertEquals(1, played.state.ai.hand.size, "ИИ добрал единственную карту")

        val discarded = engine.apply(played.state, Command.ChooseOption(0))

        assertTrue(discarded.events.has<GameEvent.TurnSkipped>())
        assertEquals(Side.YOU, discarded.state.turn, "ход вернулся игроку")
    }
}
