package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import com.example.model.AudioSourceMode
import com.example.model.AudioTrackItem
import com.example.model.DspSettings
import com.example.model.RtaBand
import com.example.model.SplWeighting
import com.example.model.ToneMode
import com.example.model.TrackCategory
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
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class CarAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Synthesis AudioTrack
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private var isSynthPlaying = false

    // Media Player for local files
    private var mediaPlayer: MediaPlayer? = null

    // Microphone RTA
    private var audioRecord: AudioRecord? = null
    private var micRecordJob: Job? = null
    var isMicRtaActive = false
        private set

    // Real-time audio levels
    var currentSplDb: Float = 68.0f
        private set
    var peakSplDb: Float = 72.0f
        private set
    var leftVuLevel: Float = 0.5f
        private set
    var rightVuLevel: Float = 0.5f
        private set
    var currentWaveform: FloatArray = FloatArray(64)
        private set
    var currentRtaBands: List<RtaBand> = createInitialRtaBands()
        private set

    // Real DSP 2.0 Telemetry & Oscilloscope
    var rmsLevelDb: Float = -24.0f
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

    // Paul Kellet Pink Noise filter states
    private var pinkB0 = 0.0
    private var pinkB1 = 0.0
    private var pinkB2 = 0.0
    private var pinkB3 = 0.0
    private var pinkB4 = 0.0
    private var pinkB5 = 0.0
    private var pinkB6 = 0.0

    private fun generatePinkNoiseSample(): Double {
        val white = Math.random() * 2.0 - 1.0
        pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
        pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
        pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
        pinkB3 = 0.86650 * pinkB3 + white * 0.3104856
        pinkB4 = 0.55000 * pinkB4 + white * 0.5329522
        pinkB5 = -0.7616 * pinkB5 - white * 0.0168980
        val pink = pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + pinkB6 + white * 0.5362
        pinkB6 = white * 0.115926
        return pink * 0.12
    }

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
    var currentTrack: AudioTrackItem = getBuiltInTracks().first()
        private set

    // Volume master
    var volumeFactor: Float = 0.85f

    private val sampleRate = 44100

    companion object {
        fun getBuiltInTracks(): List<AudioTrackItem> {
            return listOf(
                AudioTrackItem(
                    id = "track_sub_bass",
                    title = "Bass Test Sub-Sweep 40Hz - 20Hz",
                    artist = "Car Audio SPL Master",
                    durationSeconds = 120,
                    category = TrackCategory.SUB_BASS_TEST,
                    frequencyDescription = "Excursión profunda de subwoofer, prueba de resonancia de cajón"
                ),
                AudioTrackItem(
                    id = "track_open_show",
                    title = "Open Show Chuchero Test 800Hz - 8kHz",
                    artist = "Dominican & Latino Car Pro",
                    durationSeconds = 90,
                    category = TrackCategory.OPEN_SHOW,
                    frequencyDescription = "Claridad de drivers fenólicos, medios de 8'' y super-tweeters"
                ),
                AudioTrackItem(
                    id = "track_spl_heavy",
                    title = "SPL Competition Hard Kick 50Hz",
                    artist = "Basshead Heavy Power",
                    durationSeconds = 140,
                    category = TrackCategory.SPL_COMPETITION,
                    frequencyDescription = "Golpe seco y sub-grave continuo para medición de decibeles dB Drag"
                ),
                AudioTrackItem(
                    id = "track_sql_acoustic",
                    title = "Audiophile Acoustic SQL Stage",
                    artist = "Focal & Alpine Precision",
                    durationSeconds = 150,
                    category = TrackCategory.SQL_AUDIOPHILE,
                    frequencyDescription = "Alineación de tiempo, imagen estéreo y balance armónico puro"
                ),
                AudioTrackItem(
                    id = "track_pink_noise",
                    title = "Pink Noise RTA Calibrador",
                    artist = "Audio Precision Lab",
                    durationSeconds = 180,
                    category = TrackCategory.CALIBRATION_PINK_NOISE,
                    frequencyDescription = "Ruido rosa ecualizado -3dB/octava para análisis RTA acústico"
                )
            )
        }

        private fun createInitialRtaBands(): List<RtaBand> {
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
            return freqs.map { RtaBand(label = it.first, freqHz = it.second, levelDb = -45f, peakDb = -40f) }
        }
    }

    fun playTrack(track: AudioTrackItem, dspSettings: DspSettings) {
        stopPlayback()
        currentTrack = track
        isPlaying = true
        playbackPositionSeconds = 0

        if (track.customUri != null) {
            playCustomUri(track.customUri)
        } else {
            startSynthesisTrack(track, dspSettings)
        }
    }

    fun togglePlayPause(dspSettings: DspSettings) {
        if (isPlaying) {
            pausePlayback()
        } else {
            resumePlayback(dspSettings)
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

    fun resumePlayback(dspSettings: DspSettings) {
        isPlaying = true
        if (currentTrack.customUri != null && mediaPlayer != null) {
            mediaPlayer?.start()
        } else {
            startSynthesisTrack(currentTrack, dspSettings)
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
    }

    fun playTone(mode: ToneMode, freqHz: Float, dsp: DspSettings) {
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

        audioTrack?.play()

        synthJob = scope.launch {
            val chunk = ShortArray(1024)
            var phase = 0.0
            var sweepFreq = 20.0
            var sweepUp = true

            while (isActive && isToneActive) {
                val effectiveFreq = when (currentToneMode) {
                    ToneMode.SINE_WAVE -> currentToneFreq.toDouble()
                    ToneMode.FREQUENCY_SWEEP -> {
                        if (sweepUp) {
                            sweepFreq *= 1.015
                            if (sweepFreq >= 20000.0) {
                                sweepFreq = 20000.0
                                sweepUp = false
                            }
                        } else {
                            sweepFreq /= 1.015
                            if (sweepFreq <= 20.0) {
                                sweepFreq = 20.0
                                sweepUp = true
                            }
                        }
                        sweepFreq
                    }
                    ToneMode.PINK_NOISE, ToneMode.WHITE_NOISE -> 1000.0
                }

                val masterVol = (volumeFactor * (1.0 + dsp.masterGainDb / 20.0)).coerceIn(0.1, 1.0)

                for (i in chunk.indices) {
                    val sampleVal = when (currentToneMode) {
                        ToneMode.SINE_WAVE, ToneMode.FREQUENCY_SWEEP -> {
                            val sample = sin(phase)
                            phase += 2 * PI * effectiveFreq / sampleRate
                            if (phase > 2 * PI) phase -= 2 * PI
                            (sample * masterVol * 0.75 * Short.MAX_VALUE).toInt()
                        }
                        ToneMode.WHITE_NOISE -> {
                            val white = (Math.random() * 2.0 - 1.0)
                            (white * 0.3 * masterVol * Short.MAX_VALUE).toInt()
                        }
                        ToneMode.PINK_NOISE -> {
                            val pink = generatePinkNoiseSample()
                            (pink * 3.5 * masterVol * Short.MAX_VALUE).toInt()
                        }
                    }
                    chunk[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                audioTrack?.write(chunk, 0, chunk.size)

                // Update oscilloscope and visualizer from generated PCM
                if (!isMicRtaActive) {
                    sourceMode = AudioSourceMode.INTERNAL_DSP
                    val dbLevel = (-18f + (masterVol.toFloat() * 16f)).coerceIn(-45f, 0f)
                    rmsLevelDb = dbLevel
                    currentSplDb = (88f + masterVol.toFloat() * 25f + splCalibrationOffsetDb).coerceIn(40f, 138f)
                    if (currentSplDb > peakSplDb) peakSplDb = currentSplDb

                    leftVuLevel = (masterVol.toFloat() * 0.85f).coerceIn(0.1f, 1.0f)
                    rightVuLevel = leftVuLevel * 0.98f

                    // Detect clipping in generated chunk
                    var clipped = 0
                    for (s in chunk) {
                        if (abs(s.toInt()) >= 32600) clipped++
                    }
                    isClippingDetected = (clipped > 0)
                    if (isClippingDetected) clipEventsCount += clipped

                    // Update Oscilloscope buffer with trigger search
                    val osc = FloatArray(128)
                    var triggerIdx = 0
                    for (i in 0 until (chunk.size - 128).coerceAtLeast(1)) {
                        if (chunk[i] <= 0 && chunk[i + 1] > 0) {
                            triggerIdx = i
                            break
                        }
                    }
                    for (i in 0 until 128) {
                        val idx = (triggerIdx + i).coerceAtMost(chunk.size - 1)
                        osc[i] = chunk[idx].toFloat() / Short.MAX_VALUE
                    }
                    rawOscilloscopePcm = osc
                    currentWaveform = FloatArray(64) { idx -> osc[(idx * 2).coerceAtMost(127)] }

                    val updated = currentRtaBands.map { band ->
                        val diff = abs(log10(band.freqHz.toDouble()) - log10(effectiveFreq))
                        val closeness = (1.0 - diff * 3.0).coerceIn(0.0, 1.0).toFloat()
                        val bandLevel = when (currentToneMode) {
                            ToneMode.WHITE_NOISE -> (-18f + Math.random().toFloat() * 3.5f)
                            ToneMode.PINK_NOISE -> (-12f - (log10(band.freqHz.toFloat()) * 3f) + Math.random().toFloat() * 2.5f)
                            else -> (-55f + closeness * 48f)
                        }
                        val peak = if (bandLevel > band.peakDb) bandLevel else (band.peakDb - 1.5f).coerceAtLeast(-60f)
                        band.copy(levelDb = bandLevel, peakDb = peak)
                    }
                    currentRtaBands = updated
                }
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
    }

    fun playIntroBootSound() {
        scope.launch {
            try {
                val totalSamples = (sampleRate * 2.2).toInt()
                val pcmBuffer = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate

                    val sample: Double = when {
                        // 0.0s to 0.15s: Dual relay click transient
                        t < 0.15 -> {
                            val click1 = if (t in 0.02..0.038) sin(2.0 * PI * 2200.0 * t) * (1.0 - (t - 0.02) / 0.018) else 0.0
                            val click2 = if (t in 0.07..0.098) sin(2.0 * PI * 1800.0 * t) * (1.0 - (t - 0.07) / 0.028) else 0.0
                            (click1 * 0.7 + click2 * 0.8)
                        }
                        // 0.15s to 0.85s: High-tech DSP boot chime (sweeping harmonic chord)
                        t in 0.15..0.85 -> {
                            val progress = (t - 0.15) / 0.70
                            val freq = 523.0 + 523.0 * progress
                            val env = sin(PI * progress)
                            (sin(2.0 * PI * freq * t) * 0.45 + sin(2.0 * PI * freq * 1.5 * t) * 0.25) * env
                        }
                        // 0.85s to 2.2s: Deep Subwoofer bass excursion drop (75Hz gliding down to 36Hz)
                        else -> {
                            val subT = (t - 0.85) / 1.35
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

                delay(2400)
                try {
                    bootTrack.stop()
                    bootTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    fun seekTo(seconds: Int) {
        playbackPositionSeconds = seconds.coerceIn(0, currentTrack.durationSeconds)
        try {
            mediaPlayer?.seekTo(seconds * 1000)
        } catch (_: Exception) {}
    }

    private fun playCustomUri(uri: Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                start()
                setOnCompletionListener {
                    this@CarAudioEngine.isPlaying = false
                    this@CarAudioEngine.playbackPositionSeconds = 0
                }
            }
            startPlaybackTicker()
        } catch (e: Exception) {
            // Fallback to synthesis
            startSynthesisTrack(currentTrack, DspSettings())
        }
    }

    private fun startSynthesisTrack(track: AudioTrackItem, dsp: DspSettings) {
        isSynthPlaying = true
        isPlaying = true

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

        audioTrack?.play()

        synthJob = scope.launch {
            val chunk = ShortArray(1024)
            var phase = 0.0
            var tickCounter = 0

            while (isActive && isSynthPlaying) {
                val currentSeconds = playbackPositionSeconds

                // Synthesize tone based on track category & DSP settings
                val baseFreq = when (track.category) {
                    TrackCategory.SUB_BASS_TEST -> {
                        // Sweep from 45Hz down to 25Hz and back
                        35.0 + 15.0 * sin(currentSeconds * 0.5)
                    }
                    TrackCategory.OPEN_SHOW -> {
                        // Midrange punch & voice test harmonics
                        val f = if ((tickCounter / 20) % 2 == 0) 1200.0 else 2400.0
                        f
                    }
                    TrackCategory.SPL_COMPETITION -> {
                        // Hard 50Hz burst with sub-harmonics
                        50.0
                    }
                    TrackCategory.SQL_AUDIOPHILE -> {
                        // Musical rich harmonic chord (A4 440Hz + C# 554Hz + E 659Hz)
                        440.0
                    }
                    TrackCategory.CALIBRATION_PINK_NOISE -> {
                        0.0 // Handled specially as pink noise
                    }
                    else -> 60.0
                }

                // DSP adjustments
                val boostMultiplier = if (dsp.bassBoostDb > 0 && baseFreq <= 80.0) {
                    1.0 + (dsp.bassBoostDb / 12.0)
                } else {
                    1.0
                }

                val masterVol = (volumeFactor * (1.0 + dsp.masterGainDb / 20.0)).coerceIn(0.1, 1.0)

                for (i in chunk.indices) {
                    val sampleVal = if (track.category == TrackCategory.CALIBRATION_PINK_NOISE) {
                        // Fast pseudo-pink noise
                        val white = (Math.random() * 2.0 - 1.0)
                        (white * 0.3 * masterVol * Short.MAX_VALUE).toInt()
                    } else if (track.category == TrackCategory.SQL_AUDIOPHILE) {
                        val s1 = sin(phase)
                        val s2 = 0.5 * sin(phase * (554.0 / 440.0))
                        val s3 = 0.35 * sin(phase * (659.0 / 440.0))
                        phase += 2 * PI * baseFreq / sampleRate
                        if (phase > 2 * PI) phase -= 2 * PI
                        ((s1 + s2 + s3) * 0.4 * masterVol * Short.MAX_VALUE).toInt()
                    } else {
                        val sample = sin(phase)
                        phase += 2 * PI * baseFreq / sampleRate
                        if (phase > 2 * PI) phase -= 2 * PI
                        (sample * boostMultiplier * masterVol * 0.6 * Short.MAX_VALUE).toInt()
                    }
                    chunk[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                audioTrack?.write(chunk, 0, chunk.size)

                tickCounter++
                if (tickCounter % 43 == 0) { // ~ once per second
                    playbackPositionSeconds++
                    if (playbackPositionSeconds >= track.durationSeconds) {
                        playbackPositionSeconds = 0
                    }
                }

                // Update visualizer state based on current playback
                if (!isMicRtaActive) {
                    computeInternalVisualizer(track, baseFreq, boostMultiplier.toFloat())
                }
            }
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
                if (!isMicRtaActive) {
                    computeInternalVisualizer(currentTrack, 60.0, 1.0f)
                }
                delay(200)
            }
        }
    }

    private fun computeInternalVisualizer(track: AudioTrackItem, baseFreq: Double, boost: Float) {
        val rand = Math.random().toFloat()
        val energy = if (isPlaying) (0.6f + rand * 0.35f) * volumeFactor else 0.05f

        val rawDb = -45f + energy * 42f
        currentSplDb = (70f + energy * 45f + (boost * 3f)).coerceIn(40f, 138f)
        if (currentSplDb > peakSplDb) {
            peakSplDb = currentSplDb
        } else {
            peakSplDb = (peakSplDb * 0.98f).coerceAtLeast(currentSplDb)
        }

        leftVuLevel = (energy * 0.95f + rand * 0.05f).coerceIn(0f, 1f)
        rightVuLevel = (energy * 0.92f + (1f - rand) * 0.08f).coerceIn(0f, 1f)

        // Waveform samples
        val wave = FloatArray(64)
        for (i in wave.indices) {
            val t = i.toFloat() / 64f
            wave[i] = sin(t * 2 * PI.toFloat() * 3f + (System.currentTimeMillis() % 1000) / 150f) * energy
        }
        currentWaveform = wave

        // Update 30 RTA bands
        val updated = currentRtaBands.map { band ->
            val freqDist = abs(ln(band.freqHz.toDouble()) - ln(baseFreq.coerceAtLeast(20.0)))
            val bandFactor = (1.0 / (1.0 + freqDist * 1.5)).toFloat()
            val noise = (Math.random().toFloat() - 0.5f) * 6f
            val targetLevel = if (isPlaying) {
                (-55f + bandFactor * 50f * energy + noise).coerceIn(-60f, 0f)
            } else {
                -58f
            }
            val newPeak = if (targetLevel > band.peakDb) targetLevel else (band.peakDb - 0.8f).coerceAtLeast(-60f)
            band.copy(levelDb = targetLevel, peakDb = newPeak)
        }
        currentRtaBands = updated
    }

    // Microphone RTA real audio analyzer
    @SuppressLint("MissingPermission")
    fun startMicRta(hasRecordPermission: Boolean) {
        if (!hasRecordPermission) return
        if (isMicRtaActive) return

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
        } catch (e: Exception) {
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

        // Real clipping detection
        if (clippedSamples > 0) {
            isClippingDetected = true
            clipEventsCount += clippedSamples
        } else {
            isClippingDetected = false
        }

        // SPL Calibration with Weighting Adjustment:
        // C-Weighting has flat passband from 31.5Hz to 8kHz (standard for Car Audio & SPL)
        // A-Weighting severely attenuates sub-bass to mirror human ear sensitivity at low volumes
        // Z-Weighting is unweighted linear
        val weightingDelta = when (splWeighting) {
            SplWeighting.A_WEIGHTING -> if (rms > 1) -2.8f else 0f
            SplWeighting.C_WEIGHTING -> 0.0f
            SplWeighting.Z_WEIGHTING -> +0.7f
            else -> 0.0f
        }

        // Calibrated SPL calculation
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

        // Oscilloscope buffer extraction with zero-crossing rising-edge trigger
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

        // Real Discrete Fourier Transform (DFT) spectral binning across the 31 ISO bands
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
            // Combine microphone base noise floor with calibrated frequency response
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
                    val envelope = exp(-i.toDouble() / (sampleRate * 0.015)) // 15ms sharp acoustic impulse
                    val tone = sin(2.0 * PI * 1000.0 * i / sampleRate) // 1kHz sync tone
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
    }
}
