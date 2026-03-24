package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.ViewerMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerHomeViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ViewerHomeScreen(
    user: User,
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToTournamentDetail: (String) -> Unit = {},
    onNavigateToNews: () -> Unit = {}
) {
    val viewModel: ViewerHomeViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val data = s.data
            var selectedSport by remember { mutableStateOf(data.sports.firstOrNull()?.id ?: "") }
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            val filteredTournaments = remember(selectedSport, data.upcomingTournaments) {
                data.upcomingTournaments
                    .filter { selectedSport.isBlank() || it.sportId == selectedSport }
                    .take(4)
            }

            val accentColor = Accent
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.03f),
                            radius = 280.dp.toPx()
                        ),
                        radius = 280.dp.toPx(),
                        center = Offset(size.width * 0.85f, size.height * 0.03f)
                    )
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero Banner
                    FadeIn(visible, 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Accent, AccentDark)))
                                .statusBarsPadding()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("СЕЗОН 2026", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "Спортивная рейтинговая\nплатформа iLeader",
                                    fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White,
                                    textAlign = TextAlign.Center, lineHeight = 32.sp
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Честные соревнования. Прозрачные рейтинги.",
                                    fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(16.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    StatBadge("${data.totalSports}+", "Видов спорта")
                                    StatBadge("${data.totalUsers}+", "Спортсменов")
                                    StatBadge("${data.totalTournaments}+", "Турниров")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Sports Selection
                    FadeIn(visible, 200) {
                        Text(
                            "Выбери вид спорта",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                            letterSpacing = (-0.3).sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    FadeIn(visible, 200) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(data.sports) { sport ->
                                SportChip(
                                    name = sport.name,
                                    isSelected = selectedSport == sport.id,
                                    isAvailable = sport.isActive,
                                    onClick = { if (sport.isActive) selectedSport = sport.id }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Upcoming Tournaments
                    FadeIn(visible, 400) {
                        Box(Modifier.padding(horizontal = 20.dp)) {
                            SectionHeader("Ближайшие турниры", "Все", onNavigateToTournaments)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FadeIn(visible, 400) {
                        if (filteredTournaments.isEmpty()) {
                            Box(Modifier.padding(horizontal = 20.dp)) {
                                EmptyState("Нет турниров", "По выбранному виду спорта пока нет турниров")
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                filteredTournaments.forEach { tournament ->
                                    HomeTournamentCard(tournament) { onNavigateToTournamentDetail(tournament.id) }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Features (stays on MockData — static content)
                    FadeIn(visible, 600) {
                        Text(
                            "Возможности платформы",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                            letterSpacing = (-0.3).sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    val featureIcons = listOf(
                        Icons.Default.EmojiEvents, Icons.Default.CalendarMonth,
                        Icons.Default.School, Icons.Default.BarChart
                    )

                    FadeIn(visible, 600) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ViewerMockData.features.forEachIndexed { index, feature ->
                                FeatureCard(featureIcons.getOrElse(index) { Icons.Default.Star }, feature.title, feature.description)
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Platform stats card
                    FadeIn(visible, 800) {
                        DarkCardPadded(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            padding = 20.dp
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PlatformStatItem("${data.totalUsers}", "Пользователей")
                                PlatformStatItem("${data.totalTournaments}", "Турниров")
                                PlatformStatItem("${data.totalSports}", "Видов спорта")
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Latest News (stays on MockData — no articles table)
                    FadeIn(visible, 1000) {
                        Box(Modifier.padding(horizontal = 20.dp)) {
                            SectionHeader("Последние новости", "Все", onNavigateToNews)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FadeIn(visible, 1000) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            data.news.take(3).forEach { article ->
                                HomeNewsCard(article)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────────────

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun SportChip(name: String, isSelected: Boolean, isAvailable: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(enabled = isAvailable) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Accent else CardBg,
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder(true).copy(
            width = 0.5.dp, brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = when {
                    isSelected -> Color.White
                    !isAvailable -> TextMuted
                    else -> TextSecondary
                }
            )
            if (!isAvailable) {
                Spacer(Modifier.height(2.dp))
                Text("Скоро", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Accent.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun HomeTournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    val status = tournament.status ?: ""
    val isActive = status == "registration_open" || status == "in_progress"
    val chipColor = if (isActive) Accent else TextMuted

    DarkCard(Modifier.clickable { onClick() }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    formatShortDate(tournament.startDate),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                StatusBadge(getStatusLabel(status), chipColor)
                Spacer(Modifier.height(4.dp))
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${tournament.region ?: ""} · ${tournament.participantCount}/${tournament.maxParticipants ?: 0}", fontSize = 12.sp, color = TextSecondary)
            }

            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, description: String) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SoftIconBox(icon)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PlatformStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Accent, letterSpacing = (-0.3).sp)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun HomeNewsCard(article: ViewerMockData.NewsArticle) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            AccentIconBox(Icons.Default.Newspaper)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(article.category)
                    Spacer(Modifier.width(8.dp))
                    Text(formatShortDate(article.date), fontSize = 12.sp, color = TextMuted)
                }
                Spacer(Modifier.height(6.dp))
                Text(article.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(article.summary, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────

internal fun formatDateRu(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    val year = parts[0]
    return "$day ${monthNames.getOrElse(month) { "" }} $year"
}

internal fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт сейчас"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    else -> status
}

@Composable
internal fun getStatusColor(status: String): Color = when (status) {
    "in_progress", "registration_open" -> Accent
    else -> TextMuted
}
