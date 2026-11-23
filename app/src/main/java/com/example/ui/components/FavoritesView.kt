package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.MusicPlaylist
import com.example.model.YouTubeMusicItem

@Composable
fun FavoritesView(
    theme: CarAudioThemeType,
    favoriteTracks: List<AudioTrackItem>,
    currentTrack: AudioTrackItem?,
    playlists: List<MusicPlaylist>,
    ytFavoriteItems: List<YouTubeMusicItem>,
    onPlayTrack: (AudioTrackItem) -> Unit,
    onToggleFavoriteTrack: (AudioTrackItem) -> Unit,
    onAddToQueue: (AudioTrackItem) -> Unit,
    onPlayNextInQueue: (AudioTrackItem) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onOpenPlaylistInMyMusic: (String) -> Unit,
    onAddTrackToPlaylist: (String, String) -> Unit,
    onToggleYtFavorite: (String) -> Unit,
    onOpenYouTubeSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var trackForPlaylistDialog by remember { mutableStateOf<AudioTrackItem?>(null) }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Nueva Lista de Cabina", color = theme.textColor, fontWeight = FontWeight.Black) },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    )
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
            title = { Text("Agregar a Lista", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                if (playlists.isEmpty()) {
                    Text("No tienes listas creadas todavía.", color = theme.textSecondaryColor, fontSize = 12.5.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        playlists.forEach { pl ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddTrackToPlaylist(pl.id, targetTrack.id)
                                        trackForPlaylistDialog = null
                                    },
                                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                                border = BorderStroke(1.dp, theme.cardColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pl.name, color = theme.textColor, fontWeight = FontWeight.SemiBold)
                                    Text("${pl.trackIds.size} pistas", color = theme.accentColor, fontSize = 11.sp)
                                }
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
                    Text("+ Crear Lista", color = theme.accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackForPlaylistDialog = null }) {
                    Text("Cerrar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp)
            .testTag("favorites_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "favorites_summary") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = true
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = theme.accentColor)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favoritos y colección",
                                color = theme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tus pistas preferidas, listas de reproducción y accesos rápidos",
                                color = theme.textSecondaryColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FavoriteMetricBadge("${favoriteTracks.size}", "Pistas Fav", theme, Modifier.weight(1f))
                        FavoriteMetricBadge("${playlists.size}", "Listas", theme, Modifier.weight(1f))
                        FavoriteMetricBadge("${ytFavoriteItems.size}", "YT Music", theme, Modifier.weight(1f))
                    }
                }
            }
        }

        // Section 1: Favorite Local Tracks
        item(key = "fav_tracks_header") {
            Text(
                text = "Canciones favoritas (${favoriteTracks.size})",
                color = theme.textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (favoriteTracks.isEmpty()) {
            item(key = "fav_tracks_empty") {
                EmptyLibraryNotice(
                    theme = theme,
                    title = "Sin canciones favoritas",
                    subtitle = "Toca el ícono de corazón en Mi música o en el Reproductor para fijar tus pistas de referencia aquí."
                )
            }
        } else {
            items(favoriteTracks, key = { "fav_tr_${it.id}" }) { track ->
                TrackRowCard(
                    track = track,
                    isActive = currentTrack?.id == track.id,
                    theme = theme,
                    onSelect = { onPlayTrack(track) },
                    onToggleFavorite = { onToggleFavoriteTrack(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onPlayNext = { onPlayNextInQueue(track) },
                    onAddToPlaylist = { trackForPlaylistDialog = track },
                    onRemove = { onToggleFavoriteTrack(track) }
                )
            }
        }

        // Section 2: Playlists
        item(key = "fav_playlists_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listas de reproducción (${playlists.size})",
                    color = theme.textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = { showCreatePlaylistDialog = true },
                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("favorites_create_playlist_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Lista", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (playlists.isEmpty()) {
            item(key = "fav_playlists_empty") {
                EmptyLibraryNotice(
                    theme = theme,
                    title = "Aún no tienes listas guardadas",
                    subtitle = "Crea una lista para organizar tus pistas de graves, rock o sesiones de calibración."
                )
            }
        } else {
            items(playlists, key = { "fav_pl_${it.id}" }) { playlist ->
                GlassStudioCard(
                    theme = theme,
                    modifier = Modifier.clickable { onOpenPlaylistInMyMusic(playlist.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = theme.accentColor)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.name, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.trackIds.size} canciones asignadas • Tocar para abrir", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = theme.errorColor)
                        }
                    }
                }
            }
        }

        // Section 3: Favorite YouTube Music Shortcuts
        item(key = "fav_yt_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accesos de YouTube Music (${ytFavoriteItems.size})",
                    color = theme.textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onOpenYouTubeSection) {
                    Text("Explorar", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (ytFavoriteItems.isEmpty()) {
            item(key = "fav_yt_empty") {
                EmptyLibraryNotice(
                    theme = theme,
                    title = "Sin accesos favoritos de YouTube Music",
                    subtitle = "Marca con corazón cualquier acceso guardado en la sección YouTube Music para abrirlo directo desde aquí."
                )
            }
        } else {
            items(ytFavoriteItems, key = { "fav_yt_${it.id}" }) { ytItem ->
                GlassStudioCard(
                    theme = theme,
                    highlightBorder = true,
                    modifier = Modifier.clickable { openInYouTubeMusic(context, ytItem.resolvedUrl) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = theme.accentColor)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ytItem.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = listOf(ytItem.artist, ytItem.category).filter { it.isNotBlank() }.joinToString(" • "),
                                color = theme.textSecondaryColor,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onToggleYtFavorite(ytItem.id) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Favorite, contentDescription = "Quitar de favoritos", tint = theme.accentColor)
                        }
                        IconButton(onClick = { openInYouTubeMusic(context, ytItem.resolvedUrl) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir en YouTube Music", tint = theme.primaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteMetricBadge(
    value: String,
    label: String,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.backgroundColor)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = theme.accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = theme.textSecondaryColor,
            fontSize = 10.sp
        )
    }
}
