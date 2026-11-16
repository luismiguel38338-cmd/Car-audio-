package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.FaultSeverity
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.ParametricFilterType
import com.example.model.RtaBand
import com.example.model.SplWeighting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CarAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val fxLock = Any()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    // Real Android AudioFx pipeline
    private var equalizerFx: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var virtualizerFx: Virtualizer? = null
    private var loudnessEnhancerFx: LoudnessEnhancer? = null
    private var dynamicsProcessingFx: DynamicsProcessing? = null
    @Volatile private var useDynamicsProcessingPostEq: Boolean = false
    private var visualizerFx: Visualizer? = null

    @Volatile var activeAudioSessionId: Int = -1
        private set

    // Reusable DynamicsProcessing stages (avoids JNI allocations on slider sweeps)
    private var cachedPostEq: DynamicsProcessing.Eq? = null
    private var cachedMbc: DynamicsProcessing.Mbc? = null
    private var cachedLimiter: DynamicsProcessing.Limiter? = null

    // Hardware commit coalescing to prevent audio glitches during rapid slider drags
    private var pendingCommitJob: Job? = null
    @Volatile private var lastHardwareCommitMs: Long = 0L

    // Microphone RTA
    private var audioRecord: AudioRecord? = null
    private var micRecordJob: Job? = null
    @Volatile var isMicRtaActive = false
        private set

    // Pre-allocated zero-GC buffers for FFT, Waveform & RTA
    private val oscBuffer = FloatArray(128)
    private val waveformBuffer = FloatArray(64)
    private var fftMagnitudesBuffer = FloatArray(512)
    private val rtaFrequencies = IntArray(31) { EqualizerSettings.FREQUENCIES_HZ_31[it] }
    private val rtaLevelsDb = FloatArray(31) { -60f }
    private val rtaPeaksDb = FloatArray(31) { -60f }
    private val rtaTemplate = createInitialRtaBands()

    // Real-time audio levels
    @Volatile var currentSplDb: Float = 0.0f
        private set
    @Volatile var peakSplDb: Float = 0.0f
        private set
    @Volatile var leftVuLevel: Float = 0.0f
        private set
    @Volatile var rightVuLevel: Float = 0.0f
        private set
    var currentWaveform: FloatArray = waveformBuffer
        private set
    @Volatile var currentRtaBands: List<RtaBand> = rtaTemplate
        private set
    @Volatile private var rtaDirty = false

    // Real DSP Telemetry & Oscilloscope
    @Volatile var rmsLevelDb: Float = -60.0f
        private set
    @Volatile var isClippingDetected: Boolean = false
        private set
    @Volatile var clipEventsCount: Int = 0
        private set
    @Volatile var bassEnergy: Float = 0.0f
        private set
    @Volatile var midEnergy: Float = 0.0f
        private set
    @Volatile var trebleEnergy: Float = 0.0f
        private set
    var rawOscilloscopePcm: FloatArray = oscBuffer
        private set
    @Volatile var splCalibrationOffsetDb: Float = 0.0f
    @Volatile var splWeighting: SplWeighting = SplWeighting.C_WEIGHTING

    // Playback state mirrored from Media3 ExoPlayer service
    @Volatile var isPlaying = false
        private set
    @Volatile var playbackPositionSeconds = 0
        private set
    @Volatile var currentTrack: AudioTrackItem? = null
        private set
    var onPlaybackStateChangedListener: (() -> Unit)? = null

    @Volatile var volumeFactor: Float = 0.85f
        private set
    var onVolumeChangedListener: ((Float) -> Unit)? = null
    @Volatile private var isVisualizerUiActive = true

    private val sampleRate = 44100

    // AudioFX Routing & Device Compatibility
    @Volatile var externalAudioSessionId: Int = 0
        private set
    @Volatile var lastActiveSessionPackage: String = "Sistema / Apps"
        private set
    @Volatile var sessionTarget: AudioSessionTarget = AudioSessionTarget.GLOBAL_MIX_BEST_EFFORT
        private set
    @Volatile var outputDeviceStatus: OutputDeviceStatus = OutputDeviceStatus()
        private set
    var onOutputDeviceChangedListener: ((OutputDeviceStatus) -> Unit)? = null

    @Volatile var currentDspSettings: DspSettings = DspSettings()
        private set
    @Volatile var currentEqualizerSettings: EqualizerSettings = EqualizerSettings()
        private set
    var onSettingsChangedListener: ((DspSettings, EqualizerSettings) -> Unit)? = null
    var onAudioFaultListener: ((title: String, detail: String, severity: FaultSeverity) -> Unit)? = null
    @Volatile var fxEngineStatus: String = "Esperando sesión de audio Media3..."
        private set
    @Volatile var capabilityReport: DspCapabilityReport = DspCapabilityReport()
        private set

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            refreshOutputDeviceStatus()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            refreshOutputDeviceStatus()
        }
    }

    init {
        refreshOutputDeviceStatus()
        try {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, mainHandler)
        } catch (_: Throwable) {}
    }

    companion object {
        @Volatile
        private var instance: CarAudioEngine? = null

        private const val MIN_HARDWARE_COMMIT_INTERVAL_MS = 16L

        fun getInstance(context: Context): CarAudioEngine {
            return instance ?: synchronized(this) {
                instance ?: CarAudioEngine(context.applicationContext).also { instance = it }
            }
        }

        fun createInitialRtaBands(): List<RtaBand> {
            val freqs = listOf(
                Pair("20", 20), Pair("25", 25), Pair("31.5", 31), Pair("40", 40), Pair("50", 50),
                Pair("63", 63), Pair("80", 80), Pair("100", 100), Pair("125", 125),
                Pair("160", 160), Pair("200", 200), Pair("250", 250), Pair("315", 315),
                Pair("400", 400), Pair("500", 500), Pair("630", 630), Pair("800", 800),
                Pair("1k", 1000), Pair("1.2k", 1250), Pair("1.6k", 1600), Pair("2k", 2000),
                Pair("2.5k", 2500), Pair("3.1k", 3150), Pair("4k", 4000), Pair("5k", 5000),
                Pair("6.3k", 6300), Pair("8k", 8000), Pair("10k", 10000), Pair("12.5k", 12500),
                Pair("16k", 16000), Pair("20k", 20000)
            )
            return freqs.map { RtaBand(label = it.first, freqHz = it.second, levelDb = -60f, peakDb = -60f) }
        }

        fun computeIsoUpperCutoffHz(index: Int, freqs: List<Int> = EqualizerSettings.FREQUENCIES_HZ_31): Float {
            if (index >= freqs.lastIndex) return 22000f
            val f1 = freqs[index].toFloat().coerceAtLeast(15f)
            val f2 = freqs[index + 1].toFloat().coerceAtLeast(f1)
            return sqrt(f1 * f2)
        }

        /**
         * Pure acoustic transfer curve per frequency with anti-double-processing awareness:
         * - When [includeBassBoostInCurve] is true (DynamicsProcessing 31-band active), BassBoost is integrated
         *   here while hardware BassBoost / Equalizer / LoudnessEnhancer stay disabled.
         * - When [includeBassBoostInCurve] is false (legacy Equalizer + BassBoost fallback), BassBoost is delegated
         *   solely to hardware BassBoost so low frequencies are never boosted twice.
         */
        fun computeEffectiveBandGainDb(
            freqHz: Int,
            dsp: DspSettings,
            eq: EqualizerSettings,
            includeBassBoostInCurve: Boolean = true
        ): Float {
            if (!dsp.dspMasterEnabled) return 0.0f

            var gainDb = 0.0f
            val fDouble = freqHz.toDouble().coerceAtLeast(15.0)

            if (eq.isEnabled) {
                val freqs31 = EqualizerSettings.FREQUENCIES_HZ_31
                var closestIdx = 0
                var minLogDist = Double.MAX_VALUE
                for (i in freqs31.indices) {
                    val d = abs(log10(fDouble / freqs31[i].toDouble()))
                    if (d < minLogDist) {
                        minLogDist = d
                        closestIdx = i
                    }
                }
                gainDb += eq.bands31.getOrElse(closestIdx) { 0.0f }
                gainDb += eq.masterGainDb

                for (pb in eq.parametricBands) {
                    if (!pb.enabled || abs(pb.gainDb) < 0.05f) continue
                    val fc = pb.freqHz.coerceIn(20f, 20000f).toDouble()
                    val octDist = log10(fDouble / fc) / log10(2.0)
                    val absOct = abs(octDist)
                    val q = pb.q.coerceIn(0.3f, 10.0f).toDouble()
                    val sigma = (0.65 / q).coerceAtLeast(0.08)

                    val contribution = when (pb.filterType) {
                        ParametricFilterType.PEAKING -> {
                            pb.gainDb * exp(-0.5 * (absOct / sigma).pow(2.0)).toFloat()
                        }
                        ParametricFilterType.NOTCH -> {
                            -abs(pb.gainDb) * exp(-0.5 * (absOct / (sigma * 0.6)).pow(2.0)).toFloat()
                        }
                        ParametricFilterType.LOW_SHELF -> {
                            val slope = 3.2 * q.coerceIn(0.5, 2.5)
                            val sigmoid = 1.0 / (1.0 + exp(slope * octDist))
                            (pb.gainDb * sigmoid).toFloat()
                        }
                        ParametricFilterType.HIGH_SHELF -> {
                            val slope = 3.2 * q.coerceIn(0.5, 2.5)
                            val sigmoid = 1.0 / (1.0 + exp(-slope * octDist))
                            (pb.gainDb * sigmoid).toFloat()
                        }
                    }
                    gainDb += contribution
                }
            }

            // Smooth Tonal 3-Band Stage (Bass / Mid / Treble)
            val bassWeight = 1.0 / (1.0 + exp(3.0 * (log10(fDouble / 140.0) / log10(2.0))))
            val trebleWeight = 1.0 / (1.0 + exp(-3.0 * (log10(fDouble / 3500.0) / log10(2.0))))
            val midOct = abs(log10(fDouble / 900.0) / log10(2.0))
            val midWeight = exp(-0.5 * (midOct / 1.15).pow(2.0))

            if (abs(dsp.bassGainDb) > 0.05f) gainDb += (dsp.bassGainDb * bassWeight).toFloat()
            if (abs(dsp.midGainDb) > 0.05f) gainDb += (dsp.midGainDb * midWeight).toFloat()
            if (abs(dsp.trebleGainDb) > 0.05f) gainDb += (dsp.trebleGainDb * trebleWeight).toFloat()

            // Bass Boost (integrated cleanly when DynamicsProcessing owns low-end)
            if (includeBassBoostInCurve && dsp.bassBoostEnabled && dsp.bassBoostDb > 0.1f && freqHz <= 180) {
                val fcBoost = dsp.bassBoostFreqHz.coerceIn(30f, 100f).toDouble()
                val octDist = abs(log10(fDouble / fcBoost)) / log10(2.0)
                val w = exp(-0.5 * (octDist / 0.85).pow(2.0)).toFloat()
                gainDb += dsp.bassBoostDb * 0.45f * w
            }

            // Sub-Bass Extension
            if (dsp.subBassEnabled && dsp.subBassBoostDb > 0.1f && freqHz <= 110) {
                val distOct = abs(log10(fDouble / dsp.subBassFreqHz.coerceIn(20f, 85f).toDouble())) / log10(2.0)
                val w = exp(-0.5 * (distOct / 0.55).pow(2.0)).toFloat()
                gainDb += dsp.subBassBoostDb * 0.65f * w
            }

            // Punch (63-85Hz chest impact)
            if (dsp.bassPunchLevel > 0.1f && freqHz in 40..160) {
                val punchOct = abs(log10(fDouble / 72.0)) / log10(2.0)
                val w = exp(-0.5 * (punchOct / 0.48).pow(2.0)).toFloat()
                gainDb += dsp.bassPunchLevel * 0.55f * w
            }

            // Loudness Contour
            if (dsp.loudnessEnabled && dsp.loudnessGainDb > 0.1f) {
                gainDb += (dsp.loudnessGainDb * 0.5f * bassWeight).toFloat()
                gainDb += (dsp.loudnessGainDb * 0.42f * trebleWeight).toFloat()
            }

            // VIP Tube Warmth
            if (dsp.tubeWarmthLevel > 0.1f) {
                val warmOct = abs(log10(fDouble / 210.0)) / log10(2.0)
                val warmW = exp(-0.5 * (warmOct / 0.85).pow(2.0)).toFloat()
                gainDb += dsp.tubeWarmthLevel * 0.35f * warmW
                if (freqHz >= 8000) {
                    val sheenW = 1.0 / (1.0 + exp(-2.8 * (log10(fDouble / 10000.0) / log10(2.0))))
                    gainDb += (dsp.tubeWarmthLevel * 0.18f * sheenW).toFloat()
                }
            }

            // VIP Hi-Res Studio Clarity & Air
            if (dsp.hiResQualityEnabled) {
                if (freqHz in 200..400) {
                    val mudOct = abs(log10(fDouble / 280.0)) / log10(2.0)
                    gainDb -= (1.4f * exp(-0.5 * (mudOct / 0.5).pow(2.0))).toFloat()
                }
                if (freqHz >= 5000) {
                    val airW = 1.0 / (1.0 + exp(-3.0 * (log10(fDouble / 9000.0) / log10(2.0))))
                    gainDb += (2.4f * airW).toFloat()
                }
            }

            // Presence / Clarity / Exciter
            if (dsp.presenceEnabled && freqHz in 1000..5000) {
                val pOct = abs(log10(fDouble / 2500.0) / log10(2.0))
                gainDb += (dsp.presenceLevel * 0.5f * exp(-0.5 * (pOct / 0.65).pow(2.0))).toFloat()
            }
            if (dsp.clarityEnabled && freqHz >= 4000) {
                gainDb += (dsp.clarityLevel * 0.45f * trebleWeight).toFloat()
            }
            if (dsp.exciterEnabled && freqHz >= 6300) {
                val eW = 1.0 / (1.0 + exp(-3.5 * (log10(fDouble / 9000.0) / log10(2.0))))
                gainDb += (dsp.exciterLevel * 0.4f * eW).toFloat()
            }

            // Subsonic Protection Filter
            if (dsp.subsonicFilterActive && dsp.subsonicFrequencyHz > 16f) {
                val fcSub = dsp.subsonicFrequencyHz.coerceIn(15f, 50f).toDouble()
                val ratioSub = (fcSub / fDouble).coerceIn(0.01, 20.0)
                val attenSub = (-10.0 * log10(1.0 + ratioSub.pow(8.0))).toFloat()
                gainDb += attenSub.coerceIn(-24f, 0f)
            }

            // Smooth Butterworth Frequency Filters (HPF / LPF)
            if (dsp.hpfEnabled && dsp.hpfFrequencyHz > 16f) {
                val fc = dsp.hpfFrequencyHz.coerceIn(15f, 500f).toDouble()
                val order = (dsp.hpfSlopeDb / 6).coerceIn(1, 8)
                val ratio = (fc / fDouble).coerceIn(0.01, 20.0)
                val attenDb = (-10.0 * log10(1.0 + ratio.pow(2 * order))).toFloat()
                gainDb += attenDb.coerceIn(-24f, 0f)
            }
            if (dsp.lpfEnabled && dsp.lpfFrequencyHz < 19500f) {
                val fc = dsp.lpfFrequencyHz.coerceIn(50f, 20000f).toDouble()
                val order = (dsp.lpfSlopeDb / 6).coerceIn(1, 8)
                val ratio = (fDouble / fc).coerceIn(0.01, 20.0)
                val attenDb = (-10.0 * log10(1.0 + ratio.pow(2 * order))).toFloat()
                gainDb += attenDb.coerceIn(-24f, 0f)
            }

            return gainDb.coerceIn(-18f, 15f)
        }

        fun resolveAntiDoubleProcessingPlan(
            useDynamicsProcessing: Boolean,
            hasEqualizer: Boolean,
            hasBassBoost: Boolean,
            hasVirtualizer: Boolean
        ): DspCapabilityReport {
            val dpActive = useDynamicsProcessing
            val fallbackEqActive = !dpActive && hasEqualizer
            val fallbackBassActive = !dpActive && hasBassBoost
            return DspCapabilityReport(
                dynamicsProcessingSupported = dpActive,
                dynamicsProcessingActive = dpActive,
                usingFallbackEqualizer = fallbackEqActive,
                bassBoostSupported = hasBassBoost,
                bassBoostActive = fallbackBassActive,
                virtualizerSupported = hasVirtualizer,
                virtualizerActive = hasVirtualizer,
                antiDoubleProcessingSafe = !(dpActive && (fallbackEqActive || fallbackBassActive))
            )
        }
    }

    fun refreshOutputDeviceStatus() {
        try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val bluetooth = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                    it.type == AudioDeviceInfo.TYPE_HEARING_AID
            }
            val usb = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                    it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
            }
            val wired = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    it.type == AudioDeviceInfo.TYPE_LINE_ANALOG
            }

            val updated = when {
                bluetooth != null -> OutputDeviceStatus(
                    label = bluetooth.productName?.toString()?.takeIf { it.isNotBlank() }?.let { "BT: $it" }
                        ?: "Bluetooth Audio",
                    kind = OutputKind.BLUETOOTH
                )
                usb != null -> OutputDeviceStatus(
                    label = usb.productName?.toString()?.takeIf { it.isNotBlank() }?.let { "USB: $it" }
                        ?: "DAC / Audio USB",
                    kind = OutputKind.USB_AUDIO
                )
                wired != null -> OutputDeviceStatus(
                    label = "Auriculares / Auxiliar",
                    kind = OutputKind.WIRED_HEADSET
                )
                else -> OutputDeviceStatus(
                    label = "Altavoz del Teléfono",
                    kind = OutputKind.SPEAKER
                )
            }
            outputDeviceStatus = updated
            onOutputDeviceChangedListener?.invoke(updated)
        } catch (_: Throwable) {}
    }

    fun setVisualizerActive(active: Boolean) {
        isVisualizerUiActive = active
        try {
            visualizerFx?.enabled = active && isPlaying && activeAudioSessionId > 0
        } catch (_: Throwable) {}
    }

    fun getRtaSnapshot(): List<RtaBand> {
        if (!rtaDirty) return currentRtaBands
        rtaDirty = false
        val snapshot = ArrayList<RtaBand>(31)
        for (i in 0 until 31) {
            snapshot.add(
                rtaTemplate[i].copy(
                    levelDb = rtaLevelsDb[i],
                    peakDb = rtaPeaksDb[i]
                )
            )
        }
        currentRtaBands = snapshot
        return snapshot
    }

    fun attachToPlayerSession(sessionId: Int) {
        if (sessionId <= 0) return
        sessionTarget = AudioSessionTarget.INTERNAL_PLAYER
        attachAudioFx(sessionId, currentDspSettings, currentEqualizerSettings)
    }

    fun syncPlaybackStateFromMedia3(
        track: AudioTrackItem?,
        playing: Boolean,
        positionSeconds: Int
    ) {
        currentTrack = track
        isPlaying = playing
        playbackPositionSeconds = positionSeconds.coerceAtLeast(0)
        try {
            visualizerFx?.enabled = isVisualizerUiActive && playing && activeAudioSessionId > 0
        } catch (_: Throwable) {}
        if (!playing) {
            leftVuLevel = 0f
            rightVuLevel = 0f
            bassEnergy = 0f
            midEnergy = 0f
            trebleEnergy = 0f
        }
        onPlaybackStateChangedListener?.invoke()
    }

    fun attachToGlobalSystemSession(dsp: DspSettings? = null, eq: EqualizerSettings? = null) {
        val targetDsp = dsp ?: currentDspSettings
        val targetEq = eq ?: currentEqualizerSettings
        sessionTarget = AudioSessionTarget.GLOBAL_MIX_BEST_EFFORT
        attachAudioFx(0, targetDsp, targetEq)
    }

    fun onExternalSessionOpened(sessionId: Int, packageName: String?) {
        if (sessionId >= 0) {
            externalAudioSessionId = sessionId
            lastActiveSessionPackage = packageName ?: "Reproductor Externo"
            if (!isPlaying) {
                sessionTarget = if (sessionId > 0) {
                    AudioSessionTarget.EXTERNAL_APP
                } else {
                    AudioSessionTarget.GLOBAL_MIX_BEST_EFFORT
                }
                attachAudioFx(sessionId, currentDspSettings, currentEqualizerSettings)
            }
        }
    }

    fun onExternalSessionClosed(sessionId: Int) {
        if (externalAudioSessionId == sessionId) {
            externalAudioSessionId = 0
            lastActiveSessionPackage = "Sistema / Apps"
            if (!isPlaying) {
                attachToGlobalSystemSession()
            }
        }
    }

    fun getQuickBandGain(bandIndex: Int): Float {
        val repIdx = when (bandIndex) {
            0 -> 4
            1 -> 10
            2 -> 16
            3 -> 22
            else -> 28
        }
        return currentEqualizerSettings.bands31.getOrElse(repIdx) { 0f }
    }

    fun setQuickBandGain(bandIndex: Int, gainDb: Float) {
        val clamped = gainDb.coerceIn(-15f, 15f)
        val targetIndices31 = when (bandIndex) {
            0 -> listOf(0, 1, 2, 3, 4, 5, 6)
            1 -> listOf(7, 8, 9, 10, 11, 12)
            2 -> listOf(13, 14, 15, 16, 17, 18)
            3 -> listOf(19, 20, 21, 22, 23, 24)
            else -> listOf(25, 26, 27, 28, 29, 30)
        }
        val targetIndices15 = when (bandIndex) {
            0 -> listOf(0, 1, 2)
            1 -> listOf(3, 4, 5)
            2 -> listOf(6, 7, 8)
            3 -> listOf(9, 10, 11)
            else -> listOf(12, 13, 14)
        }
        val newBands31 = currentEqualizerSettings.bands31.toMutableList()
        for (idx in targetIndices31) {
            if (idx in newBands31.indices) newBands31[idx] = clamped
        }
        val newBands15 = currentEqualizerSettings.bands15.toMutableList()
        for (idx in targetIndices15) {
            if (idx in newBands15.indices) newBands15[idx] = clamped
        }
        val updatedEq = currentEqualizerSettings.copy(
            bands10 = EqualizerSettings.bands10From31(newBands31),
            bands31 = newBands31,
            bands15 = newBands15,
            isEnabled = true,
            activePresetName = "Personalizado"
        )
        currentEqualizerSettings = updatedEq
        applyDspSettings(currentDspSettings, updatedEq)
        onSettingsChangedListener?.invoke(currentDspSettings, updatedEq)
    }

    fun setQuickBassBoost(boostDb: Float) {
        val clamped = boostDb.coerceIn(0f, 24f)
        val updatedDsp = currentDspSettings.copy(
            bassBoostEnabled = clamped > 0.5f,
            bassBoostDb = clamped
        )
        currentDspSettings = updatedDsp
        applyDspSettings(updatedDsp, currentEqualizerSettings)
        onSettingsChangedListener?.invoke(updatedDsp, currentEqualizerSettings)
    }

    fun setQuickLoudness(gainDb: Float) {
        val clamped = gainDb.coerceIn(-12f, 18f)
        val updatedDsp = currentDspSettings.copy(preampDb = clamped)
        currentDspSettings = updatedDsp
        applyDspSettings(updatedDsp, currentEqualizerSettings)
        onSettingsChangedListener?.invoke(updatedDsp, currentEqualizerSettings)
    }

    fun toggleMasterPower(enabled: Boolean) {
        val updatedDsp = currentDspSettings.copy(dspMasterEnabled = enabled)
        currentDspSettings = updatedDsp
        applyDspSettings(updatedDsp, currentEqualizerSettings)
        onSettingsChangedListener?.invoke(updatedDsp, currentEqualizerSettings)
    }

    fun applyQuickPreset(presetName: String) {
        val bands5: FloatArray
        val bassDb: Float
        val preampDb: Float
        when (presetName.uppercase()) {
            "GRAVES" -> {
                bands5 = floatArrayOf(7f, 4f, -1f, 2f, 4f)
                bassDb = 8f
                preampDb = 2f
            }
            "VOCAL" -> {
                bands5 = floatArrayOf(0f, 0f, 5f, 4f, 2f)
                bassDb = 1f
                preampDb = 2f
            }
            "ROCK" -> {
                bands5 = floatArrayOf(5f, 3f, -1f, 4f, 5f)
                bassDb = 4f
                preampDb = 1f
            }
            "CLUB" -> {
                bands5 = floatArrayOf(8f, 5f, 0f, 4f, 6f)
                bassDb = 10f
                preampDb = 3f
            }
            else -> {
                bands5 = floatArrayOf(0f, 0f, 0f, 0f, 0f)
                bassDb = 0f
                preampDb = 0f
            }
        }
        val new31 = MutableList(31) { idx ->
            when {
                idx <= 6 -> bands5[0]
                idx <= 12 -> bands5[1]
                idx <= 18 -> bands5[2]
                idx <= 24 -> bands5[3]
                else -> bands5[4]
            }
        }
        val new15 = MutableList(15) { idx ->
            when {
                idx <= 2 -> bands5[0]
                idx <= 5 -> bands5[1]
                idx <= 8 -> bands5[2]
                idx <= 11 -> bands5[3]
                else -> bands5[4]
            }
        }
        val updatedEq = currentEqualizerSettings.copy(
            bands10 = EqualizerSettings.bands10From31(new31),
            bands31 = new31,
            bands15 = new15,
            isEnabled = true,
            activePresetName = presetName
        )
        val updatedDsp = currentDspSettings.copy(
            bassBoostEnabled = bassDb > 0.5f,
            bassBoostDb = bassDb,
            preampDb = preampDb
        )
        currentEqualizerSettings = updatedEq
        currentDspSettings = updatedDsp
        applyDspSettings(updatedDsp, updatedEq)
        onSettingsChangedListener?.invoke(updatedDsp, updatedEq)
    }

    fun setVolume(volume: Float) {
        volumeFactor = volume.coerceIn(0f, 1f)
        onVolumeChangedListener?.invoke(volumeFactor)
    }

    private fun attachAudioFx(sessionId: Int, dsp: DspSettings, eq: EqualizerSettings) {
        if (sessionId < 0) return
        synchronized(fxLock) {
            if (activeAudioSessionId == sessionId && (dynamicsProcessingFx != null || equalizerFx != null)) {
                commitHardwareParameters(dsp, eq)
                return
            }
            activeAudioSessionId = sessionId
            releaseAudioFxLocked()

            // 1. Hardware DynamicsProcessing (API 28+: unified 31-band PostEQ + MBC + Limiter + Balance InputGain)
            useDynamicsProcessingPostEq = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val bandCount31 = EqualizerSettings.FREQUENCIES_HZ_31.size
                    val config = DynamicsProcessing.Config.Builder(
                        DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                        2,
                        false, 0,
                        true, 4,
                        true, bandCount31,
                        true
                    ).build()
                    dynamicsProcessingFx = DynamicsProcessing(0, sessionId, config).apply {
                        enabled = dsp.dspMasterEnabled
                    }
                    cachedPostEq = DynamicsProcessing.Eq(true, dsp.dspMasterEnabled, bandCount31)
                    cachedMbc = DynamicsProcessing.Mbc(true, dsp.dspMasterEnabled && dsp.compressorEnabled, 4)
                    cachedLimiter = DynamicsProcessing.Limiter(
                        true,
                        dsp.dspMasterEnabled && dsp.limiterEnabled,
                        0,
                        0.5f,
                        65.0f,
                        10.0f,
                        dsp.limiterCeilingDb.coerceIn(-24f, -0.2f),
                        0f
                    )
                    useDynamicsProcessingPostEq = true
                } catch (_: Throwable) {
                    try {
                        dynamicsProcessingFx?.release()
                    } catch (_: Throwable) {}
                    dynamicsProcessingFx = null
                    cachedPostEq = null
                    cachedMbc = null
                    cachedLimiter = null
                    useDynamicsProcessingPostEq = false
                }
            }

            // 2. Fallback Equalizer + LoudnessEnhancer + BassBoost (NEVER instantiated simultaneously with DynamicsProcessing!)
            if (!useDynamicsProcessingPostEq) {
                try {
                    equalizerFx = Equalizer(0, sessionId)
                } catch (_: Throwable) {
                    equalizerFx = null
                }
                try {
                    loudnessEnhancerFx = LoudnessEnhancer(sessionId)
                } catch (_: Throwable) {
                    loudnessEnhancerFx = null
                }
                try {
                    bassBoostFx = BassBoost(0, sessionId)
                } catch (_: Throwable) {
                    bassBoostFx = null
                }
            }

            // 3. Hardware Virtualizer (Stereo Spatializer)
            try {
                virtualizerFx = Virtualizer(0, sessionId)
            } catch (_: Throwable) {
                virtualizerFx = null
            }

            commitHardwareParameters(dsp, eq)

            val eqBandCount = when {
                useDynamicsProcessingPostEq -> EqualizerSettings.FREQUENCIES_HZ_31.size
                equalizerFx != null -> runCatching { equalizerFx?.numberOfBands?.toInt() ?: 0 }.getOrDefault(0)
                else -> 0
            }
            val bassSupported = useDynamicsProcessingPostEq ||
                runCatching { bassBoostFx?.strengthSupported == true }.getOrDefault(false)
            val virtSupported = runCatching { virtualizerFx?.strengthSupported == true }.getOrDefault(false)

            val summary = when {
                useDynamicsProcessingPostEq -> "DynamicsProcessing 31B + Dinámica (Sin doble proceso)"
                equalizerFx != null -> "Ecualizador Nativo (${eqBandCount}B) + BassBoost Hardware"
                bassBoostFx != null -> "Solo BassBoost Hardware (EQ limitado)"
                else -> "Modo Compatibilidad (AudioFX restringido por sesión)"
            }
            fxEngineStatus = summary

            capabilityReport = DspCapabilityReport(
                audioSessionId = sessionId,
                dynamicsProcessingSupported = useDynamicsProcessingPostEq,
                dynamicsProcessingActive = useDynamicsProcessingPostEq,
                equalizerBandCount = eqBandCount,
                usingFallbackEqualizer = !useDynamicsProcessingPostEq && equalizerFx != null,
                bassBoostSupported = bassSupported,
                bassBoostActive = !useDynamicsProcessingPostEq && bassBoostFx != null,
                virtualizerSupported = virtSupported,
                virtualizerActive = virtualizerFx != null,
                antiDoubleProcessingSafe = true,
                engineSummary = summary
            )

            if (!useDynamicsProcessingPostEq && equalizerFx == null && sessionId > 0) {
                onAudioFaultListener?.invoke(
                    "Motor AudioFX Limitado",
                    "Este dispositivo no expuso el módulo completo de ecualización en hardware para la sesión actual.",
                    FaultSeverity.WARNING
                )
            }

            if (sessionId > 0) {
                try {
                    visualizerFx = Visualizer(sessionId).apply {
                        val range = Visualizer.getCaptureSizeRange()
                        if (range != null && range.size > 1) {
                            captureSize = range[1].coerceAtMost(1024)
                            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                                override fun onWaveFormDataCapture(vis: Visualizer?, waveform: ByteArray?, rate: Int) {
                                    if (waveform != null && !isMicRtaActive && isVisualizerUiActive) {
                                        processPlaybackWaveform(waveform)
                                    }
                                }

                                override fun onFftDataCapture(vis: Visualizer?, fft: ByteArray?, rate: Int) {
                                    if (fft != null && !isMicRtaActive && isVisualizerUiActive) {
                                        processPlaybackFft(fft)
                                    }
                                }
                            }, (Visualizer.getMaxCaptureRate() / 2).coerceAtMost(20000), true, true)
                            enabled = isVisualizerUiActive && isPlaying
                        }
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    fun applyDspSettings(dsp: DspSettings, eq: EqualizerSettings) {
        currentDspSettings = dsp
        currentEqualizerSettings = eq

        if (equalizerFx == null && dynamicsProcessingFx == null && dsp.dspMasterEnabled) {
            val sessionToAttach = if (activeAudioSessionId > 0) activeAudioSessionId else externalAudioSessionId
            if (sessionToAttach >= 0) {
                attachAudioFx(sessionToAttach, dsp, eq)
                return
            }
        }

        val now = android.os.SystemClock.uptimeMillis()
        val elapsed = now - lastHardwareCommitMs
        if (elapsed >= MIN_HARDWARE_COMMIT_INTERVAL_MS) {
            lastHardwareCommitMs = now
            pendingCommitJob?.cancel()
            pendingCommitJob = scope.launch {
                synchronized(fxLock) {
                    commitHardwareParameters(currentDspSettings, currentEqualizerSettings)
                }
            }
        } else if (pendingCommitJob == null || pendingCommitJob?.isActive != true) {
            pendingCommitJob = scope.launch {
                delay(MIN_HARDWARE_COMMIT_INTERVAL_MS)
                lastHardwareCommitMs = android.os.SystemClock.uptimeMillis()
                synchronized(fxLock) {
                    commitHardwareParameters(currentDspSettings, currentEqualizerSettings)
                }
            }
        }
    }

    private fun commitHardwareParameters(dsp: DspSettings, eq: EqualizerSettings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useDynamicsProcessingPostEq) {
            dynamicsProcessingFx?.let { dp ->
                try {
                    dp.enabled = dsp.dspMasterEnabled

                    val freqs31 = EqualizerSettings.FREQUENCIES_HZ_31
                    var maxEqBoost = 0f
                    val postEq = cachedPostEq ?: DynamicsProcessing.Eq(true, dsp.dspMasterEnabled, freqs31.size).also {
                        cachedPostEq = it
                    }
                    postEq.isEnabled = dsp.dspMasterEnabled
                    for (i in freqs31.indices) {
                        val gain = computeEffectiveBandGainDb(
                            freqHz = freqs31[i],
                            dsp = dsp,
                            eq = eq,
                            includeBassBoostInCurve = true
                        )
                        if (gain > maxEqBoost) maxEqBoost = gain
                        postEq.getBand(i).apply {
                            isEnabled = dsp.dspMasterEnabled
                            cutoffFrequency = computeIsoUpperCutoffHz(i, freqs31)
                            this.gain = gain
                        }
                    }
                    dp.setPostEqAllChannelsTo(postEq)

                    val rawInputGain = if (dsp.dspMasterEnabled) (dsp.preampDb + dsp.masterGainDb) else 0f
                    val compensatedInputGain = if (dsp.dspMasterEnabled && dsp.limiterEnabled && (rawInputGain + maxEqBoost) > 16f) {
                        val trim = ((rawInputGain + maxEqBoost) - 16f).coerceAtMost(8f)
                        (rawInputGain - trim).coerceIn(-30f, 18f)
                    } else {
                        rawInputGain.coerceIn(-30f, 20f)
                    }

                    val pan = if (dsp.dspMasterEnabled) dsp.balancePan.coerceIn(-1f, 1f) else 0f
                    val leftTrimDb = if (pan > 0.01f) {
                        if (pan >= 0.96f) -60f else (20f * log10((1f - pan).coerceAtLeast(0.01f)))
                    } else 0f
                    val rightTrimDb = if (pan < -0.01f) {
                        if (pan <= -0.96f) -60f else (20f * log10((1f + pan).coerceAtLeast(0.01f)))
                    } else 0f

                    if (abs(pan) < 0.01f) {
                        dp.setInputGainAllChannelsTo(compensatedInputGain)
                    } else {
                        dp.setInputGainbyChannel(0, (compensatedInputGain + leftTrimDb).coerceIn(-60f, 20f))
                        dp.setInputGainbyChannel(1, (compensatedInputGain + rightTrimDb).coerceIn(-60f, 20f))
                    }

                    val mbcBands = floatArrayOf(120f, 800f, 4000f, 20000f)
                    val mbc = cachedMbc ?: DynamicsProcessing.Mbc(true, dsp.dspMasterEnabled && dsp.compressorEnabled, mbcBands.size).also {
                        cachedMbc = it
                    }
                    val compOn = dsp.dspMasterEnabled && dsp.compressorEnabled
                    mbc.isEnabled = compOn
                    for (i in mbcBands.indices) {
                        mbc.getBand(i).apply {
                            isEnabled = compOn
                            cutoffFrequency = mbcBands[i]
                            threshold = dsp.compressorThresholdDb.coerceIn(-48f, 0f)
                            ratio = dsp.compressorRatio.coerceIn(1f, 20f)
                            attackTime = dsp.compressorAttackMs.coerceIn(1f, 100f)
                            releaseTime = dsp.compressorReleaseMs.coerceIn(15f, 1000f)
                            kneeWidth = 6f
                            noiseGateThreshold = -90f
                            expanderRatio = 1f
                            preGain = 0f
                            postGain = if (compOn) 1.0f else 0f
                        }
                    }
                    dp.setMbcAllChannelsTo(mbc)

                    val limOn = dsp.dspMasterEnabled && dsp.limiterEnabled
                    val limiter = cachedLimiter ?: DynamicsProcessing.Limiter(
                        true, limOn, 0, 0.5f, 65.0f, 10.0f, dsp.limiterCeilingDb.coerceIn(-24f, -0.2f), 0f
                    ).also { cachedLimiter = it }
                    limiter.apply {
                        isEnabled = limOn
                        attackTime = 0.5f
                        releaseTime = 65.0f
                        ratio = 10.0f
                        threshold = dsp.limiterCeilingDb.coerceIn(-24f, -0.2f)
                        postGain = 0f
                    }
                    dp.setLimiterAllChannelsTo(limiter)
                } catch (_: Throwable) {}
            }
        } else {
            try {
                equalizerFx?.let { eqFx ->
                    val numBands = eqFx.numberOfBands
                    val (minLevel, maxLevel) = eqFx.bandLevelRange
                    for (b in 0 until numBands) {
                        val centerFreqHz = eqFx.getCenterFreq(b.toShort()) / 1000
                        val targetGainDb = computeEffectiveBandGainDb(
                            freqHz = centerFreqHz,
                            dsp = dsp,
                            eq = eq,
                            includeBassBoostInCurve = false
                        )
                        val millibels = (targetGainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                        eqFx.setBandLevel(b.toShort(), millibels.toShort())
                    }
                    eqFx.enabled = dsp.dspMasterEnabled
                }
            } catch (_: Throwable) {}

            try {
                loudnessEnhancerFx?.let { le ->
                    val targetGain = if (dsp.limiterEnabled) {
                        (dsp.preampDb + dsp.masterGainDb).coerceIn(-20f, 16f)
                    } else {
                        (dsp.preampDb + dsp.masterGainDb).coerceIn(-20f, 24f)
                    }
                    le.setTargetGain((targetGain * 100).toInt())
                    le.enabled = dsp.dspMasterEnabled && (dsp.preampDb != 0f || dsp.masterGainDb != 0f)
                }
            } catch (_: Throwable) {}

            try {
                bassBoostFx?.let { bb ->
                    if (bb.strengthSupported) {
                        val strength = ((dsp.bassBoostDb / 24.0f) * 1000f).toInt().coerceIn(0, 1000)
                        bb.setStrength(strength.toShort())
                    }
                    bb.enabled = dsp.dspMasterEnabled && dsp.bassBoostEnabled && dsp.bassBoostDb > 0.2f
                }
            } catch (_: Throwable) {}
        }

        try {
            virtualizerFx?.let { virt ->
                if (virt.strengthSupported) {
                    val strength = (((dsp.stereoWidthPercent - 100f) / 150f) * 1000f).toInt().coerceIn(0, 1000)
                    virt.setStrength(strength.toShort())
                }
                virt.enabled = dsp.dspMasterEnabled && dsp.stereoWidthPercent > 105f
            }
        } catch (_: Throwable) {}
    }

    private fun releaseAudioFxLocked() {
        try {
            visualizerFx?.enabled = false
            visualizerFx?.release()
        } catch (_: Throwable) {}
        visualizerFx = null

        try {
            dynamicsProcessingFx?.enabled = false
            dynamicsProcessingFx?.release()
        } catch (_: Throwable) {}
        dynamicsProcessingFx = null
        cachedPostEq = null
        cachedMbc = null
        cachedLimiter = null
        useDynamicsProcessingPostEq = false

        try {
            equalizerFx?.enabled = false
            equalizerFx?.release()
        } catch (_: Throwable) {}
        equalizerFx = null

        try {
            bassBoostFx?.enabled = false
            bassBoostFx?.release()
        } catch (_: Throwable) {}
        bassBoostFx = null

        try {
            virtualizerFx?.enabled = false
            virtualizerFx?.release()
        } catch (_: Throwable) {}
        virtualizerFx = null

        try {
            loudnessEnhancerFx?.enabled = false
            loudnessEnhancerFx?.release()
        } catch (_: Throwable) {}
        loudnessEnhancerFx = null
    }

    private fun releaseAudioFx() {
        synchronized(fxLock) {
            activeAudioSessionId = -1
            releaseAudioFxLocked()
        }
    }

    private fun processPlaybackWaveform(waveform: ByteArray) {
        val n = waveform.size
        if (n == 0) return

        var sumSq = 0.0
        val step = (n / 128).coerceAtLeast(1)
        for (i in 0 until 128) {
            val raw = waveform[(i * step).coerceAtMost(n - 1)].toInt() and 0xFF
            val normalized = (raw - 128).toFloat() / 128f
            oscBuffer[i] = normalized
            sumSq += (normalized * normalized)
        }
        for (i in 0 until 64) {
            waveformBuffer[i] = oscBuffer[(i * 2).coerceAtMost(127)]
        }

        val rms = sqrt(sumSq / 128.0).toFloat()
        val calculatedDb = if (rms > 0.001f) 20 * log10(rms) else -60f
        rmsLevelDb = calculatedDb.coerceIn(-60f, 0f)

        val spl = (88f + (rmsLevelDb + 45f) * 1.1f + splCalibrationOffsetDb).coerceIn(40f, 138f)
        currentSplDb = spl
        peakSplDb = if (currentSplDb > peakSplDb) currentSplDb else (peakSplDb * 0.97f).coerceAtLeast(currentSplDb)

        val targetLevel = (rms * 1.85f).coerceIn(0f, 1f)
        val pan = currentDspSettings.balancePan.coerceIn(-1f, 1f)
        val targetL = (if (pan <= 0f) targetLevel else targetLevel * (1f - pan * 0.6f)).coerceIn(0f, 1f)
        val targetR = (if (pan >= 0f) targetLevel * 0.98f else targetLevel * (1f + pan * 0.6f)).coerceIn(0f, 1f)

        leftVuLevel = if (targetL > leftVuLevel) leftVuLevel + (targetL - leftVuLevel) * 0.65f else leftVuLevel + (targetL - leftVuLevel) * 0.22f
        rightVuLevel = if (targetR > rightVuLevel) rightVuLevel + (targetR - rightVuLevel) * 0.65f else rightVuLevel + (targetR - rightVuLevel) * 0.22f

        val fallbackEnergy = (targetLevel * 0.85f).coerceIn(0f, 1f)
        if (bassEnergy < 0.01f && midEnergy < 0.01f) {
            bassEnergy = fallbackEnergy * 0.8f
            midEnergy = fallbackEnergy * 0.7f
            trebleEnergy = fallbackEnergy * 0.55f
        }

        isClippingDetected = (rms > 0.94f)
        if (isClippingDetected) clipEventsCount++
    }

    private fun processPlaybackFft(fft: ByteArray) {
        if (fft.isEmpty()) return
        val numMagnitudes = fft.size / 2
        if (numMagnitudes <= 1) return
        if (fftMagnitudesBuffer.size < numMagnitudes) {
            fftMagnitudesBuffer = FloatArray(numMagnitudes)
        }

        fftMagnitudesBuffer[0] = abs(fft[0].toFloat())
        for (k in 1 until numMagnitudes) {
            val r = fft[2 * k].toFloat()
            val im = fft[2 * k + 1].toFloat()
            fftMagnitudesBuffer[k] = sqrt(r * r + im * im)
        }

        val halfSampleRate = sampleRate / 2f
        var bassAccum = 0f
        var midAccum = 0f
        var trebleAccum = 0f
        for (i in 0 until 31) {
            val binIdx = ((rtaFrequencies[i].toFloat() / halfSampleRate) * numMagnitudes)
                .toInt()
                .coerceIn(1, numMagnitudes - 1)
            val mag = fftMagnitudesBuffer[binIdx]
            val targetDb = if (mag > 0.01f) (20f * log10(mag / 128f)).coerceIn(-60f, 0f) else -60f
            val prev = rtaLevelsDb[i]
            val smoothed = if (targetDb > prev) prev + (targetDb - prev) * 0.68f else prev + (targetDb - prev) * 0.28f
            rtaLevelsDb[i] = smoothed
            rtaPeaksDb[i] = if (smoothed > rtaPeaksDb[i]) smoothed else (rtaPeaksDb[i] - 1.1f).coerceAtLeast(-60f)

            val norm = ((smoothed + 54f) / 54f).coerceIn(0f, 1f)
            when {
                i <= 8 -> bassAccum += norm
                i <= 21 -> midAccum += norm
                else -> trebleAccum += norm
            }
        }
        bassEnergy = (bassAccum / 9f).coerceIn(0f, 1f)
        midEnergy = (midAccum / 13f).coerceIn(0f, 1f)
        trebleEnergy = (trebleAccum / 9f).coerceIn(0f, 1f)
        rtaDirty = true
    }

    @SuppressLint("MissingPermission")
    fun startMicRta(hasRecordPermission: Boolean) {
        if (!hasRecordPermission || isMicRtaActive) return

        try {
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2
            )
            audioRecord?.startRecording()
            isMicRtaActive = true

            micRecordJob = scope.launch {
                val buffer = ShortArray(1024)
                while (isActive && isMicRtaActive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0 && isVisualizerUiActive) {
                        processMicPcm(buffer, read)
                    }
                    delay(45)
                }
            }
        } catch (_: Exception) {
            isMicRtaActive = false
        }
    }

    fun stopMicRta() {
        isMicRtaActive = false
        micRecordJob?.cancel()
        micRecordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    private fun processMicPcm(buffer: ShortArray, readSize: Int) {
        var sumSquares = 0.0
        var maxAmp = 0
        var clippedSamples = 0

        for (i in 0 until readSize) {
            val sample = buffer[i].toInt()
            sumSquares += (sample * sample).toDouble()
            val absSample = abs(sample)
            if (absSample > maxAmp) maxAmp = absSample
            if (absSample >= 32600) clippedSamples++
        }

        val rms = sqrt(sumSquares / readSize)
        val db = if (rms > 1) 20 * log10(rms / Short.MAX_VALUE) else -60.0
        rmsLevelDb = db.toFloat().coerceIn(-60f, 0f)

        isClippingDetected = (clippedSamples > 0)
        if (isClippingDetected) clipEventsCount += clippedSamples

        val calculatedSpl = (94.0f + db.toFloat() + 35.0f + splCalibrationOffsetDb).coerceIn(35f, 145f)
        currentSplDb = calculatedSpl
        peakSplDb = if (currentSplDb > peakSplDb) currentSplDb else (peakSplDb * 0.96f).coerceAtLeast(currentSplDb)

        val normLevel = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
        leftVuLevel = normLevel
        rightVuLevel = normLevel * 0.96f

        for (i in 0 until 128) {
            oscBuffer[i] = buffer[i.coerceAtMost(readSize - 1)].toFloat() / Short.MAX_VALUE
        }
        for (i in 0 until 64) {
            waveformBuffer[i] = oscBuffer[(i * 2).coerceAtMost(127)]
        }

        val step = (readSize / 128).coerceAtLeast(1)
        val numSamples = (readSize / step).coerceAtLeast(1)

        for (i in 0 until 31) {
            val f = rtaFrequencies[i].toDouble()
            val omega = 2.0 * PI * f / sampleRate
            var realSum = 0.0
            var imagSum = 0.0
            var sampleIdx = 0
            while (sampleIdx < readSize) {
                val s = buffer[sampleIdx].toDouble()
                val angle = omega * sampleIdx
                realSum += s * cos(angle)
                imagSum += s * sin(angle)
                sampleIdx += step
            }
            val magnitude = sqrt(realSum * realSum + imagSum * imagSum) / numSamples
            val rawBandDb = if (magnitude > 1.0) (20 * log10(magnitude / Short.MAX_VALUE)).toFloat() else -60f
            val bandDb = (rawBandDb + splCalibrationOffsetDb * 0.2f).coerceIn(-60f, 0f)
            rtaLevelsDb[i] = bandDb
            rtaPeaksDb[i] = if (bandDb > rtaPeaksDb[i]) bandDb else (rtaPeaksDb[i] - 1.2f).coerceAtLeast(-60f)
        }
        rtaDirty = true
    }

    fun release() {
        stopMicRta()
        releaseAudioFx()
        try {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Throwable) {}
    }
}
