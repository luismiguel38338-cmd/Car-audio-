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
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.net.Uri
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.RtaBand
import com.example.model.SplWeighting
import com.example.model.ToneMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class CarAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Synthesis AudioTrack
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private var isSynthPlaying = false

    // Media Player for user audio files
    private var mediaPlayer: MediaPlayer? = null

    // Real Android AudioFx pipeline
    private var equalizerFx: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var virtualizerFx: Virtualizer? = null
    private var loudnessEnhancerFx: LoudnessEnhancer? = null
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

    // Tone Generator state
    var isToneActive: Boolean = false
        private set
    var currentToneFreq: Float = 40f
        private set
    var currentToneMode: ToneMode = ToneMode.SINE_WAVE
        private set

    // Playback state
    var isPlaying = false
        private set
    var playbackPositionSeconds = 0
        private set
    var currentTrack: AudioTrackItem? = null
        private set

    // Master volume factor
    var volumeFactor: Float = 0.85f

    private val sampleRate = 44100

    companion object {
        fun getBuiltInTracks(): List<AudioTrackItem> {
            // Clean by default: no demo/mock test tracks pre-populated
            return emptyList()
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

    // ==========================================
    // Real AudioFX Setup & Routing
    // ==========================================
    private fun attachAudioFx(sessionId: Int, dsp: DspSettings, eq: EqualizerSettings) {
        if (sessionId <= 0) return
        activeAudioSessionId = sessionId
        releaseAudioFx()

        try {
            // 1. Equalizer Effect
            equalizerFx = Equalizer(0, sessionId).apply {
                val numBands = numberOfBands
                val (minLevel, maxLevel) = bandLevelRange
                for (b in 0 until numBands) {
                    val centerFreqHz = getCenterFreq(b.toShort()) / 1000
                    // Find closest matching frequency in 31 bands or 15 bands
                    val targetGainDb = findGainForFreq(centerFreqHz, eq)
                    val millibels = (targetGainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                    setBandLevel(b.toShort(), millibels.toShort())
                }
                enabled = dsp.dspMasterEnabled && eq.isEnabled
            }
        } catch (_: Exception) {}

        try {
            // 2. Bass Boost Effect
            bassBoostFx = BassBoost(0, sessionId).apply {
                if (strengthSupported) {
                    val strength = ((dsp.bassBoostDb / 18.0f) * 1000f).toInt().coerceIn(0, 1000)
                    setStrength(strength.toShort())
                }
                enabled = dsp.dspMasterEnabled && dsp.bassBoostEnabled
            }
        } catch (_: Exception) {}

        try {
            // 3. Virtualizer (Stereo Width / Spatializer)
            virtualizerFx = Virtualizer(0, sessionId).apply {
                if (strengthSupported) {
                    val strength = (((dsp.stereoWidthPercent - 100f) / 100f) * 1000f).toInt().coerceIn(0, 1000)
                    setStrength(strength.toShort())
                }
                enabled = dsp.dspMasterEnabled && dsp.stereoWidthPercent > 105f
            }
        } catch (_: Exception) {}

        try {
            // 4. Loudness Enhancer (Preamp & Limiter Gain)
            loudnessEnhancerFx = LoudnessEnhancer(sessionId).apply {
                val gainMb = ((dsp.preampDb + dsp.masterGainDb) * 100).toInt().coerceIn(-1000, 2000)
                setTargetGain(gainMb)
                enabled = dsp.dspMasterEnabled && (dsp.preampDb != 0f || dsp.masterGainDb != 0f)
            }
        } catch (_: Exception) {}

        try {
            // 5. Visualizer (Real-time FFT and Waveform from audio session)
            visualizerFx = Visualizer(sessionId).apply {
                val range = Visualizer.getCaptureSizeRange()
                captureSize = range[1] // Maximum resolution
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
        } catch (_: Exception) {}
    }

    private fun findGainForFreq(freqHz: Int, eq: EqualizerSettings): Float {
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
        return eq.bands31.getOrElse(closestIdx) { 0.0f }
    }

    fun applyDspSettings(dsp: DspSettings, eq: EqualizerSettings) {
        // Immediate live parameter update
        try {
            equalizerFx?.let { eqFx ->
                val numBands = eqFx.numberOfBands
                val (minLevel, maxLevel) = eqFx.bandLevelRange
                for (b in 0 until numBands) {
                    val centerFreqHz = eqFx.getCenterFreq(b.toShort()) / 1000
                    val targetGainDb = findGainForFreq(centerFreqHz, eq)
                    val millibels = (targetGainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                    eqFx.setBandLevel(b.toShort(), millibels.toShort())
                }
                // DSP ON/OFF directly bypasses or enables EQ!
                eqFx.enabled = dsp.dspMasterEnabled && eq.isEnabled
            }
        } catch (_: Exception) {}

        try {
            bassBoostFx?.let { bb ->
                if (bb.strengthSupported) {
                    val strength = ((dsp.bassBoostDb / 24.0f) * 1000f).toInt().coerceIn(0, 1000)
                    bb.setStrength(strength.toShort())
                }
                bb.enabled = dsp.dspMasterEnabled && dsp.bassBoostEnabled
            }
        } catch (_: Exception) {}

        try {
            virtualizerFx?.let { virt ->
                if (virt.strengthSupported) {
                    val strength = (((dsp.stereoWidthPercent - 100f) / 150f) * 1000f).toInt().coerceIn(0, 1000)
                    virt.setStrength(strength.toShort())
                }
                virt.enabled = dsp.dspMasterEnabled && dsp.stereoWidthPercent > 105f
            }
        } catch (_: Exception) {}

        try {
            loudnessEnhancerFx?.let { le ->
                val gainMb = ((dsp.preampDb + dsp.masterGainDb) * 100).toInt().coerceIn(-2000, 3000)
                le.setTargetGain(gainMb)
                le.enabled = dsp.dspMasterEnabled && (dsp.preampDb != 0f || dsp.masterGainDb != 0f)
            }
        } catch (_: Exception) {}
    }

    private fun releaseAudioFx() {
        try {
            visualizerFx?.enabled = false
            visualizerFx?.release()
        } catch (_: Exception) {}
        visualizerFx = null

        try {
            equalizerFx?.enabled = false
            equalizerFx?.release()
        } catch (_: Exception) {}
        equalizerFx = null

        try {
            bassBoostFx?.enabled = false
            bassBoostFx?.release()
        } catch (_: Exception) {}
        bassBoostFx = null

        try {
            virtualizerFx?.enabled = false
            virtualizerFx?.release()
        } catch (_: Exception) {}
        virtualizerFx = null

        try {
            loudnessEnhancerFx?.enabled = false
            loudnessEnhancerFx?.release()
        } catch (_: Exception) {}
        loudnessEnhancerFx = null
    }

    // Process real PCM waveform from Android Audio Visualizer
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
        if (currentSplDb > peakSplDb) {
            peakSplDb = currentSplDb
        } else {
            peakSplDb = (peakSplDb * 0.97f).coerceAtLeast(currentSplDb)
        }

        val level = (rms * 1.8f).coerceIn(0f, 1f)
        leftVuLevel = level
        rightVuLevel = level * 0.96f
        isClippingDetected = (rms > 0.95f)
        if (isClippingDetected) clipEventsCount++
    }

    // Process real FFT from Android Audio Visualizer
    private fun processPlaybackFft(fft: ByteArray) {
        if (fft.isEmpty()) return
        val numMagnitudes = fft.size / 2
        val magnitudes = FloatArray(numMagnitudes)

        magnitudes[0] = abs(fft[0].toFloat()) // DC
        for (k in 1 until numMagnitudes) {
            val r = fft[2 * k].toFloat()
            val im = fft[2 * k + 1].toFloat()
            magnitudes[k] = sqrt(r * r + im * im)
        }

        // Map FFT bins to 31 ISO bands
        val updated = currentRtaBands.map { band ->
            val binIdx = ((band.freqHz.toFloat() / (sampleRate / 2f)) * numMagnitudes).toInt().coerceIn(1, numMagnitudes - 1)
            val mag = magnitudes[binIdx]
            val db = if (mag > 0.01f) (20 * log10(mag / 128f)).coerceIn(-60f, 0f) else -60f
            val peak = if (db > band.peakDb) db else (band.peakDb - 1.2f).coerceAtLeast(-60f)
            band.copy(levelDb = db, peakDb = peak)
        }
        currentRtaBands = updated
    }

    // ==========================================
    // Playback Operations
    // ==========================================
    fun playTrack(track: AudioTrackItem, dspSettings: DspSettings, eqSettings: EqualizerSettings) {
        stopPlayback()
        currentTrack = track
        isPlaying = true
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
        isSynthPlaying = false
        synthJob?.cancel()
        audioTrack?.pause()
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
    }

    fun resumePlayback(dspSettings: DspSettings, eqSettings: EqualizerSettings) {
        isPlaying = true
        if (currentTrack?.customUri != null && mediaPlayer != null) {
            mediaPlayer?.start()
            applyDspSettings(dspSettings, eqSettings)
        }
    }

    fun stopPlayback() {
        isPlaying = false
        isSynthPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null

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
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                attachAudioFx(audioSessionId, dsp, eq)
                start()
                setOnCompletionListener {
                    this@CarAudioEngine.isPlaying = false
                    this@CarAudioEngine.playbackPositionSeconds = 0
                }
            }
            startPlaybackTicker()
        } catch (e: Exception) {
            stopPlayback()
        }
    }

    private fun startPlaybackTicker() {
        scope.launch {
            while (isActive && isPlaying) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        playbackPositionSeconds = mp.currentPosition / 1000
                    }
                }
                delay(250)
            }
        }
    }

    // Tone Generator
    fun playTone(mode: ToneMode, freqHz: Float, dsp: DspSettings, eq: EqualizerSettings) {
        stopPlayback()
        stopTone()

        isToneActive = true
        currentToneFreq = freqHz.coerceIn(10f, 20000f)
        currentToneMode = mode

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.let { track ->
            attachAudioFx(track.audioSessionId, dsp, eq)
            track.play()
        }

        synthJob = scope.launch {
            val chunk = ShortArray(1024)
            var phase = 0.0

            while (isActive && isToneActive) {
                val effectiveFreq = currentToneFreq.toDouble()
                val masterVol = (volumeFactor * (1.0 + dsp.masterGainDb / 20.0)).coerceIn(0.1, 1.0)

                for (i in chunk.indices) {
                    val sample = sin(phase)
                    phase += 2 * PI * effectiveFreq / sampleRate
                    if (phase > 2 * PI) phase -= 2 * PI
                    chunk[i] = (sample * masterVol * 0.75 * Short.MAX_VALUE).toInt().toShort()
                }

                audioTrack?.write(chunk, 0, chunk.size)
            }
        }
    }

    fun stopTone() {
        isToneActive = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        releaseAudioFx()
    }

    // Intro / Boot Sound
    fun playIntroBootSound() {
        scope.launch {
            try {
                val totalSamples = (sampleRate * 2.0).toInt()
                val pcmBuffer = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample: Double = when {
                        t < 0.15 -> {
                            val click1 = if (t in 0.02..0.038) sin(2.0 * PI * 2200.0 * t) * (1.0 - (t - 0.02) / 0.018) else 0.0
                            val click2 = if (t in 0.07..0.098) sin(2.0 * PI * 1800.0 * t) * (1.0 - (t - 0.07) / 0.028) else 0.0
                            (click1 * 0.7 + click2 * 0.8)
                        }
                        t in 0.15..0.85 -> {
                            val progress = (t - 0.15) / 0.70
                            val freq = 523.0 + 523.0 * progress
                            val env = sin(PI * progress)
                            (sin(2.0 * PI * freq * t) * 0.45 + sin(2.0 * PI * freq * 1.5 * t) * 0.25) * env
                        }
                        else -> {
                            val subT = (t - 0.85) / 1.15
                            val subFreq = 74.0 - 38.0 * subT.coerceIn(0.0, 1.0)
                            val subEnv = (1.0 - subT).coerceIn(0.0, 1.0) * subT.coerceAtMost(0.12) * 8.33
                            (sin(2.0 * PI * subFreq * t) * 0.85 + sin(2.0 * PI * (subFreq * 2.0) * t) * 0.25) * subEnv
                        }
                    }
                    pcmBuffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.85).toInt().toShort()
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
                delay(2200)
                try {
                    bootTrack.stop()
                    bootTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    // Microphone RTA
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

        val weightingDelta = when (splWeighting) {
            SplWeighting.A_WEIGHTING -> if (rms > 1) -2.8f else 0f
            SplWeighting.C_WEIGHTING -> 0.0f
            SplWeighting.Z_WEIGHTING -> +0.7f
            else -> 0.0f
        }

        val calculatedSpl = (94.0f + db.toFloat() + 35.0f + splCalibrationOffsetDb + weightingDelta).coerceIn(35f, 145f)
        currentSplDb = calculatedSpl
        if (currentSplDb > peakSplDb) {
            peakSplDb = currentSplDb
        } else {
            peakSplDb = (peakSplDb * 0.96f).coerceAtLeast(currentSplDb)
        }

        val normLevel = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
        leftVuLevel = normLevel
        rightVuLevel = normLevel * 0.95f

        val osc = FloatArray(128)
        var triggerOffset = 0
        for (i in 0 until (readSize - 128).coerceAtLeast(1)) {
            if (buffer[i] <= 0 && buffer[i + 1] > 0) {
                triggerOffset = i
                break
            }
        }
        for (i in 0 until 128) {
            val idx = (triggerOffset + i).coerceAtMost(readSize - 1)
            osc[i] = buffer[idx].toFloat() / Short.MAX_VALUE
        }
        rawOscilloscopePcm = osc
        currentWaveform = FloatArray(64) { idx -> osc[(idx * 2).coerceAtMost(127)] }

        val step = (readSize / 128).coerceAtLeast(1)
        val numSamples = readSize / step

        val updated = currentRtaBands.map { band ->
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
        currentRtaBands = updated
    }

    fun playAcousticPing() {
        scope.launch(Dispatchers.Default) {
            try {
                val pingBufferSize = 2048
                val pingChunk = ShortArray(pingBufferSize)
                for (i in pingChunk.indices) {
                    val envelope = exp(-i.toDouble() / (sampleRate * 0.015))
                    val tone = sin(2.0 * PI * 1000.0 * i / sampleRate)
                    pingChunk[i] = (tone * envelope * 0.8 * Short.MAX_VALUE).toInt().toShort()
                }
                val pingTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pingBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                pingTrack.write(pingChunk, 0, pingChunk.size)
                pingTrack.play()
                delay(100)
                pingTrack.stop()
                pingTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopPlayback()
        stopTone()
        stopMicRta()
        releaseAudioFx()
    }
}
