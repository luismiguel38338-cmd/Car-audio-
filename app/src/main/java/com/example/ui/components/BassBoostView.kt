package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings

@Composable
fun BassBoostView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    bassMode: BassMode,
    isPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    isClippingDetected: Boolean,
    liteVisuals: Boolean,
    onSelectBassMode: (BassMode) -> Unit,
    onSetBassBoost: (Boolean, Float, Float) -> Unit,
    onSetBassGain: (Float) -> Unit,
    onSetDeepBass: (Boolean, Float, Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val bassIntensityRatio = (dspSettings.bassBoostDb / 18f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("bass_boost_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Automotive3DCockpitView(
            theme = theme,
            isPlaying = isPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            dspMasterEnabled = dspSettings.dspMasterEnabled && dspSettings.bassBoostEnabled,
            liteVisuals = liteVisuals
        )

        EpicTriBandPowerMeters(
            isPlaying = isPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            dspMasterEnabled = dspSettings.dspMasterEnabled && dspSettings.bassBoostEnabled,
            theme = theme,
            liteVisuals = liteVisuals
        )

        // Modo de potencia: Normal / Potente / Extremo
        GlassStudioCard(theme = theme, highlightBorder = dspSettings.bassBoostEnabled) {
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
                        Icon(Icons.Default.Speaker, contentDescription = "Bass Boost", tint = theme.primaryColor)
                        Column {
                            Text("Bass Boost DSP", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Modo activo: ${bassMode.label}", color = theme.accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Switch(
                        checked = dspSettings.bassBoostEnabled,
                        onCheckedChange = { onSetBassBoost(it, dspSettings.bassBoostDb, dspSettings.bassBoostFreqHz) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.primaryColor
                        ),
                        modifier = Modifier.testTag("bass_boost_master_switch")
                    )
                }

                Text(
                    text = "Selecciona intensidad de potencia",
                    color = theme.textSecondaryColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BassMode.entries.forEach { mode ->
                        val selected = mode == bassMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) {
                                        Brush.linearGradient(
                                            listOf(
                                                theme.primaryColor.copy(alpha = 0.28f),
                                                theme.accentColor.copy(alpha = 0.16f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                theme.backgroundColor.copy(alpha = 0.85f),
                                                theme.surfaceColor.copy(alpha = 0.85f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (selected) 1.4.dp else 1.dp,
                                    color = if (selected) theme.accentColor else theme.cardColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectBassMode(mode) }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                .testTag("bass_mode_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = mode.label,
                                    color = if (selected) theme.silverDetailColor else theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${mode.defaultBoostDb.toInt()} dB",
                                    color = if (selected) theme.accentColor else theme.textSecondaryColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Text(
                    text = bassMode.description,
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp
                )
            }
        }

        // Consola rotatoria 3D para Bass, Frecuencia y Ganancia + Protección anticlipping
        GlassStudioCard(theme = theme, highlightBorder = dspSettings.bassBoostEnabled && !isClippingDetected) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERILLAS 3D DE GRAVES",
                        color = theme.silverDetailColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.6.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isClippingDetected) theme.errorColor.copy(alpha = 0.2f) else theme.backgroundColor.copy(alpha = 0.9f),
                        border = BorderStroke(
                            0.8.dp,
                            if (isClippingDetected) theme.errorColor else theme.primaryColor.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = if (isClippingDetected) "ALERTA CLIP" else "SEÑAL LIMPIA",
                            color = if (isClippingDetected) theme.errorColor else theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (bassIntensityRatio * 0.6f + bassEnergy * 0.4f).coerceIn(0f, 1f) },
                    color = if (isClippingDetected) theme.errorColor else theme.primaryColor,
                    trackColor = theme.cardColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    RotaryKnob3D(
                        label = "Bass Boost",
                        value = dspSettings.bassBoostDb,
                        range = 0f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspSettings.bassBoostEnabled,
                        decimals = 1,
                        size = 88.dp,
                        onValueChange = { onSetBassBoost(dspSettings.bassBoostEnabled, it, dspSettings.bassBoostFreqHz) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bass_boost_level_knob")
                    )
                    RotaryKnob3D(
                        label = "Frecuencia",
                        value = dspSettings.bassBoostFreqHz,
                        range = 30f..90f,
                        unit = "Hz",
                        theme = theme,
                        enabled = dspSettings.bassBoostEnabled,
                        decimals = 0,
                        size = 88.dp,
                        onValueChange = { onSetBassBoost(dspSettings.bassBoostEnabled, dspSettings.bassBoostDb, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bass_boost_freq_knob")
                    )
                    RotaryKnob3D(
                        label = "Ganancia Bass",
                        value = dspSettings.bassGainDb,
                        range = -12f..15f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspSettings.dspMasterEnabled,
                        decimals = 1,
                        size = 88.dp,
                        onValueChange = onSetBassGain,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bass_gain_knob")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor.copy(alpha = 0.85f))
                        .border(0.8.dp, theme.cardColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Sub-Bass Profundo (Deep Bass)", color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Refuerza frecuencias bajas debajo de ${dspSettings.subBassFreqHz.toInt()} Hz", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                    }
                    Switch(
                        checked = dspSettings.subBassEnabled,
                        onCheckedChange = { onSetDeepBass(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.accentColor
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor.copy(alpha = 0.85f))
                        .border(0.8.dp, theme.cardColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Limitador Anti-Clipping", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Protege tus bocinas contra distorsión digital", color = theme.textSecondaryColor, fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = dspSettings.limiterEnabled,
                        onCheckedChange = onToggleLimiter,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.primaryColor
                        )
                    )
                }
            }
        }

        BrandFooterBadge(theme = theme)
    }
}
