package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.CarAudioEngine
import kotlin.math.abs
import kotlin.math.roundToInt

class FloatingEqualizerService : Service() {

    private lateinit var windowManager: WindowManager
    private val audioEngine by lazy { CarAudioEngine.getInstance(applicationContext) }

    private var bubbleView: FrameLayout? = null
    private var windowView: FrameLayout? = null

    private var bubbleParams: WindowManager.LayoutParams? = null
    private var windowParams: WindowManager.LayoutParams? = null

    companion object {
        const val CHANNEL_ID = "car_audio_floating_eq_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START = "ACTION_START_FLOATING_EQ"
        const val ACTION_STOP = "ACTION_STOP_FLOATING_EQ"

        private const val COLOR_ELECTRIC = 0xFF00A3FF.toInt()
        private const val COLOR_NEON_GREEN = 0xFF00FF87.toInt()
        private const val COLOR_SOFT_ICE = 0xFFD9EEFF.toInt()
        private const val COLOR_DARK_CARD = 0xF50B0E14.toInt()
        private const val COLOR_DARK_CHIP = 0xFF1A2232.toInt()
        private const val COLOR_BYPASS = 0xFF232C3D.toInt()

        fun start(context: Context) {
            try {
                val intent = Intent(context, FloatingEqualizerService::class.java).apply {
                    action = ACTION_START
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
                val intent = Intent(context, FloatingEqualizerService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (_: Throwable) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            createNotificationChannel()
        } catch (_: Throwable) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val notif = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notif,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notif)
            }
        } catch (_: Throwable) {}

        try {
            if (Settings.canDrawOverlays(this)) {
                if (bubbleView == null && windowView == null) {
                    showFloatingBubble()
                }
            } else {
                stopSelf()
            }
        } catch (_: Throwable) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pantalla Flotante de Ecualizador",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Permite ecualizar YouTube y música en segundo plano"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingEqualizerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ecualizador Flotante Activo • DSP Studio")
            .setContentText("Toca la burbuja azul eléctrico para ecualizar en vivo")
            .setSmallIcon(R.drawable.ic_notification_dsp)
            .setColor(COLOR_ELECTRIC)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cerrar", stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun tintSeekBarAccent(seekBar: SeekBar) {
        val accentList = ColorStateList.valueOf(COLOR_ELECTRIC)
        val trackList = ColorStateList.valueOf(0xFF1D2C42.toInt())
        seekBar.thumbTintList = accentList
        seekBar.progressTintList = accentList
        seekBar.progressBackgroundTintList = trackList
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingBubble() {
        if (bubbleView != null) return

        val bubbleSizePx = dpToPx(62)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 250
        }
        bubbleParams = params

        val frame = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(COLOR_DARK_CARD)
                setStroke(dpToPx(2), COLOR_ELECTRIC)
            }
            background = bg
            elevation = dpToPx(8).toFloat()

            val icon = ImageView(context).apply {
                setImageResource(R.drawable.ic_floating_open_circle)
                val pad = dpToPx(6)
                setPadding(pad, pad, pad, pad)
            }
            addView(
                icon,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        frame.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(frame, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    if (diffX < 12 && diffY < 12) {
                        expandToWindow()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            bubbleView = frame
            windowManager.addView(frame, params)
        } catch (_: Throwable) {
            bubbleView = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun expandToWindow() {
        if (bubbleView != null) {
            try {
                windowManager.removeView(bubbleView)
            } catch (_: Exception) {}
            bubbleView = null
        }

        val windowWidthPx = dpToPx(340)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            windowWidthPx,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        windowParams = params

        val rootCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dpToPx(14)
            setPadding(pad, pad, pad, pad)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(18).toFloat()
                setColor(COLOR_DARK_CARD)
                setStroke(dpToPx(2), COLOR_ELECTRIC)
            }
            background = bg
            elevation = dpToPx(12).toFloat()
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleTv = TextView(this).apply {
            text = "DSP LIVE EQ • v6.5.0"
            setTextColor(COLOR_ELECTRIC)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        val statusTv = TextView(this).apply {
            text = "● Grafito Eléctrico • Tiempo Real"
            setTextColor(COLOR_NEON_GREEN)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        }

        titleCol.addView(titleTv)
        titleCol.addView(statusTv)

        val minimizeBtn = Button(this).apply {
            text = "▼"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setBackgroundColor(Color.TRANSPARENT)
            val btnSize = dpToPx(36)
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
            setOnClickListener { collapseToBubble() }
        }

        val closeBtn = Button(this).apply {
            text = "✕"
            setTextColor(COLOR_ELECTRIC)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.TRANSPARENT)
            val btnSize = dpToPx(36)
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
            setOnClickListener { stopSelf() }
        }

        header.addView(titleCol)
        header.addView(minimizeBtn)
        header.addView(closeBtn)
        rootCard.addView(header)

        // Master Power Toggle
        val powerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(6), 0, dpToPx(6))
        }

        val powerBtn = Button(this).apply {
            val isEnabled = audioEngine.currentDspSettings.dspMasterEnabled
            text = if (isEnabled) "DSP STUDIO ACTIVO (ON)" else "DSP BYPASS (OFF)"
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(if (isEnabled) COLOR_ELECTRIC else COLOR_BYPASS)
            }
            setTextColor(if (isEnabled) 0xFF04111D.toInt() else Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(36), 1f)
            setOnClickListener {
                val newState = !audioEngine.currentDspSettings.dspMasterEnabled
                audioEngine.toggleMasterPower(newState)
                text = if (newState) "DSP STUDIO ACTIVO (ON)" else "DSP BYPASS (OFF)"
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                    setColor(if (newState) COLOR_ELECTRIC else COLOR_BYPASS)
                }
                setTextColor(if (newState) 0xFF04111D.toInt() else Color.WHITE)
            }
        }
        powerRow.addView(powerBtn)
        rootCard.addView(powerRow)

        // Bass Boost & Preamp Row
        val fxRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(6), 0, dpToPx(6))
        }

        val bassCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val bassLabel = TextView(this).apply {
            val bbVal = audioEngine.currentDspSettings.bassBoostDb
            text = "BASS BOOST: ${bbVal.roundToInt()} dB"
            setTextColor(COLOR_SOFT_ICE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
        }
        val bassSeek = SeekBar(this).apply {
            max = 24
            progress = audioEngine.currentDspSettings.bassBoostDb.roundToInt().coerceIn(0, 24)
            tintSeekBarAccent(this)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        bassLabel.text = "BASS BOOST: $progress dB"
                        audioEngine.setQuickBassBoost(progress.toFloat())
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
        bassCol.addView(bassLabel)
        bassCol.addView(bassSeek)

        val loudCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val loudLabel = TextView(this).apply {
            val preamp = audioEngine.currentDspSettings.preampDb
            text = "POTENCIA: ${preamp.roundToInt()} dB"
            setTextColor(COLOR_NEON_GREEN)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
        }
        val loudSeek = SeekBar(this).apply {
            max = 30
            progress = (audioEngine.currentDspSettings.preampDb + 12f).roundToInt().coerceIn(0, 30)
            tintSeekBarAccent(this)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val db = progress - 12
                        loudLabel.text = "POTENCIA: $db dB"
                        audioEngine.setQuickLoudness(db.toFloat())
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
        loudCol.addView(loudLabel)
        loudCol.addView(loudSeek)

        fxRow.addView(bassCol)
        fxRow.addView(loudCol)
        rootCard.addView(fxRow)

        val eqTitle = TextView(this).apply {
            text = "ECUALIZADOR EN TIEMPO REAL"
            setTextColor(COLOR_SOFT_ICE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(6), 0, dpToPx(4))
        }
        rootCard.addView(eqTitle)

        val bandsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dpToPx(6))
        }

        val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6k", "14k")
        val bandSeekBars = mutableListOf<SeekBar>()
        val bandGainLabels = mutableListOf<TextView>()

        for (i in 0..4) {
            val initialDb = audioEngine.getQuickBandGain(i).roundToInt().coerceIn(-15, 15)
            val bandCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val bName = TextView(this).apply {
                text = bandLabels[i]
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
                typeface = Typeface.DEFAULT_BOLD
            }

            val bGainTv = TextView(this).apply {
                text = if (initialDb > 0) "+${initialDb}dB" else "${initialDb}dB"
                setTextColor(COLOR_NEON_GREEN)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f)
            }

            val bSeek = SeekBar(this).apply {
                max = 30
                progress = initialDb + 15
                tintSeekBarAccent(this)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val db = progress - 15
                            bGainTv.text = if (db > 0) "+${db}dB" else "${db}dB"
                            audioEngine.setQuickBandGain(i, db.toFloat())
                        }
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })
            }

            bandSeekBars.add(bSeek)
            bandGainLabels.add(bGainTv)
            bandCol.addView(bName)
            bandCol.addView(bGainTv)
            bandCol.addView(bSeek)
            bandsRow.addView(bandCol)
        }
        rootCard.addView(bandsRow)

        val scrollPresets = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, dpToPx(4), 0, 0)
        }
        val presetsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val presets = listOf("PUNCH BASS", "YOUTUBE VOCAL", "ROCK", "CLUB", "FLAT")
        presets.forEach { presetName ->
            val pBtn = Button(this).apply {
                text = presetName
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(6).toFloat()
                    setColor(COLOR_DARK_CHIP)
                    setStroke(dpToPx(1), COLOR_ELECTRIC)
                }
                val padH = dpToPx(8)
                val padV = dpToPx(2)
                setPadding(padH, padV, padH, padV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(32)
                ).apply {
                    setMargins(dpToPx(2), 0, dpToPx(2), 0)
                }

                setOnClickListener {
                    audioEngine.applyQuickPreset(presetName)
                    bassSeek.progress = audioEngine.currentDspSettings.bassBoostDb.roundToInt().coerceIn(0, 24)
                    bassLabel.text = "BASS BOOST: ${bassSeek.progress} dB"
                    loudSeek.progress = (audioEngine.currentDspSettings.preampDb + 12f).roundToInt().coerceIn(0, 30)
                    loudLabel.text = "POTENCIA: ${loudSeek.progress - 12} dB"
                    for (idx in 0..4) {
                        val db = audioEngine.getQuickBandGain(idx).roundToInt().coerceIn(-15, 15)
                        bandSeekBars[idx].progress = db + 15
                        bandGainLabels[idx].text = if (db > 0) "+${db}dB" else "${db}dB"
                    }
                }
            }
            presetsRow.addView(pBtn)
        }
        scrollPresets.addView(presetsRow)
        rootCard.addView(scrollPresets)

        val frameWrapper = FrameLayout(this).apply {
            addView(rootCard)
        }

        try {
            windowView = frameWrapper
            windowManager.addView(frameWrapper, params)
        } catch (_: Throwable) {
            windowView = null
        }
    }

    private fun collapseToBubble() {
        if (windowView != null) {
            try {
                windowManager.removeView(windowView)
            } catch (_: Exception) {}
            windowView = null
        }
        showFloatingBubble()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bubbleView != null) {
            try {
                windowManager.removeView(bubbleView)
            } catch (_: Exception) {}
            bubbleView = null
        }
        if (windowView != null) {
            try {
                windowManager.removeView(windowView)
            } catch (_: Exception) {}
            windowView = null
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
