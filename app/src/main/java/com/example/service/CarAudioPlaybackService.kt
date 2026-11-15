package com.example.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import com.example.audio.AlbumArtCache
import com.example.audio.CarAudioEngine
import com.example.notification.CarAudioNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground media playback service keeping background music, lock-screen metadata,
 * Bluetooth AVRCP info, and notification transport controls active even when the UI is minimized.
 */
class CarAudioPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioEngine by lazy { CarAudioEngine.getInstance(applicationContext) }
    private var mediaSession: MediaSession? = null

    companion object {
        const val ACTION_UPDATE_STATE = "com.example.action.UPDATE_PLAYBACK"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREV = "com.example.action.PREV"
        const val ACTION_STOP = "com.example.action.STOP"

        var onSkipNextRequested: (() -> Unit)? = null
        var onSkipPrevRequested: (() -> Unit)? = null

        fun sync(context: Context) {
            try {
                val intent = Intent(context, CarAudioPlaybackService::class.java).apply {
                    action = ACTION_UPDATE_STATE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Throwable) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, CarAudioPlaybackService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (_: Throwable) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        CarAudioNotificationHelper.initNotificationChannels(this)
        mediaSession = MediaSession(this, "CarAudioDspSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    audioEngine.resumePlayback(audioEngine.currentDspSettings, audioEngine.currentEqualizerSettings)
                    refreshNotificationAndSession()
                }

                override fun onPause() {
                    audioEngine.pausePlayback()
                    refreshNotificationAndSession()
                }

                override fun onSkipToNext() {
                    onSkipNextRequested?.invoke()
                }

                override fun onSkipToPrevious() {
                    onSkipPrevRequested?.invoke()
                }

                override fun onSeekTo(pos: Long) {
                    audioEngine.seekTo((pos / 1000L).toInt())
                    refreshNotificationAndSession()
                }

                override fun onStop() {
                    audioEngine.stopPlayback()
                    stopForegroundCompat()
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                audioEngine.togglePlayPause(audioEngine.currentDspSettings, audioEngine.currentEqualizerSettings)
                refreshNotificationAndSession()
            }
            ACTION_NEXT -> onSkipNextRequested?.invoke()
            ACTION_PREV -> onSkipPrevRequested?.invoke()
            ACTION_STOP -> {
                audioEngine.stopPlayback()
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> refreshNotificationAndSession()
        }
        return START_NOT_STICKY
    }

    private fun refreshNotificationAndSession() {
        val track = audioEngine.currentTrack
        val title = track?.title ?: "CAR AUDIO DSP PRO"
        val artist = track?.artist ?: "Reproductor Local"
        val playing = audioEngine.isPlaying
        val session = mediaSession ?: return

        val stateCode = if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SEEK_TO or
            PlaybackState.ACTION_STOP

        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(stateCode, audioEngine.playbackPositionSeconds * 1000L, 1.0f)
                .build()
        )

        val cachedArt: Bitmap? = AlbumArtCache.getCached(track?.customUri)
        publishNotification(session, title, artist, track?.durationSeconds ?: 0, playing, cachedArt)

        if (track?.customUri != null && cachedArt == null) {
            serviceScope.launch {
                val loaded = AlbumArtCache.loadOrGet(applicationContext, track.customUri)
                if (loaded != null && audioEngine.currentTrack?.id == track.id) {
                    publishNotification(session, title, artist, track.durationSeconds, audioEngine.isPlaying, loaded)
                }
            }
        }
    }

    private fun publishNotification(
        session: MediaSession,
        title: String,
        artist: String,
        durationSec: Int,
        playing: Boolean,
        albumArt: Bitmap?
    ) {
        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, durationSec * 1000L)
        if (albumArt != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
        }
        session.setMetadata(metadataBuilder.build())

        val notif = CarAudioNotificationHelper.buildMediaPlaybackNotification(
            context = this,
            mediaSession = session,
            title = title,
            artist = artist,
            isPlaying = playing,
            isDspActive = audioEngine.currentDspSettings.dspMasterEnabled,
            albumArt = albumArt,
            prevPendingIntent = actionIntent(ACTION_PREV, 10),
            playPausePendingIntent = actionIntent(ACTION_PLAY_PAUSE, 11),
            nextPendingIntent = actionIntent(ACTION_NEXT, 12),
            stopPendingIntent = actionIntent(ACTION_STOP, 13)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    CarAudioNotificationHelper.NOTIFICATION_ID_MONITOR,
                    notif,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(CarAudioNotificationHelper.NOTIFICATION_ID_MONITOR, notif)
            }
        } catch (_: Throwable) {}
    }

    private fun actionIntent(action: String, reqCode: Int): PendingIntent {
        val intent = Intent(this, CarAudioPlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopForegroundCompat() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (_: Throwable) {}
        mediaSession = null
    }
}
