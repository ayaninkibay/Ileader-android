package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import kotlinx.coroutines.launch

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun TournamentEditScreen(
    tournamentId: String,
    onBack: () -> Unit
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loadState by remember { mutableStateOf<UiState<TournamentDto>>(UiState.Loading) }
    var isSaving by remember { mutableStateOf(false) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // Load tournament
    LaunchedEffect(tournamentId) {
        loadState = UiState.Loading
        try {
            val tournament = repo.getTournamentDetail(tournamentId)
            loadState = UiState.Success(tournament)
        } catch (e: Exception) {
            loadState = UiState.Error(e.message ?: "Ошибка загрузки турнира")
        }
    }

    // Initialize form from loaded data
    LaunchedEffect(loadState) {
        val state = loadState
        if (state is UiState.Success && !initialized) {
            val t = state.data
            name = t.name
            description = t.description ?: ""
            startDate = t.startDate ?: ""
            endDate = t.endDate ?: ""
            maxParticipants = t.maxParticipants?.toString() ?: ""
            registrationDeadline = t.registrationDeadline ?: ""
            initialized = true
        }
    }

    // Check if anything changed
    val hasChanges = remember(name, description, startDate, endDate, maxParticipants, registrationDeadline, loadState) {
        val state = loadState
        if (state is UiState.Success) {
            val t = state.data
            name != t.name ||
                description != (t.description ?: "") ||
                startDate != (t.startDate ?: "") ||
                endDate != (t.endDate ?: "") ||
                maxParticipants != (t.maxParticipants?.toString() ?: "") ||
                registrationDeadline != (t.registrationDeadline ?: "")
        } else false
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
            BackHeader("Редактирование турнира", onBack)

            when (val state = loadState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = {
                        scope.launch {
                            loadState = UiState.Loading
                            try {
                                val tournament = repo.getTournamentDetail(tournamentId)
                                loadState = UiState.Success(tournament)
                            } catch (e: Exception) {
                                loadState = UiState.Error(e.message ?: "Ошибка загрузки")
                            }
                        }
                    }
                )
                is UiState.Success -> {
                    val tournament = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(20.dp))

                        DarkFormField(
                            label = "Название",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Название турнира"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Описание",
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Описание турнира",
                            singleLine = false,
                            minLines = 3
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Дата начала (YYYY-MM-DD)",
                            value = startDate,
                            onValueChange = { startDate = it },
                            placeholder = "2026-01-01"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Дата окончания (YYYY-MM-DD)",
                            value = endDate,
                            onValueChange = { endDate = it },
                            placeholder = "2026-01-02"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Макс. участников",
                            value = maxParticipants,
                            onValueChange = { maxParticipants = it },
                            placeholder = "16",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Дедлайн регистрации (YYYY-MM-DD)",
                            value = registrationDeadline,
                            onValueChange = { registrationDeadline = it },
                            placeholder = "2025-12-31"
                        )

                        Spacer(Modifier.height(24.dp))

                        // ── Save button ──
                        val enabled = hasChanges && !isSaving && name.isNotBlank() && startDate.isNotBlank()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (enabled) Accent else TextMuted.copy(alpha = 0.3f))
                                .clickable(enabled = enabled) {
                                    scope.launch {
                                        isSaving = true
                                        try {
                                            repo.updateTournament(
                                                tournamentId,
                                                TournamentInsertDto(
                                                    name = name,
                                                    sportId = tournament.sportId ?: "",
                                                    organizerId = tournament.organizerId ?: "",
                                                    locationId = tournament.locationId,
                                                    status = tournament.status ?: "draft",
                                                    startDate = startDate,
                                                    endDate = endDate.ifBlank { null },
                                                    description = description.ifBlank { null },
                                                    format = tournament.format,
                                                    matchFormat = tournament.matchFormat,
                                                    seedingType = tournament.seedingType,
                                                    visibility = tournament.visibility ?: "public",
                                                    maxParticipants = maxParticipants.toIntOrNull(),
                                                    minParticipants = tournament.minParticipants,
                                                    prize = tournament.prize,
                                                    requirements = tournament.requirements,
                                                    categories = tournament.categories,
                                                    ageCategory = tournament.ageCategory,
                                                    groupCount = tournament.groupCount,
                                                    hasThirdPlaceMatch = tournament.hasThirdPlaceMatch,
                                                    hasCheckIn = tournament.hasCheckIn,
                                                    accessCode = tournament.accessCode,
                                                    imageUrl = tournament.imageUrl,
                                                    registrationDeadline = registrationDeadline.ifBlank { null },
                                                    checkInStartsBefore = tournament.checkInStartsBefore,
                                                    prizes = tournament.prizes,
                                                    schedule = tournament.schedule,
                                                    stageMatchFormats = tournament.stageMatchFormats
                                                )
                                            )
                                            snackbarHostState.showSnackbar("Турнир обновлён")
                                            onBack()
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(e.message ?: "Ошибка сохранения")
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Сохранить",
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
    }
}
