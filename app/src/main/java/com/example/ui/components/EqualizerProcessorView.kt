package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoTuneState
import com.example.model.AutoTuneTarget
import com.example.model.CarAudioThemeType
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.ParametricBand
import com.example.model.ParametricFilterType
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.model.EqPresetItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerProcessorView(
    theme: CarAudioThemeType,
    eqSettings: EqualizerSettings,
    onTogglePower: (Boolean) -> Unit,
    onUpdateMasterGain: (Float) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onUpdateBand: (index: Int, gainDb: Float) -> Unit,
    onUpdateBand31: (index: Int, gainDb: Float) -> Unit = { _, _ -> },
    onUpdateParametric: (freqHz: Float, gainDb: Float, q: Float) -> Unit,
    onUpdateParametricBand: (bandId: Int, freqHz: Float, gainDb: Float, q: Float, type: ParametricFilterType, enabled: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onToggleParametricBand: (bandId: Int, enabled: Boolean) -> Unit = { _, _ -> },
    onSelectPreset: (String) -> Unit,
    onResetFlat: () -> Unit,
    eqPresets: List<EqPresetItem> = EqPresetCatalog.presets,
    onSelectPresetItem: (EqPresetItem) -> Unit = {},
    onSaveCustomPreset: (name: String, desc: String) -> Unit = { _, _ -> },
    onDeletePreset: (EqPresetItem) -> Unit = {},
    onRenamePreset: (oldName: String, newName: String) -> Unit = { _, _ -> },
    onExportJson: () -> String = { "" },
    onImportJson: (String) -> Boolean = { false },
    autoTuneState: AutoTuneState = AutoTuneState(),
    onStartAutoTune: () -> Unit = {},
    onSelectAutoTuneTarget: (AutoTuneTarget) -> Unit = {},
    onApplyAutoTuneProposal: () -> Unit = {},
    onDiscardAutoTuneProposal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalFaderScroll = rememberScrollState()
    val horizontalFaderScroll15 = rememberScrollState()

    var activeEqModeTab by remember { mutableIntStateOf(0) } // 0: 31-Bandas, 1: 15-Bandas, 2: Paramétrico, 3: Auto-Tune
    var selectedOctaveFilter by remember { mutableIntStateOf(0) }
    var selectedParametricBandId by remember { mutableIntStateOf(1) }

    // Dialog states for Presets
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf<EqPresetItem?>(null) }
    var renamePresetText by remember { mutableStateOf("") }

    var showExportImportDialog by remember { mutableStateOf(false) }
    var exportImportText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    // Dialog: Save Preset
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Guardar Preset de Ecualización", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce un nombre para guardar la curva actual:")
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Nombre del Preset") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPresetDesc,
                        onValueChange = { newPresetDesc = it },
                        label = { Text("Descripción (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSaveCustomPreset(newPresetName.trim(), newPresetDesc.trim())
                            showSavePresetDialog = false
                            newPresetName = ""
                            newPresetDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Guardar", color = theme.onPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog: Rename Preset
    showRenameDialog?.let { presetToRename ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Renombrar Preset", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nuevo nombre para '${presetToRename.name}':")
                    OutlinedTextField(
                        value = renamePresetText,
                        onValueChange = { renamePresetText = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renamePresetText.isNotBlank()) {
                            onRenamePreset(presetToRename.name, renamePresetText.trim())
                            showRenameDialog = null
                            renamePresetText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Renombrar", color = theme.onPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Dialog: Export / Import JSON
    if (showExportImportDialog) {
        AlertDialog(
            onDismissRequest = { showExportImportDialog = false },
            title = { Text("Exportar / Importar Presets", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Formato JSON para compartir o restaurar presets:")
                    OutlinedTextField(
                        value = exportImportText,
                        onValueChange = { exportImportText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        label = { Text("JSON de Presets") }
                    )
                    importStatusMessage?.let {
                        Text(it, fontSize = 11.sp, color = theme.primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val success = onImportJson(exportImportText)
                            importStatusMessage = if (success) "¡Presets importados con éxito!" else "Error al importar JSON"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                    ) {
                        Text("Importar", color = theme.onPrimaryColor)
                    }
                    Button(
                        onClick = {
                            exportImportText = onExportJson()
                            importStatusMessage = "JSON generado listo para copiar"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838))
                    ) {
                        Text("Generar Export")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportImportDialog = false }) { Text("Cerrar") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Controller Header Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (eqSettings.isEnabled) theme.primaryColor.copy(alpha = 0.25f)
                                else Color.DarkGray.copy(alpha = 0.3f)
                            )
                            .border(
                                1.5.dp,
                                if (eqSettings.isEnabled) theme.primaryColor else Color.Gray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "EQ Activo",
                            tint = if (eqSettings.isEnabled) theme.primaryColor else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "ECUALIZADOR GRÁFICO & PARAMÉTRICO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.textPrimaryColor
                        )
                        Text(
                            text = if (eqSettings.isEnabled) "Activo: ${eqSettings.activePresetName}" else "BYPASS / DESACTIVADO",
                            fontSize = 11.sp,
                            color = if (eqSettings.isEnabled) theme.primaryColor else Color.Gray
                        )
                    }
                }

                Switch(
                    checked = eqSettings.isEnabled,
                    onCheckedChange = onTogglePower,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.primaryColor,
                        checkedTrackColor = theme.primaryColor.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("eq_power_switch")
                )
            }
        }

        // Response Curve Visualizer Canvas
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CURVA DE RESPUESTA DSP (20Hz - 20kHz)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondaryColor
                    )
                    Text(
                        text = "Gain: %+.1f dB".format(eqSettings.masterGainDb),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                EqCurveCanvas31(
                    eq = eqSettings,
                    theme = theme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090A10))
                )
            }
        }

        // PRESETS MANAGEMENT CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRESETS DE ECUALIZACIÓN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Reset Flat Button
                        Button(
                            onClick = onResetFlat,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("FLAT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Save Preset Button
                        Button(
                            onClick = { showSavePresetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = theme.onPrimaryColor)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.onPrimaryColor)
                        }

                        // Export/Import JSON Button
                        IconButton(
                            onClick = {
                                exportImportText = onExportJson()
                                showExportImportDialog = true
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "JSON", tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Preset Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    eqPresets.forEach { preset ->
                        val isSelected = eqSettings.activePresetName == preset.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) theme.primaryColor else Color(0xFF101622))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) theme.primaryColor else Color(0xFF263238),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    onSelectPreset(preset.name)
                                    onSelectPresetItem(preset)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) theme.onPrimaryColor else Color.White
                                )

                                if (preset.isCustom) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Edit / Rename icon
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Renombrar",
                                        tint = if (isSelected) Color.Black else Color.Gray,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                renamePresetText = preset.name
                                                showRenameDialog = preset
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Delete icon
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = if (isSelected) Color.Black else Color(0xFFFF5252),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { onDeletePreset(preset) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigation Tabs: 31 Bandas vs 15 Bandas vs Paramétrico vs Auto-Tune
        TabRow(
            selectedTabIndex = activeEqModeTab,
            containerColor = theme.surfaceColor,
            contentColor = theme.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeEqModeTab]),
                    color = theme.primaryColor
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeEqModeTab == 0,
                onClick = { activeEqModeTab = 0 },
                text = { Text("EQ 31 ISO", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
            Tab(
                selected = activeEqModeTab == 1,
                onClick = { activeEqModeTab = 1 },
                text = { Text("EQ 15 Bandas", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
            Tab(
                selected = activeEqModeTab == 2,
                onClick = { activeEqModeTab = 2 },
                text = { Text("Paramétrico", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
            Tab(
                selected = activeEqModeTab == 3,
                onClick = { activeEqModeTab = 3 },
                text = { Text("Auto-Tune", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
        }

        // TAB 0: 31-BAND ISO GRAPHIC EQUALIZER
        if (activeEqModeTab == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Octave Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf("Todas 31", "Sub/Graves (20-100Hz)", "Medios Bajos (125-500Hz)", "Medios (630-2.5kHz)", "Agudos (3.15k-20kHz)")
                        filters.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = selectedOctaveFilter == idx,
                                onClick = { selectedOctaveFilter = idx },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }
                    }

                    // 31 Band Sliders in Horizontal Scroll
                    val allFreqs = EqualizerSettings.FREQUENCIES_HZ_31
                    val filteredIndices = allFreqs.indices.filter { idx ->
                        val f = allFreqs[idx]
                        when (selectedOctaveFilter) {
                            1 -> f <= 100
                            2 -> f in 125..500
                            3 -> f in 630..2500
                            4 -> f >= 3150
                            else -> true
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalFaderScroll),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredIndices.forEach { bandIdx ->
                            val freqHz = allFreqs[bandIdx]
                            val freqLabel = if (freqHz >= 1000) "${(freqHz / 1000f).toInt()}k" else "${freqHz.toInt()}"
                            val currentGain = eqSettings.bands31.getOrElse(bandIdx) { 0f }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF101018))
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = freqLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = theme.primaryColor
                                )
                                Text(
                                    text = "%+.1fdB".format(currentGain),
                                    fontSize = 9.sp,
                                    color = if (currentGain != 0f) theme.accentColor else theme.textSecondaryColor
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Fader Slider (-15dB to +15dB)
                                Slider(
                                    value = currentGain,
                                    onValueChange = { onUpdateBand31(bandIdx, it) },
                                    valueRange = -15f..15f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (currentGain > 6f) theme.accentColor else theme.primaryColor,
                                        activeTrackColor = if (currentGain > 6f) theme.accentColor else theme.primaryColor,
                                        inactiveTrackColor = Color(0xFF222230)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("eq_band31_$bandIdx")
                                )

                                // Micro-step adjustment buttons: -0.5dB, 0dB reset, +0.5dB
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand31(bandIdx, (currentGain - 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("-", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand31(bandIdx, 0f) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand31(bandIdx, (currentGain + 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("+", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Reset to Flat button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onResetFlat,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Flat (0dB)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // TAB 1: 15-BAND GRAPHIC EQUALIZER
        if (activeEqModeTab == 1) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ECUALIZADOR GRÁFICO DE 15 BANDAS (2/3 OCTAVA)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )

                    val freqs15 = EqualizerSettings.FREQUENCIES_HZ
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalFaderScroll15),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        freqs15.forEachIndexed { bandIdx, freqHz ->
                            val freqLabel = if (freqHz >= 1000) "${(freqHz / 1000f).toInt()}k" else "${freqHz.toInt()}"
                            val currentGain = eqSettings.bands15.getOrElse(bandIdx) { 0f }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF101018))
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = freqLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = theme.primaryColor
                                )
                                Text(
                                    text = "%+.1fdB".format(currentGain),
                                    fontSize = 9.sp,
                                    color = if (currentGain != 0f) theme.accentColor else theme.textSecondaryColor
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Slider(
                                    value = currentGain,
                                    onValueChange = { onUpdateBand(bandIdx, it) },
                                    valueRange = -15f..15f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (currentGain > 6f) theme.accentColor else theme.primaryColor,
                                        activeTrackColor = if (currentGain > 6f) theme.accentColor else theme.primaryColor,
                                        inactiveTrackColor = Color(0xFF263238)
                                    ),
                                    modifier = Modifier.height(130.dp)
                                )

                                // Micro-step buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand(bandIdx, (currentGain - 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("-", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand(bandIdx, 0f) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E2838))
                                            .clickable { onUpdateBand(bandIdx, (currentGain + 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("+", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: 5-BAND PARAMETRIC EQUALIZER
        if (activeEqModeTab == 2) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ECUALIZADOR PARAMÉTRICO DE 5 BANDAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )

                    // Band Selector Tabs (1 to 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        eqSettings.parametricBands.forEach { band ->
                            Button(
                                onClick = { selectedParametricBandId = band.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedParametricBandId == band.id) theme.primaryColor else Color(0xFF20202C)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "B${band.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedParametricBandId == band.id) theme.onPrimaryColor else theme.textPrimaryColor
                                )
                            }
                        }
                    }

                    val selectedBand = eqSettings.parametricBands.find { it.id == selectedParametricBandId } ?: eqSettings.parametricBands.first()

                    // Controls for Selected Band
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF11111A))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Banda ${selectedBand.id}: ${selectedBand.filterType.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                            Switch(
                                checked = selectedBand.enabled,
                                onCheckedChange = { onToggleParametricBand(selectedBand.id, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor)
                            )
                        }

                        // Frequency Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Frecuencia Central (Fc)", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Text(text = "${selectedBand.freqHz.toInt()} Hz", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                        }
                        Slider(
                            value = selectedBand.freqHz,
                            onValueChange = {
                                onUpdateParametricBand(selectedBand.id, it, selectedBand.gainDb, selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                            },
                            valueRange = 20f..20000f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                        )

                        // Gain Slider (-18dB to +18dB)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Ganancia de Banda", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E2838))
                                        .clickable {
                                            onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, (selectedBand.gainDb - 1f).coerceIn(-18f, 18f), selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("-1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E2838))
                                        .clickable {
                                            onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, 0f, selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("0", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E2838))
                                        .clickable {
                                            onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, (selectedBand.gainDb + 1f).coerceIn(-18f, 18f), selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("+1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text(text = "%+.1f dB".format(selectedBand.gainDb), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                            }
                        }
                        Slider(
                            value = selectedBand.gainDb,
                            onValueChange = {
                                onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, it, selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                            },
                            valueRange = -18f..18f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (selectedBand.gainDb > 6f) theme.accentColor else theme.primaryColor,
                                activeTrackColor = if (selectedBand.gainDb > 6f) theme.accentColor else theme.primaryColor
                            )
                        )

                        // Q Factor Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Factor de Calidad (Q / Ancho de Banda)", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Text(text = "Q: %.2f".format(selectedBand.q), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                        }
                        Slider(
                            value = selectedBand.q,
                            onValueChange = {
                                onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, selectedBand.gainDb, it, selectedBand.filterType, selectedBand.enabled)
                            },
                            valueRange = 0.3f..10.0f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                        )
                    }
                }
            }
        }

        // TAB 3: AUTO-TUNE ACÚSTICO CON MICRÓFONO & PANTALLA DE PROPUESTA PREVIA
        if (activeEqModeTab == 3) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTO-TUNE ACÚSTICO REAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PROPUESTA PREVIA REQUERIDA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }

                    Text(
                        text = "Mide la respuesta acústica del habitáculo con el micrófono usando Ruido Rosa y genera una propuesta de ecualización en 31 bandas para tu aprobación antes de aplicarla.",
                        fontSize = 11.sp,
                        color = theme.textSecondaryColor
                    )

                    // Target Curve Selector
                    Text(
                        text = "Seleccionar Curva Objetivo:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimaryColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AutoTuneTarget.entries.forEach { target ->
                            Button(
                                onClick = { onSelectAutoTuneTarget(target) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (autoTuneState.targetCurve == target) theme.primaryColor else Color(0xFF1E1E2A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = target.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoTuneState.targetCurve == target) theme.onPrimaryColor else theme.textPrimaryColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Progress bar if measuring
                    if (autoTuneState.isMeasuring) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Midiendo cabina con Ruido Rosa... (${(autoTuneState.progress * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = theme.primaryColor
                            )
                            LinearProgressIndicator(
                                progress = { autoTuneState.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = theme.primaryColor
                            )
                        }
                    } else {
                        Button(
                            onClick = onStartAutoTune,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_autotune_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = theme.onPrimaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INICIAR MEDICIÓN & CALCULAR PROPUESTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onPrimaryColor
                            )
                        }
                    }

                    // --- PROPOSAL SCREEN (SHOWN BEFORE APPLYING) ---
                    AnimatedVisibility(visible = autoTuneState.hasProposal) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F111A))
                                .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROPUESTA DE AJUSTE GENERADA",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00E676)
                                )
                                Text(
                                    text = "Objetivo: ${autoTuneState.targetCurve.label}",
                                    fontSize = 10.sp,
                                    color = theme.textSecondaryColor
                                )
                            }

                            Text(
                                text = autoTuneState.explanation,
                                fontSize = 11.sp,
                                color = theme.textPrimaryColor
                            )

                            // Comparison Canvas: Measured (Orange/Red) vs Target (Cyan) vs Proposed Correction (Green)
                            Text(
                                text = "Comparativa: Curva Medida (Rojo) • Objetivo (Cian) • Corrección (Verde)",
                                fontSize = 9.sp,
                                color = theme.textSecondaryColor
                            )

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF07070B))
                            ) {
                                val w = size.width
                                val h = size.height
                                val midY = h / 2f

                                drawLine(Color(0xFF222230), Offset(0f, midY), Offset(w, midY))

                                // Draw Measured Curve
                                val measPath = Path()
                                val step = w / 30f
                                autoTuneState.measuredCurve31.forEachIndexed { i, db ->
                                    val y = midY - (db * 0.8f)
                                    val x = i * step
                                    if (i == 0) measPath.moveTo(x, y) else measPath.lineTo(x, y)
                                }
                                drawPath(measPath, Color(0xFFFF5252), style = Stroke(width = 1.5.dp.toPx()))

                                // Draw Target Curve
                                val tgtPath = Path()
                                autoTuneState.targetCurve31.forEachIndexed { i, db ->
                                    val y = midY - (db * 3f)
                                    val x = i * step
                                    if (i == 0) tgtPath.moveTo(x, y) else tgtPath.lineTo(x, y)
                                }
                                drawPath(tgtPath, Color(0xFF00E5FF), style = Stroke(width = 1.5.dp.toPx()))

                                // Draw Proposed Correction Curve
                                val propPath = Path()
                                autoTuneState.proposedCorrection31.forEachIndexed { i, db ->
                                    val y = midY - (db * 3.5f)
                                    val x = i * step
                                    if (i == 0) propPath.moveTo(x, y) else propPath.lineTo(x, y)
                                }
                                drawPath(propPath, Color(0xFF00E676), style = Stroke(width = 2.0.dp.toPx()))
                            }

                            // Action Buttons: Approve & Apply vs Discard
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onApplyAutoTuneProposal,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("apply_autotune_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("APLICAR A EQ 31", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                OutlinedButton(
                                    onClick = onDiscardAutoTuneProposal,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.0f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DESCARTAR", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EqCurveCanvas31(
    eq: EqualizerSettings,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Grid lines
        val dbSteps = listOf(12f to 0.1f, 6f to 0.3f, 0f to 0.5f, -6f to 0.7f, -12f to 0.9f)
        dbSteps.forEach { (_, yRatio) ->
            val y = h * yRatio
            drawLine(
                color = if (yRatio == 0.5f) theme.primaryColor.copy(alpha = 0.4f) else Color(0xFF1A233A),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = if (yRatio == 0.5f) 1.5f else 1f
            )
        }

        if (!eq.isEnabled) {
            drawLine(Color.Gray, Offset(0f, midY), Offset(w, midY), strokeWidth = 2.dp.toPx())
            return@Canvas
        }

        val freqs = EqualizerSettings.FREQUENCIES_HZ_31
        val numPoints = 90
        val minLog = log10(20.0)
        val maxLog = log10(20000.0)

        val curvePath = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, midY)

        for (i in 0..numPoints) {
            val ratio = i.toFloat() / numPoints
            val logFreq = minLog + (maxLog - minLog) * ratio
            val freqHz = 10.0.pow(logFreq).toFloat()

            var totalGain = eq.masterGainDb
            freqs.forEachIndexed { bandIdx, bFreq ->
                val bandGain = eq.bands31.getOrElse(bandIdx) { 0f }
                val octaveDist = abs(log10((freqHz / bFreq).toDouble())) / log10(2.0)
                val weight = (1.0 - octaveDist * 1.5).coerceIn(0.0, 1.0).toFloat()
                totalGain += bandGain * weight
            }

            eq.parametricBands.filter { it.enabled }.forEach { pb ->
                val paramDist = abs(log10((freqHz / pb.freqHz).toDouble())) / log10(2.0)
                val paramWeight = (1.0 - (paramDist * pb.q)).coerceIn(0.0, 1.0).toFloat()
                totalGain += pb.gainDb * paramWeight
            }

            val normY = (1.0f - (totalGain / 15f)) / 2f
            val canvasY = (normY * h).coerceIn(4f, h - 4f)
            val canvasX = ratio * w

            if (i == 0) {
                curvePath.moveTo(canvasX, canvasY)
                fillPath.lineTo(canvasX, canvasY)
            } else {
                curvePath.lineTo(canvasX, canvasY)
                fillPath.lineTo(canvasX, canvasY)
            }
        }

        fillPath.lineTo(w, midY)
        fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(listOf(theme.primaryColor.copy(alpha = 0.25f), Color.Transparent)))
        drawPath(curvePath, Brush.horizontalGradient(listOf(theme.primaryColor, theme.accentColor, Color(0xFF00E5FF))), style = Stroke(width = 2.5.dp.toPx()))
    }
}
