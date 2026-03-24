package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.LocationsListData
import com.ileader.app.ui.viewmodels.OrganizerLocationsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

internal fun locationTypeLabel(type: String?): String = when (type) {
    "karting" -> "Картодром"
    "shooting" -> "Тир"
    "stadium" -> "Стадион"
    "arena" -> "Арена"
    else -> type ?: "Другое"
}

@Composable
fun OrganizerLocationsScreen(user: User) {
    var screenMode by remember { mutableStateOf("list") }
    var selectedId by remember { mutableStateOf<String?>(null) }

    when (screenMode) {
        "list" -> LocationsListContent(
            userId = user.id,
            onLocationClick = { id -> selectedId = id; screenMode = "detail" },
            onCreateClick = { screenMode = "create" }
        )
        "detail" -> {
            val id = selectedId ?: return
            OrganizerLocationDetailScreen(
                locationId = id,
                userId = user.id,
                onBack = { screenMode = "list" },
                onEditClick = { screenMode = "edit" }
            )
        }
        "edit" -> {
            val id = selectedId ?: return
            OrganizerLocationEditScreen(
                locationId = id,
                userId = user.id,
                onBack = { screenMode = "detail" },
                onSave = { screenMode = "detail" }
            )
        }
        "create" -> OrganizerLocationCreateScreen(
            userId = user.id,
            onBack = { screenMode = "list" },
            onCreated = { screenMode = "list" }
        )
    }
}

@Composable
private fun LocationsListContent(
    userId: String,
    onLocationClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    val vm: OrganizerLocationsViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(userId) { vm.load(userId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(userId) }
        is UiState.Success -> LocationsListSuccessContent(
            data = s.data,
            onLocationClick = onLocationClick,
            onCreateClick = onCreateClick
        )
    }
}

@Composable
private fun LocationsListSuccessContent(
    data: LocationsListData,
    onLocationClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    val locations = data.locations
    val countByLocation = data.tournamentCountByLocation
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }

    val filteredLocations = locations.filter { loc ->
        val matchesSearch = searchQuery.isEmpty() ||
            loc.name.lowercase().contains(searchQuery.lowercase())
        val matchesType = selectedType == "all" || loc.type == selectedType
        matchesSearch && matchesType
    }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            FadeIn(visible = started, delayMs = 0) {
                Column {
                    Text("Локации", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Площадки для турниров", fontSize = 14.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats
            FadeIn(visible = started, delayMs = 150) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Всего", locations.size.toString(), Modifier.weight(1f))
                    MiniStat("Турниров", locations.sumOf { countByLocation[it.id] ?: 0 }.toString(), Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Search + Filters
            FadeIn(visible = started, delayMs = 300) {
                Column {
                    DarkSearchField(searchQuery, { searchQuery = it }, "Поиск локации...")

                    Spacer(Modifier.height(16.dp))

                    // Type filters
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typeFilters = listOf(
                            "all" to "Все", "karting" to "Картодром", "shooting" to "Тир",
                            "stadium" to "Стадион", "arena" to "Арена"
                        )
                        typeFilters.forEach { (value, label) ->
                            DarkFilterChip(label, selectedType == value, { selectedType = value })
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("${filteredLocations.size} из ${locations.size}", fontSize = 12.sp, color = TextSecondary)

            Spacer(Modifier.height(16.dp))

            // Location cards
            FadeIn(visible = started, delayMs = 450) {
                Column {
                    filteredLocations.forEach { location ->
                        LocationCard(
                            location = location,
                            tournamentCount = countByLocation[location.id] ?: 0,
                            onClick = { onLocationClick(location.id ?: "") }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = onCreateClick,
            containerColor = Accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Добавить локацию")
        }
    }
}

@Composable
private fun LocationCard(location: LocationDto, tournamentCount: Int, onClick: () -> Unit) {
    DarkCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentIconBox(
                    when (location.type) {
                        "karting" -> Icons.Default.DirectionsCar
                        "shooting" -> Icons.Default.GpsFixed
                        "stadium" -> Icons.Default.Stadium
                        "arena" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.Place
                    }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(location.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    StatusBadge(locationTypeLabel(location.type), Accent)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("${location.address ?: ""}, ${location.city ?: ""}", fontSize = 12.sp, color = TextSecondary)
            }

            if ((location.capacity ?: 0) > 0) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("Вместимость: ${location.capacity}", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Facilities
            val facilities = location.facilities ?: emptyList()
            if (facilities.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    facilities.take(3).forEach { facility ->
                        Surface(shape = RoundedCornerShape(6.dp), color = TextMuted.copy(alpha = 0.12f)) {
                            Text(facility, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                    if (facilities.size > 3) {
                        Surface(shape = RoundedCornerShape(6.dp), color = AccentSoft) {
                            Text("+${facilities.size - 3}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Accent)
                        }
                    }
                }
            }

            // Stats row
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(14.dp), tint = Accent)
                Spacer(Modifier.width(4.dp))
                Text("$tournamentCount турниров", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                if (location.rating != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Accent)
                    Spacer(Modifier.width(2.dp))
                    Text("${location.rating}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
