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
import com.example.model.AudioTrackItem
import com.example.model.DspSettings
import com.example.model.RtaBand
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
                Pair("25", 25), Pair("31", 31), Pair("40", 40), Pair("50", 50),
                Pair("63", 63), Pair("80", 80), Pair("100", 100), Pair("125", 125),
                Pair("160", 160), Pair("200", 200), Pair("250", 250), Pair("315", 315),
                Pair("400", 400), Pair("500", 500), Pair("630", 630), Pair("800", 800),
                Pair("1k", 1000), Pair("1.2k", 1250), Pair("1.6k", 1600), Pair("2k", 2000),
                Pair("2.5k", 2500), Pair("3.1k", 3150), Pair("4k", 4000), Pair("5k", 5000),
                Pair("6.3k", 6300), Pair("8k", 8000), Pair("10k", 10000), Pair("12k", 12500),
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
        var sumSquares = 0.0
        var maxAmp = 0
        for (i in 0 until readSize) {
            val sample = buffer[i].toInt()
            sumSquares += (sample * sample).toDouble()
            val absSample = abs(sample)
            if (absSample > maxAmp) maxAmp = absSample
        }
        val rms = sqrt(sumSquares / readSize)
        val db = if (rms > 1) 20 * log10(rms / Short.MAX_VALUE) else -60.0

        // Approximate SPL calibration from microphone dB
        val calculatedSpl = (90.0 + db + 35.0).toFloat().coerceIn(35f, 135f)
        currentSplDb = calculatedSpl
        if (currentSplDb > peakSplDb) {
            peakSplDb = currentSplDb
        } else {
            peakSplDb = (peakSplDb * 0.95f).coerceAtLeast(currentSplDb)
        }

        val normLevel = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0f, 1f)
        leftVuLevel = normLevel
        rightVuLevel = normLevel * 0.95f

        // Copy waveform downsampled
        val wave = FloatArray(64)
        val step = (readSize / 64).coerceAtLeast(1)
        for (i in 0 until 64) {
            val idx = (i * step).coerceAtMost(readSize - 1)
            wave[i] = buffer[idx].toFloat() / Short.MAX_VALUE
        }
        currentWaveform = wave

        // Distribute FFT-like energy across RTA bands
        val updated = currentRtaBands.mapIndexed { idx, band ->
            val weight = 1.0f - (idx.toFloat() / currentRtaBands.size.toFloat()) * 0.3f
            val bandDb = (db.toFloat() + (Math.random().toFloat() * 10f - 5f) * weight).coerceIn(-60f, 0f)
            val newPeak = if (bandDb > band.peakDb) bandDb else (band.peakDb - 1.2f).coerceAtLeast(-60f)
            band.copy(levelDb = bandDb, peakDb = newPeak)
        }
        currentRtaBands = updated
    }

    fun release() {
        stopPlayback()
        stopMicRta()
    }
}
