package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.InviteStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.TrainerNotificationData
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TrainerNotificationsViewModel

@Composable
fun TrainerNotificationsScreen(
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: TrainerNotificationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            val notifications = s.data.notifications

            val joinRequests = notifications.filter { it.type == "join_request" }
            val sponsorOffers = notifications.filter { it.type == "sponsor_offer" }
            val otherNotifications = notifications.filter { it.type != "join_request" && it.type != "sponsor_offer" }

            val pendingJoin = joinRequests.count { it.status == InviteStatus.PENDING }
            val pendingSponsor = sponsorOffers.count { it.status == InviteStatus.PENDING }

            var selectedTab by remember { mutableIntStateOf(0) }

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
                                    Text("Заявки", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    if (pendingJoin > 0) {
                                        Spacer(Modifier.width(6.dp))
                                        Badge(containerColor = DarkTheme.Accent) { Text(pendingJoin.toString()) }
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
                                    Text("Спонсоры", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    if (pendingSponsor > 0) {
                                        Spacer(Modifier.width(6.dp))
                                        Badge(containerColor = DarkTheme.Accent) { Text(pendingSponsor.toString()) }
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
                                if (joinRequests.isEmpty()) {
                                    EmptyState("Нет заявок", "Здесь будут заявки на вступление в команду")
                                } else {
                                    joinRequests.forEach { notification ->
                                        NCard(notification, viewModel, user.id)
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                            1 -> {
                                if (sponsorOffers.isEmpty()) {
                                    EmptyState("Нет предложений", "Здесь будут предложения от спонсоров")
                                } else {
                                    sponsorOffers.forEach { notification ->
                                        NCard(notification, viewModel, user.id)
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
    }
}

@Composable
private fun NCard(notification: TrainerNotificationData, viewModel: TrainerNotificationsViewModel, userId: String) {
    val iconRes = when (notification.type) {
        "join_request" -> Icons.Default.PersonAdd
        "tournament_result" -> Icons.Default.EmojiEvents
        "sponsor_offer" -> Icons.Default.Paid
        else -> Icons.Default.Notifications
    }

    val isActive = notification.status == InviteStatus.PENDING
    val chipColor = if (isActive) DarkTheme.Accent else DarkTheme.TextMuted

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AccentIconBox(iconRes)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(notification.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                        Text("от ${notification.fromName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.width(8.dp))
                val (statusColor, statusText) = when (notification.status) {
                    InviteStatus.PENDING -> DarkTheme.Accent to "Ожидает"
                    InviteStatus.ACCEPTED -> ILeaderColors.Success to "Принято"
                    InviteStatus.DECLINED -> DarkTheme.TextMuted to "Отклонено"
                }
                StatusBadge(statusText, statusColor)
            }

            if (notification.teamName != null) {
                Spacer(Modifier.height(4.dp))
                Text("Команда: ${notification.teamName}", fontSize = 12.sp, color = DarkTheme.TextMuted)
            }

            if (notification.message.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(notification.message, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 18.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text(notification.createdAt, fontSize = 12.sp, color = DarkTheme.TextMuted)

            // Action buttons for pending join requests
            if (notification.status == InviteStatus.PENDING && notification.type == "join_request") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.respondToRequest(notification.id, false, userId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                        )
                    ) { Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { viewModel.respondToRequest(notification.id, true, userId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) { Text("Принять", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            // Action buttons for pending sponsor offers
            if (notification.status == InviteStatus.PENDING && notification.type == "sponsor_offer") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                        )
                    ) { Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) { Text("Рассмотреть", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
