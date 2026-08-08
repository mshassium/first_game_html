package com.first.game.domain.ai

import com.first.game.domain.Command
import com.first.game.domain.GameEngine
import com.first.game.domain.GameState
import com.first.game.domain.Rng

/**
 * Стратегия оппонента. Отвечает как на «сыграй карту», так и на запрос выбора:
 * движок останавливается в [com.first.game.domain.Phase.AWAITING_CHOICE], и игровой цикл
 * снова спрашивает политику.
 */
interface AiPolicy {
    val difficulty: Difficulty

    /** Команда для стороны [GameState.actingSide]. Всегда допустимая. */
    fun decide(state: GameState): Command
}

enum class Difficulty {
    /** Приоритеты из старой HTML-версии: реактивный, о своей победе не думает. */
    EASY,

    /** Добивает победу за один ход и мешает игроку добить свою. */
    NORMAL,

    /** Перебор всех своих ходов на один плай с оценкой позиции. */
    HARD;

    companion object {
        val DEFAULT = NORMAL
    }
}

fun aiPolicy(difficulty: Difficulty, rng: Rng): AiPolicy = when (difficulty) {
    Difficulty.EASY -> EasyAi(rng)
    Difficulty.NORMAL -> NormalAi(rng)
    Difficulty.HARD -> HardAi(rng, GameEngine(rng))
}
