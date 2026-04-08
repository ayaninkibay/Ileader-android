package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.ArticleDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft
private val Border: Color @Composable get() = LocalAppColors.current.border

// ══════════════════════════════════════════════════════════
// Content type
// ══════════════════════════════════════════════════════════

enum class MediaContentType { VIDEO, PHOTO, ARTICLE }

// ══════════════════════════════════════════════════════════
// Display model
// ══════════════════════════════════════════════════════════

private data class MediaDetail(
    val title: String,
    val description: String,
    val content: String,
    val type: MediaContentType,
    val category: String,
    val coverImageUrl: String?,
    val publishedAt: String,
    val views: Int,
    val duration: String?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val authorCity: String,
    val authorEmail: String,
    val authorArticlesCount: Int,
    val authorTotalViews: String,
    val authorRating: String,
    val sportName: String,
    val tournamentName: String?,
    val galleryImages: List<String>,
    val tags: List<String>
)

private fun articleToMediaDetail(article: com.ileader.app.data.remote.dto.ArticleDto): MediaDetail {
    val contentType = when (article.category) {
        "video" -> MediaContentType.VIDEO
        "photo", "gallery" -> MediaContentType.PHOTO
        else -> MediaContentType.ARTICLE
    }
    return MediaDetail(
        title = article.title,
        description = article.excerpt ?: "",
        content = article.content ?: "",
        type = contentType,
        category = article.category ?: "news",
        coverImageUrl = article.coverImageUrl,
        publishedAt = article.publishedAt ?: article.createdAt ?: "",
        views = article.views,
        duration = null,
        authorName = article.profiles?.name ?: "",
        authorAvatarUrl = article.profiles?.avatarUrl,
        authorCity = article.profiles?.city ?: "",
        authorEmail = article.profiles?.email ?: "",
        authorArticlesCount = 0,
        authorTotalViews = "${article.views}",
        authorRating = "—",
        sportName = article.sports?.name ?: "",
        tournamentName = null,
        galleryImages = emptyList(),
        tags = article.tags ?: emptyList()
    )
}


// ══════════════════════════════════════════════════════════
// Screen entry point
// ══════════════════════════════════════════════════════════

@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit,
    viewModel: ArticleDetailViewModel = viewModel()
) {
    LaunchedEffect(articleId) { viewModel.load(articleId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Статья", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Статья", onBack)
                ErrorScreen(state.message) { viewModel.load(articleId) }
            }
        }
        is UiState.Success -> {
            val media = remember(state.data) { articleToMediaDetail(state.data) }
            Column(
                modifier = Modifier.fillMaxSize().background(Bg)
            ) {
                MediaDetailContent(media = media, onBack = onBack)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Main content
// ══════════════════════════════════════════════════════════

@Composable
private fun MediaDetailContent(media: MediaDetail, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero ──
        HeroSection(media = media, onBack = onBack)

        // ── Meta row ──
        Spacer(Modifier.height(16.dp))
        MetaRow(media = media)

        // ── Title ──
        Spacer(Modifier.height(16.dp))
        Text(
            text = media.title,
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 30.sp
        )

        // ── Description ──
        Spacer(Modifier.height(8.dp))
        Text(
            text = media.description,
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 15.sp,
            color = TextSecondary,
            lineHeight = 22.sp
        )

        // ── Author card ──
        Spacer(Modifier.height(16.dp))
        AuthorCard(media = media)

        // ── Tournament link ──
        if (media.tournamentName != null) {
            Spacer(Modifier.height(12.dp))
            TournamentLinkCard(media = media)
        }

        // ── Video section ──
        if (media.type == MediaContentType.VIDEO) {
            Spacer(Modifier.height(20.dp))
            VideoSection(media = media)
        }

        // ── Photo gallery ──
        if (media.type == MediaContentType.PHOTO && media.galleryImages.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            PhotoGallery(media = media)
        }

        // ── Article content ──
        if (media.content.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Description, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Содержание", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(Modifier.height(12.dp))
                RenderArticleContent(media.content)
            }
        }

        // ── Tags ──
        if (media.tags.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            TagsSection(media.tags)
        }

        // ── Stats footer ──
        Spacer(Modifier.height(20.dp))
        StatsFooter(media = media)

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Hero
// ══���═══════════════════════════════════════════════════════

@Composable
private fun HeroSection(media: MediaDetail, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (media.coverImageUrl != null) {
            AsyncImage(
                model = media.coverImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))))
            )
        }

        // Gradients
        Box(Modifier.fillMaxWidth().height(120.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Transparent))))
        Box(Modifier.fillMaxWidth().height(140.dp).align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)))))

        // Back
        Box(
            Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                .clip(CircleShape).background(Color.Black.copy(0.3f)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // Bottom badges
        Row(
            Modifier.align(Alignment.BottomStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (typeIcon, typeLabel, typeColor) = when (media.type) {
                MediaContentType.VIDEO -> Triple(Icons.Filled.PlayCircle, "Видео", Color(0xFF3B82F6))
                MediaContentType.PHOTO -> Triple(Icons.Filled.PhotoLibrary, "Фото", Color(0xFF22C55E))
                MediaContentType.ARTICLE -> Triple(Icons.Default.Description, "Статья", Accent)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = typeColor) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(typeIcon, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(typeLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(0.15f)) {
                Text(
                    getCategoryLabel(media.category),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                )
            }
            // Sport pill
            Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(0.15f)) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(sportIcon(media.sportName), null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Text(media.sportName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        // Play overlay for video
        if (media.type == MediaContentType.VIDEO) {
            Box(
                Modifier.size(64.dp).clip(CircleShape)
                    .background(Color.White.copy(0.2f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Meta Row
// ══════════════════════════════════════════════════════════

@Composable
private fun MetaRow(media: MediaDetail) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetaChip(Icons.Outlined.CalendarMonth, formatDateRu(media.publishedAt))
        if (media.views > 0) {
            MetaChip(Icons.Outlined.Visibility, "${media.views} просмотров")
        }
        if (media.type == MediaContentType.VIDEO && media.duration != null) {
            MetaChip(Icons.Outlined.Schedule, media.duration)
        } else if (media.type == MediaContentType.PHOTO) {
            MetaChip(Icons.Outlined.PhotoLibrary, "${media.galleryImages.size} фото")
        } else {
            val wordCount = media.content.length / 5
            val readMinutes = (wordCount / 200).coerceAtLeast(1)
            MetaChip(Icons.Outlined.Schedule, "$readMinutes мин. чтения")
        }
    }
}

@Composable
private fun MetaChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextMuted)
    }
}

// ══════════════════════════════════════════════════════════
// Author Card
// ══════════════════════════════════════════════════════════

@Composable
private fun AuthorCard(media: MediaDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Автор публикации", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = TextMuted, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                if (media.authorAvatarUrl != null) {
                    AsyncImage(
                        model = media.authorAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            media.authorName.take(1).uppercase(),
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(media.authorName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(50), color = Color(0xFF8B5CF6).copy(0.12f)) {
                            Text("СМИ", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("·", fontSize = 12.sp, color = TextMuted)
                        Spacer(Modifier.width(8.dp))
                        Text(media.authorCity, fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(media.authorEmail, fontSize = 11.sp, color = TextMuted)
                }
            }

            // Stats
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AuthorStatItem(Icons.Outlined.Description, "Публикации", "${media.authorArticlesCount}")
                AuthorStatItem(Icons.Outlined.Visibility, "Просмотры", media.authorTotalViews)
                AuthorStatItem(Icons.Outlined.Star, "Рейтинг", media.authorRating)
            }
        }
    }
}

@Composable
private fun AuthorStatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

// ══════════════════════════════════════════════════════════
// Tournament Link
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentLinkCard(media: MediaDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = CardBg
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Accent.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Связанный турнир", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(media.tournamentName ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = TextMuted.copy(0.1f)) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(sportIcon(media.sportName), null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(media.sportName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Video Section
// ══════════════════════════════════════════════════════════

@Composable
private fun VideoSection(media: MediaDetail) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PlayCircle, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Видео", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))

        // Player placeholder
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
        ) {
            if (media.coverImageUrl != null) {
                AsyncImage(media.coverImageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Color(0xFF1a1a2e)))
            }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)))

            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(Color.White.copy(0.2f))
                        .border(2.dp, Color.White.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Нажмите для воспроизведения", fontSize = 12.sp, color = Color.White.copy(0.7f))
            }

            // Duration
            Surface(
                Modifier.align(Alignment.BottomEnd).padding(12.dp),
                shape = RoundedCornerShape(8.dp), color = Color.Black.copy(0.7f)
            ) {
                Text(
                    media.duration ?: "", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            }
        }

        // Video info
        Spacer(Modifier.height(10.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = CardBg) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                VideoInfoItem(Icons.Outlined.HighQuality, "HD 1080p")
                VideoInfoItem(Icons.Outlined.Schedule, media.duration ?: "")
                VideoInfoItem(Icons.Outlined.Visibility, "${media.views}")
            }
        }
    }
}

@Composable
private fun VideoInfoItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ══════════════════════════════════════════════════════════
// Photo Gallery
// ══════════════════════════════════════════════════════════

@Composable
private fun PhotoGallery(media: MediaDetail) {
    val images = media.galleryImages
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PhotoLibrary, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Фотогалерея", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(50), color = TextMuted.copy(0.1f)) {
                Text(
                    "${images.size} фото",
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Main image
        Box(
            modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = images.getOrNull(selectedIndex) ?: media.coverImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                Modifier.align(Alignment.TopEnd).padding(12.dp),
                shape = RoundedCornerShape(8.dp), color = Color.Black.copy(0.6f)
            ) {
                Text(
                    "${selectedIndex + 1} / ${images.size}",
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Thumbnails
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            images.forEachIndexed { index, url ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isSelected) Modifier.border(2.dp, Accent, RoundedCornerShape(10.dp))
                            else Modifier.border(1.dp, Border.copy(0.3f), RoundedCornerShape(10.dp))
                        )
                        .clickable { selectedIndex = index }
                ) {
                    AsyncImage(url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    if (!isSelected) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tags
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(tags: List<String>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Tag, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Теги", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tags.forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = TextMuted.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(0.15f))
                ) {
                    Text(
                        "#$tag",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Stats Footer
// ══════════════════════════════════════════════════════════

@Composable
private fun StatsFooter(media: MediaDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = CardBg
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Share, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Поделиться", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.BookmarkBorder, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Сохранить", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Outlined.Flag, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Article content renderer
// ══════════════════════════════════════════════════════════

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
                else -> Text(trimmed, fontSize = 16.sp, color = TextPrimary, lineHeight = 26.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

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
    "video" -> "Видео"
    "photo" -> "Фотоотчёт"
    "gallery" -> "Галерея"
    else -> category.replaceFirstChar { it.uppercase() }
}
