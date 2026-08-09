package com.first.game.net

import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

/**
 * Минимум работы с JSON: разбор ответов сервера и сборка тел запросов.
 *
 * Читаем через [JsonReader] — он разбирает текст в дерево и не использует
 * рефлексию, поэтому одинаково работает и на десктопе, и в веб-сборке, где
 * рефлексии нет вовсе. Пишем строкой: все тела запросов в игре — это два-три
 * поля, и ради них тащить сериализацию незачем.
 */
object Json {

    private val reader = JsonReader()

    fun parse(raw: String): JsonValue? = runCatching { reader.parse(raw) }.getOrNull()

    /** Объект из пар «ключ — значение». null-значения пропускаются. */
    fun obj(vararg fields: Pair<String, Any?>): String =
        fields.filter { it.second != null }.joinToString(",", "{", "}") { (key, value) ->
            "${quote(key)}:${literal(value)}"
        }

    private fun literal(value: Any?): String = when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        else -> quote(value.toString())
    }

    /** Экранирование по RFC 8259: кавычки, слэш, управляющие символы. */
    fun quote(text: String): String = buildString(text.length + 2) {
        append('"')
        for (char in text) {
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                else -> if (char < ' ') append("\\u").append(char.code.toString(16).padStart(4, '0'))
                else append(char)
            }
        }
        append('"')
    }
}

/** Строковое поле или null: пустые и явные null в JSON трактуются одинаково. */
fun JsonValue?.str(name: String): String? =
    this?.get(name)?.takeIf { !it.isNull }?.asString()

fun JsonValue?.int(name: String, fallback: Int = 0): Int =
    this?.get(name)?.takeIf { !it.isNull }?.asInt() ?: fallback

fun JsonValue?.bool(name: String, fallback: Boolean = false): Boolean =
    this?.get(name)?.takeIf { !it.isNull }?.asBoolean() ?: fallback

fun JsonValue?.obj(name: String): JsonValue? =
    this?.get(name)?.takeIf { !it.isNull }

/** Элементы массива по имени поля. Отсутствующий массив — пустой список. */
fun JsonValue?.array(name: String): List<JsonValue> {
    val node = this?.get(name) ?: return emptyList()
    val items = mutableListOf<JsonValue>()
    var child: JsonValue? = node.child
    while (child != null) {
        items.add(child)
        child = child.next
    }
    return items
}
