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
// Mock data model for display
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

private val mockMediaItems = listOf(
    // 0 — Video
    MediaDetail(
        title = "Лучшие моменты — Картинг 2026",
        description = "Обзор самых ярких моментов сезона картинга 2026 года в Казахстане. Невероятные обгоны, финишные фото-финиши и лучшие манёвры молодых пилотов.",
        content = """
## Сезон 2026 — Лучшие моменты

Сезон 2026 года стал одним из самых захватывающих в истории казахстанского картинга. Более 200 пилотов приняли участие в серии из 5 этапов по всей стране.

### Ключевые моменты

- Алихан Тлеубаев установил рекорд трассы в Алматы — 1:02.347
- Финальный этап собрал рекордные 48 участников
- Три фото-финиша за один уик-энд на Гран-при Астаны

### Итоги сезона

Борьба за чемпионский титул продолжалась до последнего этапа. Алихан Тлеубаев уверенно лидировал после первых трёх этапов, но Марат Касымов сократил отрыв до минимума к финалу.

> «Это был лучший сезон в моей карьере» — Алихан Тлеубаев, чемпион 2026
        """.trimIndent(),
        type = MediaContentType.VIDEO,
        category = "highlight",
        coverImageUrl = "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
        publishedAt = "2026-04-01",
        views = 1243,
        duration = "3:42",
        authorName = "Асем Нурланова",
        authorAvatarUrl = null,
        authorCity = "Алматы",
        authorEmail = "asem@ileader.kz",
        authorArticlesCount = 34,
        authorTotalViews = "15.2K",
        authorRating = "4.9",
        sportName = "Картинг",
        tournamentName = "Кубок Алматы #1",
        galleryImages = emptyList(),
        tags = listOf("картинг", "лучшие моменты", "сезон 2026", "Алматы")
    ),
    // 1 — Photo
    MediaDetail(
        title = "Фотоотчёт: Картинг",
        description = "Фотоотчёт с этапа Гран-при Алматы. Лучшие кадры с трассы, пит-лейна и церемонии награждения.",
        content = """
## Фотоотчёт — Гран-при Алматы

Команда iLeader Media присутствовала на этапе Гран-при Алматы и подготовила фотоотчёт с самыми яркими кадрами соревнований.

### На трассе

Пилоты показали настоящий класс вождения. Борьба за каждую позицию велась на каждом круге. Особенно зрелищным стал поворот №4, где произошло большинство обгонов.

### Церемония награждения

Тройка призёров получила кубки из рук организаторов турнира. Атмосфера на подиуме была невероятной — болельщики поддерживали каждого пилота аплодисментами.
        """.trimIndent(),
        type = MediaContentType.PHOTO,
        category = "photo",
        coverImageUrl = "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
        publishedAt = "2026-03-28",
        views = 876,
        duration = null,
        authorName = "Дамир Ибрагимов",
        authorAvatarUrl = null,
        authorCity = "Астана",
        authorEmail = "damir@ileader.kz",
        authorArticlesCount = 21,
        authorTotalViews = "8.5K",
        authorRating = "4.7",
        sportName = "Картинг",
        tournamentName = "Гран-при Алматы",
        galleryImages = listOf(
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting-2.jpg",
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/shooting.jpg",
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/tennis.jpg",
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/football.jpg",
            "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/boxing.jpg"
        ),
        tags = listOf("фотоотчёт", "картинг", "Алматы", "Гран-при")
    ),
    // 2 — Video
    MediaDetail(
        title = "Обзор сезона Картинг",
        description = "Полный обзор сезона картинга 2026 — статистика, рейтинги и прогнозы на следующий сезон.",
        content = """
## Обзор сезона картинга 2026

### Статистика сезона

- 5 этапов проведено
- 200+ участников
- 12 городов Казахстана

### Рейтинг пилотов

Лидером сезона стал Алихан Тлеубаев (58 очков), за ним Марат Касымов (45 очков) и Данияр Серикбаев (40 очков).

### Прогнозы на 2027

Эксперты ожидают расширение серии до 7 этапов и увеличение призового фонда.
        """.trimIndent(),
        type = MediaContentType.VIDEO,
        category = "review",
        coverImageUrl = "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
        publishedAt = "2026-03-25",
        views = 2105,
        duration = "5:18",
        authorName = "Асем Нурланова",
        authorAvatarUrl = null,
        authorCity = "Алматы",
        authorEmail = "asem@ileader.kz",
        authorArticlesCount = 34,
        authorTotalViews = "15.2K",
        authorRating = "4.9",
        sportName = "Картинг",
        tournamentName = null,
        galleryImages = emptyList(),
        tags = listOf("обзор", "картинг", "сезон 2026", "рейтинг")
    ),
    // 3 — Video
    MediaDetail(
        title = "Интервью с чемпионом",
        description = "Эксклюзивное интервью с чемпионом сезона 2026 Алиханом Тлеубаевым. О победах, трудностях и планах на будущее.",
        content = """
## Интервью с Алиханом Тлеубаевым

### О победе в чемпионате

> «Этот сезон дался мне непросто. Было много конкурентов, и каждый этап — это новая битва. Но я горжусь тем, чего удалось достичь.»

### О подготовке

- Тренировки 5 дней в неделю
- Работа с тренером по физподготовке
- Анализ телеметрии после каждой гонки

### Планы на 2027

> «Хочу попробовать себя на международных соревнованиях. Уже есть приглашение на серию в ОАЭ.»

### Совет молодым пилотам

> «Не бойтесь ошибок. Каждый сход — это урок. Главное — не сдаваться и работать над собой каждый день.»
        """.trimIndent(),
        type = MediaContentType.VIDEO,
        category = "interview",
        coverImageUrl = "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
        publishedAt = "2026-03-20",
        views = 3456,
        duration = "2:55",
        authorName = "Дамир Ибрагимов",
        authorAvatarUrl = null,
        authorCity = "Астана",
        authorEmail = "damir@ileader.kz",
        authorArticlesCount = 21,
        authorTotalViews = "8.5K",
        authorRating = "4.7",
        sportName = "Картинг",
        tournamentName = "Финал сезона 2026",
        galleryImages = emptyList(),
        tags = listOf("интервью", "чемпион", "картинг", "Тлеубаев")
    ),
    // 4 — Video
    MediaDetail(
        title = "Топ-5 голов/финишей",
        description = "Подборка пяти лучших финишей сезона картинга 2026. Разрывы в сотые доли секунды и невероятные обгоны на последнем круге.",
        content = """
## Топ-5 финишей сезона 2026

### #5 — Гран-при Шымкент, этап 3
Разрыв между первым и вторым местом — 0.087 секунды. Касымов обогнал Серикбаева на последнем повороте.

### #4 — Гран-при Алматы, этап 1
Три пилота финишировали в пределах 0.3 секунды друг от друга.

### #3 — Гран-при Астана, этап 2
Тлеубаев стартовал с 8-й позиции и финишировал первым.

### #2 — Квалификация, этап 4
Рекорд трассы побит трижды за одну сессию.

### #1 — Финал, Алматы
Борьба за титул на последнем круге — Тлеубаев опередил Касымова на 0.034 секунды!
        """.trimIndent(),
        type = MediaContentType.VIDEO,
        category = "highlight",
        coverImageUrl = "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/karting.jpg",
        publishedAt = "2026-03-15",
        views = 5621,
        duration = "4:10",
        authorName = "Асем Нурланова",
        authorAvatarUrl = null,
        authorCity = "Алматы",
        authorEmail = "asem@ileader.kz",
        authorArticlesCount = 34,
        authorTotalViews = "15.2K",
        authorRating = "4.9",
        sportName = "Картинг",
        tournamentName = null,
        galleryImages = emptyList(),
        tags = listOf("топ-5", "финиши", "картинг", "лучшие моменты")
    )
)

private fun getMockMedia(articleId: String): MediaDetail {
    val index = articleId.removePrefix("mock_").toIntOrNull()?.coerceIn(0, mockMediaItems.lastIndex) ?: 0
    return mockMediaItems[index]
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
    // Always use mock data for now
    val media = remember(articleId) { getMockMedia(articleId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        MediaDetailContent(media = media, onBack = onBack)
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

        Spacer(Modifier.height(100.dp))
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
