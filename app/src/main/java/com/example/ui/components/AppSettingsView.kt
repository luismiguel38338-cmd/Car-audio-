package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.AudioSessionTarget
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
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
    userProfile: CloudUserProfile?,
    cloudSyncActive: Boolean,
    isDeletingAccount: Boolean,
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
    onSignOut: () -> Unit,
    onConfirmDeleteAccount: (onError: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var deleteStep by remember { mutableIntStateOf(0) } // 0=hidden, 1=first warning, 2=final confirmation
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(theme = theme, onDismiss = { showPrivacyDialog = false })
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

    if (deleteStep > 0) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) deleteStep = 0 },
            containerColor = theme.cardColor,
            title = {
                Text(
                    text = if (deleteStep == 1) "¿Eliminar tu Cuenta?" else "Confirmación Irreversible",
                    color = theme.errorColor,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (deleteStep == 1) {
                            "Se borrarán tu perfil, configuraciones de audio personalizadas y listas guardadas en Cloud Firestore."
                        } else {
                            "Esta acción eliminará permanentemente tu cuenta de usuario y todos tus datos en la nube. ¿Deseas continuar?"
                        },
                        color = theme.textSecondaryColor,
                        fontSize = 12.5.sp
                    )
                    if (!deleteErrorMessage.isNullOrBlank()) {
                        Text(deleteErrorMessage!!, color = theme.errorColor, fontSize = 11.5.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isDeletingAccount,
                    onClick = {
                        if (deleteStep == 1) {
                            deleteErrorMessage = null
                            deleteStep = 2
                        } else {
                            deleteErrorMessage = null
                            onConfirmDeleteAccount { err -> deleteErrorMessage = err }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.errorColor, contentColor = theme.textColor)
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = theme.textColor)
                    } else {
                        Text(if (deleteStep == 1) "Continuar" else "Eliminar Todo", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !isDeletingAccount, onClick = { deleteStep = 0 }) {
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
            .padding(14.dp)
            .testTag("app_settings_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Google Account & Cloud Sync Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_account_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val photoUrl = userProfile?.photoUrl
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(photoUrl).crossfade(true).build(),
                            contentDescription = "Avatar Google",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.2.dp, theme.accentColor, CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                            contentDescription = "Usuario",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userProfile?.displayName ?: "Usuario Google", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(userProfile?.email ?: "Sesión activa", color = theme.textSecondaryColor, fontSize = 11.5.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = if (cloudSyncActive) theme.accentColor else theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (cloudSyncActive) "SYNC ON" else "LOCAL",
                            color = if (cloudSyncActive) theme.accentColor else theme.textSecondaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = theme.cardColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = theme.textColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar Sesión", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { deleteStep = 1 },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, theme.errorColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_delete_account_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = theme.errorColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar Cuenta", color = theme.errorColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. DSP Diagnostics & Permissions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_diagnostics_card")
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
                        Text("DIAGNÓSTICO DE AUDIO Y PERMISOS", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
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

        // 3. Tools, Onboarding, Privacy & Reset
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mostrar bienvenida al iniciar", color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Guía rápida inicial de estudio", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                    }
                    Switch(
                        checked = autoPlayIntro,
                        onCheckedChange = onToggleAutoPlayIntro,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }

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
            }
        }

        // 4. Fault Alerts Log
        if (faultAlerts.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor),
                modifier = Modifier.fillMaxWidth()
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
