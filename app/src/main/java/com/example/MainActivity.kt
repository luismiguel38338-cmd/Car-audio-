package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CarAudioThemeType
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.CarAudioToolsView
import com.example.ui.components.DspProcessorView
import com.example.ui.components.EqualizerProcessorView
import com.example.ui.components.PermissionExplanationDialog
import com.example.ui.components.PlayerView
import com.example.ui.components.PlantaMonitorView
import com.example.ui.components.ThemeSelectorSheet
import com.example.ui.components.VisualizerView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.CarAudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CarAudioViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()

            CarAudioAppTheme(themeType = currentTheme) {
                CarAudioAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarAudioAppRoot(viewModel: CarAudioViewModel) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentTheme.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val dspSettings by viewModel.dspSettings.collectAsState()
    val ampTelemetry by viewModel.ampTelemetry.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackSeconds by viewModel.playbackSeconds.collectAsState()
    val splDb by viewModel.splDb.collectAsState()
    val peakSplDb by viewModel.peakSplDb.collectAsState()
    val leftVu by viewModel.leftVu.collectAsState()
    val rightVu by viewModel.rightVu.collectAsState()
    val rtaBands by viewModel.rtaBands.collectAsState()
    val waveform by viewModel.waveform.collectAsState()
    val isMicRta by viewModel.isMicRta.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val pendingPermissionType by viewModel.pendingPermissionType.collectAsState()
    val showSplashScreen by viewModel.showSplashScreen.collectAsState()
    val equalizerSettings by viewModel.equalizerSettings.collectAsState()
    val dspChannels by viewModel.dspChannels.collectAsState()
    val selectedChannelId by viewModel.selectedChannelId.collectAsState()
    val splRunState by viewModel.splRunState.collectAsState()

    // Notification permission launcher
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPermission = isGranted
        viewModel.dismissPermissionDialog()
        if (isGranted) {
            viewModel.sendStatusNotification()
        }
    }

    // Audio record permission launcher
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        viewModel.dismissPermissionDialog()
        if (isGranted) {
            viewModel.toggleMicRta(true)
        }
    }

    // Auto-prompt notification permission on first launch if not granted
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
            viewModel.requestNotificationPermission()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor),
        containerColor = currentTheme.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_car_audio_logo_brazil_1788873346889),
                            contentDescription = "Car Audio Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, currentTheme.primaryColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CarAudio DSP Pro",
                                color = currentTheme.textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentTheme.displayName,
                                color = currentTheme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Telemetry mini-badges (Volt & Clip)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (ampTelemetry.isClipping) {
                                Box(
                                    modifier = Modifier
                                        .background(currentTheme.clipAlertColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CLIP",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(currentTheme.cardColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, currentTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${"%.1f".format(ampTelemetry.voltage)}V",
                                    color = if (ampTelemetry.isLowVoltage) currentTheme.clipAlertColor else currentTheme.primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = { viewModel.openSplashScreen() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("open_splash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Animación y Sonido de Inicio",
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentTheme.surfaceColor
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = currentTheme.surfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.DSP_PROCESSOR,
                    onClick = { viewModel.selectTab(AppTab.DSP_PROCESSOR) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "DSP") },
                    label = { Text("DSP", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_dsp")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.EQUALIZER,
                    onClick = { viewModel.selectTab(AppTab.EQUALIZER) },
                    icon = { Icon(Icons.Default.Equalizer, contentDescription = "EQ Pro") },
                    label = { Text("EQ Pro", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_equalizer")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.RTA_ANALYZER,
                    onClick = { viewModel.selectTab(AppTab.RTA_ANALYZER) },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "RTA") },
                    label = { Text("RTA", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_rta")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.TOOLS,
                    onClick = { viewModel.selectTab(AppTab.TOOLS) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Cajones Brasil y Tools") },
                    label = { Text("Brasil", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_tools")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.PLANTA_MONITOR,
                    onClick = { viewModel.selectTab(AppTab.PLANTA_MONITOR) },
                    icon = { Icon(Icons.Default.ElectricalServices, contentDescription = "Planta") },
                    label = { Text("Planta", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_planta")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.PLAYER,
                    onClick = { viewModel.selectTab(AppTab.PLAYER) },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Player") },
                    label = { Text("Player", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_player")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.THEMES,
                    onClick = { viewModel.selectTab(AppTab.THEMES) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Temas") },
                    label = { Text("Temas", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_themes")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.DSP_PROCESSOR -> {
                    DspProcessorView(
                        theme = currentTheme,
                        dsp = dspSettings,
                        onDspChange = { viewModel.updateDspSettings(it) },
                        onSelectPreset = { viewModel.loadPreset(it) },
                        dspChannels = dspChannels,
                        selectedChannelId = selectedChannelId,
                        onSelectChannel = { viewModel.selectDspChannel(it) },
                        onUpdateChannelHpf = { chId, hz, slope, en -> viewModel.updateChannelHpf(chId, hz, slope, en) },
                        onUpdateChannelLpf = { chId, hz, slope, en -> viewModel.updateChannelLpf(chId, hz, slope, en) },
                        onUpdateChannelGain = { chId, gain -> viewModel.updateChannelGain(chId, gain) },
                        onToggleChannelPhase = { viewModel.toggleChannelPhase(it) },
                        onUpdateChannelDelay = { chId, delay -> viewModel.updateChannelDelay(chId, delay) },
                        onToggleChannelMute = { viewModel.toggleChannelMute(it) },
                        onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) }
                    )
                }
                AppTab.EQUALIZER -> {
                    EqualizerProcessorView(
                        theme = currentTheme,
                        eqSettings = equalizerSettings,
                        onTogglePower = { viewModel.toggleEqualizerPower(it) },
                        onUpdateMasterGain = { viewModel.updateEqualizerMasterGain(it) },
                        onToggleLimiter = { viewModel.toggleEqualizerLimiter(it) },
                        onUpdateBand = { idx, gain -> viewModel.updateEqualizerBand(idx, gain) },
                        onUpdateParametric = { f, g, q -> viewModel.updateEqualizerParametric(f, g, q) },
                        onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                        onResetFlat = { viewModel.resetEqualizerFlat() }
                    )
                }
                AppTab.RTA_ANALYZER -> {
                    VisualizerView(
                        theme = currentTheme,
                        splDb = splDb,
                        peakSplDb = peakSplDb,
                        leftVu = leftVu,
                        rightVu = rightVu,
                        rtaBands = rtaBands,
                        waveform = waveform,
                        isMicRta = isMicRta,
                        onToggleMic = {
                            viewModel.toggleMicRta(hasAudioPermission)
                        },
                        splRunState = splRunState,
                        onStartSplRun = { viewModel.startSplRun() },
                        onStopSplRun = { viewModel.stopSplRun() },
                        onClearSplHistory = { viewModel.clearSplHistory() }
                    )
                }
                AppTab.PLAYER -> {
                    PlayerView(
                        theme = currentTheme,
                        tracks = tracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        playbackSeconds = playbackSeconds,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() },
                        onPrevious = { viewModel.previousTrack() },
                        onSeek = { viewModel.seekTo(it) },
                        onTrackSelect = { viewModel.playTrack(it) },
                        onVolumeChange = { viewModel.setVolume(it) },
                        onCustomTrackLoaded = { uri, name ->
                            viewModel.addCustomUserTrack(uri, name)
                        }
                    )
                }
                AppTab.PLANTA_MONITOR -> {
                    PlantaMonitorView(
                        theme = currentTheme,
                        telemetry = ampTelemetry,
                        hasNotificationPermission = hasNotifPermission,
                        onRequestNotificationPermission = {
                            viewModel.requestNotificationPermission()
                        },
                        onTestClippingAlert = { viewModel.testClippingAlert() },
                        onTestVoltageAlert = { viewModel.testVoltageAlert() },
                        onSendStatusNotification = { viewModel.sendStatusNotification() }
                    )
                }
                AppTab.TOOLS -> {
                    CarAudioToolsView(
                        viewModel = viewModel,
                        currentTheme = currentTheme
                    )
                }
                AppTab.THEMES -> {
                    ThemeSelectorSheet(
                        currentTheme = currentTheme,
                        onSelectTheme = { viewModel.setTheme(it) }
                    )
                }
            }
        }
    }

    // Permission Explanation Dialog
    if (showPermissionDialog) {
        PermissionExplanationDialog(
            theme = currentTheme,
            permissionType = pendingPermissionType,
            onDismiss = { viewModel.dismissPermissionDialog() },
            onConfirm = {
                if (pendingPermissionType == "notification") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotifPermission = true
                        viewModel.dismissPermissionDialog()
                    }
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        )
    }

    // Animated Startup Splash Screen with Loading Bubble & Sound (Requested by user)
    if (showSplashScreen) {
        CarAudioSplashScreen(
            theme = currentTheme,
            onStartSound = { viewModel.playStartupSound() },
            onEnterApp = { viewModel.dismissSplashScreen() }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}


