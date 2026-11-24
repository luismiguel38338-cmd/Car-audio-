package com.example.viewmodel

import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.audio.CarAudioEngine
import com.example.data.FirebaseUserRepository
import com.example.data.LocalPreferencesStore
import com.example.downloads.DownloadManagerRepository
import com.example.model.AbSlot
import com.example.model.AlbumSummary
import com.example.model.AppSection
import com.example.model.ArtistSummary
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.CloudPlaylist
import com.example.model.CloudSoundPreset
import com.example.model.CloudUserProfile
import com.example.model.CustomSoundProfile
import com.example.model.DevicePowerTelemetry
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.DspAbSnapshot
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.EqualizerSettings
import com.example.model.FaultActionType
import com.example.model.FaultSeverity
import com.example.model.FolderSummary
import com.example.model.LibrarySortOrder
import com.example.model.LocalProfileStats
import com.example.model.MusicPlaylist
import com.example.model.OnlineSearchState
import com.example.model.OnlineTrackItem
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.ParametricFilterType
import com.example.model.RepeatMode
import com.example.model.RtaBand
import com.example.model.SplWeighting
import com.example.model.SystemFaultAlert
import com.example.model.TrackCategory
import com.example.model.YouTubeMusicItem
import com.example.notification.CarAudioNotificationHelper
import com.example.online.JamendoMusicProvider
import com.example.online.OnlineCatalogResult
import com.example.online.OnlineMusicProvider
import com.example.service.CarAudioPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

enum class LibraryFilter(val label: String) {
    ALL("Todas"),
    FAVORITES("Favoritas"),
    LOADED("Importadas")
}

class CarAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val localStore = LocalPreferencesStore(context)
    private val audioEngine = CarAudioEngine.getInstance(context)
    private val onlineMusicProvider: OnlineMusicProvider = JamendoMusicProvider()
    private val downloadRepository = DownloadManagerRepository(context, viewModelScope)

    private val systemLowRam: Boolean = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.isLowRamDevice == true
    } catch (_: Throwable) {
        false
    }

    val isLowRamDevice: Boolean = systemLowRam

    val isIntroEnabledPref: Boolean
        get() = localStore.introEnabled
    val isDontShowAgainPref: Boolean
        get() = localStore.dontShowIntroAgain

    private val _showSplashScreen = MutableStateFlow(
        localStore.introEnabled && !localStore.dontShowIntroAgain
    )
    val showSplashScreen: StateFlow<Boolean> = _showSplashScreen.asStateFlow()

    private val _autoPlayIntro = MutableStateFlow(localStore.introEnabled)
    val autoPlayIntro: StateFlow<Boolean> = _autoPlayIntro.asStateFlow()

    // Full unlocked automotive studio state (no mandatory login or VIP paywall)
    private val _isVipUnlocked = MutableStateFlow(true)
    val isVipUnlocked: StateFlow<Boolean> = _isVipUnlocked.asStateFlow()

    private val _vipPromoDismissed = MutableStateFlow(true)
    val vipPromoDismissed: StateFlow<Boolean> = _vipPromoDismissed.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(localStore.keepScreenOn)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _floatingEqOnLeave = MutableStateFlow(localStore.floatingEqOnLeave)
    val floatingEqOnLeave: StateFlow<Boolean> = _floatingEqOnLeave.asStateFlow()

    private val _drivingModeEnabled = MutableStateFlow(localStore.drivingModeEnabled)
    val drivingModeEnabled: StateFlow<Boolean> = _drivingModeEnabled.asStateFlow()

    private val _drivingKeepScreenOn = MutableStateFlow(localStore.drivingKeepScreenOn)
    val drivingKeepScreenOn: StateFlow<Boolean> = _drivingKeepScreenOn.asStateFlow()

    private val _autoProfileByOutput = MutableStateFlow(localStore.autoProfileByOutput)
    val autoProfileByOutput: StateFlow<Boolean> = _autoProfileByOutput.asStateFlow()

    private val _savedOutputProfileKinds = MutableStateFlow(localStore.loadSavedOutputProfileKinds())
    val savedOutputProfileKinds: StateFlow<Set<OutputKind>> = _savedOutputProfileKinds.asStateFlow()

    private val _splWeighting = MutableStateFlow(localStore.loadSplWeighting())
    val splWeighting: StateFlow<SplWeighting> = _splWeighting.asStateFlow()

    private val _liteVisualsEnabled = MutableStateFlow(localStore.getLiteVisualsEnabled(systemLowRam))
    val liteVisualsEnabled: StateFlow<Boolean> = _liteVisualsEnabled.asStateFlow()

    private val _currentTheme = MutableStateFlow(localStore.loadTheme(true))
    val currentTheme: StateFlow<CarAudioThemeType> = _currentTheme.asStateFlow()

    private val _selectedSection = MutableStateFlow(
        if (localStore.drivingModeEnabled) AppSection.DRIVING_MODE else AppSection.HOME
    )
    val selectedSection: StateFlow<AppSection> = _selectedSection.asStateFlow()

    private val _dspSettings = MutableStateFlow(localStore.loadDspSettings(true))
    val dspSettings: StateFlow<DspSettings> = _dspSettings.asStateFlow()

    private val _bassMode = MutableStateFlow(localStore.loadBassMode())
    val bassMode: StateFlow<BassMode> = _bassMode.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(localStore.loadEqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _eqPresets = MutableStateFlow(localStore.loadPresetsCatalog())
    val eqPresets: StateFlow<List<EqPresetItem>> = _eqPresets.asStateFlow()

    private val _customSoundProfiles = MutableStateFlow(localStore.loadCustomSoundProfiles(true))
    val customSoundProfiles: StateFlow<List<CustomSoundProfile>> = _customSoundProfiles.asStateFlow()

    // Live A/B Comparison Snapshots
    private val _abSlotA = MutableStateFlow(localStore.loadAbSnapshot(AbSlot.A))
    val abSlotA: StateFlow<DspAbSnapshot?> = _abSlotA.asStateFlow()

    private val _abSlotB = MutableStateFlow(localStore.loadAbSnapshot(AbSlot.B))
    val abSlotB: StateFlow<DspAbSnapshot?> = _abSlotB.asStateFlow()

    private val _activeAbSlot = MutableStateFlow(localStore.loadActiveAbSlot())
    val activeAbSlot: StateFlow<AbSlot?> = _activeAbSlot.asStateFlow()

    // Online Music (Jamendo API v3) & Offline Downloads
    private val _onlineSearchState = MutableStateFlow(
        OnlineSearchState(
            isConfigured = onlineMusicProvider.isConfigured(),
            providerStatusMessage = if (onlineMusicProvider.isConfigured()) {
                "Conectado a Jamendo API v3 (catálogo legal Creative Commons)"
            } else {
                "Configura JAMENDO_CLIENT_ID en Secrets para habilitar música online y descargas autorizadas."
            }
        )
    )
    val onlineSearchState: StateFlow<OnlineSearchState> = _onlineSearchState.asStateFlow()

    val downloads: StateFlow<List<DownloadItem>> = downloadRepository.downloads
    val storageUsedBytes: StateFlow<Long> = downloadRepository.storageUsedBytes

    // Local Profile & Listening Stats
    private val _localProfileName = MutableStateFlow(localStore.localProfileName)
    val localProfileName: StateFlow<String> = _localProfileName.asStateFlow()

    private val _totalListeningSeconds = MutableStateFlow(localStore.totalListeningSeconds)
    val totalListeningSeconds: StateFlow<Long> = _totalListeningSeconds.asStateFlow()

    // YouTube Music Integration (Compliant Deep-Links, Shortcuts & Search History)
    private val _ytSearchQuery = MutableStateFlow("")
    val ytSearchQuery: StateFlow<String> = _ytSearchQuery.asStateFlow()

    private val _ytSavedItems = MutableStateFlow(localStore.loadYouTubeMusicItems())
    val ytSavedItems: StateFlow<List<YouTubeMusicItem>> = _ytSavedItems.asStateFlow()

    private val _ytSearchHistory = MutableStateFlow(localStore.loadYouTubeSearchHistory())
    val ytSearchHistory: StateFlow<List<String>> = _ytSearchHistory.asStateFlow()

    // Music Library, Queue, Playlists, Search & Filters
    private val _favoriteIds = MutableStateFlow(localStore.loadFavoriteIds())
    private val _tracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    private val _queueKeys = MutableStateFlow(localStore.loadQueueKeys())
    private val _queueTracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val queueTracks: StateFlow<List<AudioTrackItem>> = _queueTracks.asStateFlow()

    private val _playlists = MutableStateFlow(localStore.loadPlaylists())
    val playlists: StateFlow<List<MusicPlaylist>> = _playlists.asStateFlow()

    private val _selectedPlaylistId = MutableStateFlow<String?>(null)
    val selectedPlaylistId: StateFlow<String?> = _selectedPlaylistId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _libraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val libraryFilter: StateFlow<LibraryFilter> = _libraryFilter.asStateFlow()

    private val _librarySortOrder = MutableStateFlow(localStore.loadSortOrder())
    val librarySortOrder: StateFlow<LibrarySortOrder> = _librarySortOrder.asStateFlow()

    private val _recentTrackKeys = MutableStateFlow(localStore.loadRecentTrackKeys())
    private val _trackPlayCounts = MutableStateFlow(localStore.loadPlayCounts())
    val trackPlayCounts: StateFlow<Map<String, Int>> = _trackPlayCounts.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(localStore.playbackSpeed)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(localStore.playbackPitch)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    private val _sleepTimerRemainingSec = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSec: StateFlow<Int?> = _sleepTimerRemainingSec.asStateFlow()

    private val _sleepTimerStopAfterTrack = MutableStateFlow(false)
    val sleepTimerStopAfterTrack: StateFlow<Boolean> = _sleepTimerStopAfterTrack.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var preSleepVolume: Float? = null

    val filteredTracks: StateFlow<List<AudioTrackItem>> = combine(
        combine(_tracks, _searchQuery, _libraryFilter) { list, query, filter ->
            Triple(list, query, filter)
        },
        combine(_playlists, _selectedPlaylistId, _librarySortOrder) { playlistsList, selectedPlId, sortOrder ->
            Triple(playlistsList, selectedPlId, sortOrder)
        }
    ) { (list, query, filter), (playlistsList, selectedPlId, sortOrder) ->
        val q = query.trim().lowercase()
        val plTrackIds = selectedPlId?.let { id -> playlistsList.firstOrNull { it.id == id }?.trackIds?.toSet() }
        val filtered = list.filter { item ->
            val matchesPlaylist = plTrackIds == null || item.id in plTrackIds || item.stableKey in plTrackIds
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.FAVORITES -> item.isFavorite
                LibraryFilter.LOADED -> item.id.startsWith("user_") || item.id.startsWith("saved_")
            }
            val matchesQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.artist.lowercase().contains(q) ||
                item.album.lowercase().contains(q) ||
                item.folderName.lowercase().contains(q)
            matchesPlaylist && matchesFilter && matchesQuery
        }
        when (sortOrder) {
            LibrarySortOrder.RECENT -> filtered.sortedByDescending { it.dateAddedSec }
            LibrarySortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> filtered.sortedWith(compareBy<AudioTrackItem> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
            LibrarySortOrder.DURATION -> filtered.sortedByDescending { it.durationSeconds }
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTracks: StateFlow<List<AudioTrackItem>> = combine(_tracks, _recentTrackKeys) { all, keys ->
        if (all.isEmpty() || keys.isEmpty()) return@combine emptyList()
        val byKey = all.associateBy { it.stableKey }
        val byId = all.associateBy { it.id }
        keys.mapNotNull { k -> byKey[k] ?: byId[k] }.distinctBy { it.stableKey }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedTracks: StateFlow<List<AudioTrackItem>> = combine(_tracks, _trackPlayCounts) { all, counts ->
        if (all.isEmpty() || counts.isEmpty()) return@combine emptyList()
        all.mapNotNull { tr ->
            val count = counts[tr.stableKey] ?: counts[tr.id] ?: 0
            if (count > 0) tr to count else null
        }.sortedByDescending { it.second }.map { it.first }.take(15)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<AudioTrackItem>> = _tracks.map { list ->
        list.filter { it.isFavorite }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderSummary>> = _tracks.map { list ->
        list.groupBy { it.folderName.ifBlank { "Música" } }
            .map { (folder, items) ->
                FolderSummary(
                    name = folder,
                    trackCount = items.size,
                    sampleArtUri = items.firstOrNull { it.albumArtUri != null }?.albumArtUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<ArtistSummary>> = _tracks.map { list ->
        list.groupBy { it.artist.ifBlank { "Artista Local" } }
            .map { (artistName, items) ->
                ArtistSummary(
                    name = artistName,
                    trackCount = items.size,
                    albumCount = items.map { it.album }.distinct().size,
                    sampleArtUri = items.firstOrNull { it.albumArtUri != null }?.albumArtUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumSummary>> = _tracks.map { list ->
        list.groupBy { it.album.ifBlank { "Álbum Local" } }
            .map { (albumTitle, items) ->
                AlbumSummary(
                    title = albumTitle,
                    artist = items.firstOrNull()?.artist ?: "Varios Artistas",
                    trackCount = items.size,
                    sampleArtUri = items.firstOrNull { it.albumArtUri != null }?.albumArtUri
                )
            }
            .sortedBy { it.title.lowercase() }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(audioEngine.currentTrack)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(audioEngine.isPlaying)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSeconds = MutableStateFlow(audioEngine.playbackPositionSeconds)
    val playbackSeconds: StateFlow<Int> = _playbackSeconds.asStateFlow()

    private val _volumeLevel = MutableStateFlow(audioEngine.volumeFactor)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(localStore.shuffleEnabled)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(localStore.loadRepeatMode())
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isScanningLibrary = MutableStateFlow(false)
    val isScanningLibrary: StateFlow<Boolean> = _isScanningLibrary.asStateFlow()

    private val _hasAudioLibraryPermission = MutableStateFlow(checkAudioStoragePermission())
    val hasAudioLibraryPermission: StateFlow<Boolean> = _hasAudioLibraryPermission.asStateFlow()

    // Real-time Meters, Oscilloscope, DSP Capabilities & Power Telemetry
    private val _leftVu = MutableStateFlow(0f)
    val leftVu: StateFlow<Float> = _leftVu.asStateFlow()

    private val _rightVu = MutableStateFlow(0f)
    val rightVu: StateFlow<Float> = _rightVu.asStateFlow()

    private val _bassEnergy = MutableStateFlow(0f)
    val bassEnergy: StateFlow<Float> = _bassEnergy.asStateFlow()

    private val _midEnergy = MutableStateFlow(0f)
    val midEnergy: StateFlow<Float> = _midEnergy.asStateFlow()

    private val _trebleEnergy = MutableStateFlow(0f)
    val trebleEnergy: StateFlow<Float> = _trebleEnergy.asStateFlow()

    private val _isAnyAudioPlaying = MutableStateFlow(audioEngine.isPlaying)
    val isAnyAudioPlaying: StateFlow<Boolean> = _isAnyAudioPlaying.asStateFlow()

    private val _splDb = MutableStateFlow(0f)
    val splDb: StateFlow<Float> = _splDb.asStateFlow()

    private val _rmsDb = MutableStateFlow(-60f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _rtaBands = MutableStateFlow(CarAudioEngine.createInitialRtaBands())
    val rtaBands: StateFlow<List<RtaBand>> = _rtaBands.asStateFlow()

    private val _oscilloscopeWave = MutableStateFlow(FloatArray(128))
    val oscilloscopeWave: StateFlow<FloatArray> = _oscilloscopeWave.asStateFlow()

    private val _isClippingDetected = MutableStateFlow(false)
    val isClippingDetected: StateFlow<Boolean> = _isClippingDetected.asStateFlow()

    private val _clipEventsCount = MutableStateFlow(0)
    val clipEventsCount: StateFlow<Int> = _clipEventsCount.asStateFlow()

    private val _isMicRta = MutableStateFlow(false)
    val isMicRta: StateFlow<Boolean> = _isMicRta.asStateFlow()

    private val _outputDeviceStatus = MutableStateFlow(audioEngine.outputDeviceStatus)
    val outputDeviceStatus: StateFlow<OutputDeviceStatus> = _outputDeviceStatus.asStateFlow()

    private val _sessionTarget = MutableStateFlow(audioEngine.sessionTarget)
    val sessionTarget: StateFlow<AudioSessionTarget> = _sessionTarget.asStateFlow()

    private val _fxEngineStatus = MutableStateFlow(audioEngine.fxEngineStatus)
    val fxEngineStatus: StateFlow<String> = _fxEngineStatus.asStateFlow()

    private val _capabilityReport = MutableStateFlow(audioEngine.capabilityReport)
    val capabilityReport: StateFlow<DspCapabilityReport> = _capabilityReport.asStateFlow()

    private val _devicePowerTelemetry = MutableStateFlow(readDevicePowerTelemetry())
    val devicePowerTelemetry: StateFlow<DevicePowerTelemetry> = _devicePowerTelemetry.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(
        CarAudioNotificationHelper.hasNotificationPermission(context)
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _faultAlerts = MutableStateFlow<List<SystemFaultAlert>>(emptyList())
    val faultAlerts: StateFlow<List<SystemFaultAlert>> = _faultAlerts.asStateFlow()

    private val _latestBannerAlert = MutableStateFlow<SystemFaultAlert?>(null)
    val latestBannerAlert: StateFlow<SystemFaultAlert?> = _latestBannerAlert.asStateFlow()

    // Cloud Account & Firestore Sync
    private val _userProfile = MutableStateFlow<CloudUserProfile?>(null)
    val userProfile: StateFlow<CloudUserProfile?> = _userProfile.asStateFlow()

    private val _cloudSyncActive = MutableStateFlow(false)
    val cloudSyncActive: StateFlow<Boolean> = _cloudSyncActive.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private var firebaseRepository: FirebaseUserRepository? = null
    private var cloudSyncJobs: MutableList<Job> = mutableListOf()
    private var debouncedSyncJob: Job? = null
    private var activeUserId: String? = null
    private var restoredCloudLastPosition = false

    // Media3 Controller
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var pendingPlayRequest: Pair<AudioTrackItem, Int>? = null
    private var loadedMediaControllerKeys: List<String> = emptyList()
    private var debouncedDspSaveJob: Job? = null
    private var lastDspPersistUptimeMs: Long = 0L
    private var dspPersistBurstCount: Int = 0

    @Volatile private var uiActive = true
    private var lastClipAlertTime = 0L
    private var lastPowerTelemetryFetchMs = 0L
    private var lastListeningAccumMs = 0L
    private var lastPersistedPosTelemetryMs = 0L
    private var lastSyncedPlayingState: Boolean? = null
    private var lastSyncedPosSec: Int = -1
    private var lastSyncedTrackKey: String? = null

    init {
        audioEngine.onSettingsChangedListener = { updatedDsp, updatedEq ->
            _dspSettings.value = updatedDsp
            _equalizerSettings.value = updatedEq
            localStore.saveDspSettings(updatedDsp)
            localStore.saveEqualizerSettings(updatedEq)
        }

        audioEngine.onPlaybackStateChangedListener = {
            val playing = audioEngine.isPlaying
            _isAnyAudioPlaying.value = playing
            _isPlaying.value = playing
            _currentTrack.value = audioEngine.currentTrack
            _playbackSeconds.value = audioEngine.playbackPositionSeconds
            _sessionTarget.value = audioEngine.sessionTarget
            _fxEngineStatus.value = audioEngine.fxEngineStatus
            _capabilityReport.value = audioEngine.capabilityReport
        }

        audioEngine.setSplWeightingMode(_splWeighting.value)

        audioEngine.onOutputDeviceChangedListener = { device ->
            val prevKind = _outputDeviceStatus.value.kind
            _outputDeviceStatus.value = device
            if (_autoProfileByOutput.value && prevKind != device.kind) {
                localStore.loadOutputKindProfile(device.kind)?.let { (savedDsp, savedEq) ->
                    val (safeDsp, _) = enforceAntiClippingLimits(savedDsp, savedEq, _bassMode.value)
                    _dspSettings.value = safeDsp
                    _equalizerSettings.value = savedEq
                    audioEngine.applyDspSettings(safeDsp, savedEq)
                    localStore.saveDspSettings(safeDsp)
                    localStore.saveEqualizerSettings(savedEq)
                    _capabilityReport.value = audioEngine.capabilityReport
                }
            }
        }

        audioEngine.onPlaybackErrorListener = { title, detail ->
            _isPlaying.value = false
            _isAnyAudioPlaying.value = false
            val canAdvance = (_queueTracks.value.size > 1 || _tracks.value.size > 1)
            reportSystemFault(
                title = title,
                message = detail,
                severity = FaultSeverity.ERROR,
                actionLabel = if (canAdvance) "Siguiente pista" else "Reintentar",
                actionType = if (canAdvance) FaultActionType.NEXT_TRACK else FaultActionType.RETRY_PLAYBACK,
                sendSystemNotification = false
            )
        }

        audioEngine.onAudioFaultListener = { title, detail, severity ->
            reportSystemFault(
                title = title,
                message = detail,
                severity = severity,
                actionLabel = if (severity == FaultSeverity.ERROR) "Reintentar" else null,
                actionType = if (severity == FaultSeverity.ERROR) FaultActionType.RETRY_PLAYBACK else FaultActionType.NONE,
                sendSystemNotification = severity != FaultSeverity.INFO
            )
        }

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
                _capabilityReport.value = audioEngine.capabilityReport
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

        connectMedia3Controller()
        startTelemetryLoop()
    }

    private fun toExoRepeatMode(mode: RepeatMode): Int = when (mode) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
    }

    private fun fromExoRepeatMode(exoMode: Int): RepeatMode = when (exoMode) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.OFF
    }

    private fun connectMedia3Controller() {
        try {
            val token = SessionToken(context, ComponentName(context, CarAudioPlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            controllerFuture = future
            future.addListener(
                {
                    try {
                        val controller = future.get()
                        mediaController = controller
                        controller.volume = _volumeLevel.value
                        controller.shuffleModeEnabled = _shuffleEnabled.value
                        controller.repeatMode = toExoRepeatMode(_repeatMode.value)
                        controller.playbackParameters = PlaybackParameters(_playbackSpeed.value, _playbackPitch.value)

                        controller.addListener(object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                if (_sleepTimerStopAfterTrack.value && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                                    _sleepTimerStopAfterTrack.value = false
                                    controller.pause()
                                    _isPlaying.value = false
                                    _isAnyAudioPlaying.value = false
                                    audioEngine.syncPlaybackStateFromMedia3(_currentTrack.value, false, 0)
                                    return
                                }
                                val id = mediaItem?.mediaId ?: return
                                val matched = _queueTracks.value.firstOrNull { it.stableKey == id || it.id == id }
                                    ?: _tracks.value.firstOrNull { it.stableKey == id || it.id == id }
                                if (matched != null && matched.stableKey != _currentTrack.value?.stableKey) {
                                    _currentTrack.value = matched
                                    _playbackSeconds.value = 0
                                    recordTrackPlay(matched)
                                    localStore.lastTrackKey = matched.stableKey
                                    localStore.lastPositionSeconds = 0
                                    audioEngine.syncPlaybackStateFromMedia3(matched, controller.isPlaying, 0)
                                }
                            }

                            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                                if (_shuffleEnabled.value != shuffleModeEnabled) {
                                    _shuffleEnabled.value = shuffleModeEnabled
                                    localStore.shuffleEnabled = shuffleModeEnabled
                                }
                            }

                            override fun onRepeatModeChanged(repeatMode: Int) {
                                val mapped = fromExoRepeatMode(repeatMode)
                                if (_repeatMode.value != mapped) {
                                    _repeatMode.value = mapped
                                    localStore.saveRepeatMode(mapped)
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                _isPlaying.value = isPlaying
                                _isAnyAudioPlaying.value = isPlaying
                                val posSec = (controller.currentPosition / 1000L).toInt().coerceAtLeast(0)
                                _playbackSeconds.value = posSec
                                audioEngine.syncPlaybackStateFromMedia3(_currentTrack.value, isPlaying, posSec)
                                persistLastPlaybackPosition(posSec)
                            }

                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_ENDED) {
                                    if (_sleepTimerStopAfterTrack.value) {
                                        _sleepTimerStopAfterTrack.value = false
                                    }
                                    handleTrackCompletion()
                                }
                            }
                        })
                        pendingPlayRequest?.let { (pendingTrack, pendingPos) ->
                            pendingPlayRequest = null
                            playTrack(pendingTrack, pendingPos)
                        }
                    } catch (_: Throwable) {
                        mediaController = null
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } catch (_: Throwable) {
            mediaController = null
        }
    }

    // =====================================================================
    // Firebase Account Binding & Firestore Cloud Sync
    // =====================================================================
    fun bindAuthenticatedUser(
        uid: String,
        displayName: String,
        email: String?,
        photoUrl: String?
    ) {
        if (uid.isBlank()) return
        if (activeUserId == uid && _cloudSyncActive.value) return

        cancelCloudCollectors()
        activeUserId = uid
        restoredCloudLastPosition = false

        val repo = getOrCreateFirebaseRepository() ?: return
        _cloudSyncActive.value = true

        viewModelScope.launch {
            repo.ensureUserProfile(
                displayName = displayName,
                email = email,
                photoUrl = photoUrl,
                defaultThemeId = _currentTheme.value.id
            ).onSuccess { initialProfile ->
                applyCloudProfile(initialProfile)
            }
        }

        cloudSyncJobs += viewModelScope.launch {
            repo.observeUserProfile(uid).collect { profile ->
                if (profile != null) applyCloudProfile(profile)
            }
        }

        cloudSyncJobs += viewModelScope.launch {
            repo.observeSoundPresets(uid).collect { presets ->
                if (presets.isNotEmpty()) {
                    val cloudProfiles = presets.map { cloud ->
                        val b31 = if (cloud.eq31Bands.size == 31) cloud.eq31Bands.map { it.toFloat() }
                        else expand10To31(cloud.eq10Bands.map { it.toFloat() })
                        val b10 = if (cloud.eq10Bands.size == 10) cloud.eq10Bands.map { it.toFloat() }
                        else EqualizerSettings.bands10From31(b31)
                        CustomSoundProfile(
                            id = cloud.id,
                            name = cloud.name,
                            description = cloud.description,
                            dspSettings = _dspSettings.value.copy(
                                preampDb = cloud.preampDb.toFloat(),
                                bassBoostDb = cloud.bassBoostDb.toFloat(),
                                bassBoostEnabled = cloud.bassBoostDb > 0.2,
                                midGainDb = cloud.midGainDb.toFloat(),
                                trebleGainDb = cloud.trebleGainDb.toFloat(),
                                balancePan = cloud.stereoBalance.toFloat(),
                                stereoWidthPercent = (100f + cloud.virtualizerPercent.toFloat()).coerceIn(100f, 250f),
                                compressorEnabled = cloud.compressorEnabled,
                                limiterEnabled = cloud.limiterEnabled,
                                hpfFrequencyHz = cloud.hpfHz.toFloat(),
                                lpfFrequencyHz = cloud.lpfHz.toFloat(),
                                subsonicFrequencyHz = cloud.subsonicHz.toFloat()
                            ),
                            eqBands10 = b10,
                            eqBands15 = b10To15(b10),
                            eqBands31 = b31
                        )
                    }
                    val localOnly = _customSoundProfiles.value.filter { local ->
                        cloudProfiles.none { it.id == local.id || it.name.equals(local.name, ignoreCase = true) }
                    }
                    val merged = cloudProfiles + localOnly
                    _customSoundProfiles.value = merged
                    localStore.saveCustomSoundProfiles(merged)
                }
            }
        }

        cloudSyncJobs += viewModelScope.launch {
            repo.observePlaylists(uid).collect { cloudPls ->
                if (cloudPls.isNotEmpty()) {
                    val mapped = cloudPls.map { cp ->
                        MusicPlaylist(id = cp.id, name = cp.name, trackIds = cp.trackKeys)
                    }
                    val localOnly = _playlists.value.filter { l -> mapped.none { it.id == l.id } }
                    val merged = mapped + localOnly
                    _playlists.value = merged
                    localStore.savePlaylists(merged)
                }
            }
        }
    }

    private fun applyCloudProfile(profile: CloudUserProfile) {
        _userProfile.value = profile

        _isVipUnlocked.value = true
        localStore.isVipUnlocked = true

        if (profile.vipPromoDismissed && !_vipPromoDismissed.value) {
            _vipPromoDismissed.value = true
            localStore.vipPromoDismissed = true
        }

        if (profile.onboardingCompleted) {
            localStore.onboardingCompleted = true
        }

        val cloudTheme = CarAudioThemeType.fromId(profile.themeId)
        if (_currentTheme.value != cloudTheme) {
            _currentTheme.value = cloudTheme
            localStore.saveThemeId(cloudTheme.id)
        }

        if (profile.favoriteTrackKeys.isNotEmpty()) {
            val mergedFavs = _favoriteIds.value + profile.favoriteTrackKeys
            if (mergedFavs != _favoriteIds.value) {
                _favoriteIds.value = mergedFavs
                localStore.saveFavoriteIds(mergedFavs)
                _tracks.value = _tracks.value.map { t ->
                    t.copy(isFavorite = mergedFavs.contains(t.id) || mergedFavs.contains(t.stableKey))
                }
            }
        }

        if (profile.queueTrackKeys.isNotEmpty() && _queueKeys.value.isEmpty()) {
            _queueKeys.value = profile.queueTrackKeys
            localStore.saveQueueKeys(profile.queueTrackKeys)
            rebuildQueueFromTracks()
        }

        if (!restoredCloudLastPosition && !_isPlaying.value && !profile.lastTrackKey.isNullOrBlank()) {
            restoredCloudLastPosition = true
            localStore.lastTrackKey = profile.lastTrackKey
            localStore.lastPositionSeconds = (profile.lastPositionMs / 1000L).toInt()
            val match = _tracks.value.firstOrNull {
                it.stableKey == profile.lastTrackKey || it.id == profile.lastTrackKey
            }
            if (match != null) {
                _currentTrack.value = match
                _playbackSeconds.value = (profile.lastPositionMs / 1000L).toInt().coerceAtLeast(0)
            }
        }
    }

    fun clearSessionOnSignOut() {
        cancelCloudCollectors()
        activeUserId = null
        _userProfile.value = null
        _cloudSyncActive.value = false
        _isVipUnlocked.value = true
        localStore.isVipUnlocked = true
    }

    fun deleteAccountAndCloudData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isDeletingAccount.value) return
        _isDeletingAccount.value = true
        viewModelScope.launch {
            try {
                mediaController?.stop()
                audioEngine.stopMicRta()
                val repo = getOrCreateFirebaseRepository()
                if (repo != null) {
                    repo.deleteAllUserData().getOrThrow()
                }
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.delete()?.await()
                }
                localStore.clearAll()
                clearSessionOnSignOut()
                _isDeletingAccount.value = false
                onSuccess()
            } catch (t: Throwable) {
                _isDeletingAccount.value = false
                onError(
                    t.localizedMessage
                        ?: "Por seguridad de Google, vuelve a iniciar sesión e intenta eliminar tu cuenta de nuevo."
                )
            }
        }
    }

    private fun getOrCreateFirebaseRepository(): FirebaseUserRepository? {
        firebaseRepository?.let { return it }
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) return null
            FirebaseUserRepository(context).also { firebaseRepository = it }
        } catch (_: Throwable) {
            null
        }
    }

    private fun cancelCloudCollectors() {
        cloudSyncJobs.forEach { it.cancel() }
        cloudSyncJobs.clear()
        debouncedSyncJob?.cancel()
    }

    private fun scheduleProfileCloudSync() {
        val repo = getOrCreateFirebaseRepository() ?: return
        if (activeUserId == null) return
        debouncedSyncJob?.cancel()
        debouncedSyncJob = viewModelScope.launch {
            delay(800)
            val track = _currentTrack.value
            repo.updateProfilePreferences(
                themeId = _currentTheme.value.id,
                vipPromoDismissed = _vipPromoDismissed.value,
                onboardingCompleted = localStore.onboardingCompleted,
                lastTrackKey = track?.stableKey,
                lastTrackTitle = track?.title,
                lastTrackArtist = track?.artist,
                lastPositionMs = (_playbackSeconds.value * 1000L).coerceAtLeast(0L),
                favoriteTrackKeys = _favoriteIds.value.toList(),
                queueTrackKeys = _queueKeys.value
            )
        }
    }

    // =====================================================================
    // UI Visibility & Telemetry Loop
    // =====================================================================
    private fun needsLiveVisualizer(section: AppSection = _selectedSection.value): Boolean {
        return section == AppSection.HOME ||
            section == AppSection.PLAYER ||
            section == AppSection.DRIVING_MODE ||
            section == AppSection.EQUALIZER ||
            section == AppSection.BASS_BOOST ||
            section == AppSection.SUBWOOFER ||
            section == AppSection.VISUALIZER ||
            section == AppSection.AUDIO_TOOLS ||
            _isMicRta.value
    }

    fun setUiActive(active: Boolean) {
        uiActive = active
        audioEngine.setVisualizerActive(active && needsLiveVisualizer())
        if (active) {
            val hasPerm = checkAudioStoragePermission()
            if (hasPerm && !_hasAudioLibraryPermission.value) {
                _hasAudioLibraryPermission.value = true
                scanDeviceMusicLibrary()
            }
            audioEngine.refreshOutputDeviceStatus()
            _hasNotificationPermission.value = CarAudioNotificationHelper.hasNotificationPermission(context)
            refreshDevicePowerTelemetry()
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
                message = "No podemos escanear las carpetas del teléfono sin permiso. Puedes otorgarlo o usar 'Importar Archivos'.",
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
                message = "Activa las notificaciones para controlar el reproductor desde la pantalla bloqueada y recibir alertas DSP.",
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
        _capabilityReport.value = audioEngine.capabilityReport
        audioEngine.refreshOutputDeviceStatus()
        refreshDevicePowerTelemetry()
    }

    // =====================================================================
    // Music Library, Artists, Albums, Favorites & Playlists
    // =====================================================================
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun setLibrarySortOrder(order: LibrarySortOrder) {
        _librarySortOrder.value = order
        localStore.saveSortOrder(order)
    }

    fun toggleFavorite(track: AudioTrackItem) {
        val currentFavs = _favoriteIds.value.toMutableSet()
        val nowFav = !track.isFavorite
        if (nowFav) {
            currentFavs.add(track.id)
            currentFavs.add(track.stableKey)
        } else {
            currentFavs.remove(track.id)
            currentFavs.remove(track.stableKey)
        }
        _favoriteIds.value = currentFavs
        localStore.saveFavoriteIds(currentFavs)

        _tracks.value = _tracks.value.map {
            if (it.id == track.id || it.stableKey == track.stableKey) it.copy(isFavorite = nowFav) else it
        }
        _queueTracks.value = _queueTracks.value.map {
            if (it.id == track.id || it.stableKey == track.stableKey) it.copy(isFavorite = nowFav) else it
        }
        if (_currentTrack.value?.id == track.id || _currentTrack.value?.stableKey == track.stableKey) {
            _currentTrack.value = _currentTrack.value?.copy(isFavorite = nowFav)
        }
        scheduleProfileCloudSync()
    }

    private fun extractFolderName(rawPath: String?): String {
        if (rawPath.isNullOrBlank()) return "Música"
        val cleaned = rawPath.trim('/').trim()
        if (cleaned.isEmpty()) return "Música"
        val parts = cleaned.split('/').filter { it.isNotBlank() }
        return if (parts.isEmpty()) "Música" else parts.last()
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

                localStore.loadSavedCustomTracks(favs).forEach { track ->
                    val uriKey = track.customUri?.toString().orEmpty()
                    if (uriKey.isNotEmpty() && seenUris.add(uriKey)) {
                        result.add(track)
                    }
                }

                if (hasPerm) {
                    try {
                        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            arrayOf(
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.ALBUM_ID,
                                MediaStore.Audio.Media.MIME_TYPE,
                                MediaStore.Audio.Media.RELATIVE_PATH,
                                MediaStore.Audio.Media.DATE_ADDED
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            arrayOf(
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.ALBUM_ID,
                                MediaStore.Audio.Media.MIME_TYPE,
                                MediaStore.Audio.Media.DATA,
                                MediaStore.Audio.Media.DATE_ADDED
                            )
                        }
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
                            val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                            val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                            } else -1
                            @Suppress("DEPRECATION")
                            val dataCol = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                            } else -1

                            while (cursor.moveToNext()) {
                                val id = cursor.getLong(idCol)
                                val contentUri = ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                                val uriKey = contentUri.toString()
                                if (!seenUris.add(uriKey)) continue

                                val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Pista $id"
                                val artist = cursor.getString(artistCol)
                                    ?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Artista Local"
                                val album = cursor.getString(albumCol)
                                    ?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Música del Teléfono"
                                val durationSec = (cursor.getLong(durCol) / 1000L).toInt().coerceAtLeast(1)
                                val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                                val formatLabel = formatMimeLabel(mime)
                                val dateAddedSec = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                                val folder = when {
                                    relPathCol >= 0 -> extractFolderName(cursor.getString(relPathCol))
                                    dataCol >= 0 -> {
                                        val filePath = cursor.getString(dataCol)
                                        val parent = filePath?.substringBeforeLast('/')
                                        extractFolderName(parent)
                                    }
                                    else -> "Música"
                                }
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
                                        customUri = contentUri,
                                        albumArtUri = artUri,
                                        isFavorite = favs.contains(trackId) || favs.contains(uriKey),
                                        audioFormatLabel = formatLabel,
                                        folderName = folder,
                                        dateAddedSec = dateAddedSec
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
            rebuildQueueFromTracks()

            val lastKey = localStore.lastTrackKey
            if (_currentTrack.value == null && combined.isNotEmpty()) {
                val restored = lastKey?.let { lk ->
                    combined.firstOrNull { it.stableKey == lk || it.id == lk }
                }
                _currentTrack.value = restored ?: combined.first()
                if (restored != null) {
                    _playbackSeconds.value = localStore.lastPositionSeconds
                }
            }
            _isScanningLibrary.value = false
        }
    }

    private fun rebuildQueueFromTracks() {
        val keys = _queueKeys.value
        val byKey = _tracks.value.associateBy { it.stableKey }
        val byId = _tracks.value.associateBy { it.id }
        val resolved = keys.mapNotNull { k -> byKey[k] ?: byId[k] }
        if (resolved.isNotEmpty()) {
            _queueTracks.value = resolved
        } else if (_queueTracks.value.isEmpty() && _tracks.value.isNotEmpty()) {
            _queueTracks.value = _tracks.value
            _queueKeys.value = _tracks.value.map { it.stableKey }
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
            frequencyDescription = "Audio Importado • DSP en Vivo",
            customUri = uri,
            isFavorite = _favoriteIds.value.contains(id) || _favoriteIds.value.contains(uri.toString()),
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
                    localStore.persistCustomTrack(track)
                    track
                }
            }
            val newUriStrings = loaded.mapNotNull { it.customUri?.toString() }.toSet()
            val remaining = _tracks.value.filter { it.customUri?.toString() !in newUriStrings }
            _tracks.value = loaded + remaining
            loaded.forEach { addToQueue(it, silent = true) }
            loaded.firstOrNull()?.let { playTrack(it) }
        }
    }

    fun addCustomUserTrack(uri: Uri, name: String) {
        addCustomUserTracks(listOf(uri))
    }

    fun removeTrack(track: AudioTrackItem) {
        track.customUri?.let { localStore.removePersistedCustomTrack(it) }
        val updated = _tracks.value.filter { it.id != track.id }
        _tracks.value = updated
        val updatedQueue = _queueTracks.value.filter { it.id != track.id }
        setQueueInternal(updatedQueue)
        if (_currentTrack.value?.id == track.id) {
            mediaController?.stop()
            audioEngine.syncPlaybackStateFromMedia3(null, false, 0)
            _isPlaying.value = false
            _currentTrack.value = updated.firstOrNull()
        }
    }

    private fun allKnownTracks(): List<AudioTrackItem> =
        (_tracks.value + _queueTracks.value).distinctBy { it.stableKey }

    fun playArtist(artistName: String) {
        val list = allKnownTracks().filter { it.artist.equals(artistName, ignoreCase = true) }
        if (list.isEmpty()) return
        setQueueInternal(list)
        playTrack(list.first())
    }

    fun playAlbum(albumTitle: String) {
        val list = allKnownTracks().filter { it.album.equals(albumTitle, ignoreCase = true) }
        if (list.isEmpty()) return
        setQueueInternal(list)
        playTrack(list.first())
    }

    fun playFolder(folderName: String) {
        val list = allKnownTracks().filter { it.folderName.equals(folderName, ignoreCase = true) }
        if (list.isEmpty()) return
        setQueueInternal(list)
        playTrack(list.first())
    }

    fun playPlaylist(playlistId: String, shuffled: Boolean = false) {
        val pl = _playlists.value.firstOrNull { it.id == playlistId } ?: return
        val ids = pl.trackIds.toSet()
        val list = allKnownTracks().filter { it.id in ids || it.stableKey in ids }
        if (list.isEmpty()) return
        val finalQueue = if (shuffled) list.shuffled() else list
        setQueueInternal(finalQueue)
        playTrack(finalQueue.first())
    }

    fun playAllTracks(shuffled: Boolean = false, sourceList: List<AudioTrackItem> = filteredTracks.value) {
        val source = if (sourceList.isNotEmpty()) sourceList else _tracks.value
        if (source.isEmpty()) return
        if (shuffled && !_shuffleEnabled.value) {
            _shuffleEnabled.value = true
            localStore.shuffleEnabled = true
        } else if (!shuffled && _shuffleEnabled.value) {
            _shuffleEnabled.value = false
            localStore.shuffleEnabled = false
        }
        val finalQueue = if (shuffled) source.shuffled() else source
        setQueueInternal(finalQueue)
        playTrack(finalQueue.first())
    }

    private fun recordTrackPlay(track: AudioTrackItem) {
        val key = track.stableKey
        val updatedRecent = (listOf(key) + _recentTrackKeys.value.filter { it != key }).take(25)
        _recentTrackKeys.value = updatedRecent
        localStore.saveRecentTrackKeys(updatedRecent)

        val nextCounts = _trackPlayCounts.value.toMutableMap()
        nextCounts[key] = (nextCounts[key] ?: 0) + 1
        _trackPlayCounts.value = nextCounts
        localStore.savePlayCounts(nextCounts)
    }

    // =====================================================================
    // Playback Queue Management
    // =====================================================================
    private fun setQueueInternal(newQueue: List<AudioTrackItem>) {
        loadedMediaControllerKeys = emptyList()
        _queueTracks.value = newQueue
        val keys = newQueue.map { it.stableKey }
        _queueKeys.value = keys
        localStore.saveQueueKeys(keys)
        scheduleProfileCloudSync()
    }

    fun addToQueue(track: AudioTrackItem, silent: Boolean = false) {
        if (_queueTracks.value.any { it.stableKey == track.stableKey }) return
        val updated = _queueTracks.value + track
        setQueueInternal(updated)
        try {
            track.customUri?.let { uri ->
                val mc = mediaController
                if (mc != null && mc.mediaItemCount > 0) {
                    mc.addMediaItem(buildMediaItem(track, uri))
                }
            }
        } catch (_: Throwable) {}
    }

    fun playNextInQueue(track: AudioTrackItem) {
        val current = _currentTrack.value
        val currentList = _queueTracks.value.filter { it.stableKey != track.stableKey }.toMutableList()
        val insertIndex = if (current != null) {
            val idx = currentList.indexOfFirst { it.stableKey == current.stableKey }
            if (idx >= 0) idx + 1 else 0
        } else 0
        val clampedIdx = insertIndex.coerceIn(0, currentList.size)
        currentList.add(clampedIdx, track)
        setQueueInternal(currentList)
        try {
            val mc = mediaController
            val uri = track.customUri
            if (mc != null && uri != null && mc.mediaItemCount > 0) {
                val activeIdx = mc.currentMediaItemIndex.coerceAtLeast(0)
                val targetExoIdx = (activeIdx + 1).coerceIn(0, mc.mediaItemCount)
                mc.addMediaItem(targetExoIdx, buildMediaItem(track, uri))
            }
        } catch (_: Throwable) {}
    }

    fun removeFromQueue(index: Int) {
        val list = _queueTracks.value.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        setQueueInternal(list)
    }

    fun moveQueueItemUp(index: Int) {
        val list = _queueTracks.value.toMutableList()
        if (index <= 0 || index >= list.size) return
        val item = list.removeAt(index)
        list.add(index - 1, item)
        setQueueInternal(list)
    }

    fun moveQueueItemDown(index: Int) {
        val list = _queueTracks.value.toMutableList()
        if (index < 0 || index >= list.lastIndex) return
        val item = list.removeAt(index)
        list.add(index + 1, item)
        setQueueInternal(list)
    }

    fun clearQueue() {
        setQueueInternal(emptyList())
    }

    fun playQueueAt(index: Int) {
        val track = _queueTracks.value.getOrNull(index) ?: return
        playTrack(track)
    }

    // =====================================================================
    // Playlists (Local + Firestore)
    // =====================================================================
    fun selectPlaylistFilter(playlistId: String?) {
        _selectedPlaylistId.value = playlistId
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val playlist = MusicPlaylist(
            id = "pl_${System.currentTimeMillis()}",
            name = trimmed,
            trackIds = emptyList()
        )
        val updated = _playlists.value + playlist
        _playlists.value = updated
        localStore.savePlaylists(updated)
        syncPlaylistToCloud(playlist)
    }

    fun deletePlaylist(playlistId: String) {
        val updated = _playlists.value.filter { it.id != playlistId }
        _playlists.value = updated
        localStore.savePlaylists(updated)
        if (_selectedPlaylistId.value == playlistId) {
            _selectedPlaylistId.value = null
        }
        viewModelScope.launch {
            getOrCreateFirebaseRepository()?.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        var modified: MusicPlaylist? = null
        val updated = _playlists.value.map { pl ->
            if (pl.id == playlistId && trackId !in pl.trackIds) {
                pl.copy(trackIds = pl.trackIds + trackId).also { modified = it }
            } else pl
        }
        _playlists.value = updated
        localStore.savePlaylists(updated)
        modified?.let { syncPlaylistToCloud(it) }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        var modified: MusicPlaylist? = null
        val updated = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(trackIds = pl.trackIds.filter { it != trackId }).also { modified = it }
            } else pl
        }
        _playlists.value = updated
        localStore.savePlaylists(updated)
        modified?.let { syncPlaylistToCloud(it) }
    }

    private fun syncPlaylistToCloud(playlist: MusicPlaylist) {
        val repo = getOrCreateFirebaseRepository() ?: return
        val uid = activeUserId ?: return
        viewModelScope.launch {
            repo.savePlaylist(
                CloudPlaylist(
                    id = playlist.id,
                    userId = uid,
                    name = playlist.name,
                    trackKeys = playlist.trackIds
                )
            )
        }
    }

    // =====================================================================
    // Playback Controls (Real Multi-Item Media3 Timeline + Speed/Pitch + Sleep Timer)
    // =====================================================================
    fun toggleShuffle() {
        val next = !_shuffleEnabled.value
        _shuffleEnabled.value = next
        localStore.shuffleEnabled = next
        try {
            mediaController?.shuffleModeEnabled = next
        } catch (_: Throwable) {}
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        localStore.saveRepeatMode(next)
        try {
            mediaController?.repeatMode = toExoRepeatMode(next)
        } catch (_: Throwable) {}
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        _playbackSpeed.value = clamped
        localStore.playbackSpeed = clamped
        applyPlaybackParametersToController()
    }

    fun setPlaybackPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        _playbackPitch.value = clamped
        localStore.playbackPitch = clamped
        applyPlaybackParametersToController()
    }

    fun resetPlaybackSpeedPitch() {
        _playbackSpeed.value = 1.0f
        _playbackPitch.value = 1.0f
        localStore.playbackSpeed = 1.0f
        localStore.playbackPitch = 1.0f
        applyPlaybackParametersToController()
    }

    private fun applyPlaybackParametersToController() {
        try {
            mediaController?.playbackParameters = PlaybackParameters(_playbackSpeed.value, _playbackPitch.value)
        } catch (_: Throwable) {}
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerStopAfterTrack.value = false
        if (minutes <= 0) {
            _sleepTimerRemainingSec.value = null
            restorePreSleepVolumeIfNeeded()
            return
        }
        val totalSeconds = minutes * 60
        _sleepTimerRemainingSec.value = totalSeconds
        preSleepVolume = _volumeLevel.value
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _sleepTimerRemainingSec.value = remaining
                val baseVol = preSleepVolume ?: _volumeLevel.value
                if (remaining in 1..10 && baseVol > 0.05f) {
                    val factor = remaining / 10f
                    val faded = (baseVol * factor).coerceIn(0.02f, 1f)
                    audioEngine.setVolume(faded)
                    try {
                        mediaController?.volume = faded
                    } catch (_: Throwable) {}
                }
            }
            if (isActive && remaining <= 0) {
                _sleepTimerRemainingSec.value = null
                try {
                    mediaController?.pause()
                } catch (_: Throwable) {}
                _isPlaying.value = false
                _isAnyAudioPlaying.value = false
                audioEngine.syncPlaybackStateFromMedia3(_currentTrack.value, false, _playbackSeconds.value)
                restorePreSleepVolumeIfNeeded()
            }
        }
    }

    fun toggleSleepAfterCurrentTrack() {
        val next = !_sleepTimerStopAfterTrack.value
        _sleepTimerStopAfterTrack.value = next
        if (next) {
            sleepTimerJob?.cancel()
            _sleepTimerRemainingSec.value = null
            restorePreSleepVolumeIfNeeded()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingSec.value = null
        _sleepTimerStopAfterTrack.value = false
        restorePreSleepVolumeIfNeeded()
    }

    private fun restorePreSleepVolumeIfNeeded() {
        val saved = preSleepVolume ?: return
        preSleepVolume = null
        _volumeLevel.value = saved
        audioEngine.setVolume(saved)
        try {
            mediaController?.volume = saved
        } catch (_: Throwable) {}
    }

    private fun buildMediaItem(track: AudioTrackItem, uri: Uri): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri)
            .build()
        return MediaItem.Builder()
            .setMediaId(track.stableKey)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun playTrack(track: AudioTrackItem, startPositionSec: Int = 0) {
        if (_queueTracks.value.none { it.stableKey == track.stableKey }) {
            setQueueInternal(listOf(track) + _queueTracks.value)
        }
        _currentTrack.value = track
        _isPlaying.value = true
        _isAnyAudioPlaying.value = true
        _playbackSeconds.value = startPositionSec.coerceAtLeast(0)
        localStore.lastTrackKey = track.stableKey
        localStore.lastPositionSeconds = startPositionSec.coerceAtLeast(0)
        recordTrackPlay(track)

        audioEngine.syncPlaybackStateFromMedia3(track, playing = true, positionSeconds = startPositionSec)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)

        val playableQueue = _queueTracks.value.filter { it.customUri != null }
        val uri = track.customUri
        if (uri != null) {
            val mc = mediaController
            if (mc == null) {
                pendingPlayRequest = track to startPositionSec
            } else {
                pendingPlayRequest = null
                try {
                    val effectiveQueue = if (playableQueue.isNotEmpty()) playableQueue else listOf(track)
                    val targetIdxInFull = effectiveQueue.indexOfFirst { it.stableKey == track.stableKey }.coerceAtLeast(0)
                    val maxBinderWindow = 200
                    val windowQueue = if (effectiveQueue.size > maxBinderWindow) {
                        val windowStart = (targetIdxInFull - maxBinderWindow / 2)
                            .coerceIn(0, (effectiveQueue.size - maxBinderWindow).coerceAtLeast(0))
                        val windowEnd = (windowStart + maxBinderWindow).coerceAtMost(effectiveQueue.size)
                        effectiveQueue.subList(windowStart, windowEnd)
                    } else {
                        effectiveQueue
                    }
                    val windowKeys = windowQueue.map { it.stableKey }
                    val startIdx = windowKeys.indexOf(track.stableKey).coerceAtLeast(0)

                    if (mc.mediaItemCount == windowKeys.size &&
                        loadedMediaControllerKeys == windowKeys &&
                        windowKeys.isNotEmpty()
                    ) {
                        mc.seekTo(startIdx, startPositionSec * 1000L)
                        mc.playbackParameters = PlaybackParameters(_playbackSpeed.value, _playbackPitch.value)
                        mc.prepare()
                        mc.play()
                    } else {
                        val items = windowQueue.mapNotNull { item -> item.customUri?.let { u -> buildMediaItem(item, u) } }
                        loadedMediaControllerKeys = windowKeys
                        mc.setMediaItems(items, startIdx, startPositionSec * 1000L)
                        mc.shuffleModeEnabled = _shuffleEnabled.value
                        mc.repeatMode = toExoRepeatMode(_repeatMode.value)
                        mc.playbackParameters = PlaybackParameters(_playbackSpeed.value, _playbackPitch.value)
                        mc.prepare()
                        mc.play()
                    }
                } catch (t: Throwable) {
                    loadedMediaControllerKeys = emptyList()
                    reportSystemFault(
                        title = "No se pudo reproducir pista",
                        message = t.localizedMessage ?: "Error al abrir el archivo de audio.",
                        severity = FaultSeverity.WARNING
                    )
                }
            }
        }
        scheduleProfileCloudSync()
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: _queueTracks.value.firstOrNull() ?: _tracks.value.firstOrNull() ?: return
        val mc = mediaController
        if (mc != null && mc.mediaItemCount > 0) {
            if (mc.isPlaying) {
                mc.pause()
                _isPlaying.value = false
                _isAnyAudioPlaying.value = false
                audioEngine.syncPlaybackStateFromMedia3(track, false, _playbackSeconds.value)
            } else {
                mc.play()
                _isPlaying.value = true
                _isAnyAudioPlaying.value = true
                audioEngine.syncPlaybackStateFromMedia3(track, true, _playbackSeconds.value)
            }
            persistLastPlaybackPosition(_playbackSeconds.value)
        } else if (_currentTrack.value == null) {
            playTrack(track)
        } else {
            val nextPlaying = !_isPlaying.value
            if (nextPlaying && track.customUri != null && mc != null) {
                playTrack(track, _playbackSeconds.value)
            } else {
                _isPlaying.value = nextPlaying
                _isAnyAudioPlaying.value = nextPlaying
                audioEngine.syncPlaybackStateFromMedia3(track, nextPlaying, _playbackSeconds.value)
                persistLastPlaybackPosition(_playbackSeconds.value)
            }
        }
    }

    private fun handleTrackCompletion() {
        val current = _currentTrack.value
        if (_repeatMode.value == RepeatMode.ONE && current != null) {
            playTrack(current, 0)
            return
        }
        val list = if (_queueTracks.value.isNotEmpty()) _queueTracks.value else _tracks.value
        if (list.isEmpty()) return
        if (_repeatMode.value == RepeatMode.OFF && !_shuffleEnabled.value) {
            val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
            if (idx >= list.lastIndex) {
                _isPlaying.value = false
                _isAnyAudioPlaying.value = false
                audioEngine.syncPlaybackStateFromMedia3(current, false, 0)
                return
            }
        }
        nextTrack()
    }

    fun retryCurrentTrack() {
        val current = _currentTrack.value ?: _queueTracks.value.firstOrNull() ?: _tracks.value.firstOrNull() ?: return
        playTrack(current, _playbackSeconds.value)
    }

    fun nextTrack() {
        val mc = mediaController
        if (mc != null && mc.mediaItemCount > 1 && mc.hasNextMediaItem()) {
            mc.seekToNextMediaItem()
            if (!mc.isPlaying) mc.play()
            return
        }
        val list = if (_queueTracks.value.isNotEmpty()) _queueTracks.value else _tracks.value
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
        val mc = mediaController
        if (_playbackSeconds.value > 4 && _currentTrack.value != null) {
            seekTo(0)
            return
        }
        if (mc != null && mc.mediaItemCount > 1 && mc.hasPreviousMediaItem()) {
            mc.seekToPreviousMediaItem()
            if (!mc.isPlaying) mc.play()
            return
        }
        val list = if (_queueTracks.value.isNotEmpty()) _queueTracks.value else _tracks.value
        if (list.isEmpty()) return
        val current = _currentTrack.value
        val idx = if (current != null) list.indexOfFirst { it.id == current.id } else -1
        val prevIdx = if (idx > 0) idx - 1 else list.size - 1
        playTrack(list[prevIdx])
    }

    fun seekTo(seconds: Int) {
        val clamped = seconds.coerceAtLeast(0)
        _playbackSeconds.value = clamped
        try {
            mediaController?.seekTo(clamped * 1000L)
        } catch (_: Throwable) {}
        audioEngine.syncPlaybackStateFromMedia3(_currentTrack.value, _isPlaying.value, clamped)
        persistLastPlaybackPosition(clamped)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        preSleepVolume = null
        _volumeLevel.value = clamped
        audioEngine.setVolume(clamped)
        try {
            mediaController?.volume = clamped
        } catch (_: Throwable) {}
    }

    private fun persistLastPlaybackPosition(posSec: Int) {
        localStore.lastPositionSeconds = posSec
        _currentTrack.value?.let { localStore.lastTrackKey = it.stableKey }
        scheduleProfileCloudSync()
    }

    // =====================================================================
    // Real Telemetry Loop & Battery Current (mA)
    // =====================================================================
    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (isActive) {
                val mc = mediaController
                val enginePlaying = if (mc != null && mc.mediaItemCount > 0) mc.isPlaying else audioEngine.isPlaying
                val activeAudio = enginePlaying || audioEngine.isMicRtaActive

                if (!uiActive && !activeAudio) {
                    delay(650)
                    continue
                }

                if (mc != null && mc.mediaItemCount > 0) {
                    val pos = (mc.currentPosition / 1000L).toInt().coerceAtLeast(0)
                    if (_playbackSeconds.value != pos) {
                        _playbackSeconds.value = pos
                    }
                    val mcPlaying = mc.isPlaying
                    val trackKey = _currentTrack.value?.stableKey
                    if (lastSyncedPlayingState != mcPlaying || lastSyncedPosSec != pos || lastSyncedTrackKey != trackKey) {
                        lastSyncedPlayingState = mcPlaying
                        lastSyncedPosSec = pos
                        lastSyncedTrackKey = trackKey
                        audioEngine.syncPlaybackStateFromMedia3(_currentTrack.value, mcPlaying, pos)
                    }
                }

                if (_isAnyAudioPlaying.value != enginePlaying) _isAnyAudioPlaying.value = enginePlaying
                if (_isPlaying.value != enginePlaying) _isPlaying.value = enginePlaying

                val sess = audioEngine.sessionTarget
                if (_sessionTarget.value != sess) _sessionTarget.value = sess
                val cap = audioEngine.capabilityReport
                if (_capabilityReport.value != cap) _capabilityReport.value = cap

                val sec = _selectedSection.value
                val visualMeterScreen = sec == AppSection.HOME ||
                    sec == AppSection.PLAYER ||
                    sec == AppSection.DRIVING_MODE ||
                    sec == AppSection.AUDIO_TOOLS ||
                    sec == AppSection.VISUALIZER ||
                    sec == AppSection.SUBWOOFER ||
                    sec == AppSection.BASS_BOOST ||
                    sec == AppSection.EQUALIZER ||
                    _isMicRta.value

                if (uiActive && activeAudio && visualMeterScreen) {
                    val l = audioEngine.leftVuLevel
                    val r = audioEngine.rightVuLevel
                    if (abs(_leftVu.value - l) > 0.02f) _leftVu.value = l
                    if (abs(_rightVu.value - r) > 0.02f) _rightVu.value = r

                    val b = audioEngine.bassEnergy
                    val m = audioEngine.midEnergy
                    val t = audioEngine.trebleEnergy
                    if (abs(_bassEnergy.value - b) > 0.02f) _bassEnergy.value = b
                    if (abs(_midEnergy.value - m) > 0.02f) _midEnergy.value = m
                    if (abs(_trebleEnergy.value - t) > 0.02f) _trebleEnergy.value = t

                    val spl = audioEngine.currentSplDb
                    if (abs(_splDb.value - spl) > 0.3f) _splDb.value = spl

                    val rms = audioEngine.rmsLevelDb
                    if (abs(_rmsDb.value - rms) > 0.35f) _rmsDb.value = rms

                    if (sec == AppSection.AUDIO_TOOLS ||
                        sec == AppSection.VISUALIZER ||
                        _isMicRta.value
                    ) {
                        _rtaBands.value = audioEngine.getRtaSnapshot()
                        _oscilloscopeWave.value = audioEngine.rawOscilloscopePcm.copyOf()
                    }
                } else if (_leftVu.value != 0f || _rightVu.value != 0f || _bassEnergy.value != 0f || _midEnergy.value != 0f || _trebleEnergy.value != 0f) {
                    _leftVu.value = 0f
                    _rightVu.value = 0f
                    _bassEnergy.value = 0f
                    _midEnergy.value = 0f
                    _trebleEnergy.value = 0f
                    _rmsDb.value = -60f
                }

                val nowUptimeMs = android.os.SystemClock.elapsedRealtime()
                if (enginePlaying) {
                    if (lastListeningAccumMs == 0L) lastListeningAccumMs = nowUptimeMs
                    val deltaSec = ((nowUptimeMs - lastListeningAccumMs) / 1000L).toInt()
                    if (deltaSec >= 1) {
                        lastListeningAccumMs += deltaSec * 1000L
                        val updatedSec = _totalListeningSeconds.value + deltaSec
                        _totalListeningSeconds.value = updatedSec
                        if (updatedSec % 10L == 0L) {
                            localStore.totalListeningSeconds = updatedSec
                        }
                    }
                    if (nowUptimeMs - lastPersistedPosTelemetryMs >= 5000L) {
                        lastPersistedPosTelemetryMs = nowUptimeMs
                        localStore.lastPositionSeconds = _playbackSeconds.value
                    }
                } else {
                    lastListeningAccumMs = 0L
                }

                val powerTelemetryScreen = sec == AppSection.SETTINGS ||
                    sec == AppSection.VISUALIZER ||
                    sec == AppSection.AUDIO_TOOLS
                if (uiActive && powerTelemetryScreen && nowUptimeMs - lastPowerTelemetryFetchMs >= 8000L) {
                    lastPowerTelemetryFetchMs = nowUptimeMs
                    refreshDevicePowerTelemetry()
                }

                val dsp = _dspSettings.value
                val totalGain = dsp.masterGainDb + dsp.preampDb + dsp.bassBoostDb
                val clipCondition = audioEngine.isClippingDetected ||
                    (enginePlaying && !dsp.limiterEnabled && totalGain > 22f && _leftVu.value > 0.9f)
                if (_isClippingDetected.value != clipCondition) {
                    _isClippingDetected.value = clipCondition
                }
                if (_clipEventsCount.value != audioEngine.clipEventsCount) {
                    _clipEventsCount.value = audioEngine.clipEventsCount
                }
                if (clipCondition) {
                    val now = System.currentTimeMillis()
                    if (now - lastClipAlertTime > 12000) {
                        lastClipAlertTime = now
                        val totalClips = audioEngine.clipEventsCount
                        CarAudioNotificationHelper.showClippingAlertNotification(context, totalClips)
                        reportSystemFault(
                            title = "Alerta de Clipping / Distorsión",
                            message = "La señal alcanzó el umbral de saturación ($totalClips picos). Baja el Preamplificador o activa el Limitador.",
                            severity = FaultSeverity.WARNING,
                            actionLabel = if (!dsp.limiterEnabled) "Activar Limitador" else null,
                            actionType = if (!dsp.limiterEnabled) FaultActionType.ENABLE_LIMITER else FaultActionType.NONE,
                            sendSystemNotification = false
                        )
                    }
                }

                val intervalMs = when {
                    !uiActive -> 650L
                    !activeAudio -> 450L
                    !visualMeterScreen -> 250L
                    _liteVisualsEnabled.value -> 110L
                    else -> 70L
                }
                delay(intervalMs)
            }
        }
    }

    private fun readDevicePowerTelemetry(): DevicePowerTelemetry {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val rawCurrentUa = bm?.let { manager ->
                val nowUa = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (nowUa != Int.MIN_VALUE && nowUa != 0) nowUa
                else manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            } ?: Int.MIN_VALUE
            val hasCurrent = rawCurrentUa != Int.MIN_VALUE && rawCurrentUa != 0
            val currentMa = if (hasCurrent) rawCurrentUa / 1000f else null

            if (batteryIntent == null) {
                DevicePowerTelemetry(batteryCurrentMa = currentMa, hasCurrentSensor = hasCurrent)
            } else {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                val percent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else null

                val voltageMv = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                val voltageV = if (voltageMv > 0) voltageMv / 1000f else null

                val tempTenths = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                val hasTemp = tempTenths > -200 && tempTenths < 1200 && tempTenths != Int.MIN_VALUE
                val tempC = if (hasTemp) tempTenths / 10f else null

                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

                DevicePowerTelemetry(
                    batteryPercent = percent,
                    batteryVoltageV = voltageV,
                    batteryTempC = tempC,
                    batteryCurrentMa = currentMa,
                    isCharging = charging,
                    hasBatterySensor = percent != null || voltageV != null,
                    hasTempSensor = hasTemp,
                    hasCurrentSensor = hasCurrent
                )
            }
        } catch (_: Throwable) {
            DevicePowerTelemetry()
        }
    }

    fun refreshDevicePowerTelemetry() {
        viewModelScope.launch(Dispatchers.IO) {
            _devicePowerTelemetry.value = readDevicePowerTelemetry()
        }
    }

    // =====================================================================
    // Navigation, Onboarding, VIP Presentation & Preferences
    // =====================================================================
    fun dismissSplashScreen() {
        _showSplashScreen.value = false
        localStore.onboardingCompleted = true
        scheduleProfileCloudSync()
    }

    fun openSplashScreen() {
        _showSplashScreen.value = true
    }

    /**
     * Permanently dismisses the promotional VIP banner in Home without changing VIP entitlement.
     */
    fun dismissVipPromoPermanently() {
        _vipPromoDismissed.value = true
        localStore.vipPromoDismissed = true
        scheduleProfileCloudSync()
    }

    fun setTheme(theme: CarAudioThemeType) {
        _currentTheme.value = theme
        localStore.saveThemeId(theme.id)
        scheduleProfileCloudSync()
    }

    fun selectSection(section: AppSection) {
        _selectedSection.value = section
        audioEngine.setVisualizerActive(uiActive && needsLiveVisualizer(section))
    }

    fun setAutoPlayIntro(enabled: Boolean) {
        _autoPlayIntro.value = enabled
        localStore.introEnabled = enabled
    }

    fun setDontShowIntroAgain(dontShow: Boolean) {
        localStore.dontShowIntroAgain = dontShow
        setAutoPlayIntro(!dontShow)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        localStore.keepScreenOn = enabled
    }

    fun setFloatingEqOnLeave(enabled: Boolean) {
        _floatingEqOnLeave.value = enabled
        localStore.floatingEqOnLeave = enabled
    }

    fun setDrivingModeEnabled(enabled: Boolean) {
        _drivingModeEnabled.value = enabled
        localStore.drivingModeEnabled = enabled
        if (enabled) {
            selectSection(AppSection.DRIVING_MODE)
        } else if (_selectedSection.value == AppSection.DRIVING_MODE) {
            selectSection(AppSection.HOME)
        }
    }

    fun setDrivingKeepScreenOn(enabled: Boolean) {
        _drivingKeepScreenOn.value = enabled
        localStore.drivingKeepScreenOn = enabled
    }

    fun setAutoProfileByOutput(enabled: Boolean) {
        _autoProfileByOutput.value = enabled
        localStore.autoProfileByOutput = enabled
    }

    fun saveProfileForCurrentOutput(kind: OutputKind = _outputDeviceStatus.value.kind) {
        val (safeDsp, _) = enforceAntiClippingLimits(_dspSettings.value, _equalizerSettings.value, _bassMode.value)
        localStore.saveOutputKindProfile(kind, safeDsp, _equalizerSettings.value)
        _savedOutputProfileKinds.value = localStore.loadSavedOutputProfileKinds()
    }

    fun clearProfileForOutput(kind: OutputKind) {
        localStore.clearOutputKindProfile(kind)
        _savedOutputProfileKinds.value = localStore.loadSavedOutputProfileKinds()
    }

    fun setSplWeighting(weighting: SplWeighting) {
        _splWeighting.value = weighting
        localStore.saveSplWeighting(weighting)
        audioEngine.setSplWeightingMode(weighting)
    }

    fun setLiteVisualsEnabled(enabled: Boolean) {
        _liteVisualsEnabled.value = enabled
        localStore.setLiteVisualsEnabled(enabled)
    }

    // =====================================================================
    // Audio Laboratory & Unified DSP Actions
    // =====================================================================
    private fun schedulePersistDspAndEq(immediate: Boolean = false) {
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastDspPersistUptimeMs > 90L) {
            dspPersistBurstCount = 0
        }
        dspPersistBurstCount += 1
        lastDspPersistUptimeMs = now

        if (immediate || dspPersistBurstCount <= 3) {
            debouncedDspSaveJob?.cancel()
            localStore.saveDspSettings(_dspSettings.value)
            localStore.saveEqualizerSettings(_equalizerSettings.value)
            return
        }

        debouncedDspSaveJob?.cancel()
        debouncedDspSaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(220)
            localStore.saveDspSettings(_dspSettings.value)
            localStore.saveEqualizerSettings(_equalizerSettings.value)
        }
    }

    private fun updateAndCommitDsp(
        updated: DspSettings,
        immediatePersist: Boolean = false,
        notifyLimiterGuard: Boolean = false
    ) {
        val (safeDsp, guardMsg) = enforceAntiClippingLimits(updated, _equalizerSettings.value, _bassMode.value)
        _dspSettings.value = safeDsp
        audioEngine.applyDspSettings(safeDsp, _equalizerSettings.value)
        _capabilityReport.value = audioEngine.capabilityReport
        schedulePersistDspAndEq(immediate = immediatePersist)
        if (notifyLimiterGuard && guardMsg != null) {
            reportSystemFault(
                title = "Protección Anti-Clipping Activa",
                message = guardMsg,
                severity = FaultSeverity.INFO,
                sendSystemNotification = false
            )
        }
    }

    private fun updateAndCommitEq(updated: EqualizerSettings, immediatePersist: Boolean = false) {
        _equalizerSettings.value = updated
        val (safeDsp, _) = enforceAntiClippingLimits(_dspSettings.value, updated, _bassMode.value)
        _dspSettings.value = safeDsp
        audioEngine.applyDspSettings(safeDsp, updated)
        _capabilityReport.value = audioEngine.capabilityReport
        schedulePersistDspAndEq(immediate = immediatePersist)
    }

    fun toggleDspPower(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(dspMasterEnabled = enabled), immediatePersist = true)
    }

    fun toggleDspMaster() {
        toggleDspPower(!_dspSettings.value.dspMasterEnabled)
    }

    fun setMasterGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(masterGainDb = gainDb.coerceIn(-18f, 18f)))
    }

    fun setDspPreampGain(gainDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(preampDb = gainDb.coerceIn(-12f, 18f)))
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

    fun setBassMode(mode: BassMode) {
        _bassMode.value = mode
        localStore.saveBassMode(mode)
        val current = _dspSettings.value
        val updated = current.copy(
            dspMasterEnabled = true,
            bassBoostEnabled = true,
            bassBoostDb = mode.defaultBoostDb,
            bassBoostFreqHz = mode.defaultCenterHz,
            subBassEnabled = true,
            subBassBoostDb = mode.defaultSubBassDb,
            bassPunchLevel = mode.defaultPunch,
            limiterEnabled = if (mode.forceLimiter) true else current.limiterEnabled,
            limiterCeilingDb = if (mode == BassMode.EXTREME) -0.8f else current.limiterCeilingDb
        )
        updateAndCommitDsp(updated, immediatePersist = true)
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

    fun setLoudness(enabled: Boolean, gainDb: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                loudnessEnabled = enabled,
                loudnessGainDb = gainDb.coerceIn(0f, 15f)
            )
        )
    }

    fun setBalancePan(pan: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(balancePan = pan.coerceIn(-1f, 1f)))
    }

    fun setStereoWidth(percent: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(stereoWidthPercent = percent.coerceIn(100f, 250f)))
    }

    fun toggleCompressor(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(compressorEnabled = enabled), immediatePersist = true)
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

    fun toggleLimiter(enabled: Boolean) {
        updateAndCommitDsp(
            _dspSettings.value.copy(limiterEnabled = enabled),
            immediatePersist = true,
            notifyLimiterGuard = !enabled
        )
    }

    fun setLimiterCeiling(ceilingDb: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(limiterCeilingDb = ceilingDb.coerceIn(-24f, -0.2f)))
    }

    fun setFilterHpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float = 0.707f) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                hpfEnabled = enabled,
                hpfFrequencyHz = freqHz.coerceIn(15f, 500f),
                hpfSlopeDb = slopeDb,
                hpfQ = q.coerceIn(0.5f, 2.0f)
            )
        )
    }

    fun setFilterLpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float = 0.707f) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                lpfEnabled = enabled,
                lpfFrequencyHz = freqHz.coerceIn(200f, 20000f),
                lpfSlopeDb = slopeDb,
                lpfQ = q.coerceIn(0.5f, 2.0f)
            )
        )
    }

    fun setHiResQuality(enabled: Boolean) {
        updateAndCommitDsp(_dspSettings.value.copy(hiResQualityEnabled = enabled), immediatePersist = true)
    }

    fun setTubeWarmth(level: Float) {
        updateAndCommitDsp(_dspSettings.value.copy(tubeWarmthLevel = level.coerceIn(0f, 10f)))
    }

    fun setPresence(enabled: Boolean, level: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                presenceEnabled = enabled,
                presenceLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun setClarity(enabled: Boolean, level: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                clarityEnabled = enabled,
                clarityLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun setExciter(enabled: Boolean, level: Float) {
        updateAndCommitDsp(
            _dspSettings.value.copy(
                exciterEnabled = enabled,
                exciterLevel = level.coerceIn(0f, 10f)
            )
        )
    }

    fun resetDspToDefaults() {
        val defaultDsp = DspSettings()
        val defaultEq = EqualizerSettings(
            bands10 = List(10) { 0f },
            bands15 = List(15) { 0f },
            bands31 = List(31) { 0f },
            activePresetName = "Flat"
        )
        _dspSettings.value = defaultDsp
        _equalizerSettings.value = defaultEq
        audioEngine.applyDspSettings(defaultDsp, defaultEq)
        localStore.saveDspSettings(defaultDsp)
        localStore.saveEqualizerSettings(defaultEq)
    }

    // =====================================================================
    // Equalizer 10-Band, 31-Band & Parametric Controls
    // =====================================================================
    fun toggleEqualizerPower(enabled: Boolean) {
        updateAndCommitEq(_equalizerSettings.value.copy(isEnabled = enabled), immediatePersist = true)
    }

    fun updateEqualizerMasterGain(gainDb: Float) {
        updateAndCommitEq(_equalizerSettings.value.copy(masterGainDb = gainDb.coerceIn(-12f, 12f)))
    }

    fun updateEqualizerBand10(index: Int, gainDb: Float) {
        val current10 = _equalizerSettings.value.bands10.toMutableList()
        if (index !in current10.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current10[index] = clamped

        val expanded31 = expand10To31(current10)
        val expanded15 = b10To15(current10)

        updateAndCommitEq(
            _equalizerSettings.value.copy(
                isEnabled = true,
                bands10 = current10,
                bands15 = expanded15,
                bands31 = expanded31,
                activePresetName = "Personalizado 10B"
            )
        )
    }

    fun updateEqualizerBand31(index: Int, gainDb: Float) {
        val current31 = _equalizerSettings.value.bands31.toMutableList()
        if (index !in current31.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current31[index] = clamped

        val current10 = EqualizerSettings.bands10From31(current31)
        val current15 = _equalizerSettings.value.bands15.toMutableList()
        val idx15 = (index / 2).coerceIn(0, current15.lastIndex)
        current15[idx15] = clamped

        updateAndCommitEq(
            _equalizerSettings.value.copy(
                isEnabled = true,
                bands10 = current10,
                bands31 = current31,
                bands15 = current15,
                activePresetName = "Personalizado 31B"
            )
        )
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val current15 = _equalizerSettings.value.bands15.toMutableList()
        if (index !in current15.indices) return
        val clamped = gainDb.coerceIn(-15f, 15f)
        current15[index] = clamped

        val current31 = _equalizerSettings.value.bands31.toMutableList()
        val center31 = (index * 2).coerceIn(0, current31.lastIndex)
        current31[center31] = clamped
        if (center31 - 1 >= 0) {
            current31[center31 - 1] = (clamped + current15.getOrElse(index - 1) { clamped }) * 0.5f
        }
        if (center31 + 1 <= current31.lastIndex) {
            current31[center31 + 1] = (clamped + current15.getOrElse(index + 1) { clamped }) * 0.5f
        }
        if (index == 14 && current31.size > 30) current31[30] = clamped

        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands10 = EqualizerSettings.bands10From31(current31),
                bands15 = current15,
                bands31 = current31,
                activePresetName = "Personalizado"
            )
        )
    }

    fun updateParametricBand(
        bandId: Int,
        freqHz: Float,
        gainDb: Float,
        q: Float,
        type: ParametricFilterType,
        enabled: Boolean
    ) {
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
        val new31 = if (preset.bands31.size == 31) preset.bands31 else expand10To31(preset.bands10)
        val new10 = if (preset.bands10.size == 10) preset.bands10 else EqualizerSettings.bands10From31(new31)
        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands10 = new10,
                bands15 = if (preset.bands.size == 15) preset.bands else b10To15(new10),
                bands31 = new31,
                parametricFreqHz = preset.paramFreq,
                parametricGainDb = preset.paramGain,
                parametricQ = preset.paramQ,
                activePresetName = preset.name,
                isEnabled = true
            )
        )
    }

    fun resetEqualizerFlat() {
        updateAndCommitEq(
            _equalizerSettings.value.copy(
                bands10 = List(10) { 0f },
                bands15 = List(15) { 0f },
                bands31 = List(31) { 0f },
                masterGainDb = 0f,
                parametricGainDb = 0f,
                activePresetName = "Flat"
            )
        )
    }

    fun saveCustomPreset(name: String, description: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val eq = _equalizerSettings.value
        val newPreset = EqPresetItem(
            name = clean,
            description = description.ifBlank { "Preset personalizado" },
            bands10 = eq.bands10,
            bands = eq.bands15,
            bands31 = eq.bands31,
            paramFreq = eq.parametricFreqHz,
            paramGain = eq.parametricGainDb,
            paramQ = eq.parametricQ,
            isCustom = true
        )
        val updatedList = _eqPresets.value.filter { it.name != clean } + newPreset
        _eqPresets.value = updatedList
        localStore.saveCustomPresets(updatedList)
        updateAndCommitEq(eq.copy(activePresetName = clean))
    }

    fun deletePreset(preset: EqPresetItem) {
        val updatedList = _eqPresets.value.filter { it.name != preset.name }
        _eqPresets.value = updatedList
        localStore.saveCustomPresets(updatedList)
        if (_equalizerSettings.value.activePresetName == preset.name) {
            selectPreset(EqPresetCatalog.presets.first())
        }
    }

    // =====================================================================
    // Full Sound Profiles (Local + Cloud Firestore)
    // =====================================================================
    fun saveCustomSoundProfile(name: String, description: String) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val profileId = "csp_${System.currentTimeMillis()}"
        val eq = _equalizerSettings.value
        val (safeDsp, _) = enforceAntiClippingLimits(_dspSettings.value, eq, _bassMode.value)
        val profile = CustomSoundProfile(
            id = profileId,
            name = cleanName,
            description = description.trim().ifBlank { "Configuración guardada" },
            dspSettings = safeDsp,
            eqBands10 = eq.bands10,
            eqBands15 = eq.bands15,
            eqBands31 = eq.bands31
        )
        val updated = listOf(profile) + _customSoundProfiles.value.filter { !it.name.equals(cleanName, ignoreCase = true) }
        _customSoundProfiles.value = updated
        localStore.saveCustomSoundProfiles(updated)

        val repo = getOrCreateFirebaseRepository()
        val uid = activeUserId
        if (repo != null && uid != null) {
            viewModelScope.launch {
                repo.saveSoundPreset(
                    CloudSoundPreset(
                        id = profileId,
                        userId = uid,
                        name = cleanName,
                        description = profile.description,
                        eq10Bands = eq.bands10.map { it.toDouble() },
                        eq31Bands = eq.bands31.map { it.toDouble() },
                        preampDb = safeDsp.preampDb.toDouble(),
                        bassBoostDb = safeDsp.bassBoostDb.toDouble(),
                        midGainDb = safeDsp.midGainDb.toDouble(),
                        trebleGainDb = safeDsp.trebleGainDb.toDouble(),
                        stereoBalance = safeDsp.balancePan.toDouble(),
                        virtualizerPercent = (safeDsp.stereoWidthPercent - 100f).coerceIn(0f, 100f).toDouble(),
                        compressorEnabled = safeDsp.compressorEnabled,
                        limiterEnabled = safeDsp.limiterEnabled,
                        hpfHz = safeDsp.hpfFrequencyHz.toDouble(),
                        lpfHz = safeDsp.lpfFrequencyHz.toDouble(),
                        subsonicHz = safeDsp.subsonicFrequencyHz.toDouble()
                    )
                )
            }
        }
    }

    fun applyCustomSoundProfile(profile: CustomSoundProfile) {
        val b31 = if (profile.eqBands31.size == 31) profile.eqBands31 else expand10To31(profile.eqBands10)
        val b10 = if (profile.eqBands10.size == 10) profile.eqBands10 else EqualizerSettings.bands10From31(b31)
        val newEq = _equalizerSettings.value.copy(
            isEnabled = true,
            bands10 = b10,
            bands15 = if (profile.eqBands15.size == 15) profile.eqBands15 else b10To15(b10),
            bands31 = b31,
            activePresetName = profile.name
        )
        val (safeDsp, _) = enforceAntiClippingLimits(profile.dspSettings, newEq, _bassMode.value)
        _dspSettings.value = safeDsp
        _equalizerSettings.value = newEq
        audioEngine.applyDspSettings(safeDsp, newEq)
        _capabilityReport.value = audioEngine.capabilityReport
        localStore.saveDspSettings(safeDsp)
        localStore.saveEqualizerSettings(newEq)
    }

    fun deleteCustomSoundProfile(id: String) {
        val updated = _customSoundProfiles.value.filter { it.id != id }
        _customSoundProfiles.value = updated
        localStore.saveCustomSoundProfiles(updated)
        viewModelScope.launch {
            getOrCreateFirebaseRepository()?.deleteSoundPreset(id)
        }
    }

    // =====================================================================
    // Live A/B Configuration Comparison
    // =====================================================================
    fun captureAbSlot(slot: AbSlot) {
        val eq = _equalizerSettings.value
        val dsp = _dspSettings.value
        val snapshot = DspAbSnapshot(
            slot = slot,
            name = "${slot.label} • ${eq.activePresetName}",
            dspSettings = dsp,
            bands10 = eq.bands10.toList(),
            bands31 = eq.bands31.toList(),
            savedAtMs = System.currentTimeMillis()
        )
        when (slot) {
            AbSlot.A -> _abSlotA.value = snapshot
            AbSlot.B -> _abSlotB.value = snapshot
        }
        _activeAbSlot.value = slot
        localStore.saveAbSnapshot(slot, snapshot)
        localStore.saveActiveAbSlot(slot)
    }

    fun applyAbSlot(slot: AbSlot) {
        val snapshot = when (slot) {
            AbSlot.A -> _abSlotA.value
            AbSlot.B -> _abSlotB.value
        } ?: run {
            captureAbSlot(slot)
            return
        }
        val b31 = if (snapshot.bands31.size == 31) snapshot.bands31 else expand10To31(snapshot.bands10)
        val b10 = if (snapshot.bands10.size == 10) snapshot.bands10 else EqualizerSettings.bands10From31(b31)
        val updatedEq = _equalizerSettings.value.copy(
            isEnabled = true,
            bands10 = b10,
            bands15 = b10To15(b10),
            bands31 = b31,
            activePresetName = snapshot.name
        )
        _dspSettings.value = snapshot.dspSettings
        _equalizerSettings.value = updatedEq
        _activeAbSlot.value = slot
        audioEngine.applyDspSettings(snapshot.dspSettings, updatedEq)
        localStore.saveDspSettings(snapshot.dspSettings)
        localStore.saveEqualizerSettings(updatedEq)
        localStore.saveActiveAbSlot(slot)
    }

    fun toggleAbComparison() {
        val snapA = _abSlotA.value
        val snapB = _abSlotB.value
        when {
            snapA == null && snapB == null -> {
                captureAbSlot(AbSlot.A)
            }
            snapA != null && snapB == null -> {
                captureAbSlot(AbSlot.B)
            }
            snapA == null && snapB != null -> {
                captureAbSlot(AbSlot.A)
            }
            else -> {
                val nextSlot = if (_activeAbSlot.value == AbSlot.A) AbSlot.B else AbSlot.A
                applyAbSlot(nextSlot)
            }
        }
    }

    fun clearAbSlot(slot: AbSlot) {
        when (slot) {
            AbSlot.A -> _abSlotA.value = null
            AbSlot.B -> _abSlotB.value = null
        }
        if (_activeAbSlot.value == slot) {
            _activeAbSlot.value = null
            localStore.saveActiveAbSlot(null)
        }
        localStore.saveAbSnapshot(slot, null)
    }

    // =====================================================================
    // YouTube Music Integration (Official App / Web Search Shortcuts)
    // =====================================================================
    fun setYtSearchQuery(query: String) {
        _ytSearchQuery.value = query
    }

    fun recordYtSearch(query: String) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        val updated = (listOf(clean) + _ytSearchHistory.value.filterNot { it.equals(clean, ignoreCase = true) }).take(12)
        _ytSearchHistory.value = updated
        localStore.saveYouTubeSearchHistory(updated)
    }

    fun saveYtShortcut(
        title: String,
        artist: String = "",
        customUrl: String? = null,
        favorite: Boolean = false
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val cleanArtist = artist.trim()
        val cleanUrl = customUrl?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
        val item = YouTubeMusicItem(
            id = "yt_${System.currentTimeMillis()}_${cleanTitle.hashCode()}",
            title = cleanTitle,
            artist = cleanArtist,
            category = if (cleanUrl != null) "Enlace guardado" else "Búsqueda guardada",
            customUrl = cleanUrl,
            isFavorite = favorite
        )
        val updated = listOf(item) + _ytSavedItems.value.filterNot {
            it.title.equals(cleanTitle, ignoreCase = true) && it.artist.equals(cleanArtist, ignoreCase = true)
        }
        _ytSavedItems.value = updated
        localStore.saveYouTubeMusicItems(updated)
    }

    fun toggleYtFavorite(id: String) {
        val updated = _ytSavedItems.value.map { item ->
            if (item.id == id) item.copy(isFavorite = !item.isFavorite) else item
        }
        _ytSavedItems.value = updated
        localStore.saveYouTubeMusicItems(updated)
    }

    fun removeYtItem(id: String) {
        val updated = _ytSavedItems.value.filterNot { it.id == id }
        _ytSavedItems.value = updated
        localStore.saveYouTubeMusicItems(updated)
    }

    fun clearYtHistory() {
        _ytSearchHistory.value = emptyList()
        localStore.saveYouTubeSearchHistory(emptyList())
    }

    fun buildYouTubeMusicSearchUri(query: String): Uri {
        val clean = query.trim()
        return if (clean.startsWith("https://music.youtube.com") || clean.startsWith("http://music.youtube.com")) {
            Uri.parse(clean)
        } else if (clean.isEmpty()) {
            Uri.parse("https://music.youtube.com/")
        } else {
            Uri.parse("https://music.youtube.com/search?q=${Uri.encode(clean)}")
        }
    }

    // =====================================================================
    // Online Music (Jamendo Official v3 API), Offline Downloads & Profile
    // =====================================================================
    fun setOnlineSearchQuery(query: String) {
        _onlineSearchState.value = _onlineSearchState.value.copy(query = query)
    }

    fun searchOnlineMusic(query: String = _onlineSearchState.value.query) {
        _onlineSearchState.value = _onlineSearchState.value.copy(
            query = query,
            isLoading = true,
            errorMessage = null,
            isConfigured = onlineMusicProvider.isConfigured()
        )
        viewModelScope.launch {
            when (val res = onlineMusicProvider.searchCatalog(query)) {
                is OnlineCatalogResult.Success -> {
                    _onlineSearchState.value = _onlineSearchState.value.copy(
                        isLoading = false,
                        isConfigured = true,
                        errorMessage = null,
                        tracks = res.data.tracks,
                        artists = res.data.artists,
                        albums = res.data.albums,
                        providerStatusMessage = if (res.data.tracks.isEmpty()) {
                            "Sin resultados para \"$query\"."
                        } else {
                            "${res.data.tracks.size} canciones legales encontradas en Jamendo"
                        }
                    )
                }
                is OnlineCatalogResult.Error -> {
                    _onlineSearchState.value = _onlineSearchState.value.copy(
                        isLoading = false,
                        isConfigured = !res.isConfigurationIssue,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun loadFeaturedOnlineMusic() {
        searchOnlineMusic("")
    }

    fun playOnlineTrack(item: OnlineTrackItem) {
        val offlineUri = downloadRepository.getLocalUriForTrack(item.id)
        val audioItem = item.toAudioTrackItem(localUri = offlineUri)
        playTrack(audioItem)
        selectSection(AppSection.PLAYER)
    }

    fun startTrackDownload(item: OnlineTrackItem) {
        val result = downloadRepository.enqueueAuthorizedDownload(item)
        result.onFailure { err ->
            reportSystemFault(
                title = "Descarga no disponible",
                message = err.localizedMessage ?: "Esta pista solo está disponible en streaming.",
                severity = FaultSeverity.INFO,
                sendSystemNotification = false
            )
        }
    }

    fun pauseDownload(downloadId: String) {
        downloadRepository.pauseDownload(downloadId)
    }

    fun resumeDownload(downloadId: String) {
        downloadRepository.resumeDownload(downloadId)
    }

    fun cancelDownload(downloadId: String) {
        downloadRepository.cancelDownload(downloadId)
    }

    fun deleteDownload(downloadId: String) {
        downloadRepository.deleteDownload(downloadId)
    }

    fun clearAllDownloads(): Long = downloadRepository.clearAllDownloads()

    fun playDownloadedTrack(item: DownloadItem) {
        if (item.status != DownloadStatus.COMPLETED) return
        val file = java.io.File(item.localPath)
        if (!file.exists()) {
            reportSystemFault(
                title = "Archivo offline no encontrado",
                message = "El archivo descargado fue eliminado o movido. Puedes volver a descargarlo.",
                severity = FaultSeverity.WARNING,
                sendSystemNotification = false
            )
            return
        }
        val audioItem = AudioTrackItem(
            id = "offline_${item.trackId}",
            title = item.title,
            artist = item.artist,
            album = item.album,
            durationSeconds = item.durationSeconds,
            category = TrackCategory.USER_CUSTOM,
            frequencyDescription = "Descarga Offline • MP3",
            customUri = Uri.fromFile(file),
            albumArtUri = item.artworkUrl?.let { Uri.parse(it) },
            audioFormatLabel = "Offline HD",
            folderName = "Descargas"
        )
        playTrack(audioItem)
        selectSection(AppSection.PLAYER)
    }

    fun updateLocalProfileName(name: String) {
        val clean = name.trim().take(32).ifBlank { "Cabina de Luis Miguel" }
        _localProfileName.value = clean
        localStore.localProfileName = clean
    }

    fun getLocalProfileStats(): LocalProfileStats {
        val totalPlaysCount = _trackPlayCounts.value.values.sum()
        val customPresetsCount = _eqPresets.value.count { it.isCustom } + _customSoundProfiles.value.size
        val completedDownloads = downloads.value.count { it.status == DownloadStatus.COMPLETED }
        return LocalProfileStats(
            displayName = _localProfileName.value,
            totalPlays = totalPlaysCount,
            totalListeningSeconds = _totalListeningSeconds.value,
            customPresetsCount = customPresetsCount,
            playlistsCount = _playlists.value.size,
            favoritesCount = _tracks.value.count { it.isFavorite },
            downloadsCount = completedDownloads
        )
    }

    fun clearCachedData() {
        runCatching {
            context.cacheDir?.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }
        _recentTrackKeys.value = emptyList()
        localStore.saveRecentTrackKeys(emptyList())
        _faultAlerts.value = emptyList()
        _latestBannerAlert.value = null
    }

    fun toggleMicRta(hasRecordPermission: Boolean) {
        if (!hasRecordPermission) return
        if (audioEngine.isMicRtaActive) {
            audioEngine.stopMicRta()
            _isMicRta.value = false
        } else {
            val started = audioEngine.startMicRta(true)
            _isMicRta.value = started
        }
        audioEngine.setVisualizerActive(uiActive && needsLiveVisualizer())
        audioEngine.ensureVisualizerAttached()
    }

    private fun expand10To31(b10: List<Float>): List<Float> {
        if (b10.size != 10) return List(31) { 0f }
        val anchors = intArrayOf(2, 5, 8, 11, 14, 17, 20, 23, 26, 29)
        val out = FloatArray(31)
        for (i in 0 until 31) {
            out[i] = when {
                i <= anchors[0] -> b10[0]
                i >= anchors[9] -> b10[9]
                else -> {
                    var seg = 0
                    while (seg < 8 && i > anchors[seg + 1]) seg++
                    val a = anchors[seg]
                    val b = anchors[seg + 1]
                    val t = (i - a).toFloat() / (b - a).toFloat()
                    b10[seg] * (1f - t) + b10[seg + 1] * t
                }
            }
        }
        return out.toList()
    }

    private fun b10To15(b10: List<Float>): List<Float> {
        if (b10.size != 10) return List(15) { 0f }
        return List(15) { idx ->
            val mapped = ((idx / 14f) * 9f).toInt().coerceIn(0, 9)
            b10[mapped]
        }
    }

    override fun onCleared() {
        if (debouncedDspSaveJob?.isActive == true) {
            debouncedDspSaveJob?.cancel()
            localStore.saveDspSettings(_dspSettings.value)
            localStore.saveEqualizerSettings(_equalizerSettings.value)
        }
        cancelCloudCollectors()
        audioEngine.onSettingsChangedListener = null
        audioEngine.onPlaybackStateChangedListener = null
        audioEngine.onOutputDeviceChangedListener = null
        audioEngine.onAudioFaultListener = null
        audioEngine.onPlaybackErrorListener = null
        controllerFuture?.let {
            try {
                MediaController.releaseFuture(it)
            } catch (_: Throwable) {}
        }
        mediaController = null
        super.onCleared()
    }

    companion object {
        fun enforceAntiClippingLimits(
            dsp: DspSettings,
            eq: EqualizerSettings,
            bassMode: BassMode
        ): Pair<DspSettings, String?> {
            if (!dsp.dspMasterEnabled) return dsp to null
            val maxEqGain = eq.bands31.maxOrNull()?.coerceAtLeast(0f) ?: 0f
            val bassContrib = if (dsp.bassBoostEnabled) dsp.bassBoostDb * 0.45f else 0f
            val effectiveBoost = dsp.preampDb + dsp.masterGainDb + bassContrib + maxEqGain
            val highGainRisk = bassMode.forceLimiter || effectiveBoost >= 14f

            return if (highGainRisk) {
                val fixedCeiling = dsp.limiterCeilingDb.coerceIn(-24f, -0.5f)
                val safeDsp = dsp.copy(
                    limiterEnabled = true,
                    limiterCeilingDb = fixedCeiling
                )
                val warning = if (!dsp.limiterEnabled) {
                    "El limitador anti-saturación se mantuvo activo por seguridad debido al nivel de ganancia o modo ${bassMode.label}."
                } else null
                safeDsp to warning
            } else {
                dsp.copy(limiterCeilingDb = dsp.limiterCeilingDb.coerceIn(-24f, -0.2f)) to null
            }
        }
    }
}
