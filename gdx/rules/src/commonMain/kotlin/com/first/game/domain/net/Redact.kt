package com.first.game.domain.net

import com.first.game.domain.GameEvent
import com.first.game.domain.GameState
import com.first.game.domain.Letter
import com.first.game.domain.Side
import com.first.game.domain.SideState

/**
 * Вырезание скрытой информации перед отправкой клиенту.
 *
 * Полное состояние живёт только на сервере: в нём лежат обе руки и обе колоды.
 * Клиент получает вид, из которого вычеркнуто всё, чего игрок знать не должен.
 *
 * Что прячется:
 * - рука соперника — главное, что защищаем;
 * - колода соперника;
 * - **своя колода тоже** — порядок собственных карт означал бы знание будущего;
 * - варианты чужого ожидаемого выбора: по ловушке они строятся прямо из руки
 *   ([com.first.game.domain.ChoiceKind.TRAP_DISCARD]) и раскрыли бы её целиком.
 *
 * Что остаётся: размеры стопок, SPACE, сбросы, запреты, ловушки, чей ход и счёт.
 * Сброс открыт по правилам, поэтому события, кладущие карту в сброс или берущие
 * её оттуда, не редактируются — соперник видит эти карты и за столом.
 *
 * Скрытые карты заменяются [HIDDEN] — длина стопки сохраняется, потому что по ней
 * рисуются рубашки и счётчики. Отрисовывать эти буквы нельзя: экран показывает
 * чужие карты рубашкой, и на это опирается вся схема.
 */
object Redact {

    /** Чем заменяется скрытая карта. Любая буква: наружу она не показывается. */
    val HIDDEN = Letter.F

    /**
     * Вид состояния для [viewer]. Применяется после [Mirror], поэтому по умолчанию
     * зритель — [Side.YOU]: в своей перспективе игрок всегда эта сторона.
     */
    fun state(state: GameState, viewer: Side = Side.YOU): GameState {
        val own = state.side(viewer).hideDeck()
        val foe = state.side(viewer.other).hideDeck().hideHand()
        val pending = state.pending?.let { choice ->
            if (choice.side == viewer) choice else choice.copy(options = emptyList())
        }
        return state
            .withSide(viewer) { own }
            .withSide(viewer.other) { foe }
            .copy(pending = pending)
    }

    /** События в том же виде: чужие приходы карт обезличены. */
    fun events(events: List<GameEvent>, viewer: Side = Side.YOU): List<GameEvent> =
        events.map { event -> redactEvent(event, viewer) }

    private fun redactEvent(event: GameEvent, viewer: Side): GameEvent = when {
        // Карта приходит в чужую руку — букву знать нельзя.
        event is GameEvent.CardDealt && event.side != viewer -> event.copy(letter = HIDDEN)
        event is GameEvent.CardDrawn && event.side != viewer -> event.copy(letter = HIDDEN)
        // Соперник выбирает — сам факт виден, варианты нет.
        event is GameEvent.ChoiceRequired && event.choice.side != viewer ->
            event.copy(choice = event.choice.copy(options = emptyList()))
        else -> event
    }

    private fun SideState.hideDeck(): SideState = copy(deck = List(deck.size) { HIDDEN })

    private fun SideState.hideHand(): SideState = copy(hand = List(hand.size) { HIDDEN })
}
