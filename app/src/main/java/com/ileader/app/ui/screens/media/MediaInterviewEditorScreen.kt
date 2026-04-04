package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.InterviewInsertDto
import com.ileader.app.data.remote.dto.InterviewUpdateDto
import com.ileader.app.data.remote.dto.ProfileMinimalDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MediaViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted

private val InterviewColor = Color(0xFFE11D48)
private val InterviewColorLight = Color(0xFFFB7185)

private val statuses = listOf(
    "scheduled" to "Запланировано",
    "completed" to "Завершено",
    "published" to "Опубликовано",
    "cancelled" to "Отменено"
)

@Composable
fun MediaInterviewEditorScreen(
    userId: String,
    interviewId: String? = null,
    onBack: () -> Unit,
    vm: MediaViewModel = viewModel()
) {
    val currentInterview by vm.currentInterview.collectAsState()
    val actionState by vm.actionState.collectAsState()
    val athleteResults by vm.athleteSearch.collectAsState()
    val upcomingTournaments by vm.upcomingTournaments.collectAsState()

    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("scheduled") }

    // Athlete selection
    var selectedAthleteId by remember { mutableStateOf<String?>(null) }
    var selectedAthleteName by remember { mutableStateOf("") }
    var athleteQuery by remember { mutableStateOf("") }
    var showAthleteSearch by remember { mutableStateOf(false) }

    // Tournament selection
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    var selectedTournamentName by remember { mutableStateOf("") }
    var showTournamentPicker by remember { mutableStateOf(false) }

    var loaded by remember { mutableStateOf(false) }

    val snackbar = LocalSnackbarHost.current
    val isEditing = interviewId != null
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    // Load data
    LaunchedEffect(interviewId) {
        if (interviewId != null) {
            vm.loadInterview(interviewId)
        }
        vm.loadUpcomingTournaments()
        vm.searchAthletes("")
    }

    // Populate fields from loaded interview
    LaunchedEffect(currentInterview) {
        if (isEditing && currentInterview is UiState.Success && !loaded) {
            (currentInterview as UiState.Success).data?.let { interview ->
                title = interview.title
                topic = interview.topic ?: ""
                content = interview.content ?: ""
                scheduledDate = interview.scheduledDate ?: ""
                time = interview.time ?: ""
                location = interview.location ?: ""
                notes = interview.notes ?: ""
                selectedStatus = interview.status ?: "scheduled"
                selectedAthleteId = interview.athleteId
                selectedAthleteName = interview.athlete?.name ?: ""
                selectedTournamentId = interview.tournamentId
                selectedTournamentName = interview.tournaments?.name ?: ""
                loaded = true
            }
        }
    }

    // Handle action results
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is UiState.Success -> {
                snackbar.showSnackbar(s.data)
                vm.clearAction()
                onBack()
            }
            is UiState.Error -> {
                snackbar.showSnackbar(s.message)
                vm.clearAction()
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Header ──
        item {
            EditorHeader(
                isEditing = isEditing,
                onBack = onBack,
                onSave = {
                    if (title.isBlank() || selectedAthleteId == null) return@EditorHeader
                    if (isEditing && interviewId != null) {
                        vm.updateInterview(
                            interviewId, userId,
                            InterviewUpdateDto(
                                title = title,
                                athleteId = selectedAthleteId,
                                tournamentId = selectedTournamentId,
                                content = content.ifBlank { null },
                                topic = topic.ifBlank { null },
                                scheduledDate = scheduledDate.ifBlank { null },
                                time = time.ifBlank { null },
                                location = location.ifBlank { null },
                                notes = notes.ifBlank { null },
                                status = selectedStatus
                            )
                        )
                    } else {
                        vm.createInterview(
                            userId,
                            InterviewInsertDto(
                                mediaUserId = userId,
                                athleteId = selectedAthleteId!!,
                                tournamentId = selectedTournamentId,
                                title = title,
                                content = content.ifBlank { null },
                                topic = topic.ifBlank { null },
                                scheduledDate = scheduledDate.ifBlank { null },
                                time = time.ifBlank { null },
                                location = location.ifBlank { null },
                                notes = notes.ifBlank { null },
                                status = selectedStatus
                            )
                        )
                    }
                },
                canSave = title.isNotBlank() && selectedAthleteId != null,
                isLoading = actionState is UiState.Loading
            )
        }

        // Loading state for edit
        if (isEditing && currentInterview is UiState.Loading) {
            item {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    LoadingScreen()
                }
            }
            return@LazyColumn
        }

        // ── Athlete selection ──
        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Спортсмен *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))

                if (selectedAthleteId != null && selectedAthleteName.isNotBlank()) {
                    // Selected athlete chip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = InterviewColor.copy(0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InterviewColor.copy(0.3f))
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(
                                    Brush.linearGradient(listOf(InterviewColor.copy(0.7f), InterviewColorLight.copy(0.4f)))
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(selectedAthleteName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    selectedAthleteId = null
                                    selectedAthleteName = ""
                                    showAthleteSearch = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else {
                    // Search field
                    OutlinedTextField(
                        value = athleteQuery,
                        onValueChange = {
                            athleteQuery = it
                            showAthleteSearch = true
                            vm.searchAthletes(it)
                        },
                        placeholder = { Text("Поиск спортсмена по имени", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InterviewColor,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = InterviewColor
                        )
                    )

                    // Search results
                    if (showAthleteSearch && athleteResults.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            border = if (isDark) DarkTheme.cardBorderStroke
                            else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
                            shadowElevation = 0.dp
                        ) {
                            Column(Modifier.heightIn(max = 200.dp)) {
                                athleteResults.take(8).forEach { athlete ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAthleteId = athlete.id
                                                selectedAthleteName = athlete.name ?: ""
                                                athleteQuery = ""
                                                showAthleteSearch = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val avatarUrl = athlete.avatarUrl
                                        if (avatarUrl != null) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                Modifier.size(32.dp).clip(CircleShape).background(InterviewColor.copy(0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, null, tint = InterviewColor, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(athlete.name ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            athlete.city?.let { Text(it, fontSize = 11.sp, color = TextMuted) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Title ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Заголовок *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Тема интервью", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InterviewColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = InterviewColor
                    )
                )
            }
        }

        // ── Topic ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Тема", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = { Text("Карьера, турнир, подготовка...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InterviewColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = InterviewColor
                    )
                )
            }
        }

        // ── Tournament (optional) ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Привязка к турниру (необязательно)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))

                if (selectedTournamentId != null && selectedTournamentName.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3B82F6).copy(0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(0.3f))
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(selectedTournamentName, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            IconButton(
                                onClick = {
                                    selectedTournamentId = null
                                    selectedTournamentName = ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showTournamentPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Выбрать турнир", color = TextMuted)
                    }
                }
            }
        }

        // ── Date & Time ──
        item {
            Spacer(Modifier.height(14.dp))
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Дата", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = scheduledDate,
                        onValueChange = { scheduledDate = it },
                        placeholder = { Text("2026-04-15", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InterviewColor,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = InterviewColor
                        )
                    )
                }
                Column(Modifier.weight(0.7f)) {
                    Text("Время", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        placeholder = { Text("14:00", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InterviewColor,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = InterviewColor
                        )
                    )
                }
            }
        }

        // ── Location ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Место", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Студия, стадион, онлайн...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TextMuted) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InterviewColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = InterviewColor
                    )
                )
            }
        }

        // ── Description ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Описание / вопросы", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Подготовленные вопросы, заметки...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InterviewColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = InterviewColor
                    )
                )
            }
        }

        // ── Internal notes ──
        item {
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Внутренние заметки", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Только для вас — не публикуется", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InterviewColor,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = InterviewColor
                    )
                )
            }
        }

        // ── Status selector ──
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Статус", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses) { (key, label) ->
                        val isSelected = selectedStatus == key
                        val chipColor = getStatusChipColor(key)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) chipColor else CardBg,
                            border = if (!isSelected) {
                                if (isDark) DarkTheme.cardBorderStroke
                                else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f))
                            } else null,
                            modifier = Modifier.clickable { selectedStatus = key }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    getStatusIcon(key), null,
                                    tint = if (isSelected) Color.White else chipColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Tournament picker dialog ──
    if (showTournamentPicker) {
        TournamentPickerDialog(
            tournaments = upcomingTournaments,
            onSelect = { id, name ->
                selectedTournamentId = id
                selectedTournamentName = name
                showTournamentPicker = false
            },
            onDismiss = { showTournamentPicker = false }
        )
    }
}

// ══════════════════════════════════════════════════════════
// Editor Header
// ══════════════════════════════════════════════════════════

@Composable
private fun EditorHeader(
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(listOf(Color(0xFF9F1239), InterviewColor, InterviewColorLight))
            )
        )
        Column(
            Modifier.statusBarsPadding().padding(horizontal = 8.dp).padding(top = 4.dp, bottom = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                }
                Text(
                    if (isEditing) "Редактирование" else "Новое интервью",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = onSave,
                    enabled = canSave && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(0.2f),
                        disabledContainerColor = Color.White.copy(0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Сохранить", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournament Picker Dialog
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentPickerDialog(
    tournaments: UiState<List<TournamentWithCountsDto>>,
    onSelect: (id: String, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF3B82F6))
                Spacer(Modifier.width(8.dp))
                Text("Выберите турнир", color = TextPrimary, fontSize = 18.sp)
            }
        },
        text = {
            when (tournaments) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InterviewColor, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Error -> {
                    Text("Ошибка загрузки", color = Color(0xFFEF4444), fontSize = 13.sp)
                }
                is UiState.Success -> {
                    val list = tournaments.data
                    if (list.isEmpty()) {
                        Text("Нет доступных турниров", fontSize = 13.sp, color = TextMuted)
                    } else {
                        LazyColumn(Modifier.heightIn(max = 300.dp)) {
                            items(list, key = { it.id }) { t ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(t.id, t.name) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            t.sportName?.let { Text(it, fontSize = 11.sp, color = TextMuted) }
                                            t.startDate?.let { Text(formatDateShort(it), fontSize = 11.sp, color = TextMuted) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val m = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${m.getOrElse(month) { "" }}"
}

private fun getStatusChipColor(status: String): Color = when (status) {
    "scheduled" -> Color(0xFFF59E0B)
    "completed" -> Color(0xFF3B82F6)
    "published" -> Color(0xFF10B981)
    "cancelled" -> Color(0xFFEF4444)
    else -> Color(0xFF8E8E93)
}

private fun getStatusIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector = when (status) {
    "scheduled" -> Icons.Default.Schedule
    "completed" -> Icons.Default.CheckCircle
    "published" -> Icons.Default.Public
    "cancelled" -> Icons.Default.Cancel
    else -> Icons.Default.HourglassTop
}
