package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.ToneMode
import com.example.viewmodel.CarAudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarAudioToolsView(
    viewModel: CarAudioViewModel,
    currentTheme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    var selectedToolTab by remember { mutableIntStateOf(0) }
    val toolTabs = listOf("Cajones Brasil", "Generador Tonos", "Cajón Estándar", "Cableado AWG", "Bobinas Ohm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Tools Tab Navigation
        TabRow(
            selectedTabIndex = selectedToolTab,
            containerColor = currentTheme.surfaceColor,
            contentColor = currentTheme.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedToolTab]),
                    color = currentTheme.primaryColor,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .testTag("tools_tab_row")
        ) {
            toolTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolTab == index,
                    onClick = { selectedToolTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = if (selectedToolTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (selectedToolTab == index) currentTheme.primaryColor else currentTheme.textSecondaryColor,
                            maxLines = 1
                        )
                    },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Default.Speaker
                            1 -> Icons.Default.GraphicEq
                            2 -> Icons.Default.Inbox
                            3 -> Icons.Default.Cable
                            else -> Icons.Default.Speaker
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedToolTab == index) currentTheme.primaryColor else currentTheme.textSecondaryColor
                        )
                    }
                )
            }
        }

        // Active Tool Sub-screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedToolTab) {
                0 -> BrazilianBoxesView(
                    theme = currentTheme,
                    onApplyBoxCutsToDsp = { hpf, lpf, name ->
                        viewModel.applyBoxCutsToDsp(hpf, lpf, name)
                    }
                )
                1 -> ToneGeneratorSection(viewModel, currentTheme)
                2 -> BoxCalculatorSection(viewModel, currentTheme)
                3 -> WireCalculatorSection(viewModel, currentTheme)
                4 -> SubwooferWiringSection(viewModel, currentTheme)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. TONE GENERATOR & SWEEP SECTION
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToneGeneratorSection(
    viewModel: CarAudioViewModel,
    theme: CarAudioThemeType
) {
    val toneMode by viewModel.toneMode.collectAsState()
    val toneFreq by viewModel.toneFrequency.collectAsState()
    val isTonePlaying by viewModel.isTonePlaying.collectAsState()
    val splDb by viewModel.splDb.collectAsState()

    val quickFreqs = listOf(25f, 30f, 35f, 40f, 45f, 50f, 63f, 80f, 100f, 250f, 1000f, 5000f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tone Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.6f), theme.accentColor.copy(alpha = 0.6f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GENERADOR DE TONOS & CALIBRACIÓN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor,
                    letterSpacing = 1.sp
                )

                // Frequency Large Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (toneMode == ToneMode.SINE_WAVE) "${toneFreq.toInt()}" else toneMode.label.take(16),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isTonePlaying) theme.primaryColor else theme.textColor
                    )
                    if (toneMode == ToneMode.SINE_WAVE) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hz",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Subwoofer note indicator
                val musicalNote = when {
                    toneFreq < 30f -> "Sub-Grave Sísmico (< 30Hz)"
                    toneFreq in 30f..42f -> "Zona SPL / Entonación de Cajón (30-42Hz)"
                    toneFreq in 43f..60f -> "Golpe Seco de Bajo / Punch (43-60Hz)"
                    toneFreq in 61f..200f -> "Mid-Bass / Medios Bajos"
                    toneFreq in 201f..2000f -> "Vocal / Drivers Chuchero"
                    else -> "Agudos / Super Tweeter"
                }
                Text(
                    text = musicalNote,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.accentColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Play / Stop Button
                Button(
                    onClick = {
                        if (isTonePlaying) {
                            viewModel.stopTone()
                        } else {
                            viewModel.playTone(toneMode, toneFreq)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_tone_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTonePlaying) theme.errorColor else theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isTonePlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTonePlaying) "DETENER GENERADOR" else "TRANSMITIR TONO EN VIVO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                if (isTonePlaying) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Salida SPL actual: ${splDb.toInt()} dB", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text("AudioTrack Direct Stream (16-bit 44.1kHz)", fontSize = 10.sp, color = theme.primaryColor)
                    }
                }
            }
        }

        // Mode Selector Chips
        Text(
            text = "Modo de Calibración Acústica:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToneMode.entries.forEach { mode ->
                FilterChip(
                    selected = toneMode == mode,
                    onClick = { viewModel.setToneMode(mode) },
                    label = { Text(mode.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primaryColor,
                        selectedLabelColor = theme.onPrimaryColor,
                        containerColor = theme.cardColor,
                        labelColor = theme.textSecondaryColor
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Continuous Frequency Slider (Sine Mode)
        if (toneMode == ToneMode.SINE_WAVE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ajuste Continuo de Frecuencia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                        Text("${toneFreq.toInt()} Hz", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryColor)
                    }

                    Slider(
                        value = toneFreq,
                        onValueChange = { viewModel.setToneFrequency(it) },
                        valueRange = 10f..2000f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.surfaceColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10 Hz (Sub-sónico)", fontSize = 10.sp, color = theme.textSecondaryColor)
                        Text("2000 Hz (Agudos)", fontSize = 10.sp, color = theme.textSecondaryColor)
                    }
                }
            }

            // Quick Frequency Preset Buttons
            Text(
                text = "Frecuencias Clave para Entonar Cajón y Ajustar Ganancia:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickFreqs.forEach { f ->
                    val isSelected = toneFreq.toInt() == f.toInt()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) theme.primaryColor else theme.cardColor)
                            .border(1.dp, if (isSelected) theme.accentColor else theme.surfaceColor, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setToneFrequency(f) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${f.toInt()} Hz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) theme.onPrimaryColor else theme.textColor
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. BOX ENCLOSURE & PORT TUNING CALCULATOR
// -------------------------------------------------------------
@Composable
fun BoxCalculatorSection(
    viewModel: CarAudioViewModel,
    theme: CarAudioThemeType
) {
    var widthCmText by remember { mutableStateOf("75") }
    var heightCmText by remember { mutableStateOf("40") }
    var depthCmText by remember { mutableStateOf("45") }
    var woodThicknessText by remember { mutableStateOf("1.8") }
    var subDisplacementText by remember { mutableStateOf("3.5") }
    var isPorted by remember { mutableStateOf(true) }
    var portWidthText by remember { mutableStateOf("6") }
    var portHeightText by remember { mutableStateOf("36") }
    var portLengthText by remember { mutableStateOf("42") }

    val w = widthCmText.toFloatOrNull() ?: 70f
    val h = heightCmText.toFloatOrNull() ?: 40f
    val d = depthCmText.toFloatOrNull() ?: 45f
    val wood = woodThicknessText.toFloatOrNull() ?: 1.8f
    val subDisp = subDisplacementText.toFloatOrNull() ?: 3.5f
    val pw = portWidthText.toFloatOrNull() ?: 6f
    val ph = portHeightText.toFloatOrNull() ?: 36f
    val pl = portLengthText.toFloatOrNull() ?: 42f

    val result = viewModel.calculateBox(
        widthCm = w,
        heightCm = h,
        depthCm = d,
        woodThicknessCm = wood,
        subwooferDisplacementL = subDisp,
        isPorted = isPorted,
        portWidthCm = pw,
        portHeightCm = ph,
        portLengthCm = pl
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Result Highlight Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.5f), theme.accentColor.copy(alpha = 0.5f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RESULTADO DE LITRAJE & ENTONACIÓN (Fb)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Volumen Neto:", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", result.netVolumeLiters)} Litros",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "(${String.format(java.util.Locale.US, "%.2f", result.netVolumeCuFt)} ft³)",
                            fontSize = 12.sp,
                            color = theme.accentColor
                        )
                    }

                    if (isPorted) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sintonía Puerto (Fb):", fontSize = 11.sp, color = theme.textSecondaryColor)
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", result.portTuningHz)} Hz",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.accentColor
                            )
                            Text("Frecuencia de pico", fontSize = 11.sp, color = theme.textSecondaryColor)
                        }
                    }
                }

                Text(
                    text = "Recomendado para: ${result.recommendedSubSizeInches}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                Text(
                    text = result.description,
                    fontSize = 11.sp,
                    color = theme.textSecondaryColor
                )
            }
        }

        // Type selection: Ported vs Sealed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isPorted = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPorted) theme.primaryColor else theme.cardColor,
                    contentColor = if (isPorted) theme.onPrimaryColor else theme.textColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Porteado / Ranura (SPL)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { isPorted = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isPorted) theme.primaryColor else theme.cardColor,
                    contentColor = if (!isPorted) theme.onPrimaryColor else theme.textColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Sellado (Fidelidad SQ)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Dimensions input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Dimensiones Externas del Cajón (cm):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthCmText,
                        onValueChange = { widthCmText = it },
                        label = { Text("Ancho", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightCmText,
                        onValueChange = { heightCmText = it },
                        label = { Text("Alto", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = depthCmText,
                        onValueChange = { depthCmText = it },
                        label = { Text("Fondo", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = woodThicknessText,
                        onValueChange = { woodThicknessText = it },
                        label = { Text("Grosor Madera (MDF)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subDisplacementText,
                        onValueChange = { subDisplacementText = it },
                        label = { Text("Desplaz. Sub (Litros)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (isPorted) {
                    Text("Dimensiones del Puerto de Aire (Slot Port en cm):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = portWidthText,
                            onValueChange = { portWidthText = it },
                            label = { Text("Ancho Puerto", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = portHeightText,
                            onValueChange = { portHeightText = it },
                            label = { Text("Alto Puerto", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = portLengthText,
                            onValueChange = { portLengthText = it },
                            label = { Text("Largo Puerto", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. AWG WIRE GAUGE & FUSE CALCULATOR
// -------------------------------------------------------------
@Composable
fun WireCalculatorSection(
    viewModel: CarAudioViewModel,
    theme: CarAudioThemeType
) {
    var wattsRmsText by remember { mutableStateOf("2500") }
    var voltageText by remember { mutableStateOf("14.4") }
    var lengthMetersText by remember { mutableStateOf("5.0") }
    var isOfc by remember { mutableStateOf(true) }

    val watts = wattsRmsText.toFloatOrNull() ?: 2000f
    val volts = voltageText.toFloatOrNull() ?: 14.4f
    val length = lengthMetersText.toFloatOrNull() ?: 5.0f

    val wireResult = viewModel.calculateWire(
        wattsRms = watts,
        voltage = volts,
        lengthMeters = length,
        isOfcCobre = isOfc
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (wireResult.isSafe) theme.primaryColor else theme.errorColor,
                        theme.accentColor
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECOMENDACIÓN DE CABLEADO & PROTECCIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondaryColor
                    )
                    Icon(
                        imageVector = if (wireResult.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (wireResult.isSafe) theme.primaryColor else theme.errorColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Calibre AWG Recomendado:", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text(
                            text = wireResult.recommendedAwg,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryColor
                        )
                        Text("Consumo pico: ${wireResult.maxAmps.toInt()} Amperios", fontSize = 11.sp, color = theme.accentColor)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Fusible ANL Sugerido:", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${wireResult.recommendedFuseAmps} A",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.accentColor
                        )
                        Text("Tipo ANL / Mini-ANL", fontSize = 11.sp, color = theme.textSecondaryColor)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Caída de Tensión Calculada:", fontSize = 11.sp, color = theme.textSecondaryColor)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.2f", wireResult.voltageDropVolts)} V (${String.format(java.util.Locale.US, "%.1f", wireResult.voltageDropPercent)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (wireResult.isSafe) theme.primaryColor else theme.errorColor
                    )
                }

                Text(
                    text = wireResult.notes,
                    fontSize = 11.sp,
                    color = theme.textColor
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Datos de la Planta / Amplificador:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wattsRmsText,
                        onValueChange = { wattsRmsText = it },
                        label = { Text("Potencia Watts RMS", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = voltageText,
                        onValueChange = { voltageText = it },
                        label = { Text("Voltaje (12.6 o 14.4V)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = lengthMetersText,
                    onValueChange = { lengthMetersText = it },
                    label = { Text("Longitud del Cable Batería a Planta (Metros)", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Material del Conductor:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isOfc = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOfc) theme.primaryColor else theme.surfaceColor,
                            contentColor = if (isOfc) theme.onPrimaryColor else theme.textColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("100% Cobre OFC (Pro)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isOfc = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isOfc) theme.primaryColor else theme.surfaceColor,
                            contentColor = if (!isOfc) theme.onPrimaryColor else theme.textColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aluminio / Cobre CCA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. SUBWOOFER WIRING & OHM CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubwooferWiringSection(
    viewModel: CarAudioViewModel,
    theme: CarAudioThemeType
) {
    var numSubs by remember { mutableIntStateOf(2) }
    var coilType by remember { mutableStateOf("DVC 4Ω") }
    var wiringMode by remember { mutableStateOf("Paralelo (1Ω)") }

    val coilOptions = listOf("DVC 4Ω", "DVC 2Ω", "DVC 1Ω", "SVC 4Ω")
    val wiringOptions = listOf("Paralelo", "Serie", "Serie-Paralelo")

    val wiringResult = viewModel.calculateSubwooferWiring(
        numSubs = numSubs,
        coilType = coilType,
        wiringMode = wiringMode
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wiring Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(theme.primaryColor.copy(alpha = 0.5f), theme.accentColor.copy(alpha = 0.5f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CARGA DE IMPEDANCIA FINAL RESULTANTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Impedancia Final:", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", wiringResult.finalImpedanceOhms)} Ω",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Nivel de Seguridad Planta:", fontSize = 11.sp, color = theme.textSecondaryColor)
                        Text(
                            text = wiringResult.ampSafetyLevel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentColor
                        )
                    }
                }

                Text(
                    text = "Esquema de Conexión:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )
                Text(
                    text = wiringResult.diagramExplanation,
                    fontSize = 12.sp,
                    color = theme.textSecondaryColor
                )
            }
        }

        // Selectors
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Número de Subwoofers:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { count ->
                        val isSelected = numSubs == count
                        Button(
                            onClick = { numSubs = count },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) theme.primaryColor else theme.surfaceColor,
                                contentColor = if (isSelected) theme.onPrimaryColor else theme.textColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$count Sub${if (count > 1) "s" else ""}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Tipo de Bobina del Subwoofer:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    coilOptions.forEach { opt ->
                        FilterChip(
                            selected = coilType == opt,
                            onClick = { coilType = opt },
                            label = { Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryColor,
                                selectedLabelColor = theme.onPrimaryColor,
                                containerColor = theme.surfaceColor,
                                labelColor = theme.textSecondaryColor
                            )
                        )
                    }
                }

                Text("Modo de Conexión en Bornera:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wiringOptions.forEach { opt ->
                        FilterChip(
                            selected = wiringMode.contains(opt),
                            onClick = { wiringMode = opt },
                            label = { Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryColor,
                                selectedLabelColor = theme.onPrimaryColor,
                                containerColor = theme.surfaceColor,
                                labelColor = theme.textSecondaryColor
                            )
                        )
                    }
                }
            }
        }
    }
}
