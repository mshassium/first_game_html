package com.first.game.domain.ai

import com.first.game.domain.Command
import com.first.game.domain.GameEngine
import com.first.game.domain.GameState
import com.first.game.domain.Phase
import com.first.game.domain.SeededRng
import com.first.game.domain.Side
import kotlin.test.Test
import kotlin.test.assertTrue

class AiTest {

    @Test
    fun `любая политика всегда возвращает допустимую команду`() {
        for (difficulty in Difficulty.entries) {
            repeat(60) { seed ->
                val engine = GameEngine(SeededRng(seed.toLong()))
                val policy = aiPolicy(difficulty, SeededRng(seed * 7L))
                var state = engine.newGame().state
                var steps = 0
                while (!state.isOver && steps++ < 500) {
                    val command = policy.decide(state)
                    assertValid(state, command, "$difficulty, сид $seed")
                    state = engine.apply(state, command).state
                }
            }
        }
    }

    @Test
    fun `сложный уровень заметно сильнее лёгкого`() {
        val result = duel(Difficulty.HARD, Difficulty.EASY, games = 200)
        println("HARD vs EASY: ${result.first} / ${result.second}")
        assertTrue(
            result.first > result.second,
            "сложный должен выигрывать чаще лёгкого: ${result.first} против ${result.second}",
        )
    }

    @Test
    fun `сложный уровень сильнее обычного`() {
        val result = duel(Difficulty.HARD, Difficulty.NORMAL, games = 200)
        println("HARD vs NORMAL: ${result.first} / ${result.second}")
        assertTrue(
            result.first > result.second,
            "сложный должен выигрывать чаще обычного: ${result.first} против ${result.second}",
        )
    }

    @Test
    fun `обычный уровень сильнее лёгкого`() {
        val result = duel(Difficulty.NORMAL, Difficulty.EASY, games = 200)
        println("NORMAL vs EASY: ${result.first} / ${result.second}")
        assertTrue(
            result.first > result.second,
            "обычный должен выигрывать чаще лёгкого: ${result.first} против ${result.second}",
        )
    }

    /** Возвращает пару «побед первой политики, побед второй». Стороны меняются каждую партию. */
    private fun duel(first: Difficulty, second: Difficulty, games: Int): Pair<Int, Int> {
        var firstWins = 0
        var secondWins = 0
        repeat(games) { index ->
            val seed = index.toLong()
            val engine = GameEngine(SeededRng(seed))
            // Чётные партии: первая политика играет за YOU, нечётные — за AI.
            val firstSide = if (index % 2 == 0) Side.YOU else Side.AI
            val policies = mapOf(
                firstSide to aiPolicy(first, SeededRng(seed * 13 + 1)),
                firstSide.other to aiPolicy(second, SeededRng(seed * 13 + 2)),
            )
            var state = engine.newGame().state
            var steps = 0
            while (!state.isOver && steps++ < 500) {
                state = engine.apply(state, policies.getValue(state.actingSide).decide(state)).state
            }
            when (state.outcome?.winner) {
                firstSide -> firstWins++
                firstSide.other -> secondWins++
                else -> Unit
            }
        }
        return firstWins to secondWins
    }

    private fun assertValid(state: GameState, command: Command, hint: String) {
        when (command) {
            is Command.PlayCard -> {
                assertTrue(state.phase == Phase.AWAITING_PLAY, "$hint: розыгрыш вне фазы")
                assertTrue(
                    command.handIndex in state.side(state.turn).hand.indices,
                    "$hint: индекс руки вне диапазона",
                )
            }
            is Command.ChooseOption -> {
                val options = state.pending?.options.orEmpty()
                assertTrue(options.isNotEmpty(), "$hint: выбор без вариантов")
                assertTrue(command.optionIndex in options.indices, "$hint: вариант вне диапазона")
            }
        }
    }
}
