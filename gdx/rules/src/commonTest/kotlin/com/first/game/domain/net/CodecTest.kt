package com.first.game.domain.net

import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.Command
import com.first.game.domain.EndReason
import com.first.game.domain.GameEvent
import com.first.game.domain.Letter
import com.first.game.domain.Outcome
import com.first.game.domain.PendingChoice
import com.first.game.domain.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Кодеки — единственный мост между сервером и клиентом. Если состояние теряет
 * поле по дороге, партия у двух игроков разъезжается молча, поэтому проверяется
 * не пример-другой, а каждое состояние настоящей партии.
 */
class CodecTest {

    @Test
    fun `каждое состояние партии переживает запись и чтение`() {
        val (states, _) = Duel.play()
        assertTrue(states.size > 10, "партия оказалась подозрительно короткой")
        for (state in states) {
            assertEquals(state, StateCodec.decode(StateCodec.encode(state)))
        }
    }

    @Test
    fun `каждое событие партии переживает запись и чтение`() {
        val (_, events) = Duel.play()
        assertEquals(events, EventCodec.decode(EventCodec.encode(events)))
    }

    @Test
    fun `все виды событий кодируются, включая редкие`() {
        // Партия по первому попавшемуся ходу задевает не всё: редкие события
        // перечислены руками. Полноту encodeOne стережёт компилятор — when по
        // sealed-интерфейсу без else не соберётся, если появится новый вид.
        val events = listOf(
            GameEvent.GameStarted(Side.AI, youRoll = 2, aiRoll = 5),
            GameEvent.CardDealt(Side.YOU, Letter.F),
            GameEvent.TurnBegan(Side.AI, turnNumber = 3),
            GameEvent.CardDrawn(Side.YOU, Letter.I, deckLeft = 41),
            GameEvent.HandOverflow(Side.AI, Letter.R, handIndex = 6),
            GameEvent.TrapTriggered(Side.YOU, Letter.S, handIndex = 2, trapsLeft = 1),
            GameEvent.TrapFizzled(Side.AI, trapsLeft = 0),
            GameEvent.TurnSkipped(Side.YOU),
            GameEvent.CardPlayed(Side.AI, Letter.T, handIndex = 0),
            GameEvent.CardForbidden(Side.YOU, Letter.F, handIndex = 4),
            GameEvent.ForbidSet(Side.AI, Letter.T),
            GameEvent.ForbidBroken(Side.YOU, Letter.I),
            GameEvent.CardRecovered(Side.AI, Letter.R),
            GameEvent.CardStolen(Side.YOU, Letter.S, spaceIndex = 1),
            GameEvent.TrapSet(Side.AI, trapCount = 2),
            GameEvent.EffectFizzled(Side.YOU, Letter.R),
            GameEvent.ChoiceRequired(
                PendingChoice(
                    Side.AI,
                    ChoiceKind.STEAL_TARGET,
                    listOf(ChoiceOption(0, Letter.F), ChoiceOption(2, Letter.T)),
                ),
            ),
            GameEvent.TurnEnded(Side.AI),
            GameEvent.GameEnded(Outcome(Side.YOU, EndReason.FIVE_OF_A_KIND)),
        )
        assertEquals(events, EventCodec.decode(EventCodec.encode(events)))
    }

    @Test
    fun `выбор без вариантов не теряется`() {
        val choice = GameEvent.ChoiceRequired(
            PendingChoice(Side.YOU, ChoiceKind.TRAP_DISCARD, emptyList()),
        )
        assertEquals(listOf(choice), EventCodec.decode(EventCodec.encode(listOf(choice))))
    }

    @Test
    fun `пустой список событий кодируется пустой строкой`() {
        assertEquals("", EventCodec.encode(emptyList()))
        assertEquals(emptyList(), EventCodec.decode(""))
    }

    @Test
    fun `команды переживают дорогу`() {
        for (command in listOf(Command.PlayCard(0), Command.PlayCard(6), Command.ChooseOption(3))) {
            assertEquals(command, CommandCodec.decode(CommandCodec.encode(command)))
        }
    }

    @Test
    fun `мусор и чужая версия отбиваются, а не роняют сервер`() {
        assertNull(StateCodec.decodeOrNull("мусор"))
        assertNull(StateCodec.decodeOrNull(""))
        val (states, _) = Duel.play()
        val alien = "99\n" + StateCodec.encode(states.first()).substringAfter('\n')
        assertNull(StateCodec.decodeOrNull(alien))
        assertNull(EventCodec.decodeOrNull("ЧТОТО;YOU"))
        assertNull(CommandCodec.decodeOrNull("play"))
        assertNull(CommandCodec.decodeOrNull("play;не число"))
        assertNull(CommandCodec.decodeOrNull("surrender;0"))
    }
}
