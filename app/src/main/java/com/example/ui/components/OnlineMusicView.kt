package com.example.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CarAudioThemeType
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.OnlineSearchState
import com.example.model.OnlineTrackItem
import com.example.model.YouTubeMusicItem

@Composable
fun OnlineMusicView(
    theme: CarAudioThemeType,
    onlineState: OnlineSearchState,
    downloads: List<DownloadItem>,
    onOnlineQueryChange: (String) -> Unit,
    onSearchOnline: (String) -> Unit,
    onLoadFeatured: () -> Unit,
    onPlayOnlineTrack: (OnlineTrackItem) -> Unit,
    onDownloadTrack: (OnlineTrackItem) -> Unit,
    onOpenDownloads: () -> Unit,
    // YouTube Music Official Tab
    ytSearchQuery: String,
    ytSavedItems: List<YouTubeMusicItem>,
    ytSearchHistory: List<String>,
    onYtSearchQueryChange: (String) -> Unit,
    onYtRecordSearch: (String) -> Unit,
    onYtSaveShortcut: (title: String, artist: String, customUrl: String?, favorite: Boolean) -> Unit,
    onYtToggleFavorite: (String) -> Unit,
    onYtRemoveItem: (String) -> Unit,
    onYtClearHistory: () -> Unit,
    buildYtSearchUrl: (String) -> String,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("online_music_view")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = theme.surfaceColor.copy(alpha = 0.85f),
            contentColor = theme.primaryColor
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Jamendo (Libre)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("YouTube Music", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 1) {
            YouTubeMusicView(
                theme = theme,
                searchQuery = ytSearchQuery,
                savedItems = ytSavedItems,
                searchHistory = ytSearchHistory,
                onSearchQueryChange = onYtSearchQueryChange,
                onRecordSearch = onYtRecordSearch,
                onSaveShortcut = onYtSaveShortcut,
                onToggleFavorite = onYtToggleFavorite,
                onRemoveItem = onYtRemoveItem,
                onClearHistory = onYtClearHistory,
                buildSearchUrl = buildYtSearchUrl,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "online_search_header") {
                    GlassStudioCard(theme = theme, highlightBorder = true) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Catálogo Legal Creative Commons (Jamendo v3)",
                                color = theme.textColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = onlineState.providerStatusMessage,
                                color = theme.textSecondaryColor,
                                fontSize = 11.sp
                            )

                            OutlinedTextField(
                                value = onlineState.query,
                                onValueChange = onOnlineQueryChange,
                                placeholder = { Text("Buscar canción, artista o género...", fontSize = 12.5.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { onSearchOnline(onlineState.query) }),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.primaryColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primaryColor,
                                    unfocusedBorderColor = theme.cardColor,
                                    focusedTextColor = theme.textColor,
                                    unfocusedTextColor = theme.textColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("online_search_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowActionButton(
                                    text = "Buscar",
                                    theme = theme,
                                    onClick = { onSearchOnline(onlineState.query) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("online_search_button")
                                )
                                OutlinedButton(
                                    onClick = onLoadFeatured,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, theme.cardColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Populares", color = theme.textColor, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onOpenDownloads,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.DownloadDone, contentDescription = "Descargas", tint = theme.accentColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                if (!onlineState.isConfigured) {
                    item(key = "online_not_configured") {
                        GlassStudioCard(
                            theme = theme,
                            highlightBorder = true
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = theme.accentColor)
                                    Text("Clave de API Requerida (JAMENDO_CLIENT_ID)", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Para respetar políticas y licencias reales sin inventar catálogos falsos, agrega tu Client ID gratuito de Jamendo Developer en el panel Secrets de AI Studio bajo el nombre JAMENDO_CLIENT_ID.",
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                if (onlineState.isLoading) {
                    item(key = "online_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = theme.primaryColor)
                        }
                    }
                } else if (!onlineState.errorMessage.isNullOrBlank()) {
                    item(key = "online_error") {
                        EmptyLibraryNotice(
                            theme = theme,
                            title = "Aviso del proveedor online",
                            subtitle = onlineState.errorMessage
                        )
                    }
                }

                if (onlineState.artists.isNotEmpty()) {
                    item(key = "online_artists") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Artistas", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                onlineState.artists.forEach { artist ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = theme.surfaceColor,
                                        border = BorderStroke(1.dp, theme.cardColor),
                                        modifier = Modifier.clickable { onSearchOnline(artist.name) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                                            Text(artist.name, color = theme.textColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (onlineState.albums.isNotEmpty()) {
                    item(key = "online_albums") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Álbumes", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                onlineState.albums.forEach { album ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = theme.surfaceColor,
                                        border = BorderStroke(1.dp, theme.cardColor),
                                        modifier = Modifier.clickable { onSearchOnline(album.title) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Album, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                                            Text(album.title, color = theme.textColor, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (onlineState.tracks.isNotEmpty()) {
                    item(key = "online_tracks_label") {
                        Text("Canciones (${onlineState.tracks.size})", color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                    items(onlineState.tracks, key = { it.id }) { track ->
                        val downloadState = downloads.firstOrNull { it.trackId == track.id }
                        OnlineTrackCard(
                            track = track,
                            downloadItem = downloadState,
                            theme = theme,
                            onPlay = { onPlayOnlineTrack(track) },
                            onDownload = { onDownloadTrack(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlineTrackCard(
    track: OnlineTrackItem,
    downloadItem: DownloadItem?,
    theme: CarAudioThemeType,
    onPlay: () -> Unit,
    onDownload: () -> Unit
) {
    GlassStudioCard(
        theme = theme,
        modifier = Modifier.clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.cardColor),
                contentAlignment = Alignment.Center
            ) {
                if (!track.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = theme.primaryColor)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${track.artist} • ${formatDurationSeconds(track.durationSeconds)}", color = theme.textSecondaryColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (track.downloadAllowed) "Licencia libre • Descarga autorizada" else "Solo streaming (sin descarga)",
                    color = if (track.downloadAllowed) theme.primaryColor else theme.textSecondaryColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = theme.primaryColor)
            }

            if (track.downloadAllowed) {
                val done = downloadItem?.status == DownloadStatus.COMPLETED
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = if (done) Icons.Default.DownloadDone else Icons.Default.Download,
                        contentDescription = if (done) "Descargada" else "Descargar",
                        tint = if (done) theme.accentColor else theme.textColor
                    )
                }
            }
        }
    }
}
