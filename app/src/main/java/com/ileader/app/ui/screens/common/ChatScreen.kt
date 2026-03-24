package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MessageDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    user: User,
    conversationId: String,
    otherUserName: String,
    onBack: () -> Unit
) {
    val vm: ChatViewModel = viewModel()
    val messagesState by vm.messages.collectAsState()
    val sendingMessage by vm.sendingMessage.collectAsState()

    LaunchedEffect(conversationId) { vm.load(conversationId, user.id) }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Top bar
            ChatTopBar(otherUserName, onBack)

            // Messages
            when (val s = messagesState) {
                is UiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LoadingScreen()
                }
                is UiState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ErrorScreen(s.message) { vm.load(conversationId, user.id) }
                }
                is UiState.Success -> MessagesList(
                    messages = s.data,
                    currentUserId = user.id,
                    modifier = Modifier.weight(1f)
                )
            }

            // Input bar
            MessageInput(
                sending = sendingMessage,
                onSend = { vm.sendMessage(it) }
            )
        }
    }
}

@Composable
private fun ChatTopBar(name: String, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkTheme.CardBg,
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.Accent
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextPrimary
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<MessageDto>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Скролл к последнему сообщению при обновлении
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    if (messages.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            EmptyState("Нет сообщений", "Напишите первое сообщение")
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isOwn = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageDto, isOwn: Boolean) {
    val bubbleColor = if (isOwn) DarkTheme.Accent else DarkTheme.CardBg
    val textColor = if (isOwn) Color.White else DarkTheme.TextPrimary
    val timeColor = if (isOwn) Color.White.copy(alpha = 0.7f) else DarkTheme.TextMuted
    val alignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isOwn)
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(
                    message.content,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    message.createdAt?.let { it.substringAfter("T").take(5) } ?: "",
                    fontSize = 10.sp,
                    color = timeColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun MessageInput(sending: Boolean, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkTheme.CardBg,
        shadowElevation = 4.dp
    ) {
        Row(
            Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение...", color = DarkTheme.TextMuted, fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkTheme.Accent,
                    unfocusedBorderColor = DarkTheme.CardBorder.copy(alpha = 0.5f),
                    focusedTextColor = DarkTheme.TextPrimary,
                    unfocusedTextColor = DarkTheme.TextPrimary,
                    cursorColor = DarkTheme.Accent
                ),
                maxLines = 4,
                singleLine = false
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank() && !sending) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank() && !sending
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = DarkTheme.Accent
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        "Отправить",
                        tint = if (text.isNotBlank()) DarkTheme.Accent else DarkTheme.TextMuted
                    )
                }
            }
        }
    }
}
