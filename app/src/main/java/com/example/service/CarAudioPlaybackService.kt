package com.example.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import com.example.audio.CarAudioEngine
import com.example.model.FaultSeverity
import com.example.notification.CarAudioNotificationHelper

@OptIn(UnstableApi::class)
class CarAudioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val audioEngine by lazy { CarAudioEngine.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        CarAudioNotificationHelper.initNotificationChannels(this)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.volume = audioEngine.volumeFactor
        audioEngine.onVolumeChangedListener = { vol ->
            try {
                exoPlayer?.volume = vol.coerceIn(0f, 1f)
            } catch (_: Throwable) {}
        }

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
                    audioEngine.attachToPlayerSession(audioSessionId)
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                val sid = player.audioSessionId
                if (sid != C.AUDIO_SESSION_ID_UNSET && sid > 0 && audioEngine.activeAudioSessionId != sid) {
                    audioEngine.attachToPlayerSession(sid)
                }
                val item = mediaItem ?: return
                val current = audioEngine.currentTrack
                if (current == null || (current.id != item.mediaId && current.stableKey != item.mediaId)) {
                    val fallbackTrack = com.example.model.AudioTrackItem(
                        id = item.mediaId.ifBlank { "media_${System.currentTimeMillis()}" },
                        title = item.mediaMetadata.title?.toString() ?: "Pista de Audio",
                        artist = item.mediaMetadata.artist?.toString() ?: "Biblioteca Local",
                        album = item.mediaMetadata.albumTitle?.toString() ?: "Música",
                        durationSeconds = (player.duration / 1000L).toInt().coerceAtLeast(0),
                        customUri = item.localConfiguration?.uri,
                        albumArtUri = item.mediaMetadata.artworkUri
                    )
                    audioEngine.syncPlaybackStateFromMedia3(
                        track = fallbackTrack,
                        playing = player.isPlaying,
                        positionSeconds = (player.currentPosition / 1000L).toInt().coerceAtLeast(0)
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val sid = player.audioSessionId
                if (sid != C.AUDIO_SESSION_ID_UNSET && sid > 0 && audioEngine.activeAudioSessionId != sid) {
                    audioEngine.attachToPlayerSession(sid)
                }
                audioEngine.syncPlaybackStateFromMedia3(
                    track = audioEngine.currentTrack,
                    playing = isPlaying,
                    positionSeconds = (player.currentPosition / 1000L).toInt()
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                audioEngine.onAudioFaultListener?.invoke(
                    "Error en Reproductor Media3",
                    "No se pudo reproducir el archivo seleccionado: ${error.localizedMessage ?: error.errorCodeName}.",
                    FaultSeverity.ERROR
                )
            }
        })

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CarAudioNotificationHelper.CHANNEL_ID_STATUS)
            .setNotificationId(CarAudioNotificationHelper.NOTIFICATION_ID_MONITOR)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_notification_dsp)
            }
        setMediaNotificationProvider(notificationProvider)

        exoPlayer = player
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        audioEngine.onVolumeChangedListener = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}
