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
