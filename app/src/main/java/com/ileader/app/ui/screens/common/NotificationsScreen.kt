package com.ileader.app.ui.screens.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.NotificationDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.NotificationsViewModel

@Composable
fun NotificationsScreen(user: User, onBack: (() -> Unit)? = null) {
    val vm: NotificationsViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> NotificationsContent(user, s.data, vm, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsContent(user: User, notifications: List<NotificationDto>, vm: NotificationsViewModel, onBack: (() -> Unit)? = null) {
    val showOnlyUnread by vm.showOnlyUnread.collectAsState()
    val unreadCount = notifications.count { !it.read }
    val filtered = if (showOnlyUnread) notifications.filter { !it.read } else notifications

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Back button if navigated from ProfileTab
            if (onBack != null) {
                BackHeader("Уведомления", onBack)
            }

            // Header
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(if (onBack != null) 8.dp else 16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBack == null) {
                                Text(
                                    "Уведомления", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp
                                )
                            }
                            if (unreadCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Badge(containerColor = DarkTheme.Accent) {
                                    Text("$unreadCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }

                    if (unreadCount > 0) {
                        TextButton(onClick = { vm.markAllAsRead(user.id) }) {
                            Text("Прочитать все", fontSize = 13.sp, color = DarkTheme.Accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showOnlyUnread,
                        onClick = { if (showOnlyUnread) vm.toggleFilter() },
                        label = { Text("Все", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkTheme.Accent,
                            selectedLabelColor = Color.White,
                            containerColor = DarkTheme.CardBg,
                            labelColor = DarkTheme.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DarkTheme.CardBorder.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = !showOnlyUnread
                        )
                    )
                    FilterChip(
                        selected = showOnlyUnread,
                        onClick = { if (!showOnlyUnread) vm.toggleFilter() },
                        label = { Text("Непрочитанные", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkTheme.Accent,
                            selectedLabelColor = Color.White,
                            containerColor = DarkTheme.CardBg,
                            labelColor = DarkTheme.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DarkTheme.CardBorder.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = showOnlyUnread
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
            

            // Notification list with swipe-to-dismiss
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        if (showOnlyUnread) "Нет непрочитанных" else "Нет уведомлений",
                        "Новые уведомления появятся здесь"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filtered,
                        key = { it.id }
                    ) { notification ->
                        SwipeToDismissNotification(
                            notification = notification,
                            onDismiss = { vm.deleteNotification(notification.id, user.id) },
                            onClick = {
                                if (!notification.read) {
                                    vm.markAsRead(notification.id, user.id)
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
            
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissNotification(
    notification: NotificationDto,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    Color(0xFFE53535).copy(alpha = 0.9f)
                else Color.Transparent,
                label = "swipeBg"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Удалить", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        NotificationCard(notification = notification, onClick = onClick)
    }
}

@Composable
private fun NotificationCard(notification: NotificationDto, onClick: () -> Unit) {
    val isUnread = !notification.read
    val icon = when (notification.type) {
        "tournament_registration", "tournament_update" -> Icons.Default.EmojiEvents
        "tournament_result" -> Icons.Default.Leaderboard
        "team_invite", "team_update" -> Icons.Default.Groups
        "payment" -> Icons.Default.Payment
        "parental_request", "parental_response" -> Icons.Default.FamilyRestroom
        "league_update" -> Icons.Default.MilitaryTech
        "verification" -> Icons.Default.VerifiedUser
        "system" -> Icons.Default.Info
        else -> Icons.Default.Notifications
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = DarkTheme.CardBg,
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier
                .border(
                    0.5.dp,
                    if (isUnread) DarkTheme.Accent.copy(alpha = 0.3f) else DarkTheme.CardBorder.copy(alpha = 0.5f),
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isUnread) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = if (isUnread) DarkTheme.Accent else DarkTheme.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    notification.title ?: "Уведомление",
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = DarkTheme.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!notification.message.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        notification.message,
                        fontSize = 12.sp,
                        color = DarkTheme.TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    notification.createdAt?.take(10) ?: "",
                    fontSize = 11.sp,
                    color = DarkTheme.TextMuted
                )
            }
            if (isUnread) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                        .background(DarkTheme.Accent)
                )
            }
        }
    }
}
