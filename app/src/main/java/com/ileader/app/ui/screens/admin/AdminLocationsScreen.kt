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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminLocationsViewModel

@Composable
fun AdminLocationsScreen(
    onBack: () -> Unit,
    onViewLocation: (String) -> Unit,
    onEditLocation: (String) -> Unit
) {
    val viewModel: AdminLocationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val locations = s.data
            var searchTerm by remember { mutableStateOf("") }
            var selectedType by remember { mutableStateOf("all") }
            var selectedCity by remember { mutableStateOf("all") }
            var showDeleteDialog by remember { mutableStateOf<String?>(null) }

            val kartingCount = locations.count { it.type == "karting" }
            val shootingCount = locations.count { it.type == "shooting" }
            val stadiumArenaCount = locations.count { it.type == "stadium" || it.type == "arena" }

            val filteredLocations = locations.filter { loc ->
                val matchSearch = searchTerm.isEmpty() ||
                        loc.name.contains(searchTerm, ignoreCase = true) ||
                        loc.city?.contains(searchTerm, ignoreCase = true) == true
                val matchType = selectedType == "all" || loc.type == selectedType
                val matchCity = selectedCity == "all" || loc.city == selectedCity
                matchSearch && matchType && matchCity
            }

            Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
                BackHeader("Локации", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("Всего", "${locations.size}", modifier = Modifier.weight(1f))
                        MiniStat("Картодромов", "$kartingCount", modifier = Modifier.weight(1f))
                        MiniStat("Тиров", "$shootingCount", modifier = Modifier.weight(1f))
                        MiniStat("Стадионов", "$stadiumArenaCount", modifier = Modifier.weight(1f))
                    }

                    DarkSearchField(value = searchTerm, onValueChange = { searchTerm = it },
                        placeholder = "Поиск по названию или городу")

                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            "all" to "Все", "karting" to "Картодром", "shooting" to "Тир",
                            "stadium" to "Стадион", "arena" to "Арена", "other" to "Другое"
                        )
                        types.forEach { (key, label) ->
                            DarkFilterChip(text = label, selected = selectedType == key, onClick = { selectedType = key })
                        }
                    }

                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val cities = listOf("all" to "Все города") +
                                locations.mapNotNull { it.city }.distinct().sorted().map { it to it }
                        cities.forEach { (key, label) ->
                            DarkFilterChip(text = label, selected = selectedCity == key, onClick = { selectedCity = key })
                        }
                    }

                    Text("Показано ${filteredLocations.size} из ${locations.size}",
                        fontSize = 13.sp, color = TextMuted)

                    filteredLocations.forEach { loc ->
                        LocationCard(
                            location = loc,
                            onView = { onViewLocation(loc.id ?: "") },
                            onEdit = { onEditLocation(loc.id ?: "") },
                            onDelete = { showDeleteDialog = loc.id }
                        )
                    }

                    if (filteredLocations.isEmpty()) {
                        EmptyState("Локации не найдены")
                    }
                }
            }

            showDeleteDialog?.let { locationId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    containerColor = CardBg,
                    titleContentColor = TextPrimary,
                    textContentColor = TextSecondary,
                    title = { Text("Удалить локацию?") },
                    text = { Text("Это действие нельзя отменить.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteLocation(locationId)
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
private fun LocationCard(
    location: LocationDto,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = AdminMockData.locationTypeColor(location.type ?: "")

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = AdminMockData.locationTypeLabel(location.type ?: ""), color = typeColor)
                if (location.rating != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ILeaderColors.Warning.copy(alpha = 0.12f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = ILeaderColors.Warning, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${location.rating}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = ILeaderColors.Warning)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(location.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

            Spacer(Modifier.height(6.dp))
            Text("${location.address ?: ""}, ${location.city ?: ""}", fontSize = 12.sp, color = TextSecondary)

            Spacer(Modifier.height(6.dp))

            Text(
                "Вместимость: ${location.capacity ?: "\u2014"}",
                fontSize = 12.sp, color = TextSecondary
            )

            val facilities = location.facilities ?: emptyList()
            if (facilities.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    facilities.take(3).forEach { facility ->
                        Surface(shape = RoundedCornerShape(6.dp), color = CardBorder.copy(alpha = 0.5f)) {
                            Text(facility, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    if (facilities.size > 3) {
                        Surface(shape = RoundedCornerShape(6.dp), color = AccentSoft) {
                            Text("+${facilities.size - 3}",
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = Accent)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(0.5.dp, CardBorder)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Просмотр", fontSize = 12.sp)
                }
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить", fontSize = 12.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = TextMuted)
                }
            }
        }
    }
}
