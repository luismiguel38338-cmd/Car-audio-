package com.example.model

import android.net.Uri

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val category: TrackCategory,
    val frequencyDescription: String,
    val isSynthesized: Boolean = true,
    val customUri: Uri? = null
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
    val hpfFrequencyHz: Float = 20f,
    val hpfSlopeDb: Int = 24, // 12, 24, 48 dB/oct
    val hpfEnabled: Boolean = true,
    
    val lpfFrequencyHz: Float = 120f,
    val lpfSlopeDb: Int = 24,
    val lpfEnabled: Boolean = true,
    
    val subsonicFrequencyHz: Float = 28f,
    val subsonicEnabled: Boolean = true,
    
    val bassBoostDb: Float = 6f, // 0 to 18 dB
    val bassBoostFreqHz: Float = 45f, // 35Hz, 45Hz, 55Hz
    
    val phaseDegrees: Int = 0, // 0 or 180
    val timeAlignmentMs: Float = 1.2f, // 0 to 15 ms
    
    val masterGainDb: Float = 0f, // -24 to +6 dB
    val limiterEnabled: Boolean = true,
    val clippingThresholdPercent: Float = 92f,
    
    // 8-band EQ: 35Hz, 80Hz, 160Hz, 400Hz, 1kHz, 2.5kHz, 6.3kHz, 16kHz
    val eqBands: List<Float> = listOf(4.0f, 2.0f, 0.0f, -1.0f, 1.0f, 3.0f, 4.5f, 5.0f),
    val activePresetName: String = "Open Show Pro"
)

data class AmpTelemetry(
    val voltage: Float = 14.4f,
    val temperatureC: Float = 44f,
    val outputPowerWatts: Int = 1450,
    val maxRatedWatts: Int = 3000,
    val impedanceOhms: Float = 1.0f,
    val isClipping: Boolean = false,
    val clipCount: Int = 0,
    val isOverheated: Boolean = false,
    val isLowVoltage: Boolean = false,
    val protectionModeActive: Boolean = false,
    val isPlantaConnected: Boolean = true
)

data class RtaBand(
    val label: String,
    val freqHz: Int,
    val levelDb: Float, // -60 to 0 dB
    val peakDb: Float
)

// Multichannel 4-Way Crossover Routing Model
data class DspChannel(
    val id: Int,
    val name: String,
    val typeName: String,
    val hpfHz: Float,
    val hpfSlopeDb: Int, // 12, 24, 48 dB/oct
    val hpfEnabled: Boolean,
    val lpfHz: Float,
    val lpfSlopeDb: Int,
    val lpfEnabled: Boolean,
    val gainDb: Float, // -12 to +12 dB
    val phaseInverted: Boolean = false,
    val delayMs: Float = 0.0f, // 0 to 15 ms
    val isMuted: Boolean = false
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
    val currentSplDb: Float = 0f,
    val averageSplDb: Float = 0f,
    val maxPeakDb: Float = 0f,
    val runHistory: List<SplRecord> = emptyList()
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

// Car Audio Box Calculator Model
data class BoxCalculationResult(
    val grossVolumeLiters: Float,
    val grossVolumeCuFt: Float,
    val netVolumeLiters: Float,
    val netVolumeCuFt: Float,
    val portTuningHz: Float,
    val recommendedSubSizeInches: String,
    val description: String
)

// AWG Wire Gauge Calculator Model
data class WireCalculationResult(
    val maxAmps: Float,
    val recommendedAwg: String,
    val recommendedFuseAmps: Int,
    val voltageDropVolts: Float,
    val voltageDropPercent: Float,
    val isSafe: Boolean,
    val notes: String
)

// Subwoofer Wiring Model
data class SubwooferWiringResult(
    val numWoofers: Int,
    val coilType: String,
    val wiringMode: String,
    val finalImpedanceOhms: Float,
    val ampSafetyLevel: String, // "Estable 1Ω / 2Ω", "Cuidado 0.5Ω", etc.
    val diagramExplanation: String
)

