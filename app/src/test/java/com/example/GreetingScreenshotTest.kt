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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudioEngine
import com.example.model.AlbumSummary
import com.example.model.AppSection
import com.example.model.ArtistSummary
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
import com.example.model.DevicePowerTelemetry
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.ui.components.AppSettingsView
import com.example.ui.components.AudioLabView
import com.example.ui.components.AudioMetersView
import com.example.ui.components.AuthScreen
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.PlayerView
import com.example.ui.components.TutorialCenterView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.LibraryFilter
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
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

    private val sampleProfile = CloudUserProfile(
        id = "uid_test",
        userId = "uid_test",
        displayName = "Luis Estudio",
        email = "estudio@audiodsp.pro",
        vipStatus = "free",
        vipPromoDismissed = false
    )

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
                                        contentDescription = "Logo Audio DSP Pro",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Audio DSP Pro",
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
                            Triple(AppSection.MUSIC_PLAYER, "Música", Icons.Default.LibraryMusic),
                            Triple(AppSection.AUDIO_LAB, "Lab DSP", Icons.Default.Equalizer),
                            Triple(AppSection.AUDIO_METERS, "Medidores", Icons.Default.GraphicEq),
                            Triple(AppSection.TUTORIALS, "Guías", Icons.AutoMirrored.Filled.MenuBook)
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
    fun capture_01_musica_panel() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.MUSIC_PLAYER) {
                PlayerView(
                    theme = theme,
                    tracks = sampleTracks,
                    artists = listOf(ArtistSummary("Estudio Session", 1, 1, null)),
                    albums = listOf(AlbumSummary("Sesiones Hi-Res", "Estudio Session", 1, null)),
                    queueTracks = sampleTracks,
                    playlists = listOf(MusicPlaylist("pl1", "Referencia Hi-Res", listOf("media_101"))),
                    selectedPlaylistId = null,
                    currentTrack = sampleTracks.first(),
                    isPlaying = true,
                    playbackSeconds = 68,
                    volumeLevel = 0.88f,
                    shuffleEnabled = true,
                    repeatMode = RepeatMode.ALL,
                    searchQuery = "",
                    libraryFilter = LibraryFilter.ALL,
                    hasAudioLibraryPermission = true,
                    isScanningLibrary = false,
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Headphones", OutputKind.BLUETOOTH),
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.72f,
                    midEnergy = 0.58f,
                    trebleEnergy = 0.64f,
                    liteVisuals = false,
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeek = {},
                    onVolumeChange = {},
                    onToggleShuffle = {},
                    onCycleRepeat = {},
                    onTrackSelect = {},
                    onToggleFavorite = {},
                    onRemoveTrack = {},
                    onAddToQueue = {},
                    onPlayNextInQueue = {},
                    onPlayQueueAt = {},
                    onMoveQueueUp = {},
                    onMoveQueueDown = {},
                    onRemoveFromQueue = {},
                    onClearQueue = {},
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
                    onRefreshLibrary = {},
                    onOpenAudioLab = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("studio_signal_indicator_bar").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/01_musica_panel.png")
    }

    @Test
    fun capture_02_audio_lab_view() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.AUDIO_LAB) {
                AudioLabView(
                    theme = theme,
                    dspSettings = studioDsp,
                    eqSettings = studioEq,
                    presets = EqPresetCatalog.presets,
                    customProfiles = emptyList(),
                    capabilityReport = capability,
                    isVipUnlocked = false,
                    cloudSyncActive = true,
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
                    onSelectPreset = {},
                    onDeletePreset = {},
                    onSaveSoundProfile = { _, _ -> },
                    onApplySoundProfile = {},
                    onDeleteSoundProfile = {},
                    onPreampChanged = {},
                    onMasterGainChanged = {},
                    onBassGainChanged = {},
                    onMidGainChanged = {},
                    onTrebleGainChanged = {},
                    onBassBoostChanged = { _, _, _ -> },
                    onSubBassChanged = { _, _, _ -> },
                    onSubsonicChanged = { _, _ -> },
                    onLoudnessChanged = { _, _ -> },
                    onBalanceChanged = {},
                    onStereoWidthChanged = {},
                    onToggleCompressor = {},
                    onCompressorParamsChanged = { _, _, _, _ -> },
                    onToggleLimiter = {},
                    onLimiterCeilingChanged = {},
                    onHpfChanged = { _, _, _ -> },
                    onLpfChanged = { _, _, _ -> },
                    onOpenVipZone = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("audio_lab_view").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/02_audio_lab.png")
    }

    @Test
    fun capture_03_audio_meters_view() {
        val activeRta = CarAudioEngine.createInitialRtaBands().mapIndexed { idx, band ->
            val lvl = -12f - (idx % 6) * 5f + (if (idx < 8) 5f else 0f)
            band.copy(levelDb = lvl.coerceIn(-55f, -2f), peakDb = (lvl + 3f).coerceIn(-52f, 0f))
        }
        val wave = FloatArray(128) { idx -> kotlin.math.sin(idx * 0.25f) * 0.65f }

        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.AUDIO_METERS) {
                AudioMetersView(
                    theme = theme,
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
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Headphones", OutputKind.BLUETOOTH),
                    powerTelemetry = DevicePowerTelemetry(
                        batteryPercent = 86,
                        batteryVoltageV = 4.18f,
                        batteryTempC = 30.8f,
                        batteryCurrentMa = -310f,
                        isCharging = false,
                        hasBatterySensor = true,
                        hasTempSensor = true,
                        hasCurrentSensor = true
                    ),
                    onToggleMicRta = {},
                    onRefreshTelemetry = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("audio_meters_view").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/03_audio_meters.png")
    }

    @Test
    fun capture_04_welcome_and_auth() {
        composeTestRule.mainClock.autoAdvance = false
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
        composeTestRule.mainClock.advanceTimeBy(120)
        composeTestRule.onNodeWithTag("audio_dsp_welcome_screen").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/04_welcome.png")
    }

    @Test
    fun capture_05_auth_screen() {
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                AuthScreen(
                    theme = theme,
                    isSigningIn = false,
                    errorMessage = null,
                    onGoogleSignIn = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("auth_screen").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/05_auth_screen.png")
    }

    @Test
    fun capture_05b_settings_pro() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.SETTINGS) {
                AppSettingsView(
                    theme = theme,
                    userProfile = sampleProfile,
                    cloudSyncActive = true,
                    isDeletingAccount = false,
                    autoPlayIntro = true,
                    onToggleAutoPlayIntro = {},
                    onReplayIntro = {},
                    onResetDspDefaults = {},
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Headphones", OutputKind.BLUETOOTH),
                    sessionTarget = AudioSessionTarget.INTERNAL_PLAYER,
                    capabilityReport = capability,
                    hasAudioPermission = true,
                    hasNotificationPermission = true,
                    faultAlerts = emptyList(),
                    onRequestNotificationPermission = {},
                    onRequestAudioPermission = {},
                    onRefreshDiagnostics = {},
                    onClearFaults = {},
                    onSignOut = {},
                    onConfirmDeleteAccount = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_settings_view").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/05_ajustes_pro.png")
    }

    @Test
    fun capture_06_home_dashboard_and_tutorials() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.HOME) {
                HomeDashboardView(
                    theme = theme,
                    userProfile = sampleProfile,
                    dspSettings = studioDsp,
                    equalizerSettings = studioEq,
                    capabilityReport = capability,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.72f,
                    midEnergy = 0.56f,
                    trebleEnergy = 0.60f,
                    currentTrack = sampleTracks.first(),
                    playbackSeconds = 72,
                    tracksCount = 24,
                    favoriteTracks = sampleTracks.take(1),
                    playlists = listOf(MusicPlaylist("pl1", "Referencia Hi-Res", listOf("media_101"))),
                    outputDeviceStatus = OutputDeviceStatus("BT: Studio Headphones", OutputKind.BLUETOOTH),
                    powerTelemetry = DevicePowerTelemetry(88, 4.21f, 30.5f, -240f, false, true, true, true),
                    isVipUnlocked = false,
                    vipPromoDismissed = false,
                    liteVisuals = false,
                    onDismissVipPromo = {},
                    onNavigateSection = {},
                    onTogglePlayPause = {},
                    onPlayTrack = {},
                    onSelectPlaylist = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_dashboard_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("one_time_vip_banner").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/06_home_dashboard.png")
    }

    @Test
    fun capture_07_tutorial_center() {
        composeTestRule.setContent {
            ScreenshotScaffold(activeSection = AppSection.TUTORIALS) {
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
