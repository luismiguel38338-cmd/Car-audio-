package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.session.MediaSession
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object CarAudioNotificationHelper {
    const val CHANNEL_ID_STATUS = "car_audio_status_channel"
    const val CHANNEL_ID_ALERTS = "car_audio_alerts_channel"

    const val NOTIFICATION_ID_MONITOR = 1001
    const val NOTIFICATION_ID_CLIP_ALERT = 1002

    private const val CRIMSON_NOTIF_COLOR = 0xFFFF172B.toInt()
    @Volatile private var channelsInitialized = false

    fun initNotificationChannels(context: Context) {
        if (channelsInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val statusChannel = NotificationChannel(
                    CHANNEL_ID_STATUS,
                    "Reproductor y Ecualizador DSP",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Controles multimedia y estado del procesador DSP"
                    setShowBadge(false)
                }

                val alertChannel = NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Alertas de Clipping de Audio",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisa cuando la ganancia provoca recorte o distorsión en la salida"
                    enableVibration(true)
                    setShowBadge(true)
                }

                manager.createNotificationChannel(statusChannel)
                manager.createNotificationChannel(alertChannel)
                channelsInitialized = true
            } catch (_: Throwable) {}
        } else {
            channelsInitialized = true
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @Suppress("DEPRECATION")
    fun buildMediaPlaybackNotification(
        context: Context,
        mediaSession: MediaSession,
        title: String,
        artist: String,
        isPlaying: Boolean,
        isDspActive: Boolean,
        albumArt: Bitmap?,
        prevPendingIntent: PendingIntent,
        playPausePendingIntent: PendingIntent,
        nextPendingIntent: PendingIntent,
        stopPendingIntent: PendingIntent
    ): Notification {
        initNotificationChannels(context)

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID_STATUS)
        } else {
            Notification.Builder(context)
        }

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseLabel = if (isPlaying) "Pausar" else "Reproducir"
        val dspBadge = if (isDspActive) "DSP Rojo Activo" else "Bypass"

        builder
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(CRIMSON_NOTIF_COLOR)
            .setContentTitle(title)
            .setContentText("$artist • $dspBadge")
            .setContentIntent(openAppIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, "Anterior", prevPendingIntent).build())
            .addAction(Notification.Action.Builder(playPauseIcon, playPauseLabel, playPausePendingIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, "Siguiente", nextPendingIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Cerrar", stopPendingIntent).build())
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        if (albumArt != null) {
            builder.setLargeIcon(albumArt)
        }

        return builder.build()
    }

    fun showMonitorNotification(
        context: Context,
        trackTitle: String,
        isPlaying: Boolean,
        isClipping: Boolean = false
    ) {
        if (!hasNotificationPermission(context)) return
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clipText = if (isClipping) "CLIP ACTIVO" else "Señal Limpia"
        val playState = if (isPlaying) "Reproduciendo" else "Pausado"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(CRIMSON_NOTIF_COLOR)
            .setContentTitle("CAR AUDIO DSP • $trackTitle")
            .setContentText("$playState • $clipText")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MONITOR, notification)
        } catch (_: SecurityException) {}
    }

    fun showClippingAlertNotification(context: Context, clipCount: Int) {
        if (!hasNotificationPermission(context)) return
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(CRIMSON_NOTIF_COLOR)
            .setContentTitle("Alerta de Clipping en Salida")
            .setContentText("Distorsión detectada ($clipCount eventos). Reduce la ganancia o activa el Limitador.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CLIP_ALERT, notification)
        } catch (_: SecurityException) {}
    }
}
