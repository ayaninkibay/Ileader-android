package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MyTournamentsViewModel
import com.ileader.app.ui.viewmodels.SportViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private enum class Tab(val label: String) {
    MY("Мои турниры"), FAVORITES("Избранные ★")
}

private enum class Filter(val label: String) {
    ALL("Все"), ACTIVE("Активные"), UPCOMING("Предстоящие"), COMPLETED("Завершённые")
}

@Composable
fun MyTournamentsScreen(
    user: User,
    onTournamentClick: (String) -> Unit,
    onQrScan: (String, String) -> Unit = { _, _ -> },
    onManualCheckIn: (String, String) -> Unit = { _, _ -> },
    onEditTournament: (String) -> Unit = {},
    onHelperManagement: (String, String) -> Unit = { _, _ -> },
    onCreateTournament: () -> Unit = {}
) {
    val vm: MyTournamentsViewModel = viewModel()
    val roleTournaments by vm.roleTournaments.collectAsState()
    val favoriteTournaments by vm.favoriteTournaments.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val favoritesPref = remember { com.ileader.app.data.preferences.FavoritesPreference(context) }
    val favoriteIds by favoritesPref.favoriteTournamentIds.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(if (user.role == UserRole.USER) Tab.FAVORITES else Tab.MY) }
    var selectedFilter by remember { mutableStateOf(Filter.ALL) }

    val isDark = DarkTheme.isDark

    LaunchedEffect(user.id) {
        vm.load(user.id, user.role)
    }

    LaunchedEffect(favoriteIds) {
        if (favoriteIds.isNotEmpty()) vm.loadFavorites(favoriteIds)
    }

    // Data extraction
    val myAll = if (roleTournaments is UiState.Success) {
        extractTournaments(user.role, (roleTournaments as UiState.Success).data)
    } else emptyList()

    val favAll = if (favoriteTournaments is UiState.Success) {
        (favoriteTournaments as UiState.Success<List<TournamentWithCountsDto>>).data.map { t ->
            TournamentCardData(t.id, t.name, t.sportName, t.startDate ?: "", t.locationName ?: "",
                t.status ?: "", t.imageUrl, t.participantCount, t.maxParticipants)
        }
    } else emptyList()

    val currentList = if (selectedTab == Tab.MY) myAll else favAll
    val active = currentList.filter { it.status in listOf("registration_open", "in_progress", "check_in") }
    val upcoming = currentList.filter { it.status in listOf("registration_closed", "draft") }
    val completed = currentList.filter { it.status in listOf("completed", "cancelled") }
    val filtered = when (selectedFilter) {
        Filter.ALL -> currentList; Filter.ACTIVE -> active
        Filter.UPCOMING -> upcoming; Filter.COMPLETED -> completed
    }

    val today = remember { java.time.LocalDate.now() }
    val nearest = remember(myAll) {
        myAll.filter { t ->
            t.status in listOf("registration_open", "in_progress", "check_in") &&
                t.date.take(10).let { it >= today.toString() }
        }.minByOrNull { it.date }
    }
    val daysUntil = remember(nearest) {
        nearest?.date?.take(10)?.let {
            try { java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse(it)).toInt() }
            catch (_: Exception) { null }
        }
    }

    val heroSport = myAll.firstOrNull()?.sportName ?: favAll.firstOrNull()?.sportName
    val heroImageUrl = heroSport?.let { SportViewModel.getFallbackImage(SportDto(id = "", name = it)) }

    val isLoading = (selectedTab == Tab.MY && roleTournaments is UiState.Loading) ||
        (selectedTab == Tab.FAVORITES && favoriteTournaments is UiState.Loading && favoriteIds.isNotEmpty())

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ══════════════════════════════════════
        // HERO
        // ══════════════════════════════════════
        item {
            HeroSection(
                user = user,
                total = myAll.size,
                active = active.size,
                completed = completed.size,
                heroImageUrl = heroImageUrl,
                isDark = isDark
            )
            
        }

        // ══════════════════════════════════════
        // TAB SWITCHER (Мои / Избранные)
        // ══════════════════════════════════════
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Tab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val count = if (tab == Tab.MY) myAll.size else favAll.size
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent else CardBg,
                        border = if (!isSelected && isDark) androidx.compose.foundation.BorderStroke(1.dp, Border.copy(0.2f))
                        else if (!isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, Border.copy(0.3f)) else null,
                        shadowElevation = 0.dp,
                        modifier = Modifier.weight(1f).clickable { selectedTab = tab; selectedFilter = Filter.ALL }
                    ) {
                        Row(
                            Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                tab.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                            if (count > 0) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }

        // ══════════════════════════════════════
        // COUNTDOWN (nearest tournament, only on MY tab)
        // ══════════════════════════════════════
        if (selectedTab == Tab.MY && nearest != null && daysUntil != null) {
            item {
                Spacer(Modifier.height(12.dp))
                CountdownCard(
                    data = nearest,
                    daysUntil = daysUntil,
                    onClick = { onTournamentClick(nearest.id) },
                    isDark = isDark
                )
                
            }
        }

        // ══════════════════════════════════════
        // FILTER CHIPS
        // ══════════════════════════════════════
        if (currentList.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                FilterChips(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                    counts = mapOf(
                        Filter.ALL to currentList.size,
                        Filter.ACTIVE to active.size,
                        Filter.UPCOMING to upcoming.size,
                        Filter.COMPLETED to completed.size
                    ),
                    isDark = isDark
                )
                
            }
        }

        // ══════════════════════════════════════
        // LOADING
        // ══════════════════════════════════════
        if (isLoading) {
            item {
                Spacer(Modifier.height(40.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            }
        }

        // ══════════════════════════════════════
        // EMPTY STATE
        // ══════════════════════════════════════
        if (!isLoading && currentList.isEmpty()) {
            item {
                Spacer(Modifier.height(40.dp))
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(72.dp).background(Accent.copy(0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (selectedTab == Tab.FAVORITES) Icons.Default.FavoriteBorder else Icons.Default.EmojiEvents,
                            null, tint = Accent.copy(0.4f), modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (selectedTab == Tab.FAVORITES) "Нет избранных турниров"
                        else "Нет турниров",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (selectedTab == Tab.FAVORITES) "Нажмите ★ на турнире, чтобы добавить"
                        else "Зарегистрируйтесь на турнир на вкладке Спорт",
                        fontSize = 14.sp, color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )
                }
                
            }
        }

        // ══════════════════════════════════════
        // FILTERED EMPTY
        // ══════════════════════════════════════
        if (!isLoading && currentList.isNotEmpty() && filtered.isEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                EmptyState(title = "Нет турниров", subtitle = "В этой категории пока пусто")
            }
        }

        // ══════════════════════════════════════
        // TOURNAMENT CARDS (big, beautiful)
        // ══════════════════════════════════════
        if (!isLoading && filtered.isNotEmpty()) {
            itemsIndexed(filtered, key = { _, it -> it.id }) { index, t ->
                val delay = (140 + index * 50).coerceAtMost(500)
                BigTournamentCard(
                    data = t,
                    onClick = { onTournamentClick(t.id) },
                    isOrganizer = user.role == UserRole.ORGANIZER,
                    onEdit = { onEditTournament(t.id) },
                    onQrScan = { onQrScan(t.id, t.name) },
                    onHelpers = { onHelperManagement(t.id, t.name) },
                    isDark = isDark
                )

            }
        }

        // ══════════════════════════════════════
        // CREATE TOURNAMENT (organizer only)
        // ══════════════════════════════════════
        if (user.role == UserRole.ORGANIZER) {
            item {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onCreateTournament() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(Accent, ILeaderColors.DarkRed)),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape)
                                    .background(Color.White.copy(0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Создать турнир", fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White
                                )
                                Text(
                                    "Организуйте новое мероприятие",
                                    fontSize = 12.sp, color = Color.White.copy(0.7f)
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                                tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Hero Section
// ══════════════════════════════════════════════════════════

@Composable
private fun HeroSection(
    user: User, total: Int, active: Int, completed: Int,
    heroImageUrl: String?, isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Background
        if (heroImageUrl != null) {
            AsyncImage(model = heroImageUrl, contentDescription = null,
                modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(0.5f), Color.Black.copy(0.8f)))
            ))
        } else {
            Box(Modifier.matchParentSize().background(
                Brush.linearGradient(listOf(ILeaderColors.DarkRed, ILeaderColors.PrimaryRed, Color(0xFFFF8A80)))
            ))
        }

        Column(
            Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 20.dp)
        ) {
            // Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(Color.White.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Мои турниры", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, letterSpacing = (-0.5).sp)
                    Text(getRoleSubtitle(user.role), fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stat cards row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroStatCard("Всего", total, Icons.Default.EmojiEvents, Color(0xFF3B82F6), Modifier.weight(1f))
                HeroStatCard("Активных", active, Icons.Default.PlayArrow, Color(0xFF10B981), Modifier.weight(1f))
                HeroStatCard("Завершённых", completed, Icons.Default.Check, Color(0xFF8B5CF6), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStatCard(label: String, value: Int, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text("$value", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(0.7f), maxLines = 1)
        }
    }
}

// ══════════════════════════════════════════════════════════
// Countdown Card (nearest tournament)
// ══════════════════════════════════════════════════════════

@Composable
private fun CountdownCard(
    data: TournamentCardData, daysUntil: Int, onClick: () -> Unit, isDark: Boolean
) {
    val sportBgColor = if (data.sportName != null) sportColor(data.sportName) else Accent
    val imgUrl = data.imageUrl ?: data.sportName?.let { SportViewModel.getFallbackImage(SportDto(id = "", name = it)) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, sportBgColor.copy(0.2f)) else null
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Sport image thumbnail
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
            ) {
                if (imgUrl != null) {
                    AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(sportBgColor.copy(0.8f), sportBgColor.copy(0.4f)))
                    ))
                }
                // Days badge
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when (daysUntil) { 0 -> "!"; else -> "$daysUntil" },
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    when (daysUntil) { 0 -> "Сегодня!"; 1 -> "Завтра"; else -> "Через $daysUntil дн." },
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = sportBgColor
                )
                Spacer(Modifier.height(2.dp))
                Text(data.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    data.sportName?.let { sport ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(sportIcon(sport), null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(sport, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(formatDateShort(data.date), fontSize = 12.sp, color = TextMuted)
                    }
                }
            }

            Box(
                Modifier.size(32.dp).clip(CircleShape).background(sportBgColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = sportBgColor, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Filter Chips
// ══════════════════════════════════════════════════════════

@Composable
private fun FilterChips(selected: Filter, onSelect: (Filter) -> Unit, counts: Map<Filter, Int>, isDark: Boolean) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(Filter.entries.toList()) { filter ->
            val isSelected = selected == filter
            val count = counts[filter] ?: 0
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) Accent else CardBg,
                border = if (!isSelected && isDark) DarkTheme.cardBorderStroke
                else if (!isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, Border.copy(0.3f)) else null,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary)
                    if (count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Big Tournament Card (full-width cover image)
// ══════════════════════════════════════════════════════════

@Composable
private fun BigTournamentCard(
    data: TournamentCardData,
    onClick: () -> Unit,
    isOrganizer: Boolean = false,
    onEdit: () -> Unit = {},
    onQrScan: () -> Unit = {},
    onHelpers: () -> Unit = {},
    isDark: Boolean
) {
    val sportBgColor = if (data.sportName != null) sportColor(data.sportName) else Accent
    val imgUrl = data.imageUrl ?: data.sportName?.let {
        SportViewModel.getFallbackImage(SportDto(id = "", name = it))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Border.copy(0.15f)) else null
    ) {
        Column {
            // Cover image
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (imgUrl != null) {
                    AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(sportBgColor.copy(0.8f), sportBgColor.copy(0.4f)))
                    ))
                }
                // Gradient overlay
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))
                ))

                // Badges on image
                Row(
                    Modifier.align(Alignment.TopStart).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sport badge
                    data.sportName?.let { sport ->
                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.45f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sport), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(sport, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Status badge (top right)
                val statusColor = getStatusColor(data.status)
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(0.85f)
                ) {
                    Text(
                        getStatusLabel(data.status),
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }

                // Tournament name on image bottom
                Text(
                    data.name,
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp
                )
            }

            // Info section
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatDateShort(data.date), fontSize = 13.sp, color = TextSecondary)
                    }
                    // Location
                    if (data.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                            Icon(Icons.Outlined.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(data.location, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Participants progress bar
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${data.participantCount}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    data.maxParticipants?.let { max ->
                        if (max > 0) {
                            Text(" / $max", fontSize = 13.sp, color = TextMuted)
                            Spacer(Modifier.width(10.dp))
                            val progress = (data.participantCount.toFloat() / max).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (progress > 0.8f) Color(0xFFEF4444) else Accent,
                                trackColor = Border.copy(0.2f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                    if (data.maxParticipants == null || data.maxParticipants == 0) {
                        Text(" участников", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }

            // Organizer quick actions
            if (isOrganizer) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrganizerActionChip("Редактировать", Icons.Default.Edit, onClick = onEdit, modifier = Modifier.weight(1f))
                    OrganizerActionChip("QR", Icons.Default.QrCodeScanner, onClick = onQrScan, modifier = Modifier.weight(1f))
                    OrganizerActionChip("Помощники", Icons.Default.People, onClick = onHelpers, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OrganizerActionChip(
    label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Accent.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Accent, maxLines = 1)
        }
    }
}

// ══════════════════════════════════════════════════════════
// Data model & helpers
// ══════════════════════════════════════════════════════════

private data class TournamentCardData(
    val id: String,
    val name: String,
    val sportName: String?,
    val date: String,
    val location: String,
    val status: String,
    val imageUrl: String?,
    val participantCount: Int,
    val maxParticipants: Int? = null
)

private fun extractTournaments(role: UserRole, data: List<Any>): List<TournamentCardData> {
    return when (role) {
        UserRole.ATHLETE -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<Tournament>)?.map { t ->
                TournamentCardData(t.id, t.name, t.sportName, t.startDate, t.location, t.status.name.lowercase(), t.imageUrl, t.currentParticipants, t.maxParticipants)
            } ?: emptyList()
        }
        UserRole.ORGANIZER -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<TournamentWithCountsDto>)?.map { t ->
                TournamentCardData(t.id, t.name, t.sportName, t.startDate ?: "", t.locationName ?: "",
                    t.status ?: "", t.imageUrl, t.participantCount, t.maxParticipants)
            } ?: emptyList()
        }
        UserRole.REFEREE -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<com.ileader.app.data.models.RefereeTournament>)?.map { t ->
                TournamentCardData(t.id, t.name, t.sport, t.date, t.location, t.status.name.lowercase(), null, 0)
            } ?: emptyList()
        }
        else -> {
            data.mapNotNull { item ->
                when (item) {
                    is Tournament -> TournamentCardData(item.id, item.name, item.sportName, item.startDate, item.location, item.status.name.lowercase(), item.imageUrl, item.currentParticipants, item.maxParticipants)
                    is TournamentWithCountsDto -> TournamentCardData(item.id, item.name, item.sportName, item.startDate ?: "", item.locationName ?: "", item.status ?: "", item.imageUrl, item.participantCount, item.maxParticipants)
                    else -> null
                }
            }
        }
    }
}

private fun getRoleSubtitle(role: UserRole): String = when (role) {
    UserRole.ATHLETE -> "Ваши турниры и избранные"
    UserRole.TRAINER -> "Турниры вашей команды"
    UserRole.ORGANIZER -> "Управление турнирами"
    UserRole.REFEREE -> "Назначенные турниры"
    UserRole.MEDIA -> "Аккредитации и события"
    UserRole.SPONSOR -> "Спонсируемые турниры"
    UserRole.ADMIN -> "Все турниры платформы"
    UserRole.CONTENT_MANAGER -> "Турниры и контент"
    UserRole.USER -> "Избранные и зрительские"
}

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val m = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${m.getOrElse(month) { "" }}"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    "draft" -> "Черновик"
    else -> status
}

private fun getStatusColor(status: String): Color = when (status) {
    "registration_open" -> Color(0xFF22C55E)
    "in_progress" -> Color(0xFF3B82F6)
    "check_in" -> Color(0xFF8B5CF6)
    "completed" -> Color(0xFF6B7280)
    "cancelled" -> Color(0xFFEF4444)
    "draft" -> Color(0xFFF59E0B)
    else -> Color(0xFF6B7280)
}
