package com.first.game

import com.badlogic.gdx.Gdx
import com.first.game.domain.ai.Difficulty
import com.first.game.i18n.Strings

/** Настройки игрока. Партия между запусками не сохраняется (решение D-8). */
object GamePrefs {

    private val prefs by lazy { Gdx.app.getPreferences("first_game_prefs") }

    var language: Strings.Language
        get() = Strings.Language.of(prefs.getString(KEY_LANGUAGE, defaultLanguage().code))
        set(value) = prefs.putString(KEY_LANGUAGE, value.code).flush()

    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC, 0.45f)
        set(value) = prefs.putFloat(KEY_MUSIC, value.coerceIn(0f, 1f)).flush()

    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX, 0.8f)
        set(value) = prefs.putFloat(KEY_SFX, value.coerceIn(0f, 1f)).flush()

    var animationSpeed: AnimationSpeed
        get() = AnimationSpeed.of(prefs.getString(KEY_SPEED, AnimationSpeed.NORMAL.name))
        set(value) = prefs.putString(KEY_SPEED, value.name).flush()

    var difficulty: Difficulty
        get() = runCatching { Difficulty.valueOf(prefs.getString(KEY_DIFFICULTY, Difficulty.DEFAULT.name)) }
            .getOrDefault(Difficulty.DEFAULT)
        set(value) = prefs.putString(KEY_DIFFICULTY, value.name).flush()

    private fun defaultLanguage(): Strings.Language {
        val locale = java.util.Locale.getDefault().language
        return if (locale == "ru") Strings.Language.RU else Strings.Language.EN
    }

    private const val KEY_LANGUAGE = "language"
    private const val KEY_MUSIC = "music"
    private const val KEY_SFX = "sfx"
    private const val KEY_SPEED = "speed"
    private const val KEY_DIFFICULTY = "difficulty"
}

enum class AnimationSpeed(val factor: Float, val labelKey: String) {
    NORMAL(1f, "settings.speed.normal"),
    FAST(0.6f, "settings.speed.fast"),
    INSTANT(0f, "settings.speed.instant");

    companion object {
        fun of(name: String): AnimationSpeed = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}
