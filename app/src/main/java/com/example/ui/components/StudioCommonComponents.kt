package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.AlbumArtCache
import com.example.model.AbSlot
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspAbSnapshot

@Composable
fun TrackArtwork(
    track: AudioTrackItem?,
    theme: CarAudioThemeType,
    size: Dp = 44.dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var embeddedBitmap by remember(track?.stableKey) {
        mutableStateOf<Bitmap?>(AlbumArtCache.getCached(track?.customUri))
    }

    LaunchedEffect(track?.stableKey, track?.customUri) {
        val uri = track?.customUri
        if (uri != null && track.albumArtUri == null && embeddedBitmap == null) {
            embeddedBitmap = AlbumArtCache.loadOrGet(context, uri)
        } else if (uri != null && embeddedBitmap == null) {
            // Also populate cache silently in case MediaStore albumArtUri fails
            embeddedBitmap = AlbumArtCache.loadOrGet(context, uri)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        theme.cardColor,
                        theme.surfaceColor
                    )
                )
            )
            .border(0.75.dp, theme.cardColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        when {
            track?.albumArtUri != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            embeddedBitmap != null -> {
                Image(
                    bitmap = embeddedBitmap!!.asImageBitmap(),
                    contentDescription = track?.title ?: "Carátula",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = track?.title ?: "Audio",
                    tint = theme.accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size((size.value * 0.45f).dp)
                )
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    track: AudioTrackItem,
    isPlaying: Boolean,
    playbackSeconds: Int,
    theme: CarAudioThemeType,
    onOpenPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (track.durationSeconds > 0) {
        (playbackSeconds.toFloat() / track.durationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = theme.surfaceColor.copy(alpha = 0.88f),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenPlayer() }
            .testTag("mini_player_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                theme.primaryColor.copy(alpha = if (isPlaying) 0.70f else 0.30f),
                                theme.silverDetailColor.copy(alpha = 0.28f),
                                theme.accentColor.copy(alpha = if (isPlaying) 0.70f else 0.30f)
                            )
                        )
                    )
            )
            LinearProgressIndicator(
                progress = { progress },
                color = theme.primaryColor,
                trackColor = theme.cardColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrackArtwork(
                    track = track,
                    theme = theme,
                    size = 42.dp,
                    cornerRadius = 10.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = theme.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • ${formatDurationSeconds(playbackSeconds)} / ${formatDurationSeconds(track.durationSeconds)}",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    theme.primaryColor.copy(alpha = 0.30f),
                                    theme.accentColor.copy(alpha = 0.18f)
                                )
                            )
                        )
                        .border(1.dp, theme.accentColor.copy(alpha = 0.55f), CircleShape)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = theme.silverDetailColor
                    )
                }
                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente canción",
                        tint = theme.textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TrackDetailsDialog(
    track: AudioTrackItem,
    playCount: Int,
    theme: CarAudioThemeType,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cardColor,
        title = {
            Text(
                text = "Detalles de pista",
                color = theme.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    TrackArtwork(track = track, theme = theme, size = 48.dp, cornerRadius = 10.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(track.artist, color = theme.accentColor, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TrackDetailRow("Álbum", track.album, theme)
                TrackDetailRow("Carpeta", track.folderName, theme)
                TrackDetailRow("Formato", track.audioFormatLabel, theme)
                TrackDetailRow("Duración", formatDurationSeconds(track.durationSeconds), theme)
                if (track.bitrateKbps > 0) {
                    TrackDetailRow("Bitrate", "${track.bitrateKbps} kbps", theme)
                }
                TrackDetailRow("Reproducciones", "$playCount", theme)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = theme.accentColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TrackDetailRow(label: String, value: String, theme: CarAudioThemeType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = theme.textSecondaryColor, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            color = theme.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DspSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    enabled: Boolean,
    theme: CarAudioThemeType,
    decimals: Int = 1,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (enabled) theme.textColor else theme.textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = theme.backgroundColor
            ) {
                val formatted = if (decimals == 0) {
                    "${value.toInt()} $unit"
                } else {
                    "%.${decimals}f %s".format(value, unit)
                }
                Text(
                    text = formatted.trim(),
                    color = if (enabled) theme.accentColor else theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = theme.primaryColor,
                activeTrackColor = theme.accentColor,
                inactiveTrackColor = theme.cardColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        )
    }
}

fun formatDurationSeconds(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    val mins = s / 60
    val rem = s % 60
    return "%d:%02d".format(mins, rem)
}

fun formatMinutesSecondsClock(totalSec: Int): String {
    val s = totalSec.coerceAtLeast(0)
    val hrs = s / 3600
    val mins = (s % 3600) / 60
    val rem = s % 60
    return if (hrs > 0) "%d:%02d:%02d".format(hrs, mins, rem) else "%d:%02d".format(mins, rem)
}

/**
 * Opens a search or URL cleanly in the official YouTube Music app when installed,
 * falling back to standard browser URL without broad package queries.
 */
fun openInYouTubeMusic(context: Context, url: String) {
    val uri = Uri.parse(url)
    try {
        val ytMusicIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(ytMusicIntent)
    } catch (_: ActivityNotFoundException) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Throwable) {}
    } catch (_: Throwable) {}
}

@Composable
fun AbQuickComparisonBar(
    theme: CarAudioThemeType,
    slotA: DspAbSnapshot?,
    slotB: DspAbSnapshot?,
    activeSlot: AbSlot?,
    onCaptureSlot: (AbSlot) -> Unit,
    onApplySlot: (AbSlot) -> Unit,
    onToggleAb: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        highlightBorder = activeSlot != null,
        modifier = modifier.testTag("ab_comparison_bar")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Comparación A/B",
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Comparador A / B",
                        color = theme.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = when (activeSlot) {
                        AbSlot.A -> "Activo: A"
                        AbSlot.B -> "Activo: B"
                        null -> "Sin captura"
                    },
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isA = activeSlot == AbSlot.A
                val isB = activeSlot == AbSlot.B

                OutlinedButton(
                    onClick = { if (slotA != null) onApplySlot(AbSlot.A) else onCaptureSlot(AbSlot.A) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isA) theme.accentColor else theme.cardColor),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ab_apply_a_button")
                ) {
                    Text(
                        text = if (slotA != null) "Usar A" else "Guardar A",
                        color = if (isA) theme.accentColor else theme.textColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { if (slotB != null) onApplySlot(AbSlot.B) else onCaptureSlot(AbSlot.B) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isB) theme.accentColor else theme.cardColor),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ab_apply_b_button")
                ) {
                    Text(
                        text = if (slotB != null) "Usar B" else "Guardar B",
                        color = if (isB) theme.accentColor else theme.textColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onToggleAb,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ab_swap_button")
                ) {
                    Text(
                        text = "A ↔ B",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Guardar actual en:",
                    color = theme.textSecondaryColor,
                    fontSize = 10.5.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.backgroundColor)
                        .border(1.dp, theme.cardColor, RoundedCornerShape(8.dp))
                        .clickable { onCaptureSlot(AbSlot.A) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("ab_capture_a_chip")
                ) {
                    Text("Fijar A", color = theme.accentColor, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.backgroundColor)
                        .border(1.dp, theme.cardColor, RoundedCornerShape(8.dp))
                        .clickable { onCaptureSlot(AbSlot.B) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("ab_capture_b_chip")
                ) {
                    Text("Fijar B", color = theme.accentColor, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TrackRowCard(
    track: AudioTrackItem,
    isActive: Boolean,
    theme: CarAudioThemeType,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onShowDetails: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val trackBorder = if (isActive) {
        Brush.linearGradient(
            listOf(
                theme.silverDetailColor.copy(alpha = 0.50f),
                theme.primaryColor.copy(alpha = 0.85f),
                theme.accentColor.copy(alpha = 0.75f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                theme.silverDetailColor.copy(alpha = 0.14f),
                theme.primaryColor.copy(alpha = 0.28f)
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, trackBorder, RoundedCornerShape(15.dp))
            .clickable { onSelect() }
            .testTag("track_row_${track.id}"),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) theme.cardColor.copy(alpha = 0.78f) else theme.surfaceColor.copy(alpha = 0.72f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackArtwork(
                track = track,
                theme = theme,
                size = 44.dp,
                cornerRadius = 10.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
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
                    text = "${track.artist} • ${formatDurationSeconds(track.durationSeconds)} • ${track.audioFormatLabel}",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (track.isFavorite) theme.accentColor else theme.textSecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(48.dp)) {
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
                        text = { Text("Añadir a la cola", color = theme.textColor) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = theme.accentColor) },
                        onClick = {
                            menuExpanded = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar a una lista", color = theme.textColor) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = theme.accentColor) },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylist()
                        }
                    )
                    if (onShowDetails != null) {
                        DropdownMenuItem(
                            text = { Text("Detalles de pista", color = theme.textColor) },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = theme.accentColor) },
                            onClick = {
                                menuExpanded = false
                                onShowDetails()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Quitar de la biblioteca", color = theme.errorColor) },
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
fun EmptyLibraryNotice(
    theme: CarAudioThemeType,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    GlassStudioCard(
        theme = theme,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = theme.textColor, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = theme.textSecondaryColor, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun GlassStudioCard(
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier,
    highlightBorder: Boolean = false,
    content: @Composable () -> Unit
) {
    val borderBrush = Brush.linearGradient(
        listOf(
            theme.silverDetailColor.copy(alpha = if (highlightBorder) 0.42f else 0.16f),
            theme.primaryColor.copy(alpha = if (highlightBorder) 0.78f else 0.34f),
            theme.accentColor.copy(alpha = if (highlightBorder) 0.68f else 0.24f)
        )
    )
    val glassBg = if (theme.isLightTheme) {
        Brush.verticalGradient(
            listOf(
                theme.surfaceColor.copy(alpha = 0.96f),
                theme.backgroundColor.copy(alpha = 0.92f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                theme.cardColor.copy(alpha = 0.68f),
                theme.surfaceColor.copy(alpha = 0.84f),
                theme.backgroundColor.copy(alpha = 0.90f)
            )
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderBrush, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassBg)
        ) {
            content()
        }
    }
}

@Composable
fun BrandFooterBadge(
    theme: CarAudioThemeType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = theme.surfaceColor.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, theme.cardColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CAR AUDIO DSP PRO • POTENCIA TU MÚSICA",
                    color = theme.primaryColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.7.sp
                )
                Text(
                    text = "LM Produce — Producción de Luis Miguel",
                    color = theme.textSecondaryColor,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "v6.5",
                color = theme.accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

fun formatStorageBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.2f GB".format(gb)
        mb >= 1.0 -> "%.1f MB".format(mb)
        else -> "%.0f KB".format(kb.coerceAtLeast(1.0))
    }
}

