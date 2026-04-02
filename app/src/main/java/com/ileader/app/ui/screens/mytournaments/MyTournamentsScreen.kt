package com.ileader.app.ui.screens.mytournaments

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EmptyState
import com.ileader.app.ui.components.FadeIn
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.sportColor
import com.ileader.app.ui.components.sportIcon
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
    onHelperManagement: (String, String) -> Unit = { _, _ -> }
) {
    val vm: MyTournamentsViewModel = viewModel()
    val roleTournaments by vm.roleTournaments.collectAsState()

    var started by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(Filter.ALL) }

    LaunchedEffect(user.id) {
        vm.load(user.id, user.role)
        started = true
    }

    val isDark = DarkTheme.isDark

    // Pre-compute data outside LazyColumn
    val state = roleTournaments
    val all = if (state is UiState.Success) extractTournaments(user.role, state.data) else emptyList()
    val active = all.filter { it.status in listOf("registration_open", "in_progress", "check_in") }
    val upcoming = all.filter { it.status in listOf("registration_closed", "draft") }
    val completed = all.filter { it.status in listOf("completed", "cancelled") }
    val heroSport = all.firstOrNull()?.sportName
    val heroImageUrl = heroSport?.let { SportViewModel.getFallbackImage(SportDto(id = "", name = it)) }
    val filtered = when (selectedFilter) {
        Filter.ALL -> all
        Filter.ACTIVE -> active
        Filter.UPCOMING -> upcoming
        Filter.COMPLETED -> completed
    }
    val today = remember { java.time.LocalDate.now() }
    val nearest = remember(all) {
        all.filter { t ->
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        when (state) {
            is UiState.Loading -> {
                item {
                    HeroSection(user = user, total = 0, active = 0, completed = 0, heroImageUrl = null)
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { LoadingScreen() }
                }
            }
            is UiState.Error -> {
                item {
                    HeroSection(user = user, total = 0, active = 0, completed = 0, heroImageUrl = null)
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.padding(16.dp)) { EmptyState(title = "Ошибка загрузки", subtitle = state.message) }
                }
            }
            is UiState.Success -> {

                // ── 1. Hero ──
                item {
                    FadeIn(visible = started, delayMs = 0) {
                        HeroSection(user = user, total = all.size, active = active.size, completed = completed.size, heroImageUrl = heroImageUrl)
                    }
                }

                if (all.isEmpty()) {
                    item {
                        Spacer(Modifier.height(40.dp))
                        EmptyState(title = "Нет турниров", subtitle = "Вы пока не участвуете в турнирах")
                    }
                } else {
                    // ── 2. Countdown (nearest tournament) ──
                    if (nearest != null && daysUntil != null) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            FadeIn(visible = started, delayMs = 60) {
                                CountdownCard(
                                    data = nearest,
                                    daysUntil = daysUntil,
                                    onClick = { onTournamentClick(nearest.id) }
                                )
                            }
                        }
                    }

                    // ── 3. Working filter chips ──
                    item {
                        Spacer(Modifier.height(16.dp))
                        FadeIn(visible = started, delayMs = 120) {
                            FilterChips(
                                selected = selectedFilter,
                                onSelect = { selectedFilter = it },
                                counts = mapOf(
                                    Filter.ALL to all.size,
                                    Filter.ACTIVE to active.size,
                                    Filter.UPCOMING to upcoming.size,
                                    Filter.COMPLETED to completed.size
                                )
                            )
                        }
                    }

                    // ── 4. Vertical tournament list ──
                    if (filtered.isEmpty()) {
                        item {
                            Spacer(Modifier.height(24.dp))
                            EmptyState(
                                title = "Нет турниров",
                                subtitle = "В этой категории пока нет турниров"
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, it -> it.id }) { index, t ->
                            val delay = (180 + index * 50).coerceAtMost(500)
                            FadeIn(visible = started, delayMs = delay) {
                                CompactTournamentCard(
                                    data = t,
                                    onClick = { onTournamentClick(t.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Hero
// ══════════════════════════════════════════════════════════

@Composable
private fun HeroSection(user: User, total: Int, active: Int, completed: Int, heroImageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        if (heroImageUrl != null) {
            AsyncImage(model = heroImageUrl, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.5f), Color.Black.copy(0.75f)))))
        } else {
            Box(Modifier.matchParentSize().background(Brush.linearGradient(listOf(ILeaderColors.DarkRed, ILeaderColors.PrimaryRed, Color(0xFFFF8A80)))))
        }
        Column(
            Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 20.dp)
        ) {
            Text("Мои турниры", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text(getRoleSubtitle(user.role), fontSize = 14.sp, color = Color.White.copy(0.7f))
            if (total > 0) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill("Всего", total, Color.White.copy(0.2f))
                    if (active > 0) HeroPill("Активных", active, ILeaderColors.Success.copy(0.3f))
                    if (completed > 0) HeroPill("Завершённых", completed, Color.White.copy(0.15f))
                }
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: Int, bg: Color) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$value", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.White.copy(0.8f))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Countdown card (nearest tournament)
// ══════════════════════════════════════════════════════════

@Composable
private fun CountdownCard(data: TournamentCardData, daysUntil: Int, onClick: () -> Unit) {
    val sportBgColor = if (data.sportName != null) sportColor(data.sportName) else Accent
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Countdown circle
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(
                    Brush.linearGradient(listOf(sportBgColor, sportBgColor.copy(0.6f)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (daysUntil) { 0 -> "!" ; else -> "$daysUntil" },
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                    )
                    if (daysUntil > 0) {
                        Text("дн.", fontSize = 10.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    when (daysUntil) { 0 -> "Сегодня!"; 1 -> "Завтра"; else -> "Через $daysUntil дн." },
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = sportBgColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    data.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
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
                        Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(formatDateShort(data.date), fontSize = 12.sp, color = TextMuted)
                    }
                }
            }

            Box(
                Modifier.size(32.dp).clip(CircleShape).background(LocalAppColors.current.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Accent, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Working filter chips
// ══════════════════════════════════════════════════════════

@Composable
private fun FilterChips(selected: Filter, onSelect: (Filter) -> Unit, counts: Map<Filter, Int>) {
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
                border = if (!isSelected && DarkTheme.isDark) DarkTheme.cardBorderStroke else null,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                    if (count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Compact vertical tournament card
// ══════════════════════════════════════════════════════════

@Composable
private fun CompactTournamentCard(data: TournamentCardData, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    val sportBgColor = if (data.sportName != null) sportColor(data.sportName) else Accent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Sport color strip + image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val imgUrl = data.imageUrl ?: data.sportName?.let {
                    SportViewModel.getFallbackImage(SportDto(id = "", name = it))
                }
                if (imgUrl != null) {
                    AsyncImage(
                        model = imgUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.2f)))
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(sportBgColor.copy(0.8f), sportBgColor.copy(0.4f)))
                        )
                    )
                }
                // Sport icon centered
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    data.sportName?.let {
                        Icon(sportIcon(it), null, tint = Color.White.copy(0.9f), modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Title
                Text(
                    data.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // Info row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(formatDateShort(data.date), fontSize = 12.sp, color = TextMuted)
                    }
                    if (data.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                            Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(data.location, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Participants
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${data.participantCount} уч.", fontSize = 12.sp, color = TextMuted)
                }
            }

            // Status pill
            val statusColor = getStatusColor(data.status)
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.15f)) {
                Text(
                    getStatusLabel(data.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                )
            }
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
    val position: Int? = null
)

private fun extractTournaments(role: UserRole, data: List<Any>): List<TournamentCardData> {
    return when (role) {
        UserRole.ATHLETE -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<Tournament>)?.map { t ->
                TournamentCardData(t.id, t.name, t.sportName, t.startDate, t.location, t.status.name.lowercase(), t.imageUrl, t.currentParticipants)
            } ?: emptyList()
        }
        UserRole.ORGANIZER -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<TournamentWithCountsDto>)?.map { t ->
                TournamentCardData(t.id, t.name, t.sportName, t.startDate ?: "", t.locationName ?: "", t.status ?: "", t.imageUrl, t.participantCount)
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
                    is Tournament -> TournamentCardData(item.id, item.name, item.sportName, item.startDate, item.location, item.status.name.lowercase(), item.imageUrl, item.currentParticipants)
                    is TournamentWithCountsDto -> TournamentCardData(item.id, item.name, item.sportName, item.startDate ?: "", item.locationName ?: "", item.status ?: "", item.imageUrl, item.participantCount)
                    else -> null
                }
            }
        }
    }
}

private fun getRoleSubtitle(role: UserRole): String = when (role) {
    UserRole.ATHLETE -> "Ваши турниры"
    UserRole.TRAINER -> "Турниры вашей команды"
    UserRole.ORGANIZER -> "Управление турнирами"
    UserRole.REFEREE -> "Назначенные турниры"
    UserRole.MEDIA -> "Аккредитации и события"
    UserRole.SPONSOR -> "Спонсируемые турниры"
    UserRole.ADMIN -> "Все турниры платформы"
    UserRole.CONTENT_MANAGER -> "Турниры и контент"
    UserRole.USER -> "Турниры зрителя"
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
    "registration_open" -> ILeaderColors.Success
    "in_progress" -> ILeaderColors.Info
    "check_in" -> ILeaderColors.Warning
    "completed" -> Color(0xFF8E8E93)
    "cancelled" -> Color(0xFFEF4444)
    "draft" -> Color(0xFF8B5CF6)
    else -> Color(0xFF8E8E93)
}
