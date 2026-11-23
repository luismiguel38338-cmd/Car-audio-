package com.example.downloads

import android.content.Context
import android.net.Uri
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.OnlineTrackItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Real offline download manager storing authorized tracks inside app-private filesDir/downloads.
 * Supports background streaming, HTTP Range pause/resume, cancellation, progress %, and deletion.
 * Strictly blocks any track without explicit downloadAllowed == true and valid licenseUrl.
 */
class DownloadManagerRepository(
    context: Context,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("car_audio_downloads_prefs", Context.MODE_PRIVATE)
    private val downloadDir = File(context.filesDir, "downloads").apply { mkdirs() }
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(loadManifest())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val _storageUsedBytes = MutableStateFlow(calculateStorageBytes())
    val storageUsedBytes: StateFlow<Long> = _storageUsedBytes.asStateFlow()

    fun enqueueAuthorizedDownload(track: OnlineTrackItem): Result<DownloadItem> {
        val remoteUrl = track.downloadUrl?.trim()
        val licenseUrl = track.licenseUrl?.trim()
        if (!track.downloadAllowed || remoteUrl.isNullOrBlank() || licenseUrl.isNullOrBlank()) {
            return Result.failure(
                IllegalStateException("Esta pista solo permite streaming y no cuenta con licencia de descarga autorizada.")
            )
        }

        val existing = _downloads.value.firstOrNull { it.trackId == track.id }
        if (existing != null && existing.status == DownloadStatus.COMPLETED && File(existing.localPath).exists()) {
            return Result.success(existing)
        }

        val safeFileName = buildSafeFileName(track.id, track.title)
        val targetFile = File(downloadDir, safeFileName)

        val item = DownloadItem(
            id = "dl_${track.id}",
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            artworkUrl = track.artworkUrl,
            remoteUrl = remoteUrl,
            localPath = targetFile.absolutePath,
            licenseUrl = licenseUrl,
            durationSeconds = track.durationSeconds,
            bytesDownloaded = if (targetFile.exists()) targetFile.length() else 0L,
            totalBytes = existing?.totalBytes ?: 0L,
            status = DownloadStatus.QUEUED,
            updatedAtMs = System.currentTimeMillis()
        )

        upsertItem(item)
        startWorker(item)
        return Result.success(item)
    }

    fun pauseDownload(downloadId: String) {
        activeJobs.remove(downloadId)?.cancel()
        val current = _downloads.value.firstOrNull { it.id == downloadId } ?: return
        if (current.status == DownloadStatus.DOWNLOADING || current.status == DownloadStatus.QUEUED) {
            upsertItem(
                current.copy(
                    status = DownloadStatus.PAUSED,
                    errorMessage = null,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    fun resumeDownload(downloadId: String) {
        val current = _downloads.value.firstOrNull { it.id == downloadId } ?: return
        if (current.status == DownloadStatus.COMPLETED && File(current.localPath).exists()) return
        val resumed = current.copy(
            status = DownloadStatus.QUEUED,
            errorMessage = null,
            updatedAtMs = System.currentTimeMillis()
        )
        upsertItem(resumed)
        startWorker(resumed)
    }

    fun cancelDownload(downloadId: String) {
        activeJobs.remove(downloadId)?.cancel()
        val current = _downloads.value.firstOrNull { it.id == downloadId } ?: return
        runCatching { File(current.localPath).delete() }
        upsertItem(
            current.copy(
                bytesDownloaded = 0L,
                status = DownloadStatus.CANCELLED,
                errorMessage = null,
                updatedAtMs = System.currentTimeMillis()
            )
        )
        refreshStorageBytes()
    }

    fun deleteDownload(downloadId: String) {
        activeJobs.remove(downloadId)?.cancel()
        val current = _downloads.value.firstOrNull { it.id == downloadId }
        if (current != null) {
            runCatching { File(current.localPath).delete() }
        }
        val next = _downloads.value.filterNot { it.id == downloadId }
        _downloads.value = next
        saveManifest(next)
        refreshStorageBytes()
    }

    fun clearAllDownloads(): Long {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        val freed = calculateStorageBytes()
        downloadDir.listFiles()?.forEach { runCatching { it.delete() } }
        _downloads.value = emptyList()
        saveManifest(emptyList())
        refreshStorageBytes()
        return freed
    }

    fun getLocalUriForTrack(onlineTrackId: String): Uri? {
        val item = _downloads.value.firstOrNull {
            it.trackId == onlineTrackId && it.status == DownloadStatus.COMPLETED
        } ?: return null
        val file = File(item.localPath)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file) else null
    }

    private fun startWorker(initial: DownloadItem) {
        activeJobs.remove(initial.id)?.cancel()
        val job = scope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val outFile = File(initial.localPath)
                outFile.parentFile?.mkdirs()
                var existingBytes = if (outFile.exists()) outFile.length() else 0L

                upsertItem(
                    initial.copy(
                        bytesDownloaded = existingBytes,
                        status = DownloadStatus.DOWNLOADING,
                        errorMessage = null,
                        updatedAtMs = System.currentTimeMillis()
                    )
                )

                connection = (URL(initial.remoteUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 20000
                    instanceFollowRedirects = true
                    if (existingBytes > 0L) {
                        setRequestProperty("Range", "bytes=$existingBytes-")
                    }
                }

                val code = connection.responseCode
                val isPartial = code == HttpURLConnection.HTTP_PARTIAL
                if (code !in 200..299 && !isPartial) {
                    throw IllegalStateException("Servidor respondió HTTP $code")
                }

                if (!isPartial && existingBytes > 0L) {
                    existingBytes = 0L
                }

                val contentLength = connection.contentLengthLong.coerceAtLeast(0L)
                val totalBytes = if (isPartial && contentLength > 0L) {
                    existingBytes + contentLength
                } else if (contentLength > 0L) {
                    contentLength
                } else {
                    initial.totalBytes
                }

                val buffer = ByteArray(16 * 1024)
                var downloaded = existingBytes
                var lastUiUpdateMs = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(outFile, isPartial).use { output ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read

                            val now = System.currentTimeMillis()
                            if (now - lastUiUpdateMs >= 200L) {
                                lastUiUpdateMs = now
                                updateProgress(initial.id, downloaded, totalBytes)
                            }
                        }
                        output.flush()
                    }
                }

                val latest = _downloads.value.firstOrNull { it.id == initial.id } ?: initial
                upsertItem(
                    latest.copy(
                        bytesDownloaded = downloaded,
                        totalBytes = totalBytes.coerceAtLeast(downloaded),
                        status = DownloadStatus.COMPLETED,
                        errorMessage = null,
                        updatedAtMs = System.currentTimeMillis()
                    )
                )
                refreshStorageBytes()
            } catch (_: CancellationException) {
                // Handled by pauseDownload or cancelDownload
            } catch (t: Throwable) {
                val latest = _downloads.value.firstOrNull { it.id == initial.id } ?: initial
                upsertItem(
                    latest.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = t.localizedMessage ?: "Error al descargar",
                        updatedAtMs = System.currentTimeMillis()
                    )
                )
                refreshStorageBytes()
            } finally {
                connection?.disconnect()
                activeJobs.remove(initial.id)
            }
        }
        activeJobs[initial.id] = job
    }

    private fun updateProgress(downloadId: String, downloaded: Long, totalBytes: Long) {
        val current = _downloads.value
        val updated = current.map { item ->
            if (item.id == downloadId) {
                item.copy(
                    bytesDownloaded = downloaded,
                    totalBytes = totalBytes,
                    status = DownloadStatus.DOWNLOADING,
                    updatedAtMs = System.currentTimeMillis()
                )
            } else {
                item
            }
        }
        _downloads.value = updated
    }

    private fun upsertItem(item: DownloadItem) {
        val current = _downloads.value.toMutableList()
        val idx = current.indexOfFirst { it.id == item.id }
        if (idx >= 0) current[idx] = item else current.add(0, item)
        _downloads.value = current
        saveManifest(current)
    }

    private fun refreshStorageBytes() {
        _storageUsedBytes.value = calculateStorageBytes()
    }

    private fun calculateStorageBytes(): Long {
        return downloadDir.listFiles()?.sumOf { if (it.isFile) it.length() else 0L } ?: 0L
    }

    private fun buildSafeFileName(trackId: String, title: String): String {
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(36).ifBlank { "audio" }
        val cleanId = trackId.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(24)
        return "${cleanId}_$cleanTitle.mp3"
    }

    private fun loadManifest(): List<DownloadItem> {
        val raw = prefs.getString("downloads_manifest_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val rawStatus = obj.optString("status", DownloadStatus.PAUSED.name)
                val parsedStatus = runCatching { DownloadStatus.valueOf(rawStatus) }
                    .getOrDefault(DownloadStatus.PAUSED)
                val normalizedStatus = if (parsedStatus == DownloadStatus.DOWNLOADING || parsedStatus == DownloadStatus.QUEUED) {
                    DownloadStatus.PAUSED
                } else {
                    parsedStatus
                }
                val localPath = obj.optString("localPath", "")
                val fileSize = if (localPath.isNotBlank() && File(localPath).exists()) File(localPath).length() else 0L
                DownloadItem(
                    id = obj.getString("id"),
                    trackId = obj.getString("trackId"),
                    title = obj.getString("title"),
                    artist = obj.optString("artist", "Artista"),
                    album = obj.optString("album", "Jamendo"),
                    artworkUrl = obj.optString("artworkUrl", "").takeIf { it.isNotBlank() },
                    remoteUrl = obj.getString("remoteUrl"),
                    localPath = localPath,
                    licenseUrl = obj.optString("licenseUrl", "").takeIf { it.isNotBlank() },
                    durationSeconds = obj.optInt("duration", 0),
                    bytesDownloaded = fileSize.coerceAtLeast(obj.optLong("bytes", 0L)),
                    totalBytes = obj.optLong("totalBytes", 0L),
                    status = normalizedStatus,
                    errorMessage = obj.optString("error", "").takeIf { it.isNotBlank() },
                    updatedAtMs = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveManifest(items: List<DownloadItem>) {
        try {
            val arr = JSONArray()
            items.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("trackId", item.trackId)
                        put("title", item.title)
                        put("artist", item.artist)
                        put("album", item.album)
                        put("artworkUrl", item.artworkUrl ?: "")
                        put("remoteUrl", item.remoteUrl)
                        put("localPath", item.localPath)
                        put("licenseUrl", item.licenseUrl ?: "")
                        put("duration", item.durationSeconds)
                        put("bytes", item.bytesDownloaded)
                        put("totalBytes", item.totalBytes)
                        put("status", item.status.name)
                        put("error", item.errorMessage ?: "")
                        put("updatedAt", item.updatedAtMs)
                    }
                )
            }
            prefs.edit().putString("downloads_manifest_json", arr.toString()).apply()
        } catch (_: Throwable) {}
    }
}
