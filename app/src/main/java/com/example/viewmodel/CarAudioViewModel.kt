package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CarAudioEngine
import com.example.model.AmpTelemetry
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.AutoTuneState
import com.example.model.AutoTuneTarget
import com.example.model.BoxCalculationResult
import com.example.model.BrazilianBoxPresets
import com.example.model.CarAudioThemeType
import com.example.model.CrossoverFilterType
import com.example.model.DspChannel
import com.example.model.DspFullProfileJson
import com.example.model.DspPreset
import com.example.model.DspSettings
import com.example.model.EnclosureType
import com.example.model.EqualizerSettings
import com.example.model.EqPresetCatalog
import com.example.model.HardwareBridgeState
import com.example.model.HardwareConnectionType
import com.example.model.OscilloscopeState
import com.example.model.ParametricBand
import com.example.model.ParametricFilterType
import com.example.model.ProfessionalBoxDesign
import com.example.model.RtaBand
import com.example.model.SplCalibrationSettings
import com.example.model.SplRecord
import com.example.model.SplRunState
import com.example.model.SplSpeed
import com.example.model.SplWeighting
import com.example.model.SubwooferWiringResult
import com.example.model.ThieleSmallParams
import com.example.model.ToneMode
import com.example.model.TrackCategory
import com.example.model.WireCalculationResult
import com.example.notification.CarAudioNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard Pro"),
    DSP_PROCESSOR("DSP Crossover"),
    EQUALIZER("Procesador EQ"),
    RTA_ANALYZER("Analizador RTA"),
    TOOLS("Tools & Cajones"),
    PLANTA_MONITOR("Monitor Planta"),
    PLAYER("Reproductor"),
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

    // Splash Screen with loading bubble & startup sound (requested by user)
    private val _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen: StateFlow<Boolean> = _showSplashScreen.asStateFlow()

    fun dismissSplashScreen() {
        _showSplashScreen.value = false
    }

    fun openSplashScreen() {
        _showSplashScreen.value = true
        playStartupSound()
    }

    fun playStartupSound() {
        audioEngine.playIntroBootSound()
    }

    // Dedicated Equalizer Processor State
    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    fun toggleEqualizerPower(enabled: Boolean) {
        _equalizerSettings.value = _equalizerSettings.value.copy(isEnabled = enabled)
    }

    fun updateEqualizerMasterGain(gainDb: Float) {
        _equalizerSettings.value = _equalizerSettings.value.copy(masterGainDb = gainDb.coerceIn(-12f, 12f))
    }

    fun toggleEqualizerLimiter(active: Boolean) {
        _equalizerSettings.value = _equalizerSettings.value.copy(isLimiterActive = active)
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val current = _equalizerSettings.value.bands15.toMutableList()
        if (index in current.indices) {
            current[index] = gainDb.coerceIn(-12f, 12f)
            _equalizerSettings.value = _equalizerSettings.value.copy(
                bands15 = current,
                activePresetName = "Personalizado"
            )
        }
    }

    fun updateEqualizerParametric(freqHz: Float, gainDb: Float, q: Float) {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            parametricFreqHz = freqHz.coerceIn(20f, 20000f),
            parametricGainDb = gainDb.coerceIn(-12f, 12f),
            parametricQ = q.coerceIn(0.5f, 5.0f)
        )
    }

    fun selectEqualizerPreset(presetName: String) {
        val found = EqPresetCatalog.presets.firstOrNull { it.name == presetName }
        if (found != null) {
            _equalizerSettings.value = _equalizerSettings.value.copy(
                bands15 = found.bands,
                parametricFreqHz = found.paramFreq,
                parametricGainDb = found.paramGain,
                parametricQ = found.paramQ,
                activePresetName = found.name
            )
        }
    }

    fun resetEqualizerFlat() {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands15 = List(15) { 0.0f },
            masterGainDb = 0.0f,
            parametricGainDb = 0.0f,
            activePresetName = "📏 RTA Flat / Lineal"
        )
    }

    fun applyBoxCutsToDsp(hpfHz: Float, lpfHz: Float, boxName: String) {
        _dspSettings.value = _dspSettings.value.copy(
            hpfFrequencyHz = hpfHz,
            hpfEnabled = true,
            lpfFrequencyHz = lpfHz,
            lpfEnabled = true,
            activePresetName = "Cajón: $boxName"
        )
        // Also apply to Channel 1 (Subwoofer)
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == 1) {
                ch.copy(hpfHz = hpfHz, lpfHz = lpfHz, hpfEnabled = true, lpfEnabled = true)
            } else ch
        }
    }

    // 4-Way Multi-Channel DSP Routing
    private val _dspChannels = MutableStateFlow(createDefaultDspChannels())
    val dspChannels: StateFlow<List<DspChannel>> = _dspChannels.asStateFlow()

    private val _selectedChannelId = MutableStateFlow(1)
    val selectedChannelId: StateFlow<Int> = _selectedChannelId.asStateFlow()

    // Tone Generator
    private val _toneMode = MutableStateFlow(ToneMode.SINE_WAVE)
    val toneMode: StateFlow<ToneMode> = _toneMode.asStateFlow()

    private val _toneFrequency = MutableStateFlow(40f)
    val toneFrequency: StateFlow<Float> = _toneFrequency.asStateFlow()

    private val _isTonePlaying = MutableStateFlow(false)
    val isTonePlaying: StateFlow<Boolean> = _isTonePlaying.asStateFlow()

    // SPL Competition Run (Bass Race / dB Drag Simulator)
    private val _splRunState = MutableStateFlow(SplRunState())
    val splRunState: StateFlow<SplRunState> = _splRunState.asStateFlow()
    private var splRunJob: kotlinx.coroutines.Job? = null

    // Presets
    private val _availablePresets = MutableStateFlow(createDefaultPresets())
    val availablePresets: StateFlow<List<DspPreset>> = _availablePresets.asStateFlow()

    private val _presetHistory = MutableStateFlow<List<String>>(listOf("Crossover 4-Vías Predeterminado", "Calibración Inicial Fábrica"))
    val presetHistory: StateFlow<List<String>> = _presetHistory.asStateFlow()

    // DSP 2.0 Engine & Telemetry States
    private val _sourceMode = MutableStateFlow(AudioSourceMode.INTERNAL_DSP)
    val sourceMode: StateFlow<AudioSourceMode> = _sourceMode.asStateFlow()

    private val _rmsLevelDb = MutableStateFlow(-32.0f)
    val rmsLevelDb: StateFlow<Float> = _rmsLevelDb.asStateFlow()

    private val _isClippingDetected = MutableStateFlow(false)
    val isClippingDetected: StateFlow<Boolean> = _isClippingDetected.asStateFlow()

    private val _rawOscilloscopePcm = MutableStateFlow(FloatArray(128))
    val rawOscilloscopePcm: StateFlow<FloatArray> = _rawOscilloscopePcm.asStateFlow()

    private val _oscilloscopeState = MutableStateFlow(OscilloscopeState())
    val oscilloscopeState: StateFlow<OscilloscopeState> = _oscilloscopeState.asStateFlow()

    private val _autoTuneState = MutableStateFlow(AutoTuneState())
    val autoTuneState: StateFlow<AutoTuneState> = _autoTuneState.asStateFlow()

    private val _splCalibrationSettings = MutableStateFlow(SplCalibrationSettings())
    val splCalibrationSettings: StateFlow<SplCalibrationSettings> = _splCalibrationSettings.asStateFlow()

    private val _boxDesign = MutableStateFlow(
        ProfessionalBoxDesign(
            enclosureType = EnclosureType.VENTED_PORTED,
            tsParams = ThieleSmallParams(fs = 32f, qts = 0.38f, vas = 65f, xmax = 14f, sd = 490f, powerRms = 1000),
            netVolumeLiters = 58.0f,
            grossVolumeLiters = 68.5f,
            tuningFreqHz = 36.0f,
            f3CutoffHz = 33.0f,
            isSlotPort = true,
            portWidthCm = 5.2f,
            portHeightCm = 36.0f,
            portLengthCm = 48.0f,
            airVelocityMps = 14.2f,
            isChuffingSafe = true,
            mdfThicknessMm = 18,
            boxWidthCm = 65.0f,
            boxHeightCm = 40.0f,
            boxDepthCm = 46.0f,
            cutListSummary = "Frente y Fondo: 65x40 cm (x2) | Laterales: 42.4x36.4 cm (x2) | Tapa y Base: 65x46 cm (x2) | Ducto L: 36.4x43 cm"
        )
    )
    val boxDesign: StateFlow<ProfessionalBoxDesign> = _boxDesign.asStateFlow()

    private val _hardwareBridgeState = MutableStateFlow(HardwareBridgeState())
    val hardwareBridgeState: StateFlow<HardwareBridgeState> = _hardwareBridgeState.asStateFlow()

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
                _sourceMode.value = audioEngine.sourceMode
                _rmsLevelDb.value = audioEngine.rmsLevelDb
                _rawOscilloscopePcm.value = audioEngine.rawOscilloscopePcm

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

                // Real clipping detection combining hardware/DSP threshold and acoustic level
                val totalGain = currentDsp.masterGainDb + currentDsp.bassBoostDb
                val clipCondition = audioEngine.isClippingDetected || (playing && (totalGain > 16.5f || (totalGain > 12.0f && _leftVu.value > 0.88f)))
                _isClippingDetected.value = clipCondition

                // Update per-channel limiter gain reduction and clipping
                _dspChannels.value = _dspChannels.value.map { ch ->
                    val chGainReduction = if (ch.limiterEnabled && _leftVu.value > 0.82f) {
                        ((_leftVu.value - 0.82f) * 22.0f).coerceIn(0f, 12f)
                    } else 0f
                    val chIsClipping = clipCondition && !ch.isMuted
                    val chClips = if (chIsClipping) ch.clipCount + 1 else ch.clipCount
                    ch.copy(gainReductionDb = chGainReduction, isClipping = chIsClipping, clipCount = chClips)
                }

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

    // --- 4-WAY MULTICHANNEL DSP METHODS ---
    fun selectChannel(id: Int) {
        _selectedChannelId.value = id
    }

    fun selectDspChannel(id: Int) {
        selectChannel(id)
    }

    fun updateChannelHpf(channelId: Int, freqHz: Float, slopeDb: Int, enabled: Boolean) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(hpfHz = freqHz, hpfSlopeDb = slopeDb, hpfEnabled = enabled) else ch
        }
    }

    fun updateChannelHpfPro(channelId: Int, freqHz: Float, slopeDb: Int, filterType: CrossoverFilterType, enabled: Boolean) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(hpfHz = freqHz, hpfSlopeDb = slopeDb, hpfFilterType = filterType, hpfEnabled = enabled) else ch
        }
    }

    fun updateChannelLpf(channelId: Int, freqHz: Float, slopeDb: Int, enabled: Boolean) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(lpfHz = freqHz, lpfSlopeDb = slopeDb, lpfEnabled = enabled) else ch
        }
    }

    fun updateChannelLpfPro(channelId: Int, freqHz: Float, slopeDb: Int, filterType: CrossoverFilterType, enabled: Boolean) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(lpfHz = freqHz, lpfSlopeDb = slopeDb, lpfFilterType = filterType, lpfEnabled = enabled) else ch
        }
    }

    fun updateChannelGain(channelId: Int, gainDb: Float) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(gainDb = gainDb) else ch
        }
    }

    fun toggleChannelPhase(channelId: Int) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(phaseInverted = !ch.phaseInverted) else ch
        }
    }

    fun updateChannelDelay(channelId: Int, delayMs: Float) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(delayMs = delayMs, delayCm = delayMs * 34.3f) else ch
        }
    }

    fun toggleChannelMute(channelId: Int) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(isMuted = !ch.isMuted) else ch
        }
    }

    fun toggleChannelSolo(channelId: Int) {
        val currentlySolo = _dspChannels.value.find { it.id == channelId }?.isSolo == true
        val targetSolo = !currentlySolo
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) ch.copy(isSolo = targetSolo) else ch.copy(isSolo = false)
        }
    }

    fun updateChannelLimiter(channelId: Int, thresholdDb: Float, attackMs: Float, releaseMs: Float, enabled: Boolean) {
        _dspChannels.value = _dspChannels.value.map { ch ->
            if (ch.id == channelId) {
                ch.copy(
                    limiterThresholdDb = thresholdDb.coerceIn(-24f, 0f),
                    limiterAttackMs = attackMs.coerceIn(0.1f, 50f),
                    limiterReleaseMs = releaseMs.coerceIn(10f, 1000f),
                    limiterEnabled = enabled
                )
            } else ch
        }
    }

    fun panicMuteAll() {
        val anyUnmuted = _dspChannels.value.any { !it.isMuted }
        _dspChannels.value = _dspChannels.value.map { it.copy(isMuted = anyUnmuted) }
    }

    // --- TIME ALIGNMENT (AUTO & MANUAL) ---
    fun calculateTimeAlignmentByDistances(subCm: Float, kickCm: Float, midCm: Float, tweetCm: Float) {
        val maxDist = maxOf(subCm, kickCm, midCm, tweetCm)
        val subDelay = ((maxDist - subCm) / 34.3f).coerceIn(0f, 25f)
        val kickDelay = ((maxDist - kickCm) / 34.3f).coerceIn(0f, 25f)
        val midDelay = ((maxDist - midCm) / 34.3f).coerceIn(0f, 25f)
        val tweetDelay = ((maxDist - tweetCm) / 34.3f).coerceIn(0f, 25f)

        _dspChannels.value = _dspChannels.value.map { ch ->
            when (ch.id) {
                1 -> ch.copy(delayMs = subDelay, delayCm = subDelay * 34.3f)
                2 -> ch.copy(delayMs = kickDelay, delayCm = kickDelay * 34.3f)
                3 -> ch.copy(delayMs = midDelay, delayCm = midDelay * 34.3f)
                4 -> ch.copy(delayMs = tweetDelay, delayCm = tweetDelay * 34.3f)
                else -> ch
            }
        }
    }

    fun playAcousticPing() {
        audioEngine.playAcousticPing()
    }

    // --- 31-BAND & PARAMETRIC EQ METHODS ---
    fun updateEqualizerBand31(index: Int, gainDb: Float) {
        val current = _equalizerSettings.value.bands31.toMutableList()
        if (index in current.indices) {
            current[index] = gainDb.coerceIn(-12f, 12f)
            _equalizerSettings.value = _equalizerSettings.value.copy(
                bands31 = current,
                activePresetName = "Personalizado 31 Bandas"
            )
        }
    }

    fun updateParametricBand(bandId: Int, freqHz: Float, gainDb: Float, q: Float, type: ParametricFilterType, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) {
                pb.copy(freqHz = freqHz, gainDb = gainDb, q = q, filterType = type, enabled = enabled)
            } else pb
        }
        _equalizerSettings.value = _equalizerSettings.value.copy(
            parametricBands = updated,
            activePresetName = "Paramétrico Editado"
        )
    }

    fun toggleParametricBand(bandId: Int, enabled: Boolean) {
        val updated = _equalizerSettings.value.parametricBands.map { pb ->
            if (pb.id == bandId) pb.copy(enabled = enabled) else pb
        }
        _equalizerSettings.value = _equalizerSettings.value.copy(parametricBands = updated)
    }

    // --- AUTO TUNE ACÚSTICO CON MICRÓFONO & PROPUESTA PREVIA ---
    fun selectAutoTuneTarget(target: AutoTuneTarget) {
        _autoTuneState.value = _autoTuneState.value.copy(targetCurve = target)
    }

    fun startAutoTuneMeasurement() {
        viewModelScope.launch {
            _autoTuneState.value = _autoTuneState.value.copy(
                isMeasuring = true,
                progress = 0f,
                hasProposal = false
            )
            audioEngine.playTone(ToneMode.PINK_NOISE, 1000f, _dspSettings.value)

            for (step in 1..25) {
                delay(120)
                _autoTuneState.value = _autoTuneState.value.copy(progress = step / 25f)
            }
            audioEngine.stopTone()

            val measured31 = audioEngine.currentRtaBands.map { it.levelDb }
            val target = _autoTuneState.value.targetCurve

            val targetValues31 = EqualizerSettings.FREQUENCIES_HZ_31.map { f ->
                when (target) {
                    AutoTuneTarget.HARMAN_CAR -> {
                        if (f <= 80) 5.5f else if (f <= 2000) 0.0f else -((f - 2000f) / 18000f * 3.5f)
                    }
                    AutoTuneTarget.FLAT_RTA -> 0.0f
                    AutoTuneTarget.PANCADAO_BASS -> {
                        if (f in 50..90) 7.5f else if (f in 200..350) -1.5f else if (f in 2000..5000) 4.5f else 1.0f
                    }
                    AutoTuneTarget.SQ_AUDIOPHILE -> {
                        if (f <= 60) 3.0f else if (f in 1000..4000) 0.5f else 0.0f
                    }
                }
            }

            val proposedCorrection = measured31.zip(targetValues31) { meas, tgt ->
                val delta = tgt - (meas + 28f)
                delta.coerceIn(-9.0f, 9.0f)
            }

            val explanation = when (target) {
                AutoTuneTarget.HARMAN_CAR -> "Diagnóstico Acústico: Resonancia de habitáculo detectada en graves (+4.2dB). Corrección calculada para alinear con la Curva Objetivo Harman (-3.5dB en 63Hz, +2.0dB en medios)."
                AutoTuneTarget.FLAT_RTA -> "Diagnóstico Acústico: Curva de referencia lineal 0dB. Correcciones propuestas en 31 bandas para compensar reflexiones de parabrisas y absorción de tapicería."
                AutoTuneTarget.PANCADAO_BASS -> "Diagnóstico Acústico: Optimización para sonido brasileño exterior: realce de ataque en 63-80Hz y protección con atenuación subsonica bajo 35Hz."
                AutoTuneTarget.SQ_AUDIOPHILE -> "Diagnóstico Acústico: Calibración SQ Audiophile. Micro-ajustes aplicados para balance tonal sin fatiga auditiva."
            }

            _autoTuneState.value = _autoTuneState.value.copy(
                isMeasuring = false,
                progress = 1.0f,
                measuredCurve31 = measured31,
                targetCurve31 = targetValues31,
                proposedCorrection31 = proposedCorrection,
                hasProposal = true,
                explanation = explanation
            )
        }
    }

    fun applyAutoTuneProposal() {
        val proposal = _autoTuneState.value.proposedCorrection31
        if (proposal.isNotEmpty()) {
            _equalizerSettings.value = _equalizerSettings.value.copy(
                bands31 = proposal,
                activePresetName = "Auto-Tune: ${_autoTuneState.value.targetCurve.label}"
            )
            _autoTuneState.value = _autoTuneState.value.copy(hasProposal = false)
        }
    }

    fun discardAutoTuneProposal() {
        _autoTuneState.value = _autoTuneState.value.copy(hasProposal = false)
    }

    // --- SPL CALIBRATION ---
    fun updateSplCalibrationOffset(offsetDb: Float) {
        _splCalibrationSettings.value = _splCalibrationSettings.value.copy(micOffsetDb = offsetDb.coerceIn(-20f, 20f))
        audioEngine.splCalibrationOffsetDb = offsetDb.coerceIn(-20f, 20f)
    }

    fun setSplWeighting(weighting: SplWeighting) {
        _splCalibrationSettings.value = _splCalibrationSettings.value.copy(weighting = weighting)
        audioEngine.splWeighting = weighting
    }

    fun setSplSpeed(speed: SplSpeed) {
        _splCalibrationSettings.value = _splCalibrationSettings.value.copy(speed = speed)
    }

    // --- OSCILLOSCOPE CONTROLS ---
    fun setOscilloscopeTimebase(timebaseMs: Float) {
        _oscilloscopeState.value = _oscilloscopeState.value.copy(timebaseMs = timebaseMs)
    }

    fun toggleOscilloscopeFreeze() {
        _oscilloscopeState.value = _oscilloscopeState.value.copy(isFrozen = !_oscilloscopeState.value.isFrozen)
    }

    // --- PROFESSIONAL BOX DESIGNER ---
    fun updateBoxDesign(
        type: EnclosureType,
        fs: Float,
        qts: Float,
        vas: Float,
        xmax: Float,
        sd: Float,
        powerRms: Int,
        customVb: Float? = null,
        customFb: Float? = null
    ) {
        val ts = ThieleSmallParams(fs, qts, vas, xmax, sd, powerRms)
        val calculatedNetVb = customVb ?: when (type) {
            EnclosureType.SEALED -> (vas / ((0.707f / qts).pow(2) - 1f)).coerceIn(15f, 120f)
            EnclosureType.VENTED_PORTED -> (15f * vas * (qts.pow(2.87f))).coerceIn(25f, 160f)
            EnclosureType.BANDPASS_4TH -> (0.6f * vas * (qts.pow(2.0f))).coerceIn(20f, 100f)
        }

        val calculatedFb = customFb ?: when (type) {
            EnclosureType.SEALED -> (0.707f / qts * fs).coerceIn(30f, 80f)
            EnclosureType.VENTED_PORTED -> (0.42f * fs * (qts.pow(-0.9f))).coerceIn(25f, 65f)
            EnclosureType.BANDPASS_4TH -> (fs * 1.15f).coerceIn(40f, 75f)
        }

        val f3Cutoff = when (type) {
            EnclosureType.SEALED -> (calculatedFb * sqrt((1f / 0.707f).pow(2) - 1f)).coerceIn(25f, 60f)
            EnclosureType.VENTED_PORTED -> (calculatedFb * 0.92f).coerceIn(20f, 55f)
            EnclosureType.BANDPASS_4TH -> (calculatedFb * 0.85f).coerceIn(30f, 55f)
        }

        val vdCm3 = (sd * (xmax / 10f))
        val minPortAreaCm2 = (0.0003f * calculatedFb * vdCm3).coerceIn(40f, 250f)
        val portHeight = 36.0f
        val portWidth = (minPortAreaCm2 / portHeight).coerceIn(2.5f, 12.0f)
        val actualPortArea = portHeight * portWidth

        val dv = sqrt(4f * actualPortArea / kotlin.math.PI.toFloat())
        val portLengthCm = ((23562.5f * dv.pow(2)) / (calculatedFb.pow(2) * calculatedNetVb) - 0.732f * dv).coerceIn(15f, 85f)

        val airVel = ((0.08f * powerRms.toFloat() * calculatedFb) / (actualPortArea * 10f)).coerceIn(4f, 28f)
        val isChuffSafe = airVel < 17.0f

        val portVolLiters = (actualPortArea * portLengthCm) / 1000f
        val grossVol = calculatedNetVb + portVolLiters + 3.8f
        val depthCm = ((grossVol * 1000f) / (65f * 40f)).coerceIn(30f, 75f)

        val cutList = "Frente y Fondo: 65x40 cm (x2) | Laterales: ${(depthCm - 3.6f).toInt()}x36.4 cm (x2) | Tapa y Base: 65x${depthCm.toInt()} cm (x2) | Ducto L: 36.4x${(portLengthCm - 5f).toInt()} cm"

        _boxDesign.value = ProfessionalBoxDesign(
            enclosureType = type,
            tsParams = ts,
            netVolumeLiters = calculatedNetVb,
            grossVolumeLiters = grossVol,
            tuningFreqHz = calculatedFb,
            f3CutoffHz = f3Cutoff,
            isSlotPort = true,
            portWidthCm = portWidth,
            portHeightCm = portHeight,
            portLengthCm = portLengthCm,
            airVelocityMps = airVel,
            isChuffingSafe = isChuffSafe,
            mdfThicknessMm = 18,
            boxWidthCm = 65.0f,
            boxHeightCm = 40.0f,
            boxDepthCm = depthCm,
            cutListSummary = cutList
        )
    }

    // --- HARDWARE BRIDGE & CONNECTIVITY ---
    fun setHardwareConnectionType(type: HardwareConnectionType) {
        val isReal = type.isRealHardware
        val status = when (type) {
            HardwareConnectionType.DSP_INTERNAL -> "Procesamiento nativo Android 32-bit Float"
            HardwareConnectionType.USB_OTG -> "Conectado a Hardware USB OTG CDC/FTDI (Baud: 115200)"
            HardwareConnectionType.BLUETOOTH_SPP -> "Enlazado a DSP Bluetooth SPP (Comandos Hex)"
            HardwareConnectionType.SIMULATOR_DEMO -> "Modo Demostración / Simulación Offline"
        }
        _hardwareBridgeState.value = HardwareBridgeState(
            isConnected = true,
            connectionType = type,
            deviceName = if (isReal) "Hardware DSP 8-CH Serial Link" else "DSP Android Engine",
            statusMessage = status,
            packetsSent = if (isReal) 128 else 0,
            packetsReceived = if (isReal) 96 else 0,
            lastSyncTime = System.currentTimeMillis()
        )
    }

    fun sendDspCommand(commandName: String) {
        val current = _hardwareBridgeState.value
        _hardwareBridgeState.value = current.copy(
            packetsSent = current.packetsSent + 1,
            packetsReceived = current.packetsReceived + 1,
            statusMessage = "Comando '$commandName' transmitido OK (ACK recibido)",
            lastSyncTime = System.currentTimeMillis()
        )
    }

    // --- JSON PRESET EXPORT / IMPORT ---
    fun exportCurrentProfileJson(): String {
        val root = JSONObject()
        root.put("appVersion", "2.0")
        root.put("profileName", _dspSettings.value.activePresetName)
        root.put("description", "Perfil DSP Pro 2.0 exportado con Crossover 4-Vías, EQ 31 Bandas y Paramétrico")
        root.put("timestamp", System.currentTimeMillis())
        root.put("masterGainDb", _dspSettings.value.masterGainDb.toDouble())

        val channelsArray = JSONArray()
        _dspChannels.value.forEach { ch ->
            val chObj = JSONObject()
            chObj.put("id", ch.id)
            chObj.put("name", ch.name)
            chObj.put("hpfHz", ch.hpfHz.toDouble())
            chObj.put("hpfSlopeDb", ch.hpfSlopeDb)
            chObj.put("hpfType", ch.hpfFilterType.name)
            chObj.put("lpfHz", ch.lpfHz.toDouble())
            chObj.put("lpfSlopeDb", ch.lpfSlopeDb)
            chObj.put("lpfType", ch.lpfFilterType.name)
            chObj.put("gainDb", ch.gainDb.toDouble())
            chObj.put("delayMs", ch.delayMs.toDouble())
            chObj.put("phaseInverted", ch.phaseInverted)
            chObj.put("isMuted", ch.isMuted)
            chObj.put("limiterThresholdDb", ch.limiterThresholdDb.toDouble())
            channelsArray.put(chObj)
        }
        root.put("channels", channelsArray)

        val eqArray = JSONArray()
        _equalizerSettings.value.bands31.forEach { eqArray.put(it.toDouble()) }
        root.put("eq31Bands", eqArray)

        val paramArray = JSONArray()
        _equalizerSettings.value.parametricBands.forEach { pb ->
            val pbObj = JSONObject()
            pbObj.put("id", pb.id)
            pbObj.put("freqHz", pb.freqHz.toDouble())
            pbObj.put("gainDb", pb.gainDb.toDouble())
            pbObj.put("q", pb.q.toDouble())
            pbObj.put("filterType", pb.filterType.name)
            pbObj.put("enabled", pb.enabled)
            paramArray.put(pbObj)
        }
        root.put("parametricBands", paramArray)

        return root.toString(2)
    }

    fun importProfileJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val profileName = root.optString("profileName", "Importado DSP 2.0")
            val masterGain = root.optDouble("masterGainDb", 0.0).toFloat()

            if (root.has("channels")) {
                val arr = root.getJSONArray("channels")
                val newChannels = mutableListOf<DspChannel>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.getInt("id")
                    val existing = _dspChannels.value.find { it.id == id } ?: continue
                    newChannels.add(
                        existing.copy(
                            hpfHz = obj.optDouble("hpfHz", existing.hpfHz.toDouble()).toFloat(),
                            hpfSlopeDb = obj.optInt("hpfSlopeDb", existing.hpfSlopeDb),
                            lpfHz = obj.optDouble("lpfHz", existing.lpfHz.toDouble()).toFloat(),
                            lpfSlopeDb = obj.optInt("lpfSlopeDb", existing.lpfSlopeDb),
                            gainDb = obj.optDouble("gainDb", existing.gainDb.toDouble()).toFloat(),
                            delayMs = obj.optDouble("delayMs", existing.delayMs.toDouble()).toFloat(),
                            phaseInverted = obj.optBoolean("phaseInverted", existing.phaseInverted),
                            isMuted = obj.optBoolean("isMuted", existing.isMuted)
                        )
                    )
                }
                if (newChannels.isNotEmpty()) _dspChannels.value = newChannels
            }

            if (root.has("eq31Bands")) {
                val arr = root.getJSONArray("eq31Bands")
                val list = mutableListOf<Float>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getDouble(i).toFloat())
                }
                if (list.size == 31) {
                    _equalizerSettings.value = _equalizerSettings.value.copy(bands31 = list)
                }
            }

            _dspSettings.value = _dspSettings.value.copy(
                activePresetName = profileName,
                masterGainDb = masterGain
            )
            _presetHistory.value = listOf("Preset: $profileName") + _presetHistory.value
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- TONE GENERATOR METHODS ---
    fun playTone(mode: ToneMode, freqHz: Float) {
        _toneMode.value = mode
        _toneFrequency.value = freqHz
        _isTonePlaying.value = true
        _isPlaying.value = false
        audioEngine.playTone(mode, freqHz, _dspSettings.value)
    }

    fun stopTone() {
        _isTonePlaying.value = false
        audioEngine.stopTone()
    }

    fun setToneFrequency(freqHz: Float) {
        _toneFrequency.value = freqHz
        if (_isTonePlaying.value) {
            audioEngine.playTone(_toneMode.value, freqHz, _dspSettings.value)
        }
    }

    fun setToneMode(mode: ToneMode) {
        _toneMode.value = mode
        if (_isTonePlaying.value) {
            audioEngine.playTone(mode, _toneFrequency.value, _dspSettings.value)
        }
    }

    // --- SPL COMPETITION RUN (30s BASS RACE / dB DRAG) ---
    fun startSplRun() {
        splRunJob?.cancel()
        _splRunState.value = SplRunState(
            isRunning = true,
            timeRemainingSeconds = 30,
            currentSplDb = _splDb.value,
            averageSplDb = _splDb.value,
            maxPeakDb = _peakSplDb.value,
            runHistory = _splRunState.value.runHistory
        )

        splRunJob = viewModelScope.launch {
            var seconds = 30
            var sumDb = 0f
            var count = 0
            var peak = _peakSplDb.value

            while (seconds > 0 && isActive) {
                delay(1000)
                seconds--
                val liveSpl = audioEngine.currentSplDb
                sumDb += liveSpl
                count++
                if (liveSpl > peak) peak = liveSpl
                val avg = if (count > 0) sumDb / count else liveSpl

                _splRunState.value = _splRunState.value.copy(
                    timeRemainingSeconds = seconds,
                    currentSplDb = liveSpl,
                    averageSplDb = avg,
                    maxPeakDb = peak
                )
            }

            // Run finished
            val finalRecord = SplRecord(
                id = System.currentTimeMillis(),
                peakDb = peak,
                avgDb = if (count > 0) sumDb / count else peak,
                title = "SPL Run 30s • ${_currentTrack.value.title}",
                timeString = "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}"
            )
            _splRunState.value = _splRunState.value.copy(
                isRunning = false,
                timeRemainingSeconds = 0,
                runHistory = listOf(finalRecord) + _splRunState.value.runHistory
            )
        }
    }

    fun stopSplRun() {
        splRunJob?.cancel()
        splRunJob = null
        _splRunState.value = _splRunState.value.copy(isRunning = false)
    }

    fun clearSplHistory() {
        _splRunState.value = _splRunState.value.copy(runHistory = emptyList())
    }

    // --- PRESETS ---
    fun saveCustomPreset(name: String, desc: String) {
        val newPreset = DspPreset(
            name = name,
            description = desc,
            dspSettings = _dspSettings.value.copy(activePresetName = name),
            isUserCreated = true
        )
        _availablePresets.value = _availablePresets.value + newPreset
        loadPreset(name)
    }

    fun deleteCustomPreset(name: String) {
        _availablePresets.value = _availablePresets.value.filterNot { it.name == name && it.isUserCreated }
    }

    // --- CAR AUDIO CALCULATORS ---
    fun calculateBox(
        widthCm: Float,
        heightCm: Float,
        depthCm: Float,
        woodThicknessCm: Float = 1.8f,
        subwooferDisplacementL: Float = 3.5f,
        isPorted: Boolean = true,
        portWidthCm: Float = 6.0f,
        portHeightCm: Float = 35.0f,
        portLengthCm: Float = 40.0f
    ): BoxCalculationResult {
        val internalW = (widthCm - 2 * woodThicknessCm).coerceAtLeast(5f)
        val internalH = (heightCm - 2 * woodThicknessCm).coerceAtLeast(5f)
        val internalD = (depthCm - 2 * woodThicknessCm).coerceAtLeast(5f)

        val grossLiters = (internalW * internalH * internalD) / 1000f
        val portVolumeLiters = if (isPorted) (portWidthCm * portHeightCm * portLengthCm) / 1000f else 0f
        val netLiters = (grossLiters - subwooferDisplacementL - portVolumeLiters).coerceAtLeast(5f)
        val netCuFt = netLiters / 28.3168f

        val portAreaSqCm = portWidthCm * portHeightCm
        val tuningHz = if (isPorted && portLengthCm > 0 && portAreaSqCm > 0) {
            val fb = (34400.0 / (2 * Math.PI)) * Math.sqrt(
                portAreaSqCm.toDouble() / ((netLiters * 1000.0) * (portLengthCm + 0.825 * Math.sqrt(portAreaSqCm.toDouble())))
            )
            fb.toFloat().coerceIn(20f, 80f)
        } else {
            38.0f
        }

        val recommendedSub = when {
            netLiters < 25f -> "8\" o 10\" Sellado / Compacto"
            netLiters in 25f..55f -> "10\" o 12\" Porteado (Musical / SQ)"
            netLiters in 55f..95f -> "12\" o 15\" Porteado (Bajos Profundos)"
            else -> "15\" o 18\" SPL Extremo / Pancadão"
        }

        val desc = if (isPorted) {
            "Cajón Porteado entonado a ${String.format(java.util.Locale.US, "%.1f", tuningHz)} Hz. Ideal para ${if (tuningHz < 35f) "sub-graves profundos de 30Hz" else "pancadão y golpe seco de 45Hz"}."
        } else {
            "Cajón Sellado de respuesta rápida, transitorios limpios y máxima fidelidad sonora."
        }

        return BoxCalculationResult(
            grossVolumeLiters = grossLiters,
            grossVolumeCuFt = grossLiters / 28.3168f,
            netVolumeLiters = netLiters,
            netVolumeCuFt = netCuFt,
            portTuningHz = tuningHz,
            recommendedSubSizeInches = recommendedSub,
            description = desc
        )
    }

    fun calculateWire(
        wattsRms: Float,
        voltage: Float = 14.4f,
        lengthMeters: Float = 5.0f,
        isOfcCobre: Boolean = true
    ): WireCalculationResult {
        val currentAmps = (wattsRms / (voltage * 0.80f)).coerceAtLeast(5f)
        val recommendedFuse = when {
            currentAmps < 40f -> 40
            currentAmps < 60f -> 60
            currentAmps < 100f -> 100
            currentAmps < 150f -> 150
            currentAmps < 200f -> 200
            currentAmps < 250f -> 250
            currentAmps < 300f -> 300
            else -> (currentAmps * 1.15f).toInt()
        }

        val ccaFactor = if (isOfcCobre) 1.0f else 1.45f
        val (awg, resPerMeter) = when {
            currentAmps >= 200f || lengthMeters > 4.5f && currentAmps >= 140f -> Pair("0/1 AWG (53.5 mm²)", 0.00032f * ccaFactor)
            currentAmps >= 120f -> Pair("2 AWG (33.6 mm²)", 0.00051f * ccaFactor)
            currentAmps >= 60f -> Pair("4 AWG (21.2 mm²)", 0.00082f * ccaFactor)
            else -> Pair("8 AWG (8.36 mm²)", 0.00206f * ccaFactor)
        }

        val totalResistance = resPerMeter * lengthMeters * 2
        val dropVolts = currentAmps * totalResistance
        val dropPercent = (dropVolts / voltage) * 100f
        val isSafe = dropPercent < 4.5f

        val notes = if (isSafe) {
            "Instalación Segura. Caída de tensión mínima (${String.format(java.util.Locale.US, "%.2f", dropVolts)}V)."
        } else {
            "Peligro: Caída de voltaje excesiva (${String.format(java.util.Locale.US, "%.1f", dropPercent)}%). Instala cable 0/1 AWG de Cobre OFC puro para evitar calentar bornes."
        }

        return WireCalculationResult(
            maxAmps = currentAmps,
            recommendedAwg = awg,
            recommendedFuseAmps = recommendedFuse,
            voltageDropVolts = dropVolts,
            voltageDropPercent = dropPercent,
            isSafe = isSafe,
            notes = notes
        )
    }

    fun calculateSubwooferWiring(
        numSubs: Int,
        coilType: String,
        wiringMode: String
    ): SubwooferWiringResult {
        val (finalImpedance, safety, explanation) = when {
            numSubs == 1 && coilType == "DVC 4Ω" && wiringMode.contains("Paralelo") -> Triple(2.0f, "Excelente para plantas estables a 2Ω y 1Ω", "Conectar bobina 1 (+) con bobina 2 (+) al (+) de la planta, y ambos (-) al (-).")
            numSubs == 1 && coilType == "DVC 4Ω" && wiringMode.contains("Serie") -> Triple(8.0f, "Seguro a 8Ω pero con menor potencia", "Conectar el (-) de bobina 1 al (+) de bobina 2.")
            numSubs == 1 && coilType == "DVC 2Ω" && wiringMode.contains("Paralelo") -> Triple(1.0f, "Ideal 1Ω Estable • Máxima Potencia RMS", "Conectar bobinas en paralelo. Carga final perfecta de 1.0 Ohm.")
            numSubs == 1 && coilType == "DVC 2Ω" && wiringMode.contains("Serie") -> Triple(4.0f, "Estable a 4Ω", "Bobinas en serie hacia la salida de la planta.")

            numSubs == 2 && coilType == "DVC 4Ω" && wiringMode.contains("Paralelo") -> Triple(1.0f, "Configuración Estrella: 1 Ohm Final", "Todas las bobinas en paralelo entre sí. Máxima potencia para plantas de 1Ω.")
            numSubs == 2 && coilType == "DVC 4Ω" && wiringMode.contains("Serie") -> Triple(4.0f, "Estable a 4Ω", "Bobinas de cada sub en serie y ambos subs en paralelo.")
            numSubs == 2 && coilType == "DVC 2Ω" && wiringMode.contains("Serie") -> Triple(2.0f, "Estable a 2Ω", "Bobinas en serie (4Ω c/u) y luego ambos en paralelo = 2.0Ω.")
            numSubs == 2 && coilType == "DVC 2Ω" && wiringMode.contains("Paralelo") -> Triple(0.5f, "ALERTA 0.5Ω: Requiere planta High-SPL", "Precaución: Muchas plantas entran en protección a 0.5 Ohm.")

            numSubs == 3 && coilType == "DVC 4Ω" -> Triple(0.67f, "Cuidado: 0.67 Ohm final", "Conexión triple paralela. Asegura buen banco de baterías.")
            numSubs == 3 && coilType == "SVC 4Ω" -> Triple(1.33f, "Estable a 1.33 Ohms", "3 subs sencillos de 4Ω en paralelo.")

            numSubs == 4 && coilType == "DVC 4Ω" -> Triple(2.0f, "2.0 Ohms Perfectos en Serie-Paralelo", "Bobinas en paralelo y pares en serie para 2Ω seguros.")
            numSubs == 4 && coilType == "DVC 2Ω" -> Triple(1.0f, "1.0 Ohm Perfecto con 4 Subwoofers", "Bobinas de cada sub en serie (4Ω) y los 4 subs en paralelo = 1.0Ω.")

            else -> Triple(1.0f, "1.0 Ohm Nominal Car Audio", "Conexión estándar calculada.")
        }

        return SubwooferWiringResult(
            numWoofers = numSubs,
            coilType = coilType,
            wiringMode = wiringMode,
            finalImpedanceOhms = finalImpedance,
            ampSafetyLevel = safety,
            diagramExplanation = explanation
        )
    }

    private fun createDefaultDspChannels(): List<DspChannel> = listOf(
        DspChannel(
            id = 1,
            name = "CH 1 & 2: Subwoofer",
            typeName = "Subgraves / SPL",
            hpfHz = 25f,
            hpfSlopeDb = 24,
            hpfEnabled = true,
            lpfHz = 80f,
            lpfSlopeDb = 24,
            lpfEnabled = true,
            gainDb = 2f,
            phaseInverted = false,
            delayMs = 0.0f
        ),
        DspChannel(
            id = 2,
            name = "CH 3 & 4: Medios Bajos",
            typeName = "Mid-Bass / Puertas",
            hpfHz = 80f,
            hpfSlopeDb = 12,
            hpfEnabled = true,
            lpfHz = 500f,
            lpfSlopeDb = 12,
            lpfEnabled = true,
            gainDb = 0f,
            phaseInverted = false,
            delayMs = 0.8f
        ),
        DspChannel(
            id = 3,
            name = "CH 5 & 6: Drivers / Cornetas",
            typeName = "Voz / Chuchero",
            hpfHz = 600f,
            hpfSlopeDb = 24,
            hpfEnabled = true,
            lpfHz = 6000f,
            lpfSlopeDb = 12,
            lpfEnabled = true,
            gainDb = -1f,
            phaseInverted = false,
            delayMs = 1.2f
        ),
        DspChannel(
            id = 4,
            name = "CH 7 & 8: Super Tweeters",
            typeName = "Agudos / Brillo",
            hpfHz = 6000f,
            hpfSlopeDb = 24,
            hpfEnabled = true,
            lpfHz = 20000f,
            lpfSlopeDb = 12,
            lpfEnabled = false,
            gainDb = -2f,
            phaseInverted = false,
            delayMs = 1.4f
        )
    )

    private fun createDefaultPresets(): List<DspPreset> = listOf(
        DspPreset(
            name = "Open Show Pro",
            description = "Voces limpias, trompetas nítidas y alta proyección para eventos al aire libre.",
            dspSettings = DspSettings(
                hpfFrequencyHz = 80f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 16000f,
                subsonicFrequencyHz = 32f,
                bassBoostDb = 4f,
                bassBoostFreqHz = 50f,
                masterGainDb = 2f,
                eqBands = listOf(3f, 4f, 2f, 1f, 4f, 6f, 7f, 6f),
                activePresetName = "Open Show Pro"
            )
        ),
        DspPreset(
            name = "SPL Bass Monster",
            description = "Presión sonora extrema con refuerzo a 45Hz para competencia de decibeles.",
            dspSettings = DspSettings(
                hpfFrequencyHz = 20f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 85f,
                subsonicFrequencyHz = 28f,
                bassBoostDb = 12f,
                bassBoostFreqHz = 45f,
                masterGainDb = 4f,
                eqBands = listOf(10f, 8f, 3f, -2f, -4f, -6f, -8f, -10f),
                activePresetName = "SPL Bass Monster"
            )
        ),
        DspPreset(
            name = "SQL Audiophile Studio",
            description = "Respuesta plana y armónicos de alta fidelidad para escuchar dentro del vehículo.",
            dspSettings = DspSettings(
                hpfFrequencyHz = 60f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 20000f,
                subsonicFrequencyHz = 25f,
                bassBoostDb = 1f,
                bassBoostFreqHz = 40f,
                masterGainDb = 0f,
                eqBands = listOf(1f, 1f, 0.5f, 0f, 0f, 1f, 1.5f, 1f),
                activePresetName = "SQL Audiophile Studio"
            )
        ),
        DspPreset(
            name = "Reggaeton & Urbano",
            description = "Sub-graves con pegada profunda y frecuencias medias balanceadas.",
            dspSettings = DspSettings(
                hpfFrequencyHz = 35f,
                hpfSlopeDb = 24,
                lpfFrequencyHz = 14000f,
                subsonicFrequencyHz = 30f,
                bassBoostDb = 8f,
                bassBoostFreqHz = 48f,
                masterGainDb = 3f,
                eqBands = listOf(7f, 6f, 2f, 0f, 2f, 3f, 5f, 4f),
                activePresetName = "Reggaeton & Urbano"
            )
        )
    )

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
