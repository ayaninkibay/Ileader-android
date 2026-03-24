package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ConversationItem
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ChatListViewModel

@Composable
fun ChatListScreen(
    user: User,
    onOpenChat: (conversationId: String, otherUserName: String) -> Unit
) {
    val vm: ChatListViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> ChatListContent(user, s.data, onOpenChat) { vm.load(user.id) }
    }
}

@Composable
private fun ChatListContent(
    user: User,
    conversations: List<ConversationItem>,
    onOpenChat: (conversationId: String, otherUserName: String) -> Unit,
    onRefresh: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            FadeIn(visible, 0) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Сообщения", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            FadeIn(visible, 200) {
                if (conversations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState("Нет диалогов", "Начните переписку с другими пользователями")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(conversations, key = { it.conversationId }) { item ->
                            ConversationCard(
                                item = item,
                                onClick = { onOpenChat(item.conversationId, item.otherUserName) }
                            )
                        }
                        item { Spacer(Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(item: ConversationItem, onClick: () -> Unit) {
    val hasUnread = item.unreadCount > 0

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = DarkTheme.CardBg
    ) {
        Row(
            Modifier
                .border(
                    0.5.dp,
                    if (hasUnread) DarkTheme.Accent.copy(alpha = 0.2f) else DarkTheme.CardBorder.copy(alpha = 0.4f),
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                if (item.otherUserAvatar != null) {
                    AsyncImage(
                        model = item.otherUserAvatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        item.otherUserName.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.Accent
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.otherUserName,
                        fontSize = 15.sp,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                        color = DarkTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.lastMessageTime != null) {
                        Text(
                            formatChatTime(item.lastMessageTime),
                            fontSize = 11.sp,
                            color = if (hasUnread) DarkTheme.Accent else DarkTheme.TextMuted
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.lastMessage ?: "Нет сообщений",
                        fontSize = 13.sp,
                        color = if (hasUnread) DarkTheme.TextPrimary else DarkTheme.TextSecondary,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasUnread) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = DarkTheme.Accent) {
                            Text(
                                "${item.unreadCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatChatTime(isoTime: String): String {
    return try {
        // "2026-03-16T14:30:00" → "14:30" или "16.03"
        val date = isoTime.take(10)
        val time = isoTime.substringAfter("T").take(5)
        val today = java.time.LocalDate.now().toString()
        if (date == today) time else date.substring(8, 10) + "." + date.substring(5, 7)
    } catch (_: Exception) {
        isoTime.take(10)
    }
}
