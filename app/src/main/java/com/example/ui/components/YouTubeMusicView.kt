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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarAudioThemeType
import com.example.model.YouTubeMusicCatalog
import com.example.model.YouTubeMusicItem

@Composable
fun YouTubeMusicView(
    theme: CarAudioThemeType,
    searchQuery: String,
    savedItems: List<YouTubeMusicItem>,
    searchHistory: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onRecordSearch: (String) -> Unit,
    onSaveShortcut: (title: String, artist: String, customUrl: String?, favorite: Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    buildSearchUrl: (String) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogArtist by remember { mutableStateOf("") }
    var dialogUrl by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = theme.cardColor,
            title = { Text("Guardar Acceso de YouTube Music", color = theme.textColor, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text("Canción, mezcla o playlist") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialogArtist,
                        onValueChange = { dialogArtist = it },
                        label = { Text("Artista o canal (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialogUrl,
                        onValueChange = { dialogUrl = it },
                        label = { Text("URL music.youtube.com (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogTitle.isNotBlank()) {
                            onSaveShortcut(dialogTitle, dialogArtist, dialogUrl.takeIf { it.isNotBlank() }, true)
                            dialogTitle = ""
                            dialogArtist = ""
                            dialogUrl = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
            .testTag("youtube_music_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Official Launch & Policy Compliance Banner
        item(key = "yt_header_card") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = true
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = theme.accentColor)
                            Column {
                                Text("YouTube Music", color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Búsqueda rápida y accesos directos oficiales", color = theme.accentColor, fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Integración oficial segura: al pulsar reproducir o buscar, se abre directamente la app oficial de YouTube Music (o music.youtube.com) sin descargas ni extracción externa de audio.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp,
                            lineHeight = 14.5.sp
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Buscar canción, artista o mix en YouTube Music...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar en YouTube Music") },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("yt_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowActionButton(
                            text = if (searchQuery.isBlank()) "Abrir YouTube Music" else "Buscar en YouTube Music",
                            theme = theme,
                            onClick = {
                                val clean = searchQuery.trim()
                                if (clean.isNotEmpty()) onRecordSearch(clean)
                                openInYouTubeMusic(context, buildSearchUrl(clean))
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(17.dp))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("yt_open_search_button")
                        )

                        OutlinedButton(
                            onClick = {
                                val clean = searchQuery.trim()
                                if (clean.isNotEmpty()) {
                                    onSaveShortcut(clean, "Búsqueda en YouTube Music", null, false)
                                } else {
                                    showAddDialog = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            modifier = Modifier.testTag("yt_save_shortcut_button")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Guardar acceso", tint = theme.accentColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 2. Search History Chips
        if (searchHistory.isNotEmpty()) {
            item(key = "yt_search_history") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(16.dp))
                            Text("Búsquedas recientes", color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = onClearHistory) {
                            Text("Limpiar", color = theme.accentColor, fontSize = 10.5.sp)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        searchHistory.forEach { term ->
                            AssistChip(
                                onClick = {
                                    onSearchQueryChange(term)
                                    openInYouTubeMusic(context, buildSearchUrl(term))
                                },
                                label = { Text(term, fontSize = 11.sp, color = theme.textColor) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = theme.surfaceColor)
                            )
                        }
                    }
                }
            }
        }

        // 3. Saved Shortcuts & Favorites
        if (savedItems.isNotEmpty()) {
            item(key = "yt_saved_header") {
                Text(
                    text = "Mis accesos guardados (${savedItems.size})",
                    color = theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(savedItems, key = { it.id }) { item ->
                YouTubeShortcutCard(
                    item = item,
                    theme = theme,
                    onOpen = { openInYouTubeMusic(context, item.resolvedUrl) },
                    onToggleFavorite = { onToggleFavorite(item.id) },
                    onDelete = { onRemoveItem(item.id) }
                )
            }
        }

        // 4. Automotive Curated Quick Searches
        item(key = "yt_quick_catalog_header") {
            Text(
                text = "Sugerencias rápidas",
                color = theme.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        items(YouTubeMusicCatalog.quickLaunchSearches, key = { it.id }) { item ->
            YouTubeShortcutCard(
                item = item,
                theme = theme,
                onOpen = {
                    onRecordSearch(item.title)
                    openInYouTubeMusic(context, item.resolvedUrl)
                },
                onToggleFavorite = {
                    onSaveShortcut(item.title, item.artist, item.resolvedUrl, true)
                },
                onDelete = null
            )
        }
    }
}

@Composable
private fun YouTubeShortcutCard(
    item: YouTubeMusicItem,
    theme: CarAudioThemeType,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?
) {
    GlassStudioCard(
        theme = theme,
        highlightBorder = item.isFavorite,
        modifier = Modifier
            .clickable { onOpen() }
            .testTag("yt_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.backgroundColor)
                    .border(1.dp, theme.cardColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = item.title, tint = theme.accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = listOf(item.artist, item.category).filter { it.isNotBlank() }.joinToString(" • "),
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Guardar en Favoritos",
                    tint = if (item.isFavorite) theme.accentColor else theme.textSecondaryColor
                )
            }
            IconButton(onClick = onOpen, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir en YouTube Music", tint = theme.accentColor)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar acceso", tint = theme.textSecondaryColor)
                }
            }
        }
    }
}
