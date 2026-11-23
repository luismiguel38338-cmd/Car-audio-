package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CarAudioThemeType

/**
 * Interactive 3D Automotive Soundstage & Subwoofer/Amplifier Visualizer.
 * Optimized for low/mid-tier Android phones via liteVisuals fast path.
 */
@Composable
fun Automotive3DCockpitView(
    theme: CarAudioThemeType,
    isPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    dspMasterEnabled: Boolean,
    liteVisuals: Boolean,
    modifier: Modifier = Modifier
) {
    var visualMode by remember { mutableIntStateOf(0) } // 0 = Cabina 3D, 1 = Subwoofers 3D
    val animMs = if (liteVisuals) 45 else 95

    val smoothBass by animateFloatAsState(
        targetValue = if (isPlaying) bassEnergy.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(animMs),
        label = "cockpit_bass"
    )
    val smoothMid by animateFloatAsState(
        targetValue = if (isPlaying) midEnergy.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(animMs),
        label = "cockpit_mid"
    )
    val smoothTreble by animateFloatAsState(
        targetValue = if (isPlaying) trebleEnergy.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(animMs),
        label = "cockpit_treble"
    )

    GlassStudioCard(
        theme = theme,
        highlightBorder = isPlaying && dspMasterEnabled,
        modifier = modifier.testTag("automotive_3d_cockpit_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CABINA AUTOMOTRIZ 3D • AUDIO DSP",
                        color = theme.accentColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isPlaying) {
                            "Subwoofer ${(smoothBass * 100).toInt()}% • Medios ${(smoothMid * 100).toInt()}% • Tweeters ${(smoothTreble * 100).toInt()}%"
                        } else {
                            "Sistema automotriz listo • Reproduce música para activar vibración 3D"
                        },
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.backgroundColor)
                        .padding(3.dp)
                ) {
                    listOf("Cabina 3D", "Subwoofers").forEachIndexed { idx, label ->
                        val selected = visualMode == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) theme.accentColor else Color.Transparent)
                                .clickable { visualMode = idx }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("cockpit_mode_$idx")
                        ) {
                            Text(
                                text = label,
                                color = if (selected) theme.onPrimaryColor else theme.textSecondaryColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(198.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, theme.cardColor, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_car_audio_cockpit_1794912323387),
                    contentDescription = "Sistema de sonido automotriz 3D con subwoofers y amplificador",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    theme.backgroundColor.copy(alpha = 0.60f),
                                    theme.backgroundColor.copy(alpha = 0.35f),
                                    theme.backgroundColor.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (visualMode == 0) {
                        drawPerspectiveCarCockpit3D(
                            theme = theme,
                            bass = smoothBass,
                            mid = smoothMid,
                            treble = smoothTreble,
                            dspActive = dspMasterEnabled,
                            lite = liteVisuals
                        )
                    } else {
                        drawDualSubwooferRack3D(
                            theme = theme,
                            bass = smoothBass,
                            mid = smoothMid,
                            treble = smoothTreble,
                            dspActive = dspMasterEnabled,
                            lite = liteVisuals
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (dspMasterEnabled) "AMP DSP ACTIVO" else "DSP EN BYPASS",
                        color = if (dspMasterEnabled) theme.accentColor else theme.textSecondaryColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (liteVisuals) "MODO LIGERO 3D" else "RENDER 3D HD",
                        color = theme.primaryColor.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPerspectiveCarCockpit3D(
    theme: CarAudioThemeType,
    bass: Float,
    mid: Float,
    treble: Float,
    dspActive: Boolean,
    lite: Boolean
) {
    val w = size.width
    val h = size.height
    val ledGlowAlpha = if (dspActive) (0.28f + bass * 0.58f).coerceIn(0.2f, 0.9f) else 0.14f

    // 3D perspective cabin grid floor
    val floorYStart = h * 0.48f
    val floorYEnd = h * 0.92f
    for (i in 0..4) {
        val t = i / 4f
        val y = floorYStart + (floorYEnd - floorYStart) * t
        drawLine(
            color = theme.accentColor.copy(alpha = 0.10f + t * 0.16f),
            start = Offset(w * (0.18f - t * 0.14f), y),
            end = Offset(w * (0.82f + t * 0.14f), y),
            strokeWidth = 1.2f
        )
    }

    // 3D Car silhouette frame with rhythm-synced LED underglow
    val carRoofPath = Path().apply {
        moveTo(w * 0.16f, h * 0.52f)
        lineTo(w * 0.28f, h * 0.18f)
        lineTo(w * 0.72f, h * 0.18f)
        lineTo(w * 0.84f, h * 0.52f)
        lineTo(w * 0.90f, h * 0.78f)
        lineTo(w * 0.10f, h * 0.78f)
        close()
    }
    if (!lite) {
        drawPath(
            path = carRoofPath,
            color = theme.accentColor.copy(alpha = ledGlowAlpha * 0.45f),
            style = Stroke(width = 7.dp.toPx())
        )
    }
    drawPath(
        path = carRoofPath,
        color = theme.primaryColor.copy(alpha = 0.65f),
        style = Stroke(width = 1.8.dp.toPx())
    )

    // Left & Right A-Pillar Tweeters pulsing with Treble
    val tweeterRadius = (10.dp.toPx() + treble * 3.dp.toPx())
    listOf(Offset(w * 0.24f, h * 0.30f), Offset(w * 0.76f, h * 0.30f)).forEach { center ->
        drawCircle(
            color = theme.accentColor.copy(alpha = 0.20f + treble * 0.55f),
            radius = tweeterRadius * 1.45f,
            center = center
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, theme.primaryColor, Color(0xFF1F2937)),
                center = center,
                radius = tweeterRadius
            ),
            radius = tweeterRadius,
            center = center
        )
    }

    // Left & Right Door Mid-Woofers pulsing with Mid energy
    val midRadius = (19.dp.toPx() + mid * 4.5.dp.toPx())
    listOf(Offset(w * 0.18f, h * 0.62f), Offset(w * 0.82f, h * 0.62f)).forEach { center ->
        drawSpeakerCone3D(
            center = center,
            outerRadius = midRadius,
            excursion = mid,
            ringColor = theme.primaryColor,
            glowColor = theme.accentColor,
            lite = lite
        )
    }

    // Center 3D Subwoofer & Amplifier Unit pulsing with Bass
    val subRadius = (36.dp.toPx() + bass * 8.dp.toPx())
    drawSpeakerCone3D(
        center = Offset(w * 0.50f, h * 0.56f),
        outerRadius = subRadius,
        excursion = bass,
        ringColor = theme.accentColor,
        glowColor = theme.accentColor,
        lite = lite
    )

    // Rhythm LED light strip across dashboard bottom
    val ledCount = 12
    val stripW = w * 0.74f
    val startX = w * 0.13f
    val ledW = (stripW / ledCount) * 0.68f
    val gap = (stripW / ledCount) * 0.32f
    for (i in 0 until ledCount) {
        val energy = when {
            i < 4 || i > 7 -> bass
            i == 5 || i == 6 -> treble
            else -> mid
        }
        val activeAlpha = if (dspActive) (0.25f + energy * 0.75f) else 0.12f
        drawRoundRect(
            color = theme.accentColor.copy(alpha = activeAlpha),
            topLeft = Offset(startX + i * (ledW + gap), h * 0.83f),
            size = Size(ledW, 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawDualSubwooferRack3D(
    theme: CarAudioThemeType,
    bass: Float,
    mid: Float,
    treble: Float,
    dspActive: Boolean,
    lite: Boolean
) {
    val w = size.width
    val h = size.height

    // 3D Brushed aluminum DSP amplifier housing in center
    val ampW = w * 0.28f
    val ampH = h * 0.62f
    val ampTopLeft = Offset(w * 0.5f - ampW / 2f, h * 0.16f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
        ),
        topLeft = ampTopLeft,
        size = Size(ampW, ampH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = if (dspActive) theme.accentColor.copy(alpha = 0.55f + bass * 0.35f) else theme.cardColor,
        topLeft = ampTopLeft,
        size = Size(ampW, ampH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 1.6.dp.toPx())
    )

    // Cooling fins & power LED meter inside central amp
    for (row in 0..4) {
        val y = ampTopLeft.y + ampH * (0.20f + row * 0.15f)
        val level = if (row % 2 == 0) bass else mid
        drawRoundRect(
            color = if (dspActive) theme.accentColor.copy(alpha = 0.35f + level * 0.65f) else Color.Gray.copy(alpha = 0.25f),
            topLeft = Offset(ampTopLeft.x + 12.dp.toPx(), y),
            size = Size((ampW - 24.dp.toPx()) * (0.35f + level * 0.65f).coerceIn(0.25f, 1f), 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }

    // Dual large 3D subwoofers vibrating to Bass
    val subR = 42.dp.toPx() + bass * 9.dp.toPx()
    listOf(Offset(w * 0.21f, h * 0.48f), Offset(w * 0.79f, h * 0.48f)).forEach { center ->
        drawSpeakerCone3D(
            center = center,
            outerRadius = subR,
            excursion = bass,
            ringColor = theme.accentColor,
            glowColor = theme.accentColor,
            lite = lite
        )
    }

    // Top tweeters pulsing to treble
    listOf(Offset(w * 0.34f, h * 0.22f), Offset(w * 0.66f, h * 0.22f)).forEach { center ->
        val tR = 9.dp.toPx() + treble * 3.dp.toPx()
        drawCircle(
            color = theme.primaryColor.copy(alpha = 0.25f + treble * 0.65f),
            radius = tR * 1.4f,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = tR * 0.55f,
            center = center
        )
    }
}

private fun DrawScope.drawSpeakerCone3D(
    center: Offset,
    outerRadius: Float,
    excursion: Float,
    ringColor: Color,
    glowColor: Color,
    lite: Boolean
) {
    // External LED Halo synchronized with beat
    if (!lite && excursion > 0.05f) {
        drawCircle(
            color = glowColor.copy(alpha = (0.18f + excursion * 0.36f).coerceIn(0f, 0.55f)),
            radius = outerRadius * (1.16f + excursion * 0.14f),
            center = center
        )
    }

    // Chrome / metallic mounting frame
    drawCircle(
        brush = Brush.linearGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFF475569), Color(0xFF94A3B8)),
            start = Offset(center.x - outerRadius, center.y - outerRadius),
            end = Offset(center.x + outerRadius, center.y + outerRadius)
        ),
        radius = outerRadius,
        center = center
    )

    // Ribbed dark rubber surround
    drawCircle(
        color = Color(0xFF0F172A),
        radius = outerRadius * 0.86f,
        center = center
    )
    drawCircle(
        color = ringColor.copy(alpha = 0.40f + excursion * 0.55f),
        radius = outerRadius * 0.88f,
        center = center,
        style = Stroke(width = 1.6.dp.toPx())
    )

    // 3D cone body with depth shading
    val coneRadius = outerRadius * (0.72f + excursion * 0.04f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF090D14)),
            center = center,
            radius = coneRadius
        ),
        radius = coneRadius,
        center = center
    )

    // Center metallic dust cap with 3D specular reflection
    val capRadius = outerRadius * (0.30f + excursion * 0.05f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color.White.copy(alpha = 0.85f),
                ringColor.copy(alpha = 0.75f),
                Color(0xFF0F172A)
            ),
            center = Offset(center.x - capRadius * 0.25f, center.y - capRadius * 0.25f),
            radius = capRadius
        ),
        radius = capRadius,
        center = center
    )
}
