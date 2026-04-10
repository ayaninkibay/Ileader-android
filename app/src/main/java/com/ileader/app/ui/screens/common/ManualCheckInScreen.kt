package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.repository.CheckInRepository
import com.ileader.app.data.repository.CheckInRepository.AttendeeInfo
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.CheckInAccessState
import com.ileader.app.ui.viewmodels.CheckInViewModel
import kotlinx.coroutines.launch

private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun ManualCheckInScreen(
    tournamentId: String,
    tournamentName: String,
    userId: String,
    onBack: () -> Unit
) {
    val repo = remember { CheckInRepository() }
    val scope = rememberCoroutineScope()
    val accessVm: CheckInViewModel = viewModel()
    val accessState by accessVm.accessState.collectAsState()

    LaunchedEffect(userId, tournamentId) {
        accessVm.verifyAccess(userId, tournamentId)
    }

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<AttendeeInfo>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var checkInMessage by remember { mutableStateOf<String?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    // Load all attendees on start — only after access is granted
    LaunchedEffect(tournamentId, accessState) {
        if (accessState is CheckInAccessState.Allowed && !hasSearched) {
            isSearching = true
            try {
                results = repo.searchAttendees(tournamentId, "")
            } catch (_: Exception) { }
            isSearching = false
            hasSearched = true
        }
    }

    // ── Access gate ──
    when (val access = accessState) {
        is CheckInAccessState.Checking -> {
            Column(
                Modifier.fillMaxSize().background(DarkTheme.Bg).statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BackHeader(tournamentName, onBack)
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(16.dp))
                Text("Проверка доступа…", fontSize = 14.sp, color = TextMuted)
            }
            return
        }
        is CheckInAccessState.Denied -> {
            Column(
                Modifier.fillMaxSize().background(DarkTheme.Bg).statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BackHeader(tournamentName, onBack)
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.ErrorOutline, null,
                    tint = Accent, modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Доступ запрещён", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    access.reason,
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Назад") }
                Spacer(Modifier.weight(1f))
            }
            return
        }
        is CheckInAccessState.Allowed -> Unit
    }

    fun doSearch() {
        scope.launch {
            isSearching = true
            hasSearched = true
            try {
                results = repo.searchAttendees(tournamentId, query.trim())
            } catch (_: Exception) {
                results = emptyList()
            }
            isSearching = false
        }
    }

    fun doCheckIn(attendee: AttendeeInfo) {
        scope.launch {
            try {
                when (attendee.type) {
                    "athlete" -> repo.markParticipantCheckIn(tournamentId, attendee.userId)
                    "referee" -> repo.markRefereeCheckIn(tournamentId, attendee.userId)
                    else -> repo.markSpectatorCheckIn(tournamentId, attendee.userId)
                }
                checkInMessage = "${attendee.name} отмечен(а)"
                // Refresh
                results = repo.searchAttendees(tournamentId, query.trim())
            } catch (e: Exception) {
                checkInMessage = "Ошибка: ${e.message}"
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(DarkTheme.Bg)
            .statusBarsPadding()
    ) {
        BackHeader(tournamentName, onBack)

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Search field
            DarkSearchField(
                value = query,
                onValueChange = {
                    query = it
                    doSearch()
                },
                placeholder = "Поиск по имени..."
            )

            Spacer(Modifier.height(8.dp))

            // Snackbar message
            checkInMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (msg.startsWith("Ошибка")) Color(0xFFEF4444).copy(alpha = 0.1f)
                            else Color(0xFF22C55E).copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (msg.startsWith("Ошибка")) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            null,
                            tint = if (msg.startsWith("Ошибка")) Color(0xFFEF4444) else Color(0xFF22C55E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(msg, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { checkInMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Stats
            val checked = results.count { it.checkInStatus == "checked_in" }
            Text(
                "Найдено: ${results.size} • Отмечено: $checked",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(8.dp))

            if (isSearching) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                }
            } else if (results.isEmpty() && hasSearched) {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "Никого не нашли",
                    subtitle = if (query.isNotEmpty()) "Попробуйте другой запрос" else "Нет зарегистрированных участников"
                )
            } else {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    results.forEach { attendee ->
                        AttendeeCard(attendee, onCheckIn = { doCheckIn(attendee) })
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(attendee: AttendeeInfo, onCheckIn: () -> Unit) {
    val isCheckedIn = attendee.checkInStatus == "checked_in"
    val typeBadge = when (attendee.type) {
        "referee" -> "Судья"
        "spectator" -> attendee.extra ?: "Зритель"
        else -> "Участник"
    }
    val badgeColor = when (attendee.type) {
        "referee" -> Color(0xFFF59E0B)
        "spectator" -> Color(0xFF3B82F6)
        else -> DarkTheme.Accent
    }

    DarkCard {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = badgeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            typeBadge,
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    if (attendee.extra != null && attendee.type == "athlete") {
                        Spacer(Modifier.width(6.dp))
                        Text(attendee.extra, fontSize = 11.sp, color = TextMuted)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(attendee.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (attendee.city != null) {
                    Text(attendee.city, fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Check-in button or status
            if (isCheckedIn) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отмечен", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
                    }
                }
            } else {
                Button(
                    onClick = onCheckIn,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Отметить", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
