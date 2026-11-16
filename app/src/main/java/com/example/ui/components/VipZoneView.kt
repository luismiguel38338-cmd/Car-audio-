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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
import com.example.model.DspSettings

@Composable
fun VipZoneView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    userProfile: CloudUserProfile?,
    isVipUnlocked: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
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
        StudioSignalIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "ZONA VIP • ESTADO DE MEMBRESÍA"
        )

        // Status & Cloud Restoration Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(
                1.2.dp,
                if (isVipUnlocked) theme.accentColor.copy(alpha = 0.65f) else theme.cardColor
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(theme.cardColor)
                                .border(
                                    1.2.dp,
                                    if (isVipUnlocked) theme.accentColor else theme.textSecondaryColor.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                contentDescription = "Estado VIP",
                                tint = if (isVipUnlocked) theme.accentColor else theme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isVipUnlocked) "MEMBRESÍA VIP ACTIVA" else "PLAN GRATUITO DE ESTUDIO",
                                color = theme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = userProfile?.email ?: userProfile?.displayName ?: "Cuenta Google conectada",
                                color = theme.textSecondaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVipUnlocked) theme.accentColor else theme.cardColor)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVipUnlocked) "VIP" else "FREE",
                            color = if (isVipUnlocked) theme.onPrimaryColor else theme.textSecondaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = theme.cardColor)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (isVipUnlocked) {
                            "Tu membresía VIP está verificada en el servidor de Firestore y se restaura automáticamente en todos tus dispositivos al iniciar sesión."
                        } else {
                            "El acceso VIP se asigna desde el servidor asociado a tu cuenta Google y se restaura automáticamente al cambiar de dispositivo."
                        },
                        color = theme.textSecondaryColor,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }

                if (!isVipUnlocked) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Suscripción Google Play Billing",
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "La compra directa desde Google Play se habilitará en la distribución oficial de Play Store. Las funciones gratuitas del reproductor y del Laboratorio de Audio permanecen completamente disponibles.",
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Honest Comparison Matrix: Free vs VIP
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vip_comparison_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "COMPARATIVA GRATUITO VS VIP",
                    color = theme.accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                val matrix = listOf(
                    Triple("Reproductor Media3, Cola, Artistas, Álbumes y Listas", "Incluido", "Incluido"),
                    Triple("Ecualizador 10/31 Bandas, Paramétrico 5B, Bass Boost y Filtros", "Incluido", "Incluido"),
                    Triple("Analizador RTA 31B, Osciloscopio, VU dBFS y Batería mA", "Incluido", "Incluido"),
                    Triple("Sincronización en la Nube Firebase (Perfiles y Listas)", "Incluido", "Incluido"),
                    Triple("Modo Hi-Res Studio (Curva de aire y limpieza en 280 Hz)", "—", "VIP"),
                    Triple("Calidez Analógica, Presencia Vocal, Claridad HD y Excitador", "—", "VIP"),
                    Triple("Presets Master VIP (Estudio Master, Sub-Bass Extremo, Concierto)", "—", "VIP"),
                    Triple("Temas Exclusivos (Obsidiana, Platino Ártico, Acero Nocturno)", "—", "VIP")
                )

                matrix.forEachIndexed { idx, (feature, freeLabel, vipLabel) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (freeLabel == "Incluido") theme.primaryColor else theme.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = feature,
                                color = theme.textColor,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (freeLabel == "Incluido") "GRATIS" else vipLabel,
                            color = if (freeLabel == "Incluido") theme.textSecondaryColor else theme.accentColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (idx < matrix.lastIndex) {
                        HorizontalDivider(color = theme.cardColor.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Real VIP Controls (Enabled only when server entitlement is active)
        if (isVipUnlocked) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vip_zone_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROCESADORES AVANZADOS VIP",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modo Hi-Res Studio", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                            Text("Depura frecuencias turbias en 280 Hz y añade aire superior.", fontSize = 10.sp, color = theme.textSecondaryColor)
                        }
                        Switch(
                            checked = dspSettings.hiResQualityEnabled,
                            onCheckedChange = onHiResChanged,
                            enabled = dspOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("vip_hires_switch")
                        )
                    }

                    DspSliderRow(
                        label = "Calidez Analógica (Tube Warmth)",
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
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                        )
                    }
                    if (dspSettings.presenceEnabled) {
                        DspSliderRow(
                            label = "Nivel Presencia Vocal",
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
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                        )
                    }
                    if (dspSettings.clarityEnabled) {
                        DspSliderRow(
                            label = "Nivel Claridad HD",
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
                            colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                        )
                    }
                    if (dspSettings.exciterEnabled) {
                        DspSliderRow(
                            label = "Intensidad Excitador",
                            value = dspSettings.exciterLevel,
                            range = 0f..10f,
                            unit = "dB",
                            enabled = dspOn,
                            theme = theme,
                            onValueChange = { onExciterChanged(true, it) }
                        )
                    }
                }
            }
        }
    }
}
