package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ads.AdSlotManager
import com.example.model.CarAudioThemeType
import com.example.model.DspCapabilityReport

@Composable
fun AboutView(
    theme: CarAudioThemeType,
    capabilityReport: DspCapabilityReport,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val pulse = rememberInfiniteTransition(label = "about_pulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "about_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("about_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassStudioCard(theme = theme, highlightBorder = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                    contentDescription = "Logo CAR AUDIO DSP PRO",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(92.dp)
                        .graphicsLayer {
                            scaleX = pulse.value
                            scaleY = pulse.value
                        }
                        .clip(CircleShape)
                        .border(2.dp, Brush.sweepGradient(listOf(theme.primaryColor, theme.accentColor, theme.primaryColor)), CircleShape)
                )
                Text(
                    text = "CAR AUDIO DSP PRO",
                    color = theme.textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "POTENCIA TU MÚSICA",
                    color = theme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "LM Produce — Producción de Luis Miguel",
                    color = theme.accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Versión ${BuildConfig.VERSION_NAME} • Optimizado para Android 13 y TECNO BG6",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Reporte real de capacidades de hardware DSP
        GlassStudioCard(theme = theme) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = theme.primaryColor)
                    Text("Capacidades Reales DSP en Este Teléfono", color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
                AboutDetailRow("Motor Activo", capabilityReport.engineSummary, theme)
                AboutDetailRow("DynamicsProcessing (API 28+)", if (capabilityReport.dynamicsProcessingSupported) "Soportado" else "Modo Ecualizador Fallback", theme)
                AboutDetailRow("Bandas Hardware", "${capabilityReport.equalizerBandCount} bandas activas", theme)
                AboutDetailRow("BassBoost Hardware", if (capabilityReport.bassBoostSupported) "Disponible" else "Limitado por dispositivo", theme)
                AboutDetailRow("Virtualizer Estéreo", if (capabilityReport.virtualizerSupported) "Disponible" else "Limitado por dispositivo", theme)
                Text(
                    text = "El audio local y streaming interno pasan por Media3 AudioFX nativo. Otras aplicaciones externas pueden restringir AudioFX según el fabricante.",
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Estado modular de monetización y cumplimiento Google Play
        GlassStudioCard(theme = theme) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = theme.accentColor)
                    Text("Estado de Monetización y Licencias", color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = AdSlotManager.statusDescription(),
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Música online via Jamendo API oficial (licencias Creative Commons) y enlaces oficiales a YouTube Music.",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://luismiguel38338-cmd.github.io/Car-audio-/"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sitio Oficial y Privacidad LM Produce", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AboutDetailRow(label: String, value: String, theme: CarAudioThemeType) {
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
