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
    const val CHANNEL_ID_STATUS = "car_audio_status_channel"
    const val CHANNEL_ID_ALERTS = "car_audio_alerts_channel"
    
    const val NOTIFICATION_ID_MONITOR = 1001
    const val NOTIFICATION_ID_CLIP_ALERT = 1002
    const val NOTIFICATION_ID_VOLT_ALERT = 1003

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Monitor status channel
            val statusChannel = NotificationChannel(
                CHANNEL_ID_STATUS,
                "Monitor Planta Car Audio & Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el estado activo de la planta car audio, voltajes y reproductor"
                setShowBadge(false)
            }
            
            // Critical alerts channel (Clipping & Low Voltage)
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertas Críticas de Planta Car Audio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de clipping, sobrecalentamiento y bajón de voltaje de batería"
                enableVibration(true)
                setShowBadge(true)
            }
            
            manager.createNotificationChannel(statusChannel)
            manager.createNotificationChannel(alertChannel)
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

    fun showMonitorNotification(
        context: Context,
        trackTitle: String,
        isPlaying: Boolean,
        voltage: Float,
        temperatureC: Float,
        isClipping: Boolean
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clipText = if (isClipping) "⚠️ CLIP ACTIVO" else "✅ Señal Limpia"
        val playState = if (isPlaying) "▶️ Reproduciendo" else "⏸️ Pausado"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.car_audio_icon_1788805964010)
            .setContentTitle("CarAudio DSP • $trackTitle")
            .setContentText("$playState | Voltaje: ${"%.1f".format(voltage)}V | Temp: ${temperatureC.toInt()}°C | $clipText")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Pista: $trackTitle ($playState)\n" +
                    "Planta: ${"%.1f".format(voltage)}V • ${temperatureC.toInt()}°C • $clipText\n" +
                    "DSP Crossover activo con protección de transistores."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MONITOR, notification)
        } catch (_: SecurityException) {
            // Permission rejected
        }
    }

    fun showClippingAlertNotification(context: Context, clipCount: Int) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.car_audio_icon_1788805964010)
            .setContentTitle("⚠️ ¡ALERTA DE CLIPPING EN PLANTA!")
            .setContentText("Distorsión armónica detectada ($clipCount eventos). Baja la ganancia para no quemar las bobinas.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Se ha detectado recorte de señal (Clipping) en la salida de tu amplificador.\n" +
                    "Riesgo de sobrecalentamiento de bobinas en subwoofers o drivers de agudos.\n" +
                    "Acción recomendada: Reducir Bass Boost o atenuar la ganancia (Gain) en el procesador."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CLIP_ALERT, notification)
        } catch (_: SecurityException) {
        }
    }

    fun showVoltageAlertNotification(context: Context, voltage: Float) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.car_audio_icon_1788805964010)
            .setContentTitle("🔋 ¡BAJÓN DE VOLTAJE DE BATERÍA!")
            .setContentText("Voltaje crítico: ${"%.1f".format(voltage)}V. Riesgo de protección de la planta car audio.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "El voltaje del sistema cayó a ${"%.1f".format(voltage)}V (mínimo seguro: 12.0V - 14.4V con alternador).\n" +
                    "El amplificador puede entrar en modo PROTECT o distorsionar fuertemente."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_VOLT_ALERT, notification)
        } catch (_: SecurityException) {
        }
    }
}
