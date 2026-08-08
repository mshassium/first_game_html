package com.first.game.domain.ai

import com.first.game.domain.ChoiceKind
import com.first.game.domain.Command
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.PendingChoice
import com.first.game.domain.Rng
import com.first.game.domain.Side

/**
 * Оппонент по умолчанию. Три правила поверх лёгкого уровня:
 * 1) видит собственную победу в один ход и забирает её;
 * 2) видит победу игрока в один ход и мешает — крадёт ключевую карту или запрещает нужную букву;
 * 3) в спокойной позиции достраивает свой набор, а не просто «жмёт кнопки».
 */
class NormalAi(private val rng: Rng) : AiPolicy {

    override val difficulty = Difficulty.NORMAL

    override fun decide(state: GameState): Command {
        val pending = state.pending
        if (pending != null) return decideChoice(state, pending)

        val me = state.turn
        val hand = state.side(me).hand
        val mine = state.side(me)
        val opponent = state.side(me.other)
        val forbidden = state.traps.forbidOn(me)

        fun indexOf(letter: Letter): Int? =
            hand.indexOf(letter).takeIf { it >= 0 && letter != forbidden }

        // 1. Победа в один ход.
        Heuristics.winningLetters(mine).firstNotNullOfOrNull { indexOf(it) }?.let { return Command.PlayCard(it) }

        // 2. Игрок в шаге от победы — мешаем.
        if (Heuristics.isOneCardFromWin(opponent)) {
            if (opponent.space.isNotEmpty()) indexOf(Letter.S)?.let { return Command.PlayCard(it) }
            indexOf(Letter.F)?.let { return Command.PlayCard(it) }
        }

        // 3. Достраиваем своё: если карта приближает набор или стопку — играем её.
        val wanted = Heuristics.wantedLetters(mine)
        if (mine.distinctInSpace >= 3 || mine.maxSameInSpace >= 3) {
            wanted.firstNotNullOfOrNull { indexOf(it) }?.let { return Command.PlayCard(it) }
        }

        // 4. Полезность эффектов.
        if (opponent.space.isNotEmpty()) indexOf(Letter.S)?.let { return Command.PlayCard(it) }
        if (mine.discard.isNotEmpty()) indexOf(Letter.R)?.let { return Command.PlayCard(it) }
        if (state.traps.forbidOn(me.other) == null) indexOf(Letter.F)?.let { return Command.PlayCard(it) }
        if (state.side(me.other).hand.isNotEmpty()) indexOf(Letter.T)?.let { return Command.PlayCard(it) }
        indexOf(Letter.I)?.let { return Command.PlayCard(it) }

        // 5. Ничего осмысленного: не тратим карту, которая запрещена, если есть выбор.
        val allowed = hand.indices.filter { hand[it] != forbidden }
        if (allowed.isEmpty()) return Command.PlayCard(0)
        return Command.PlayCard(allowed[rng.nextInt(allowed.size)])
    }

    private fun decideChoice(state: GameState, pending: PendingChoice): Command {
        val me: Side = pending.side
        val option = when (pending.kind) {
            ChoiceKind.FORBID_LETTER -> {
                val letter = Heuristics.bestForbidLetter(state, me)
                pending.options.firstOrNull { it.letter == letter } ?: pending.options.first()
            }
            ChoiceKind.RECOVER_LETTER -> Heuristics.bestRecoverOption(state, me, pending.options)
            ChoiceKind.STEAL_TARGET -> Heuristics.bestStealOption(state, me, pending.options)
            ChoiceKind.TRAP_DISCARD -> Heuristics.worstHandOption(state, me, pending.options)
        }
        val index = pending.options.indexOf(option)
        return Command.ChooseOption(if (index >= 0) index else 0)
    }
}
