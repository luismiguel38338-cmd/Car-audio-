package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbSlot
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.OutputDeviceStatus
import com.example.model.RepeatMode
import kotlin.math.roundToInt

@Composable
fun PlayerView(
    theme: CarAudioThemeType,
    queueTracks: List<AudioTrackItem>,
    currentTrack: AudioTrackItem?,
    isPlaying: Boolean,
    playbackSeconds: Int,
    volumeLevel: Float,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float = 1.0f,
    playbackPitch: Float = 1.0f,
    sleepTimerRemainingSec: Int? = null,
    sleepTimerStopAfterTrack: Boolean = false,
    trackPlayCounts: Map<String, Int> = emptyMap(),
    outputDeviceStatus: OutputDeviceStatus,
    isAnyAudioPlaying: Boolean,
    bassEnergy: Float,
    midEnergy: Float,
    trebleEnergy: Float,
    liteVisuals: Boolean,
    activeAbSlot: AbSlot?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (AudioTrackItem) -> Unit,
    onToggleAbComparison: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit = {},
    onSetPlaybackPitch: (Float) -> Unit = {},
    onResetPlaybackSpeedPitch: () -> Unit = {},
    onStartSleepTimer: (Int) -> Unit = {},
    onToggleSleepAfterTrack: () -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onPlayQueueAt: (Int) -> Unit,
    onMoveQueueUp: (Int) -> Unit,
    onMoveQueueDown: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onOpenMyMusic: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSleepDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showVolumeControl by remember { mutableStateOf(false) }

    val maxSec = (currentTrack?.durationSeconds ?: 180).coerceAtLeast(1)
    var isSeeking by remember { mutableStateOf(false) }
    var seekSliderValue by remember(currentTrack?.stableKey) {
        mutableFloatStateOf(playbackSeconds.coerceIn(0, maxSec).toFloat())
    }
    LaunchedEffect(currentTrack?.stableKey) {
        isSeeking = false
    }
    val displayedPositionSec = if (isSeeking) seekSliderValue.roundToInt() else playbackSeconds.coerceIn(0, maxSec)

    if (showSleepDialog) {
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            containerColor = theme.cardColor,
            title = {
                Text("Temporizador de reposo", color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when {
                            sleepTimerRemainingSec != null -> "Deteniendo reproducción con fade suave en ${formatMinutesSecondsClock(sleepTimerRemainingSec)}."
                            sleepTimerStopAfterTrack -> "Se pausará al terminar la canción actual."
                            else -> "Pausa la música automáticamente después del tiempo elegido."
                        },
                        color = theme.textSecondaryColor,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 15, 30).forEach { mins ->
                            OutlinedButton(
                                onClick = {
                                    onStartSleepTimer(mins)
                                    showSleepDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$mins min", color = theme.textColor, fontSize = 11.5.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(45, 60).forEach { mins ->
                            OutlinedButton(
                                onClick = {
                                    onStartSleepTimer(mins)
                                    showSleepDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$mins min", color = theme.textColor, fontSize = 11.5.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onToggleSleepAfterTrack()
                                showSleepDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (sleepTimerStopAfterTrack) theme.accentColor else theme.cardColor
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(
                                text = "Fin de pista",
                                color = if (sleepTimerStopAfterTrack) theme.accentColor else theme.textColor,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (sleepTimerRemainingSec != null || sleepTimerStopAfterTrack) {
                    TextButton(onClick = {
                        onCancelSleepTimer()
                        showSleepDialog = false
                    }) {
                        Text("Desactivar", color = theme.errorColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepDialog = false }) {
                    Text("Cerrar", color = theme.accentColor)
                }
            }
        )
    }

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = theme.cardColor,
            title = {
                Text("Velocidad y tono", color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.75f, 0.9f, 1.0f, 1.15f, 1.25f, 1.5f).forEach { spd ->
                            val selected = kotlin.math.abs(playbackSpeed - spd) < 0.02f
                            FilterChip(
                                selected = selected,
                                onClick = { onSetPlaybackSpeed(spd) },
                                label = { Text("%.2fx".format(spd).replace(".00x", ".0x"), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }
                    }
                    DspSliderRow(
                        label = "Velocidad de reproducción",
                        value = playbackSpeed,
                        range = 0.5f..2.0f,
                        unit = "x",
                        enabled = true,
                        theme = theme,
                        decimals = 2,
                        onValueChange = onSetPlaybackSpeed
                    )
                    DspSliderRow(
                        label = "Tono (Pitch)",
                        value = playbackPitch,
                        range = 0.5f..2.0f,
                        unit = "x",
                        enabled = true,
                        theme = theme,
                        decimals = 2,
                        onValueChange = onSetPlaybackPitch
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSpeedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Listo")
                }
            },
            dismissButton = {
                TextButton(onClick = onResetPlaybackSpeedPitch) {
                    Text("Normal (1.0x)", color = theme.accentColor)
                }
            }
        )
    }

    if (showDetailsDialog && currentTrack != null) {
        val count = trackPlayCounts[currentTrack.stableKey] ?: trackPlayCounts[currentTrack.id] ?: 0
        TrackDetailsDialog(
            track = currentTrack,
            playCount = count,
            theme = theme,
            onDismiss = { showDetailsDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .testTag("music_player_view"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Reproductor Principal
        item(key = "player_head_unit_card") {
            GlassStudioCard(
                theme = theme,
                highlightBorder = isPlaying
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Salida, temporizador y acciones de pista
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.backgroundColor.copy(alpha = 0.85f))
                                .border(0.8.dp, theme.cardColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(14.dp))
                            Text(
                                text = outputDeviceStatus.label,
                                color = theme.silverDetailColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (sleepTimerRemainingSec != null || sleepTimerStopAfterTrack) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = theme.primaryColor.copy(alpha = 0.22f),
                                    border = BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { showSleepDialog = true }
                                        .padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = if (sleepTimerRemainingSec != null) formatMinutesSecondsClock(sleepTimerRemainingSec) else "Fin pista",
                                        color = theme.accentColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (currentTrack != null) {
                                IconButton(
                                    onClick = { showDetailsDialog = true },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Detalles de pista", tint = theme.textSecondaryColor, modifier = Modifier.size(19.dp))
                                }
                                IconButton(
                                    onClick = { onToggleFavorite(currentTrack) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentTrack.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Marcar favorita",
                                        tint = if (currentTrack.isFavorite) theme.accentColor else theme.textSecondaryColor
                                    )
                                }
                            }
                        }
                    }

                    // Carátula grande con aura violeta sutil
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .then(
                                if (!liteVisuals) {
                                    Modifier.drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    theme.primaryColor.copy(alpha = if (isPlaying) 0.34f else 0.14f),
                                                    theme.accentColor.copy(alpha = if (isPlaying) 0.12f else 0.03f),
                                                    Color.Transparent
                                                )
                                            ),
                                            radius = size.maxDimension * 0.72f
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(21.dp))
                                .border(
                                    width = 1.3.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            theme.silverDetailColor.copy(alpha = 0.42f),
                                            theme.primaryColor.copy(alpha = 0.80f),
                                            theme.accentColor.copy(alpha = 0.60f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(21.dp)
                                )
                        ) {
                            TrackArtwork(
                                track = currentTrack,
                                theme = theme,
                                size = 240.dp,
                                cornerRadius = 20.dp,
                                modifier = if (liteVisuals) Modifier else Modifier.shadow(12.dp, RoundedCornerShape(20.dp))
                            )
                        }
                    }

                    // Título, artista y formato
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentTrack?.title ?: "Sin canción seleccionada",
                            color = theme.textColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack?.let { "${it.artist} • ${it.album}" }
                                ?: "Abre Mi música para elegir una canción del teléfono",
                            color = theme.textSecondaryColor,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val metaBadge = buildString {
                            append(currentTrack?.audioFormatLabel ?: "Audio local")
                            if (currentTrack != null && currentTrack.bitrateKbps > 0) {
                                append(" • ${currentTrack.bitrateKbps} kbps")
                            }
                            if (kotlin.math.abs(playbackSpeed - 1.0f) > 0.02f) {
                                append(" • %.2fx".format(playbackSpeed))
                            }
                        }
                        Text(
                            text = metaBadge,
                            color = theme.accentColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Barra de progreso
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = if (isSeeking) seekSliderValue else playbackSeconds.coerceIn(0, maxSec).toFloat(),
                            onValueChange = {
                                isSeeking = true
                                seekSliderValue = it
                            },
                            onValueChangeFinished = {
                                val target = seekSliderValue.roundToInt()
                                isSeeking = false
                                onSeek(target)
                            },
                            valueRange = 0f..maxSec.toFloat(),
                            enabled = currentTrack != null,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.silverDetailColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.cardColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .testTag("player_seek_slider")
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = formatDurationSeconds(displayedPositionSec),
                                color = theme.textSecondaryColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = formatDurationSeconds(currentTrack?.durationSeconds ?: 0),
                                color = theme.textSecondaryColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Controles de transporte
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Aleatorio",
                                tint = if (shuffleEnabled) theme.accentColor else theme.textSecondaryColor
                            )
                        }
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_prev_button")
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = theme.silverDetailColor, modifier = Modifier.size(32.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            theme.primaryColor,
                                            theme.accentColor
                                        )
                                    )
                                )
                                .border(
                                    1.4.dp,
                                    theme.silverDetailColor.copy(alpha = 0.55f),
                                    CircleShape
                                )
                                .clickable { onPlayPause() }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = theme.onPrimaryColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        IconButton(
                            onClick = onNext,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = theme.silverDetailColor, modifier = Modifier.size(32.dp))
                        }
                        IconButton(
                            onClick = onCycleRepeat,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = repeatMode.label,
                                tint = if (repeatMode != RepeatMode.OFF) theme.accentColor else theme.textSecondaryColor
                            )
                        }
                    }

                    // Acciones limpias de Reproductor (EQ, Volumen, Temporizador, Velocidad, A/B, Biblioteca)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenEqualizer,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.55f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Ecualizador", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = { showVolumeControl = !showVolumeControl },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (showVolumeControl) theme.accentColor else theme.cardColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("${(volumeLevel * 100).toInt()}%", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = { showSleepDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (sleepTimerRemainingSec != null || sleepTimerStopAfterTrack) theme.accentColor else theme.cardColor
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("player_sleep_timer_button")
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Temporizador", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = { showSpeedDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (kotlin.math.abs(playbackSpeed - 1f) > 0.02f || kotlin.math.abs(playbackPitch - 1f) > 0.02f) theme.accentColor else theme.cardColor
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("player_speed_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("%.2gx".format(playbackSpeed), color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = onToggleAbComparison,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (activeAbSlot != null) theme.accentColor else theme.cardColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("A/B (${activeAbSlot?.badge ?: "-"})", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onOpenMyMusic,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Biblioteca", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Control de volumen desplegable
                    AnimatedVisibility(
                        visible = showVolumeControl,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volumen", tint = theme.accentColor, modifier = Modifier.size(18.dp))
                            Slider(
                                value = volumeLevel,
                                onValueChange = onVolumeChange,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.silverDetailColor,
                                    activeTrackColor = theme.primaryColor,
                                    inactiveTrackColor = theme.cardColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                            )
                            Text("${(volumeLevel * 100).toInt()}%", color = theme.silverDetailColor, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 2. Cola de reproducción inmediata
        item(key = "player_queue_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cola de reproducción (${queueTracks.size})",
                    color = theme.silverDetailColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (queueTracks.isNotEmpty()) {
                    TextButton(onClick = onClearQueue) {
                        Text("Vaciar cola", color = theme.accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        if (queueTracks.isEmpty()) {
            item(key = "player_empty_queue") {
                EmptyLibraryNotice(
                    theme = theme,
                    title = "Cola vacía",
                    subtitle = "Selecciona canciones desde Mi música para armar tu lista de reproducción."
                )
            }
        } else {
            itemsIndexed(queueTracks, key = { index, item -> "${index}_${item.stableKey}" }) { index, qTrack ->
                val isActive = currentTrack?.stableKey == qTrack.stableKey
                var queueMenuExpanded by remember { mutableStateOf(false) }
                GlassStudioCard(
                    theme = theme,
                    highlightBorder = isActive,
                    modifier = Modifier
                        .clickable { onPlayQueueAt(index) }
                        .testTag("queue_item_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrackArtwork(track = qTrack, theme = theme, size = 40.dp, cornerRadius = 8.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = qTrack.title,
                                color = if (isActive) theme.accentColor else theme.textColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${qTrack.artist} • ${formatDurationSeconds(qTrack.durationSeconds)}",
                                color = theme.textSecondaryColor,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { queueMenuExpanded = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones en cola",
                                    tint = theme.textSecondaryColor
                                )
                            }
                            DropdownMenu(
                                expanded = queueMenuExpanded,
                                onDismissRequest = { queueMenuExpanded = false },
                                containerColor = theme.cardColor
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Subir en cola", color = if (index > 0) theme.textColor else theme.textSecondaryColor) },
                                    leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = theme.accentColor) },
                                    enabled = index > 0,
                                    onClick = {
                                        queueMenuExpanded = false
                                        onMoveQueueUp(index)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bajar en cola", color = if (index < queueTracks.lastIndex) theme.textColor else theme.textSecondaryColor) },
                                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = theme.accentColor) },
                                    enabled = index < queueTracks.lastIndex,
                                    onClick = {
                                        queueMenuExpanded = false
                                        onMoveQueueDown(index)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Quitar de la cola", color = theme.errorColor) },
                                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = theme.errorColor) },
                                    onClick = {
                                        queueMenuExpanded = false
                                        onRemoveFromQueue(index)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
