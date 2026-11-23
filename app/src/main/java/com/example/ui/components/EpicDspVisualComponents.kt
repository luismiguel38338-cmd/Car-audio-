package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deep OLED obsidian backdrop with subtle ambient violet/purple radial LED illumination.
 */
@Composable
fun EpicAmbientBackdrop(
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            theme.primaryColor.copy(alpha = 0.16f),
                            theme.accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.08f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.18f, size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            theme.accentColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.82f),
                        radius = size.width * 0.75f
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.88f, size.height * 0.82f)
                )
            }
    ) {
        content()
    }
}

/**
 * Modern glowing violet/purple LED action button with silver specular rim.
 */
@Composable
fun GlowActionButton(
    text: String,
    theme: CarAudioThemeType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val containerBrush = if (isPrimary) {
        Brush.horizontalGradient(
            listOf(
                theme.primaryColor,
                theme.accentColor
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                theme.cardColor.copy(alpha = 0.85f),
                theme.surfaceColor.copy(alpha = 0.90f)
            )
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isPrimary) theme.onPrimaryColor else theme.textColor,
            disabledContainerColor = theme.cardColor.copy(alpha = 0.45f),
            disabledContentColor = theme.textSecondaryColor
        ),
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    theme.silverDetailColor.copy(alpha = if (isPrimary) 0.45f else 0.22f),
                    theme.accentColor.copy(alpha = if (isPrimary) 0.85f else 0.42f)
                )
            )
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBrush, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 3D Metallic Rotary Hardware Knob for Bass, Sub, Frequency & DSP studio controls.
 * Uses horizontal drag + tap micro-adjustments so vertical screen scrolling never conflicts.
 */
@Composable
fun RotaryKnob3D(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    theme: CarAudioThemeType,
    enabled: Boolean = true,
    decimals: Int = 1,
    size: Dp = 94.dp,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val span = (range.endInclusive - range.start).coerceAtLeast(0.001f)
    val norm = ((value - range.start) / span).coerceIn(0f, 1f)
    val latestValue by rememberUpdatedState(value)
    val latestOnChange by rememberUpdatedState(onValueChange)

    val formattedValue = remember(value, decimals, unit) {
        val v = value.coerceIn(range.start, range.endInclusive)
        if (decimals == 0) "${v.toInt()} $unit".trim()
        else "%.${decimals}f %s".format(v, unit).trim()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier.semantics {
            contentDescription = "$label: $formattedValue"
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (enabled) {
                        Modifier.pointerInput(range.start, range.endInclusive) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaRatio = dragAmount / (size.toPx() * 1.45f)
                                val next = (latestValue + deltaRatio * span)
                                    .coerceIn(range.start, range.endInclusive)
                                latestOnChange(next)
                            }
                        }
                    } else Modifier
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = this.size.minDimension / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val arcInset = 6.dp.toPx()
                val arcSize = Size(this.size.width - arcInset * 2f, this.size.height - arcInset * 2f)
                val arcTopLeft = Offset(arcInset, arcInset)

                // Subtle outer violet aura when active
                if (enabled && norm > 0.04f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                theme.primaryColor.copy(alpha = 0.24f * norm + 0.06f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                }

                // Tick marks around 270 deg sweep (-225 to +45 in Compose arc coordinates)
                val tickCount = 15
                for (i in 0 until tickCount) {
                    val frac = i / (tickCount - 1f)
                    val angleDeg = 135f + frac * 270f
                    val angleRad = (angleDeg * PI / 180.0).toFloat()
                    val innerTick = r - 10.dp.toPx()
                    val outerTick = r - 5.dp.toPx()
                    val tickLit = enabled && frac <= norm + 0.02f
                    drawLine(
                        color = if (tickLit) theme.accentColor else theme.silverDetailColor.copy(alpha = 0.22f),
                        start = Offset(
                            center.x + cos(angleRad) * innerTick,
                            center.y + sin(angleRad) * innerTick
                        ),
                        end = Offset(
                            center.x + cos(angleRad) * outerTick,
                            center.y + sin(angleRad) * outerTick
                        ),
                        strokeWidth = if (tickLit) 2.dp.toPx() else 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Inactive dark metallic track arc
                drawArc(
                    color = Color(0xFF1E1736),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active glowing violet LED arc
                if (norm > 0.005f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                theme.primaryColor,
                                theme.accentColor,
                                theme.silverDetailColor,
                                theme.primaryColor
                            ),
                            center = center
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * norm,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        alpha = if (enabled) 1f else 0.35f,
                        style = Stroke(width = 4.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 3D Brushed metallic bezel
                val bezelR = r * 0.72f
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF4B4563),
                            Color(0xFF141024),
                            Color(0xFF2B2344)
                        ),
                        start = Offset(center.x - bezelR, center.y - bezelR),
                        end = Offset(center.x + bezelR, center.y + bezelR)
                    ),
                    radius = bezelR,
                    center = center
                )

                // Recessed inner obsidian dial with 3D specular highlight
                val innerR = r * 0.62f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFF221A3D),
                            Color(0xFF0A0714),
                            Color(0xFF05040A)
                        ),
                        center = Offset(center.x - innerR * 0.28f, center.y - innerR * 0.28f),
                        radius = innerR * 1.35f
                    ),
                    radius = innerR,
                    center = center
                )

                // Specular silver rim highlight
                drawCircle(
                    color = theme.silverDetailColor.copy(alpha = if (enabled) 0.30f else 0.12f),
                    radius = innerR,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Rotating LED pointer notch
                val pointerAngle = ((135f + norm * 270f) * PI / 180.0).toFloat()
                val pStart = Offset(
                    center.x + cos(pointerAngle) * (innerR * 0.34f),
                    center.y + sin(pointerAngle) * (innerR * 0.34f)
                )
                val pEnd = Offset(
                    center.x + cos(pointerAngle) * (innerR * 0.84f),
                    center.y + sin(pointerAngle) * (innerR * 0.84f)
                )
                drawLine(
                    color = if (enabled) Color.White else theme.textSecondaryColor,
                    start = pStart,
                    end = pEnd,
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                if (enabled) {
                    drawCircle(
                        color = theme.accentColor.copy(alpha = 0.65f),
                        radius = 3.dp.toPx(),
                        center = pEnd
                    )
                }
            }
        }

        // Value readout badge + accessible quick tap -/+ controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val step = (span / 18f)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.cardColor.copy(alpha = 0.85f))
                    .clickable(enabled = enabled) {
                        onValueChange((value - step).coerceIn(range.start, range.endInclusive))
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = theme.silverDetailColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.backgroundColor.copy(alpha = 0.90f),
                border = BorderStroke(
                    0.8.dp,
                    if (enabled) theme.primaryColor.copy(alpha = 0.45f) else theme.cardColor
                )
            ) {
                Text(
                    text = formattedValue,
                    color = if (enabled) theme.accentColor else theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.cardColor.copy(alpha = 0.85f))
                    .clickable(enabled = enabled) {
                        onValueChange((value + step).coerceIn(range.start, range.endInclusive))
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = theme.silverDetailColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = label,
            color = if (enabled) theme.textColor else theme.textSecondaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Animated EQ Frequency Response Curve Canvas (20 Hz -> 20 kHz) with smooth violet LED glow.
 */
@Composable
fun AnimatedFrequencyCurveCanvas(
    bands: List<Float>,
    enabled: Boolean,
    isPlaying: Boolean,
    bassEnergy: Float,
    trebleEnergy: Float,
    theme: CarAudioThemeType,
    liteVisuals: Boolean = false,
    modifier: Modifier = Modifier
) {
    val phase = if (isPlaying && !liteVisuals) {
        val transition = rememberInfiniteTransition(label = "eq_curve_shimmer")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "eq_phase"
        )
        p
    } else 0f

    val animMs = if (liteVisuals) 40 else 90
    val smoothBass by animateFloatAsState(if (isPlaying) bassEnergy else 0f, tween(animMs), label = "curve_bass")
    val smoothTreble by animateFloatAsState(if (isPlaying) trebleEnergy else 0f, tween(animMs), label = "curve_treble")

    GlassStudioCard(
        theme = theme,
        highlightBorder = enabled,
        modifier = modifier.testTag("eq_frequency_curve_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURVA DE RESPUESTA DSP",
                    color = theme.silverDetailColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = if (enabled) "20 Hz — 20 kHz" else "BYPASS 0 dB",
                    color = if (enabled) theme.accentColor else theme.textSecondaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.backgroundColor.copy(alpha = 0.92f))
                    .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // Reference grid (+12, 0, -12 dB)
                    listOf(0.15f, 0.5f, 0.85f).forEachIndexed { idx, frac ->
                        val y = h * frac
                        drawLine(
                            color = if (idx == 1) theme.silverDetailColor.copy(alpha = 0.26f)
                            else theme.primaryColor.copy(alpha = 0.14f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = if (idx == 1) 1.2f else 0.8f
                        )
                    }
                    // Vertical octave guides
                    for (i in 1..4) {
                        val x = w * (i / 5f)
                        drawLine(
                            color = theme.primaryColor.copy(alpha = 0.10f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 0.8f
                        )
                    }

                    val count = bands.size.coerceAtLeast(2)
                    val points = ArrayList<Offset>(count)
                    for (i in 0 until count) {
                        val x = (i / (count - 1).toFloat()) * w
                        val rawDb = if (enabled) bands[i].coerceIn(-15f, 15f) else 0f
                        val energyPulse = if (isPlaying && enabled) {
                            val freqWeight = 1f - (i / count.toFloat())
                            val liveBump = (freqWeight * smoothBass + (1f - freqWeight) * smoothTreble) * 1.4f
                            sin(phase + i * 0.45f) * liveBump
                        } else 0f
                        val effectiveDb = (rawDb + energyPulse).coerceIn(-15f, 15f)
                        val normY = 0.5f - (effectiveDb / 32f)
                        points.add(Offset(x, (normY * h).coerceIn(4f, h - 4f)))
                    }

                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.lastIndex) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(w, midY)
                        lineTo(0f, midY)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                theme.primaryColor.copy(alpha = if (enabled) 0.38f else 0.10f),
                                theme.accentColor.copy(alpha = 0.04f)
                            )
                        )
                    )

                    if (enabled && !liteVisuals) {
                        drawPath(
                            path = linePath,
                            color = theme.primaryColor.copy(alpha = 0.36f),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            listOf(
                                theme.primaryColor,
                                Color.White,
                                theme.accentColor
                            )
                        ),
                        alpha = if (enabled) 1f else 0.4f,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("31 Hz", "125 Hz", "500 Hz", "2 kHz", "8 kHz", "16 kHz").forEach { freq ->
                    Text(
                        text = freq,
                        color = theme.textSecondaryColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Bass, Treble & Output Power LED Meters with metallic framing and violet glow.
 */
@Composable
fun EpicTriBandPowerMeters(
    isPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    dspMasterEnabled: Boolean,
    theme: CarAudioThemeType,
    liteVisuals: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animMs = if (liteVisuals) 45 else 95
    val bass by animateFloatAsState(if (isPlaying) bassEnergy.coerceIn(0.06f, 1f) else 0.08f, tween(animMs), label = "m_bass")
    val treble by animateFloatAsState(if (isPlaying) trebleEnergy.coerceIn(0.06f, 1f) else 0.08f, tween(animMs), label = "m_treble")
    val power by animateFloatAsState(
        targetValue = if (isPlaying) ((bassEnergy * 0.50f + midEnergy * 0.30f + trebleEnergy * 0.20f) * if (dspMasterEnabled) 1.0f else 0.72f).coerceIn(0.08f, 1f) else 0.08f,
        animationSpec = tween(animMs),
        label = "m_power"
    )

    GlassStudioCard(
        theme = theme,
        highlightBorder = isPlaying && dspMasterEnabled,
        modifier = modifier.testTag("epic_tri_band_meters")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MEDIDORES DE GRAVES, AGUDOS Y POTENCIA",
                    color = theme.silverDetailColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (isPlaying) "ACTIVO" else "STANDBY",
                    color = if (isPlaying) theme.accentColor else theme.textSecondaryColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            TriMeterBarRow("GRAVES (SUB/BASS)", bass, theme.primaryColor, theme)
            TriMeterBarRow("AGUDOS (PRES/AIR)", treble, theme.accentColor, theme)
            TriMeterBarRow("POTENCIA SALIDA", power, theme.silverDetailColor, theme)
        }
    }
}

@Composable
private fun TriMeterBarRow(
    label: String,
    level: Float,
    leadColor: Color,
    theme: CarAudioThemeType
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = theme.textSecondaryColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(level.coerceIn(0f, 1f) * 100).toInt()}%",
                color = theme.textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(theme.backgroundColor)
                .border(0.7.dp, theme.cardColor, RoundedCornerShape(5.dp))
        ) {
            val segments = 18
            val gap = 2.dp.toPx()
            val segW = ((size.width - gap * (segments - 1)) / segments).coerceAtLeast(2f)
            val litCount = (level.coerceIn(0f, 1f) * segments).toInt()
            val r = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            for (i in 0 until segments) {
                val active = i < litCount
                val segColor = when {
                    !active -> theme.cardColor.copy(alpha = 0.55f)
                    i >= segments - 2 -> theme.clipAlertColor
                    i >= segments - 6 -> theme.accentColor
                    else -> leadColor
                }
                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(i * (segW + gap), 1.dp.toPx()),
                    size = Size(segW, size.height - 2.dp.toPx()),
                    cornerRadius = r
                )
            }
        }
    }
}
