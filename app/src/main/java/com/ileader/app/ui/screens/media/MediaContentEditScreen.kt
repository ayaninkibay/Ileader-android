package com.ileader.app.ui.screens.media

import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ArticleSaveState
import com.ileader.app.ui.viewmodels.MediaContentViewModel

private val CATEGORIES = listOf(
    "news" to "Новости",
    "review" to "Обзор",
    "interview" to "Интервью",
    "report" to "Репортаж",
    "analysis" to "Аналитика",
    "announcement" to "Анонс"
)

@Composable
fun MediaContentEditScreen(
    user: User,
    articleId: String? = null,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    vm: MediaContentViewModel = viewModel()
) {
    val isNew = articleId == null || articleId == "new"
    val articleDetail by vm.articleDetail.collectAsState()
    val saveState by vm.saveState.collectAsState()

    var title by remember { mutableStateOf("") }
    var excerpt by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var tagsText by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(isNew) }

    // Load existing article if editing
    LaunchedEffect(articleId) {
        if (!isNew && articleId != null) {
            vm.loadArticleDetail(articleId)
        }
    }

    // Populate fields when article loads
    LaunchedEffect(articleDetail) {
        val detail = articleDetail
        if (!isNew && detail is UiState.Success && !isLoaded) {
            val a = detail.data
            title = a.title
            excerpt = a.excerpt ?: ""
            content = a.content ?: ""
            tagsText = a.tags?.joinToString(", ") ?: ""
            val catIndex = CATEGORIES.indexOfFirst { it.first == a.category }
            if (catIndex >= 0) selectedCategoryIndex = catIndex
            isLoaded = true
        }
    }

    // Handle save success
    LaunchedEffect(saveState) {
        if (saveState is ArticleSaveState.Success) {
            vm.resetSaveState()
            vm.clearDetail()
            onSave()
        }
    }

    val showError = saveState is ArticleSaveState.Error
    val isSaving = saveState is ArticleSaveState.Saving

    // Show loading while fetching article for edit
    if (!isNew && !isLoaded) {
        when (articleDetail) {
            is UiState.Loading -> { LoadingScreen(); return }
            is UiState.Error -> { ErrorScreen((articleDetail as UiState.Error).message) { onBack() }; return }
            else -> {}
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
                if (showError) {
                    FadeIn(visible, 50) {
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkTheme.Accent.copy(alpha = 0.1f)
                        ) {
                            Text(
                                (saveState as ArticleSaveState.Error).message,
                                Modifier.padding(12.dp), fontSize = 13.sp, color = DarkTheme.Accent
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Title card
                FadeIn(visible, 100) {
                    DarkCardPadded {
                        Text("Заголовок", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, color = DarkTheme.TextMuted)
                        Spacer(Modifier.height(8.dp))
                        EditTextField(
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
                            CATEGORIES.forEachIndexed { index, (_, label) ->
                                DarkFilterChip(label, selectedCategoryIndex == index, { selectedCategoryIndex = index })
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
                        EditTextField(
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
                        EditTextField(
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
                        EditTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            placeholder = "картинг, турнир, новости",
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Action buttons
                FadeIn(visible, 600) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val tags = tagsText.split(",").map { it.trim() }
                                    val category = CATEGORIES[selectedCategoryIndex].first
                                    if (isNew) {
                                        vm.createArticle(title, content, excerpt, category, tags, "draft")
                                    } else {
                                        vm.updateArticle(articleId!!, title, content, excerpt, category, tags, "draft")
                                    }
                                }
                            },
                            enabled = title.isNotBlank() && !isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = DarkTheme.TextSecondary)
                            } else {
                                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("Черновик", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val tags = tagsText.split(",").map { it.trim() }
                                    val category = CATEGORIES[selectedCategoryIndex].first
                                    if (isNew) {
                                        vm.createArticle(title, content, excerpt, category, tags, "published")
                                    } else {
                                        vm.updateArticle(articleId!!, title, content, excerpt, category, tags, "published")
                                    }
                                }
                            },
                            enabled = title.isNotBlank() && !isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = DarkTheme.TextPrimary)
                            } else {
                                Icon(Icons.Default.Publish, null, Modifier.size(18.dp))
                            }
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
private fun EditTextField(
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
