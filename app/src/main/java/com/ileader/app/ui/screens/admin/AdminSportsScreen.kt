package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminSportsViewModel

@Composable
fun AdminSportsScreen(
    onBack: () -> Unit,
    onEditSport: (String) -> Unit
) {
    val viewModel: AdminSportsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val sports = s.data
            var searchTerm by remember { mutableStateOf("") }
            var sortBy by remember { mutableStateOf("name") }
            var showDeleteDialog by remember { mutableStateOf<String?>(null) }

            val filteredSports = sports
                .filter { searchTerm.isEmpty() || it.name.contains(searchTerm, ignoreCase = true) }
                .let { list ->
                    when (sortBy) {
                        "name" -> list.sortedBy { it.name }
                        "date" -> list.sortedByDescending { it.createdAt ?: "" }
                        else -> list
                    }
                }

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackHeader("Виды спорта", onBack) {
                    Button(
                        onClick = { /* mock: add sport */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Добавить", fontSize = 13.sp)
                    }
                }

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            border = BorderStroke(0.5.dp, CardBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${sports.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Accent)
                                Spacer(Modifier.width(6.dp))
                                Text("видов спорта", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DarkFilterChip("По названию", sortBy == "name", { sortBy = "name" })
                            DarkFilterChip("По дате", sortBy == "date", { sortBy = "date" })
                        }
                    }

                    DarkSearchField(value = searchTerm, onValueChange = { searchTerm = it }, placeholder = "Поиск по названию")

                    filteredSports.forEach { sport ->
                        SportCard(sport = sport, onEdit = { onEditSport(sport.id) }, onDelete = { showDeleteDialog = sport.id })
                    }

                    if (filteredSports.isEmpty()) {
                        EmptyState("Виды спорта не найдены")
                    }
                }
            }

            showDeleteDialog?.let { sportId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    containerColor = CardBg,
                    titleContentColor = TextPrimary,
                    textContentColor = TextSecondary,
                    title = { Text("Удалить вид спорта?") },
                    text = { Text("Это действие нельзя отменить.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteSport(sportId)
                            showDeleteDialog = null
                        }) { Text("Удалить", color = Accent) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена", color = TextSecondary) }
                    }
                )
            }
        }
    }
}

@Composable
private fun SportCard(
    sport: SportWithCountsDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sportIcon: ImageVector = when (sport.slug) {
        "karting" -> Icons.Default.DirectionsCar
        "shooting" -> Icons.Default.GpsFixed
        "tennis" -> Icons.Default.SportsTennis
        "football" -> Icons.Default.SportsSoccer
        "boxing" -> Icons.Default.SportsMma
        "swimming" -> Icons.Default.Pool
        "athletics" -> Icons.AutoMirrored.Filled.DirectionsRun
        "rowing" -> Icons.Default.Sailing
        else -> Icons.Default.SportsSoccer
    }

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AccentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(sportIcon, null, tint = Accent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(sport.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                }
                StatusBadge(
                    text = if (sport.isActive) "Активен" else "Неактивен",
                    color = if (sport.isActive) Accent else TextMuted
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(sport.description ?: "", fontSize = 13.sp, color = TextSecondary,
                maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(12.dp))

            Text(
                "${sport.athleteCount} спортсменов \u00B7 ${sport.tournamentCount} турниров",
                fontSize = 12.sp, color = TextSecondary
            )

            Spacer(Modifier.height(4.dp))
            Text("Добавлен: ${sport.createdAt ?: ""}", fontSize = 11.sp, color = TextMuted)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Редактировать", fontSize = 13.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = TextMuted)
                }
            }
        }
    }
}
