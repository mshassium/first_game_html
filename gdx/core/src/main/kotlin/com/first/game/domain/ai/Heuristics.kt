package com.first.game.domain.ai

import com.first.game.domain.ChoiceOption
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Rules
import com.first.game.domain.Side
import com.first.game.domain.SideState

/**
 * Общие для всех уровней сложности прикидки.
 *
 * ВАЖНО: ни одна функция здесь не смотрит на содержимое руки оппонента и на порядок колод —
 * это скрытая информация. Разрешено пользоваться только тем, что видит живой игрок:
 * своей рукой, обоими SPACE, обоими сбросами, счётчиками карт и активными эффектами.
 */
object Heuristics {

    /** Насколько сторона близка к победе: 0.0 — только начала, 1.0 — победила. */
    fun threat(side: SideState): Double {
        val bySet = side.distinctInSpace.toDouble() / Letter.ALL.size
        val bySame = side.maxSameInSpace.toDouble() / Rules.SAME_LETTERS_TO_WIN
        return maxOf(bySet, bySame)
    }

    /** Сторона выигрывает следующим же выложенным подходящим письмом. */
    fun isOneCardFromWin(side: SideState): Boolean =
        side.distinctInSpace == Letter.ALL.size - 1 ||
            side.maxSameInSpace == Rules.SAME_LETTERS_TO_WIN - 1

    /** Буквы, выкладывание которых немедленно приносит победу. */
    fun winningLetters(side: SideState): List<Letter> = Letter.ALL.filter { letter ->
        val space = side.space + letter
        val distinct = Letter.ALL.count { l -> space.any { it == l } }
        val same = space.count { it == letter }
        distinct == Letter.ALL.size || same >= Rules.SAME_LETTERS_TO_WIN
    }

    /**
     * Что для стороны ценнее всего получить в SPACE следующим ходом.
     * Первой идёт самая полезная буква.
     */
    fun wantedLetters(side: SideState): List<Letter> {
        val missing = side.missingForFirst
        val bestSame = Letter.ALL.maxByOrNull { side.inSpace(it) } ?: Letter.F
        return buildList {
            // Если до набора остались одна-две буквы — они и нужны.
            if (missing.size <= 2) addAll(missing)
            // Иначе полезнее наращивать самую многочисленную стопку.
            if (side.inSpace(bestSame) >= 3) add(bestSame)
            addAll(missing)
            add(bestSame)
            addAll(Letter.ALL)
        }.distinct()
    }

    /** Какую букву осмысленнее всего запретить оппоненту. */
    fun bestForbidLetter(state: GameState, me: Side): Letter {
        val opponent = state.side(me.other)
        if (opponent.space.isEmpty()) return Letter.ALL[0]
        return wantedLetters(opponent).first()
    }

    /** Какую карту в SPACE оппонента убрать, чтобы сильнее отбросить его назад. */
    fun bestStealOption(state: GameState, me: Side, options: List<ChoiceOption>): ChoiceOption {
        val opponent = state.side(me.other)
        // Ломаем набор: убираем букву, которая есть у оппонента в единственном экземпляре.
        if (opponent.distinctInSpace >= Letter.ALL.size - 1) {
            options.filter { opponent.inSpace(it.letter) == 1 }
                .minByOrNull { it.letter.ordinal }
                ?.let { return it }
        }
        // Иначе бьём по самой высокой стопке.
        return options.maxByOrNull { opponent.inSpace(it.letter) } ?: options.last()
    }

    /** Какую карту вернуть из своего сброса. */
    fun bestRecoverOption(state: GameState, me: Side, options: List<ChoiceOption>): ChoiceOption {
        val mine = state.side(me)
        val wanted = wantedLetters(mine)
        return options.minByOrNull { option ->
            val rank = wanted.indexOf(option.letter)
            if (rank < 0) Int.MAX_VALUE else rank
        } ?: options.last()
    }

    /** Какую карту не жалко сбросить по ловушке. */
    fun worstHandOption(state: GameState, me: Side, options: List<ChoiceOption>): ChoiceOption {
        val mine = state.side(me)
        val wanted = wantedLetters(mine)
        val counts = options.groupingBy { it.letter }.eachCount()
        return options.maxByOrNull { option ->
            val rank = wanted.indexOf(option.letter).takeIf { it >= 0 } ?: Letter.ALL.size
            // Чем бесполезнее буква и чем больше её дублей в руке — тем охотнее сбрасываем.
            rank * 10 + (counts[option.letter] ?: 1)
        } ?: options.first()
    }
}
