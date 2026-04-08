package com.ileader.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MessageDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ChatViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = DarkTheme.CardBorder

@Composable
fun ChatScreen(
    conversationId: String,
    myUserId: String,
    title: String,
    onBack: () -> Unit
) {
    val vm: ChatViewModel = viewModel()
    val state by vm.state.collectAsState()
    val sending by vm.sending.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) { vm.load(conversationId, myUserId) }

    LaunchedEffect(state) {
        val size = (state as? UiState.Success)?.data?.size ?: 0
        if (size > 0) listState.animateScrollToItem(size - 1)
    }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding().imePadding()
    ) {
        BackHeader(title, onBack)

        Box(Modifier.weight(1f)) {
            when (val s = state) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(s.message) { vm.load(conversationId, myUserId) }
                is UiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(s.data, key = { it.id }) { msg ->
                            MessageBubble(msg, isMine = msg.senderId == myUserId)
                        }
                    }
                }
            }
        }

        // Input
        Surface(
            color = CardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(0.5.dp, Border, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = Bg
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(Accent),
                        decorationBox = { inner ->
                            Box {
                                if (input.isEmpty()) {
                                    Text("Сообщение...", color = TextMuted, fontSize = 14.sp)
                                }
                                inner()
                            }
                        }
                    )
                }
                Spacer(Modifier.width(8.dp))
                val enabled = input.isNotBlank() && !sending
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (enabled) Accent else Accent.copy(0.3f),
                            RoundedCornerShape(22.dp)
                        )
                        .then(
                            if (enabled) Modifier.clickable {
                                vm.send(input); input = ""
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageDto, isMine: Boolean) {
    val bubbleColor = if (isMine) Accent else CardBg
    val textColor = if (isMine) Color.White else TextPrimary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                msg.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 14.sp,
                color = textColor,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
