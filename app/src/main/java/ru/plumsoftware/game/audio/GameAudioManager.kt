package ru.plumsoftware.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator

class GameAudioManager(context: Context) {

    private var soundEnabled = true
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun playCorrect() {
        if (!soundEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
    }

    fun playWrong() {
        if (!soundEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 150)
    }

    fun playComplete() {
        if (!soundEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }

    fun playCoin() {
        if (!soundEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
    }

    fun release() {
        toneGenerator.release()
    }
}
