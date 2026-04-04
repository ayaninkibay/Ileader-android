package com.ileader.app.ui.screens.onboarding

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.ileader.app.ui.components.sportIcon
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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Static background glow
        val bgAlpha = if (isDark) 0.08f else 0.04f
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ILeaderColors.PrimaryRed.copy(alpha = bgAlpha),
                radius = size.width * 0.35f,
                center = Offset(
                    size.width * 0.35f,
                    size.height * 0.15f
                )
            )
            drawCircle(
                color = ILeaderColors.DarkRed.copy(alpha = bgAlpha * 0.5f),
                radius = size.width * 0.25f,
                center = Offset(
                    size.width * 0.75f,
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
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Выберите 1-3 вида спорта",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
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
                            SportCard(
                                sport = sport,
                                isSelected = sport.id in selectedIds,
                                onClick = { viewModel.toggleSport(sport.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Box {
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

    val borderColor = if (isSelected) Accent else Border
    val bgColor = if (isSelected) Accent.copy(alpha = 0.12f) else CardBg

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
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
                Icon(
                    sportIcon(sport.name), null,
                    tint = TextSecondary,
                    modifier = Modifier.size(36.dp)
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
