package com.example

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudioEngine
import com.example.model.AbSlot
import com.example.model.AlbumSummary
import com.example.model.AppSection
import com.example.model.ArtistSummary
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspAbSnapshot
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.model.YouTubeMusicItem
import com.example.ui.components.AppSettingsView
import com.example.ui.components.AudioToolsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.EqualizerView
import com.example.ui.components.FavoritesView
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.MyMusicView
import com.example.ui.components.PlayerView
import com.example.ui.components.PresetsView
import com.example.ui.components.TutorialCenterView
import com.example.ui.components.YouTubeMusicView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.LibraryFilter
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val theme = CarAudioThemeType.GRAPHITE_STUDIO

    private val sampleTracks = listOf(
        AudioTrackItem(
            id = "media_101",
            title = "Noche Acústica (Master 24-bit)",
            artist = "Estudio Session",
            album = "Sesiones Hi-Res",
            durationSeconds = 214,
            category = TrackCategory.USER_CUSTOM,
            isFavorite = true
        ),
        AudioTrackItem(
            id = "media_102",
            title = "Sub-Bass Reference 38Hz",
            artist = "Audio Local",
            album = "Descargas",
            durationSeconds = 186,
            category = TrackCategory.USER_CUSTOM
        ),
        AudioTrackItem(
            id = "media_103",
            title = "Vocal Clarity Studio Take",
            artist = "Mi Carpeta",
            album = "Biblioteca",
            durationSeconds = 242,
            category = TrackCategory.USER_CUSTOM
        )
    )

    private val studioDsp = DspSettings(
        dspMasterEnabled = true,
        preampDb = 2.0f,
        bassGainDb = 3.5f,
        midGainDb = 0.5f,
        trebleGainDb = 2.5f,
        bassBoostEnabled = true,
        bassBoostDb = 6.0f,
        stereoWidthPercent = 125f,
        compressorEnabled = true,
        limiterEnabled = true,
        limiterCeilingDb = -0.5f
    )

    private val studioEq = EqualizerSettings(
        isEnabled = true,
        masterGainDb = 1.0f,
        bands10 = listOf(4.0f, 3.5f, 1.5f, 0.0f, -0.5f, 0.5f, 1.5f, 2.5f, 3.0f, 2.5f),
        bands31 = List(31) { idx ->
            when {
                idx <= 6 -> 4.0f - idx * 0.4f
                idx in 7..14 -> 1.0f - (idx - 7) * 0.2f
                idx in 15..22 -> (idx - 15) * 0.3f
                else -> 2.8f
            }
        },
        activePresetName = "Estudio Equilibrado"
    )

    private val capability = DspCapabilityReport(
        audioSessionId = 42,
        dynamicsProcessingSupported = true,
        dynamicsProcessingActive = true,
        equalizerBandCount = 31,
        usingFallbackEqualizer = false,
        bassBoostSupported = false,
        bassBoostActive = false,
        virtualizerSupported = true,
        virtualizerActive = true,
        antiDoubleProcessingSafe = true,
        engineSummary = "DynamicsProcessing 31B Activo"
    )

    private val snapshotA = DspAbSnapshot(
        slot = AbSlot.A,
        name = "A • Estudio",
        dspSettings = studioDsp,
        bands10 = studioEq.bands10,
        bands31 = studioEq.bands31
    )

    private val snapshotB = DspAbSnapshot(
        slot = AbSlot.B,
        name = "B • Sub-Bass",
        dspSettings = studioDsp.copy(bassGainDb = 7.0f),
        bands10 = List(10) { if (it < 2) 6.0f else 0f },
        bands31 = List(31) { if (it < 6) 6.0f else 0f }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ScreenshotScaffold(
        activeSection: AppSection,
        content: @Composable () -> Unit
    ) {
        CarAudioAppTheme(themeType = theme) {
            Scaffold(
                containerColor = theme.backgroundColor,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.backgroundColor)
                                        .border(1.5.dp, theme.accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                                        contentDescription = "Logo Car Audio DSP Pro",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Car Audio DSP Pro",
                                        color = theme.textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${activeSection.shortTitle} • Bluetooth Audio",
                                        color = theme.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(theme.cardColor)
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
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(theme.cardColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = null,
                                            tint = theme.textColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.surfaceColor)
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = theme.surfaceColor) {
                        listOf(
                            Triple(AppSection.HOME, "Inicio", Icons.Default.Home),
                            Triple(AppSection.MY_MUSIC, "Mi música", Icons.Default.LibraryMusic),
                            Triple(AppSection.PLAYER, "Repro", Icons.Default.PlayCircle),
                            Triple(AppSection.EQUALIZER, "EQ", Icons.Default.Equalizer)
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

    @Test
    fun render_01_home_dashboard_3d() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.HOME) {
                HomeDashboardView(
                    theme = theme,
                    dspSettings = studioDsp,
                    equalizerSettings = studioEq,
                    capabilityReport = capability,
                    isAnyAudioPlaying = true,
                    currentTrack = sampleTracks.first(),
                    playbackSeconds = 72,
                    tracksCount = 24,
                    recentTracks = sampleTracks,
                    mostPlayedTracks = sampleTracks.take(2),
                    favoriteTracks = sampleTracks.take(1),
                    playlists = listOf(MusicPlaylist("pl1", "Referencia Hi-Res", listOf("media_101"))),
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Car", OutputKind.BLUETOOTH),
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
        composeTestRule.onNodeWithTag("home_dashboard_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_continue_listening_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_play_all_button").assertIsDisplayed()
    }

    @Test
    fun render_02_my_music_and_player() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.MY_MUSIC) {
                MyMusicView(
                    theme = theme,
                    tracks = sampleTracks,
                    artists = listOf(ArtistSummary("Estudio Session", 1, 1, null)),
                    albums = listOf(AlbumSummary("Sesiones Hi-Res", "Estudio Session", 1, null)),
                    playlists = listOf(MusicPlaylist("pl1", "Referencia Hi-Res", listOf("media_101"))),
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
        composeTestRule.onNodeWithTag("my_music_view").assertIsDisplayed()
    }

    @Test
    fun render_03_player_console() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.PLAYER) {
                PlayerView(
                    theme = theme,
                    queueTracks = sampleTracks,
                    currentTrack = sampleTracks.first(),
                    isPlaying = true,
                    playbackSeconds = 68,
                    volumeLevel = 0.88f,
                    shuffleEnabled = true,
                    repeatMode = RepeatMode.ALL,
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Car", OutputKind.BLUETOOTH),
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.72f,
                    midEnergy = 0.58f,
                    trebleEnergy = 0.64f,
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
        composeTestRule.onNodeWithTag("music_player_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_play_pause_button").assertIsDisplayed()
    }

    @Test
    fun render_04_equalizer_and_youtube_music() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.EQUALIZER) {
                EqualizerView(
                    theme = theme,
                    dspSettings = studioDsp,
                    eqSettings = studioEq,
                    capabilityReport = capability,
                    abSlotA = snapshotA,
                    abSlotB = snapshotB,
                    activeAbSlot = AbSlot.A,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.70f,
                    midEnergy = 0.56f,
                    trebleEnergy = 0.52f,
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
        composeTestRule.onNodeWithTag("equalizer_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ab_comparison_bar").assertIsDisplayed()
    }

    @Test
    fun render_05_youtube_music_view() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.YOUTUBE_MUSIC) {
                YouTubeMusicView(
                    theme = theme,
                    searchQuery = "Bass Test 35Hz",
                    savedItems = listOf(
                        YouTubeMusicItem("yt1", "Subwoofer Test 35Hz", "Car Audio", isFavorite = true)
                    ),
                    searchHistory = listOf("Bass Test 35Hz"),
                    onSearchQueryChange = {},
                    onRecordSearch = {},
                    onSaveShortcut = { _, _, _, _ -> },
                    onToggleFavorite = {},
                    onRemoveItem = {},
                    onClearHistory = {},
                    buildSearchUrl = { "https://music.youtube.com/search?q=$it" }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("youtube_music_view").assertIsDisplayed()
    }

    @Test
    fun render_06_audio_tools_presets_favorites_and_settings() {
        val activeRta = CarAudioEngine.createInitialRtaBands().mapIndexed { idx, band ->
            val lvl = -12f - (idx % 6) * 5f + (if (idx < 8) 5f else 0f)
            band.copy(levelDb = lvl.coerceIn(-55f, -2f), peakDb = (lvl + 3f).coerceIn(-52f, 0f))
        }
        val wave = FloatArray(128) { idx -> kotlin.math.sin(idx * 0.25f) * 0.65f }

        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.AUDIO_TOOLS) {
                AudioToolsView(
                    theme = theme,
                    dspSettings = studioDsp,
                    capabilityReport = capability,
                    rtaBands = activeRta,
                    oscilloscopeWave = wave,
                    leftVu = 0.76f,
                    rightVu = 0.72f,
                    rmsDb = -9.4f,
                    splDb = 78.2f,
                    isClippingDetected = false,
                    clipEventsCount = 0,
                    isMicRtaActive = false,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.76f,
                    midEnergy = 0.60f,
                    trebleEnergy = 0.54f,
                    liteVisuals = false,
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Car", OutputKind.BLUETOOTH),
                    powerTelemetry = DevicePowerTelemetry(86, 4.18f, 30.8f, -310f, false, true, true, true),
                    onToggleDspMaster = {},
                    onPreampChanged = {},
                    onMasterGainChanged = {},
                    onBassGainChanged = {},
                    onMidGainChanged = {},
                    onTrebleGainChanged = {},
                    onBassBoostChanged = { _, _, _ -> },
                    onSubBassChanged = { _, _, _ -> },
                    onLoudnessChanged = { _, _ -> },
                    onBalanceChanged = {},
                    onStereoWidthChanged = {},
                    onToggleCompressor = {},
                    onCompressorParamsChanged = { _, _, _, _ -> },
                    onToggleLimiter = {},
                    onLimiterCeilingChanged = {},
                    onSubsonicChanged = { _, _ -> },
                    onHpfChanged = { _, _, _ -> },
                    onLpfChanged = { _, _, _ -> },
                    onHiResChanged = {},
                    onTubeWarmthChanged = {},
                    onPresenceChanged = { _, _ -> },
                    onClarityChanged = { _, _ -> },
                    onExciterChanged = { _, _ -> },
                    onToggleMicRta = {},
                    onRefreshTelemetry = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("audio_tools_view").assertIsDisplayed()
    }

    @Test
    fun render_07_presets_favorites_settings_and_tutorials() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.PRESETS) {
                PresetsView(
                    theme = theme,
                    presets = EqPresetCatalog.presets,
                    activePresetName = "Estudio Equilibrado",
                    customProfiles = emptyList(),
                    abSlotA = snapshotA,
                    abSlotB = snapshotB,
                    activeAbSlot = AbSlot.A,
                    onSelectPreset = {},
                    onDeletePreset = {},
                    onSaveSoundProfile = { _, _ -> },
                    onApplySoundProfile = {},
                    onDeleteSoundProfile = {},
                    onCaptureAbSlot = {},
                    onApplyAbSlot = {},
                    onToggleAbComparison = {},
                    onClearAbSlot = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("presets_view").assertIsDisplayed()
    }

    @Test
    fun render_08_favorites_and_settings() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.FAVORITES) {
                FavoritesView(
                    theme = theme,
                    favoriteTracks = sampleTracks.take(1),
                    currentTrack = sampleTracks.first(),
                    playlists = listOf(MusicPlaylist("pl1", "Referencia Hi-Res", listOf("media_101"))),
                    ytFavoriteItems = listOf(YouTubeMusicItem("yt1", "Bass Check", "Subwoofer", isFavorite = true)),
                    onPlayTrack = {},
                    onToggleFavoriteTrack = {},
                    onAddToQueue = {},
                    onPlayNextInQueue = {},
                    onCreatePlaylist = {},
                    onDeletePlaylist = {},
                    onOpenPlaylistInMyMusic = {},
                    onAddTrackToPlaylist = { _, _ -> },
                    onToggleYtFavorite = {},
                    onOpenYouTubeSection = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorites_view").assertIsDisplayed()
    }

    @Test
    fun render_09_settings_welcome_and_tutorials() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.SETTINGS) {
                AppSettingsView(
                    theme = theme,
                    onSelectTheme = {},
                    liteVisuals = false,
                    onToggleLiteVisuals = {},
                    keepScreenOn = true,
                    onToggleKeepScreenOn = {},
                    autoPlayIntro = false,
                    onToggleAutoPlayIntro = {},
                    onReplayIntro = {},
                    onResetDspDefaults = {},
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Car", OutputKind.BLUETOOTH),
                    sessionTarget = AudioSessionTarget.INTERNAL_PLAYER,
                    capabilityReport = capability,
                    hasAudioPermission = true,
                    hasNotificationPermission = true,
                    faultAlerts = emptyList(),
                    onRequestNotificationPermission = {},
                    onRequestAudioPermission = {},
                    onRefreshDiagnostics = {},
                    onClearFaults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_settings_view").assertIsDisplayed()
    }

    @Test
    fun render_10_welcome_and_tutorial_center() {
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                CarAudioSplashScreen(
                    theme = theme,
                    onEnterApp = {},
                    onSetDontShowAgain = {},
                    initialDontShowAgain = true
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("audio_dsp_welcome_screen").assertIsDisplayed()
    }

    @Test
    fun render_11_tutorial_center() {
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                TutorialCenterView(
                    theme = theme,
                    onReplayQuickGuide = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tutorial_center_view").assertIsDisplayed()
    }
}
