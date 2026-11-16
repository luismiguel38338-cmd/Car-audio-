package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.AlbumArtCache
import com.example.model.AlbumSummary
import com.example.model.ArtistSummary
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.RepeatMode
import com.example.viewmodel.LibraryFilter

@Composable
fun PlayerView(
    theme: CarAudioThemeType,
    tracks: List<AudioTrackItem>,
    artists: List<ArtistSummary>,
    albums: List<AlbumSummary>,
    queueTracks: List<AudioTrackItem>,
    playlists: List<MusicPlaylist>,
    selectedPlaylistId: String?,
    currentTrack: AudioTrackItem?,
    isPlaying: Boolean,
    playbackSeconds: Int,
    volumeLevel: Float,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    searchQuery: String,
    libraryFilter: LibraryFilter,
    hasAudioLibraryPermission: Boolean,
    isScanningLibrary: Boolean,
    outputDeviceStatus: OutputDeviceStatus,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onTrackSelect: (AudioTrackItem) -> Unit,
    onToggleFavorite: (AudioTrackItem) -> Unit,
    onRemoveTrack: (AudioTrackItem) -> Unit,
    onAddToQueue: (AudioTrackItem) -> Unit,
    onPlayNextInQueue: (AudioTrackItem) -> Unit,
    onPlayQueueAt: (Int) -> Unit,
    onMoveQueueUp: (Int) -> Unit,
    onMoveQueueDown: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onPlayArtist: (String) -> Unit,
    onPlayAlbum: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLibraryFilterChange: (LibraryFilter) -> Unit,
    onSelectPlaylist: (String?) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddTrackToPlaylist: (String, String) -> Unit,
    onCustomTracksLoaded: (List<Uri>) -> Unit,
    onRequestLibraryPermission: () -> Unit,
    onRefreshLibrary: () -> Unit,
    onOpenAudioLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Canciones, 1=Artistas, 2=Álbumes, 3=Listas, 4=Cola
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var trackForPlaylistDialog by remember { mutableStateOf<AudioTrackItem?>(null) }

    var currentCoverBitmap by remember(currentTrack?.id) {
        mutableStateOf<Bitmap?>(AlbumArtCache.getCached(currentTrack?.customUri))
    }
    LaunchedEffect(currentTrack?.id, currentTrack?.customUri) {
        val uri = currentTrack?.customUri
        currentCoverBitmap = if (uri != null) AlbumArtCache.loadOrGet(context, uri) else null
    }

    val multiAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) onCustomTracksLoaded(uris)
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Nueva Lista de Reproducción", fontWeight = FontWeight.Black, color = theme.textColor) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    trackForPlaylistDialog?.let { targetTrack ->
        AlertDialog(
            onDismissRequest = { trackForPlaylistDialog = null },
            containerColor = theme.cardColor,
            title = { Text("Agregar a Lista", fontWeight = FontWeight.Black, color = theme.textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(targetTrack.title, color = theme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (playlists.isEmpty()) {
                        Text("Aún no tienes listas creadas.", color = theme.textSecondaryColor, fontSize = 12.sp)
                    } else {
                        playlists.forEach { pl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.surfaceColor)
                                    .clickable {
                                        onAddTrackToPlaylist(pl.id, targetTrack.id)
                                        trackForPlaylistDialog = null
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pl.name, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("${pl.trackIds.size} pistas", color = theme.textSecondaryColor, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { trackForPlaylistDialog = null }) {
                    Text("Cerrar", color = theme.accentColor)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp)
            .testTag("music_player_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Now Playing Studio Card
        item(key = "player_now_playing") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.cardColor)
                                .border(1.dp, theme.accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                currentCoverBitmap != null -> {
                                    Image(
                                        bitmap = currentCoverBitmap!!.asImageBitmap(),
                                        contentDescription = currentTrack?.title ?: "Carátula",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                currentTrack?.albumArtUri != null -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(currentTrack.albumArtUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = currentTrack.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    Icon(Icons.Default.Audiotrack, contentDescription = "Sin carátula", tint = theme.accentColor, modifier = Modifier.size(30.dp))
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack?.title ?: "Sin canción activa",
                                color = theme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack?.let { "${it.artist} • ${it.album}" }
                                    ?: "Escanea o importa música del dispositivo",
                                color = theme.textSecondaryColor,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${outputDeviceStatus.label} • ${currentTrack?.audioFormatLabel ?: "Media3"}",
                                color = theme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = onOpenAudioLab, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Equalizer, contentDescription = "Abrir Laboratorio de Audio", tint = theme.accentColor)
                        }
                    }

                    val maxSec = (currentTrack?.durationSeconds ?: 180).coerceAtLeast(1)
                    Column {
                        Slider(
                            value = playbackSeconds.coerceIn(0, maxSec).toFloat(),
                            onValueChange = { onSeek(it.toInt()) },
                            valueRange = 0f..maxSec.toFloat(),
                            enabled = currentTrack != null,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.accentColor,
                                inactiveTrackColor = theme.cardColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatDurationSeconds(playbackSeconds), color = theme.textSecondaryColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(formatDurationSeconds(currentTrack?.durationSeconds ?: 0), color = theme.textSecondaryColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Aleatorio",
                                tint = if (shuffleEnabled) theme.accentColor else theme.textSecondaryColor
                            )
                        }
                        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp).testTag("player_prev_button")) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = theme.textColor, modifier = Modifier.size(28.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                                .clickable { onPlayPause() }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = theme.onPrimaryColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(onClick = onNext, modifier = Modifier.size(48.dp).testTag("player_next_button")) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = theme.textColor, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = onCycleRepeat, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = repeatMode.label,
                                tint = if (repeatMode != RepeatMode.OFF) theme.accentColor else theme.textSecondaryColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volumen", tint = theme.textSecondaryColor, modifier = Modifier.size(18.dp))
                        Slider(
                            value = volumeLevel,
                            onValueChange = onVolumeChange,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.accentColor,
                                inactiveTrackColor = theme.cardColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        )
                        Text("${(volumeLevel * 100).toInt()}%", color = theme.textSecondaryColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 2. Signal Bar
        item(key = "player_signal_bar") {
            StudioSignalIndicatorBar(
                isAudioPlaying = isAnyAudioPlaying,
                bassEnergy = bassEnergy,
                midEnergy = midEnergy,
                trebleEnergy = trebleEnergy,
                theme = theme,
                liteVisuals = liteVisuals,
                title = "SEÑAL MEDIA3 EN TIEMPO REAL"
            )
        }

        // 3. Import & Scan Actions + Search
        item(key = "player_actions_and_search") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { multiAudioPicker.launch(arrayOf("audio/*")) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_import_audio_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar Música", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (hasAudioLibraryPermission) onRefreshLibrary() else onRequestLibraryPermission()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, theme.cardColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_scan_device_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isScanningLibrary -> "Escaneando..."
                                !hasAudioLibraryPermission -> "Dar Permiso"
                                else -> "Escanear Teléfono"
                            },
                            color = theme.accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar canciones, artistas o álbumes...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar música") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.accentColor,
                        unfocusedBorderColor = theme.cardColor,
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor,
                        focusedContainerColor = theme.surfaceColor,
                        unfocusedContainerColor = theme.surfaceColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. 5 Real Library Tabs (Canciones, Artistas, Álbumes, Listas, Cola)
        item(key = "player_sub_tabs") {
            val tabTitles = listOf("Canciones", "Artistas", "Álbumes", "Listas", "Cola (${queueTracks.size})")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = theme.surfaceColor,
                contentColor = theme.accentColor,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = theme.accentColor
                        )
                    }
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("player_tab_$idx")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> { // Canciones
                item(key = "songs_filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LibraryFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = libraryFilter == filter && selectedPlaylistId == null,
                                onClick = {
                                    onSelectPlaylist(null)
                                    onLibraryFilterChange(filter)
                                },
                                label = { Text(filter.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.accentColor,
                                    selectedLabelColor = theme.onPrimaryColor,
                                    containerColor = theme.surfaceColor,
                                    labelColor = theme.textColor
                                )
                            )
                        }
                    }
                }

                if (tracks.isEmpty()) {
                    item(key = "empty_tracks_notice") {
                        EmptyLibraryNotice(
                            theme = theme,
                            title = "No se encontraron canciones",
                            subtitle = "Escanea el almacenamiento del teléfono o importa tus archivos MP3/FLAC/WAV locales."
                        )
                    }
                } else {
                    items(tracks, key = { it.stableKey }) { track ->
                        TrackRowCard(
                            track = track,
                            isActive = currentTrack?.stableKey == track.stableKey,
                            theme = theme,
                            onSelect = { onTrackSelect(track) },
                            onToggleFavorite = { onToggleFavorite(track) },
                            onAddToQueue = { onAddToQueue(track) },
                            onPlayNext = { onPlayNextInQueue(track) },
                            onAddToPlaylist = { trackForPlaylistDialog = track },
                            onRemove = { onRemoveTrack(track) }
                        )
                    }
                }
            }

            1 -> { // Artistas
                if (artists.isEmpty()) {
                    item(key = "empty_artists") {
                        EmptyLibraryNotice(theme, "Sin artistas locales", "Escanea o importa música para agrupar por artista.")
                    }
                } else {
                    items(artists, key = { it.name }) { artist ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayArtist(artist.name) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = BorderStroke(1.dp, theme.cardColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(theme.cardColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = artist.name, tint = theme.accentColor)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(artist.name, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${artist.trackCount} canciones • ${artist.albumCount} álbumes", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir artista", tint = theme.accentColor)
                            }
                        }
                    }
                }
            }

            2 -> { // Álbumes
                if (albums.isEmpty()) {
                    item(key = "empty_albums") {
                        EmptyLibraryNotice(theme, "Sin álbumes locales", "Escanea o importa música para agrupar por álbum.")
                    }
                } else {
                    items(albums, key = { "${it.title}_${it.artist}" }) { album ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayAlbum(album.title) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = BorderStroke(1.dp, theme.cardColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.cardColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Album, contentDescription = album.title, tint = theme.accentColor)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${album.artist} • ${album.trackCount} pistas", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir álbum", tint = theme.accentColor)
                            }
                        }
                    }
                }
            }

            3 -> { // Listas
                item(key = "create_playlist_row") {
                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor, contentColor = theme.accentColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Nueva Lista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (playlists.isEmpty()) {
                    item(key = "empty_playlists") {
                        EmptyLibraryNotice(theme, "Sin listas creadas", "Crea una lista y agrega canciones desde el menú de cada pista.")
                    }
                } else {
                    items(playlists, key = { it.id }) { pl ->
                        val active = selectedPlaylistId == pl.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectPlaylist(if (active) null else pl.id)
                                    selectedTab = 0
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = BorderStroke(1.dp, if (active) theme.accentColor else theme.cardColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pl.name, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${pl.trackIds.size} canciones sincronizadas", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                IconButton(onClick = { onDeletePlaylist(pl.id) }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = theme.textSecondaryColor)
                                }
                            }
                        }
                    }
                }
            }

            else -> { // Cola de reproducción
                item(key = "queue_header_actions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLA ACTUAL (${queueTracks.size} pistas)",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        if (queueTracks.isNotEmpty()) {
                            TextButton(onClick = onClearQueue) {
                                Text("Limpiar Cola", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (queueTracks.isEmpty()) {
                    item(key = "empty_queue") {
                        EmptyLibraryNotice(theme, "Cola vacía", "Añade canciones desde la pestaña Canciones para programar tu sesión.")
                    }
                } else {
                    itemsIndexed(queueTracks, key = { index, item -> "${index}_${item.stableKey}" }) { index, qTrack ->
                        val isActive = currentTrack?.stableKey == qTrack.stableKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayQueueAt(index) }
                                .testTag("queue_item_$index"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isActive) theme.cardColor else theme.surfaceColor),
                            border = BorderStroke(1.dp, if (isActive) theme.accentColor else theme.cardColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", color = theme.accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(24.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(qTrack.title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(qTrack.artist, color = theme.textSecondaryColor, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { onMoveQueueUp(index) }, enabled = index > 0, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Subir en cola", tint = if (index > 0) theme.textColor else theme.textSecondaryColor.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onMoveQueueDown(index) }, enabled = index < queueTracks.lastIndex, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar en cola", tint = if (index < queueTracks.lastIndex) theme.textColor else theme.textSecondaryColor.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onRemoveFromQueue(index) }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar de la cola", tint = theme.textSecondaryColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRowCard(
    track: AudioTrackItem,
    isActive: Boolean,
    theme: CarAudioThemeType,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemove: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("track_row_${track.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) theme.cardColor else theme.surfaceColor),
        border = BorderStroke(1.dp, if (isActive) theme.accentColor else theme.cardColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = track.title,
                    tint = if (isActive) theme.accentColor else theme.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isActive) theme.accentColor else theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${formatDurationSeconds(track.durationSeconds)} • ${track.audioFormatLabel}",
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (track.isFavorite) theme.accentColor else theme.textSecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones de pista", tint = theme.textSecondaryColor)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = theme.cardColor
                ) {
                    DropdownMenuItem(
                        text = { Text("Reproducir a continuación", color = theme.textColor) },
                        leadingIcon = { Icon(Icons.Default.QueuePlayNext, contentDescription = null, tint = theme.accentColor) },
                        onClick = {
                            menuExpanded = false
                            onPlayNext()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Añadir a la Cola", color = theme.textColor) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = theme.accentColor) },
                        onClick = {
                            menuExpanded = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar a una Lista", color = theme.textColor) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = theme.accentColor) },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Quitar de la Biblioteca", color = theme.errorColor) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = theme.errorColor) },
                        onClick = {
                            menuExpanded = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryNotice(
    theme: CarAudioThemeType,
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(1.dp, theme.cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = theme.textSecondaryColor, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}
