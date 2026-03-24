package com.ileader.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val RED = Color(0xFFE53535)

private data class IconSpec(
    val xFraction: Float,
    val yFraction: Float,
    val size: Dp,
    val alpha: Float,
    val iconIndex: Int
)

private val ICON_SPECS = listOf(
    IconSpec(0.05f, 0.03f, 22.dp, 0.18f, 0),
    IconSpec(0.25f, 0.05f, 20.dp, 0.14f, 1),
    IconSpec(0.48f, 0.02f, 22.dp, 0.16f, 2),
    IconSpec(0.70f, 0.06f, 20.dp, 0.14f, 3),
    IconSpec(0.90f, 0.04f, 18.dp, 0.18f, 4),

    IconSpec(0.12f, 0.15f, 20.dp, 0.16f, 5),
    IconSpec(0.35f, 0.18f, 22.dp, 0.12f, 6),
    IconSpec(0.58f, 0.14f, 20.dp, 0.18f, 0),
    IconSpec(0.82f, 0.17f, 22.dp, 0.14f, 7),

    IconSpec(0.03f, 0.28f, 20.dp, 0.18f, 8),
    IconSpec(0.22f, 0.30f, 18.dp, 0.14f, 3),
    IconSpec(0.42f, 0.26f, 22.dp, 0.16f, 1),
    IconSpec(0.65f, 0.29f, 20.dp, 0.12f, 2),
    IconSpec(0.88f, 0.27f, 18.dp, 0.18f, 5),

    IconSpec(0.08f, 0.40f, 20.dp, 0.14f, 4),
    IconSpec(0.30f, 0.42f, 22.dp, 0.18f, 6),
    IconSpec(0.52f, 0.38f, 20.dp, 0.14f, 7),
    IconSpec(0.75f, 0.41f, 22.dp, 0.16f, 0),
    IconSpec(0.93f, 0.39f, 18.dp, 0.12f, 8),

    IconSpec(0.04f, 0.53f, 20.dp, 0.16f, 2),
    IconSpec(0.20f, 0.55f, 18.dp, 0.18f, 1),
    IconSpec(0.40f, 0.51f, 22.dp, 0.14f, 8),
    IconSpec(0.62f, 0.54f, 20.dp, 0.16f, 5),
    IconSpec(0.85f, 0.52f, 20.dp, 0.18f, 3),

    IconSpec(0.10f, 0.65f, 22.dp, 0.14f, 6),
    IconSpec(0.33f, 0.68f, 20.dp, 0.16f, 0),
    IconSpec(0.55f, 0.63f, 18.dp, 0.18f, 4),
    IconSpec(0.78f, 0.66f, 22.dp, 0.14f, 7),

    IconSpec(0.06f, 0.78f, 20.dp, 0.18f, 7),
    IconSpec(0.28f, 0.80f, 18.dp, 0.14f, 2),
    IconSpec(0.50f, 0.76f, 20.dp, 0.16f, 1),
    IconSpec(0.72f, 0.79f, 22.dp, 0.18f, 8),
    IconSpec(0.92f, 0.77f, 18.dp, 0.12f, 4),

    IconSpec(0.15f, 0.90f, 20.dp, 0.16f, 3),
    IconSpec(0.45f, 0.92f, 18.dp, 0.18f, 6),
    IconSpec(0.68f, 0.88f, 22.dp, 0.14f, 0),
    IconSpec(0.88f, 0.91f, 20.dp, 0.16f, 5),
)

private val ICONS = listOf(
    Icons.Default.DirectionsCar,
    Icons.Default.EmojiEvents,
    Icons.Default.GpsFixed,
    Icons.Default.Flag,
    Icons.Default.Bolt,
    Icons.Default.MilitaryTech,
    Icons.Default.SportsMartialArts,
    Icons.Default.Timer,
    Icons.Default.RadioButtonChecked,
)

@Composable
fun SportBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Сетка
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 50.dp.toPx()
            val lineColor = Color.White.copy(alpha = 0.04f)
            var x = 0f
            while (x <= size.width) {
                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += gridSize
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += gridSize
            }

            // Красные свечения
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RED.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RED.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(0f, size.height)
            )
        }

        // Плавающие иконки
        Layout(
            content = {
                ICON_SPECS.forEach { spec ->
                    Icon(
                        imageVector = ICONS[spec.iconIndex],
                        contentDescription = null,
                        tint = RED,
                        modifier = Modifier.alpha(spec.alpha)
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.mapIndexed { i, m ->
                val spec = ICON_SPECS[i]
                val sizePx = spec.size.toPx().toInt()
                m.measure(constraints.copy(minWidth = sizePx, maxWidth = sizePx, minHeight = sizePx, maxHeight = sizePx))
            }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { i, p ->
                    val spec = ICON_SPECS[i]
                    val x = (constraints.maxWidth * spec.xFraction).toInt()
                    val y = (constraints.maxHeight * spec.yFraction).toInt()
                    p.placeRelative(x - p.width / 2, y - p.height / 2)
                }
            }
        }
    }
}
