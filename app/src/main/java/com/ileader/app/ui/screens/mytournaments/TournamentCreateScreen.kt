package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkSwitchField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun TournamentCreateScreen(
    userId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit = {}
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isCreating by remember { mutableStateOf(false) }

    // Reference data
    var sports by remember { mutableStateOf<List<SportDto>>(emptyList()) }
    var locations by remember { mutableStateOf<List<LocationDto>>(emptyList()) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSportId by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("single_elimination") }
    var matchFormat by remember { mutableStateOf("best_of_1") }
    var visibility by remember { mutableStateOf("public") }
    var hasCheckIn by remember { mutableStateOf(false) }
    var prize by remember { mutableStateOf("") }

    // Load sports and locations
    LaunchedEffect(Unit) {
        try {
            sports = repo.getSports()
            locations = repo.getMyLocations(userId)
        } catch (_: Exception) {}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
        ) {
            BackHeader("Создание турнира", onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // ── Name ──
                DarkFormField(
                    label = "Название турнира *",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Чемпионат по картингу 2026"
                )
                Spacer(Modifier.height(12.dp))

                // ── Sport ──
                CreateDropdown(
                    label = "Вид спорта *",
                    value = selectedSportId,
                    onValueChange = { selectedSportId = it },
                    options = sports.map { it.id to it.name }
                )
                Spacer(Modifier.height(12.dp))

                // ── Location (optional) ──
                if (locations.isNotEmpty()) {
                    CreateDropdown(
                        label = "Локация",
                        value = selectedLocationId,
                        onValueChange = { selectedLocationId = it },
                        options = listOf("" to "Без локации") + locations.map { (it.id ?: "") to "${it.name} (${it.city ?: ""})" }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Dates ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DarkFormField(
                        label = "Дата начала *",
                        value = startDate,
                        onValueChange = { startDate = it },
                        placeholder = "2026-06-15",
                        modifier = Modifier.weight(1f)
                    )
                    DarkFormField(
                        label = "Дата окончания",
                        value = endDate,
                        onValueChange = { endDate = it },
                        placeholder = "2026-06-16",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                DarkFormField(
                    label = "Дедлайн регистрации",
                    value = registrationDeadline,
                    onValueChange = { registrationDeadline = it },
                    placeholder = "2026-06-10"
                )
                Spacer(Modifier.height(12.dp))

                // ── Participants ──
                DarkFormField(
                    label = "Макс. участников",
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    placeholder = "16",
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(12.dp))

                // ── Format ──
                CreateDropdown(
                    label = "Формат сетки",
                    value = format,
                    onValueChange = { format = it },
                    options = listOf(
                        "single_elimination" to "Single Elimination",
                        "double_elimination" to "Double Elimination",
                        "round_robin" to "Round Robin",
                        "group_stage" to "Групповой этап",
                        "groups_knockout" to "Группы + Плей-офф"
                    )
                )
                Spacer(Modifier.height(12.dp))

                CreateDropdown(
                    label = "Формат матча",
                    value = matchFormat,
                    onValueChange = { matchFormat = it },
                    options = listOf(
                        "best_of_1" to "Bo1 (1 игра)",
                        "best_of_3" to "Bo3 (до 2 побед)",
                        "best_of_5" to "Bo5 (до 3 побед)"
                    )
                )
                Spacer(Modifier.height(12.dp))

                // ── Visibility ──
                CreateDropdown(
                    label = "Видимость",
                    value = visibility,
                    onValueChange = { visibility = it },
                    options = listOf(
                        "public" to "Публичный",
                        "private" to "Приватный",
                        "unlisted" to "По ссылке"
                    )
                )
                Spacer(Modifier.height(12.dp))

                // ── Check-in ──
                DarkSwitchField(
                    label = "Включить check-in",
                    description = "Участники должны отметиться перед турниром",
                    checked = hasCheckIn,
                    onCheckedChange = { hasCheckIn = it }
                )
                Spacer(Modifier.height(12.dp))

                // ── Prize ──
                DarkFormField(
                    label = "Призовой фонд",
                    value = prize,
                    onValueChange = { prize = it },
                    placeholder = "100 000 ₸"
                )
                Spacer(Modifier.height(12.dp))

                // ── Description ──
                DarkFormField(
                    label = "Описание",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Описание турнира",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))

                // ── Create button ──
                val canCreate = name.isNotBlank() && selectedSportId.isNotBlank() && startDate.isNotBlank() && !isCreating
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canCreate) Accent else TextMuted.copy(alpha = 0.3f))
                        .clickable(enabled = canCreate) {
                            scope.launch {
                                isCreating = true
                                try {
                                    val tournamentId = repo.createTournament(
                                        TournamentInsertDto(
                                            name = name,
                                            sportId = selectedSportId,
                                            organizerId = userId,
                                            locationId = selectedLocationId.ifBlank { null },
                                            status = "draft",
                                            startDate = startDate,
                                            endDate = endDate.ifBlank { null },
                                            description = description.ifBlank { null },
                                            format = format,
                                            matchFormat = matchFormat,
                                            visibility = visibility,
                                            maxParticipants = maxParticipants.toIntOrNull(),
                                            registrationDeadline = registrationDeadline.ifBlank { null },
                                            hasCheckIn = hasCheckIn,
                                            prize = prize.ifBlank { null }
                                        )
                                    )
                                    snackbarHostState.showSnackbar("Турнир создан")
                                    onCreated(tournamentId)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(e.message ?: "Ошибка создания")
                                } finally {
                                    isCreating = false
                                }
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Создать турнир",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CreateDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<Pair<String, String>>
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == value }?.second ?: options.firstOrNull()?.second ?: "—"

    Column {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
        }
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBg, RoundedCornerShape(12.dp))
                    .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedLabel,
                    fontSize = 14.sp,
                    color = if (value.isEmpty()) colors.textMuted else colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ExpandMore, null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colors.cardBg)
            ) {
                options.forEach { (key, displayName) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                displayName,
                                color = if (key == value) colors.accent else colors.textPrimary,
                                fontWeight = if (key == value) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueChange(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
