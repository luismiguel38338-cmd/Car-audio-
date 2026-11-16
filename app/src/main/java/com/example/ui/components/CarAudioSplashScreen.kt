package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Speaker
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
 * Replaces the old loading splash screen with a polished Welcome + Brief Interactive Tutorial flow.
 */
@Composable
fun CarAudioSplashScreen(
    theme: CarAudioThemeType,
    onStartSound: () -> Unit,
    onEnterApp: () -> Unit,
    onSetDontShowAgain: (Boolean) -> Unit,
    initialDontShowAgain: Boolean = false,
    liteVisuals: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) } // 0 = Bienvenida, 1 = Tutorial Breve
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }

    val bgBrush = remember(theme) {
        Brush.verticalGradient(
            listOf(
                theme.backgroundColor,
                theme.surfaceColor,
                theme.backgroundColor
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("car_audio_welcome_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Step indicator + Skip
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
                                    if (active) theme.primaryColor else theme.textSecondaryColor.copy(alpha = 0.35f)
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

            // Main Animated Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "welcome_tutorial_step"
            ) { step ->
                if (step == 0) {
                    WelcomeHeroStep(theme = theme)
                } else {
                    BriefTutorialStep(theme = theme)
                }
            }

            // Bottom Navigation Buttons + Don't show again
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
                            checkedColor = theme.primaryColor,
                            checkmarkColor = theme.onPrimaryColor
                        )
                    )
                    Text(
                        text = "Entrar directo la próxima vez",
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
                            border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.6f)),
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
                                onStartSound()
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = theme.onPrimaryColor
                        )
                    ) {
                        Text(
                            text = if (currentStep == 0) "SIGUIENTE" else "COMENZAR AHORA",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Siguiente",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeroStep(theme: CarAudioThemeType) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Banner Image with studio logo overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, theme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.car_audio_cockpit_hero_1794824451080),
                contentDescription = "Cabina Car Audio Studio",
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
                                theme.backgroundColor.copy(alpha = 0.35f),
                                theme.backgroundColor.copy(alpha = 0.92f)
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
                    painter = painterResource(id = R.drawable.car_audio_dsp_icon_1794824449077),
                    contentDescription = "Logo Car Audio DSP Pro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, theme.primaryColor, CircleShape)
                )
                Column {
                    Text(
                        text = "BIENVENIDO A",
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "CAR AUDIO DSP PRO",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tu Cabina Profesional de Sonido y Radio Pioneer",
            color = theme.textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Autoestéreos Pioneer con emisoras en vivo, reproductor independiente con listas, ecualizador de 31 bandas, panel de altavoces y barra dinámica de 5 LED.",
            color = theme.textSecondaryColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feature highlight cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WelcomeHighlightChip(
                icon = Icons.Default.Radio,
                title = "Radio Pioneer",
                subtitle = "5 Diseños & FM",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            WelcomeHighlightChip(
                icon = Icons.Default.LibraryMusic,
                title = "Música Real",
                subtitle = "MP3, FLAC, Listas",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            WelcomeHighlightChip(
                icon = Icons.Default.GraphicEq,
                title = "DSP 31 Bandas",
                subtitle = "5-LED & Voltímetro",
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WelcomeHighlightChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = theme.primaryColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                color = theme.textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = theme.textSecondaryColor,
                fontSize = 9.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BriefTutorialStep(theme: CarAudioThemeType) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cómo Usar tu Estudio",
            color = theme.textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Sigue estos 4 pasos rápidos para sacarle el máximo provecho a tu sonido:",
            color = theme.textSecondaryColor,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        TutorialStepCard(
            stepNumber = "1",
            icon = Icons.Default.Radio,
            title = "Radio Pioneer Independiente",
            description = "Elige entre 5 carátulas Pioneer, personaliza su iluminación LED, sintoniza emisoras reales en vivo y guarda tus favoritas M1–M6.",
            theme = theme
        )
        TutorialStepCard(
            stepNumber = "2",
            icon = Icons.Default.LibraryMusic,
            title = "Reproductor de Música Local y Listas",
            description = "Carga tus archivos MP3/FLAC/WAV, crea listas de reproducción independientes y visualiza las luces 5-LED al ritmo de cada canción.",
            theme = theme
        )
        TutorialStepCard(
            stepNumber = "3",
            icon = Icons.Default.Speaker,
            title = "Ecualizador, Altavoces y Herramientas",
            description = "Ajusta 31 bandas ISO, subwoofer, balance, fader, tweeters, RTA y monitorea la batería real de tu teléfono desde el menú de 9 apartados.",
            theme = theme
        )
        TutorialStepCard(
            stepNumber = "4",
            icon = Icons.Default.NotificationsActive,
            title = "Alertas y Diagnóstico en Vivo",
            description = "Si ocurre clipping, falta un permiso o falla una transmisión, recibirás una notificación clara para solucionarlo al instante.",
            theme = theme
        )
    }
}

@Composable
private fun TutorialStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String,
    theme: CarAudioThemeType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor.copy(alpha = 0.16f))
                    .border(1.2.dp, theme.primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryColor)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "PASO $stepNumber",
                            color = theme.onPrimaryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = title,
                        color = theme.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = description,
                    color = theme.textSecondaryColor,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
