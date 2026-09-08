package com.example.model

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val masterGainDb: Float = 0.0f, // -12 to +12 dB
    val isLimiterActive: Boolean = true,
    val clippingThreshold: Float = 95f,
    // 15 Standard ISO Graphic Bands:
    // 25Hz, 40Hz, 63Hz, 100Hz, 160Hz, 250Hz, 400Hz, 630Hz, 1kHz, 1.6kHz, 2.5kHz, 4kHz, 6.3kHz, 10kHz, 16kHz
    val bands15: List<Float> = listOf(
        4.0f,  // 25Hz
        5.0f,  // 40Hz
        6.5f,  // 63Hz
        3.0f,  // 100Hz
        0.0f,  // 160Hz
        -1.0f, // 250Hz
        0.5f,  // 400Hz
        2.0f,  // 630Hz
        3.5f,  // 1kHz
        4.0f,  // 1.6kHz
        3.0f,  // 2.5kHz
        2.0f,  // 4kHz
        3.5f,  // 6.3kHz
        5.0f,  // 10kHz
        4.0f   // 16kHz
    ),
    // Parametric band
    val parametricFreqHz: Float = 65.0f,
    val parametricGainDb: Float = 4.0f,
    val parametricQ: Float = 1.4f,
    val activePresetName: String = "🇧🇷 Pancadão Som Automotivo"
) {
    companion object {
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
    val paramFreq: Float,
    val paramGain: Float,
    val paramQ: Float
)

object EqPresetCatalog {
    val presets: List<EqPresetItem> = listOf(
        EqPresetItem(
            name = "🇧🇷 Pancadão Som Automotivo",
            description = "Graves secos y contundentes en 63Hz, ataque de medios altos para corneteras brasileñas.",
            bands = listOf(2f, 4f, 7f, 3f, 0f, -1f, 1f, 2.5f, 4f, 4.5f, 3f, 2.5f, 3.5f, 5f, 4f),
            paramFreq = 63f,
            paramGain = 4.5f,
            paramQ = 1.8f
        ),
        EqPresetItem(
            name = "🔊 Subão Grave Pesado",
            description = "Excursión profunda de subwoofers en 35Hz-50Hz con caída suave en medios para bajos expansivos.",
            bands = listOf(6f, 8f, 6.5f, 3f, 0f, -2f, -1f, 0f, 1f, 1f, 1.5f, 2f, 2.5f, 3f, 3f),
            paramFreq = 42f,
            paramGain = 6f,
            paramQ = 1.2f
        ),
        EqPresetItem(
            name = "🎤 Voz Chuchero / Open Show",
            description = "Corte de subgraves para protección, máxima proyección en voces y drivers de titanio.",
            bands = listOf(-6f, -4f, -2f, 0f, 1f, 2f, 3.5f, 5f, 6f, 6.5f, 5.5f, 4f, 4.5f, 5f, 4f),
            paramFreq = 1200f,
            paramGain = 5f,
            paramQ = 1.0f
        ),
        EqPresetItem(
            name = "🎶 Forró & Piseiro Brasil",
            description = "Especial para teclado, zabumba y acordeón: pegada seca y medios cristalinos.",
            bands = listOf(1f, 3f, 5.5f, 4f, 1f, 0f, 1.5f, 3f, 3.5f, 4f, 3f, 3f, 4f, 4.5f, 3.5f),
            paramFreq = 75f,
            paramGain = 3.5f,
            paramQ = 1.5f
        ),
        EqPresetItem(
            name = "⚡ Batidão Funk Brasil",
            description = "Subgrave redondo con agudos súper estirados para platillos y percusión sintética.",
            bands = listOf(5f, 7f, 6f, 2f, -1f, -1.5f, 0f, 1f, 2f, 3f, 2.5f, 3f, 5f, 6.5f, 5.5f),
            paramFreq = 50f,
            paramGain = 5f,
            paramQ = 1.3f
        ),
        EqPresetItem(
            name = "🎸 Rock & SQL Punch",
            description = "Bombo apretado en 80Hz, cuerpo de bajo en 250Hz y ataque de guitarras en 2.5kHz.",
            bands = listOf(2f, 3.5f, 4.5f, 3f, 2f, 1.5f, 0.5f, 1f, 2f, 2.5f, 3.5f, 3f, 3.5f, 4f, 3.5f),
            paramFreq = 80f,
            paramGain = 3f,
            paramQ = 1.4f
        ),
        EqPresetItem(
            name = "📏 RTA Flat / Lineal",
            description = "Curva completamente plana a 0 dB para calibración acústica con micrófono RTA.",
            bands = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            paramFreq = 1000f,
            paramGain = 0f,
            paramQ = 1.0f
        )
    )
}
