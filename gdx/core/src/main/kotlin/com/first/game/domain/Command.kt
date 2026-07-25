package com.first.game.domain

/**
 * Всё, что сторона может сообщить движку. Других способов изменить состояние нет.
 */
sealed interface Command {
    /** Разыграть карту из руки по индексу. Допустима только в [Phase.AWAITING_PLAY]. */
    data class PlayCard(val handIndex: Int) : Command

    /** Ответить на [PendingChoice], выбрав вариант по его позиции в списке options. */
    data class ChooseOption(val optionIndex: Int) : Command
}
