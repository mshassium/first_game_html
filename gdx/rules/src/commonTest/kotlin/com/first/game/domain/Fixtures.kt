package com.first.game.domain

import com.first.game.domain.Letter.F
import com.first.game.domain.Letter.I
import com.first.game.domain.Letter.R
import com.first.game.domain.Letter.S
import com.first.game.domain.Letter.T

/**
 * Помощники для тестов: собирать состояние руками нагляднее, чем подгонять сид генератора.
 */
object Fixtures {

    fun engine(seed: Long = 1L) = GameEngine(SeededRng(seed))

    /** Колода из строки вида "FIRST": последняя буква строки будет взята первой. */
    fun deck(letters: String): List<Letter> = letters.map { char ->
        when (char.uppercaseChar()) {
            'F' -> F
            'I' -> I
            'R' -> R
            'S' -> S
            'T' -> T
            else -> error("Неизвестная буква: $char")
        }
    }

    fun cards(letters: String): List<Letter> = deck(letters)

    /** Колода нужного размера, добитая до 50 карт указанной буквой. */
    fun paddedDeck(letters: String = "", filler: Letter = I, size: Int = Rules.DECK_SIZE): List<Letter> {
        val head = deck(letters)
        return List(size - head.size) { filler } + head
    }

    /**
     * Состояние в фазе розыгрыша: ход стороны [turn], всё остальное — по умолчанию пустое.
     * Колоды по умолчанию непустые, чтобы случайный добор не заканчивал партию.
     */
    fun state(
        turn: Side = Side.YOU,
        youHand: String = "",
        aiHand: String = "",
        youSpace: String = "",
        aiSpace: String = "",
        youDiscard: String = "",
        aiDiscard: String = "",
        youDeck: List<Letter> = List(20) { I },
        aiDeck: List<Letter> = List(20) { I },
        traps: Traps = Traps(),
        firstTurnDone: Boolean = true,
    ): GameState = GameState(
        you = SideState(
            deck = youDeck,
            hand = cards(youHand),
            space = cards(youSpace),
            discard = cards(youDiscard),
        ),
        ai = SideState(
            deck = aiDeck,
            hand = cards(aiHand),
            space = cards(aiSpace),
            discard = cards(aiDiscard),
        ),
        turn = turn,
        firstPlayer = turn,
        firstTurnDone = firstTurnDone,
        traps = traps,
        phase = Phase.AWAITING_PLAY,
    )

    /** Индекс варианта выбора по букве. */
    fun GameState.optionIndexOf(letter: Letter): Int =
        pending?.options?.indexOfFirst { it.letter == letter }
            ?: error("Нет ожидающего выбора")

    fun EngineResult.choose(engine: GameEngine, optionIndex: Int): EngineResult {
        val next = engine.apply(state, Command.ChooseOption(optionIndex))
        return EngineResult(next.state, events + next.events)
    }

    fun EngineResult.play(engine: GameEngine, handIndex: Int): EngineResult {
        val next = engine.apply(state, Command.PlayCard(handIndex))
        return EngineResult(next.state, events + next.events)
    }

    inline fun <reified E : GameEvent> List<GameEvent>.has(): Boolean = any { it is E }

    inline fun <reified E : GameEvent> List<GameEvent>.first(): E =
        firstNotNullOf { it as? E }

    inline fun <reified E : GameEvent> List<GameEvent>.count(): Int = count { it is E }
}
