package com.first.game.net

import com.badlogic.gdx.Gdx

/**
 * Кто мы для сервера.
 *
 * Регистрации нет: игра заводит анонимного пользователя Supabase и запоминает
 * его надолго. Токен доступа живёт час, поэтому рядом хранится второй, по
 * которому первый обновляется. Оба лежат в настройках игры — в вебе это
 * localStorage, так что перезагрузка страницы игрока не теряет.
 *
 * Ник — отдельная вещь: он живёт в профиле на сервере, а здесь только копия,
 * чтобы не спрашивать его при каждом запуске.
 */
object Auth {

    private val prefs by lazy { Gdx.app.getPreferences("first-net") }

    private const val KEY_ACCESS = "accessToken"
    private const val KEY_REFRESH = "refreshToken"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_EXPIRES = "expiresAt"

    var accessToken: String? = null
        private set

    var nickname: String?
        get() = prefs.getString(KEY_NICKNAME, "").takeIf { it.isNotBlank() }
        set(value) {
            prefs.putString(KEY_NICKNAME, value ?: "").flush()
        }

    val signedIn: Boolean get() = accessToken != null

    /**
     * Готовит токен к работе: продлевает сохранённый, а если продлевать нечего —
     * заводит нового анонимного пользователя.
     */
    fun signIn(onDone: (Boolean) -> Unit) {
        val stillValid = accessToken != null && prefs.getLong(KEY_EXPIRES, 0L) > now() + 60_000
        if (stillValid) {
            onDone(true)
            return
        }

        val refresh = prefs.getString(KEY_REFRESH, "")
        if (refresh.isNotBlank()) {
            refresh(refresh) { ok ->
                if (ok) onDone(true) else signUpAnonymously(onDone)
            }
        } else {
            signUpAnonymously(onDone)
        }
    }

    /** Забыть игрока. Нужно, если сессия испортилась и проще начать заново. */
    fun signOut() {
        accessToken = null
        prefs.remove(KEY_ACCESS)
        prefs.remove(KEY_REFRESH)
        prefs.remove(KEY_EXPIRES)
        prefs.flush()
    }

    private fun signUpAnonymously(onDone: (Boolean) -> Unit) {
        // Анонимный вход в Supabase — это регистрация с пустым телом.
        Http.post("${NetConfig.SUPABASE_URL}/auth/v1/signup", "{}") { result ->
            onDone(remember(result))
        }
    }

    private fun refresh(refreshToken: String, onDone: (Boolean) -> Unit) {
        val url = "${NetConfig.SUPABASE_URL}/auth/v1/token?grant_type=refresh_token"
        Http.post(url, Json.obj("refresh_token" to refreshToken)) { result ->
            onDone(remember(result))
        }
    }

    private fun remember(result: HttpResult): Boolean {
        val access = result.json.str("access_token") ?: run {
            Gdx.app.error("net", "вход не удался: код ${result.status}, ответ ${result.raw.take(200)}")
            return false
        }
        accessToken = access
        prefs.putString(KEY_ACCESS, access)
        result.json.str("refresh_token")?.let { prefs.putString(KEY_REFRESH, it) }
        val lifetimeMs = result.json.int("expires_in", 3600) * 1000L
        prefs.putLong(KEY_EXPIRES, now() + lifetimeMs)
        prefs.flush()
        return true
    }

    /**
     * Восстанавливает сессию из настроек при запуске. Сам по себе токен может
     * быть просрочен — это выяснится в [signIn].
     */
    fun load() {
        accessToken = prefs.getString(KEY_ACCESS, "").takeIf { it.isNotBlank() }
    }

    private fun now(): Long = System.currentTimeMillis()
}
