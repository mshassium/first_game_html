package com.first.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import com.first.game.GamePrefs

/**
 * Звук по спецификации docs/gdx/07-audio-spec.md.
 *
 * Файлов может не быть — их подбирают из CC0-библиотек отдельно. Отсутствующий
 * звук молча пропускается: игра не должна падать из-за незакрытой позиции ассетов.
 */
class SoundManager : Disposable {

    enum class Sfx(val file: String) {
        CARD_DRAW("audio/sfx_card_draw.mp3"),
        CARD_PLACE("audio/sfx_card_place.mp3"),
        CARD_FLIP("audio/sfx_card_flip.mp3"),
        CARD_DISCARD("audio/sfx_card_discard.mp3"),
        FORBID_CAST("audio/sfx_forbid_cast.mp3"),
        FORBID_TRIGGER("audio/sfx_forbid_trigger.mp3"),
        INCREASE("audio/sfx_increase.mp3"),
        RECOVER("audio/sfx_recover.mp3"),
        STEAL("audio/sfx_steal.mp3"),
        TRAP_SET("audio/sfx_trap_set.mp3"),
        TRAP_SNAP("audio/sfx_trap_snap.mp3"),
        DICE_ROLL("audio/sfx_dice_roll.mp3"),
        UI_CLICK("audio/sfx_ui_click.mp3"),
        TURN_START("audio/sfx_turn_start.mp3"),
        VICTORY("audio/sfx_victory.mp3"),
        DEFEAT("audio/sfx_defeat.mp3"),
    }

    enum class Track(val file: String) {
        MENU("audio/music_menu.mp3"),
        BATTLE("audio/music_battle.mp3"),
    }

    private val sounds = mutableMapOf<Sfx, Sound>()
    private val tracks = mutableMapOf<Track, Music>()
    private var current: Music? = null

    /** Веб-браузер не даёт играть звук до первого жеста пользователя. */
    var unlocked: Boolean = false
        private set

    private var pendingTrack: Track? = null

    fun load() {
        for (sfx in Sfx.entries) {
            val handle = Gdx.files.internal(sfx.file)
            if (handle.exists()) {
                runCatching { sounds[sfx] = Gdx.audio.newSound(handle) }
            }
        }
    }

    fun play(sfx: Sfx, pitchVariation: Boolean = true) {
        if (!unlocked) return
        val sound = sounds[sfx] ?: return
        val pitch = if (pitchVariation) 0.94f + Math.random().toFloat() * 0.12f else 1f
        sound.play(GamePrefs.sfxVolume, pitch, 0f)
    }

    fun playMusic(track: Track) {
        if (!unlocked) {
            pendingTrack = track
            return
        }
        if (current != null && tracks[track] === current) return
        current?.stop()
        val music = tracks.getOrPut(track) {
            val handle = Gdx.files.internal(track.file)
            if (!handle.exists()) return
            Gdx.audio.newMusic(handle)
        }
        music.isLooping = true
        music.volume = GamePrefs.musicVolume
        music.play()
        current = music
    }

    /** Вызывается при первом касании экрана — после него звук разрешён. */
    fun unlock() {
        if (unlocked) return
        unlocked = true
        pendingTrack?.let { playMusic(it) }
        pendingTrack = null
    }

    fun applyVolumes() {
        current?.volume = GamePrefs.musicVolume
    }

    override fun dispose() {
        sounds.values.forEach { it.dispose() }
        tracks.values.forEach { it.dispose() }
        sounds.clear()
        tracks.clear()
    }
}
