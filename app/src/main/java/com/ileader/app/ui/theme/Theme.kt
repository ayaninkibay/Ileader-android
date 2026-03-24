package com.ileader.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════
// MATERIAL 3 COLOR SCHEMES (synced with website)
// ══════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = ILeaderColors.PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = ILeaderColors.DarkRed,
    onPrimaryContainer = Color.White,
    secondary = ILeaderColors.LightRed,
    onSecondary = Color.White,
    tertiary = ILeaderColors.Info,
    background = DarkAppColors.bg,
    surface = DarkAppColors.cardBg,
    surfaceVariant = DarkAppColors.cardHover,
    surfaceContainerHighest = DarkAppColors.cardHover,
    onBackground = DarkAppColors.textPrimary,
    onSurface = DarkAppColors.textPrimary,
    onSurfaceVariant = DarkAppColors.textSecondary,
    outline = DarkAppColors.border,
    outlineVariant = DarkAppColors.borderLight,
    error = ILeaderColors.Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ILeaderColors.PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = ILeaderColors.LightRed,
    onPrimaryContainer = ILeaderColors.DeepRed,
    secondary = ILeaderColors.DarkRed,
    onSecondary = Color.White,
    tertiary = ILeaderColors.Info,
    background = LightAppColors.bg,
    surface = LightAppColors.cardBg,
    surfaceVariant = LightAppColors.bgSecondary,
    surfaceContainerHighest = LightAppColors.cardHover,
    onBackground = LightAppColors.textPrimary,
    onSurface = LightAppColors.textPrimary,
    onSurfaceVariant = LightAppColors.textSecondary,
    outline = LightAppColors.border,
    outlineVariant = LightAppColors.borderLight,
    error = ILeaderColors.Error,
    onError = Color.White
)

// ══════════════════════════════════════════════════════════
// THEME MODE
// ══════════════════════════════════════════════════════════

enum class ThemeMode { LIGHT, DARK, SYSTEM }

// ══════════════════════════════════════════════════════════
// MAIN THEME COMPOSABLE
// ══════════════════════════════════════════════════════════

@Composable
fun ILeaderTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = true // Всегда тёмная тема

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
