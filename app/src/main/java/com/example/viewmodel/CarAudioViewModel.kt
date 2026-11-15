package com.example.viewmodel

import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CarAudioEngine
import com.example.model.AudioSessionTarget
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.FaultActionType
import com.example.model.FaultSeverity
import com.example.model.OutputDeviceStatus
import com.example.model.ParametricFilterType
import com.example.model.RepeatMode
import com.example.model.RtaBand
import com.example.model.SubwooferPreset
import com.example.model.SystemFaultAlert
import com.example.model.TrackCategory
import com.example.notification.CarAudioNotificationHelper
import com.example.service.CarAudioPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random

enum class AppTab(val title: String) {
    PLAYER("MÚSICA"),
    BASS("BAJOS"),
    EQ("EQ"),
    DSP("DSP"),
    AJUSTES("AJUSTES")
}

enum class LibraryFilter(val label: String) {
    ALL("Todas"),
    FAVORITES("Favoritas"),
    LOADED("Cargadas")
}

class CarAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val audioEngine = CarAudioEngine.getInstance(context)
    private val prefs = application.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val VIP_ACCESS_CODE = "2020"
    }

    private val systemLowRam: Boolean = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.isLowRamDevice == true
    } catch (_: Throwable) {
        false
    }

    val isIntroEnabledPref: Boolean
        get() = prefs.getBoolean("intro_video_enabled", true)
    val isDontShowAgainPref: Boolean
        get() = prefs.getBoolean("dont_show_again", false)

    private val _showSplashScreen = MutableStateFlow(
        !prefs.getBoolean("onboarding_completed", false) || (isIntroEnabledPref && !isDontShowAgainPref)
    )
    val showSplashScreen: StateFlow<Boolean> = _showSplashScreen.asStateFlow()

    private val _autoPlayIntro = MutableStateFlow(isIntroEnabledPref)
    val autoPlayIntro: StateFlow<Boolean> = _autoPlayIntro.asStateFlow()

    private val _isVipUnlocked = MutableStateFlow(prefs.getBoolean("vip_unlocked", false))
    val isVipUnlocked: StateFlow<Boolean> = _isVipUnlocked.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean("keep_screen_on", true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _liteVisualsEnabled = MutableStateFlow(
        prefs.getBoolean("lite_visuals_enabled", systemLowRam)
    )
    val liteVisualsEnabled: StateFlow<Boolean> = _liteVisualsEnabled.asStateFlow()

    val isLowRamDevice: Boolean = systemLowRam

    // Defaults to Crimson Studio Red ("Todo Rojo") or persisted user choice
    private val _currentTheme = MutableStateFlow(loadSavedTheme())
    val currentTheme: StateFlow<CarAudioThemeType> = _currentTheme.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.PLAYER)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _dspSettings = MutableStateFlow(loadSavedDspSettings())
    val dspSettings: StateFlow<DspSettings> = _dspSettings.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(loadSavedEqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _eqPresets = MutableStateFlow(loadSavedPresetsCatalog())
    val eqPresets: StateFlow<List<EqPresetItem>> = _eqPresets.asStateFlow()

    // Phone Music Library, Search, Filter, Shuffle & Repeat
    private val _favoriteIds = MutableStateFlow(loadFavoriteIds())
    private val _tracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _libraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val libraryFilter: StateFlow<LibraryFilter> = _libraryFilter.asStateFlow()

    val filteredTracks: StateFlow<List<AudioTrackItem>> = combine(
        _tracks,
        _searchQuery,
        _libraryFilter
    ) { list, query, filter ->
        val q = query.trim().lowercase()
        list.filter { item ->
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.FAVORITES -> item.isFavorite
                LibraryFilter.LOADED -> item.id.startsWith("user_") || item.id.startsWith("saved_")
            }
            val matchesQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.artist.lowercase().contains(q) ||
                item.album.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(audioEngine.currentTrack)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(audioEngine.isPlaying)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSeconds = MutableStateFlow(audioEngine.playbackPositionSeconds)
    val playbackSeconds: StateFlow<Int> = _playbackSeconds.asStateFlow()

    private val _volumeLevel = MutableStateFlow(audioEngine.volumeFactor)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(prefs.getBoolean("shuffle_enabled", false))
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(loadSavedRepeatMode())
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isScanningLibrary = MutableStateFlow(false)
    val isScanningLibrary: StateFlow<Boolean> = _isScanningLibrary.asStateFlow()

    private val _hasAudioLibraryPermission = MutableStateFlow(checkAudioStoragePermission())
    val hasAudioLibraryPermission: StateFlow<Boolean> = _hasAudioLibraryPermission.asStateFlow()

    // Real-time Meter, Device Routing & Session Telemetry
    private val _leftVu = MutableStateFlow(0f)
    val leftVu: StateFlow<Float> = _leftVu.asStateFlow()

    private val _rightVu = MutableStateFlow(0f)
    val rightVu: StateFlow<Float> = _rightVu.asStateFlow()

    private val _splDb = MutableStateFlow(0f)
    val splDb: StateFlow<Float> = _splDb.asStateFlow()

    private val _rtaBands = MutableStateFlow(CarAudioEngine.createInitialRtaBands())
    val rtaBands: StateFlow<List<RtaBand>> = _rtaBands.asStateFlow()

    private val _isClippingDetected = MutableStateFlow(false)
    val isClippingDetected: StateFlow<Boolean> = _isClippingDetected.asStateFlow()

    private val _isMicRta = MutableStateFlow(false)
    val isMicRta: StateFlow<Boolean> = _isMicRta.asStateFlow()

    private val _sourceMode = MutableStateFlow(AudioSourceMode.INTERNAL_DSP)
    val sourceMode: StateFlow<AudioSourceMode> = _sourceMode.asStateFlow()

    private val _outputDeviceStatus = MutableStateFlow(audioEngine.outputDeviceStatus)
    val outputDeviceStatus: StateFlow<OutputDeviceStatus> = _outputDeviceStatus.asStateFlow()

    private val _sessionTarget = MutableStateFlow(audioEngine.sessionTarget)
    val sessionTarget: StateFlow<AudioSessionTarget> = _sessionTarget.asStateFlow()

    private val _fxEngineStatus = MutableStateFlow(audioEngine.fxEngineStatus)
    val fxEngineStatus: StateFlow<String> = _fxEngineStatus.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(
        CarAudioNotificationHelper.hasNotificationPermission(context)
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _faultAlerts = MutableStateFlow<List<SystemFaultAlert>>(emptyList())
    val faultAlerts: StateFlow<List<SystemFaultAlert>> = _faultAlerts.asStateFlow()

    private val _latestBannerAlert = MutableStateFlow<SystemFaultAlert?>(null)
    val latestBannerAlert: StateFlow<SystemFaultAlert?> = _latestBannerAlert.asStateFlow()

    @Volatile private var uiActive = true
    private var lastClipAlertTime = 0L
    private var clipCount = 0

    init {
        audioEngine.onSettingsChangedListener = { updatedDsp, updatedEq ->
            _dspSettings.value = updatedDsp
            _equalizerSettings.value = updatedEq
            persistDspAndEq(updatedDsp, updatedEq)
        }

        audioEngine.onTrackCompletedListener = {
            handleTrackCompletion()
        }

        audioEngine.onPlaybackStateChangedListener = {
            _isPlaying.value = audioEngine.isPlaying
            _currentTrack.value = audioEngine.currentTrack
            _playbackSeconds.value = audioEngine.playbackPositionSeconds
            _sessionTarget.value = audioEngine.sessionTarget
            _fxEngineStatus.value = audioEngine.fxEngineStatus
            if (audioEngine.currentTrack != null) {
                CarAudioPlaybackService.sync(context)
            }
        }

        audioEngine.onOutputDeviceChangedListener = { device ->
            _outputDeviceStatus.value = device
        }

        audioEngine.onAudioFaultListener = { title, detail, severity ->
            reportSystemFault(
                title = title,
                message = detail,
                severity = severity,
                actionLabel = if (severity == FaultSeverity.ERROR) "Reintentar" else null,
                actionType = if (severity == FaultSeverity.ERROR) FaultActionType.RELOAD_LIBRARY else FaultActionType.NONE,
                sendSystemNotification = severity != FaultSeverity.INFO
            )
        }

        CarAudioPlaybackService.onSkipNextRequested = { nextTrack() }
        CarAudioPlaybackService.onSkipPrevRequested = { previousTrack() }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                CarAudioNotificationHelper.initNotificationChannels(context)
                if (audioEngine.activeAudioSessionId < 0) {
                    audioEngine.attachToGlobalSystemSession(_dspSettings.value, _equalizerSettings.value)
                } else {
                    audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
                }
                _sessionTarget.value = audioEngine.sessionTarget
                _fxEngineStatus.value = audioEngine.fxEngineStatus
            } catch (t: Throwable) {
                reportSystemFault(
                    title = "Fallo al Iniciar Motor AudioFX",
                    message = t.localizedMessage ?: "No se pudo inicializar el procesador de audio.",
                    severity = FaultSeverity.WARNING,
                    actionLabel = "Diagnóstico",
                    actionType = FaultActionType.OPEN_DIAGNOSTICS
                )
            }
            scanDeviceMusicLibrary()
        }

        startTelemetryLoop()
    }

    fun setUiActive(active: Boolean) {
        uiActive = active
        val needVisualizer = active && (_selectedTab.value == AppTab.DSP || _selectedTab.value == AppTab.BASS || _isMicRta.value)
        audioEngine.setVisualizerActive(needVisualizer)
        if (active) {
            audioEngine.refreshOutputDeviceStatus()
            _hasNotificationPermission.value = CarAudioNotificationHelper.hasNotificationPermission(context)
        }
    }

    private fun checkAudioStoragePermission(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    fun onAudioStoragePermissionResult(granted: Boolean) {
        val nowGranted = granted || checkAudioStoragePermission()
        _hasAudioLibraryPermission.value = nowGranted
        if (!nowGranted) {
            reportSystemFault(
                title = "Permiso de Música Denegado",
                message = "No podemos escanear las carpetas del teléfono sin permiso. Puedes otorgarlo o usar 'Cargar Música'.",
                severity = FaultSeverity.WARNING,
                actionLabel = "Dar Permiso",
                actionType = FaultActionType.REQUEST_AUDIO_PERMISSION
            )
        }
        scanDeviceMusicLibrary()
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        val hasPerm = granted || CarAudioNotificationHelper.hasNotificationPermission(context)
        _hasNotificationPermission.value = hasPerm
        if (!hasPerm) {
            reportSystemFault(
                title = "Notificaciones Desactivadas",
                message = "Activa las notificaciones para recibir alertas inmediatas cuando falle un archivo o haya clipping.",
                severity = FaultSeverity.WARNING,
                actionLabel = "Activar",
                actionType = FaultActionType.REQUEST_NOTIFICATION_PERMISSION,
                sendSystemNotification = false
            )
        }
    }

    fun reportSystemFault(
        title: String,
        message: String,
        severity: FaultSeverity = FaultSeverity.WARNING,
        actionLabel: String? = null,
        actionType: FaultActionType = FaultActionType.NONE,
        sendSystemNotification: Boolean = true
    ) {
        val alert = SystemFaultAlert(
            id = "${System.currentTimeMillis()}_${title.hashCode()}",
            title = title,
            message = message,
            severity = severity,
            timestampMs = System.currentTimeMillis(),
            actionLabel = actionLabel,
            actionType = actionType
        )
        _faultAlerts.value = (listOf(alert) + _faultAlerts.value).take(25)
        _latestBannerAlert.value = alert
        if (sendSystemNotification) {
            CarAudioNotificationHelper.showSystemFaultNotification(
                context = context,
                title = title,
                message = message,
                faultCode = title.hashCode()
            )
        }
    }

    fun dismissBannerAlert() {
        _latestBannerAlert.value = null
    }

    fun clearFaultHistory() {
        _faultAlerts.value = emptyList()
        _latestBannerAlert.value = null
    }

    fun refreshSystemHealth() {
        _hasAudioLibraryPermission.value = checkAudioStoragePermission()
        _hasNotificationPermission.value = CarAudioNotificationHelper.hasNotificationPermission(context)
        _fxEngineStatus.value = audioEngine.fxEngineStatus
        audioEngine.refreshOutputDeviceStatus()
        if (!_hasNotificationPermission.value) {
            reportSystemFault(
                title = "Permiso de Notificaciones Faltante",
                message = "Android requiere permiso de notificaciones para avisarte si falla una canción o hay distorsión.",
                severity = FaultSeverity.WARNING,
                actionLabel = "Activar",
                actionType = FaultActionType.REQUEST_NOTIFICATION_PERMISSION,
                sendSystemNotification = false
            )
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun toggleFavorite(track: AudioTrackItem) {
        val currentFavs = _favoriteIds.value.toMutableSet()
        val nowFav = !track.isFavorite
        if (nowFav) currentFavs.add(track.id) else currentFavs.remove(track.id)
        _favoriteIds.value = currentFavs
        prefs.edit().putStringSet("favorite_track_ids", currentFavs).apply()

        _tracks.value = _tracks.value.map {
            if (it.id == track.id) it.copy(isFavorite = nowFav) else it
        }
        if (_currentTrack.value?.id == track.id) {
            _currentTrack.value = _currentTrack.value?.copy(isFavorite = nowFav)
        }
    }

    fun scanDeviceMusicLibrary() {
        viewModelScope.launch {
            _isScanningLibrary.value = true
            val hasPerm = checkAudioStoragePermission()
            _hasAudioLibraryPermission.value = hasPerm
            val favs = _favoriteIds.value

            val combined = withContext(Dispatchers.IO) {
                val result = ArrayList<AudioTrackItem>(64)
                val seenUris = HashSet<String>(64)

                // 1. Load saved user-picked URIs from SharedPreferences
                loadSavedCustomTracks(favs).forEach { track ->
                    val uriKey = track.customUri?.toString().orEmpty()
                    if (uriKey.isNotEmpty() && seenUris.add(uriKey)) {
                        result.add(track)
                    }
                }

                // 2. Scan MediaStore.Audio.Media if device permission granted
                if (hasPerm) {
                    try {
                        val projection = arrayOf(
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.MIME_TYPE
                        )
                        val selection = "${MediaStore.Audio.Media.DURATION} >= 3000"
                        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
                        context.contentResolver.query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            selection,
                            null,
                            sortOrder
                        )?.use { cursor ->
                            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                            val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                            val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

                            while (cursor.moveToNext()) {
                                val id = cursor.getLong(idCol)
                                val contentUri = ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                                val uriKey = contentUri.toString()
                                if (!seenUris.add(uriKey)) continue

                                val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Pista $id"
                                val artist = cursor.getString(artistCol)?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Artista Local"
                                val album = cursor.getString(albumCol)?.takeIf { it.isNotBlank() } ?: "Música del Teléfono"
                                val durationSec = (cursor.getLong(durCol) / 1000L).toInt().coerceAtLeast(1)
                                val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                                val formatLabel = formatMimeLabel(mime)
                                val artUri = if (albumIdCol >= 0) {
                                    val albumId = cursor.getLong(albumIdCol)
                                    if (albumId > 0L) {
                                        ContentUris.withAppendedId(
                                            Uri.parse("content://media/external/audio/albumart"),
                                            albumId
                                        )
                                    } else null
                                } else null

                                val trackId = "media_$id"
                                result.add(
                                    AudioTrackItem(
                                        id = trackId,
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        durationSeconds = durationSec,
                                        category = TrackCategory.USER_CUSTOM,
                                        frequencyDescription = "Música del Teléfono • DSP Activo",
                                        isSynthesized = false,
                                        customUri = contentUri,
                                        albumArtUri = artUri,
                                        isFavorite = favs.contains(trackId),
                                        audioFormatLabel = formatLabel
                                    )
                                )
                            }
                        }
                    } catch (t: Throwable) {
                        reportSystemFault(
                            title = "Error al Escanear Biblioteca",
                            message = t.localizedMessage ?: "No se pudo consultar MediaStore del dispositivo.",
                            severity = FaultSeverity.WARNING
                        )
                    }
                }
                result
            }

            _tracks.value = combined
            if (_currentTrack.value == null && combined.isNotEmpty()) {
                _currentTrack.value = combined.first()
            }
            _isScanningLibrary.value = false
        }
    }

    private fun formatMimeLabel(mime: String?): String {
        val m = mime?.lowercase().orEmpty()
        return when {
            m.contains("flac") -> "FLAC Hi-Res"
            m.contains("wav") || m.contains("x-wav") -> "WAV PCM"
            m.contains("aac") || m.contains("mp4") || m.contains("m4a") -> "AAC / M4A"
            m.contains("ogg") || m.contains("opus") -> "OGG / OPUS"
            m.contains("mpeg") || m.contains("mp3") -> "MP3"
            else -> "Audio Local"
        }
    }

    private fun loadSavedCustomTracks(favs: Set<String>): List<AudioTrackItem> {
        val raw = prefs.getString("saved_custom_tracks", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = ArrayList<AudioTrackItem>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val uriStr = obj.optString("uri", "")
                if (uriStr.isBlank()) continue
                val uri = Uri.parse(uriStr)
                val trackId = obj.optString("id", "saved_$i")
                list.add(
                    AudioTrackItem(
                        id = trackId,
                        title = obj.optString("title", "Audio Local"),
                        artist = obj.optString("artist", "Archivo Cargado"),
                        album = obj.optString("album", "Dispositivo"),
                        durationSeconds = obj.optInt("duration", 180),
                        category = TrackCategory.USER_CUSTOM,
                        frequencyDescription = "Archivo Cargado • DSP en Vivo",
                        isSynthesized = false,
                        customUri = uri,
                        isFavorite = favs.contains(trackId)
                    )
                )
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun persistCustomTrack(track: AudioTrackItem) {
        val uri = track.customUri ?: return
        try {
            val currentRaw = prefs.getString("saved_custom_tracks", "[]")
            val arr = JSONArray(currentRaw)
            val newArr = JSONArray()
            val entry = JSONObject().apply {
                put("id", track.id)
                put("uri", uri.toString())
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("duration", track.durationSeconds)
            }
            newArr.put(entry)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("uri") != uri.toString()) {
                    newArr.put(obj)
                }
            }
            prefs.edit().putString("saved_custom_tracks", newArr.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun removePersistedCustomTrack(uri: Uri) {
        try {
            val currentRaw = prefs.getString("saved_custom_tracks", "[]")
            val arr = JSONArray(currentRaw)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("uri") != uri.toString()) {
                    newArr.put(obj)
                }
            }
            prefs.edit().putString("saved_custom_tracks", newArr.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun resolveTrackMetadata(uri: Uri, fallbackName: String): AudioTrackItem {
        var displayName = fallbackName
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        c.getString(idx)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                    }
                }
            }
        } catch (_: Throwable) {}

        var title = displayName.substringBeforeLast(".").ifBlank { "Pista de Audio" }
        var artist = "Archivo Local"
        var album = "Mi Teléfono"
        var durationSec = 180
        var bitrateKbps = 0
        var formatLabel = formatMimeLabel(context.contentResolver.getType(uri))

        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let { title = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let { artist = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let { album = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull()?.let { ms ->
                if (ms > 0) durationSec = ms / 1000
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let { bps ->
                if (bps > 0) bitrateKbps = bps / 1000
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let { mime ->
                formatLabel = formatMimeLabel(mime)
            }
        } catch (t: Throwable) {
            reportSystemFault(
                title = "Metadatos Incompletos",
                message = "No se pudieron leer todos los datos de \"$title\": ${t.localizedMessage ?: "formato parcial"}.",
                severity = FaultSeverity.WARNING,
                sendSystemNotification = false
            )
        } finally {
            try {
                retriever?.release()
            } catch (_: Throwable) {}
        }

        val id = "user_${uri.toString().hashCode()}"
        return AudioTrackItem(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSec,
            category = TrackCategory.USER_CUSTOM,
            frequencyDescription = "Audio Cargado • Modificación en Vivo",
            isSynthesized = false,
            customUri = uri,
            isFavorite = _favoriteIds.value.contains(id),
            audioFormatLabel = formatLabel,
            bitrateKbps = bitrateKbps
        )
    }

    fun addCustomUserTracks(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                uris.map { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Throwable) {}
                    val track = resolveTrackMetadata(uri, uri.lastPathSegment ?: "Pista Local")
                    persistCustomTrack(track)
                    track
                }
            }
            val newUriStrings = loaded.mapNotNull { it.customUri?.toString() }.toSet()
            val remaining = _tracks.value.filter { it.customUri?.toString() !in newUriStrings }
            _tracks.value = loaded + remaining
            loaded.firstOrNull()?.let { playTrack(it) }
        }
    }

    fun addCustomUserTrack(uri: Uri, name: String) {
        addCustomUserTracks(listOf(uri))
    }

    fun removeTrack(track: AudioTrackItem) {
        track.customUri?.let { removePersistedCustomTrack(it) }
        val updated = _tracks.value.filter { it.id != track.id }
        _tracks.value = updated
        if (_currentTrack.value?.id == track.id) {
            audioEngine.stopPlayback()
            _isPlaying.value = false
            _currentTrack.value = updated.firstOrNull()
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (isActive) {
                val activeAudio = audioEngine.isPlaying || audioEngine.isMicRtaActive

                if (!uiActive && !activeAudio) {
                    delay(500)
                    continue
                }

                val pos = audioEngine.playbackPositionSeconds
                if (_playbackSeconds.value != pos) {
                    _playbackSeconds.value = pos
                }

                val enginePlaying = audioEngine.isPlaying
                if (_isPlaying.value != enginePlaying) {
                    _isPlaying.value = enginePlaying
                }

                val sess = audioEngine.sessionTarget
                if (_sessionTarget.value != sess) {
                    _sessionTarget.value = sess
                }

                if (uiActive && activeAudio) {
                    val l = audioEngine.leftVuLevel
                    val r = audioEngine.rightVuLevel
                    if (abs(_leftVu.value - l) > 0.01f) _leftVu.value = l
                    if (abs(_rightVu.value - r) > 0.01f) _rightVu.value = r

                    val spl = audioEngine.currentSplDb
                    if (abs(_splDb.value - spl) > 0.2f) _splDb.value = spl

                    if (_selectedTab.value == AppTab.DSP || _isMicRta.value) {
                        _rtaBands.value = audioEngine.getRtaSnapshot()
                    }
                } else if (_leftVu.value != 0f || _rightVu.value != 0f) {
                    _leftVu.value = 0f
                    _rightVu.value = 0f
                }

                val dsp = _dspSettings.value
                val totalGain = dsp.masterGainDb + dsp.preampDb + dsp.bassBoostDb
                val clipCondition = audioEngine.isClippingDetected ||
                    (enginePlaying && !dsp.limiterEnabled && totalGain > 22f && _leftVu.value > 0.9f)
                if (_isClippingDetected.value != clipCondition) {
                    _isClippingDetected.value = clipCondition
                }
                if (clipCondition) {
                    clipCount++
                    val now = System.currentTimeMillis()
                    if (now - lastClipAlertTime > 12000) {
                        lastClipAlertTime = now
                        CarAudioNotificationHelper.showClippingAlertNotification(context, clipCount)
                        reportSystemFault(
                            title = "Alerta de Clipping / Distorsión",
                            message = "La salida superó el límite limpio ($clipCount picos). Baja el Preamp o activa el Limitador.",
                            severity = FaultSeverity.WARNING,
                            actionLabel = if (!dsp.limiterEnabled) "Activar Limitador" else null,
                            actionType = if (!dsp.limiterEnabled) FaultActionType.ENABLE_LIMITER else FaultActionType.NONE,
                            sendSystemNotification = false
                        )
                    }
                }

                val intervalMs = when {
                    !uiActive -> 450L
                    !activeAudio -> 280L
                    _liteVisualsEnabled.value -> 85L
                    else -> 45L
                }
                delay(intervalMs)
            }
        }
    }

    // Navigation, Onboarding, VIP & Preferences
    fun dismissSplashScreen() {
        _showSplashScreen.value = false
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun openSplashScreen() {
        _showSplashScreen.value = true
    }

    fun playStartupSound() { audioEngine.playIntroBootSound() }

    fun unlockVipWithCode(code: String): Boolean {
        val valid = code.trim() == VIP_ACCESS_CODE
        if (valid) {
            _isVipUnlocked.value = true
            prefs.edit().putBoolean("vip_unlocked", true).apply()
        }
        return valid
    }

    fun lockVip() {
        _isVipUnlocked.value = false
        prefs.edit().putBoolean("vip_unlocked", false).apply()
        if (_currentTheme.value.isVipOnly) {
            setTheme(CarAudioThemeType.CRIMSON_RED)
        }
        val curr = _dspSettings.value
        if (curr.hiResQualityEnabled || curr.tubeWarmthLevel > 0f) {
            updateAndCommitDsp(curr.copy(hiResQualityEnabled = false, tubeWarmthLevel = 0f))
        }
    }

    fun setTheme(theme: CarAudioThemeType) {
        if (theme.isVipOnly && !_isVipUnlocked.value) return
        _currentTheme.value = theme
        prefs.edit().putString("selected_theme_id", theme.id).apply()
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
        audioEngine.setVisualizerActive(uiActive && (tab == AppTab.DSP || tab == AppTab.BASS || _isMicRta.value))
    }

    fun setAutoPlayIntro(enabled: Boolean) {
        _autoPlayIntro.value = enabled
        prefs.edit().putBoolean("intro_video_enabled", enabled).apply()
    }

    fun setDontShowIntroAgain(dontShow: Boolean) {
        prefs.edit().putBoolean("dont_show_again", dontShow).apply()
        setAutoPlayIntro(!dontShow)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        prefs.edit().putBoolean("keep_screen_on", enabled).apply()
    }

    fun setLiteVisualsEnabled(enabled: Boolean) {
        _liteVisualsEnabled.value = enabled
        prefs.edit().putBoolean("lite_visuals_enabled", enabled).apply()
    }

    // Playback & Queue Controls
    fun toggleShuffle() {
        val next = !_shuffleEnabled.value
        _shuffleEnabled.value = next
        prefs.edit().putBoolean("shuffle_enabled", next).apply()
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        prefs.edit().putString("repeat_mode", next.name).apply()
    }

    fun playTrack(track: AudioTrackItem) {
        audioEngine.playTrack(track, _dspSettings.value, _equalizerSettings.value)
        _currentTrack.value = track
        _isPlaying.value = true
        _sessionTarget.value = audioEngine.sessionTarget
        CarAudioPlaybackService.sync(context)
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: _tracks.value.firstOrNull() ?: return
        if (_currentTrack.value == null) {
            playTrack(track)
            return
        }
        audioEngine.togglePlayPause(_dspSettings.value, _equalizerSettings.value)
        _isPlaying.value = audioEngine.isPlaying
        CarAudioPlaybackService.sync(context)
    }

    private fun handleTrackCompletion() {
        val current = _currentTrack.value
        if (_repeatMode.value == RepeatMode.ONE && current != null) {
            playTrack(current)
            return
        }
        val list = _tracks.value
        if (list.isEmpty()) return
        if (_repeatMode.value == RepeatMode.OFF && !_shuffleEnabled.value) {
            val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
            if (idx >= list.lastIndex) {
                _isPlaying.value = false
                CarAudioPlaybackService.sync(context)
                return
            }
        }
        nextTrack()
    }

    fun nextTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        if (_shuffleEnabled.value && list.size > 1) {
            val current = _currentTrack.value
            val candidates = list.filter { it.id != current?.id }
            playTrack(candidates[Random.nextInt(candidates.size)])
            return
        }
        val current = _currentTrack.value
        val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
        val nextIdx = if (idx >= 0 && idx < list.size - 1) idx + 1 else 0
        playTrack(list[nextIdx])
    }

    fun previousTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        if (_playbackSeconds.value > 4 && _currentTrack.value != null) {
            seekTo(0)
            return
        }
        val current = _currentTrack.value
        val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
        val prevIdx = if (idx > 0) idx - 1 else list.size - 1
        playTrack(list[prevIdx])
    }

    fun seekTo(seconds: Int) {
        audioEngine.seekTo(seconds)
        _playbackSeconds.value = seconds
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _volumeLevel.value = clamped
        audioEngine.setVolume(clamped)
    }

    // Inline Quick Modifier & DSP Controls (100% Real-Time Wired & Persisted)
    fun setQuickBandGain(bandIndex: Int, gainDb: Float) {
        audioEngine.setQuickBandGain(bandIndex, gainDb)
    }

    fun getQuickBandGain(bandIndex: Int): Float = audioEngine.getQuickBandGain(bandIndex)

    fun applyQuickPreset(name: String) {
        audioEngine.applyQuickPreset(name)
    }

    private fun updateAndCommitDsp(updated: DspSettings) {
        _dspSettings.value = updated
        audioEngine.applyDspSettings(updated, _equalizerSettings.value)
        persistDspSettings(updated)
    }

    private fun updateAndCommitEq(updated: EqualizerSettings) {
        _equalizerSettings.value = updated
        audioEngine.applyDspSettings(_dspSettings.value, updated)
        persistEqSettings(updated)
    }

    fun toggleDspPower(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(dspMasterEnabled = enabled))
    }

    fun toggleDspMaster() {
        toggleDspPower(!_dspSettings.value.dspMasterEnabled)
    }

    fun setMasterGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(masterGainDb = gainDb.coerceIn(-24f, 18f)))
    }

    fun setDspPreampGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(preampDb = gainDb.coerceIn(-18f, 18f)))
    }

    fun setDspBassGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(bassGainDb = gainDb.coerceIn(-18f, 18f)))
    }

    fun setDspMidGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(midGainDb = gainDb.coerceIn(-18f, 18f)))
    }

    fun setDspTrebleGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(trebleGainDb = gainDb.coerceIn(-18f, 18f)))
    }

    fun setBassBoost(enabled: Boolean, boostDb: Float, freqHz: Float = _dspSettings.value.bassBoostFreqHz) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                bassBoostEnabled = enabled,
                bassBoostDb = boostDb.coerceIn(0f, 24f),
                bassBoostFreqHz = freqHz.coerceIn(30f, 90f)
            )
        )
    }

    fun setSubBass(enabled: Boolean, boostDb: Float, freqHz: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                subBassEnabled = enabled,
                subBassBoostDb = boostDb.coerceIn(0f, 18f),
                subBassFreqHz = freqHz.coerceIn(20f, 85f)
            )
        )
    }

    fun setSubsonicFilter(enabled: Boolean, freqHz: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                subsonicFilterActive = enabled,
                subsonicFrequencyHz = freqHz.coerceIn(15f, 50f)
            )
        )
    }

    fun setBassPunch(level: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(bassPunchLevel = level.coerceIn(0f, 10f)))
    }

    fun setBassPhase(degrees: Int) {
        updateAndCommitDsp(_dspSettings.value.copy(phaseDegrees = if (degrees == 180) 180 else 0))
    }

    fun setTimeAlignment(ms: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(timeAlignmentMs = ms.coerceIn(0f, 15f)))
    }

    fun applySubwooferPreset(preset: SubwooferPreset) {
        if (preset.isVip && !_isVipUnlocked.value) return
        updateAndCommitDsp(
            _dspSettings.value.copy(
                dspMasterEnabled = true,
                bassBoostEnabled = true,
                bassBoostDb = preset.bassBoostDb,
                bassBoostFreqHz = preset.subFreqHz,
                subBassEnabled = true,
                subBassBoostDb = preset.subBassDb,
                subBassFreqHz = preset.subFreqHz,
                subsonicFilterActive = true,
                subsonicFrequencyHz = preset.subsonicHz,
                lpfEnabled = true,
                lpfFrequencyHz = preset.lpfHz,
                lpfSlopeDb = if (preset.isVip) 36 else 24,
                bassPunchLevel = preset.punchLevel
            )
        )
    }

    fun setLoudness(enabled: Boolean, gainDb: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                loudnessEnabled = enabled,
                loudnessGainDb = gainDb.coerceIn(0f, 15f)
            )
        )
    }

    fun setHiResQuality(enabled: Boolean) {
        if (enabled && !_isVipUnlocked.value) return
        updateAndCommitDsp(_dspSettings.value.copy(hiResQualityEnabled = enabled))
    }

    fun setTubeWarmth(level: Float) {
        if (level > 0f && !_isVipUnlocked.value) return
        updateAndCommitDsp(_dspSettings.value.copy(tubeWarmthLevel = level.coerceIn(0f, 10f)))
    }

    fun setPresence(enabled: Boolean, level: Float) {
        if (enabled && !_isVipUnlocked.value) return
        updateAndCommitDsp(
            _dspSettings.value.copy(
                presenceEnabled = enabled,
                presenceLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun setClarity(enabled: Boolean, level: Float) {
        if (enabled && !_isVipUnlocked.value) return
        updateAndCommitDsp(
            _dspSettings.value.copy(
                clarityEnabled = enabled,
                clarityLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun setExciter(enabled: Boolean, level: Float) {
        if (enabled && !_isVipUnlocked.value) return
        updateAndCommitDsp(
            _dspSettings.value.copy(
                exciterEnabled = enabled,
                exciterLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun setCompressorParams(thresholdDb: Float, ratio: Float, attackMs: Float, releaseMs: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                compressorThresholdDb = thresholdDb.coerceIn(-48f, 0f),
                compressorRatio = ratio.coerceIn(1f, 20f),
                compressorAttackMs = attackMs.coerceIn(1f, 100f),
                compressorReleaseMs = releaseMs.coerceIn(15f, 1000f)
            )
        )
    }

    fun setCrossoverSlopes(hpfSlopeDb: Int, lpfSlopeDb: Int) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                hpfSlopeDb = hpfSlopeDb,
                lpfSlopeDb = lpfSlopeDb
            )
        )
    }

    fun setStereoWidth(percent: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(stereoWidthPercent = percent.coerceIn(100f, 250f)))
    }

    fun setBalancePan(pan: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(balancePan = pan.coerceIn(-1f, 1f)))
    }

    fun toggleCompressor(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(compressorEnabled = enabled))
    }

    fun toggleLimiter(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(limiterEnabled = enabled))
    }

    fun setLimiterCeiling(ceilingDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(limiterCeilingDb = ceilingDb.coerceIn(-24f, 0f)))
    }

    fun setFilterHpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                hpfEnabled = enabled,
                hpfFrequencyHz = freqHz.coerceIn(15f, 500f),
                hpfSlopeDb = slopeDb,
                hpfQ = q.coerceIn(0.5f, 2.0f)
            )
        )
    }

    fun setFilterLpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                lpfEnabled = enabled,
                lpfFrequencyHz = freqHz.coerceIn(50f, 20000f),
                lpfSlopeDb = slopeDb,
                lpfQ = q.coerceIn(0.5f, 2.0f)
            )
        )
    }

    fun resetDspToDefaults() {
        val defaultDsp = DspSettings()
        val defaultEq = EqualizerSettings(
            bands15 = List(15) { 0f },
            bands31 = List(31) { 0f },
            activePresetName = "Flat"
        )
        _dspSettings.value = defaultDsp
        _equalizerSettings.value = defaultEq
        audioEngine.applyDspSettings(defaultDsp, defaultEq)
        persistDspAndEq(defaultDsp, defaultEq)
    }

    // Equalizer Controls (Smooth 15-to-31 interpolation & live hardware update)
    fun toggleEqualizerPower(enabled: Boolean) {
        updateAndCommitEq(_equalizerSettings.value.copy(isEnabled = enabled))
    }

    fun updateEqualizerMasterGain(gainDb: Float) {
        updateAndCommitEq(_equalizerSettings.value.copy(masterGainDb = gainDb.coerceIn(-12f, 12f)))
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val current15 = _equalizerSettings.value.bands15.toMutableList()
        if (index !in current15.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current15[index] = clamped

        // Smooth interpolation across 31-band ISO frequencies
        val current31 = _equalizerSettings.value.bands31.toMutableList()
        val center31 = (index * 2).coerceIn(0, current31.lastIndex)
        current31[center31] = clamped
        if (center31 - 1 >= 0) {
            val prev15 = current15.getOrElse(index - 1) { clamped }
            current31[center31 - 1] = (clamped + prev15) * 0.5f
        }
        if (center31 + 1 <= current31.lastIndex) {
            val next15 = current15.getOrElse(index + 1) { clamped }
            current31[center31 + 1] = (clamped + next15) * 0.5f
        }
        if (index == 14 && current31.size > 30) {
            current31[30] = clamped
        }

        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands15 = current15,
                bands31 = current31,
                activePresetName = "Personalizado"
            )
        )
    }

    fun updateEqualizerBand31(index: Int, gainDb: Float) {
        val current = _equalizerSettings.value.bands31.toMutableList()
        if (index !in current.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current[index] = clamped

        val current15 = _equalizerSettings.value.bands15.toMutableList()
        val idx15 = (index / 2).coerceIn(0, current15.lastIndex)
        current15[idx15] = clamped

        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands31 = current,
                bands15 = current15,
                activePresetName = "Personalizado 31 Bandas"
            )
        )
    }

    fun updateParametricBand(bandId: Int, freqHz: Float, gainDb: Float, q: Float, type: ParametricFilterType, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) pb.copy(freqHz = freqHz, gainDb = gainDb, q = q, filterType = type, enabled = enabled) else pb
        }
        updateAndCommitEq(
            _equalizerSettings.value.copy(
                parametricBands = updated,
                activePresetName = "Paramétrico Editado"
            )
        )
    }

    fun toggleParametricBand(bandId: Int, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) pb.copy(enabled = enabled) else pb
        }
        updateAndCommitEq(_equalizerSettings.value.copy(parametricBands = updated))
    }

    fun selectPreset(preset: EqPresetItem) {
        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands15 = preset.bands,
                bands31 = if (preset.bands31.isNotEmpty()) preset.bands31 else _equalizerSettings.value.bands31,
                parametricFreqHz = preset.paramFreq,
                parametricGainDb = preset.paramGain,
                parametricQ = preset.paramQ,
                activePresetName = preset.name,
                isEnabled = true
            )
        )
    }

    fun selectEqualizerPreset(presetName: String) {
        _eqPresets.value.firstOrNull { it.name == presetName }?.let { selectPreset(it) }
    }

    fun saveCustomPreset(name: String, description: String) {
        val newPreset = EqPresetItem(
            name = name,
            description = description.ifBlank { "Preset guardado por el usuario" },
            bands = _equalizerSettings.value.bands15,
            bands31 = _equalizerSettings.value.bands31,
            paramFreq = _equalizerSettings.value.parametricFreqHz,
            paramGain = _equalizerSettings.value.parametricGainDb,
            paramQ = _equalizerSettings.value.parametricQ,
            isCustom = true
        )
        val updatedList = _eqPresets.value.filter { it.name != name } + newPreset
        _eqPresets.value = updatedList
        persistCustomPresets(updatedList)
        updateAndCommitEq(_equalizerSettings.value.copy(activePresetName = name))
    }

    fun deletePreset(preset: EqPresetItem) {
        val updatedList = _eqPresets.value.filter { it.name != preset.name }
        _eqPresets.value = updatedList
        persistCustomPresets(updatedList)
        if (_equalizerSettings.value.activePresetName == preset.name) {
            selectPreset(EqPresetCatalog.presets.first())
        }
    }

    fun resetEqualizerFlat() {
        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands15 = List(15) { 0.0f },
                bands31 = List(31) { 0.0f },
                masterGainDb = 0.0f,
                parametricGainDb = 0.0f,
                activePresetName = "Flat"
            )
        )
    }

    fun toggleMicRta(hasRecordPermission: Boolean) {
        if (!hasRecordPermission) return
        if (audioEngine.isMicRtaActive) {
            audioEngine.stopMicRta()
            _isMicRta.value = false
        } else {
            audioEngine.startMicRta(true)
            _isMicRta.value = true
        }
        audioEngine.setVisualizerActive(uiActive && (_selectedTab.value == AppTab.DSP || _isMicRta.value))
    }

    // ==========================================
    // Lightweight Async SharedPreferences Persistence
    // ==========================================
    private fun loadSavedTheme(): CarAudioThemeType {
        val savedId = prefs.getString("selected_theme_id", CarAudioThemeType.CRIMSON_RED.id)
        val found = CarAudioThemeType.entries.firstOrNull { it.id == savedId } ?: CarAudioThemeType.CRIMSON_RED
        val vipUnlocked = prefs.getBoolean("vip_unlocked", false)
        return if (found.isVipOnly && !vipUnlocked) CarAudioThemeType.CRIMSON_RED else found
    }

    private fun loadSavedRepeatMode(): RepeatMode {
        val raw = prefs.getString("repeat_mode", RepeatMode.ALL.name)
        return try {
            RepeatMode.valueOf(raw ?: RepeatMode.ALL.name)
        } catch (_: Throwable) {
            RepeatMode.ALL
        }
    }

    private fun loadFavoriteIds(): Set<String> {
        return prefs.getStringSet("favorite_track_ids", emptySet()) ?: emptySet()
    }

    private fun loadSavedDspSettings(): DspSettings {
        val raw = prefs.getString("saved_dsp_json", null) ?: return DspSettings()
        return try {
            val o = JSONObject(raw)
            val vipUnlocked = prefs.getBoolean("vip_unlocked", false)
            DspSettings(
                dspMasterEnabled = o.optBoolean("master", true),
                preampDb = o.optDouble("preamp", 0.0).toFloat(),
                bassGainDb = o.optDouble("bass", 2.0).toFloat(),
                midGainDb = o.optDouble("mid", 0.0).toFloat(),
                trebleGainDb = o.optDouble("treble", 1.5).toFloat(),
                masterGainDb = o.optDouble("masterGain", 0.0).toFloat(),
                bassBoostEnabled = o.optBoolean("bbEn", true),
                bassBoostDb = o.optDouble("bbDb", 4.0).toFloat(),
                bassBoostFreqHz = o.optDouble("bbHz", 45.0).toFloat(),
                subBassEnabled = o.optBoolean("subEn", false),
                subBassBoostDb = o.optDouble("subDb", 3.0).toFloat(),
                subBassFreqHz = o.optDouble("subHz", 35.0).toFloat(),
                subsonicFilterActive = o.optBoolean("subsonicAct", false),
                subsonicFrequencyHz = o.optDouble("subsonicHz", 28.0).toFloat(),
                bassPunchLevel = o.optDouble("punch", 0.0).toFloat(),
                loudnessEnabled = o.optBoolean("loudEn", false),
                loudnessGainDb = o.optDouble("loudDb", 4.0).toFloat(),
                phaseDegrees = o.optInt("phase", 0),
                timeAlignmentMs = o.optDouble("alignMs", 1.2).toFloat(),
                stereoWidthPercent = o.optDouble("stereo", 100.0).toFloat(),
                balancePan = o.optDouble("pan", 0.0).toFloat(),
                compressorEnabled = o.optBoolean("comp", false),
                limiterEnabled = o.optBoolean("lim", true),
                limiterCeilingDb = o.optDouble("limCeil", -0.5).toFloat(),
                hpfEnabled = o.optBoolean("hpfEn", true),
                hpfFrequencyHz = o.optDouble("hpfHz", 25.0).toFloat(),
                hpfSlopeDb = o.optInt("hpfSlope", 24),
                lpfEnabled = o.optBoolean("lpfEn", true),
                lpfFrequencyHz = o.optDouble("lpfHz", 18000.0).toFloat(),
                lpfSlopeDb = o.optInt("lpfSlope", 24),
                hiResQualityEnabled = vipUnlocked && o.optBoolean("hiRes", false),
                tubeWarmthLevel = if (vipUnlocked) o.optDouble("tube", 0.0).toFloat() else 0f,
                presenceEnabled = vipUnlocked && o.optBoolean("presEn", false),
                presenceLevel = o.optDouble("presLvl", 2.0).toFloat(),
                clarityEnabled = vipUnlocked && o.optBoolean("clarEn", false),
                clarityLevel = o.optDouble("clarLvl", 2.5).toFloat(),
                exciterEnabled = vipUnlocked && o.optBoolean("excEn", false),
                exciterLevel = o.optDouble("excLvl", 3.0).toFloat()
            )
        } catch (_: Throwable) {
            DspSettings()
        }
    }

    private fun persistDspSettings(dsp: DspSettings) {
        try {
            val o = JSONObject().apply {
                put("master", dsp.dspMasterEnabled)
                put("preamp", dsp.preampDb.toDouble())
                put("bass", dsp.bassGainDb.toDouble())
                put("mid", dsp.midGainDb.toDouble())
                put("treble", dsp.trebleGainDb.toDouble())
                put("masterGain", dsp.masterGainDb.toDouble())
                put("bbEn", dsp.bassBoostEnabled)
                put("bbDb", dsp.bassBoostDb.toDouble())
                put("bbHz", dsp.bassBoostFreqHz.toDouble())
                put("subEn", dsp.subBassEnabled)
                put("subDb", dsp.subBassBoostDb.toDouble())
                put("subHz", dsp.subBassFreqHz.toDouble())
                put("subsonicAct", dsp.subsonicFilterActive)
                put("subsonicHz", dsp.subsonicFrequencyHz.toDouble())
                put("punch", dsp.bassPunchLevel.toDouble())
                put("loudEn", dsp.loudnessEnabled)
                put("loudDb", dsp.loudnessGainDb.toDouble())
                put("phase", dsp.phaseDegrees)
                put("alignMs", dsp.timeAlignmentMs.toDouble())
                put("stereo", dsp.stereoWidthPercent.toDouble())
                put("pan", dsp.balancePan.toDouble())
                put("comp", dsp.compressorEnabled)
                put("lim", dsp.limiterEnabled)
                put("limCeil", dsp.limiterCeilingDb.toDouble())
                put("hpfEn", dsp.hpfEnabled)
                put("hpfHz", dsp.hpfFrequencyHz.toDouble())
                put("hpfSlope", dsp.hpfSlopeDb)
                put("lpfEn", dsp.lpfEnabled)
                put("lpfHz", dsp.lpfFrequencyHz.toDouble())
                put("lpfSlope", dsp.lpfSlopeDb)
                put("hiRes", dsp.hiResQualityEnabled)
                put("tube", dsp.tubeWarmthLevel.toDouble())
                put("presEn", dsp.presenceEnabled)
                put("presLvl", dsp.presenceLevel.toDouble())
                put("clarEn", dsp.clarityEnabled)
                put("clarLvl", dsp.clarityLevel.toDouble())
                put("excEn", dsp.exciterEnabled)
                put("excLvl", dsp.exciterLevel.toDouble())
            }
            prefs.edit().putString("saved_dsp_json", o.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun loadSavedEqualizerSettings(): EqualizerSettings {
        val raw = prefs.getString("saved_eq_json", null) ?: return EqualizerSettings()
        return try {
            val o = JSONObject(raw)
            val arr31 = o.optJSONArray("b31")
            val b31 = if (arr31 != null && arr31.length() == 31) {
                List(31) { arr31.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings().bands31
            val arr15 = o.optJSONArray("b15")
            val b15 = if (arr15 != null && arr15.length() == 15) {
                List(15) { arr15.optDouble(it, 0.0).toFloat() }
            } else EqualizerSettings().bands15
            EqualizerSettings(
                isEnabled = o.optBoolean("en", true),
                masterGainDb = o.optDouble("mg", 0.0).toFloat(),
                bands31 = b31,
                bands15 = b15,
                activePresetName = o.optString("preset", "Personalizado")
            )
        } catch (_: Throwable) {
            EqualizerSettings()
        }
    }

    private fun persistEqSettings(eq: EqualizerSettings) {
        try {
            val o = JSONObject().apply {
                put("en", eq.isEnabled)
                put("mg", eq.masterGainDb.toDouble())
                put("preset", eq.activePresetName)
                put("b31", JSONArray(eq.bands31.map { it.toDouble() }))
                put("b15", JSONArray(eq.bands15.map { it.toDouble() }))
            }
            prefs.edit().putString("saved_eq_json", o.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun persistDspAndEq(dsp: DspSettings, eq: EqualizerSettings) {
        persistDspSettings(dsp)
        persistEqSettings(eq)
    }

    private fun loadSavedPresetsCatalog(): List<EqPresetItem> {
        val raw = prefs.getString("saved_custom_presets", null) ?: return EqPresetCatalog.presets
        return try {
            val arr = JSONArray(raw)
            val customList = ArrayList<EqPresetItem>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val b15Arr = o.optJSONArray("b15")
                val b31Arr = o.optJSONArray("b31")
                if (b15Arr != null && b15Arr.length() == 15) {
                    customList.add(
                        EqPresetItem(
                            name = o.getString("name"),
                            description = o.optString("desc", "Preset personalizado"),
                            bands = List(15) { b15Arr.optDouble(it, 0.0).toFloat() },
                            bands31 = if (b31Arr != null && b31Arr.length() == 31) {
                                List(31) { b31Arr.optDouble(it, 0.0).toFloat() }
                            } else emptyList(),
                            isCustom = true
                        )
                    )
                }
            }
            val builtInNames = EqPresetCatalog.presets.map { it.name }.toSet()
            EqPresetCatalog.presets + customList.filter { it.name !in builtInNames }
        } catch (_: Throwable) {
            EqPresetCatalog.presets
        }
    }

    private fun persistCustomPresets(allPresets: List<EqPresetItem>) {
        try {
            val builtInNames = EqPresetCatalog.presets.map { it.name }.toSet()
            val arr = JSONArray()
            allPresets.filter { it.isCustom && it.name !in builtInNames }.forEach { p ->
                arr.put(
                    JSONObject().apply {
                        put("name", p.name)
                        put("desc", p.description)
                        put("b15", JSONArray(p.bands.map { it.toDouble() }))
                        put("b31", JSONArray(p.bands31.map { it.toDouble() }))
                    }
                )
            }
            prefs.edit().putString("saved_custom_presets", arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.onSettingsChangedListener = null
        audioEngine.onTrackCompletedListener = null
        audioEngine.onPlaybackStateChangedListener = null
        audioEngine.onOutputDeviceChangedListener = null
        CarAudioPlaybackService.onSkipNextRequested = null
        CarAudioPlaybackService.onSkipPrevRequested = null
        audioEngine.setVisualizerActive(false)
        if (!audioEngine.isPlaying) {
            audioEngine.stopMicRta()
        }
    }
}
