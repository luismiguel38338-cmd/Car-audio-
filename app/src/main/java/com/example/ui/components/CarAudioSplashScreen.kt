package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CarAudioThemeType
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class SplashBubble(
    val xRatio: Float,
    val initialY: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float,
    val colorIndex: Int
)

@Composable
fun CarAudioSplashScreen(
    theme: CarAudioThemeType,
    onStartSound: () -> Unit,
    onEnterApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0.0f) }
    var statusText by remember { mutableStateOf("Conectando relays de potencia...") }
    var soundTriggered by remember { mutableStateOf(false) }

    // Infinite transitions for pulsing bubble and logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Pre-calculated bubbles for the background
    val bubbles = remember {
        List(18) {
            SplashBubble(
                xRatio = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 16f + 6f,
                alpha = Random.nextFloat() * 0.4f + 0.15f,
                speed = Random.nextFloat() * 0.4f + 0.2f,
                colorIndex = it % 3
            )
        }
    }

    // Progress and audio trigger
    LaunchedEffect(Unit) {
        if (!soundTriggered) {
            soundTriggered = true
            onStartSound()
        }

        // Simulate high-tech DSP boot sequence
        val steps = listOf(
            0.15f to "Iniciando procesador DSP 32-bit Float...",
            0.35f to "Cargando bancos de corte y cajas brasileñas...",
            0.60f to "Sincronizando crossover activo 4 vías...",
            0.82f to "Calibrando ecualización paramétrica y limitador...",
            1.00f to "¡Sistema Car Audio energizado y listo!"
        )

        for ((targetProg, text) in steps) {
            statusText = text
            val current = progress
            val diff = targetProg - current
            val subSteps = 10
            for (s in 1..subSteps) {
                progress = current + diff * (s.toFloat() / subSteps)
                delay(40)
            }
            delay(180)
        }

        delay(400)
        onEnterApp()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0D1424),
                        Color(0xFF03070E)
                    )
                )
            )
            .testTag("car_audio_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Floating audio neon bubbles canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            bubbles.forEachIndexed { i, b ->
                val dynamicY = ((b.initialY + (wavePhase / 6.28f) * b.speed) % 1.0f) * height
                val dynamicX = (b.xRatio * width) + sin(wavePhase + i) * 15f
                val color = when (b.colorIndex) {
                    0 -> theme.primaryColor
                    1 -> theme.accentColor
                    else -> Color(0xFF00E5FF)
                }

                drawCircle(
                    color = color.copy(alpha = b.alpha * auraAlpha),
                    radius = b.radius,
                    center = Offset(dynamicX, height - dynamicY)
                )
            }

            // Expanding ripple rings around center
            val centerOffset = Offset(width / 2f, height * 0.40f)
            val ringRadius1 = 120.dp.toPx() * pulseScale
            val ringRadius2 = 145.dp.toPx() * (2.02f - pulseScale)

            drawCircle(
                color = theme.primaryColor.copy(alpha = 0.22f * auraAlpha),
                radius = ringRadius1,
                center = centerOffset
            )
            drawCircle(
                color = theme.accentColor.copy(alpha = 0.15f * auraAlpha),
                radius = ringRadius2,
                center = centerOffset
            )
        }

        // Main Splash Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Central Logo Bubble Container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Outer glowing neon aura
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        theme.primaryColor.copy(alpha = auraAlpha * 0.7f),
                                        theme.accentColor.copy(alpha = auraAlpha * 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .blur(14.dp)
                    )

                    // Secondary decorative neon ring
                    Box(
                        modifier = Modifier
                            .size(174.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        theme.primaryColor,
                                        theme.accentColor,
                                        Color(0xFF00E5FF),
                                        theme.primaryColor
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner Logo Image Card in bubble
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0F1D))
                            .border(2.dp, theme.primaryColor.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_car_audio_logo_brazil_1788873346889),
                            contentDescription = "Logo Car Audio DSP",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Title
                Text(
                    text = "CAR AUDIO DSP PRO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "SOM AUTOMOTIVO & BRASIL EDITION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accentColor,
                    letterSpacing = 2.sp
                )
            }

            // Bottom Loading & Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Bubble Progress Indicator Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF151C2C))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        theme.primaryColor,
                                        theme.accentColor
                                    )
                                )
                            )
                    )
                }

                // Percentage text
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Enter now or Replay Startup Sound
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onStartSound() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = theme.accentColor
                        ),
                        modifier = Modifier.testTag("btn_replay_splash_sound")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sonido de Inicio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onEnterApp() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = theme.onPrimaryColor
                        ),
                        modifier = Modifier.testTag("btn_enter_car_audio")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ENTRAR", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
