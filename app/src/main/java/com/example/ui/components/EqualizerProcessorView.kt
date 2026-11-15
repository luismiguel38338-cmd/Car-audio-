package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType
import com.example.service.FloatingEqualizerService
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun EqualizerProcessorView(
    theme: CarAudioThemeType,
    eqSettings: EqualizerSettings,
    onTogglePower: (Boolean) -> Unit,
    onUpdateMasterGain: (Float) -> Unit,
    onUpdateBand: (index: Int, gainDb: Float) -> Unit,
    onUpdateBand31: (index: Int, gainDb: Float) -> Unit,
    onUpdateParametricBand: (bandId: Int, freqHz: Float, gainDb: Float, q: Float, type: ParametricFilterType, enabled: Boolean) -> Unit,
    onToggleParametricBand: (bandId: Int, enabled: Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onResetFlat: () -> Unit,
    eqPresets: List<EqPresetItem> = EqPresetCatalog.presets,
    onSelectPresetItem: (EqPresetItem) -> Unit = {},
    onSaveCustomPreset: (name: String, desc: String) -> Unit = { _, _ -> },
    onDeletePreset: (EqPresetItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verticalScroll = rememberScrollState()
    val horizontalFaderScroll = rememberScrollState()
    val horizontalFaderScroll15 = rememberScrollState()

    var activeEqModeTab by remember { mutableIntStateOf(0) } // 0: 31-Bandas, 1: 15-Bandas, 2: Paramétrico
    var selectedOctaveFilter by remember { mutableIntStateOf(0) }
    var selectedParametricBandId by remember { mutableIntStateOf(1) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Guardar Preset Rojo Studio", fontWeight = FontWeight.Bold, color = theme.textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nombre de tu curva de ecualización:", color = theme.textSecondaryColor, fontSize = 12.sp)
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
                TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancelar", color = theme.textSecondaryColor) }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(verticalScroll)
            .padding(vertical = 10.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Real-Time YouTube & Global Overlay Banner (Todo Rojo)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TIEMPO REAL: YOUTUBE & MÚSICA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentColor
                        )
                    }
                    Text(
                        text = "v6.0 PRO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )
                }

                Text(
                    text = "Todos los ajustes de bandas y filtros se aplican en tiempo real a tu música cargada. En YouTube y apps externas depende de la sesión compartida que permita Android.",
                    fontSize = 11.sp,
                    color = theme.textSecondaryColor,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = {
                        if (Settings.canDrawOverlays(context)) {
                            FloatingEqualizerService.start(context)
                        } else {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = "Pantalla Virtual Flotante",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVAR PANTALLA FLOTANTE (CÍRCULO ROJO)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // EQ Master Switch + Gain
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (eqSettings.isEnabled) theme.primaryColor.copy(alpha = 0.22f) else theme.cardColor)
                                .border(1.5.dp, if (eqSettings.isEnabled) theme.primaryColor else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "EQ Activo",
                                tint = if (eqSettings.isEnabled) theme.primaryColor else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ECUALIZADOR DE PRECISIÓN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = theme.textColor
                            )
                            Text(
                                text = if (eqSettings.isEnabled) "Activo: ${eqSettings.activePresetName}" else "BYPASS / DESACTIVADO",
                                fontSize = 11.sp,
                                color = if (eqSettings.isEnabled) theme.accentColor else Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = onTogglePower,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.primaryColor,
                            checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("eq_power_switch")
                    )
                }

                // Master EQ Gain Slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ganancia de Ecualizador", fontSize = 11.sp, color = theme.textSecondaryColor)
                    Text("%+.1f dB".format(eqSettings.masterGainDb), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                }
                Slider(
                    value = eqSettings.masterGainDb,
                    onValueChange = onUpdateMasterGain,
                    valueRange = -12f..12f,
                    enabled = eqSettings.isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primaryColor,
                        activeTrackColor = theme.primaryColor,
                        inactiveTrackColor = theme.cardColor
                    ),
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        // Response Curve Canvas (Todo Rojo)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "CURVA DE RESPUESTA EN VIVO (20Hz - 20kHz)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                EqCurveCanvas31(
                    eq = eqSettings,
                    theme = theme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.backgroundColor)
                )
            }
        }

        // Presets Strip
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
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
                        text = "PRESETS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.textColor,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onResetFlat,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("FLAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        }
                        Button(
                            onClick = { showSavePresetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = theme.onPrimaryColor)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.onPrimaryColor)
                        }
                    }
                }

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
                                .background(if (isSelected) theme.primaryColor else theme.cardColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.3f),
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
                                    color = if (isSelected) theme.onPrimaryColor else theme.textColor
                                )
                                if (preset.isCustom && preset.name != "Custom") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = if (isSelected) theme.onPrimaryColor else theme.accentColor,
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clickable { onDeletePreset(preset) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // EQ Tabs: 31 ISO / 15 Bandas / Paramétrico 5B
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
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
        }

        // TAB 0: 31-BAND ISO
        if (activeEqModeTab == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf("Todas 31", "Sub/Graves", "Medios Bajos", "Medios", "Agudos")
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
                            val freqLabel = if (freqHz >= 1000) "${(freqHz / 1000f).toInt()}k" else "$freqHz"
                            val currentGain = eqSettings.bands31.getOrElse(bandIdx) { 0f }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.backgroundColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                                    onValueChange = { onUpdateBand31(bandIdx, it) },
                                    valueRange = -15f..15f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = theme.primaryColor,
                                        activeTrackColor = theme.primaryColor,
                                        inactiveTrackColor = theme.cardColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("eq_band31_$bandIdx")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand31(bandIdx, (currentGain - 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("-", fontSize = 10.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand31(bandIdx, 0f) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand31(bandIdx, (currentGain + 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("+", fontSize = 10.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = onResetFlat,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryColor),
                            border = BorderStroke(1.dp, theme.primaryColor),
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

        // TAB 1: 15-BAND GRAPHIC EQ
        if (activeEqModeTab == 1) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ECUALIZADOR GRÁFICO DE 15 BANDAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    val freqs15 = EqualizerSettings.FREQUENCIES_HZ
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalFaderScroll15),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        freqs15.forEachIndexed { bandIdx, freqHz ->
                            val freqLabel = if (freqHz >= 1000) "${(freqHz / 1000f).toInt()}k" else "$freqHz"
                            val currentGain = eqSettings.bands15.getOrElse(bandIdx) { 0f }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.backgroundColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                                        thumbColor = theme.primaryColor,
                                        activeTrackColor = theme.primaryColor,
                                        inactiveTrackColor = theme.cardColor
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand(bandIdx, (currentGain - 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("-", fontSize = 10.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand(bandIdx, 0f) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(theme.cardColor)
                                            .clickable { onUpdateBand(bandIdx, (currentGain + 0.5f).coerceIn(-15f, 15f)) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("+", fontSize = 10.sp, fontWeight = FontWeight.Black, color = theme.textColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: 5-BAND PARAMETRIC EQ
        if (activeEqModeTab == 2) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ECUALIZADOR PARAMÉTRICO DE 5 BANDAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        eqSettings.parametricBands.forEach { band ->
                            Button(
                                onClick = { selectedParametricBandId = band.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedParametricBandId == band.id) theme.primaryColor else theme.cardColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "B${band.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedParametricBandId == band.id) theme.onPrimaryColor else theme.textColor
                                )
                            }
                        }
                    }

                    val selectedBand = eqSettings.parametricBands.find { it.id == selectedParametricBandId }
                        ?: eqSettings.parametricBands.first()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                            .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Banda ${selectedBand.id}: ${selectedBand.filterType.label}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                            Switch(
                                checked = selectedBand.enabled,
                                onCheckedChange = { onToggleParametricBand(selectedBand.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                )
                            )
                        }

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

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Ganancia de Banda", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Text(text = "%+.1f dB".format(selectedBand.gainDb), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                        }
                        Slider(
                            value = selectedBand.gainDb,
                            onValueChange = {
                                onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, it, selectedBand.q, selectedBand.filterType, selectedBand.enabled)
                            },
                            valueRange = -15f..15f,
                            colors = SliderDefaults.colors(thumbColor = theme.primaryColor, activeTrackColor = theme.primaryColor)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Factor Q", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Text(text = "Q: %.2f".format(selectedBand.q), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.accentColor)
                        }
                        Slider(
                            value = selectedBand.q,
                            onValueChange = {
                                onUpdateParametricBand(selectedBand.id, selectedBand.freqHz, selectedBand.gainDb, it, selectedBand.filterType, selectedBand.enabled)
                            },
                            valueRange = 0.3f..10.0f,
                            colors = SliderDefaults.colors(thumbColor = theme.accentColor, activeTrackColor = theme.accentColor)
                        )
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
    // Precompute normalized gain samples only when EQ values actually change
    val normalizedGains = remember(eq.isEnabled, eq.bands31, eq.parametricBands, eq.masterGainDb) {
        if (!eq.isEnabled) {
            emptyList()
        } else {
            val freqs = EqualizerSettings.FREQUENCIES_HZ_31
            val numPoints = 72
            val minLog = log10(20.0)
            val maxLog = log10(20000.0)
            val log2 = log10(2.0)

            List(numPoints + 1) { i ->
                val ratio = i.toFloat() / numPoints
                val logFreq = minLog + (maxLog - minLog) * ratio
                val freqHz = 10.0.pow(logFreq)

                var totalGain = eq.masterGainDb
                var closestIdx = 0
                var minDist = Double.MAX_VALUE
                for (bIdx in freqs.indices) {
                    val d = abs(log10(freqHz / freqs[bIdx].toDouble()))
                    if (d < minDist) {
                        minDist = d
                        closestIdx = bIdx
                    }
                }
                // Smooth blend around closest ISO 1/3-octave band
                totalGain += eq.bands31.getOrElse(closestIdx) { 0f }

                for (pb in eq.parametricBands) {
                    if (!pb.enabled || abs(pb.gainDb) < 0.05f) continue
                    val octDist = log10(freqHz / pb.freqHz.coerceIn(20f, 20000f).toDouble()) / log2
                    val sigma = (0.65 / pb.q.coerceIn(0.3f, 10.0f)).coerceAtLeast(0.08)
                    val w = when (pb.filterType) {
                        ParametricFilterType.PEAKING -> exp(-0.5 * (abs(octDist) / sigma).pow(2.0))
                        ParametricFilterType.NOTCH -> -exp(-0.5 * (abs(octDist) / (sigma * 0.6)).pow(2.0))
                        ParametricFilterType.LOW_SHELF -> 1.0 / (1.0 + exp(3.2 * pb.q.coerceIn(0.5f, 2.5f) * octDist))
                        ParametricFilterType.HIGH_SHELF -> 1.0 / (1.0 + exp(-3.2 * pb.q.coerceIn(0.5f, 2.5f) * octDist))
                    }
                    totalGain += (pb.gainDb * w).toFloat()
                }

                (1.0f - (totalGain / 15f)) / 2f
            }
        }
    }

    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val gridYs = floatArrayOf(0.1f * h, 0.3f * h, 0.5f * h, 0.7f * h, 0.9f * h)

            val fillBrush = Brush.verticalGradient(
                listOf(theme.primaryColor.copy(alpha = 0.3f), Color.Transparent)
            )
            val strokeBrush = Brush.horizontalGradient(
                listOf(theme.primaryColor, theme.accentColor, theme.primaryColor)
            )

            val curvePath = Path()
            val fillPath = Path()

            if (eq.isEnabled && normalizedGains.isNotEmpty()) {
                fillPath.moveTo(0f, midY)
                val lastIdx = (normalizedGains.size - 1).coerceAtLeast(1)
                normalizedGains.forEachIndexed { i, normY ->
                    val canvasX = (i.toFloat() / lastIdx) * w
                    val canvasY = (normY * h).coerceIn(4f, h - 4f)
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
            }

            val strokeStyle = Stroke(width = 2.6.dp.toPx())
            val bypassWidth = 2.dp.toPx()

            onDrawBehind {
                for (idx in gridYs.indices) {
                    val y = gridYs[idx]
                    val isCenter = idx == 2
                    drawLine(
                        color = if (isCenter) theme.primaryColor.copy(alpha = 0.45f) else theme.cardColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = if (isCenter) 1.5f else 1f
                    )
                }

                if (!eq.isEnabled) {
                    drawLine(Color.Gray, Offset(0f, midY), Offset(w, midY), strokeWidth = bypassWidth)
                } else {
                    drawPath(fillPath, fillBrush)
                    drawPath(curvePath, strokeBrush, style = strokeStyle)
                }
            }
        }
    )
}
