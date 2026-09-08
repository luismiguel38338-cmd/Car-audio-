package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.AudioSourceMode
import com.example.model.CarAudioThemeType
import com.example.model.OscilloscopeState
import com.example.model.RtaBand
import com.example.model.SplCalibrationSettings
import com.example.model.SplRunState
import com.example.model.SplSpeed
import com.example.model.SplWeighting

@Composable
fun VisualizerView(
    theme: CarAudioThemeType,
    splDb: Float,
    peakSplDb: Float,
    rmsLevelDb: Float = -28.0f,
    leftVu: Float,
    rightVu: Float,
    rtaBands: List<RtaBand>,
    waveform: FloatArray,
    rawOscilloscope: FloatArray = FloatArray(128),
    oscilloscopeState: OscilloscopeState = OscilloscopeState(),
    splCalibrationSettings: SplCalibrationSettings = SplCalibrationSettings(),
    isClippingDetected: Boolean = false,
    sourceMode: AudioSourceMode = AudioSourceMode.INTERNAL_DSP,
    isMicRta: Boolean,
    onToggleMic: () -> Unit,
    onSetTimebase: (Float) -> Unit = {},
    onToggleOscilloscopeFreeze: () -> Unit = {},
    onUpdateSplCalibrationOffset: (Float) -> Unit = {},
    onSetSplWeighting: (SplWeighting) -> Unit = {},
    onSetSplSpeed: (SplSpeed) -> Unit = {},
    splRunState: SplRunState = SplRunState(),
    onStartSplRun: () -> Unit = {},
    onStopSplRun: () -> Unit = {},
    onClearSplHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showCalibrationPanel by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "clip_flash")
    val clipAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clip_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // MODE & CLIPPING MONITOR BAR
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isClippingDetected) Color(0xFFFF1744) else theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distinction: REAL MODE vs INTERNAL DSP vs DEMO
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (sourceMode) {
                                    AudioSourceMode.REAL_MIC -> Color(0xFF00E676).copy(alpha = 0.18f)
                                    AudioSourceMode.INTERNAL_DSP -> theme.primaryColor.copy(alpha = 0.18f)
                                    else -> Color(0xFFFFB300).copy(alpha = 0.18f)
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
                            text = when (sourceMode) {
                                AudioSourceMode.REAL_MIC -> "MODO REAL: MIC EN VIVO"
                                AudioSourceMode.INTERNAL_DSP -> "MODO DSP: GENERADOR NATIVO"
                                else -> "MODO DEMO OFFLINE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (sourceMode) {
                                AudioSourceMode.REAL_MIC -> Color(0xFF00E676)
                                AudioSourceMode.INTERNAL_DSP -> theme.primaryColor
                                else -> Color(0xFFFFB300)
                            }
                        )
                    }

                    // Clipping LED Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isClippingDetected) Color(0xFFFF1744).copy(alpha = clipAlpha)
                                    else Color(0xFF333333)
                                )
                                .border(1.dp, if (isClippingDetected) Color(0xFFFF1744) else Color(0xFF555555), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isClippingDetected) "CLIPPING ACTIVO" else "SIN CLIPPING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClippingDetected) Color(0xFFFF1744) else theme.textSecondaryColor
                        )
                    }
                }

                // Mic on/off switch & RMS level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onToggleMic,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMicRta) Color(0xFF00E676) else Color(0xFF22222E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isMicRta) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Micrófono RTA",
                                tint = if (isMicRta) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMicRta) "MIC RTA ON" else "ACTIVAR MIC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMicRta) Color.Black else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "RMS: %.1f dBFS".format(rmsLevelDb),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = theme.primaryColor
                        )
                    }

                    OutlinedButton(
                        onClick = { showCalibrationPanel = !showCalibrationPanel },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showCalibrationPanel) "Ocultar Calibración" else "Calibrar SPL", fontSize = 10.sp)
                    }
                }

                // SPL Calibration Accordion Panel
                AnimatedVisibility(visible = showCalibrationPanel) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF101018))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CALIBRACIÓN DE MICRÓFONO & PONDERACIÓN SPL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Offset Calibración (dBC):", fontSize = 10.sp, color = theme.textSecondaryColor)
                            Text("%+.1f dB".format(splCalibrationSettings.micOffsetDb), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                        }

                        Slider(
                            value = splCalibrationSettings.micOffsetDb,
                            onValueChange = onUpdateSplCalibrationOffset,
                            valueRange = -20f..20f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                        )

                        // Weighting Chips (A, C, Z)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Ponderación:", fontSize = 10.sp, color = theme.textSecondaryColor)
                            SplWeighting.entries.forEach { w ->
                                FilterChip(
                                    selected = splCalibrationSettings.weighting == w,
                                    onClick = { onSetSplWeighting(w) },
                                    label = { Text(w.label, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.primaryColor,
                                        selectedLabelColor = theme.onPrimaryColor
                                    )
                                )
                            }
                        }

                        // Speed Chips (FAST / SLOW)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Respuesta:", fontSize = 10.sp, color = theme.textSecondaryColor)
                            SplSpeed.entries.forEach { s ->
                                FilterChip(
                                    selected = splCalibrationSettings.speed == s,
                                    onClick = { onSetSplSpeed(s) },
                                    label = { Text(s.label, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.primaryColor,
                                        selectedLabelColor = theme.onPrimaryColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // DUAL SPL & VU METER GAUGE
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
                    Column {
                        Text(
                            text = "PRESIÓN SONORA (SPL METRIC)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondaryColor
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(splDb),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (splDb > 135f) Color(0xFFFF1744) else theme.primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "dB ${splCalibrationSettings.weighting.name} (${splCalibrationSettings.speed.name})",
                                fontSize = 12.sp,
                                color = theme.textSecondaryColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "PEAK HOLD", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "%.1f dB".format(peakSplDb),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentColor
                        )
                    }
                }

                // Dual VU Meters (Left / Right)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("L", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor, modifier = Modifier.width(16.dp))
                        VuMeterBar(level = leftVu, theme = theme, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("R", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor, modifier = Modifier.width(16.dp))
                        VuMeterBar(level = rightVu, theme = theme, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // REAL-TIME RTA (31-BAND ISO SPECTRUM WITH PEAK HOLD & RMS)
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
                        text = "ANALIZADOR RTA EN TIEMPO REAL (31 BANDAS ISO)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )
                    Text(
                        text = "Peak Hold & RMS",
                        fontSize = 10.sp,
                        color = theme.accentColor
                    )
                }

                // 31-Band RTA Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090A12))
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    RtaSpectrumCanvas31(bands = rtaBands, theme = theme)
                }

                // Octave Range Annotations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUB (20-63Hz)", color = theme.primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("BASS (80-250Hz)", color = theme.accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("MEDIOS (315-2kHz)", color = theme.textSecondaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("VOCES / HORN (2.5k-6.3k)", color = theme.primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("AGUDOS (8k-20kHz)", color = theme.accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // PROFESSIONAL DIGITAL OSCILLOSCOPE
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
                        text = "OSCILOSCOPIO DIGITAL (DISPARO POR FLANCO)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Freeze / Run button
                        Button(
                            onClick = onToggleOscilloscopeFreeze,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (oscilloscopeState.isFrozen) Color(0xFFFFEA00) else Color(0xFF222230)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(width = 65.dp, height = 28.dp)
                        ) {
                            Text(
                                text = if (oscilloscopeState.isFrozen) "HOLD" else "RUN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (oscilloscopeState.isFrozen) Color.Black else Color.White
                            )
                        }
                    }
                }

                // Timebase selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Base de Tiempo:", fontSize = 10.sp, color = theme.textSecondaryColor)
                    val timebases = listOf(0.5f, 1.0f, 2.0f, 5.0f, 10.0f)
                    timebases.forEach { tb ->
                        FilterChip(
                            selected = oscilloscopeState.timebaseMs == tb,
                            onClick = { onSetTimebase(tb) },
                            label = { Text("${tb}ms/div", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryColor,
                                selectedLabelColor = theme.onPrimaryColor
                            )
                        )
                    }
                }

                // Oscilloscope Trace Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06070B))
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // Oscilloscope Grid Lines (horizontal and vertical)
                    val numHorizLines = 4
                    for (i in 1 until numHorizLines) {
                        val y = (h / numHorizLines) * i
                        drawLine(Color(0xFF161A28), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    }
                    val numVertLines = 8
                    for (j in 1 until numVertLines) {
                        val x = (w / numVertLines) * j
                        drawLine(Color(0xFF161A28), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    }

                    // Center Line
                    drawLine(Color(0xFF283048), Offset(0f, centerY), Offset(w, centerY), strokeWidth = 1.5f)

                    // Clipping threshold lines (+/- 0dBFS)
                    drawLine(Color(0xFFFF1744).copy(alpha = 0.5f), Offset(0f, 6f), Offset(w, 6f), strokeWidth = 1f)
                    drawLine(Color(0xFFFF1744).copy(alpha = 0.5f), Offset(0f, h - 6f), Offset(w, h - 6f), strokeWidth = 1f)

                    val pcmData = if (rawOscilloscope.isNotEmpty()) rawOscilloscope else waveform
                    if (pcmData.isNotEmpty()) {
                        val path = Path()
                        val step = w / (pcmData.size - 1).coerceAtLeast(1)

                        pcmData.forEachIndexed { idx, s ->
                            val x = idx * step
                            val y = centerY - (s * centerY * 0.92f)
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = if (isClippingDetected) Color(0xFFFF1744) else theme.primaryColor,
                            style = Stroke(width = 2.2.dp.toPx())
                        )
                    }
                }
            }
        }

        // SPL COMPETITION BASS RACE (30s)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COMPETENCIA SPL (BASS RACE 30s)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textPrimaryColor)
                    }

                    if (splRunState.history.isNotEmpty()) {
                        IconButton(onClick = onClearSplHistory, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar Historial", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Tiempo: ${splRunState.elapsedSeconds}s / 30s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                        Text(text = "Media Actual: %.1f dB • Pico: %.1f dB".format(splRunState.averageDb, splRunState.peakDb), fontSize = 11.sp, color = theme.textSecondaryColor)
                    }

                    Button(
                        onClick = if (splRunState.isRunning) onStopSplRun else onStartSplRun,
                        colors = ButtonDefaults.buttonColors(containerColor = if (splRunState.isRunning) Color(0xFFFF1744) else theme.primaryColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(if (splRunState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (splRunState.isRunning) "DETENER" else "START RUN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VuMeterBar(level: Float, theme: CarAudioThemeType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF151722))
    ) {
        val fraction = level.coerceIn(0f, 1f)
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

@Composable
fun RtaSpectrumCanvas31(bands: List<RtaBand>, theme: CarAudioThemeType, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val bandCount = bands.size.coerceAtLeast(1)
        val barSpacing = 2.dp.toPx()
        val totalSpacing = barSpacing * (bandCount - 1)
        val barWidth = ((size.width - totalSpacing) / bandCount).coerceAtLeast(2f)

        bands.forEachIndexed { i, band ->
            val normLevel = ((band.levelDb + 65f) / 65f).coerceIn(0.04f, 1f)
            val barHeight = size.height * normLevel
            val x = i * (barWidth + barSpacing)
            val y = size.height - barHeight

            // Bar fill
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        if (band.levelDb > -5f) Color(0xFFFF1744) else theme.primaryColor,
                        theme.primaryColor.copy(alpha = 0.4f)
                    ),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Peak hold dot
            val normPeak = ((band.peakDb + 65f) / 65f).coerceIn(0.04f, 1f)
            val peakY = size.height - (size.height * normPeak)
            drawLine(
                color = theme.accentColor,
                start = Offset(x, peakY),
                end = Offset(x + barWidth, peakY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
