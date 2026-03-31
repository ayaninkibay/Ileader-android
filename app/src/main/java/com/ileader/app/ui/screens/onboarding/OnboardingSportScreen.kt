package com.ileader.app.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.GradientButton
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.sportEmoji
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.OnboardingViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = DarkTheme.CardBorder

@Composable
fun OnboardingSportScreen(
    userId: String,
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    val sportsState by viewModel.sportsState.collectAsState()
    val selectedIds by viewModel.selectedSportIds.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current

    // Entrance animations
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100),
        label = "titleAlpha"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = tween(700, delayMillis = 100, easing = EaseOutBack),
        label = "titleOffset"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 800),
        label = "buttonAlpha"
    )

    // Background glow
    val infiniteTransition = rememberInfiniteTransition(label = "onboardBg")
    val glowShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Subtle animated background
        val bgAlpha = if (isDark) 0.08f else 0.04f
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ILeaderColors.PrimaryRed.copy(alpha = bgAlpha),
                radius = size.width * 0.35f,
                center = Offset(
                    size.width * (0.2f + glowShift * 0.3f),
                    size.height * 0.15f
                )
            )
            drawCircle(
                color = ILeaderColors.DarkRed.copy(alpha = bgAlpha * 0.5f),
                radius = size.width * 0.25f,
                center = Offset(
                    size.width * (0.85f - glowShift * 0.2f),
                    size.height * 0.8f
                )
            )
        }

        when (val state = sportsState) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.retry() }
            )
            is UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))

                    // Title with animation
                    Text(
                        text = "Выберите вид спорта",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = titleAlpha
                            translationY = titleOffset
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Выберите 1-3 вида спорта",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = titleAlpha
                            translationY = titleOffset
                        }
                    )

                    // Selected count indicator
                    if (selectedIds.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Accent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Выбрано: ${selectedIds.size}/3",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Accent
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(state.data) { index, sport ->
                            val delay = (index * 80).coerceAtMost(500)
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay((300 + delay).toLong())
                                itemVisible = true
                            }
                            val itemAlpha by animateFloatAsState(
                                targetValue = if (itemVisible) 1f else 0f,
                                animationSpec = tween(400),
                                label = "sportAlpha$index"
                            )
                            val itemScale by animateFloatAsState(
                                targetValue = if (itemVisible) 1f else 0.8f,
                                animationSpec = tween(400, easing = EaseOutBack),
                                label = "sportScale$index"
                            )

                            Box(
                                modifier = Modifier.graphicsLayer {
                                    alpha = itemAlpha
                                    scaleX = itemScale
                                    scaleY = itemScale
                                }
                            ) {
                                SportCard(
                                    sport = sport,
                                    isSelected = sport.id in selectedIds,
                                    onClick = { viewModel.toggleSport(sport.id) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.graphicsLayer { alpha = buttonAlpha }
                    ) {
                        GradientButton(
                            text = if (isSaving) "" else "Продолжить",
                            onClick = {
                                viewModel.saveSports(userId, context, onComplete)
                            },
                            enabled = selectedIds.isNotEmpty() && !isSaving,
                            loading = isSaving
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SportCard(
    sport: SportDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    // Animate selection
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Accent else Border,
        animationSpec = tween(250),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Accent.copy(alpha = 0.12f) else CardBg,
        animationSpec = tween(250),
        label = "bgColor"
    )
    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selectScale"
    )

    // Tap bounce
    var justTapped by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        targetValue = if (justTapped) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "tapScale"
    )
    LaunchedEffect(justTapped) {
        if (justTapped) {
            kotlinx.coroutines.delay(120)
            justTapped = false
        }
    }

    Surface(
        onClick = {
            justTapped = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .scale(tapScale * selectScale),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Checkmark for selected
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sportEmoji(sport.name),
                    fontSize = 36.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = sport.name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Accent else TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
