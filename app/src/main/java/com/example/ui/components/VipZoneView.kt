package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings

@Composable
fun VipZoneView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    isVipUnlocked: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    onRequestVipDialog: () -> Unit,
    onLockVip: () -> Unit,
    onHiResChanged: (Boolean) -> Unit,
    onTubeWarmthChanged: (Float) -> Unit,
    onPresenceChanged: (Boolean, Float) -> Unit,
    onClarityChanged: (Boolean, Float) -> Unit,
    onExciterChanged: (Boolean, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val dspOn = dspSettings.dspMasterEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("vip_zone_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FiveLedIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "ZONA VIP STUDIO • 5 LED"
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.3.dp, if (isVipUnlocked) theme.accentColor.copy(alpha = 0.7f) else theme.primaryColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor.copy(alpha = 0.18f))
                                .border(1.2.dp, theme.primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                contentDescription = "Estado VIP",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isVipUnlocked) "ACCESO VIP ACTIVO" else "ZONA VIP STUDIO",
                                color = theme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (isVipUnlocked) "Tus mejoras Pro se mantienen activas permanentemente" else "Mejoras de fidelidad HD y módulos avanzados",
                                color = if (isVipUnlocked) theme.accentColor else theme.textSecondaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVipUnlocked) theme.accentColor else theme.cardColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVipUnlocked) "VIP ON" else "BLOQUEADO",
                            color = if (isVipUnlocked) Color(0xFF06150E) else theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (!isVipUnlocked) {
                    val benefits = listOf(
                        "Motor Hi-Res Studio (limpieza en medios-graves y aire HD)",
                        "Calidez Analógica de Bulbos (Tube Warmth 0-10x)",
                        "Presencia Vocal (2-5 kHz), Claridad HD y Excitador Armónico",
                        "Filtros Linkwitz-Riley / Butterworth de hasta 48 dB/oct",
                        "Presets Hairtrick 32Hz, SPL Competencia y SQL Audiófilo",
                        "Compresor multibanda avanzado y temas visuales exclusivos"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        benefits.forEach { line ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                                Text(text = line, color = theme.textColor, fontSize = 11.sp, lineHeight = 14.sp)
                            }
                        }
                    }

                    Button(
                        onClick = onRequestVipDialog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vip_zone_unlock_button")
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INGRESAR CLAVE VIP", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text(
                        text = "Activa o ajusta las etapas exclusivas de calidad sonora; su estado queda guardado al cambiar de pantalla o reiniciar.",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        if (isVipUnlocked) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vip_zone_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PROCESADORES EXCLUSIVOS DE CALIDAD HD", color = theme.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Black)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modo Hi-Res Studio (Curva Audiófila)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Text("Depura frecuencias turbias en 280Hz y realza micro-detalle en agudos", fontSize = 10.sp, color = theme.textSecondaryColor)
                        }
                        Switch(
                            checked = dspSettings.hiResQualityEnabled,
                            onCheckedChange = onHiResChanged,
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("vip_hires_switch")
                        )
                    }

                    DspSliderRow(
                        label = "CALIDEZ DE BULBOS ANALÓGICOS",
                        value = dspSettings.tubeWarmthLevel,
                        range = 0f..10f,
                        unit = "x",
                        enabled = dspOn,
                        theme = theme,
                        onValueChange = onTubeWarmthChanged
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Presencia Vocal (2 - 5 kHz)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Switch(
                            checked = dspSettings.presenceEnabled,
                            onCheckedChange = { onPresenceChanged(it, dspSettings.presenceLevel) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f))
                        )
                    }
                    if (dspSettings.presenceEnabled) {
                        DspSliderRow(
                            label = "NIVEL PRESENCIA VOCAL",
                            value = dspSettings.presenceLevel,
                            range = 0f..10f,
                            unit = "dB",
                            enabled = dspOn,
                            theme = theme,
                            onValueChange = { onPresenceChanged(true, it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Claridad HD (8 - 16 kHz)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Switch(
                            checked = dspSettings.clarityEnabled,
                            onCheckedChange = { onClarityChanged(it, dspSettings.clarityLevel) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.35f))
                        )
                    }
                    if (dspSettings.clarityEnabled) {
                        DspSliderRow(
                            label = "NIVEL CLARIDAD HD",
                            value = dspSettings.clarityLevel,
                            range = 0f..10f,
                            unit = "dB",
                            enabled = dspOn,
                            theme = theme,
                            onValueChange = { onClarityChanged(true, it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Excitador Armónico Digital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Switch(
                            checked = dspSettings.exciterEnabled,
                            onCheckedChange = { onExciterChanged(it, dspSettings.exciterLevel) },
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.35f))
                        )
                    }
                    if (dspSettings.exciterEnabled) {
                        DspSliderRow(
                            label = "INTENSIDAD EXCITADOR",
                            value = dspSettings.exciterLevel,
                            range = 0f..10f,
                            unit = "dB",
                            enabled = dspOn,
                            theme = theme,
                            onValueChange = { onExciterChanged(true, it) }
                        )
                    }

                    OutlinedButton(
                        onClick = onLockVip,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.textSecondaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_lock_vip_button")
                    ) {
                        Text("Bloquear Acceso VIP", color = theme.textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
