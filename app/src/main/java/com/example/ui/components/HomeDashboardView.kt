package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSection
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus

@Composable
fun HomeDashboardView(
    theme: CarAudioThemeType,
    dspSettings: DspSettings,
    equalizerSettings: EqualizerSettings,
    capabilityReport: DspCapabilityReport,
    isAnyAudioPlaying: Boolean,
    currentTrack: AudioTrackItem?,
    playbackSeconds: Int,
    tracksCount: Int,
    recentTracks: List<AudioTrackItem> = emptyList(),
    mostPlayedTracks: List<AudioTrackItem> = emptyList(),
    favoriteTracks: List<AudioTrackItem>,
    playlists: List<MusicPlaylist>,
    outputDeviceStatus: OutputDeviceStatus,
    onNavigateSection: (AppSection) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onPlayTrack: (AudioTrackItem) -> Unit,
    onPlayAll: (Boolean) -> Unit = {},
    onSelectPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 16.dp)
            .testTag("home_dashboard_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Discreet Output & Audio Status Bar
        item(key = "home_status_header") {
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
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Tu música",
                            color = theme.textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${outputDeviceStatus.label} • $tracksCount pistas locales",
                            color = theme.textSecondaryColor,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceColor)
                        .border(
                            1.dp,
                            if (dspSettings.dspMasterEnabled) theme.accentColor.copy(alpha = 0.45f) else theme.cardColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onNavigateSection(AppSection.EQUALIZER) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("home_preset_chip")
                ) {
                    Text(
                        text = if (dspSettings.dspMasterEnabled) equalizerSettings.activePresetName else "DSP en pausa",
                        color = if (dspSettings.dspMasterEnabled) theme.accentColor else theme.textSecondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. Continue Listening Hero Card
        item(key = "home_now_playing_deck") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (currentTrack != null) onNavigateSection(AppSection.PLAYER)
                        else onNavigateSection(AppSection.MY_MUSIC)
                    }
                    .testTag("home_continue_listening_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TrackArtwork(
                            track = currentTrack,
                            theme = theme,
                            size = 64.dp,
                            cornerRadius = 12.dp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAnyAudioPlaying) "Sonando ahora" else if (currentTrack != null) "Continuar escuchando" else "Biblioteca lista",
                                color = theme.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = currentTrack?.title ?: "Elige una canción de tu teléfono",
                                color = theme.textColor,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack?.let { "${it.artist} • ${it.album}" }
                                    ?: "Toca aquí para explorar tu música",
                                color = theme.textSecondaryColor,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (currentTrack != null && currentTrack.durationSeconds > 0) {
                        val progress = (playbackSeconds.toFloat() / currentTrack.durationSeconds.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = theme.accentColor,
                                trackColor = theme.cardColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatDurationSeconds(playbackSeconds), color = theme.textSecondaryColor, fontSize = 10.5.sp)
                                Text(formatDurationSeconds(currentTrack.durationSeconds), color = theme.textSecondaryColor, fontSize = 10.5.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onPlayAll(false) },
                                enabled = tracksCount > 0,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.cardColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("home_play_all_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Todo", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            }
                            OutlinedButton(
                                onClick = { onPlayAll(true) },
                                enabled = tracksCount > 0,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.cardColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("home_shuffle_all_button")
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aleatorio", color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onPreviousTrack, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = theme.textColor)
                            }
                            IconButton(
                                onClick = {
                                    if (currentTrack != null) onTogglePlayPause()
                                    else onNavigateSection(AppSection.MY_MUSIC)
                                },
                                modifier = Modifier
                                    .size(44.dp)
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
                            IconButton(onClick = onNextTrack, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = theme.textColor)
                            }
                        }
                    }
                }
            }
        }

        // 3. Recently Played Horizontal Shelf
        if (recentTracks.isNotEmpty()) {
            item(key = "home_recent_tracks") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Reproducido recientemente",
                                color = theme.textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(onClick = { onNavigateSection(AppSection.MY_MUSIC) }) {
                            Text("Ver todo", color = theme.accentColor, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentTracks.take(10).forEach { track ->
                            Card(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clickable { onPlayTrack(track) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentTrack?.stableKey == track.stableKey) theme.accentColor.copy(alpha = 0.6f) else theme.cardColor
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TrackArtwork(
                                        track = track,
                                        theme = theme,
                                        size = 116.dp,
                                        cornerRadius = 10.dp
                                    )
                                    Text(
                                        text = track.title,
                                        color = theme.textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        color = theme.textSecondaryColor,
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Most Played Tracks (compact list)
        if (mostPlayedTracks.isNotEmpty()) {
            item(key = "home_most_played") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Más escuchadas",
                        color = theme.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        mostPlayedTracks.take(3).forEach { track ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlayTrack(track) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(1.dp, theme.cardColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TrackArtwork(track = track, theme = theme, size = 40.dp, cornerRadius = 8.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${track.artist} • ${formatDurationSeconds(track.durationSeconds)}", color = theme.textSecondaryColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = theme.accentColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Playlists & Favorite Tracks Strip
        if (favoriteTracks.isNotEmpty() || playlists.isNotEmpty()) {
            item(key = "home_favorites_and_playlists") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Listas y favoritas",
                        color = theme.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
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
                                        onNavigateSection(AppSection.MY_MUSIC)
                                    },
                                shape = RoundedCornerShape(14.dp),
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.cardColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = pl.name, tint = theme.accentColor, modifier = Modifier.size(20.dp))
                                    }
                                    Column {
                                        Text(pl.name, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${pl.trackIds.size} pistas", color = theme.textSecondaryColor, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        favoriteTracks.take(8).forEach { fav ->
                            Card(
                                modifier = Modifier
                                    .width(184.dp)
                                    .clickable { onPlayTrack(fav) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(1.dp, theme.cardColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TrackArtwork(track = fav, theme = theme, size = 38.dp, cornerRadius = 8.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(fav.title, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(fav.artist, color = theme.textSecondaryColor, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Clean Audio & Library Shortcuts Grid
        item(key = "home_modules_header") {
            Text(
                text = "Explorar y estudio de audio",
                color = theme.textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        val hubItems = listOf(
            Triple(AppSection.MY_MUSIC, Icons.Default.LibraryMusic, "$tracksCount canciones • Álbumes y carpetas"),
            Triple(AppSection.PLAYER, Icons.Default.PlayCircle, "Carátula, temporizador, velocidad y cola"),
            Triple(AppSection.EQUALIZER, Icons.Default.Equalizer, "31 y 10 bandas, paramétrico y A / B"),
            Triple(AppSection.AUDIO_TOOLS, Icons.Default.Tune, capabilityReport.engineSummary),
            Triple(AppSection.PRESETS, Icons.Default.GraphicEq, "Perfiles de sonido y comparación A / B"),
            Triple(AppSection.YOUTUBE_MUSIC, Icons.Default.MusicNote, "Búsqueda y accesos directos oficiales"),
            Triple(AppSection.FAVORITES, Icons.Default.Favorite, "Canciones favoritas y listas"),
            Triple(AppSection.SETTINGS, Icons.Default.Settings, "Temas discretos, diagnóstico y guías")
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
        shape = RoundedCornerShape(14.dp),
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
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(theme.cardColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = section.title,
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = section.title,
                color = theme.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                color = theme.textSecondaryColor,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
