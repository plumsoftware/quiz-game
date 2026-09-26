package ru.plumsoftware.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.staticCompositionLocalOf
import ru.plumsoftware.game.R
import ru.plumsoftware.game.data.GameSettings
import java.util.Locale

/** Звуки из §10. Файлы лежат в res/raw. */
enum class Sfx(val res: Int) {
    TAP(R.raw.sfx_tap),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    TICK(R.raw.sfx_tick),
    COIN(R.raw.sfx_coin),
    CHEST(R.raw.sfx_chest),
    STAR(R.raw.sfx_star),
    LEVEL_UP(R.raw.sfx_levelup),
    ACHIEVEMENT(R.raw.sfx_achievement)
}

enum class Vibe { LIGHT, MEDIUM, SUCCESS }

/**
 * Звук, музыка, вибрация и озвучка вопросов (ТЗ §5.5, §5.11, §10).
 * Все настройки берутся из [GameSettings] через [applySettings].
 */
class GameFeedback(context: Context) {
    private val app = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val soundIds: Map<Sfx, Int> = Sfx.entries.associateWith { soundPool.load(app, it.res, 1) }

    private var settings = GameSettings()
    private var music: MediaPlayer? = null
    private var inForeground = false
    private var musicSuppressed = false
    private var speaking = false

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    fun applySettings(s: GameSettings) {
        settings = s
        if (!s.voice) stopSpeaking()
        updateMusic()
    }

    fun play(sfx: Sfx) {
        if (!settings.sound) return
        soundIds[sfx]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun vibrate(kind: Vibe) {
        if (!settings.vibro) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val effect = when (kind) {
            Vibe.LIGHT -> VibrationEffect.createOneShot(15, 80)
            Vibe.MEDIUM -> VibrationEffect.createOneShot(60, 180)
            Vibe.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40, 60, 80), -1)
        }
        try {
            v.vibrate(effect)
        } catch (_: Exception) {
        }
    }

    // ---------- музыка ----------

    fun onForeground(foreground: Boolean) {
        inForeground = foreground
        updateMusic()
    }

    /** Глушим музыку на время видеорекламы (§10). */
    fun setMusicSuppressed(suppressed: Boolean) {
        musicSuppressed = suppressed
        updateMusic()
    }

    private fun updateMusic() {
        val shouldPlay = settings.music && inForeground && !musicSuppressed
        try {
            if (shouldPlay) {
                val player = music ?: MediaPlayer.create(app, R.raw.music_loop)?.also {
                    it.isLooping = true
                    music = it
                } ?: return
                player.setVolume(musicVolume(), musicVolume())
                if (!player.isPlaying) player.start()
            } else {
                music?.let { if (it.isPlaying) it.pause() }
            }
        } catch (_: Exception) {
        }
    }

    private fun musicVolume(): Float = if (speaking) 0.08f else 0.35f

    // ---------- озвучка ----------

    /** Озвучивает вопрос системным TTS, если включено (§5.11). */
    fun speak(text: String) {
        if (!settings.voice) return
        if (tts == null) {
            pendingText = text
            tts = TextToSpeech(app) { status ->
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) {
                    tts?.setLanguage(Locale("ru", "RU"))
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = setSpeaking(true)
                        override fun onDone(utteranceId: String?) = setSpeaking(false)

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) = setSpeaking(false)
                    })
                    pendingText?.let { speakNow(it) }
                    pendingText = null
                }
            }
            return
        }
        if (ttsReady) speakNow(text) else pendingText = text
    }

    private var pendingText: String? = null

    private fun speakNow(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "q_${text.hashCode()}")
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        setSpeaking(false)
    }

    private fun setSpeaking(value: Boolean) {
        speaking = value
        try {
            music?.setVolume(musicVolume(), musicVolume())
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            soundPool.release()
            music?.release()
            music = null
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }
}

/** Доступ к звукам из любого экрана. null — в превью. */
val LocalFeedback = staticCompositionLocalOf<GameFeedback?> { null }
