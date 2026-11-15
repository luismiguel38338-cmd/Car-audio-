package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun CarAudioSplashScreen(
    theme: CarAudioThemeType,
    onStartSound: () -> Unit,
    onEnterApp: () -> Unit,
    onSetDontShowAgain: (Boolean) -> Unit,
    initialDontShowAgain: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }
    var bootProgress by remember { mutableFloatStateOf(0.15f) }
    var bootStage by remember { mutableStateOf("Inicializando motor Rojo Studio...") }

    val infiniteTransition = rememberInfiniteTransition(label = "crimson_dial")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = LinearEasing)),
        label = "sweep"
    )

    LaunchedEffect(Unit) {
        onStartSound()
        bootProgress = 0.45f
        bootStage = "Cargando ecualizador de audio en vivo..."
        delay(350)
        bootProgress = 0.80f
        bootStage = "Conectando música local y YouTube..."
        delay(350)
        bootProgress = 1.0f
        bootStage = "Listo • CAR AUDIO DSP PRO v5.0.0"
        delay(300)
        onEnterApp()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0203),
                        Color(0xFF1A0508),
                        Color(0xFF070102)
                    )
                )
            )
            .testTag("car_audio_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(185.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 10.dp.toPx()

                    drawCircle(
                        color = Color(0xFF330A10),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 8.dp.toPx())
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFFFF172B).copy(alpha = 0.1f),
                                Color(0xFFFF172B),
                                Color(0xFFFF5252),
                                Color(0xFFFF172B)
                            ),
                            center = center
                        ),
                        startAngle = sweepAngle,
                        sweepAngle = 250f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val totalTicks = 28
                    for (i in 0 until totalTicks) {
                        val angleRad = ((i.toFloat() / totalTicks) * 360f * PI / 180f).toFloat()
                        val tickLen = if (i % 4 == 0) 10.dp.toPx() else 5.dp.toPx()
                        val innerR = radius - 13.dp.toPx()
                        val outerR = innerR - tickLen
                        val p1 = Offset(center.x + innerR * cos(angleRad), center.y + innerR * sin(angleRad))
                        val p2 = Offset(center.x + outerR * cos(angleRad), center.y + outerR * sin(angleRad))
                        drawLine(
                            color = (if (i % 2 == 0) Color(0xFFFF172B) else Color(0xFFFF5252)).copy(alpha = 0.7f),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.crimson_dsp_logo_1794742268901),
                    contentDescription = "Crimson Studio Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(106.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFFF172B), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CAR AUDIO DSP PRO",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "EDICIÓN TODO ROJO • v5.0.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFF5252),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { bootProgress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFFF172B),
                trackColor = Color(0xFF2B0B10),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bootStage,
                fontSize = 11.sp,
                color = Color(0xFFD18A92),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onEnterApp,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(48.dp)
                    .testTag("btn_enter_studio"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF172B),
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ENTRAR AL ESTUDIO",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Entrar",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    dontShowAgain = !dontShowAgain
                    onSetDontShowAgain(dontShowAgain)
                }
            ) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = {
                        dontShowAgain = it
                        onSetDontShowAgain(it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFF172B),
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "Iniciar directamente sin introducción",
                    color = Color(0xFFD18A92),
                    fontSize = 12.sp
                )
            }
        }
    }
}
