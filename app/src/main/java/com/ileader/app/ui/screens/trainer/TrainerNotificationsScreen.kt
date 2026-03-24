package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
            var selectedFilter by remember { mutableIntStateOf(0) }
            val filters = listOf("Все", "Ожидают", "Отвечено")

            val filteredNotifications = when (selectedFilter) {
                1 -> notifications.filter { it.status == InviteStatus.PENDING }
                2 -> notifications.filter { it.status != InviteStatus.PENDING }
                else -> notifications
            }

            val pendingCount = notifications.count { it.status == InviteStatus.PENDING }
            val acceptedCount = notifications.count { it.status == InviteStatus.ACCEPTED }

            Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── BACK + TITLE ──
                    BackHeader("Уведомления", onBack) {
                        if (pendingCount > 0) {
                            Badge(containerColor = DarkTheme.Accent) {
                                Text("$pendingCount")
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── STATS ──
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        MiniStat("Ожидают", pendingCount.toString(), Modifier.weight(1f))
                        MiniStat("Принято", acceptedCount.toString(), Modifier.weight(1f))
                        MiniStat("Всего", notifications.size.toString(), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── FILTERS ──
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filters.forEachIndexed { index, filter ->
                            DarkFilterChip(filter, selectedFilter == index, { selectedFilter = index })
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── NOTIFICATION LIST ──
                    if (filteredNotifications.isEmpty()) {
                        EmptyState("Нет уведомлений")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredNotifications.forEach { notification ->
                                NCard(notification, viewModel, user.id)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
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

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(DarkTheme.AccentSoft),
                    Alignment.Center
                ) {
                    Icon(iconRes, null, Modifier.size(22.dp), DarkTheme.Accent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(notification.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                    Text("от ${notification.fromName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
                val (statusColor, statusText) = when (notification.status) {
                    InviteStatus.PENDING -> DarkTheme.Accent to "Ожидает"
                    InviteStatus.ACCEPTED -> ILeaderColors.Success to "Принято"
                    InviteStatus.DECLINED -> DarkTheme.Accent to "Отклонено"
                }
                StatusBadge(statusText, statusColor)
            }

            if (notification.teamName != null) {
                Spacer(Modifier.height(4.dp))
                Text("Команда: ${notification.teamName}", fontSize = 12.sp, color = DarkTheme.TextMuted)
            }

            Spacer(Modifier.height(8.dp))
            Text(notification.message, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)

            Spacer(Modifier.height(8.dp))
            Text(notification.createdAt, fontSize = 12.sp, color = DarkTheme.TextMuted)

            // Action buttons for pending join requests
            if (notification.status == InviteStatus.PENDING && notification.type == "join_request") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.respondToRequest(notification.id, false, userId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.Accent),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.Accent.copy(alpha = 0.3f), DarkTheme.Accent.copy(alpha = 0.3f)))
                        )
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { viewModel.respondToRequest(notification.id, true, userId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Принять", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Action buttons for pending sponsor offers
            if (notification.status == InviteStatus.PENDING && notification.type == "sponsor_offer") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                        border = DarkTheme.cardBorderStroke
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
