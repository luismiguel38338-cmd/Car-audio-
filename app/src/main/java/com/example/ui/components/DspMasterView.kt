package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.RtaBand
import kotlin.math.roundToInt

@Composable
fun DspMasterView(
    dspSettings: DspSettings,
    rtaBands: List<RtaBand>,
    theme: CarAudioThemeType,
    leftVu: Float,
    rightVu: Float,
    isClipping: Boolean,
    onToggleDspMaster: () -> Unit,
    onPreampChanged: (Float) -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onBassBoostChanged: (Boolean, Float) -> Unit,
    onStereoWidthChanged: (Float) -> Unit,
    onBalancePanChanged: (Float) -> Unit,
    onCompressorToggle: (Boolean) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    onLimiterCeilingChanged: (Float) -> Unit,
    onHpfChanged: (Boolean, Float) -> Unit,
    onLpfChanged: (Boolean, Float) -> Unit,
    liteVisuals: Boolean = false,
    isVipUnlocked: Boolean = false,
    onRequestVipDialog: () -> Unit = {},
    onHiResChanged: (Boolean) -> Unit = {},
    onTubeWarmthChanged: (Float) -> Unit = {},
    onPresenceChanged: (Boolean, Float) -> Unit = { _, _ -> },
    onClarityChanged: (Boolean, Float) -> Unit = { _, _ -> },
    onExciterChanged: (Boolean, Float) -> Unit = { _, _ -> },
    onCompressorParamsChanged: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> },
    onCrossoverSlopesChanged: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val dspEnabled = dspSettings.dspMasterEnabled
    val cardBg = theme.surfaceColor
    val borderCol = if (dspEnabled) theme.primaryColor.copy(alpha = 0.45f) else theme.cardColor

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. MASTER DSP POWER & HARDWARE SPECTRUM
        item(key = "dsp_master_header", contentType = "header") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dsp_master_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.5.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (dspEnabled) theme.primaryColor else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PROCESADOR DSP STUDIO",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = theme.textColor,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (dspEnabled) "AUDIOFX ACTIVO EN TIEMPO REAL" else "BYPASS ACTIVO (AUDIO ORIGINAL)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dspEnabled) theme.accentColor else theme.textSecondaryColor
                            )
                        }

                        Button(
                            onClick = onToggleDspMaster,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dspEnabled) theme.primaryColor else theme.cardColor,
                                contentColor = if (dspEnabled) theme.onPrimaryColor else theme.textColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dsp_master_power_toggle")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (dspEnabled) "ON" else "OFF", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    // Live L/R VU & Clip LED
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("L", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondaryColor)
                                Text("${(leftVu * 100).toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textColor)
                            }
                            LinearProgressIndicator(
                                progress = { leftVu.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = theme.primaryColor,
                                trackColor = theme.cardColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("R", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondaryColor)
                                Text("${(rightVu * 100).toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textColor)
                            }
                            LinearProgressIndicator(
                                progress = { rightVu.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = theme.accentColor,
                                trackColor = theme.cardColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isClipping) theme.primaryColor else theme.cardColor)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "CLIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isClipping) Color.White else theme.textSecondaryColor
                            )
                        }
                    }

                    // Zero-Allocation Cached Real-Time Spectrum Visualizer
                    if (rtaBands.isNotEmpty()) {
                        val step = if (liteVisuals) 2 else 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (liteVisuals) 56.dp else 72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.backgroundColor)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .drawWithCache {
                                    val barBrush = Brush.verticalGradient(
                                        listOf(theme.primaryColor, theme.accentColor.copy(alpha = 0.4f))
                                    )
                                    val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    val spacing = 2.dp.toPx()
                                    onDrawBehind {
                                        val barCount = ((rtaBands.size + step - 1) / step).coerceAtLeast(1)
                                        val barW = ((size.width - spacing * (barCount - 1)) / barCount).coerceAtLeast(2f)
                                        var drawIdx = 0
                                        var i = 0
                                        while (i < rtaBands.size) {
                                            val norm = ((rtaBands[i].levelDb + 60f) / 60f).coerceIn(0.05f, 1f)
                                            val bh = size.height * norm
                                            val x = drawIdx * (barW + spacing)
                                            drawRoundRect(
                                                brush = barBrush,
                                                topLeft = Offset(x, size.height - bh),
                                                size = Size(barW, bh),
                                                cornerRadius = corner
                                            )
                                            drawIdx++
                                            i += step
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }

        // 2. POTENCIA & TONALIDAD EN TIEMPO REAL
        item(key = "dsp_tonal_stage", contentType = "card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TONALIDAD Y GANANCIA EN VIVO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.textColor
                        )
                    }

                    DspSliderRow("PREAMP GAIN", dspSettings.preampDb, -18f..18f, "dB", dspEnabled, theme, onPreampChanged)
                    DspSliderRow("BASS (GRAVES)", dspSettings.bassGainDb, -18f..18f, "dB", dspEnabled, theme, onBassGainChanged)
                    DspSliderRow("MID (VOCES)", dspSettings.midGainDb, -18f..18f, "dB", dspEnabled, theme, onMidGainChanged)
                    DspSliderRow("TREBLE (AGUDOS)", dspSettings.trebleGainDb, -18f..18f, "dB", dspEnabled, theme, onTrebleGainChanged)
                    DspSliderRow("MASTER OUTPUT", dspSettings.masterGainDb, -24f..18f, "dB", true, theme, onMasterGainChanged)
                }
            }
        }

        // 3. BASS BOOST, ESPACIALIZADOR Y BALANCE L-R
        item(key = "dsp_bass_stereo", contentType = "card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BASS BOOST & ESTÉREO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                        }
                        Switch(
                            checked = dspSettings.bassBoostEnabled,
                            onCheckedChange = { onBassBoostChanged(it, dspSettings.bassBoostDb) },
                            enabled = dspEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            )
                        )
                    }

                    DspSliderRow(
                        label = "HARDWARE BASS BOOST",
                        value = dspSettings.bassBoostDb,
                        range = 0f..24f,
                        unit = "dB",
                        enabled = dspEnabled && dspSettings.bassBoostEnabled,
                        theme = theme,
                        onValueChange = { onBassBoostChanged(true, it) }
                    )

                    DspSliderRow(
                        label = "AMPLITUD ESTÉREO",
                        value = dspSettings.stereoWidthPercent,
                        range = 100f..250f,
                        unit = "%",
                        enabled = dspEnabled,
                        theme = theme,
                        onValueChange = onStereoWidthChanged
                    )

                    DspSliderRow(
                        label = "BALANCE ESTÉREO (L / R)",
                        value = dspSettings.balancePan,
                        range = -1.0f..1.0f,
                        unit = "Pan",
                        enabled = true,
                        theme = theme,
                        onValueChange = onBalancePanChanged
                    )
                }
            }
        }

        // 4. LIMITADOR ANTI-CLIP & COMPRESOR HARDWARE
        item(key = "dsp_dynamics", contentType = "card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DINÁMICA Y PROTECCIÓN (LIMITADOR)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("COMPRESOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                                Switch(
                                    checked = dspSettings.compressorEnabled,
                                    onCheckedChange = onCompressorToggle,
                                    enabled = dspEnabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = theme.primaryColor,
                                        checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                            Text("Control multibanda sin cortes", fontSize = 9.sp, color = theme.textSecondaryColor)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("LIMITADOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                                Switch(
                                    checked = dspSettings.limiterEnabled,
                                    onCheckedChange = onLimiterToggle,
                                    enabled = dspEnabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = theme.primaryColor,
                                        checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                            Text("Techo: ${dspSettings.limiterCeilingDb.roundToInt()} dBFS", fontSize = 9.sp, color = theme.textSecondaryColor)
                        }
                    }

                    DspSliderRow(
                        label = "TECHO ANTI-CLIPPING (LIMITER)",
                        value = dspSettings.limiterCeilingDb,
                        range = -18f..0f,
                        unit = "dB",
                        enabled = dspEnabled && dspSettings.limiterEnabled,
                        theme = theme,
                        onValueChange = onLimiterCeilingChanged
                    )

                    if (dspSettings.compressorEnabled) {
                        if (isVipUnlocked) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.cardColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("COMPRESOR AVANZADO VIP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = theme.primaryColor)
                                }
                                DspSliderRow(
                                    label = "UMBRAL (THRESHOLD)",
                                    value = dspSettings.compressorThresholdDb,
                                    range = -48f..0f,
                                    unit = "dB",
                                    enabled = dspEnabled,
                                    theme = theme,
                                    onValueChange = {
                                        onCompressorParamsChanged(it, dspSettings.compressorRatio, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs)
                                    }
                                )
                                DspSliderRow(
                                    label = "RATIO",
                                    value = dspSettings.compressorRatio,
                                    range = 1f..20f,
                                    unit = ":1",
                                    enabled = dspEnabled,
                                    theme = theme,
                                    onValueChange = {
                                        onCompressorParamsChanged(dspSettings.compressorThresholdDb, it, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs)
                                    }
                                )
                                DspSliderRow(
                                    label = "ATAQUE (ATTACK)",
                                    value = dspSettings.compressorAttackMs,
                                    range = 1f..100f,
                                    unit = "ms",
                                    enabled = dspEnabled,
                                    theme = theme,
                                    onValueChange = {
                                        onCompressorParamsChanged(dspSettings.compressorThresholdDb, dspSettings.compressorRatio, it, dspSettings.compressorReleaseMs)
                                    }
                                )
                                DspSliderRow(
                                    label = "RELAJACIÓN (RELEASE)",
                                    value = dspSettings.compressorReleaseMs,
                                    range = 15f..1000f,
                                    unit = "ms",
                                    enabled = dspEnabled,
                                    theme = theme,
                                    onValueChange = {
                                        onCompressorParamsChanged(dspSettings.compressorThresholdDb, dspSettings.compressorRatio, dspSettings.compressorAttackMs, it)
                                    }
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .clickable { onRequestVipDialog() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ajustes de Umbral, Ratio, Attack y Release (VIP)",
                                        fontSize = 10.5.sp,
                                        color = theme.textSecondaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text("VIP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = theme.primaryColor)
                            }
                        }
                    }
                }
            }
        }

        // 5. FILTROS DE CORTE HPF & LPF
        item(key = "dsp_crossovers", contentType = "card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FILTROS DE CORTE BUTTERWORTH (HPF / LPF)", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HPF (Pasa Altas): ${dspSettings.hpfFrequencyHz.toInt()} Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Switch(
                            checked = dspSettings.hpfEnabled,
                            onCheckedChange = { onHpfChanged(it, dspSettings.hpfFrequencyHz) },
                            enabled = dspEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            )
                        )
                    }
                    Slider(
                        value = dspSettings.hpfFrequencyHz,
                        onValueChange = { onHpfChanged(dspSettings.hpfEnabled, it) },
                        valueRange = 20f..400f,
                        enabled = dspEnabled && dspSettings.hpfEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.cardColor
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    Text("Pendiente HPF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(12, 24, 36, 48).forEach { slope ->
                            val isVipSlope = slope >= 36
                            val locked = isVipSlope && !isVipUnlocked
                            val selected = dspSettings.hpfSlopeDb == slope
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) theme.primaryColor else theme.cardColor)
                                    .border(
                                        1.dp,
                                        if (selected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.35f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = dspEnabled) {
                                        if (locked) onRequestVipDialog() else onCrossoverSlopesChanged(slope, dspSettings.lpfSlopeDb)
                                    }
                                    .padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (locked) {
                                    Icon(Icons.Default.Lock, contentDescription = "VIP", tint = theme.primaryColor, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = "$slope dB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) theme.onPrimaryColor else theme.textColor
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LPF (Pasa Bajas): ${dspSettings.lpfFrequencyHz.toInt()} Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Switch(
                            checked = dspSettings.lpfEnabled,
                            onCheckedChange = { onLpfChanged(it, dspSettings.lpfFrequencyHz) },
                            enabled = dspEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.primaryColor,
                                checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                            )
                        )
                    }
                    Slider(
                        value = dspSettings.lpfFrequencyHz,
                        onValueChange = { onLpfChanged(dspSettings.lpfEnabled, it) },
                        valueRange = 80f..20000f,
                        enabled = dspEnabled && dspSettings.lpfEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentColor,
                            activeTrackColor = theme.accentColor,
                            inactiveTrackColor = theme.cardColor
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    Text("Pendiente LPF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(12, 24, 36, 48).forEach { slope ->
                            val isVipSlope = slope >= 36
                            val locked = isVipSlope && !isVipUnlocked
                            val selected = dspSettings.lpfSlopeDb == slope
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) theme.accentColor else theme.cardColor)
                                    .border(
                                        1.dp,
                                        if (selected) theme.accentColor else theme.primaryColor.copy(alpha = 0.35f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = dspEnabled) {
                                        if (locked) onRequestVipDialog() else onCrossoverSlopesChanged(dspSettings.hpfSlopeDb, slope)
                                    }
                                    .padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (locked) {
                                    Icon(Icons.Default.Lock, contentDescription = "VIP", tint = theme.primaryColor, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = "$slope dB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) theme.onPrimaryColor else theme.textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. VIP MOTOR DE CALIDAD Y FIDELIDAD HD
        item(key = "dsp_vip_quality_engine", contentType = "card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isVipUnlocked) { onRequestVipDialog() }
                    .testTag("dsp_vip_quality_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isVipUnlocked) cardBg else theme.cardColor),
                border = BorderStroke(1.3.dp, theme.primaryColor.copy(alpha = if (isVipUnlocked) 0.65f else 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                contentDescription = "VIP Fidelidad HD",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIP MOTOR DE CALIDAD Y FIDELIDAD HD",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.textColor
                            )
                        }
                        if (!isVipUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.primaryColor)
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text("DESBLOQUEAR", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = theme.onPrimaryColor)
                            }
                        }
                    }

                    if (!isVipUnlocked) {
                        Text(
                            text = "Activa tu clave VIP para desbloquear Modo Hi-Res Studio, Calidez de Bulbos Analógicos, Presencia Vocal, Claridad HD y Excitador Armónico.",
                            fontSize = 11.sp,
                            color = theme.textSecondaryColor,
                            lineHeight = 15.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Modo Hi-Res Studio (Curva Audiófila)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                                Text("Realce equilibrado de subgraves + micro-detalle en agudos", fontSize = 10.sp, color = theme.textSecondaryColor)
                            }
                            Switch(
                                checked = dspSettings.hiResQualityEnabled,
                                onCheckedChange = onHiResChanged,
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("vip_hires_switch")
                            )
                        }

                        DspSliderRow(
                            label = "CALIDEZ ANALÓGICA (TUBE WARMTH)",
                            value = dspSettings.tubeWarmthLevel,
                            range = 0f..10f,
                            unit = "x",
                            enabled = dspEnabled,
                            theme = theme,
                            onValueChange = onTubeWarmthChanged
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Presencia Vocal (2-5 kHz)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Switch(
                                checked = dspSettings.presenceEnabled,
                                onCheckedChange = { onPresenceChanged(it, dspSettings.presenceLevel) },
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                )
                            )
                        }
                        if (dspSettings.presenceEnabled) {
                            DspSliderRow(
                                label = "NIVEL PRESENCIA VOCAL",
                                value = dspSettings.presenceLevel,
                                range = 0f..10f,
                                unit = "dB",
                                enabled = dspEnabled,
                                theme = theme,
                                onValueChange = { onPresenceChanged(true, it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Claridad HD (Aire y Brillo 8-16 kHz)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Switch(
                                checked = dspSettings.clarityEnabled,
                                onCheckedChange = { onClarityChanged(it, dspSettings.clarityLevel) },
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                )
                            )
                        }
                        if (dspSettings.clarityEnabled) {
                            DspSliderRow(
                                label = "NIVEL CLARIDAD HD",
                                value = dspSettings.clarityLevel,
                                range = 0f..10f,
                                unit = "dB",
                                enabled = dspEnabled,
                                theme = theme,
                                onValueChange = { onClarityChanged(true, it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Excitador Armónico", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Switch(
                                checked = dspSettings.exciterEnabled,
                                onCheckedChange = { onExciterChanged(it, dspSettings.exciterLevel) },
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                )
                            )
                        }
                        if (dspSettings.exciterEnabled) {
                            DspSliderRow(
                                label = "NIVEL EXCITADOR ARMÓNICO",
                                value = dspSettings.exciterLevel,
                                range = 0f..10f,
                                unit = "dB",
                                enabled = dspEnabled,
                                theme = theme,
                                onValueChange = { onExciterChanged(true, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DspSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    enabled: Boolean,
    theme: CarAudioThemeType,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatted = remember(value, unit) {
        if (unit == "%" || unit == "Hz") {
            "${value.roundToInt()} $unit"
        } else {
            val rounded = (value * 10f).roundToInt() / 10f
            val sign = if (rounded > 0f && unit == "dB") "+" else ""
            "$sign$rounded $unit"
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) theme.textColor else Color.Gray
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.cardColor)
                        .clickable(enabled = enabled) {
                            onValueChange(0f.coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("0", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (enabled) theme.primaryColor else Color.DarkGray)
                }
                Text(
                    text = formatted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) theme.primaryColor else Color.DarkGray
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = theme.primaryColor,
                activeTrackColor = theme.primaryColor,
                inactiveTrackColor = theme.cardColor,
                disabledThumbColor = Color.DarkGray,
                disabledActiveTrackColor = Color.DarkGray
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}
