package com.example

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.model.AudioTrackItem
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.DspCapabilityReport
import com.example.model.DspSettings
import com.example.model.LocalProfileStats
import com.example.model.MusicPlaylist
import com.example.model.OnlineSearchState
import com.example.model.OnlineTrackItem
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.ui.components.AboutView
import com.example.ui.components.BassBoostView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.DownloadsView
import com.example.ui.components.OnlineMusicView
import com.example.ui.components.PlaylistsView
import com.example.ui.components.ProfileView
import com.example.ui.components.SubwooferView
import com.example.ui.components.VisualizerView
import com.example.ui.theme.CarAudioAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarAudioProUiRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `all 8 new pro screens and splash screen render cleanly in neon dsp theme`() {
        val theme = CarAudioThemeType.NEON_DSP_PRO
        val sampleTrack = AudioTrackItem("t1", "Dembow Test", "LM Produce", "Cabina", 180, isFavorite = true)
        val sampleOnline = OnlineTrackItem(
            id = "o1",
            title = "Electric Neon",
            artist = "Jamendo CC",
            album = "Beats",
            durationSeconds = 210,
            artworkUrl = null,
            streamUrl = "https://example.com/o1.mp3",
            downloadUrl = "https://example.com/o1_dl.mp3",
            downloadAllowed = true,
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/"
        )
        val sampleDownload = DownloadItem(
            id = "dl_o1",
            trackId = "o1",
            title = "Electric Neon",
            artist = "Jamendo CC",
            album = "Beats",
            artworkUrl = null,
            remoteUrl = "https://example.com/o1_dl.mp3",
            localPath = "/tmp/o1.mp3",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            durationSeconds = 210,
            bytesDownloaded = 1024L,
            totalBytes = 2048L,
            status = DownloadStatus.PAUSED
        )
        val capability = DspCapabilityReport(
            audioSessionId = 10,
            dynamicsProcessingSupported = true,
            dynamicsProcessingActive = true,
            equalizerBandCount = 31,
            usingFallbackEqualizer = false,
            bassBoostSupported = true,
            bassBoostActive = false,
            virtualizerSupported = true,
            virtualizerActive = true,
            antiDoubleProcessingSafe = true,
            engineSummary = "DynamicsProcessing 31B Activo"
        )

        val screenStep = mutableIntStateOf(0)
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                when (screenStep.intValue) {
                    0 -> AboutView(theme = theme, capabilityReport = capability)
                    1 -> ProfileView(
                        theme = theme,
                        stats = LocalProfileStats(
                            displayName = "Cabina Pro",
                            totalPlays = 12,
                            totalListeningSeconds = 1800L,
                            customPresetsCount = 3,
                            playlistsCount = 2,
                            favoritesCount = 4,
                            downloadsCount = 1
                        ),
                        topTracks = listOf(sampleTrack),
                        favoriteTracks = listOf(sampleTrack),
                        playCounts = mapOf("t1" to 4),
                        onUpdateProfileName = {},
                        onPlayTrack = {}
                    )
                    2 -> PlaylistsView(
                        theme = theme,
                        playlists = listOf(MusicPlaylist("p1", "Car Bass", listOf("t1"))),
                        allTracks = listOf(sampleTrack),
                        onCreatePlaylist = {},
                        onDeletePlaylist = {},
                        onPlayPlaylist = {},
                        onAddTrackToPlaylist = { _, _ -> },
                        onOpenLibrary = {}
                    )
                    3 -> DownloadsView(
                        theme = theme,
                        downloads = listOf(sampleDownload),
                        storageUsedBytes = 1024L,
                        onPauseDownload = {},
                        onResumeDownload = {},
                        onCancelDownload = {},
                        onDeleteDownload = {},
                        onPlayDownloaded = {},
                        onClearAll = {},
                        onOpenOnlineMusic = {}
                    )
                    4 -> OnlineMusicView(
                        theme = theme,
                        onlineState = OnlineSearchState(tracks = listOf(sampleOnline)),
                        downloads = listOf(sampleDownload),
                        onOnlineQueryChange = {},
                        onSearchOnline = {},
                        onLoadFeatured = {},
                        onPlayOnlineTrack = {},
                        onDownloadTrack = {},
                        onOpenDownloads = {},
                        ytSearchQuery = "",
                        ytSavedItems = emptyList(),
                        ytSearchHistory = emptyList(),
                        onYtSearchQueryChange = {},
                        onYtRecordSearch = {},
                        onYtSaveShortcut = { _, _, _, _ -> },
                        onYtToggleFavorite = {},
                        onYtRemoveItem = {},
                        onYtClearHistory = {},
                        buildYtSearchUrl = { "https://music.youtube.com/search?q=$it" }
                    )
                    5 -> BassBoostView(
                        theme = theme,
                        dspSettings = DspSettings(),
                        bassMode = BassMode.POWERFUL,
                        isPlaying = true,
                        bassEnergy = 0.7f,
                        midEnergy = 0.5f,
                        trebleEnergy = 0.4f,
                        isClippingDetected = false,
                        liteVisuals = false,
                        onSelectBassMode = {},
                        onSetBassBoost = { _, _, _ -> },
                        onSetBassGain = {},
                        onSetDeepBass = { _, _, _ -> },
                        onToggleLimiter = {}
                    )
                    6 -> SubwooferView(
                        theme = theme,
                        dspSettings = DspSettings(),
                        isPlaying = true,
                        bassEnergy = 0.8f,
                        midEnergy = 0.5f,
                        trebleEnergy = 0.4f,
                        liteVisuals = false,
                        onSetSubBass = { _, _, _ -> },
                        onSetBassPunch = {},
                        onSetSubsonicFilter = { _, _ -> },
                        onSetFilterLpf = { _, _, _ -> }
                    )
                    7 -> VisualizerView(
                        theme = theme,
                        rtaBands = emptyList(),
                        oscilloscopeWave = FloatArray(64) { 0f },
                        leftVu = 0.6f,
                        rightVu = 0.6f,
                        rmsDb = -12f,
                        splDb = 80f,
                        isClippingDetected = false,
                        clipEventsCount = 0,
                        isMicRtaActive = false,
                        isAnyAudioPlaying = true,
                        bassEnergy = 0.65f,
                        midEnergy = 0.5f,
                        trebleEnergy = 0.4f,
                        dspMasterEnabled = true,
                        liteVisuals = true,
                        outputDeviceStatus = OutputDeviceStatus("BT Car", OutputKind.BLUETOOTH),
                        powerTelemetry = DevicePowerTelemetry(80, 4.1f, 31f, -300f, false, true, true, true),
                        onToggleLiteVisuals = {},
                        onToggleMicRta = {},
                        onRefreshTelemetry = {}
                    )
                    else -> CarAudioSplashScreen(
                        theme = theme,
                        onEnterApp = {},
                        onSetDontShowAgain = {}
                    )
                }
            }
        }

        val expectedTags = listOf(
            "about_view",
            "profile_view",
            "playlists_view",
            "downloads_view",
            "online_music_view",
            "bass_boost_view",
            "subwoofer_view",
            "visualizer_view",
            "audio_dsp_welcome_screen"
        )
        expectedTags.forEachIndexed { idx, tag ->
            composeTestRule.runOnUiThread { screenStep.intValue = idx }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }
    }
}
