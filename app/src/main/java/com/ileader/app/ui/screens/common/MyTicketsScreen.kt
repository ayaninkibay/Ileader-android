package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TicketItem
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TicketsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun MyTicketsScreen(user: User) {
    val vm: TicketsViewModel = viewModel()
    val state by vm.state.collectAsState()

    // QR ticket sub-screen
    var showQrTicket by remember { mutableStateOf<TicketItem?>(null) }

    LaunchedEffect(user.id) { vm.loadTickets(user.id) }

    showQrTicket?.let { ticket ->
        QrTicketScreen(
            userName = user.displayName,
            userId = user.id,
            tournamentId = ticket.tournamentId,
            tournamentName = ticket.tournamentName,
            type = ticket.type,
            isCheckedIn = ticket.checkInStatus == "checked_in",
            onBack = { showQrTicket = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(icon = Icons.Default.ConfirmationNumber)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Мои билеты", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("QR-коды для входа на турниры", fontSize = 13.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { vm.loadTickets(user.id) }
            is UiState.Success -> {
                val tickets = s.data
                if (tickets.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ConfirmationNumber,
                        title = "Нет билетов",
                        subtitle = "Зарегистрируйтесь на турнир как участник или зритель"
                    )
                } else {
                    tickets.forEach { ticket ->
                        TicketCard(ticket = ticket, onShowQr = { showQrTicket = ticket })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun TicketCard(ticket: TicketItem, onShowQr: () -> Unit) {
    val isCheckedIn = ticket.checkInStatus == "checked_in"
    val typeBadge = when (ticket.type) {
        "referee" -> "Судья"
        "spectator" -> "Зритель"
        else -> "Участник"
    }
    val typeBadgeColor = when (ticket.type) {
        "referee" -> Color(0xFFF59E0B) // orange
        "spectator" -> Color(0xFF3B82F6) // blue
        else -> Accent // red
    }

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeBadgeColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        typeBadge,
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeBadgeColor
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Tournament status
                val statusText = when (ticket.status) {
                    "check_in" -> "Check-in"
                    "in_progress" -> "В процессе"
                    "registration_open" -> "Регистрация"
                    "registration_closed" -> "Рег. закрыта"
                    else -> ""
                }
                if (statusText.isNotEmpty()) {
                    StatusBadge(statusText, when (ticket.status) {
                        "check_in" -> Accent
                        "in_progress" -> Color(0xFF22C55E)
                        else -> TextMuted
                    })
                }

                Spacer(Modifier.weight(1f))

                if (isCheckedIn) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                ticket.tournamentName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ticket.sportName != null) {
                    Text(
                        sportEmoji(ticket.sportName) + " " + ticket.sportName,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.width(12.dp))
                }
                if (ticket.startDate != null) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = TextMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(ticket.startDate.take(10), fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            if (isCheckedIn) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Вы отмечены на входе", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
                }
            } else {
                Button(
                    onClick = onShowQr,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Показать QR-код", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
