package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.FloatingEqualizerService
import com.example.ui.components.AppSettingsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.DspMasterView
import com.example.ui.components.EqualizerProcessorView
import com.example.ui.components.PlayerView
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        try {
            if (Settings.canDrawOverlays(this)) {
                FloatingEqualizerService.start(this)
            }
        } catch (_: Throwable) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarAudioAppRoot(viewModel: CarAudioViewModel) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentTheme.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val dspSettings by viewModel.dspSettings.collectAsState()
    val equalizerSettings by viewModel.equalizerSettings.collectAsState()
    val eqPresets by viewModel.eqPresets.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackSeconds by viewModel.playbackSeconds.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()
    val hasAudioLibraryPermission by viewModel.hasAudioLibraryPermission.collectAsState()
    val isScanningLibrary by viewModel.isScanningLibrary.collectAsState()
    val leftVu by viewModel.leftVu.collectAsState()
    val rightVu by viewModel.rightVu.collectAsState()
    val rtaBands by viewModel.rtaBands.collectAsState()
    val isClippingDetected by viewModel.isClippingDetected.collectAsState()
    val showSplashScreen by viewModel.showSplashScreen.collectAsState()
    val autoPlayIntro by viewModel.autoPlayIntro.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()

    val activity = context as? ComponentActivity
    LaunchedEffect(keepScreenOn) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Load local music library on startup if permission is already granted
    LaunchedEffect(Unit) {
        viewModel.scanDeviceMusicLibrary()
    }

    val audioStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onAudioStoragePermissionResult(isGranted)
    }

    val requestAudioLibraryPermission: () -> Unit = {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        audioStoragePermissionLauncher.launch(perm)
    }

    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = {
                Text(
                    text = "Pantalla Virtual Flotante (YouTube)",
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textColor
                )
            },
            text = {
                Text(
                    text = "Para que aparezca el círculo rojo abierto flotante y puedas ecualizar YouTube mientras ves videos fuera de la app, habilita el permiso 'Mostrar sobre otras aplicaciones'.",
                    color = currentTheme.textSecondaryColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Text("ACTIVAR", color = currentTheme.primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("CANCELAR", color = currentTheme.textSecondaryColor)
                }
            },
            containerColor = currentTheme.cardColor
        )
    }

    Scaffold(
        containerColor = currentTheme.backgroundColor,
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
                                .background(currentTheme.backgroundColor)
                                .border(1.5.dp, currentTheme.primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.crimson_dsp_logo_1794742268901),
                                contentDescription = "Logo Car Audio DSP Pro",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CAR AUDIO DSP PRO",
                                color = currentTheme.textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (dspSettings.dspMasterEnabled) {
                                    "TODO ROJO EN VIVO • YouTube & Música"
                                } else {
                                    "DSP EN BYPASS"
                                },
                                color = if (dspSettings.dspMasterEnabled) currentTheme.primaryColor else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isClippingDetected) {
                                Box(
                                    modifier = Modifier
                                        .background(currentTheme.primaryColor, RoundedCornerShape(4.dp))
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

                            // Floating YouTube Equalizer Bubble Button (Crimson Red)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.cardColor)
                                    .border(1.dp, currentTheme.primaryColor, CircleShape)
                                    .clickable {
                                        if (Settings.canDrawOverlays(context)) {
                                            FloatingEqualizerService.start(context)
                                        } else {
                                            showOverlayPermissionDialog = true
                                        }
                                    }
                                    .testTag("top_floating_overlay_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "Pantalla Virtual Flotante YouTube",
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Master Power Quick Toggle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (dspSettings.dspMasterEnabled) {
                                            currentTheme.primaryColor.copy(alpha = 0.22f)
                                        } else {
                                            currentTheme.cardColor
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (dspSettings.dspMasterEnabled) currentTheme.primaryColor else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable { viewModel.toggleDspPower(!dspSettings.dspMasterEnabled) }
                                    .testTag("top_master_power_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "DSP Master Power",
                                    tint = if (dspSettings.dspMasterEnabled) currentTheme.primaryColor else Color.Gray,
                                    modifier = Modifier.size(18.dp)
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
                // 1. MÚSICA (Reproductor + Carga de Archivos + Modificador en Vivo en el Mismo Panel)
                NavigationBarItem(
                    selected = selectedTab == AppTab.PLAYER,
                    onClick = { viewModel.selectTab(AppTab.PLAYER) },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Música") },
                    label = { Text("MÚSICA", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_player")
                )

                // 2. EQ (Ecualizador 31 / 15 Bandas / Paramétrico + Presets)
                NavigationBarItem(
                    selected = selectedTab == AppTab.EQ,
                    onClick = { viewModel.selectTab(AppTab.EQ) },
                    icon = { Icon(Icons.Default.Equalizer, contentDescription = "EQ") },
                    label = { Text("EQ", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_eq")
                )

                // 3. DSP (Procesador Master, Bass Boost, Limitador, Cortes)
                NavigationBarItem(
                    selected = selectedTab == AppTab.DSP,
                    onClick = { viewModel.selectTab(AppTab.DSP) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "DSP") },
                    label = { Text("DSP", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_dsp")
                )

                // 4. AJUSTES (Flotante YouTube, Intro, Pantalla, Tema)
                NavigationBarItem(
                    selected = selectedTab == AppTab.AJUSTES,
                    onClick = { viewModel.selectTab(AppTab.AJUSTES) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("AJUSTES", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = currentTheme.onPrimaryColor,
                        indicatorColor = currentTheme.primaryColor,
                        selectedTextColor = currentTheme.primaryColor,
                        unselectedIconColor = currentTheme.textSecondaryColor,
                        unselectedTextColor = currentTheme.textSecondaryColor
                    ),
                    modifier = Modifier.testTag("nav_tab_ajustes")
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
                AppTab.PLAYER -> {
                    PlayerView(
                        theme = currentTheme,
                        tracks = tracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        playbackSeconds = playbackSeconds,
                        volumeLevel = volumeLevel,
                        dspSettings = dspSettings,
                        eqSettings = equalizerSettings,
                        hasAudioLibraryPermission = hasAudioLibraryPermission,
                        isScanningLibrary = isScanningLibrary,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() },
                        onPrevious = { viewModel.previousTrack() },
                        onSeek = { viewModel.seekTo(it) },
                        onTrackSelect = { viewModel.playTrack(it) },
                        onRemoveTrack = { viewModel.removeTrack(it) },
                        onVolumeChange = { viewModel.setVolume(it) },
                        onCustomTracksLoaded = { uris -> viewModel.addCustomUserTracks(uris) },
                        onRequestLibraryPermission = requestAudioLibraryPermission,
                        onRefreshLibrary = { viewModel.scanDeviceMusicLibrary() },
                        onOpenFloatingOverlayPermission = { showOverlayPermissionDialog = true },
                        onToggleDspMaster = { viewModel.toggleDspPower(it) },
                        onBassBoostChange = { db -> viewModel.setBassBoost(db > 0.5f, db) },
                        onPreampChange = { db -> viewModel.setDspPreampGain(db) },
                        onQuickBandChange = { idx, gain -> viewModel.setQuickBandGain(idx, gain) },
                        onQuickPresetSelect = { preset -> viewModel.applyQuickPreset(preset) }
                    )
                }

                AppTab.EQ -> {
                    EqualizerProcessorView(
                        theme = currentTheme,
                        eqSettings = equalizerSettings,
                        onTogglePower = { viewModel.toggleEqualizerPower(it) },
                        onUpdateMasterGain = { viewModel.updateEqualizerMasterGain(it) },
                        onUpdateBand = { idx, gain -> viewModel.updateEqualizerBand(idx, gain) },
                        onUpdateBand31 = { idx, gain -> viewModel.updateEqualizerBand31(idx, gain) },
                        onUpdateParametricBand = { id, f, g, q, type, en ->
                            viewModel.updateParametricBand(id, f, g, q, type, en)
                        },
                        onToggleParametricBand = { id, en -> viewModel.toggleParametricBand(id, en) },
                        onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                        onResetFlat = { viewModel.resetEqualizerFlat() },
                        eqPresets = eqPresets,
                        onSelectPresetItem = { viewModel.selectPreset(it) },
                        onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) },
                        onDeletePreset = { viewModel.deletePreset(it) }
                    )
                }

                AppTab.DSP -> {
                    DspMasterView(
                        dspSettings = dspSettings,
                        rtaBands = rtaBands,
                        theme = currentTheme,
                        leftVu = leftVu,
                        rightVu = rightVu,
                        isClipping = isClippingDetected,
                        onToggleDspMaster = { viewModel.toggleDspMaster() },
                        onPreampChanged = { viewModel.setDspPreampGain(it) },
                        onBassGainChanged = { viewModel.setDspBassGain(it) },
                        onMidGainChanged = { viewModel.setDspMidGain(it) },
                        onTrebleGainChanged = { viewModel.setDspTrebleGain(it) },
                        onMasterGainChanged = { viewModel.setMasterGain(it) },
                        onBassBoostChanged = { en, db -> viewModel.setBassBoost(en, db) },
                        onStereoWidthChanged = { viewModel.setStereoWidth(it) },
                        onBalancePanChanged = { viewModel.setBalancePan(it) },
                        onCompressorToggle = { viewModel.toggleCompressor(it) },
                        onLimiterToggle = { viewModel.toggleLimiter(it) },
                        onLimiterCeilingChanged = { viewModel.setLimiterCeiling(it) },
                        onHpfChanged = { en, freq ->
                            viewModel.setFilterHpf(en, freq, dspSettings.hpfSlopeDb, dspSettings.hpfQ)
                        },
                        onLpfChanged = { en, freq ->
                            viewModel.setFilterLpf(en, freq, dspSettings.lpfSlopeDb, dspSettings.lpfQ)
                        }
                    )
                }

                AppTab.AJUSTES -> {
                    AppSettingsView(
                        theme = currentTheme,
                        autoPlayIntro = autoPlayIntro,
                        onToggleAutoPlayIntro = { viewModel.setAutoPlayIntro(it) },
                        onReplayIntro = { viewModel.openSplashScreen() },
                        keepScreenOn = keepScreenOn,
                        onToggleKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                        onSelectTheme = { viewModel.setTheme(it) },
                        onResetDspDefaults = { viewModel.resetDspToDefaults() }
                    )
                }
            }
        }
    }

    if (showSplashScreen) {
        CarAudioSplashScreen(
            theme = currentTheme,
            onStartSound = { viewModel.playStartupSound() },
            onEnterApp = { viewModel.dismissSplashScreen() },
            onSetDontShowAgain = { viewModel.setAutoPlayIntro(!it) },
            initialDontShowAgain = !autoPlayIntro
        )
    }
}
