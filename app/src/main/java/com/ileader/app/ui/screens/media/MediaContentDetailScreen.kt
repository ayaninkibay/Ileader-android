package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ArticleSaveState
import com.ileader.app.ui.viewmodels.MediaContentViewModel

@Composable
fun MediaContentDetailScreen(
    user: User,
    articleId: String,
    onBack: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    vm: MediaContentViewModel = viewModel()
) {
    val articleDetail by vm.articleDetail.collectAsState()
    val saveState by vm.saveState.collectAsState()

    LaunchedEffect(articleId) { vm.loadArticleDetail(articleId) }

    // Handle delete success
    LaunchedEffect(saveState) {
        if (saveState is ArticleSaveState.Success) {
            vm.resetSaveState()
            onBack()
        }
    }

    when (val d = articleDetail) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ArticleNotFound(d.message, onBack)
        is UiState.Success -> ArticleDetailBody(d.data, onBack, onEdit, vm)
        null -> ArticleNotFound("Статья не найдена", onBack)
    }
}

@Composable
private fun ArticleNotFound(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape)
                .background(DarkTheme.CardBorder.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Description, null, Modifier.size(30.dp), DarkTheme.TextMuted)
        }
        Spacer(Modifier.height(16.dp))
        Text("Статья не найдена", fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(message, fontSize = 13.sp,
            color = DarkTheme.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("К списку статей", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ArticleDetailBody(
    article: ArticleDto,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    vm: MediaContentViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить статью?", color = DarkTheme.TextPrimary) },
            text = { Text("Это действие нельзя отменить.", color = DarkTheme.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    vm.deleteArticle(article.id)
                }) {
                    Text("Удалить", color = DarkTheme.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = DarkTheme.TextSecondary)
                }
            },
            containerColor = DarkTheme.CardBg
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Top bar
            FadeIn(visible, 0) {
                Surface(Modifier.fillMaxWidth(), color = DarkTheme.CardBg) {
                    Row(
                        Modifier.fillMaxWidth()
                            .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(0.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            vm.clearDetail()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextPrimary)
                        }
                        Text("Статья", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEdit(article.id) }) {
                            Icon(Icons.Default.Edit, "Редактировать", tint = DarkTheme.Accent)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Удалить", tint = DarkTheme.Accent)
                        }
                    }
                }
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                // Status + Category badges
                FadeIn(visible, 100) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPublished = article.status == "published"
                        val statusLabel = if (isPublished) "Опубликовано" else "Черновик"
                        val statusColor = if (isPublished) DarkTheme.Accent else DarkTheme.TextMuted
                        StatusBadge(statusLabel, statusColor)
                        if (article.category != null) {
                            StatusBadge(categoryLabel(article.category), DarkTheme.Accent)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Title
                FadeIn(visible, 200) {
                    Text(article.title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp, lineHeight = 28.sp)
                }

                Spacer(Modifier.height(12.dp))

                // Meta info
                FadeIn(visible, 300) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (article.profiles?.name != null) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Person, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                                Text(article.profiles.name, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                            Text(formatDate(article.publishedAt ?: article.createdAt),
                                fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                        if (article.views > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Visibility, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                                Text("${article.views}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Content card
                FadeIn(visible, 400) {
                    DarkCardPadded {
                        Text(
                            text = if (!article.content.isNullOrEmpty()) article.content
                                   else article.excerpt ?: "Нет содержания",
                            fontSize = 15.sp, color = DarkTheme.TextSecondary, lineHeight = 24.sp
                        )
                    }
                }

                // Tags
                if (!article.tags.isNullOrEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    FadeIn(visible, 500) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            article.tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(6.dp),
                                    color = DarkTheme.CardBorder.copy(alpha = 0.5f)) {
                                    Text("#$tag",
                                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Sport info
                if (article.sports != null) {
                    Spacer(Modifier.height(12.dp))
                    FadeIn(visible, 550) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.SportsSoccer, null, Modifier.size(16.dp), DarkTheme.TextSecondary)
                            Text(article.sports.name, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Stats section
                FadeIn(visible, 600) {
                    Text("Статистика", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.2).sp)

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard(Modifier.weight(1f), Icons.Default.Visibility,
                            "Просмотров", "${article.views}")
                        DetailStatCard(Modifier.weight(1f), Icons.Default.DateRange,
                            "Создана", formatDate(article.createdAt))
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SoftIconBox(icon, size = 44.dp, iconSize = 22.dp)
            Column {
                Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
            }
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
