package com.ileader.app.ui.screens.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.MediaDashboardData
import com.ileader.app.ui.viewmodels.MediaDashboardViewModel

@Composable
fun MediaDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: MediaDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DashboardContent(user, s.data, onNavigate)
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: MediaDashboardData,
    onNavigate: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = DarkTheme.Accent
    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    ILeaderBrandHeader(role = user.role)
                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 200) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Badge,
                        "${data.accreditationStats.total}", "Аккредитаций")
                    StatItem(Modifier.weight(1f), Icons.Default.CheckCircle,
                        "${data.accreditationStats.accepted}", "Одобрено")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Schedule,
                        "${data.accreditationStats.pending}", "На рассмотрении")
                    StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents,
                        "${data.upcomingTournaments.size}", "Турниров")
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 350) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavigate("media/content") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Article, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Мои статьи", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onNavigate("media/tournaments") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.Accent),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.Accent.copy(alpha = 0.5f), DarkTheme.Accent.copy(alpha = 0.2f)))
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Newspaper, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Аккредитация", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 500) {
                SectionHeader("Публикации", "Все") { onNavigate("media/content") }
                Spacer(Modifier.height(12.dp))

                if (data.recentArticles.isEmpty()) {
                    EmptyState("Нет статей", "Создавайте статьи на сайте ileader.kz")
                } else {
                    data.recentArticles.forEach { article ->
                        ArticlePreviewItem(article)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 700) {
                SectionHeader("Предстоящие турниры", "Все") { onNavigate("media/tournaments") }
                Spacer(Modifier.height(12.dp))

                if (data.upcomingTournaments.isEmpty()) {
                    EmptyState("Нет турниров")
                } else {
                    data.upcomingTournaments.forEach { tournament ->
                        TournamentPreviewItem(tournament)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 800) {
                SectionHeader("Статистика контента")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Просмотров", "${data.articleStats.totalViews}", Modifier.weight(1f))
                    MiniStat("Статей", "${data.articleStats.published}", Modifier.weight(1f))
                    MiniStat("Черновиков", "${data.articleStats.drafts}", Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ArticlePreviewItem(article: ArticleDto) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.AutoMirrored.Filled.Article)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(article.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${article.category ?: "Статья"} · ${article.views} просм.",
                    fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
        }
    }
}

@Composable
private fun TournamentPreviewItem(tournament: TournamentWithCountsDto) {
    val isActive = isTournamentActive(tournament.status)
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${formatDate(tournament.startDate)} · ${tournament.locationName ?: ""}",
                    fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            StatusBadge(tournamentStatusLabel(tournament.status),
                if (isActive) DarkTheme.Accent else DarkTheme.TextMuted)
        }
    }
}
