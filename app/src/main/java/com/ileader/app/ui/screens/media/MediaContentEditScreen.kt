// TODO: Подключить к БД когда будет создана таблица articles
// Сейчас используются данные из MediaMockData
package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.mock.MediaMockData
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.*

@Composable
fun MediaContentEditScreen(
    user: User,
    articleId: String? = null,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val existingArticle = articleId?.let { MediaMockData.getArticleById(it) }
    val isNew = existingArticle == null

    var title by remember { mutableStateOf(existingArticle?.title ?: "") }
    var excerpt by remember { mutableStateOf(existingArticle?.excerpt ?: "") }
    var content by remember { mutableStateOf(existingArticle?.content ?: existingArticle?.excerpt ?: "") }
    var selectedCategory by remember { mutableIntStateOf(
        existingArticle?.let { MediaMockData.ArticleCategory.entries.indexOf(it.category) } ?: 0
    ) }
    var tagsText by remember { mutableStateOf(existingArticle?.tags?.joinToString(", ") ?: "") }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextPrimary)
                        }
                        Text(
                            text = if (isNew) "Новая статья" else "Редактирование",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                // Title card
                FadeIn(visible, 100) {
                    DarkCardPadded {
                        Text("Заголовок", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        DarkTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Введите заголовок статьи...",
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Category card
                FadeIn(visible, 200) {
                    DarkCardPadded {
                        Text("Категория", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MediaMockData.ArticleCategory.entries.forEachIndexed { index, category ->
                                DarkFilterChip(category.label, selectedCategory == index, { selectedCategory = index })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Excerpt card
                FadeIn(visible, 300) {
                    DarkCardPadded {
                        Text("Краткое описание", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        DarkTextField(
                            value = excerpt,
                            onValueChange = { excerpt = it },
                            placeholder = "Краткое описание статьи...",
                            singleLine = false,
                            minHeight = 80.dp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Content card
                FadeIn(visible, 400) {
                    DarkCardPadded {
                        Text("Содержание", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        DarkTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = "Текст статьи...",
                            singleLine = false,
                            minHeight = 200.dp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tags card
                FadeIn(visible, 500) {
                    DarkCardPadded {
                        Text("Теги (через запятую)", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        DarkTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            placeholder = "картинг, турнир, новости",
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Cover image placeholder
                FadeIn(visible, 600) {
                    DarkCardPadded {
                        Text("Обложка", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkTheme.CardBorder.copy(alpha = 0.3f)
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    Modifier.size(44.dp).clip(CircleShape)
                                        .background(DarkTheme.CardBorder.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, null,
                                        Modifier.size(22.dp), DarkTheme.TextMuted)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Нажмите для загрузки", fontSize = 13.sp, color = DarkTheme.TextMuted)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Action buttons
                FadeIn(visible, 700) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Черновик", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Publish, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Опубликовать", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: Dp = 0.dp
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)
                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
            singleLine = singleLine,
            cursorBrush = SolidColor(DarkTheme.Accent),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = DarkTheme.TextMuted)
                }
                innerTextField()
            }
        )
    }
}
