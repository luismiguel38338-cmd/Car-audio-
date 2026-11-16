package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType

@Composable
fun PrivacyPolicyDialog(
    theme: CarAudioThemeType,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cardColor,
        title = {
            Text(
                text = "Política de Privacidad y Seguridad de Datos",
                color = theme.textColor,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PolicySection(
                    title = "1. Autenticación y Cuenta",
                    body = "Audio DSP Pro utiliza Google Sign-In mediante Android Credential Manager y Firebase Authentication. Solo almacenamos tu UID, nombre para mostrar, correo electrónico y foto de perfil para vincular tus configuraciones.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "2. Archivos de Música Locales",
                    body = "Los archivos de audio descubiertos con READ_MEDIA_AUDIO o seleccionados manualmente se reproducen localmente en tu dispositivo mediante Media3 / ExoPlayer. Nunca subimos tus archivos de música a servidores externos.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "3. Datos Sincronizados en Cloud Firestore",
                    body = "Guardamos únicamente tus perfiles de ecualización, nombres de listas, claves de favoritos/cola, última posición de reproducción y preferencias de tema bajo acceso estricto exclusivo para tu usuario autenticado.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "4. Permiso de Micrófono (RTA Opcional)",
                    body = "El analizador acústico de sala mediante micrófono solo se activa cuando lo habilitas manualmente en Medidores. El audio capturado se procesa en memoria en tiempo real para calcular bandas FFT y nunca se graba ni se envía fuera del teléfono.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "5. Eliminación Total de Datos",
                    body = "Puedes eliminar tu cuenta y todos tus registros en la nube de forma inmediata e irreversible desde Configuración > Eliminar Cuenta y Datos en la Nube.",
                    theme = theme
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ENTENDIDO", color = theme.accentColor, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
private fun PolicySection(
    title: String,
    body: String,
    theme: CarAudioThemeType
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            color = theme.textColor,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = body,
            color = theme.textSecondaryColor,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
        )
    }
}
