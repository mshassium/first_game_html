package com.first.game.domain

/**
 * Пять школ магии. Порядок объявления задаёт порядок отображения на столе.
 */
enum class Letter {
    F, I, R, S, T;

    companion object {
        val ALL: List<Letter> = entries.toList()
    }
}

enum class Side {
    YOU, AI;

    val other: Side get() = if (this == YOU) AI else YOU
}

object Rules {
    /** Карт каждой буквы в колоде одной стороны. */
    const val CARDS_PER_LETTER = 10

    /** Размер колоды одной стороны. */
    const val DECK_SIZE = CARDS_PER_LETTER * 5

    /** Стартовая рука. */
    const val START_HAND = 5

    /** Максимум карт в руке; лишние уходят в сброс. */
    const val HAND_LIMIT = 7

    /** Сколько одинаковых карт в SPACE даёт победу. */
    const val SAME_LETTERS_TO_WIN = 5
}

data class SideState(
    val deck: List<Letter> = emptyList(),
    val hand: List<Letter> = emptyList(),
    val space: List<Letter> = emptyList(),
    val discard: List<Letter> = emptyList(),
) {
    /** Сумма всех карт стороны. Инвариант: всегда равна [Rules.DECK_SIZE]. */
    val totalCards: Int get() = deck.size + hand.size + space.size + discard.size

    fun inSpace(letter: Letter): Int = space.count { it == letter }

    /** Сколько разных букв уже лежит в SPACE (0..5). */
    val distinctInSpace: Int get() = Letter.ALL.count { l -> space.any { it == l } }

    /** Наибольшее количество одинаковых букв в SPACE. */
    val maxSameInSpace: Int get() = Letter.ALL.maxOf { l -> inSpace(l) }

    /** Буквы, которых не хватает до полного набора F-I-R-S-T. */
    val missingForFirst: List<Letter> get() = Letter.ALL.filter { l -> space.none { it == l } }
}

data class Traps(
    /** Буква, запрещённая игроку. */
    val forbidOnYou: Letter? = null,
    /** Буква, запрещённая ИИ. */
    val forbidOnAi: Letter? = null,
    /** Сколько ловушек висит на игроке. */
    val trapsOnYou: Int = 0,
    /** Сколько ловушек висит на ИИ. */
    val trapsOnAi: Int = 0,
) {
    fun forbidOn(side: Side): Letter? = if (side == Side.YOU) forbidOnYou else forbidOnAi

    fun withForbidOn(side: Side, letter: Letter?): Traps =
        if (side == Side.YOU) copy(forbidOnYou = letter) else copy(forbidOnAi = letter)

    fun trapsOn(side: Side): Int = if (side == Side.YOU) trapsOnYou else trapsOnAi

    fun withTrapsOn(side: Side, count: Int): Traps =
        if (side == Side.YOU) copy(trapsOnYou = count) else copy(trapsOnAi = count)
}

enum class Phase {
    /** Активная сторона должна разыграть карту. */
    AWAITING_PLAY,

    /** Активная сторона должна ответить на [PendingChoice]. */
    AWAITING_CHOICE,

    /** Партия завершена, [GameState.outcome] заполнен. */
    GAME_OVER,
}

enum class ChoiceKind {
    /** F: какую букву запретить оппоненту. */
    FORBID_LETTER,

    /** R: какую карту вернуть из своего сброса. */
    RECOVER_LETTER,

    /** S: какую карту в SPACE оппонента отправить в его сброс. */
    STEAL_TARGET,

    /** T: какую карту сбросить из руки по сработавшей ловушке. */
    TRAP_DISCARD,
}

/**
 * Вариант выбора. [index] — позиция в исходной коллекции (рука, сброс, SPACE),
 * трактуется в зависимости от [ChoiceKind]. [letter] — что показать игроку.
 */
data class ChoiceOption(val index: Int, val letter: Letter)

data class PendingChoice(
    val side: Side,
    val kind: ChoiceKind,
    val options: List<ChoiceOption>,
)

enum class EndReason {
    /** Собран полный набор F-I-R-S-T. */
    FIRST_SET,

    /** Собрано 5 одинаковых букв. */
    FIVE_OF_A_KIND,

    /** У проигравшего кончилась колода при обязательном доборе. */
    DECK_OUT,
}

data class Outcome(val winner: Side, val reason: EndReason)

data class GameState(
    val you: SideState,
    val ai: SideState,
    val turn: Side,
    val firstPlayer: Side,
    val firstTurnDone: Boolean,
    val traps: Traps = Traps(),
    val phase: Phase = Phase.AWAITING_PLAY,
    val pending: PendingChoice? = null,
    val outcome: Outcome? = null,
    val turnNumber: Int = 0,
) {
    fun side(s: Side): SideState = if (s == Side.YOU) you else ai

    fun withSide(s: Side, transform: (SideState) -> SideState): GameState =
        if (s == Side.YOU) copy(you = transform(you)) else copy(ai = transform(ai))

    val isOver: Boolean get() = phase == Phase.GAME_OVER

    /** Сторона, от которой сейчас ждут команду. */
    val actingSide: Side get() = pending?.side ?: turn
}
