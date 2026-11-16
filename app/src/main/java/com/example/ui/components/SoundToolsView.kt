package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.CustomSoundProfile
import com.example.model.DevicePowerTelemetry
import com.example.model.DspSettings
import com.example.model.RtaBand

@Composable
fun SoundToolsView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    rtaBands: List<RtaBand>,
    leftVu: Float,
    rightVu: Float,
    isClipping: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    isMicRta: Boolean,
    liteVisuals: Boolean,
    powerTelemetry: DevicePowerTelemetry,
    customProfiles: List<CustomSoundProfile>,
    onRefreshPowerTelemetry: () -> Unit,
    onToggleMicRta: () -> Unit,
    onBassGainChanged: (Float) -> Unit,
    onMidGainChanged: (Float) -> Unit,
    onTrebleGainChanged: (Float) -> Unit,
    onPreampChanged: (Float) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onHpfChanged: (Boolean, Float) -> Unit,
    onLpfChanged: (Boolean, Float) -> Unit,
    onLimiterToggle: (Boolean) -> Unit,
    onLimiterCeilingChanged: (Float) -> Unit,
    onSaveCustomProfile: (name: String, description: String) -> Unit,
    onApplyCustomProfile: (CustomSoundProfile) -> Unit,
    onDeleteCustomProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dspOn = dspSettings.dspMasterEnabled
    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileDesc by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Guardar Configuración Completa", fontWeight = FontWeight.Black, color = theme.textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guarda todo tu ajuste actual de EQ, filtros, subwoofer y tweeters:", color = theme.textSecondaryColor, fontSize = 11.5.sp)
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = profileDesc,
                        onValueChange = { profileDesc = it },
                        label = { Text("Nota o vehículo (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (profileName.isNotBlank()) {
                            onSaveCustomProfile(profileName, profileDesc)
                            profileName = ""
                            profileDesc = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Guardar", color = theme.onPrimaryColor, fontWeight = FontWeight.Black)
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
            .testTag("sound_tools_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 5-LED Bar
        item(key = "tools_5led") {
            FiveLedIndicatorBar(
                isAudioPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                title = "HERRAMIENTAS DE SONIDO • 5 LED"
            )
        }

        // 2. Real Android Device Battery / Voltage / Temperature Telemetry Card
        item(key = "tools_power_telemetry") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_power_telemetry_card")
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
                            Icon(
                                imageVector = if (powerTelemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                contentDescription = "Energía del dispositivo",
                                tint = theme.accentColor
                            )
                            Text("MEDIDOR REAL DE BATERÍA, VOLTAJE Y TEMP.", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = onRefreshPowerTelemetry, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar sensores", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryTile(
                            label = "VOLTAJE CEL.",
                            value = powerTelemetry.batteryVoltageV?.let { "%.2f V".format(it) } ?: "N/D",
                            accent = theme.primaryColor,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryTile(
                            label = "BATERÍA",
                            value = powerTelemetry.batteryPercent?.let { "$it%${if (powerTelemetry.isCharging) " ⚡" else ""}" } ?: "N/D",
                            accent = theme.accentColor,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryTile(
                            label = "TEMPERATURA",
                            value = if (powerTelemetry.hasTempSensor && powerTelemetry.batteryTempC != null) {
                                "%.1f °C".format(powerTelemetry.batteryTempC)
                            } else {
                                "Sin sensor"
                            },
                            accent = Color(0xFFF5F9FF),
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.cardColor)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Lectura real del sistema Android (BatteryManager). Para medir 12V/14.4V y temperatura de planta externa se requiere hardware compatible.",
                            color = theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // 3. RTA Spectrum Analyzer + Mic Mode + VU
        item(key = "tools_rta_analyzer") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.4f)),
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
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.primaryColor)
                            Text("ANALIZADOR DE ESPECTRO RTA (31B)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMicRta) theme.accentColor else theme.cardColor)
                                .clickable { onToggleMicRta() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("tools_mic_rta_button")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Micrófono RTA",
                                tint = if (isMicRta) Color(0xFF05101A) else theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMicRta) "MIC ACTIVO" else "MIC RTA",
                                color = if (isMicRta) Color(0xFF05101A) else theme.textColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("L: ${(leftVu * 100).toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondaryColor)
                            LinearProgressIndicator(
                                progress = { leftVu.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = theme.primaryColor,
                                trackColor = theme.cardColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("R: ${(rightVu * 100).toInt()}%", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondaryColor)
                            LinearProgressIndicator(
                                progress = { rightVu.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = theme.accentColor,
                                trackColor = theme.cardColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isClipping) FiveLedColors.RED else theme.cardColor)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("CLIP", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    val step = if (liteVisuals) 2 else 1
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                            .padding(6.dp)
                            .drawWithCache {
                                val barBrush = Brush.verticalGradient(listOf(theme.accentColor, theme.primaryColor))
                                val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                val gap = 2.dp.toPx()
                                onDrawBehind {
                                    val count = ((rtaBands.size + step - 1) / step).coerceAtLeast(1)
                                    val bw = ((size.width - gap * (count - 1)) / count).coerceAtLeast(2f)
                                    var drawIdx = 0
                                    var i = 0
                                    while (i < rtaBands.size) {
                                        val norm = ((rtaBands[i].levelDb + 60f) / 60f).coerceIn(0.05f, 1f)
                                        val bh = size.height * norm
                                        drawRoundRect(
                                            brush = barBrush,
                                            topLeft = Offset(drawIdx * (bw + gap), size.height - bh),
                                            size = Size(bw, bh),
                                            cornerRadius = corner
                                        )
                                        drawIdx++
                                        i += step
                                    }
                                }
                            }
                    )
                }
            }
        }

        // 4. Tonalidad, Preamp, Filtros y Limitador
        item(key = "tools_tone_crossovers") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Text("TONOS, FILTROS DE FRECUENCIA Y LIMITADOR", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    }
                    DspSliderRow("GRAVES (BASS)", dspSettings.bassGainDb, -18f..18f, "dB", dspOn, theme, onBassGainChanged)
                    DspSliderRow("MEDIOS (MID / VOCES)", dspSettings.midGainDb, -18f..18f, "dB", dspOn, theme, onMidGainChanged)
                    DspSliderRow("AGUDOS (TREBLE)", dspSettings.trebleGainDb, -18f..18f, "dB", dspOn, theme, onTrebleGainChanged)
                    DspSliderRow("PREAMP GAIN", dspSettings.preampDb, -18f..18f, "dB", dspOn, theme, onPreampChanged)
                    DspSliderRow("MASTER OUTPUT", dspSettings.masterGainDb, -24f..18f, "dB", true, theme, onMasterGainChanged)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filtro HPF (${dspSettings.hpfFrequencyHz.toInt()} Hz)", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = dspSettings.hpfEnabled,
                            onCheckedChange = { onHpfChanged(it, dspSettings.hpfFrequencyHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor)
                        )
                    }
                    DspSliderRow(
                        label = "CORTE HPF",
                        value = dspSettings.hpfFrequencyHz,
                        range = 20f..400f,
                        unit = "Hz",
                        enabled = dspOn && dspSettings.hpfEnabled,
                        theme = theme,
                        onValueChange = { onHpfChanged(true, it) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filtro LPF (${dspSettings.lpfFrequencyHz.toInt()} Hz)", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = dspSettings.lpfEnabled,
                            onCheckedChange = { onLpfChanged(it, dspSettings.lpfFrequencyHz) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor)
                        )
                    }
                    DspSliderRow(
                        label = "CORTE LPF",
                        value = dspSettings.lpfFrequencyHz,
                        range = 80f..20000f,
                        unit = "Hz",
                        enabled = dspOn && dspSettings.lpfEnabled,
                        theme = theme,
                        onValueChange = { onLpfChanged(true, it) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Limitador Anti-Clip (${dspSettings.limiterCeilingDb} dB)", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = dspSettings.limiterEnabled,
                            onCheckedChange = onLimiterToggle,
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor)
                        )
                    }
                    DspSliderRow("TECHO LIMITADOR", dspSettings.limiterCeilingDb, -18f..0f, "dB", dspOn && dspSettings.limiterEnabled, theme, onLimiterCeilingChanged)
                }
            }
        }

        // 5. Save / Apply Custom Sound Profiles
        item(key = "tools_saved_profiles") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.45f)),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                            Text("CONFIGURACIONES GUARDADAS", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = { showSaveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_custom_profile_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (customProfiles.isEmpty()) {
                        Text(
                            text = "Aún no tienes configuraciones guardadas. Pulsa GUARDAR para almacenar tu curva DSP + EQ actual.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp
                        )
                    } else {
                        customProfiles.forEach { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { onApplyCustomProfile(profile) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(profile.name, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(profile.description, color = theme.textSecondaryColor, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("APLICAR", color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                    IconButton(onClick = { onDeleteCustomProfile(profile.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar perfil", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                                    }
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
private fun TelemetryTile(
    label: String,
    value: String,
    accent: Color,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.cardColor)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = value,
            color = accent,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = theme.textSecondaryColor,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
