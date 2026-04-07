package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleInsertDto
import com.ileader.app.data.remote.dto.ArticleUpdateDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MediaViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted

private val MediaColor = Color(0xFF06B6D4)
private val PurpleColor = Color(0xFF7C3AED)

private val categories = listOf(
    "news" to "Новости",
    "interview" to "Интервью",
    "review" to "Обзор",
    "analytics" to "Аналитика",
    "preview" to "Превью",
    "recap" to "Итоги",
    "feature" to "Репортаж",
    "opinion" to "Мнение",
    "tournament_report" to "Отчёт о турнире",
    "profile" to "Профиль спортсмена"
)

@Composable
fun MediaArticleEditorScreen(
    userId: String,
    articleId: String? = null,
    onBack: () -> Unit,
    vm: MediaViewModel = viewModel()
) {
    val currentArticle by vm.currentArticle.collectAsState()
    val actionState by vm.actionState.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var excerpt by remember { mutableStateOf("") }
    var coverImageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isPublished by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    val snackbar = LocalSnackbarHost.current
    val isEditing = articleId != null
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    // Load article if editing
    LaunchedEffect(articleId) {
        if (articleId != null) {
            vm.loadArticle(articleId)
        }
    }

    // Populate fields from loaded article
    LaunchedEffect(currentArticle) {
        if (isEditing && currentArticle is UiState.Success && !loaded) {
            (currentArticle as UiState.Success).data?.let { article ->
                title = article.title
                content = article.content ?: ""
                excerpt = article.excerpt ?: ""
                coverImageUrl = article.coverImageUrl ?: ""
                selectedCategory = article.category
                isPublished = article.status == "published"
                loaded = true
            }
        }
    }

    // Handle action results
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is UiState.Success -> {
                snackbar.showSnackbar(s.data)
                vm.clearAction()
                onBack()
            }
            is UiState.Error -> {
                snackbar.showSnackbar(s.message)
                vm.clearAction()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Header ──
        item {
            EditorHeader(
                isEditing = isEditing,
                onBack = onBack,
                onSave = {
                    if (title.isBlank()) return@EditorHeader
                    if (isEditing && articleId != null) {
                        vm.updateArticle(
                            articleId, userId,
                            ArticleUpdateDto(
                                title = title,
                                content = content.ifBlank { null },
                                excerpt = excerpt.ifBlank { null },
                                coverImageUrl = coverImageUrl.ifBlank { null },
                                category = selectedCategory,
                                status = if (isPublished) "published" else "draft"
                            )
                        )
                    } else {
                        vm.createArticle(
                            userId,
                            ArticleInsertDto(
                                authorId = userId,
                                title = title,
                                content = content.ifBlank { null },
                                excerpt = excerpt.ifBlank { null },
                                coverImageUrl = coverImageUrl.ifBlank { null },
                                category = selectedCategory,
                                status = if (isPublished) "published" else "draft"
                            )
                        )
                    }
                },
                canSave = title.isNotBlank(),
                isLoading = actionState is UiState.Loading
            )
        }

        // Loading state for edit
        if (isEditing && currentArticle is UiState.Loading) {
            item {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    LoadingScreen()
                }
            }
            return@LazyColumn
        }

        // ── Title ──
        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Заголовок *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Введите заголовок статьи", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PurpleColor
                    )
                )
            }
        }

        // ── Excerpt ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Краткое описание", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    placeholder = { Text("Краткое описание для превью", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PurpleColor
                    )
                )
            }
        }

        // ── Cover Image URL ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Обложка (URL)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = coverImageUrl,
                    onValueChange = { coverImageUrl = it },
                    placeholder = { Text("https://example.com/image.jpg", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Image, null, tint = TextMuted) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PurpleColor
                    )
                )
            }
        }

        // ── Category ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Категория", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) PurpleColor else CardBg,
                            border = if (!isSelected && isDark) DarkTheme.cardBorderStroke
                            else if (!isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f))
                            else null,
                            modifier = Modifier.clickable { selectedCategory = if (isSelected) null else key }
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // ── Content ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Текст статьи", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Напишите текст статьи...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    minLines = 8,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PurpleColor
                    )
                )
            }
        }

        // ── Publish toggle ──
        item {
            Spacer(Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg,
                border = if (isDark) DarkTheme.cardBorderStroke
                else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f))
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(
                            if (isPublished) Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                            else Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPublished) Icons.Default.Public else Icons.Default.Edit,
                            null, tint = Color.White, modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isPublished) "Опубликована" else "Черновик",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            if (isPublished) "Статья видна всем пользователям" else "Статья видна только вам",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TextMuted.copy(0.3f)
                        )
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Editor Header
// ══════════════════════════════════════════════════════════

@Composable
private fun EditorHeader(
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    isLoading: Boolean
) {
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
            Modifier.statusBarsPadding().padding(horizontal = 8.dp).padding(top = 4.dp, bottom = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                }
                Text(
                    if (isEditing) "Редактирование" else "Новая статья",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = onSave,
                    enabled = canSave && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(0.2f),
                        disabledContainerColor = Color.White.copy(0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Сохранить", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
