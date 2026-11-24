package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudioEngine
import com.example.model.AbSlot
import com.example.model.AlbumSummary
import com.example.model.AppSection
import com.example.model.ArtistSummary
import com.example.model.AudioTrackItem
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspAbSnapshot
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqualizerSettings
import com.example.model.FolderSummary
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.ui.components.BassBoostView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.EpicAmbientBackdrop
import com.example.ui.components.EqualizerView
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.MyMusicView
import com.example.ui.components.PlayerView
import com.example.ui.components.VisualizerView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.LibraryFilter
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class StoreScreenshotCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val theme = CarAudioThemeType.NEON_DSP_PRO

    private val sampleTracks = listOf(
        AudioTrackItem(
            id = "track_01",
            title = "Noche de Cabina (Studio Master)",
            artist = "LM Produce",
            album = "Car Audio Sessions",
            durationSeconds = 214,
            category = TrackCategory.USER_CUSTOM,
            isFavorite = true,
            audioFormatLabel = "FLAC 24-bit",
            folderName = "Música"
        ),
        AudioTrackItem(
            id = "track_02",
            title = "Dembow Deep Bass 40Hz",
            artist = "Estudio Dominicano",
            album = "Subwoofer Reference",
            durationSeconds = 186,
            category = TrackCategory.USER_CUSTOM,
            isFavorite = true,
            audioFormatLabel = "MP3 320k",
            folderName = "Dembow"
        ),
        AudioTrackItem(
            id = "track_03",
            title = "Midnight Neon Drive",
            artist = "Synth Collective",
            album = "Electro Cabina",
            durationSeconds = 242,
            category = TrackCategory.USER_CUSTOM,
            audioFormatLabel = "WAV",
            folderName = "Electrónica"
        ),
        AudioTrackItem(
            id = "track_04",
            title = "Vocal Clarity Acoustic",
            artist = "Acústica Pura",
            album = "Referencia Hi-Fi",
            durationSeconds = 198,
            category = TrackCategory.USER_CUSTOM,
            audioFormatLabel = "AAC",
            folderName = "Favoritas"
        )
    )

    private val proDsp = DspSettings(
        dspMasterEnabled = true,
        preampDb = 2.0f,
        bassGainDb = 5.5f,
        midGainDb = 1.0f,
        trebleGainDb = 3.5f,
        bassBoostEnabled = true,
        bassBoostDb = 7.5f,
        bassBoostFreqHz = 46f,
        subBassEnabled = true,
        subBassBoostDb = 5.0f,
        subBassFreqHz = 36f,
        bassPunchLevel = 4.5f,
        stereoWidthPercent = 135f,
        compressorEnabled = true,
        limiterEnabled = true,
        limiterCeilingDb = -0.5f,
        activePresetName = "Dembow dominicano"
    )

    private val proEq = EqualizerSettings(
        isEnabled = true,
        masterGainDb = 1.5f,
        bands10 = listOf(5.5f, 4.5f, 2.0f, 0.5f, -0.5f, 0.5f, 1.5f, 2.5f, 3.5f, 3.0f),
        bands31 = List(31) { idx ->
            when {
                idx <= 5 -> 5.5f - idx * 0.45f
                idx in 6..13 -> 2.0f - (idx - 6) * 0.3f
                idx in 14..22 -> (idx - 14) * 0.38f
                else -> 3.4f
            }
        },
        activePresetName = "Dembow dominicano"
    )

    private val capability = DspCapabilityReport(
        audioSessionId = 48,
        dynamicsProcessingSupported = true,
        dynamicsProcessingActive = true,
        equalizerBandCount = 31,
        usingFallbackEqualizer = false,
        bassBoostSupported = true,
        bassBoostActive = true,
        virtualizerSupported = true,
        virtualizerActive = true,
        antiDoubleProcessingSafe = true,
        engineSummary = "DynamicsProcessing 31B Activo"
    )

    private val snapshotA = DspAbSnapshot(
        slot = AbSlot.A,
        name = "A • Dembow",
        dspSettings = proDsp,
        bands10 = proEq.bands10,
        bands31 = proEq.bands31
    )

    private val snapshotB = DspAbSnapshot(
        slot = AbSlot.B,
        name = "B • Vocal",
        dspSettings = proDsp.copy(bassGainDb = 1.5f, trebleGainDb = 5.0f),
        bands10 = listOf(0f, 1f, 1f, 2f, 3f, 4f, 4f, 3f, 2f, 1f),
        bands31 = List(31) { if (it in 12..22) 3.5f else 0.5f }
    )

    @Before
    fun onlyWhenRequested() {
        Assume.assumeTrue(
            "Solo captura capturas de tienda cuando se ejecuta recordRoborazziDebug",
            System.getProperty("roborazzi.test.record") == "true"
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CaptureShell(
        activeSection: AppSection,
        content: @Composable () -> Unit
    ) {
        CarAudioAppTheme(themeType = theme) {
            EpicAmbientBackdrop(theme = theme) {
                Scaffold(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menú",
                                            tint = theme.textColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Image(
                                            painter = painterResource(id = R.drawable.img_car_audio_logo_1795273778926),
                                            contentDescription = "Logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .border(1.2.dp, theme.primaryColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = activeSection.title,
                                                color = theme.textColor,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Bluetooth Car Audio • EQ 31B",
                                                color = theme.accentColor,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.cardColor)
                                                    .border(0.8.dp, theme.silverDetailColor.copy(alpha = 0.25f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = theme.silverDetailColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.primaryColor.copy(alpha = 0.26f))
                                                    .border(1.dp, theme.accentColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PowerSettingsNew,
                                                    contentDescription = null,
                                                    tint = theme.accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = theme.surfaceColor.copy(alpha = 0.88f)
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                theme.primaryColor.copy(alpha = 0.55f),
                                                theme.silverDetailColor.copy(alpha = 0.30f),
                                                theme.accentColor.copy(alpha = 0.55f)
                                            )
                                        )
                                    )
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(containerColor = theme.surfaceColor.copy(alpha = 0.94f)) {
                            listOf(
                                Triple(AppSection.HOME, "Inicio", Icons.Default.Home),
                                Triple(AppSection.MY_MUSIC, "Música", Icons.Default.LibraryMusic),
                                Triple(AppSection.PLAYER, "Repro", Icons.Default.PlayCircle),
                                Triple(AppSection.EQUALIZER, "EQ", Icons.Default.Equalizer),
                                Triple(AppSection.SETTINGS, "Ajustes", Icons.Default.Settings)
                            ).forEach { (sec, label, icon) ->
                                NavigationBarItem(
                                    selected = activeSection == sec,
                                    onClick = {},
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = theme.onPrimaryColor,
                                        indicatorColor = theme.primaryColor,
                                        selectedTextColor = theme.accentColor,
                                        unselectedIconColor = theme.textSecondaryColor,
                                        unselectedTextColor = theme.textSecondaryColor
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        content()
                    }
                }
            }
        }
    }

    @Test
    fun capture_01_inicio_panel() {
        composeTestRule.setContent {
            CaptureShell(AppSection.HOME) {
                HomeDashboardView(
                    theme = theme,
                    dspSettings = proDsp,
                    equalizerSettings = proEq,
                    capabilityReport = capability,
                    isAnyAudioPlaying = true,
                    currentTrack = sampleTracks.first(),
                    playbackSeconds = 84,
                    tracksCount = 42,
                    recentTracks = sampleTracks,
                    mostPlayedTracks = sampleTracks.take(3),
                    favoriteTracks = sampleTracks.take(2),
                    playlists = listOf(
                        MusicPlaylist("pl1", "Car Bass 2026", listOf("track_01", "track_02")),
                        MusicPlaylist("pl2", "Dembow Nocturno", listOf("track_02"))
                    ),
                    outputDeviceStatus = OutputDeviceStatus("BT: Pioneer Car Audio", OutputKind.BLUETOOTH),
                    bassMode = BassMode.POWERFUL,
                    libraryTracks = sampleTracks,
                    hasAudioLibraryPermission = true,
                    bassEnergy = 0.78f,
                    midEnergy = 0.62f,
                    trebleEnergy = 0.56f,
                    onNavigateSection = {},
                    onTogglePlayPause = {},
                    onNextTrack = {},
                    onPreviousTrack = {},
                    onPlayTrack = {},
                    onPlayAll = {},
                    onSelectPlaylist = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/01_inicio_panel.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/01_inicio_panel.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/01_inicio_panel.png")
    }

    @Test
    fun capture_02_reproductor_pro() {
        composeTestRule.setContent {
            CaptureShell(AppSection.PLAYER) {
                PlayerView(
                    theme = theme,
                    queueTracks = sampleTracks,
                    currentTrack = sampleTracks.first(),
                    isPlaying = true,
                    playbackSeconds = 84,
                    volumeLevel = 0.88f,
                    shuffleEnabled = true,
                    repeatMode = RepeatMode.ALL,
                    outputDeviceStatus = OutputDeviceStatus("BT: Pioneer Car Audio", OutputKind.BLUETOOTH),
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.80f,
                    midEnergy = 0.60f,
                    trebleEnergy = 0.55f,
                    liteVisuals = false,
                    activeAbSlot = AbSlot.A,
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeek = {},
                    onVolumeChange = {},
                    onToggleShuffle = {},
                    onCycleRepeat = {},
                    onToggleFavorite = {},
                    onToggleAbComparison = {},
                    onPlayQueueAt = {},
                    onMoveQueueUp = {},
                    onMoveQueueDown = {},
                    onRemoveFromQueue = {},
                    onClearQueue = {},
                    onOpenMyMusic = {},
                    onOpenEqualizer = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/02_reproductor_pro.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/02_reproductor_pro.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/02_reproductor_pro.png")
    }

    @Test
    fun capture_03_ecualizador_31b() {
        composeTestRule.setContent {
            CaptureShell(AppSection.EQUALIZER) {
                EqualizerView(
                    theme = theme,
                    dspSettings = proDsp,
                    eqSettings = proEq,
                    capabilityReport = capability,
                    abSlotA = snapshotA,
                    abSlotB = snapshotB,
                    activeAbSlot = AbSlot.A,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.78f,
                    midEnergy = 0.62f,
                    trebleEnergy = 0.58f,
                    liteVisuals = false,
                    onToggleDspMaster = {},
                    onToggleEqPower = {},
                    onUpdateBand10 = { _, _ -> },
                    onUpdateBand31 = { _, _ -> },
                    onUpdateParametricBand = { _, _, _, _, _, _ -> },
                    onToggleParametricBand = { _, _ -> },
                    onResetFlat = {},
                    onCaptureAbSlot = {},
                    onApplyAbSlot = {},
                    onToggleAbComparison = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/03_ecualizador_31b.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/03_ecualizador_31b.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/03_ecualizador_31b.png")
    }

    @Test
    fun capture_04_bass_boost_3d() {
        composeTestRule.setContent {
            CaptureShell(AppSection.BASS_BOOST) {
                BassBoostView(
                    theme = theme,
                    dspSettings = proDsp,
                    bassMode = BassMode.POWERFUL,
                    isPlaying = true,
                    bassEnergy = 0.85f,
                    midEnergy = 0.56f,
                    trebleEnergy = 0.48f,
                    isClippingDetected = false,
                    liteVisuals = false,
                    onSelectBassMode = {},
                    onSetBassBoost = { _, _, _ -> },
                    onSetBassGain = {},
                    onSetDeepBass = { _, _, _ -> },
                    onToggleLimiter = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/04_bass_boost_3d.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/04_bass_boost_3d.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/04_bass_boost_3d.png")
    }

    @Test
    fun capture_05_visualizador_rta() {
        val activeRta = CarAudioEngine.createInitialRtaBands().mapIndexed { idx, band ->
            val lvl = -10f - (idx % 5) * 4.5f + (if (idx < 9) 5f else 0f)
            band.copy(levelDb = lvl.coerceIn(-55f, -2f), peakDb = (lvl + 3f).coerceIn(-52f, 0f))
        }
        val wave = FloatArray(128) { idx -> kotlin.math.sin(idx * 0.25f) * 0.68f }
        composeTestRule.setContent {
            CaptureShell(AppSection.VISUALIZER) {
                VisualizerView(
                    theme = theme,
                    rtaBands = activeRta,
                    oscilloscopeWave = wave,
                    leftVu = 0.82f,
                    rightVu = 0.78f,
                    rmsDb = -8.6f,
                    splDb = 84.5f,
                    isClippingDetected = false,
                    clipEventsCount = 0,
                    isMicRtaActive = false,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.82f,
                    midEnergy = 0.62f,
                    trebleEnergy = 0.54f,
                    dspMasterEnabled = true,
                    liteVisuals = false,
                    outputDeviceStatus = OutputDeviceStatus("BT: Pioneer Car Audio", OutputKind.BLUETOOTH),
                    powerTelemetry = DevicePowerTelemetry(86, 4.18f, 30.8f, -310f, false, true, true, true),
                    onToggleLiteVisuals = {},
                    onToggleMicRta = {},
                    onRefreshTelemetry = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/05_visualizador_rta.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/05_visualizador_rta.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/05_visualizador_rta.png")
    }

    @Test
    fun capture_06_biblioteca_musica() {
        composeTestRule.setContent {
            CaptureShell(AppSection.MY_MUSIC) {
                MyMusicView(
                    theme = theme,
                    tracks = sampleTracks,
                    artists = listOf(
                        ArtistSummary("LM Produce", 2, 1),
                        ArtistSummary("Estudio Dominicano", 1, 1),
                        ArtistSummary("Synth Collective", 1, 1)
                    ),
                    albums = listOf(
                        AlbumSummary("Car Audio Sessions", "LM Produce", 2),
                        AlbumSummary("Subwoofer Reference", "Estudio Dominicano", 1)
                    ),
                    folders = listOf(
                        FolderSummary("Música", 2),
                        FolderSummary("Dembow", 1)
                    ),
                    playlists = listOf(MusicPlaylist("pl1", "Car Bass 2026", listOf("track_01", "track_02"))),
                    selectedPlaylistId = null,
                    currentTrack = sampleTracks.first(),
                    searchQuery = "",
                    libraryFilter = LibraryFilter.ALL,
                    hasAudioLibraryPermission = true,
                    isScanningLibrary = false,
                    onTrackSelect = {},
                    onToggleFavorite = {},
                    onRemoveTrack = {},
                    onAddToQueue = {},
                    onPlayNextInQueue = {},
                    onPlayArtist = {},
                    onPlayAlbum = {},
                    onSearchQueryChange = {},
                    onLibraryFilterChange = {},
                    onSelectPlaylist = {},
                    onCreatePlaylist = {},
                    onDeletePlaylist = {},
                    onAddTrackToPlaylist = { _, _ -> },
                    onCustomTracksLoaded = {},
                    onRequestLibraryPermission = {},
                    onRefreshLibrary = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/06_biblioteca_musica.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/es-ES/images/phoneScreenshots/06_biblioteca_musica.png")
        composeTestRule.onRoot().captureRoboImage("../fastlane/metadata/android/en-US/images/phoneScreenshots/06_biblioteca_musica.png")
    }

    @Test
    fun capture_07_bienvenida_neon() {
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                CarAudioSplashScreen(
                    theme = theme,
                    onEnterApp = {},
                    onSetDontShowAgain = {},
                    initialDontShowAgain = false
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("../screenshots/07_bienvenida_neon.png")
    }
}
