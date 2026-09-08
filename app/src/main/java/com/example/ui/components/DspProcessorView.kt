package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.CrossoverFilterType
import com.example.model.DspChannel
import com.example.model.DspSettings

@Composable
fun DspProcessorView(
    theme: CarAudioThemeType,
    dsp: DspSettings,
    onDspChange: (DspSettings) -> Unit,
    onSelectPreset: (String) -> Unit,
    dspChannels: List<DspChannel> = emptyList(),
    selectedChannelId: Int = 1,
    onSelectChannel: (Int) -> Unit = {},
    onUpdateChannelHpf: (Int, Float, Int, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateChannelHpfPro: (Int, Float, Int, CrossoverFilterType, Boolean) -> Unit = { _, _, _, _, _ -> },
    onUpdateChannelLpf: (Int, Float, Int, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateChannelLpfPro: (Int, Float, Int, CrossoverFilterType, Boolean) -> Unit = { _, _, _, _, _ -> },
    onUpdateChannelGain: (Int, Float) -> Unit = { _, _ -> },
    onToggleChannelPhase: (Int) -> Unit = {},
    onUpdateChannelDelay: (Int, Float) -> Unit = { _, _ -> },
    onToggleChannelMute: (Int) -> Unit = {},
    onToggleChannelSolo: (Int) -> Unit = {},
    onUpdateChannelLimiter: (Int, Float, Float, Float, Boolean) -> Unit = { _, _, _, _, _ -> },
    onCalculateTimeAlignment: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> },
    onPlayPing: () -> Unit = {},
    onExportJson: () -> String = { "" },
    onImportJson: (String) -> Boolean = { false },
    presetHistory: List<String> = emptyList(),
    onSaveCustomPreset: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var dspModeTab by remember { mutableIntStateOf(0) } // 0: 4 Vías Multicanal, 1: Master Crossover & EQ, 2: Time Align & Presets JSON
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showTimeAlignDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var importStatusMsg by remember { mutableStateOf("") }

    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    // Distance States for Time Alignment
    var distSubCm by remember { mutableFloatStateOf(240f) }
    var distKickCm by remember { mutableFloatStateOf(160f) }
    var distMidCm by remember { mutableFloatStateOf(120f) }
    var distTweetCm by remember { mutableFloatStateOf(105f) }

    val presets = listOf(
        "Open Show Pro",
        "SPL Bass Monster",
        "SQL Audiophile Studio",
        "Reggaeton & Urbano",
        "Rock & Metal Punch"
    )

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Guardar Preset Car Audio", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guarda la ecualización y crossovers actuales:", fontSize = 12.sp, color = theme.textSecondaryColor)
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
                            onSaveCustomPreset(newPresetName, newPresetDesc)
                            showSavePresetDialog = false
                            newPresetName = ""
                            newPresetDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Guardar", color = theme.onPrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancelar", color = theme.textColor)
                }
            }
        )
    }

    // Export JSON Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Exportar Perfil DSP en JSON", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copia o comparte la configuración completa del procesador:", fontSize = 11.sp, color = theme.textSecondaryColor)
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Cerrar", color = theme.onPrimaryColor)
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Importar Perfil DSP desde JSON", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pega el texto JSON de configuración del DSP:", fontSize = 11.sp, color = theme.textSecondaryColor)
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )
                    if (importStatusMsg.isNotBlank()) {
                        Text(importStatusMsg, fontSize = 11.sp, color = if (importStatusMsg.contains("OK")) Color(0xFF00E676) else Color(0xFFFF5252))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onImportJson(importJsonText)
                        if (success) {
                            importStatusMsg = "¡Perfil importado con éxito!"
                            showImportDialog = false
                            importJsonText = ""
                        } else {
                            importStatusMsg = "Error: formato JSON no válido."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Cargar Perfil", color = theme.onPrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar", color = theme.textColor)
                }
            }
        )
    }

    // Time Alignment Automatic Dialog
    if (showTimeAlignDialog) {
        AlertDialog(
            onDismissRequest = { showTimeAlignDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Time Alignment Automático", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Introduce las distancias físicas desde la posición del conductor (o punto de escucha) hasta cada componente:",
                        fontSize = 11.sp,
                        color = theme.textSecondaryColor
                    )

                    // Subwoofer Distance
                    Text("Distancia al Subwoofer: ${distSubCm.toInt()} cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                    Slider(value = distSubCm, onValueChange = { distSubCm = it }, valueRange = 50f..400f)

                    // Kick Bass Distance
                    Text("Distancia al Kick Bass / Medios Bajos: ${distKickCm.toInt()} cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                    Slider(value = distKickCm, onValueChange = { distKickCm = it }, valueRange = 30f..300f)

                    // Driver / Horn Distance
                    Text("Distancia a Driver / Corneta: ${distMidCm.toInt()} cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                    Slider(value = distMidCm, onValueChange = { distMidCm = it }, valueRange = 30f..250f)

                    // Tweeter Distance
                    Text("Distancia a Super Tweeter: ${distTweetCm.toInt()} cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                    Slider(value = distTweetCm, onValueChange = { distTweetCm = it }, valueRange = 30f..250f)

                    Button(
                        onClick = onPlayPing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282835)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Emitir Ping Acústico de Comprobación", fontSize = 10.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCalculateTimeAlignment(distSubCm, distKickCm, distMidCm, distTweetCm)
                        showTimeAlignDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Calcular y Aplicar", color = theme.onPrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeAlignDialog = false }) {
                    Text("Cerrar", color = theme.textColor)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Gain & DSP Power Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dsp_master_gain_card"),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.primaryColor, theme.accentColor)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "PROCESADOR DSP 2.0 (4-VÍAS)", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text(text = "Preset Activo: ${dsp.activePresetName}", color = theme.textSecondaryColor, fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = {
                            exportedJsonText = onExportJson()
                            showExportDialog = true
                        }) {
                            Icon(Icons.Default.Upload, contentDescription = "Exportar JSON", tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(Icons.Default.Download, contentDescription = "Importar JSON", tint = theme.accentColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Master Gain Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ganancia Master DSP", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${if (dsp.masterGainDb > 0) "+" else ""}${"%.1f".format(dsp.masterGainDb)} dB",
                        color = theme.primaryColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = dsp.masterGainDb,
                    onValueChange = { onDspChange(dsp.copy(masterGainDb = it)) },
                    valueRange = -18f..18f,
                    colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor, inactiveTrackColor = theme.cardColor)
                )
            }
        }

        // Sub-Navigation Tabs
        TabRow(
            selectedTabIndex = dspModeTab,
            containerColor = theme.surfaceColor,
            contentColor = theme.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[dspModeTab]),
                    color = theme.primaryColor
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = dspModeTab == 0,
                onClick = { dspModeTab = 0 },
                text = { Text("4 Vías Multicanal", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = dspModeTab == 1,
                onClick = { dspModeTab = 1 },
                text = { Text("Master Crossover", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = dspModeTab == 2,
                onClick = { dspModeTab = 2 },
                text = { Text("Time Align & JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // TAB 0: 4-WAY MULTICHANNEL DSP STRIP
        if (dspModeTab == 0 && dspChannels.isNotEmpty()) {
            MultichannelDspSectionPro(
                theme = theme,
                channels = dspChannels,
                selectedChannelId = selectedChannelId,
                onSelectChannel = onSelectChannel,
                onUpdateHpf = onUpdateChannelHpf,
                onUpdateHpfPro = onUpdateChannelHpfPro,
                onUpdateLpf = onUpdateChannelLpf,
                onUpdateLpfPro = onUpdateChannelLpfPro,
                onUpdateGain = onUpdateChannelGain,
                onTogglePhase = onToggleChannelPhase,
                onUpdateDelay = onUpdateChannelDelay,
                onToggleMute = onToggleChannelMute,
                onToggleSolo = onToggleChannelSolo,
                onUpdateLimiter = onUpdateChannelLimiter,
                onOpenTimeAlignCalculator = { showTimeAlignDialog = true }
            )
        } else if (dspModeTab == 1) {
            // Master Crossover Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "FILTROS CROSSOVER MASTER", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    CrossoverControlItem(
                        name = "HPF Paso Alto Master",
                        freqText = "${dsp.hpfFrequencyHz.toInt()} Hz",
                        slopeText = "${dsp.hpfSlopeDb} dB/oct",
                        sliderValue = dsp.hpfFrequencyHz,
                        range = 20f..250f,
                        enabled = dsp.hpfEnabled,
                        onToggle = { onDspChange(dsp.copy(hpfEnabled = it)) },
                        onValueChange = { onDspChange(dsp.copy(hpfFrequencyHz = it)) },
                        theme = theme
                    )

                    CrossoverControlItem(
                        name = "LPF Paso Bajo Master",
                        freqText = "${dsp.lpfFrequencyHz.toInt()} Hz",
                        slopeText = "${dsp.lpfSlopeDb} dB/oct",
                        sliderValue = dsp.lpfFrequencyHz,
                        range = 40f..300f,
                        enabled = dsp.lpfEnabled,
                        onToggle = { onDspChange(dsp.copy(lpfEnabled = it)) },
                        onValueChange = { onDspChange(dsp.copy(lpfFrequencyHz = it)) },
                        theme = theme
                    )

                    CrossoverControlItem(
                        name = "Filtro Subsónico de Seguridad",
                        freqText = "${dsp.subsonicFrequencyHz.toInt()} Hz",
                        slopeText = "24 dB/oct",
                        sliderValue = dsp.subsonicFrequencyHz,
                        range = 10f..50f,
                        enabled = dsp.subsonicEnabled,
                        onToggle = { onDspChange(dsp.copy(subsonicEnabled = it)) },
                        onValueChange = { onDspChange(dsp.copy(subsonicFrequencyHz = it)) },
                        theme = theme
                    )
                }
            }
        } else if (dspModeTab == 2) {
            // Time Alignment & JSON Presets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ALINEACIÓN DE TIEMPO & PRESETS JSON", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showTimeAlignDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Calculadora Acústica", fontSize = 10.sp, color = theme.onPrimaryColor)
                        }
                    }

                    // Presets chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { p ->
                            FilterChip(
                                selected = p == dsp.activePresetName,
                                onClick = { onSelectPreset(p) },
                                label = { Text(p, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                exportedJsonText = onExportJson()
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar JSON", fontSize = 10.sp)
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importar JSON", fontSize = 10.sp)
                        }
                    }

                    // Preset History
                    if (presetHistory.isNotEmpty()) {
                        Text("Historial de Presets Guardados / Cargados:", fontSize = 10.sp, color = theme.textSecondaryColor)
                        presetHistory.take(4).forEach { item ->
                            Text("• $item", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = theme.textPrimaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultichannelDspSectionPro(
    theme: CarAudioThemeType,
    channels: List<DspChannel>,
    selectedChannelId: Int,
    onSelectChannel: (Int) -> Unit,
    onUpdateHpf: (Int, Float, Int, Boolean) -> Unit,
    onUpdateHpfPro: (Int, Float, Int, CrossoverFilterType, Boolean) -> Unit,
    onUpdateLpf: (Int, Float, Int, Boolean) -> Unit,
    onUpdateLpfPro: (Int, Float, Int, CrossoverFilterType, Boolean) -> Unit,
    onUpdateGain: (Int, Float) -> Unit,
    onTogglePhase: (Int) -> Unit,
    onUpdateDelay: (Int, Float) -> Unit,
    onToggleMute: (Int) -> Unit,
    onToggleSolo: (Int) -> Unit,
    onUpdateLimiter: (Int, Float, Float, Float, Boolean) -> Unit,
    onOpenTimeAlignCalculator: () -> Unit
) {
    val currentChannel = channels.find { it.id == selectedChannelId } ?: channels.firstOrNull() ?: return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Channel Selector Tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CANAL SELECCIONADO PARA AJUSTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(channels) { ch ->
                        val isSelected = ch.id == selectedChannelId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectChannel(ch.id) },
                            label = {
                                Text(
                                    text = ch.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryColor,
                                selectedLabelColor = theme.onPrimaryColor,
                                containerColor = theme.surfaceColor,
                                labelColor = if (ch.isMuted) Color(0xFFFF5252) else theme.textColor
                            )
                        )
                    }
                }
            }
        }

        // Active Channel Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.6f), theme.accentColor.copy(alpha = 0.6f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Channel Top Strip with Mute, Solo & Phase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = currentChannel.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                        Text(text = "Vía: ${currentChannel.typeName}", fontSize = 11.sp, color = theme.accentColor)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Solo button
                        Button(
                            onClick = { onToggleSolo(currentChannel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentChannel.isSolo) Color(0xFFFFEA00) else Color(0xFF282835)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("SOLO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentChannel.isSolo) Color.Black else Color.White)
                        }

                        // Mute button
                        Button(
                            onClick = { onToggleMute(currentChannel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentChannel.isMuted) Color(0xFFFF1744) else Color(0xFF282835)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (currentChannel.isMuted) "MUTED" else "MUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Phase 0/180
                        Button(
                            onClick = { onTogglePhase(currentChannel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentChannel.phaseInverted) theme.accentColor else Color(0xFF282835)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (currentChannel.phaseInverted) "180°" else "0°", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentChannel.phaseInverted) Color.Black else Color.White)
                        }
                    }
                }

                // Individual Gain Slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ganancia Individual Canal", fontSize = 11.sp, color = theme.textColor)
                    Text("${if (currentChannel.gainDb > 0) "+" else ""}${"%.1f".format(currentChannel.gainDb)} dB", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                }
                Slider(
                    value = currentChannel.gainDb,
                    onValueChange = { onUpdateGain(currentChannel.id, it) },
                    valueRange = -18f..12f,
                    colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                )

                // Time Alignment (Delay)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Delay / Time Alignment", fontSize = 11.sp, color = theme.textColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(Calc)", fontSize = 9.sp, color = theme.primaryColor, modifier = Modifier.clickable { onOpenTimeAlignCalculator() })
                    }
                    Text("${"%.1f".format(currentChannel.delayMs)} ms (${currentChannel.delayCm.toInt()} cm)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                }
                Slider(
                    value = currentChannel.delayMs,
                    onValueChange = { onUpdateDelay(currentChannel.id, it) },
                    valueRange = 0f..25f,
                    colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                )

                // HPF Control
                CrossoverControlItem(
                    name = "HPF Paso Alto (Corte Bajo)",
                    freqText = "${currentChannel.hpfHz.toInt()} Hz",
                    slopeText = "${currentChannel.hpfSlopeDb} dB (${currentChannel.hpfFilterType.name})",
                    sliderValue = currentChannel.hpfHz,
                    range = 10f..8000f,
                    enabled = currentChannel.hpfEnabled,
                    onToggle = { onUpdateHpf(currentChannel.id, currentChannel.hpfHz, currentChannel.hpfSlopeDb, it) },
                    onValueChange = { onUpdateHpf(currentChannel.id, it, currentChannel.hpfSlopeDb, currentChannel.hpfEnabled) },
                    theme = theme
                )

                // LPF Control
                CrossoverControlItem(
                    name = "LPF Paso Bajo (Corte Alto)",
                    freqText = "${currentChannel.lpfHz.toInt()} Hz",
                    slopeText = "${currentChannel.lpfSlopeDb} dB (${currentChannel.lpfFilterType.name})",
                    sliderValue = currentChannel.lpfHz,
                    range = 40f..20000f,
                    enabled = currentChannel.lpfEnabled,
                    onToggle = { onUpdateLpf(currentChannel.id, currentChannel.lpfHz, currentChannel.lpfSlopeDb, it) },
                    onValueChange = { onUpdateLpf(currentChannel.id, it, currentChannel.lpfSlopeDb, currentChannel.lpfEnabled) },
                    theme = theme
                )

                // Per-Channel Limiter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF101018))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIMITER POR CANAL (PROTECCIÓN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        }

                        Switch(
                            checked = currentChannel.limiterEnabled,
                            onCheckedChange = {
                                onUpdateLimiter(
                                    currentChannel.id,
                                    currentChannel.limiterThresholdDb,
                                    currentChannel.limiterAttackMs,
                                    currentChannel.limiterReleaseMs,
                                    it
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor)
                        )
                    }

                    if (currentChannel.limiterEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Threshold (Umbral)", fontSize = 10.sp, color = theme.textSecondaryColor)
                            Text("${"%.1f".format(currentChannel.limiterThresholdDb)} dBFS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                        }
                        Slider(
                            value = currentChannel.limiterThresholdDb,
                            onValueChange = {
                                onUpdateLimiter(currentChannel.id, it, currentChannel.limiterAttackMs, currentChannel.limiterReleaseMs, true)
                            },
                            valueRange = -24f..0f
                        )

                        if (currentChannel.gainReductionDb > 0.1f) {
                            Text(
                                "Reducción de Ganancia: -${"%.1f".format(currentChannel.gainReductionDb)} dB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFEA00)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrossoverControlItem(
    name: String,
    freqText: String,
    slopeText: String,
    sliderValue: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    theme: CarAudioThemeType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "Corte: $freqText • $slopeText", color = theme.primaryColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = theme.onPrimaryColor, checkedTrackColor = theme.primaryColor, uncheckedTrackColor = theme.backgroundColor)
            )
        }

        if (enabled) {
            Slider(
                value = sliderValue,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor, inactiveTrackColor = theme.backgroundColor)
            )
        }
    }
}
