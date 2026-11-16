package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class AppSection(
    val title: String,
    val shortTitle: String,
    val subtitle: String
) {
    HOME("Inicio", "Inicio", "Panel principal y acceso rápido"),
    RADIO_PIONEER("Radio Pioneer", "Radio", "Autoestéreos Pioneer y emisoras en vivo"),
    MUSIC_PLAYER("Reproductor de música", "Música", "Biblioteca real, listas y portada"),
    EQUALIZER("Ecualizador", "EQ", "31 bandas ISO, 15 bandas y paramétrico"),
    SPEAKERS("Panel de altavoces", "Altavoces", "Subwoofer, fader, balance y tweeters"),
    SOUND_TOOLS("Herramientas de sonido", "Sonido", "RTA, voltímetro batería y configuraciones"),
    PERSONALIZATION("Personalización", "Estilos", "Temas, iluminación Pioneer y efectos"),
    VIP_ZONE("Zona VIP", "VIP", "Mejoras Hi-Res Studio y funciones Pro"),
    SETTINGS("Configuración", "Ajustes", "Diagnóstico, notificaciones y sistema")
}

enum class PioneerRadioStyle(
    val id: String,
    val displayName: String,
    val modelCode: String,
    val subtitle: String
) {
    CLASSIC("classic", "Pioneer Clásico", "DEH-X6800BT", "Carátula clásica OLED Mixtrax con dial cromado"),
    DIGITAL("digital", "Pioneer Digital", "DEH-P80PRS", "Doble línea OLED audiophile de alta precisión"),
    MULTIMEDIA("multimedia", "Pioneer Multimedia", "DMH-WT8600", "Panel táctil flotante con tarjetas en vivo"),
    SCREEN("screen", "Pioneer con Pantalla", "AVH-Z9200DAB", "Pantalla ancha cockpit con espectro visual"),
    COMPETITION("competition", "Pioneer Competición", "ODR SPL-PRO", "Telemetría negra carbono de competencia")
}

enum class PioneerIlluminationColor(
    val id: String,
    val displayName: String,
    val primaryHex: Long,
    val secondaryHex: Long
) {
    ELECTRIC_BLUE("electric_blue", "Azul Eléctrico", 0xFF00A3FF, 0xFF00FF87),
    NEON_GREEN("neon_green", "Verde Neón", 0xFF00FF87, 0xFF00A3FF),
    CRISP_WHITE("crisp_white", "Blanco Puro", 0xFFF5F9FF, 0xFF00A3FF),
    ARCTIC_CYAN("arctic_cyan", "Cian Ice", 0xFF00E5FF, 0xFFF5F9FF),
    AMBER_CLASSIC("amber_classic", "Ámbar Clásico", 0xFFFFB300, 0xFF00A3FF);

    val primaryColor: Color get() = Color(primaryHex)
    val secondaryColor: Color get() = Color(secondaryHex)
}

@Immutable
data class RadioStation(
    val id: String,
    val name: String,
    val frequencyMhz: Float,
    val genre: String,
    val country: String,
    val streamUrl: String,
    val bitrateKbps: Int,
    val formatLabel: String = "MP3",
    val isFavorite: Boolean = false
)

object VerifiedRadioCatalog {
    val stations: List<RadioStation> = listOf(
        RadioStation(
            id = "rp_main",
            name = "Radio Paradise Main Mix",
            frequencyMhz = 91.1f,
            genre = "Rock / Eclectic HD",
            country = "EE.UU.",
            streamUrl = "https://stream.radioparadise.com/mp3-192",
            bitrateKbps = 192
        ),
        RadioStation(
            id = "rp_rock",
            name = "Radio Paradise Rock",
            frequencyMhz = 93.5f,
            genre = "Rock Clásico & Moderno",
            country = "EE.UU.",
            streamUrl = "https://stream.radioparadise.com/rock-192",
            bitrateKbps = 192
        ),
        RadioStation(
            id = "rp_mellow",
            name = "Radio Paradise Mellow",
            frequencyMhz = 95.7f,
            genre = "Acústico & Chill",
            country = "EE.UU.",
            streamUrl = "https://stream.radioparadise.com/mellow-192",
            bitrateKbps = 192
        ),
        RadioStation(
            id = "soma_groove",
            name = "SomaFM Groove Salad",
            frequencyMhz = 89.3f,
            genre = "Downtempo / Ambient",
            country = "San Francisco",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            bitrateKbps = 128
        ),
        RadioStation(
            id = "soma_trip",
            name = "SomaFM The Trip",
            frequencyMhz = 106.5f,
            genre = "Progressive House / Trance",
            country = "San Francisco",
            streamUrl = "https://ice1.somafm.com/thetrip-128-mp3",
            bitrateKbps = 128
        ),
        RadioStation(
            id = "soma_beat",
            name = "SomaFM Beat Blender",
            frequencyMhz = 96.9f,
            genre = "Deep House / Beats",
            country = "San Francisco",
            streamUrl = "https://ice1.somafm.com/beatblender-128-mp3",
            bitrateKbps = 128
        ),
        RadioStation(
            id = "nightride_main",
            name = "Nightride FM Synthwave",
            frequencyMhz = 98.5f,
            genre = "Synthwave / Bass",
            country = "Internacional",
            streamUrl = "https://stream.nightride.fm/nightride.mp3",
            bitrateKbps = 320
        ),
        RadioStation(
            id = "nightride_chill",
            name = "Chillsynth FM",
            frequencyMhz = 101.3f,
            genre = "Chillsynth Night Drive",
            country = "Internacional",
            streamUrl = "https://stream.nightride.fm/chillsynth.mp3",
            bitrateKbps = 320
        ),
        RadioStation(
            id = "nightride_space",
            name = "Spacesynth FM",
            frequencyMhz = 103.7f,
            genre = "Electronic Bass",
            country = "Internacional",
            streamUrl = "https://stream.nightride.fm/spacesynth.mp3",
            bitrateKbps = 320
        ),
        RadioStation(
            id = "nightride_data",
            name = "Datawave FM",
            frequencyMhz = 105.9f,
            genre = "Cyberpunk Electro",
            country = "Internacional",
            streamUrl = "https://stream.nightride.fm/datawave.mp3",
            bitrateKbps = 320
        ),
        RadioStation(
            id = "swiss_pop",
            name = "Radio Swiss Pop",
            frequencyMhz = 99.9f,
            genre = "Pop & Éxitos",
            country = "Suiza",
            streamUrl = "https://stream.srg-ssr.ch/m/rsp/mp3_128",
            bitrateKbps = 128
        ),
        RadioStation(
            id = "soma_defcon",
            name = "SomaFM DEF CON Radio",
            frequencyMhz = 107.3f,
            genre = "Techno / Bass Electrónico",
            country = "San Francisco",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
            bitrateKbps = 128
        )
    )
}

@Immutable
data class MusicPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList()
)

@Immutable
data class DevicePowerTelemetry(
    val batteryPercent: Int? = null,
    val batteryVoltageV: Float? = null,
    val batteryTempC: Float? = null,
    val isCharging: Boolean = false,
    val hasBatterySensor: Boolean = false,
    val hasTempSensor: Boolean = false
)

@Immutable
data class CustomSoundProfile(
    val id: String,
    val name: String,
    val description: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val dspSettings: DspSettings,
    val eqBands15: List<Float>,
    val eqBands31: List<Float>
)
