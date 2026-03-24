package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun ViewerNewsDetailScreen(
    articleId: String,
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: ViewerNewsViewModel = viewModel()
    val articles by viewModel.articles.collectAsState()

    val article = remember(articleId, articles) {
        articles.find { it.id == articleId }
    }

    if (article == null) {
        NotFoundScreen(message = "Статья не найдена", onBack = onBack)
        return
    }

    val relatedArticles = remember(articleId, articles) {
        articles.filter { it.id != articleId }.take(3)
    }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).statusBarsPadding()
    ) {
        FadeIn(visible = started, delayMs = 0) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
            }
            Text("Новости", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
        }
        }

        Spacer(Modifier.height(4.dp))

        FadeIn(visible = started, delayMs = 150) {
        DarkCardPadded(modifier = Modifier.padding(horizontal = 20.dp), padding = 20.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(article.category)
                Spacer(Modifier.width(8.dp))
                Text(formatDateRu(article.date), fontSize = 12.sp, color = TextMuted)
            }
            Spacer(Modifier.height(16.dp))
            Text(article.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 28.sp, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Accent, AccentDark))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(article.authorName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(Modifier.height(20.dp))
            Text(article.content, fontSize = 15.sp, color = TextSecondary, lineHeight = 24.sp)
        }
        }

        Spacer(Modifier.height(28.dp))

        FadeIn(visible = started, delayMs = 300) {
        Column {
        if (relatedArticles.isNotEmpty()) {
            Text("Другие новости", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                relatedArticles.forEach { related ->
                    DarkCard {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(related.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2)
                                Spacer(Modifier.height(2.dp))
                                Text(formatDateRu(related.date), fontSize = 12.sp, color = TextMuted)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        }
        }

        Spacer(Modifier.height(32.dp))
    }
}
