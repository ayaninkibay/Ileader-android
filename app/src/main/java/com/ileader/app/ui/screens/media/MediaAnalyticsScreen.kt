// TODO: Подключить к БД когда будет создана таблица articles
// Сейчас используются данные из MediaMockData. Аналитика привязана к контенту, которого нет в БД.
package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.mock.MediaMockData
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.DarkTheme

@Composable
fun MediaAnalyticsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    var selectedPeriod by remember { mutableIntStateOf(0) }

    val viewsData = when (selectedPeriod) {
        1 -> MediaMockData.viewsDataMonth
        else -> MediaMockData.viewsDataWeek
    }
    val totalPeriodViews = viewsData.sumOf { it.views }
    val maxViews = viewsData.maxOfOrNull { it.views } ?: 1

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible, 0) {
                Column {
                    Text("Аналитика", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── OVERVIEW STATS 2x2 ──
            FadeIn(visible, 150) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Visibility,
                        "${MediaMockData.totalViews}", "Просмотров")
                    StatItem(Modifier.weight(1f), Icons.Default.Description,
                        "${MediaMockData.publishedArticles}", "Статей")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.People,
                        "~${MediaMockData.totalViews / 3}", "Читателей")
                    StatItem(Modifier.weight(1f), Icons.Default.North,
                        "+18%", "Рост")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── VIEWS CHART ──
            FadeIn(visible, 300) {
                Text("Просмотры", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
                        Alignment.CenterVertically) {
                        Text("Всего за период: $totalPeriodViews",
                            fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Неделя", "Месяц").forEachIndexed { index, label ->
                                DarkFilterChip(label, selectedPeriod == index, { selectedPeriod = index })
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Simple bar chart
                    Row(
                        Modifier.fillMaxWidth().height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        viewsData.forEach { dataPoint ->
                            val fraction = dataPoint.views.toFloat() / maxViews.toFloat()
                            Column(
                                Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${dataPoint.views / 1000}k", fontSize = 9.sp,
                                    color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier.fillMaxWidth(0.7f).fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(DarkTheme.Accent.copy(alpha = 0.5f + fraction * 0.5f))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Labels
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        viewsData.forEach { dataPoint ->
                            Text(dataPoint.label, Modifier.weight(1f), fontSize = 10.sp,
                                color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── TOP ARTICLES ──
            FadeIn(visible, 450) {
                Text("Топ статьи", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    MediaMockData.topArticles.forEachIndexed { index, article ->
                        if (index > 0) Spacer(Modifier.height(10.dp))
                        TopArticleItem(rank = index + 1, article = article)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── CATEGORIES DISTRIBUTION ──
            FadeIn(visible, 600) {
                Text("По категориям", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    MediaMockData.categoryStats.forEach { stat ->
                        CategoryStatItem(stat = stat)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── AUDIENCE STATS ──
            FadeIn(visible, 750) {
                Text("Аудитория по возрасту", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    MediaMockData.audienceStats.forEach { stat ->
                        AudienceStatItem(stat = stat)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopArticleItem(rank: Int, article: MediaMockData.TopArticle) {
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
            Column(horizontalAlignment = Alignment.End) {
                Text("${article.views}", fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Text(article.change, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = if (article.trendUp) DarkTheme.Accent else DarkTheme.TextMuted)
            }
        }
    }
}

@Composable
private fun CategoryStatItem(stat: MediaMockData.CategoryStat) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stat.category.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = DarkTheme.TextPrimary, modifier = Modifier.width(80.dp))
        DarkProgressBar(stat.percentage / 100f, Modifier.weight(1f))
        Text("${stat.count} (${stat.percentage}%)", fontSize = 12.sp,
            color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AudienceStatItem(stat: MediaMockData.AudienceStat) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stat.age, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = DarkTheme.TextPrimary, modifier = Modifier.width(50.dp))
        DarkProgressBar(stat.percentage / 100f, Modifier.weight(1f))
        Text("${stat.percentage}%", fontSize = 12.sp,
            color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}
