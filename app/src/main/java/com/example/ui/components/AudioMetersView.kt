package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.OutputDeviceStatus
import com.example.model.RtaBand

@Composable
fun AudioMetersView(
    theme: CarAudioThemeType,
    rtaBands: List<RtaBand>,
    oscilloscopeWave: FloatArray,
    leftVu: Float,
    rightVu: Float,
    rmsDb: Float,
    splDb: Float,
    isClippingDetected: Boolean,
    clipEventsCount: Int,
    isMicRtaActive: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    outputDeviceStatus: OutputDeviceStatus,
    powerTelemetry: DevicePowerTelemetry,
    onToggleMicRta: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("audio_meters_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudioSignalIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying || isMicRtaActive,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "MEDIDORES DE ESTUDIO • ENERGÍA POR BANDAS",
            showMiniBars = true
        )

        // 1. 31-Band Real-Time Spectrum Analyzer (RTA)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("meters_rta_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.accentColor)
                        Column {
                            Text("ANALIZADOR DE ESPECTRO (RTA 31B)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = if (isMicRtaActive) "Fuente: Micrófono Acústico (Sala)" else "Fuente: Reproducción Media3 / PCM",
                                color = theme.accentColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Button(
                        onClick = onToggleMicRta,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMicRtaActive) theme.accentColor else theme.cardColor,
                            contentColor = if (isMicRtaActive) theme.onPrimaryColor else theme.textColor
                        ),
                        modifier = Modifier.testTag("meters_mic_rta_button")
                    ) {
                        Icon(
                            imageVector = if (isMicRtaActive) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Micrófono RTA",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isMicRtaActive) "MIC ON" else "MIC RTA", fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor)
                        .border(1.dp, theme.cardColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bandCount = rtaBands.size.coerceAtLeast(1)
                        val gap = 2.dp.toPx()
                        val barW = ((size.width - gap * (bandCount - 1)) / bandCount).coerceAtLeast(2f)
                        val radius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                        // Subtle horizontal dB grid lines
                        listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                            drawLine(
                                color = theme.cardColor.copy(alpha = 0.7f),
                                start = Offset(0f, size.height * frac),
                                end = Offset(size.width, size.height * frac),
                                strokeWidth = 1f
                            )
                        }

                        rtaBands.forEachIndexed { idx, band ->
                            val normLevel = ((band.levelDb + 60f) / 60f).coerceIn(0.03f, 1f)
                            val normPeak = ((band.peakDb + 60f) / 60f).coerceIn(0.03f, 1f)
                            val x = idx * (barW + gap)
                            val h = size.height * normLevel
                            val yPeak = size.height * (1f - normPeak)
                            val barColor = if (band.levelDb > -4f) theme.clipAlertColor else theme.accentColor

                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(barColor, theme.primaryColor.copy(alpha = 0.25f))),
                                topLeft = Offset(x, size.height - h),
                                size = Size(barW, h),
                                cornerRadius = radius
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(x, yPeak),
                                end = Offset(x + barW, yPeak),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("20 Hz", "100 Hz", "500 Hz", "2 kHz", "8 kHz", "20 kHz").forEach { lbl ->
                        Text(lbl, color = theme.textSecondaryColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 2. Real-Time Oscilloscope Canvas
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("meters_oscilloscope_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = theme.accentColor)
                        Text("OSCILOSCOPIO DE FORMA DE ONDA", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = if (isAnyAudioPlaying || isMicRtaActive) "TIEMPO REAL" else "SIN SEÑAL",
                        color = theme.textSecondaryColor,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor)
                        .border(1.dp, theme.cardColor, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midY = size.height / 2f
                        drawLine(
                            color = theme.cardColor,
                            start = Offset(0f, midY),
                            end = Offset(size.width, midY),
                            strokeWidth = 1f
                        )
                        val count = oscilloscopeWave.size
                        if (count > 1) {
                            val path = Path()
                            val stepX = size.width / (count - 1).toFloat()
                            for (i in 0 until count) {
                                val sample = oscilloscopeWave[i].coerceIn(-1f, 1f)
                                val x = i * stepX
                                val y = midY - sample * (size.height * 0.44f)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = if (isClippingDetected) theme.clipAlertColor else theme.accentColor,
                                style = Stroke(width = 2.2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // 3. Audio Level Meter (VU L/R, RMS dBFS, Peak Clipping)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(
                1.dp,
                if (isClippingDetected) theme.clipAlertColor else theme.cardColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("meters_vu_rms_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MEDIDOR DE NIVEL (VU • RMS • PEAK)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    if (isClippingDetected) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Clipping detectado", tint = theme.clipAlertColor, modifier = Modifier.size(16.dp))
                            Text("CLIPPING", color = theme.clipAlertColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                VuMeterRow("Canal L", leftVu, theme)
                VuMeterRow("Canal R", rightVu, theme)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryStatChip("Nivel RMS", "%.1f dBFS".format(rmsDb), theme, Modifier.weight(1f))
                    TelemetryStatChip("SPL Est.", if (splDb > 0f) "%.1f dB".format(splDb) else "--", theme, Modifier.weight(1f))
                    TelemetryStatChip("Eventos Clip", "$clipEventsCount", theme, Modifier.weight(1f), highlightAlert = clipEventsCount > 0 && isClippingDetected)
                }
            }
        }

        // 4. Real Android Battery Telemetry (%, V, °C, and mA Current)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("meters_battery_telemetry_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (powerTelemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = "Telemetría de Batería",
                            tint = theme.accentColor
                        )
                        Column {
                            Text("TELEMETRÍA REAL DE BATERÍA", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("Salida actual: ${outputDeviceStatus.label}", color = theme.textSecondaryColor, fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onRefreshTelemetry, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar telemetría", tint = theme.accentColor)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryStatChip(
                        label = "Carga",
                        value = powerTelemetry.batteryPercent?.let { "$it%" } ?: "N/D",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = "Voltaje",
                        value = powerTelemetry.batteryVoltageV?.let { "%.2f V".format(it) } ?: "N/D",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = "Temp",
                        value = powerTelemetry.batteryTempC?.let { "%.1f °C".format(it) } ?: "N/D",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = "Corriente",
                        value = powerTelemetry.batteryCurrentMa?.let { "%.0f mA".format(it) } ?: "N/D",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VuMeterRow(
    label: String,
    level: Float,
    theme: CarAudioThemeType
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = theme.textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("${(level.coerceIn(0f, 1f) * 100).toInt()}%", color = theme.textColor, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
        }
        LinearProgressIndicator(
            progress = { level.coerceIn(0f, 1f) },
            color = if (level > 0.9f) theme.clipAlertColor else theme.accentColor,
            trackColor = theme.backgroundColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun TelemetryStatChip(
    label: String,
    value: String,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier,
    highlightAlert: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.backgroundColor)
            .border(1.dp, if (highlightAlert) theme.clipAlertColor else theme.cardColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = theme.textSecondaryColor, fontSize = 9.5.sp)
        Text(
            text = value,
            color = if (highlightAlert) theme.clipAlertColor else theme.textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
