package com.ileader.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.ileader.app.R
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.ILeaderOutlinedButton
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ══════════════════════════════════════════════════════════
// Floating particles data
// ══════════════════════════════════════════════════════════

private data class Particle(
    val startX: Float,
    val startY: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val drift: Float
)

private fun generateParticles(count: Int): List<Particle> {
    val rng = Random(42)
    return List(count) {
        Particle(
            startX = rng.nextFloat(),
            startY = rng.nextFloat(),
            radius = rng.nextFloat() * 2.5f + 1f,
            speed = rng.nextFloat() * 0.3f + 0.1f,
            alpha = rng.nextFloat() * 0.25f + 0.05f,
            drift = (rng.nextFloat() - 0.5f) * 0.15f
        )
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg

    // Static values (animations removed)
    val logoAlpha = 1f
    val logoScale = 1f
    val titleOffset = 0f
    val titleAlpha = 1f
    val subtitleAlpha = 1f
    val statsAlpha = 1f
    val statsOffset = 0f
    val buttonsAlpha = 1f
    val buttonsOffset = 0f
    val pulseScale = 1f
    val glowAlpha = 0.3f
    val orbitRotation = 0f
    val secondOrbitRotation = 0f
    val gradientOffset = 0.5f
    val particleTime = 0.5f

    val particles = remember { generateParticles(30) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Animated background gradient ──
        val bgGradientAlpha = if (isDark) 0.12f else 0.06f
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            // Soft gradient blobs
            val blob1X = w * (0.2f + gradientOffset * 0.3f)
            val blob1Y = h * (0.15f + gradientOffset * 0.1f)
            val blob2X = w * (0.8f - gradientOffset * 0.2f)
            val blob2Y = h * (0.7f + gradientOffset * 0.1f)

            drawCircle(
                color = ILeaderColors.PrimaryRed.copy(alpha = bgGradientAlpha),
                radius = w * 0.45f,
                center = Offset(blob1X, blob1Y)
            )
            drawCircle(
                color = ILeaderColors.DarkRed.copy(alpha = bgGradientAlpha * 0.7f),
                radius = w * 0.35f,
                center = Offset(blob2X, blob2Y)
            )

            // Floating particles
            val particleColor = ILeaderColors.PrimaryRed
            particles.forEach { p ->
                val y = ((p.startY - particleTime * p.speed) % 1f + 1f) % 1f
                val x = p.startX + sin(y * Math.PI * 2 + p.drift * 10).toFloat() * 0.03f
                drawCircle(
                    color = particleColor.copy(alpha = p.alpha),
                    radius = p.radius.dp.toPx(),
                    center = Offset(x * w, y * h)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.22f))

            // ── Logo + orbiting icons ──
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = logoAlpha
                        scaleX = logoScale
                        scaleY = logoScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer orbit ring
                val outerIcons = listOf(
                    Icons.Default.SportsMotorsports,
                    Icons.Default.SportsSoccer,
                    Icons.Default.SportsTennis,
                    Icons.Default.SportsKabaddi,
                    Icons.Default.SportsMartialArts,
                    Icons.Default.SportsHandball
                )

                outerIcons.forEachIndexed { index, icon ->
                    val angle = orbitRotation + (index * 60f)
                    val radius = 115.dp
                    val offsetX = cos(Math.toRadians(angle.toDouble())).toFloat()
                    val offsetY = sin(Math.toRadians(angle.toDouble())).toFloat()

                    Box(
                        modifier = Modifier
                            .offset(x = radius * offsetX, y = radius * offsetY)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg.copy(alpha = 0.85f))
                            .border(
                                width = 0.5.dp,
                                color = colors.border.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ILeaderColors.PrimaryRed.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Inner orbit ring
                val innerIcons = listOf(
                    Icons.Default.Timer,
                    Icons.Default.Leaderboard,
                    Icons.Default.Groups
                )

                innerIcons.forEachIndexed { index, icon ->
                    val angle = secondOrbitRotation + (index * 120f)
                    val radius = 70.dp
                    val offsetX = cos(Math.toRadians(angle.toDouble())).toFloat()
                    val offsetY = sin(Math.toRadians(angle.toDouble())).toFloat()

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.textMuted.copy(alpha = 0.3f),
                        modifier = Modifier
                            .offset(x = radius * offsetX, y = radius * offsetY)
                            .size(16.dp)
                    )
                }

                // Main logo with glow
                val logoRes = if (isDark) R.drawable.logo_dark else R.drawable.logo_light

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Outer glow
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .blur(30.dp)
                                .background(
                                    ILeaderColors.PrimaryRed.copy(alpha = glowAlpha * 0.5f),
                                    CircleShape
                                )
                        )
                        // Inner glow ring
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .blur(15.dp)
                                .background(
                                    ILeaderColors.LightRed.copy(alpha = glowAlpha * 0.3f),
                                    CircleShape
                                )
                        )
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = "iLeader",
                            modifier = Modifier
                                .size(80.dp)
                                .scale(pulseScale)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ──
            Text(
                text = "iLeader",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha
                    translationY = titleOffset
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "СПОРТИВНАЯ ПЛАТФОРМА",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                letterSpacing = 5.sp,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Stats row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = statsAlpha
                        translationY = statsOffset
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.cardBg.copy(alpha = 0.8f))
                    .border(
                        width = 0.5.dp,
                        color = colors.border.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = 22.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WelcomeStatItem(value = "10+", label = "Видов\nспорта")
                WelcomeStatDivider()
                WelcomeStatItem(value = "1000+", label = "Спортсменов")
                WelcomeStatDivider()
                WelcomeStatItem(value = "50+", label = "Турниров")
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // ── Tagline ──
            Text(
                text = "Турниры, команды, результаты —\nвсё в одном месте",
                fontSize = 15.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = buttonsAlpha
                    translationY = buttonsOffset
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Buttons ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = buttonsAlpha
                        translationY = buttonsOffset
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ILeaderButton(
                    text = "Создать аккаунт",
                    onClick = onNavigateToRegister,
                    icon = Icons.Default.PersonAdd
                )

                ILeaderOutlinedButton(
                    text = "Войти в аккаунт",
                    onClick = onNavigateToLogin,
                    icon = Icons.AutoMirrored.Filled.Login
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WelcomeStatItem(value: String, label: String) {
    val colors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ILeaderColors.PrimaryRed,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun WelcomeStatDivider() {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.border,
                        Color.Transparent
                    )
                )
            )
    )
}
