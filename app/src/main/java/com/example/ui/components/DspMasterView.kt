package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        // 1. MASTER DSP POWER
        item {
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

                    // Compact Real-Time Spectrum Visualizer
                    if (rtaBands.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.backgroundColor)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = rtaBands.size.coerceAtLeast(1)
                                val spacing = 2.dp.toPx()
                                val barW = ((size.width - spacing * (count - 1)) / count).coerceAtLeast(2f)
                                rtaBands.forEachIndexed { idx, band ->
                                    val norm = ((band.levelDb + 60f) / 60f).coerceIn(0.05f, 1f)
                                    val bh = size.height * norm
                                    val x = idx * (barW + spacing)
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(theme.primaryColor, theme.accentColor.copy(alpha = 0.4f))
                                        ),
                                        topLeft = Offset(x, size.height - bh),
                                        size = Size(barW, bh),
                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. POTENCIA & TONALIDAD EN TIEMPO REAL
        item {
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
        item {
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
        item {
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
                            Text("Control automático de picos", fontSize = 9.sp, color = theme.textSecondaryColor)
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
                }
            }
        }

        // 5. FILTROS DE CORTE HPF & LPF
        item {
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
                        Text("FILTROS DE CORTE (HPF / LPF)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textColor)
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
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("0", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (enabled) theme.primaryColor else Color.DarkGray)
                }
                val formatted = if (unit == "%" || unit == "Hz") "${value.roundToInt()} $unit"
                else "${if (value > 0 && unit == "dB") "+" else ""}${String.format("%.1f", value)} $unit"
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
