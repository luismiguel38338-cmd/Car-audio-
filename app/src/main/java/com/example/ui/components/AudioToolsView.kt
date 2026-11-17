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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.OutputDeviceStatus
import com.example.model.RtaBand

@Composable
fun AudioToolsView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    capabilityReport: DspCapabilityReport,
    rtaBands: List<RtaBand>,
    oscilloscopeWave: FloatArray,
    leftVu: Float,
    rightVu: Float,
    rmsDb: Float,
    splDb: Float,
    isClippingDetected: Boolean,
    clipEventsCount: Int,
    isMicRtaActive: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    outputDeviceStatus: OutputDeviceStatus,
    powerTelemetry: DevicePowerTelemetry,
    onToggleDspMaster: (Boolean) -> Unit,
    onPreampChanged: (Float) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onBassBoostChanged: (Boolean, Float, Float) -> Unit,
    onSubBassChanged: (Boolean, Float, Float) -> Unit,
    onLoudnessChanged: (Boolean, Float) -> Unit,
    onBalanceChanged: (Float) -> Unit,
    onStereoWidthChanged: (Float) -> Unit,
    onToggleCompressor: (Boolean) -> Unit,
    onCompressorParamsChanged: (Float, Float, Float, Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onLimiterCeilingChanged: (Float) -> Unit,
    onSubsonicChanged: (Boolean, Float) -> Unit,
    onHpfChanged: (Boolean, Float, Int) -> Unit,
    onLpfChanged: (Boolean, Float, Int) -> Unit,
    onHiResChanged: (Boolean) -> Unit,
    onTubeWarmthChanged: (Float) -> Unit,
    onPresenceChanged: (Boolean, Float) -> Unit,
    onClarityChanged: (Boolean, Float) -> Unit,
    onExciterChanged: (Boolean, Float) -> Unit,
    onToggleMicRta: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Tono y Mejoras, 1=Dinámica y Filtros, 2=RTA y Medidores dB
    val dspOn = dspSettings.dspMasterEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .testTag("audio_tools_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.accentColor)
                        Column {
                            Text("Estudio DSP", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(capabilityReport.engineSummary, color = theme.accentColor, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = dspOn,
                        onCheckedChange = onToggleDspMaster,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tono y Mejoras Pro", "Dinámica y Filtros", "RTA y Medidores dB").forEachIndexed { idx, title ->
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
                        modifier = Modifier.testTag("audio_tools_tab_$idx")
                    )
                }
            }
        }

        if (selectedTab == 2) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AudioMetersView(
                    theme = theme,
                    rtaBands = rtaBands,
                    oscilloscopeWave = oscilloscopeWave,
                    leftVu = leftVu,
                    rightVu = rightVu,
                    rmsDb = rmsDb,
                    splDb = splDb,
                    isClippingDetected = isClippingDetected,
                    clipEventsCount = clipEventsCount,
                    isMicRtaActive = isMicRtaActive,
                    isAnyAudioPlaying = isAnyAudioPlaying,
                    bassEnergy = bassEnergy,
                    midEnergy = midEnergy,
                    trebleEnergy = trebleEnergy,
                    liteVisuals = liteVisuals,
                    outputDeviceStatus = outputDeviceStatus,
                    powerTelemetry = powerTelemetry,
                    onToggleMicRta = onToggleMicRta,
                    onRefreshTelemetry = onRefreshTelemetry
                )
            }
        } else {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioSignalIndicatorBar(
                    isAudioPlaying = isAnyAudioPlaying,
                    bassEnergy = bassEnergy,
                    midEnergy = midEnergy,
                    trebleEnergy = trebleEnergy,
                    theme = theme,
                    liteVisuals = liteVisuals,
                    title = "Respuesta de señal"
                )

                if (selectedTab == 0) {
                    ToneAndEnhancersContent(
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
                        onStereoWidthChanged = onStereoWidthChanged,
                        onHiResChanged = onHiResChanged,
                        onTubeWarmthChanged = onTubeWarmthChanged,
                        onPresenceChanged = onPresenceChanged,
                        onClarityChanged = onClarityChanged,
                        onExciterChanged = onExciterChanged
                    )
                } else {
                    DynamicsAndCrossoverContent(
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
                }
            }
        }
    }
}

@Composable
private fun ToneAndEnhancersContent(
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
    onStereoWidthChanged: (Float) -> Unit,
    onHiResChanged: (Boolean) -> Unit,
    onTubeWarmthChanged: (Float) -> Unit,
    onPresenceChanged: (Boolean, Float) -> Unit,
    onClarityChanged: (Boolean, Float) -> Unit,
    onExciterChanged: (Boolean, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Preamp y tono", color = theme.accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                    Text("Refuerzo de graves / Sub-Bass", color = theme.accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.bassBoostEnabled,
                        onCheckedChange = { onBassBoostChanged(it, dspSettings.bassBoostDb, dspSettings.bassBoostFreqHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                DspSliderRow("Ganancia Bass Boost", dspSettings.bassBoostDb, 0f..24f, "dB", dspOn && dspSettings.bassBoostEnabled, theme, onValueChange = { onBassBoostChanged(true, it, dspSettings.bassBoostFreqHz) })
                DspSliderRow("Frecuencia Central Bass", dspSettings.bassBoostFreqHz, 30f..90f, "Hz", dspOn && dspSettings.bassBoostEnabled, theme, decimals = 0, onValueChange = { onBassBoostChanged(true, dspSettings.bassBoostDb, it) })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Extensión Sub-Bass Profundo", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Campo estéreo y realce", color = theme.accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                DspSliderRow("Balance Izquierda / Derecha", dspSettings.balancePan * 100f, -100f..100f, "%", dspOn, theme, decimals = 0, onValueChange = { onBalanceChanged(it / 100f) })
                DspSliderRow("Amplitud Virtualizer Estéreo", dspSettings.stereoWidthPercent, 100f..250f, "%", dspOn, theme, decimals = 0, onValueChange = onStereoWidthChanged)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Hi-Res Studio", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Depura medios turbio (280 Hz) y aporta aire limpio.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = dspSettings.hiResQualityEnabled,
                        onCheckedChange = onHiResChanged,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("tools_hires_switch")
                    )
                }

                DspSliderRow("Calidez Analógica (Tube)", dspSettings.tubeWarmthLevel, 0f..10f, "x", dspOn, theme, onValueChange = onTubeWarmthChanged)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Presencia Vocal (2 - 5 kHz)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = dspSettings.presenceEnabled,
                        onCheckedChange = { onPresenceChanged(it, dspSettings.presenceLevel) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.presenceEnabled) {
                    DspSliderRow("Nivel Presencia", dspSettings.presenceLevel, 0f..10f, "dB", dspOn, theme, onValueChange = { onPresenceChanged(true, it) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Claridad HD (8 - 16 kHz)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = dspSettings.clarityEnabled,
                        onCheckedChange = { onClarityChanged(it, dspSettings.clarityLevel) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.clarityEnabled) {
                    DspSliderRow("Nivel Claridad", dspSettings.clarityLevel, 0f..10f, "dB", dspOn, theme, onValueChange = { onClarityChanged(true, it) })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Excitador Armónico Digital", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = dspSettings.exciterEnabled,
                        onCheckedChange = { onExciterChanged(it, dspSettings.exciterLevel) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
                if (dspSettings.exciterEnabled) {
                    DspSliderRow("Intensidad Excitador", dspSettings.exciterLevel, 0f..10f, "dB", dspOn, theme, onValueChange = { onExciterChanged(true, it) })
                }
            }
        }
    }
}

@Composable
private fun DynamicsAndCrossoverContent(
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
                        Text("Protege altavoces contra recorte y saturación digital", color = theme.textSecondaryColor, fontSize = 10.sp)
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
                Text("Crossover y filtros de corte", color = theme.accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtro Subsónico (Protección Sub)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
