package com.example.data

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

object SoundManager {
    private var isSoundEnabled = true

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun isEnabled(): Boolean = isSoundEnabled

    private fun playSynthSound(frequencyStart: Float, frequencyEnd: Float, durationMs: Long, waveType: String = "sine") {
        if (!isSoundEnabled) return
        Thread {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val currentFreq = frequencyStart + (frequencyEnd - frequencyStart) * progress
                    val angle = 2.0 * Math.PI * currentFreq * (i.toDouble() / sampleRate)
                    val value = when (waveType) {
                        "sine" -> sin(angle)
                        "square" -> if (sin(angle) >= 0) 0.5 else -0.5
                        "saw" -> 2.0 * ((currentFreq * i / sampleRate) % 1.0) - 1.0
                        else -> sin(angle)
                    }
                    buffer[i] = (value * 12000).toInt().toShort() // Keep amplitude clean to avoid clipping
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                Thread.sleep(durationMs + 30)
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playSpinShort() {
        playSynthSound(450f, 250f, 40, "square")
    }

    fun playWheelSuccess() {
        playSynthSound(350f, 1000f, 300, "sine")
    }

    fun playSuccess() {
        Thread {
            try {
                playSynthSound(523.25f, 523.25f, 90, "sine") // C5
                Thread.sleep(90)
                playSynthSound(659.25f, 659.25f, 90, "sine") // E5
                Thread.sleep(90)
                playSynthSound(783.99f, 783.99f, 90, "sine") // G5
                Thread.sleep(90)
                playSynthSound(1046.50f, 1046.50f, 200, "sine") // C6
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playPass() {
        playSynthSound(220f, 130f, 250, "square")
    }

    fun playSuspense() {
        playSynthSound(180f, 380f, 500, "sine")
    }
}
