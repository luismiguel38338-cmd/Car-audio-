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
                text = "Política de Privacidad y Uso de Datos",
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
                    title = "1. Modo Local Completo y Cuenta Google Opcional",
                    body = "Car Audio DSP Pro funciona al 100% sin exigir registro ni cuentas. Opcionalmente, desde Perfil puedes iniciar sesión con Google para sincronizar tus presets, listas, favoritos y tema en Firebase Firestore, y eliminar tu cuenta y respaldo en la nube en cualquier momento desde el mismo panel.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "2. Almacenamiento Local en tu Dispositivo",
                    body = "Todas tus curvas de ecualización (31 y 10 bandas), perfiles por salida (Bluetooth, AUX, USB, Altavoz), snapshots A/B, listas y preferencias permanecen guardadas localmente en tu teléfono aunque no uses la nube.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "3. Archivos de Música, Jamendo API y Descargas",
                    body = "Las canciones locales escaneadas (READ_MEDIA_AUDIO) o importadas se reproducen con Media3 / ExoPlayer sin salir de tu teléfono. Música Online consulta el catálogo público Creative Commons de Jamendo API v3 y guarda descargas autorizadas en almacenamiento privado de la app.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "4. Micrófono RTA Local y SPL Relativo (Opcional)",
                    body = "El analizador acústico mediante micrófono (RECORD_AUDIO) solo se activa cuando lo pulsas manualmente. Se procesa únicamente en memoria para mostrar el RTA y un SPL relativo no calibrado; nunca se graba ni se transmite audio.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "5. Ecualizador Flotante (Opt-in) y Enlaces YouTube Music",
                    body = "La burbuja flotante sobre otras aplicaciones (SYSTEM_ALERT_WINDOW) está desactivada por defecto al salir de la app y solo se activa si tú la habilitas en Ajustes. Los accesos de YouTube Music abren búsquedas en la app o web oficial sin extraer audio.",
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
