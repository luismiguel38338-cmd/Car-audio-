package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbSlot
import com.example.model.CarAudioThemeType
import com.example.model.DspAbSnapshot
import com.example.model.DspCapabilityReport
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("equalizer_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = theme.accentColor)
                        Column {
                            Text("Ecualizador DSP", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(capabilityReport.engineSummary, color = theme.accentColor, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = dspOn,
                        onCheckedChange = onToggleDspMaster,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("eq_dsp_master_switch")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.backgroundColor)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                    Text(
                        text = if (capabilityReport.antiDoubleProcessingSafe) {
                            "Etapa única verificada: sin doble procesamiento EQ/BassBoost."
                        } else {
                            "Modo compatibilidad AudioFX activo"
                        },
                        color = theme.textSecondaryColor,
                        fontSize = 10.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(17.dp))
                        Text("Módulo Ecualizador (${eqSettings.activePresetName})", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = onToggleEqPower,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
            }
        }

        AbQuickComparisonBar(
            theme = theme,
            slotA = abSlotA,
            slotB = abSlotB,
            activeSlot = activeAbSlot,
            onCaptureSlot = onCaptureAbSlot,
            onApplySlot = onApplyAbSlot,
            onToggleAb = onToggleAbComparison
        )

        StudioSignalIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "Respuesta de señal"
        )

        val tabs = listOf("31 Bandas ISO", "10 Bandas Rápido", "Paramétrico 5B")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.accentColor,
                        selectedLabelColor = theme.onPrimaryColor,
                        containerColor = theme.surfaceColor,
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
