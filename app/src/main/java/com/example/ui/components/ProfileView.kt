package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.LocalProfileStats

@Composable
fun ProfileView(
    theme: CarAudioThemeType,
    stats: LocalProfileStats,
    topTracks: List<AudioTrackItem>,
    favoriteTracks: List<AudioTrackItem>,
    playCounts: Map<String, Int>,
    onUpdateProfileName: (String) -> Unit,
    onPlayTrack: (AudioTrackItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditName by remember { mutableStateOf(false) }
    var draftName by remember(stats.displayName) { mutableStateOf(stats.displayName) }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            containerColor = theme.cardColor,
            title = { Text("Nombre de tu Cabina", color = theme.textColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Nombre local") },
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
                        onUpdateProfileName(draftName)
                        showEditName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("Cancelar", color = theme.textSecondaryColor)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp)
            .testTag("profile_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "profile_header_card") {
            GlassStudioCard(theme = theme, highlightBorder = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                        contentDescription = "Avatar Cabina",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, theme.primaryColor, CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stats.displayName, color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Modo local sin cuenta obligatoria", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("LM Produce — Producción de Luis Miguel", color = theme.textSecondaryColor, fontSize = 10.5.sp)
                    }
                    IconButton(onClick = { showEditName = true }, modifier = Modifier.testTag("profile_edit_name_button")) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar nombre", tint = theme.accentColor)
                    }
                }
            }
        }

        item(key = "profile_stats_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatCard(
                        title = "Reproducciones",
                        value = "${stats.totalPlays}",
                        icon = Icons.Default.Headphones,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "Tiempo de escucha",
                        value = formatMinutesSecondsClock(stats.totalListeningSeconds.toInt()),
                        icon = Icons.Default.Timer,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatCard(
                        title = "Favoritas",
                        value = "${stats.favoritesCount}",
                        icon = Icons.Default.Favorite,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "Descargas / Presets",
                        value = "${stats.downloadsCount} / ${stats.customPresetsCount}",
                        icon = Icons.Default.PlayArrow,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (topTracks.isNotEmpty()) {
            item(key = "profile_top_header") {
                Text("Canciones más escuchadas", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            items(topTracks.take(6), key = { "top_${it.stableKey}" }) { tr ->
                val plays = playCounts[tr.stableKey] ?: playCounts[tr.id] ?: 0
                GlassStudioCard(
                    theme = theme,
                    modifier = Modifier.clickable { onPlayTrack(tr) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackArtwork(track = tr, theme = theme, size = 40.dp, cornerRadius = 8.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(tr.artist, color = theme.textSecondaryColor, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("$plays rep.", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        if (favoriteTracks.isNotEmpty()) {
            item(key = "profile_favs_header") {
                Text("Tus favoritas recientes", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            items(favoriteTracks.take(4), key = { "pfav_${it.stableKey}" }) { tr ->
                GlassStudioCard(
                    theme = theme,
                    modifier = Modifier.clickable { onPlayTrack(tr) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackArtwork(track = tr, theme = theme, size = 38.dp, cornerRadius = 8.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr.title, color = theme.textColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(tr.artist, color = theme.textSecondaryColor, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                Text(title, color = theme.textSecondaryColor, fontSize = 10.5.sp)
            }
            Text(value, color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}
