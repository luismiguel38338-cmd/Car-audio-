package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AlbumSummary
import com.example.model.ArtistSummary
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.FolderSummary
import com.example.model.LibrarySortOrder
import com.example.model.MusicPlaylist
import com.example.viewmodel.LibraryFilter
import kotlinx.coroutines.delay

@Composable
fun MyMusicView(
    theme: CarAudioThemeType,
    tracks: List<AudioTrackItem>,
    artists: List<ArtistSummary>,
    albums: List<AlbumSummary>,
    folders: List<FolderSummary> = emptyList(),
    playlists: List<MusicPlaylist>,
    selectedPlaylistId: String?,
    currentTrack: AudioTrackItem?,
    searchQuery: String,
    libraryFilter: LibraryFilter,
    sortOrder: LibrarySortOrder = LibrarySortOrder.RECENT,
    trackPlayCounts: Map<String, Int> = emptyMap(),
    hasAudioLibraryPermission: Boolean,
    isScanningLibrary: Boolean,
    onTrackSelect: (AudioTrackItem) -> Unit,
    onPlayAll: (Boolean) -> Unit = {},
    onToggleFavorite: (AudioTrackItem) -> Unit,
    onRemoveTrack: (AudioTrackItem) -> Unit,
    onAddToQueue: (AudioTrackItem) -> Unit,
    onPlayNextInQueue: (AudioTrackItem) -> Unit,
    onPlayArtist: (String) -> Unit,
    onPlayAlbum: (String) -> Unit,
    onPlayFolder: (String) -> Unit = {},
    onPlayPlaylist: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onLibraryFilterChange: (LibraryFilter) -> Unit,
    onSortOrderChange: (LibrarySortOrder) -> Unit = {},
    onSelectPlaylist: (String?) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddTrackToPlaylist: (String, String) -> Unit,
    onCustomTracksLoaded: (List<Uri>) -> Unit,
    onRequestLibraryPermission: () -> Unit,
    onRefreshLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Canciones, 1=Álbumes, 2=Artistas, 3=Carpetas, 4=Listas
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var trackForPlaylistDialog by remember { mutableStateOf<AudioTrackItem?>(null) }
    var trackForDetailsDialog by remember { mutableStateOf<AudioTrackItem?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    var selectedAlbumTitle by remember { mutableStateOf<String?>(null) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var selectedFolderName by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionMessage) {
        if (actionMessage != null) {
            delay(2200L)
            actionMessage = null
        }
    }

    val hasInlineDrilldown = (selectedTab == 1 && selectedAlbumTitle != null) ||
        (selectedTab == 2 && selectedArtistName != null) ||
        (selectedTab == 3 && selectedFolderName != null)

    BackHandler(enabled = hasInlineDrilldown) {
        when (selectedTab) {
            1 -> selectedAlbumTitle = null
            2 -> selectedArtistName = null
            3 -> selectedFolderName = null
        }
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
            title = { Text("Nueva lista de reproducción", fontWeight = FontWeight.Bold, color = theme.textColor) },
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
                            val name = newPlaylistName.trim()
                            onCreatePlaylist(name)
                            actionMessage = "Lista \"$name\" creada"
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
            title = { Text("Agregar a lista", fontWeight = FontWeight.Bold, color = theme.textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(targetTrack.title, color = theme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                        actionMessage = "Agregada a \"${pl.name}\""
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
                TextButton(onClick = {
                    trackForPlaylistDialog = null
                    showCreatePlaylistDialog = true
                }) {
                    Text("+ Nueva lista", color = theme.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackForPlaylistDialog = null }) {
                    Text("Cerrar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    trackForDetailsDialog?.let { target ->
        val count = trackPlayCounts[target.stableKey] ?: trackPlayCounts[target.id] ?: 0
        TrackDetailsDialog(
            track = target,
            playCount = count,
            theme = theme,
            onDismiss = { trackForDetailsDialog = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .testTag("my_music_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "mymusic_header") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = true
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = theme.accentColor)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biblioteca musical", color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tracks.size} canciones • ${albums.size} álbumes • ${folders.size} carpetas",
                                color = theme.textSecondaryColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowActionButton(
                            text = "Importar audio",
                            theme = theme,
                            onClick = { multiAudioPicker.launch(arrayOf("audio/*")) },
                            leadingIcon = {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mymusic_import_button")
                        )

                        OutlinedButton(
                            onClick = {
                                if (hasAudioLibraryPermission) onRefreshLibrary() else onRequestLibraryPermission()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mymusic_scan_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isScanningLibrary -> "Escaneando..."
                                    !hasAudioLibraryPermission -> "Dar permiso"
                                    else -> "Actualizar"
                                },
                                color = theme.accentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Buscar canción, artista, álbum o carpeta...", fontSize = 12.sp) },
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
                            focusedContainerColor = theme.backgroundColor,
                            unfocusedContainerColor = theme.backgroundColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "mymusic_action_feedback") {
            AnimatedVisibility(
                visible = actionMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.primaryColor.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(17.dp))
                        Text(
                            text = actionMessage.orEmpty(),
                            color = theme.silverDetailColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item(key = "mymusic_tabs") {
            val tabTitles = listOf("Canciones", "Álbumes", "Artistas", "Carpetas", "Listas")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp,
                containerColor = theme.surfaceColor.copy(alpha = 0.80f),
                contentColor = theme.accentColor,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = theme.accentColor
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = {
                            selectedTab = idx
                            if (idx != 1) selectedAlbumTitle = null
                            if (idx != 2) selectedArtistName = null
                            if (idx != 3) selectedFolderName = null
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("mymusic_tab_$idx")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item(key = "mymusic_batch_and_sort") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onPlayAll(false) },
                            enabled = tracks.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reproducir todo", color = theme.textColor, fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onPlayAll(true) },
                            enabled = tracks.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aleatorio", color = theme.textColor, fontSize = 11.sp, maxLines = 1)
                        }

                        Box {
                            OutlinedButton(
                                onClick = { sortMenuExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.cardColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("mymusic_sort_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(sortOrder.label, color = theme.textColor, fontSize = 11.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                containerColor = theme.cardColor
                            ) {
                                LibrarySortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.label,
                                                color = if (order == sortOrder) theme.accentColor else theme.textColor,
                                                fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            sortMenuExpanded = false
                                            onSortOrderChange(order)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "mymusic_filter_chips") {
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
                        if (selectedPlaylistId != null) {
                            val activePl = playlists.firstOrNull { it.id == selectedPlaylistId }
                            if (activePl != null) {
                                FilterChip(
                                    selected = true,
                                    onClick = { onSelectPlaylist(null) },
                                    label = { Text("${activePl.name} ✕", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.primaryColor,
                                        selectedLabelColor = theme.onPrimaryColor
                                    )
                                )
                            }
                        }
                    }
                }

                if (tracks.isEmpty()) {
                    if (!hasAudioLibraryPermission) {
                        item(key = "mymusic_permission_card") {
                            GlassStudioCard(theme = theme, highlightBorder = true) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Permiso necesario para leer tu música",
                                        color = theme.textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Autoriza el acceso al almacenamiento de audio para listar automáticamente las canciones de tu teléfono, o usa 'Importar audio' para archivos específicos.",
                                        color = theme.textSecondaryColor,
                                        fontSize = 12.sp
                                    )
                                    GlowActionButton(
                                        text = "Permitir acceso a música",
                                        theme = theme,
                                        onClick = onRequestLibraryPermission,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("mymusic_empty_permission_button")
                                    )
                                }
                            }
                        }
                    } else {
                        item(key = "mymusic_empty_tracks") {
                            EmptyLibraryNotice(
                                theme = theme,
                                title = "No se encontraron canciones",
                                subtitle = "Pulsa 'Actualizar' o 'Importar audio' para cargar archivos MP3, FLAC, WAV o M4A."
                            )
                        }
                    }
                } else {
                    items(tracks, key = { it.stableKey }) { track ->
                        TrackRowCard(
                            track = track,
                            isActive = currentTrack?.stableKey == track.stableKey,
                            theme = theme,
                            onSelect = { onTrackSelect(track) },
                            onToggleFavorite = { onToggleFavorite(track) },
                            onAddToQueue = {
                                onAddToQueue(track)
                                actionMessage = "Añadida a la cola: ${track.title}"
                            },
                            onPlayNext = {
                                onPlayNextInQueue(track)
                                actionMessage = "Sonará después: ${track.title}"
                            },
                            onAddToPlaylist = { trackForPlaylistDialog = track },
                            onRemove = { onRemoveTrack(track) },
                            onShowDetails = { trackForDetailsDialog = track }
                        )
                    }
                }
            }

            1 -> {
                val activeAlbum = selectedAlbumTitle
                if (activeAlbum != null) {
                    val albumTracks = tracks.filter { it.album.equals(activeAlbum, ignoreCase = true) }
                    item(key = "album_drilldown_header") {
                        DrilldownGroupHeader(
                            title = activeAlbum,
                            subtitle = "${albumTracks.size} canciones en este álbum",
                            theme = theme,
                            onBack = { selectedAlbumTitle = null },
                            onPlayAll = { onPlayAlbum(activeAlbum) }
                        )
                    }
                    if (albumTracks.isEmpty()) {
                        item(key = "album_drilldown_empty") {
                            EmptyLibraryNotice(theme, "Sin pistas visibles", "Limpia el filtro o la búsqueda para ver todas las canciones de este álbum.")
                        }
                    } else {
                        items(albumTracks, key = { "album_${it.stableKey}" }) { track ->
                            TrackRowCard(
                                track = track,
                                isActive = currentTrack?.stableKey == track.stableKey,
                                theme = theme,
                                onSelect = { onTrackSelect(track) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onAddToQueue = {
                                    onAddToQueue(track)
                                    actionMessage = "Añadida a la cola: ${track.title}"
                                },
                                onPlayNext = {
                                    onPlayNextInQueue(track)
                                    actionMessage = "Sonará después: ${track.title}"
                                },
                                onAddToPlaylist = { trackForPlaylistDialog = track },
                                onRemove = { onRemoveTrack(track) },
                                onShowDetails = { trackForDetailsDialog = track }
                            )
                        }
                    }
                } else if (albums.isEmpty()) {
                    item(key = "mymusic_empty_albums") {
                        EmptyLibraryNotice(theme, "Sin álbumes locales", "Escanea o importa música para agrupar por álbum.")
                    }
                } else {
                    items(albums, key = { "${it.title}_${it.artist}" }) { album ->
                        GlassStudioCard(
                            theme = theme,
                            modifier = Modifier.clickable { selectedAlbumTitle = album.title }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.cardColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (album.sampleArtUri != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(album.sampleArtUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = album.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Album, contentDescription = album.title, tint = theme.accentColor)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.title, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${album.artist} • ${album.trackCount} pistas", color = theme.textSecondaryColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(
                                    onClick = { onPlayAlbum(album.title) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir álbum", tint = theme.accentColor)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val activeArtist = selectedArtistName
                if (activeArtist != null) {
                    val artistTracks = tracks.filter { it.artist.equals(activeArtist, ignoreCase = true) }
                    item(key = "artist_drilldown_header") {
                        DrilldownGroupHeader(
                            title = activeArtist,
                            subtitle = "${artistTracks.size} canciones disponibles",
                            theme = theme,
                            onBack = { selectedArtistName = null },
                            onPlayAll = { onPlayArtist(activeArtist) }
                        )
                    }
                    if (artistTracks.isEmpty()) {
                        item(key = "artist_drilldown_empty") {
                            EmptyLibraryNotice(theme, "Sin pistas visibles", "Limpia el filtro o la búsqueda para ver las canciones de este artista.")
                        }
                    } else {
                        items(artistTracks, key = { "artist_${it.stableKey}" }) { track ->
                            TrackRowCard(
                                track = track,
                                isActive = currentTrack?.stableKey == track.stableKey,
                                theme = theme,
                                onSelect = { onTrackSelect(track) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onAddToQueue = {
                                    onAddToQueue(track)
                                    actionMessage = "Añadida a la cola: ${track.title}"
                                },
                                onPlayNext = {
                                    onPlayNextInQueue(track)
                                    actionMessage = "Sonará después: ${track.title}"
                                },
                                onAddToPlaylist = { trackForPlaylistDialog = track },
                                onRemove = { onRemoveTrack(track) },
                                onShowDetails = { trackForDetailsDialog = track }
                            )
                        }
                    }
                } else if (artists.isEmpty()) {
                    item(key = "mymusic_empty_artists") {
                        EmptyLibraryNotice(theme, "Sin artistas locales", "Escanea o importa música para agrupar por artista.")
                    }
                } else {
                    items(artists, key = { it.name }) { artist ->
                        GlassStudioCard(
                            theme = theme,
                            modifier = Modifier.clickable { selectedArtistName = artist.name }
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
                                    Text(artist.name, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${artist.trackCount} canciones • ${artist.albumCount} álbumes", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { onPlayArtist(artist.name) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir artista", tint = theme.accentColor)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                val activeFolder = selectedFolderName
                if (activeFolder != null) {
                    val folderTracks = tracks.filter { it.folderName.equals(activeFolder, ignoreCase = true) }
                    item(key = "folder_drilldown_header") {
                        DrilldownGroupHeader(
                            title = activeFolder,
                            subtitle = "${folderTracks.size} archivos en esta carpeta",
                            theme = theme,
                            onBack = { selectedFolderName = null },
                            onPlayAll = { onPlayFolder(activeFolder) }
                        )
                    }
                    if (folderTracks.isEmpty()) {
                        item(key = "folder_drilldown_empty") {
                            EmptyLibraryNotice(theme, "Sin archivos visibles", "Limpia el filtro o la búsqueda para ver las pistas de esta carpeta.")
                        }
                    } else {
                        items(folderTracks, key = { "folder_${it.stableKey}" }) { track ->
                            TrackRowCard(
                                track = track,
                                isActive = currentTrack?.stableKey == track.stableKey,
                                theme = theme,
                                onSelect = { onTrackSelect(track) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onAddToQueue = {
                                    onAddToQueue(track)
                                    actionMessage = "Añadida a la cola: ${track.title}"
                                },
                                onPlayNext = {
                                    onPlayNextInQueue(track)
                                    actionMessage = "Sonará después: ${track.title}"
                                },
                                onAddToPlaylist = { trackForPlaylistDialog = track },
                                onRemove = { onRemoveTrack(track) },
                                onShowDetails = { trackForDetailsDialog = track }
                            )
                        }
                    }
                } else if (folders.isEmpty()) {
                    item(key = "mymusic_empty_folders") {
                        EmptyLibraryNotice(theme, "Sin carpetas detectadas", "Escanea el dispositivo para organizar tu música por carpetas.")
                    }
                } else {
                    items(folders, key = { it.name }) { folder ->
                        GlassStudioCard(
                            theme = theme,
                            modifier = Modifier.clickable { selectedFolderName = folder.name }
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
                                    Icon(Icons.Default.Folder, contentDescription = folder.name, tint = theme.accentColor)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(folder.name, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${folder.trackCount} archivos de audio", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { onPlayFolder(folder.name) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir carpeta", tint = theme.accentColor)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                item(key = "mymusic_create_playlist") {
                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor, contentColor = theme.accentColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear nueva lista", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (playlists.isEmpty()) {
                    item(key = "mymusic_empty_playlists") {
                        EmptyLibraryNotice(theme, "Sin listas creadas", "Crea una lista y añade pistas desde el menú de cada canción.")
                    }
                } else {
                    items(playlists, key = { it.id }) { pl ->
                        val active = selectedPlaylistId == pl.id
                        GlassStudioCard(
                            theme = theme,
                            highlightBorder = active,
                            modifier = Modifier.clickable {
                                onSelectPlaylist(if (active) null else pl.id)
                                selectedTab = 0
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pl.name, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${pl.trackIds.size} pistas guardadas", color = theme.textSecondaryColor, fontSize = 11.sp)
                                }
                                if (pl.trackIds.isNotEmpty()) {
                                    IconButton(onClick = { onPlayPlaylist(pl.id) }, modifier = Modifier.size(44.dp)) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir lista", tint = theme.accentColor)
                                    }
                                }
                                IconButton(onClick = { onDeletePlaylist(pl.id) }, modifier = Modifier.size(44.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = theme.textSecondaryColor)
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
private fun DrilldownGroupHeader(
    title: String,
    subtitle: String,
    theme: CarAudioThemeType,
    onBack: () -> Unit,
    onPlayAll: () -> Unit
) {
    GlassStudioCard(theme = theme, highlightBorder = true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = theme.silverDetailColor
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.textColor,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp
                )
            }
            OutlinedButton(
                onClick = onPlayAll,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.65f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Todo", color = theme.accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
