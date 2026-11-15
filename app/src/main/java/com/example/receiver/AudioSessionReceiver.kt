package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import com.example.audio.CarAudioEngine

/**
 * BroadcastReceiver to intercept Android audio sessions opened by YouTube,
 * Spotify, and other media playback apps, allowing real-time equalization.
 */
class AudioSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Reproductor"

        val engine = CarAudioEngine.getInstance(context)

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId >= 0) {
                    engine.onExternalSessionOpened(sessionId, packageName)
                }
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId >= 0) {
                    engine.onExternalSessionClosed(sessionId)
                }
            }
        }
    }
}
