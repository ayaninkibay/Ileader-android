package com.ileader.app.ui.screens.referee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ResultInsertDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.RefereeTournamentDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeTournamentResultsScreen(
    user: User,
    tournamentId: String = "t-7",
    onBack: () -> Unit = {}
) {
    val viewModel: RefereeTournamentDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(tournamentId, user.id) { viewModel.load(tournamentId, user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(tournamentId, user.id) }
        is UiState.Success -> ResultsContent(s.data.tournament, s.data.participants, viewModel, onBack)
    }
}

@Composable
private fun ResultsContent(
    tournament: RefereeTournament,
    participants: List<RefereeParticipant>,
    viewModel: RefereeTournamentDetailViewModel,
    onBack: () -> Unit
) {
    val results = remember {
        mutableStateMapOf<String, String>().apply {
            participants.forEach { put(it.id, "") }
        }
    }
    var isSaving by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Back
            Row(Modifier.clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(20.dp), TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Назад к турниру", fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Text("Внесение результатов", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text(tournament.name, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.date, fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.location.take(25), fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── RESULTS FORM ──
            FadeIn(visible, 200) {
                DarkCardPadded(padding = 14.dp) {
                    Text("Результаты участников", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = TextPrimary, letterSpacing = (-0.3).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Введите позицию или время", fontSize = 13.sp, color = TextSecondary)

                    Spacer(Modifier.height(16.dp))

                    participants.forEach { participant ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AccentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#${participant.number}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Accent)
                            }

                            Column(Modifier.weight(1f)) {
                                Text(participant.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(participant.team, fontSize = 11.sp, color = TextSecondary)
                            }

                            // Input
                            Surface(modifier = Modifier.width(90.dp), shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.4f)) {
                                BasicTextField(
                                    value = results[participant.id] ?: "",
                                    onValueChange = { results[participant.id] = it },
                                    modifier = Modifier
                                        .border(0.5.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Accent),
                                    decorationBox = { inner ->
                                        if ((results[participant.id] ?: "").isEmpty()) {
                                            Text("Позиция", fontSize = 12.sp, color = TextMuted)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ACTIONS ──
            FadeIn(visible, 400) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Отмена", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            isSaving = true
                            val resultDtos = results.entries
                                .filter { it.value.isNotBlank() }
                                .mapNotNull { (athleteId, positionStr) ->
                                    positionStr.toIntOrNull()?.let { pos ->
                                        ResultInsertDto(
                                            tournamentId = tournament.id,
                                            athleteId = athleteId,
                                            position = pos
                                        )
                                    }
                                }
                            if (resultDtos.isNotEmpty()) {
                                viewModel.saveResults(resultDtos) { onBack() }
                            } else {
                                isSaving = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Сохранить", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
