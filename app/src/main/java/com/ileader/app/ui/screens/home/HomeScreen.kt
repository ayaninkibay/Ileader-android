package com.ileader.app.ui.screens.home

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.HomeViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun HomeScreen(
    onArticleClick: (String) -> Unit,
    onTournamentClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val sportPref = remember { SportPreference(context) }
    val sportIds by sportPref.selectedSportIds.collectAsState(initial = emptyList())

    LaunchedEffect(sportIds) {
        viewModel.load(sportIds)
    }

    val state = viewModel.state
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    // Scale animation for the gradient header
    val headerScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.95f,
        animationSpec = tween(500, easing = EaseOutBack),
        label = "headerScale"
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400),
        label = "headerAlpha"
    )

    val colors = LocalAppColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Gradient Header ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = headerScale
                        scaleY = headerScale
                        alpha = headerAlpha
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE53535),
                                Color(0xFFFF6B6B),
                                Color(0xFFE53535).copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 28.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "iLeader",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Спортивная платформа",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Decorative icon
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // ── Sport Circles (story-like filter) ──
        item {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = started, delayMs = 0) {
                val sportsList = remember {
                    listOf(
                        Triple("karting", "Картинг", "🏎️"),
                        Triple("shooting", "Стрельба", "🎯"),
                        Triple("tennis", "Теннис", "🎾"),
                        Triple("football", "Футбол", "⚽"),
                        Triple("boxing", "Бокс", "🥊"),
                        Triple("swimming", "Плавание", "🏊"),
                        Triple("athletics", "Атлетика", "🏃"),
                        Triple("rowing", "Гребля", "🚣")
                    )
                }
                var selectedSport by remember { mutableStateOf<String?>(null) }
                SportCirclesRow(
                    sports = sportsList,
                    selectedId = selectedSport,
                    onSelect = { selectedSport = it }
                )
            }
        }

        // ── Новости ──
        item {
            Spacer(Modifier.height(20.dp))
            FadeIn(visible = started, delayMs = 60) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(title = "Новости", action = "Все", onAction = {})
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        item {
            FadeIn(visible = started, delayMs = 60) {
                NewsContent(state = state.news, onArticleClick = onArticleClick)
            }
        }

        // ── Турниры ──
        item {
            Spacer(Modifier.height(24.dp))
            FadeIn(visible = started, delayMs = 150) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(title = "Ближайшие турниры", action = "Все", onAction = {})
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        item {
            FadeIn(visible = started, delayMs = 150) {
                TournamentsContent(state = state.tournaments, onTournamentClick = onTournamentClick)
            }
        }

        // ── Люди ──
        item {
            Spacer(Modifier.height(24.dp))
            FadeIn(visible = started, delayMs = 300) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(title = "Сообщество", action = "Все", onAction = {})
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        item {
            FadeIn(visible = started, delayMs = 300) {
                PeopleContent(state = state.people, onProfileClick = onProfileClick)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// News
// ══════════════════════════════════════════════════════════

@Composable
private fun NewsContent(
    state: UiState<List<ArticleDto>>,
    onArticleClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { LoadingScreen() }
        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет новостей", subtitle = "Новости появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, article ->
                        val delay = (index * 60).coerceAtMost(400)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(400),
                            label = "newsItemAlpha"
                        )
                        val itemOffset by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 40f,
                            animationSpec = tween(400, easing = EaseOutBack),
                            label = "newsItemOffset"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                translationY = itemOffset
                            }
                        ) {
                            NewsCard(article = article, onClick = { onArticleClick(article.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(article: ArticleDto, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        tween(100), label = "newsPress"
    )

    Surface(
        modifier = Modifier
            .width(270.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true; tryAwaitRelease(); isPressed = false },
                    onTap = { onClick() }
                )
            }
        ) {
            // Cover image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = article.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
                // Category badge — pill shape
                article.category?.let { cat ->
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(50),
                        color = Accent.copy(alpha = 0.9f)
                    ) {
                        Text(
                            getCategoryLabel(cat),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = article.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule, null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = formatDateShort(article.publishedAt ?: article.createdAt),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournaments
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentsContent(
    state: UiState<List<TournamentWithCountsDto>>,
    onTournamentClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { LoadingScreen() }
        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет турниров", subtitle = "Турниры появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, tournament ->
                        val delay = (index * 60).coerceAtMost(400)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(400),
                            label = "tournamentItemAlpha"
                        )
                        val itemOffset by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 40f,
                            animationSpec = tween(400, easing = EaseOutBack),
                            label = "tournamentItemOffset"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                translationY = itemOffset
                            }
                        ) {
                            TournamentCard(
                                tournament = tournament,
                                onClick = { onTournamentClick(tournament.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        tween(100), label = "tournPress"
    )

    Surface(
        modifier = Modifier
            .width(260.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true; tryAwaitRelease(); isPressed = false },
                    onTap = { onClick() }
                )
            }
        ) {
            // Gradient sport header strip
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                getStatusColor(tournament.status ?: ""),
                                getStatusColor(tournament.status ?: "").copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Sport + status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tournament.sportName?.let { sport ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Accent.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "${sportEmoji(sport)} $sport",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Accent
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    tournament.status?.let { status ->
                        val statusColor = getStatusColor(status)
                        if (status == "in_progress") {
                            LiveIndicator()
                        } else {
                            StatusBadge(
                                text = getStatusLabel(status),
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Title
                Text(
                    text = tournament.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                    letterSpacing = (-0.2).sp
                )

                Spacer(Modifier.height(12.dp))

                // Info chips row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = Icons.Default.CalendarMonth,
                        text = formatDateShort(tournament.startDate)
                    )
                    if (!tournament.locationName.isNullOrEmpty()) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = tournament.locationName,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                // Participants bar
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.People, null, modifier = Modifier.size(15.dp), tint = TextMuted)
                        Text(
                            text = "${tournament.participantCount} участников",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Mini arrow indicator
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = Accent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// People
// ══════════════════════════════════════════════════════════

@Composable
private fun PeopleContent(
    state: UiState<List<CommunityProfileDto>>,
    onProfileClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { LoadingScreen() }
        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет людей", subtitle = "Данные появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, profile ->
                        val delay = (index * 60).coerceAtMost(400)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(400),
                            label = "personItemAlpha"
                        )
                        val itemScale by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0.8f,
                            animationSpec = tween(400, easing = EaseOutBack),
                            label = "personItemScale"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                        ) {
                            PersonCard(
                                profile = profile,
                                onClick = { onProfileClick(profile.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(profile: CommunityProfileDto, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark

    Surface(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar using shared UserAvatar component
            UserAvatar(
                avatarUrl = profile.avatarUrl,
                name = profile.name ?: "?",
                size = 58.dp,
                showGradientBorder = profile.primaryRating > 0
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = profile.name ?: "",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (profile.primarySportName.isNotEmpty()) {
                Text(
                    text = profile.primarySportName,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            if (profile.primaryRating > 0) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${profile.primaryRating}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Utils
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String?): String {
    if (dateStr == null) return ""
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", "янв", "фев", "мар", "апр", "мая",
        "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    else -> status
}

private fun getStatusColor(status: String): Color = when (status) {
    "registration_open" -> Color(0xFF22C55E)
    "in_progress" -> Color(0xFF3B82F6)
    "check_in" -> Color(0xFFF59E0B)
    "completed" -> Color(0xFF8E8E93)
    else -> Color(0xFF8E8E93)
}

private fun getCategoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "report" -> "Отчёт"
    "interview" -> "Интервью"
    "analytics" -> "Аналитика"
    "review" -> "Обзор"
    "announcement" -> "Анонс"
    "highlight" -> "Главное"
    else -> category.replaceFirstChar { it.uppercase() }
}
