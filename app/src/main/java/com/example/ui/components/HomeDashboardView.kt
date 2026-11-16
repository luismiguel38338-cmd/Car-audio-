package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppSection
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspSettings
import com.example.model.OutputDeviceStatus
import com.example.model.RadioStation

@Composable
fun HomeDashboardView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    isRadioPlaying: Boolean,
    activeRadioStation: RadioStation?,
    currentTrack: AudioTrackItem?,
    isMusicPlaying: Boolean,
    outputDeviceStatus: OutputDeviceStatus,
    powerTelemetry: DevicePowerTelemetry,
    isVipUnlocked: Boolean,
    vipIntroDismissed: Boolean,
    liteVisuals: Boolean,
    onDismissVipIntro: () -> Unit,
    onOpenVipDialog: () -> Unit,
    onNavigateSection: (AppSection) -> Unit,
    onToggleActivePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("home_dashboard_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Cockpit Hero Banner + Live Status
        item(key = "home_hero_banner") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(166.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.car_audio_cockpit_hero_1794824451080),
                        contentDescription = "Cabina Car Audio DSP Pro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        theme.backgroundColor.copy(alpha = 0.25f),
                                        theme.backgroundColor.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.car_audio_dsp_icon_1794824449077),
                                contentDescription = "Icono Car Audio DSP Pro",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, theme.primaryColor, CircleShape)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CAR AUDIO DSP PRO",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                val statusLine = when {
                                    isRadioPlaying && activeRadioStation != null ->
                                        "Radio Pioneer: ${activeRadioStation.name} (${activeRadioStation.frequencyMhz} FM)"
                                    isMusicPlaying && currentTrack != null ->
                                        "Música: ${currentTrack.title} • ${currentTrack.artist}"
                                    else -> "${outputDeviceStatus.label} • DSP ${if (dspSettings.dspMasterEnabled) "ON" else "BYPASS"}"
                                }
                                Text(
                                    text = statusLine,
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = onToggleActivePlayPause,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                                    .testTag("home_quick_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (isAnyAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAnyAudioPlaying) "Pausar audio" else "Reproducir audio",
                                    tint = theme.onPrimaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Signature 5-LED Indicator Bar (ROJO - VERDE - ROJO - VERDE - BLANCO)
        item(key = "home_five_led_bar") {
            FiveLedIndicatorBar(
                isAudioPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                title = "TELEMETRÍA DE PANEL • 5 LED",
                showMiniBars = true
            )
        }

        // 3. One-Time Dismissible VIP Presentation Banner
        // Appears once until closed or unlocked; NEVER revokes VIP when closed.
        if (!vipIntroDismissed && !isVipUnlocked) {
            item(key = "one_time_vip_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("one_time_vip_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = BorderStroke(1.3.dp, theme.accentColor.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Presentación VIP",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ESTUDIO VIP DISPONIBLE",
                                    color = theme.textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            IconButton(
                                onClick = onDismissVipIntro,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("dismiss_vip_intro_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar anuncio VIP",
                                    tint = theme.textSecondaryColor
                                )
                            }
                        }
                        Text(
                            text = "Activa Hi-Res Studio, Calidez de Bulbos, Presencia Vocal, Claridad HD y pendientes hasta 48 dB/oct cuando quieras en la Zona VIP.",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenVipDialog,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primaryColor,
                                    contentColor = theme.onPrimaryColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ACTIVAR VIP", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Button(
                                onClick = onDismissVipIntro,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.surfaceColor,
                                    contentColor = theme.textSecondaryColor
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("CERRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Compact Power & Output Bar
        item(key = "home_power_chip_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (powerTelemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = "Batería Android",
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = powerTelemetry.batteryVoltageV?.let { "%.2f V".format(it) } ?: "N/D",
                                color = theme.textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Tel: ${powerTelemetry.batteryPercent ?: "--"}%${powerTelemetry.batteryTempC?.let { " • %.1f°C".format(it) } ?: ""}",
                                color = theme.textSecondaryColor,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateSection(AppSection.RADIO_PIONEER) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio Pioneer",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "RADIO PIONEER",
                                color = theme.textColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = activeRadioStation?.let { "${it.frequencyMhz} FM" } ?: "5 diseños",
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. 9-Section Professional Hub Grid
        item(key = "home_section_grid_title") {
            Text(
                text = "MENÚ PRINCIPAL DE ESTUDIO (9 SECCIONES)",
                color = theme.textSecondaryColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }

        val hubItems = listOf(
            Triple(AppSection.RADIO_PIONEER, Icons.Default.Radio, "5 autoestéreos reales y emisoras FM"),
            Triple(AppSection.MUSIC_PLAYER, Icons.Default.LibraryMusic, "Música local, listas y carátulas"),
            Triple(AppSection.EQUALIZER, Icons.Default.Equalizer, "31 ISO, 15B y Paramétrico 5B"),
            Triple(AppSection.SPEAKERS, Icons.Default.Speaker, "Subwoofer, fader, balance y tweeters"),
            Triple(AppSection.SOUND_TOOLS, Icons.Default.Build, "RTA, voltímetro y perfiles"),
            Triple(AppSection.PERSONALIZATION, Icons.Default.Palette, "Temas e iluminación Pioneer"),
            Triple(AppSection.VIP_ZONE, Icons.Default.WorkspacePremium, if (isVipUnlocked) "VIP Activo" else "Mejoras Hi-Res Pro"),
            Triple(AppSection.SETTINGS, Icons.Default.Settings, "Diagnóstico, permisos y sistema")
        )

        items(hubItems.chunked(2).size) { rowIndex ->
            val pair = hubItems.chunked(2)[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { (section, icon, desc) ->
                    HomeHubTile(
                        section = section,
                        icon = icon,
                        description = desc,
                        theme = theme,
                        onClick = { onNavigateSection(section) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHubTile(
    section: AppSection,
    icon: ImageVector,
    description: String,
    theme: CarAudioThemeType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("home_tile_${section.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.primaryColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = section.title,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = section.title,
                color = theme.textColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                color = theme.textSecondaryColor,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
