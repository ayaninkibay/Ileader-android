package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ArticleDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit,
    viewModel: ArticleDetailViewModel = viewModel()
) {
    LaunchedEffect(articleId) { viewModel.load(articleId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        when (val state = viewModel.state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> {
                BackHeader(title = "Статья", onBack = onBack)
                ErrorScreen(state.message, onRetry = { viewModel.load(articleId) })
            }
            is UiState.Success -> {
                val article = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Hero image with overlay back button ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        if (!article.coverImageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = article.coverImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFE53535), Color(0xFFFF6B6B))
                                        )
                                    )
                            )
                        }

                        // Top gradient for status bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        )
                        // Bottom gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )

                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp).size(22.dp)
                                )
                            }
                        }

                        // Category badge
                        article.category?.let { cat ->
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Accent
                            ) {
                                Text(
                                    getCategoryLabel(cat),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // ── Content ──
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(20.dp))

                        // Title
                        Text(
                            text = article.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 30.sp
                        )
                        

                        Spacer(Modifier.height(14.dp))

                        // Author + date card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Author avatar
                                Surface(
                                    shape = CircleShape,
                                    color = Accent.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.Person, null, tint = Accent, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = article.profiles?.name ?: "Автор",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(12.dp), tint = TextMuted)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = formatDateRu(article.publishedAt ?: article.createdAt),
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                // Sport badge
                                article.sports?.let { sport ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TextMuted.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(sportIcon(sport.name), null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(sport.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                        

                        Spacer(Modifier.height(20.dp))

                        // Views + reading time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (article.views > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Visibility, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${article.views}", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            val wordCount = (article.content?.length ?: 0) / 5
                            val readMinutes = (wordCount / 200).coerceAtLeast(1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("$readMinutes мин. чтения", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        

                        Spacer(Modifier.height(16.dp))

                        // Excerpt (if any)
                        if (!article.excerpt.isNullOrEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentSoft.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    article.excerpt,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                        }

                        // Article content with simple markdown rendering
                        if (!article.content.isNullOrEmpty()) {
                            RenderArticleContent(article.content)
                            
                        }

                        // Tags
                        if (!article.tags.isNullOrEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                article.tags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = TextMuted.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "#$tag",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderArticleContent(content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        content.split("\n").forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> Spacer(Modifier.height(8.dp))
                trimmed.startsWith("## ") -> {
                    Spacer(Modifier.height(12.dp))
                    Text(trimmed.removePrefix("## "), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 26.sp)
                    Spacer(Modifier.height(4.dp))
                }
                trimmed.startsWith("### ") -> {
                    Spacer(Modifier.height(8.dp))
                    Text(trimmed.removePrefix("### "), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, lineHeight = 24.sp)
                    Spacer(Modifier.height(2.dp))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(Modifier.padding(start = 8.dp)) {
                        Text("•", fontSize = 16.sp, color = TextSecondary, modifier = Modifier.width(16.dp))
                        Text(
                            trimmed.removePrefix("- ").removePrefix("* "),
                            fontSize = 16.sp, color = TextPrimary, lineHeight = 26.sp
                        )
                    }
                }
                trimmed.startsWith("> ") -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = CardBg
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            Box(Modifier.width(3.dp).height(IntrinsicSize.Min).background(Accent, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                trimmed.removePrefix("> "),
                                fontSize = 15.sp, color = TextSecondary, lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> Text(
                    trimmed, fontSize = 16.sp, color = TextPrimary, lineHeight = 26.sp
                )
            }
        }
    }
}

private fun formatDateRu(dateStr: String?): String {
    if (dateStr == null) return ""
    val parts = dateStr.take(10).split("-")
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

private fun getCategoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "report" -> "Отчёт"
    "interview" -> "Интервью"
    "analytics" -> "Аналитика"
    "review" -> "Обзор"
    "announcement" -> "Анонс"
    "highlight" -> "Главное"
    "preview" -> "Превью"
    else -> category.replaceFirstChar { it.uppercase() }
}
