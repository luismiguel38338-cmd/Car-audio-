package com.example.ui.components

import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.service.FloatingEqualizerService
import kotlin.math.roundToInt

@Composable
fun PlayerView(
    theme: CarAudioThemeType,
    tracks: List<AudioTrackItem>,
    currentTrack: AudioTrackItem?,
    isPlaying: Boolean,
    playbackSeconds: Int,
    volumeLevel: Float,
    dspSettings: DspSettings,
    eqSettings: EqualizerSettings,
    hasAudioLibraryPermission: Boolean,
    isScanningLibrary: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Int) -> Unit,
    onTrackSelect: (AudioTrackItem) -> Unit,
    onRemoveTrack: (AudioTrackItem) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onCustomTracksLoaded: (List<Uri>) -> Unit,
    onRequestLibraryPermission: () -> Unit,
    onRefreshLibrary: () -> Unit,
    onOpenFloatingOverlayPermission: () -> Unit,
    onToggleDspMaster: (Boolean) -> Unit,
    onBassBoostChange: (Float) -> Unit,
    onPreampChange: (Float) -> Unit,
    onQuickBandChange: (Int, Float) -> Unit,
    onQuickPresetSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInlineModifier by remember { mutableStateOf(true) }

    val multiAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onCustomTracksLoaded(uris)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. MAIN PLAYER DECK
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_main_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.2.dp, theme.primaryColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceColor)
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (dspSettings.dspMasterEnabled) theme.primaryColor else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dspSettings.dspMasterEnabled) "DSP ACTIVO" else "BYPASS",
                                color = if (dspSettings.dspMasterEnabled) theme.accentColor else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Launch Floating YouTube Bubble
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryColor.copy(alpha = 0.18f))
                                .border(1.dp, theme.primaryColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (Settings.canDrawOverlays(context)) {
                                        FloatingEqualizerService.start(context)
                                    } else {
                                        onOpenFloatingOverlayPermission()
                                    }
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPicture,
                                contentDescription = "Pantalla Flotante YouTube",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "FLOTANTE YOUTUBE",
                                color = theme.textColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Crimson Studio Turntable Dial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(theme.backgroundColor)
                                .border(2.dp, theme.primaryColor.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val maxR = size.minDimension / 2f - 6.dp.toPx()
                                for (rStep in 1..4) {
                                    drawCircle(
                                        color = theme.primaryColor.copy(alpha = 0.22f),
                                        radius = maxR * (rStep / 4f),
                                        center = center,
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                }
                                drawCircle(
                                    color = theme.surfaceColor,
                                    radius = maxR * 0.36f,
                                    center = center
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio en vivo",
                                tint = if (isPlaying) theme.primaryColor else theme.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack?.title ?: "Selecciona o Carga Música",
                                color = theme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentTrack?.artist ?: "Tu música del celular se abre aquí",
                                color = theme.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentTrack?.album ?: "Listo para modificar en tiempo real",
                                color = theme.textSecondaryColor,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Seek Bar
                    val duration = currentTrack?.durationSeconds?.coerceAtLeast(1) ?: 100
                    Slider(
                        value = playbackSeconds.toFloat().coerceIn(0f, duration.toFloat()),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..duration.toFloat(),
                        enabled = currentTrack != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryColor,
                            activeTrackColor = theme.primaryColor,
                            inactiveTrackColor = theme.surfaceColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "%02d:%02d".format(playbackSeconds / 60, playbackSeconds % 60),
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "%02d:%02d".format(duration / 60, duration % 60),
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Transport Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = theme.textColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = theme.onPrimaryColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente",
                                tint = theme.textColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Volumen bajo",
                            tint = theme.textSecondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Slider(
                            value = volumeLevel,
                            onValueChange = onVolumeChange,
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("player_volume_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.surfaceColor
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volumen alto",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. MODIFICADOR EN VIVO (EN EL MISMO PANEL)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_panel_modifier_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInlineModifier = !showInlineModifier },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MODIFICAR MÚSICA EN VIVO",
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Ecualiza y potencia tu pista desde este mismo panel",
                                    color = theme.textSecondaryColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = dspSettings.dspMasterEnabled,
                                onCheckedChange = onToggleDspMaster,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.35f)
                                )
                            )
                            Icon(
                                imageVector = if (showInlineModifier) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expandir modificador",
                                tint = theme.accentColor
                            )
                        }
                    }

                    AnimatedVisibility(visible = showInlineModifier) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Quick Presets Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("PUNCH BASS", "CLUB", "ROCK", "YOUTUBE VOCAL", "FLAT").forEach { pName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.cardColor)
                                            .border(1.dp, theme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .clickable { onQuickPresetSelect(pName) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = pName,
                                            color = theme.textColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Bass Boost & Preamp Live Sliders
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("REFUERZO BASS BOOST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor)
                                    Text("+${dspSettings.bassBoostDb.roundToInt()} dB", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                                }
                                Slider(
                                    value = dspSettings.bassBoostDb,
                                    onValueChange = onBassBoostChange,
                                    valueRange = 0f..24f,
                                    enabled = dspSettings.dspMasterEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = theme.primaryColor,
                                        activeTrackColor = theme.primaryColor,
                                        inactiveTrackColor = theme.cardColor
                                    ),
                                    modifier = Modifier.height(26.dp)
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("PREAMP / POTENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondaryColor)
                                    Text("${if (dspSettings.preampDb > 0) "+" else ""}${dspSettings.preampDb.roundToInt()} dB", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = theme.accentColor)
                                }
                                Slider(
                                    value = dspSettings.preampDb,
                                    onValueChange = onPreampChange,
                                    valueRange = -12f..18f,
                                    enabled = dspSettings.dspMasterEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = theme.accentColor,
                                        activeTrackColor = theme.accentColor,
                                        inactiveTrackColor = theme.cardColor
                                    ),
                                    modifier = Modifier.height(26.dp)
                                )
                            }

                            // 5-Band Quick Graphic EQ Sliders
                            val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6k", "14k")
                            val repIndices = listOf(4, 10, 16, 22, 28)
                            bandLabels.forEachIndexed { bIdx, bLabel ->
                                val valDb = eqSettings.bands31.getOrElse(repIndices[bIdx]) { 0f }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bLabel,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textColor,
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Slider(
                                        value = valDb,
                                        onValueChange = { onQuickBandChange(bIdx, it) },
                                        valueRange = -15f..15f,
                                        enabled = dspSettings.dspMasterEnabled,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = theme.primaryColor,
                                            activeTrackColor = theme.primaryColor,
                                            inactiveTrackColor = theme.cardColor
                                        )
                                    )
                                    Text(
                                        text = "%+.0fdB".format(valDb),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (valDb != 0f) theme.primaryColor else theme.textSecondaryColor,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. CARGAR MÚSICA DEL CELULAR (EN EL MISMO PANEL)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { multiAudioPicker.launch(arrayOf("audio/*")) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("load_audio_file_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Abrir archivos", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CARGAR MÚSICA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                OutlinedButton(
                    onClick = {
                        if (hasAudioLibraryPermission) {
                            onRefreshLibrary()
                        } else {
                            onRequestLibraryPermission()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scan_device_music_button"),
                    border = BorderStroke(1.2.dp, theme.primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.accentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (hasAudioLibraryPermission) Icons.Default.Refresh else Icons.Default.LibraryMusic,
                        contentDescription = "Escanear celular",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanningLibrary) "BUSCANDO..." else if (hasAudioLibraryPermission) "ACTUALIZAR" else "VER MI MÚSICA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Library Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MÚSICA DE TU CELULAR (${tracks.size})",
                    color = theme.textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SE ABRE EN ESTE PANEL",
                    color = theme.primaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (tracks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = if (hasAudioLibraryPermission) "No se encontraron canciones aún" else "Carga la música de tu teléfono",
                            color = theme.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Presiona 'CARGAR MÚSICA' para abrir tus archivos MP3/WAV/FLAC o 'VER MI MÚSICA' para cargar todas las canciones guardadas en tu celular dentro de este mismo panel.",
                            color = theme.textSecondaryColor,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(tracks, key = { it.id }) { track ->
                val isSelected = track.id == currentTrack?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackSelect(track) }
                        .testTag("track_item_${track.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) theme.cardColor else theme.surfaceColor
                    ),
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(theme.primaryColor, theme.accentColor)))
                    } else {
                        BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.15f))
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) theme.primaryColor else theme.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected && isPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = if (isSelected) theme.onPrimaryColor else theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = if (isSelected) theme.primaryColor else theme.textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${track.artist} • ${track.durationSeconds / 60}:%02d".format(track.durationSeconds % 60),
                                color = theme.textSecondaryColor,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected && isPlaying) {
                            Text(
                                text = "EN VIVO",
                                color = theme.primaryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (track.id.startsWith("user_")) {
                            IconButton(
                                onClick = { onRemoveTrack(track) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar pista",
                                    tint = theme.textSecondaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
