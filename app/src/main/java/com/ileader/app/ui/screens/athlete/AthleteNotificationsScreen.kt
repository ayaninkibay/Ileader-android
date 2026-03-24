package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteNotificationsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteNotificationsScreen(
    user: User,
    onBack: () -> Unit
) {
    val viewModel: AthleteNotificationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> NotificationsContent(
            invites = s.data.invites,
            teamRequests = s.data.teamRequests,
            viewModel = viewModel,
            onBack = onBack
        )
    }
}

@Composable
private fun NotificationsContent(
    invites: List<TournamentInvite>,
    teamRequests: List<TeamRequest>,
    viewModel: AthleteNotificationsViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingInvites = invites.count { it.status == InviteStatus.PENDING }
    val pendingRequests = teamRequests.count { it.status == InviteStatus.PENDING }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // ── HEADER ──
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack, shape = CircleShape, color = DarkTheme.CardBg,
                    modifier = Modifier.size(40.dp).border(0.5.dp, DarkTheme.CardBorder, CircleShape)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(20.dp), DarkTheme.TextPrimary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Уведомления", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
            }

            // ── TABS ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = DarkTheme.Accent,
                modifier = Modifier.padding(horizontal = 20.dp),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Приглашения", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            if (pendingInvites > 0) {
                                Spacer(Modifier.width(6.dp))
                                Badge(containerColor = DarkTheme.Accent) { Text(pendingInvites.toString()) }
                            }
                        }
                    },
                    selectedContentColor = DarkTheme.Accent,
                    unselectedContentColor = DarkTheme.TextSecondary
                )
                Tab(
                    selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Заявки", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            if (pendingRequests > 0) {
                                Spacer(Modifier.width(6.dp))
                                Badge(containerColor = DarkTheme.Accent) { Text(pendingRequests.toString()) }
                            }
                        }
                    },
                    selectedContentColor = DarkTheme.Accent,
                    unselectedContentColor = DarkTheme.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── CONTENT ──
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (invites.isEmpty()) {
                            EmptyState("Нет приглашений", "Здесь будут приглашения на турниры")
                        } else {
                            invites.forEach { invite ->
                                InviteCard(invite, viewModel)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                    1 -> {
                        if (teamRequests.isEmpty()) {
                            EmptyState("Нет заявок", "Здесь будут заявки в команды")
                        } else {
                            teamRequests.forEach { request ->
                                TeamRequestCard(request, viewModel)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InviteCard(invite: TournamentInvite, viewModel: AthleteNotificationsViewModel) {
    val isActive = invite.status == InviteStatus.PENDING
    val chipColor = if (isActive) DarkTheme.Accent else DarkTheme.TextMuted

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AccentIconBox(Icons.Default.EmojiEvents)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(invite.tournamentName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(invite.createdAt, fontSize = 12.sp, color = DarkTheme.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(invite.status.displayName, chipColor)
            }

            if (invite.message != null) {
                Spacer(Modifier.height(10.dp))
                Text(invite.message, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 18.sp)
            }

            if (invite.status == InviteStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.respondToInvite(invite.id, false) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                        )
                    ) { Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { viewModel.respondToInvite(invite.id, true) },
                        Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) { Text("Принять", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun TeamRequestCard(request: TeamRequest, viewModel: AthleteNotificationsViewModel) {
    val isActive = request.status == InviteStatus.PENDING
    val chipColor = if (isActive) DarkTheme.Accent else DarkTheme.TextMuted

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.Groups)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(request.teamName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(request.createdAt, fontSize = 12.sp, color = DarkTheme.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(request.status.displayName, chipColor)
            }

            if (request.message != null) {
                Spacer(Modifier.height(10.dp))
                Text(request.message, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 18.sp)
            }

            if (request.responseMessage != null && request.status != InviteStatus.PENDING) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.CardBorder.copy(alpha = 0.3f)) {
                    Row(
                        Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(10.dp)).padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.QuestionAnswer, null, Modifier.size(16.dp), DarkTheme.TextMuted)
                        Spacer(Modifier.width(8.dp))
                        Text(request.responseMessage, fontSize = 13.sp, color = DarkTheme.TextPrimary, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
