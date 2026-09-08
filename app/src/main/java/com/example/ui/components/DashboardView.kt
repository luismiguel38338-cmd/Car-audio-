package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.model.AmpTelemetry
import com.example.model.AudioSourceMode
import com.example.model.CarAudioThemeType
import com.example.model.DspChannel
import com.example.model.DspSettings
import com.example.model.HardwareBridgeState
import com.example.model.HardwareConnectionType
import com.example.model.RtaBand
import com.example.viewmodel.AppTab

@Composable
fun DashboardView(
    theme: CarAudioThemeType,
    dsp: DspSettings,
    channels: List<DspChannel>,
    telemetry: AmpTelemetry,
    splDb: Float,
    peakSplDb: Float,
    rmsLevelDb: Float,
    isClipping: Boolean,
    sourceMode: AudioSourceMode,
    hardwareState: HardwareBridgeState,
    rtaBands: List<RtaBand>,
    rawOscilloscope: FloatArray,
    onNavigateTab: (AppTab) -> Unit,
    onPanicMute: () -> Unit,
    onToggleChannelMute: (Int) -> Unit,
    onToggleChannelSolo: (Int) -> Unit,
    onQuickPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Blinking animation for clipping warning LED
    val infiniteTransition = rememberInfiniteTransition(label = "clip_blink")
    val clipPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(220),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP SYSTEM INTEGRITY & MODE STRIP
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isClipping) Color(0xFFFF2A2A) else theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source Mode Badge (Differentiates Real Mic vs Internal DSP vs Demo)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (sourceMode) {
                                    AudioSourceMode.REAL_MIC -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    AudioSourceMode.INTERNAL_DSP -> theme.primaryColor.copy(alpha = 0.2f)
                                    else -> Color(0xFFFFB300).copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (sourceMode) {
                                        AudioSourceMode.REAL_MIC -> Color(0xFF00E676)
                                        AudioSourceMode.INTERNAL_DSP -> theme.primaryColor
                                        else -> Color(0xFFFFB300)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sourceMode.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (sourceMode) {
                                AudioSourceMode.REAL_MIC -> Color(0xFF00E676)
                                AudioSourceMode.INTERNAL_DSP -> theme.primaryColor
                                else -> Color(0xFFFFB300)
                            }
                        )
                    }

                    // Master Clipping LED Warning Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isClipping) Color(0xFFFF1744).copy(alpha = clipPulseAlpha)
                                    else Color(0xFF333333)
                                )
                                .border(1.dp, if (isClipping) Color(0xFFFF1744) else Color(0xFF555555), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isClipping) "CLIP DETECTADO" else "CLIP HEADROOM OK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isClipping) Color(0xFFFF1744) else theme.textSecondaryColor
                        )
                    }
                }

                // Title and Panic Mute Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAR AUDIO DSP PRO 2.0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "Preset activo: ${dsp.activePresetName}",
                            fontSize = 12.sp,
                            color = theme.textSecondaryColor
                        )
                    }

                    // Panic Mute Button
                    val isAnyMuted = channels.any { it.isMuted }
                    Button(
                        onClick = onPanicMute,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAnyMuted) Color(0xFFFF3D00) else Color(0xFF222222)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("panic_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isAnyMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute de Pánico",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAnyMuted) "MUTED" else "PANIC MUTE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Hardware Link Status strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF101015))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (hardwareState.connectionType) {
                                HardwareConnectionType.USB_OTG -> Icons.Default.Usb
                                HardwareConnectionType.BLUETOOTH_SPP -> Icons.Default.Bluetooth
                                else -> Icons.Default.AltRoute
                            },
                            contentDescription = "Hardware",
                            tint = if (hardwareState.isConnected) theme.primaryColor else theme.textSecondaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${hardwareState.connectionType.label}: ${hardwareState.deviceName}",
                            fontSize = 10.sp,
                            color = theme.textPrimaryColor
                        )
                    }
                    Text(
                        text = if (hardwareState.connectionType.isRealHardware) "TX:${hardwareState.packetsSent} RX:${hardwareState.packetsReceived}" else "32-bit Float AudioTrack",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.textSecondaryColor
                    )
                }
            }
        }

        // TELEMETRY METERS: SPL GAUGE, VOLTAGE & POWER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live SPL Gauge Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PRESIÓN SONORA (SPL)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.1f".format(splDb),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (splDb > 130f) Color(0xFFFF1744) else theme.primaryColor
                    )
                    Text(
                        text = "dB SPL • Peak: %.1f dB".format(peakSplDb),
                        fontSize = 10.sp,
                        color = theme.textSecondaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Linear SPL Gauge Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E1E28))
                    ) {
                        val fraction = ((splDb - 40f) / 105f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00E676), Color(0xFFFFEA00), Color(0xFFFF1744))
                                    )
                                )
                        )
                    }
                }
            }

            // Power & Alternator Telemetry Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "VOLTAJE", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "%.1fV".format(telemetry.voltage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isLowVoltage) Color(0xFFFF1744) else theme.primaryColor
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "POTENCIA", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${telemetry.outputPowerWatts}W",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimaryColor
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TEMPERATURA", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${telemetry.temperatureC.toInt()}°C",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isOverheated) Color(0xFFFF1744) else theme.textPrimaryColor
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "RMS dBFS", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "%.1f dB".format(rmsLevelDb),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }

        // 4-WAY MULTICHANNEL DSP STRIP (GAIN, CROSSOVER, LIMITER & MUTE)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CANALES DSP 4-VÍAS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )
                    Text(
                        text = "IR AL PROCESADOR →",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        modifier = Modifier.clickable { onNavigateTab(AppTab.DSP_PROCESSOR) }
                    )
                }

                HorizontalDivider(color = theme.primaryColor.copy(alpha = 0.2f))

                channels.forEach { ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ch.isMuted) Color(0xFF1B1B22) else Color(0xFF14141B))
                            .border(
                                1.dp,
                                if (ch.isSolo) Color(0xFFFFEA00) else if (ch.isClipping) Color(0xFFFF1744) else Color(0xFF222230),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Channel Name and Crossover points
                        Column(modifier = Modifier.weight(1.5f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CH ${ch.id}: ${ch.name.substringAfter(": ")}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ch.isMuted) theme.textSecondaryColor else theme.textPrimaryColor
                                )
                                if (ch.isSolo) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SOLO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFEA00),
                                        modifier = Modifier
                                            .background(Color(0xFFFFEA00).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "HPF: ${ch.hpfHz.toInt()}Hz (${ch.hpfSlopeDb}dB) | LPF: ${ch.lpfHz.toInt()}Hz (${ch.lpfSlopeDb}dB) | Del: ${"%.1f".format(ch.delayMs)}ms",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = theme.textSecondaryColor
                            )
                        }

                        // Limiter & Clip Indicators
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Limiter Reduction Tag
                            if (ch.gainReductionDb > 0.1f) {
                                Text(
                                    text = "-%.1fdB".format(ch.gainReductionDb),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFFEA00),
                                    modifier = Modifier
                                        .background(Color(0xFFFFEA00).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            // Solo Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (ch.isSolo) Color(0xFFFFEA00) else Color(0xFF282835))
                                    .clickable { onToggleChannelSolo(ch.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "S",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ch.isSolo) Color.Black else Color.White
                                )
                            }

                            // Mute Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (ch.isMuted) Color(0xFFFF1744) else Color(0xFF282835))
                                    .clickable { onToggleChannelMute(ch.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "M",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // REAL-TIME RTA & OSCILLOSCOPE MINI OVERVIEW
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESPECTRO RTA (31 BANDAS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )
                    Text(
                        text = "VER COMPLETO →",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        modifier = Modifier.clickable { onNavigateTab(AppTab.RTA_ANALYZER) }
                    )
                }

                // Mini RTA 31-Band Visualizer Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0C12))
                ) {
                    val bandCount = rtaBands.size.coerceAtLeast(1)
                    val barSpacing = 2.dp.toPx()
                    val totalSpacing = barSpacing * (bandCount - 1)
                    val barWidth = ((size.width - totalSpacing) / bandCount).coerceAtLeast(2f)

                    rtaBands.forEachIndexed { i, band ->
                        val normLevel = ((band.levelDb + 60f) / 60f).coerceIn(0.04f, 1f)
                        val barHeight = size.height * normLevel
                        val x = i * (barWidth + barSpacing)
                        val y = size.height - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(theme.primaryColor, theme.primaryColor.copy(alpha = 0.4f)),
                                startY = y,
                                endY = size.height
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                // Mini Oscilloscope Trace
                Text(
                    text = "TRAZO DE ONDA / OSCILOSCOPIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0C12))
                ) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // Grid line
                    drawLine(
                        color = Color(0xFF222230),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (rawOscilloscope.isNotEmpty()) {
                        val path = Path()
                        val step = w / (rawOscilloscope.size - 1).coerceAtLeast(1)
                        rawOscilloscope.forEachIndexed { idx, sample ->
                            val x = idx * step
                            val y = midY - (sample * midY * 0.9f)
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = if (isClipping) Color(0xFFFF1744) else theme.primaryColor,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }
            }
        }

        // QUICK PRESETS & TOOL SHORTCUTS
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PRESETS RÁPIDOS & HERRAMIENTAS PRO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimaryColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf("Open Show Pro", "SPL Bass Monster", "SQL Audiophile Studio", "Reggaeton & Urbano")
                    presets.take(2).forEach { p ->
                        Button(
                            onClick = { onQuickPreset(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dsp.activePresetName == p) theme.primaryColor else Color(0xFF22222D)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = p,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dsp.activePresetName == p) theme.onPrimaryColor else theme.textPrimaryColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateTab(AppTab.EQUALIZER) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "EQ 31 & Auto-Tune", fontSize = 10.sp, color = theme.textPrimaryColor)
                    }

                    Button(
                        onClick = { onNavigateTab(AppTab.TOOLS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Diseñador Cajones", fontSize = 10.sp, color = theme.textPrimaryColor)
                    }
                }
            }
        }
    }
}
