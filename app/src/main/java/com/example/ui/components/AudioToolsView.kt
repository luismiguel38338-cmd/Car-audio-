package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSessionTarget
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspCapabilityReport
import com.example.model.DspEngineStatus
import com.example.model.DspSettings
import com.example.model.OutputDeviceStatus
import com.example.model.RtaBand
import com.example.model.SplWeighting

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
    sessionTarget: AudioSessionTarget = AudioSessionTarget.INTERNAL_PLAYER,
    splWeighting: SplWeighting = SplWeighting.C_WEIGHTING,
    onSelectSplWeighting: (SplWeighting) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Tono y Mejoras, 1=Dinámica y Filtros, 2=RTA y Medidores dB
    val dspOn = dspSettings.dspMasterEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("audio_tools_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassStudioCard(theme = theme, highlightBorder = dspOn) {
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
                            Text("Estudio DSP", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(sessionTarget.label, color = theme.accentColor, fontSize = 11.sp)
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
                        )
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
                    onRefreshTelemetry = onRefreshTelemetry,
                    splWeighting = splWeighting,
                    onSelectSplWeighting = onSelectSplWeighting
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
                EpicTriBandPowerMeters(
                    isPlaying = isAnyAudioPlaying,
                    bassEnergy = bassEnergy,
                    midEnergy = midEnergy,
                    trebleEnergy = trebleEnergy,
                    dspMasterEnabled = dspOn,
                    theme = theme,
                    liteVisuals = liteVisuals
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
        // Consola de Perillas 3D para Preamp, Master y Tono de 3 Bandas
        GlassStudioCard(theme = theme, highlightBorder = dspOn) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PREAMP Y CONSOLA DE TONO 3D",
                    color = theme.silverDetailColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RotaryKnob3D(
                        label = "Preamp",
                        value = dspSettings.preampDb,
                        range = -12f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onPreampChanged,
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Master",
                        value = dspSettings.masterGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onMasterGainChanged,
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Calidez Tube",
                        value = dspSettings.tubeWarmthLevel,
                        range = 0f..10f,
                        unit = "x",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onTubeWarmthChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RotaryKnob3D(
                        label = "Graves",
                        value = dspSettings.bassGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onBassGainChanged,
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Medios",
                        value = dspSettings.midGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onMidGainChanged,
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Agudos",
                        value = dspSettings.trebleGainDb,
                        range = -18f..18f,
                        unit = "dB",
                        theme = theme,
                        enabled = dspOn,
                        size = 86.dp,
                        onValueChange = onTrebleGainChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        GlassStudioCard(theme = theme, highlightBorder = dspOn && (dspSettings.bassBoostEnabled || dspSettings.subBassEnabled || dspSettings.loudnessEnabled)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("REFUERZO DE GRAVES / SUB-BASS", color = theme.silverDetailColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = dspSettings.bassBoostEnabled,
                        onCheckedChange = { onBassBoostChanged(it, dspSettings.bassBoostDb, dspSettings.bassBoostFreqHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.accentColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
                    )
                }
                if (dspSettings.loudnessEnabled) {
                    DspSliderRow("Nivel Loudness", dspSettings.loudnessGainDb, 0f..15f, "dB", dspOn, theme, onValueChange = { onLoudnessChanged(true, it) })
                }
            }
        }

        GlassStudioCard(theme = theme, highlightBorder = dspOn && dspSettings.hiResQualityEnabled) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("CAMPO ESTÉREO Y REALCE HI-RES", color = theme.silverDetailColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RotaryKnob3D(
                        label = "Balance L / R",
                        value = dspSettings.balancePan * 100f,
                        range = -100f..100f,
                        unit = "%",
                        theme = theme,
                        enabled = dspOn,
                        decimals = 0,
                        size = 88.dp,
                        onValueChange = { onBalanceChanged(it / 100f) },
                        modifier = Modifier.weight(1f)
                    )
                    RotaryKnob3D(
                        label = "Estéreo 3D",
                        value = dspSettings.stereoWidthPercent,
                        range = 100f..250f,
                        unit = "%",
                        theme = theme,
                        enabled = dspOn,
                        decimals = 0,
                        size = 88.dp,
                        onValueChange = onStereoWidthChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor.copy(alpha = 0.85f))
                        .border(0.8.dp, theme.cardColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Hi-Res Studio", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Depura medios turbio (280 Hz) y aporta aire limpio.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = dspSettings.hiResQualityEnabled,
                        onCheckedChange = onHiResChanged,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor),
                        modifier = Modifier.testTag("tools_hires_switch")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Presencia Vocal (2 - 5 kHz)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = dspSettings.presenceEnabled,
                        onCheckedChange = { onPresenceChanged(it, dspSettings.presenceLevel) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.accentColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.accentColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
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
        GlassStudioCard(theme = theme, highlightBorder = dspOn && (dspSettings.compressorEnabled || dspSettings.limiterEnabled)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COMPRESOR MULTIBANDA", color = theme.silverDetailColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (capabilityReport.dynamicsProcessingActive) "Etapa MBC en hardware activa" else "Requiere DynamicsProcessing API 28+",
                            color = theme.accentColor,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = dspSettings.compressorEnabled,
                        onCheckedChange = onToggleCompressor,
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
                    )
                }
                DspSliderRow("Techo del Limitador", dspSettings.limiterCeilingDb, -24f..-0.2f, "dB", dspOn && dspSettings.limiterEnabled, theme, onValueChange = onLimiterCeilingChanged)
            }
        }

        GlassStudioCard(theme = theme, highlightBorder = dspOn && (dspSettings.subsonicFilterActive || dspSettings.hpfEnabled || dspSettings.lpfEnabled)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CROSSOVER Y FILTROS DE CORTE", color = theme.silverDetailColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtro Subsónico (Protección Sub)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = dspSettings.subsonicFilterActive,
                        onCheckedChange = { onSubsonicChanged(it, dspSettings.subsonicFrequencyHz) },
                        enabled = dspOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.accentColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.silverDetailColor, checkedTrackColor = theme.primaryColor)
                    )
                }
                if (dspSettings.lpfEnabled) {
                    DspSliderRow("Frecuencia LPF", dspSettings.lpfFrequencyHz, 200f..20000f, "Hz", dspOn, theme, decimals = 0, onValueChange = { onLpfChanged(true, it, dspSettings.lpfSlopeDb) })
                }
            }
        }
    }
}
