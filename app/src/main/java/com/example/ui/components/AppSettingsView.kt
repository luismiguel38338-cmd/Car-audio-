package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSessionTarget
import com.example.model.CarAudioThemeType
import com.example.model.OutputDeviceStatus
import com.example.service.FloatingEqualizerService

@Composable
fun AppSettingsView(
    theme: CarAudioThemeType,
    autoPlayIntro: Boolean,
    onToggleAutoPlayIntro: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onSelectTheme: (CarAudioThemeType) -> Unit,
    onResetDspDefaults: () -> Unit,
    liteVisuals: Boolean = false,
    onToggleLiteVisuals: (Boolean) -> Unit = {},
    isLowRamDevice: Boolean = false,
    outputDeviceStatus: OutputDeviceStatus = OutputDeviceStatus(),
    sessionTarget: AudioSessionTarget = AudioSessionTarget.GLOBAL_MIX_BEST_EFFORT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    val orderedThemes = remember {
        listOf(CarAudioThemeType.CRIMSON_RED) +
            CarAudioThemeType.entries.filter { it != CarAudioThemeType.CRIMSON_RED }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Restablecer DSP a Fábrica", fontWeight = FontWeight.Bold, color = theme.textColor) },
            text = {
                Text(
                    "Esta acción regresará todas las ganancias, filtros y ecualizadores a valores planos (FLAT).",
                    color = theme.textSecondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDspDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Restablecer Todo", color = theme.onPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rendimiento y Teléfonos Modestos
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primaryColor)
                    Text("RENDIMIENTO Y BATERÍA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Ligero (Teléfonos Modestos)",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isLowRamDevice) {
                                "Dispositivo de poca RAM detectado: optimiza gráficos y espectro para máxima fluidez."
                            } else {
                                "Reduce animaciones y tasa de espectro sin perder calidad de audio DSP."
                            },
                            color = theme.textSecondaryColor,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = liteVisuals,
                        onCheckedChange = onToggleLiteVisuals,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.primaryColor,
                            checkedTrackColor = theme.primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("toggle_lite_visuals_switch")
                    )
                }
            }
        }

        // Compatibilidad Real de Salida y Sesiones Android
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Headphones, contentDescription = null, tint = theme.primaryColor)
                    Text("COMPATIBILIDAD DE AUDIO Y SALIDA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)
                Text(
                    text = "Salida actual: ${outputDeviceStatus.label}",
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Estado AudioFX: ${sessionTarget.label}",
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${sessionTarget.detail}. Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) — " +
                        "En el Reproductor Local el procesamiento DSP es 100% garantizado; en apps externas como YouTube depende de que el fabricante y la app permitan sesiones AudioFX.",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Floating Virtual Screen Quick Setup
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PictureInPicture, contentDescription = null, tint = theme.primaryColor)
                    Text("PANTALLA VIRTUAL FLOTANTE (YOUTUBE)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                Text(
                    text = "Activa el círculo rojo flotante para controlar el ecualizador rápido mientras usas YouTube u otras apps compatibles.",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp
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
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ABRIR BURBUJA FLOTANTE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Intro & Arranque
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = theme.primaryColor)
                    Text("INTRO & ARRANQUE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mostrar Intro al Iniciar", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Animación rápida y sonido de encendido studio.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = autoPlayIntro,
                        onCheckedChange = onToggleAutoPlayIntro,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.primaryColor,
                            checkedTrackColor = theme.primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("toggle_auto_intro_switch")
                    )
                }
                Button(
                    onClick = onReplayIntro,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor, contentColor = theme.textColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replay_intro_button")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER INTRO ROJO STUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Pantalla & Energía
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.BrightnessHigh, contentDescription = null, tint = theme.accentColor)
                    Text("PANTALLA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mantener Pantalla Encendida", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Ideal cuando usas la app conectada al estéreo.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = onToggleKeepScreenOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.primaryColor,
                            checkedTrackColor = theme.primaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("keep_screen_on_switch")
                    )
                }
            }
        }

        // Selección de Estilo Visual
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = theme.primaryColor)
                    Text("ESTILO VISUAL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                orderedThemes.forEach { themeItem ->
                    val isSelected = theme == themeItem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.16f) else theme.backgroundColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.primaryColor else theme.cardColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTheme(themeItem) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(themeItem.primaryColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = themeItem.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primaryColor else theme.textColor
                                )
                                Text(
                                    text = themeItem.subtitle,
                                    fontSize = 10.sp,
                                    color = theme.textSecondaryColor
                                )
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.primaryColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = theme.onPrimaryColor)
                            }
                        }
                    }
                }
            }
        }

        // Restablecer DSP
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = theme.primaryColor)
                    Text("RESTABLECER DSP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primaryColor)
                }
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESTABLECER VALORES FLAT", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Acerca de
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CAR AUDIO DSP PRO",
                    color = theme.primaryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Versión 6.0.0 • Edición Todo Rojo Pro",
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Reproductor local en segundo plano, carátulas, MediaSession y procesador AudioFX de alta precisión.",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
