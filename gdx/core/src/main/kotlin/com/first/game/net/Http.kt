package com.first.game.net

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpRequestBuilder
import com.badlogic.gdx.utils.JsonValue

/** Ответ сервера: код, разобранное тело и сырой текст на случай непонятного. */
class HttpResult(val status: Int, val json: JsonValue?, val raw: String) {

    val ok: Boolean get() = status in 200..299

    /** Код ошибки API (`{"error": "..."}`) или null. */
    val error: String? get() = json.str("error")
}

/**
 * HTTP поверх `Gdx.net`.
 *
 * Одна реализация на все платформы: в вебе libGDX переводит это в XHR, на
 * десктопе — в обычное соединение. Свой клиент пришлось бы писать дважды.
 *
 * Ответ приходит в чужом потоке, поэтому колбэк переносится в игровой через
 * `postRunnable`: трогать сцену из сетевого потока нельзя.
 */
object Http {

    fun get(url: String, token: String? = null, onDone: (HttpResult) -> Unit) =
        send("GET", url, null, token, onDone)

    fun post(url: String, body: String?, token: String? = null, onDone: (HttpResult) -> Unit) =
        send("POST", url, body, token, onDone)

    private fun send(
        method: String,
        url: String,
        body: String?,
        token: String?,
        onDone: (HttpResult) -> Unit,
    ) {
        val request = HttpRequestBuilder()
            .newRequest()
            .method(method)
            .url(url)
            .timeout(NetConfig.HTTP_TIMEOUT_MS)
            .header("Content-Type", "application/json")
            .apply {
                // Ключ проекта нужен только самой Supabase. Своему API он не нужен,
                // а браузер за лишний заголовок наказывает: preflight отклоняется,
                // если сервер не перечислил его в Access-Control-Allow-Headers.
                if (url.startsWith(NetConfig.SUPABASE_URL)) header("apikey", NetConfig.SUPABASE_KEY)
                if (token != null) header("Authorization", "Bearer $token")
                if (body != null) content(body)
            }
            .build()

        Gdx.net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(response: Net.HttpResponse) {
                val text = response.resultAsString ?: ""
                finish(HttpResult(response.status.statusCode, Json.parse(text), text), onDone)
            }

            override fun failed(t: Throwable) {
                Gdx.app.error("net", "$method $url: ${t.message}")
                // Ноль в статусе означает «до сервера не дошли» — клиенту этого
                // достаточно, чтобы показать «нет связи» и попробовать позже.
                finish(HttpResult(0, null, t.message ?: ""), onDone)
            }

            override fun cancelled() {
                finish(HttpResult(0, null, "отменено"), onDone)
            }
        })
    }

    private fun finish(result: HttpResult, onDone: (HttpResult) -> Unit) {
        Gdx.app.postRunnable { onDone(result) }
    }
}
