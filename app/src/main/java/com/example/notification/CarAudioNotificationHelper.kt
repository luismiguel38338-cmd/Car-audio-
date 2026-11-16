package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object CarAudioNotificationHelper {
    const val CHANNEL_ID_STATUS = "audio_dsp_playback_channel"
    const val CHANNEL_ID_ALERTS = "audio_dsp_alerts_channel"
    const val CHANNEL_ID_ERRORS = "audio_dsp_errors_channel"

    const val NOTIFICATION_ID_MONITOR = 1001
    const val NOTIFICATION_ID_CLIP_ALERT = 1002
    const val NOTIFICATION_ID_FAULT_BASE = 1100

    private const val STUDIO_ACCENT_NOTIF_COLOR = 0xFF78B7E8.toInt()
    @Volatile private var channelsInitialized = false

    fun initNotificationChannels(context: Context) {
        if (channelsInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val statusChannel = NotificationChannel(
                    CHANNEL_ID_STATUS,
                    "Reproductor Media3 y Procesador DSP",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Controles multimedia de Audio DSP Pro en pantalla bloqueada y barra de notificaciones"
                    setShowBadge(false)
                }

                val alertChannel = NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Alertas de Saturación (Clipping)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisa cuando la ganancia provoca recorte o saturación en la señal de salida"
                    enableVibration(true)
                    setShowBadge(true)
                }

                val errorChannel = NotificationChannel(
                    CHANNEL_ID_ERRORS,
                    "Diagnóstico del Motor de Audio",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifica fallos de lectura, permisos o compatibilidad AudioFX"
                    enableVibration(true)
                    setShowBadge(true)
                }

                manager.createNotificationChannel(statusChannel)
                manager.createNotificationChannel(alertChannel)
                manager.createNotificationChannel(errorChannel)
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

    fun showClippingAlertNotification(context: Context, clipCount: Int) {
        if (!hasNotificationPermission(context)) return
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(STUDIO_ACCENT_NOTIF_COLOR)
            .setContentTitle("Alerta de Saturación en Salida")
            .setContentText("Recorte digital detectado ($clipCount eventos). Reduce el preamplificador o activa el Limitador.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CLIP_ALERT, notification)
        } catch (_: SecurityException) {}
    }

    fun showSystemFaultNotification(
        context: Context,
        title: String,
        message: String,
        faultCode: Int = 0
    ) {
        if (!hasNotificationPermission(context)) return
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            200 + (faultCode and 0xFF),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ERRORS)
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(STUDIO_ACCENT_NOTIF_COLOR)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_FAULT_BASE + (faultCode and 0x3F),
                notification
            )
        } catch (_: SecurityException) {}
    }
}
