package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.ParametricFilterType
import com.example.model.RtaBand
import com.example.model.SplWeighting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class CarAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Media Player for phone music files
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null

    // Real Android AudioFx pipeline
    private var equalizerFx: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var virtualizerFx: Virtualizer? = null
    private var loudnessEnhancerFx: LoudnessEnhancer? = null
    private var dynamicsProcessingFx: DynamicsProcessing? = null
    private var useDynamicsProcessingPostEq: Boolean = false
    private var visualizerFx: Visualizer? = null
    var activeAudioSessionId: Int = 0
        private set

    // Microphone RTA
    private var audioRecord: AudioRecord? = null
    private var micRecordJob: Job? = null
    var isMicRtaActive = false
        private set

    // Real-time audio levels
    var currentSplDb: Float = 0.0f
        private set
    var peakSplDb: Float = 0.0f
        private set
    var leftVuLevel: Float = 0.0f
        private set
    var rightVuLevel: Float = 0.0f
        private set
    var currentWaveform: FloatArray = FloatArray(64)
        private set
    var currentRtaBands: List<RtaBand> = createInitialRtaBands()
        private set

    // Real DSP Telemetry & Oscilloscope
    var rmsLevelDb: Float = -60.0f
        private set
    var isClippingDetected: Boolean = false
        private set
    var clipEventsCount: Int = 0
        private set
    var rawOscilloscopePcm: FloatArray = FloatArray(128)
        private set
    var splCalibrationOffsetDb: Float = 0.0f
    var splWeighting: SplWeighting = SplWeighting.C_WEIGHTING
    var sourceMode: AudioSourceMode = AudioSourceMode.INTERNAL_DSP
        private set

    // Playback state
    var isPlaying = false
        private set
    var playbackPositionSeconds = 0
        private set
    var currentTrack: AudioTrackItem? = null
        private set
    var onTrackCompletedListener: (() -> Unit)? = null

    // Master volume factor (0.0..1.0)
    var volumeFactor: Float = 0.85f
        private set

    private val sampleRate = 44100

    // Real AudioFX Setup & Routing (Global Session 0, External YouTube Sessions, Local MediaPlayer)
    var externalAudioSessionId: Int = 0
        private set
    var lastActiveSessionPackage: String = "YouTube / Sistema"
        private set
    var currentDspSettings: DspSettings = DspSettings()
        private set
    var currentEqualizerSettings: EqualizerSettings = EqualizerSettings()
        private set
    var onSettingsChangedListener: ((DspSettings, EqualizerSettings) -> Unit)? = null

    companion object {
        @Volatile
        private var instance: CarAudioEngine? = null

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
    }

    fun attachToGlobalSystemSession(dsp: DspSettings? = null, eq: EqualizerSettings? = null) {
        val targetDsp = dsp ?: currentDspSettings
        val targetEq = eq ?: currentEqualizerSettings
        attachAudioFx(0, targetDsp, targetEq)
    }

    fun onExternalSessionOpened(sessionId: Int, packageName: String?) {
        if (sessionId >= 0) {
            externalAudioSessionId = sessionId
            lastActiveSessionPackage = packageName ?: "YouTube / Sistema"
            if (mediaPlayer == null || !isPlaying) {
                attachAudioFx(sessionId, currentDspSettings, currentEqualizerSettings)
            }
        }
    }

    fun onExternalSessionClosed(sessionId: Int) {
        if (externalAudioSessionId == sessionId) {
            externalAudioSessionId = 0
            lastActiveSessionPackage = "YouTube / Sistema"
            if (mediaPlayer == null || !isPlaying) {
                attachToGlobalSystemSession()
            }
        }
    }

    fun getQuickBandGain(bandIndex: Int): Float {
        val repIdx = when (bandIndex) {
            0 -> 4   // ~50-60Hz
            1 -> 10  // ~200-230Hz
            2 -> 16  // ~800-910Hz
            3 -> 22  // ~3.1k-3.6k
            else -> 28 // ~12.5k-14k
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
            bands31 = newBands31,
            bands15 = newBands15,
            isEnabled = true,
            activePresetName = "En Vivo"
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
        when (presetName.uppercase()) {
            "PUNCH BASS" -> {
                setQuickBandGain(0, 9f)
                setQuickBandGain(1, 5f)
                setQuickBandGain(2, -1f)
                setQuickBandGain(3, 2f)
                setQuickBandGain(4, 5f)
                setQuickBassBoost(12f)
                setQuickLoudness(4f)
            }
            "YOUTUBE VOCAL" -> {
                setQuickBandGain(0, 1f)
                setQuickBandGain(1, 0f)
                setQuickBandGain(2, 6f)
                setQuickBandGain(3, 4f)
                setQuickBandGain(4, 2f)
                setQuickBassBoost(2f)
                setQuickLoudness(3f)
            }
            "ROCK" -> {
                setQuickBandGain(0, 6f)
                setQuickBandGain(1, 3f)
                setQuickBandGain(2, -1f)
                setQuickBandGain(3, 4f)
                setQuickBandGain(4, 6f)
                setQuickBassBoost(6f)
                setQuickLoudness(2f)
            }
            "CLUB" -> {
                setQuickBandGain(0, 11f)
                setQuickBandGain(1, 7f)
                setQuickBandGain(2, 1f)
                setQuickBandGain(3, 5f)
                setQuickBandGain(4, 8f)
                setQuickBassBoost(15f)
                setQuickLoudness(5f)
            }
            else -> {
                for (b in 0..4) setQuickBandGain(b, 0f)
                setQuickBassBoost(0f)
                setQuickLoudness(0f)
            }
        }
    }

    fun setVolume(volume: Float) {
        volumeFactor = volume.coerceIn(0f, 1f)
        applyPlayerVolume()
    }

    private fun applyPlayerVolume() {
        val vol = volumeFactor.coerceIn(0f, 1f)
        val pan = if (currentDspSettings.dspMasterEnabled) currentDspSettings.balancePan.coerceIn(-1f, 1f) else 0f
        val left = (if (pan <= 0f) vol else vol * (1f - pan)).coerceIn(0f, 1f)
        val right = (if (pan >= 0f) vol else vol * (1f + pan)).coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(left, right)
        } catch (_: Throwable) {}
    }

    private fun attachAudioFx(sessionId: Int, dsp: DspSettings, eq: EqualizerSettings) {
        if (sessionId < 0) return
        activeAudioSessionId = sessionId
        releaseAudioFx()

        // 1. Hardware DynamicsProcessing (API 28+: 31-band PostEQ + MBC Compressor + Limiter + InputGain)
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
                useDynamicsProcessingPostEq = true
            } catch (_: Throwable) {
                try {
                    dynamicsProcessingFx?.release()
                } catch (_: Throwable) {}
                dynamicsProcessingFx = null
                useDynamicsProcessingPostEq = false
            }
        }

        // 2. Standard Android Equalizer & LoudnessEnhancer (used when DynamicsProcessing isn't handling PostEQ)
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
        }

        // 3. Hardware Bass Boost
        try {
            bassBoostFx = BassBoost(0, sessionId)
        } catch (_: Throwable) {
            bassBoostFx = null
        }

        // 4. Hardware Virtualizer (Stereo Spatializer)
        try {
            virtualizerFx = Virtualizer(0, sessionId)
        } catch (_: Throwable) {
            virtualizerFx = null
        }

        // Apply all current DSP parameters immediately
        applyDspSettings(dsp, eq)

        // 5. Visualizer: Only attach for local MediaPlayer sessions (> 0)
        if (sessionId > 0) {
            try {
                visualizerFx = Visualizer(sessionId).apply {
                    val range = Visualizer.getCaptureSizeRange()
                    if (range != null && range.size > 1) {
                        captureSize = range[1]
                        setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(vis: Visualizer?, waveform: ByteArray?, rate: Int) {
                                if (waveform != null && !isMicRtaActive) {
                                    processPlaybackWaveform(waveform)
                                }
                            }

                            override fun onFftDataCapture(vis: Visualizer?, fft: ByteArray?, rate: Int) {
                                if (fft != null && !isMicRtaActive) {
                                    processPlaybackFft(fft)
                                }
                            }
                        }, Visualizer.getMaxCaptureRate() / 2, true, true)
                        enabled = true
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    private fun computeEffectiveBandGainDb(freqHz: Int, dsp: DspSettings, eq: EqualizerSettings): Float {
        if (!dsp.dspMasterEnabled) return 0.0f

        var gainDb = 0.0f

        if (eq.isEnabled) {
            // Base 31-band ISO graphic curve
            val freqs31 = EqualizerSettings.FREQUENCIES_HZ_31
            var closestIdx = 0
            var minDiff = Int.MAX_VALUE
            for (i in freqs31.indices) {
                val diff = abs(freqs31[i] - freqHz)
                if (diff < minDiff) {
                    minDiff = diff
                    closestIdx = i
                }
            }
            gainDb += eq.bands31.getOrElse(closestIdx) { 0.0f }
            gainDb += eq.masterGainDb

            // Active 5-band parametric EQ filters
            for (pb in eq.parametricBands) {
                if (!pb.enabled || abs(pb.gainDb) < 0.05f) continue
                val ratio = freqHz.toDouble() / pb.freqHz.coerceAtLeast(20f).toDouble()
                val octDist = abs(log10(ratio)) / log10(2.0)
                val weight = when (pb.filterType) {
                    ParametricFilterType.LOW_SHELF -> if (freqHz <= pb.freqHz) 1.0f else (1.0 - octDist * 1.2).coerceIn(0.0, 1.0).toFloat()
                    ParametricFilterType.HIGH_SHELF -> if (freqHz >= pb.freqHz) 1.0f else (1.0 - octDist * 1.2).coerceIn(0.0, 1.0).toFloat()
                    ParametricFilterType.NOTCH -> (1.0 - octDist * pb.q * 1.5).coerceIn(0.0, 1.0).toFloat()
                    ParametricFilterType.PEAKING -> (1.0 - octDist * pb.q).coerceIn(0.0, 1.0).toFloat()
                }
                gainDb += if (pb.filterType == ParametricFilterType.NOTCH) -abs(pb.gainDb) * weight else pb.gainDb * weight
            }
        }

        // Tonal 3-Band Stage (Bass / Mid / Treble)
        if (freqHz <= 160 && abs(dsp.bassGainDb) > 0.05f) {
            val weight = if (freqHz <= 100) 1.0f else 0.55f
            gainDb += dsp.bassGainDb * weight
        } else if (freqHz in 200..2500 && abs(dsp.midGainDb) > 0.05f) {
            val weight = if (freqHz in 315..1600) 1.0f else 0.6f
            gainDb += dsp.midGainDb * weight
        } else if (freqHz >= 3150 && abs(dsp.trebleGainDb) > 0.05f) {
            val weight = if (freqHz >= 5000) 1.0f else 0.6f
            gainDb += dsp.trebleGainDb * weight
        }

        // Sub-Bass Extreme
        if (dsp.subBassEnabled && dsp.subBassBoostDb > 0.1f && freqHz <= 90) {
            val dist = abs(log10(freqHz.coerceAtLeast(20) / dsp.subBassFreqHz.coerceIn(20f, 80f).toDouble())) / log10(2.0)
            val w = (1.0 - dist * 0.9).coerceIn(0.0, 1.0).toFloat()
            gainDb += dsp.subBassBoostDb * 0.65f * w
        }

        // Loudness Contour (Fletcher-Munson U-Curve)
        if (dsp.loudnessEnabled && dsp.loudnessGainDb > 0.1f) {
            if (freqHz <= 100) gainDb += dsp.loudnessGainDb * 0.55f
            else if (freqHz >= 6300) gainDb += dsp.loudnessGainDb * 0.45f
        }

        // Presence / Clarity / Exciter
        if (dsp.presenceEnabled && freqHz in 1250..4500) {
            gainDb += dsp.presenceLevel * 0.5f
        }
        if (dsp.clarityEnabled && freqHz in 5000..16000) {
            gainDb += dsp.clarityLevel * 0.5f
        }
        if (dsp.exciterEnabled && freqHz >= 8000) {
            gainDb += dsp.exciterLevel * 0.4f
        }

        // Crossover HPF / LPF attenuation
        if (dsp.hpfEnabled && freqHz < dsp.hpfFrequencyHz) {
            val octavesBelow = (log10(dsp.hpfFrequencyHz.toDouble() / freqHz.coerceAtLeast(15)) / log10(2.0)).toFloat()
            gainDb -= (octavesBelow * (dsp.hpfSlopeDb * 0.45f)).coerceIn(0f, 18f)
        }
        if (dsp.lpfEnabled && freqHz > dsp.lpfFrequencyHz) {
            val octavesAbove = (log10(freqHz.toDouble() / dsp.lpfFrequencyHz.coerceAtLeast(40f)) / log10(2.0)).toFloat()
            gainDb -= (octavesAbove * (dsp.lpfSlopeDb * 0.45f)).coerceIn(0f, 18f)
        }

        return gainDb.coerceIn(-15f, 15f)
    }

    fun applyDspSettings(dsp: DspSettings, eq: EqualizerSettings) {
        currentDspSettings = dsp
        currentEqualizerSettings = eq

        applyPlayerVolume()

        if (equalizerFx == null && dynamicsProcessingFx == null && dsp.dspMasterEnabled) {
            val sessionToAttach = if (activeAudioSessionId >= 0) activeAudioSessionId else externalAudioSessionId
            attachAudioFx(sessionToAttach, dsp, eq)
            return
        }

        // Update API 28+ DynamicsProcessing pipeline
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dynamicsProcessingFx?.let { dp ->
                try {
                    dp.enabled = dsp.dspMasterEnabled
                    val totalInputGain = if (dsp.dspMasterEnabled) (dsp.preampDb + dsp.masterGainDb).coerceIn(-30f, 20f) else 0f
                    dp.setInputGainAllChannelsTo(totalInputGain)

                    // 31-Band PostEQ
                    val freqs31 = EqualizerSettings.FREQUENCIES_HZ_31
                    val postEq = DynamicsProcessing.Eq(true, dsp.dspMasterEnabled, freqs31.size)
                    for (i in freqs31.indices) {
                        val f = freqs31[i].toFloat()
                        val cutoff = if (i < freqs31.size - 1) (f + freqs31[i + 1].toFloat()) / 2f else 22000f
                        val gain = computeEffectiveBandGainDb(freqs31[i], dsp, eq)
                        postEq.getBand(i).apply {
                            isEnabled = dsp.dspMasterEnabled
                            cutoffFrequency = cutoff
                            this.gain = gain
                        }
                    }
                    dp.setPostEqAllChannelsTo(postEq)

                    // Hardware Multiband Compressor (MBC)
                    val mbcBands = listOf(120f, 800f, 4000f, 20000f)
                    val mbc = DynamicsProcessing.Mbc(true, dsp.dspMasterEnabled && dsp.compressorEnabled, mbcBands.size)
                    for (i in mbcBands.indices) {
                        mbc.getBand(i).apply {
                            isEnabled = dsp.dspMasterEnabled && dsp.compressorEnabled
                            cutoffFrequency = mbcBands[i]
                            threshold = dsp.compressorThresholdDb.coerceIn(-48f, 0f)
                            ratio = dsp.compressorRatio.coerceIn(1f, 20f)
                            attackTime = dsp.compressorAttackMs.coerceIn(1f, 100f)
                            releaseTime = dsp.compressorReleaseMs.coerceIn(10f, 1000f)
                            kneeWidth = 6f
                            noiseGateThreshold = -90f
                            expanderRatio = 1f
                            preGain = 0f
                            postGain = if (dsp.compressorEnabled) 1.5f else 0f
                        }
                    }
                    dp.setMbcAllChannelsTo(mbc)

                    // Hardware Anti-Clip Limiter
                    val limiter = DynamicsProcessing.Limiter(
                        true,
                        dsp.dspMasterEnabled && dsp.limiterEnabled,
                        0,
                        1.0f,
                        60.0f,
                        10.0f,
                        dsp.limiterCeilingDb.coerceIn(-24f, 0f),
                        0f
                    )
                    dp.setLimiterAllChannelsTo(limiter)
                } catch (_: Throwable) {}
            }
        }

        // Standard Equalizer fallback
        if (!useDynamicsProcessingPostEq) {
            try {
                equalizerFx?.let { eqFx ->
                    val numBands = eqFx.numberOfBands
                    val (minLevel, maxLevel) = eqFx.bandLevelRange
                    for (b in 0 until numBands) {
                        val centerFreqHz = eqFx.getCenterFreq(b.toShort()) / 1000
                        val targetGainDb = computeEffectiveBandGainDb(centerFreqHz, dsp, eq)
                        val millibels = (targetGainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                        eqFx.setBandLevel(b.toShort(), millibels.toShort())
                    }
                    eqFx.enabled = dsp.dspMasterEnabled
                }
            } catch (_: Throwable) {}

            try {
                loudnessEnhancerFx?.let { le ->
                    val gainMb = ((dsp.preampDb + dsp.masterGainDb) * 100).toInt().coerceIn(-2000, 3000)
                    le.setTargetGain(gainMb)
                    le.enabled = dsp.dspMasterEnabled && (dsp.preampDb != 0f || dsp.masterGainDb != 0f)
                }
            } catch (_: Throwable) {}
        }

        // BassBoost
        try {
            bassBoostFx?.let { bb ->
                if (bb.strengthSupported) {
                    val strength = ((dsp.bassBoostDb / 24.0f) * 1000f).toInt().coerceIn(0, 1000)
                    bb.setStrength(strength.toShort())
                }
                bb.enabled = dsp.dspMasterEnabled && dsp.bassBoostEnabled && dsp.bassBoostDb > 0.2f
            }
        } catch (_: Throwable) {}

        // Virtualizer (Stereo Spatializer)
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

    private fun releaseAudioFx() {
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

    private fun processPlaybackWaveform(waveform: ByteArray) {
        sourceMode = AudioSourceMode.INTERNAL_DSP
        val n = waveform.size
        if (n == 0) return

        var sumSq = 0.0
        val osc = FloatArray(128)
        val step = (n / 128).coerceAtLeast(1)

        for (i in 0 until 128) {
            val raw = waveform[(i * step).coerceAtMost(n - 1)].toInt() and 0xFF
            val normalized = (raw - 128).toFloat() / 128f
            osc[i] = normalized
            sumSq += (normalized * normalized)
        }
        rawOscilloscopePcm = osc
        currentWaveform = FloatArray(64) { idx -> osc[(idx * 2).coerceAtMost(127)] }

        val rms = sqrt(sumSq / 128.0).toFloat()
        val calculatedDb = if (rms > 0.001f) 20 * log10(rms) else -60f
        rmsLevelDb = calculatedDb.coerceIn(-60f, 0f)

        val spl = (88f + (rmsLevelDb + 45f) * 1.1f + splCalibrationOffsetDb).coerceIn(40f, 138f)
        currentSplDb = spl
        peakSplDb = if (currentSplDb > peakSplDb) currentSplDb else (peakSplDb * 0.97f).coerceAtLeast(currentSplDb)

        val level = (rms * 1.8f).coerceIn(0f, 1f)
        val pan = currentDspSettings.balancePan.coerceIn(-1f, 1f)
        leftVuLevel = (if (pan <= 0f) level else level * (1f - pan * 0.6f)).coerceIn(0f, 1f)
        rightVuLevel = (if (pan >= 0f) level * 0.98f else level * (1f + pan * 0.6f)).coerceIn(0f, 1f)
        isClippingDetected = (rms > 0.94f)
        if (isClippingDetected) clipEventsCount++
    }

    private fun processPlaybackFft(fft: ByteArray) {
        if (fft.isEmpty()) return
        val numMagnitudes = fft.size / 2
        val magnitudes = FloatArray(numMagnitudes)

        magnitudes[0] = abs(fft[0].toFloat())
        for (k in 1 until numMagnitudes) {
            val r = fft[2 * k].toFloat()
            val im = fft[2 * k + 1].toFloat()
            magnitudes[k] = sqrt(r * r + im * im)
        }

        currentRtaBands = currentRtaBands.map { band ->
            val binIdx = ((band.freqHz.toFloat() / (sampleRate / 2f)) * numMagnitudes).toInt().coerceIn(1, numMagnitudes - 1)
            val mag = magnitudes[binIdx]
            val db = if (mag > 0.01f) (20 * log10(mag / 128f)).coerceIn(-60f, 0f) else -60f
            val peak = if (db > band.peakDb) db else (band.peakDb - 1.2f).coerceAtLeast(-60f)
            band.copy(levelDb = db, peakDb = peak)
        }
    }

    // ==========================================
    // Playback Operations
    // ==========================================
    fun playTrack(track: AudioTrackItem, dspSettings: DspSettings, eqSettings: EqualizerSettings) {
        stopPlayback()
        currentTrack = track
        playbackPositionSeconds = 0

        if (track.customUri != null) {
            playCustomUri(track.customUri, dspSettings, eqSettings)
        }
    }

    fun togglePlayPause(dspSettings: DspSettings, eqSettings: EqualizerSettings) {
        if (isPlaying) {
            pausePlayback()
        } else {
            resumePlayback(dspSettings, eqSettings)
        }
    }

    fun pausePlayback() {
        isPlaying = false
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
    }

    fun resumePlayback(dspSettings: DspSettings, eqSettings: EqualizerSettings) {
        val track = currentTrack ?: return
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                isPlaying = true
                applyPlayerVolume()
                applyDspSettings(dspSettings, eqSettings)
                startPlaybackTicker()
            } catch (_: Exception) {
                if (track.customUri != null) {
                    playCustomUri(track.customUri, dspSettings, eqSettings)
                }
            }
        } else if (track.customUri != null) {
            playCustomUri(track.customUri, dspSettings, eqSettings)
        }
    }

    fun stopPlayback() {
        isPlaying = false
        tickerJob?.cancel()
        tickerJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        releaseAudioFx()
    }

    fun seekTo(seconds: Int) {
        playbackPositionSeconds = seconds.coerceIn(0, currentTrack?.durationSeconds ?: 0)
        try {
            mediaPlayer?.seekTo(seconds * 1000)
        } catch (_: Exception) {}
    }

    private fun playCustomUri(uri: Uri, dsp: DspSettings, eq: EqualizerSettings) {
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                setOnCompletionListener {
                    this@CarAudioEngine.isPlaying = false
                    this@CarAudioEngine.playbackPositionSeconds = 0
                    onTrackCompletedListener?.invoke()
                }
            }
            mediaPlayer = mp
            isPlaying = true
            attachAudioFx(mp.audioSessionId, dsp, eq)
            applyPlayerVolume()
            mp.start()
            startPlaybackTicker()
        } catch (_: Throwable) {
            stopPlayback()
            // Re-attach global session so YouTube/system audio still stays equalized
            attachToGlobalSystemSession(dsp, eq)
        }
    }

    private fun startPlaybackTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && isPlaying) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            playbackPositionSeconds = mp.currentPosition / 1000
                        }
                    }
                } catch (_: Exception) {}
                delay(250)
            }
        }
    }

    // Intro / Boot Sound
    fun playIntroBootSound() {
        scope.launch {
            try {
                val totalSamples = (sampleRate * 1.4).toInt()
                val pcmBuffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = sin(PI * (t / 1.4).coerceIn(0.0, 1.0))
                    val sweepFreq = 65.0 + 420.0 * (t / 1.4)
                    val sample = (sin(2.0 * PI * sweepFreq * t) * 0.45 + sin(2.0 * PI * 55.0 * t) * 0.45) * env
                    pcmBuffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.65).toInt().toShort()
                }

                val bootTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                bootTrack.write(pcmBuffer, 0, pcmBuffer.size)
                bootTrack.play()
                delay(1500)
                try {
                    bootTrack.stop()
                    bootTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    // Microphone RTA (Optional Real Acoustic Mic)
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
                    if (read > 0) {
                        processMicPcm(buffer, read)
                    }
                    delay(40)
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
        sourceMode = AudioSourceMode.REAL_MIC
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

        val osc = FloatArray(128)
        for (i in 0 until 128) {
            osc[i] = buffer[i.coerceAtMost(readSize - 1)].toFloat() / Short.MAX_VALUE
        }
        rawOscilloscopePcm = osc
        currentWaveform = FloatArray(64) { idx -> osc[(idx * 2).coerceAtMost(127)] }

        val step = (readSize / 128).coerceAtLeast(1)
        val numSamples = (readSize / step).coerceAtLeast(1)

        currentRtaBands = currentRtaBands.map { band ->
            val f = band.freqHz.toDouble()
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
            val newPeak = if (bandDb > band.peakDb) bandDb else (band.peakDb - 1.2f).coerceAtLeast(-60f)
            band.copy(levelDb = bandDb, peakDb = newPeak)
        }
    }

    fun release() {
        stopPlayback()
        stopMicRta()
        releaseAudioFx()
    }
}
