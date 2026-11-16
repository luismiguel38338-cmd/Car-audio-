package com.example.model

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class AppSection(
    val route: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String
) {
    HOME("home", "Inicio", "Inicio", "Panel principal y acceso rápido"),
    MUSIC_PLAYER("music_player", "Música", "Música", "Biblioteca, artistas, álbumes, listas y cola"),
    AUDIO_LAB("audio_lab", "Laboratorio de Audio", "Lab DSP", "Ecualizador 10/31 bandas, filtros y dinámica"),
    AUDIO_METERS("audio_meters", "Medidores y RTA", "Medidores", "Analizador de espectro, osciloscopio, VU y batería"),
    TUTORIALS("tutorials", "Tutoriales", "Guías", "Centro interactivo de guías de audio DSP"),
    PERSONALIZATION("personalization", "Apariencia", "Estilos", "Temas de estudio y rendimiento visual"),
    VIP_ZONE("vip_zone", "Zona VIP", "VIP", "Herramientas Hi-Res y estado de membresía"),
    SETTINGS("settings", "Configuración", "Ajustes", "Cuenta Google, nube Firebase, diagnóstico y privacidad")
}

@Immutable
data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String = "Desconocido",
    val album: String = "Archivo Local",
    val durationSeconds: Int = 0,
    val category: TrackCategory = TrackCategory.USER_CUSTOM,
    val frequencyDescription: String = "Audio Local",
    val customUri: Uri? = null,
    val albumArtUri: Uri? = null,
    val isFavorite: Boolean = false,
    val audioFormatLabel: String = "Audio HD",
    val bitrateKbps: Int = 0
) {
    val stableKey: String
        get() = customUri?.toString() ?: id
}

@Immutable
data class ArtistSummary(
    val name: String,
    val trackCount: Int,
    val albumCount: Int,
    val sampleArtUri: Uri? = null
)

@Immutable
data class AlbumSummary(
    val title: String,
    val artist: String,
    val trackCount: Int,
    val sampleArtUri: Uri? = null
)

enum class TrackCategory {
    USER_CUSTOM
}

enum class RepeatMode(val label: String) {
    OFF("Normal"),
    ALL("Repetir Todo"),
    ONE("Repetir 1")
}

enum class OutputKind {
    SPEAKER,
    WIRED_HEADSET,
    BLUETOOTH,
    USB_AUDIO,
    OTHER
}

@Immutable
data class OutputDeviceStatus(
    val label: String = "Altavoz del Teléfono",
    val kind: OutputKind = OutputKind.SPEAKER
)

enum class AudioSessionTarget(val label: String, val detail: String) {
    INTERNAL_PLAYER(
        "AudioFX Directo (Media3 / ExoPlayer)",
        "Procesamiento DSP nativo sincronizado con la sesión del reproductor"
    ),
    EXTERNAL_APP(
        "Sesión Externa Detectada",
        "AudioFX enlazado a la sesión compartida por otra aplicación de audio"
    ),
    GLOBAL_MIX_BEST_EFFORT(
        "Sesión Global (Sistema)",
        "Modo compatibilidad: algunas apps restringen efectos externos según el fabricante"
    )
}

@Immutable
data class DspCapabilityReport(
    val audioSessionId: Int = -1,
    val dynamicsProcessingSupported: Boolean = false,
    val dynamicsProcessingActive: Boolean = false,
    val equalizerBandCount: Int = 0,
    val usingFallbackEqualizer: Boolean = false,
    val bassBoostSupported: Boolean = false,
    val bassBoostActive: Boolean = false,
    val virtualizerSupported: Boolean = false,
    val virtualizerActive: Boolean = false,
    val antiDoubleProcessingSafe: Boolean = true,
    val engineSummary: String = "Esperando sesión de audio..."
)

enum class FaultSeverity {
    INFO,
    WARNING,
    ERROR
}

enum class FaultActionType {
    NONE,
    ENABLE_LIMITER,
    REQUEST_AUDIO_PERMISSION,
    REQUEST_NOTIFICATION_PERMISSION,
    RELOAD_LIBRARY,
    OPEN_DIAGNOSTICS
}

@Immutable
data class SystemFaultAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: FaultSeverity = FaultSeverity.WARNING,
    val timestampMs: Long = System.currentTimeMillis(),
    val actionLabel: String? = null,
    val actionType: FaultActionType = FaultActionType.NONE
)

@Immutable
data class DspSettings(
    // Master DSP State
    val dspMasterEnabled: Boolean = true,
    val inputGainDb: Float = 0.0f, // -12 to +12 dB
    val outputGainDb: Float = 0.0f, // -12 to +12 dB
    val preampDb: Float = 0.0f, // -12 to +18 dB
    val bassGainDb: Float = 1.5f, // -18 to +18 dB
    val midGainDb: Float = 0.0f, // -18 to +18 dB
    val trebleGainDb: Float = 1.0f, // -18 to +18 dB
    val headroomDb: Float = 6.0f,
    val sampleRate: Int = 48000,

    // Frequency Filters
    val hpfFrequencyHz: Float = 24f,
    val hpfSlopeDb: Int = 24, // 6, 12, 18, 24, 36, 48 dB/oct
    val hpfEnabled: Boolean = true,
    val hpfQ: Float = 0.707f,

    val lpfFrequencyHz: Float = 19000f,
    val lpfSlopeDb: Int = 24,
    val lpfEnabled: Boolean = false,
    val lpfQ: Float = 0.707f,

    val subsonicFrequencyHz: Float = 26f,
    val subsonicEnabled: Boolean = true,
    val subsonicFilterActive: Boolean = false,

    // Bass Boost & Sub-Bass
    val bassBoostEnabled: Boolean = true,
    val bassBoostDb: Float = 3.0f, // 0 to 24 dB
    val bassBoostFreqHz: Float = 48f, // 30Hz - 80Hz

    val subBassEnabled: Boolean = false,
    val subBassBoostDb: Float = 3.0f,
    val subBassFreqHz: Float = 35f,
    val bassPunchLevel: Float = 0.0f, // 0 to 10 dB transient punch

    val loudnessEnabled: Boolean = false,
    val loudnessGainDb: Float = 3.5f,

    // Stereo Field & Virtualizer
    val stereoWidthPercent: Float = 100f, // 100 to 250%
    val balancePan: Float = 0.0f, // -1.0 (Left) to +1.0 (Right)

    // Dynamics (Multiband Compressor & Brickwall Limiter)
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

    // VIP Studio Enhancers
    val hiResQualityEnabled: Boolean = false,
    val tubeWarmthLevel: Float = 0.0f, // 0 to 10

    val masterGainDb: Float = 0f, // -18 to +18 dB
    val activePresetName: String = "Estudio Equilibrado"
)

@Immutable
data class RtaBand(
    val label: String,
    val freqHz: Int,
    val levelDb: Float, // -60 to 0 dB
    val peakDb: Float
)

@Immutable
data class OscilloscopeState(
    val isRunning: Boolean = true,
    val isFrozen: Boolean = false,
    val timebaseMs: Float = 2.0f,
    val triggerLevel: Float = 0.0f,
    val triggerAuto: Boolean = true,
    val isClippingDetected: Boolean = false,
    val peakVoltageEstimate: Float = 0.0f
)

enum class SplWeighting(val label: String, val suffix: String) {
    A_WEIGHTING("Ponderación A (dBA)", "dBA"),
    C_WEIGHTING("Ponderación C (dBC)", "dBC"),
    Z_WEIGHTING("Ponderación Z (dBZ - Plana)", "dBZ")
}

enum class SplSpeed(val label: String) {
    FAST("Rápido (125 ms)"),
    SLOW("Lento (1000 ms)")
}

@Immutable
data class SplCalibrationSettings(
    val micOffsetDb: Float = 0.0f,
    val weighting: SplWeighting = SplWeighting.C_WEIGHTING,
    val speed: SplSpeed = SplSpeed.FAST,
    val isCalibratedMic: Boolean = false
)

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
    val batteryCurrentMa: Float? = null,
    val isCharging: Boolean = false,
    val hasBatterySensor: Boolean = false,
    val hasTempSensor: Boolean = false,
    val hasCurrentSensor: Boolean = false
)

@Immutable
data class CustomSoundProfile(
    val id: String,
    val name: String,
    val description: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val dspSettings: DspSettings,
    val eqBands10: List<Float> = List(10) { 0f },
    val eqBands15: List<Float> = List(15) { 0f },
    val eqBands31: List<Float> = List(31) { 0f }
)

@Immutable
data class TutorialTopic(
    val id: Int,
    val title: String,
    val category: String,
    val summary: String,
    val steps: List<String>,
    val proTip: String
)

object TutorialCatalog {
    val topics: List<TutorialTopic> = listOf(
        TutorialTopic(
            id = 1,
            title = "1. Primeros pasos",
            category = "Inicio rápido",
            summary = "Conecta tu cuenta Google, prepara tus permisos multimedia y activa el motor DSP nativo.",
            steps = listOf(
                "Inicia sesión con Google para respaldar tus playlists, presets y preferencias en Firebase.",
                "Concede el permiso de Audio Local para descubrir las canciones, artistas y álbumes de tu dispositivo.",
                "Verifica que el botón de encendido maestro DSP esté activo en la barra superior."
            ),
            proTip = "El límite de seguridad (-0.5 dB) viene activado por defecto para proteger tus auriculares contra picos digitales."
        ),
        TutorialTopic(
            id = 2,
            title = "2. Cómo importar música",
            category = "Biblioteca",
            summary = "Escanea carpetas de tu teléfono o selecciona archivos individuales en alta resolución.",
            steps = listOf(
                "Abre la sección Música desde la barra inferior.",
                "Pulsa 'Escanear Teléfono' para indexar automáticamente archivos MP3, FLAC, WAV, AAC u OGG.",
                "Usa el botón 'Importar Archivos' para elegir pistas específicas mediante el selector seguro de Android."
            ),
            proTip = "Solo mostramos archivos reales autorizados en tu dispositivo; nunca mezclamos pistas de demostración falsas."
        ),
        TutorialTopic(
            id = 3,
            title = "3. Cómo utilizar el reproductor",
            category = "Reproducción",
            summary = "Navega por canciones, artistas, álbumes, listas y administra la cola de reproducción.",
            steps = listOf(
                "Alterna entre las pestañas Canciones, Artistas, Álbumes, Listas y Cola.",
                "Usa el menú de cada pista para añadirla a la Cola, reproducirla a continuación o guardarla en Favoritos.",
                "Controla la reproducción desde la barra de notificaciones, pantalla bloqueada o auriculares Bluetooth."
            ),
            proTip = "La última canción y su posición exacta se guardan automáticamente en tu cuenta."
        ),
        TutorialTopic(
            id = 4,
            title = "4. Cómo configurar el ecualizador",
            category = "Laboratorio DSP",
            summary = "Elige entre 10 bandas rápidas, 31 bandas de alta precisión o 5 bandas paramétricas.",
            steps = listOf(
                "Entra al Laboratorio de Audio y activa el módulo Ecualizador.",
                "En 10 Bandas ajusta los rangos principales (31 Hz a 16 kHz) de forma ágil.",
                "En 31 Bandas (cuando tu dispositivo soporta DynamicsProcessing API 28+) realiza ajustes quirúrgicos de 1/3 de octava."
            ),
            proTip = "Nuestro motor evita el doble procesamiento apagando las etapas redundantes automáticamente."
        ),
        TutorialTopic(
            id = 5,
            title = "5. Cómo mejorar los bajos",
            category = "Graves y Sub-Bass",
            summary = "Logra graves profundos y firmes sin saturar ni embarrar las voces.",
            steps = listOf(
                "Ajusta el control Bass Boost entre +2 dB y +6 dB según el tamaño de tus auriculares o altavoces.",
                "Activa el Filtro Subsónico entre 24 Hz y 30 Hz para eliminar frecuencias inaudibles que consumen energía.",
                "Si buscas mayor definición, atenúa levemente alrededor de 250 Hz (-1 dB a -2 dB) en el ecualizador."
            ),
            proTip = "Sube el grave poco a poco mientras observas el medidor de Clipping en la pestaña Medidores."
        ),
        TutorialTopic(
            id = 6,
            title = "6. Cómo configurar los agudos",
            category = "Claridad y Aire",
            summary = "Añade brillo, aire y detalle vocal sin provocar fatiga auditiva.",
            steps = listOf(
                "En la pestaña Tono del Laboratorio, eleva Agudos entre +1 dB y +3 dB.",
                "Usa las bandas de 8 kHz a 16 kHz para dar sensación de espacio y detalle en platillos.",
                "Si percibes sibilancia excesiva en voces ('eses' fuertes), reduce suavemente entre 5 kHz y 6.3 kHz."
            ),
            proTip = "Activa Presencia o Claridad en niveles moderados si escuchas a volumen bajo."
        ),
        TutorialTopic(
            id = 7,
            title = "7. Cómo utilizar los presets",
            category = "Presets",
            summary = "Aplica curvas calibradas por género o estilo en un solo toque.",
            steps = listOf(
                "Abre la pestaña Presets dentro del Laboratorio de Audio.",
                "Toca cualquier preset de estudio (Flat, Estudio Equilibrado, Graves Limpios, Vocal, Rock, etc.).",
                "Al modificar cualquier banda después de cargar un preset, el perfil cambia automáticamente a Personalizado."
            ),
            proTip = "Usa 'Flat' como punto de partida neutral cuando cambies de auriculares."
        ),
        TutorialTopic(
            id = 8,
            title = "8. Cómo funciona el analizador de espectro",
            category = "Medidores",
            summary = "Interpreta las 31 bandas RTA, el osciloscopio de onda y el nivel VU/RMS en dBFS.",
            steps = listOf(
                "Abre la sección Medidores y reproduce una canción para ver las barras RTA de 20 Hz a 20 kHz.",
                "Observa los picos blancos superiores y el osciloscopio inferior para detectar compresión o recorte.",
                "Opcionalmente activa el modo Micrófono RTA (requiere permiso de micrófono) para medir la respuesta acústica real de tu sala."
            ),
            proTip = "Si los picos golpean 0 dBFS repetidamente, activa el Limitador o reduce el Preamplificador."
        ),
        TutorialTopic(
            id = 9,
            title = "9. Cómo guardar configuraciones",
            category = "Nube Firebase",
            summary = "Guarda tus propios perfiles de ecualización y filtros con sincronización en la nube.",
            steps = listOf(
                "Dentro del Laboratorio de Audio, pulsa 'Guardar Configuración' en la pestaña Presets.",
                "Asigna un nombre y una descripción breve a tu ajuste.",
                "Tus configuraciones se guardan tanto en el dispositivo como en tu cuenta de Firebase Firestore."
            ),
            proTip = "Al iniciar sesión en otro teléfono con la misma cuenta Google, recuperarás tus perfiles guardados."
        ),
        TutorialTopic(
            id = 10,
            title = "10. Cómo utilizar las funciones VIP",
            category = "Zona VIP",
            summary = "Conoce los módulos avanzados Hi-Res, temas exclusivos y cómo se gestiona tu membresía.",
            steps = listOf(
                "Visita Zona VIP para comparar las funciones gratuitas frente a las herramientas avanzadas VIP.",
                "Las cuentas VIP acceden a procesamiento Hi-Res Studio, calidez analógica, presets maestros y temas de diseño.",
                "Tu estado VIP se valida desde el servidor de tu cuenta y se restaura al iniciar sesión."
            ),
            proTip = "Si cierras el aviso promocional en Inicio, no volverá a mostrarse automáticamente."
        )
    )
}
