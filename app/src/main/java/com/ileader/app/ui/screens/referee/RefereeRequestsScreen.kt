package com.ileader.app.ui.screens.referee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.RefereeRequestsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeRequestsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: RefereeRequestsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> RequestsContent(s.data, viewModel)
    }
}

@Composable
private fun RequestsContent(
    data: com.ileader.app.ui.viewmodels.RefereeRequestsData,
    viewModel: RefereeRequestsViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) }

    val incomingInvites = data.incoming
    val outgoingApps = data.outgoing

    val pendingIncoming = incomingInvites.count { it.status == InviteStatus.PENDING }
    val pendingOutgoing = outgoingApps.count { it.status == InviteStatus.PENDING }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = Accent
    Box(Modifier.fillMaxSize().background(Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.03f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.03f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Text("Заявки", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text("Входящие приглашения и ваши заявки", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // ── TABS ──
            FadeIn(visible, 200) {
                val tabs = listOf(
                    "Входящие" to pendingIncoming,
                    "Исходящие" to pendingOutgoing
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEachIndexed { index, (title, count) ->
                        val label = if (count > 0) "$title ($count)" else title
                        DarkFilterChip(label, activeTab == index, { activeTab = index })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 400) {
                if (activeTab == 0) {
                    IncomingContent(incomingInvites,
                        onAccept = { invite -> viewModel.respond(invite.id, true) },
                        onDecline = { invite -> viewModel.respond(invite.id, false) }
                    )
                } else {
                    OutgoingContent(outgoingApps)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IncomingContent(
    invites: List<RefereeInvite>,
    onAccept: (RefereeInvite) -> Unit,
    onDecline: (RefereeInvite) -> Unit
) {
    if (invites.isEmpty()) {
        EmptyState("Нет входящих приглашений", "Когда организатор пригласит вас — заявка появится здесь")
        return
    }
    val pending = invites.filter { it.status == InviteStatus.PENDING }
    val resolved = invites.filter { it.status != InviteStatus.PENDING }

    if (pending.isNotEmpty()) {
        Text("ОЖИДАЮТ ОТВЕТА (${pending.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        pending.forEach { invite ->
            RequestCard(invite) {
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAccept(invite) },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Принять", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { onDecline(invite) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отклонить", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (resolved.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text("РЕШЁННЫЕ (${resolved.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = TextMuted, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        resolved.forEach { invite ->
            RequestCard(invite)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun OutgoingContent(
    applications: List<RefereeInvite>
) {
    if (applications.isEmpty()) {
        EmptyState("Нет исходящих заявок", "Подайте заявку на судейство в разделе Турниры")
        return
    }
    val pending = applications.filter { it.status == InviteStatus.PENDING }
    val resolved = applications.filter { it.status != InviteStatus.PENDING }

    if (pending.isNotEmpty()) {
        Text("НА РАССМОТРЕНИИ (${pending.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        pending.forEach { app ->
            RequestCard(app)
            Spacer(Modifier.height(10.dp))
        }
    }

    if (resolved.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text("РЕШЁННЫЕ (${resolved.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = TextMuted, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        resolved.forEach { app ->
            RequestCard(app)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RequestCard(invite: RefereeInvite, actions: @Composable (() -> Unit)? = null) {
    val statusColor = when (invite.status) {
        InviteStatus.PENDING -> Accent
        InviteStatus.ACCEPTED -> ILeaderColors.Success
        InviteStatus.DECLINED -> TextMuted
    }

    DarkCardPadded(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(invite.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (invite.sportName.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(invite.sportName, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(invite.role.label, Accent)
            StatusBadge(invite.status.displayName, statusColor)
        }

        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), TextMuted)
            Spacer(Modifier.width(4.dp))
            Text(invite.createdAt, fontSize = 12.sp, color = TextSecondary)
        }

        // Response info
        if (invite.status != InviteStatus.PENDING &&
            (invite.responseMessage != null || invite.contactPhone != null)
        ) {
            Spacer(Modifier.height(10.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                color = CardBorder.copy(alpha = 0.3f)) {
                Column(Modifier.padding(12.dp)) {
                    invite.contactPhone?.let { phone ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, Modifier.size(14.dp), TextMuted)
                            Spacer(Modifier.width(6.dp))
                            Text("Телефон: $phone", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    invite.responseMessage?.let { msg ->
                        val label = if (invite.status == InviteStatus.ACCEPTED) "Комментарий" else "Причина"
                        Text("$label: $msg", fontSize = 12.sp, color = TextSecondary,
                            modifier = Modifier.padding(top = if (invite.contactPhone != null) 4.dp else 0.dp))
                    }
                }
            }
        }

        actions?.invoke()
    }
}
