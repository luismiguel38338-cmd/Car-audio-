package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

/**
 * Welcome & Interactive Quick Tutorial screen for Audio DSP Pro.
 */
@Composable
fun CarAudioSplashScreen(
    theme: CarAudioThemeType,
    onEnterApp: () -> Unit,
    onSetDontShowAgain: (Boolean) -> Unit,
    initialDontShowAgain: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }

    EpicAmbientBackdrop(
        theme = theme,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("audio_dsp_welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 1).forEach { stepIdx ->
                        val active = currentStep == stepIdx
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (active) 28.dp else 10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) theme.accentColor else theme.textSecondaryColor.copy(alpha = 0.35f)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentStep == 0) "Paso 1 de 2 • Bienvenida" else "Paso 2 de 2 • Guía Rápida",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onEnterApp,
                    modifier = Modifier.testTag("btn_skip_welcome")
                ) {
                    Text(
                        text = "Saltar",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "welcome_tutorial_step"
            ) { step ->
                if (step == 0) {
                    WelcomeStudioStep(theme = theme)
                } else {
                    QuickStartGuideStep(theme = theme)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            dontShowAgain = !dontShowAgain
                            onSetDontShowAgain(dontShowAgain)
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = {
                            dontShowAgain = it
                            onSetDontShowAgain(it)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = theme.accentColor,
                            checkmarkColor = theme.onPrimaryColor
                        )
                    )
                    Text(
                        text = "No mostrar al iniciar la próxima vez",
                        color = theme.textSecondaryColor,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep == 1) {
                        OutlinedButton(
                            onClick = { currentStep = 0 },
                            modifier = Modifier
                                .weight(0.42f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.textColor)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Atrás", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep == 0) {
                                currentStep = 1
                            } else {
                                onEnterApp()
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentStep == 1) 0.58f else 1f)
                            .height(52.dp)
                            .testTag(if (currentStep == 0) "btn_welcome_next" else "btn_enter_studio"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    theme.silverDetailColor.copy(alpha = 0.48f),
                                    theme.accentColor.copy(alpha = 0.85f)
                                )
                            )
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = theme.onPrimaryColor
                        )
                    ) {
                        Text(
                            text = if (currentStep == 0) "SIGUIENTE" else "ABRIR AUDIO DSP PRO",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continuar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStudioStep(theme: CarAudioThemeType) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "splash_logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1600),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(theme.primaryColor.copy(alpha = 0.7f), theme.accentColor.copy(alpha = 0.7f))),
                    RoundedCornerShape(22.dp)
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_car_audio_cockpit_1794912323387),
                contentDescription = "Car Audio DSP Pro Cabina 3D",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                theme.backgroundColor.copy(alpha = 0.50f),
                                theme.backgroundColor.copy(alpha = 0.96f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                    contentDescription = "Logo Car Audio DSP Pro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .clip(CircleShape)
                        .border(1.8.dp, Brush.sweepGradient(listOf(theme.primaryColor, theme.accentColor, theme.primaryColor)), CircleShape)
                )
                Column {
                    Text(
                        text = "POTENCIA TU MÚSICA",
                        color = theme.primaryColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "CAR AUDIO DSP PRO",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "LM Produce — Producción de Luis Miguel",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Estudio Automotriz 3D y Procesamiento DSP Real",
            color = theme.textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Reproduce tu música local u online con Media3, calibra 31 y 10 bandas, activa Bass Boost Extremo, controla tu subwoofer y descarga pistas con licencia libre.",
            color = theme.textSecondaryColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WelcomePill(
                icon = Icons.Default.LibraryMusic,
                title = "Mi Música",
                subtitle = "Media3 & Cola",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            WelcomePill(
                icon = Icons.Default.Equalizer,
                title = "EQ 31 Bandas",
                subtitle = "Comparador A/B",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            WelcomePill(
                icon = Icons.Default.GraphicEq,
                title = "Cabina 3D",
                subtitle = "Subs & RTA/dB",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WelcomePill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        highlightBorder = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = title, tint = theme.accentColor, modifier = Modifier.size(22.dp))
            Text(title, color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, color = theme.textSecondaryColor, fontSize = 9.5.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun QuickStartGuideStep(theme: CarAudioThemeType) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cómo Usar Car Audio DSP Pro",
            color = theme.textColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Cuatro pasos rápidos para comenzar; encontrarás las 10 guías completas dentro de Configuración > Tutoriales.",
            color = theme.textSecondaryColor,
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        )

        QuickGuideRow(
            step = "1",
            icon = Icons.Default.LibraryMusic,
            title = "Escanea o Importa tu Música",
            desc = "Descubre canciones, artistas y álbumes de tu dispositivo o importa archivos FLAC, WAV, MP3 y AAC directamente.",
            theme = theme
        )
        QuickGuideRow(
            step = "2",
            icon = Icons.Default.Tune,
            title = "Calibra EQ y Comparador A/B",
            desc = "Ajusta 31 o 10 bandas, Bass Boost, filtros HPF/LPF y alterna configuraciones A ↔ B al instante mientras escuchas.",
            theme = theme
        )
        QuickGuideRow(
            step = "3",
            icon = Icons.Default.GraphicEq,
            title = "Supervisa Subwoofers 3D y RTA",
            desc = "Observa la cabina 3D sincronizada al ritmo, el analizador RTA, el osciloscopio, RMS dBFS y telemetría de batería.",
            theme = theme
        )
        QuickGuideRow(
            step = "4",
            icon = Icons.Default.CloudDone,
            title = "YouTube Music y Presets Locales",
            desc = "Busca canciones para abrir en YouTube Music y guarda tus presets favoritos localmente sin cuentas obligatorias.",
            theme = theme
        )
    }
}

@Composable
private fun QuickGuideRow(
    step: String,
    icon: ImageVector,
    title: String,
    desc: String,
    theme: CarAudioThemeType
) {
    GlassStudioCard(
        theme = theme,
        highlightBorder = true
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.cardColor)
                    .border(1.dp, theme.accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "PASO $step • $title",
                    color = theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = theme.textSecondaryColor,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
