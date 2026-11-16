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
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
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
import com.example.model.AppSection
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DevicePowerTelemetry
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.MusicPlaylist
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.PioneerIlluminationColor
import com.example.model.PioneerRadioStyle
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.model.VerifiedRadioCatalog
import com.example.ui.components.AppSettingsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.EqualizerProcessorView
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.PioneerRadioView
import com.example.ui.components.PlayerView
import com.example.ui.components.SoundToolsView
import com.example.ui.theme.CarAudioAppTheme
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

  private val theme = CarAudioThemeType.GRAPHITE_ELECTRIC_NEON

  private val sampleTracks = listOf(
    AudioTrackItem(
      id = "media_101",
      title = "Noche de Baile (Remix Bass)",
      artist = "DJ Electric Studio",
      album = "Música del Teléfono",
      durationSeconds = 214,
      category = TrackCategory.USER_CUSTOM,
      isFavorite = true
    ),
    AudioTrackItem(
      id = "media_102",
      title = "Subwoofer Deep 38Hz Mix",
      artist = "Audio Local",
      album = "Descargas",
      durationSeconds = 186,
      category = TrackCategory.USER_CUSTOM
    ),
    AudioTrackItem(
      id = "media_103",
      title = "Open Show Vocals HD",
      artist = "Carpeta Música",
      album = "Mi Celular",
      durationSeconds = 242,
      category = TrackCategory.USER_CUSTOM
    )
  )

  private val punchDsp = DspSettings(
    dspMasterEnabled = true,
    preampDb = 4.0f,
    bassGainDb = 5.5f,
    midGainDb = 1.0f,
    trebleGainDb = 3.5f,
    bassBoostEnabled = true,
    bassBoostDb = 12.0f,
    stereoWidthPercent = 145f,
    compressorEnabled = true,
    limiterEnabled = true,
    limiterCeilingDb = -1.0f
  )

  private val punchEq = EqualizerSettings(
    isEnabled = true,
    masterGainDb = 2.0f,
    bands15 = listOf(8f, 7f, 6f, 4f, 2f, 0f, -1f, 0f, 1f, 2f, 3f, 4f, 5f, 6f, 5f),
    bands31 = List(31) { idx ->
      when {
        idx <= 6 -> 8f - idx * 0.5f
        idx in 7..14 -> 2f - (idx - 7) * 0.4f
        idx in 15..22 -> (idx - 15) * 0.5f
        else -> 5.5f
      }
    },
    activePresetName = "PUNCH BASS"
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
                    .border(1.5.dp, theme.primaryColor, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.car_audio_dsp_icon_1794824449077),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "CAR AUDIO DSP PRO",
                    color = theme.textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "${activeSection.shortTitle} • Bluetooth Car Audio",
                    color = theme.accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(theme.cardColor)
                      .border(1.dp, theme.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PictureInPicture,
                      contentDescription = null,
                      tint = theme.primaryColor,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(theme.primaryColor.copy(alpha = 0.22f))
                      .border(1.dp, theme.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PowerSettingsNew,
                      contentDescription = null,
                      tint = theme.primaryColor,
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
              Triple(AppSection.HOME, "INICIO", Icons.Default.Home),
              Triple(AppSection.RADIO_PIONEER, "RADIO", Icons.Default.Radio),
              Triple(AppSection.MUSIC_PLAYER, "MÚSICA", Icons.Default.LibraryMusic),
              Triple(AppSection.EQUALIZER, "EQ", Icons.Default.Equalizer),
              Triple(AppSection.SOUND_TOOLS, "MENÚ", Icons.Default.Menu)
            ).forEach { (sec, label, icon) ->
              NavigationBarItem(
                selected = activeSection == sec,
                onClick = {},
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = theme.onPrimaryColor,
                  indicatorColor = theme.primaryColor,
                  selectedTextColor = theme.primaryColor,
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
          currentTrack = sampleTracks.first(),
          isPlaying = true,
          playbackSeconds = 68,
          volumeLevel = 0.88f,
          dspSettings = punchDsp,
          eqSettings = punchEq,
          hasAudioLibraryPermission = true,
          isScanningLibrary = false,
          onPlayPause = {},
          onNext = {},
          onPrevious = {},
          onSeek = {},
          onTrackSelect = {},
          onRemoveTrack = {},
          onVolumeChange = {},
          onCustomTracksLoaded = {},
          onRequestLibraryPermission = {},
          onRefreshLibrary = {},
          onOpenFloatingOverlayPermission = {},
          onToggleDspMaster = {},
          onBassBoostChange = {},
          onPreampChange = {},
          onQuickBandChange = { _, _ -> },
          onQuickPresetSelect = {},
          shuffleEnabled = true,
          repeatMode = RepeatMode.ALL,
          outputDeviceStatus = OutputDeviceStatus("Bluetooth Car Audio", OutputKind.BLUETOOTH),
          sessionTarget = AudioSessionTarget.INTERNAL_PLAYER,
          isAnyAudioPlaying = true,
          bassEnergy = 0.75f,
          midEnergy = 0.58f,
          trebleEnergy = 0.64f,
          playlists = listOf(MusicPlaylist("pl1", "Carretera Nocturna", listOf("media_101")))
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("five_led_indicator_bar").assertIsDisplayed()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/01_musica_panel.png")
  }

  @Test
  fun capture_02_ecualizador_pro() {
    composeTestRule.setContent {
      ScreenshotScaffold(activeSection = AppSection.EQUALIZER) {
        EqualizerProcessorView(
          theme = theme,
          eqSettings = punchEq,
          onTogglePower = {},
          onUpdateMasterGain = {},
          onUpdateBand = { _, _ -> },
          onUpdateBand31 = { _, _ -> },
          onUpdateParametricBand = { _, _, _, _, _, _ -> },
          onToggleParametricBand = { _, _ -> },
          onSelectPreset = {},
          onResetFlat = {},
          eqPresets = EqPresetCatalog.presets,
          isAnyAudioPlaying = true,
          bassEnergy = 0.72f,
          midEnergy = 0.60f,
          trebleEnergy = 0.55f
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/02_ecualizador_rojo.png")
  }

  @Test
  fun capture_03_dsp_pro() {
    val activeRta = CarAudioEngine.createInitialRtaBands().mapIndexed { idx, band ->
      val lvl = -10f - (idx % 6) * 5f + (if (idx < 8) 6f else 0f)
      band.copy(levelDb = lvl.coerceIn(-55f, -2f), peakDb = (lvl + 3f).coerceIn(-52f, 0f))
    }
    composeTestRule.setContent {
      ScreenshotScaffold(activeSection = AppSection.SOUND_TOOLS) {
        SoundToolsView(
          theme = theme,
          dspSettings = punchDsp,
          rtaBands = activeRta,
          leftVu = 0.78f,
          rightVu = 0.74f,
          isClipping = false,
          isAnyAudioPlaying = true,
          bassEnergy = 0.8f,
          midEnergy = 0.62f,
          trebleEnergy = 0.5f,
          isMicRta = false,
          liteVisuals = false,
          powerTelemetry = DevicePowerTelemetry(84, 4.18f, 31.2f, false, true, true),
          customProfiles = emptyList(),
          onRefreshPowerTelemetry = {},
          onToggleMicRta = {},
          onBassGainChanged = {},
          onMidGainChanged = {},
          onTrebleGainChanged = {},
          onPreampChanged = {},
          onMasterGainChanged = {},
          onHpfChanged = { _, _ -> },
          onLpfChanged = { _, _ -> },
          onLimiterToggle = {},
          onLimiterCeilingChanged = {},
          onSaveCustomProfile = { _, _ -> },
          onApplyCustomProfile = {},
          onDeleteCustomProfile = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/03_dsp_pro.png")
  }

  @Test
  fun capture_04_splash_logo() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      CarAudioAppTheme(themeType = theme) {
        CarAudioSplashScreen(
          theme = theme,
          onStartSound = {},
          onEnterApp = {},
          onSetDontShowAgain = {},
          initialDontShowAgain = false
        )
      }
    }
    composeTestRule.mainClock.advanceTimeBy(120)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/04_splash_logo.png")
  }

  @Test
  fun capture_05_ajustes_pro() {
    composeTestRule.setContent {
      ScreenshotScaffold(activeSection = AppSection.SETTINGS) {
        AppSettingsView(
          theme = theme,
          autoPlayIntro = true,
          onToggleAutoPlayIntro = {},
          onReplayIntro = {},
          keepScreenOn = true,
          onToggleKeepScreenOn = {},
          onSelectTheme = {},
          onResetDspDefaults = {},
          liteVisuals = false,
          onToggleLiteVisuals = {},
          isLowRamDevice = false,
          outputDeviceStatus = OutputDeviceStatus("Bluetooth Car Audio", OutputKind.BLUETOOTH),
          sessionTarget = AudioSessionTarget.INTERNAL_PLAYER
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/05_ajustes_pro.png")
  }

  @Test
  fun capture_06_home_dashboard() {
    composeTestRule.setContent {
      ScreenshotScaffold(activeSection = AppSection.HOME) {
        HomeDashboardView(
          theme = theme,
          dspSettings = punchDsp,
          isAnyAudioPlaying = true,
          bassEnergy = 0.78f,
          midEnergy = 0.56f,
          trebleEnergy = 0.62f,
          isRadioPlaying = false,
          activeRadioStation = VerifiedRadioCatalog.stations.first(),
          currentTrack = sampleTracks.first(),
          isMusicPlaying = true,
          outputDeviceStatus = OutputDeviceStatus("Bluetooth Car Audio", OutputKind.BLUETOOTH),
          powerTelemetry = DevicePowerTelemetry(88, 4.21f, 30.5f, true, true, true),
          isVipUnlocked = false,
          vipIntroDismissed = false,
          liteVisuals = false,
          onDismissVipIntro = {},
          onOpenVipDialog = {},
          onNavigateSection = {},
          onToggleActivePlayPause = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_dashboard_view").assertIsDisplayed()
    composeTestRule.onNodeWithTag("one_time_vip_banner").assertIsDisplayed()
  }

  @Test
  fun capture_07_pioneer_radio() {
    composeTestRule.setContent {
      ScreenshotScaffold(activeSection = AppSection.RADIO_PIONEER) {
        PioneerRadioView(
          theme = theme,
          isPowerOn = true,
          selectedStyle = PioneerRadioStyle.CLASSIC,
          illuminationColor = PioneerIlluminationColor.ELECTRIC_BLUE,
          stations = VerifiedRadioCatalog.stations,
          activeStation = VerifiedRadioCatalog.stations.first(),
          presetMemoryIds = List(6) { idx -> VerifiedRadioCatalog.stations[idx].id },
          isRadioPlaying = true,
          isRadioBuffering = false,
          radioErrorMessage = null,
          searchQuery = "",
          volumeLevel = 0.85f,
          bassEnergy = 0.7f,
          midEnergy = 0.55f,
          trebleEnergy = 0.5f,
          liteVisuals = false,
          onTogglePower = {},
          onSelectStyle = {},
          onSelectIllumination = {},
          onSelectStation = {},
          onTogglePlayPause = {},
          onNextStation = {},
          onPrevStation = {},
          onToggleFavoriteStation = {},
          onSelectPresetMemory = {},
          onAssignPresetMemory = {},
          onSearchQueryChange = {},
          onVolumeChange = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pioneer_radio_view").assertIsDisplayed()
  }
}
