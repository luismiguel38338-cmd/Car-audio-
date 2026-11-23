package com.example.model

import androidx.compose.runtime.Immutable

enum class ParametricFilterType(val label: String) {
    PEAKING("Campana (Peaking)"),
    LOW_SHELF("Low Shelf"),
    HIGH_SHELF("High Shelf"),
    NOTCH("Filtro Notch")
}

@Immutable
data class ParametricBand(
    val id: Int,
    val enabled: Boolean = true,
    val freqHz: Float,
    val gainDb: Float,
    val q: Float,
    val filterType: ParametricFilterType = ParametricFilterType.PEAKING
)

@Immutable
data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val masterGainDb: Float = 0.0f, // -12 to +12 dB
    val isLimiterActive: Boolean = true,
    val clippingThreshold: Float = 95f,
    // 10 Standard ISO Octave Graphic Bands (31Hz - 16kHz)
    val bands10: List<Float> = listOf(
        2.5f, 3.0f, 1.5f, 0.0f, -0.5f, 0.5f, 1.5f, 2.5f, 3.0f, 2.5f
    ),
    // 31 Standard ISO 1/3-Octave Graphic Bands (20Hz - 20kHz)
    val bands31: List<Float> = listOf(
        1.5f, 2.0f, 2.5f, 3.0f, 3.0f, 3.0f, 2.0f, 1.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.0f, 0.5f, 0.8f, 1.0f, 1.4f, 1.8f, 2.0f, 2.2f,
        2.0f, 1.8f, 1.5f, 2.0f, 2.4f, 2.8f, 3.0f, 3.0f, 2.8f, 2.5f, 2.0f
    ),
    // 15 Standard ISO Graphic Bands (retained for compatibility with saved profiles)
    val bands15: List<Float> = listOf(
        2.0f, 3.0f, 3.0f, 1.5f, 0.0f, -0.5f, 0.5f, 1.2f, 1.8f, 2.2f, 1.8f, 1.5f, 2.5f, 3.0f, 2.5f
    ),
    // Multi-band Parametric EQ (5 fully configurable bands)
    val parametricBands: List<ParametricBand> = listOf(
        ParametricBand(1, true, 45f, 2.0f, 1.4f, ParametricFilterType.LOW_SHELF),
        ParametricBand(2, true, 90f, 1.5f, 1.8f, ParametricFilterType.PEAKING),
        ParametricBand(3, true, 260f, -1.0f, 1.4f, ParametricFilterType.PEAKING),
        ParametricBand(4, true, 1600f, 1.5f, 1.2f, ParametricFilterType.PEAKING),
        ParametricBand(5, true, 9000f, 1.5f, 1.0f, ParametricFilterType.HIGH_SHELF)
    ),
    val parametricFreqHz: Float = 65.0f,
    val parametricGainDb: Float = 2.0f,
    val parametricQ: Float = 1.4f,
    val activePresetName: String = "Estudio Equilibrado"
) {
    companion object {
        val FREQUENCY_LABELS_10 = listOf(
            "31", "63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"
        )
        val FREQUENCIES_HZ_10 = listOf(
            31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
        )

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

        /**
         * Derives 10 ISO octave bands cleanly from a 31-band array using representative centers.
         */
        fun bands10From31(bands31: List<Float>): List<Float> {
            val map31Indices = listOf(2, 5, 8, 11, 14, 17, 20, 23, 26, 29)
            return map31Indices.map { idx -> bands31.getOrElse(idx) { 0f } }
        }
    }
}

@Immutable
data class EqPresetItem(
    val name: String,
    val description: String,
    val bands10: List<Float>,
    val bands: List<Float>,
    val bands31: List<Float> = emptyList(),
    val paramFreq: Float = 60f,
    val paramGain: Float = 0f,
    val paramQ: Float = 1.4f,
    val isCustom: Boolean = false,
    val isVip: Boolean = false
)

object EqPresetCatalog {
    val presets: List<EqPresetItem> = listOf(
        EqPresetItem(
            name = "Flat",
            description = "Respuesta plana y lineal 0 dB en todas las bandas.",
            bands10 = List(10) { 0.0f },
            bands = List(15) { 0.0f },
            bands31 = List(31) { 0.0f },
            paramFreq = 1000f,
            paramGain = 0f,
            paramQ = 1.0f
        ),
        EqPresetItem(
            name = "Estudio Equilibrado",
            description = "Curva balanceada de referencia con graves firmes y detalle natural.",
            bands10 = listOf(2.5f, 3.0f, 1.5f, 0.0f, -0.5f, 0.5f, 1.5f, 2.5f, 3.0f, 2.5f),
            bands = listOf(2.0f, 3.0f, 3.0f, 1.5f, 0.0f, -0.5f, 0.5f, 1.2f, 1.8f, 2.2f, 1.8f, 1.5f, 2.5f, 3.0f, 2.5f),
            bands31 = listOf(
                1.5f, 2.0f, 2.5f, 3.0f, 3.0f, 3.0f, 2.0f, 1.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.0f, 0.5f, 0.8f, 1.0f, 1.4f, 1.8f, 2.0f, 2.2f,
                2.0f, 1.8f, 1.5f, 2.0f, 2.4f, 2.8f, 3.0f, 3.0f, 2.8f, 2.5f, 2.0f
            ),
            paramFreq = 65f,
            paramGain = 2.0f,
            paramQ = 1.4f
        ),
        EqPresetItem(
            name = "Dembow dominicano",
            description = "Golpe seco en 45-85Hz, presencia vocal nítida y agudos brillantes estilo chipeo.",
            bands10 = listOf(6.8f, 7.2f, 3.0f, -1.0f, 1.2f, 3.2f, 4.0f, 4.8f, 5.5f, 4.0f),
            bands = listOf(5.5f, 7.0f, 7.5f, 4.5f, 1.5f, -1.0f, 0.0f, 1.5f, 3.0f, 3.8f, 4.2f, 4.8f, 5.2f, 5.5f, 4.2f),
            bands31 = listOf(
                4.0f, 5.5f, 6.8f, 7.5f, 7.8f, 7.2f, 5.5f, 3.5f, 2.0f, 0.0f,
                -1.0f, -1.0f, -0.5f, 0.5f, 1.2f, 2.0f, 2.8f, 3.2f, 3.8f, 4.0f,
                4.0f, 4.2f, 4.5f, 4.8f, 5.2f, 5.5f, 5.5f, 5.0f, 4.5f, 4.0f, 3.2f
            ),
            paramFreq = 55f,
            paramGain = 6.5f,
            paramQ = 1.5f
        ),
        EqPresetItem(
            name = "Reggaetón",
            description = "Sub-graves cálidos 35-65Hz, pegada de bombo y voces limpias al frente.",
            bands10 = listOf(6.2f, 6.0f, 2.0f, -0.5f, 1.5f, 3.5f, 3.2f, 3.8f, 5.0f, 3.6f),
            bands = listOf(5.0f, 6.8f, 6.2f, 3.5f, 1.0f, -0.5f, 0.5f, 1.8f, 3.2f, 3.8f, 3.4f, 3.6f, 4.4f, 5.0f, 3.8f),
            bands31 = listOf(
                3.5f, 4.8f, 6.2f, 7.2f, 6.8f, 6.0f, 4.2f, 2.5f, 1.2f, 0.0f,
                -0.5f, -0.5f, 0.0f, 0.8f, 1.5f, 2.2f, 3.0f, 3.5f, 3.6f, 3.4f,
                3.2f, 3.4f, 3.6f, 3.8f, 4.4f, 4.8f, 5.0f, 4.6f, 4.2f, 3.6f, 2.8f
            ),
            paramFreq = 48f,
            paramGain = 6.0f,
            paramQ = 1.5f
        ),
        EqPresetItem(
            name = "Bachata",
            description = "Calidez en güira y requinto, bajo suave y melodía vocal protagonista.",
            bands10 = listOf(3.0f, 3.8f, 2.5f, 1.0f, 1.5f, 3.0f, 4.2f, 4.8f, 4.5f, 3.0f),
            bands = listOf(2.0f, 3.2f, 3.8f, 2.8f, 1.8f, 1.0f, 1.2f, 2.0f, 3.0f, 3.8f, 4.2f, 4.8f, 4.6f, 4.2f, 3.2f),
            bands31 = listOf(
                1.5f, 2.2f, 3.0f, 3.6f, 4.0f, 3.8f, 3.0f, 2.5f, 1.8f, 1.2f,
                1.0f, 1.0f, 1.2f, 1.4f, 1.5f, 2.0f, 2.5f, 3.0f, 3.6f, 4.0f,
                4.2f, 4.5f, 4.8f, 4.8f, 4.6f, 4.5f, 4.5f, 4.0f, 3.5f, 3.0f, 2.2f
            ),
            paramFreq = 2500f,
            paramGain = 3.5f,
            paramQ = 1.2f
        ),
        EqPresetItem(
            name = "Merengue",
            description = "Tambora firme, brillo metálico en trompetas y saxofones con ritmo ágil.",
            bands10 = listOf(3.5f, 4.2f, 2.8f, 0.5f, 1.0f, 2.5f, 3.8f, 4.5f, 5.2f, 3.8f),
            bands = listOf(2.5f, 3.8f, 4.2f, 3.0f, 1.5f, 0.5f, 0.8f, 1.5f, 2.5f, 3.2f, 3.8f, 4.5f, 5.0f, 5.2f, 4.0f),
            bands31 = listOf(
                2.0f, 2.8f, 3.5f, 4.0f, 4.4f, 4.2f, 3.4f, 2.8f, 1.8f, 1.0f,
                0.5f, 0.5f, 0.8f, 1.0f, 1.2f, 1.8f, 2.2f, 2.5f, 3.2f, 3.6f,
                3.8f, 4.0f, 4.2f, 4.5f, 4.8f, 5.0f, 5.2f, 4.8f, 4.2f, 3.8f, 3.0f
            ),
            paramFreq = 4000f,
            paramGain = 4.0f,
            paramQ = 1.3f
        ),
        EqPresetItem(
            name = "Trap",
            description = "Sub-graves 808 extendidos (30-50Hz), hi-hats crujientes y voz definida.",
            bands10 = listOf(8.0f, 6.2f, 1.0f, -1.5f, 0.5f, 2.5f, 3.2f, 4.2f, 5.8f, 4.8f),
            bands = listOf(7.0f, 8.5f, 6.5f, 3.0f, 0.0f, -1.5f, -0.5f, 1.0f, 2.5f, 3.0f, 3.5f, 4.2f, 5.0f, 5.8f, 4.8f),
            bands31 = listOf(
                5.5f, 7.0f, 8.0f, 8.8f, 7.8f, 6.2f, 3.8f, 2.0f, 0.5f, -0.8f,
                -1.5f, -1.2f, -0.5f, 0.0f, 0.5f, 1.2f, 2.0f, 2.5f, 3.0f, 3.2f,
                3.2f, 3.6f, 4.0f, 4.2f, 4.8f, 5.4f, 5.8f, 5.6f, 5.2f, 4.8f, 4.0f
            ),
            paramFreq = 36f,
            paramGain = 7.5f,
            paramQ = 1.6f
        ),
        EqPresetItem(
            name = "Hip-Hop",
            description = "Bombo profundo, bajo redondo y articulación limpia en rimas y coros.",
            bands10 = listOf(6.5f, 5.8f, 1.5f, -0.5f, 1.0f, 3.0f, 2.8f, 3.2f, 4.5f, 3.5f),
            bands = listOf(5.0f, 6.8f, 5.8f, 3.0f, 0.8f, -0.5f, 0.2f, 1.5f, 2.8f, 3.2f, 2.8f, 3.2f, 4.0f, 4.5f, 3.5f),
            bands31 = listOf(
                3.8f, 5.0f, 6.5f, 7.2f, 6.5f, 5.8f, 3.8f, 2.2f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.0f, 0.5f, 1.0f, 1.8f, 2.4f, 3.0f, 3.0f, 2.8f,
                2.8f, 3.0f, 3.2f, 3.2f, 3.8f, 4.2f, 4.5f, 4.2f, 3.8f, 3.5f, 2.8f
            ),
            paramFreq = 48f,
            paramGain = 6.0f,
            paramQ = 1.5f
        ),
        EqPresetItem(
            name = "Electrónica",
            description = "Curva dinámica en V con pegada rítmica, kicks profundos y brillo extendido.",
            bands10 = listOf(6.2f, 5.0f, 0.0f, -0.8f, 1.2f, 3.0f, 3.8f, 5.0f, 6.8f, 4.5f),
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
            description = "Cuerpo en 80-250Hz, garra de bajo y presencia en guitarras eléctricas.",
            bands10 = listOf(2.8f, 4.8f, 2.5f, 1.5f, 0.8f, 2.2f, 3.2f, 3.0f, 3.8f, 3.5f),
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
            name = "Música vocal",
            description = "Máxima claridad y definición en voces principales e instrumentos acústicos (800Hz - 3.1kHz).",
            bands10 = listOf(-4.0f, 0.0f, 1.5f, 3.0f, 5.0f, 6.5f, 5.0f, 3.5f, 3.0f, 2.0f),
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
            name = "Graves Limpios",
            description = "Realce controlado de bajos 40Hz-100Hz con medios transparentes.",
            bands10 = listOf(4.5f, 4.0f, 2.0f, 0.0f, 0.0f, 1.0f, 1.5f, 2.0f, 3.0f, 2.0f),
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
            name = "Sub-Bass Competición",
            description = "Excursión profunda controlada en 25-50 Hz para subwoofer automotriz sin opacar voces.",
            bands10 = listOf(9.5f, 8.0f, 1.5f, -1.5f, 0.5f, 2.8f, 3.2f, 3.4f, 4.6f, 3.2f),
            bands = listOf(8f, 10f, 8.5f, 4.5f, 1.0f, -1.5f, -1.0f, 0.5f, 2.0f, 3.5f, 3.0f, 3.0f, 4.0f, 4.5f, 3.5f),
            bands31 = listOf(
                6.5f, 8.0f, 9.5f, 10.5f, 9.5f, 8.0f, 5.8f, 3.5f, 1.5f, 0f,
                -1.2f, -1.5f, -1.0f, -0.5f, 0.5f, 1.2f, 2.0f, 2.8f, 3.4f, 3.5f,
                3.2f, 3.0f, 3.0f, 3.4f, 4.0f, 4.4f, 4.6f, 4.2f, 3.8f, 3.2f, 2.5f
            ),
            paramFreq = 36f,
            paramGain = 8.0f,
            paramQ = 1.5f,
            isVip = false
        ),
        EqPresetItem(
            name = "Acústica Cabina Pro",
            description = "Amplitud acústica inmersiva para cabina de vehículo, calidez analógica y brillo sedoso.",
            bands10 = listOf(4.0f, 3.8f, 0.5f, -0.8f, 1.0f, 3.2f, 3.8f, 4.0f, 5.5f, 4.2f),
            bands = listOf(3f, 4.5f, 4f, 2f, 0.5f, -1f, -0.5f, 1f, 2.5f, 3.5f, 4f, 3.5f, 4.5f, 5.5f, 5f),
            bands31 = listOf(
                2f, 3f, 4f, 4.8f, 4.5f, 3.8f, 2.5f, 1.5f, 0.5f, -0.5f,
                -1f, -0.8f, -0.4f, 0.2f, 1.0f, 1.8f, 2.5f, 3.2f, 3.8f, 4.0f,
                3.8f, 3.5f, 3.6f, 4.0f, 4.6f, 5.2f, 5.5f, 5.2f, 4.8f, 4.2f, 3.6f
            ),
            paramFreq = 60f,
            paramGain = 4.0f,
            paramQ = 1.2f,
            isVip = false
        ),
        EqPresetItem(
            name = "Personalizado",
            description = "Perfil personalizado libre ajustado a tu cabina automotriz.",
            bands10 = List(10) { 0.0f },
            bands = List(15) { 0.0f },
            bands31 = List(31) { 0.0f },
            paramFreq = 60f,
            paramGain = 0f,
            paramQ = 1.4f,
            isCustom = true
        )
    )
}

