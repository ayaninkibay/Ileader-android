package com.ileader.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════
// BRAND & SHARED COLORS (same in both themes)
// ══════════════════════════════════════════════════════════

object ILeaderColors {
    // Brand Red
    val PrimaryRed = Color(0xFFE53535)
    val DarkRed = Color(0xFFC62828)
    val LightRed = Color(0xFFFF5252)
    val DeepRed = Color(0xFF9B1C1C)

    // Status Colors (matching web)
    val Success = Color(0xFF22C55E)
    val SuccessLight = Color(0xFF4ADE80)
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFBBF24)
    val Error = Color(0xFFE53535)
    val ErrorLight = Color(0xFFFF5252)
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFF60A5FA)

    // Role Colors (matching web)
    val AthleteColor = Color(0xFFE53535)
    val TrainerColor = Color(0xFF3B82F6)
    val OrganizerColor = Color(0xFF22C55E)
    val RefereeColor = Color(0xFFF59E0B)
    val SponsorColor = Color(0xFF8B5CF6)
    val MediaColor = Color(0xFF06B6D4)
    val AdminColor = Color(0xFFF97316)
    val ViewerColor = Color(0xFF64748B)

    // Gold / Silver / Bronze (medals)
    val Gold = Color(0xFFFFD700)
    val GoldLight = Color(0xFFFFE44D)
    val Silver = Color(0xFFC0C0C0)
    val Bronze = Color(0xFFCD7F32)
}

// ══════════════════════════════════════════════════════════
// SEMANTIC APP COLORS — theme-aware palette
// Replaces old hardcoded DarkTheme object
// ══════════════════════════════════════════════════════════

@Immutable
data class AppColorScheme(
    // Backgrounds
    val bg: Color,
    val bgSecondary: Color,
    // Cards
    val cardBg: Color,
    val cardHover: Color,
    // Borders
    val border: Color,
    val borderLight: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textPlaceholder: Color,
    // Accent (red brand)
    val accent: Color,
    val accentDark: Color,
    val accentSoft: Color,
    // Accent gradient
    val accentGradient: Brush,
)

// ── DARK palette (synced with website: --background: #0a0a0a) ──

val DarkAppColors = AppColorScheme(
    bg = Color(0xFF0A0A0A),
    bgSecondary = Color(0xFF141414),
    cardBg = Color(0xFF18181B),
    cardHover = Color(0xFF27272A),
    border = Color(0xFF27272A),
    borderLight = Color(0xFF3F3F46),
    textPrimary = Color(0xFFF0F0F0),
    textSecondary = Color(0xFFA8A8B2),
    textMuted = Color(0xFF78788A),
    textPlaceholder = Color(0xFF56565E),
    accent = ILeaderColors.PrimaryRed,
    accentDark = ILeaderColors.DarkRed,
    accentSoft = ILeaderColors.PrimaryRed.copy(alpha = 0.12f),
    accentGradient = Brush.horizontalGradient(
        listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
    ),
)

// ── LIGHT palette (synced with website: --background: #f5f5f7) ──

val LightAppColors = AppColorScheme(
    bg = Color(0xFFF5F5F7),
    bgSecondary = Color(0xFFEFEFF2),
    cardBg = Color(0xFFFFFFFF),
    cardHover = Color(0xFFF0F0F3),
    border = Color(0xFFE5E5E5),
    borderLight = Color(0xFFEEEEEE),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF6B7280),
    textMuted = Color(0xFF9CA3AF),
    textPlaceholder = Color(0xFFD1D5DB),
    accent = ILeaderColors.PrimaryRed,
    accentDark = ILeaderColors.DarkRed,
    accentSoft = ILeaderColors.PrimaryRed.copy(alpha = 0.08f),
    accentGradient = Brush.horizontalGradient(
        listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
    ),
)

// ══════════════════════════════════════════════════════════
// COMPOSITION LOCAL
// ══════════════════════════════════════════════════════════

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Shortcut: use `AppColors` anywhere inside a composable. */
object AppColors {
    val current: AppColorScheme
        @Composable get() = LocalAppColors.current
}
