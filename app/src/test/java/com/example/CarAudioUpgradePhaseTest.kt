package com.example

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.audio.CarAudioEngine
import com.example.data.LocalPreferencesStore
import com.example.model.AudioTrackItem
import com.example.model.BassMode
import com.example.model.CarAudioThemeType
import com.example.model.CloudUserProfile
import com.example.model.DevicePowerTelemetry
import com.example.model.DspCapabilityReport
import com.example.model.DspEngineStatus
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.LocalProfileStats
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.SplWeighting
import com.example.ui.components.AudioMetersView
import com.example.ui.components.DrivingModeView
import com.example.ui.components.ProfileView
import com.example.ui.components.SmartLightStatus
import com.example.ui.components.SmartStatusLedBadge
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.CarAudioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarAudioUpgradePhaseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("car_audio_dsp_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `enforceAntiClippingLimits protects extreme and high gain unless master dsp bypassed`() {
        val eqBoosted = EqualizerSettings(bands31 = List(31) { 9f })
        val unsafeDsp = DspSettings(
            dspMasterEnabled = true,
            preampDb = 6f,
            masterGainDb = 2f,
            bassBoostEnabled = true,
            bassBoostDb = 6f,
            limiterEnabled = false,
            limiterCeilingDb = 0f
        )

        val (safeExtreme, warnExtreme) = CarAudioViewModel.enforceAntiClippingLimits(
            dsp = unsafeDsp,
            eq = eqBoosted,
            bassMode = BassMode.EXTREME
        )
        assertTrue(safeExtreme.limiterEnabled)
        assertTrue(safeExtreme.limiterCeilingDb <= -0.5f)
        assertNotNull(warnExtreme)

        val (safePowerful, _) = CarAudioViewModel.enforceAntiClippingLimits(
            dsp = unsafeDsp,
            eq = EqualizerSettings(),
            bassMode = BassMode.POWERFUL
        )
        assertTrue(safePowerful.limiterEnabled)
        assertTrue(safePowerful.limiterCeilingDb <= -0.5f)

        val (safeHighTotal, warnHighTotal) = CarAudioViewModel.enforceAntiClippingLimits(
            dsp = unsafeDsp,
            eq = eqBoosted,
            bassMode = BassMode.NORMAL
        )
        assertTrue(safeHighTotal.limiterEnabled)
        assertNotNull(warnHighTotal)

        val bypassedDsp = unsafeDsp.copy(dspMasterEnabled = false, limiterEnabled = false)
        val (bypassedOut, bypassedWarn) = CarAudioViewModel.enforceAntiClippingLimits(
            dsp = bypassedDsp,
            eq = eqBoosted,
            bassMode = BassMode.EXTREME
        )
        assertFalse(bypassedOut.limiterEnabled)
        assertNull(bypassedWarn)
    }

    @Test
    fun `local preferences defaults and per output kind profiles and spl weighting persist`() {
        val store = LocalPreferencesStore(app)
        assertFalse(store.floatingEqOnLeave)
        assertFalse(store.drivingModeEnabled)
        assertTrue(store.autoProfileByOutput)
        assertEquals(SplWeighting.C_WEIGHTING, store.loadSplWeighting())

        store.floatingEqOnLeave = true
        store.drivingModeEnabled = true
        store.autoProfileByOutput = false
        store.saveSplWeighting(SplWeighting.A_WEIGHTING)

        val dspBt = DspSettings(bassBoostDb = 7.5f, bassGainDb = 4.0f, activePresetName = "Cabina BT")
        val eqBt = EqualizerSettings(
            bands10 = List(10) { if (it == 0) 5.0f else 0f },
            bands31 = List(31) { if (it < 4) 5.0f else 0f },
            activePresetName = "Cabina BT"
        )
        store.saveOutputKindProfile(OutputKind.BLUETOOTH, dspBt, eqBt)

        val reloaded = LocalPreferencesStore(app)
        assertTrue(reloaded.floatingEqOnLeave)
        assertTrue(reloaded.drivingModeEnabled)
        assertFalse(reloaded.autoProfileByOutput)
        assertEquals(SplWeighting.A_WEIGHTING, reloaded.loadSplWeighting())
        assertTrue(OutputKind.BLUETOOTH in reloaded.loadSavedOutputProfileKinds())

        val loadedBt = reloaded.loadOutputKindProfile(OutputKind.BLUETOOTH)
        assertNotNull(loadedBt)
        assertEquals(7.5f, loadedBt!!.first.bassBoostDb, 0.01f)
        assertEquals("Cabina BT", loadedBt.second.activePresetName)
        assertEquals(5.0f, loadedBt.second.bands10[0], 0.01f)

        reloaded.clearOutputKindProfile(OutputKind.BLUETOOTH)
        assertNull(reloaded.loadOutputKindProfile(OutputKind.BLUETOOTH))
        assertFalse(OutputKind.BLUETOOTH in reloaded.loadSavedOutputProfileKinds())
    }

    @Test
    fun `daylight studio light theme and effective band gain sanity`() {
        assertEquals(8, CarAudioThemeType.entries.size)
        assertTrue(CarAudioThemeType.DAYLIGHT_STUDIO.isLightTheme)
        assertFalse(CarAudioThemeType.NEON_DSP_PRO.isLightTheme)

        val dsp = DspSettings(
            dspMasterEnabled = true,
            bassGainDb = 3.0f,
            midGainDb = 0f,
            trebleGainDb = 2.0f,
            bassBoostEnabled = false,
            hpfEnabled = false,
            lpfEnabled = false
        )
        val eq = EqualizerSettings(
            isEnabled = true,
            bands31 = List(31) { 1.5f },
            masterGainDb = 0f
        )
        val lowGain = CarAudioEngine.computeEffectiveBandGainDb(63, dsp, eq)
        val highGain = CarAudioEngine.computeEffectiveBandGainDb(8000, dsp, eq)
        assertTrue("Expected positive low gain, got $lowGain", lowGain > 3.0f)
        assertTrue("Expected positive high gain, got $highGain", highGain > 2.0f)
    }

    @Test
    fun `driving mode view displays honest disclaimer and triggers transport and exit actions`() {
        var playPauseClicked = false
        var exitClicked = false
        val theme = CarAudioThemeType.DAYLIGHT_STUDIO

        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                DrivingModeView(
                    theme = theme,
                    currentTrack = AudioTrackItem("d1", "Prueba Cabina", "LM Produce", durationSeconds = 190),
                    isPlaying = true,
                    volumeLevel = 0.7f,
                    dspSettings = DspSettings(),
                    capabilityReport = DspCapabilityReport(engineStatus = DspEngineStatus.ACTIVE_INTERNAL),
                    outputDeviceStatus = OutputDeviceStatus("BT: Auto", OutputKind.BLUETOOTH),
                    quickPresets = EqPresetCatalog.presets.take(3),
                    activePresetName = "Estudio Equilibrado",
                    drivingKeepScreenOn = true,
                    onPlayPause = { playPauseClicked = true },
                    onPrevious = {},
                    onNext = {},
                    onVolumeChange = {},
                    onSelectPreset = {},
                    onToggleKeepScreenOn = {},
                    onExitDrivingMode = { exitClicked = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("driving_mode_disclaimer_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("No es Android Auto oficial", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("driving_play_pause_button").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("driving_exit_button").performScrollTo().assertIsDisplayed().performClick()

        assertTrue(playPauseClicked)
        assertTrue(exitClicked)
    }

    @Test
    fun `audio meters view labels spl as uncalibrated relative and opens help dialog`() {
        val theme = CarAudioThemeType.NEON_DSP_PRO
        var selectedWeighting = SplWeighting.C_WEIGHTING

        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                AudioMetersView(
                    theme = theme,
                    rtaBands = CarAudioEngine.createInitialRtaBands(),
                    oscilloscopeWave = FloatArray(64) { 0f },
                    leftVu = 0.5f,
                    rightVu = 0.5f,
                    rmsDb = -14.2f,
                    splDb = 72.4f,
                    isClippingDetected = false,
                    clipEventsCount = 0,
                    isMicRtaActive = false,
                    isAnyAudioPlaying = true,
                    bassEnergy = 0.6f,
                    midEnergy = 0.4f,
                    trebleEnergy = 0.3f,
                    liteVisuals = true,
                    outputDeviceStatus = OutputDeviceStatus(),
                    powerTelemetry = DevicePowerTelemetry(),
                    onToggleMicRta = {},
                    onRefreshTelemetry = {},
                    splWeighting = selectedWeighting,
                    onSelectSplWeighting = { selectedWeighting = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("SPL Relativo (No calibrado)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("meters_battery_telemetry_card").performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("meters_spl_weighting_dba").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(SplWeighting.A_WEIGHTING, selectedWeighting)

        composeTestRule.onNodeWithTag("meters_spl_help_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("micrófonos MEMS", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Entendido").assertIsDisplayed()
    }

    @Test
    fun `profile view shows optional google sign in when signed out and delete dialog when signed in`() {
        val theme = CarAudioThemeType.NEON_DSP_PRO
        val stats = LocalProfileStats(displayName = "Cabina Demo")

        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                ProfileView(
                    theme = theme,
                    stats = stats,
                    topTracks = emptyList(),
                    favoriteTracks = emptyList(),
                    playCounts = emptyMap(),
                    onUpdateProfileName = {},
                    onPlayTrack = {},
                    userProfile = null
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_google_signin_button").assertIsDisplayed()
    }

    @Test
    fun `profile view signed in exposes sign out and account deletion confirmation`() {
        val theme = CarAudioThemeType.NEON_DSP_PRO
        val stats = LocalProfileStats(displayName = "Cabina Demo")
        var deleteConfirmed = false

        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                ProfileView(
                    theme = theme,
                    stats = stats,
                    topTracks = emptyList(),
                    favoriteTracks = emptyList(),
                    playCounts = emptyMap(),
                    onUpdateProfileName = {},
                    onPlayTrack = {},
                    userProfile = CloudUserProfile(
                        id = "uid_test",
                        userId = "uid_test",
                        displayName = "Luis Miguel",
                        email = "cabina@example.com"
                    ),
                    cloudSyncActive = true,
                    onDeleteAccountAndCloudData = { onOk, _ ->
                        deleteConfirmed = true
                        onOk()
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_signout_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_delete_account_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_confirm_delete_account_button").assertIsDisplayed().performClick()
        assertTrue(deleteConfirmed)
    }

    @Test
    fun `smart status led badge renders all status modes cleanly on compact 320dp width`() {
        val theme = CarAudioThemeType.NEON_DSP_PRO
        composeTestRule.setContent {
            CarAudioAppTheme(themeType = theme) {
                Box(modifier = Modifier.width(320.dp)) {
                    androidx.compose.foundation.layout.Column {
                        SmartStatusLedBadge(SmartLightStatus.ACTIVE_READY, "DIRECTO", theme, liteVisuals = true)
                        SmartStatusLedBadge(SmartLightStatus.WARNING_LIMIT, "COMPATIBLE", theme, liteVisuals = true)
                        SmartStatusLedBadge(SmartLightStatus.DANGER_CLIP, "CLIPPING", theme, liteVisuals = true)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("DIRECTO").assertIsDisplayed()
        composeTestRule.onNodeWithText("COMPATIBLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("CLIPPING").assertIsDisplayed()
    }
}
