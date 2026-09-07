package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.model.CarAudioThemeType

@Composable
fun CarAudioAppTheme(
    themeType: CarAudioThemeType = CarAudioThemeType.NEON_CYBER,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = themeType.primaryColor,
        onPrimary = themeType.onPrimaryColor,
        primaryContainer = themeType.surfaceColor,
        onPrimaryContainer = themeType.primaryColor,
        secondary = themeType.accentColor,
        onSecondary = themeType.onPrimaryColor,
        secondaryContainer = themeType.surfaceColor,
        onSecondaryContainer = themeType.accentColor,
        tertiary = themeType.accentColor,
        background = themeType.backgroundColor,
        onBackground = themeType.textColor,
        surface = themeType.surfaceColor,
        onSurface = themeType.textColor,
        surfaceVariant = themeType.cardColor,
        onSurfaceVariant = themeType.textSecondaryColor,
        error = themeType.clipAlertColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CarAudioAppTheme(
        themeType = CarAudioThemeType.NEON_CYBER,
        content = content
    )
}


