package com.first.game.domain

/**
 * Что произошло по правилам. Презентационный слой проигрывает эти события
 * анимацией и пишет их в лог боя; сам он состояние не меняет.
 */
sealed interface GameEvent {

    data class GameStarted(
        val firstPlayer: Side,
        val youRoll: Int,
        val aiRoll: Int,
    ) : GameEvent

    /** Карта роздана в стартовую руку. */
    data class CardDealt(val side: Side, val letter: Letter) : GameEvent

    data class TurnBegan(val side: Side, val turnNumber: Int) : GameEvent

    data class CardDrawn(val side: Side, val letter: Letter, val deckLeft: Int) : GameEvent

    /** Рука переполнена — карта ушла в сброс. */
    data class HandOverflow(val side: Side, val letter: Letter) : GameEvent

    /** Ловушка сработала: сторона сбросила карту. */
    data class TrapTriggered(val side: Side, val letter: Letter, val trapsLeft: Int) : GameEvent

    /** Ловушка не сработала: рука была пуста. */
    data class TrapFizzled(val side: Side, val trapsLeft: Int) : GameEvent

    /** Ход пропущен: разыгрывать нечем. */
    data class TurnSkipped(val side: Side) : GameEvent

    data class CardPlayed(val side: Side, val letter: Letter) : GameEvent

    /** Запрет сработал: карта ушла в свой сброс, эффект не выполнен. */
    data class CardForbidden(val side: Side, val letter: Letter) : GameEvent

    /** F: запрет наложен стороной [by] на её оппонента. */
    data class ForbidSet(val by: Side, val letter: Letter) : GameEvent

    /** R: карта возвращена из сброса в руку. */
    data class CardRecovered(val side: Side, val letter: Letter) : GameEvent

    /** S: карта из SPACE стороны [victim] отправлена в её сброс. */
    data class CardStolen(val victim: Side, val letter: Letter, val spaceIndex: Int) : GameEvent

    /** T: ловушка поставлена на сторону [on]. */
    data class TrapSet(val on: Side, val trapCount: Int) : GameEvent

    /** Эффект не сработал: нечего возвращать или нечего забирать. */
    data class EffectFizzled(val side: Side, val letter: Letter) : GameEvent

    data class ChoiceRequired(val choice: PendingChoice) : GameEvent

    data class TurnEnded(val side: Side) : GameEvent

    data class GameEnded(val outcome: Outcome) : GameEvent
}

/** Результат применения команды: новое состояние и что показать. */
data class EngineResult(val state: GameState, val events: List<GameEvent>)
