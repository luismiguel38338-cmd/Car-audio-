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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.AppSection
import com.example.model.FaultActionType
import com.example.model.FaultSeverity
import com.example.service.FloatingEqualizerService
import com.example.ui.components.AppSettingsView
import com.example.ui.components.AudioToolsView
import com.example.ui.components.CarAudioSplashScreen
import com.example.ui.components.EqualizerView
import com.example.ui.components.FavoritesView
import com.example.ui.components.HomeDashboardView
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.MyMusicView
import com.example.ui.components.PlayerView
import com.example.ui.components.PresetsView
import com.example.ui.components.YouTubeMusicView
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
    AppSection.MY_MUSIC -> Icons.Default.LibraryMusic
    AppSection.PLAYER -> Icons.Default.PlayCircle
    AppSection.EQUALIZER -> Icons.Default.Equalizer
    AppSection.YOUTUBE_MUSIC -> Icons.Default.MusicNote
    AppSection.AUDIO_TOOLS -> Icons.Default.Tune
    AppSection.PRESETS -> Icons.Default.GraphicEq
    AppSection.FAVORITES -> Icons.Default.Favorite
    AppSection.SETTINGS -> Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarAudioAppRoot(
    viewModel: CarAudioViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()

    val dspSettings by viewModel.dspSettings.collectAsStateWithLifecycle()
    val equalizerSettings by viewModel.equalizerSettings.collectAsStateWithLifecycle()
    val eqPresets by viewModel.eqPresets.collectAsStateWithLifecycle()
    val customSoundProfiles by viewModel.customSoundProfiles.collectAsStateWithLifecycle()
    val capabilityReport by viewModel.capabilityReport.collectAsStateWithLifecycle()

    // Live A/B Comparison
    val abSlotA by viewModel.abSlotA.collectAsStateWithLifecycle()
    val abSlotB by viewModel.abSlotB.collectAsStateWithLifecycle()
    val activeAbSlot by viewModel.activeAbSlot.collectAsStateWithLifecycle()

    // YouTube Music Integration
    val ytSearchQuery by viewModel.ytSearchQuery.collectAsStateWithLifecycle()
    val ytSavedItems by viewModel.ytSavedItems.collectAsStateWithLifecycle()
    val ytSearchHistory by viewModel.ytSearchHistory.collectAsStateWithLifecycle()

    // Music Player & Library
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val recentTracks by viewModel.recentTracks.collectAsStateWithLifecycle()
    val mostPlayedTracks by viewModel.mostPlayedTracks.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val queueTracks by viewModel.queueTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle()
    val librarySortOrder by viewModel.librarySortOrder.collectAsStateWithLifecycle()
    val trackPlayCounts by viewModel.trackPlayCounts.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackSeconds by viewModel.playbackSeconds.collectAsStateWithLifecycle()
    val volumeLevel by viewModel.volumeLevel.collectAsStateWithLifecycle()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val playbackPitch by viewModel.playbackPitch.collectAsStateWithLifecycle()
    val sleepTimerRemainingSec by viewModel.sleepTimerRemainingSec.collectAsStateWithLifecycle()
    val sleepTimerStopAfterTrack by viewModel.sleepTimerStopAfterTrack.collectAsStateWithLifecycle()
    val hasAudioLibraryPermission by viewModel.hasAudioLibraryPermission.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val isScanningLibrary by viewModel.isScanningLibrary.collectAsStateWithLifecycle()

    // Meters & Live Telemetry
    val leftVu by viewModel.leftVu.collectAsStateWithLifecycle()
    val rightVu by viewModel.rightVu.collectAsStateWithLifecycle()
    val bassEnergy by viewModel.bassEnergy.collectAsStateWithLifecycle()
    val midEnergy by viewModel.midEnergy.collectAsStateWithLifecycle()
    val trebleEnergy by viewModel.trebleEnergy.collectAsStateWithLifecycle()
    val isAnyAudioPlaying by viewModel.isAnyAudioPlaying.collectAsStateWithLifecycle()
    val rtaBands by viewModel.rtaBands.collectAsStateWithLifecycle()
    val oscilloscopeWave by viewModel.oscilloscopeWave.collectAsStateWithLifecycle()
    val rmsDb by viewModel.rmsDb.collectAsStateWithLifecycle()
    val splDb by viewModel.splDb.collectAsStateWithLifecycle()
    val isClippingDetected by viewModel.isClippingDetected.collectAsStateWithLifecycle()
    val clipEventsCount by viewModel.clipEventsCount.collectAsStateWithLifecycle()
    val isMicRta by viewModel.isMicRta.collectAsStateWithLifecycle()
    val devicePowerTelemetry by viewModel.devicePowerTelemetry.collectAsStateWithLifecycle()

    // App State & Diagnostics
    val showSplashScreen by viewModel.showSplashScreen.collectAsStateWithLifecycle()
    val autoPlayIntro by viewModel.autoPlayIntro.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val liteVisuals by viewModel.liteVisualsEnabled.collectAsStateWithLifecycle()
    val outputDeviceStatus by viewModel.outputDeviceStatus.collectAsStateWithLifecycle()
    val sessionTarget by viewModel.sessionTarget.collectAsStateWithLifecycle()
    val faultAlerts by viewModel.faultAlerts.collectAsStateWithLifecycle()
    val latestBannerAlert by viewModel.latestBannerAlert.collectAsStateWithLifecycle()

    val navigateToSection: (AppSection) -> Unit = { target ->
        viewModel.selectSection(target)
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute != target.route) {
            navController.navigate(target.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        val route = navBackStackEntry?.destination?.route ?: return@LaunchedEffect
        val matched = AppSection.entries.firstOrNull { it.route == route }
        if (matched != null && matched != selectedSection) {
            viewModel.selectSection(matched)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setUiActive(true)
                Lifecycle.Event.ON_STOP -> viewModel.setUiActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
    ) { isGranted -> viewModel.onAudioStoragePermissionResult(isGranted) }

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
    ) { isGranted -> viewModel.onNotificationPermissionResult(isGranted) }

    val requestNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onNotificationPermissionResult(true)
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> viewModel.toggleMicRta(isGranted) }

    val handleToggleMicRta: () -> Unit = {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) viewModel.toggleMicRta(true)
        else recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = {
                Text("Ecualizador Flotante", fontWeight = FontWeight.Bold, color = currentTheme.textColor)
            },
            text = {
                Text(
                    "Para mostrar el acceso flotante al ecualizador sobre otras aplicaciones, habilita el permiso correspondiente en Android.",
                    color = currentTheme.textSecondaryColor,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            )
                        } catch (_: Exception) {}
                    }
                ) {
                    Text("ACTIVAR", color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
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

    if (showSplashScreen) {
        CarAudioSplashScreen(
            theme = currentTheme,
            onEnterApp = { viewModel.dismissSplashScreen() },
            onSetDontShowAgain = { viewModel.setDontShowIntroAgain(it) },
            initialDontShowAgain = viewModel.isDontShowAgainPref
        )
        return
    }

    val primaryBottomSections = listOf(
        AppSection.HOME,
        AppSection.MY_MUSIC,
        AppSection.PLAYER,
        AppSection.EQUALIZER
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
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
                            painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                            contentDescription = "Logo Car Audio DSP Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, currentTheme.accentColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Car Audio DSP",
                                color = currentTheme.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reproductor y estudio de audio",
                                color = currentTheme.textSecondaryColor,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(
                        color = currentTheme.cardColor,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    AppSection.entries.forEach { section ->
                        val selected = selectedSection == section
                        NavigationDrawerItem(
                            selected = selected,
                            onClick = {
                                navigateToSection(section)
                                coroutineScope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = sectionIcon(section),
                                    contentDescription = section.title,
                                    tint = if (selected) currentTheme.onPrimaryColor else currentTheme.accentColor
                                )
                            },
                            label = {
                                Column {
                                    Text(
                                        text = section.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = section.subtitle,
                                        fontSize = 10.sp,
                                        color = if (selected) currentTheme.onPrimaryColor.copy(alpha = 0.8f) else currentTheme.textSecondaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

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
                                    contentDescription = "Abrir menú de navegación",
                                    tint = currentTheme.textColor
                                )
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_icon_dsp_studio_1794828267413),
                                    contentDescription = "Car Audio DSP Pro",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, currentTheme.accentColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedSection.title,
                                        color = currentTheme.textColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (dspSettings.dspMasterEnabled) {
                                            outputDeviceStatus.label
                                        } else {
                                            "DSP en bypass • ${outputDeviceStatus.label}"
                                        },
                                        color = currentTheme.textSecondaryColor,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(6.dp)
                                            .clip(CircleShape)
                                            .background(currentTheme.cardColor)
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
                                            contentDescription = "Ecualizador Flotante",
                                            tint = currentTheme.textSecondaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (dspSettings.dspMasterEnabled) currentTheme.accentColor.copy(alpha = 0.16f) else currentTheme.cardColor
                                            )
                                            .border(
                                                1.dp,
                                                if (dspSettings.dspMasterEnabled) currentTheme.accentColor.copy(alpha = 0.7f) else currentTheme.cardColor,
                                                CircleShape
                                            )
                                            .clickable { viewModel.toggleDspPower(!dspSettings.dspMasterEnabled) }
                                            .testTag("top_master_power_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PowerSettingsNew,
                                            contentDescription = "Encendido Maestro DSP",
                                            tint = if (dspSettings.dspMasterEnabled) currentTheme.accentColor else currentTheme.textSecondaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.surfaceColor),
                        windowInsets = WindowInsets.statusBars
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        val isExtraSection = selectedSection !in primaryBottomSections
                        NavigationBar(
                            containerColor = currentTheme.surfaceColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav"),
                            tonalElevation = 6.dp
                        ) {
                            primaryBottomSections.forEach { section ->
                                val selected = selectedSection == section
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToSection(section) },
                                    icon = { Icon(sectionIcon(section), contentDescription = section.shortTitle) },
                                    label = {
                                        Text(
                                            text = section.shortTitle,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = currentTheme.onPrimaryColor,
                                        indicatorColor = currentTheme.primaryColor,
                                        selectedTextColor = currentTheme.accentColor,
                                        unselectedIconColor = currentTheme.textSecondaryColor,
                                        unselectedTextColor = currentTheme.textSecondaryColor
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${section.route}")
                                )
                            }
                            NavigationBarItem(
                                selected = isExtraSection,
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "Más opciones") },
                                label = { Text("Menú", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
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
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = currentTheme.surfaceColor,
                            modifier = Modifier
                                .fillMaxHeight()
                                .testTag("tablet_navigation_rail")
                        ) {
                            AppSection.entries.forEach { section ->
                                NavigationRailItem(
                                    selected = selectedSection == section,
                                    onClick = { navigateToSection(section) },
                                    icon = { Icon(sectionIcon(section), contentDescription = section.title) },
                                    label = { Text(section.shortTitle, fontSize = 10.sp, maxLines = 1) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = currentTheme.onPrimaryColor,
                                        indicatorColor = currentTheme.primaryColor,
                                        selectedTextColor = currentTheme.accentColor,
                                        unselectedIconColor = currentTheme.textSecondaryColor,
                                        unselectedTextColor = currentTheme.textSecondaryColor
                                    )
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 860.dp)
                        ) {
                            AnimatedVisibility(
                                visible = latestBannerAlert != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                latestBannerAlert?.let { alert ->
                                    val sevColor = when (alert.severity) {
                                        FaultSeverity.ERROR -> currentTheme.errorColor
                                        FaultSeverity.WARNING -> currentTheme.clipAlertColor
                                        FaultSeverity.INFO -> currentTheme.accentColor
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .testTag("fault_alert_banner"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                        border = BorderStroke(1.dp, sevColor)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (alert.severity) {
                                                    FaultSeverity.ERROR -> Icons.Default.Error
                                                    FaultSeverity.WARNING -> Icons.Default.Warning
                                                    FaultSeverity.INFO -> Icons.Default.Info
                                                },
                                                contentDescription = alert.title,
                                                tint = sevColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(alert.title, color = currentTheme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(alert.message, color = currentTheme.textSecondaryColor, fontSize = 10.5.sp)
                                            }
                                            if (alert.actionLabel != null && alert.actionType != FaultActionType.NONE) {
                                                TextButton(
                                                    onClick = {
                                                        when (alert.actionType) {
                                                            FaultActionType.ENABLE_LIMITER -> viewModel.toggleLimiter(true)
                                                            FaultActionType.REQUEST_AUDIO_PERMISSION -> requestAudioLibraryPermission()
                                                            FaultActionType.REQUEST_NOTIFICATION_PERMISSION -> requestNotificationPermission()
                                                            FaultActionType.RELOAD_LIBRARY -> viewModel.scanDeviceMusicLibrary()
                                                            FaultActionType.OPEN_DIAGNOSTICS -> navigateToSection(AppSection.SETTINGS)
                                                            FaultActionType.NONE -> Unit
                                                        }
                                                        viewModel.dismissBannerAlert()
                                                    }
                                                ) {
                                                    Text(alert.actionLabel, color = currentTheme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            IconButton(onClick = { viewModel.dismissBannerAlert() }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Cerrar alerta", tint = currentTheme.textSecondaryColor)
                                            }
                                        }
                                    }
                                }
                            }

                            NavHost(
                                navController = navController,
                                startDestination = AppSection.HOME.route,
                                modifier = Modifier.weight(1f)
                            ) {
                                composable(AppSection.HOME.route) {
                                    HomeDashboardView(
                                        theme = currentTheme,
                                        dspSettings = dspSettings,
                                        equalizerSettings = equalizerSettings,
                                        capabilityReport = capabilityReport,
                                        isAnyAudioPlaying = isAnyAudioPlaying,
                                        currentTrack = currentTrack,
                                        playbackSeconds = playbackSeconds,
                                        tracksCount = tracks.size,
                                        recentTracks = recentTracks,
                                        mostPlayedTracks = mostPlayedTracks,
                                        favoriteTracks = tracks.filter { it.isFavorite },
                                        playlists = playlists,
                                        outputDeviceStatus = outputDeviceStatus,
                                        onNavigateSection = navigateToSection,
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNextTrack = { viewModel.nextTrack() },
                                        onPreviousTrack = { viewModel.previousTrack() },
                                        onPlayTrack = { track ->
                                            viewModel.playTrack(track)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onPlayAll = { shuffled ->
                                            viewModel.playAllTracks(shuffled = shuffled, sourceList = tracks)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onSelectPlaylist = { plId ->
                                            viewModel.selectPlaylistFilter(plId)
                                            navigateToSection(AppSection.MY_MUSIC)
                                        }
                                    )
                                }

                                composable(AppSection.MY_MUSIC.route) {
                                    MyMusicView(
                                        theme = currentTheme,
                                        tracks = filteredTracks,
                                        artists = artists,
                                        albums = albums,
                                        folders = folders,
                                        playlists = playlists,
                                        selectedPlaylistId = selectedPlaylistId,
                                        currentTrack = currentTrack,
                                        searchQuery = searchQuery,
                                        libraryFilter = libraryFilter,
                                        sortOrder = librarySortOrder,
                                        trackPlayCounts = trackPlayCounts,
                                        hasAudioLibraryPermission = hasAudioLibraryPermission,
                                        isScanningLibrary = isScanningLibrary,
                                        onTrackSelect = {
                                            viewModel.playTrack(it)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onPlayAll = { shuffled ->
                                            viewModel.playAllTracks(shuffled = shuffled)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onRemoveTrack = { viewModel.removeTrack(it) },
                                        onAddToQueue = { viewModel.addToQueue(it) },
                                        onPlayNextInQueue = { viewModel.playNextInQueue(it) },
                                        onPlayArtist = {
                                            viewModel.playArtist(it)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onPlayAlbum = {
                                            viewModel.playAlbum(it)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onPlayFolder = {
                                            viewModel.playFolder(it)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onPlayPlaylist = { plId ->
                                            viewModel.playPlaylist(plId)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onLibraryFilterChange = { viewModel.setLibraryFilter(it) },
                                        onSortOrderChange = { viewModel.setLibrarySortOrder(it) },
                                        onSelectPlaylist = { viewModel.selectPlaylistFilter(it) },
                                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                        onAddTrackToPlaylist = { plId, trId -> viewModel.addTrackToPlaylist(plId, trId) },
                                        onCustomTracksLoaded = { viewModel.addCustomUserTracks(it) },
                                        onRequestLibraryPermission = requestAudioLibraryPermission,
                                        onRefreshLibrary = { viewModel.scanDeviceMusicLibrary() }
                                    )
                                }

                                composable(AppSection.PLAYER.route) {
                                    PlayerView(
                                        theme = currentTheme,
                                        queueTracks = queueTracks,
                                        currentTrack = currentTrack,
                                        isPlaying = isPlaying,
                                        playbackSeconds = playbackSeconds,
                                        volumeLevel = volumeLevel,
                                        shuffleEnabled = shuffleEnabled,
                                        repeatMode = repeatMode,
                                        playbackSpeed = playbackSpeed,
                                        playbackPitch = playbackPitch,
                                        sleepTimerRemainingSec = sleepTimerRemainingSec,
                                        sleepTimerStopAfterTrack = sleepTimerStopAfterTrack,
                                        trackPlayCounts = trackPlayCounts,
                                        outputDeviceStatus = outputDeviceStatus,
                                        isAnyAudioPlaying = isAnyAudioPlaying,
                                        bassEnergy = bassEnergy,
                                        midEnergy = midEnergy,
                                        trebleEnergy = trebleEnergy,
                                        liteVisuals = liteVisuals,
                                        activeAbSlot = activeAbSlot,
                                        onPlayPause = { viewModel.togglePlayPause() },
                                        onNext = { viewModel.nextTrack() },
                                        onPrevious = { viewModel.previousTrack() },
                                        onSeek = { viewModel.seekTo(it) },
                                        onVolumeChange = { viewModel.setVolume(it) },
                                        onToggleShuffle = { viewModel.toggleShuffle() },
                                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onToggleAbComparison = { viewModel.toggleAbComparison() },
                                        onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                                        onSetPlaybackPitch = { viewModel.setPlaybackPitch(it) },
                                        onResetPlaybackSpeedPitch = { viewModel.resetPlaybackSpeedPitch() },
                                        onStartSleepTimer = { viewModel.startSleepTimer(it) },
                                        onToggleSleepAfterTrack = { viewModel.toggleSleepAfterCurrentTrack() },
                                        onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                                        onPlayQueueAt = { viewModel.playQueueAt(it) },
                                        onMoveQueueUp = { viewModel.moveQueueItemUp(it) },
                                        onMoveQueueDown = { viewModel.moveQueueItemDown(it) },
                                        onRemoveFromQueue = { viewModel.removeFromQueue(it) },
                                        onClearQueue = { viewModel.clearQueue() },
                                        onOpenMyMusic = { navigateToSection(AppSection.MY_MUSIC) },
                                        onOpenEqualizer = { navigateToSection(AppSection.EQUALIZER) }
                                    )
                                }

                                composable(AppSection.EQUALIZER.route) {
                                    EqualizerView(
                                        theme = currentTheme,
                                        dspSettings = dspSettings,
                                        eqSettings = equalizerSettings,
                                        capabilityReport = capabilityReport,
                                        abSlotA = abSlotA,
                                        abSlotB = abSlotB,
                                        activeAbSlot = activeAbSlot,
                                        isAnyAudioPlaying = isAnyAudioPlaying,
                                        bassEnergy = bassEnergy,
                                        midEnergy = midEnergy,
                                        trebleEnergy = trebleEnergy,
                                        liteVisuals = liteVisuals,
                                        onToggleDspMaster = { viewModel.toggleDspPower(it) },
                                        onToggleEqPower = { viewModel.toggleEqualizerPower(it) },
                                        onUpdateBand10 = { idx, db -> viewModel.updateEqualizerBand10(idx, db) },
                                        onUpdateBand31 = { idx, db -> viewModel.updateEqualizerBand31(idx, db) },
                                        onUpdateParametricBand = { id, f, g, q, t, en -> viewModel.updateParametricBand(id, f, g, q, t, en) },
                                        onToggleParametricBand = { id, en -> viewModel.toggleParametricBand(id, en) },
                                        onResetFlat = { viewModel.resetEqualizerFlat() },
                                        onCaptureAbSlot = { viewModel.captureAbSlot(it) },
                                        onApplyAbSlot = { viewModel.applyAbSlot(it) },
                                        onToggleAbComparison = { viewModel.toggleAbComparison() }
                                    )
                                }

                                composable(AppSection.YOUTUBE_MUSIC.route) {
                                    YouTubeMusicView(
                                        theme = currentTheme,
                                        searchQuery = ytSearchQuery,
                                        savedItems = ytSavedItems,
                                        searchHistory = ytSearchHistory,
                                        onSearchQueryChange = { viewModel.setYtSearchQuery(it) },
                                        onRecordSearch = { viewModel.recordYtSearch(it) },
                                        onSaveShortcut = { t, a, u, fav -> viewModel.saveYtShortcut(t, a, u, fav) },
                                        onToggleFavorite = { viewModel.toggleYtFavorite(it) },
                                        onRemoveItem = { viewModel.removeYtItem(it) },
                                        onClearHistory = { viewModel.clearYtHistory() },
                                        buildSearchUrl = { q -> viewModel.buildYouTubeMusicSearchUri(q).toString() }
                                    )
                                }

                                composable(AppSection.AUDIO_TOOLS.route) {
                                    AudioToolsView(
                                        theme = currentTheme,
                                        dspSettings = dspSettings,
                                        capabilityReport = capabilityReport,
                                        rtaBands = rtaBands,
                                        oscilloscopeWave = oscilloscopeWave,
                                        leftVu = leftVu,
                                        rightVu = rightVu,
                                        rmsDb = rmsDb,
                                        splDb = splDb,
                                        isClippingDetected = isClippingDetected,
                                        clipEventsCount = clipEventsCount,
                                        isMicRtaActive = isMicRta,
                                        isAnyAudioPlaying = isAnyAudioPlaying,
                                        bassEnergy = bassEnergy,
                                        midEnergy = midEnergy,
                                        trebleEnergy = trebleEnergy,
                                        liteVisuals = liteVisuals,
                                        outputDeviceStatus = outputDeviceStatus,
                                        powerTelemetry = devicePowerTelemetry,
                                        onToggleDspMaster = { viewModel.toggleDspPower(it) },
                                        onPreampChanged = { viewModel.setDspPreampGain(it) },
                                        onMasterGainChanged = { viewModel.setMasterGain(it) },
                                        onBassGainChanged = { viewModel.setDspBassGain(it) },
                                        onMidGainChanged = { viewModel.setDspMidGain(it) },
                                        onTrebleGainChanged = { viewModel.setDspTrebleGain(it) },
                                        onBassBoostChanged = { en, db, hz -> viewModel.setBassBoost(en, db, hz) },
                                        onSubBassChanged = { en, db, hz -> viewModel.setSubBass(en, db, hz) },
                                        onLoudnessChanged = { en, db -> viewModel.setLoudness(en, db) },
                                        onBalanceChanged = { viewModel.setBalancePan(it) },
                                        onStereoWidthChanged = { viewModel.setStereoWidth(it) },
                                        onToggleCompressor = { viewModel.toggleCompressor(it) },
                                        onCompressorParamsChanged = { th, rat, att, rel -> viewModel.setCompressorParams(th, rat, att, rel) },
                                        onToggleLimiter = { viewModel.toggleLimiter(it) },
                                        onLimiterCeilingChanged = { viewModel.setLimiterCeiling(it) },
                                        onSubsonicChanged = { en, hz -> viewModel.setSubsonicFilter(en, hz) },
                                        onHpfChanged = { en, hz, slope -> viewModel.setFilterHpf(en, hz, slope) },
                                        onLpfChanged = { en, hz, slope -> viewModel.setFilterLpf(en, hz, slope) },
                                        onHiResChanged = { viewModel.setHiResQuality(it) },
                                        onTubeWarmthChanged = { viewModel.setTubeWarmth(it) },
                                        onPresenceChanged = { en, lvl -> viewModel.setPresence(en, lvl) },
                                        onClarityChanged = { en, lvl -> viewModel.setClarity(en, lvl) },
                                        onExciterChanged = { en, lvl -> viewModel.setExciter(en, lvl) },
                                        onToggleMicRta = handleToggleMicRta,
                                        onRefreshTelemetry = { viewModel.refreshDevicePowerTelemetry() }
                                    )
                                }

                                composable(AppSection.PRESETS.route) {
                                    PresetsView(
                                        theme = currentTheme,
                                        presets = eqPresets,
                                        activePresetName = equalizerSettings.activePresetName,
                                        customProfiles = customSoundProfiles,
                                        abSlotA = abSlotA,
                                        abSlotB = abSlotB,
                                        activeAbSlot = activeAbSlot,
                                        onSelectPreset = { viewModel.selectPreset(it) },
                                        onDeletePreset = { viewModel.deletePreset(it) },
                                        onSaveSoundProfile = { n, d -> viewModel.saveCustomSoundProfile(n, d) },
                                        onApplySoundProfile = { viewModel.applyCustomSoundProfile(it) },
                                        onDeleteSoundProfile = { viewModel.deleteCustomSoundProfile(it) },
                                        onCaptureAbSlot = { viewModel.captureAbSlot(it) },
                                        onApplyAbSlot = { viewModel.applyAbSlot(it) },
                                        onToggleAbComparison = { viewModel.toggleAbComparison() },
                                        onClearAbSlot = { viewModel.clearAbSlot(it) }
                                    )
                                }

                                composable(AppSection.FAVORITES.route) {
                                    FavoritesView(
                                        theme = currentTheme,
                                        favoriteTracks = tracks.filter { it.isFavorite },
                                        currentTrack = currentTrack,
                                        playlists = playlists,
                                        ytFavoriteItems = ytSavedItems.filter { it.isFavorite },
                                        onPlayTrack = {
                                            viewModel.playTrack(it)
                                            navigateToSection(AppSection.PLAYER)
                                        },
                                        onToggleFavoriteTrack = { viewModel.toggleFavorite(it) },
                                        onAddToQueue = { viewModel.addToQueue(it) },
                                        onPlayNextInQueue = { viewModel.playNextInQueue(it) },
                                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                        onOpenPlaylistInMyMusic = { plId ->
                                            viewModel.selectPlaylistFilter(plId)
                                            navigateToSection(AppSection.MY_MUSIC)
                                        },
                                        onAddTrackToPlaylist = { plId, trId -> viewModel.addTrackToPlaylist(plId, trId) },
                                        onToggleYtFavorite = { viewModel.toggleYtFavorite(it) },
                                        onOpenYouTubeSection = { navigateToSection(AppSection.YOUTUBE_MUSIC) }
                                    )
                                }

                                composable(AppSection.SETTINGS.route) {
                                    AppSettingsView(
                                        theme = currentTheme,
                                        onSelectTheme = { viewModel.setTheme(it) },
                                        liteVisuals = liteVisuals,
                                        onToggleLiteVisuals = { viewModel.setLiteVisualsEnabled(it) },
                                        keepScreenOn = keepScreenOn,
                                        onToggleKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                                        autoPlayIntro = autoPlayIntro,
                                        onToggleAutoPlayIntro = { viewModel.setAutoPlayIntro(it) },
                                        onReplayIntro = { viewModel.openSplashScreen() },
                                        onResetDspDefaults = { viewModel.resetDspToDefaults() },
                                        outputDeviceStatus = outputDeviceStatus,
                                        sessionTarget = sessionTarget,
                                        capabilityReport = capabilityReport,
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

                            AnimatedVisibility(
                                visible = currentTrack != null && selectedSection != AppSection.PLAYER,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                currentTrack?.let { activeTrack ->
                                    MiniPlayerBar(
                                        track = activeTrack,
                                        isPlaying = isPlaying,
                                        playbackSeconds = playbackSeconds,
                                        theme = currentTheme,
                                        onOpenPlayer = { navigateToSection(AppSection.PLAYER) },
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNextTrack = { viewModel.nextTrack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
