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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Commercial Studio Signal Bar palette:
 * Graphite / Silver / Crisp White / Discreet Ice Blue.
 */
object StudioLedColors {
    val SILVER = Color(0xFFD6DCE5)
    val ICE_BLUE = Color(0xFF78B7E8)
    val WHITE = Color(0xFFF5F7FA)

    val bandLabels: List<String> = listOf("SUB", "GRAVE", "MEDIO", "PRES", "AGUD")
    val bandColors: List<Color> = listOf(ICE_BLUE, SILVER, WHITE, SILVER, ICE_BLUE)
}

/**
 * Commercial Studio Signal & Band Indicator Bar.
 * Smoothly reacts to bass, mid, and treble energy while playing and rests subtly when paused.
 */
@Composable
fun StudioSignalIndicatorBar(
    isAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    theme: CarAudioThemeType,
    liteVisuals: Boolean = false,
    title: String = "INDICADOR DE SEÑAL Y BANDAS",
    showMiniBars: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animDuration = if (liteVisuals) 45 else 100

    val targetIntensities = remember(isAudioPlaying, bassEnergy, midEnergy, trebleEnergy) {
        if (!isAudioPlaying) {
            listOf(0.18f, 0.18f, 0.18f, 0.18f, 0.18f)
        } else {
            val b = bassEnergy.coerceIn(0.15f, 1f)
            val m = midEnergy.coerceIn(0.15f, 1f)
            val t = trebleEnergy.coerceIn(0.15f, 1f)
            listOf(
                (0.24f + b * 0.76f).coerceIn(0.2f, 1f),
                (0.22f + (b * 0.55f + m * 0.45f) * 0.78f).coerceIn(0.2f, 1f),
                (0.22f + m * 0.78f).coerceIn(0.2f, 1f),
                (0.20f + (m * 0.45f + t * 0.55f) * 0.80f).coerceIn(0.2f, 1f),
                (0.20f + t * 0.80f).coerceIn(0.2f, 1f)
            )
        }
    }

    val led0 by animateFloatAsState(targetIntensities[0], tween(animDuration), label = "band0")
    val led1 by animateFloatAsState(targetIntensities[1], tween(animDuration), label = "band1")
    val led2 by animateFloatAsState(targetIntensities[2], tween(animDuration), label = "band2")
    val led3 by animateFloatAsState(targetIntensities[3], tween(animDuration), label = "band3")
    val led4 by animateFloatAsState(targetIntensities[4], tween(animDuration), label = "band4")
    val intensities = listOf(led0, led1, led2, led3, led4)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_signal_indicator_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(
            1.dp,
            if (isAudioPlaying) theme.accentColor.copy(alpha = 0.45f) else theme.cardColor
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.6.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isAudioPlaying) theme.accentColor.copy(alpha = 0.16f) else theme.cardColor
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isAudioPlaying) "ACTIVO" else "EN REPOSO",
                        color = if (isAudioPlaying) theme.accentColor else theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

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
                StudioLedColors.bandLabels.forEachIndexed { idx, label ->
                    val intensity = intensities[idx]
                    val baseColor = if (idx == 0 || idx == 4) theme.accentColor else theme.primaryColor
                    val haloAlpha = if (isAudioPlaying && !liteVisuals) (intensity * 0.28f) else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.testTag("signal_band_dot_$idx")
                    ) {
                        Box(
                            modifier = Modifier.size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (haloAlpha > 0.04f) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(baseColor.copy(alpha = haloAlpha))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(baseColor.copy(alpha = intensity))
                                    .border(
                                        0.8.dp,
                                        baseColor.copy(alpha = if (isAudioPlaying) 0.85f else 0.30f),
                                        CircleShape
                                    )
                            )
                        }
                        Text(
                            text = label,
                            color = if (isAudioPlaying) {
                                theme.textColor.copy(alpha = 0.85f)
                            } else {
                                theme.textSecondaryColor.copy(alpha = 0.6f)
                            },
                            fontSize = 10.sp,
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
                    val bars = 16
                    val gap = 3.dp.toPx()
                    val bw = ((size.width - gap * (bars - 1)) / bars).coerceAtLeast(2f)
                    val corner = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    for (i in 0 until bars) {
                        val bandWeight = when {
                            i < 5 -> bassEnergy
                            i < 11 -> midEnergy
                            else -> trebleEnergy
                        }
                        val wave = if (isAudioPlaying) {
                            (0.20f + bandWeight * (0.55f + (i % 3) * 0.14f)).coerceIn(0.14f, 1f)
                        } else {
                            0.12f
                        }
                        val h = size.height * wave
                        val barColor = if (i % 4 == 0) theme.accentColor else theme.primaryColor
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.30f))),
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
