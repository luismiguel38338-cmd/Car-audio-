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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType

@Composable
fun AppSettingsView(
    theme: CarAudioThemeType,
    autoPlayIntro: Boolean,
    onToggleAutoPlayIntro: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    splOffsetDb: Float,
    onUpdateSplOffset: (Float) -> Unit,
    onSelectTheme: (CarAudioThemeType) -> Unit,
    onResetDspDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restablecer DSP a Fábrica", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Esta acción restablecerá todas las ganancias, crossovers, filtros paramétricos y ecualizadores a valores planos (FLAT / CERO)."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDspDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00))
                ) {
                    Text("Restablecer Todo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
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
        // Section: Video / Splash Intro Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = theme.primaryColor
                    )
                    Text(
                        text = "INTRO & ARRANQUE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color(0xFF263238), thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reproducir Intro al Iniciar",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Muestra la animación e intro cinematográfica DSP al abrir la app.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.sp
                        )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replay_intro_button")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER INTRO Y ANIMACIÓN COMPLETA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: Pantalla y Energía
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = null,
                        tint = theme.accentColor
                    )
                    Text(
                        text = "PANTALLA & ENERGÍA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color(0xFF263238), thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mantener Pantalla Siempre Encendida",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Evita que la pantalla se apague durante mediciones o sesiones de música en vivo.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = onToggleKeepScreenOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.accentColor,
                            checkedTrackColor = theme.accentColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("keep_screen_on_switch")
                    )
                }
            }
        }

        // Section: Calibración Micrófono & RTA
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "CALIBRACIÓN ACÚSTICA RTA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color(0xFF263238), thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Compensación SPL (Offset Micrófono):",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%+.1f dB".format(splOffsetDb),
                        color = Color(0xFF00E5FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = splOffsetDb,
                    onValueChange = onUpdateSplOffset,
                    valueRange = -20f..20f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.testTag("spl_offset_slider")
                )
            }
        }

        // Section: Selección de Temas (Neon, Brasil, Cyber, Gold, etc.)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = theme.primaryColor
                    )
                    Text(
                        text = "ESTILO VISUAL & TEMAS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color(0xFF263238), thickness = 0.8.dp)

                CarAudioThemeType.entries.forEach { themeItem ->
                    val isSelected = theme == themeItem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.15f) else Color(0xFF0C101A))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.primaryColor else Color(0xFF1E2838),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTheme(themeItem) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(themeItem.primaryColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = themeItem.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primaryColor else Color.White
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
                                Text(
                                    text = "ACTIVO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = theme.onPrimaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Restablecer DSP
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFF3D00).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = Color(0xFFFF3D00)
                    )
                    Text(
                        text = "RESTABLECER DSP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFF3D00)
                    )
                }

                Text(
                    text = "Regresa todos los parámetros a su estado original (FLAT). CUIDADO: esto anulará cualquier curva no guardada.",
                    fontSize = 10.sp,
                    color = theme.textSecondaryColor
                )

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1515)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESTABLECER A VALORES DE FÁBRICA", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: Acerca de CAR AUDIO DSP PRO
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0D15)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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
                    text = "Versión 2.0 • Edición Profesional",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Motor de Procesamiento Digital de Audio de Baja Latencia para Sistemas de Car Audio, SPL y Open Show.",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
