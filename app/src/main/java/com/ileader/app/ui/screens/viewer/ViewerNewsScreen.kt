package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.ViewerMockData
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerNewsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark

@Composable
fun ViewerNewsScreen(
    user: User,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val viewModel: ViewerNewsViewModel = viewModel()
    val articles by viewModel.articles.collectAsState()
    val categories = viewModel.categories

    var selectedFilter by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val filteredArticles = remember(selectedFilter, articles) {
        val category = categories.getOrNull(selectedFilter) ?: "Все"
        if (category == "Все") articles
        else articles.filter { it.category == category }
    }

    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        FadeIn(visible, 0) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Новости", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Хайлайты, репортажи и события из мира спорта", fontSize = 14.sp, color = TextSecondary)
                }
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
            }
        }

        Spacer(Modifier.height(20.dp))

        FadeIn(visible, 200) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEachIndexed { index, category ->
                    DarkFilterChip(
                        text = category,
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        FadeIn(visible, 400) {
            if (filteredArticles.isEmpty()) {
                EmptyState("Нет новостей", "В этой категории пока нет новостей")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredArticles.forEach { article ->
                        NewsCard(article) { onNavigateToDetail(article.id) }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun NewsCard(article: ViewerMockData.NewsArticle, onClick: () -> Unit) {
    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(article.category)
                Spacer(Modifier.width(8.dp))
                Text(formatDateRu(article.date), fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(10.dp))

            Text(article.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(6.dp))

            Text(article.summary, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(article.authorName, fontSize = 12.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Читать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                    Icon(Icons.Default.ChevronRight, null, tint = Accent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
