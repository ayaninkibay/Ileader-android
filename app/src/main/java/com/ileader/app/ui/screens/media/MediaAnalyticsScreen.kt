package com.ileader.app.ui.screens.media

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.data.repository.MediaRepository
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryStat(val category: String, val count: Int, val percentage: Int)

@Composable
fun MediaAnalyticsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val vm: MediaAnalyticsViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> AnalyticsContent(user, s.data)
    }
}

@Composable
private fun AnalyticsContent(user: User, data: MediaAnalyticsData) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val totalArticles = data.stats.total
    val totalViews = data.stats.totalViews
    val publishedArticles = data.stats.published

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            FadeIn(visible, 0) {
                Column {
                    Text("Аналитика", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Overview stats 2x2
            FadeIn(visible, 150) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.Visibility,
                            "$totalViews", "Просмотров")
                        StatItem(Modifier.weight(1f), Icons.Default.Description,
                            "$publishedArticles", "Опубликовано")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.Drafts,
                            "${data.stats.drafts}", "Черновиков")
                        StatItem(Modifier.weight(1f), Icons.Default.Article,
                            "$totalArticles", "Всего статей")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Top articles by views
            FadeIn(visible, 350) {
                Text("Топ статьи по просмотрам", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    if (data.topArticles.isEmpty()) {
                        EmptyState("Нет данных", "Статьи пока не опубликованы")
                    } else {
                        data.topArticles.forEachIndexed { index, article ->
                            if (index > 0) Spacer(Modifier.height(10.dp))
                            TopArticleItem(rank = index + 1, article = article)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Categories distribution
            FadeIn(visible, 500) {
                Text("По категориям", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    if (data.categoryStats.isEmpty()) {
                        EmptyState("Нет данных", "Нет статистики по категориям")
                    } else {
                        data.categoryStats.forEach { stat ->
                            CategoryStatItem(stat = stat)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // TODO: Audience stats require analytics service not yet available in DB
            FadeIn(visible, 650) {
                Text("Аудитория", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    EmptyState("Скоро", "Данные об аудитории пока недоступны")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopArticleItem(rank: Int, article: ArticleDto) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                Modifier.size(28.dp), shape = RoundedCornerShape(8.dp),
                color = if (rank <= 3) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$rank", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) DarkTheme.Accent else DarkTheme.TextMuted)
                }
            }
            Text(article.title, fontSize = 13.sp, color = DarkTheme.TextPrimary,
                modifier = Modifier.weight(1f), maxLines = 1,
                overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("${article.views}", fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun CategoryStatItem(stat: CategoryStat) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stat.category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = DarkTheme.TextPrimary, modifier = Modifier.width(80.dp))
        DarkProgressBar(stat.percentage / 100f, Modifier.weight(1f))
        Text("${stat.count} (${stat.percentage}%)", fontSize = 12.sp,
            color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

// ViewModel for analytics - fetches real data from repository
class MediaAnalyticsViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaAnalyticsData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaAnalyticsData>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val stats = repo.getArticleStats(userId)
                val topArticles = repo.getTopArticlesByViews(userId, 5)
                val categoryMap = repo.getArticlesByCategory(userId)

                val totalArticles = stats.total.coerceAtLeast(1)
                val categoryStats = categoryMap.map { (cat, count) ->
                    CategoryStat(
                        category = categoryLabel(cat),
                        count = count,
                        percentage = (count * 100) / totalArticles
                    )
                }.sortedByDescending { it.count }

                _state.value = UiState.Success(
                    MediaAnalyticsData(
                        stats = stats,
                        topArticles = topArticles,
                        categoryStats = categoryStats
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}

data class MediaAnalyticsData(
    val stats: ArticleStatsDto,
    val topArticles: List<ArticleDto>,
    val categoryStats: List<CategoryStat>
)

private fun categoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "interview" -> "Интервью"
    "review" -> "Обзор"
    "analysis" -> "Аналитика"
    "announcement" -> "Анонс"
    "report" -> "Репортаж"
    "opinion" -> "Мнение"
    "training" -> "Тренировки"
    "equipment" -> "Оборудование"
    "health" -> "Здоровье"
    "event" -> "Событие"
    "other" -> "Другое"
    else -> category
}
