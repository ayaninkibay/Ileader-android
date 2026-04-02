package com.ileader.app.ui.screens.sport

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.RankingEntry
import com.ileader.app.ui.viewmodels.RankingsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft
private val Border: Color @Composable get() = LocalAppColors.current.border

// Medal colors
private val Gold = Color(0xFFCA8A04)
private val GoldBg = Color(0xFFFEF9C3)
private val Silver = Color(0xFF6B7280)
private val SilverBg = Color(0xFFF3F4F6)
private val Bronze = Color(0xFFB45309)
private val BronzeBg = Color(0xFFFEF3C7)

@Composable
fun RankingsScreen(
    userId: String?,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: RankingsViewModel = viewModel()
) {
    val s = viewModel.state
    val isDark = DarkTheme.isDark

    LaunchedEffect(Unit) { viewModel.init(userId) }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Hero Section ──
        item {
            HeroSection(
                totalAthletes = s.totalAthletes,
                maxPoints = s.maxPoints,
                totalTournaments = s.totalTournaments,
                isDark = isDark,
                onBack = onBack
            )
        }

        // ── Sport Filter Tabs ──
        item {
            FadeIn(started, 100) {
                SportFilterTabs(
                    sports = s.sports,
                    selectedSportId = s.selectedSportId,
                    onSelectSport = { viewModel.selectSport(it) }
                )
            }
        }

        // ── Rankings Table ──
        item {
            FadeIn(started, 200) {
                when (val entries = s.entries) {
                    is UiState.Loading -> Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent, strokeWidth = 2.dp)
                    }
                    is UiState.Error -> Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(title = "Ошибка", subtitle = entries.message)
                    }
                    is UiState.Success -> {
                        if (entries.data.isEmpty()) {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(title = "Нет данных", subtitle = "Спортсмены появятся позже")
                            }
                        } else {
                            RankingTable(
                                entries = entries.data,
                                isDark = isDark,
                                onProfileClick = onProfileClick
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Hero Section
// ═══════════════════════════════════════════════════

@Composable
private fun HeroSection(
    totalAthletes: Int,
    maxPoints: Int,
    totalTournaments: Int,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val accentColor = Accent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.15f),
                        accentColor.copy(alpha = 0.05f),
                        Bg
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardBg,
                    shadowElevation = if (isDark) 0.dp else 2.dp,
                    modifier = Modifier.size(40.dp).clickable(onClick = onBack)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад",
                            tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // Crown icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFFEF3C7),
                modifier = Modifier.size(56.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EmojiEvents, null,
                        tint = Color(0xFFCA8A04), modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Title
            Text(
                "Глобальный рейтинг",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Лучшие спортсмены платформы",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            // Stat cards
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    icon = Icons.Default.People,
                    value = "$totalAthletes",
                    label = "Спортсменов",
                    color = Color(0xFF3B82F6),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Default.Leaderboard,
                    value = "$maxPoints",
                    label = "Макс. рейтинг",
                    color = Color(0xFFF59E0B),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Default.EmojiEvents,
                    value = "$totalTournaments",
                    label = "Турниров",
                    color = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatMiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp,
        border = if (isDark) BorderStroke(1.dp, Border.copy(0.2f)) else null
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextMuted, maxLines = 1)
        }
    }
}

// ═══════════════════════════════════════════════════
// Sport Filter Tabs
// ═══════════════════════════════════════════════════

@Composable
private fun SportFilterTabs(
    sports: List<com.ileader.app.data.remote.dto.SportDto>,
    selectedSportId: String?,
    onSelectSport: (String?) -> Unit
) {
    val isDark = DarkTheme.isDark

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        // "All" chip
        item {
            val isSelected = selectedSportId == null
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) Accent else CardBg,
                border = if (!isSelected && isDark) BorderStroke(1.dp, Border.copy(0.3f))
                else if (!isSelected) BorderStroke(1.dp, Border.copy(0.2f)) else null,
                shadowElevation = if (isDark || isSelected) 0.dp else 1.dp,
                modifier = Modifier.clickable { onSelectSport(null) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Apps, null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Все", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        // Sport chips
        itemsIndexed(sports) { _, sport ->
            val isSelected = sport.id == selectedSportId
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) Accent else CardBg,
                border = if (!isSelected && isDark) BorderStroke(1.dp, Border.copy(0.3f))
                else if (!isSelected) BorderStroke(1.dp, Border.copy(0.2f)) else null,
                shadowElevation = if (isDark || isSelected) 0.dp else 1.dp,
                modifier = Modifier.clickable { onSelectSport(sport.id) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(sportIcon(sport.name), null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        sport.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Ranking Table
// ═══════════════════════════════════════════════════

@Composable
private fun RankingTable(
    entries: List<RankingEntry>,
    isDark: Boolean,
    onProfileClick: (String) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        // Table header
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = CardBg,
            border = if (isDark) BorderStroke(1.dp, Border.copy(0.2f)) else null,
            shadowElevation = if (isDark) 0.dp else 2.dp
        ) {
            Column {
                // Header row
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, modifier = Modifier.width(32.dp))
                    Text("Спортсмен", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, modifier = Modifier.weight(1f))
                    Text("Рейтинг", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, textAlign = TextAlign.End,
                        modifier = Modifier.width(70.dp))
                }

                HorizontalDivider(color = Border.copy(0.3f), thickness = 0.5.dp)
            }
        }

        // Entries
        entries.forEachIndexed { index, entry ->
            val animDelay = 100 + index * 40
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(animDelay.toLong())
                visible = true
            }
            val alpha by animateFloatAsState(
                if (visible) 1f else 0f,
                tween(350, easing = FastOutSlowInEasing), label = "row$index"
            )
            val offset by animateFloatAsState(
                if (visible) 0f else 20f,
                tween(350, easing = EaseOutBack), label = "rowOff$index"
            )

            val isLast = index == entries.lastIndex
            val shape = if (isLast) RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            else RoundedCornerShape(0.dp)

            Surface(
                shape = shape,
                color = when {
                    entry.isCurrentUser -> Accent.copy(alpha = 0.08f)
                    else -> CardBg
                },
                border = if (isDark) BorderStroke(0.5.dp, Border.copy(0.15f)) else null,
                modifier = Modifier
                    .graphicsLayer { translationY = offset; this.alpha = alpha }
                    .clickable { onProfileClick(entry.athleteId) }
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank badge
                        RankBadge(entry.rank, isDark)

                        Spacer(Modifier.width(12.dp))

                        // Avatar
                        if (entry.avatarUrl != null) {
                            AsyncImage(
                                model = entry.avatarUrl,
                                contentDescription = entry.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = when (entry.rank) {
                                            1 -> Gold
                                            2 -> Silver
                                            3 -> Bronze
                                            else -> Border.copy(0.3f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        when (entry.rank) {
                                            1 -> Gold.copy(0.15f)
                                            2 -> Silver.copy(0.15f)
                                            3 -> Bronze.copy(0.15f)
                                            else -> Accent.copy(0.1f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    entry.name.take(1).uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (entry.rank) {
                                        1 -> Gold
                                        2 -> Silver
                                        3 -> Bronze
                                        else -> Accent
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Name + city
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    entry.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (entry.isCurrentUser) Accent else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (entry.isCurrentUser) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Accent.copy(0.15f)
                                    ) {
                                        Text(
                                            "Вы", fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Accent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                entry.city?.let { city ->
                                    Text(
                                        city, fontSize = 12.sp,
                                        color = TextMuted, maxLines = 1
                                    )
                                }
                                entry.sportName?.let { sport ->
                                    if (entry.city != null) {
                                        Text(" · ", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Text(sport, fontSize = 12.sp, color = TextMuted, maxLines = 1)
                                }
                            }
                        }

                        // Rating
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.width(70.dp)
                        ) {
                            Text(
                                "${entry.rating}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (entry.rank) {
                                    1 -> Gold
                                    2 -> Silver
                                    3 -> Bronze
                                    else -> TextPrimary
                                }
                            )
                            Text(
                                "очков", fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (!isLast) {
                        HorizontalDivider(
                            color = Border.copy(0.15f), thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Rank Badge
// ═══════════════════════════════════════════════════

@Composable
private fun RankBadge(rank: Int, isDark: Boolean) {
    val (bgColor, textColor) = when (rank) {
        1 -> if (isDark) Pair(Gold.copy(0.2f), Gold) else Pair(GoldBg, Gold)
        2 -> if (isDark) Pair(Silver.copy(0.2f), Silver) else Pair(SilverBg, Silver)
        3 -> if (isDark) Pair(Bronze.copy(0.2f), Bronze) else Pair(BronzeBg, Bronze)
        else -> Pair(Color.Transparent, TextMuted)
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .then(
                if (rank <= 3) Modifier.background(bgColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (rank <= 3) {
            Text(
                when (rank) { 1 -> "1"; 2 -> "2"; 3 -> "3"; else -> "$rank" },
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        } else {
            Text(
                "$rank", fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
