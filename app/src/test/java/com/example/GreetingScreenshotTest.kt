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
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudioEngine
import com.example.model.AudioSessionTarget
import com.example.model.AudioTrackItem
import com.example.model.CarAudioThemeType
import com.example.model.DspSettings
import com.example.model.EqPresetCatalog
import com.example.model.EqualizerSettings
import com.example.model.OutputDeviceStatus
import com.example.model.OutputKind
import com.example.model.RepeatMode
import com.example.model.TrackCategory
import com.example.ui.components.AppSettingsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.DspMasterView
import com.example.ui.components.EqualizerProcessorView
import com.example.ui.components.PlayerView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.AppTab
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

  private val theme = CarAudioThemeType.CRIMSON_RED

  private val sampleTracks = listOf(
    AudioTrackItem(
      id = "media_101",
      title = "Noche de Baile (Remix Bass)",
      artist = "DJ Studio Red",
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
    activeTab: AppTab,
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
                    painter = painterResource(id = R.drawable.crimson_dsp_logo_1794742268901),
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
                    text = "v6.0 • Bluetooth Car Audio",
                    color = theme.primaryColor,
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
              Triple(AppTab.PLAYER, "MÚSICA", Icons.Default.LibraryMusic),
              Triple(AppTab.BASS, "BAJOS", Icons.Default.Speaker),
              Triple(AppTab.EQ, "EQ", Icons.Default.Equalizer),
              Triple(AppTab.DSP, "DSP", Icons.Default.Tune),
              Triple(AppTab.AJUSTES, "AJUSTES", Icons.Default.Settings)
            ).forEach { (tab, label, icon) ->
              NavigationBarItem(
                selected = activeTab == tab,
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
      ScreenshotScaffold(activeTab = AppTab.PLAYER) {
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
          sessionTarget = AudioSessionTarget.INTERNAL_PLAYER
        )
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/01_musica_panel.png")
  }

  @Test
  fun capture_02_ecualizador_rojo() {
    composeTestRule.setContent {
      ScreenshotScaffold(activeTab = AppTab.EQ) {
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
          eqPresets = EqPresetCatalog.presets
        )
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/02_ecualizador_rojo.png")
  }

  @Test
  fun capture_03_dsp_pro() {
    val activeRta = CarAudioEngine.createInitialRtaBands().mapIndexed { idx, band ->
      val lvl = -10f - (idx % 6) * 5f + (if (idx < 8) 6f else 0f)
      band.copy(levelDb = lvl.coerceIn(-55f, -2f), peakDb = (lvl + 3f).coerceIn(-52f, 0f))
    }
    composeTestRule.setContent {
      ScreenshotScaffold(activeTab = AppTab.DSP) {
        DspMasterView(
          dspSettings = punchDsp,
          rtaBands = activeRta,
          theme = theme,
          leftVu = 0.78f,
          rightVu = 0.74f,
          isClipping = false,
          onToggleDspMaster = {},
          onPreampChanged = {},
          onBassGainChanged = {},
          onMidGainChanged = {},
          onTrebleGainChanged = {},
          onMasterGainChanged = {},
          onBassBoostChanged = { _, _ -> },
          onStereoWidthChanged = {},
          onBalancePanChanged = {},
          onCompressorToggle = {},
          onLimiterToggle = {},
          onLimiterCeilingChanged = {},
          onHpfChanged = { _, _ -> },
          onLpfChanged = { _, _ -> }
        )
      }
    }
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
      ScreenshotScaffold(activeTab = AppTab.AJUSTES) {
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
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/05_ajustes_pro.png")
  }
}
