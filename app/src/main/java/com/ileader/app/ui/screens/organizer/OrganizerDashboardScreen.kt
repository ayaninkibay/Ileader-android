package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.ui.components.*
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

@Composable
fun OrganizerDashboardScreen(
    user: User,
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val vm: OrganizerDashboardViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> DashboardContent(user, s.data, vm, onNavigateToTournaments, onNavigateToNotifications)
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: DashboardData,
    vm: OrganizerDashboardViewModel,
    onNavigateToTournaments: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val stats = data.stats
    val upcoming = data.upcomingTournaments.take(3)
    val pendingRegistrations = data.recentRegistrations.filter { it.status == "pending" }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = Accent
    Box(Modifier.fillMaxSize().background(Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.03f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.03f)
            )
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ILeaderBrandHeader(role = user.role)
                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                }
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 200) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, stats.activeTournaments.toString(), "Активных")
                        StatItem(Modifier.weight(1f), Icons.Default.Groups, stats.totalParticipants.toString(), "Участников")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.LocationOn, stats.totalLocations.toString(), "Локаций")
                        StatItem(Modifier.weight(1f), Icons.Default.CalendarMonth, stats.totalTournaments.toString(), "Всего турниров")
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            if (upcoming.isNotEmpty()) {
                FadeIn(visible, 350) {
                    SectionHeader("Ближайшие турниры", "Все") { onNavigateToTournaments() }
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        upcoming.forEach { t -> DashTournamentCard(t, onClick = onNavigateToTournaments) }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            if (pendingRegistrations.isNotEmpty()) {
                FadeIn(visible, 500) {
                    SectionHeader("Последние заявки")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            FadeIn(visible, 600) {
                SectionHeader("Быстрые действия")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashQuickAction(Icons.Default.EmojiEvents, "Мои турниры") { onNavigateToTournaments() }
                    DashQuickAction(Icons.Default.Notifications, "Уведомления") { onNavigateToNotifications() }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DashTournamentCard(tournament: TournamentDto, onClick: () -> Unit) {
    DarkCard {
        Row(Modifier.clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("${tournament.startDate ?: ""} \u2022 ${tournament.locations?.name ?: ""}", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            StatusBadge(statusLabel(tournament.status), if (isActiveStatus(tournament.status)) Accent else TextMuted)
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
private fun DashQuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(130.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = DarkTheme.CardBg, border = DarkTheme.cardBorderStroke) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AccentIconBox(icon)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(), lineHeight = 16.sp)
        }
    }
}
