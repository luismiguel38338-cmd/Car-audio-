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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onUpdateChannelLpf: (Int, Float, Int, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateChannelGain: (Int, Float) -> Unit = { _, _ -> },
    onToggleChannelPhase: (Int) -> Unit = {},
    onUpdateChannelDelay: (Int, Float) -> Unit = { _, _ -> },
    onToggleChannelMute: (Int) -> Unit = {},
    onSaveCustomPreset: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var dspModeTab by remember { mutableIntStateOf(0) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    val presets = listOf(
        "Open Show Pro",
        "SPL Bass Monster",
        "SQL Audiophile Studio",
        "Reggaeton & Urbano",
        "Rock & Metal Punch"
    )

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
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPresetDesc,
                        onValueChange = { newPresetDesc = it },
                        label = { Text("Descripción") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSaveCustomPreset(newPresetName.trim(), newPresetDesc.trim().ifBlank { "Preset de usuario" })
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
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DSP Mode Switcher Tab
        TabRow(
            selectedTabIndex = dspModeTab,
            containerColor = theme.surfaceColor,
            contentColor = theme.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[dspModeTab]),
                    color = theme.primaryColor,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = dspModeTab == 0,
                onClick = { dspModeTab = 0 },
                text = { Text("PROCESADOR MAESTRO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = dspModeTab == 1,
                onClick = { dspModeTab = 1 },
                text = { Text("CROSSOVER 4-VÍAS PRO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (dspModeTab == 1 && dspChannels.isNotEmpty()) {
            MultichannelDspSection(
                theme = theme,
                channels = dspChannels,
                selectedChannelId = selectedChannelId,
                onSelectChannel = onSelectChannel,
                onUpdateHpf = onUpdateChannelHpf,
                onUpdateLpf = onUpdateChannelLpf,
                onUpdateGain = onUpdateChannelGain,
                onTogglePhase = onToggleChannelPhase,
                onUpdateDelay = onUpdateChannelDelay,
                onToggleMute = onToggleChannelMute
            )
        } else {
            // Presets Selector Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dsp_presets_card"),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Presets",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRESETS DE PROCESADOR CAR AUDIO",
                                color = theme.textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showSavePresetDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Guardar Preset",
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = preset == dsp.activePresetName
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectPreset(preset) },
                                label = {
                                    Text(
                                        text = preset,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.surfaceColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }
                }
            }

        // Crossover Section (HPF, LPF, Subsonic)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crossover_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Crossover",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FILTROS CROSSOVER DIGITAL",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HPF (High-Pass Filter)
                CrossoverControlItem(
                    name = "HPF (Paso Alto - Medios y Voces)",
                    freqText = "${dsp.hpfFrequencyHz.toInt()} Hz",
                    slopeText = "${dsp.hpfSlopeDb} dB/oct",
                    sliderValue = dsp.hpfFrequencyHz,
                    range = 20f..250f,
                    enabled = dsp.hpfEnabled,
                    onToggle = { onDspChange(dsp.copy(hpfEnabled = it)) },
                    onValueChange = { onDspChange(dsp.copy(hpfFrequencyHz = it)) },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // LPF (Low-Pass Filter)
                CrossoverControlItem(
                    name = "LPF (Paso Bajo - Subwoofer)",
                    freqText = "${dsp.lpfFrequencyHz.toInt()} Hz",
                    slopeText = "${dsp.lpfSlopeDb} dB/oct",
                    sliderValue = dsp.lpfFrequencyHz,
                    range = 40f..300f,
                    enabled = dsp.lpfEnabled,
                    onToggle = { onDspChange(dsp.copy(lpfEnabled = it)) },
                    onValueChange = { onDspChange(dsp.copy(lpfFrequencyHz = it)) },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subsonic Filter
                CrossoverControlItem(
                    name = "Filtro Subsónico (Cajón Porteado)",
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

        // Bass Engine & Time Alignment Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bass_engine_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Bass Engine",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BASS BOOST & ALINEACIÓN DE TIEMPO",
                        color = theme.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bass Boost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Refuerzo de Graves (Bass Boost)", color = theme.textColor, fontSize = 12.sp)
                    Text(
                        text = "+${"%.1f".format(dsp.bassBoostDb)} dB @ ${dsp.bassBoostFreqHz.toInt()}Hz",
                        color = theme.primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = dsp.bassBoostDb,
                    onValueChange = { onDspChange(dsp.copy(bassBoostDb = it)) },
                    valueRange = 0f..18f,
                    modifier = Modifier.testTag("bass_boost_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primaryColor,
                        activeTrackColor = theme.primaryColor,
                        inactiveTrackColor = theme.surfaceColor
                    )
                )

                // Frequency selection chips for Bass Boost
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frecuencia Central:", color = theme.textSecondaryColor, fontSize = 11.sp)
                    listOf(35f, 45f, 55f).forEach { freq ->
                        val isSelected = dsp.bassBoostFreqHz == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDspChange(dsp.copy(bassBoostFreqHz = freq)) },
                            label = { Text("${freq.toInt()}Hz", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.accentColor,
                                selectedLabelColor = theme.onPrimaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Alignment Delay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Retardo / Alineación (Time Alignment)", color = theme.textColor, fontSize = 12.sp)
                    val distanceCm = (dsp.timeAlignmentMs * 34.3f).toInt()
                    Text(
                        text = "${"%.1f".format(dsp.timeAlignmentMs)} ms ($distanceCm cm)",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = dsp.timeAlignmentMs,
                    onValueChange = { onDspChange(dsp.copy(timeAlignmentMs = it)) },
                    valueRange = 0f..15f,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.accentColor,
                        activeTrackColor = theme.accentColor,
                        inactiveTrackColor = theme.surfaceColor
                    )
                )

                // Phase inverter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fase de Subwoofer (Phase)",
                        color = theme.textColor,
                        fontSize = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onDspChange(dsp.copy(phaseDegrees = 0)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dsp.phaseDegrees == 0) theme.primaryColor else theme.surfaceColor,
                                contentColor = if (dsp.phaseDegrees == 0) theme.onPrimaryColor else theme.textColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("0° (Normal)", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onDspChange(dsp.copy(phaseDegrees = 180)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dsp.phaseDegrees == 180) theme.primaryColor else theme.surfaceColor,
                                contentColor = if (dsp.phaseDegrees == 180) theme.onPrimaryColor else theme.textColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("180° (Invertida)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 8-Band Graphic Equalizer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("graphic_eq_card"),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "EQ",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ECUALIZADOR PARAMÉTRICO (8 BANDAS)",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onDspChange(dsp.copy(eqBands = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.surfaceColor,
                            contentColor = theme.textSecondaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Flat", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plano (0dB)", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val bandLabels = listOf(
                    Pair("35Hz", "Sub"),
                    Pair("80Hz", "Bass"),
                    Pair("160Hz", "Punch"),
                    Pair("400Hz", "L-Mid"),
                    Pair("1kHz", "Voz"),
                    Pair("2.5kHz", "Claridad"),
                    Pair("6.3kHz", "Brillo"),
                    Pair("16kHz", "Agudo")
                )

                bandLabels.forEachIndexed { index, (freq, desc) ->
                    val gain = dsp.eqBands.getOrElse(index) { 0f }
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$freq ($desc)",
                                color = theme.textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${if (gain > 0) "+" else ""}${"%.1f".format(gain)} dB",
                                color = if (gain > 0) theme.primaryColor else if (gain < 0) theme.accentColor else theme.textSecondaryColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = gain,
                            onValueChange = { newVal ->
                                val updated = dsp.eqBands.toMutableList()
                                updated[index] = newVal
                                onDspChange(dsp.copy(eqBands = updated))
                            },
                            valueRange = -12f..12f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.surfaceColor
                            )
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun MultichannelDspSection(
    theme: CarAudioThemeType,
    channels: List<DspChannel>,
    selectedChannelId: Int,
    onSelectChannel: (Int) -> Unit,
    onUpdateHpf: (Int, Float, Int, Boolean) -> Unit,
    onUpdateLpf: (Int, Float, Int, Boolean) -> Unit,
    onUpdateGain: (Int, Float) -> Unit,
    onTogglePhase: (Int) -> Unit,
    onUpdateDelay: (Int, Float) -> Unit,
    onToggleMute: (Int) -> Unit
) {
    val currentChannel = channels.find { it.id == selectedChannelId } ?: channels.firstOrNull() ?: return

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Channel Selector Tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECCIÓN DE VÍA / CANAL ACTIVO",
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
                                labelColor = if (ch.isMuted) theme.errorColor else theme.textColor
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
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.6f), theme.accentColor.copy(alpha = 0.6f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header of Channel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentChannel.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.textColor
                        )
                        Text(
                            text = "Rango de Trabajo: ${currentChannel.typeName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.accentColor
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Mute button
                        Button(
                            onClick = { onToggleMute(currentChannel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentChannel.isMuted) theme.errorColor else theme.cardColor,
                                contentColor = if (currentChannel.isMuted) Color.White else theme.textColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (currentChannel.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentChannel.isMuted) "MUTED" else "ACTIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Phase 0 / 180 button
                        Button(
                            onClick = { onTogglePhase(currentChannel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentChannel.phaseInverted) theme.accentColor else theme.cardColor,
                                contentColor = if (currentChannel.phaseInverted) Color.Black else theme.textColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (currentChannel.phaseInverted) "180°" else "0°", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Time Alignment (Delay)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MoreTime, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Alineación de Tiempo (Time Alignment)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            }
                            val distanceCm = (currentChannel.delayMs * 34.3f).toInt()
                            Text("${"%.1f".format(currentChannel.delayMs)} ms ($distanceCm cm)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryColor)
                        }

                        Slider(
                            value = currentChannel.delayMs,
                            onValueChange = { onUpdateDelay(currentChannel.id, it) },
                            valueRange = 0f..15f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor, inactiveTrackColor = theme.surfaceColor)
                        )
                    }
                }

                // Individual Gain Slider
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ganancia Individual de Salida", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Text("${if (currentChannel.gainDb > 0) "+" else ""}${"%.1f".format(currentChannel.gainDb)} dB", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryColor)
                        }

                        Slider(
                            value = currentChannel.gainDb,
                            onValueChange = { onUpdateGain(currentChannel.id, it) },
                            valueRange = -12f..12f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor, inactiveTrackColor = theme.surfaceColor)
                        )
                    }
                }

                // HPF Cut Filter for this Channel
                CrossoverControlItem(
                    name = "Filtro Paso Alto (HPF)",
                    freqText = "${currentChannel.hpfHz.toInt()} Hz",
                    slopeText = "${currentChannel.hpfSlopeDb} dB/Oct",
                    sliderValue = currentChannel.hpfHz,
                    range = 10f..8000f,
                    enabled = currentChannel.hpfEnabled,
                    onToggle = { onUpdateHpf(currentChannel.id, currentChannel.hpfHz, currentChannel.hpfSlopeDb, it) },
                    onValueChange = { onUpdateHpf(currentChannel.id, it, currentChannel.hpfSlopeDb, currentChannel.hpfEnabled) },
                    theme = theme
                )

                // LPF Cut Filter for this Channel
                CrossoverControlItem(
                    name = "Filtro Paso Bajo (LPF)",
                    freqText = "${currentChannel.lpfHz.toInt()} Hz",
                    slopeText = "${currentChannel.lpfSlopeDb} dB/Oct",
                    sliderValue = currentChannel.lpfHz,
                    range = 40f..20000f,
                    enabled = currentChannel.lpfEnabled,
                    onToggle = { onUpdateLpf(currentChannel.id, currentChannel.lpfHz, currentChannel.lpfSlopeDb, it) },
                    onValueChange = { onUpdateLpf(currentChannel.id, it, currentChannel.lpfSlopeDb, currentChannel.lpfEnabled) },
                    theme = theme
                )
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
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Corte: $freqText • Pendiente: $slopeText",
                    color = theme.primaryColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = theme.onPrimaryColor,
                    checkedTrackColor = theme.primaryColor,
                    uncheckedTrackColor = theme.backgroundColor
                )
            )
        }

        if (enabled) {
            Slider(
                value = sliderValue,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = theme.primaryColor,
                    activeTrackColor = theme.primaryColor,
                    inactiveTrackColor = theme.backgroundColor
                )
            )
        }
    }
}
