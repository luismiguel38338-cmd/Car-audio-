package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.PioneerIlluminationColor
import com.example.model.PioneerRadioStyle
import com.example.model.RadioStation

@Composable
fun PioneerRadioView(
    theme: CarAudioThemeType,
    isPowerOn: Boolean,
    selectedStyle: PioneerRadioStyle,
    illuminationColor: PioneerIlluminationColor,
    stations: List<RadioStation>,
    activeStation: RadioStation?,
    presetMemoryIds: List<String>,
    isRadioPlaying: Boolean,
    isRadioBuffering: Boolean,
    radioErrorMessage: String?,
    searchQuery: String,
    volumeLevel: Float,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    onTogglePower: () -> Unit,
    onSelectStyle: (PioneerRadioStyle) -> Unit,
    onSelectIllumination: (PioneerIlluminationColor) -> Unit,
    onSelectStation: (RadioStation) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextStation: () -> Unit,
    onPrevStation: () -> Unit,
    onToggleFavoriteStation: (RadioStation) -> Unit,
    onSelectPresetMemory: (Int) -> Unit,
    onAssignPresetMemory: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var saveMemoryMode by remember { mutableStateOf(false) }
    val glowPrimary = if (isPowerOn) illuminationColor.primaryColor else Color(0xFF475569)
    val glowSecondary = if (isPowerOn) illuminationColor.secondaryColor else Color(0xFF334155)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("pioneer_radio_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 5-LED Sound Bar
        item(key = "pioneer_5led") {
            FiveLedIndicatorBar(
                isAudioPlaying = isPowerOn && isRadioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                title = "RADIO PIONEER • 5 LED"
            )
        }

        // 2. Interactive Pioneer Head Unit Deck (5 Distinct Designs)
        item(key = "pioneer_head_unit") {
            PioneerFaceplateDeck(
                theme = theme,
                style = selectedStyle,
                isPowerOn = isPowerOn,
                glowPrimary = glowPrimary,
                glowSecondary = glowSecondary,
                activeStation = activeStation,
                isRadioPlaying = isRadioPlaying,
                isRadioBuffering = isRadioBuffering,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                volumeLevel = volumeLevel,
                onTogglePower = onTogglePower,
                onTogglePlayPause = onTogglePlayPause,
                onNextStation = onNextStation,
                onPrevStation = onPrevStation,
                onVolumeChange = onVolumeChange,
                onToggleFavorite = { activeStation?.let(onToggleFavoriteStation) }
            )
        }

        // 3. Preset Memories M1..M6
        item(key = "pioneer_memories") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, glowPrimary.copy(alpha = 0.38f)),
                modifier = Modifier.fillMaxWidth()
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
                        Text("MEMORIA DE EMISORAS (M1 - M6)", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (saveMemoryMode) glowPrimary else theme.cardColor)
                                .clickable { saveMemoryMode = !saveMemoryMode }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("pioneer_memory_save_toggle")
                        ) {
                            Icon(
                                Icons.Default.BookmarkAdd,
                                contentDescription = "Guardar memoria",
                                tint = if (saveMemoryMode) Color(0xFF06101B) else glowPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (saveMemoryMode) "ELIGE RANURA" else "GUARDAR ACTUAL",
                                color = if (saveMemoryMode) Color(0xFF06101B) else theme.textColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (slot in 0 until 6) {
                            val assignedId = presetMemoryIds.getOrNull(slot)
                            val assignedStation = stations.firstOrNull { it.id == assignedId }
                                ?: com.example.model.VerifiedRadioCatalog.stations.firstOrNull { it.id == assignedId }
                            val isActiveMem = isPowerOn && activeStation?.id == assignedId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActiveMem) glowPrimary else theme.cardColor)
                                    .border(
                                        1.dp,
                                        if (saveMemoryMode || isActiveMem) glowPrimary else theme.cardColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (saveMemoryMode) {
                                            onAssignPresetMemory(slot)
                                            saveMemoryMode = false
                                        } else {
                                            onSelectPresetMemory(slot)
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 2.dp)
                                    .testTag("pioneer_memory_slot_${slot + 1}")
                            ) {
                                Text(
                                    text = "M${slot + 1}",
                                    color = if (isActiveMem) Color(0xFF05101A) else glowPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = assignedStation?.let { "%.1f".format(it.frequencyMhz) } ?: "---",
                                    color = if (isActiveMem) Color(0xFF05101A) else theme.textSecondaryColor,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Customize Design (5 Pioneer Models) & LED Illumination
        item(key = "pioneer_style_selector") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("DISEÑO PIONEER & COLOR DE ILUMINACIÓN", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PioneerRadioStyle.entries.forEach { style ->
                            val selected = selectedStyle == style
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectStyle(style) },
                                label = { Text("${style.displayName} (${style.modelCode})", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = glowPrimary,
                                    selectedLabelColor = Color(0xFF05101A),
                                    containerColor = theme.cardColor,
                                    labelColor = theme.textColor
                                ),
                                modifier = Modifier.testTag("pioneer_style_${style.id}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LEDs:", color = theme.textSecondaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        PioneerIlluminationColor.entries.forEach { illum ->
                            val selected = illuminationColor == illum
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) theme.cardColor else Color.Transparent)
                                    .border(1.dp, if (selected) illum.primaryColor else theme.cardColor, RoundedCornerShape(16.dp))
                                    .clickable { onSelectIllumination(illum) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(illum.primaryColor)
                                )
                                Text(illum.displayName, color = theme.textColor, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Error / Connectivity Banner or Broadcast Info Note
        item(key = "pioneer_stream_note") {
            if (radioErrorMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.errorColor.copy(alpha = 0.14f)),
                    border = BorderStroke(1.dp, theme.errorColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = radioErrorMessage,
                        color = theme.textColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceColor)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = glowPrimary, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Las frecuencias FM sintonizan transmisiones digitales HTTPS reales en vivo (requiere conexión a internet).",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        lineHeight = 13.5.sp
                    )
                }
            }
        }

        // 6. Search Verified Radio Stations
        item(key = "pioneer_search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar emisora real, género o frecuencia FM...", fontSize = 12.sp, color = theme.textSecondaryColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar emisora", tint = glowPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = theme.textSecondaryColor)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = glowPrimary,
                    unfocusedBorderColor = theme.cardColor,
                    focusedTextColor = theme.textColor,
                    unfocusedTextColor = theme.textColor,
                    focusedContainerColor = theme.surfaceColor,
                    unfocusedContainerColor = theme.surfaceColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pioneer_station_search")
            )
        }

        // 7. Verified Streaming Stations List
        items(stations, key = { it.id }) { station ->
            val selected = activeStation?.id == station.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStation(station) }
                    .testTag("radio_station_card_${station.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) theme.cardColor else theme.surfaceColor),
                border = BorderStroke(
                    if (selected) 1.5.dp else 1.dp,
                    if (selected) glowPrimary else theme.cardColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected && isRadioPlaying) glowPrimary else theme.backgroundColor)
                            .border(1.dp, glowPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%.1f".format(station.frequencyMhz),
                            color = if (selected && isRadioPlaying) Color(0xFF05101A) else glowPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = station.name,
                            color = theme.textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${station.genre} • ${station.country}",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${station.formatLabel} ${station.bitrateKbps} kbps • HTTPS en vivo",
                            color = glowSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = { onToggleFavoriteStation(station) },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("radio_fav_${station.id}")
                    ) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (station.isFavorite) "Quitar favorita" else "Marcar favorita",
                            tint = if (station.isFavorite) FiveLedColors.RED else theme.textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PioneerFaceplateDeck(
    theme: CarAudioThemeType,
    style: PioneerRadioStyle,
    isPowerOn: Boolean,
    glowPrimary: Color,
    glowSecondary: Color,
    activeStation: RadioStation?,
    isRadioPlaying: Boolean,
    isRadioBuffering: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    volumeLevel: Float,
    onTogglePower: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextStation: () -> Unit,
    onPrevStation: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: () -> Unit
) {
    val bezelBg = when (style) {
        PioneerRadioStyle.CLASSIC -> Color(0xFF0A0F18)
        PioneerRadioStyle.DIGITAL -> Color(0xFF070C14)
        PioneerRadioStyle.MULTIMEDIA -> Color(0xFF0E1420)
        PioneerRadioStyle.SCREEN -> Color(0xFF080D16)
        PioneerRadioStyle.COMPETITION -> Color(0xFF06080D)
    }

    Card(
        shape = RoundedCornerShape(if (style == PioneerRadioStyle.MULTIMEDIA || style == PioneerRadioStyle.SCREEN) 20.dp else 14.dp),
        colors = CardDefaults.cardColors(containerColor = bezelBg),
        border = BorderStroke(1.8.dp, if (isPowerOn) glowPrimary else Color(0xFF283244)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pioneer_faceplate_deck")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Bezel Header: Pioneer badge + model code + Power button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Radio, contentDescription = "Pioneer", tint = glowPrimary, modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            text = "PIONEER ${style.modelCode}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = style.subtitle,
                            color = glowPrimary.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePower,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPowerOn) glowPrimary.copy(alpha = 0.22f) else Color(0xFF1A2232))
                        .border(1.2.dp, if (isPowerOn) glowPrimary else Color.Gray, CircleShape)
                        .testTag("pioneer_power_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isPowerOn) "Apagar radio" else "Encender radio",
                        tint = if (isPowerOn) glowPrimary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central OLED / Screen Display matching the selected Pioneer architecture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPowerOn) Color(0xFF03070E) else Color(0xFF0A0D12))
                    .border(1.2.dp, if (isPowerOn) glowPrimary.copy(alpha = 0.55f) else Color(0xFF1E2634), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                if (!isPowerOn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PIONEER STANDBY • RADIO APAGADA",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeStation?.let { "FM1 • %.1f MHz".format(it.frequencyMhz) } ?: "FM1 • ---.-",
                                color = glowPrimary,
                                fontSize = if (style == PioneerRadioStyle.CLASSIC || style == PioneerRadioStyle.DIGITAL) 22.sp else 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isRadioBuffering) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    CircularProgressIndicator(
                                        color = glowSecondary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("CONECTANDO", color = glowSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                Text(
                                    text = if (isRadioPlaying) "EN VIVO • STEREO" else "PAUSA / LISTO",
                                    color = if (isRadioPlaying) glowSecondary else theme.textSecondaryColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = activeStation?.name ?: "Selecciona una emisora abajo",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeStation?.let { "${it.genre} • ${it.bitrateKbps} kbps • ${it.country}" } ?: "Stream digital verificado",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Visual analyzer strip inside Screen / Multimedia / Competition styles
                        if (style == PioneerRadioStyle.SCREEN || style == PioneerRadioStyle.MULTIMEDIA || style == PioneerRadioStyle.COMPETITION) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .padding(top = 4.dp)
                            ) {
                                val bars = 16
                                val gap = 3.dp.toPx()
                                val bw = ((size.width - gap * (bars - 1)) / bars).coerceAtLeast(2f)
                                val r = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                                for (i in 0 until bars) {
                                    val energy = when {
                                        i < 5 -> bassEnergy
                                        i < 11 -> midEnergy
                                        else -> trebleEnergy
                                    }
                                    val hRatio = if (isRadioPlaying) (0.2f + energy * (0.6f + (i % 4) * 0.1f)).coerceIn(0.15f, 1f) else 0.12f
                                    val h = size.height * hRatio
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(listOf(glowSecondary, glowPrimary)),
                                        topLeft = Offset(i * (bw + gap), size.height - h),
                                        size = Size(bw, h),
                                        cornerRadius = r
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Rotary / Seek / Play Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevStation,
                    enabled = isPowerOn,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pioneer_prev_station")
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Emisora anterior", tint = if (isPowerOn) Color.White else Color.DarkGray, modifier = Modifier.size(28.dp))
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isPowerOn) glowPrimary else Color(0xFF1E293B))
                        .testTag("pioneer_play_pause")
                ) {
                    Icon(
                        imageVector = if (isRadioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRadioPlaying) "Pausar radio" else "Reproducir radio",
                        tint = if (isPowerOn) Color(0xFF04101C) else Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = onNextStation,
                    enabled = isPowerOn,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pioneer_next_station")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente emisora", tint = if (isPowerOn) Color.White else Color.DarkGray, modifier = Modifier.size(28.dp))
                }

                IconButton(
                    onClick = onToggleFavorite,
                    enabled = isPowerOn && activeStation != null,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (activeStation?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorita",
                        tint = if (activeStation?.isFavorite == true) FiveLedColors.RED else glowPrimary
                    )
                }
            }

            // Head Unit Volume Knob Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeDown, contentDescription = null, tint = theme.textSecondaryColor, modifier = Modifier.size(18.dp))
                Slider(
                    value = volumeLevel,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    enabled = isPowerOn,
                    colors = SliderDefaults.colors(
                        thumbColor = glowPrimary,
                        activeTrackColor = glowPrimary,
                        inactiveTrackColor = Color(0xFF1A2232)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "VOL ${(volumeLevel * 40).toInt()}",
                    color = glowSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = glowPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
