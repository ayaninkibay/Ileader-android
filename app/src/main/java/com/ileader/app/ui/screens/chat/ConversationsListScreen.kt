package com.ileader.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Text
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
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ConversationDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ConversationsListViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun ConversationsListScreen(
    myUserId: String,
    onBack: () -> Unit,
    onOpenConversation: (conversationId: String, otherName: String) -> Unit
) {
    val vm: ConversationsListViewModel = viewModel()
    val state by vm.state.collectAsState()
    LaunchedEffect(myUserId) { vm.load(myUserId) }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        BackHeader("Сообщения", onBack)
        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { vm.load(myUserId) }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ChatBubbleOutline,
                        title = "Нет диалогов",
                        subtitle = "Начните общение со спортсменом или тренером"
                    )
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        s.data.forEach { conv ->
                            ConversationRow(
                                conversation = conv,
                                myUserId = myUserId,
                                onClick = { id, name ->
                                    onOpenConversation(id, name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: ConversationDto,
    myUserId: String,
    onClick: (id: String, otherName: String) -> Unit
) {
    val other = conversation.participants
        ?.firstOrNull { it.userId != myUserId }
        ?.profiles
    val name = other?.name ?: "Диалог"
    val avatarUrl = other?.avatarUrl
    val preview = conversation.messages?.lastOrNull()?.content ?: "Нет сообщений"

    DarkCard(
        modifier = Modifier.clickable { onClick(conversation.id, name) }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(Accent.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    preview,
                    fontSize = 13.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
