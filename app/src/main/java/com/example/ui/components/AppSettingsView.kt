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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioSessionTarget
import com.example.model.CarAudioThemeType
import com.example.model.DspCapabilityReport
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
    onSelectTheme: (CarAudioThemeType) -> Unit,
    liteVisuals: Boolean,
    onToggleLiteVisuals: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    autoPlayIntro: Boolean,
    onToggleAutoPlayIntro: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    onResetDspDefaults: () -> Unit,
    outputDeviceStatus: OutputDeviceStatus,
    sessionTarget: AudioSessionTarget,
    capabilityReport: DspCapabilityReport,
    hasAudioPermission: Boolean,
    hasNotificationPermission: Boolean,
    faultAlerts: List<SystemFaultAlert>,
    onRequestNotificationPermission: () -> Unit,
    onRequestAudioPermission: () -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onClearFaults: () -> Unit,
    storageUsedBytes: Long = 0L,
    onClearCache: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTutorialsDialog by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(theme = theme, onDismiss = { showPrivacyDialog = false })
    }

    if (showTutorialsDialog) {
        Dialog(
            onDismissRequest = { showTutorialsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                color = theme.backgroundColor,
                border = BorderStroke(1.dp, theme.cardColor)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.surfaceColor)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GUÍAS Y TUTORIALES DEL ESTUDIO",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        TextButton(onClick = { showTutorialsDialog = false }) {
                            Text("Cerrar", color = theme.accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    TutorialCenterView(
                        theme = theme,
                        onReplayQuickGuide = {
                            showTutorialsDialog = false
                            onReplayIntro()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Restablecer Laboratorio DSP", fontWeight = FontWeight.Bold, color = theme.textColor) },
            text = {
                Text(
                    "Esta acción regresará las 10 y 31 bandas, filtros y dinámica a valores planos (Flat).",
                    color = theme.textSecondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDspDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Restablecer", fontWeight = FontWeight.Bold)
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
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("app_settings_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 3D Automotive Themes & Performance Optimization
        GlassStudioCard(
            theme = theme,
            highlightBorder = true,
            modifier = Modifier.testTag("settings_appearance_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = theme.accentColor)
                    Column {
                        Text(
                            text = "Apariencia y rendimiento",
                            color = theme.textColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Temas visuales de estudio y opciones de fluidez",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CarAudioThemeType.entries.forEach { item ->
                        val isSelected = item == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) item.cardColor else theme.backgroundColor)
                                .border(
                                    1.dp,
                                    if (isSelected) item.accentColor else theme.cardColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectTheme(item) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("theme_option_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(item.surfaceColor)
                                    .border(2.dp, item.accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(item.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    color = theme.textColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    color = theme.textSecondaryColor,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "ACTIVO",
                                    color = theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = theme.cardColor)

                SettingsToggleRow(
                    title = "Efectos Visuales Ligeros (Gama Baja/Media)",
                    subtitle = "Reduce animaciones y efectos para máxima fluidez y menor consumo",
                    checked = liteVisuals,
                    onCheckedChange = onToggleLiteVisuals,
                    theme = theme,
                    modifier = Modifier.testTag("settings_lite_visuals_switch")
                )

                SettingsToggleRow(
                    title = "Mantener Pantalla Encendida",
                    subtitle = "Ideal cuando usas el teléfono montado en el tablero del vehículo",
                    checked = keepScreenOn,
                    onCheckedChange = onToggleKeepScreenOn,
                    theme = theme,
                    modifier = Modifier.testTag("settings_keep_screen_switch")
                )
            }
        }

        // 2. DSP Diagnostics & Permissions
        GlassStudioCard(
            theme = theme,
            modifier = Modifier.testTag("settings_diagnostics_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = theme.accentColor)
                        Text("Diagnóstico de audio y permisos", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onRefreshDiagnostics) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revisar", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                DiagnosticStatusRow("Motor DSP", capabilityReport.engineSummary, theme)
                DiagnosticStatusRow("Sesión AudioFX", sessionTarget.label, theme)
                DiagnosticStatusRow("Salida Activa", outputDeviceStatus.label, theme)
                DiagnosticStatusRow("Anti Doble Proceso", if (capabilityReport.antiDoubleProcessingSafe) "Verificado y Seguro" else "Revisar", theme)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRequestAudioPermission,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (hasAudioPermission) theme.accentColor.copy(alpha = 0.4f) else theme.clipAlertColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasAudioPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasAudioPermission) theme.accentColor else theme.clipAlertColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasAudioPermission) "Música OK" else "Permiso Música",
                            color = theme.textColor,
                            fontSize = 10.5.sp
                        )
                    }
                    OutlinedButton(
                        onClick = onRequestNotificationPermission,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (hasNotificationPermission) theme.accentColor.copy(alpha = 0.4f) else theme.clipAlertColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (hasNotificationPermission) theme.accentColor else theme.clipAlertColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasNotificationPermission) "Notif. OK" else "Notificaciones",
                            color = theme.textColor,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
        }

        // 3. Tutorials, Tools, Privacy & Reset
        GlassStudioCard(theme = theme) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlowActionButton(
                    text = "Centro de Tutoriales (10 Guías DSP)",
                    theme = theme,
                    onClick = { showTutorialsDialog = true },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_tutorials_button")
                )

                SettingsToggleRow(
                    title = "Mostrar guía rápida al iniciar",
                    subtitle = "Desactivado por defecto para abrir directo en el reproductor",
                    checked = autoPlayIntro,
                    onCheckedChange = onToggleAutoPlayIntro,
                    theme = theme
                )

                OutlinedButton(
                    onClick = onReplayIntro,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, theme.cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Bienvenida y Guía Rápida", color = theme.textColor, fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                )
                            } catch (_: Throwable) {}
                        } else {
                            FloatingEqualizerService.start(context)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, theme.cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureInPicture, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Ecualizador Flotante Rápido", color = theme.textColor, fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = { showPrivacyDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, theme.cardColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_privacy_button")
                ) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Política de Privacidad y Seguridad de Datos", color = theme.textColor, fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, theme.clipAlertColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = theme.clipAlertColor, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restablecer Laboratorio DSP a Plano (Flat)", color = theme.clipAlertColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = theme.cardColor)

                DiagnosticStatusRow("Almacenamiento Descargas", formatStorageBytes(storageUsedBytes), theme)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearCache,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpiar Caché", color = theme.textColor, fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onOpenProfile,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mi Perfil", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenAbout,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Acerca de", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Fault Alerts Log
        if (faultAlerts.isNotEmpty()) {
            GlassStudioCard(theme = theme) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("REGISTRO DE EVENTOS (${faultAlerts.size})", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        TextButton(onClick = onClearFaults) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar eventos", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar", color = theme.accentColor, fontSize = 11.sp)
                        }
                    }
                    faultAlerts.take(6).forEach { alert ->
                        val sevColor = when (alert.severity) {
                            FaultSeverity.ERROR -> theme.errorColor
                            FaultSeverity.WARNING -> theme.clipAlertColor
                            FaultSeverity.INFO -> theme.accentColor
                        }
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (alert.severity == FaultSeverity.ERROR) Icons.Default.Error else Icons.Default.Warning,
                                contentDescription = null,
                                tint = sevColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Column {
                                Text("${alert.title} • ${timeFormatter.format(Date(alert.timestampMs))}", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(alert.message, color = theme.textSecondaryColor, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = theme.textSecondaryColor, fontSize = 10.5.sp, lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.primaryColor,
                checkedTrackColor = theme.accentColor.copy(alpha = 0.45f)
            )
        )
    }
}

@Composable
private fun DiagnosticStatusRow(label: String, value: String, theme: CarAudioThemeType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = theme.textSecondaryColor, fontSize = 11.sp)
        Text(value, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
