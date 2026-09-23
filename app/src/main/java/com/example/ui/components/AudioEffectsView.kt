package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings

@Composable
fun AudioEffectsView(
    dspSettings: DspSettings,
    theme: CarAudioThemeType,
    onBassBoostToggle: (Boolean, Float, Float) -> Unit,
    onSubBassToggle: (Boolean, Float, Float) -> Unit,
    onLoudnessToggle: (Boolean, Float) -> Unit,
    onStereoWidthChange: (Float) -> Unit,
    onBalancePanChange: (Float) -> Unit,
    onCompressorChange: (Boolean, Float, Float, Float, Float) -> Unit,
    onLimiterChange: (Boolean, Float) -> Unit,
    onExciterChange: (Boolean, Float) -> Unit,
    onPresenceChange: (Boolean, Float) -> Unit,
    onClarityChange: (Boolean, Float) -> Unit,
    onHpfChange: (Boolean, Float, Int, Float) -> Unit,
    onLpfChange: (Boolean, Float, Int, Float) -> Unit,
    onBandPassChange: (Boolean, Float, Float, Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    val categories = listOf("EFECTOS DSP", "FILTROS / CROSSOVER")

    val cardBg = Color(0xFF0C1422)
    val neonGreen = Color(0xFF00E676)
    val neonCyan = Color(0xFF00B0FF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040810))
    ) {
        // Tab Header: EFECTOS DSP / FILTROS CROSSOVER
        TabRow(
            selectedTabIndex = selectedCategoryTab,
            containerColor = Color(0xFF070D18),
            contentColor = neonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                    color = neonGreen,
                    height = 3.dp
                )
            }
        ) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedCategoryTab == index,
                    onClick = { selectedCategoryTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedCategoryTab == index) FontWeight.Black else FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = if (selectedCategoryTab == index) Color.White else Color.Gray
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedCategoryTab == 0) {
                // ==========================================
                // SECTION 1: EFECTOS DSP
                // ==========================================

                // 1. Bass Boost
                item {
                    EffectControlCard(
                        title = "BASS BOOST",
                        subtitle = "Realce armónico dinámico de graves (Modo SPL Punch)",
                        icon = Icons.Default.Speed,
                        isEnabled = dspSettings.bassBoostEnabled,
                        onToggle = { onBassBoostToggle(it, dspSettings.bassBoostDb, dspSettings.bassBoostFreqHz) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Frequency selector: 30Hz to 80Hz
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Frecuencia Central:", fontSize = 11.sp, color = Color.Gray)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val freqs = listOf(30f, 35f, 40f, 45f, 50f, 55f, 60f, 80f)
                                    items(freqs) { freq ->
                                        val isSel = dspSettings.bassBoostFreqHz == freq
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) neonGreen else Color(0xFF131D2D))
                                                .border(1.dp, if (isSel) neonGreen else Color(0xFF263238), RoundedCornerShape(8.dp))
                                                .clickable(enabled = dspSettings.bassBoostEnabled) {
                                                    onBassBoostToggle(dspSettings.bassBoostEnabled, dspSettings.bassBoostDb, freq)
                                                }
                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${freq.toInt()}Hz",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Boost Gain Slider (0 to 24 dB Extreme Power)
                            FxSliderRow(
                                label = "Ganancia de Realce (Punch)",
                                value = dspSettings.bassBoostDb,
                                range = 0f..24f,
                                unit = "dB",
                                enabled = dspSettings.bassBoostEnabled,
                                onValueChange = { onBassBoostToggle(dspSettings.bassBoostEnabled, it, dspSettings.bassBoostFreqHz) }
                            )
                        }
                    }
                }

                // 2. Sub Bass
                item {
                    EffectControlCard(
                        title = "SUB BASS EXTREMO",
                        subtitle = "Graves profundos en ultra-bajas frecuencias (25Hz - 60Hz)",
                        icon = Icons.Default.Waves,
                        isEnabled = dspSettings.subBassEnabled,
                        onToggle = { onSubBassToggle(it, dspSettings.subBassBoostDb, dspSettings.subBassFreqHz) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Frecuencia Sub-Grave:", fontSize = 11.sp, color = Color.Gray)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val subFreqs = listOf(25f, 30f, 35f, 40f, 50f, 60f)
                                    items(subFreqs) { freq ->
                                        val isSel = dspSettings.subBassFreqHz == freq
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) neonCyan else Color(0xFF131D2D))
                                                .border(1.dp, if (isSel) neonCyan else Color(0xFF263238), RoundedCornerShape(8.dp))
                                                .clickable(enabled = dspSettings.subBassEnabled) {
                                                    onSubBassToggle(dspSettings.subBassEnabled, dspSettings.subBassBoostDb, freq)
                                                }
                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${freq.toInt()}Hz",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            FxSliderRow(
                                label = "Potencia Sub-Grave",
                                value = dspSettings.subBassBoostDb,
                                range = 0f..18f,
                                unit = "dB",
                                enabled = dspSettings.subBassEnabled,
                                onValueChange = { onSubBassToggle(dspSettings.subBassEnabled, it, dspSettings.subBassFreqHz) }
                            )
                        }
                    }
                }

                // 3. Loudness
                item {
                    EffectControlCard(
                        title = "LOUDNESS CONTOUR",
                        subtitle = "Compensación acústica Fletcher-Munson a volumen moderado",
                        icon = Icons.Default.Hearing,
                        isEnabled = dspSettings.loudnessEnabled,
                        onToggle = { onLoudnessToggle(it, dspSettings.loudnessGainDb) }
                    ) {
                        FxSliderRow(
                            label = "Nivel de Contorno",
                            value = dspSettings.loudnessGainDb,
                            range = 0f..15f,
                            unit = "dB",
                            enabled = dspSettings.loudnessEnabled,
                            onValueChange = { onLoudnessToggle(dspSettings.loudnessEnabled, it) }
                        )
                    }
                }

                // 4. Stereo Width & Balance
                item {
                    EffectControlCard(
                        title = "STEREO WIDTH & BALANCE",
                        subtitle = "Apertura de escenario sonoro y paneo automotriz",
                        icon = Icons.Default.GraphicEq,
                        isEnabled = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FxSliderRow(
                                label = "Amplitud Estéreo (Spatializer)",
                                value = dspSettings.stereoWidthPercent,
                                range = 0f..250f,
                                unit = "%",
                                enabled = true,
                                onValueChange = onStereoWidthChange
                            )
                            FxSliderRow(
                                label = "Balance / Paneo L-R",
                                value = dspSettings.balancePan,
                                range = -1.0f..1.0f,
                                unit = "Pan",
                                enabled = true,
                                onValueChange = onBalancePanChange
                            )
                        }
                    }
                }

                // 5. Compressor
                item {
                    EffectControlCard(
                        title = "COMPRESOR DE AUDIO",
                        subtitle = "Control de dinámica y reducción de transitorios para SPL",
                        icon = Icons.Default.Bolt,
                        isEnabled = dspSettings.compressorEnabled,
                        onToggle = { onCompressorChange(it, dspSettings.compressorThresholdDb, dspSettings.compressorRatio, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FxSliderRow(
                                label = "Umbral (Threshold)",
                                value = dspSettings.compressorThresholdDb,
                                range = -48f..0f,
                                unit = "dBFS",
                                enabled = dspSettings.compressorEnabled,
                                onValueChange = { onCompressorChange(dspSettings.compressorEnabled, it, dspSettings.compressorRatio, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs) }
                            )
                            FxSliderRow(
                                label = "Relación (Ratio)",
                                value = dspSettings.compressorRatio,
                                range = 1f..20f,
                                unit = ":1",
                                enabled = dspSettings.compressorEnabled,
                                onValueChange = { onCompressorChange(dspSettings.compressorEnabled, dspSettings.compressorThresholdDb, it, dspSettings.compressorAttackMs, dspSettings.compressorReleaseMs) }
                            )
                        }
                    }
                }

                // 6. Limiter
                item {
                    EffectControlCard(
                        title = "LIMITADOR DE SALIDA (ANTI-CLIP)",
                        subtitle = "Techo de seguridad para prevenir distorsión y daño en bocinas",
                        icon = Icons.Default.Warning,
                        isEnabled = dspSettings.limiterEnabled,
                        onToggle = { onLimiterChange(it, dspSettings.limiterCeilingDb) }
                    ) {
                        FxSliderRow(
                            label = "Techo Máximo (Ceiling)",
                            value = dspSettings.limiterCeilingDb,
                            range = -24.0f..0.0f,
                            unit = "dBFS",
                            enabled = dspSettings.limiterEnabled,
                            onValueChange = { onLimiterChange(dspSettings.limiterEnabled, it) }
                        )
                    }
                }

                // 7. Exciter, Presence & Clarity
                item {
                    EffectControlCard(
                        title = "CLARIDAD, PRESENCIA Y EXCITER",
                        subtitle = "Realce de transitorios, voces cristalinas y aire",
                        icon = Icons.Default.Tune,
                        isEnabled = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FxSliderRow(
                                label = "Harmonic Exciter",
                                value = dspSettings.exciterLevel,
                                range = 0f..10f,
                                unit = "Nivel",
                                enabled = true,
                                onValueChange = { onExciterChange(it > 0f, it) }
                            )
                            FxSliderRow(
                                label = "Presencia (Voces Medias)",
                                value = dspSettings.presenceLevel,
                                range = 0f..10f,
                                unit = "Nivel",
                                enabled = true,
                                onValueChange = { onPresenceChange(it > 0f, it) }
                            )
                            FxSliderRow(
                                label = "Claridad (Agudos)",
                                value = dspSettings.clarityLevel,
                                range = 0f..10f,
                                unit = "Nivel",
                                enabled = true,
                                onValueChange = { onClarityChange(it > 0f, it) }
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // SECTION 2: FILTROS / CROSSOVER
                // ==========================================

                // 1. HPF (High Pass Filter)
                item {
                    FilterControlCard(
                        filterName = "HPF - PASA ALTAS",
                        description = "Elimina subgraves peligrosos bajo la sintonía del cajón acústico",
                        isEnabled = dspSettings.hpfEnabled,
                        frequency = dspSettings.hpfFrequencyHz,
                        freqRange = 15f..500f,
                        slopeDb = dspSettings.hpfSlopeDb,
                        qFactor = dspSettings.hpfQ,
                        onToggle = { onHpfChange(it, dspSettings.hpfFrequencyHz, dspSettings.hpfSlopeDb, dspSettings.hpfQ) },
                        onFreqChange = { onHpfChange(dspSettings.hpfEnabled, it, dspSettings.hpfSlopeDb, dspSettings.hpfQ) },
                        onSlopeChange = { onHpfChange(dspSettings.hpfEnabled, dspSettings.hpfFrequencyHz, it, dspSettings.hpfQ) },
                        onQChange = { onHpfChange(dspSettings.hpfEnabled, dspSettings.hpfFrequencyHz, dspSettings.hpfSlopeDb, it) }
                    )
                }

                // 2. LPF (Low Pass Filter)
                item {
                    FilterControlCard(
                        filterName = "LPF - PASA BAJAS",
                        description = "Corta las frecuencias altas para canalizar solo la respuesta del altavoz",
                        isEnabled = dspSettings.lpfEnabled,
                        frequency = dspSettings.lpfFrequencyHz,
                        freqRange = 50f..20000f,
                        slopeDb = dspSettings.lpfSlopeDb,
                        qFactor = dspSettings.lpfQ,
                        onToggle = { onLpfChange(it, dspSettings.lpfFrequencyHz, dspSettings.lpfSlopeDb, dspSettings.lpfQ) },
                        onFreqChange = { onLpfChange(dspSettings.lpfEnabled, it, dspSettings.lpfSlopeDb, dspSettings.lpfQ) },
                        onSlopeChange = { onLpfChange(dspSettings.lpfEnabled, dspSettings.lpfFrequencyHz, it, dspSettings.lpfQ) },
                        onQChange = { onLpfChange(dspSettings.lpfEnabled, dspSettings.lpfFrequencyHz, dspSettings.lpfSlopeDb, it) }
                    )
                }

                // 3. Band Pass
                item {
                    FilterControlCard(
                        filterName = "BAND PASS (PASA BANDA)",
                        description = "Aisla un rango específico para medios y cornetas Chuchero",
                        isEnabled = dspSettings.bandPassEnabled,
                        frequency = dspSettings.bandPassCenterHz,
                        freqRange = 200f..8000f,
                        slopeDb = dspSettings.bandPassSlopeDb,
                        qFactor = dspSettings.bandPassQ,
                        onToggle = { onBandPassChange(it, dspSettings.bandPassCenterHz, dspSettings.bandPassWidthHz, dspSettings.bandPassSlopeDb, dspSettings.bandPassQ) },
                        onFreqChange = { onBandPassChange(dspSettings.bandPassEnabled, it, dspSettings.bandPassWidthHz, dspSettings.bandPassSlopeDb, dspSettings.bandPassQ) },
                        onSlopeChange = { onBandPassChange(dspSettings.bandPassEnabled, dspSettings.bandPassCenterHz, dspSettings.bandPassWidthHz, it, dspSettings.bandPassQ) },
                        onQChange = { onBandPassChange(dspSettings.bandPassEnabled, dspSettings.bandPassCenterHz, dspSettings.bandPassWidthHz, dspSettings.bandPassSlopeDb, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun EffectControlCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1422)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFF1E2D44))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) Color(0xFF00E676) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0x3300E676)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            content()
        }
    }
}

@Composable
fun FilterControlCard(
    filterName: String,
    description: String,
    isEnabled: Boolean,
    frequency: Float,
    freqRange: ClosedFloatingPointRange<Float>,
    slopeDb: Int,
    qFactor: Float,
    onToggle: (Boolean) -> Unit,
    onFreqChange: (Float) -> Unit,
    onSlopeChange: (Int) -> Unit,
    onQChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1422)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) Color(0xFF00E676).copy(alpha = 0.45f) else Color(0xFF1E2D44))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = filterName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0x3300E676)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            // Frequency Slider
            FxSliderRow(
                label = "Frecuencia de Corte",
                value = frequency,
                range = freqRange,
                unit = if (frequency >= 1000f) "kHz" else "Hz",
                enabled = isEnabled,
                onValueChange = onFreqChange
            )

            // Slope Selector: 6, 12, 18, 24, 36, 48 dB/oct
            Column {
                Text(
                    text = "Pendiente de Atenuación (Slope):",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val slopes = listOf(6, 12, 18, 24, 36, 48)
                    items(slopes) { s ->
                        val isSelected = slopeDb == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF00E676) else Color(0xFF131D2D))
                                .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF263238), RoundedCornerShape(8.dp))
                                .clickable(enabled = isEnabled) { onSlopeChange(s) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$s dB/oct",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Q Factor Slider (0.5 to 2.0)
            FxSliderRow(
                label = "Factor Q (Resonancia)",
                value = qFactor,
                range = 0.5f..2.0f,
                unit = "Q",
                enabled = isEnabled,
                onValueChange = onQChange
            )
        }
    }
}

@Composable
fun FxSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val step = when (unit) {
        "%", "Hz" -> if (range.endInclusive > 1000f) 50f else 5f
        "kHz" -> 100f
        "Q" -> 0.1f
        "Pan" -> 0.1f
        else -> 1.0f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.LightGray else Color.Gray
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Step Down
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Color(0xFF162234) else Color(0xFF0D131D))
                        .clickable(enabled = enabled) {
                            onValueChange((value - step).coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.DarkGray)
                }

                // Step Up
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Color(0xFF162234) else Color(0xFF0D131D))
                        .clickable(enabled = enabled) {
                            onValueChange((value + step).coerceIn(range.start, range.endInclusive))
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.DarkGray)
                }

                val displayVal = if (unit == "kHz") String.format("%.2f", value / 1000f) else String.format("%.1f", value)
                Text(
                    text = "$displayVal $unit",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) (if (value > 12f && unit == "dB") Color(0xFFFF9100) else Color(0xFF00E676)) else Color.DarkGray
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = if (value > 12f && unit == "dB") Color(0xFFFF9100) else Color(0xFF00E676),
                activeTrackColor = if (value > 12f && unit == "dB") Color(0xFFFF9100) else Color(0xFF00E676),
                inactiveTrackColor = Color(0xFF1E2838),
                disabledThumbColor = Color.DarkGray,
                disabledActiveTrackColor = Color.DarkGray
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
