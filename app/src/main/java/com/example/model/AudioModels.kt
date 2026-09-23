package com.example.model

import android.net.Uri

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String = "Desconocido",
    val album: String = "Archivo Local",
    val durationSeconds: Int = 0,
    val category: TrackCategory = TrackCategory.USER_CUSTOM,
    val frequencyDescription: String = "Audio del Usuario",
    val isSynthesized: Boolean = false,
    val customUri: Uri? = null,
    val albumArtUri: Uri? = null
)

enum class TrackCategory {
    SUB_BASS_TEST,
    OPEN_SHOW,
    SPL_COMPETITION,
    SQL_AUDIOPHILE,
    CALIBRATION_PINK_NOISE,
    USER_CUSTOM
}

data class DspSettings(
    // Master DSP State
    val dspMasterEnabled: Boolean = true,
    val inputGainDb: Float = 0.0f, // -12 to +12 dB
    val outputGainDb: Float = 0.0f, // -12 to +12 dB
    val preampDb: Float = 0.0f, // -12 to +12 dB
    val bassGainDb: Float = 2.0f, // -12 to +12 dB
    val midGainDb: Float = 0.0f, // -12 to +12 dB
    val trebleGainDb: Float = 1.5f, // -12 to +12 dB
    val headroomDb: Float = 6.0f,
    val sampleRate: Int = 48000,

    // Crossover Filters
    val hpfFrequencyHz: Float = 25f,
    val hpfSlopeDb: Int = 24, // 6, 12, 18, 24, 36, 48 dB/oct
    val hpfEnabled: Boolean = true,
    val hpfQ: Float = 0.707f,

    val lpfFrequencyHz: Float = 18000f,
    val lpfSlopeDb: Int = 24, // 6, 12, 18, 24, 36, 48 dB/oct
    val lpfEnabled: Boolean = true,
    val lpfQ: Float = 0.707f,

    val bandPassEnabled: Boolean = false,
    val bandPassCenterHz: Float = 1000f,
    val bandPassWidthHz: Float = 800f,
    val bandPassSlopeDb: Int = 12,
    val bandPassQ: Float = 1.0f,

    val subsonicFrequencyHz: Float = 28f,
    val subsonicEnabled: Boolean = true,

    // Audio FX Controls
    val bassBoostEnabled: Boolean = true,
    val bassBoostDb: Float = 4.0f, // 0 to 18 dB
    val bassBoostFreqHz: Float = 45f, // 35Hz, 45Hz, 55Hz

    val subBassEnabled: Boolean = false,
    val subBassBoostDb: Float = 3.0f,
    val subBassFreqHz: Float = 35f,

    val loudnessEnabled: Boolean = false,
    val loudnessGainDb: Float = 4.0f,

    val stereoWidthPercent: Float = 100f, // 0 to 200%
    val balancePan: Float = 0.0f, // -1.0 (Left) to +1.0 (Right)

    val compressorEnabled: Boolean = false,
    val compressorThresholdDb: Float = -12f,
    val compressorRatio: Float = 4.0f,
    val compressorAttackMs: Float = 10f,
    val compressorReleaseMs: Float = 100f,

    val limiterEnabled: Boolean = true,
    val limiterCeilingDb: Float = -0.5f,
    val clippingThresholdPercent: Float = 92f,

    val exciterEnabled: Boolean = false,
    val exciterLevel: Float = 3.0f,

    val presenceEnabled: Boolean = false,
    val presenceLevel: Float = 2.0f,

    val clarityEnabled: Boolean = false,
    val clarityLevel: Float = 2.5f,

    val phaseDegrees: Int = 0, // 0 or 180
    val timeAlignmentMs: Float = 1.2f, // 0 to 15 ms
    val masterGainDb: Float = 0f, // -24 to +6 dB

    // 8-band EQ (backward compatibility)
    val eqBands: List<Float> = listOf(4.0f, 2.0f, 0.0f, -1.0f, 1.0f, 3.0f, 4.5f, 5.0f),
    val activePresetName: String = "Flat"
)

data class AmpTelemetry(
    val voltage: Float? = null, // Null indicates N/A (no real OBD/CAN sensor)
    val isVoltageAvailable: Boolean = false,
    val temperatureC: Float? = null,
    val outputPowerWatts: Int = 0,
    val maxRatedWatts: Int = 3000,
    val impedanceOhms: Float = 1.0f,
    val isClipping: Boolean = false,
    val clipCount: Int = 0,
    val isOverheated: Boolean = false,
    val isLowVoltage: Boolean = false,
    val protectionModeActive: Boolean = false,
    val isPlantaConnected: Boolean = false
)

data class RtaBand(
    val label: String,
    val freqHz: Int,
    val levelDb: Float, // -60 to 0 dB
    val peakDb: Float
)

// Multichannel 4-Way Crossover Routing Model
enum class CrossoverFilterType(val label: String) {
    BUTTERWORTH("Butterworth"),
    LINKWITZ_RILEY("Linkwitz-Riley")
}

data class DspChannel(
    val id: Int,
    val name: String,
    val typeName: String,
    val hpfHz: Float,
    val hpfSlopeDb: Int, // 12, 24, 48 dB/oct
    val hpfEnabled: Boolean,
    val hpfFilterType: CrossoverFilterType = CrossoverFilterType.BUTTERWORTH,
    val lpfHz: Float,
    val lpfSlopeDb: Int,
    val lpfEnabled: Boolean,
    val lpfFilterType: CrossoverFilterType = CrossoverFilterType.BUTTERWORTH,
    val gainDb: Float, // -24 to +12 dB
    val phaseInverted: Boolean = false,
    val delayMs: Float = 0.0f, // 0 to 25 ms
    val delayCm: Float = delayMs * 34.3f, // Distance in cm = delayMs * 34.3
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    // Per-channel Limiter & Clipping
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -1.0f, // -24 to 0 dBFS
    val limiterAttackMs: Float = 5.0f,
    val limiterReleaseMs: Float = 50.0f,
    val gainReductionDb: Float = 0.0f,
    val isClipping: Boolean = false,
    val clipCount: Int = 0
)

enum class AudioSourceMode(val label: String, val isReal: Boolean) {
    REAL_MIC("Micrófono Real (Calibrado)", true),
    INTERNAL_DSP("DSP / Sintetizador Interno", true),
    DEMO_MODE("Modo Simulación Demo", false),
    SIMULATED_DEMO("Modo Simulación Demo", false)
}

data class OscilloscopeState(
    val isRunning: Boolean = true,
    val isFrozen: Boolean = false,
    val timebaseMs: Float = 2.0f, // 0.5ms to 10ms per div
    val triggerLevel: Float = 0.0f,
    val triggerAuto: Boolean = true,
    val isClippingDetected: Boolean = false,
    val peakVoltageEstimate: Float = 0.0f
)

enum class AutoTuneTarget(val label: String, val description: String) {
    HARMAN_CAR("Curva Harman Car Audio", "Realce de graves +6dB en subgraves, medios neutros y caída suave en agudos para máxima calidez."),
    FLAT_RTA("Acoustic Flat Lineal (0 dB)", "Respuesta perfectamente plana de 20Hz a 20kHz para calibración acústica de precisión."),
    PANCADAO_BASS("Pancadão Som Automotivo", "Pico agresivo en 63-80Hz, corte seco de sub y realce en 2k-4kHz para voces y cornetas."),
    SQ_AUDIOPHILE("Sound Quality (SQ Audiophile)", "Escenario acústico suave, graves articulados y extensión de armónicos para fidelidad de estudio.")
}

data class AutoTuneState(
    val isMeasuring: Boolean = false,
    val progress: Float = 0f,
    val targetCurve: AutoTuneTarget = AutoTuneTarget.HARMAN_CAR,
    val measuredCurve31: List<Float> = emptyList(),
    val targetCurve31: List<Float> = emptyList(),
    val proposedCorrection31: List<Float> = emptyList(),
    val hasProposal: Boolean = false,
    val explanation: String = ""
)

enum class SplWeighting(val label: String, val suffix: String) {
    A_WEIGHTING("Ponderación A (dBA)", "dBA"),
    C_WEIGHTING("Ponderación C (dBC - Car Audio)", "dBC"),
    Z_WEIGHTING("Ponderación Z (dBZ - Plana)", "dBZ")
}

enum class SplSpeed(val label: String) {
    FAST("Rápido (125 ms)"),
    SLOW("Lento (1000 ms)")
}

data class SplCalibrationSettings(
    val micOffsetDb: Float = 0.0f, // -20dB to +20dB
    val weighting: SplWeighting = SplWeighting.C_WEIGHTING,
    val speed: SplSpeed = SplSpeed.FAST,
    val isCalibratedMic: Boolean = false
)

enum class HardwareConnectionType(val label: String, val isRealHardware: Boolean) {
    DSP_INTERNAL("DSP Digital Interno 32-bit (Android)", false),
    USB_OTG("Hardware USB OTG (Serial CDC/FTDI)", true),
    BLUETOOTH_SPP("Hardware Bluetooth SPP / BLE", true),
    SIMULATOR_DEMO("Modo Demo / Simulación Offline", false)
}

data class HardwareBridgeState(
    val isConnected: Boolean = false,
    val connectionType: HardwareConnectionType = HardwareConnectionType.DSP_INTERNAL,
    val deviceName: String = "DSP Interno Nativo 32-bit",
    val statusMessage: String = "Procesamiento nativo en dispositivo",
    val packetsSent: Long = 0,
    val packetsReceived: Long = 0,
    val lastSyncTime: Long = 0L
)

data class DspFullProfileJson(
    val appVersion: String = "2.0",
    val profileName: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val channels: List<DspChannel>,
    val eq31Bands: List<Float>,
    val parametricBands: List<ParametricBand>,
    val masterGainDb: Float,
    val masterLimiterEnabled: Boolean,
    val toneGeneratorFreq: Float = 40f
)

enum class ToneMode(val label: String) {
    SINE_WAVE("Onda Senoidal Pura"),
    FREQUENCY_SWEEP("Barrido 20Hz-20kHz"),
    PINK_NOISE("Ruido Rosa Acústico"),
    WHITE_NOISE("Ruido Blanco")
}

data class SplRunState(
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Int = 30,
    val elapsedSeconds: Int = 30 - timeRemainingSeconds,
    val currentSplDb: Float = 0f,
    val averageSplDb: Float = 0f,
    val averageDb: Float = averageSplDb,
    val maxPeakDb: Float = 0f,
    val peakDb: Float = maxPeakDb,
    val runHistory: List<SplRecord> = emptyList(),
    val history: List<SplRecord> = runHistory
)

data class SplRecord(
    val id: Long,
    val peakDb: Float,
    val avgDb: Float,
    val title: String,
    val timeString: String
)

data class DspPreset(
    val name: String,
    val description: String,
    val dspSettings: DspSettings,
    val isUserCreated: Boolean = false
)

