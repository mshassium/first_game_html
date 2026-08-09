package com.first.game.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import java.util.Locale

/**
 * Локализация. Все строки живут в assets/i18n — в коде их быть не должно.
 */
object Strings {

    enum class Language(val code: String, val label: String) {
        EN("en", "English"),
        RU("ru", "Русский");

        companion object {
            fun of(code: String): Language = entries.firstOrNull { it.code == code } ?: EN
        }
    }

    var language: Language = Language.EN
        private set

    private lateinit var bundle: I18NBundle

    fun load(language: Language) {
        this.language = language
        bundle = I18NBundle.createBundle(
            Gdx.files.internal("i18n/strings"),
            Locale(language.code),
            "UTF-8",
        )
    }

    operator fun get(key: String): String = bundle[key]

    /**
     * Есть ли такая строка. Нужно там, где ключ складывается из данных сервера:
     * незнакомый код ошибки не должен превращаться в «???key???» на экране.
     */
    fun has(key: String): Boolean = runCatching { bundle[key] }.getOrNull()?.startsWith("???") == false

    fun format(key: String, vararg args: Any): String = bundle.format(key, *args)

    /** Следующий язык по кругу — для кнопки-переключателя. */
    fun nextLanguage(): Language {
        val values = Language.entries
        return values[(values.indexOf(language) + 1) % values.size]
    }
}
