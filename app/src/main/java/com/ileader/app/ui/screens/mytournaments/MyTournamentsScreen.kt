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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
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

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

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
    LaunchedEffect(user.id) {
        vm.load(user.id, user.role)
        started = true
    }

    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Header ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Мои турниры",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = getRoleSubtitle(user.role),
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }
        }

        // ── Content ──
        when (val state = roleTournaments) {
            is UiState.Loading -> {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { LoadingScreen() }
                }
            }
            is UiState.Error -> {
                item {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 40.dp)) {
                        EmptyState(
                            title = "Ошибка загрузки",
                            subtitle = state.message
                        )
                    }
                }
            }
            is UiState.Success -> {
                val tournaments = extractTournaments(user.role, state.data)

                if (tournaments.isEmpty()) {
                    item {
                        Spacer(Modifier.height(40.dp))
                        EmptyState(
                            title = "Нет турниров",
                            subtitle = "Вы пока не участвуете в турнирах"
                        )
                    }
                } else {
                    // ── Active tournaments (horizontal) ──
                    val active = tournaments.filter { it.status in listOf("registration_open", "in_progress", "check_in") }
                    if (active.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            FadeIn(visible = started, delayMs = 0) {
                                Column(Modifier.padding(horizontal = 16.dp)) {
                                    SectionHeader(
                                        title = "Активные",
                                        count = active.size
                                    )
                                }
                            }
                        }
                        item {
                            FadeIn(visible = started, delayMs = 60) {
                                TournamentRow(
                                    tournaments = active,
                                    onTournamentClick = onTournamentClick
                                )
                            }
                        }
                    }

                    // ── Upcoming tournaments ──
                    val upcoming = tournaments.filter { it.status == "registration_closed" || it.status == "draft" }
                    if (upcoming.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            FadeIn(visible = started, delayMs = 120) {
                                Column(Modifier.padding(horizontal = 16.dp)) {
                                    SectionHeader(
                                        title = "Предстоящие",
                                        count = upcoming.size
                                    )
                                }
                            }
                        }
                        item {
                            FadeIn(visible = started, delayMs = 180) {
                                TournamentRow(
                                    tournaments = upcoming,
                                    onTournamentClick = onTournamentClick
                                )
                            }
                        }
                    }

                    // ── Completed tournaments ──
                    val completed = tournaments.filter { it.status in listOf("completed", "cancelled") }
                    if (completed.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            FadeIn(visible = started, delayMs = 240) {
                                Column(Modifier.padding(horizontal = 16.dp)) {
                                    SectionHeader(
                                        title = "Завершённые",
                                        count = completed.size
                                    )
                                }
                            }
                        }
                        item {
                            FadeIn(visible = started, delayMs = 300) {
                                TournamentRow(
                                    tournaments = completed,
                                    onTournamentClick = onTournamentClick
                                )
                            }
                        }
                    }

                    // ── Stats card ──
                    item {
                        Spacer(Modifier.height(20.dp))
                        FadeIn(visible = started, delayMs = 360) {
                            StatsCard(
                                total = tournaments.size,
                                active = active.size,
                                completed = completed.size
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournament row (horizontal, like HomeScreen)
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentRow(
    tournaments: List<TournamentCardData>,
    onTournamentClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(tournaments, key = { _, it -> it.id }) { index, t ->
            val delay = (index * 60).coerceAtMost(400)
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(delay.toLong())
                itemVisible = true
            }
            val itemAlpha by animateFloatAsState(
                targetValue = if (itemVisible) 1f else 0f,
                animationSpec = tween(400),
                label = "tItemAlpha$index"
            )
            val itemOffset by animateFloatAsState(
                targetValue = if (itemVisible) 0f else 40f,
                animationSpec = tween(400, easing = EaseOutBack),
                label = "tItemOffset$index"
            )
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = itemAlpha
                    translationY = itemOffset
                }
            ) {
                MyTournamentCard(
                    data = t,
                    onClick = { onTournamentClick(t.id) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournament card (HomeScreen style)
// ══════════════════════════════════════════════════════════

@Composable
private fun MyTournamentCard(data: TournamentCardData, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        tween(100), label = "cardPress"
    )

    val sportBgColor = if (data.sportName != null) sportColor(data.sportName) else Accent

    Surface(
        modifier = Modifier
            .width(260.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true; tryAwaitRelease(); isPressed = false },
                    onTap = { onClick() }
                )
            }
        ) {
            // Cover image or sport gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (data.imageUrl != null) {
                    AsyncImage(
                        model = data.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        sportBgColor.copy(alpha = 0.8f),
                                        sportBgColor.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                }

                // Dark overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // Sport + status pills on image
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    data.sportName?.let { sport ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    sportIcon(sport), null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(sport, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    val statusColor = getStatusColor(data.status)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            getStatusLabel(data.status),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Trophy icon bottom-right for completed
                if (data.status == "completed" && data.position != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = when (data.position) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "#${data.position}"
                            },
                            modifier = Modifier.padding(6.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Card body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = data.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.2).sp
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = Icons.Default.CalendarMonth,
                        text = formatDateShort(data.date)
                    )
                    if (data.location.isNotEmpty()) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = data.location,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

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
                            "${data.participantCount} участников",
                            fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LocalAppColors.current.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = Accent, modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Stats card (like RatingPromoCard in HomeScreen)
// ══════════════════════════════════════════════════════════

@Composable
private fun StatsCard(total: Int, active: Int, completed: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Всего", total)
                StatItem("Активных", active)
                StatItem("Завершённых", completed)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

// ══════════════════════════════════════════════════════════
// Section header (like HomeScreen)
// ══════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String, count: Int = 0) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.EmojiEvents, null,
            tint = Accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (count > 0) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Accent.copy(alpha = 0.1f)
            ) {
                Text(
                    "$count",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Accent
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

// ══════════════════════════════════════════════════════════
// Info chip (like HomeScreen)
// ══════════════════════════════════════════════════════════

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = TextMuted)
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
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
                TournamentCardData(
                    id = t.id,
                    name = t.name,
                    sportName = t.sportName,
                    date = t.startDate,
                    location = t.location,
                    status = t.status.name.lowercase(),
                    imageUrl = t.imageUrl,
                    participantCount = t.currentParticipants
                )
            } ?: emptyList()
        }
        UserRole.ORGANIZER -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<TournamentWithCountsDto>)?.map { t ->
                TournamentCardData(
                    id = t.id,
                    name = t.name,
                    sportName = t.sportName,
                    date = t.startDate ?: "",
                    location = t.locationName ?: "",
                    status = t.status ?: "",
                    imageUrl = t.imageUrl,
                    participantCount = t.participantCount
                )
            } ?: emptyList()
        }
        UserRole.REFEREE -> {
            @Suppress("UNCHECKED_CAST")
            (data as? List<com.ileader.app.data.models.RefereeTournament>)?.map { t ->
                TournamentCardData(
                    id = t.id,
                    name = t.name,
                    sportName = t.sport,
                    date = t.date,
                    location = t.location,
                    status = t.status.name.lowercase(),
                    imageUrl = null,
                    participantCount = 0
                )
            } ?: emptyList()
        }
        else -> {
            // For other roles, try to map generic data
            data.mapNotNull { item ->
                when (item) {
                    is Tournament -> TournamentCardData(
                        id = item.id,
                        name = item.name,
                        sportName = item.sportName,
                        date = item.startDate,
                        location = item.location,
                        status = item.status.name.lowercase(),
                        imageUrl = item.imageUrl,
                        participantCount = item.currentParticipants
                    )
                    is TournamentWithCountsDto -> TournamentCardData(
                        id = item.id,
                        name = item.name,
                        sportName = item.sportName,
                        date = item.startDate ?: "",
                        location = item.locationName ?: "",
                        status = item.status ?: "",
                        imageUrl = item.imageUrl,
                        participantCount = item.participantCount
                    )
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
