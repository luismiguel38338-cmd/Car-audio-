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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.MusicPlaylist

@Composable
fun PlaylistsView(
    theme: CarAudioThemeType,
    playlists: List<MusicPlaylist>,
    allTracks: List<AudioTrackItem>,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPlayPlaylist: (String) -> Unit,
    onAddTrackToPlaylist: (String, AudioTrackItem) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var manageTracksPlaylist by remember { mutableStateOf<MusicPlaylist?>(null) }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Nueva lista de reproducción", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreatePlaylist(newName.trim())
                            newName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    manageTracksPlaylist?.let { playlist ->
        AlertDialog(
            onDismissRequest = { manageTracksPlaylist = null },
            containerColor = theme.cardColor,
            title = { Text("Agregar pistas a ${playlist.name}", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                if (allTracks.isEmpty()) {
                    Text("Primero importa o escanea canciones en tu Biblioteca.", color = theme.textSecondaryColor, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allTracks, key = { it.stableKey }) { track ->
                            val alreadyIn = track.id in playlist.trackIds || track.stableKey in playlist.trackIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.backgroundColor)
                                    .clickable {
                                        if (!alreadyIn) onAddTrackToPlaylist(playlist.id, track)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = alreadyIn,
                                    onCheckedChange = { if (!alreadyIn) onAddTrackToPlaylist(playlist.id, track) },
                                    colors = CheckboxDefaults.colors(checkedColor = theme.primaryColor)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(track.artist, color = theme.textSecondaryColor, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { manageTracksPlaylist = null }) {
                    Text("Listo", color = theme.primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp)
            .testTag("playlists_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "playlists_header") {
            GlassStudioCard(theme = theme, highlightBorder = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Listas de Reproducción", color = theme.textColor, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                        Text("${playlists.size} listas guardadas localmente", color = theme.textSecondaryColor, fontSize = 11.5.sp)
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                        modifier = Modifier.testTag("playlists_create_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (playlists.isEmpty()) {
            item(key = "playlists_empty") {
                GlassStudioCard(theme = theme) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(36.dp))
                        Text("Crea tu primera mezcla automotriz", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Agrupa tus canciones de Dembow, Reggaetón, Bachata o Bass Test por sesión.", color = theme.textSecondaryColor, fontSize = 11.5.sp)
                        OutlinedButton(onClick = onOpenLibrary) {
                            Text("Ir a Biblioteca", color = theme.primaryColor)
                        }
                    }
                }
            }
        } else {
            items(playlists, key = { it.id }) { pl ->
                GlassStudioCard(theme = theme, highlightBorder = pl.trackIds.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = theme.primaryColor)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.name, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${pl.trackIds.size} canciones", color = theme.textSecondaryColor, fontSize = 11.sp)
                            }
                            IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = theme.errorColor)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlowActionButton(
                                text = "Reproducir",
                                theme = theme,
                                onClick = { onPlayPlaylist(pl.id) },
                                enabled = pl.trackIds.isNotEmpty(),
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { manageTracksPlaylist = pl },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.cardColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Agregar", color = theme.textColor, fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
