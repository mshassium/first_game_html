package com.first.game.domain.net

import com.first.game.domain.Side

/**
 * Место в комнате.
 *
 * Домен знает только про [Side.YOU] и [Side.AI] — переименовывать их ради сети
 * незачем, это переписало бы половину презентации ради косметики. Вместо этого
 * сервер хранит партию «в системе координат места A», а место B получает то же
 * состояние отражённым ([Mirror]). Каждый клиент видит себя как [Side.YOU]
 * и не догадывается, что играет по сети.
 */
enum class Seat {
    /** Хозяин комнаты. */
    A,

    /** Гость. */
    B;

    val side: Side get() = if (this == A) Side.YOU else Side.AI

    val other: Seat get() = if (this == A) B else A

    companion object {
        fun of(side: Side): Seat = if (side == Side.YOU) A else B

        fun ofOrNull(raw: String): Seat? = entries.firstOrNull { it.name == raw }
    }
}
