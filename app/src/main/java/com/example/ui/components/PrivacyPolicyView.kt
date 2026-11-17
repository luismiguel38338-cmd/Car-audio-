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
                    title = "1. Acceso Directo Sin Cuenta Obligatoria",
                    body = "Car Audio DSP Pro funciona directamente sin exigir inicio de sesión ni recopilar tu nombre, correo electrónico o contraseñas personales.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "2. Almacenamiento 100% Local en tu Dispositivo",
                    body = "Todas tus curvas de ecualización (31 y 10 bandas), snapshots de comparación A/B, perfiles DSP, listas de reproducción, tema visual 3D y accesos guardados permanecen exclusivamente en las preferencias locales de tu teléfono.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "3. Archivos de Música Locales",
                    body = "Las canciones escaneadas con READ_MEDIA_AUDIO o importadas manualmente se reproducen localmente mediante Media3 / ExoPlayer. Nunca subimos tus archivos de música a servidores externos.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "4. Permiso de Micrófono (RTA Opcional)",
                    body = "El analizador acústico de cabina mediante micrófono solo se activa cuando lo habilitas manualmente en Herramientas de audio > RTA y Medidores dB. El audio se procesa en memoria en tiempo real y nunca se graba ni se envía fuera del teléfono.",
                    theme = theme
                )
                HorizontalDivider(color = theme.surfaceColor)
                PolicySection(
                    title = "5. Enlaces Directos a YouTube Music",
                    body = "La sección YouTube Music construye búsquedas y enlaces directos que abren la aplicación oficial de YouTube Music o el portal web oficial en tu navegador, sin descargar ni extraer audio de terceros.",
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
