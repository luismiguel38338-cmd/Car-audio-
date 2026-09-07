package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CarAudioEngine
import com.example.model.AmpTelemetry
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.RtaBand
import com.example.model.TrackCategory
import com.example.notification.CarAudioNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DSP_PROCESSOR("DSP Crossover"),
    RTA_ANALYZER("Analizador RTA"),
    PLAYER("Reproductor"),
    PLANTA_MONITOR("Monitor Planta"),
    THEMES("12 Temas")
}

class CarAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val audioEngine = CarAudioEngine(context)

    // Current Theme (Defaults to Cyber Neon Green)
    private val _currentTheme = MutableStateFlow(CarAudioThemeType.NEON_CYBER)
    val currentTheme: StateFlow<CarAudioThemeType> = _currentTheme.asStateFlow()

    // Active Tab
    private val _selectedTab = MutableStateFlow(AppTab.DSP_PROCESSOR)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // DSP Settings
    private val _dspSettings = MutableStateFlow(DspSettings())
    val dspSettings: StateFlow<DspSettings> = _dspSettings.asStateFlow()

    // Amp Telemetry
    private val _ampTelemetry = MutableStateFlow(AmpTelemetry())
    val ampTelemetry: StateFlow<AmpTelemetry> = _ampTelemetry.asStateFlow()

    // Audio Playback state
    private val _tracks = MutableStateFlow(CarAudioEngine.getBuiltInTracks())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow(CarAudioEngine.getBuiltInTracks().first())
    val currentTrack: StateFlow<AudioTrackItem> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSeconds = MutableStateFlow(0)
    val playbackSeconds: StateFlow<Int> = _playbackSeconds.asStateFlow()

    // Real-time RTA state
    private val _splDb = MutableStateFlow(65f)
    val splDb: StateFlow<Float> = _splDb.asStateFlow()

    private val _peakSplDb = MutableStateFlow(70f)
    val peakSplDb: StateFlow<Float> = _peakSplDb.asStateFlow()

    private val _leftVu = MutableStateFlow(0.4f)
    val leftVu: StateFlow<Float> = _leftVu.asStateFlow()

    private val _rightVu = MutableStateFlow(0.4f)
    val rightVu: StateFlow<Float> = _rightVu.asStateFlow()

    private val _rtaBands = MutableStateFlow<List<RtaBand>>(emptyList())
    val rtaBands: StateFlow<List<RtaBand>> = _rtaBands.asStateFlow()

    private val _waveform = MutableStateFlow(FloatArray(64))
    val waveform: StateFlow<FloatArray> = _waveform.asStateFlow()

    private val _isMicRta = MutableStateFlow(false)
    val isMicRta: StateFlow<Boolean> = _isMicRta.asStateFlow()

    // Permissions state
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _pendingPermissionType = MutableStateFlow("notification")
    val pendingPermissionType: StateFlow<String> = _pendingPermissionType.asStateFlow()

    private var lastClipAlertTime = 0L
    private var lastVoltAlertTime = 0L

    init {
        CarAudioNotificationHelper.initNotificationChannels(context)
        startTelemetryAndVisualizerLoop()
    }

    private fun startTelemetryAndVisualizerLoop() {
        viewModelScope.launch {
            var simStep = 0
            while (isActive) {
                // Update audio playback state from engine
                _isPlaying.value = audioEngine.isPlaying
                _currentTrack.value = audioEngine.currentTrack
                _playbackSeconds.value = audioEngine.playbackPositionSeconds
                _splDb.value = audioEngine.currentSplDb
                _peakSplDb.value = audioEngine.peakSplDb
                _leftVu.value = audioEngine.leftVuLevel
                _rightVu.value = audioEngine.rightVuLevel
                _rtaBands.value = audioEngine.currentRtaBands
                _waveform.value = audioEngine.currentWaveform
                _isMicRta.value = audioEngine.isMicRtaActive

                // Real-time Amp Telemetry physics simulation
                val currentDsp = _dspSettings.value
                val playing = _isPlaying.value

                // Dynamic voltage behavior:
                // Alternator 14.4V normally; drops when heavy bass boost and high SPL occurs
                val bassLoad = if (playing) {
                    (currentDsp.bassBoostDb / 18f) * 1.8f + (_leftVu.value * 0.9f)
                } else {
                    0.0f
                }
                val jitter = (Math.random().toFloat() - 0.5f) * 0.15f
                val calcVoltage = (14.4f - bassLoad + jitter).coerceIn(10.8f, 14.6f)

                // Clipping detection: if gain + bass boost exceeds threshold
                val totalGain = currentDsp.masterGainDb + currentDsp.bassBoostDb
                val clipCondition = playing && (totalGain > 16.5f || (totalGain > 12.0f && _leftVu.value > 0.88f))

                // Power in Watts RMS
                val baseWatts = if (playing) {
                    (800 + (_leftVu.value * 1600f) + (currentDsp.bassBoostDb * 40f)).toInt()
                } else {
                    45 // Idle standby
                }

                // Temperature
                val tempTarget = if (clipCondition) 68f else if (playing) 48f else 36f
                val currentTemp = _ampTelemetry.value.temperatureC + (tempTarget - _ampTelemetry.value.temperatureC) * 0.02f

                val isLowVolt = calcVoltage < 11.8f
                val newClipCount = if (clipCondition) _ampTelemetry.value.clipCount + 1 else _ampTelemetry.value.clipCount

                _ampTelemetry.value = _ampTelemetry.value.copy(
                    voltage = calcVoltage,
                    temperatureC = currentTemp,
                    outputPowerWatts = baseWatts,
                    isClipping = clipCondition,
                    clipCount = newClipCount,
                    isOverheated = currentTemp > 75f,
                    isLowVoltage = isLowVolt,
                    protectionModeActive = isLowVolt || currentTemp > 85f
                )

                // Periodic or event notifications
                val now = System.currentTimeMillis()
                if (clipCondition && (now - lastClipAlertTime > 8000)) {
                    lastClipAlertTime = now
                    CarAudioNotificationHelper.showClippingAlertNotification(context, newClipCount)
                }

                if (isLowVolt && (now - lastVoltAlertTime > 15000)) {
                    lastVoltAlertTime = now
                    CarAudioNotificationHelper.showVoltageAlertNotification(context, calcVoltage)
                }

                // Update sticky notification every ~4 seconds if playing
                if (simStep % 40 == 0 && playing) {
                    CarAudioNotificationHelper.showMonitorNotification(
                        context = context,
                        trackTitle = _currentTrack.value.title,
                        isPlaying = playing,
                        voltage = calcVoltage,
                        temperatureC = currentTemp,
                        isClipping = clipCondition
                    )
                }

                simStep++
                delay(100)
            }
        }
    }

    fun setTheme(theme: CarAudioThemeType) {
        _currentTheme.value = theme
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun playTrack(track: AudioTrackItem) {
        audioEngine.playTrack(track, _dspSettings.value)
        _currentTrack.value = track
        _isPlaying.value = true
        CarAudioNotificationHelper.showMonitorNotification(
            context,
            track.title,
            isPlaying = true,
            voltage = _ampTelemetry.value.voltage,
            temperatureC = _ampTelemetry.value.temperatureC,
            isClipping = _ampTelemetry.value.isClipping
        )
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause(_dspSettings.value)
        _isPlaying.value = audioEngine.isPlaying
        CarAudioNotificationHelper.showMonitorNotification(
            context,
            _currentTrack.value.title,
            isPlaying = _isPlaying.value,
            voltage = _ampTelemetry.value.voltage,
            temperatureC = _ampTelemetry.value.temperatureC,
            isClipping = _ampTelemetry.value.isClipping
        )
    }

    fun nextTrack() {
        val list = _tracks.value
        val currentIndex = list.indexOfFirst { it.id == _currentTrack.value.id }
        val nextIndex = if (currentIndex >= 0 && currentIndex < list.size - 1) currentIndex + 1 else 0
        playTrack(list[nextIndex])
    }

    fun previousTrack() {
        val list = _tracks.value
        val currentIndex = list.indexOfFirst { it.id == _currentTrack.value.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        playTrack(list[prevIndex])
    }

    fun seekTo(seconds: Int) {
        audioEngine.seekTo(seconds)
        _playbackSeconds.value = seconds
    }

    fun setVolume(volume: Float) {
        audioEngine.volumeFactor = volume.coerceIn(0f, 1f)
    }

    fun updateDspSettings(newSettings: DspSettings) {
        _dspSettings.value = newSettings
    }

    fun loadPreset(presetName: String) {
        val updated = when (presetName) {
            "Open Show Pro" -> DspSettings(
                hpfFrequencyHz = 80f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 16000f,
                subsonicFrequencyHz = 32f,
                bassBoostDb = 4f,
                bassBoostFreqHz = 50f,
                phaseDegrees = 0,
                timeAlignmentMs = 1.0f,
                masterGainDb = 2f,
                eqBands = listOf(3f, 4f, 2f, 1f, 4f, 6f, 7f, 6f),
                activePresetName = "Open Show Pro"
            )
            "SPL Bass Monster" -> DspSettings(
                hpfFrequencyHz = 20f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 85f,
                subsonicFrequencyHz = 28f,
                bassBoostDb = 12f,
                bassBoostFreqHz = 45f,
                phaseDegrees = 0,
                timeAlignmentMs = 0f,
                masterGainDb = 4f,
                eqBands = listOf(10f, 8f, 3f, -2f, -4f, -6f, -8f, -10f),
                activePresetName = "SPL Bass Monster"
            )
            "SQL Audiophile Studio" -> DspSettings(
                hpfFrequencyHz = 60f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 20000f,
                subsonicFrequencyHz = 25f,
                bassBoostDb = 1f,
                bassBoostFreqHz = 40f,
                phaseDegrees = 0,
                timeAlignmentMs = 2.4f,
                masterGainDb = 0f,
                eqBands = listOf(1f, 1f, 0.5f, 0f, 0f, 1f, 1.5f, 1f),
                activePresetName = "SQL Audiophile Studio"
            )
            "Reggaeton & Urbano" -> DspSettings(
                hpfFrequencyHz = 35f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 14000f,
                subsonicFrequencyHz = 30f,
                bassBoostDb = 8f,
                bassBoostFreqHz = 48f,
                phaseDegrees = 0,
                timeAlignmentMs = 0.8f,
                masterGainDb = 3f,
                eqBands = listOf(7f, 6f, 2f, 0f, 2f, 3f, 5f, 4f),
                activePresetName = "Reggaeton & Urbano"
            )
            "Rock & Metal Punch" -> DspSettings(
                hpfFrequencyHz = 45f,
                hpfSlopeDb = 12,
                lpfFrequencyHz = 18000f,
                subsonicFrequencyHz = 25f,
                bassBoostDb = 3f,
                bassBoostFreqHz = 60f,
                phaseDegrees = 0,
                timeAlignmentMs = 1.2f,
                masterGainDb = 1f,
                eqBands = listOf(4f, 5f, 3f, 1f, 0f, 2f, 4f, 5f),
                activePresetName = "Rock & Metal Punch"
            )
            else -> DspSettings(activePresetName = presetName)
        }
        _dspSettings.value = updated
    }

    fun toggleMicRta(hasRecordPermission: Boolean) {
        if (!hasRecordPermission) {
            _pendingPermissionType.value = "audio"
            _showPermissionDialog.value = true
            return
        }
        if (audioEngine.isMicRtaActive) {
            audioEngine.stopMicRta()
            _isMicRta.value = false
        } else {
            audioEngine.startMicRta(true)
            _isMicRta.value = true
        }
    }

    fun requestNotificationPermission() {
        _pendingPermissionType.value = "notification"
        _showPermissionDialog.value = true
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    fun testClippingAlert() {
        val newClipCount = _ampTelemetry.value.clipCount + 1
        _ampTelemetry.value = _ampTelemetry.value.copy(isClipping = true, clipCount = newClipCount)
        CarAudioNotificationHelper.showClippingAlertNotification(context, newClipCount)
    }

    fun testVoltageAlert() {
        val criticalVolt = 11.2f
        _ampTelemetry.value = _ampTelemetry.value.copy(voltage = criticalVolt, isLowVoltage = true)
        CarAudioNotificationHelper.showVoltageAlertNotification(context, criticalVolt)
    }

    fun sendStatusNotification() {
        CarAudioNotificationHelper.showMonitorNotification(
            context = context,
            trackTitle = _currentTrack.value.title,
            isPlaying = _isPlaying.value,
            voltage = _ampTelemetry.value.voltage,
            temperatureC = _ampTelemetry.value.temperatureC,
            isClipping = _ampTelemetry.value.isClipping
        )
    }

    fun addCustomUserTrack(uri: Uri, name: String) {
        val newTrack = AudioTrackItem(
            id = "user_${System.currentTimeMillis()}",
            title = name.substringBeforeLast(".").ifBlank { "Pista Car Audio" },
            artist = "Música de mi Equipo",
            durationSeconds = 240,
            category = TrackCategory.USER_CUSTOM,
            frequencyDescription = "Audio local seleccionado desde el dispositivo",
            isSynthesized = false,
            customUri = uri
        )
        val updatedList = listOf(newTrack) + _tracks.value
        _tracks.value = updatedList
        playTrack(newTrack)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
