package com.ileader.app.ui.screens.helper

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.common.ManualCheckInScreen
import com.ileader.app.ui.screens.common.QrScannerScreen
import com.ileader.app.ui.viewmodels.HelperViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun HelperDashboardScreen(user: User) {
    val vm: HelperViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Navigation: null = dashboard, "scan/{id}/{name}" = QR, "manual/{id}/{name}" = manual search
    var subScreen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) { vm.loadAssignments(user.id) }

    when {
        subScreen?.startsWith("scan/") == true -> {
            val parts = subScreen!!.removePrefix("scan/").split("/", limit = 2)
            QrScannerScreen(
                tournamentId = parts[0],
                tournamentName = parts.getOrElse(1) { "Турнир" },
                onBack = { subScreen = null }
            )
            return
        }
        subScreen?.startsWith("manual/") == true -> {
            val parts = subScreen!!.removePrefix("manual/").split("/", limit = 2)
            ManualCheckInScreen(
                tournamentId = parts[0],
                tournamentName = parts.getOrElse(1) { "Турнир" },
                onBack = { subScreen = null }
            )
            return
        }
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

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(icon = Icons.Default.QrCodeScanner)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Помощник", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Check-in участников", fontSize = 13.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is UiState.Loading -> {
                LoadingScreen()
            }
            is UiState.Error -> {
                ErrorScreen(s.message) { vm.loadAssignments(user.id) }
            }
            is UiState.Success -> {
                val assignments = s.data
                if (assignments.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Нет назначений",
                        subtitle = "Вас пока не назначили помощником ни на один турнир"
                    )
                } else {
                    Text(
                        "Ваши турниры (${assignments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    assignments.forEach { assignment ->
                        val tName = assignment.tournaments?.name ?: "Турнир"
                        HelperTournamentCard(
                            assignment = assignment,
                            onScanQr = { subScreen = "scan/${assignment.tournamentId}/$tName" },
                            onManualCheckIn = { subScreen = "manual/${assignment.tournamentId}/$tName" }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun HelperTournamentCard(
    assignment: TournamentHelperDto,
    onScanQr: () -> Unit,
    onManualCheckIn: () -> Unit
) {
    val tournament = assignment.tournaments

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            // Tournament name
            Text(
                tournament?.name ?: "Турнир",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(6.dp))

            // Sport and status
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tournament?.sports != null) {
                    Text(
                        sportEmoji(tournament.sports.name) + " " + tournament.sports.name,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.width(12.dp))
                }
                val statusText = when (tournament?.status) {
                    "check_in" -> "Check-in"
                    "in_progress" -> "В процессе"
                    "registration_open" -> "Регистрация"
                    "completed" -> "Завершён"
                    else -> tournament?.status ?: ""
                }
                if (statusText.isNotEmpty()) {
                    StatusBadge(statusText, when (tournament?.status) {
                        "check_in" -> DarkTheme.Accent
                        "in_progress" -> Color(0xFF22C55E)
                        "completed" -> TextMuted
                        else -> TextSecondary
                    })
                }
            }

            // Date
            if (tournament?.startDate != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = TextMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(tournament.startDate.take(10), fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onScanQr,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("QR", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onManualCheckIn,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(18.dp), tint = TextPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Найти", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }
    }
}
