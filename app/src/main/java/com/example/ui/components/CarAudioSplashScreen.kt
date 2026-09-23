package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CarAudioThemeType
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class IntroScene {
    SCENE_1_BLACK_START,
    SCENE_2_BASS_PULSE,
    SCENE_3_DYNAMIC_EQ,
    SCENE_4_DOMINICAN_ATMOSPHERE,
    SCENE_5_LOGO_REVEAL,
    SCENE_6_POWER_BURST,
    SCENE_7_READY_TRANSITION
}

@Composable
fun CarAudioSplashScreen(
    theme: CarAudioThemeType,
    onStartSound: () -> Unit,
    onEnterApp: () -> Unit,
    onSetDontShowAgain: (Boolean) -> Unit,
    initialDontShowAgain: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentScene by remember { mutableStateOf(IntroScene.SCENE_1_BLACK_START) }
    var sceneProgress by remember { mutableFloatStateOf(0.0f) }
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }
    var soundTriggered by remember { mutableStateOf(false) }

    // Pulsing bass animations
    val infiniteTransition = rememberInfiniteTransition(label = "intro_anim")
    val bassPulsing by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bass_pulse"
    )

    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_glow"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Cinematic Intro Director Sequence
    LaunchedEffect(Unit) {
        if (!soundTriggered) {
            soundTriggered = true
            onStartSound()
        }

        // Scene 1: Black start & subtle bass vibration
        currentScene = IntroScene.SCENE_1_BLACK_START
        sceneProgress = 0.10f
        delay(700)

        // Scene 2: Bass activation with pulsing circular green waveform
        currentScene = IntroScene.SCENE_2_BASS_PULSE
        sceneProgress = 0.25f
        delay(900)

        // Scene 3: Dynamic equalizer bars responding in real time
        currentScene = IntroScene.SCENE_3_DYNAMIC_EQ
        sceneProgress = 0.45f
        delay(1000)

        // Scene 4: Dominican car-audio atmosphere (subtle blue/red reflections with intense neon green energy)
        currentScene = IntroScene.SCENE_4_DOMINICAN_ATMOSPHERE
        sceneProgress = 0.65f
        delay(900)

        // Scene 5: Logo reveal with CAR AUDIO DSP PRO
        currentScene = IntroScene.SCENE_5_LOGO_REVEAL
        sceneProgress = 0.85f
        delay(1100)

        // Scene 6: Power effect bass pulse & vibration
        currentScene = IntroScene.SCENE_6_POWER_BURST
        sceneProgress = 0.98f
        delay(800)

        // Scene 7: Smooth transition to the Logo and Main Interface
        currentScene = IntroScene.SCENE_7_READY_TRANSITION
        sceneProgress = 1.0f
        delay(500)
        onEnterApp()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03070E),
                        Color(0xFF070E1A),
                        Color(0xFF020408)
                    )
                )
            )
            .testTag("car_audio_splash_screen")
    ) {
        // Dynamic Background Canvas responding to current scene
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.42f

            when (currentScene) {
                IntroScene.SCENE_1_BLACK_START -> {
                    // Subtle dark vignette with center dot
                    drawCircle(
                        color = Color(0xFF00E676).copy(alpha = 0.15f * neonGlowAlpha),
                        radius = 40.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
                IntroScene.SCENE_2_BASS_PULSE -> {
                    // Multiple pulsing concentric rings (Subwoofer cone expansion)
                    for (r in 1..4) {
                        drawCircle(
                            color = Color(0xFF00E676).copy(alpha = (0.25f / r) * neonGlowAlpha),
                            radius = (r * 45).dp.toPx() * bassPulsing,
                            center = Offset(centerX, centerY)
                        )
                    }
                }
                IntroScene.SCENE_3_DYNAMIC_EQ -> {
                    // Dynamic spectrum lines in circular arrangement
                    val numBars = 32
                    for (i in 0 until numBars) {
                        val angle = (i.toFloat() / numBars) * 2 * PI.toFloat()
                        val barHeight = (sin(angle * 3 + wavePhase) * 35f + 45f).dp.toPx()
                        val rInner = 80.dp.toPx()
                        val rOuter = rInner + barHeight
                        val start = Offset(centerX + cos(angle) * rInner, centerY + sin(angle) * rInner)
                        val end = Offset(centerX + cos(angle) * rOuter, centerY + sin(angle) * rOuter)
                        drawLine(
                            color = Color(0xFF00E676).copy(alpha = 0.7f),
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
                IntroScene.SCENE_4_DOMINICAN_ATMOSPHERE -> {
                    // Dominican Caribbean neon reflections: Blue, Red, and dominant intense Green
                    drawCircle(
                        color = Color(0xFF0055FF).copy(alpha = 0.22f),
                        radius = 160.dp.toPx(),
                        center = Offset(centerX - 60f, centerY)
                    )
                    drawCircle(
                        color = Color(0xFFFF1744).copy(alpha = 0.20f),
                        radius = 160.dp.toPx(),
                        center = Offset(centerX + 60f, centerY)
                    )
                    drawCircle(
                        color = Color(0xFF00E676).copy(alpha = 0.35f * neonGlowAlpha),
                        radius = 130.dp.toPx() * bassPulsing,
                        center = Offset(centerX, centerY)
                    )
                }
                IntroScene.SCENE_5_LOGO_REVEAL, IntroScene.SCENE_6_POWER_BURST, IntroScene.SCENE_7_READY_TRANSITION -> {
                    // High-energy glowing aura around the central logo
                    drawCircle(
                        color = Color(0xFF00E676).copy(alpha = 0.32f * neonGlowAlpha),
                        radius = 150.dp.toPx() * (if (currentScene == IntroScene.SCENE_6_POWER_BURST) bassPulsing else 1.0f),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color(0xFF00B0FF).copy(alpha = 0.18f),
                        radius = 180.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }

        // Top skip bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF101B2B))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentScene) {
                        IntroScene.SCENE_1_BLACK_START -> "INICIANDO DSP"
                        IntroScene.SCENE_2_BASS_PULSE -> "BASS ENGINE ON"
                        IntroScene.SCENE_3_DYNAMIC_EQ -> "CALIBRANDO RTA"
                        IntroScene.SCENE_4_DOMINICAN_ATMOSPHERE -> "CAR AUDIO PRO"
                        IntroScene.SCENE_5_LOGO_REVEAL -> "LOGO SYNC"
                        IntroScene.SCENE_6_POWER_BURST -> "POWER 32-BIT"
                        IntroScene.SCENE_7_READY_TRANSITION -> "DSP LISTO"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            // Skip Intro Button
            Button(
                onClick = onEnterApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x3300E676),
                    contentColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("skip_intro_button")
            ) {
                Text(
                    text = "Saltar Intro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Center Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Visual / Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer glowing aura
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(if (currentScene >= IntroScene.SCENE_2_BASS_PULSE) bassPulsing else 1.0f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E676).copy(alpha = 0.45f * neonGlowAlpha),
                                    Color(0xFF00B0FF).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(16.dp)
                )

                // Neon circular border
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00E676),
                                    Color(0xFF00B0FF),
                                    Color(0xFF00E676)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // High-res Logo Image
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF070C16))
                        .border(2.dp, Color(0xFF00E676).copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.car_audio_dsp_logo),
                        contentDescription = "Logo CAR AUDIO DSP PRO",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main App Title
            Text(
                text = "CAR AUDIO DSP PRO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "PROCESADOR DIGITAL DE AUDIO AUTOMOTRIZ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E676),
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Scene Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF142032))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sceneProgress)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00B0FF),
                                    Color(0xFF00E676)
                                )
                            )
                        )
                )
            }
        }

        // Bottom Controls: Don't show again checkbox & Complete Intro Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox: "No reproducir automáticamente al iniciar"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        dontShowAgain = !dontShowAgain
                        onSetDontShowAgain(dontShowAgain)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { checked ->
                        dontShowAgain = checked
                        onSetDontShowAgain(checked)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF00E676),
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No reproducir automáticamente al iniciar",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            // Direct start button
            Button(
                onClick = onEnterApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676),
                    contentColor = Color(0xFF041209)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp)
                    .testTag("enter_dsp_button")
            ) {
                Text(
                    text = "ENTRAR AL DSP PRO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
