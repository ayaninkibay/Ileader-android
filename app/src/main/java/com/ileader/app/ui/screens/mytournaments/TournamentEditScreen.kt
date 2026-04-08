package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.ileader.app.data.remote.dto.ScheduleItemDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkSwitchField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EditableCoverImage
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@OptIn(ExperimentalLayoutApi::class)
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
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var scheduleItems by remember { mutableStateOf<List<ScheduleItemDto>>(emptyList()) }

    // ── Basic fields ──
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var minParticipants by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }

    // ── Format fields ──
    var format by remember { mutableStateOf("") }
    var matchFormat by remember { mutableStateOf("") }
    var seedingType by remember { mutableStateOf("") }
    var groupCount by remember { mutableStateOf("") }
    var hasThirdPlaceMatch by remember { mutableStateOf(false) }

    // ── Access & visibility ──
    var visibility by remember { mutableStateOf("public") }
    var accessCode by remember { mutableStateOf("") }

    // ── Categories ──
    var ageCategory by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var genderCategory by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    // ── Check-in ──
    var hasCheckIn by remember { mutableStateOf(false) }
    var checkInStartsBefore by remember { mutableStateOf("") }

    // ── Entry fee ──
    var entryFee by remember { mutableStateOf("") }

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
            minParticipants = t.minParticipants?.toString() ?: ""
            registrationDeadline = t.registrationDeadline ?: ""
            prize = t.prize ?: ""
            format = t.format ?: ""
            matchFormat = t.matchFormat ?: ""
            seedingType = t.seedingType ?: ""
            groupCount = t.groupCount?.toString() ?: ""
            hasThirdPlaceMatch = t.hasThirdPlaceMatch ?: false
            visibility = t.visibility ?: "public"
            accessCode = t.accessCode ?: ""
            ageCategory = t.ageCategory ?: ""
            skillLevel = t.skillLevel ?: ""
            genderCategory = t.genderCategory ?: ""
            region = t.region ?: ""
            hasCheckIn = t.hasCheckIn ?: false
            checkInStartsBefore = t.checkInStartsBefore?.toString() ?: ""
            entryFee = t.entryFee?.let { if (it > 0) it.toInt().toString() else "" } ?: ""
            imageUrl = t.imageUrl
            scheduleItems = t.schedule?.let {
                try {
                    kotlinx.serialization.json.Json.decodeFromJsonElement(
                        kotlinx.serialization.builtins.ListSerializer(ScheduleItemDto.serializer()),
                        it
                    )
                } catch (_: Exception) { emptyList() }
            } ?: emptyList()
            initialized = true
        }
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

                        // ══════════════════════════════
                        // SECTION: Обложка
                        // ══════════════════════════════
                        SectionHeader("Обложка турнира")
                        EditableCoverImage(
                            imageUrl = imageUrl,
                            isUploading = isUploadingImage,
                            onImageSelected = { bytes ->
                                scope.launch {
                                    isUploadingImage = true
                                    try {
                                        val url = repo.uploadTournamentImage(tournamentId, bytes)
                                        imageUrl = url
                                        snackbarHostState.showSnackbar("Обложка обновлена")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(e.message ?: "Ошибка загрузки")
                                    } finally {
                                        isUploadingImage = false
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Основная информация
                        // ══════════════════════════════
                        SectionHeader("Основная информация")

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
                            label = "Призовой фонд",
                            value = prize,
                            onValueChange = { prize = it },
                            placeholder = "100 000 ₸ или описание"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Вступительный взнос (₸)",
                            value = entryFee,
                            onValueChange = { entryFee = it },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Даты
                        // ══════════════════════════════
                        SectionHeader("Даты")

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DarkFormField(
                                label = "Дата начала",
                                value = startDate,
                                onValueChange = { startDate = it },
                                placeholder = "2026-01-01",
                                modifier = Modifier.weight(1f)
                            )
                            DarkFormField(
                                label = "Дата окончания",
                                value = endDate,
                                onValueChange = { endDate = it },
                                placeholder = "2026-01-02",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Дедлайн регистрации",
                            value = registrationDeadline,
                            onValueChange = { registrationDeadline = it },
                            placeholder = "2025-12-31"
                        )

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Участники
                        // ══════════════════════════════
                        SectionHeader("Участники")

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DarkFormField(
                                label = "Макс. участников",
                                value = maxParticipants,
                                onValueChange = { maxParticipants = it },
                                placeholder = "16",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                            DarkFormField(
                                label = "Мин. участников",
                                value = minParticipants,
                                onValueChange = { minParticipants = it },
                                placeholder = "4",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Формат
                        // ══════════════════════════════
                        SectionHeader("Формат турнира")

                        DropdownSelector(
                            label = "Формат сетки",
                            value = format,
                            onValueChange = { format = it },
                            options = listOf(
                                "" to "Не выбран",
                                "single_elimination" to "Single Elimination",
                                "double_elimination" to "Double Elimination",
                                "round_robin" to "Round Robin",
                                "group_stage" to "Групповой этап",
                                "groups_knockout" to "Группы + Плей-офф",
                                "swiss" to "Швейцарская система"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        DropdownSelector(
                            label = "Формат матча",
                            value = matchFormat,
                            onValueChange = { matchFormat = it },
                            options = listOf(
                                "" to "Не выбран",
                                "best_of_1" to "Bo1 (1 игра)",
                                "best_of_3" to "Bo3 (до 2 побед)",
                                "best_of_5" to "Bo5 (до 3 побед)",
                                "best_of_7" to "Bo7 (до 4 побед)"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        DropdownSelector(
                            label = "Тип рассеивания",
                            value = seedingType,
                            onValueChange = { seedingType = it },
                            options = listOf(
                                "" to "Не выбран",
                                "random" to "Случайное",
                                "manual" to "Ручное",
                                "rating" to "По рейтингу"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        if (format in listOf("group_stage", "groups_knockout")) {
                            DarkFormField(
                                label = "Количество групп",
                                value = groupCount,
                                onValueChange = { groupCount = it },
                                placeholder = "4",
                                keyboardType = KeyboardType.Number
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        DarkSwitchField(
                            label = "Матч за 3-е место",
                            description = "Проводить ли матч за бронзу",
                            checked = hasThirdPlaceMatch,
                            onCheckedChange = { hasThirdPlaceMatch = it }
                        )

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Категории
                        // ══════════════════════════════
                        SectionHeader("Категории и фильтры")

                        DropdownSelector(
                            label = "Возрастная категория",
                            value = ageCategory,
                            onValueChange = { ageCategory = it },
                            options = listOf(
                                "" to "Без ограничений",
                                "children" to "Дети (до 12)",
                                "youth" to "Юноши (13-17)",
                                "adult" to "Взрослые (18+)",
                                "senior" to "Ветераны",
                                "open" to "Открытая"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        DropdownSelector(
                            label = "Уровень",
                            value = skillLevel,
                            onValueChange = { skillLevel = it },
                            options = listOf(
                                "" to "Любой",
                                "beginner" to "Начинающий",
                                "intermediate" to "Средний",
                                "advanced" to "Продвинутый",
                                "pro" to "Профессионал",
                                "open" to "Открытый"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        DropdownSelector(
                            label = "Пол",
                            value = genderCategory,
                            onValueChange = { genderCategory = it },
                            options = listOf(
                                "" to "Без ограничений",
                                "male" to "Мужчины",
                                "female" to "Женщины",
                                "mixed" to "Смешанный",
                                "open" to "Открытый"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Регион",
                            value = region,
                            onValueChange = { region = it },
                            placeholder = "Алматы, Казахстан"
                        )

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Доступ
                        // ══════════════════════════════
                        SectionHeader("Видимость и доступ")

                        DropdownSelector(
                            label = "Видимость",
                            value = visibility,
                            onValueChange = { visibility = it },
                            options = listOf(
                                "public" to "Публичный",
                                "private" to "Приватный",
                                "unlisted" to "По ссылке"
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        if (visibility == "private") {
                            DarkFormField(
                                label = "Код доступа",
                                value = accessCode,
                                onValueChange = { accessCode = it },
                                placeholder = "Секретный код для участия"
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Check-in
                        // ══════════════════════════════
                        SectionHeader("Check-in")

                        DarkSwitchField(
                            label = "Включить check-in",
                            description = "Участники должны отметиться перед началом",
                            checked = hasCheckIn,
                            onCheckedChange = { hasCheckIn = it }
                        )
                        Spacer(Modifier.height(12.dp))

                        if (hasCheckIn) {
                            DarkFormField(
                                label = "Check-in начинается за (минут)",
                                value = checkInStartsBefore,
                                onValueChange = { checkInStartsBefore = it },
                                placeholder = "60",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════
                        // SECTION: Расписание
                        // ══════════════════════════════
                        SectionHeader("Расписание")
                        ScheduleEditor(
                            items = scheduleItems,
                            onItemsChange = { scheduleItems = it }
                        )

                        Spacer(Modifier.height(24.dp))

                        // ── Save button ──
                        val enabled = !isSaving && name.isNotBlank() && startDate.isNotBlank()
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
                                                    format = format.ifBlank { null },
                                                    matchFormat = matchFormat.ifBlank { null },
                                                    seedingType = seedingType.ifBlank { null },
                                                    visibility = visibility,
                                                    maxParticipants = maxParticipants.toIntOrNull(),
                                                    minParticipants = minParticipants.toIntOrNull(),
                                                    prize = prize.ifBlank { null },
                                                    ageCategory = ageCategory.ifBlank { null },
                                                    skillLevel = skillLevel.ifBlank { null },
                                                    genderCategory = genderCategory.ifBlank { null },
                                                    entryFee = entryFee.toDoubleOrNull(),
                                                    region = region.ifBlank { null },
                                                    groupCount = groupCount.toIntOrNull(),
                                                    hasThirdPlaceMatch = hasThirdPlaceMatch,
                                                    hasCheckIn = hasCheckIn,
                                                    accessCode = accessCode.ifBlank { null },
                                                    imageUrl = imageUrl,
                                                    registrationDeadline = registrationDeadline.ifBlank { null },
                                                    checkInStartsBefore = checkInStartsBefore.toIntOrNull(),
                                                    prizes = tournament.prizes,
                                                    schedule = if (scheduleItems.isNotEmpty()) {
                                                        kotlinx.serialization.json.Json.encodeToJsonElement(
                                                            kotlinx.serialization.builtins.ListSerializer(ScheduleItemDto.serializer()),
                                                            scheduleItems
                                                        )
                                                    } else null,
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

// ── Schedule Editor ──
@Composable
private fun ScheduleEditor(
    items: List<ScheduleItemDto>,
    onItemsChange: (List<ScheduleItemDto>) -> Unit
) {
    val colors = LocalAppColors.current
    var newTime by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Existing items
        items.forEachIndexed { idx, item ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(colors.cardBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Accent.copy(0.1f)
                ) {
                    Text(
                        item.time,
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp, color = Accent, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    item.title,
                    fontSize = 13.sp, color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.Close, null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onItemsChange(items.toMutableList().also { it.removeAt(idx) }) }
                )
            }
        }

        // Add new item
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.width(80.dp)
                    .background(colors.cardBg, RoundedCornerShape(10.dp))
                    .border(0.5.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = newTime,
                    onValueChange = { newTime = it.take(5) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp, color = colors.textPrimary
                    ),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (newTime.isEmpty()) {
                            Text("10:00", fontSize = 13.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                )
            }
            Box(
                Modifier.weight(1f)
                    .background(colors.cardBg, RoundedCornerShape(10.dp))
                    .border(0.5.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp, color = colors.textPrimary
                    ),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (newTitle.isEmpty()) {
                            Text("Регистрация участников", fontSize = 13.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                )
            }
            val canAdd = newTime.isNotBlank() && newTitle.isNotBlank()
            Box(
                Modifier.size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (canAdd) Accent else colors.textMuted.copy(0.3f))
                    .clickable(enabled = canAdd) {
                        onItemsChange(items + ScheduleItemDto(time = newTime, title = newTitle))
                        newTime = ""
                        newTitle = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.Add, null,
                    tint = Color.White, modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Section Header ──
@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTheme.TextPrimary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

// ── Dropdown Selector ──
@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<Pair<String, String>>
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == value }?.second ?: options.first().second

    Column {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
        }
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBg, RoundedCornerShape(12.dp))
                    .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedLabel,
                    fontSize = 14.sp,
                    color = if (value.isEmpty()) colors.textMuted else colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ExpandMore, null,
                    tint = colors.textMuted, modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colors.cardBg)
            ) {
                options.forEach { (key, displayName) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                displayName,
                                color = if (key == value) colors.accent else colors.textPrimary,
                                fontWeight = if (key == value) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueChange(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
