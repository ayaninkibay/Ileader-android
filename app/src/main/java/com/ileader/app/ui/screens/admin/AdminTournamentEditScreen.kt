package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminTournamentsViewModel
import com.ileader.app.ui.viewmodels.AdminUserEditViewModel

@Composable
fun AdminTournamentEditScreen(tournamentId: String, onBack: () -> Unit) {
    val viewModel: AdminTournamentsViewModel = viewModel()
    val state by viewModel.detailState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(tournamentId) { viewModel.loadDetail(tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.loadDetail(tournamentId) })
        is UiState.Success -> {
            val tournament = s.data

            var name by remember { mutableStateOf(tournament.name) }
            var startDate by remember { mutableStateOf(tournament.startDate ?: "") }
            var endDate by remember { mutableStateOf(tournament.endDate ?: "") }
            var status by remember { mutableStateOf(tournament.status ?: "draft") }
            var maxParticipants by remember { mutableStateOf(tournament.maxParticipants?.toString() ?: "") }
            var description by remember { mutableStateOf(tournament.description ?: "") }
            var showSaved by remember { mutableStateOf(false) }

            LaunchedEffect(saveState) {
                if (saveState is AdminUserEditViewModel.SaveState.Success) {
                    showSaved = true
                    viewModel.resetSaveState()
                }
            }

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackHeader("Редактирование турнира", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showSaved) {
                        SuccessBanner("Турнир обновлён")
                    }

                    val sColor = AdminMockData.statusColor(status)
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = sColor.copy(alpha = 0.1f)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = sColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(tournament.sports?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                Text(tournament.locations?.name ?: "",
                                    fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            DarkFormField("Название", name, { name = it })
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(Modifier.weight(1f)) {
                                    DarkFormField("Дата начала", startDate, { startDate = it })
                                }
                                Column(Modifier.weight(1f)) {
                                    DarkFormField("Дата окончания", endDate, { endDate = it })
                                }
                            }
                            DarkFormField("Макс. участников", maxParticipants, { maxParticipants = it })
                            DarkFormField("Описание", description, { description = it },
                                placeholder = "Описание турнира", singleLine = false, minLines = 3)

                            FieldLabel("Статус") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val statuses = listOf(
                                        "draft" to "Черновик", "registration_open" to "Регистрация открыта",
                                        "registration_closed" to "Регистрация закрыта", "in_progress" to "Идёт",
                                        "completed" to "Завершён", "cancelled" to "Отменён"
                                    )
                                    statuses.chunked(2).forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            row.forEach { (key, label) ->
                                                val chipColor = AdminMockData.statusColor(key)
                                                FilterChip(
                                                    selected = status == key,
                                                    onClick = { status = key },
                                                    label = { Text(label, fontSize = 12.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = chipColor,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = CardBg,
                                                        labelColor = TextSecondary
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        borderColor = CardBorder,
                                                        selectedBorderColor = Color.Transparent,
                                                        enabled = true,
                                                        selected = status == key
                                                    ),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                            }
                                            if (row.size == 1) Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            AdminSectionTitle("Текущее состояние")
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column {
                                    Text("${tournament.maxParticipants ?: 0}", fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp, color = Accent, letterSpacing = (-0.3).sp)
                                    Text("Участников", fontSize = 12.sp, color = TextSecondary)
                                }
                                Column {
                                    Text(tournament.profiles?.name ?: "", fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp, color = TextPrimary)
                                    Text("Организатор", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateTournament(tournamentId, mapOf(
                                "name" to name,
                                "start_date" to startDate,
                                "end_date" to endDate,
                                "status" to status,
                                "max_participants" to maxParticipants,
                                "description" to description
                            ))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Сохранить изменения", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
