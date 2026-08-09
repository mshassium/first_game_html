package com.first.game.net

/**
 * Адреса сетевой игры.
 *
 * Публикуемый ключ Supabase лежит здесь открытым текстом намеренно: он для того
 * и существует, чтобы уезжать в клиент. Прав он не даёт никаких — всё решают
 * политики базы и токен игрока. Секретный ключ в игру не попадает никогда,
 * он живёт только в переменных окружения сервера.
 */
object NetConfig {

    /** Серверные функции: комнаты, ходы, тайм-ауты. */
    const val API_BASE = "https://first-game-api.vercel.app/api"

    const val SUPABASE_URL = "https://zugfqmlvohvxqdfbmiee.supabase.co"

    const val SUPABASE_KEY = "sb_publishable_PninOTHDbXfn-x60uKmYNw_8P_qtzJ9"

    /** Поток изменений: сюда приходит ход соперника. */
    val REALTIME_URL: String
        get() = SUPABASE_URL.replace("https://", "wss://") + "/realtime/v1/websocket?apikey=$SUPABASE_KEY&vsn=1.0.0"

    /** Сколько ждать ответа сервера, прежде чем считать запрос потерянным. */
    const val HTTP_TIMEOUT_MS = 15_000

    /** Как часто опрашивать сервер, когда сокет не поднялся. */
    const val POLL_SECONDS = 2f

    /** Сколько ждать сокет, прежде чем перейти на опрос. */
    const val SOCKET_GRACE_SECONDS = 5f
}
