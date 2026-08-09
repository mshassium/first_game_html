package com.first.game.teavm

import com.badlogic.gdx.ApplicationListener
import org.teavm.jso.JSBody

/**
 * Обёртка, которая показывает падение игры в вебе.
 *
 * Бэкенд ловит исключение первого кадра, помечает приложение сломанным и молча
 * перестаёт планировать кадры: экран остаётся чёрным, в консоли пусто. Отладить
 * такое нечем, поэтому исключения перехватываются здесь и печатаются в консоль
 * браузера до того, как их проглотит бэкенд.
 */
class CrashReporter(private val delegate: ApplicationListener) : ApplicationListener {

    override fun create() = guard("create") { delegate.create() }

    override fun resize(width: Int, height: Int) = guard("resize") { delegate.resize(width, height) }

    override fun render() = guard("render") { delegate.render() }

    override fun pause() = guard("pause") { delegate.pause() }

    override fun resume() = guard("resume") { delegate.resume() }

    override fun dispose() = guard("dispose") { delegate.dispose() }

    private inline fun guard(stage: String, body: () -> Unit) {
        try {
            body()
        } catch (error: Throwable) {
            report("[$stage] ${error::class.simpleName}: ${error.message}")
            for (line in error.stackTraceToString().lines().take(20)) report("    $line")
            throw error
        }
    }

}

/**
 * Печать в консоль браузера напрямую.
 *
 * Именно верхнего уровня: TeaVM принимает `@JSBody` только на статическом
 * методе, а в companion object он оказывается методом объекта.
 */
@JSBody(params = ["message"], script = "console.error(message);")
private external fun report(message: String)
