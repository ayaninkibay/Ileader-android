package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MediaInviteFullDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.MediaNotificationsData
import com.ileader.app.ui.viewmodels.MediaNotificationsViewModel
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Status badge colors (ONLY for text badges, NOT for buttons/icons)
private val StatusSuccess get() = ILeaderColors.Success
private val StatusWarning get() = ILeaderColors.Warning

@Composable
fun MediaNotificationsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: MediaNotificationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> NotificationsContent(user, s.data, viewModel)
    }
}

@Composable
private fun NotificationsContent(
    user: User,
    data: MediaNotificationsData,
    viewModel: MediaNotificationsViewModel
) {
    var selectedFilter by remember { mutableIntStateOf(0) }

    var showAcceptDialog by remember { mutableStateOf<MediaInviteFullDto?>(null) }
    var showDeclineDialog by remember { mutableStateOf<MediaInviteFullDto?>(null) }
    var phoneInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var declineReason by remember { mutableStateOf("") }

    val invites = data.invites
    val pendingCount = invites.count { it.status == "pending" }
    val acceptedCount = invites.count { it.status == "accepted" }
    val filters = listOf("Все", "Ожидают", "Отвечено")

    val filteredInvites = invites.filter { invite ->
        when (selectedFilter) {
            1 -> invite.status == "pending"
            2 -> invite.status != "pending"
            else -> true
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible, 0) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Уведомления", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    if (pendingCount > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.Accent) {
                            Text("$pendingCount", fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS ──
            FadeIn(visible, 150) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotifStatCard(Modifier.weight(1f), Icons.Default.Schedule,
                        "Ожидают", "$pendingCount")
                    NotifStatCard(Modifier.weight(1f), Icons.Default.CheckCircle,
                        "Принято", "$acceptedCount")
                    NotifStatCard(Modifier.weight(1f), Icons.Default.Notifications,
                        "Всего", "${invites.size}")
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── FILTERS ──
            FadeIn(visible, 300) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEachIndexed { index, filter ->
                        DarkFilterChip(
                            text = filter,
                            selected = selectedFilter == index,
                            onClick = { selectedFilter = index }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── LIST ──
            FadeIn(visible, 450) {
                if (filteredInvites.isEmpty()) {
                    EmptyState("Нет уведомлений", "Приглашения появятся здесь")
                } else {
                    filteredInvites.forEach { invite ->
                        InviteCard(
                            invite = invite,
                            onAccept = {
                                phoneInput = ""
                                messageInput = ""
                                showAcceptDialog = invite
                            },
                            onDecline = {
                                declineReason = ""
                                showDeclineDialog = invite
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Accept Dialog
    showAcceptDialog?.let { invite ->
        AlertDialog(
            onDismissRequest = { showAcceptDialog = null },
            containerColor = DarkTheme.CardBg,
            titleContentColor = DarkTheme.TextPrimary,
            textContentColor = DarkTheme.TextSecondary,
            title = {
                Column {
                    Text("Принять приглашение", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = DarkTheme.TextPrimary)
                    Text(invite.tournaments?.name ?: "", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                        BasicTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            modifier = Modifier.fillMaxWidth()
                                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                            singleLine = true,
                            cursorBrush = SolidColor(DarkTheme.Accent),
                            decorationBox = { innerTextField ->
                                if (phoneInput.isEmpty()) {
                                    Text("Телефон для связи *", fontSize = 14.sp, color = DarkTheme.TextMuted)
                                }
                                innerTextField()
                            }
                        )
                    }
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                        BasicTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            modifier = Modifier.fillMaxWidth()
                                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                            cursorBrush = SolidColor(DarkTheme.Accent),
                            decorationBox = { innerTextField ->
                                if (messageInput.isEmpty()) {
                                    Text("Сообщение организатору (необязательно)",
                                        fontSize = 14.sp, color = DarkTheme.TextMuted)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.acceptInvite(
                            invite.id,
                            phoneInput,
                            messageInput.ifEmpty { null }
                        )
                        showAcceptDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = phoneInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Подтвердить", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = null }) {
                    Text("Отмена", color = DarkTheme.TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Decline Dialog
    showDeclineDialog?.let { invite ->
        AlertDialog(
            onDismissRequest = { showDeclineDialog = null },
            containerColor = DarkTheme.CardBg,
            titleContentColor = DarkTheme.TextPrimary,
            textContentColor = DarkTheme.TextSecondary,
            title = {
                Column {
                    Text("Отклонить приглашение", fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = DarkTheme.TextPrimary)
                    Text(invite.tournaments?.name ?: "", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
            },
            text = {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                    BasicTextField(
                        value = declineReason,
                        onValueChange = { declineReason = it },
                        modifier = Modifier.fillMaxWidth()
                            .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                        cursorBrush = SolidColor(DarkTheme.Accent),
                        decorationBox = { innerTextField ->
                            if (declineReason.isEmpty()) {
                                Text("Причина отказа *", fontSize = 14.sp, color = DarkTheme.TextMuted)
                            }
                            innerTextField()
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.declineInvite(invite.id, declineReason)
                        showDeclineDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = declineReason.isNotBlank()
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Отклонить", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = null }) {
                    Text("Отмена", color = DarkTheme.TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun NotifStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconBox(icon, size = 36.dp, iconSize = 18.dp)
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                Text(label, fontSize = 10.sp, color = DarkTheme.TextMuted)
            }
        }
    }
}

@Composable
private fun InviteCard(
    invite: MediaInviteFullDto,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isPending = invite.status == "pending"
    val tournamentName = invite.tournaments?.name ?: ""
    val sportName = invite.tournaments?.sports?.name ?: ""
    val locationCity = invite.tournaments?.locations?.city ?: ""
    val createdAt = formatDate(invite.createdAt)

    // Parse contact phone from comments jsonb
    val contactPhone = invite.comments?.let { commentsEl ->
        try {
            commentsEl.jsonObject["contact_phone"]?.jsonPrimitive?.content
        } catch (_: Exception) { null }
    }

    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = DarkTheme.CardBg, border = DarkTheme.cardBorderStroke
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPending) DarkTheme.AccentSoft
                                else DarkTheme.CardBorder.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, Modifier.size(22.dp),
                            if (isPending) DarkTheme.Accent else DarkTheme.TextMuted)
                    }
                    Column {
                        Text(tournamentName, fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp, color = DarkTheme.TextPrimary)
                        Text(sportName, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }

                val statusConfig = when (invite.status) {
                    "pending" -> "Ожидает" to StatusWarning
                    "accepted" -> "Принято" to StatusSuccess
                    "declined" -> "Отклонено" to DarkTheme.Accent
                    else -> (invite.status ?: "") to DarkTheme.TextMuted
                }
                StatusBadge(statusConfig.first, statusConfig.second)
            }

            // Message
            invite.message?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, fontSize = 13.sp, color = DarkTheme.TextSecondary)
            }

            // Meta
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                    Text(createdAt, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
                if (locationCity.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                        Text(locationCity, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }
            }

            // Actions for pending
            if (isPending) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Принять", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отклонить", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Accepted response
            if (invite.status == "accepted" &&
                (contactPhone != null || invite.responseMessage != null)
            ) {
                Spacer(Modifier.height(10.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    color = DarkTheme.AccentSoft) {
                    Column(Modifier.padding(12.dp)) {
                        contactPhone?.let { phone ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Phone, null, Modifier.size(14.dp), DarkTheme.Accent)
                                Text("Телефон: $phone", fontSize = 13.sp, color = DarkTheme.Accent)
                            }
                        }
                        invite.responseMessage?.let { msg ->
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Sms, null, Modifier.size(14.dp), DarkTheme.Accent)
                                Text("Сообщение: $msg", fontSize = 13.sp, color = DarkTheme.Accent)
                            }
                        }
                    }
                }
            }

            // Declined response
            if (invite.status == "declined" && invite.responseMessage != null) {
                Spacer(Modifier.height(10.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    color = DarkTheme.Accent.copy(alpha = 0.08f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Sms, null, Modifier.size(14.dp), DarkTheme.Accent)
                        Text("Причина: ${invite.responseMessage}",
                            fontSize = 13.sp, color = DarkTheme.Accent)
                    }
                }
            }
        }
    }
}
