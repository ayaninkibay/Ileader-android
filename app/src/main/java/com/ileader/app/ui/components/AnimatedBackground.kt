package com.ileader.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.LocalAppColors
import kotlin.math.PI
import kotlin.math.sin

private data class FloatingIcon(
    val icon: ImageVector,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Float,
    val opacity: Float,
    val animType: Int,
    val delayFraction: Float
)

private val ICONS = listOf(
    // Row 1
    FloatingIcon(Icons.Default.DirectionsCar, 0.05f, 0.03f, 22f, 0.18f, 0, 0.0f),
    FloatingIcon(Icons.Default.EmojiEvents, 0.25f, 0.05f, 20f, 0.16f, 1, 0.15f),
    FloatingIcon(Icons.Default.Flag, 0.48f, 0.04f, 18f, 0.14f, 2, 0.35f),
    FloatingIcon(Icons.Default.GpsFixed, 0.70f, 0.06f, 22f, 0.17f, 0, 0.55f),
    FloatingIcon(Icons.Default.Bolt, 0.90f, 0.03f, 18f, 0.15f, 1, 0.75f),
    // Row 2
    FloatingIcon(Icons.Default.MilitaryTech, 0.12f, 0.16f, 20f, 0.16f, 2, 0.10f),
    FloatingIcon(Icons.Default.CenterFocusStrong, 0.35f, 0.18f, 18f, 0.14f, 0, 0.30f),
    FloatingIcon(Icons.Default.DirectionsCar, 0.58f, 0.15f, 22f, 0.18f, 1, 0.50f),
    FloatingIcon(Icons.Default.SportsMma, 0.82f, 0.17f, 20f, 0.15f, 2, 0.70f),
    // Row 3
    FloatingIcon(Icons.Default.Timer, 0.03f, 0.28f, 18f, 0.14f, 1, 0.05f),
    FloatingIcon(Icons.Default.RadioButtonChecked, 0.22f, 0.30f, 20f, 0.16f, 2, 0.25f),
    FloatingIcon(Icons.Default.EmojiEvents, 0.42f, 0.27f, 22f, 0.18f, 0, 0.45f),
    FloatingIcon(Icons.Default.Flag, 0.65f, 0.29f, 18f, 0.15f, 1, 0.65f),
    FloatingIcon(Icons.Default.MilitaryTech, 0.88f, 0.26f, 20f, 0.17f, 2, 0.85f),
    // Row 4
    FloatingIcon(Icons.Default.Bolt, 0.08f, 0.40f, 22f, 0.17f, 0, 0.20f),
    FloatingIcon(Icons.Default.GpsFixed, 0.30f, 0.42f, 18f, 0.14f, 1, 0.40f),
    FloatingIcon(Icons.Default.CenterFocusStrong, 0.52f, 0.38f, 20f, 0.16f, 2, 0.60f),
    FloatingIcon(Icons.Default.DirectionsCar, 0.75f, 0.41f, 22f, 0.18f, 0, 0.80f),
    FloatingIcon(Icons.Default.SportsMma, 0.93f, 0.39f, 18f, 0.15f, 1, 0.95f),
    // Row 5
    FloatingIcon(Icons.Default.Flag, 0.04f, 0.53f, 20f, 0.15f, 2, 0.08f),
    FloatingIcon(Icons.Default.EmojiEvents, 0.20f, 0.55f, 18f, 0.14f, 0, 0.28f),
    FloatingIcon(Icons.Default.Timer, 0.40f, 0.51f, 22f, 0.18f, 1, 0.48f),
    FloatingIcon(Icons.Default.MilitaryTech, 0.62f, 0.54f, 20f, 0.16f, 2, 0.68f),
    FloatingIcon(Icons.Default.RadioButtonChecked, 0.85f, 0.52f, 18f, 0.15f, 0, 0.88f),
    // Row 6
    FloatingIcon(Icons.Default.GpsFixed, 0.10f, 0.65f, 22f, 0.17f, 1, 0.12f),
    FloatingIcon(Icons.Default.DirectionsCar, 0.33f, 0.68f, 18f, 0.14f, 2, 0.32f),
    FloatingIcon(Icons.Default.Bolt, 0.55f, 0.63f, 20f, 0.16f, 0, 0.52f),
    FloatingIcon(Icons.Default.CenterFocusStrong, 0.78f, 0.66f, 22f, 0.18f, 1, 0.72f),
    // Row 7
    FloatingIcon(Icons.Default.SportsMma, 0.06f, 0.78f, 18f, 0.15f, 2, 0.18f),
    FloatingIcon(Icons.Default.Flag, 0.28f, 0.80f, 22f, 0.17f, 0, 0.38f),
    FloatingIcon(Icons.Default.EmojiEvents, 0.50f, 0.76f, 20f, 0.16f, 1, 0.58f),
    FloatingIcon(Icons.Default.Timer, 0.72f, 0.79f, 18f, 0.14f, 2, 0.78f),
    FloatingIcon(Icons.Default.MilitaryTech, 0.92f, 0.77f, 20f, 0.18f, 0, 0.92f),
    // Row 8
    FloatingIcon(Icons.Default.RadioButtonChecked, 0.15f, 0.90f, 20f, 0.16f, 1, 0.22f),
    FloatingIcon(Icons.Default.GpsFixed, 0.45f, 0.88f, 22f, 0.17f, 2, 0.42f),
    FloatingIcon(Icons.Default.DirectionsCar, 0.68f, 0.91f, 18f, 0.14f, 0, 0.62f),
    FloatingIcon(Icons.Default.Bolt, 0.88f, 0.89f, 20f, 0.15f, 1, 0.82f),
)

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg

    val gridColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f)
    val glowColor = Color(0xFFEF4444)
    val glow1Alpha = if (isDark) 0.20f else 0.15f
    val glow2Alpha = if (isDark) 0.15f else 0.12f
    val iconColor = Color(0xFFEF4444)

    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val anim1 = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "f1"
    )
    val anim2 = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "f2"
    )
    val anim3 = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart),
        label = "f3"
    )

    val density = LocalDensity.current

    // Prepare icon painters
    val iconPainters = ICONS.map { item ->
        val painter = rememberVectorPainter(image = item.icon)
        item to painter
    }

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gridSize = with(density) { 50.dp.toPx() }

        // 1. Grid pattern
        var x = 0f
        while (x <= w) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            x += gridSize
        }
        var y = 0f
        while (y <= h) {
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            y += gridSize
        }

        // 2. Gradient glow blobs
        val glow1Radius = with(density) { 350.dp.toPx() }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = glow1Alpha), Color.Transparent),
                center = Offset(w, 0f),
                radius = glow1Radius
            ),
            radius = glow1Radius,
            center = Offset(w, 0f)
        )

        val glow2Radius = with(density) { 300.dp.toPx() }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = glow2Alpha), Color.Transparent),
                center = Offset(0f, h),
                radius = glow2Radius
            ),
            radius = glow2Radius,
            center = Offset(0f, h)
        )

        // 3. Floating icons
        iconPainters.forEach { (item, painter) ->
            val progress = when (item.animType) {
                0 -> anim1.value
                1 -> anim2.value
                else -> anim3.value
            }

            val t = ((progress + item.delayFraction) % 1f) * 2f * PI.toFloat()
            val dx = sin(t) * with(density) { 15.dp.toPx() }
            val dy = sin(t * 0.7f + 1f) * with(density) { 20.dp.toPx() }

            val iconSizePx = with(density) { item.sizeDp.dp.toPx() }
            val cx = item.xFraction * w + dx
            val cy = item.yFraction * h + dy

            translate(left = cx - iconSizePx / 2f, top = cy - iconSizePx / 2f) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(iconSizePx, iconSizePx),
                        alpha = item.opacity,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor)
                    )
                }
            }
        }
    }
}
