package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.CustomSoundProfile
import com.example.model.DspCapabilityReport
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType

@Composable
fun GraphicEqTab(
    theme: CarAudioThemeType,
    bands: List<Float>,
    labels: List<String>,
    enabled: Boolean,
    is31BandMode: Boolean,
    capabilityReport: DspCapabilityReport,
    onBandChanged: (Int, Float) -> Unit,
    onResetFlat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(1.dp, theme.cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (is31BandMode) "ECUALIZADOR 31 BANDAS ISO (1/3 OCT)" else "ECUALIZADOR 10 BANDAS ISO",
                        color = theme.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (is31BandMode) {
                            if (capabilityReport.dynamicsProcessingActive) {
                                "Hardware DynamicsProcessing 31B activo"
                            } else {
                                "Modo compatibilidad: mapeado a ${capabilityReport.equalizerBandCount.coerceAtLeast(5)} bandas hardware"
                            }
                        } else {
                            "Control rápido de 31 Hz a 16 kHz (-15 a +15 dB)"
                        },
                        color = theme.accentColor,
                        fontSize = 10.sp
                    )
                }
                OutlinedButton(
                    onClick = onResetFlat,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, theme.cardColor)
                ) {
                    Text("Plano 0 dB", color = theme.textColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.backgroundColor)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(if (is31BandMode) 6.dp else 10.dp)
            ) {
                bands.forEachIndexed { idx, gain ->
                    val label = labels.getOrElse(idx) { "${idx + 1}" }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .width(if (is31BandMode) 36.dp else 44.dp)
                            .testTag(if (is31BandMode) "eq31_band_$idx" else "eq10_band_$idx")
                    ) {
                        Text(
                            text = "%+.1f".format(gain),
                            color = if (gain > 0.2f || gain < -0.2f) theme.accentColor else theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Slider(
                            value = gain.coerceIn(-15f, 15f),
                            onValueChange = { onBandChanged(idx, it) },
                            valueRange = -15f..15f,
                            enabled = enabled,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.accentColor,
                                inactiveTrackColor = theme.cardColor
                            ),
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = 270f
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                }
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(
                                        Constraints(
                                            minWidth = constraints.minHeight,
                                            maxWidth = constraints.maxHeight,
                                            minHeight = constraints.minWidth,
                                            maxHeight = constraints.maxWidth
                                        )
                                    )
                                    layout(placeable.height, placeable.width) {
                                        placeable.place(
                                            x = -(placeable.width - placeable.height) / 2,
                                            y = -(placeable.height - placeable.width) / 2
                                        )
                                    }
                                }
                                .width(150.dp)
                                .height(34.dp)
                        )

                        Text(
                            text = label,
                            color = theme.textColor,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParametricEqTab(
    theme: CarAudioThemeType,
    eqSettings: EqualizerSettings,
    dspMasterEnabled: Boolean,
    onUpdateBand: (id: Int, freq: Float, gain: Float, q: Float, type: ParametricFilterType, enabled: Boolean) -> Unit,
    onToggleBand: (id: Int, enabled: Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        eqSettings.parametricBands.forEach { pb ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, if (pb.enabled) theme.accentColor.copy(alpha = 0.45f) else theme.cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BANDA ${pb.id} • ${pb.filterType.label}",
                            color = theme.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = pb.enabled,
                            onCheckedChange = { onToggleBand(pb.id, it) },
                            enabled = dspMasterEnabled && eqSettings.isEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ParametricFilterType.entries.forEach { fType ->
                            FilterChip(
                                selected = pb.filterType == fType,
                                onClick = { onUpdateBand(pb.id, pb.freqHz, pb.gainDb, pb.q, fType, pb.enabled) },
                                label = { Text(fType.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.accentColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.backgroundColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }

                    val bandActive = dspMasterEnabled && eqSettings.isEnabled && pb.enabled
                    DspSliderRow(
                        label = "Frecuencia Central",
                        value = pb.freqHz,
                        range = 20f..18000f,
                        unit = "Hz",
                        decimals = 0,
                        enabled = bandActive,
                        theme = theme,
                        onValueChange = { onUpdateBand(pb.id, it, pb.gainDb, pb.q, pb.filterType, pb.enabled) }
                    )
                    DspSliderRow(
                        label = "Ganancia",
                        value = pb.gainDb,
                        range = -15f..15f,
                        unit = "dB",
                        enabled = bandActive,
                        theme = theme,
                        onValueChange = { onUpdateBand(pb.id, pb.freqHz, it, pb.q, pb.filterType, pb.enabled) }
                    )
                    DspSliderRow(
                        label = "Factor Q (Ancho de banda)",
                        value = pb.q,
                        range = 0.4f..8.0f,
                        unit = "Q",
                        enabled = bandActive,
                        theme = theme,
                        onValueChange = { onUpdateBand(pb.id, pb.freqHz, pb.gainDb, it, pb.filterType, pb.enabled) }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetsAndCloudTab(
    theme: CarAudioThemeType,
    presets: List<EqPresetItem>,
    activePresetName: String,
    customProfiles: List<CustomSoundProfile>,
    isVipUnlocked: Boolean,
    cloudSyncActive: Boolean,
    onSelectPreset: (EqPresetItem) -> Unit,
    onDeletePreset: (EqPresetItem) -> Unit,
    onSaveSoundProfile: (name: String, desc: String) -> Unit,
    onApplySoundProfile: (CustomSoundProfile) -> Unit,
    onDeleteSoundProfile: (String) -> Unit,
    onOpenVipZone: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileDesc by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Guardar Configuración DSP", color = theme.textColor, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Guarda todas tus bandas EQ, bajos, balance y filtros en el dispositivo y en tu nube Firestore.",
                        color = theme.textSecondaryColor,
                        fontSize = 11.5.sp
                    )
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Nombre del perfil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = profileDesc,
                        onValueChange = { profileDesc = it },
                        label = { Text("Descripción (ej. Auriculares Estudio)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (profileName.isNotBlank()) {
                            onSaveSoundProfile(profileName.trim(), profileDesc.trim())
                            profileName = ""
                            profileDesc = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { showSaveDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audiolab_save_cloud_profile_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Configuración Actual (Local y Nube)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (customProfiles.isNotEmpty()) {
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
                        Text("PERFILES GUARDADOS", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        if (cloudSyncActive) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(14.dp))
                                Text("Firestore", color = theme.accentColor, fontSize = 10.sp)
                            }
                        }
                    }

                    customProfiles.forEach { prof ->
                        val active = activePresetName == prof.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) theme.accentColor.copy(alpha = 0.14f) else theme.backgroundColor)
                                .border(1.dp, if (active) theme.accentColor else theme.cardColor, RoundedCornerShape(10.dp))
                                .clickable { onApplySoundProfile(prof) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prof.name, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Text(prof.description, color = theme.textSecondaryColor, fontSize = 10.5.sp)
                            }
                            IconButton(onClick = { onDeleteSoundProfile(prof.id) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar perfil", tint = theme.textSecondaryColor, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

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
                Text("PRESETS DE ESTUDIO", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                presets.forEach { preset ->
                    val selected = activePresetName == preset.name
                    val locked = preset.isVip && !isVipUnlocked
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) theme.accentColor.copy(alpha = 0.14f) else theme.backgroundColor)
                            .border(1.dp, if (selected) theme.accentColor else theme.cardColor, RoundedCornerShape(10.dp))
                            .clickable {
                                if (locked) onOpenVipZone() else onSelectPreset(preset)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("eq_preset_${preset.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(preset.name, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                if (preset.isVip) {
                                    Text("VIP", color = theme.accentColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(preset.description, color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                        when {
                            locked -> Icon(Icons.Default.Lock, contentDescription = "Preset VIP", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                            preset.isCustom && preset.name != "Custom" -> {
                                IconButton(onClick = { onDeletePreset(preset) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar preset", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
