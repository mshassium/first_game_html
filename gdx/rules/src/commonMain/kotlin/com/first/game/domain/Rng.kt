package com.first.game.domain

import kotlin.random.Random

/**
 * Источник случайности. Инжектируется, чтобы партию можно было точно воспроизвести
 * по сиду — в тестах и при разборе багов.
 */
interface Rng {
    /** Случайное число в диапазоне [0, bound). */
    fun nextInt(bound: Int): Int
}

class SeededRng(seed: Long) : Rng {
    private val random = Random(seed)
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}

/** Кубик d6. */
fun Rng.rollDie(): Int = nextInt(6) + 1

/** Перемешивание Фишера — Йетса на инжектированном [Rng]. */
fun <T> List<T>.shuffled(rng: Rng): List<T> {
    val result = toMutableList()
    for (i in result.lastIndex downTo 1) {
        val j = rng.nextInt(i + 1)
        val tmp = result[i]
        result[i] = result[j]
        result[j] = tmp
    }
    return result
}
