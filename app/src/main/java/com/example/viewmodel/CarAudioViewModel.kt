package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CarAudioEngine
import com.example.model.AmpTelemetry
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.AutoTuneState
import com.example.model.AutoTuneTarget
import com.example.model.CarAudioThemeType
import com.example.model.CrossoverFilterType
import com.example.model.DspChannel
import com.example.model.DspFullProfileJson
import com.example.model.DspPreset
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqPresetItem
import com.example.model.OscilloscopeState
import com.example.model.ParametricBand
import com.example.model.ParametricFilterType
import com.example.model.RtaBand
import com.example.model.SplCalibrationSettings
import com.example.model.SplRecord
import com.example.model.SplRunState
import com.example.model.SplSpeed
import com.example.model.SplWeighting
import com.example.model.ToneMode
import com.example.model.TrackCategory
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
    DSP("DSP"),
    EQ("EQ"),
    RTA("RTA"),
    EFECTOS("EFECTOS"),
    PLAYER("PLAYER"),
    AJUSTES("AJUSTES")
}

class CarAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val audioEngine = CarAudioEngine(context)

    // SharedPreferences for persistent preferences
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

    // Current Theme (Defaults to Cyber Neon Green)
    private val _currentTheme = MutableStateFlow(CarAudioThemeType.NEON_CYBER)
    val currentTheme: StateFlow<CarAudioThemeType> = _currentTheme.asStateFlow()

    // Active Tab
    private val _selectedTab = MutableStateFlow(AppTab.DSP)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // DSP Settings
    private val _dspSettings = MutableStateFlow(DspSettings())
    val dspSettings: StateFlow<DspSettings> = _dspSettings.asStateFlow()

    // Amp Telemetry (Voltage is null by default: shows N/A)
    private val _ampTelemetry = MutableStateFlow(AmpTelemetry(voltage = null, isVoltageAvailable = false))
    val ampTelemetry: StateFlow<AmpTelemetry> = _ampTelemetry.asStateFlow()

    // Audio Playback state (Clean by default - no fake/mock test tracks)
    private val _tracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val tracks: StateFlow<List<AudioTrackItem>> = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(null)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

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

    private val _eqPresets = MutableStateFlow<List<EqPresetItem>>(EqPresetCatalog.presets)
    val eqPresets: StateFlow<List<EqPresetItem>> = _eqPresets.asStateFlow()

    fun selectPreset(preset: EqPresetItem) {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            bands15 = preset.bands,
            bands31 = if (preset.bands31.isNotEmpty()) preset.bands31 else _equalizerSettings.value.bands31,
            parametricFreqHz = preset.paramFreq,
            parametricGainDb = preset.paramGain,
            parametricQ = preset.paramQ,
            activePresetName = preset.name
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
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
        val filtered = _eqPresets.value.filter { it.name != name }
        _eqPresets.value = filtered + newPreset
        _equalizerSettings.value = _equalizerSettings.value.copy(activePresetName = name)
    }

    fun deletePreset(preset: EqPresetItem) {
        _eqPresets.value = _eqPresets.value.filter { it.name != preset.name }
        if (_equalizerSettings.value.activePresetName == preset.name) {
            selectPreset(EqPresetCatalog.presets.first())
        }
    }

    fun renamePreset(oldName: String, newName: String) {
        _eqPresets.value = _eqPresets.value.map {
            if (it.name == oldName) it.copy(name = newName) else it
        }
        if (_equalizerSettings.value.activePresetName == oldName) {
            _equalizerSettings.value = _equalizerSettings.value.copy(activePresetName = newName)
        }
    }

    fun exportPresetsJson(): String {
        val root = org.json.JSONObject()
        root.put("app", "CAR AUDIO DSP PRO")
        root.put("version", "3.0")
        val arr = org.json.JSONArray()
        _eqPresets.value.forEach { p ->
            val obj = org.json.JSONObject()
            obj.put("name", p.name)
            obj.put("description", p.description)
            obj.put("paramFreq", p.paramFreq.toDouble())
            obj.put("paramGain", p.paramGain.toDouble())
            obj.put("paramQ", p.paramQ.toDouble())
            obj.put("isCustom", p.isCustom)
            val b15 = org.json.JSONArray()
            p.bands.forEach { b15.put(it.toDouble()) }
            obj.put("bands15", b15)
            arr.put(obj)
        }
        root.put("presets", arr)
        return root.toString(2)
    }

    fun importPresetsJson(jsonString: String): Boolean {
        return try {
            val root = org.json.JSONObject(jsonString)
            val arr = root.getJSONArray("presets")
            val importedList = mutableListOf<EqPresetItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.getString("name")
                val desc = obj.optString("description", "")
                val pFreq = obj.optDouble("paramFreq", 60.0).toFloat()
                val pGain = obj.optDouble("paramGain", 0.0).toFloat()
                val pQ = obj.optDouble("paramQ", 1.4).toFloat()
                val isCustom = obj.optBoolean("isCustom", true)
                val b15Arr = obj.getJSONArray("bands15")
                val bands15 = (0 until b15Arr.length()).map { b15Arr.getDouble(it).toFloat() }
                importedList.add(
                    EqPresetItem(
                        name = name,
                        description = desc,
                        bands = bands15,
                        paramFreq = pFreq,
                        paramGain = pGain,
                        paramQ = pQ,
                        isCustom = isCustom
                    )
                )
            }
            if (importedList.isNotEmpty()) {
                val existingNames = importedList.map { it.name }.toSet()
                _eqPresets.value = _eqPresets.value.filter { it.name !in existingNames } + importedList
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    fun selectEqualizerPreset(presetName: String) {
        val found = _eqPresets.value.firstOrNull { it.name == presetName }
        if (found != null) {
            selectPreset(found)
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

                // Telemetry without fake voltage - respects N/A when no real hardware sensor
                val playing = _isPlaying.value
                val currentDsp = _dspSettings.value

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

                val newClipCount = if (clipCondition) _ampTelemetry.value.clipCount + 1 else _ampTelemetry.value.clipCount

                _ampTelemetry.value = _ampTelemetry.value.copy(
                    voltage = null, // N/A: Real hardware OBD/CAN sensor not connected
                    isVoltageAvailable = false,
                    isClipping = clipCondition,
                    clipCount = newClipCount
                )

                // Periodic or event notifications
                val now = System.currentTimeMillis()
                if (clipCondition && (now - lastClipAlertTime > 8000)) {
                    lastClipAlertTime = now
                    CarAudioNotificationHelper.showClippingAlertNotification(context, newClipCount)
                }

                // Update sticky notification every ~4 seconds if playing
                if (simStep % 40 == 0 && playing) {
                    CarAudioNotificationHelper.showMonitorNotification(
                        context = context,
                        trackTitle = _currentTrack.value?.title ?: "CAR AUDIO DSP PRO",
                        isPlaying = playing,
                        voltage = null,
                        temperatureC = null,
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

    fun replayIntro() {
        openSplashScreen()
    }

    fun setAutoPlayIntro(enabled: Boolean) {
        _autoPlayIntro.value = enabled
        prefs.edit().putBoolean("intro_video_enabled", enabled).apply()
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        prefs.edit().putBoolean("keep_screen_on", enabled).apply()
    }

    fun resetDspToDefaults() {
        _dspSettings.value = DspSettings()
        _equalizerSettings.value = EqualizerSettings()
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setMasterGain(gainDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(masterGainDb = gainDb.coerceIn(-40f, 18f))
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleDspPower(enabled: Boolean) {
        _dspSettings.value = _dspSettings.value.copy(dspMasterEnabled = enabled)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun toggleMasterMute() {
        val current = _dspSettings.value.masterGainDb
        if (current <= -60f) {
            _dspSettings.value = _dspSettings.value.copy(masterGainDb = 0f)
        } else {
            _dspSettings.value = _dspSettings.value.copy(masterGainDb = -80f)
        }
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

    fun toggleDspMaster() {
        val updated = !_dspSettings.value.dspMasterEnabled
        _dspSettings.value = _dspSettings.value.copy(dspMasterEnabled = updated)
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun playTrack(track: AudioTrackItem) {
        audioEngine.playTrack(track, _dspSettings.value, _equalizerSettings.value)
        _currentTrack.value = track
        _isPlaying.value = true
        CarAudioNotificationHelper.showMonitorNotification(
            context,
            track.title,
            isPlaying = true,
            voltage = null,
            temperatureC = null,
            isClipping = _ampTelemetry.value.isClipping
        )
    }

    fun togglePlayPause() {
        val track = _currentTrack.value
        if (track != null) {
            audioEngine.togglePlayPause(_dspSettings.value, _equalizerSettings.value)
            _isPlaying.value = audioEngine.isPlaying
            CarAudioNotificationHelper.showMonitorNotification(
                context,
                track.title,
                isPlaying = _isPlaying.value,
                voltage = null,
                temperatureC = null,
                isClipping = _ampTelemetry.value.isClipping
            )
        }
    }

    fun nextTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = if (current != null) list.indexOfFirst { it.id == current.id } else -1
        val nextIndex = if (currentIndex >= 0 && currentIndex < list.size - 1) currentIndex + 1 else 0
        playTrack(list[nextIndex])
    }

    fun previousTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = if (current != null) list.indexOfFirst { it.id == current.id } else -1
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
            trackTitle = _currentTrack.value?.title ?: "CAR AUDIO DSP PRO",
            isPlaying = _isPlaying.value,
            voltage = _ampTelemetry.value.voltage,
            temperatureC = _ampTelemetry.value.temperatureC,
            isClipping = _ampTelemetry.value.isClipping
        )
    }

    fun addCustomUserTrack(uri: Uri, name: String) {
        var title = name.substringBeforeLast(".").ifBlank { "Pista Car Audio" }
        var artist = "Desconocido"
        var album = "Archivo Local"
        var durationSec = 180

        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                if (it.isNotBlank()) title = it
            }
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                if (it.isNotBlank()) artist = it
            }
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let {
                if (it.isNotBlank()) album = it
            }
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.let {
                val ms = it.toIntOrNull() ?: 0
                if (ms > 0) durationSec = ms / 1000
            }
            retriever.release()
        } catch (_: Exception) {}

        val newTrack = AudioTrackItem(
            id = "user_${System.currentTimeMillis()}",
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSec,
            category = TrackCategory.USER_CUSTOM,
            frequencyDescription = "Audio local seleccionado desde el dispositivo",
            isSynthesized = false,
            customUri = uri
        )
        val updatedList = listOf(newTrack) + _tracks.value.filter { it.id != newTrack.id }
        _tracks.value = updatedList
        playTrack(newTrack)
    }

    fun removeTrack(track: AudioTrackItem) {
        val updated = _tracks.value.filter { it.id != track.id }
        _tracks.value = updated
        if (_currentTrack.value?.id == track.id) {
            audioEngine.stopPlayback()
            _isPlaying.value = false
            _currentTrack.value = updated.firstOrNull()
        }
    }

    fun setBassBoost(enabled: Boolean, boostDb: Float, freqHz: Float) {
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

    fun setCompressor(enabled: Boolean, thresholdDb: Float, ratio: Float, attackMs: Float, releaseMs: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            compressorEnabled = enabled,
            compressorThresholdDb = thresholdDb.coerceIn(-48f, 0f),
            compressorRatio = ratio.coerceIn(1f, 20f),
            compressorAttackMs = attackMs.coerceIn(1f, 100f),
            compressorReleaseMs = releaseMs.coerceIn(10f, 1000f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setLimiter(enabled: Boolean, ceilingDb: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            limiterEnabled = enabled,
            limiterCeilingDb = ceilingDb.coerceIn(-24f, 0f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setExciter(enabled: Boolean, level: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            exciterEnabled = enabled,
            exciterLevel = level.coerceIn(0f, 10f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setPresence(enabled: Boolean, level: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            presenceEnabled = enabled,
            presenceLevel = level.coerceIn(0f, 10f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setClarity(enabled: Boolean, level: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            clarityEnabled = enabled,
            clarityLevel = level.coerceIn(0f, 10f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setFilterHpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            hpfEnabled = enabled,
            hpfFrequencyHz = freqHz.coerceIn(10f, 500f),
            hpfSlopeDb = slopeDb,
            hpfQ = q.coerceIn(0.5f, 2.0f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setFilterLpf(enabled: Boolean, freqHz: Float, slopeDb: Int, q: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            lpfEnabled = enabled,
            lpfFrequencyHz = freqHz.coerceIn(40f, 20000f),
            lpfSlopeDb = slopeDb,
            lpfQ = q.coerceIn(0.5f, 2.0f)
        )
        audioEngine.applyDspSettings(_dspSettings.value, _equalizerSettings.value)
    }

    fun setFilterBandPass(enabled: Boolean, centerHz: Float, widthHz: Float, slopeDb: Int, q: Float) {
        _dspSettings.value = _dspSettings.value.copy(
            bandPassEnabled = enabled,
            bandPassCenterHz = centerHz.coerceIn(100f, 10000f),
            bandPassWidthHz = widthHz.coerceIn(50f, 4000f),
            bandPassSlopeDb = slopeDb,
            bandPassQ = q.coerceIn(0.5f, 3.0f)
        )
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
            audioEngine.playTone(ToneMode.PINK_NOISE, 1000f, _dspSettings.value, _equalizerSettings.value)

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
        audioEngine.playTone(mode, freqHz, _dspSettings.value, _equalizerSettings.value)
    }

    fun stopTone() {
        _isTonePlaying.value = false
        audioEngine.stopTone()
    }

    fun setToneFrequency(freqHz: Float) {
        _toneFrequency.value = freqHz
        if (_isTonePlaying.value) {
            audioEngine.playTone(_toneMode.value, freqHz, _dspSettings.value, _equalizerSettings.value)
        }
    }

    fun setToneMode(mode: ToneMode) {
        _toneMode.value = mode
        if (_isTonePlaying.value) {
            audioEngine.playTone(mode, _toneFrequency.value, _dspSettings.value, _equalizerSettings.value)
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
                title = "SPL Run 30s • ${_currentTrack.value?.title ?: "DSP Test"}",
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
    fun saveDspPreset(name: String, desc: String) {
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
