package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspCapabilityReport
import com.example.model.DspEngineStatus
import com.example.model.DspSettings
import com.example.model.EqPresetItem
import com.example.model.OutputDeviceStatus

/**
 * Manual Opt-In Driving Mode UI:
 * - Extra-large touch targets (60dp-72dp) for safer quick taps on dashboard mounts.
 * - Zero looping animations to prevent visual distraction.
 * - Explicitly notes that it is an in-app manual mode and NOT official Android Auto.
 */
@Composable
fun DrivingModeView(
    theme: CarAudioThemeType,
    currentTrack: AudioTrackItem?,
    isPlaying: Boolean,
    volumeLevel: Float,
    dspSettings: DspSettings,
    capabilityReport: DspCapabilityReport,
    outputDeviceStatus: OutputDeviceStatus,
    quickPresets: List<EqPresetItem>,
    activePresetName: String,
    drivingKeepScreenOn: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSelectPreset: (EqPresetItem) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onExitDrivingMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onExitDrivingMode()
    }

    val displayedPresets = quickPresets.take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("driving_mode_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explicit Scope Disclaimer Banner (Honest: NOT official Android Auto)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = theme.surfaceColor,
            border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("driving_mode_disclaimer_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Modo Conducción manual",
                    tint = theme.accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo Conducción manual en pantalla — No es Android Auto oficial",
                        color = theme.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Activación manual en cabina. Opera el teléfono únicamente con el vehículo detenido.",
                        color = theme.textSecondaryColor,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        // Status Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartStatusLedBadge(
                status = when {
                    !dspSettings.dspMasterEnabled -> SmartLightStatus.DISABLED
                    capabilityReport.engineStatus == DspEngineStatus.ACTIVE_INTERNAL -> SmartLightStatus.ACTIVE_READY
                    capabilityReport.engineStatus == DspEngineStatus.ACTIVE_EXTERNAL -> SmartLightStatus.AVAILABLE
                    else -> SmartLightStatus.WARNING_LIMIT
                },
                text = if (dspSettings.dspMasterEnabled) capabilityReport.engineStatus.badgeText else "BYPASS",
                theme = theme,
                liteVisuals = true,
                pulse = false
            )
            SmartStatusLedBadge(
                status = if (dspSettings.limiterEnabled) SmartLightStatus.ACTIVE_READY else SmartLightStatus.WARNING_LIMIT,
                text = if (dspSettings.limiterEnabled) "LIMITADOR ON" else "SIN LIMITADOR",
                theme = theme,
                liteVisuals = true,
                pulse = false
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Headphones, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(14.dp))
                Text(
                    text = outputDeviceStatus.kind.displayLabel,
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Current Track Display
        GlassStudioCard(theme = theme, highlightBorder = isPlaying) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = currentTrack?.title ?: "Sin canción en reproducción",
                    color = theme.textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack?.artist ?: "Selecciona una pista desde Mi música antes de conducir",
                    color = theme.textSecondaryColor,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Oversized Transport Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(theme.cardColor)
                    .border(1.2.dp, theme.silverDetailColor.copy(alpha = 0.35f), CircleShape)
                    .clickable { onPrevious() }
                    .testTag("driving_prev_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Canción anterior",
                    tint = theme.textColor,
                    modifier = Modifier.size(34.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor)
                    .border(1.5.dp, theme.accentColor, CircleShape)
                    .clickable { onPlayPause() }
                    .testTag("driving_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = theme.onPrimaryColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(theme.cardColor)
                    .border(1.2.dp, theme.silverDetailColor.copy(alpha = 0.35f), CircleShape)
                    .clickable { onNext() }
                    .testTag("driving_next_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente canción",
                    tint = theme.textColor,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Large Volume Bar
        GlassStudioCard(theme = theme) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOLUMEN MAESTRO",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(volumeLevel * 100).toInt()}%",
                        color = theme.accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onVolumeChange((volumeLevel - 0.10f).coerceIn(0f, 1f)) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("driving_vol_down_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Bajar volumen", tint = theme.textColor)
                    }

                    Slider(
                        value = volumeLevel,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.cardColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("driving_volume_slider")
                    )

                    OutlinedButton(
                        onClick = { onVolumeChange((volumeLevel + 0.10f).coerceIn(0f, 1f)) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("driving_vol_up_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Subir volumen", tint = theme.textColor)
                    }
                }
            }
        }

        // Max 3 Quick EQ Presets
        if (displayedPresets.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PRESETS RÁPIDOS (MÁX. 3)",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayedPresets.forEachIndexed { idx, preset ->
                        val selected = preset.name.equals(activePresetName, ignoreCase = true)
                        Button(
                            onClick = { onSelectPreset(preset) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) theme.primaryColor else theme.cardColor,
                                contentColor = if (selected) theme.onPrimaryColor else theme.textColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("driving_preset_$idx")
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Keep Screen On + Exit
        GlassStudioCard(theme = theme) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = theme.accentColor)
                    Text(
                        text = "Mantener pantalla encendida en cabina",
                        color = theme.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = drivingKeepScreenOn,
                    onCheckedChange = onToggleKeepScreenOn,
                    colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor),
                    modifier = Modifier.testTag("driving_keep_screen_switch")
                )
            }
        }

        Button(
            onClick = onExitDrivingMode,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.cardColor,
                contentColor = theme.textColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(1.dp, theme.accentColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .testTag("driving_exit_button")
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salir de Modo Conducción", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}
