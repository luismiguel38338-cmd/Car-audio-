package com.example.model

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class AppSection(
    val route: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String
) {
    HOME("home", "Inicio", "Inicio", "Tu música reciente, biblioteca y accesos rápidos"),
    MY_MUSIC("my_music", "Mi Música", "Música", "Canciones, álbumes, artistas, carpetas y listas"),
    PLAYER("player", "Reproductor", "Sonando", "Carátula, barra de progreso, cola y temporizador"),
    ONLINE_MUSIC("online_music", "Música Online", "Online", "Catálogo Jamendo libre de derechos y YouTube Music"),
    DOWNLOADS("downloads", "Descargas", "Descargas", "Descargas offline con progreso y pausa"),
    EQUALIZER("equalizer", "Ecualizador", "EQ / DSP", "Ecualizador de 10 y 31 bandas, paramétrico y A/B"),
    BASS_BOOST("bass_boost", "Bass Boost", "Graves", "Potenciador Normal, Potente y Extremo con anticlipping"),
    SUBWOOFER("subwoofer", "Subwoofer", "Subwoofer", "Control de sub-graves, filtro subsónico y crossover LPF"),
    AUDIO_TOOLS("audio_tools", "Efectos de Sonido", "Efectos", "Loudness, balance L/R, estéreo, compresor y excitador"),
    VISUALIZER("visualizer", "Visualizador", "Visualizador", "Espectro 31B, osciloscopio, medidores y modo ligero"),
    PRESETS("presets", "Presets", "Presets", "Dembow, Reggaetón, Bachata, Trap, perfiles y A/B"),
    FAVORITES("favorites", "Favoritos", "Favoritos", "Canciones favoritas y accesos rápidos guardados"),
    PLAYLISTS("playlists", "Listas", "Listas", "Crea, edita y reproduce tus listas de reproducción"),
    SETTINGS("settings", "Ajustes", "Ajustes", "Temas, rendimiento, almacenamiento y permisos"),
    PROFILE("profile", "Perfil", "Perfil", "Estadísticas locales y canciones más escuchadas"),
    ABOUT("about", "Acerca de", "Acerca", "CAR AUDIO DSP PRO • LM Produce — Producción de Luis Miguel"),
    YOUTUBE_MUSIC("youtube_music", "YouTube Music", "YT Music", "Búsqueda y accesos directos oficiales");
}

enum class BassMode(
    val label: String,
    val description: String,
    val defaultBoostDb: Float,
    val defaultCenterHz: Float,
    val defaultSubBassDb: Float,
    val defaultPunch: Float,
    val forceLimiter: Boolean
) {
    NORMAL(
        label = "Normal",
        description = "Graves balanceados y limpios para bocinas de cabina",
        defaultBoostDb = 3.0f,
        defaultCenterHz = 52f,
        defaultSubBassDb = 2.0f,
        defaultPunch = 1.5f,
        forceLimiter = false
    ),
    POWERFUL(
        label = "Potente",
        description = "Pegada reforzada en sub-bajos con buena definición vocal",
        defaultBoostDb = 7.5f,
        defaultCenterHz = 46f,
        defaultSubBassDb = 5.0f,
        defaultPunch = 4.5f,
        forceLimiter = true
    ),
    EXTREME(
        label = "Extremo",
        description = "Máxima presión sonora con limitador anti-saturación activo",
        defaultBoostDb = 11.5f,
        defaultCenterHz = 40f,
        defaultSubBassDb = 8.5f,
        defaultPunch = 7.5f,
        forceLimiter = true
    )
}

@Immutable
data class OnlineTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val artworkUrl: String?,
    val streamUrl: String,
    val downloadUrl: String?,
    val downloadAllowed: Boolean,
    val licenseUrl: String?,
    val sourceName: String = "Jamendo"
) {
    fun toAudioTrackItem(localUri: Uri? = null): AudioTrackItem = AudioTrackItem(
        id = "online_$id",
        title = title,
        artist = artist,
        album = album.ifBlank { sourceName },
        durationSeconds = durationSeconds,
        category = TrackCategory.USER_CUSTOM,
        frequencyDescription = if (localUri != null) "Descarga Offline • $sourceName" else "Streaming Online • $sourceName",
        customUri = localUri ?: Uri.parse(streamUrl),
        albumArtUri = artworkUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) },
        audioFormatLabel = if (localUri != null) "MP3 Offline" else "MP3 Stream",
        folderName = sourceName
    )
}

@Immutable
data class OnlineArtistItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val shareUrl: String?
)

@Immutable
data class OnlineAlbumItem(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String?,
    val releaseDate: String?
)

@Immutable
data class OnlineSearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isConfigured: Boolean = false,
    val errorMessage: String? = null,
    val tracks: List<OnlineTrackItem> = emptyList(),
    val artists: List<OnlineArtistItem> = emptyList(),
    val albums: List<OnlineAlbumItem> = emptyList(),
    val providerStatusMessage: String = ""
)

enum class DownloadStatus(val label: String) {
    QUEUED("En cola"),
    DOWNLOADING("Descargando"),
    PAUSED("Pausada"),
    COMPLETED("Completada"),
    FAILED("Error"),
    CANCELLED("Cancelada")
}

@Immutable
data class DownloadItem(
    val id: String,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val remoteUrl: String,
    val localPath: String,
    val licenseUrl: String?,
    val durationSeconds: Int,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null,
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (totalBytes > 0L) (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        else if (status == DownloadStatus.COMPLETED) 1f else 0f
}

@Immutable
data class LocalProfileStats(
    val displayName: String = "Cabina de Luis Miguel",
    val totalPlays: Int = 0,
    val totalListeningSeconds: Long = 0L,
    val customPresetsCount: Int = 0,
    val playlistsCount: Int = 0,
    val favoritesCount: Int = 0,
    val downloadsCount: Int = 0
)

enum class AdSlotType(val label: String) {
    BANNER("Banner inferior discreto"),
    INTERSTITIAL("Intersticial entre pantallas"),
    REWARDED("Recompensado opcional")
}

@Immutable
data class AdSlotConfig(
    val slotType: AdSlotType,
    val isSdkEnabled: Boolean = false,
    val isConfigured: Boolean = false,
    val policySummary: String = "Desactivado por política del Programa para Creadores de Google Play."
)

enum class LibrarySortOrder(val label: String) {
    RECENT("Recientes"),
    TITLE("Título A–Z"),
    ARTIST("Artista"),
    DURATION("Duración")
}

enum class AbSlot(val label: String, val badge: String) {
    A("Configuración A", "A"),
    B("Configuración B", "B")
}

@Immutable
data class DspAbSnapshot(
    val slot: AbSlot,
    val name: String,
    val dspSettings: DspSettings,
    val bands10: List<Float>,
    val bands31: List<Float>,
    val savedAtMs: Long = System.currentTimeMillis()
)

@Immutable
data class YouTubeMusicItem(
    val id: String,
    val title: String,
    val artist: String,
    val category: String = "Búsqueda directa",
    val customUrl: String? = null,
    val isFavorite: Boolean = false,
    val addedAtMs: Long = System.currentTimeMillis()
) {
    val resolvedUrl: String
        get() {
            val raw = customUrl?.trim()
            if (!raw.isNullOrBlank()) return raw
            val query = listOf(title, artist).filter { it.isNotBlank() }.joinToString(" ").trim()
            return "https://music.youtube.com/search?q=${Uri.encode(query)}"
        }
}

object YouTubeMusicCatalog {
    val quickLaunchSearches: List<YouTubeMusicItem> = listOf(
        YouTubeMusicItem(
            id = "yt_search_acoustic_ref",
            title = "Acoustic Studio Hi-Fi",
            artist = "Calibración Vocal",
            category = "Referencia"
        ),
        YouTubeMusicItem(
            id = "yt_search_bass_test",
            title = "Deep Sub Bass Test 35Hz",
            artist = "Prueba de Graves",
            category = "Graves"
        ),
        YouTubeMusicItem(
            id = "yt_search_electronic_club",
            title = "Night Drive Electronic Mix",
            artist = "Sesión Nocturna",
            category = "Electrónica"
        ),
        YouTubeMusicItem(
            id = "yt_search_rock_classics",
            title = "Rock Clásico Alta Dinámica",
            artist = "Guitarras y Batería",
            category = "Rock"
        )
    )
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
    val bitrateKbps: Int = 0,
    val folderName: String = "Música",
    val dateAddedSec: Long = 0L
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

@Immutable
data class FolderSummary(
    val name: String,
    val trackCount: Int,
    val sampleArtUri: Uri? = null
)

enum class TrackCategory {
    USER_CUSTOM
}

enum class RepeatMode(val label: String) {
    OFF("Sin repetir"),
    ALL("Repetir todo"),
    ONE("Repetir una")
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

    // Studio Enhancers
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
            title = "1. Acceso directo y cabina 3D",
            category = "Inicio rápido",
            summary = "Ingresa directo al panel automotriz sin cuentas obligatorias ni sonidos automáticos al arrancar.",
            steps = listOf(
                "Abre Car Audio DSP Pro directamente en la cabina automotriz 3D.",
                "Concede el permiso de Audio Local en Mi música para listar tus canciones, álbumes y artistas.",
                "Verifica el encendido maestro DSP en la barra superior y observa los subwoofers vibrar al ritmo."
            ),
            proTip = "El limitador de seguridad (-0.5 dB) protege las bocinas y auriculares contra picos digitales."
        ),
        TutorialTopic(
            id = 2,
            title = "2. Cómo cargar música del teléfono",
            category = "Mi música",
            summary = "Escanea carpetas locales con MediaStore o importa archivos MP3, FLAC, WAV y AAC.",
            steps = listOf(
                "Entra a la sección Mi música desde la barra inferior o el menú.",
                "Pulsa 'Escanear Teléfono' para detectar automáticamente la biblioteca del dispositivo.",
                "Usa 'Importar Archivos' para elegir canciones específicas sin copiar archivos duplicados."
            ),
            proTip = "Todas las canciones mostradas son archivos reales del teléfono reproducidos con Media3/ExoPlayer."
        ),
        TutorialTopic(
            id = 3,
            title = "3. Consola Reproductor y Cola",
            category = "Reproductor",
            summary = "Controla reproducción continua en segundo plano, orden de cola y Bluetooth vehicular.",
            steps = listOf(
                "Abre Reproductor para ver el subwoofer 3D animado y la carátula en tiempo real.",
                "Usa Siguiente, Anterior, Barra de tiempo, Aleatorio, Repetir y Volumen.",
                "Organiza la cola de reproducción reordenando canciones o guardando pistas en Favoritos."
            ),
            proTip = "La reproducción sigue activa con la pantalla apagada y muestra controles en la notificación."
        ),
        TutorialTopic(
            id = 4,
            title = "4. Ecualizador de 31 y 10 bandas",
            category = "Ecualizador",
            summary = "Ajusta curvas quirúrgicas de 1/3 de octava, 10 bandas rápidas o 5 bandas paramétricas.",
            steps = listOf(
                "Ve a Ecualizador y activa el interruptor del módulo.",
                "Elige 31 Bandas ISO (DynamicsProcessing API 28+) o 10 Bandas ISO para ajustes rápidos.",
                "Usa el botón 'Plano 0 dB' en cualquier momento para restaurar la curva lineal."
            ),
            proTip = "El motor evita el doble procesamiento apagando las etapas heredadas cuando DynamicsProcessing está activo."
        ),
        TutorialTopic(
            id = 5,
            title = "5. Comparación rápida A / B",
            category = "Comparador A/B",
            summary = "Guarda dos configuraciones de audio diferentes (A y B) y altérnalas en vivo con un toque.",
            steps = listOf(
                "Ajusta tu primera curva y pulsa 'Guardar A' en el Ecualizador o en Presets.",
                "Modifica graves, ecualización o filtros para probar otra idea y pulsa 'Guardar B'.",
                "Toca el botón A ↔ B para comparar ambas configuraciones en tiempo real mientras suena la música."
            ),
            proTip = "Igualar la ganancia entre A y B ayuda a juzgar la claridad real sin engañarte por el volumen."
        ),
        TutorialTopic(
            id = 6,
            title = "6. Graves, Sub-Bass y Filtros HPF/LPF",
            category = "Herramientas de audio",
            summary = "Obtén bajos potentes y controlados en cabina automotriz sin distorsionar medios ni voces.",
            steps = listOf(
                "En Herramientas de audio ajusta Bass Boost entre +2 dB y +6 dB alrededor de 45-55 Hz.",
                "Activa el Filtro Subsónico o Pasa-Altos (24-30 Hz) para proteger subwoofers de excursión innecesaria.",
                "Atenúa suavemente 250 Hz (-1 a -2 dB) si sientes los bajos retumbantes."
            ),
            proTip = "Observa el contador de Clipping en RTA y Medidores para asegurarte de no saturar la salida."
        ),
        TutorialTopic(
            id = 7,
            title = "7. Integración con YouTube Music",
            category = "YouTube Music",
            summary = "Busca canciones o artistas y ábrelos directamente en la app oficial de YouTube Music.",
            steps = listOf(
                "Abre la sección YouTube Music desde el menú o el acceso rápido en Inicio.",
                "Escribe el nombre de una canción, artista o género y pulsa 'Abrir en YouTube Music'.",
                "Guarda tus enlaces y búsquedas frecuentes como accesos rápidos o márcalos en Favoritos."
            ),
            proTip = "Por respeto a las políticas y derechos de autor, los enlaces lanzan el reproductor oficial de YouTube Music sin descargar audio."
        ),
        TutorialTopic(
            id = 8,
            title = "8. Analizador RTA 31B y Medidor dB",
            category = "Medidores",
            summary = "Mide el espectro de 20 Hz a 20 kHz, forma de onda, niveles VU/RMS y batería.",
            steps = listOf(
                "Entra a Herramientas de audio > pestaña RTA y Medidores dB.",
                "Reproduce una canción para ver las 31 barras de frecuencia, picos máximos y osciloscopio.",
                "Activa opcionalmente el Micrófono RTA para medir la acústica real dentro del vehículo."
            ),
            proTip = "Si los picos tocan 0 dBFS, activa el Limitador o baja unos dB en Preamplificador."
        ),
        TutorialTopic(
            id = 9,
            title = "9. Presets y perfiles guardados",
            category = "Presets",
            summary = "Aplica curvas calibradas por género o guarda tus propias configuraciones en el teléfono.",
            steps = listOf(
                "Visita la sección Presets para probar curvas listas (Estudio, Sub-Bass Competición, Vocal, Club, Rock).",
                "Pulsa 'Guardar Configuración Actual' para almacenar tu mezcla de EQ, bajos, balance y filtros.",
                "Carga o elimina tus perfiles locales cuando cambies de bocinas o auriculares."
            ),
            proTip = "Todos los presets y ajustes de estudio están desbloqueados sin suscripciones ni registros."
        ),
        TutorialTopic(
            id = 10,
            title = "10. Optimización para gama baja/media",
            category = "Configuración",
            summary = "Ajusta la cabina 3D automotriz para lograr máxima fluidez en cualquier teléfono Android.",
            steps = listOf(
                "Ve a Configuración y explora los 6 estilos visuales automotrices.",
                "Activa 'Efectos Visuales Ligeros' para reducir pasadas de iluminación 3D y ahorrar batería.",
                "Usa 'Mantener Pantalla Encendida' si colocas el teléfono en el tablero del vehículo."
            ),
            proTip = "El procesamiento de audio DSP mantiene la misma calidad sonora incluso con visuales ligeros activos."
        )
    )
}
