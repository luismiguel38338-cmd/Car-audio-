package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType

/**
 * Exact 5-LED indicator palette required across every sound panel:
 * ROJO - VERDE - ROJO - VERDE - BLANCO
 */
object FiveLedColors {
    val RED = Color(0xFFFF2A3C)
    val GREEN = Color(0xFF00FF87)
    val WHITE = Color(0xFFF5F9FF)

    val orderedColors: List<Color> = listOf(RED, GREEN, RED, GREEN, WHITE)
    val orderedNames: List<String> = listOf("ROJO", "VERDE", "ROJO", "VERDE", "BLANCO")
}

/**
 * Dynamic 5-LED hardware indicator strip (ROJO - VERDE - ROJO - VERDE - BLANCO).
 * Reacts smoothly to live bass/mid/treble energy while audio plays and rests dimly when paused.
 */
@Composable
fun FiveLedIndicatorBar(
    isAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    theme: CarAudioThemeType,
    liteVisuals: Boolean = false,
    title: String = "INDICADOR DINÁMICO 5-LED",
    showMiniBars: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animDuration = if (liteVisuals) 40 else 95

    // Map each LED to distinct musical frequency bands for organic stereo rhythmic movement:
    // 0: ROJO (Sub-Bass), 1: VERDE (Mid-Bass), 2: ROJO (Vocal Punch), 3: VERDE (Presence), 4: BLANCO (Treble Peak)
    val targetIntensities = remember(isAudioPlaying, bassEnergy, midEnergy, trebleEnergy) {
        if (!isAudioPlaying) {
            listOf(0.14f, 0.14f, 0.14f, 0.14f, 0.16f)
        } else {
            val b = bassEnergy.coerceIn(0.18f, 1f)
            val m = midEnergy.coerceIn(0.16f, 1f)
            val t = trebleEnergy.coerceIn(0.15f, 1f)
            listOf(
                (0.28f + b * 0.72f).coerceIn(0.2f, 1f),
                (0.24f + (b * 0.5f + m * 0.5f) * 0.76f).coerceIn(0.2f, 1f),
                (0.24f + m * 0.76f).coerceIn(0.2f, 1f),
                (0.22f + (m * 0.45f + t * 0.55f) * 0.78f).coerceIn(0.2f, 1f),
                (0.22f + t * 0.78f).coerceIn(0.2f, 1f)
            )
        }
    }

    val led0 by animateFloatAsState(targetIntensities[0], tween(animDuration), label = "led0")
    val led1 by animateFloatAsState(targetIntensities[1], tween(animDuration), label = "led1")
    val led2 by animateFloatAsState(targetIntensities[2], tween(animDuration), label = "led2")
    val led3 by animateFloatAsState(targetIntensities[3], tween(animDuration), label = "led3")
    val led4 by animateFloatAsState(targetIntensities[4], tween(animDuration), label = "led4")
    val intensities = listOf(led0, led1, led2, led3, led4)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("five_led_indicator_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(
            1.dp,
            if (isAudioPlaying) theme.primaryColor.copy(alpha = 0.55f) else theme.cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = theme.textSecondaryColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isAudioPlaying) theme.accentColor.copy(alpha = 0.18f) else theme.cardColor
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isAudioPlaying) "EN RITMO" else "REPOSO",
                        color = if (isAudioPlaying) theme.accentColor else theme.textSecondaryColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 5 LED row: ROJO - VERDE - ROJO - VERDE - BLANCO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.backgroundColor)
                    .border(1.dp, theme.cardColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FiveLedColors.orderedColors.forEachIndexed { idx, baseColor ->
                    val intensity = intensities[idx]
                    val haloAlpha = if (isAudioPlaying && !liteVisuals) (intensity * 0.42f) else 0f
                    val coreColor = baseColor.copy(alpha = intensity)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.testTag("five_led_dot_$idx")
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (haloAlpha > 0.04f) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(baseColor.copy(alpha = haloAlpha))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(coreColor)
                                    .border(
                                        0.8.dp,
                                        baseColor.copy(alpha = if (isAudioPlaying) 0.9f else 0.35f),
                                        CircleShape
                                    )
                            )
                        }
                        Text(
                            text = FiveLedColors.orderedNames[idx],
                            color = if (isAudioPlaying) baseColor.copy(alpha = 0.85f) else theme.textSecondaryColor.copy(alpha = 0.55f),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (showMiniBars) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.backgroundColor)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    val bars = 12
                    val gap = 3.dp.toPx()
                    val bw = ((size.width - gap * (bars - 1)) / bars).coerceAtLeast(2f)
                    val corner = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    for (i in 0 until bars) {
                        val bandWeight = when {
                            i < 4 -> bassEnergy
                            i < 8 -> midEnergy
                            else -> trebleEnergy
                        }
                        val wave = if (isAudioPlaying) {
                            (0.22f + bandWeight * (0.55f + (i % 3) * 0.15f)).coerceIn(0.15f, 1f)
                        } else {
                            0.12f
                        }
                        val h = size.height * wave
                        val barColor = if (i < 4) theme.primaryColor else if (i < 9) theme.accentColor else Color(0xFFF5F9FF)
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.35f))),
                            topLeft = Offset(i * (bw + gap), size.height - h),
                            size = Size(bw, h),
                            cornerRadius = corner
                        )
                    }
                }
            }
        }
    }
}
