package com.ileader.app.ui.screens.auth

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.ILeaderOutlinedButton
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val colors = LocalAppColors.current

    // Entrance animations
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "logoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 700),
        label = "buttonsAlpha"
    )

    val statsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 500),
        label = "statsAlpha"
    )

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    val secondOrbitRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondOrbit"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.25f))

            // Logo + orbiting icons
            Box(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg.copy(alpha = 0.8f))
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
                            tint = ILeaderColors.PrimaryRed.copy(alpha = 0.5f),
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

                // Main trophy icon with glow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .blur(20.dp)
                                .background(
                                    ILeaderColors.PrimaryRed.copy(alpha = glowAlpha * 0.4f),
                                    CircleShape
                                )
                        )
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "iLeader",
                            tint = ILeaderColors.PrimaryRed,
                            modifier = Modifier
                                .size(72.dp)
                                .scale(pulseScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "iLeader",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "СПОРТИВНАЯ ПЛАТФОРМА",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(statsAlpha)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardBg)
                    .border(
                        width = 0.5.dp,
                        color = colors.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 20.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WelcomeStatItem(value = "10+", label = "Видов\nспорта")
                WelcomeStatDivider()
                WelcomeStatItem(value = "1000+", label = "Спортсменов")
                WelcomeStatDivider()
                WelcomeStatItem(value = "50+", label = "Турниров")
            }

            Spacer(modifier = Modifier.weight(0.35f))

            // Tagline
            Text(
                text = "Турниры, команды, результаты —\nвсё в одном месте",
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
                modifier = Modifier.alpha(buttonsAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(buttonsAlpha),
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
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ILeaderColors.PrimaryRed,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
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
