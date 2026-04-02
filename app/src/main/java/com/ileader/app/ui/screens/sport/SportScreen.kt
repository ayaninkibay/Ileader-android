package com.ileader.app.ui.screens.sport

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.DarkSearchField
import com.ileader.app.ui.components.DarkSegmentedControl
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EmptyState
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.FadeIn
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.formatShortDate
import com.ileader.app.ui.components.sportColor
import com.ileader.app.ui.components.sportEmoji
import com.ileader.app.ui.components.sportIcon
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.SportViewModel
import com.ileader.app.ui.viewmodels.SportViewModel.SportSubTab

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun SportScreen(
    user: User,
    onTournamentClick: (String) -> Unit,
    onArticleClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLeagueClick: (String, String, String?) -> Unit = { _, _, _ -> },
    viewModel: SportViewModel = viewModel()
) {
    val s = viewModel.state
    var showFilterPopup by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    // Mock leagues data
    val mockLeagues = remember { listOf(
        MockLeague("Лига Картинга Казахстан 2026", "Картинг", 5, 3, "in_progress", 24,
            "https://ileader.kz/img/karting/karting-04-1280x853.jpeg", "15 апреля",
            listOf(MockLeader("Алихан Т.", 58), MockLeader("Марат К.", 45), MockLeader("Данияр С.", 40))),
        MockLeague("Чемпионат по Стрельбе", "Стрельба", 4, 0, "registration_open", 12,
            "https://ileader.kz/img/shooting/shooting-01-1280x853.jpeg", "20 апреля", emptyList()),
        MockLeague("Теннисная Лига Алматы", "Теннис", 8, 1, "in_progress", 32,
            "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&q=80", "12 апреля",
            listOf(MockLeader("Аян Б.", 30), MockLeader("Тимур Н.", 20), MockLeader("Ерлан Ж.", 15)))
    ) }

    // Search bar slide-down animation
    val searchAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400),
        label = "searchAlpha"
    )
    val searchOffset by animateFloatAsState(
        targetValue = if (started) 0f else -30f,
        animationSpec = tween(400, easing = EaseOutBack),
        label = "searchOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search + Filter button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = searchAlpha
                    translationY = searchOffset
                }
        ) {
            DarkSearchField(
                value = s.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = "Поиск...",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { showFilterPopup = true }) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Фильтры",
                    tint = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Segmented control
        DarkSegmentedControl(
            options = listOf("Турниры", "Люди", "Новости", "Лиги"),
            selectedIndex = s.activeTab.ordinal,
            onSelect = { viewModel.setTab(SportSubTab.entries[it]) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Content
        when (s.activeTab) {
            SportSubTab.TOURNAMENTS -> TournamentsContent(
                state = s.tournaments,
                hasMore = s.hasMoreTournaments,
                onClick = onTournamentClick,
                onLoadMore = { viewModel.loadMore() },
                onRetry = { viewModel.retry() }
            )
            SportSubTab.PEOPLE -> PeopleContent(
                state = s.people,
                hasMore = s.hasMorePeople,
                onClick = onProfileClick,
                onLoadMore = { viewModel.loadMore() },
                onRetry = { viewModel.retry() }
            )
            SportSubTab.NEWS -> NewsContent(
                state = s.news,
                hasMore = s.hasMoreNews,
                onClick = onArticleClick,
                onLoadMore = { viewModel.loadMore() },
                onRetry = { viewModel.retry() }
            )
            SportSubTab.LEAGUES -> LeaguesContent(
                leagues = mockLeagues,
                onLeagueClick = onLeagueClick
            )
        }
    }

    // Filter popup
    if (showFilterPopup) {
        FilterPopupScreen(
            activeTab = s.activeTab,
            sports = s.sports,
            filters = s.filters,
            onApply = {
                viewModel.applyFilters(it)
                showFilterPopup = false
            },
            onReset = {
                viewModel.resetFilters()
                showFilterPopup = false
            },
            onDismiss = { showFilterPopup = false }
        )
    }
}

// ═══════════════════════════════════════════════════
// Tournaments
// ═══════════════════════════════════════════════════

@Composable
private fun TournamentsContent(
    state: UiState<List<TournamentWithCountsDto>>,
    hasMore: Boolean,
    onClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = onRetry)
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(title = "Турниры не найдены", subtitle = "Попробуйте изменить фильтры")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, tournament ->
                        val delay = (index * 50).coerceAtMost(500)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(350),
                            label = "tItemAlpha"
                        )
                        val itemOffset by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 50f,
                            animationSpec = tween(350, easing = EaseOutBack),
                            label = "tItemOffset"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                translationY = itemOffset
                            }
                        ) {
                            TournamentCard(tournament = tournament, onClick = { onClick(tournament.id) })
                        }
                    }
                    if (hasMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                TextButton(onClick = onLoadMore) {
                                    Text("Загрузить ещё", color = Accent, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Pill badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tournament.sportName?.let { sport ->
                    PillBadge(
                        text = sport,
                        color = TextMuted,
                        icon = sportIcon(sport)
                    )
                }
                tournament.status?.let { status ->
                    PillBadge(
                        text = getStatusLabel(status),
                        color = getStatusColor(status)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                text = tournament.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Info rows with icons
            InfoRow(
                icon = Icons.Default.CalendarMonth,
                text = formatShortDate(tournament.startDate)
            )

            tournament.locationName?.let { location ->
                Spacer(Modifier.height(6.dp))
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    text = location
                )
            }

            Spacer(Modifier.height(6.dp))
            InfoRow(
                icon = Icons.Default.People,
                text = "${tournament.participantCount} участников"
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// People
// ═══════════════════════════════════════════════════

@Composable
private fun PeopleContent(
    state: UiState<List<CommunityProfileDto>>,
    hasMore: Boolean,
    onClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = onRetry)
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(title = "Люди не найдены", subtitle = "Попробуйте изменить фильтры")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, person ->
                        val delay = (index * 50).coerceAtMost(500)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(350),
                            label = "pItemAlpha"
                        )
                        val itemOffset by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 50f,
                            animationSpec = tween(350, easing = EaseOutBack),
                            label = "pItemOffset"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                translationY = itemOffset
                            }
                        ) {
                            PersonCard(person = person, onClick = { onClick(person.id) })
                        }
                    }
                    if (hasMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                TextButton(onClick = onLoadMore) {
                                    Text("Загрузить ещё", color = Accent, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(person: CommunityProfileDto, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = Border,
                modifier = Modifier.size(48.dp)
            ) {
                if (person.avatarUrl != null) {
                    AsyncImage(
                        model = person.avatarUrl,
                        contentDescription = person.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // Name + info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name ?: "Без имени",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (person.primarySportName.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(sportIcon(person.primarySportName), null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = person.primarySportName,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                person.city?.let { city ->
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(text = city, color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            // Rating pill
            if (person.primaryRating > 0) {
                PillBadge(
                    text = "${person.primaryRating}",
                    color = Accent
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// News
// ═══════════════════════════════════════════════════

@Composable
private fun NewsContent(
    state: UiState<List<ArticleDto>>,
    hasMore: Boolean,
    onClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = onRetry)
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(title = "Новости не найдены", subtitle = "Попробуйте изменить фильтры")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, article ->
                        val delay = (index * 50).coerceAtMost(500)
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(delay.toLong())
                            itemVisible = true
                        }
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (itemVisible) 1f else 0f,
                            animationSpec = tween(350),
                            label = "aItemAlpha"
                        )
                        val itemOffset by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 50f,
                            animationSpec = tween(350, easing = EaseOutBack),
                            label = "aItemOffset"
                        )
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = itemAlpha
                                translationY = itemOffset
                            }
                        ) {
                            ArticleCard(article = article, onClick = { onClick(article.id) })
                        }
                    }
                    if (hasMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                TextButton(onClick = onLoadMore) {
                                    Text("Загрузить ещё", color = Accent, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleDto, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Cover image
            if (article.coverImageUrl != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Border,
                    modifier = Modifier.size(80.dp)
                ) {
                    AsyncImage(
                        model = article.coverImageUrl,
                        contentDescription = article.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(14.dp))
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                article.excerpt?.let { excerpt ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = excerpt,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    article.category?.let { category ->
                        PillBadge(
                            text = getCategoryLabel(category),
                            color = Accent
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = formatShortDate(article.publishedAt ?: article.createdAt),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Shared components
// ═══════════════════════════════════════════════════

@Composable
private fun PillBadge(text: String, color: Color, icon: ImageVector? = null) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

// ═══════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════

private fun getStatusLabel(status: String): String = when (status) {
    "registration_open" -> "Регистрация"
    "in_progress" -> "Идёт"
    "completed" -> "Завершён"
    "check_in" -> "Check-in"
    "draft" -> "Черновик"
    else -> status
}

@Composable
private fun getStatusColor(status: String): Color = when (status) {
    "registration_open" -> ILeaderColors.Success
    "in_progress" -> ILeaderColors.Info
    "completed" -> TextSecondary
    "check_in" -> ILeaderColors.Warning
    else -> TextSecondary
}

private fun getCategoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "report" -> "Отчёт"
    "interview" -> "Интервью"
    "analytics" -> "Аналитика"
    "review" -> "Обзор"
    "announcement" -> "Анонс"
    else -> category.replaceFirstChar { it.uppercase() }
}

private fun getLeagueStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт"
    "registration_open" -> "Регистрация"
    "completed" -> "Завершена"
    "upcoming" -> "Скоро"
    else -> status
}

@Composable
private fun getLeagueStatusColor(status: String): Color = when (status) {
    "in_progress" -> ILeaderColors.Info
    "registration_open" -> ILeaderColors.Success
    "completed" -> TextSecondary
    "upcoming" -> ILeaderColors.Warning
    else -> TextSecondary
}

private fun getMedalEmoji(index: Int): String = when (index) {
    0 -> "\uD83E\uDD47"
    1 -> "\uD83E\uDD48"
    2 -> "\uD83E\uDD49"
    else -> "${index + 1}."
}

// ═══════════════════════════════════════════════════
// Leagues
// ═══════════════════════════════════════════════════

@Composable
private fun LeaguesContent(
    leagues: List<MockLeague>,
    onLeagueClick: (String, String, String?) -> Unit
) {
    if (leagues.isEmpty()) {
        EmptyState(title = "Лиги не найдены", subtitle = "Скоро здесь появятся лиги")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(leagues) { index, league ->
                val delay = (index * 80).coerceAtMost(400)
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(delay.toLong())
                    itemVisible = true
                }
                val itemAlpha by animateFloatAsState(
                    targetValue = if (itemVisible) 1f else 0f,
                    animationSpec = tween(350),
                    label = "lItemAlpha"
                )
                val itemOffset by animateFloatAsState(
                    targetValue = if (itemVisible) 0f else 50f,
                    animationSpec = tween(350, easing = EaseOutBack),
                    label = "lItemOffset"
                )
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = itemAlpha
                        translationY = itemOffset
                    }
                ) {
                    LeagueMiniCard(
                        league = league,
                        onClick = { onLeagueClick(league.name, league.sportName, league.imageUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeagueMiniCard(league: MockLeague, onClick: () -> Unit) {
    val accentColor = Accent
    val cardBg = CardBg
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val textMuted = TextMuted
    val borderColor = Border

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Hero image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                AsyncImage(
                    model = league.imageUrl,
                    contentDescription = league.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 30f
                            )
                        )
                )
                // Sport + status pills on image top
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sport pill with icon
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                sportIcon(league.sportName),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = league.sportName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getLeagueStatusColor(league.status).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = getLeagueStatusLabel(league.status),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                // Name on image bottom
                Text(
                    text = league.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            // Content below image
            Column(modifier = Modifier.padding(12.dp)) {
                // Stage timeline header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Этапы",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${league.completedStages}/${league.totalStages}",
                        color = textMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                StageTimeline(
                    totalStages = league.totalStages,
                    completedStages = league.completedStages,
                    accentColor = accentColor,
                    borderColor = borderColor
                )

                // Top 3 leaders
                if (league.leaders.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Лидеры",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    league.leaders.forEachIndexed { idx, leader ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = getMedalEmoji(idx),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = leader.name,
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${leader.points} очк.",
                                color = textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Next stage date + participants
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    league.nextStageDate?.let { date ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = date,
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${league.participants}",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageTimeline(
    totalStages: Int,
    completedStages: Int,
    accentColor: Color,
    borderColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0 until totalStages) {
            val isCompleted = i < completedStages
            val isCurrent = i == completedStages

            // Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(20.dp)
            ) {
                if (isCompleted) {
                    // Completed: accent filled with check
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else if (isCurrent) {
                    // Current: accent border, empty
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.dp, accentColor, CircleShape)
                    )
                } else {
                    // Future: gray
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(borderColor)
                    )
                }
            }

            // Line between circles
            if (i < totalStages - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (i < completedStages) accentColor else borderColor)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Data classes
// ═══════════════════════════════════════════════════

private data class MockLeague(
    val name: String, val sportName: String, val totalStages: Int,
    val completedStages: Int, val status: String, val participants: Int,
    val imageUrl: String?, val nextStageDate: String?,
    val leaders: List<MockLeader>
)

private data class MockLeader(val name: String, val points: Int)
