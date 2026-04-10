package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.LapTimeItem
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.remote.dto.LapTimeInsertDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.*
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun LapTimesScreen(userId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var laps by remember { mutableStateOf<List<LapTimeItem>>(emptyList()) }
    var activeTournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    fun load() {
        scope.launch {
            loading = true; error = null
            try {
                laps = repo.getLapTimes(userId)
                activeTournaments = try {
                    repo.getMyTournaments(userId).filter {
                        it.status == TournamentStatus.IN_PROGRESS ||
                        it.status == TournamentStatus.CHECK_IN ||
                        it.status == TournamentStatus.REGISTRATION_CLOSED
                    }
                } catch (_: Exception) { emptyList() }
            }
            catch (e: Exception) { error = e.message ?: "Ошибка загрузки" }
            finally { loading = false }
        }
    }
    LaunchedEffect(userId) { load() }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Accent, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Bg)) {
            BackHeader("Лучшие круги", onBack)
            when {
                loading -> LoadingScreen()
                error != null -> ErrorScreen(error!!) { load() }
                laps.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                    EmptyState(title = "Нет записей", subtitle = "Добавьте время круга", icon = Icons.Outlined.Speed)
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(laps) { LapRow(it) }
                }
            }
        }
    }

    if (showDialog) {
        AddLapDialog(
            tournaments = activeTournaments,
            onDismiss = { showDialog = false },
            onSubmit = { time, conditions, equipment, tournament ->
                scope.launch {
                    try {
                        repo.addLapTime(LapTimeInsertDto(
                            athleteId = userId,
                            date = java.time.LocalDate.now().toString(),
                            timeSeconds = time,
                            conditions = conditions.ifBlank { null },
                            equipment = equipment.ifBlank { null },
                            tournamentId = tournament?.id,
                            locationId = tournament?.locationId?.takeIf { it.isNotBlank() }
                        ))
                        showDialog = false
                        snackbar.showSnackbar("Добавлено")
                        load()
                    } catch (e: Exception) {
                        snackbar.showSnackbar(e.message ?: "Ошибка")
                    }
                }
            }
        )
    }
}

private fun formatLapTime(t: Double): String {
    val mins = (t / 60).toInt()
    val secs = t - mins * 60
    return if (mins > 0) String.format("%d:%06.3f", mins, secs) else String.format("%.3f", secs)
}

@Composable
private fun LapRow(l: LapTimeItem) {
    DarkCardPadded {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatLapTime(l.timeSeconds), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (l.isBest) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.15f)) {
                            Text("BEST", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                    }
                }
                val sub = listOfNotNull(
                    l.date.takeIf { it.isNotBlank() },
                    l.conditions,
                    l.equipment
                ).joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(sub, fontSize = 12.sp, color = TextMuted)
                }
            }
            if (l.lapNumber != null) {
                Text("Круг ${l.lapNumber}", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun AddLapDialog(
    tournaments: List<Tournament>,
    onDismiss: () -> Unit,
    onSubmit: (Double, String, String, Tournament?) -> Unit
) {
    var time by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var selectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var tournamentMenuOpen by remember { mutableStateOf(false) }
    val parsed = time.replace(",", ".").toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Новое время круга", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                DarkFormField("Время (сек)", time, { time = it }, placeholder = "65.432",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                Spacer(Modifier.height(8.dp))
                DarkFormField("Условия", conditions, { conditions = it }, placeholder = "Сухо / Дождь")
                Spacer(Modifier.height(8.dp))
                DarkFormField("Оборудование", equipment, { equipment = it }, placeholder = "Шасси / двигатель")

                if (tournaments.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Турнир (опционально)",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { tournamentMenuOpen = true }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedTournament?.name ?: "Без привязки",
                                    fontSize = 14.sp,
                                    color = if (selectedTournament != null) TextPrimary else TextMuted,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                            }
                        }
                        DropdownMenu(
                            expanded = tournamentMenuOpen,
                            onDismissRequest = { tournamentMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Без привязки") },
                                onClick = {
                                    selectedTournament = null
                                    tournamentMenuOpen = false
                                }
                            )
                            tournaments.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.name, fontSize = 14.sp) },
                                    onClick = {
                                        selectedTournament = t
                                        tournamentMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let { onSubmit(it, conditions.trim(), equipment.trim(), selectedTournament) } },
                enabled = parsed != null && parsed > 0
            ) {
                Text("Добавить", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) } }
    )
}
