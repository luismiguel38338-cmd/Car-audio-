package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings

@Composable
fun SubwooferView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    isPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    onSetSubBass: (Boolean, Float, Float) -> Unit,
    onSetBassPunch: (Float) -> Unit,
    onSetSubsonicFilter: (Boolean, Float) -> Unit,
    onSetFilterLpf: (Boolean, Float, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("subwoofer_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Automotive3DCockpitView(
            theme = theme,
            isPlaying = isPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            dspMasterEnabled = dspSettings.dspMasterEnabled && dspSettings.subBassEnabled,
            liteVisuals = liteVisuals
        )

        EpicTriBandPowerMeters(
            isPlaying = isPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            dspMasterEnabled = dspSettings.dspMasterEnabled && dspSettings.subBassEnabled,
            theme = theme,
            liteVisuals = liteVisuals
        )

        GlassStudioCard(theme = theme, highlightBorder = dspSettings.subBassEnabled) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = "Subwoofer", tint = theme.primaryColor)
                        Column {
                            Text("Control de Subwoofer 3D", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Excursión profunda y respuesta transitoria", color = theme.accentColor, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = dspSettings.subBassEnabled,
                        onCheckedChange = { onSetSubBass(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.primaryColor
                        ),
                        modifier = Modifier.testTag("subwoofer_enable_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    RotaryKnob3D(
                        label = "Ganancia Sub",
                        value = dspSettings.subBassBoostDb,
                        range = 0f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspSettings.subBassEnabled,
                        decimals = 1,
                        size = 88.dp,
                        onValueChange = { onSetSubBass(dspSettings.subBassEnabled, it, dspSettings.subBassFreqHz) },
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Corte Sub",
                        value = dspSettings.subBassFreqHz,
                        range = 20f..85f,
                        unit = "Hz",
                        theme = theme,
                        enabled = dspSettings.subBassEnabled,
                        decimals = 0,
                        size = 88.dp,
                        onValueChange = { onSetSubBass(dspSettings.subBassEnabled, dspSettings.subBassBoostDb, it) },
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Bass Punch",
                        value = dspSettings.bassPunchLevel,
                        range = 0f..10f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspSettings.dspMasterEnabled,
                        decimals = 1,
                        size = 88.dp,
                        onValueChange = onSetBassPunch,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        GlassStudioCard(theme = theme, highlightBorder = dspSettings.subsonicFilterActive || dspSettings.lpfEnabled) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PROTECCIÓN Y CROSSOVER DE SUBWOOFER",
                    color = theme.silverDetailColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )

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
                        Icon(Icons.Default.Shield, contentDescription = null, tint = theme.accentColor)
                        Column {
                            Text("Filtro Subsónico (Protección)", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Protege bajo ${dspSettings.subsonicFrequencyHz.toInt()} Hz", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                    }
                    Switch(
                        checked = dspSettings.subsonicFilterActive,
                        onCheckedChange = { onSetSubsonicFilter(it, dspSettings.subsonicFrequencyHz) },
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
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.primaryColor)
                        Column {
                            Text("Crossover Pasa-Bajos (LPF)", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Limita frecuencias altas hacia bajos", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                    }
                    Switch(
                        checked = dspSettings.lpfEnabled,
                        onCheckedChange = { onSetFilterLpf(it, dspSettings.lpfFrequencyHz, dspSettings.lpfSlopeDb) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.primaryColor
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    RotaryKnob3D(
                        label = "Subsónico",
                        value = dspSettings.subsonicFrequencyHz,
                        range = 15f..50f,
                        unit = "Hz",
                        theme = theme,
                        enabled = dspSettings.subsonicFilterActive,
                        decimals = 0,
                        size = 92.dp,
                        onValueChange = { onSetSubsonicFilter(dspSettings.subsonicFilterActive, it) },
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Crossover LPF",
                        value = dspSettings.lpfFrequencyHz,
                        range = 200f..20000f,
                        unit = "Hz",
                        theme = theme,
                        enabled = dspSettings.lpfEnabled,
                        decimals = 0,
                        size = 92.dp,
                        onValueChange = { onSetFilterLpf(dspSettings.lpfEnabled, it, dspSettings.lpfSlopeDb) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        BrandFooterBadge(theme = theme)
    }
}
