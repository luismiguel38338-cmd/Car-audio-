package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType

@Composable
fun PersonalizationView(
    theme: CarAudioThemeType,
    isVipUnlocked: Boolean,
    liteVisuals: Boolean,
    keepScreenOn: Boolean,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    onSelectTheme: (CarAudioThemeType) -> Unit,
    onToggleLiteVisuals: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onNavigateVipZone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val orderedThemes = remember {
        listOf(CarAudioThemeType.GRAPHITE_STUDIO) +
            CarAudioThemeType.entries.filter { it != CarAudioThemeType.GRAPHITE_STUDIO }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .verticalScroll(scroll)
            .padding(14.dp)
            .testTag("personalization_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudioSignalIndicatorBar(
            isAudioPlaying = isAnyAudioPlaying,
            bassEnergy = bassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            theme = theme,
            liteVisuals = liteVisuals,
            title = "ESTILO VISUAL • INDICADOR DE SEÑAL"
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = theme.accentColor)
                    Text(
                        text = "TEMAS DE ESTUDIO (GRAFITO / PLATA / AZUL HIELO)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = theme.textColor
                    )
                }

                orderedThemes.forEach { themeItem ->
                    val isSelected = theme == themeItem
                    val locked = themeItem.isVipOnly && !isVipUnlocked
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) theme.accentColor.copy(alpha = 0.14f) else theme.backgroundColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.accentColor else theme.cardColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (locked) onNavigateVipZone() else onSelectTheme(themeItem)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("theme_item_${themeItem.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.primaryColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.accentColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = themeItem.displayName,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) theme.accentColor else theme.textColor
                                    )
                                    if (themeItem.isVipOnly) {
                                        Text(
                                            text = "VIP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = theme.accentColor
                                        )
                                    }
                                }
                                Text(text = themeItem.subtitle, fontSize = 10.sp, color = theme.textSecondaryColor)
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.accentColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = theme.onPrimaryColor)
                            }
                        } else if (locked) {
                            Icon(Icons.Default.Lock, contentDescription = "Tema VIP", tint = theme.textSecondaryColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = BorderStroke(1.dp, theme.cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = theme.accentColor)
                    Text("RENDIMIENTO Y PANTALLA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = theme.textColor)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efectos Visuales Ligeros", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Reduce uso de GPU y batería manteniendo la misma fidelidad sonora.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = liteVisuals,
                        onCheckedChange = onToggleLiteVisuals,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mantener Pantalla Encendida", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Evita que el teléfono se apague mientras monitoreas los medidores RTA.", color = theme.textSecondaryColor, fontSize = 10.sp)
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = onToggleKeepScreenOn,
                        colors = SwitchDefaults.colors(checkedThumbColor = theme.primaryColor, checkedTrackColor = theme.accentColor.copy(alpha = 0.45f))
                    )
                }
            }
        }
    }
}
