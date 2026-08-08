package com.first.game.domain.ai

import com.first.game.domain.ChoiceKind
import com.first.game.domain.Command
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Rng

/**
 * Точный порт приоритетов старой HTML-версии: реактивный оппонент,
 * который не строит собственную победу. Оставлен как «Лёгкий» уровень.
 */
class EasyAi(private val rng: Rng) : AiPolicy {

    override val difficulty = Difficulty.EASY

    override fun decide(state: GameState): Command {
        val pending = state.pending
        if (pending != null) return decideChoice(state, pending.kind, pending.options.size, pending)
        return Command.PlayCard(pickHandIndex(state))
    }

    private fun pickHandIndex(state: GameState): Int {
        val me = state.turn
        val hand = state.side(me).hand
        val opponent = state.side(me.other)
        val mine = state.side(me)

        if (opponent.space.isNotEmpty()) hand.indexOf(Letter.S).takeIf { it >= 0 }?.let { return it }
        if (mine.discard.isNotEmpty()) hand.indexOf(Letter.R).takeIf { it >= 0 }?.let { return it }
        hand.indexOf(Letter.F).takeIf { it >= 0 }?.let { return it }
        hand.indexOf(Letter.T).takeIf { it >= 0 }?.let { return it }
        hand.indexOf(Letter.I).takeIf { it >= 0 }?.let { return it }
        return 0
    }

    private fun decideChoice(
        state: GameState,
        kind: ChoiceKind,
        optionCount: Int,
        pending: com.first.game.domain.PendingChoice,
    ): Command = when (kind) {
        // Запрещаем самую частую букву в SPACE игрока, иначе случайную.
        ChoiceKind.FORBID_LETTER -> {
            val opponentSpace = state.side(pending.side.other).space
            val letter = opponentSpace.groupingBy { it }.eachCount()
                .maxByOrNull { it.value }?.key
                ?: Letter.ALL[rng.nextInt(Letter.ALL.size)]
            Command.ChooseOption(pending.options.indexOfFirst { it.letter == letter }.coerceAtLeast(0))
        }
        // Забираем последнюю карту сброса и последнюю карту стола — как в старой версии.
        ChoiceKind.RECOVER_LETTER -> Command.ChooseOption(
            pending.options.indices.maxByOrNull { pending.options[it].index } ?: 0
        )
        ChoiceKind.STEAL_TARGET -> Command.ChooseOption(optionCount - 1)
        ChoiceKind.TRAP_DISCARD -> Command.ChooseOption(rng.nextInt(optionCount))
    }
}
