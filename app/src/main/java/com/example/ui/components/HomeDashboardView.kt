package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.AppSection
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
import com.example.model.DevicePowerTelemetry
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.TutorialCatalog

@Composable
fun HomeDashboardView(
    theme: CarAudioThemeType,
    userProfile: CloudUserProfile?,
    dspSettings: DspSettings,
    equalizerSettings: EqualizerSettings,
    capabilityReport: DspCapabilityReport,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    currentTrack: AudioTrackItem?,
    playbackSeconds: Int,
    tracksCount: Int,
    favoriteTracks: List<AudioTrackItem>,
    playlists: List<MusicPlaylist>,
    outputDeviceStatus: OutputDeviceStatus,
    powerTelemetry: DevicePowerTelemetry,
    isVipUnlocked: Boolean,
    vipPromoDismissed: Boolean,
    liteVisuals: Boolean,
    onDismissVipPromo: () -> Unit,
    onNavigateSection: (AppSection) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayTrack: (AudioTrackItem) -> Unit,
    onSelectPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val greetingName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: "Estudio"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("home_dashboard_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Personalized Header & User Profile Greeting
        item(key = "home_user_greeting") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val photoUrl = userProfile?.photoUrl
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil de $greetingName",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, theme.accentColor, CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                            contentDescription = "Perfil Audio DSP Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, theme.accentColor, CircleShape)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, $greetingName",
                            color = theme.textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${outputDeviceStatus.label} • DSP ${if (dspSettings.dspMasterEnabled) "Activo" else "Bypass"}",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isVipUnlocked) theme.accentColor.copy(alpha = 0.2f) else theme.cardColor)
                            .border(
                                1.dp,
                                if (isVipUnlocked) theme.accentColor else theme.textSecondaryColor.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onNavigateSection(AppSection.VIP_ZONE) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("home_vip_badge")
                    ) {
                        Text(
                            text = if (isVipUnlocked) "VIP ACTIVO" else "PLAN GRATIS",
                            color = if (isVipUnlocked) theme.accentColor else theme.textSecondaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. Continue Listening / Recent Playback Card
        item(key = "home_continue_listening") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateSection(AppSection.MUSIC_PLAYER) }
                    .testTag("home_continue_listening_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.35f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_studio_hero_1794828268351),
                        contentDescription = "Estudio de Audio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        theme.backgroundColor.copy(alpha = 0.94f),
                                        theme.surfaceColor.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentTrack != null) "CONTINUAR ESCUCHANDO" else "BIBLIOTECA MUSICAL",
                                    color = theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = currentTrack?.title ?: "Escanea o importa tu música local",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentTrack?.let {
                                        "${it.artist} • ${it.album} • ${it.audioFormatLabel}"
                                    } ?: "$tracksCount pistas disponibles • Reproductor Media3",
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (currentTrack != null) onTogglePlayPause()
                                    else onNavigateSection(AppSection.MUSIC_PLAYER)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                                    .testTag("home_quick_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (isAnyAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAnyAudioPlaying) "Pausar reproducción" else "Reproducir música",
                                    tint = theme.onPrimaryColor
                                )
                            }
                        }

                        if (currentTrack != null && currentTrack.durationSeconds > 0) {
                            val progress = (playbackSeconds.toFloat() / currentTrack.durationSeconds.toFloat()).coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = theme.accentColor,
                                    trackColor = theme.backgroundColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatDurationSeconds(playbackSeconds),
                                        color = theme.textSecondaryColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = formatDurationSeconds(currentTrack.durationSeconds),
                                        color = theme.textSecondaryColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Studio Signal Indicator Bar (Graphite / Silver / Ice Blue)
        item(key = "home_studio_signal_bar") {
            StudioSignalIndicatorBar(
                isAudioPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                showMiniBars = true
            )
        }

        // 4. One-Time Permanently Dismissible VIP Banner
        if (!vipPromoDismissed && !isVipUnlocked) {
            item(key = "one_time_vip_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("one_time_vip_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f))
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Zona VIP",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Audio DSP Pro VIP",
                                    color = theme.textColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            IconButton(
                                onClick = onDismissVipPromo,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("dismiss_vip_intro_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar aviso VIP",
                                    tint = theme.textSecondaryColor
                                )
                            }
                        }
                        Text(
                            text = "Explora procesamiento Hi-Res Studio, Calidez Analógica, Claridad HD, Presets de masterización y temas exclusivos en la Zona VIP.",
                            color = theme.textSecondaryColor,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateSection(AppSection.VIP_ZONE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primaryColor,
                                    contentColor = theme.onPrimaryColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("VER ZONA VIP", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Button(
                                onClick = onDismissVipPromo,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.surfaceColor,
                                    contentColor = theme.textSecondaryColor
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("OCULTAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Access to Audio Lab / Equalizer & Power Telemetry
        item(key = "home_quick_eq_and_battery") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateSection(AppSection.AUDIO_LAB) }
                        .testTag("home_quick_eq_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = "Ecualizador", tint = theme.accentColor)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ecualizador", color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = equalizerSettings.activePresetName,
                                color = theme.accentColor,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateSection(AppSection.AUDIO_METERS) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (powerTelemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                contentDescription = "Batería",
                                tint = theme.primaryColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${powerTelemetry.batteryPercent ?: "--"}% • ${powerTelemetry.batteryVoltageV?.let { "%.2fV".format(it) } ?: "N/D"}",
                                color = theme.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = powerTelemetry.batteryCurrentMa?.let { "%.0f mA".format(it) }
                                    ?: (powerTelemetry.batteryTempC?.let { "%.1f °C".format(it) } ?: "Medidores"),
                                color = theme.textSecondaryColor,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Favorite Lists & Quick Playlists
        if (favoriteTracks.isNotEmpty() || playlists.isNotEmpty()) {
            item(key = "home_favorites_and_playlists") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FAVORITOS Y LISTAS",
                        color = theme.textSecondaryColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        playlists.forEach { pl ->
                            Card(
                                modifier = Modifier
                                    .width(168.dp)
                                    .clickable {
                                        onSelectPlaylist(pl.id)
                                        onNavigateSection(AppSection.MUSIC_PLAYER)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(1.dp, theme.cardColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.QueueMusic, contentDescription = pl.name, tint = theme.accentColor, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(pl.name, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${pl.trackIds.size} pistas", color = theme.textSecondaryColor, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        favoriteTracks.take(6).forEach { fav ->
                            Card(
                                modifier = Modifier
                                    .width(176.dp)
                                    .clickable { onPlayTrack(fav) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(1.dp, theme.cardColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = fav.title, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(fav.title, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(fav.artist, color = theme.textSecondaryColor, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Featured Tools Grid
        item(key = "home_featured_tools_header") {
            Text(
                text = "HERRAMIENTAS DESTACADAS",
                color = theme.textSecondaryColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }

        val hubItems = listOf(
            Triple(AppSection.MUSIC_PLAYER, Icons.Default.LibraryMusic, "$tracksCount pistas • Canciones, Artistas, Álbumes y Cola"),
            Triple(AppSection.AUDIO_LAB, Icons.Default.Tune, capabilityReport.engineSummary),
            Triple(AppSection.AUDIO_METERS, Icons.Default.GraphicEq, "RTA 31B, Osciloscopio, VU dBFS y Batería"),
            Triple(AppSection.TUTORIALS, Icons.Default.MenuBook, "10 guías interactivas de audio y DSP"),
            Triple(AppSection.PERSONALIZATION, Icons.Default.Palette, "Estilos Grafito, Plata y Azul Hielo"),
            Triple(AppSection.SETTINGS, Icons.Default.Settings, "Cuenta Google, Firestore, Privacidad y Sistema")
        )

        items(hubItems.chunked(2).size) { rowIndex ->
            val pair = hubItems.chunked(2)[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { (section, icon, desc) ->
                    HomeToolCard(
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

        // 8. Recommended Tutorials Strip
        item(key = "home_recommended_tutorials") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TUTORIALES RECOMENDADOS",
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TutorialCatalog.topics.take(4).forEach { topic ->
                        Card(
                            modifier = Modifier
                                .width(210.dp)
                                .clickable { onNavigateSection(AppSection.TUTORIALS) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = BorderStroke(1.dp, theme.cardColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(topic.category.uppercase(), color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                Text(topic.title, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(topic.summary, color = theme.textSecondaryColor, fontSize = 10.5.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeToolCard(
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
        border = BorderStroke(1.dp, theme.cardColor)
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
                    .background(theme.cardColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = section.title,
                    tint = theme.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = section.title,
                color = theme.textColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
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
