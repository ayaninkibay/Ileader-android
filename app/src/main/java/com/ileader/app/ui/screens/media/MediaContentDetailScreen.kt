// TODO: Подключить к БД когда будет создана таблица articles
// Сейчас используются данные из MediaMockData
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
import com.ileader.app.data.mock.MediaMockData
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.*

@Composable
fun MediaContentDetailScreen(
    user: User,
    articleId: String,
    onBack: () -> Unit = {},
    onEdit: (String) -> Unit = {}
) {
    val article = MediaMockData.getArticleById(articleId)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (article == null) {
                // Article not found
                Column(
                    modifier = Modifier.fillMaxSize().padding(40.dp),
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
                    Text("Возможно, она была удалена", fontSize = 13.sp,
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
            } else {
                // Top bar
                FadeIn(visible, 0) {
                    Surface(Modifier.fillMaxWidth(), color = DarkTheme.CardBg) {
                        Row(
                            Modifier.fillMaxWidth()
                                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(0.dp))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextPrimary)
                            }
                            Text("Статья", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onEdit(article.id) }) {
                                Icon(Icons.Default.Edit, "Редактировать", tint = DarkTheme.Accent)
                            }
                            IconButton(onClick = { onBack() /* Mock: удаление будет с БД */ }) {
                                Icon(Icons.Default.Delete, "Удалить", tint = DarkTheme.Accent)
                            }
                        }
                    }
                }

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Status + Category badges
                    FadeIn(visible, 100) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isPublished = article.status == MediaMockData.ContentStatus.PUBLISHED
                            val statusColor = if (isPublished) DarkTheme.Accent else DarkTheme.TextMuted
                            StatusBadge(article.status.label, statusColor)
                            StatusBadge(article.category.label, DarkTheme.Accent)
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
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Person, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                                Text(article.authorName, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                                Text(article.publishedAt ?: article.createdAt, fontSize = 13.sp, color = DarkTheme.TextSecondary)
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
                                text = if (article.content.isNotEmpty()) article.content else article.excerpt,
                                fontSize = 15.sp, color = DarkTheme.TextSecondary, lineHeight = 24.sp
                            )
                        }
                    }

                    // Tags
                    if (article.tags.isNotEmpty()) {
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
                                "Создана", article.createdAt)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
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
