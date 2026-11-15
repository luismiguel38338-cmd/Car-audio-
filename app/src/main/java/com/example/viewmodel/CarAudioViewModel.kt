package com.example.viewmodel

import android.Manifest
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
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType
import com.example.model.RtaBand
import com.example.model.TrackCategory
import com.example.notification.CarAudioNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class AppTab(val title: String) {
    PLAYER("MÚSICA"),
    EQ("EQ"),
    DSP("DSP"),
    AJUSTES("AJUSTES")
}

class CarAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val audioEngine = CarAudioEngine.getInstance(context)
    private val prefs = application.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE)

    val isIntroEnabledPref: Boolean
        get() = prefs.getBoolean("intro_video_enabled", true)
    val isDontShowAgainPref: Boolean
        get() = prefs.getBoolean("dont_show_again", false)

    private val _showSplashScreen = MutableStateFlow(isIntroEnabledPref && !isDontShowAgainPref)
    val showSplashScreen: StateFlow<Boolean> = _showSplashScreen.asStateFlow()

    private val _autoPlayIntro = MutableStateFlow(isIntroEnabledPref)
    val autoPlayIntro: StateFlow<Boolean> = _autoPlayIntro.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean("keep_screen_on", true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    // Defaults to Crimson Studio Red ("Todo Rojo")
    private val _currentTheme = MutableStateFlow(CarAudioThemeType.CRIMSON_RED)
    val currentTheme: StateFlow<CarAudioThemeType> = _currentTheme.asStateFlow()

    // Opens directly into MÚSICA (in-panel phone library + live modifier)
    private val _selectedTab = MutableStateFlow(AppTab.PLAYER)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _dspSettings = MutableStateFlow(DspSettings())
    val dspSettings: StateFlow<DspSettings> = _dspSettings.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _eqPresets = MutableStateFlow(EqPresetCatalog.presets)
    val eqPresets: StateFlow<List<EqPresetItem>> = _eqPresets.asStateFlow()

    // Phone Music Library & Loaded Files
    private val _tracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(null)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSeconds = MutableStateFlow(0)
    val playbackSeconds: StateFlow<Int> = _playbackSeconds.asStateFlow()

    private val _volumeLevel = MutableStateFlow(0.85f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _isScanningLibrary = MutableStateFlow(false)
    val isScanningLibrary: StateFlow<Boolean> = _isScanningLibrary.asStateFlow()

    private val _hasAudioLibraryPermission = MutableStateFlow(checkAudioStoragePermission())
    val hasAudioLibraryPermission: StateFlow<Boolean> = _hasAudioLibraryPermission.asStateFlow()

    // Real-time Meter & Spectrum Telemetry
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

    private var lastClipAlertTime = 0L
    private var clipCount = 0

    init {
        try {
            CarAudioNotificationHelper.initNotificationChannels(context)
        } catch (_: Throwable) {}

        audioEngine.onSettingsChangedListener = { updatedDsp, updatedEq ->
            _dspSettings.value = updatedDsp
            _equalizerSettings.value = updatedEq
        }

        audioEngine.onTrackCompletedListener = {
            nextTrack()
        }

        viewModelScope.launch {
            try {
                audioEngine.attachToGlobalSystemSession(_dspSettings.value, _equalizerSettings.value)
            } catch (_: Throwable) {}
            scanDeviceMusicLibrary()
        }

        startTelemetryLoop()
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
        _hasAudioLibraryPermission.value = granted || checkAudioStoragePermission()
        scanDeviceMusicLibrary()
    }

    fun scanDeviceMusicLibrary() {
        viewModelScope.launch {
            _isScanningLibrary.value = true
            val hasPerm = checkAudioStoragePermission()
            _hasAudioLibraryPermission.value = hasPerm

            val combined = withContext(Dispatchers.IO) {
                val result = mutableListOf<AudioTrackItem>()
                val seenUris = HashSet<String>()

                // 1. Load saved user-picked URIs from SharedPreferences
                loadSavedCustomTracks().forEach { track ->
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
                            MediaStore.Audio.Media.IS_MUSIC
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

                                result.add(
                                    AudioTrackItem(
                                        id = "media_$id",
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        durationSeconds = durationSec,
                                        category = TrackCategory.USER_CUSTOM,
                                        frequencyDescription = "Música del Teléfono • DSP Activo",
                                        isSynthesized = false,
                                        customUri = contentUri
                                    )
                                )
                            }
                        }
                    } catch (_: Throwable) {}
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

    private fun loadSavedCustomTracks(): List<AudioTrackItem> {
        val raw = prefs.getString("saved_custom_tracks", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AudioTrackItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val uriStr = obj.optString("uri", "")
                if (uriStr.isBlank()) continue
                val uri = Uri.parse(uriStr)
                // Verify we still can open or query the URI
                val accessible = try {
                    context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
                } catch (_: Throwable) {
                    false
                }
                if (accessible) {
                    list.add(
                        AudioTrackItem(
                            id = obj.optString("id", "saved_$i"),
                            title = obj.optString("title", "Audio Local"),
                            artist = obj.optString("artist", "Archivo Cargado"),
                            album = obj.optString("album", "Dispositivo"),
                            durationSeconds = obj.optInt("duration", 180),
                            category = TrackCategory.USER_CUSTOM,
                            frequencyDescription = "Archivo Cargado • DSP en Vivo",
                            isSynthesized = false,
                            customUri = uri
                        )
                    )
                }
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

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let { title = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let { artist = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let { album = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull()?.let { ms ->
                if (ms > 0) durationSec = ms / 1000
            }
            retriever.release()
        } catch (_: Throwable) {}

        return AudioTrackItem(
            id = "user_${uri.toString().hashCode()}",
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSec,
            category = TrackCategory.USER_CUSTOM,
            frequencyDescription = "Audio Cargado • Modificación en Vivo",
            isSynthesized = false,
            customUri = uri
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
            var tick = 0
            while (isActive) {
                _isPlaying.value = audioEngine.isPlaying
                audioEngine.currentTrack?.let { _currentTrack.value = it }
                _playbackSeconds.value = audioEngine.playbackPositionSeconds
                _splDb.value = audioEngine.currentSplDb
                _leftVu.value = audioEngine.leftVuLevel
                _rightVu.value = audioEngine.rightVuLevel
                _rtaBands.value = audioEngine.currentRtaBands
                _isMicRta.value = audioEngine.isMicRtaActive
                _sourceMode.value = audioEngine.sourceMode

                val playing = _isPlaying.value
                val dsp = _dspSettings.value
                val totalGain = dsp.masterGainDb + dsp.preampDb + dsp.bassBoostDb
                val clipCondition = audioEngine.isClippingDetected || (playing && totalGain > 22f && _leftVu.value > 0.9f)
                _isClippingDetected.value = clipCondition
                if (clipCondition) clipCount++

                val now = System.currentTimeMillis()
                if (clipCondition && (now - lastClipAlertTime > 10000)) {
                    lastClipAlertTime = now
                    CarAudioNotificationHelper.showClippingAlertNotification(context, clipCount)
                }

                if (tick % 40 == 0 && playing) {
                    CarAudioNotificationHelper.showMonitorNotification(
                        context = context,
                        trackTitle = _currentTrack.value?.title ?: "CAR AUDIO DSP PRO",
                        isPlaying = true,
                        isClipping = clipCondition
                    )
                }

                tick++
                delay(100)
            }
        }
    }

    // Navigation & Preferences
    fun dismissSplashScreen() { _showSplashScreen.value = false }
    fun openSplashScreen() {
        _showSplashScreen.value = true
        playStartupSound()
    }
    fun playStartupSound() { audioEngine.playIntroBootSound() }
    fun setTheme(theme: CarAudioThemeType) { _currentTheme.value = theme }
    fun selectTab(tab: AppTab) { _selectedTab.value = tab }
    fun setAutoPlayIntro(enabled: Boolean) {
        _autoPlayIntro.value = enabled
        prefs.edit().putBoolean("intro_video_enabled", enabled).apply()
    }
    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        prefs.edit().putBoolean("keep_screen_on", enabled).apply()
    }

    // Playback controls
    fun playTrack(track: AudioTrackItem) {
        audioEngine.playTrack(track, _dspSettings.value, _equalizerSettings.value)
        _currentTrack.value = track
        _isPlaying.value = true
        CarAudioNotificationHelper.showMonitorNotification(
            context = context,
            trackTitle = track.title,
            isPlaying = true,
            isClipping = _isClippingDetected.value
        )
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: _tracks.value.firstOrNull() ?: return
        if (_currentTrack.value == null) {
            playTrack(track)
            return
        }
        audioEngine.togglePlayPause(_dspSettings.value, _equalizerSettings.value)
        _isPlaying.value = audioEngine.isPlaying
        CarAudioNotificationHelper.showMonitorNotification(
            context = context,
            trackTitle = track.title,
            isPlaying = _isPlaying.value,
            isClipping = _isClippingDetected.value
        )
    }

    fun nextTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        val current = _currentTrack.value
        val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
        val nextIdx = if (idx >= 0 && idx < list.size - 1) idx + 1 else 0
        playTrack(list[nextIdx])
    }

    fun previousTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
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

    // Inline Quick Modifier & DSP Controls (100% Real-Time Wired)
    fun setQuickBandGain(bandIndex: Int, gainDb: Float) {
        audioEngine.setQuickBandGain(bandIndex, gainDb)
    }

    fun getQuickBandGain(bandIndex: Int): Float = audioEngine.getQuickBandGain(bandIndex)

    fun applyQuickPreset(name: String) {
        audioEngine.applyQuickPreset(name)
    }

    fun toggleDspPower(enabled: Boolean) {
        _dspSettings.value = _dspSettings.value.copy(dspMasterEnabled = enabled)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleDspMaster() {
        toggleDspPower(!_dspSettings.value.dspMasterEnabled)
    }

    fun setMasterGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(masterGainDb = gainDb.coerceIn(-24f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setDspPreampGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(preampDb = gainDb.coerceIn(-18f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setDspBassGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(bassGainDb = gainDb.coerceIn(-18f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setDspMidGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(midGainDb = gainDb.coerceIn(-18f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setDspTrebleGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(trebleGainDb = gainDb.coerceIn(-18f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setBassBoost(enabled: Boolean, boostDb: Float, freqHz: Float = _dspSettings.value.bassBoostFreqHz) {
        _dspSettings.value = _dspSettings.value.copy(
            bassBoostEnabled = enabled,
            bassBoostDb = boostDb.coerceIn(0f, 24f),
            bassBoostFreqHz = freqHz
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setSubBass(enabled: Boolean, boostDb: Float, freqHz: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            subBassEnabled = enabled,
            subBassBoostDb = boostDb.coerceIn(0f, 18f),
            subBassFreqHz = freqHz
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setLoudness(enabled: Boolean, gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            loudnessEnabled = enabled,
            loudnessGainDb = gainDb.coerceIn(0f, 15f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setStereoWidth(percent: Float) {
        _dspSettings.value = _dspSettings.value.copy(stereoWidthPercent = percent.coerceIn(0f, 250f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setBalancePan(pan: Float) {
        _dspSettings.value = _dspSettings.value.copy(balancePan = pan.coerceIn(-1f, 1f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleCompressor(enabled: Boolean) {
        _dspSettings.value = _dspSettings.value.copy(compressorEnabled = enabled)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleLimiter(enabled: Boolean) {
        _dspSettings.value = _dspSettings.value.copy(limiterEnabled = enabled)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setLimiterCeiling(ceilingDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(limiterCeilingDb = ceilingDb.coerceIn(-24f, 0f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setFilterHpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            hpfEnabled = enabled,
            hpfFrequencyHz = freqHz.coerceIn(15f, 500f),
            hpfSlopeDb = slopeDb,
            hpfQ = q.coerceIn(0.5f, 2.0f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setFilterLpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            lpfEnabled = enabled,
            lpfFrequencyHz = freqHz.coerceIn(50f, 20000f),
            lpfSlopeDb = slopeDb,
            lpfQ = q.coerceIn(0.5f, 2.0f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun resetDspToDefaults() {
        _dspSettings.value = DspSettings()
        _equalizerSettings.value = EqualizerSettings(
            bands15 = List(15) { 0f },
            bands31 = List(31) { 0f },
            activePresetName = "Flat"
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    // Equalizer Controls (Every edit triggers live hardware AudioFX update!)
    fun toggleEqualizerPower(enabled: Boolean) {
        _equalizerSettings.value = _equalizerSettings.value.copy(isEnabled = enabled)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun updateEqualizerMasterGain(gainDb: Float) {
        _equalizerSettings.value = _equalizerSettings.value.copy(masterGainDb = gainDb.coerceIn(-12f, 12f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val current15 = _equalizerSettings.value.bands15.toMutableList()
        if (index !in current15.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current15[index] = clamped

        // Sync smoothly into bands31 so hardware EQ & response curve update immediately
        val current31 = _equalizerSettings.value.bands31.toMutableList()
        val start31 = (index * 2).coerceIn(0, current31.lastIndex)
        val end31 = (start31 + 1).coerceIn(0, current31.lastIndex)
        current31[start31] = clamped
        current31[end31] = clamped
        if (index == 14 && current31.size > 30) {
            current31[30] = clamped
        }

        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands15 = current15,
            bands31 = current31,
            activePresetName = "Personalizado"
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun updateEqualizerBand31(index: Int, gainDb: Float) {
        val current = _equalizerSettings.value.bands31.toMutableList()
        if (index !in current.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current[index] = clamped

        val current15 = _equalizerSettings.value.bands15.toMutableList()
        val idx15 = (index / 2).coerceIn(0, current15.lastIndex)
        current15[idx15] = clamped

        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands31 = current,
            bands15 = current15,
            activePresetName = "Personalizado 31 Bandas"
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun updateParametricBand(bandId: Int, freqHz: Float, gainDb: Float, q: Float, type: ParametricFilterType, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) pb.copy(freqHz = freqHz, gainDb = gainDb, q = q, filterType = type, enabled = enabled) else pb
        }
        _equalizerSettings.value = _equalizerSettings.value.copy(
            parametricBands = updated,
            activePresetName = "Paramétrico Editado"
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleParametricBand(bandId: Int, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) pb.copy(enabled = enabled) else pb
        }
        _equalizerSettings.value = _equalizerSettings.value.copy(parametricBands = updated)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun selectPreset(preset: EqPresetItem) {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands15 = preset.bands,
            bands31 = if (preset.bands31.isNotEmpty()) preset.bands31 else _equalizerSettings.value.bands31,
            parametricFreqHz = preset.paramFreq,
            parametricGainDb = preset.paramGain,
            parametricQ = preset.paramQ,
            activePresetName = preset.name,
            isEnabled = true
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
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
        _eqPresets.value = _eqPresets.value.filter { it.name != name } + newPreset
        _equalizerSettings.value = _equalizerSettings.value.copy(activePresetName = name)
    }

    fun deletePreset(preset: EqPresetItem) {
        _eqPresets.value = _eqPresets.value.filter { it.name != preset.name }
        if (_equalizerSettings.value.activePresetName == preset.name) {
            selectPreset(EqPresetCatalog.presets.first())
        }
    }

    fun resetEqualizerFlat() {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands15 = List(15) { 0.0f },
            bands31 = List(31) { 0.0f },
            masterGainDb = 0.0f,
            parametricGainDb = 0.0f,
            activePresetName = "Flat"
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
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
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
