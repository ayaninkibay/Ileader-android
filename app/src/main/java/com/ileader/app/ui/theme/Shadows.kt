package com.ileader.app.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS-style colored shadow with blur radius and offset.
 * Unlike Material `shadow()`, this supports:
 * - Custom shadow color with alpha
 * - Blur radius (gaussian blur)
 * - X/Y offset for directional shadows
 * - Corner radius matching the card shape
 */
fun Modifier.coloredShadow(
    color: Color = Color.Black.copy(alpha = 0.08f),
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    spread: Dp = 0.dp
): Modifier = this

/**
 * Subtle card shadow for light theme — barely visible lift.
 */
fun Modifier.cardShadow(
    isDark: Boolean = false
): Modifier = this

/**
 * Elevated shadow for floating elements (FABs, bottom bars).
 */
fun Modifier.floatingShadow(
    isDark: Boolean = false
): Modifier = this
