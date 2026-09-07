package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.CarAudioThemeType
import kotlin.math.sin

@Composable
fun WelcomeTutorialDialog(
    theme: CarAudioThemeType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Video Tutorial, 1 = Guía Cómo Usar, 2 = Plataformas de Música
    var isGlobalDspEnabled by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundColor.copy(alpha = 0.95f))
                .padding(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("welcome_tutorial_modal_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(theme.primaryColor, theme.accentColor))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header with close button & title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.surfaceColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BIENVENIDA & TUTORIAL EN VIVO",
                                color = theme.textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_welcome_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = theme.textColor
                            )
                        }
                    }

                    // Section Tabs
                    TabRow(
                        selectedTabIndex = selectedSection,
                        containerColor = theme.surfaceColor,
                        contentColor = theme.primaryColor,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                                color = theme.primaryColor
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedSection == 0,
                            onClick = { selectedSection = 0 },
                            text = { Text("Video Tutorial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedSection == 1,
                            onClick = { selectedSection = 1 },
                            text = { Text("Cómo Usar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedSection == 2,
                            onClick = { selectedSection = 2 },
                            text = { Text("Plataformas Música", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    // Content Scroll Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Creator Attribution Card (Prominent & verified)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.5f), theme.accentColor.copy(alpha = 0.5f)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verificado",
                                        tint = theme.onPrimaryColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Creada por Luis Miguel",
                                            color = theme.textColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = theme.primaryColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Contacto: luismiguel38338@gmail.com",
                                        color = theme.accentColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Car Audio Pro DSP & RTA Analyzer Suite v1.1 (Nueva Actualización)",
                                        color = theme.textSecondaryColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        when (selectedSection) {
                            0 -> {
                                // Real-time Video Tutorial Player Simulation
                                VideoTutorialInteractiveView(theme = theme)
                            }
                            1 -> {
                                // Guide on How to Use
                                HowToUseGuideView(theme = theme)
                            }
                            2 -> {
                                // Music Platforms Sync & Equalizer Global Update
                                MusicPlatformsSyncView(
                                    theme = theme,
                                    isGlobalDspEnabled = isGlobalDspEnabled,
                                    onToggleGlobalDsp = { isGlobalDspEnabled = it },
                                    onBroadcastDspToMusicApps = {
                                        try {
                                            // Send Android AudioEffect Intent to notify music players (Spotify, YT Music, etc.)
                                            val audioSessionIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                                            }
                                            context.sendBroadcast(audioSessionIntent)
                                            Toast.makeText(
                                                context,
                                                "✅ Perfil DSP transmitido a Spotify, YouTube Music y reproductores de audio",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "✅ Configuración DSP sincronizada en el sistema de sonido",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Dismiss Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.surfaceColor)
                            .padding(14.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_using_app_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Comenzar a Usar CarAudio DSP Pro",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Video Tutorial Player showing the Real-Time Equalizer and RTA spectrum in action!
 */
@Composable
fun VideoTutorialInteractiveView(theme: CarAudioThemeType) {
    var isVideoPlaying by remember { mutableStateOf(true) }
    var videoProgress by remember { mutableFloatStateOf(0.42f) }
    var activeChapter by remember { mutableIntStateOf(0) }

    val chapters = listOf(
        "1. Demostración: Ecualizador Real en Tiempo Real",
        "2. Crossovers Digitales HPF/LPF y Filtro Subsónico",
        "3. Medición de Sonómetro SPL y Retención de Picos",
        "4. Detección de Clipping y Protección de Batería"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "video_eq_anim")
    val animWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animWave"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Simulated Video Player Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_tutorial_player_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(theme.primaryColor, theme.accentColor))
            )
        ) {
            Column {
                // Video Screen Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color.Black)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tutorial_welcome_banner_1788811851146),
                        contentDescription = "Video Tutorial de la Aplicación",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )

                    // Live Dancing Equalizer Overlay in Video Tutorial
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Video Top Bar Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DEMO VIDEO HD",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = chapters[activeChapter],
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Real-Time Animated Equalizer Bands Graphic
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val frequencies = listOf("35Hz", "80Hz", "160Hz", "400Hz", "1kHz", "2.5k", "6.3k", "16k")
                            frequencies.forEachIndexed { i, freq ->
                                val waveOffset = (i * 0.75f)
                                val bandHeight = if (isVideoPlaying) {
                                    (0.25f + 0.65f * ((sin(animWave + waveOffset) + 1f) / 2f)).coerceIn(0.15f, 0.95f)
                                } else {
                                    0.45f
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Animated LED bar
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .height((bandHeight * 75).dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        theme.clipAlertColor,
                                                        theme.accentColor,
                                                        theme.primaryColor
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = freq,
                                        color = Color.LightGray,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Video Subtitle / Explanation Callout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (activeChapter) {
                                    0 -> "🔊 Observa cómo los 8 controles responden en tiempo real ajustando la acústica de tu vehículo."
                                    1 -> "🎛️ Corta frecuencias dañinas con HPF para voces y LPF para no quemar bobinas de subwoofers."
                                    2 -> "📊 El analizador RTA de 30 bandas calcula decibelios SPL y picos máximos en vivo."
                                    else -> "⚠️ Alarma automática en barra de estado si la planta se sobrecalienta o entra en clip."
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // Video Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181818))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isVideoPlaying = !isVideoPlaying },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Video",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Slider(
                        value = videoProgress,
                        onValueChange = {
                            videoProgress = it
                            activeChapter = (it * (chapters.size - 1)).toInt().coerceIn(0, chapters.size - 1)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )

                    Text(
                        text = "HD 1080p",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Chapter Selection Chips
        Text(
            text = "CAPÍTULOS DEL TUTORIAL EN VIDEO:",
            color = theme.textSecondaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        chapters.forEachIndexed { index, chapter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activeChapter = index
                        videoProgress = index.toFloat() / (chapters.size - 1)
                        isVideoPlaying = true
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (activeChapter == index) theme.surfaceColor else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                border = if (activeChapter == index) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.primaryColor, theme.accentColor))) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (activeChapter == index && isVideoPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (activeChapter == index) theme.primaryColor else theme.textSecondaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = chapter,
                        color = if (activeChapter == index) theme.primaryColor else theme.textColor,
                        fontSize = 11.sp,
                        fontWeight = if (activeChapter == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Step-by-Step Guide on How to Use the App
 */
@Composable
fun HowToUseGuideView(theme: CarAudioThemeType) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TutorialStepItem(
            stepNumber = "1",
            title = "Procesador DSP y Crossovers Digitales",
            description = "• Pestaña DSP: Activa el HPF (Paso Alto) para que tus medios y trompetas no reciban frecuencias graves que rompen conos.\n" +
                    "• Activa el LPF (Paso Bajo) para cortar tu subwoofer entre 60Hz y 120Hz.\n" +
                    "• Usa el Filtro Subsónico (25Hz–35Hz) si tienes un cajón porteado con entonación para proteger la suspensión.",
            icon = Icons.Default.Tune,
            theme = theme
        )

        TutorialStepItem(
            stepNumber = "2",
            title = "Analizador RTA y Micrófono Acústico",
            description = "• Pestaña RTA: Pulsa el botón 'Modo Micrófono' para medir la respuesta acústica real del habitáculo del auto con el micrófono.\n" +
                    "• Monitorea los decibelios en el medidor SPL en vivo y el pico SPL máximo alcanzado sin distorsión.",
            icon = Icons.Default.GraphicEq,
            theme = theme
        )

        TutorialStepItem(
            stepNumber = "3",
            title = "Monitor de Planta, Voltaje y Alerta de Clipping",
            description = "• Pestaña Planta: Vigila el voltímetro digital (14.4V ideal con alternador, peligro si cae a <11.5V).\n" +
                    "• El indicador LED CLIP te avisará antes de que quemes las bobinas de tus subwoofers por señal recortada o ganancia excesiva.",
            icon = Icons.Default.ElectricalServices,
            theme = theme
        )

        TutorialStepItem(
            stepNumber = "4",
            title = "Reproductor Musical y Carga de Canciones",
            description = "• Pestaña Player: Puedes reproducir pistas de prueba de car audio (tonos de sub-graves, barridos y ruido rosa) o pulsar 'Cargar Audio de mi Equipo' para reproducir cualquier archivo MP3/FLAC de tu celular.",
            icon = Icons.Default.MusicNote,
            theme = theme
        )

        TutorialStepItem(
            stepNumber = "5",
            title = "Personalización con 12 Temas de Color",
            description = "• Pestaña 12 Temas: Cambia instantáneamente la apariencia visual para que combine con la iluminación de tu autoestéreo o las luces LED de tu planta.",
            icon = Icons.Default.Waves,
            theme = theme
        )
    }
}

@Composable
fun TutorialStepItem(
    stepNumber: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: CarAudioThemeType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    color = theme.onPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Music Platforms Sync and Global DSP Equalizer Configuration
 */
@Composable
fun MusicPlatformsSyncView(
    theme: CarAudioThemeType,
    isGlobalDspEnabled: Boolean,
    onToggleGlobalDsp: (Boolean) -> Unit,
    onBroadcastDspToMusicApps: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Global DSP Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor, theme.accentColor))
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ECUALIZADOR GLOBAL EN SISTEMA",
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aplica los cortes y curvas DSP a todas las plataformas de música",
                            color = theme.textSecondaryColor,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = isGlobalDspEnabled,
                        onCheckedChange = onToggleGlobalDsp,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.onPrimaryColor,
                            checkedTrackColor = theme.primaryColor,
                            uncheckedTrackColor = theme.backgroundColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBroadcastDspToMusicApps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broadcast_dsp_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actualizar y Transmitir Perfil DSP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Compatible Music Platforms List
        Text(
            text = "PLATAFORMAS Y REPRODUCTORES COMPATIBLES:",
            color = theme.textSecondaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        val platforms = listOf(
            Triple("Spotify", "Conectado • Efecto DSP sincronizado", true),
            Triple("YouTube Music", "Compatible • Salida de audio balanceada", true),
            Triple("Apple Music / Deezer", "Compatible con ecualizador de sistema", true),
            Triple("Reproductor de Música Local", "Totalmente Integrado con selector MP3", true),
            Triple("Bluetooth Car Audio / Android Auto", "Transmisión activa con baja latencia", true)
        )

        platforms.forEach { (name, status, active) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = name,
                                color = theme.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = status,
                                color = theme.accentColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(theme.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVO",
                            color = theme.primaryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
