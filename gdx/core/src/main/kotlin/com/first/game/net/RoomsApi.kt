package com.first.game.net

/** Комната в списке. */
data class RoomInfo(
    val id: String,
    val code: String,
    val name: String,
    val host: String,
    val hasPassword: Boolean,
)

/** Состояние своей комнаты: пришёл ли гость и началась ли партия. */
data class RoomState(
    val id: String,
    val code: String,
    val status: String,
    val guest: String?,
    val matchId: String?,
)

/** Чем закончился запрос: успехом или кодом ошибки от сервера. */
sealed interface NetResult<out T> {
    data class Ok<T>(val value: T) : NetResult<T>

    /** [code] — код из ответа сервера либо `offline`, если до него не дошли. */
    data class Fail(val code: String) : NetResult<Nothing>
}

/** Комнаты: список, создание, вход, выход. */
object RoomsApi {

    fun list(onDone: (NetResult<List<RoomInfo>>) -> Unit) {
        Http.get("${NetConfig.API_BASE}/rooms", Auth.accessToken) { result ->
            onDone(result.map { json ->
                json.array("rooms").map { room ->
                    RoomInfo(
                        id = room.str("id").orEmpty(),
                        code = room.str("code").orEmpty(),
                        name = room.str("name").orEmpty(),
                        host = room.str("host").orEmpty(),
                        hasPassword = room.bool("hasPassword"),
                    )
                }
            })
        }
    }

    fun setNickname(nickname: String, onDone: (NetResult<String>) -> Unit) {
        Http.post("${NetConfig.API_BASE}/profile", Json.obj("nickname" to nickname), Auth.accessToken) { result ->
            onDone(result.map { json -> json.str("nickname").orEmpty() })
        }
    }

    fun create(name: String, password: String?, onDone: (NetResult<RoomInfo>) -> Unit) {
        val body = Json.obj("name" to name, "password" to password?.takeIf { it.isNotBlank() })
        Http.post("${NetConfig.API_BASE}/rooms", body, Auth.accessToken) { result ->
            onDone(result.map { json ->
                RoomInfo(
                    id = json.str("id").orEmpty(),
                    code = json.str("code").orEmpty(),
                    name = json.str("name").orEmpty(),
                    host = Auth.nickname.orEmpty(),
                    hasPassword = json.bool("hasPassword"),
                )
            })
        }
    }

    /** Состояние комнаты: хозяин опрашивает его, пока ждёт соперника. */
    fun state(roomId: String, onDone: (NetResult<RoomState>) -> Unit) {
        Http.get("${NetConfig.API_BASE}/rooms/$roomId", Auth.accessToken) { result ->
            onDone(result.map { json ->
                RoomState(
                    id = json.str("id").orEmpty(),
                    code = json.str("code").orEmpty(),
                    status = json.str("status").orEmpty(),
                    guest = json.str("guest"),
                    matchId = json.str("matchId"),
                )
            })
        }
    }

    /** Вход по id из списка или по коду, набранному вручную. */
    fun join(roomId: String?, code: String?, password: String?, onDone: (NetResult<String>) -> Unit) {
        val body = Json.obj(
            "roomId" to roomId,
            "code" to code?.uppercase(),
            "password" to password?.takeIf { it.isNotBlank() },
        )
        Http.post("${NetConfig.API_BASE}/rooms/join", body, Auth.accessToken) { result ->
            onDone(result.map { json -> json.str("matchId").orEmpty() })
        }
    }

    fun leave(roomId: String, onDone: (NetResult<Unit>) -> Unit = {}) {
        Http.post("${NetConfig.API_BASE}/rooms/$roomId/leave", "{}", Auth.accessToken) { result ->
            onDone(result.map { })
        }
    }
}

/**
 * Ответ сервера в результат: тело разбирается только при успехе, иначе наружу
 * идёт код ошибки. Недоступный сервер — это `offline`, чтобы экран мог сказать
 * «нет связи», а не «непонятная ошибка».
 */
internal inline fun <T> HttpResult.map(transform: (com.badlogic.gdx.utils.JsonValue?) -> T): NetResult<T> =
    when {
        ok -> NetResult.Ok(transform(json))
        status == 0 -> NetResult.Fail("offline")
        else -> NetResult.Fail(error ?: "server_error")
    }
