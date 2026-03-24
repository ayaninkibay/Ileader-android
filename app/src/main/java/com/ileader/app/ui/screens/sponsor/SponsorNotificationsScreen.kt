package com.ileader.app.ui.screens.sponsor

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
import com.ileader.app.data.remote.dto.TournamentInviteDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorNotificationsViewModel

private val SponsorBadge = com.ileader.app.ui.theme.ILeaderColors.SponsorColor

@Composable
fun SponsorNotificationsScreen(user: User) {
    val viewModel: SponsorNotificationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> NotificationsContent(user, s.data.invites, viewModel)
    }
}

@Composable
private fun NotificationsContent(user: User, invites: List<TournamentInviteDto>, viewModel: SponsorNotificationsViewModel) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var acceptDialogInvite by remember { mutableStateOf<TournamentInviteDto?>(null) }
    var declineDialogInvite by remember { mutableStateOf<TournamentInviteDto?>(null) }

    val pendingCount = invites.count { it.status == "pending" }
    val acceptedCount = invites.count { it.status == "accepted" }
    val filters = listOf("Все", "Ожидают", "Отвечено")

    val filteredInvites = when (selectedFilter) {
        1 -> invites.filter { it.status == "pending" }
        2 -> invites.filter { it.status != "pending" }
        else -> invites
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Заявки", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                            if (pendingCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Badge(containerColor = DarkTheme.Accent) {
                                    Text("$pendingCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FadeIn(visible, 200) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotifStatCard(Modifier.weight(1f), "$pendingCount", "Ожидают", Icons.Default.Notifications)
                    NotifStatCard(Modifier.weight(1f), "$acceptedCount", "Принято", Icons.Default.Check)
                    NotifStatCard(Modifier.weight(1f), "${invites.size}", "Всего", Icons.Default.EmojiEvents)
                }
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 300) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEachIndexed { index, filter ->
                        DarkFilterChip(text = filter, selected = selectedFilter == index, onClick = { selectedFilter = index })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 400) {
                if (filteredInvites.isEmpty()) {
                    EmptyState("Нет заявок", "Приглашения от организаторов будут здесь")
                } else {
                    filteredInvites.forEach { invite ->
                        InviteCard(invite = invite,
                            onAccept = { acceptDialogInvite = invite },
                            onDecline = { declineDialogInvite = invite })
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    acceptDialogInvite?.let { invite ->
        AcceptInviteDialog(invite = invite,
            onConfirm = { viewModel.acceptInvite(invite.id, user.id); acceptDialogInvite = null },
            onDismiss = { acceptDialogInvite = null })
    }

    declineDialogInvite?.let { invite ->
        DeclineInviteDialog(invite = invite,
            onConfirm = { reason -> viewModel.declineInvite(invite.id, reason, user.id); declineDialogInvite = null },
            onDismiss = { declineDialogInvite = null })
    }
}

@Composable
private fun NotifStatCard(modifier: Modifier, value: String, label: String, icon: ImageVector) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SoftIconBox(icon, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun InviteCard(invite: TournamentInviteDto, onAccept: () -> Unit, onDecline: () -> Unit) {
    val isPending = invite.status == "pending"

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(
                if (isPending) DarkTheme.Accent.copy(alpha = 0.3f) else DarkTheme.CardBorder.copy(alpha = 0.6f),
                DarkTheme.CardBorder.copy(alpha = 0.2f)
            )), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isPending) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AttachMoney, null,
                        tint = if (isPending) DarkTheme.Accent else DarkTheme.TextMuted, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(invite.tournaments?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                    Text("Приглашение стать спонсором", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(invite.tournaments?.sports?.name ?: "", SponsorBadge)
                Spacer(Modifier.width(12.dp))
                Text(invite.createdAt?.take(10) ?: "", fontSize = 12.sp, color = DarkTheme.TextMuted)
            }

            when (invite.status) {
                "pending" -> {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = SolidColor(DarkTheme.CardBorder))) {
                            Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)) {
                            Text("Принять", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                "accepted" -> {
                    Spacer(Modifier.height(10.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                        Column(Modifier.padding(12.dp)) {
                            if (invite.responseMessage != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, null, tint = DarkTheme.Accent, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(invite.responseMessage, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                                }
                            }
                        }
                    }
                }
                "declined" -> {
                    if (invite.responseMessage != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = DarkTheme.CardBorder.copy(alpha = 0.3f)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(invite.responseMessage, fontSize = 13.sp, color = DarkTheme.TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AcceptInviteDialog(invite: TournamentInviteDto, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(20.dp), containerColor = DarkTheme.Bg,
        title = { Text("Принять приглашение", fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary) },
        text = {
            Column {
                Text("Турнир: ${invite.tournaments?.name ?: ""}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                Spacer(Modifier.height(16.dp))
                Text("Телефон для связи *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(6.dp))
                DarkTextField(phone, { phone = it }, "+7 XXX XXX XX XX")
                Spacer(Modifier.height(12.dp))
                Text("Сообщение организатору", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(6.dp))
                DarkTextField(message, { message = it }, "Необязательно", singleLine = false, minHeight = 80.dp)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = phone.isNotBlank(), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)) {
                Text("Подтвердить", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = DarkTheme.TextSecondary) }
        }
    )
}

@Composable
private fun DeclineInviteDialog(invite: TournamentInviteDto, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(20.dp), containerColor = DarkTheme.Bg,
        title = { Text("Отклонить приглашение", fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary) },
        text = {
            Column {
                Text("Турнир: ${invite.tournaments?.name ?: ""}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                Spacer(Modifier.height(16.dp))
                Text("Причина отказа *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(6.dp))
                DarkTextField(reason, { reason = it }, "Укажите причину", singleLine = false, minHeight = 80.dp)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(reason) }, enabled = reason.isNotBlank(), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)) {
                Text("Отклонить", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = DarkTheme.TextSecondary) }
        }
    )
}

@Composable
private fun DarkTextField(value: String, onValueChange: (String) -> Unit, placeholder: String,
                          singleLine: Boolean = true, minHeight: androidx.compose.ui.unit.Dp = 0.dp) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
        Box(Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)) {
            if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = DarkTheme.TextMuted)
            BasicTextField(value = value, onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                singleLine = singleLine, cursorBrush = SolidColor(DarkTheme.Accent), modifier = Modifier.fillMaxWidth())
        }
    }
}
