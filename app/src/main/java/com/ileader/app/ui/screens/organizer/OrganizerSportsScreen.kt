package com.ileader.app.ui.screens.organizer

import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.OrganizerSportsViewModel
import com.ileader.app.ui.viewmodels.SportsData

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun OrganizerSportsScreen(user: User) {
    val vm: OrganizerSportsViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> SportsContent(s.data)
    }
}

@Composable
private fun SportsContent(data: SportsData) {
    val sports = data.sports
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRequestSent by remember { mutableStateOf(false) }

    val filteredSports = sports.filter { sport ->
        searchQuery.isEmpty() || sport.name.lowercase().contains(searchQuery.lowercase())
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
                    Text("Виды спорта", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Управление дисциплинами", fontSize = 14.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Info banner
            FadeIn(visible = started, delayMs = 150) {
                Column {
                    InfoBanner("Виды спорта управляются администратором. Вы можете запросить добавление нового вида спорта.")

                    if (showRequestSent) {
                        Spacer(Modifier.height(8.dp))
                        SuccessBanner("Запрос на добавление отправлен!")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Search field
            FadeIn(visible = started, delayMs = 300) {
                DarkSearchField(searchQuery, { searchQuery = it }, "Поиск вида спорта...")
            }

            Spacer(Modifier.height(20.dp))

            // Sport cards
            FadeIn(visible = started, delayMs = 450) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredSports.forEach { sport ->
                        SportDarkCard(sport)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Add sport button
            FadeIn(visible = started, delayMs = 600) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Запросить добавление вида спорта", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Add sport dialog
    if (showAddDialog) {
        DarkAddSportDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = {
                showAddDialog = false
                showRequestSent = true
            }
        )
    }
}

@Composable
private fun SportDarkCard(sport: SportDto) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.SportsSoccer)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sport.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(
                        if (sport.isActive) "Активен" else "Неактивен",
                        if (sport.isActive) Accent else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkAddSportDialog(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    var sportName by remember { mutableStateOf("") }
    var sportDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkTheme.CardBg,
        title = {
            Text("Запрос на новый вид спорта", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                Text("Заполните информацию о виде спорта, который хотите добавить.", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(16.dp))

                DarkFormField("Название", sportName, { sportName = it }, "Введите название")

                Spacer(Modifier.height(12.dp))

                DarkFormField("Описание", sportDescription, { sportDescription = it }, "Опишите вид спорта", singleLine = false, minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = sportName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Отправить запрос", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
