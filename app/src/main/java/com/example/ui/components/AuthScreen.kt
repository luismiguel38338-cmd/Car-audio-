package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CarAudioThemeType

@Composable
fun AuthScreen(
    theme: CarAudioThemeType,
    isSigningIn: Boolean,
    errorMessage: String?,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            theme = theme,
            onDismiss = { showPrivacyDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        theme.backgroundColor,
                        theme.surfaceColor,
                        theme.backgroundColor
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, theme.cardColor, RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_studio_hero_1794828268351),
                        contentDescription = "Audio DSP Pro Estudio Móvil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        theme.backgroundColor.copy(alpha = 0.15f),
                                        theme.backgroundColor.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                            contentDescription = "Logo Audio DSP Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, theme.accentColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "AUDIO DSP PRO",
                                style = MaterialTheme.typography.titleLarge,
                                color = theme.textColor,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Plataforma Musical y Laboratorio DSP",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = "Tu música local con precisión de estudio",
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Inicia sesión con Google para sincronizar tus listas de reproducción, perfiles de ecualización, historial reciente y estado VIP en la nube.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.textSecondaryColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.cardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuthFeatureRow(
                            icon = Icons.Default.LibraryMusic,
                            title = "Reproductor Media3 / ExoPlayer",
                            subtitle = "Explora canciones, artistas, álbumes, listas y cola real",
                            theme = theme
                        )
                        AuthFeatureRow(
                            icon = Icons.Default.Equalizer,
                            title = "Laboratorio de Audio Unificado",
                            subtitle = "Ecualizador de 10 y 31 bandas sin doble procesamiento",
                            theme = theme
                        )
                        AuthFeatureRow(
                            icon = Icons.Default.GraphicEq,
                            title = "Analizador RTA y Osciloscopio",
                            subtitle = "Medición real de espectro, nivel VU y telemetría de batería",
                            theme = theme
                        )
                        AuthFeatureRow(
                            icon = Icons.Default.CloudDone,
                            title = "Respaldo en Firebase Firestore",
                            subtitle = "Conserva tus perfiles y listas al cambiar de dispositivo",
                            theme = theme
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.errorColor.copy(alpha = 0.14f)),
                        border = BorderStroke(1.dp, theme.errorColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = errorMessage,
                            color = theme.errorColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = onGoogleSignIn,
                    enabled = !isSigningIn,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = theme.onPrimaryColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Conectando con Google...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Continuar con Google",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continuar con Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                TextButton(
                    onClick = { showPrivacyDialog = true },
                    modifier = Modifier.testTag("auth_privacy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Política de Privacidad y Seguridad de Datos",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    theme: CarAudioThemeType
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(theme.cardColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = theme.textSecondaryColor, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}
