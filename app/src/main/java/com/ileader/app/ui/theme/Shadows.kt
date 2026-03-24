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
): Modifier = this.then(
    Modifier.drawBehind {
        val shadowColor = color.toArgb()
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        frameworkPaint.color = shadowColor
        if (blurRadius > 0.dp) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blurRadius.toPx(),
                BlurMaskFilter.Blur.NORMAL
            )
        }

        val spreadPx = spread.toPx()
        val offsetXPx = offsetX.toPx()
        val offsetYPx = offsetY.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = -spreadPx + offsetXPx,
                    top = -spreadPx + offsetYPx,
                    right = size.width + spreadPx + offsetXPx,
                    bottom = size.height + spreadPx + offsetYPx,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
        }

        drawIntoCanvas { canvas ->
            canvas.drawPath(path, paint)
        }
    }
)

/**
 * Subtle card shadow for light theme — barely visible lift.
 */
fun Modifier.cardShadow(
    isDark: Boolean = false
): Modifier = if (isDark) {
    this // No shadow in dark theme — use borders instead
} else {
    this.coloredShadow(
        color = Color.Black.copy(alpha = 0.06f),
        blurRadius = 10.dp,
        offsetY = 4.dp,
        cornerRadius = 16.dp
    )
}

/**
 * Elevated shadow for floating elements (FABs, bottom bars).
 */
fun Modifier.floatingShadow(
    isDark: Boolean = false
): Modifier = if (isDark) {
    this.coloredShadow(
        color = Color.Black.copy(alpha = 0.4f),
        blurRadius = 12.dp,
        offsetY = (-2).dp,
        cornerRadius = 20.dp
    )
} else {
    this.coloredShadow(
        color = Color.Black.copy(alpha = 0.10f),
        blurRadius = 14.dp,
        offsetY = (-2).dp,
        cornerRadius = 20.dp
    )
}
