package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppSection
import com.example.model.FaultActionType
import com.example.model.FaultSeverity
import com.example.service.FloatingEqualizerService
import com.example.ui.components.AppSettingsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.EqualizerProcessorView
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.PersonalizationView
import com.example.ui.components.PioneerRadioView
import com.example.ui.components.PlayerView
import com.example.ui.components.SoundToolsView
import com.example.ui.components.SpeakerPanelView
import com.example.ui.components.VipUnlockDialog
import com.example.ui.components.VipZoneView
import com.example.ui.theme.CarAudioAppTheme
import com.example.viewmodel.CarAudioViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CarAudioViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

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

private fun sectionIcon(section: AppSection): ImageVector = when (section) {
    AppSection.HOME -> Icons.Default.Home
    AppSection.RADIO_PIONEER -> Icons.Default.Radio
    AppSection.MUSIC_PLAYER -> Icons.Default.LibraryMusic
    AppSection.EQUALIZER -> Icons.Default.Equalizer
    AppSection.SPEAKERS -> Icons.Default.Speaker
    AppSection.SOUND_TOOLS -> Icons.Default.Build
    AppSection.PERSONALIZATION -> Icons.Default.Palette
    AppSection.VIP_ZONE -> Icons.Default.WorkspacePremium
    AppSection.SETTINGS -> Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarAudioAppRoot(viewModel: CarAudioViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val dspSettings by viewModel.dspSettings.collectAsStateWithLifecycle()
    val equalizerSettings by viewModel.equalizerSettings.collectAsStateWithLifecycle()
    val eqPresets by viewModel.eqPresets.collectAsStateWithLifecycle()
    val customSoundProfiles by viewModel.customSoundProfiles.collectAsStateWithLifecycle()

    // Music Player & Playlists
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackSeconds by viewModel.playbackSeconds.collectAsStateWithLifecycle()
    val volumeLevel by viewModel.volumeLevel.collectAsStateWithLifecycle()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val hasAudioLibraryPermission by viewModel.hasAudioLibraryPermission.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val isScanningLibrary by viewModel.isScanningLibrary.collectAsStateWithLifecycle()

    // Radio Pioneer
    val isRadioPowerOn by viewModel.isRadioPowerOn.collectAsStateWithLifecycle()
    val selectedPioneerStyle by viewModel.selectedPioneerStyle.collectAsStateWithLifecycle()
    val pioneerIlluminationColor by viewModel.pioneerIlluminationColor.collectAsStateWithLifecycle()
    val filteredRadioStations by viewModel.filteredRadioStations.collectAsStateWithLifecycle()
    val activeRadioStation by viewModel.activeRadioStation.collectAsStateWithLifecycle()
    val presetMemoryStationIds by viewModel.presetMemoryStationIds.collectAsStateWithLifecycle()
    val isRadioPlaying by viewModel.isRadioPlaying.collectAsStateWithLifecycle()
    val isRadioBuffering by viewModel.isRadioBuffering.collectAsStateWithLifecycle()
    val radioErrorMessage by viewModel.radioErrorMessage.collectAsStateWithLifecycle()
    val radioSearchQuery by viewModel.radioSearchQuery.collectAsStateWithLifecycle()

    // Live Telemetry & 5-LED Bar
    val leftVu by viewModel.leftVu.collectAsStateWithLifecycle()
    val rightVu by viewModel.rightVu.collectAsStateWithLifecycle()
    val bassEnergy by viewModel.bassEnergy.collectAsStateWithLifecycle()
    val midEnergy by viewModel.midEnergy.collectAsStateWithLifecycle()
    val trebleEnergy by viewModel.trebleEnergy.collectAsStateWithLifecycle()
    val isAnyAudioPlaying by viewModel.isAnyAudioPlaying.collectAsStateWithLifecycle()
    val rtaBands by viewModel.rtaBands.collectAsStateWithLifecycle()
    val isClippingDetected by viewModel.isClippingDetected.collectAsStateWithLifecycle()
    val isMicRta by viewModel.isMicRta.collectAsStateWithLifecycle()
    val devicePowerTelemetry by viewModel.devicePowerTelemetry.collectAsStateWithLifecycle()

    // App State, VIP & Diagnostics
    val showSplashScreen by viewModel.showSplashScreen.collectAsStateWithLifecycle()
    val autoPlayIntro by viewModel.autoPlayIntro.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val liteVisuals by viewModel.liteVisualsEnabled.collectAsStateWithLifecycle()
    val outputDeviceStatus by viewModel.outputDeviceStatus.collectAsStateWithLifecycle()
    val sessionTarget by viewModel.sessionTarget.collectAsStateWithLifecycle()
    val isVipUnlocked by viewModel.isVipUnlocked.collectAsStateWithLifecycle()
    val vipIntroDismissed by viewModel.vipIntroDismissed.collectAsStateWithLifecycle()
    val fxEngineStatus by viewModel.fxEngineStatus.collectAsStateWithLifecycle()
    val faultAlerts by viewModel.faultAlerts.collectAsStateWithLifecycle()
    val latestBannerAlert by viewModel.latestBannerAlert.collectAsStateWithLifecycle()

    // Pause FFT Visualizer and high-frequency UI telemetry when backgrounded
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setUiActive(true)
                Lifecycle.Event.ON_STOP -> viewModel.setUiActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val activity = context as? ComponentActivity
    LaunchedEffect(keepScreenOn) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onNotificationPermissionResult(isGranted)
    }

    val requestNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onNotificationPermissionResult(true)
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.toggleMicRta(isGranted)
    }

    val handleToggleMicRta: () -> Unit = {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            viewModel.toggleMicRta(true)
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(showSplashScreen) {
        if (!showSplashScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = drawerState.isOpen || selectedSection != AppSection.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.selectSection(AppSection.HOME)
        }
    }

    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }

    if (showVipDialog) {
        VipUnlockDialog(
            theme = currentTheme,
            onDismiss = { showVipDialog = false },
            onAttemptUnlock = { code -> viewModel.unlockVipWithCode(code) }
        )
    }

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
                    text = "Para que aparezca la burbuja azul eléctrico flotante al salir de la app, habilita el permiso 'Mostrar sobre otras aplicaciones'.",
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !showSplashScreen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = currentTheme.surfaceColor,
                drawerContentColor = currentTheme.textColor,
                modifier = Modifier
                    .width(296.dp)
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .testTag("main_navigation_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.car_audio_dsp_icon_1794824449077),
                            contentDescription = "Logo Car Audio DSP Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, currentTheme.primaryColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CAR AUDIO DSP PRO",
                                color = currentTheme.textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "9 Apartados • Estudio v6.5.0",
                                color = currentTheme.accentColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        color = currentTheme.primaryColor.copy(alpha = 0.28f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    AppSection.entries.forEachIndexed { idx, section ->
                        val selected = selectedSection == section
                        NavigationDrawerItem(
                            selected = selected,
                            onClick = {
                                viewModel.selectSection(section)
                                coroutineScope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = sectionIcon(section),
                                    contentDescription = section.title,
                                    tint = if (selected) currentTheme.onPrimaryColor else currentTheme.primaryColor
                                )
                            },
                            label = {
                                Column {
                                    Text(
                                        text = "${idx + 1}. ${section.title}",
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = section.subtitle,
                                        fontSize = 10.sp,
                                        color = if (selected) {
                                            currentTheme.onPrimaryColor.copy(alpha = 0.85f)
                                        } else {
                                            currentTheme.textSecondaryColor
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            badge = if (section == AppSection.VIP_ZONE) {
                                {
                                    Text(
                                        text = if (isVipUnlocked) "ON" else "PRO",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (selected) currentTheme.onPrimaryColor else currentTheme.accentColor
                                    )
                                }
                            } else null,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = currentTheme.primaryColor,
                                selectedTextColor = currentTheme.onPrimaryColor,
                                unselectedContainerColor = Color.Transparent,
                                unselectedTextColor = currentTheme.textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("drawer_section_${section.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = currentTheme.backgroundColor,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("top_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú de secciones",
                                tint = currentTheme.textColor
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.backgroundColor)
                                    .border(1.5.dp, currentTheme.primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.car_audio_dsp_icon_1794824449077),
                                    contentDescription = "Logo Car Audio DSP Pro",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CAR AUDIO DSP PRO",
                                    color = currentTheme.textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.4.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (dspSettings.dspMasterEnabled) {
                                        "${selectedSection.shortTitle} • ${outputDeviceStatus.label}"
                                    } else {
                                        "${selectedSection.shortTitle} • BYPASS"
                                    },
                                    color = if (dspSettings.dspMasterEnabled) currentTheme.accentColor else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isClippingDetected) {
                                    Box(
                                        modifier = Modifier
                                            .background(currentTheme.errorColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CLIP",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // VIP Status Pill / Zona VIP Trigger
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isVipUnlocked) currentTheme.accentColor else currentTheme.cardColor
                                        )
                                        .border(
                                            1.dp,
                                            if (isVipUnlocked) currentTheme.accentColor else currentTheme.primaryColor.copy(alpha = 0.7f),
                                            CircleShape
                                        )
                                        .clickable {
                                            viewModel.selectSection(AppSection.VIP_ZONE)
                                        }
                                        .testTag("top_vip_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVipUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                        contentDescription = if (isVipUnlocked) "Zona VIP Activa" else "Zona VIP",
                                        tint = if (isVipUnlocked) currentTheme.onPrimaryColor else currentTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Floating YouTube Equalizer Bubble Button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(6.dp)
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
                                        contentDescription = "Pantalla Virtual Flotante",
                                        tint = currentTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Master Power Quick Toggle
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(6.dp)
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
                val isDrawerSectionActive = selectedSection !in listOf(
                    AppSection.HOME,
                    AppSection.RADIO_PIONEER,
                    AppSection.MUSIC_PLAYER,
                    AppSection.EQUALIZER
                )
                NavigationBar(
                    containerColor = currentTheme.surfaceColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav"),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedSection == AppSection.HOME,
                        onClick = { viewModel.selectSection(AppSection.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("INICIO", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentTheme.onPrimaryColor,
                            indicatorColor = currentTheme.primaryColor,
                            selectedTextColor = currentTheme.primaryColor,
                            unselectedIconColor = currentTheme.textSecondaryColor,
                            unselectedTextColor = currentTheme.textSecondaryColor
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = selectedSection == AppSection.RADIO_PIONEER,
                        onClick = { viewModel.selectSection(AppSection.RADIO_PIONEER) },
                        icon = { Icon(Icons.Default.Radio, contentDescription = "Radio Pioneer") },
                        label = { Text("RADIO", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentTheme.onPrimaryColor,
                            indicatorColor = currentTheme.primaryColor,
                            selectedTextColor = currentTheme.primaryColor,
                            unselectedIconColor = currentTheme.textSecondaryColor,
                            unselectedTextColor = currentTheme.textSecondaryColor
                        ),
                        modifier = Modifier.testTag("nav_tab_radio")
                    )

                    NavigationBarItem(
                        selected = selectedSection == AppSection.MUSIC_PLAYER,
                        onClick = { viewModel.selectSection(AppSection.MUSIC_PLAYER) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Música") },
                        label = { Text("MÚSICA", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
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
                        selected = selectedSection == AppSection.EQUALIZER,
                        onClick = { viewModel.selectSection(AppSection.EQUALIZER) },
                        icon = { Icon(Icons.Default.Equalizer, contentDescription = "Ecualizador") },
                        label = { Text("EQ", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentTheme.onPrimaryColor,
                            indicatorColor = currentTheme.primaryColor,
                            selectedTextColor = currentTheme.primaryColor,
                            unselectedIconColor = currentTheme.textSecondaryColor,
                            unselectedTextColor = currentTheme.textSecondaryColor
                        ),
                        modifier = Modifier.testTag("nav_tab_eq")
                    )

                    NavigationBarItem(
                        selected = isDrawerSectionActive,
                        onClick = { coroutineScope.launch { drawerState.open() } },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Más secciones") },
                        label = { Text("MENÚ", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentTheme.onPrimaryColor,
                            indicatorColor = currentTheme.accentColor,
                            selectedTextColor = currentTheme.accentColor,
                            unselectedIconColor = currentTheme.textSecondaryColor,
                            unselectedTextColor = currentTheme.textSecondaryColor
                        ),
                        modifier = Modifier.testTag("nav_tab_menu")
                    )
                }
            }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                ) {
                    // Live Fault Alert Banner
                    AnimatedVisibility(
                        visible = latestBannerAlert != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        latestBannerAlert?.let { alert ->
                            val sevColor = when (alert.severity) {
                                FaultSeverity.ERROR -> currentTheme.errorColor
                                FaultSeverity.WARNING -> Color(0xFFFFB300)
                                FaultSeverity.INFO -> currentTheme.accentColor
                            }
                            val sevIcon = when (alert.severity) {
                                FaultSeverity.ERROR -> Icons.Default.Error
                                FaultSeverity.WARNING -> Icons.Default.Warning
                                FaultSeverity.INFO -> Icons.Default.Info
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("fault_alert_banner"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, sevColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = sevIcon,
                                        contentDescription = alert.title,
                                        tint = sevColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.title,
                                            color = currentTheme.textColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = alert.message,
                                            color = currentTheme.textSecondaryColor,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (alert.actionLabel != null && alert.actionType != FaultActionType.NONE) {
                                        TextButton(
                                            onClick = {
                                                when (alert.actionType) {
                                                    FaultActionType.ENABLE_LIMITER -> viewModel.toggleLimiter(true)
                                                    FaultActionType.REQUEST_AUDIO_PERMISSION -> requestAudioLibraryPermission()
                                                    FaultActionType.REQUEST_NOTIFICATION_PERMISSION -> requestNotificationPermission()
                                                    FaultActionType.RELOAD_LIBRARY -> viewModel.scanDeviceMusicLibrary()
                                                    FaultActionType.OPEN_DIAGNOSTICS -> viewModel.selectSection(AppSection.SETTINGS)
                                                    FaultActionType.NONE -> Unit
                                                }
                                                viewModel.dismissBannerAlert()
                                            }
                                        ) {
                                            Text(
                                                text = alert.actionLabel,
                                                color = currentTheme.primaryColor,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissBannerAlert() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar alerta",
                                            tint = currentTheme.textSecondaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Crossfade(
                        targetState = selectedSection,
                        animationSpec = tween(durationMillis = if (liteVisuals) 0 else 140),
                        label = "main_section_crossfade",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { section ->
                        when (section) {
                            AppSection.HOME -> {
                                HomeDashboardView(
                                    theme = currentTheme,
                                    dspSettings = dspSettings,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    isRadioPlaying = isRadioPlaying,
                                    activeRadioStation = activeRadioStation,
                                    currentTrack = currentTrack,
                                    isMusicPlaying = isPlaying,
                                    outputDeviceStatus = outputDeviceStatus,
                                    powerTelemetry = devicePowerTelemetry,
                                    isVipUnlocked = isVipUnlocked,
                                    vipIntroDismissed = vipIntroDismissed,
                                    liteVisuals = liteVisuals,
                                    onDismissVipIntro = { viewModel.dismissVipIntroPermanently() },
                                    onOpenVipDialog = { showVipDialog = true },
                                    onNavigateSection = { viewModel.selectSection(it) },
                                    onToggleActivePlayPause = {
                                        if (isRadioPlaying || (currentTrack == null && activeRadioStation != null)) {
                                            viewModel.toggleRadioPlayPause()
                                        } else {
                                            viewModel.togglePlayPause()
                                        }
                                    }
                                )
                            }

                            AppSection.RADIO_PIONEER -> {
                                PioneerRadioView(
                                    theme = currentTheme,
                                    isPowerOn = isRadioPowerOn,
                                    selectedStyle = selectedPioneerStyle,
                                    illuminationColor = pioneerIlluminationColor,
                                    stations = filteredRadioStations,
                                    activeStation = activeRadioStation,
                                    presetMemoryIds = presetMemoryStationIds,
                                    isRadioPlaying = isRadioPlaying,
                                    isRadioBuffering = isRadioBuffering,
                                    radioErrorMessage = radioErrorMessage,
                                    searchQuery = radioSearchQuery,
                                    volumeLevel = volumeLevel,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    liteVisuals = liteVisuals,
                                    onTogglePower = { viewModel.toggleRadioPower() },
                                    onSelectStyle = { viewModel.selectPioneerStyle(it) },
                                    onSelectIllumination = { viewModel.selectPioneerIllumination(it) },
                                    onSelectStation = { viewModel.playRadioStation(it) },
                                    onTogglePlayPause = { viewModel.toggleRadioPlayPause() },
                                    onNextStation = { viewModel.tuneNextStation() },
                                    onPrevStation = { viewModel.tunePrevStation() },
                                    onToggleFavoriteStation = { viewModel.toggleFavoriteStation(it) },
                                    onSelectPresetMemory = { viewModel.selectPresetMemory(it) },
                                    onAssignPresetMemory = { viewModel.assignPresetMemory(it) },
                                    onSearchQueryChange = { viewModel.setRadioSearchQuery(it) },
                                    onVolumeChange = { viewModel.setVolume(it) }
                                )
                            }

                            AppSection.MUSIC_PLAYER -> {
                                PlayerView(
                                    theme = currentTheme,
                                    tracks = filteredTracks,
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
                                    onQuickPresetSelect = { preset -> viewModel.applyQuickPreset(preset) },
                                    searchQuery = searchQuery,
                                    libraryFilter = libraryFilter,
                                    shuffleEnabled = shuffleEnabled,
                                    repeatMode = repeatMode,
                                    outputDeviceStatus = outputDeviceStatus,
                                    sessionTarget = sessionTarget,
                                    liteVisuals = liteVisuals,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onLibraryFilterChange = { viewModel.setLibraryFilter(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onToggleShuffle = { viewModel.toggleShuffle() },
                                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                                    isVipUnlocked = isVipUnlocked,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    playlists = playlists,
                                    selectedPlaylistId = selectedPlaylistId,
                                    onSelectPlaylist = { viewModel.selectPlaylistFilter(it) },
                                    onCreatePlaylist = { viewModel.createPlaylist(it) },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                    onAddTrackToPlaylist = { plId, trId -> viewModel.addTrackToPlaylist(plId, trId) },
                                    onRemoveTrackFromPlaylist = { plId, trId -> viewModel.removeTrackFromPlaylist(plId, trId) },
                                    onOpenEqSection = { viewModel.selectSection(AppSection.EQUALIZER) }
                                )
                            }

                            AppSection.EQUALIZER -> {
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
                                    onDeletePreset = { viewModel.deletePreset(it) },
                                    isVipUnlocked = isVipUnlocked,
                                    onRequestVipDialog = { showVipDialog = true },
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    liteVisuals = liteVisuals
                                )
                            }

                            AppSection.SPEAKERS -> {
                                SpeakerPanelView(
                                    theme = currentTheme,
                                    dspSettings = dspSettings,
                                    eqSettings = equalizerSettings,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    liteVisuals = liteVisuals,
                                    isVipUnlocked = isVipUnlocked,
                                    onBalancePanChanged = { viewModel.setBalancePan(it) },
                                    onFaderChanged = { viewModel.setFaderFrontRear(it) },
                                    onSpeakerConfigChanged = { sizeLabel, count ->
                                        viewModel.setSpeakerConfig(sizeLabel, count)
                                    },
                                    onTweeterConfigChanged = { sizeMm, mat, hpfHz, lvlDb ->
                                        viewModel.setTweeterConfig(sizeMm, mat, hpfHz, lvlDb)
                                    },
                                    onToggleBassBoost = { en, db, freq -> viewModel.setBassBoost(en, db, freq) },
                                    onSubBassChanged = { en, db, freq -> viewModel.setSubBass(en, db, freq) },
                                    onSubsonicChanged = { en, freq -> viewModel.setSubsonicFilter(en, freq) },
                                    onLpfChanged = { en, freq ->
                                        viewModel.setFilterLpf(en, freq, dspSettings.lpfSlopeDb, dspSettings.lpfQ)
                                    },
                                    onBassPunchChanged = { viewModel.setBassPunch(it) },
                                    onPhaseChanged = { viewModel.setBassPhase(it) },
                                    onTimeAlignmentChanged = { viewModel.setTimeAlignment(it) },
                                    onApplySubPreset = { viewModel.applySubwooferPreset(it) },
                                    onRequestVipDialog = { showVipDialog = true }
                                )
                            }

                            AppSection.SOUND_TOOLS -> {
                                SoundToolsView(
                                    theme = currentTheme,
                                    dspSettings = dspSettings,
                                    rtaBands = rtaBands,
                                    leftVu = leftVu,
                                    rightVu = rightVu,
                                    isClipping = isClippingDetected,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    isMicRta = isMicRta,
                                    liteVisuals = liteVisuals,
                                    powerTelemetry = devicePowerTelemetry,
                                    customProfiles = customSoundProfiles,
                                    onRefreshPowerTelemetry = { viewModel.refreshDevicePowerTelemetry() },
                                    onToggleMicRta = handleToggleMicRta,
                                    onBassGainChanged = { viewModel.setDspBassGain(it) },
                                    onMidGainChanged = { viewModel.setDspMidGain(it) },
                                    onTrebleGainChanged = { viewModel.setDspTrebleGain(it) },
                                    onPreampChanged = { viewModel.setDspPreampGain(it) },
                                    onMasterGainChanged = { viewModel.setMasterGain(it) },
                                    onHpfChanged = { en, freq ->
                                        viewModel.setFilterHpf(en, freq, dspSettings.hpfSlopeDb, dspSettings.hpfQ)
                                    },
                                    onLpfChanged = { en, freq ->
                                        viewModel.setFilterLpf(en, freq, dspSettings.lpfSlopeDb, dspSettings.lpfQ)
                                    },
                                    onLimiterToggle = { viewModel.toggleLimiter(it) },
                                    onLimiterCeilingChanged = { viewModel.setLimiterCeiling(it) },
                                    onSaveCustomProfile = { n, d -> viewModel.saveCustomSoundProfile(n, d) },
                                    onApplyCustomProfile = { viewModel.applyCustomSoundProfile(it) },
                                    onDeleteCustomProfile = { viewModel.deleteCustomSoundProfile(it) }
                                )
                            }

                            AppSection.PERSONALIZATION -> {
                                PersonalizationView(
                                    theme = currentTheme,
                                    isVipUnlocked = isVipUnlocked,
                                    selectedPioneerStyle = selectedPioneerStyle,
                                    pioneerIllumination = pioneerIlluminationColor,
                                    liteVisuals = liteVisuals,
                                    keepScreenOn = keepScreenOn,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    onSelectTheme = { viewModel.setTheme(it) },
                                    onSelectPioneerStyle = { viewModel.selectPioneerStyle(it) },
                                    onSelectPioneerIllumination = { viewModel.selectPioneerIllumination(it) },
                                    onToggleLiteVisuals = { viewModel.setLiteVisualsEnabled(it) },
                                    onToggleKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                                    onRequestVipDialog = { showVipDialog = true }
                                )
                            }

                            AppSection.VIP_ZONE -> {
                                VipZoneView(
                                    theme = currentTheme,
                                    dspSettings = dspSettings,
                                    isVipUnlocked = isVipUnlocked,
                                    isAnyAudioPlaying = isAnyAudioPlaying,
                                    bassEnergy = bassEnergy,
                                    midEnergy = midEnergy,
                                    trebleEnergy = trebleEnergy,
                                    liteVisuals = liteVisuals,
                                    onRequestVipDialog = { showVipDialog = true },
                                    onLockVip = { viewModel.lockVip() },
                                    onHiResChanged = { viewModel.setHiResQuality(it) },
                                    onTubeWarmthChanged = { viewModel.setTubeWarmth(it) },
                                    onPresenceChanged = { en, lvl -> viewModel.setPresence(en, lvl) },
                                    onClarityChanged = { en, lvl -> viewModel.setClarity(en, lvl) },
                                    onExciterChanged = { en, lvl -> viewModel.setExciter(en, lvl) }
                                )
                            }

                            AppSection.SETTINGS -> {
                                AppSettingsView(
                                    theme = currentTheme,
                                    autoPlayIntro = autoPlayIntro,
                                    onToggleAutoPlayIntro = { viewModel.setAutoPlayIntro(it) },
                                    onReplayIntro = { viewModel.openSplashScreen() },
                                    keepScreenOn = keepScreenOn,
                                    onToggleKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                                    onSelectTheme = { viewModel.setTheme(it) },
                                    onResetDspDefaults = { viewModel.resetDspToDefaults() },
                                    liteVisuals = liteVisuals,
                                    onToggleLiteVisuals = { viewModel.setLiteVisualsEnabled(it) },
                                    isLowRamDevice = viewModel.isLowRamDevice,
                                    outputDeviceStatus = outputDeviceStatus,
                                    sessionTarget = sessionTarget,
                                    isVipUnlocked = isVipUnlocked,
                                    onRequestVipDialog = { showVipDialog = true },
                                    onLockVip = { viewModel.lockVip() },
                                    fxEngineStatus = fxEngineStatus,
                                    hasAudioPermission = hasAudioLibraryPermission,
                                    hasNotificationPermission = hasNotificationPermission,
                                    faultAlerts = faultAlerts,
                                    onRequestNotificationPermission = requestNotificationPermission,
                                    onRequestAudioPermission = requestAudioLibraryPermission,
                                    onRefreshDiagnostics = { viewModel.refreshSystemHealth() },
                                    onClearFaults = { viewModel.clearFaultHistory() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSplashScreen) {
        CarAudioSplashScreen(
            theme = currentTheme,
            onStartSound = { viewModel.playStartupSound() },
            onEnterApp = { viewModel.dismissSplashScreen() },
            onSetDontShowAgain = { viewModel.setDontShowIntroAgain(it) },
            initialDontShowAgain = !autoPlayIntro,
            liteVisuals = liteVisuals
        )
    }
}
