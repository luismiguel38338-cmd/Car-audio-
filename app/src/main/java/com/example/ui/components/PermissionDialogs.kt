package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun PermissionExplanationDialog(
    theme: CarAudioThemeType,
    permissionType: String, // "notification" or "audio"
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isNotification = permissionType == "notification"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("permission_explanation_dialog"),
        containerColor = theme.cardColor,
        shape = RoundedCornerShape(18.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNotification) Icons.Default.NotificationsActive else Icons.Default.Mic,
                    contentDescription = null,
                    tint = theme.onPrimaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isNotification) "Permiso de Notificaciones" else "Permiso de Micrófono / RTA",
                color = theme.textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isNotification) {
                        "CarAudio DSP necesita el permiso de notificaciones para alertarte inmediatamente cuando:\n\n" +
                                "• La planta car audio entra en recorte (CLIPPING) para evitar quemar las bobinas de tus altavoces.\n" +
                                "• El voltaje de batería cae por debajo de 11.8V para prevenir apagones o daño eléctrico.\n" +
                                "• Mostrar los controles activos del reproductor musical."
                    } else {
                        "CarAudio DSP necesita acceso al micrófono para analizar en tiempo real la respuesta acústica y el espectro RTA de tu equipo musical o planta en el vehículo."
                    },
                    color = theme.textSecondaryColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_permission_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primaryColor,
                    contentColor = theme.onPrimaryColor
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Permitir Ahora", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora No", color = theme.textSecondaryColor)
            }
        }
    )
}
