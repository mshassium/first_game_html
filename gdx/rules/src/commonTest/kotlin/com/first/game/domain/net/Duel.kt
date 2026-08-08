package com.first.game.domain.net

import com.first.game.domain.Command
import com.first.game.domain.GameEngine
import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.SeededRng

/**
 * Прогон партии для сетевых тестов.
 *
 * Стратегия нарочно тупая — всегда первый допустимый ход: проверяется не игра,
 * а то, что состояние и события переживают дорогу до клиента и обратно.
 */
object Duel {

    /** Партия целиком: список всех состояний и все события по порядку. */
    fun play(seed: Long = 7L, limit: Int = 500): Pair<List<GameState>, List<GameEvent>> {
        val engine = GameEngine(SeededRng(seed))
        var result = engine.newGame()
        val states = mutableListOf(result.state)
        val events = result.events.toMutableList()

        var steps = 0
        while (!result.state.isOver) {
            check(steps++ < limit) { "партия не закончилась за $limit ходов" }
            result = engine.apply(result.state, firstMove(result.state))
            states += result.state
            events += result.events
        }
        return states to events
    }

    /** Первый допустимый ход: разыграть карту слева или выбрать первый вариант. */
    fun firstMove(state: GameState): Command =
        if (state.pending != null) Command.ChooseOption(0) else Command.PlayCard(0)
}
