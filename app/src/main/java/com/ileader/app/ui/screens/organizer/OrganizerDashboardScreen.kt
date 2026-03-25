package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.DashboardData
import com.ileader.app.ui.viewmodels.OrganizerDashboardViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

internal fun statusLabel(status: String?): String = when (status) {
    "draft" -> "Черновик"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "in_progress" -> "В процессе"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    else -> status ?: "—"
}

internal fun isActiveStatus(status: String?): Boolean =
    status in listOf("registration_open", "check_in", "in_progress")

private fun statusColor(status: String?): Color = when (status) {
    "registration_open" -> Color(0xFF22C55E)
    "in_progress" -> Color(0xFFF97316)
    "check_in" -> Color(0xFF3B82F6)
    "completed" -> Color(0xFFE53535)
    else -> Color.White
}

@Composable
fun OrganizerDashboardScreen(
    user: User,
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val vm: OrganizerDashboardViewModel = viewModel()
    val state by vm.state.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    val detailViewModel: AthleteTournamentsViewModel = viewModel()

    LaunchedEffect(user.id) { vm.load(user.id) }
    LaunchedEffect(user.id) { detailViewModel.load(user.id) }

    // Tournament detail screen
    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = detailViewModel,
            onBack = { selectedTournamentId = null },
            onShowQrTicket = { _, _ -> }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> DashboardContent(
            user, s.data, vm, onNavigateToTournaments, onNavigateToNotifications,
            onTournamentClick = { selectedTournamentId = it }
        )
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: DashboardData,
    vm: OrganizerDashboardViewModel,
    onNavigateToTournaments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    val stats = data.stats
    val upcoming = data.upcomingTournaments
    val pendingRegistrations = data.recentRegistrations.filter { it.status == "pending" }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // -- HEADER --
            FadeIn(visible, 0) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Привет, ${user.displayName.split(" ").firstOrNull() ?: user.displayName}",
                            fontSize = 14.sp,
                            color = DarkTheme.TextMuted,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            "Мои турниры",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkTheme.TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                }
            }

            Spacer(Modifier.height(24.dp))

            // -- STATS ROW --
            FadeIn(visible, 150) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashStatItem(Modifier.weight(1f), stats.activeTournaments.toString(), "Активных", Icons.Default.EmojiEvents)
                        DashStatItem(Modifier.weight(1f), stats.totalParticipants.toString(), "Участников", Icons.Default.Groups)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashStatItem(Modifier.weight(1f), stats.totalLocations.toString(), "Локаций", Icons.Default.LocationOn)
                        DashStatItem(Modifier.weight(1f), stats.totalTournaments.toString(), "Всего", Icons.Default.CalendarMonth)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- HERO CARD -- next upcoming tournament
            FadeIn(visible, 250) {
                val hero = upcoming.firstOrNull()
                if (hero != null) {
                    HeroTournamentCard(
                        tournament = hero,
                        onClick = { onTournamentClick(hero.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- UPCOMING TOURNAMENTS -- horizontal scroll
            FadeIn(visible, 350) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ближайшие турниры",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = onNavigateToTournaments, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (upcoming.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет предстоящих турниров")
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        upcoming.forEachIndexed { i, t ->
                            TournamentScrollCard(
                                tournament = t,
                                seed = i + 1,
                                onClick = { onTournamentClick(t.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- PENDING REGISTRATIONS --
            if (pendingRegistrations.isNotEmpty()) {
                FadeIn(visible, 500) {
                    SectionHeader("Последние заявки")
                    Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pendingRegistrations.forEach { p ->
                            DashPendingCard(p,
                                onApprove = { vm.approveParticipant(p.tournamentId, p.athleteId, user.id) },
                                onDecline = { vm.declineParticipant(p.tournamentId, p.athleteId, user.id) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // -- QUICK ACTIONS --
            FadeIn(visible, 600) {
                Text(
                    "Быстрые действия",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(Modifier.weight(1f), Icons.Default.EmojiEvents, "Турниры", onNavigateToTournaments)
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Notifications, "Уведомления", onNavigateToNotifications)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════

@Composable
private fun DashStatItem(modifier: Modifier, value: String, label: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = DarkTheme.Accent)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkTheme.TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = DarkTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroTournamentCard(tournament: TournamentDto, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val sportName = tournament.sports?.name ?: ""
    val heroImage = tournamentImageUrl(sportName, tournament.imageUrl)

    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1a0a0a))
            .clickable(onClick = onClick)
    ) {
        if (heroImage != null) {
            AsyncImage(
                model = heroImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF1a0a0a)))
        }
        // Dark gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Content
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    "Следующий турнир",
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Column {
                Text(
                    tournament.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.width(4.dp))
                        Text(formatShortDate(tournament.startDate), fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    if (sportName.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(sportIcon(sportName), null, Modifier.size(14.dp), Color.White.copy(alpha = 0.8f))
                            Text(sportName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentScrollCard(tournament: TournamentDto, seed: Int = 0, onClick: () -> Unit = {}) {
    val sportName = tournament.sports?.name ?: ""
    val scrollImage = tournamentImageUrl(sportName, tournament.imageUrl, seed)
    val sColor = statusColor(tournament.status)

    Box(
        Modifier
            .width(180.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1a0808))
            .clickable(onClick = onClick)
    ) {
        if (scrollImage != null) {
            AsyncImage(
                model = scrollImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF1a0808)))
        }
        // Dark overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = sColor.copy(alpha = 0.2f)
            ) {
                Text(
                    statusLabel(tournament.status),
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = sColor
                )
            }
            Column {
                Text(
                    tournament.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatShortDate(tournament.startDate),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun DashPendingCard(participant: ParticipantDto, onApprove: () -> Unit, onDecline: () -> Unit) {
    val name = participant.profiles?.name ?: "Участник"
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(AccentSoft), contentAlignment = Alignment.Center) {
                Text(name.first().toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(participant.tournaments?.name ?: "Ожидает подтверждения", fontSize = 12.sp, color = TextMuted)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Accent.copy(alpha = 0.10f)).clickable(onClick = onApprove), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, "Одобрить", tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(TextMuted.copy(alpha = 0.10f)).clickable(onClick = onDecline), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, "Отклонить", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextPrimary,
                maxLines = 1
            )
        }
    }
}
