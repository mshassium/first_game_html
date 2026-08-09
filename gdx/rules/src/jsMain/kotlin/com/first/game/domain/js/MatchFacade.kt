package com.first.game.domain.js

import com.first.game.domain.net.MatchService
import com.first.game.domain.net.Seat

/**
 * Точка входа для серверной функции мультиплеера.
 *
 * Обёртка нарочно тонкая: вся логика в `MatchService` из общего кода, здесь
 * только перевод в типы, которые понимает JavaScript. Места приходят строками
 * `"A"`/`"B"` — так их отдаёт база.
 *
 * Ошибки не бросаются, а возвращаются кодом в [JsMatchResult.error]: серверная
 * функция превращает его в HTTP-ответ, и исключение через границу языков ей
 * только мешало бы.
 */
@JsExport
object MatchFacade {

    /** Новая партия. Событиями идут бросок кубиков и раздача. */
    fun newMatch(seed: String): JsMatchResult = MatchService.newMatch(seed).toJs()

    /**
     * Применить команду места [seat] (`"play;<индекс>"` или `"choose;<индекс>"`).
     * [seed] должен включать номер версии партии, иначе повторится расклад.
     */
    fun apply(state: String, seat: String, command: String, seed: String): JsMatchResult {
        val parsed = Seat.ofOrNull(seat) ?: return JsMatchResult(false, "BAD_SEAT", "", "")
        return MatchService.apply(state, parsed, command, seed).toJs()
    }

    /**
     * Завершить партию по внешней причине: `"TIMEOUT"` или `"SURRENDER"`.
     * [winner] — место, которому засчитывается победа.
     */
    fun finish(state: String, winner: String, reason: String): JsMatchResult {
        val seat = Seat.ofOrNull(winner) ?: return JsMatchResult(false, "BAD_SEAT", "", "")
        val cause = runCatching { com.first.game.domain.EndReason.valueOf(reason) }.getOrNull()
            ?: return JsMatchResult(false, "BAD_REASON", "", "")
        return MatchService.finish(state, seat, cause).toJs()
    }

    /** Состояние в перспективе места, без карт соперника. null — состояние битое. */
    fun viewFor(state: String, seat: String): String? =
        Seat.ofOrNull(seat)?.let { MatchService.viewFor(state, it) }

    /** События в перспективе места, без карт соперника. */
    fun eventsFor(events: String, seat: String): String? =
        Seat.ofOrNull(seat)?.let { MatchService.eventsFor(events, it) }

    fun isOver(state: String): Boolean = MatchService.isOver(state)

    /** `"A"`, `"B"` или null, пока партия идёт. */
    fun winnerSeat(state: String): String? = MatchService.winnerSeat(state)?.name

    /** Чьей команды ждут: `"A"`, `"B"` или null. */
    fun actingSeat(state: String): String? = MatchService.actingSeat(state)?.name
}

/** Результат для JS: успех, код ошибки и строки состояния и событий. */
@JsExport
class JsMatchResult internal constructor(
    val ok: Boolean,
    val error: String?,
    val state: String,
    val events: String,
)

private fun com.first.game.domain.net.MatchResult.toJs(): JsMatchResult =
    JsMatchResult(ok, error?.name, state, events)
