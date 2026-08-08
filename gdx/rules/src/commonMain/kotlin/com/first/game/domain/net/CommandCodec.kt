package com.first.game.domain.net

import com.first.game.domain.Command

/**
 * Строковое представление команды.
 *
 * Всё, что игрок может прислать серверу, — это одно из двух чисел: какую карту
 * руки разыграть или какой вариант выбрать. Проверять здесь нечего: допустимость
 * индекса решает движок, а не разбор строки.
 */
object CommandCodec {

    fun encode(command: Command): String = when (command) {
        is Command.PlayCard -> "play;${command.handIndex}"
        is Command.ChooseOption -> "choose;${command.optionIndex}"
    }

    fun decode(raw: String): Command {
        val parts = raw.split(';')
        require(parts.size >= 2) { "команда должна быть вида kind;index" }
        val index = parts[1].toIntOrNull() ?: throw IllegalArgumentException("индекс не число: ${parts[1]}")
        return when (parts[0]) {
            "play" -> Command.PlayCard(index)
            "choose" -> Command.ChooseOption(index)
            else -> throw IllegalArgumentException("неизвестная команда: ${parts[0]}")
        }
    }

    fun decodeOrNull(raw: String): Command? = runCatching { decode(raw) }.getOrNull()
}
