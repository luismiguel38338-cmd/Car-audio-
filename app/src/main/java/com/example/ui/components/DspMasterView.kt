package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import kotlin.math.roundToInt

data class ChainNode(
    val id: String,
    val name: String,
    val subtext: String,
    val icon: ImageVector,
    val isEnabled: Boolean,
    val valueLabel: String
)

@Composable
fun DspMasterView(
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    theme: CarAudioThemeType,
    leftVu: Float,
    rightVu: Float,
    splDb: Float,
    isClipping: Boolean,
    onToggleDspMaster: () -> Unit,
    onPreampChanged: (Float) -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onCompressorToggle: (Boolean) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val dspEnabled = dspSettings.dspMasterEnabled

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_master")
    val clipPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clip_pulse"
    )

    val neonColor = if (dspEnabled) Color(0xFF00E676) else Color(0xFF757575)
    val cardBg = Color(0xFF0C1422)
    val borderCol = if (dspEnabled) Color(0xFF00E676).copy(alpha = 0.35f) else Color(0xFF263238)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040810))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. MASTER DSP POWER & BYPASS SWITCH BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dsp_master_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (dspEnabled) Color(0xFF00E676) else Color.Red)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DSP MASTER ENGINE",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (dspEnabled) "PROCESAMIENTO REAL ACTIVO (AUDIO FX)" else "BYPASS ACTIVO (AUDIO PURO ORIGINAL)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dspEnabled) Color(0xFF00E676) else Color(0xFFFF5252),
                                letterSpacing = 1.sp
                            )
                        }

                        // Neon Power Switch Button
                        Button(
                            onClick = onToggleDspMaster,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dspEnabled) Color(0xFF00E676) else Color(0xFF263238),
                                contentColor = if (dspEnabled) Color(0xFF021206) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dsp_master_power_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dspEnabled) "DSP: ON" else "DSP: OFF",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Route indication
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B14))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MODO: ${if (dspEnabled) "DSP PROCESSING" else "DIRECT BYPASS"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                        Text(
                            text = "CALIDAD: 32-BIT FLOAT 48kHz",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (dspEnabled) Color(0xFF00E676) else Color.Gray
                        )
                    }
                }
            }
        }

        // 2. REAL-TIME TELEMETRY STRIP (VU, CLIPPING, HEADROOM)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TELEMETRÍA EN TIEMPO REAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    // VU Meters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Meter
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CANAL L", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                Text("${(leftVu * 100).toInt()}%", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { leftVu.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (leftVu > 0.85f) Color.Red else Color(0xFF00E676),
                                trackColor = Color(0xFF131D2D)
                            )
                        }

                        // Right Meter
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CANAL R", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                Text("${(rightVu * 100).toInt()}%", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { rightVu.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (rightVu > 0.85f) Color.Red else Color(0xFF00E676),
                                trackColor = Color(0xFF131D2D)
                            )
                        }

                        // Clipping Indicator LED
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isClipping) Color(0xFFFF1744) else Color(0xFF1A2638))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLIP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isClipping) Color.White else Color.DarkGray
                            )
                        }
                    }

                    // Status details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SPL: ${splDb.roundToInt()} dB",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00B0FF)
                        )
                        Text(
                            text = "HEADROOM: +${dspSettings.headroomDb} dB",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "LATENCIA: 1.2 ms",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // 3. VISUAL PROCESSING CHAIN DIAGRAM
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CADENA DE PROCESAMIENTO VISUAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val chainNodes = listOf(
                        ChainNode("in", "INPUT", "Audio Source", Icons.Default.VolumeUp, true, "0 dB"),
                        ChainNode("pre", "PREAMP", "Gain Stage", Icons.Default.Bolt, dspEnabled, "${if (dspSettings.preampDb > 0) "+" else ""}${dspSettings.preampDb} dB"),
                        ChainNode("eq", "EQ PRO", "31-Band / 15-Band", Icons.Default.GraphicEq, dspEnabled && eqSettings.isEnabled, eqSettings.activePresetName),
                        ChainNode("bass", "BASS FX", "Sub & Punch", Icons.Default.Speed, dspEnabled && dspSettings.bassBoostEnabled, "+${dspSettings.bassBoostDb} dB"),
                        ChainNode("mid", "MID", "Voice & Clarity", Icons.Default.Equalizer, dspEnabled, "${dspSettings.midGainDb} dB"),
                        ChainNode("treb", "TREBLE", "Air & Brilliance", Icons.Default.Equalizer, dspEnabled, "${dspSettings.trebleGainDb} dB"),
                        ChainNode("comp", "COMPRESSOR", "Dynamics Control", Icons.Default.Bolt, dspEnabled && dspSettings.compressorEnabled, if (dspSettings.compressorEnabled) "ON" else "OFF"),
                        ChainNode("lim", "LIMITER", "Anti-Clip Ceiling", Icons.Default.Warning, dspEnabled && dspSettings.limiterEnabled, "${dspSettings.limiterCeilingDb} dB"),
                        ChainNode("out", "OUTPUT", "Master Out", Icons.Default.VolumeUp, true, "${dspSettings.masterGainDb} dB")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(chainNodes) { node ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (node.isEnabled) Color(0xFF101E32) else Color(0xFF080C14))
                                    .border(
                                        width = 1.dp,
                                        color = if (node.isEnabled) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFF1E2838),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = node.icon,
                                        contentDescription = null,
                                        tint = if (node.isEnabled) Color(0xFF00E676) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = node.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (node.isEnabled) Color.White else Color.Gray
                                    )
                                    Text(
                                        text = node.valueLabel,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (node.isEnabled) Color(0xFF00B0FF) else Color.DarkGray
                                    )
                                }
                            }

                            if (node.id != "out") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (dspEnabled) Color(0xFF00E676).copy(alpha = 0.6f) else Color.DarkGray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. QUICK STAGE ADJUSTMENT CONTROLS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AJUSTES RÁPIDOS DEL DSP • MODO ALTA POTENCIA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    // Quick DSP Punch Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("FLAT", 0f, 0f),
                            Triple("BASS +6dB", 6f, 0f),
                            Triple("VOCES +4dB", 2f, 4f),
                            Triple("SPL PUNCH", 10f, 3f),
                            Triple("EXTREMO", 15f, 6f)
                        ).forEach { (pName, bGain, mGain) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF142032))
                                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .clickable(enabled = dspEnabled) {
                                        onBassGainChanged(bGain)
                                        onMidGainChanged(mGain)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dspEnabled) Color(0xFF00E676) else Color.DarkGray
                                )
                            }
                        }
                    }

                    // Preamp Gain Slider (-18dB to +18dB)
                    DspSliderRow(
                        label = "PREAMP GAIN (PRE-AMPLIFICACIÓN)",
                        value = dspSettings.preampDb,
                        range = -18f..18f,
                        unit = "dB",
                        enabled = dspEnabled,
                        onValueChange = onPreampChanged
                    )

                    // Bass Gain Slider (-18dB to +18dB)
                    DspSliderRow(
                        label = "BASS GAIN (GRAVES & PUNCH)",
                        value = dspSettings.bassGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        enabled = dspEnabled,
                        onValueChange = onBassGainChanged
                    )

                    // Mid Gain Slider (-18dB to +18dB)
                    DspSliderRow(
                        label = "MID GAIN (VOCES & MEDIOS)",
                        value = dspSettings.midGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        enabled = dspEnabled,
                        onValueChange = onMidGainChanged
                    )

                    // Treble Gain Slider (-18dB to +18dB)
                    DspSliderRow(
                        label = "TREBLE GAIN (AGUDOS & BRILLO)",
                        value = dspSettings.trebleGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        enabled = dspEnabled,
                        onValueChange = onTrebleGainChanged
                    )

                    // Master Output Gain Slider (-40dB to +18dB High Output)
                    DspSliderRow(
                        label = "MASTER OUTPUT (SALIDA PRINCIPAL)",
                        value = dspSettings.masterGainDb,
                        range = -40f..18f,
                        unit = "dB",
                        enabled = true,
                        onValueChange = onMasterGainChanged
                    )
                }
            }
        }

        // 5. COMPRESSOR & LIMITER MODULE STATUS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Compressor Mini-Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("COMPRESOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Switch(
                                checked = dspSettings.compressorEnabled,
                                onCheckedChange = onCompressorToggle,
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00E676),
                                    checkedTrackColor = Color(0x3300E676)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                        Text(
                            text = "Ratio: ${dspSettings.compressorRatio}:1 | Thresh: ${dspSettings.compressorThresholdDb} dB",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Limiter Mini-Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LIMITADOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Switch(
                                checked = dspSettings.limiterEnabled,
                                onCheckedChange = onLimiterToggle,
                                enabled = dspEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00E676),
                                    checkedTrackColor = Color(0x3300E676)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                        Text(
                            text = "Techo: ${dspSettings.limiterCeilingDb} dBFS (Protección)",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.LightGray else Color.Gray
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Step -1dB
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Color(0xFF162234) else Color(0xFF0D131D))
                        .clickable(enabled = enabled) {
                            onValueChange((value - 1.0f).coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("-1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.DarkGray)
                }

                // Reset 0dB
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Color(0xFF162234) else Color(0xFF0D131D))
                        .clickable(enabled = enabled) {
                            onValueChange(0.0f.coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("0", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color(0xFF00E676) else Color.DarkGray)
                }

                // Step +1dB
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Color(0xFF162234) else Color(0xFF0D131D))
                        .clickable(enabled = enabled) {
                            onValueChange((value + 1.0f).coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("+1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.DarkGray)
                }

                Text(
                    text = "${if (value > 0) "+" else ""}${String.format("%.1f", value)} $unit",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) (if (value > 6f) Color(0xFFFF9100) else Color(0xFF00E676)) else Color.DarkGray
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = if (value > 6f) Color(0xFFFF9100) else Color(0xFF00E676),
                activeTrackColor = if (value > 6f) Color(0xFFFF9100) else Color(0xFF00E676),
                inactiveTrackColor = Color(0xFF1E2838),
                disabledThumbColor = Color.DarkGray,
                disabledActiveTrackColor = Color.DarkGray
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
