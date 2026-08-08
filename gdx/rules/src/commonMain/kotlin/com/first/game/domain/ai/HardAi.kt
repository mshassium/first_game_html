package com.first.game.domain.ai

import com.first.game.domain.ChoiceKind
import com.first.game.domain.Command
import com.first.game.domain.GameEngine
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Phase
import com.first.game.domain.Rng
import com.first.game.domain.Rules
import com.first.game.domain.Side
import com.first.game.domain.SideState

/**
 * Перебор всех своих ходов на один плай: движок чистый, поэтому позицию можно
 * честно «проиграть» и оценить.
 *
 * Скрытая информация не утекает. Позиция оценивается двумя разными функциями:
 * [ownScore] знает содержимое своей руки (её ИИ видит по праву), а [visibleScore]
 * для оппонента пользуется только публичными данными — SPACE, сбросом и счётчиками.
 * Заглядывать в руку игрока или в порядок колод ИИ не умеет.
 */
class HardAi(
    private val rng: Rng,
    private val engine: GameEngine,
    private val weights: HardAiWeights = HardAiWeights(),
) : AiPolicy {

    override val difficulty = Difficulty.HARD

    /**
     * Базовый план — правила «обычного» уровня. Перебор отклоняется от него только
     * тогда, когда видит заметно лучший ход: за один плай он не способен оценить
     * долгую стратегию, зато хорошо считает разменные ситуации.
     */
    private val playbook = NormalAi(rng)

    override fun decide(state: GameState): Command {
        val me = state.actingSide

        state.pending?.let { pending ->
            // Какую букву запретить — из перебора не видно: запрет бьёт по руке оппонента,
            // а её содержимое скрыто. Здесь работает эвристика по его столу.
            if (pending.kind == ChoiceKind.FORBID_LETTER) {
                val letter = Heuristics.bestForbidLetter(state, me)
                val index = pending.options.indexOfFirst { it.letter == letter }
                return Command.ChooseOption(index.coerceAtLeast(0))
            }
            val best = pending.options.indices.maxByOrNull { index ->
                score(engine.apply(state, Command.ChooseOption(index)).state, me)
            } ?: 0
            return Command.ChooseOption(best)
        }

        val hand = state.side(me).hand
        if (hand.isEmpty()) return Command.PlayCard(0)

        // Одинаковые буквы дают одинаковый ход — перебираем по одному индексу на букву.
        val candidates = hand.indices.distinctBy { hand[it] }
        val scored = candidates.associateWith { index -> scorePlay(state, index, me) }
        val best = scored.maxByOrNull { it.value } ?: return Command.PlayCard(0)

        val planned = (playbook.decide(state) as? Command.PlayCard)?.handIndex
            ?.takeIf { it in hand.indices }
            ?: return Command.PlayCard(best.key)

        val plannedScore = scored[planned] ?: scorePlay(state, planned, me)
        return if (best.value - plannedScore > weights.deviationMargin) {
            Command.PlayCard(best.key)
        } else {
            Command.PlayCard(planned)
        }
    }

    /** Оценка хода вместе с лучшим для нас продолжением выбора, если он потребуется. */
    private fun scorePlay(state: GameState, handIndex: Int, me: Side): Double {
        val afterPlay = engine.apply(state, Command.PlayCard(handIndex)).state
        if (afterPlay.phase != Phase.AWAITING_CHOICE || afterPlay.pending?.side != me) {
            return score(afterPlay, me)
        }
        if (afterPlay.pending?.kind == ChoiceKind.FORBID_LETTER) {
            // Запрет одинаково «стоит» при любой букве — саму карту F это не обесценивает.
            return score(afterPlay, me)
        }
        val options = afterPlay.pending?.options.orEmpty()
        return options.indices.maxOfOrNull { index ->
            score(engine.apply(afterPlay, Command.ChooseOption(index)).state, me)
        } ?: score(afterPlay, me)
    }

    private fun score(state: GameState, me: Side): Double {
        state.outcome?.let { return if (it.winner == me) WIN_SCORE else -WIN_SCORE }
        // Микрошум разводит равноценные ходы, чтобы ИИ не выглядел «зациклённым».
        return evaluate(state, me) + rng.nextInt(100) * 0.001
    }

    private fun evaluate(state: GameState, me: Side): Double {
        val mine = ownScore(state.side(me), state.traps.forbidOn(me), state.traps.trapsOn(me))
        val theirs = visibleScore(
            state.side(me.other),
            state.traps.forbidOn(me.other) != null,
            state.traps.trapsOn(me.other),
        )
        // Небольшой перевес в сторону защиты: проиграть дороже, чем не успеть выиграть.
        return mine - weights.opponentWeight * theirs
    }

    /** Своя сторона: к позиции на столе добавляется потенциал руки. */
    private fun ownScore(side: SideState, forbidden: Letter?, traps: Int): Double {
        var value = visibleScore(side, forbidden != null, traps)
        val missing = side.missingForFirst
        val tallest = Letter.ALL.maxByOrNull { side.inSpace(it) } ?: Letter.F
        val tallestHeight = side.inSpace(tallest)

        for (letter in side.hand) {
            // Карта, которой не хватает до набора, — самая ценная.
            if (letter in missing) value += weights.missingInHand
            // Карта, наращивающая самую высокую стопку, тем ценнее, чем стопка выше.
            if (letter == tallest && tallestHeight >= 2) value += weights.sameInHand * tallestHeight
            // Запрещённую карту так просто не выложить.
            if (letter == forbidden) value -= weights.forbiddenInHand
        }
        return value
    }

    /** Оппонент: только то, что видно игроку со стороны. */
    private fun visibleScore(side: SideState, forbidden: Boolean, traps: Int): Double {
        var value = 0.0
        value += weights.setProgress[side.distinctInSpace]
        value += weights.sameProgress[side.maxSameInSpace.coerceAtMost(Rules.SAME_LETTERS_TO_WIN)]
        value += side.hand.size * weights.handCard
        value += side.deck.size * 0.08
        value -= traps * 2.5
        // Цена запрета не постоянна: чем ближе сторона к победе, тем дороже ей стоит
        // потерянный ход. Без этой зависимости ИИ недооценивал карту F.
        if (forbidden) value -= weights.forbidBase + weights.forbidByThreat * Heuristics.threat(side)
        return value
    }

    private companion object {
        const val WIN_SCORE = 1_000_000.0
    }
}

/**
 * Веса оценочной функции. Вынесены отдельно, чтобы силу ИИ можно было измерять
 * и настраивать прогонами партий, а не на глаз.
 */
data class HardAiWeights(
    /** Ценность набора разных букв по индексу 0..5. Нелинейно: последний шаг самый дорогой. */
    val setProgress: DoubleArray = doubleArrayOf(0.0, 4.0, 14.0, 40.0, 220.0, 500.0),
    /** Ценность стопки одинаковых букв по индексу 0..5. */
    val sameProgress: DoubleArray = doubleArrayOf(0.0, 0.5, 10.0, 34.0, 190.0, 500.0),
    val opponentWeight: Double = 1.05,
    val handCard: Double = 0.4,
    val missingInHand: Double = 4.0,
    val sameInHand: Double = 1.5,
    val forbiddenInHand: Double = 4.0,
    val forbidBase: Double = 3.0,
    val forbidByThreat: Double = 45.0,
    /** Насколько перебор должен опережать план, чтобы от плана отклониться. */
    val deviationMargin: Double = 12.0,
) {
    override fun equals(other: Any?): Boolean = this === other
    // Веса сравниваются по ссылке: массивы внутри data class иначе дали бы
    // сравнение по идентичности в equals и по содержимому в hashCode.
    override fun hashCode(): Int = super.hashCode()
}
