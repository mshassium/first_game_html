package com.first.game.audio

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import com.first.game.GamePrefs

/**
 * Звук по спецификации docs/gdx/07-audio-spec.md.
 *
 * Файлов может не быть — их подбирают отдельно. Отсутствующий звук молча пропускается:
 * игра не должна падать из-за незакрытой позиции ассетов. Отсутствующий трек при этом
 * не глушит уже играющий: лучше продолжать старую музыку, чем уйти в тишину.
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
    private var currentTrack: Track? = null
    private var fadingOut: Music? = null
    private var fadeProgress = 1f

    /**
     * Браузер не даёт играть звук до первого жеста пользователя. На остальных
     * платформах ограничения нет, и ждать касания незачем — иначе меню встречает тишиной.
     */
    var unlocked: Boolean = false
        private set

    private var pendingTrack: Track? = null

    fun load() {
        unlocked = Gdx.app.type != Application.ApplicationType.WebGL

        var found = 0
        for (sfx in Sfx.entries) {
            val handle = Gdx.files.internal(sfx.file)
            if (handle.exists()) {
                runCatching { sounds[sfx] = Gdx.audio.newSound(handle) }.onSuccess { found++ }
            }
        }
        val musicFound = Track.entries.count { Gdx.files.internal(it.file).exists() }
        Gdx.app.log("audio", "звуков $found из ${Sfx.entries.size}, треков $musicFound из ${Track.entries.size}")
    }

    fun play(sfx: Sfx, pitchVariation: Boolean = true) {
        if (!unlocked) return
        val sound = sounds[sfx] ?: return
        val pitch = if (pitchVariation) 0.94f + Math.random().toFloat() * 0.12f else 1f
        sound.play(GamePrefs.sfxVolume, pitch, 0f)
    }

    /** Включает трек с кроссфейдом. Уже играющий трек повторно не перезапускается. */
    fun playMusic(track: Track) {
        if (!unlocked) {
            pendingTrack = track
            return
        }
        if (track == currentTrack && current?.isPlaying == true) return

        val music = tracks.getOrPut(track) {
            val handle = Gdx.files.internal(track.file)
            // Трека нет — оставляем играть то, что уже звучит.
            if (!handle.exists()) return
            Gdx.audio.newMusic(handle).apply { isLooping = true }
        }

        fadingOut?.stop()
        fadingOut = current
        fadeProgress = if (fadingOut == null) 1f else 0f

        current = music
        currentTrack = track
        music.volume = if (fadingOut == null) GamePrefs.musicVolume else 0f
        music.play()
        Gdx.app.log("audio", "играет ${track.file}, громкость ${GamePrefs.musicVolume}")
    }

    /** Двигает кроссфейд. Вызывается экранами каждый кадр. */
    fun update(delta: Float) {
        if (fadeProgress >= 1f) return
        fadeProgress = (fadeProgress + delta / CROSSFADE_SECONDS).coerceAtMost(1f)
        val volume = GamePrefs.musicVolume
        current?.volume = volume * fadeProgress
        fadingOut?.volume = volume * (1f - fadeProgress)
        if (fadeProgress >= 1f) {
            fadingOut?.stop()
            fadingOut = null
        }
    }

    /**
     * Вызывается при каждом касании экрана.
     *
     * Первое касание снимает браузерный запрет. Последующие — страховка: если
     * запуск не удался (мобильный браузер мог отклонить его, вкладку сворачивали,
     * звуковой контекст усыпили), пробуем ещё раз. Одной попытки мало:
     * на телефоне первая проваливается регулярно.
     */
    fun unlock() {
        if (!unlocked) {
            unlocked = true
            pendingTrack?.let { playMusic(it) }
            pendingTrack = null
            return
        }
        val music = current ?: return
        if (!music.isPlaying) {
            music.volume = GamePrefs.musicVolume
            music.play()
        }
    }

    fun applyVolumes() {
        if (fadeProgress >= 1f) current?.volume = GamePrefs.musicVolume
    }

    override fun dispose() {
        sounds.values.forEach { it.dispose() }
        tracks.values.forEach { it.dispose() }
        sounds.clear()
        tracks.clear()
        current = null
        fadingOut = null
    }

    private companion object {
        /** Длительность кроссфейда между треками, docs/gdx/07-audio-spec.md §3. */
        const val CROSSFADE_SECONDS = 1.2f
    }
}
