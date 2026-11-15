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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSessionTarget
import com.example.model.CarAudioThemeType
import com.example.model.FaultSeverity
import com.example.model.OutputDeviceStatus
import com.example.model.SystemFaultAlert
import com.example.service.FloatingEqualizerService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    isVipUnlocked: Boolean = false,
    onRequestVipDialog: () -> Unit = {},
    onLockVip: () -> Unit = {},
    fxEngineStatus: String = "AudioFX Activo",
    hasAudioPermission: Boolean = true,
    hasNotificationPermission: Boolean = true,
    faultAlerts: List<SystemFaultAlert> = emptyList(),
    onRequestNotificationPermission: () -> Unit = {},
    onRequestAudioPermission: () -> Unit = {},
    onRefreshDiagnostics: () -> Unit = {},
    onClearFaults: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    val orderedThemes = remember {
        listOf(CarAudioThemeType.CRIMSON_RED) +
            CarAudioThemeType.entries.filter { it != CarAudioThemeType.CRIMSON_RED }
    }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

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
        // 1. ACCESO VIP STUDIO
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isVipUnlocked) theme.surfaceColor else theme.cardColor),
            border = BorderStroke(1.3.dp, theme.primaryColor.copy(alpha = if (isVipUnlocked) 0.7f else 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_vip_card")
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
                            imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                            contentDescription = "Acceso VIP Studio",
                            tint = theme.primaryColor
                        )
                        Text(
                            text = if (isVipUnlocked) "VIP STUDIO ACTIVO" else "ACCESO VIP STUDIO",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = theme.textColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isVipUnlocked) theme.primaryColor else theme.surfaceColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isVipUnlocked) "DESBLOQUEADO" else "BLOQUEADO",
                            color = if (isVipUnlocked) theme.onPrimaryColor else theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                HorizontalDivider(color = theme.backgroundColor.copy(alpha = 0.6f), thickness = 0.8.dp)

                if (isVipUnlocked) {
                    Text(
                        text = "Todos los módulos Pro están activos: Motor Hi-Res Studio, Calidez Analógica, Presencia Vocal, Claridad HD, Excitador Armónico, Pendientes 36/48 dB/oct, Presets VIP y Temas Exclusivos.",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    OutlinedButton(
                        onClick = onLockVip,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.textSecondaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_lock_vip_button")
                    ) {
                        Text("Bloquear Acceso VIP", color = theme.textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Ingresa tu clave VIP para desbloquear mejoras de calidad HD, módulos avanzados de bajos, filtros paramétricos completos y estilos visuales exclusivos.",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Button(
                        onClick = onRequestVipDialog,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_unlock_vip_button")
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DESBLOQUEAR FUNCIONES VIP", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // 2. CENTRO DE DIAGNÓSTICO Y NOTIFICACIONES DE FALLOS
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_diagnostics_card")
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
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = theme.primaryColor)
                        Text("DIAGNÓSTICO Y ALERTAS DE FALLOS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = theme.textColor)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onRefreshDiagnostics) {
                            Icon(Icons.Default.Refresh, contentDescription = "Verificar sistema", tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Verificar", color = theme.primaryColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        if (faultAlerts.isNotEmpty()) {
                            TextButton(onClick = onClearFaults) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar historial", tint = theme.textSecondaryColor, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Limpiar", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                            }
                        }
                    }
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                DiagnosticStatusRow(
                    label = "Motor AudioFX",
                    value = fxEngineStatus,
                    isOk = !fxEngineStatus.contains("error", ignoreCase = true) && !fxEngineStatus.contains("fall", ignoreCase = true),
                    theme = theme
                )
                DiagnosticStatusRow(
                    label = "Dispositivo de Salida",
                    value = "${outputDeviceStatus.label} • ${sessionTarget.label}",
                    isOk = true,
                    theme = theme
                )
                DiagnosticStatusRow(
                    label = "Permiso de Música Local",
                    value = if (hasAudioPermission) "Concedido" else "Denegado (toca para activar)",
                    isOk = hasAudioPermission,
                    theme = theme,
                    onClick = if (!hasAudioPermission) onRequestAudioPermission else null
                )
                DiagnosticStatusRow(
                    label = "Permiso de Notificaciones",
                    value = if (hasNotificationPermission) "Activo (alertará si algo falla)" else "Inactivo (toca para activar)",
                    isOk = hasNotificationPermission,
                    theme = theme,
                    onClick = if (!hasNotificationPermission) onRequestNotificationPermission else null
                )

                Text(
                    text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) • ${sessionTarget.detail}",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                if (faultAlerts.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.cardColor)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Sin fallos recientes. El sistema monitorea archivos, clipping y desconexiones en tiempo real.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HISTORIAL DE ALERTAS (${faultAlerts.size})",
                            color = theme.primaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        faultAlerts.take(6).forEach { alert ->
                            val sevColor = when (alert.severity) {
                                FaultSeverity.ERROR -> theme.errorColor
                                FaultSeverity.WARNING -> Color(0xFFFFB300)
                                FaultSeverity.INFO -> theme.accentColor
                            }
                            val sevIcon = when (alert.severity) {
                                FaultSeverity.ERROR -> Icons.Default.Error
                                FaultSeverity.WARNING -> Icons.Default.Warning
                                FaultSeverity.INFO -> Icons.Default.Info
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardColor)
                                    .border(1.dp, sevColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(sevIcon, contentDescription = null, tint = sevColor, modifier = Modifier.size(16.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(alert.title, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = timeFormatter.format(Date(alert.timestampMs)),
                                            color = theme.textSecondaryColor,
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(alert.message, color = theme.textSecondaryColor, fontSize = 10.sp, lineHeight = 13.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. RENDIMIENTO, PANTALLA Y BURBUJA FLOTANTE
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
                    Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primaryColor)
                    Text("RENDIMIENTO, PANTALLA Y FLOTANTE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mantener Pantalla Encendida", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Ideal cuando usas la app conectada al estéreo del auto.", color = theme.textSecondaryColor, fontSize = 10.sp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Ligero (Teléfonos Modestos)", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isLowRamDevice) {
                                "Dispositivo de poca RAM detectado: optimiza gráficos sin perder calidad DSP."
                            } else {
                                "Reduce animaciones y tasa de espectro para máxima fluidez."
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

                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PictureInPicture, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                    Text("Burbuja Flotante (YouTube / Apps Externas)", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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

        // 4. ESTILOS VISUALES (FREE + VIP)
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
                    Text("ESTILOS VISUALES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                orderedThemes.forEach { themeItem ->
                    val isSelected = theme == themeItem
                    val locked = themeItem.isVipOnly && !isVipUnlocked
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
                            .clickable {
                                if (locked) onRequestVipDialog() else onSelectTheme(themeItem)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(themeItem.primaryColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(
                                        text = themeItem.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) theme.primaryColor else theme.textColor
                                    )
                                    if (themeItem.isVipOnly) {
                                        Text(
                                            text = "VIP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = theme.primaryColor
                                        )
                                    }
                                }
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
                        } else if (locked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Tema VIP bloqueado",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. BIENVENIDA, TUTORIAL Y RESTABLECER DSP
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
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = theme.primaryColor)
                    Text("BIENVENIDA, TUTORIAL Y FÁBRICA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textColor)
                }
                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mostrar Bienvenida al Iniciar", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Pantalla de bienvenida y guía rápida al abrir la app.", color = theme.textSecondaryColor, fontSize = 10.sp)
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
                    Text("VER BIENVENIDA Y TUTORIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = theme.cardColor, thickness = 0.8.dp)

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTABLECER VALORES FLAT", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. ACERCA DE
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
                    text = "Versión 6.0.0 • Studio Edition",
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Reproductor local real, Sistema de Bajos dedicado, alertas en tiempo real y procesador AudioFX de alta precisión.",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DiagnosticStatusRow(
    label: String,
    value: String,
    isOk: Boolean,
    theme: CarAudioThemeType,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.cardColor)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) Color(0xFF00E676) else Color(0xFFFFB300),
                modifier = Modifier.size(15.dp)
            )
            Column {
                Text(label, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(value, color = if (isOk) theme.textSecondaryColor else Color(0xFFFFB300), fontSize = 10.sp)
            }
        }
        if (onClick != null) {
            Text("ACTIVAR", color = theme.primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}
