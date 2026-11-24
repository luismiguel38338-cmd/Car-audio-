package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbSlot
import com.example.model.CarAudioThemeType
import com.example.model.DspAbSnapshot
import com.example.model.DspCapabilityReport
import com.example.model.DspEngineStatus
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType

@Composable
fun EqualizerView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    capabilityReport: DspCapabilityReport,
    abSlotA: DspAbSnapshot?,
    abSlotB: DspAbSnapshot?,
    activeAbSlot: AbSlot?,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    onToggleDspMaster: (Boolean) -> Unit,
    onToggleEqPower: (Boolean) -> Unit,
    onUpdateBand10: (Int, Float) -> Unit,
    onUpdateBand31: (Int, Float) -> Unit,
    onUpdateParametricBand: (Int, Float, Float, Float, ParametricFilterType, Boolean) -> Unit,
    onToggleParametricBand: (Int, Boolean) -> Unit,
    onResetFlat: () -> Unit,
    onCaptureAbSlot: (AbSlot) -> Unit,
    onApplyAbSlot: (AbSlot) -> Unit,
    onToggleAbComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 31 Bandas, 1 = 10 Bandas, 2 = Paramétrico 5B
    val scroll = rememberScrollState()
    val dspOn = dspSettings.dspMasterEnabled
    val curveBands = remember(selectedTab, eqSettings) {
        when (selectedTab) {
            1 -> eqSettings.bands10
            else -> eqSettings.bands31
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("equalizer_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassStudioCard(theme = theme, highlightBorder = dspOn && eqSettings.isEnabled) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = theme.primaryColor)
                        Column {
                            Text("Ecualizador DSP", color = theme.textColor, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (dspOn) "Preset: ${eqSettings.activePresetName}" else "DSP apagado (toca para activar)",
                                color = if (dspOn) theme.accentColor else theme.textSecondaryColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SmartStatusLedBadge(
                                status = when (capabilityReport.engineStatus) {
                                    DspEngineStatus.ACTIVE_INTERNAL -> SmartLightStatus.ACTIVE_READY
                                    DspEngineStatus.ACTIVE_EXTERNAL -> SmartLightStatus.AVAILABLE
                                    DspEngineStatus.LIMITED_FALLBACK -> SmartLightStatus.WARNING_LIMIT
                                    DspEngineStatus.BYPASSED -> SmartLightStatus.DISABLED
                                },
                                text = capabilityReport.engineStatus.label,
                                theme = theme,
                                liteVisuals = liteVisuals
                            )
                        }
                    }
                    Switch(
                        checked = dspOn,
                        onCheckedChange = onToggleDspMaster,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.primaryColor
                        ),
                        modifier = Modifier.testTag("eq_dsp_master_switch")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor.copy(alpha = 0.85f))
                        .border(0.8.dp, theme.cardColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                        Text("Activar curvas de ecualización", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = onToggleEqPower,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.silverDetailColor,
                            checkedTrackColor = theme.accentColor
                        )
                    )
                }
            }
        }

        AnimatedFrequencyCurveCanvas(
            bands = curveBands,
            enabled = dspOn && eqSettings.isEnabled,
            isPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals
        )

        AbQuickComparisonBar(
            theme = theme,
            slotA = abSlotA,
            slotB = abSlotB,
            activeSlot = activeAbSlot,
            onCaptureSlot = onCaptureAbSlot,
            onApplySlot = onApplyAbSlot,
            onToggleAb = onToggleAbComparison
        )

        EpicTriBandPowerMeters(
            isPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            dspMasterEnabled = dspOn,
            theme = theme,
            liteVisuals = liteVisuals
        )

        val tabs = listOf("31 Bandas ISO", "10 Bandas Rápido", "Paramétrico 5B")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                val selected = selectedTab == idx
                FilterChip(
                    selected = selected,
                    onClick = { selectedTab = idx },
                    label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    border = BorderStroke(
                        1.dp,
                        if (selected) theme.silverDetailColor.copy(alpha = 0.4f) else theme.cardColor
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primaryColor,
                        selectedLabelColor = theme.onPrimaryColor,
                        containerColor = theme.surfaceColor.copy(alpha = 0.85f),
                        labelColor = theme.textColor
                    ),
                    modifier = Modifier.testTag("equalizer_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> GraphicEqTab(
                theme = theme,
                bands = eqSettings.bands31,
                labels = EqualizerSettings.FREQUENCY_LABELS_31,
                enabled = dspOn && eqSettings.isEnabled,
                is31BandMode = true,
                capabilityReport = capabilityReport,
                onBandChanged = onUpdateBand31,
                onResetFlat = onResetFlat
            )
            1 -> GraphicEqTab(
                theme = theme,
                bands = eqSettings.bands10,
                labels = EqualizerSettings.FREQUENCY_LABELS_10,
                enabled = dspOn && eqSettings.isEnabled,
                is31BandMode = false,
                capabilityReport = capabilityReport,
                onBandChanged = onUpdateBand10,
                onResetFlat = onResetFlat
            )
            else -> ParametricEqTab(
                theme = theme,
                eqSettings = eqSettings,
                dspMasterEnabled = dspOn,
                onUpdateBand = onUpdateParametricBand,
                onToggleBand = onToggleParametricBand
            )
        }
    }
}
