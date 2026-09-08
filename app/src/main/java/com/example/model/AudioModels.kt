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

enum class EnclosureType(val label: String) {
    VENTED_PORTED("Porteada / Ventilada (Bass Reflex)"),
    SEALED("Sellada (Acoustic Suspension)"),
    BANDPASS_4TH("Pasa-banda 4to Orden (4th Order)")
}

data class ThieleSmallParams(
    val fsHz: Float = 32f,
    val qts: Float = 0.38f,
    val vasLiters: Float = 65f,
    val xmaxMm: Float = 16f,
    val sdCm2: Float = 510f,
    val powerRmsWatts: Int = 1000,
    val fs: Float = fsHz,
    val vas: Float = vasLiters,
    val xmax: Float = xmaxMm,
    val sd: Float = sdCm2,
    val powerRms: Int = powerRmsWatts
)

data class ProfessionalBoxDesign(
    val enclosureType: EnclosureType = EnclosureType.VENTED_PORTED,
    val tsParams: ThieleSmallParams = ThieleSmallParams(),
    val netVolumeLiters: Float = 55f,
    val grossVolumeLiters: Float = 68f,
    val tuningFreqHz: Float = 36f,
    val f3CutoffHz: Float = 32f,
    // Port dimensions
    val isSlotPort: Boolean = true,
    val portWidthCm: Float = 5.0f,
    val portHeightCm: Float = 36.0f,
    val portLengthCm: Float = 42.0f,
    val airVelocityMps: Float = 12.4f, // Warning if > 17 m/s (chuffing)
    val isChuffingSafe: Boolean = true,
    // Outer box physical dimensions (MDF 18mm)
    val mdfThicknessMm: Int = 18,
    val boxWidthCm: Float = 65.0f,
    val boxHeightCm: Float = 40.0f,
    val boxDepthCm: Float = 45.0f,
    val cutListSummary: String = "Frente y Fondo: 65x40cm (x2) | Laterales: 41.4x36.4cm (x2) | Tapa y Base: 65x45cm (x2) | Ducto Baffle: 36.4x37cm"
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

