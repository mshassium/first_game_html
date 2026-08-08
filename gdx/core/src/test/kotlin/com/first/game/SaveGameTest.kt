package com.first.game

import com.first.game.domain.ChoiceKind
import com.first.game.domain.ChoiceOption
import com.first.game.domain.EndReason
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Outcome
import com.first.game.domain.PendingChoice
import com.first.game.domain.Phase
import com.first.game.domain.Side
import com.first.game.domain.SideState
import com.first.game.domain.Traps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Кодек сохранения проверяется отдельно от Preferences: там libGDX, здесь чистые данные.
 */
class SaveGameTest {

    private val state = GameState(
        you = SideState(
            deck = listOf(Letter.F, Letter.I, Letter.R),
            hand = listOf(Letter.S, Letter.T),
            space = listOf(Letter.F, Letter.F),
            discard = listOf(Letter.I),
        ),
        ai = SideState(
            deck = listOf(Letter.T),
            hand = emptyList(),
            space = listOf(Letter.R),
            discard = emptyList(),
        ),
        turn = Side.AI,
        firstPlayer = Side.YOU,
        firstTurnDone = true,
        traps = Traps(forbidOnYou = Letter.S, forbidOnAi = null, trapsOnYou = 0, trapsOnAi = 2),
        phase = Phase.AWAITING_CHOICE,
        pending = PendingChoice(
            side = Side.YOU,
            kind = ChoiceKind.RECOVER_LETTER,
            options = listOf(ChoiceOption(0, Letter.I), ChoiceOption(3, Letter.T)),
        ),
        turnNumber = 7,
    )

    @Test
    fun `состояние переживает запись и чтение`() {
        val restored = SaveGame.decode(SaveGame.encode(state, elapsedSeconds = 42.5f))
        assertEquals(state, restored?.state)
        assertEquals(42.5f, restored?.elapsedSeconds)
    }

    @Test
    fun `пустые стопки и отсутствующий выбор не теряются`() {
        val plain = state.copy(
            ai = SideState(),
            traps = Traps(),
            phase = Phase.AWAITING_PLAY,
            pending = null,
        )
        val restored = SaveGame.decode(SaveGame.encode(plain, elapsedSeconds = 0f))
        assertEquals(plain, restored?.state)
    }

    @Test
    fun `сохранение чужой версии отбрасывается, а не роняет игру`() {
        val alien = "99\n" + SaveGame.encode(state, 1f).substringAfter('\n')
        assertNull(SaveGame.decode(alien))
    }

    @Test
    fun `испорченная строка не проходит разбор`() {
        assertNull(SaveGame.decode("2\nмусор"))
    }

    @Test
    fun `законченная партия восстанавливается вместе с исходом`() {
        val finished = state.copy(
            phase = Phase.GAME_OVER,
            pending = null,
            outcome = Outcome(Side.AI, EndReason.DECK_OUT),
        )
        assertEquals(finished, SaveGame.decode(SaveGame.encode(finished, 1f))?.state)
    }
}
