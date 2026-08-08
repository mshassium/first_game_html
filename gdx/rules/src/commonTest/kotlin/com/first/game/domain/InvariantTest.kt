package com.first.game.domain

import com.first.game.domain.ai.Difficulty
import com.first.game.domain.ai.aiPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Прогон полных партий: инварианты должны держаться на каждом шаге,
 * а партия — всегда завершаться.
 */
class InvariantTest {

    @Test
    fun `в любой момент партии у каждой стороны ровно 50 карт и по 10 каждой буквы`() {
        repeat(GAMES) { seed ->
            playOut(seed.toLong()) { state ->
                for (side in listOf(Side.YOU, Side.AI)) {
                    val s = state.side(side)
                    assertEquals(Rules.DECK_SIZE, s.totalCards, "сид $seed, сторона $side")
                    for (letter in Letter.ALL) {
                        val count = (s.deck + s.hand + s.space + s.discard).count { it == letter }
                        assertEquals(
                            Rules.CARDS_PER_LETTER, count,
                            "сид $seed, сторона $side, буква $letter",
                        )
                    }
                    assertTrue(s.hand.size <= Rules.HAND_LIMIT, "сид $seed: рука $side")
                }
            }
        }
    }

    @Test
    fun `партия всегда завершается и имеет ровно один исход`() {
        repeat(GAMES) { seed ->
            val final = playOut(seed.toLong())
            assertNotNull(final.outcome, "сид $seed: партия не завершилась")
            assertEquals(Phase.GAME_OVER, final.phase)
        }
    }

    @Test
    fun `выбор всегда запрашивается у той стороны, чей сейчас ход`() {
        repeat(GAMES) { seed ->
            playOut(seed.toLong()) { state ->
                state.pending?.let {
                    assertEquals(state.turn, it.side, "сид $seed")
                    assertTrue(it.options.isNotEmpty(), "сид $seed: пустой список вариантов")
                }
            }
        }
    }

    /** Играет партию «ИИ против ИИ» до конца, вызывая [onStep] после каждой команды. */
    private fun playOut(seed: Long, onStep: (GameState) -> Unit = {}): GameState {
        val rng = SeededRng(seed)
        val engine = GameEngine(rng)
        val policies = mapOf(
            Side.YOU to aiPolicy(Difficulty.NORMAL, SeededRng(seed * 31 + 1)),
            Side.AI to aiPolicy(Difficulty.EASY, SeededRng(seed * 31 + 2)),
        )

        var state = engine.newGame().state
        onStep(state)

        var steps = 0
        while (!state.isOver) {
            assertTrue(steps++ < MAX_STEPS, "сид $seed: партия не сходится за $MAX_STEPS шагов")
            val policy = policies.getValue(state.actingSide)
            state = engine.apply(state, policy.decide(state)).state
            onStep(state)
        }
        return state
    }

    private companion object {
        const val GAMES = 200
        const val MAX_STEPS = 500
    }
}
