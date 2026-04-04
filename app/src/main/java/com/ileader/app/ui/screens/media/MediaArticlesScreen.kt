package com.ileader.app.ui.screens.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MediaViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

private val MediaColor = Color(0xFF06B6D4)

private enum class ArticleFilter(val label: String) {
    ALL("Все"), PUBLISHED("Опубликованные"), DRAFT("Черновики")
}

@Composable
fun MediaArticlesScreen(
    user: User,
    onBack: () -> Unit,
    onArticleClick: (String) -> Unit,
    onCreateArticle: () -> Unit,
    vm: MediaViewModel = viewModel()
) {
    val articles by vm.articles.collectAsState()
    val stats by vm.articleStats.collectAsState()
    val topArticles by vm.topArticles.collectAsState()
    val actionState by vm.actionState.collectAsState()

    var selectedFilter by remember { mutableStateOf(ArticleFilter.ALL) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    val snackbar = LocalSnackbarHost.current

    LaunchedEffect(user.id) {
        vm.loadArticles(user.id)
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is UiState.Success -> {
                snackbar.showSnackbar(s.data)
                vm.clearAction()
                deleteConfirmId = null
            }
            is UiState.Error -> {
                snackbar.showSnackbar(s.message)
                vm.clearAction()
            }
            else -> {}
        }
    }

    val allArticles = if (articles is UiState.Success) (articles as UiState.Success).data else emptyList()
    val published = allArticles.filter { it.status == "published" }
    val drafts = allArticles.filter { it.status == "draft" }
    val filtered = when (selectedFilter) {
        ArticleFilter.ALL -> allArticles
        ArticleFilter.PUBLISHED -> published
        ArticleFilter.DRAFT -> drafts
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── 1. Hero ──
        item {
            ArticlesHero(stats = stats, onBack = onBack)
            
        }

        // ── 2. Top articles ──
        if (topArticles.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Популярные статьи")
                
                Spacer(Modifier.height(8.dp))
            }
            item {
                TopArticlesRow(articles = topArticles, onClick = onArticleClick)
                
            }
        }

        // ── 3. Create button ──
        item {
            Spacer(Modifier.height(16.dp))
            CreateArticleButton(onClick = onCreateArticle)
            
        }

        // ── 4. Filter chips ──
        item {
            Spacer(Modifier.height(16.dp))
            ArticleFilterChips(
                selected = selectedFilter,
                onSelect = { selectedFilter = it },
                counts = mapOf(
                    ArticleFilter.ALL to allArticles.size,
                    ArticleFilter.PUBLISHED to published.size,
                    ArticleFilter.DRAFT to drafts.size
                )
            )
            
        }

        when (articles) {
            is UiState.Loading -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        LoadingScreen()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(
                            title = "Ошибка загрузки",
                            subtitle = (articles as UiState.Error).message
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (filtered.isEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        EmptyState(
                            title = if (allArticles.isEmpty()) "Нет статей" else "Нет статей",
                            subtitle = if (allArticles.isEmpty()) "Создайте первую статью — расскажите о турнире или спортсмене"
                            else "В этой категории пока нет статей",
                            icon = Icons.AutoMirrored.Filled.Article
                        )
                    }
                } else {
                    item { Spacer(Modifier.height(8.dp)) }
                    itemsIndexed(filtered, key = { _, it -> it.id }) { index, article ->
                        val delay = (160 + index * 50).coerceAtMost(500)
                        ArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.id) },
                            onDelete = { deleteConfirmId = article.id }
                        )
                        
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Удалить статью?", color = TextPrimary) },
            text = { Text("Это действие нельзя отменить", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { deleteConfirmId?.let { vm.deleteArticle(it, user.id) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════
// Hero
// ══════════════════════════════════════════════════════════

@Composable
private fun ArticlesHero(stats: ArticleStatsDto, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFC084FC)))
            )
        )
        Column(
            Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 20.dp)
        ) {
            // Back button
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Article, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("Мои статьи", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Публикации и черновики", fontSize = 14.sp, color = Color.White.copy(0.7f))

            Spacer(Modifier.height(16.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(Icons.AutoMirrored.Filled.Article, "${stats.total}", "Всего", Color.White.copy(0.2f))
                StatPill(Icons.Default.Public, "${stats.published}", "Опубликовано", Color(0xFF10B981).copy(0.4f))
                StatPill(Icons.Default.Edit, "${stats.drafts}", "Черновики", Color(0xFFF59E0B).copy(0.4f))
            }

            if (stats.totalViews > 0) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${formatViews(stats.totalViews)} просмотров",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, bg: Color) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(12.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(0.7f))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Top Articles Horizontal Row
// ══════════════════════════════════════════════════════════

@Composable
private fun TopArticlesRow(articles: List<ArticleDto>, onClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles, key = { it.id }) { article ->
            TopArticleCard(article = article, onClick = { onClick(article.id) })
        }
    }
}

@Composable
private fun TopArticleCard(article: ArticleDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = Modifier.width(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = 0.dp
    ) {
        Column {
            // Cover image or gradient
            Box(
                Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (article.coverImageUrl != null) {
                    AsyncImage(
                        model = article.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(Color(0xFF7C3AED).copy(0.8f), Color(0xFFA855F7).copy(0.5f)))
                        )
                    )
                }

                // Views badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(0.6f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${article.views}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(Modifier.padding(10.dp)) {
                Text(
                    article.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                article.category?.let { cat ->
                    Text(
                        getCategoryLabel(cat),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Create Article Button
// ══════════════════════════════════════════════════════════

@Composable
private fun CreateArticleButton(onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, MediaColor.copy(0.3f)),
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(
                    Brush.linearGradient(listOf(MediaColor, MediaColor.copy(0.7f)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Написать статью", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Расскажите о турнире или спортсмене", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Filter chips
// ══════════════════════════════════════════════════════════

@Composable
private fun ArticleFilterChips(
    selected: ArticleFilter,
    onSelect: (ArticleFilter) -> Unit,
    counts: Map<ArticleFilter, Int>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ArticleFilter.entries.toList()) { filter ->
            val isSelected = selected == filter
            val count = counts[filter] ?: 0
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFF7C3AED) else CardBg,
                border = if (!isSelected && DarkTheme.isDark) DarkTheme.cardBorderStroke else null,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                    if (count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Article Card
// ══════════════════════════════════════════════════════════

@Composable
private fun ArticleCard(
    article: ArticleDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    val isPublished = article.status == "published"
    val statusColor = if (isPublished) Color(0xFF10B981) else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = 0.dp
    ) {
        Row(Modifier.padding(12.dp)) {
            // Cover thumbnail
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
            ) {
                if (article.coverImageUrl != null) {
                    AsyncImage(
                        model = article.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF7C3AED).copy(0.6f),
                                    Color(0xFFA855F7).copy(0.3f)
                                )
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Article, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Title
                Text(
                    article.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Excerpt
                article.excerpt?.let { excerpt ->
                    Spacer(Modifier.height(2.dp))
                    Text(excerpt, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(6.dp))

                // Meta row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.15f)) {
                        Text(
                            if (isPublished) "Опубликована" else "Черновик",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }

                    // Category
                    article.category?.let { cat ->
                        Text(getCategoryLabel(cat), fontSize = 11.sp, color = TextMuted)
                    }

                    // Sport
                    article.sports?.name?.let { sport ->
                        Text(sport, fontSize = 11.sp, color = TextMuted)
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Views and date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (article.views > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${article.views}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        article.createdAt?.let { date ->
                            Text(formatDateShort(date), fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = TextMuted.copy(0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val m = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${m.getOrElse(month) { "" }}"
}

private fun formatViews(views: Int): String {
    return when {
        views >= 1_000_000 -> "${views / 1_000_000}M"
        views >= 1_000 -> "${views / 1_000}K"
        else -> "$views"
    }
}

private fun getCategoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "interview" -> "Интервью"
    "review" -> "Обзор"
    "analytics" -> "Аналитика"
    "preview" -> "Превью"
    "recap" -> "Итоги"
    "feature" -> "Репортаж"
    "opinion" -> "Мнение"
    "transfer" -> "Трансферы"
    "injury" -> "Травмы"
    "tournament_report" -> "Отчёт"
    "profile" -> "Профиль"
    else -> category.replaceFirstChar { it.uppercase() }
}
