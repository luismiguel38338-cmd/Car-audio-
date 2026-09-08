package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrazilianBox
import com.example.model.BrazilianBoxPresets
import com.example.model.CarAudioThemeType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrazilianBoxesView(
    theme: CarAudioThemeType,
    onApplyBoxCutsToDsp: (hpfHz: Float, lpfHz: Float, boxName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val boxes = BrazilianBoxPresets.list
    val currentBox = boxes[selectedIndex.coerceIn(0, boxes.size - 1)]

    var showApplyNotification by remember { mutableStateOf(false) }

    // Custom calculator variables
    var customWidth by remember { mutableStateOf("45") }
    var customHeight by remember { mutableStateOf("50") }
    var customDepth by remember { mutableStateOf("40") }
    var customMdf by remember { mutableStateOf("18") }
    var customTuningHz by remember { mutableStateOf("52") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner: Brazilian Sound System
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(theme.primaryColor.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, theme.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speaker,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CAJONES BRASILEÑOS SOM AUTOMOTIVO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.textColor,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Planos y litraje exacto: Caixa Bob 1x, 8\", 15\", 6\" y Bajo de 6\"",
                        fontSize = 11.sp,
                        color = theme.textSecondaryColor
                    )
                }
            }
        }

        // Selector of the 5 requested Brazilian box categories
        Text(
            text = "SELECCIONA EL MODELO DE CAJÓN BRASILEÑO:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = theme.primaryColor,
            letterSpacing = 1.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            boxes.forEachIndexed { index, box ->
                val isSelected = index == selectedIndex
                val tag = when (box.iconTag) {
                    "BOB_1X" -> "1 Woofer / Bob"
                    "MEDIO_8" -> "Médios 8\""
                    "EUCLIDES_15" -> "Pancadão 15\""
                    "MEDIO_6" -> "Médios 6\""
                    "SUB_6" -> "Bajo 6\" Mini Sub"
                    else -> box.name
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) theme.primaryColor.copy(alpha = 0.25f)
                            else theme.surfaceColor
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedIndex = index
                            showApplyNotification = false
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("btn_box_${box.iconTag}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) theme.primaryColor else theme.textColor
                        )
                    }
                }
            }
        }

        // Selected Box Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Box Title and Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentBox.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "Categoría: ${currentBox.category} • Madera MDF ${currentBox.mdfThicknessMm}mm",
                            fontSize = 11.sp,
                            color = theme.textSecondaryColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.accentColor.copy(alpha = 0.2f))
                            .border(1.dp, theme.accentColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentBox.tuningHz.toInt()} Hz Fb",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.accentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = theme.primaryColor.copy(alpha = 0.15f))

                // 2D Schematic Canvas Blueprint
                Text(
                    text = "DIAGRAMA TÉCNICO DE CORTE Y DUCTO:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondaryColor,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070B13))
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BrazilianBoxSchematicCanvas(
                        box = currentBox,
                        theme = theme,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Litraje and Dimension Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricInfoPill(
                        label = "Litraje Neto",
                        value = "${currentBox.netLiters} L",
                        subValue = "${String.format("%.2f", currentBox.netLiters / 28.316f)} cu.ft",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )

                    MetricInfoPill(
                        label = "Dimensiones",
                        value = "${currentBox.widthCm.toInt()}x${currentBox.heightCm.toInt()}",
                        subValue = "x${currentBox.depthCm.toInt()} cm",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )

                    MetricInfoPill(
                        label = "Potencia RMS",
                        value = currentBox.powerHandlingRms.split(" ").firstOrNull() ?: "1000W",
                        subValue = "Recomendada",
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Port Specification
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C1322))
                        .border(1.dp, theme.accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tipo de Ducto: ${currentBox.portType}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accentColor
                            )
                        }
                        Text(
                            text = currentBox.portDimensions,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Wood cut list
                Text(
                    text = "DESPIECE DE CORTE DE MADERA (MDF ${currentBox.mdfThicknessMm}mm):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A0E1A))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentBox.woodCutList.forEach { cutLine ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cutLine,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Acoustic explanation & Tuning Tip
                Text(
                    text = currentBox.acousticDescription,
                    fontSize = 11.sp,
                    color = theme.textSecondaryColor,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.primaryColor.copy(alpha = 0.12f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentBox.tuningTip,
                        fontSize = 11.sp,
                        color = theme.textColor,
                        lineHeight = 15.sp
                    )
                }

                // DSP Application Button
                Button(
                    onClick = {
                        onApplyBoxCutsToDsp(currentBox.hpfRecHz, currentBox.lpfRecHz, currentBox.name)
                        showApplyNotification = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_apply_box_dsp_cuts")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APLICAR CORTES DSP (${currentBox.hpfRecHz.toInt()}Hz HPF - ${currentBox.lpfRecHz.toInt()}Hz LPF)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (showApplyNotification) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2E1E))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "¡Cortes aplicados con éxito al DSP Procesador!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        // Custom Brazilian Box Volume Calculator
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALCULADORA DE LITROS Y DUCTOS PERSONALIZADA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customWidth,
                        onValueChange = { customWidth = it },
                        label = { Text("Ancho cm", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.3f),
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = customHeight,
                        onValueChange = { customHeight = it },
                        label = { Text("Alto cm", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.3f),
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = customDepth,
                        onValueChange = { customDepth = it },
                        label = { Text("Prof cm", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.3f),
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                val w = customWidth.toFloatOrNull() ?: 45f
                val h = customHeight.toFloatOrNull() ?: 50f
                val d = customDepth.toFloatOrNull() ?: 40f
                val mdf = (customMdf.toFloatOrNull() ?: 18f) / 10f // cm

                val inW = (w - 2 * mdf).coerceAtLeast(1f)
                val inH = (h - 2 * mdf).coerceAtLeast(1f)
                val inD = (d - 2 * mdf).coerceAtLeast(1f)

                val grossLiters = (w * h * d) / 1000f
                val netLiters = ((inW * inH * inD) / 1000f) * 0.88f // -12% speaker and port displacement

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090D18))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Volumen Bruto", fontSize = 10.sp, color = theme.textSecondaryColor)
                            Text(
                                "${String.format("%.1f", grossLiters)} L",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Volumen Neto Estimado", fontSize = 10.sp, color = theme.primaryColor)
                            Text(
                                "${String.format("%.1f", netLiters)} L",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.primaryColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pies Cúbicos", fontSize = 10.sp, color = theme.textSecondaryColor)
                            Text(
                                "${String.format("%.2f", netLiters / 28.316f)} ft³",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrazilianBoxSchematicCanvas(
    box: BrazilianBox,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val padding = 24f
        val boxWidth = w - padding * 2
        val boxHeight = h - padding * 2

        // Outer box MDF boundary
        drawRect(
            color = theme.primaryColor.copy(alpha = 0.85f),
            topLeft = Offset(padding, padding),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = 3.dp.toPx())
        )

        // Internal MDF wall thickness line
        val mdfPx = 10f
        drawRect(
            color = theme.primaryColor.copy(alpha = 0.35f),
            topLeft = Offset(padding + mdfPx, padding + mdfPx),
            size = Size(boxWidth - mdfPx * 2, boxHeight - mdfPx * 2),
            style = Stroke(width = 1.5.dp.toPx())
        )

        when (box.iconTag) {
            "EUCLIDES_15" -> {
                // V-Shape Euclides Deflector
                val vPath = Path().apply {
                    moveTo(padding + mdfPx, padding + boxHeight * 0.2f)
                    lineTo(w / 2f, h * 0.58f)
                    lineTo(w - padding - mdfPx, padding + boxHeight * 0.2f)
                }
                drawPath(
                    path = vPath,
                    color = theme.accentColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // 15" Woofer circle in V
                drawCircle(
                    color = theme.primaryColor,
                    radius = boxHeight * 0.28f,
                    center = Offset(w / 2f, h * 0.44f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = theme.primaryColor.copy(alpha = 0.4f),
                    radius = boxHeight * 0.10f,
                    center = Offset(w / 2f, h * 0.44f)
                )
            }
            "MEDIO_8" -> {
                // 2x 8" Speakers
                val r8 = boxHeight * 0.26f
                drawCircle(
                    color = theme.primaryColor,
                    radius = r8,
                    center = Offset(w * 0.33f, h / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = theme.primaryColor,
                    radius = r8,
                    center = Offset(w * 0.67f, h / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Triangular corner ports
                val triPath = Path().apply {
                    moveTo(padding + mdfPx, padding + mdfPx)
                    lineTo(padding + mdfPx + 30f, padding + mdfPx)
                    lineTo(padding + mdfPx, padding + mdfPx + 30f)
                    close()
                }
                drawPath(triPath, color = theme.accentColor)
            }
            "BOB_1X" -> {
                // Bob Esponja: Lower Subwoofer, Upper Mid & Horn
                // Divider line
                drawLine(
                    color = theme.primaryColor.copy(alpha = 0.5f),
                    start = Offset(padding + mdfPx, h * 0.48f),
                    end = Offset(w - padding - mdfPx, h * 0.48f),
                    strokeWidth = 2.dp.toPx()
                )

                // Lower Sub 12"
                drawCircle(
                    color = theme.primaryColor,
                    radius = boxHeight * 0.22f,
                    center = Offset(w / 2f, h * 0.72f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Upper 8" Mid
                drawCircle(
                    color = theme.accentColor,
                    radius = boxHeight * 0.14f,
                    center = Offset(w * 0.35f, h * 0.26f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Upper Corneta / Driver
                drawRect(
                    color = theme.accentColor.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.62f, h * 0.18f),
                    size = Size(boxWidth * 0.26f, boxHeight * 0.16f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Bottom slot port
                drawRect(
                    color = theme.accentColor,
                    topLeft = Offset(padding + mdfPx, h - padding - mdfPx - 14f),
                    size = Size(boxWidth - mdfPx * 2, 14f)
                )
            }
            "MEDIO_6" -> {
                // 2x 6" Speakers
                val r6 = boxHeight * 0.28f
                drawCircle(
                    color = theme.primaryColor,
                    radius = r6,
                    center = Offset(w * 0.32f, h / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = theme.primaryColor,
                    radius = r6,
                    center = Offset(w * 0.68f, h / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Round mini ports
                drawCircle(
                    color = theme.accentColor,
                    radius = 12f,
                    center = Offset(w * 0.32f, h * 0.82f)
                )
                drawCircle(
                    color = theme.accentColor,
                    radius = 12f,
                    center = Offset(w * 0.68f, h * 0.82f)
                )
            }
            "SUB_6" -> {
                // Mini 6.5" Subwoofer with Maze Folded Slot Port
                drawCircle(
                    color = theme.primaryColor,
                    radius = boxHeight * 0.24f,
                    center = Offset(w * 0.38f, h * 0.44f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Labyrinth port lines
                val mazePath = Path().apply {
                    moveTo(w * 0.72f, padding + mdfPx)
                    lineTo(w * 0.72f, h - padding - mdfPx - 26f)
                    lineTo(w * 0.86f, h - padding - mdfPx - 26f)
                    lineTo(w * 0.86f, padding + mdfPx + 20f)
                }
                drawPath(
                    path = mazePath,
                    color = theme.accentColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun MetricInfoPill(
    label: String,
    value: String,
    subValue: String,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, theme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 9.sp, color = theme.textSecondaryColor)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textColor)
            Text(subValue, fontSize = 9.sp, color = theme.primaryColor)
        }
    }
}
