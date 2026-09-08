package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.RtaBand
import com.example.model.SplRunState

@Composable
fun VisualizerView(
    theme: CarAudioThemeType,
    splDb: Float,
    peakSplDb: Float,
    leftVu: Float,
    rightVu: Float,
    rtaBands: List<RtaBand>,
    waveform: FloatArray,
    isMicRta: Boolean,
    onToggleMic: () -> Unit,
    splRunState: SplRunState = SplRunState(),
    onStartSplRun: () -> Unit = {},
    onStopSplRun: () -> Unit = {},
    onClearSplHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top SPL Sound Pressure Meter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("spl_meter_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.6f), theme.accentColor.copy(alpha = 0.4f))))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "SPL Meter",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRESIÓN SONORA (SPL)",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.1f".format(splDb),
                            color = theme.textColor,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "dB SPL",
                            color = theme.primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "PEAK RECORD: ${"%.1f".format(peakSplDb)} dB",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Mic or Source switch
                Button(
                    onClick = onToggleMic,
                    modifier = Modifier.testTag("toggle_mic_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMicRta) theme.primaryColor else theme.surfaceColor,
                        contentColor = if (isMicRta) theme.onPrimaryColor else theme.primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isMicRta) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Micrófono RTA",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMicRta) "Mic En Vivo" else "Audio Player",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Stereo VU Meter (Left / Right)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vu_meter_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VU METER ESTÉREO (CANAL L / R)",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "-20dB  -10dB  -3dB  0dB  +3dB",
                        color = theme.textSecondaryColor.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                VuBar(channel = "L", level = leftVu, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))
                VuBar(channel = "R", level = rightVu, theme = theme)
            }
        }

        // RTA Real-time 30-Band Spectrum Analyzer Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .testTag("rta_spectrum_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Espectro",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANALIZADOR DE ESPECTRO RTA (20Hz - 20kHz)",
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "30 BANDAS",
                        color = theme.primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas drawing spectrum bars with peak hold dots
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(theme.backgroundColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    RtaSpectrumCanvas(bands = rtaBands, theme = theme)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Frequency labels row (Sub, Bass, Mid, Hi)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUB 20-60Hz", color = theme.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("BASS 60-250Hz", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("MEDIOS 500-2k", color = theme.textSecondaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("VOCES / DRIVER", color = theme.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("AGUDOS 8k-20k", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Real-time Oscilloscope Waveform Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .testTag("oscilloscope_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = "OSCILOSCOPIO DIGITAL (ONDA DE AUDIO)",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // Grid line
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, centerY),
                        end = Offset(w, centerY),
                        strokeWidth = 1f
                    )

                    if (waveform.isNotEmpty()) {
                        val path = Path()
                        val step = w / (waveform.size - 1).coerceAtLeast(1)

                        for (i in waveform.indices) {
                            val x = i * step
                            val y = centerY - (waveform[i] * centerY * 0.85f)
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = theme.primaryColor,
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }

        // SPL Competition Run Card (30-second Bass Race / dB Drag Simulator)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("spl_competition_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (splRunState.isRunning) theme.clipAlertColor else theme.primaryColor.copy(alpha = 0.5f),
                        theme.accentColor.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Competencia SPL",
                            tint = if (splRunState.isRunning) theme.clipAlertColor else theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MODO COMPETENCIA SPL (BASS RACE 30s)",
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (splRunState.isRunning) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.clipAlertColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${splRunState.timeRemainingSeconds}s",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // 3 Score Digits: Live, Average, Peak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("En Vivo:", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${"%.1f".format(splRunState.currentSplDb)} dB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.textColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Promedio (Avg):", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${"%.1f".format(splRunState.averageSplDb)} dB",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.primaryColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pico Récord:", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${"%.1f".format(splRunState.maxPeakDb)} dB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentColor
                        )
                    }
                }

                // Action button: Start or Stop
                Button(
                    onClick = {
                        if (splRunState.isRunning) onStopSplRun() else onStartSplRun()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_spl_run_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (splRunState.isRunning) theme.clipAlertColor else theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (splRunState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (splRunState.isRunning) "DETENER PRUEBA SPL" else "INICIAR RONDA SPL 30s",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                // Run History records
                if (splRunState.runHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de Récords Guardados:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondaryColor
                        )
                        IconButton(
                            onClick = onClearSplHistory,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar historial",
                                tint = theme.textSecondaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        splRunState.runHistory.take(3).forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(theme.surfaceColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.title,
                                    fontSize = 11.sp,
                                    color = theme.textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Avg: ${"%.1f".format(record.avgDb)} dB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.primaryColor
                                    )
                                    Text(
                                        text = "Peak: ${"%.1f".format(record.peakDb)} dB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = theme.accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VuBar(
    channel: String,
    level: Float,
    theme: CarAudioThemeType
) {
    val animLevel by animateFloatAsState(targetValue = level.coerceIn(0f, 1f), label = "vuAnim")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = channel,
            color = theme.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(18.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val activeWidth = totalWidth * animLevel
                val segments = 24
                val segmentWidth = totalWidth / segments

                for (i in 0 until segments) {
                    val segX = i * segmentWidth
                    if (segX + segmentWidth <= activeWidth) {
                        val segRatio = i.toFloat() / segments
                        val segColor = when {
                            segRatio > 0.85f -> theme.clipAlertColor // Red warning clip zone
                            segRatio > 0.65f -> Color(0xFFFFB300) // Amber high level
                            else -> theme.primaryColor // Normal operating level
                        }

                        drawRoundRect(
                            color = segColor,
                            topLeft = Offset(segX + 1.5f, 1.5f),
                            size = Size(segmentWidth - 3f, size.height - 3f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RtaSpectrumCanvas(
    bands: List<RtaBand>,
    theme: CarAudioThemeType
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (bands.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val barCount = bands.size
        val barSpacing = 2.5f
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = (w - totalSpacing) / barCount

        val gradient = Brush.verticalGradient(
            colors = listOf(
                theme.clipAlertColor,
                theme.accentColor,
                theme.primaryColor
            ),
            startY = 0f,
            endY = h
        )

        for (i in bands.indices) {
            val band = bands[i]
            // Map -60dB .. 0dB to 0..h
            val normLevel = ((band.levelDb + 60f) / 60f).coerceIn(0.04f, 1.0f)
            val barHeight = h * normLevel
            val x = i * (barWidth + barSpacing)
            val y = h - barHeight

            // Draw frequency bar
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Draw peak hold dot
            val normPeak = ((band.peakDb + 60f) / 60f).coerceIn(0.04f, 1.0f)
            val peakY = (h - (h * normPeak) - 3f).coerceAtLeast(0f)
            drawRect(
                color = theme.meterPeakColor,
                topLeft = Offset(x, peakY),
                size = Size(barWidth, 3f)
            )
        }
    }
}
