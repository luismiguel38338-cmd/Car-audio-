package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerProcessorView(
    theme: CarAudioThemeType,
    eqSettings: EqualizerSettings,
    onTogglePower: (Boolean) -> Unit,
    onUpdateMasterGain: (Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onUpdateBand: (index: Int, gainDb: Float) -> Unit,
    onUpdateParametric: (freqHz: Float, gainDb: Float, q: Float) -> Unit,
    onSelectPreset: (String) -> Unit,
    onResetFlat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalFaderScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Controller Header Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (eqSettings.isEnabled) theme.primaryColor.copy(alpha = 0.25f)
                                else Color.DarkGray.copy(alpha = 0.3f)
                            )
                            .border(
                                1.5.dp,
                                if (eqSettings.isEnabled) theme.primaryColor else Color.Gray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (eqSettings.isEnabled) theme.primaryColor else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "CONTROLADOR PROCESADOR EQ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.textColor,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (eqSettings.isEnabled) "Activo • 15 Bandas ISO + Paramétrico" else "BYPASS / Desactivado",
                            fontSize = 11.sp,
                            color = if (eqSettings.isEnabled) theme.primaryColor else Color.Gray
                        )
                    }
                }

                // Master Power Switch
                Switch(
                    checked = eqSettings.isEnabled,
                    onCheckedChange = { onTogglePower(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.onPrimaryColor,
                        checkedTrackColor = theme.primaryColor,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("switch_eq_power")
                )
            }
        }

        // Real-Time Equalizer Frequency Response Curve
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CURVA DE RESPUESTA EN FRECUENCIA (dB)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondaryColor,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Preset: ${eqSettings.activePresetName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentColor
                    )
                }

                // Interactive Response Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B13))
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                ) {
                    EqCurveCanvas(
                        eq = eqSettings,
                        theme = theme,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Frequency range zones indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUBGRAVE (20-60Hz)", fontSize = 9.sp, color = theme.primaryColor, fontFamily = FontFamily.Monospace)
                    Text("MEDIO-GRAVE (80-250Hz)", fontSize = 9.sp, color = theme.textSecondaryColor, fontFamily = FontFamily.Monospace)
                    Text("VOCES (500-2kHz)", fontSize = 9.sp, color = theme.accentColor, fontFamily = FontFamily.Monospace)
                    Text("AGUDOS (4k-20kHz)", fontSize = 9.sp, color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Master Gain & Limiter Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Master Gain Slider
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ganancia Master EQ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Text(
                            "${if (eqSettings.masterGainDb >= 0) "+" else ""}${String.format("%.1f", eqSettings.masterGainDb)} dB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = eqSettings.masterGainDb,
                        onValueChange = { onUpdateMasterGain(it) },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.primaryColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("slider_eq_master_gain")
                    )
                }

                // Anti-Clip Limiter Toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Limitador", fontSize = 10.sp, color = theme.textSecondaryColor)
                    Switch(
                        checked = eqSettings.isLimiterActive,
                        onCheckedChange = { onToggleLimiter(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.accentColor,
                            checkedTrackColor = theme.accentColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("switch_eq_limiter")
                    )
                }

                // Reset Flat button
                OutlinedButton(
                    onClick = { onResetFlat() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.textSecondaryColor),
                    modifier = Modifier.testTag("btn_reset_eq_flat")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flat 0dB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Brazilian & Professional EQ Presets
        Text(
            text = "PRESETS DE ECUALIZACIÓN BRASILERA & CAR AUDIO:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = theme.primaryColor,
            letterSpacing = 1.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EqPresetCatalog.presets.forEach { preset ->
                val isSelected = preset.name == eqSettings.activePresetName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) theme.primaryColor.copy(alpha = 0.25f)
                            else theme.surfaceColor
                        )
                        .border(
                            1.dp,
                            if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectPreset(preset.name) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("preset_${preset.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.primaryColor else theme.textColor
                        )
                    }
                }
            }
        }

        // 15-Band Graphic Equalizer Faders
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
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
                    Text(
                        text = "15 BANDAS ISO GRÁFICAS (-12 dB a +12 dB)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Text(
                        text = "Desliza horizontalmente ➔",
                        fontSize = 10.sp,
                        color = theme.textSecondaryColor
                    )
                }

                // Horizontal scrollable fader deck
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalFaderScroll)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EqualizerSettings.FREQUENCY_LABELS.forEachIndexed { index, label ->
                        val gain = eqSettings.bands15.getOrElse(index) { 0.0f }
                        VerticalEqFader(
                            label = label,
                            gainDb = gain,
                            onGainChange = { onUpdateBand(index, it) },
                            theme = theme
                        )
                    }
                }
            }
        }

        // Parametric Equalizer Control Section
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FILTRO PARAMÉTRICO PRO (Puntual Quirúrgico)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                }

                // Parametric Frequency Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Frecuencia Central", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            "${eqSettings.parametricFreqHz.toInt()} Hz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = eqSettings.parametricFreqHz,
                        onValueChange = {
                            onUpdateParametric(it, eqSettings.parametricGainDb, eqSettings.parametricQ)
                        },
                        valueRange = 20f..15000f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentColor,
                            activeTrackColor = theme.accentColor
                        )
                    )
                }

                // Parametric Gain Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ganancia Filtro", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            "${if (eqSettings.parametricGainDb >= 0) "+" else ""}${String.format("%.1f", eqSettings.parametricGainDb)} dB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = eqSettings.parametricGainDb,
                        onValueChange = {
                            onUpdateParametric(eqSettings.parametricFreqHz, it, eqSettings.parametricQ)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor
                        )
                    )
                }

                // Parametric Q Factor Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Factor Q (Ancho de Banda)", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text(
                            "Q = ${String.format("%.2f", eqSettings.parametricQ)} (${if (eqSettings.parametricQ > 2.0) "Estrecho" else "Amplio"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = eqSettings.parametricQ,
                        onValueChange = {
                            onUpdateParametric(eqSettings.parametricFreqHz, eqSettings.parametricGainDb, it)
                        },
                        valueRange = 0.5f..5.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalEqFader(
    label: String,
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF080C16))
            .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Gain Readout
        Text(
            text = "${if (gainDb >= 0) "+" else ""}${gainDb.toInt()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (gainDb > 0) theme.primaryColor else if (gainDb < 0) theme.accentColor else Color.Gray,
            fontFamily = FontFamily.Monospace
        )

        // Vertical Slider using rotated Box or Custom Slider
        Box(
            modifier = Modifier
                .height(130.dp)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background line and center detent
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(size.width / 2f, 10f),
                    end = Offset(size.width / 2f, size.height - 10f),
                    strokeWidth = 3f
                )
                // Center line (0dB detent)
                drawLine(
                    color = theme.primaryColor.copy(alpha = 0.6f),
                    start = Offset(6f, midY),
                    end = Offset(size.width - 6f, midY),
                    strokeWidth = 2f
                )
            }

            // Clickable / Draggable area using slider
            Slider(
                value = gainDb,
                onValueChange = onGainChange,
                valueRange = -12f..12f,
                colors = SliderDefaults.colors(
                    thumbColor = if (gainDb >= 0) theme.primaryColor else theme.accentColor,
                    activeTrackColor = theme.primaryColor,
                    inactiveTrackColor = Color(0xFF1E283C)
                ),
                modifier = Modifier
                    .size(width = 120.dp, height = 36.dp)
                    .drawWithRotation(-90f)
            )
        }

        // Frequency Label
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

// Utility to rotate the slider for a vertical fader feel
fun Modifier.drawWithRotation(degrees: Float): Modifier = this.graphicsLayer {
    rotationZ = degrees
}

@Composable
fun EqCurveCanvas(
    eq: EqualizerSettings,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Grid lines: +12dB, +6dB, 0dB, -6dB, -12dB
        val dbSteps = listOf(12f to 0.1f, 6f to 0.3f, 0f to 0.5f, -6f to 0.7f, -12f to 0.9f)
        dbSteps.forEach { (_, yRatio) ->
            val y = h * yRatio
            drawLine(
                color = if (yRatio == 0.5f) theme.primaryColor.copy(alpha = 0.4f) else Color(0xFF1A233A),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = if (yRatio == 0.5f) 1.5f else 1f
            )
        }

        if (!eq.isEnabled) {
            // Bypass flat line
            drawLine(
                color = Color.Gray,
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val freqs = EqualizerSettings.FREQUENCIES_HZ
        val numPoints = 80
        val minLog = log10(20.0)
        val maxLog = log10(20000.0)

        val curvePath = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, midY)

        for (i in 0..numPoints) {
            val ratio = i.toFloat() / numPoints
            val logFreq = minLog + (maxLog - minLog) * ratio
            val freqHz = 10.0.pow(logFreq).toFloat()

            // Sum contribution from graphic bands
            var totalGain = eq.masterGainDb
            freqs.forEachIndexed { bandIdx, bFreq ->
                val bandGain = eq.bands15.getOrElse(bandIdx) { 0f }
                val octaveDist = abs(log10((freqHz / bFreq).toDouble())) / log10(2.0)
                val weight = (1.0 - octaveDist).coerceIn(0.0, 1.0).toFloat()
                totalGain += bandGain * weight
            }

            // Contribution from parametric filter
            val paramDist = abs(log10((freqHz / eq.parametricFreqHz).toDouble())) / log10(2.0)
            val paramWeight = (1.0 - (paramDist * eq.parametricQ)).coerceIn(0.0, 1.0).toFloat()
            totalGain += eq.parametricGainDb * paramWeight

            // Apply limiter clipping threshold
            if (eq.isLimiterActive) {
                totalGain = totalGain.coerceIn(-12f, 10.5f)
            }

            // Map totalGain (-15 to +15 dB) to canvas Y
            val normY = (1.0f - (totalGain / 15f)) / 2f
            val canvasY = (normY * h).coerceIn(4f, h - 4f)
            val canvasX = ratio * w

            if (i == 0) {
                curvePath.moveTo(canvasX, canvasY)
                fillPath.lineTo(canvasX, canvasY)
            } else {
                curvePath.lineTo(canvasX, canvasY)
                fillPath.lineTo(canvasX, canvasY)
            }
        }

        fillPath.lineTo(w, midY)
        fillPath.close()

        // Draw area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(
                    theme.primaryColor.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        // Draw curve stroke
        drawPath(
            path = curvePath,
            brush = Brush.horizontalGradient(
                listOf(
                    theme.primaryColor,
                    theme.accentColor,
                    Color(0xFF00E5FF)
                )
            ),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}
