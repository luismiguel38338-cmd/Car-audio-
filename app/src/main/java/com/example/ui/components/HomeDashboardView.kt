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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.DspCapabilityReport
import com.example.model.DspEngineStatus
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
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
    bassMode: BassMode = BassMode.NORMAL,
    presets: List<EqPresetItem> = EqPresetCatalog.presets,
    onNavigateSection: (AppSection) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onPlayTrack: (AudioTrackItem) -> Unit,
    onPlayAll: (Boolean) -> Unit = {},
    onSelectPlaylist: (String) -> Unit,
    onSelectBassMode: (BassMode) -> Unit = {},
    onSelectPreset: (EqPresetItem) -> Unit = {},
    bassEnergy: Float = 0.28f,
    midEnergy: Float = 0.24f,
    trebleEnergy: Float = 0.20f,
    liteVisuals: Boolean = false,
    libraryTracks: List<AudioTrackItem> = emptyList(),
    hasAudioLibraryPermission: Boolean = true,
    onRequestAudioPermission: () -> Unit = {},
    onToggleFavorite: (AudioTrackItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quickPlayTracks = remember(libraryTracks, recentTracks, mostPlayedTracks) {
        libraryTracks
            .ifEmpty { recentTracks.ifEmpty { mostPlayedTracks } }
            .take(6)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .testTag("home_dashboard_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cabecera limpia estilo reproductor del teléfono + estado EQ/Salida
        item(key = "home_status_header") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = dspSettings.dspMasterEnabled
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                            contentDescription = "Car Audio DSP Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, theme.accentColor, CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Car Audio DSP Pro",
                                color = theme.silverDetailColor,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = outputDeviceStatus.label,
                                    color = theme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val engineLight = when (capabilityReport.engineStatus) {
                            DspEngineStatus.ACTIVE_INTERNAL, DspEngineStatus.ACTIVE_EXTERNAL -> SmartLightStatus.ACTIVE_READY
                            DspEngineStatus.LIMITED_FALLBACK -> SmartLightStatus.WARNING_LIMIT
                            DspEngineStatus.BYPASSED -> SmartLightStatus.DISABLED
                        }
                        SmartStatusLedBadge(
                            status = engineLight,
                            text = capabilityReport.engineStatus.badgeText,
                            theme = theme,
                            liteVisuals = liteVisuals,
                            pulse = isAnyAudioPlaying
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            theme.backgroundColor.copy(alpha = 0.90f),
                                            theme.cardColor.copy(alpha = 0.70f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (dspSettings.dspMasterEnabled) theme.accentColor.copy(alpha = 0.65f) else theme.cardColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onNavigateSection(AppSection.EQUALIZER) }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("home_preset_chip")
                        ) {
                            Text(
                                text = if (dspSettings.dspMasterEnabled) equalizerSettings.activePresetName else "EQ en pausa",
                                color = if (dspSettings.dspMasterEnabled) theme.accentColor else theme.textSecondaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 2. Aviso directo para acceder a la música del teléfono si falta permiso
        if (!hasAudioLibraryPermission && libraryTracks.isEmpty()) {
            item(key = "home_permission_prompt") {
                GlassStudioCard(theme = theme, highlightBorder = true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.primaryColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.accentColor)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accede a la música de tu teléfono",
                                    color = theme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Permite el acceso para mostrar y reproducir todas tus canciones MP3, FLAC y WAV.",
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        GlowActionButton(
                            text = "Permitir acceso a música",
                            theme = theme,
                            onClick = onRequestAudioPermission,
                            isPrimary = true,
                            leadingIcon = {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_request_permission_button")
                        )
                    }
                }
            }
        }

        // 3. Tarjeta Hero Reproductor (Sonando ahora / Reproducir todo)
        item(key = "home_now_playing_deck") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = isAnyAudioPlaying,
                modifier = Modifier
                    .clickable {
                        if (currentTrack != null) onNavigateSection(AppSection.PLAYER)
                        else onNavigateSection(AppSection.MY_MUSIC)
                    }
                    .testTag("home_continue_listening_card")
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .border(
                                    1.2.dp,
                                    if (isAnyAudioPlaying) theme.accentColor.copy(alpha = 0.75f) else theme.cardColor,
                                    RoundedCornerShape(15.dp)
                                )
                        ) {
                            TrackArtwork(
                                track = currentTrack,
                                theme = theme,
                                size = 66.dp,
                                cornerRadius = 14.dp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    isAnyAudioPlaying -> "SONANDO AHORA"
                                    currentTrack != null -> "LISTO PARA SONAR"
                                    else -> "TU MÚSICA"
                                },
                                color = theme.accentColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = currentTrack?.title ?: "Elige una canción de tu teléfono",
                                color = theme.textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack?.let { "${it.artist} • ${it.album}" }
                                    ?: "$tracksCount canciones en tu biblioteca",
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
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatDurationSeconds(playbackSeconds), color = theme.textSecondaryColor, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                                Text(formatDurationSeconds(currentTrack.durationSeconds), color = theme.textSecondaryColor, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowActionButton(
                                text = "Todo",
                                theme = theme,
                                onClick = { onPlayAll(false) },
                                enabled = tracksCount > 0,
                                isPrimary = true,
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_play_all_button")
                            )
                            GlowActionButton(
                                text = "Aleatorio",
                                theme = theme,
                                onClick = { onPlayAll(true) },
                                enabled = tracksCount > 0,
                                isPrimary = false,
                                leadingIcon = {
                                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_shuffle_all_button")
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onPreviousTrack, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = theme.silverDetailColor)
                            }
                            IconButton(
                                onClick = {
                                    if (currentTrack != null) onTogglePlayPause()
                                    else onNavigateSection(AppSection.MY_MUSIC)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                theme.primaryColor,
                                                theme.accentColor
                                            )
                                        )
                                    )
                                    .border(1.dp, theme.silverDetailColor.copy(alpha = 0.5f), CircleShape)
                                    .testTag("home_quick_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (isAnyAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAnyAudioPlaying) "Pausar reproducción" else "Reproducir música",
                                    tint = theme.onPrimaryColor
                                )
                            }
                            IconButton(onClick = onNextTrack, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = theme.silverDetailColor)
                            }
                        }
                    }

                    StudioSignalIndicatorBar(
                        isAudioPlaying = isAnyAudioPlaying,
                        bassEnergy = bassEnergy,
                        midEnergy = midEnergy,
                        trebleEnergy = trebleEnergy,
                        theme = theme,
                        liteVisuals = liteVisuals,
                        title = "ESPECTRO DE SALIDA EN VIVO",
                        showMiniBars = !liteVisuals
                    )
                }
            }
        }

        // 4. Lista directa de canciones del teléfono para tocar y reproducir al instante
        if (quickPlayTracks.isNotEmpty()) {
            item(key = "home_quick_tracks_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tracksCount > 0) "Tu música ($tracksCount)" else "Tu música",
                        color = theme.silverDetailColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { onNavigateSection(AppSection.MY_MUSIC) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Ver todas", color = theme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            items(quickPlayTracks, key = { "home_track_${it.stableKey}" }) { track ->
                val isActive = currentTrack?.stableKey == track.stableKey
                GlassStudioCard(
                    theme = theme,
                    highlightBorder = isActive,
                    modifier = Modifier
                        .clickable { onPlayTrack(track) }
                        .testTag("home_track_row_${track.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackArtwork(track = track, theme = theme, size = 44.dp, cornerRadius = 10.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = if (isActive) theme.accentColor else theme.textColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${track.artist} • ${formatDurationSeconds(track.durationSeconds)}",
                                color = theme.textSecondaryColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { onToggleFavorite(track) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorita",
                                tint = if (track.isFavorite) theme.accentColor else theme.textSecondaryColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Escuchado recientemente
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
                                text = "Escuchado recientemente",
                                color = theme.textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = { onNavigateSection(AppSection.MY_MUSIC) }) {
                            Text("Ver todo", color = theme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentTracks.take(10).forEach { track ->
                            GlassStudioCard(
                                theme = theme,
                                highlightBorder = currentTrack?.stableKey == track.stableKey,
                                modifier = Modifier
                                    .width(136.dp)
                                    .clickable { onPlayTrack(track) }
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

        // 6. Listas y favoritas
        if (favoriteTracks.isNotEmpty() || playlists.isNotEmpty()) {
            item(key = "home_favorites_and_playlists") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Listas y favoritas",
                        color = theme.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        playlists.forEach { pl ->
                            GlassStudioCard(
                                theme = theme,
                                modifier = Modifier
                                    .width(168.dp)
                                    .clickable {
                                        onSelectPlaylist(pl.id)
                                        onNavigateSection(AppSection.MY_MUSIC)
                                    }
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
                                            .background(theme.primaryColor.copy(alpha = 0.16f)),
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
                            GlassStudioCard(
                                theme = theme,
                                modifier = Modifier
                                    .width(184.dp)
                                    .clickable { onPlayTrack(fav) }
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

        // 7. Perfil de sonido rápido (compacto, sin saturar el inicio)
        item(key = "home_quick_sound_deck") {
            GlassStudioCard(theme = theme, highlightBorder = dspSettings.bassBoostEnabled) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ecualizador y graves",
                            color = theme.silverDetailColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onNavigateSection(AppSection.BASS_BOOST) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Ajustar graves", color = theme.accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BassMode.entries.forEach { mode ->
                            val selected = mode == bassMode && dspSettings.bassBoostEnabled
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) theme.primaryColor.copy(alpha = 0.25f)
                                        else theme.backgroundColor.copy(alpha = 0.85f)
                                    )
                                    .border(
                                        width = if (selected) 1.2.dp else 1.dp,
                                        color = if (selected) theme.accentColor else theme.cardColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectBassMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (selected) theme.silverDetailColor else theme.textColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(8).forEach { preset ->
                            val active = equalizerSettings.activePresetName.equals(preset.name, ignoreCase = true)
                            FilterChip(
                                selected = active,
                                onClick = { onSelectPreset(preset) },
                                label = {
                                    Text(
                                        text = preset.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (active) theme.silverDetailColor.copy(alpha = 0.4f) else theme.cardColor
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.backgroundColor.copy(alpha = 0.85f),
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // 8. Accesos Rápidos (2x2)
        item(key = "home_primary_shortcuts") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Acceso rápido",
                    color = theme.silverDetailColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeFeatureCard(
                        title = "Ecualizador",
                        subtitle = "${equalizerSettings.activePresetName} • 31B",
                        icon = Icons.Default.Equalizer,
                        highlightColor = theme.primaryColor,
                        theme = theme,
                        onClick = { onNavigateSection(AppSection.EQUALIZER) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tile_equalizer")
                    )
                    HomeFeatureCard(
                        title = "Subwoofer 3D",
                        subtitle = "Graves y perillas",
                        icon = Icons.Default.Speaker,
                        highlightColor = theme.accentColor,
                        theme = theme,
                        onClick = { onNavigateSection(AppSection.SUBWOOFER) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tile_subwoofer")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeFeatureCard(
                        title = "Biblioteca",
                        subtitle = "$tracksCount canciones",
                        icon = Icons.Default.LibraryMusic,
                        highlightColor = theme.primaryColor,
                        theme = theme,
                        onClick = { onNavigateSection(AppSection.MY_MUSIC) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tile_my_music")
                    )
                    HomeFeatureCard(
                        title = "Música Online",
                        subtitle = "Jamendo y YT Music",
                        icon = Icons.Default.CloudQueue,
                        highlightColor = theme.accentColor,
                        theme = theme,
                        onClick = { onNavigateSection(AppSection.ONLINE_MUSIC) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tile_online_music")
                    )
                }
            }
        }

        // 9. Barra compacta de herramientas adicionales
        item(key = "home_utility_strip") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(AppSection.DRIVING_MODE, "Modo Conducción", Icons.Default.DirectionsCar),
                    Triple(AppSection.AUDIO_TOOLS, "Estudio DSP", Icons.Default.Equalizer),
                    Triple(AppSection.PRESETS, "Presets", Icons.Default.MusicNote),
                    Triple(AppSection.VISUALIZER, "Visualizador 3D", Icons.Default.GraphicEq),
                    Triple(AppSection.DOWNLOADS, "Descargas", Icons.Default.DownloadDone),
                    Triple(AppSection.SETTINGS, "Ajustes", Icons.Default.Settings)
                ).forEach { (section, label, icon) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceColor.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clickable { onNavigateSection(section) }
                            .testTag("home_tile_${section.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(icon, contentDescription = label, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Text(label, color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item(key = "home_brand_footer") {
            BrandFooterBadge(theme = theme)
        }
    }
}

@Composable
private fun HomeFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    highlightColor: Color,
    theme: CarAudioThemeType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(highlightColor.copy(alpha = 0.16f))
                    .border(0.8.dp, highlightColor.copy(alpha = 0.45f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = highlightColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.textColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
