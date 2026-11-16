package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.CustomSoundProfile
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType

@Composable
fun AudioLabView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    presets: List<EqPresetItem>,
    customProfiles: List<CustomSoundProfile>,
    capabilityReport: DspCapabilityReport,
    isVipUnlocked: Boolean,
    cloudSyncActive: Boolean,
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
    onSelectPreset: (EqPresetItem) -> Unit,
    onDeletePreset: (EqPresetItem) -> Unit,
    onSaveSoundProfile: (String, String) -> Unit,
    onApplySoundProfile: (CustomSoundProfile) -> Unit,
    onDeleteSoundProfile: (String) -> Unit,
    onPreampChanged: (Float) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onBassBoostChanged: (Boolean, Float, Float) -> Unit,
    onSubBassChanged: (Boolean, Float, Float) -> Unit,
    onSubsonicChanged: (Boolean, Float) -> Unit,
    onLoudnessChanged: (Boolean, Float) -> Unit,
    onBalanceChanged: (Float) -> Unit,
    onStereoWidthChanged: (Float) -> Unit,
    onToggleCompressor: (Boolean) -> Unit,
    onCompressorParamsChanged: (Float, Float, Float, Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onLimiterCeilingChanged: (Float) -> Unit,
    onHpfChanged: (Boolean, Float, Int) -> Unit,
    onLpfChanged: (Boolean, Float, Int) -> Unit,
    onOpenVipZone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()
    val dspOn = dspSettings.dspMasterEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("audio_lab_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hardware Capability & Anti-Double-Processing Badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audiolab_capability_card")
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
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.accentColor)
                        Column {
                            Text("LABORATORIO DE AUDIO DSP", color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                            Text(capabilityReport.engineSummary, color = theme.accentColor, fontSize = 10.5.sp)
                        }
                    }
                    Switch(
                        checked = dspOn,
                        onCheckedChange = onToggleDspMaster,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("audiolab_master_switch")
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
                            "Etapa única verificada: sin doble aplicación de EQ ni BassBoost."
                        } else {
                            "Modo compatibilidad activo"
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
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(17.dp))
                        Text("Ecualizador Activo (${eqSettings.activePresetName})", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
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

        StudioSignalIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "RESPUESTA DE SEÑAL DSP"
        )

        // 2. 6-Tab Audio Lab Selector
        val tabs = listOf(
            "10 Bandas",
            "31 Bandas",
            "Paramétrico",
            "Tono y Estéreo",
            "Dinámica y Filtros",
            "Presets y Nube"
        )
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
                    modifier = Modifier.testTag("audiolab_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> GraphicEqTab(
                theme = theme,
                bands = eqSettings.bands10,
                labels = EqualizerSettings.FREQUENCY_LABELS_10,
                enabled = dspOn && eqSettings.isEnabled,
                is31BandMode = false,
                capabilityReport = capabilityReport,
                onBandChanged = onUpdateBand10,
                onResetFlat = onResetFlat
            )

            1 -> GraphicEqTab(
                theme = theme,
                bands = eqSettings.bands31,
                labels = EqualizerSettings.FREQUENCY_LABELS_31,
                enabled = dspOn && eqSettings.isEnabled,
                is31BandMode = true,
                capabilityReport = capabilityReport,
                onBandChanged = onUpdateBand31,
                onResetFlat = onResetFlat
            )

            2 -> ParametricEqTab(
                theme = theme,
                eqSettings = eqSettings,
                dspMasterEnabled = dspOn,
                onUpdateBand = onUpdateParametricBand,
                onToggleBand = onToggleParametricBand
            )

            3 -> ToneAndStereoTab(
                theme = theme,
                dspSettings = dspSettings,
                dspOn = dspOn,
                onPreampChanged = onPreampChanged,
                onMasterGainChanged = onMasterGainChanged,
                onBassGainChanged = onBassGainChanged,
                onMidGainChanged = onMidGainChanged,
                onTrebleGainChanged = onTrebleGainChanged,
                onBassBoostChanged = onBassBoostChanged,
                onSubBassChanged = onSubBassChanged,
                onLoudnessChanged = onLoudnessChanged,
                onBalanceChanged = onBalanceChanged,
                onStereoWidthChanged = onStereoWidthChanged
            )

            4 -> DynamicsAndFiltersTab(
                theme = theme,
                dspSettings = dspSettings,
                dspOn = dspOn,
                capabilityReport = capabilityReport,
                onToggleCompressor = onToggleCompressor,
                onCompressorParamsChanged = onCompressorParamsChanged,
                onToggleLimiter = onToggleLimiter,
                onLimiterCeilingChanged = onLimiterCeilingChanged,
                onSubsonicChanged = onSubsonicChanged,
                onHpfChanged = onHpfChanged,
                onLpfChanged = onLpfChanged
            )

            else -> PresetsAndCloudTab(
                theme = theme,
                presets = presets,
                activePresetName = eqSettings.activePresetName,
                customProfiles = customProfiles,
                isVipUnlocked = isVipUnlocked,
                cloudSyncActive = cloudSyncActive,
                onSelectPreset = onSelectPreset,
                onDeletePreset = onDeletePreset,
                onSaveSoundProfile = onSaveSoundProfile,
                onApplySoundProfile = onApplySoundProfile,
                onDeleteSoundProfile = onDeleteSoundProfile,
                onOpenVipZone = onOpenVipZone
            )
        }
    }
}

@Composable
private fun ToneAndStereoTab(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    dspOn: Boolean,
    onPreampChanged: (Float) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onBassBoostChanged: (Boolean, Float, Float) -> Unit,
    onSubBassChanged: (Boolean, Float, Float) -> Unit,
    onLoudnessChanged: (Boolean, Float) -> Unit,
    onBalanceChanged: (Float) -> Unit,
    onStereoWidthChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PREAMPLIFICADOR Y TONO (GRAVES / MEDIOS / AGUDOS)", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                DspSliderRow("Preamplificador (Preamp)", dspSettings.preampDb, -12f..18f, "dB", dspOn, theme, onValueChange = onPreampChanged)
                DspSliderRow("Ganancia Maestra", dspSettings.masterGainDb, -18f..18f, "dB", dspOn, theme, onValueChange = onMasterGainChanged)
                DspSliderRow("Graves (Bass)", dspSettings.bassGainDb, -18f..18f, "dB", dspOn, theme, onValueChange = onBassGainChanged)
                DspSliderRow("Medios (Mid)", dspSettings.midGainDb, -18f..18f, "dB", dspOn, theme, onValueChange = onMidGainChanged)
                DspSliderRow("Agudos (Treble)", dspSettings.trebleGainDb, -18f..18f, "dB", dspOn, theme, onValueChange = onTrebleGainChanged)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("BASS BOOST Y SUB-GRAVES", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = dspSettings.bassBoostEnabled,
                        onCheckedChange = { onBassBoostChanged(it, dspSettings.bassBoostDb, dspSettings.bassBoostFreqHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                DspSliderRow("Refuerzo Bass Boost", dspSettings.bassBoostDb, 0f..24f, "dB", dspOn && dspSettings.bassBoostEnabled, theme, onValueChange = { onBassBoostChanged(true, it, dspSettings.bassBoostFreqHz) })
                DspSliderRow("Frecuencia Bass Boost", dspSettings.bassBoostFreqHz, 30f..90f, "Hz", dspOn && dspSettings.bassBoostEnabled, theme, decimals = 0, onValueChange = { onBassBoostChanged(true, dspSettings.bassBoostDb, it) })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Extensión Sub-Bass", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.subBassEnabled,
                        onCheckedChange = { onSubBassChanged(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.subBassEnabled) {
                    DspSliderRow("Nivel Sub-Bass", dspSettings.subBassBoostDb, 0f..18f, "dB", dspOn, theme, onValueChange = { onSubBassChanged(true, it, dspSettings.subBassFreqHz) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Contorno Loudness", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.loudnessEnabled,
                        onCheckedChange = { onLoudnessChanged(it, dspSettings.loudnessGainDb) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.loudnessEnabled) {
                    DspSliderRow("Nivel Loudness", dspSettings.loudnessGainDb, 0f..15f, "dB", dspOn, theme, onValueChange = { onLoudnessChanged(true, it) })
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("BALANCE ESTÉREO Y VIRTUALIZADOR", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                DspSliderRow("Balance Izquierda / Derecha", dspSettings.balancePan * 100f, -100f..100f, "%", dspOn, theme, decimals = 0, onValueChange = { onBalanceChanged(it / 100f) })
                DspSliderRow("Amplitud Virtualizer", dspSettings.stereoWidthPercent, 100f..250f, "%", dspOn, theme, decimals = 0, onValueChange = onStereoWidthChanged)
            }
        }
    }
}

@Composable
private fun DynamicsAndFiltersTab(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    dspOn: Boolean,
    capabilityReport: DspCapabilityReport,
    onToggleCompressor: (Boolean) -> Unit,
    onCompressorParamsChanged: (Float, Float, Float, Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onLimiterCeilingChanged: (Float) -> Unit,
    onSubsonicChanged: (Boolean, Float) -> Unit,
    onHpfChanged: (Boolean, Float, Int) -> Unit,
    onLpfChanged: (Boolean, Float, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COMPRESOR MULTIBANDA", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (capabilityReport.dynamicsProcessingActive) "Etapa MBC en hardware activa" else "Requiere DynamicsProcessing API 28+",
                            color = theme.textSecondaryColor,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = dspSettings.compressorEnabled,
                        onCheckedChange = onToggleCompressor,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.compressorEnabled) {
                    DspSliderRow("Umbral (Threshold)", dspSettings.compressorThresholdDb, -48f..0f, "dB", dspOn, theme, onValueChange = { onCompressorParamsChanged(it, dspSettings.compressorRatio, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs) })
                    DspSliderRow("Relación (Ratio)", dspSettings.compressorRatio, 1f..20f, ":1", dspOn, theme, onValueChange = { onCompressorParamsChanged(dspSettings.compressorThresholdDb, it, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs) })
                    DspSliderRow("Ataque", dspSettings.compressorAttackMs, 1f..100f, "ms", dspOn, theme, decimals = 0, onValueChange = { onCompressorParamsChanged(dspSettings.compressorThresholdDb, dspSettings.compressorRatio, it, dspSettings.compressorReleaseMs) })
                    DspSliderRow("Liberación", dspSettings.compressorReleaseMs, 15f..1000f, "ms", dspOn, theme, decimals = 0, onValueChange = { onCompressorParamsChanged(dspSettings.compressorThresholdDb, dspSettings.compressorRatio, dspSettings.compressorAttackMs, it) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LIMITADOR DE SEGURIDAD", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Evita recorte y distorsión en picos digitales", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = dspSettings.limiterEnabled,
                        onCheckedChange = onToggleLimiter,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                DspSliderRow("Techo del Limitador", dspSettings.limiterCeilingDb, -24f..-0.2f, "dB", dspOn && dspSettings.limiterEnabled, theme, onValueChange = onLimiterCeilingChanged)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FILTROS DE FRECUENCIA", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtro Subsónico (Protección)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.subsonicFilterActive,
                        onCheckedChange = { onSubsonicChanged(it, dspSettings.subsonicFrequencyHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.subsonicFilterActive) {
                    DspSliderRow("Corte Subsónico", dspSettings.subsonicFrequencyHz, 15f..50f, "Hz", dspOn, theme, decimals = 0, onValueChange = { onSubsonicChanged(true, it) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtro Pasa-Altos (HPF)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.hpfEnabled,
                        onCheckedChange = { onHpfChanged(it, dspSettings.hpfFrequencyHz, dspSettings.hpfSlopeDb) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.hpfEnabled) {
                    DspSliderRow("Frecuencia HPF", dspSettings.hpfFrequencyHz, 15f..500f, "Hz", dspOn, theme, decimals = 0, onValueChange = { onHpfChanged(true, it, dspSettings.hpfSlopeDb) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtro Pasa-Bajos (LPF)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.lpfEnabled,
                        onCheckedChange = { onLpfChanged(it, dspSettings.lpfFrequencyHz, dspSettings.lpfSlopeDb) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.lpfEnabled) {
                    DspSliderRow("Frecuencia LPF", dspSettings.lpfFrequencyHz, 200f..20000f, "Hz", dspOn, theme, decimals = 0, onValueChange = { onLpfChanged(true, it, dspSettings.lpfSlopeDb) })
                }
            }
        }
    }
}
