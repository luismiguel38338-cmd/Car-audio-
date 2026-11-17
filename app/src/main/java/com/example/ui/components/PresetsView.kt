package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbSlot
import com.example.model.CarAudioThemeType
import com.example.model.CustomSoundProfile
import com.example.model.DspAbSnapshot
import com.example.model.EqPresetItem

@Composable
fun PresetsView(
    theme: CarAudioThemeType,
    presets: List<EqPresetItem>,
    activePresetName: String,
    customProfiles: List<CustomSoundProfile>,
    abSlotA: DspAbSnapshot?,
    abSlotB: DspAbSnapshot?,
    activeAbSlot: AbSlot?,
    onSelectPreset: (EqPresetItem) -> Unit,
    onDeletePreset: (EqPresetItem) -> Unit,
    onSaveSoundProfile: (name: String, desc: String) -> Unit,
    onApplySoundProfile: (CustomSoundProfile) -> Unit,
    onDeleteSoundProfile: (String) -> Unit,
    onCaptureAbSlot: (AbSlot) -> Unit,
    onApplyAbSlot: (AbSlot) -> Unit,
    onToggleAbComparison: () -> Unit,
    onClearAbSlot: (AbSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileDesc by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Guardar Perfil DSP Automotriz", color = theme.textColor, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Guarda tu curva de 31/10 bandas, refuerzo de subwoofer, filtros y balance en el teléfono.",
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
                        label = { Text("Descripción (ej. Cabina Vehículo / Sub 12\")") },
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("presets_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Detailed A / B Comparison Studio Card
        item(key = "presets_ab_detailed") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.2.dp, theme.accentColor.copy(alpha = 0.45f)),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = theme.accentColor)
                            Column {
                                Text("Comparador A / B", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Alterna al instante entre dos calibraciones DSP", color = theme.textSecondaryColor, fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = onToggleAbComparison,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                            modifier = Modifier.testTag("presets_ab_swap_button")
                        ) {
                            Text("A ↔ B", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AbSlotDetailCard(
                            slot = AbSlot.A,
                            snapshot = abSlotA,
                            isActive = activeAbSlot == AbSlot.A,
                            theme = theme,
                            onCapture = { onCaptureAbSlot(AbSlot.A) },
                            onApply = { onApplyAbSlot(AbSlot.A) },
                            onClear = { onClearAbSlot(AbSlot.A) },
                            modifier = Modifier.weight(1f)
                        )
                        AbSlotDetailCard(
                            slot = AbSlot.B,
                            snapshot = abSlotB,
                            isActive = activeAbSlot == AbSlot.B,
                            theme = theme,
                            onCapture = { onCaptureAbSlot(AbSlot.B) },
                            onApply = { onApplyAbSlot(AbSlot.B) },
                            onClear = { onClearAbSlot(AbSlot.B) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Save Current Configuration Button
        item(key = "presets_save_button") {
            Button(
                onClick = { showSaveDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("presets_save_profile_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Configuración Actual", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }

        // 3. Saved Custom Sound Profiles
        if (customProfiles.isNotEmpty()) {
            item(key = "presets_custom_header") {
                Text(
                    text = "Mis perfiles guardados (${customProfiles.size})",
                    color = theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(customProfiles, key = { it.id }) { prof ->
                val active = activePresetName == prof.name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplySoundProfile(prof) }
                        .testTag("custom_profile_${prof.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, if (active) theme.accentColor else theme.cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prof.name, color = if (active) theme.accentColor else theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(prof.description, color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                        IconButton(onClick = { onDeleteSoundProfile(prof.id) }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar perfil", tint = theme.textSecondaryColor)
                        }
                    }
                }
            }
        }

        // 4. Built-in Automotive & Studio Presets
        item(key = "presets_catalog_header") {
            Text(
                text = "Presets calibrados (${presets.size})",
                color = theme.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(presets, key = { it.name }) { preset ->
            val selected = activePresetName == preset.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPreset(preset) }
                    .testTag("eq_preset_${preset.name}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, if (selected) theme.accentColor else theme.cardColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (selected) theme.accentColor else theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = preset.name,
                                color = if (selected) theme.accentColor else theme.textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(preset.description, color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                    }
                    if (preset.isCustom && preset.name != "Custom") {
                        IconButton(onClick = { onDeletePreset(preset) }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar preset", tint = theme.textSecondaryColor)
                        }
                    } else if (selected) {
                        Text("ACTIVO", color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun AbSlotDetailCard(
    slot: AbSlot,
    snapshot: DspAbSnapshot?,
    isActive: Boolean,
    theme: CarAudioThemeType,
    onCapture: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) theme.accentColor.copy(alpha = 0.12f) else theme.backgroundColor)
            .border(1.dp, if (isActive) theme.accentColor else theme.cardColor, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SLOT ${slot.badge}", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            if (snapshot != null) {
                Text(
                    text = "Limpiar",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { onClear() }
                )
            }
        }

        if (snapshot == null) {
            Text("Sin guardar", color = theme.textSecondaryColor, fontSize = 11.sp)
        } else {
            Text(snapshot.name, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "Bass +${snapshot.dspSettings.bassBoostDb.toInt()}dB • Pre ${snapshot.dspSettings.preampDb.toInt()}dB",
                color = theme.textSecondaryColor,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = onCapture,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, theme.cardColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Fijar", color = theme.textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            if (snapshot != null) {
                Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor, contentColor = theme.onPrimaryColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isActive) "Activo" else "Usar", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
