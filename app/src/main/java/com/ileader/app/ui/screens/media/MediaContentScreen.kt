package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.MediaContentData
import com.ileader.app.ui.viewmodels.MediaContentViewModel

@Composable
fun MediaContentScreen(user: User) {
    val vm: MediaContentViewModel = viewModel()
    val state by vm.state.collectAsState()
    val articleDetail by vm.articleDetail.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    // Article detail sub-screen
    if (articleDetail != null) {
        when (val d = articleDetail) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(d.message) { vm.clearDetail() }
            is UiState.Success -> ArticleDetailContent(d.data) { vm.clearDetail() }
            else -> {}
        }
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> ContentListScreen(user, s.data, vm)
    }
}

@Composable
private fun ContentListScreen(user: User, data: MediaContentData, vm: MediaContentViewModel) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val statusFilters = listOf("Все", "Опубликовано", "Черновики")

    val filteredArticles = data.articles.filter { article ->
        val matchesSearch = searchQuery.isEmpty() ||
                article.title.lowercase().contains(searchQuery.lowercase())
        val matchesFilter = when (selectedFilter) {
            1 -> article.status == "published"
            2 -> article.status == "draft"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Column {
                    Text("Мои статьи", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            FadeIn(visible, 150) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Всего", "${data.stats.total}", Modifier.weight(1f))
                    MiniStat("Опубл.", "${data.stats.published}", Modifier.weight(1f))
                    MiniStat("Просмотры", "${data.stats.totalViews}", Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 250) {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                    Row(
                        Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                            singleLine = true, cursorBrush = SolidColor(DarkTheme.Accent),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Поиск статей...", fontSize = 14.sp, color = DarkTheme.TextMuted)
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp), DarkTheme.TextMuted)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            FadeIn(visible, 350) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statusFilters.forEachIndexed { index, filter ->
                        DarkFilterChip(filter, selectedFilter == index, { selectedFilter = index })
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FadeIn(visible, 450) {
                if (filteredArticles.isEmpty()) {
                    EmptyState("Статьи не найдены", "Создавайте статьи на сайте ileader.kz")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredArticles.forEach { article ->
                            ArticleCard(article) { vm.loadArticleDetail(article.id) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleDto, onClick: () -> Unit) {
    val isPublished = article.status == "published"
    val statusLabel = if (isPublished) "Опубликовано" else "Черновик"
    val statusColor = if (isPublished) DarkTheme.Accent else DarkTheme.TextMuted

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = DarkTheme.CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Text(article.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(8.dp))
                StatusBadge(statusLabel, statusColor)
            }
            if (!article.excerpt.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(article.excerpt, fontSize = 12.sp, color = DarkTheme.TextSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (article.category != null) {
                    StatusBadge(categoryLabel(article.category), DarkTheme.Accent)
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (article.views > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Visibility, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                            Text("${article.views}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                        }
                    }
                    Text(article.publishedAt?.take(10) ?: article.createdAt?.take(10) ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
            }
            if (!article.tags.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    article.tags.take(3).forEach { tag ->
                        Surface(shape = RoundedCornerShape(6.dp), color = DarkTheme.CardBorder.copy(alpha = 0.5f)) {
                            Text("#$tag", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp, color = DarkTheme.TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleDetailContent(article: ArticleDto, onBack: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Статья", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            FadeIn(visible, 100) {
                Column {
                    Text(article.title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, lineHeight = 28.sp)

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val statusLabel = if (article.status == "published") "Опубликовано" else "Черновик"
                        val statusColor = if (article.status == "published") DarkTheme.Accent else DarkTheme.TextMuted
                        StatusBadge(statusLabel, statusColor)
                        if (article.category != null) {
                            StatusBadge(categoryLabel(article.category), DarkTheme.Accent)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (article.profiles?.name != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Person, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                                Text(article.profiles.name, fontSize = 12.sp, color = DarkTheme.TextSecondary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                            Text(article.publishedAt?.take(10) ?: article.createdAt?.take(10) ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Visibility, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                            Text("${article.views}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            FadeIn(visible, 200) {
                if (!article.content.isNullOrEmpty()) {
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DarkTheme.CardBg,
                        border = DarkTheme.cardBorderStroke
                    ) {
                        Text(
                            article.content,
                            Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = DarkTheme.TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                } else if (!article.excerpt.isNullOrEmpty()) {
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DarkTheme.CardBg,
                        border = DarkTheme.cardBorderStroke
                    ) {
                        Text(
                            article.excerpt,
                            Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = DarkTheme.TextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (!article.tags.isNullOrEmpty()) {
                Spacer(Modifier.height(16.dp))
                FadeIn(visible, 300) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        article.tags.forEach { tag ->
                            Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.AccentSoft) {
                                Text("#$tag", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp, color = DarkTheme.Accent, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (article.sports != null) {
                Spacer(Modifier.height(12.dp))
                FadeIn(visible, 350) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.SportsSoccer, null, Modifier.size(16.dp), DarkTheme.TextSecondary)
                        Text(article.sports.name, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

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
