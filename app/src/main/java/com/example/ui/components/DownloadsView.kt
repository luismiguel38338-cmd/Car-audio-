package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CarAudioThemeType
import com.example.model.DownloadItem
import com.example.model.DownloadStatus

@Composable
fun DownloadsView(
    theme: CarAudioThemeType,
    downloads: List<DownloadItem>,
    storageUsedBytes: Long,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onPlayDownloaded: (DownloadItem) -> Unit,
    onClearAll: () -> Unit,
    onOpenOnlineMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = downloads.count { it.status == DownloadStatus.COMPLETED }
    val activeCount = downloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp)
            .testTag("downloads_view"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "downloads_header") {
            GlassStudioCard(theme = theme, highlightBorder = activeCount > 0) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = "Descargas offline",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "Gestor de Descargas Offline",
                                    color = theme.textColor,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$completedCount listas para reproducir • ${formatStorageBytes(storageUsedBytes)} usados",
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        if (downloads.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onClearAll,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, theme.errorColor.copy(alpha = 0.45f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Limpiar todo", tint = theme.errorColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vaciar", color = theme.errorColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.backgroundColor)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                        Text(
                            text = "Solo se descargan pistas de Música Online con licencia Creative Commons / permiso explícito del autor.",
                            color = theme.textSecondaryColor,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        if (downloads.isEmpty()) {
            item(key = "downloads_empty") {
                GlassStudioCard(theme = theme) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Downloading, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(38.dp))
                        Text("Sin descargas todavía", color = theme.textColor, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Busca música libre de derechos en Música Online y descarga tus pistas favoritas para escucharlas sin internet en el carro.",
                            color = theme.textSecondaryColor,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                        GlowActionButton(
                            text = "Explorar Música Online",
                            theme = theme,
                            onClick = onOpenOnlineMusic,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            items(downloads, key = { it.id }) { item ->
                DownloadRowCard(
                    item = item,
                    theme = theme,
                    onPause = { onPauseDownload(item.id) },
                    onResume = { onResumeDownload(item.id) },
                    onCancel = { onCancelDownload(item.id) },
                    onDelete = { onDeleteDownload(item.id) },
                    onPlay = { onPlayDownloaded(item) }
                )
            }
        }
    }
}

@Composable
private fun DownloadRowCard(
    item: DownloadItem,
    theme: CarAudioThemeType,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    val pct = (item.progressFraction * 100f).toInt().coerceIn(0, 100)
    GlassStudioCard(
        theme = theme,
        highlightBorder = item.status == DownloadStatus.COMPLETED,
        modifier = Modifier.testTag("download_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    if (!item.artworkUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.artworkUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (item.status == DownloadStatus.COMPLETED) Icons.Default.DownloadDone else Icons.Default.Downloading,
                            contentDescription = null,
                            tint = theme.primaryColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = theme.textColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.artist} • ${item.status.label}",
                        color = theme.textSecondaryColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.backgroundColor
                ) {
                    Text(
                        text = if (item.status == DownloadStatus.COMPLETED) formatStorageBytes(item.bytesDownloaded) else "$pct%",
                        color = if (item.status == DownloadStatus.COMPLETED) theme.primaryColor else theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.QUEUED) {
                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    color = theme.primaryColor,
                    trackColor = theme.cardColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            if (!item.errorMessage.isNullOrBlank() && item.status == DownloadStatus.FAILED) {
                Text(
                    text = item.errorMessage,
                    color = theme.errorColor,
                    fontSize = 10.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (item.status) {
                    DownloadStatus.COMPLETED -> {
                        Button(
                            onClick = onPlay,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reproducir Offline", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        OutlinedButton(
                            onClick = onPause,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pausar", color = theme.textColor, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.cardColor)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancelar", tint = theme.errorColor, modifier = Modifier.size(15.dp))
                        }
                    }
                    DownloadStatus.PAUSED, DownloadStatus.FAILED, DownloadStatus.CANCELLED -> {
                        OutlinedButton(
                            onClick = onResume,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.55f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reanudar", color = theme.textColor, fontSize = 11.5.sp)
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar descarga", tint = theme.textSecondaryColor)
                }
            }
        }
    }
}
