package com.ileader.app.ui.screens.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LeagueDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.LeaguesListViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun LeaguesListScreen(
    onBack: () -> Unit,
    onOpenLeague: (String) -> Unit
) {
    val vm: LeaguesListViewModel = viewModel()
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var query by remember { mutableStateOf("") }
    var sportFilter by remember { mutableIntStateOf(0) }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Лиги", onBack)

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { vm.load() }
            is UiState.Success -> {
                val data = s.data
                val filters = remember(data.sports) {
                    buildList {
                        add("all" to "Все виды")
                        data.sports.forEach { add(it.id to it.name) }
                    }
                }
                val filtered = remember(query, sportFilter, data.leagues) {
                    val sportValue = filters.getOrNull(sportFilter)?.first ?: "all"
                    data.leagues.filter { league ->
                        (sportValue == "all" || league.sportId == sportValue) &&
                        (query.isBlank() || league.name.contains(query, true))
                    }
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(4.dp))
                    DarkSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Поиск лиги..."
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEachIndexed { index, (_, label) ->
                            DarkFilterChip(
                                text = label,
                                selected = sportFilter == index,
                                onClick = { sportFilter = index }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Найдено: ${filtered.size} лиг",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(12.dp))

                    if (filtered.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.EmojiEvents,
                            title = "Лиги не найдены",
                            subtitle = "Попробуйте изменить фильтры"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filtered.forEach { league ->
                                LeagueCard(league, onClick = { onOpenLeague(league.id) })
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun LeagueCard(league: LeagueDto, onClick: () -> Unit) {
    DarkCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!league.sports?.name.isNullOrEmpty()) {
                    StatusBadge(text = league.sports!!.name, color = TextSecondary)
                }
                val statusLabel = when (league.status) {
                    "active" -> "Активна"
                    "upcoming" -> "Скоро"
                    "completed" -> "Завершена"
                    else -> league.status ?: ""
                }
                if (statusLabel.isNotEmpty()) {
                    Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                league.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!league.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    league.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                league.season?.let {
                    Text("Сезон: $it", fontSize = 12.sp, color = TextMuted)
                }
                Text("Этапов: ${league.totalStages}", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}
