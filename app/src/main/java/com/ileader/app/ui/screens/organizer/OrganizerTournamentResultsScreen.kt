package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.*

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun OrganizerTournamentResultsScreen(
    tournamentId: String,
    onBack: () -> Unit
) {
    val vm: OrganizerTournamentResultsViewModel = viewModel()
    val state by vm.state.collectAsState()
    val saveState by vm.saveState.collectAsState()

    LaunchedEffect(tournamentId) { vm.load(tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(tournamentId) }
        is UiState.Success -> {
            val tournament = s.data.tournament
            val results = s.data.results

            var showSaved by remember { mutableStateOf(false) }

            // Handle save/publish success
            LaunchedEffect(saveState) {
                if (saveState is UiState.Success) {
                    showSaved = true
                }
            }

            Box(Modifier.fillMaxSize().background(Bg)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    BackHeader("Результаты", onBack)

                    if (showSaved) {
                        Spacer(Modifier.height(8.dp))
                        SuccessBanner("Результаты сохранены!")
                    }

                    if (saveState is UiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (saveState as UiState.Error).message,
                            color = ILeaderColors.LightRed,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tournament info card
                    if (tournament != null) {
                        DarkCard {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AccentIconBox(Icons.Default.EmojiEvents)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        tournament.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "${tournament.sports?.name ?: ""} • ${tournament.startDate ?: ""}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    if (results.isEmpty()) {
                        DarkCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = TextMuted
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Результатов пока нет",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        // Results table card
                        DarkCard {
                            Column {
                                // Table header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            AccentSoft,
                                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#", modifier = Modifier.width(30.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Accent, textAlign = TextAlign.Center)
                                    Text("Участник", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Accent)
                                    Text("Время", modifier = Modifier.width(70.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Accent, textAlign = TextAlign.Center)
                                    Text("Очки", modifier = Modifier.width(50.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Accent, textAlign = TextAlign.Center)
                                }

                                // Result rows
                                results.forEachIndexed { index, result ->
                                    ResultRow(result, isLast = index == results.lastIndex)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    val isSaving = saveState is UiState.Loading

                    // Save button
                    Button(
                        onClick = {
                            val insertDtos = results.map { r ->
                                ResultInsertDto(
                                    tournamentId = r.tournamentId,
                                    athleteId = r.athleteId,
                                    position = r.position,
                                    points = r.points,
                                    time = r.time,
                                    penalty = r.penalty,
                                    notes = r.notes
                                )
                            }
                            vm.saveResults(tournamentId, insertDtos)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && results.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Сохранить результаты", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Publish button
                    OutlinedButton(
                        onClick = { vm.publishResults(tournamentId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && results.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = DarkTheme.cardBorderStroke,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Publish, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Опубликовать результаты", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultRow(result: ResultDto, isLast: Boolean) {
    val positionColor = when (result.position) {
        1 -> ILeaderColors.Gold
        2 -> ILeaderColors.Silver
        3 -> ILeaderColors.Bronze
        else -> TextSecondary
    }

    Column {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(positionColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(result.position.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = positionColor)
            }
            Spacer(Modifier.width(8.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.profiles?.name ?: "Участник",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkTheme.TextPrimary
                )
            }

            // Time
            Text(
                result.time ?: "",
                modifier = Modifier.width(70.dp),
                fontSize = 13.sp,
                color = DarkTheme.TextPrimary,
                textAlign = TextAlign.Center
            )

            // Points
            Text(
                (result.points ?: 0).toString(),
                modifier = Modifier.width(50.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTheme.TextPrimary,
                textAlign = TextAlign.Center
            )
        }

        // Penalty / notes
        if (result.penalty != null || result.notes != null) {
            Row(
                modifier = Modifier.padding(start = 48.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.penalty != null) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Accent.copy(alpha = 0.1f)) {
                        Text(result.penalty, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = Accent)
                    }
                }
                if (result.notes != null) {
                    Surface(shape = RoundedCornerShape(4.dp), color = AccentSoft) {
                        Text(result.notes, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = Accent)
                    }
                }
            }
        }

        // Thin separator
        if (!isLast) {
            Box(
                modifier = Modifier.fillMaxWidth().height(0.5.dp)
                    .background(DarkTheme.CardBorder.copy(alpha = 0.5f))
            )
        }
    }
}
