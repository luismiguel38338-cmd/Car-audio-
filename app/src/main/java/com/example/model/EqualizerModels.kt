package com.example.model

enum class ParametricFilterType(val label: String) {
    PEAKING("Campana (Peaking)"),
    LOW_SHELF("Low Shelf"),
    HIGH_SHELF("High Shelf"),
    NOTCH("Filtro Notch")
}

data class ParametricBand(
    val id: Int,
    val enabled: Boolean = true,
    val freqHz: Float,
    val gainDb: Float,
    val q: Float,
    val filterType: ParametricFilterType = ParametricFilterType.PEAKING
)

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val masterGainDb: Float = 0.0f, // -12 to +12 dB
    val isLimiterActive: Boolean = true,
    val clippingThreshold: Float = 95f,
    // 31 Standard ISO 1/3-Octave Graphic Bands (20Hz - 20kHz)
    val bands31: List<Float> = listOf(
        3.0f, 4.0f, 5.0f, 5.5f, 6.0f, 6.5f, 5.0f, 3.5f, 2.0f, 0.5f,
        -0.5f, -1.0f, -0.5f, 0.5f, 1.5f, 2.0f, 2.8f, 3.5f, 4.0f, 4.2f,
        3.8f, 3.0f, 2.5f, 2.0f, 2.8f, 3.5f, 4.5f, 5.0f, 4.5f, 4.0f, 3.0f
    ),
    // 15 Standard ISO Graphic Bands (retained for backward compatibility):
    val bands15: List<Float> = listOf(
        4.0f, 5.0f, 6.5f, 3.0f, 0.0f, -1.0f, 0.5f, 2.0f, 3.5f, 4.0f, 3.0f, 2.0f, 3.5f, 5.0f, 4.0f
    ),
    // Multi-band Parametric EQ (5 fully configurable bands)
    val parametricBands: List<ParametricBand> = listOf(
        ParametricBand(1, true, 45f, 4.0f, 1.8f, ParametricFilterType.LOW_SHELF),
        ParametricBand(2, true, 80f, 2.5f, 2.0f, ParametricFilterType.PEAKING),
        ParametricBand(3, true, 250f, -1.5f, 1.4f, ParametricFilterType.PEAKING),
        ParametricBand(4, true, 1200f, 3.0f, 1.2f, ParametricFilterType.PEAKING),
        ParametricBand(5, true, 8000f, 2.0f, 1.0f, ParametricFilterType.HIGH_SHELF)
    ),
    // Single parametric band fields (backward compatibility)
    val parametricFreqHz: Float = 65.0f,
    val parametricGainDb: Float = 4.0f,
    val parametricQ: Float = 1.4f,
    val activePresetName: String = "🇧🇷 Pancadão Som Automotivo"
) {
    companion object {
        val FREQUENCY_LABELS_31 = listOf(
            "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160",
            "200", "250", "315", "400", "500", "630", "800", "1k", "1.2k", "1.6k",
            "2k", "2.5k", "3.1k", "4k", "5k", "6.3k", "8k", "10k", "12.5k", "16k", "20k"
        )
        val FREQUENCIES_HZ_31 = listOf(
            20, 25, 31, 40, 50, 63, 80, 100, 125, 160,
            200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600,
            2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000
        )

        val FREQUENCY_LABELS = listOf(
            "25Hz", "40Hz", "63Hz", "100Hz", "160Hz",
            "250Hz", "400Hz", "630Hz", "1kHz", "1.6kHz",
            "2.5kHz", "4kHz", "6.3kHz", "10kHz", "16kHz"
        )
        val FREQUENCIES_HZ = listOf(
            25, 40, 63, 100, 160,
            250, 400, 630, 1000, 1600,
            2500, 4000, 6300, 10000, 16000
        )
    }
}

data class EqPresetItem(
    val name: String,
    val description: String,
    val bands: List<Float>,
    val bands31: List<Float> = emptyList(),
    val paramFreq: Float = 60f,
    val paramGain: Float = 0f,
    val paramQ: Float = 1.4f,
    val isCustom: Boolean = false
)

object EqPresetCatalog {
    val presets: List<EqPresetItem> = listOf(
        EqPresetItem(
            name = "Flat",
            description = "Respuesta plana y lineal 0 dB en todas las bandas.",
            bands = List(15) { 0.0f },
            bands31 = List(31) { 0.0f },
            paramFreq = 1000f,
            paramGain = 0f,
            paramQ = 1.0f
        ),
        EqPresetItem(
            name = "Bass",
            description = "Realce controlado de graves 40Hz-100Hz (+5dB) con medios equilibrados.",
            bands = listOf(4f, 5f, 4f, 2f, 0f, 0f, 0f, 1f, 1.5f, 2f, 2f, 2f, 2.5f, 3f, 2.5f),
            bands31 = listOf(
                2f, 3f, 4.5f, 5.5f, 5.0f, 4.0f, 3.0f, 2.0f, 1f, 0f,
                0f, 0f, 0f, 0f, 0.5f, 1f, 1.2f, 1.5f, 1.8f, 2f,
                2f, 2f, 2.2f, 2.5f, 2.8f, 3f, 3f, 3f, 2.5f, 2f, 1.5f
            ),
            paramFreq = 50f,
            paramGain = 4.0f,
            paramQ = 1.4f
        ),
        EqPresetItem(
            name = "Deep Bass",
            description = "Excursión profunda y pesada en sub-frecuencias 31.5Hz-50Hz (+8dB) para subwoofers.",
            bands = listOf(6f, 8f, 6.5f, 3f, 0f, -2f, -1f, 0f, 1f, 1f, 1.5f, 2f, 2.5f, 3f, 3f),
            bands31 = listOf(
                4f, 5.5f, 7f, 8.5f, 8f, 6.5f, 4.5f, 3f, 1f, 0f,
                -1f, -2f, -1.5f, -1f, 0f, 0.5f, 1f, 1f, 1.2f, 1.5f,
                1.8f, 2f, 2.2f, 2.5f, 2.8f, 3f, 3f, 3f, 3f, 2.5f, 2f
            ),
            paramFreq = 38f,
            paramGain = 7.0f,
            paramQ = 1.6f
        ),
        EqPresetItem(
            name = "SPL",
            description = "Configuración para competencia de presión acústica: pico de potencia en 50Hz-63Hz.",
            bands = listOf(3f, 6f, 9f, 4f, 1f, 0f, 0.5f, 1.5f, 2f, 3f, 2.5f, 2f, 2.5f, 3f, 2.5f),
            bands31 = listOf(
                1f, 3f, 5.5f, 7.5f, 9.5f, 8.0f, 5.5f, 3f, 1.5f, 0f,
                0f, 0f, 0.5f, 0.8f, 1.2f, 1.8f, 2.2f, 2.5f, 2.8f, 3f,
                2.8f, 2.5f, 2.5f, 2.8f, 3f, 3.2f, 3f, 2.8f, 2.5f, 2f, 1.5f
            ),
            paramFreq = 55f,
            paramGain = 8.5f,
            paramQ = 2.2f
        ),
        EqPresetItem(
            name = "Vocal",
            description = "Máxima claridad y presencia en voces humanas y cornetas (800Hz - 3.15kHz).",
            bands = listOf(-4f, -2f, 0f, 1f, 2f, 3f, 4.5f, 5.5f, 6f, 5.5f, 4.5f, 3.5f, 3f, 3f, 2f),
            bands31 = listOf(
                -6f, -5f, -4f, -3f, -1.5f, 0f, 0.5f, 1f, 1.5f, 2f,
                2.5f, 3f, 3.8f, 4.5f, 5.2f, 5.8f, 6.2f, 6.5f, 6.0f, 5.5f,
                5.0f, 4.5f, 4.0f, 3.5f, 3.5f, 3.2f, 3.0f, 2.8f, 2.5f, 2.0f, 1.5f
            ),
            paramFreq = 1200f,
            paramGain = 5.0f,
            paramQ = 1.2f
        ),
        EqPresetItem(
            name = "Latin",
            description = "Pegada rítmica limpia en 80Hz y brillo en percusiones y metales latinos.",
            bands = listOf(2f, 3.5f, 5f, 3.5f, 1.5f, 0.5f, 1.5f, 2.5f, 3.5f, 4f, 4.5f, 4f, 4.5f, 5f, 4f),
            bands31 = listOf(
                0.5f, 1.5f, 2.5f, 3.8f, 4.8f, 5.5f, 4.8f, 3.5f, 2f, 1f,
                0.8f, 1f, 1.5f, 2f, 2.8f, 3.5f, 4f, 4.5f, 4.8f, 4.5f,
                4.2f, 4f, 4.2f, 4.5f, 4.8f, 5.2f, 5f, 4.8f, 4.2f, 3.8f, 3f
            ),
            paramFreq = 75f,
            paramGain = 4.0f,
            paramQ = 1.5f
        ),
        EqPresetItem(
            name = "Reggaeton",
            description = "Sub-grave 40Hz-55Hz demoledor para dembow urbano con voces al frente.",
            bands = listOf(5f, 7f, 6.5f, 3f, 0.5f, 0f, 1f, 2.5f, 3.5f, 4f, 3.5f, 3f, 4f, 5f, 4f),
            bands31 = listOf(
                3f, 4.5f, 6.5f, 8f, 7.5f, 6f, 4f, 2f, 1f, 0.5f,
                0f, 0.5f, 1f, 1.5f, 2.5f, 3.2f, 3.8f, 4.2f, 4.2f, 4f,
                3.5f, 3.2f, 3.5f, 3.8f, 4.2f, 4.8f, 5.2f, 5f, 4.5f, 3.8f, 3f
            ),
            paramFreq = 48f,
            paramGain = 6.0f,
            paramQ = 1.7f
        ),
        EqPresetItem(
            name = "Dance",
            description = "Curva en V enérgica con bombos contundentes y agudos electrónicos extendidos.",
            bands = listOf(4.5f, 6.5f, 5f, 2f, -0.5f, -1f, 0f, 1f, 2f, 3f, 3.5f, 4f, 5.5f, 6.5f, 5f),
            bands31 = listOf(
                3f, 4.5f, 6.2f, 7f, 6.5f, 5f, 3f, 1.5f, 0f, -0.8f,
                -1f, -0.8f, 0f, 0.5f, 1.2f, 2f, 2.5f, 3f, 3.2f, 3.5f,
                3.8f, 4f, 4.5f, 5f, 5.8f, 6.5f, 6.8f, 6.2f, 5.5f, 4.5f, 3.5f
            ),
            paramFreq = 52f,
            paramGain = 5.0f,
            paramQ = 1.3f
        ),
        EqPresetItem(
            name = "Rock",
            description = "Cuerpo en 80-250Hz, garra de bajo y ataque agresivo en guitarras eléctricas.",
            bands = listOf(2f, 3.5f, 4.5f, 3f, 2f, 1.5f, 0.5f, 1f, 2f, 2.5f, 3.5f, 3f, 3.5f, 4f, 3.5f),
            bands31 = listOf(
                1f, 2f, 2.8f, 3.5f, 4.2f, 4.8f, 4.5f, 3f, 2.5f, 2f,
                1.8f, 1.5f, 1f, 0.5f, 0.8f, 1.2f, 1.8f, 2.2f, 2.5f, 2.8f,
                3.2f, 3.5f, 3.2f, 3f, 3.2f, 3.5f, 3.8f, 4f, 3.8f, 3.5f, 3f
            ),
            paramFreq = 80f,
            paramGain = 3.5f,
            paramQ = 1.4f
        ),
        EqPresetItem(
            name = "Custom",
            description = "Perfil personalizado libre guardado por el usuario.",
            bands = List(15) { 0.0f },
            bands31 = List(31) { 0.0f },
            paramFreq = 60f,
            paramGain = 0f,
            paramQ = 1.4f,
            isCustom = true
        )
    )
}

