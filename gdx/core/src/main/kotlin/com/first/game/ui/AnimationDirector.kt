package com.first.game.ui

import com.first.game.AnimationSpeed

/**
 * Очередь анимаций поверх потока игровых событий.
 *
 * Состояние игры к моменту постановки в очередь уже изменено движком — режиссёр
 * только приводит картинку в соответствие. Поэтому упавшая или пропущенная
 * анимация не может испортить партию.
 */
class AnimationDirector(private val speed: () -> AnimationSpeed) {

    /** Шаг анимации: делает своё дело и вызывает [onDone], когда закончил. */
    private val queue = ArrayDeque<(onDone: () -> Unit) -> Unit>()

    private var running = false
    private var fastForward = false

    val isIdle: Boolean get() = !running && queue.isEmpty()

    fun enqueue(step: (onDone: () -> Unit) -> Unit) {
        queue.addLast(step)
        pump()
    }

    /** Доиграть всё немедленно — по тапу игрока. */
    fun skip() {
        if (isIdle) return
        fastForward = true
    }

    fun clear() {
        queue.clear()
        running = false
        fastForward = false
    }

    /** Длительность с учётом настройки скорости и режима «пропустить». */
    fun duration(seconds: Float): Float =
        if (fastForward) 0f else seconds * speed().factor

    private fun pump() {
        if (running) return
        val step = queue.removeFirstOrNull()
        if (step == null) {
            fastForward = false
            return
        }
        running = true
        step {
            running = false
            pump()
        }
    }
}
