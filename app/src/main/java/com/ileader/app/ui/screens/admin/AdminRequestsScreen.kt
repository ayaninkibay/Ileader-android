package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TeamRequestDto
import com.ileader.app.data.remote.dto.TournamentInviteDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminRequestsViewModel

@Composable
fun AdminRequestsScreen(user: User) {
    val viewModel: AdminRequestsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            var activeTab by remember { mutableIntStateOf(0) }
            var filter by remember { mutableStateOf("all") }
            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }

            val invites = s.data.invites
            val teamReqs = s.data.teamRequests

            val pendingInvites = invites.count { it.status == "pending" }
            val pendingTeamReqs = teamReqs.count { it.status == "pending" }
            val acceptedCount = invites.count { it.status == "accepted" } + teamReqs.count { it.status == "accepted" }

            Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Заявки", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, letterSpacing = (-0.3).sp)
                    Spacer(Modifier.width(8.dp))
                    if (pendingInvites + pendingTeamReqs > 0) {
                        Badge(containerColor = Accent) {
                            Text("${pendingInvites + pendingTeamReqs}")
                        }
                    }
                }

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FadeIn(visible = started, delayMs = 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.Schedule,
                                    value = "${pendingInvites + pendingTeamReqs}", label = "Ожидают")
                                StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.Email,
                                    value = "${invites.size}", label = "Приглашений")
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.Groups,
                                    value = "${teamReqs.size}", label = "В команды")
                                StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle,
                                    value = "$acceptedCount", label = "Принято")
                            }
                        }
                    }

                    FadeIn(visible = started, delayMs = 150) {
                        DarkCard {
                            Row(Modifier.padding(4.dp)) {
                                TabButton("Приглашения", pendingInvites, activeTab == 0, { activeTab = 0 }, Modifier.weight(1f))
                                TabButton("В команды", pendingTeamReqs, activeTab == 1, { activeTab = 1 }, Modifier.weight(1f))
                            }
                        }
                    }

                    FadeIn(visible = started, delayMs = 300) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("all" to "Все", "pending" to "Ожидают", "answered" to "Отвечено").forEach { (key, label) ->
                                    DarkFilterChip(text = label, selected = filter == key, onClick = { filter = key })
                                }
                            }

                            InfoBanner("Только чтение — решение принимают участники")
                        }
                    }

                    FadeIn(visible = started, delayMs = 450) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (activeTab == 0) {
                                val filtered = when (filter) {
                                    "pending" -> invites.filter { it.status == "pending" }
                                    "answered" -> invites.filter { it.status != "pending" }
                                    else -> invites
                                }
                                if (filtered.isEmpty()) {
                                    EmptyState("Приглашений не найдено")
                                } else {
                                    filtered.forEach { invite -> InviteCard(invite) }
                                }
                            } else {
                                val filtered = when (filter) {
                                    "pending" -> teamReqs.filter { it.status == "pending" }
                                    "answered" -> teamReqs.filter { it.status != "pending" }
                                    else -> teamReqs
                                }
                                if (filtered.isEmpty()) {
                                    EmptyState("Заявок не найдено")
                                } else {
                                    filtered.forEach { req -> TeamRequestCard(req) }
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
private fun InviteCard(invite: TournamentInviteDto) {
    val status = invite.status ?: "pending"
    val statusColor = when (status) {
        "pending" -> ILeaderColors.Warning
        "accepted" -> ILeaderColors.Success
        "declined" -> ILeaderColors.Error
        else -> TextSecondary
    }
    val statusLabel = when (status) {
        "pending" -> "Ожидает"; "accepted" -> "Принято"; "declined" -> "Отклонено"; else -> status
    }
    val userRole = when (invite.role) {
        "referee" -> UserRole.REFEREE
        "sponsor" -> UserRole.SPONSOR
        "media" -> UserRole.MEDIA
        else -> UserRole.USER
    }
    val roleIcon = when (userRole) {
        UserRole.REFEREE -> Icons.Default.Shield
        UserRole.SPONSOR -> Icons.Default.AttachMoney
        UserRole.MEDIA -> Icons.Default.CameraAlt
        else -> Icons.Default.Person
    }
    val roleColor = AdminMockData.roleColor(userRole)

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(roleColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(roleIcon, null, tint = roleColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(invite.profiles?.name ?: "\u2014", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text(userRole.displayName, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                StatusBadge(text = statusLabel, color = statusColor)
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentSoft
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Accent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(invite.tournaments?.name ?: "\u2014", fontSize = 12.sp, color = TextPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (invite.message != null) {
                Spacer(Modifier.height(8.dp))
                Text(invite.message, fontSize = 13.sp, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text(invite.createdAt ?: "", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun TeamRequestCard(req: TeamRequestDto) {
    val status = req.status ?: "pending"
    val statusColor = when (status) {
        "pending" -> ILeaderColors.Warning
        "accepted" -> ILeaderColors.Success
        "declined" -> ILeaderColors.Error
        else -> TextSecondary
    }
    val statusLabel = when (status) {
        "pending" -> "Ожидает"; "accepted" -> "Принято"; "declined" -> "Отклонено"; else -> status
    }

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(AccentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, null, tint = Accent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(req.profiles?.name ?: "\u2014", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text("\u2192 ${req.teams?.name ?: "\u2014"}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                StatusBadge(text = statusLabel, color = statusColor)
            }
            if (req.message != null) {
                Spacer(Modifier.height(12.dp))
                Text(req.message, fontSize = 13.sp, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text(req.createdAt ?: "", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    badge: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        color = if (selected) Accent else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else TextSecondary)
            if (badge > 0) {
                Spacer(Modifier.width(6.dp))
                Badge(containerColor = if (selected) Color.White.copy(alpha = 0.2f) else Accent) {
                    Text("$badge", color = Color.White)
                }
            }
        }
    }
}
