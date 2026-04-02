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
): Modifier = this.drawBehind {
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.color = color.toArgb()

    if (blurRadius > 0.dp) {
        frameworkPaint.maskFilter = BlurMaskFilter(
            blurRadius.toPx(),
            BlurMaskFilter.Blur.NORMAL
        )
    }

    val spreadPx = spread.toPx()
    val left = -spreadPx + offsetX.toPx()
    val top = -spreadPx + offsetY.toPx()
    val right = size.width + spreadPx + offsetX.toPx()
    val bottom = size.height + spreadPx + offsetY.toPx()

    drawIntoCanvas { canvas ->
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            )
        }
        canvas.drawPath(path, paint)
    }
}

/**
 * Card shadow — subtle in light mode (inzhu style), flat in dark mode.
 */
fun Modifier.cardShadow(
    isDark: Boolean = false
): Modifier = if (isDark) this else this.coloredShadow(
    color = Color.Black.copy(alpha = 0.08f),
    blurRadius = 10.dp,
    offsetX = 0.dp,
    offsetY = 3.dp,
    cornerRadius = 16.dp
)

/**
 * Floating element shadow — subtle in light mode, flat in dark mode.
 */
fun Modifier.floatingShadow(
    isDark: Boolean = false
): Modifier = if (isDark) this else this.coloredShadow(
    color = Color.Black.copy(alpha = 0.1f),
    blurRadius = 12.dp,
    offsetX = 0.dp,
    offsetY = 4.dp,
    cornerRadius = 20.dp
)
