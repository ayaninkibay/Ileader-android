package com.ileader.app.ui.screens.admin

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.components.DarkTheme

// ── Palette aliases (shorthand for admin screens) ──
// All values delegate to shared DarkTheme object.
internal val Bg: Color @Composable get() = DarkTheme.Bg
internal val CardBg: Color @Composable get() = DarkTheme.CardBg
internal val CardBorder: Color @Composable get() = DarkTheme.CardBorder
internal val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
internal val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
internal val TextMuted: Color @Composable get() = DarkTheme.TextMuted
internal val Accent: Color @Composable get() = DarkTheme.Accent
internal val AccentDark: Color @Composable get() = DarkTheme.AccentDark
internal val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

// ── Admin-specific components (not available in shared DarkThemeComponents) ──

@Composable
fun AdminSectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.2).sp)
}
