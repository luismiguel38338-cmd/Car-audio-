package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudioEngine
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.SubwooferPreset
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun BassSystemView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    isVipUnlocked: Boolean,
    onToggleBassBoost: (Boolean, Float, Float) -> Unit,
    onSubBassChanged: (Boolean, Float, Float) -> Unit,
    onSubsonicChanged: (Boolean, Float) -> Unit,
    onLpfChanged: (Boolean, Float) -> Unit,
    onBassPunchChanged: (Float) -> Unit,
    onLoudnessChanged: (Boolean, Float) -> Unit,
    onPhaseChanged: (Int) -> Unit,
    onTimeAlignmentChanged: (Float) -> Unit,
    onApplySubPreset: (SubwooferPreset) -> Unit,
    onLpfSlopeChanged: (Int) -> Unit,
    onRequestVipDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dspOn = dspSettings.dspMasterEnabled
    val targetFreqs = remember { listOf(30f, 35f, 45f, 55f, 65f, 80f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("bass_system_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. HEADER & LIVE SUB-BASS RESPONSE GRAPH (20Hz - 250Hz)
        item(key = "bass_header_graph") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.45f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor.copy(alpha = 0.18f))
                                    .border(1.dp, theme.primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speaker,
                                    contentDescription = "Sistema de Bajos",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SISTEMA DE BAJOS & SUBWOOFER",
                                    color = theme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (dspOn) {
                                        "Corte Sub ${dspSettings.subsonicFrequencyHz.toInt()}Hz • LPF ${dspSettings.lpfFrequencyHz.toInt()}Hz"
                                    } else {
                                        "DSP en Bypass — Activa el poder maestro"
                                    },
                                    color = theme.accentColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.cardColor)
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${dspSettings.subBassFreqHz.toInt()} Hz",
                                color = theme.primaryColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    BassCurveCanvas(
                        dsp = dspSettings,
                        eq = eqSettings,
                        theme = theme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("20Hz", "35Hz", "55Hz", "80Hz", "125Hz", "250Hz").forEach { lbl ->
                            Text(
                                text = lbl,
                                color = theme.textSecondaryColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 2. PRESETS DE BAJOS (FREE + VIP)
        item(key = "bass_presets") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PERFILES DE SUBWOOFER",
                        color = theme.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubwooferPreset.entries.forEach { preset ->
                            val locked = preset.isVip && !isVipUnlocked
                            Card(
                                modifier = Modifier
                                    .width(142.dp)
                                    .clickable {
                                        if (locked) onRequestVipDialog() else onApplySubPreset(preset)
                                    }
                                    .testTag("bass_preset_${preset.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (locked) theme.backgroundColor else theme.cardColor
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (preset.isVip) theme.accentColor.copy(alpha = 0.6f) else theme.primaryColor.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.label,
                                            color = theme.textColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        if (preset.isVip) {
                                            Icon(
                                                imageVector = if (locked) Icons.Default.Lock else Icons.Default.WorkspacePremium,
                                                contentDescription = "VIP",
                                                tint = theme.primaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.subtitle,
                                        color = theme.textSecondaryColor,
                                        fontSize = 9.5.sp,
                                        lineHeight = 13.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. MOTOR DE BAJOS Y SUB-BASS EXTREMO
        item(key = "bass_boost_engine") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Waves, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("POTENCIA SUB-BASS & PUNCH", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Switch(
                            checked = dspSettings.subBassEnabled,
                            onCheckedChange = { onSubBassChanged(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("bass_sub_switch")
                        )
                    }

                    DspSliderRow(
                        label = "HARDWARE BASS BOOST",
                        value = dspSettings.bassBoostDb,
                        range = 0f..24f,
                        unit = "dB",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = { onToggleBassBoost(it > 0.5f, it, dspSettings.bassBoostFreqHz) }
                    )

                    DspSliderRow(
                        label = "SUB-BASS PROFUNDO",
                        value = dspSettings.subBassBoostDb,
                        range = 0f..18f,
                        unit = "dB",
                        enabled = dspOn && dspSettings.subBassEnabled,
                        theme = theme,
                        onValueChange = { onSubBassChanged(true, it, dspSettings.subBassFreqHz) }
                    )

                    Text(
                        text = "FRECUENCIA CENTRAL DEL BAJO",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        targetFreqs.forEach { freq ->
                            val selected = kotlin.math.abs(dspSettings.subBassFreqHz - freq) < 3f
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onSubBassChanged(true, dspSettings.subBassBoostDb, freq)
                                    onToggleBassBoost(dspSettings.bassBoostEnabled, dspSettings.bassBoostDb, freq)
                                },
                                label = { Text("${freq.toInt()} Hz", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.cardColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }

                    DspSliderRow(
                        label = "IMPACTO / PUNCH (63-85 Hz)",
                        value = dspSettings.bassPunchLevel,
                        range = 0f..10f,
                        unit = "dB",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = onBassPunchChanged
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Loudness Dinámico (Bajos a Bajo Volumen)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Compensa graves y brillo cuando escuchas a volumen moderado", color = theme.textSecondaryColor, fontSize = 10.sp)
                        }
                        Switch(
                            checked = dspSettings.loudnessEnabled,
                            onCheckedChange = { onLoudnessChanged(it, dspSettings.loudnessGainDb) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            )
                        )
                    }
                    if (dspSettings.loudnessEnabled) {
                        DspSliderRow(
                            label = "NIVEL LOUDNESS",
                            value = dspSettings.loudnessGainDb,
                            range = 0f..15f,
                            unit = "dB",
                            enabled = dspOn,
                            theme = theme,
                            onValueChange = { onLoudnessChanged(true, it) }
                        )
                    }
                }
            }
        }

        // 4. PROTECCIÓN SUBSÓNICA, CROSSOVER LPF & FASE
        item(key = "bass_protection_phase") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PROTECCIÓN, CROSSOVER Y FASE", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Filtro Subsónico Protector (${dspSettings.subsonicFrequencyHz.toInt()} Hz)",
                                color = theme.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Evita que frecuencias ultra-bajas dañen la bobina del subwoofer",
                                color = theme.textSecondaryColor,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = dspSettings.subsonicFilterActive,
                            onCheckedChange = { onSubsonicChanged(it, dspSettings.subsonicFrequencyHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("bass_subsonic_switch")
                        )
                    }

                    Slider(
                        value = dspSettings.subsonicFrequencyHz,
                        onValueChange = { onSubsonicChanged(dspSettings.subsonicFilterActive, it) },
                        valueRange = 15f..50f,
                        enabled = dspOn && dspSettings.subsonicFilterActive,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.cardColor
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Crossover Pasa-Bajas LPF: ${dspSettings.lpfFrequencyHz.toInt()} Hz",
                            color = theme.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = dspSettings.lpfEnabled,
                            onCheckedChange = { onLpfChanged(it, dspSettings.lpfFrequencyHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.accentColor,
                                checkedTrackColor = theme.accentColor.copy(alpha = 0.35f)
                            )
                        )
                    }
                    Slider(
                        value = dspSettings.lpfFrequencyHz.coerceIn(50f, 250f),
                        onValueChange = { onLpfChanged(true, it) },
                        valueRange = 50f..250f,
                        enabled = dspOn && dspSettings.lpfEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentColor,
                            activeTrackColor = theme.accentColor,
                            inactiveTrackColor = theme.cardColor
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    // Phase 0° / 180° + Time Alignment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fase del Subwoofer", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 180).forEach { deg ->
                                val active = dspSettings.phaseDegrees == deg
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) theme.primaryColor else theme.cardColor)
                                        .border(1.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onPhaseChanged(deg) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${deg}°",
                                        color = if (active) theme.onPrimaryColor else theme.textColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    DspSliderRow(
                        label = "ALINEACIÓN TEMPORAL (DELAY)",
                        value = dspSettings.timeAlignmentMs,
                        range = 0f..15f,
                        unit = "ms",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = onTimeAlignmentChanged
                    )
                }
            }
        }

        // 5. SECCIÓN VIP DE BAJOS (PENDIENTE 36/48 dB/oct)
        item(key = "bass_vip_module") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isVipUnlocked) { onRequestVipDialog() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.55f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                contentDescription = "VIP Bajos",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVipUnlocked) "CROSSOVER PRO VIP (ACTIVO)" else "MÓDULO PRO VIP DE BAJOS",
                                color = theme.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (!isVipUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.primaryColor)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("DESBLOQUEAR", color = theme.onPrimaryColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    if (isVipUnlocked) {
                        Text(
                            text = "Pendiente Linkwitz-Riley / Butterworth del Subwoofer:",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(12, 24, 36, 48).forEach { slope ->
                                val selected = dspSettings.lpfSlopeDb == slope
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) theme.primaryColor else theme.surfaceColor)
                                        .border(1.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onLpfSlopeChanged(slope) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$slope dB",
                                        color = if (selected) theme.onPrimaryColor else theme.textColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Toca aquí para ingresar tu código VIP y desbloquear pendientes de 36/48 dB/oct, Hairtrick 32Hz y SPL Competencia.",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BassCurveCanvas(
    dsp: DspSettings,
    eq: EqualizerSettings,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    val points = remember(
        dsp.dspMasterEnabled,
        dsp.bassBoostEnabled,
        dsp.bassBoostDb,
        dsp.subBassEnabled,
        dsp.subBassBoostDb,
        dsp.subBassFreqHz,
        dsp.bassPunchLevel,
        dsp.subsonicFilterActive,
        dsp.subsonicFrequencyHz,
        dsp.lpfEnabled,
        dsp.lpfFrequencyHz,
        dsp.lpfSlopeDb,
        eq
    ) {
        val minLog = log10(20.0)
        val maxLog = log10(250.0)
        List(49) { i ->
            val ratio = i / 48.0
            val freq = 10.0.pow(minLog + (maxLog - minLog) * ratio).roundToInt().coerceIn(20, 250)
            val gain = CarAudioEngine.computeEffectiveBandGainDb(freq, dsp, eq)
            ((1f - (gain / 18f)) / 2f).coerceIn(0.05f, 0.95f)
        }
    }

    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val fillBrush = Brush.verticalGradient(
                listOf(theme.primaryColor.copy(alpha = 0.35f), Color.Transparent)
            )
            val strokeBrush = Brush.horizontalGradient(
                listOf(theme.primaryColor, theme.accentColor)
            )
            val strokePath = Path()
            val fillPath = Path()
            fillPath.moveTo(0f, midY)
            val last = (points.size - 1).coerceAtLeast(1)
            points.forEachIndexed { idx, ny ->
                val x = (idx.toFloat() / last) * w
                val y = (ny * h).coerceIn(4f, h - 4f)
                if (idx == 0) {
                    strokePath.moveTo(x, y)
                } else {
                    strokePath.lineTo(x, y)
                }
                fillPath.lineTo(x, y)
            }
            fillPath.lineTo(w, midY)
            fillPath.close()
            val strokeStyle = Stroke(width = 2.5.dp.toPx())

            onDrawBehind {
                drawLine(
                    color = theme.primaryColor.copy(alpha = 0.3f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.2f
                )
                drawPath(fillPath, fillBrush)
                drawPath(strokePath, strokeBrush, style = strokeStyle)
            }
        }
    )
}
