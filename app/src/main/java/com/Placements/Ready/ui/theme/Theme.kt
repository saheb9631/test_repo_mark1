package com.Placements.Ready.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// CompositionLocal to provide ThemePreferences throughout the tree
val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences?> { null }

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonOrange,
    background = LightBgColor,
    surface = LightSurfaceColor,
    surfaceVariant = LightCardColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF6E6E8A),
    outline = Color(0xFF9E9EB8),
    outlineVariant = LightCardBorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonOrange,
    background = DarkBgColor,
    surface = DarkSurfaceColor,
    surfaceVariant = DarkCardColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0D0),
    outline = Color(0xFF6E6E9A),
    outlineVariant = DarkCardBorderColor
)

@Composable
fun CampusPlacementTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
