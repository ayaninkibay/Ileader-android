package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.*

@Composable
fun OrganizerTournamentEditScreen(
    tournamentId: String?,
    userId: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val vm: OrganizerTournamentEditViewModel = viewModel()
    val loadState by vm.loadState.collectAsState()
    val form by vm.form.collectAsState()
    val currentStep by vm.currentStep.collectAsState()
    val errors by vm.errors.collectAsState()
    val saveState by vm.saveState.collectAsState()

    LaunchedEffect(tournamentId, userId) { vm.load(tournamentId, userId) }
    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            vm.clearSaveState()
            onSave()
        }
    }

    when (val s = loadState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(tournamentId, userId) }
        is UiState.Success -> {
            WizardContent(
                tournamentId = tournamentId,
                form = form,
                currentStep = currentStep,
                errors = errors,
                sports = s.data.sports,
                locations = s.data.locations,
                saveState = saveState,
                onUpdateForm = { vm.updateForm(it) },
                onNext = { vm.goNext() },
                onPrev = { vm.goPrev() },
                onStepClick = { vm.goToStep(it) },
                onSave = { asDraft -> vm.save(tournamentId, asDraft) },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun WizardContent(
    tournamentId: String?,
    form: TournamentFormData,
    currentStep: WizardStep,
    errors: Map<String, String>,
    sports: List<SportDto>,
    locations: List<LocationDto>,
    saveState: UiState<Boolean>?,
    onUpdateForm: (TournamentFormData.() -> TournamentFormData) -> Unit,
    onNext: () -> Boolean,
    onPrev: () -> Unit,
    onStepClick: (WizardStep) -> Unit,
    onSave: (asDraft: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val isCreate = tournamentId == null
    val title = if (isCreate) "Создать турнир" else "Редактировать турнир"
    val isSaving = saveState is UiState.Loading

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height(8.dp))
            BackHeader(title, onBack)
            Spacer(Modifier.height(8.dp))

            WizardStepIndicator(
                steps = listOf(
                    "Основное" to Icons.Default.Description,
                    "Локация" to Icons.Default.LocationOn,
                    "Формат" to Icons.Default.AccountTree,
                    "Настройки" to Icons.Default.Settings,
                    "Превью" to Icons.Default.Visibility
                ),
                currentStep = currentStep.ordinal,
                onStepClick = { onStepClick(WizardStep.entries[it]) }
            )

            Spacer(Modifier.height(12.dp))

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                when (currentStep) {
                    WizardStep.BASIC -> Step1Basic(form, errors, sports, onUpdateForm)
                    WizardStep.LOCATION -> Step2Location(form, errors, locations, onUpdateForm)
                    WizardStep.FORMAT -> Step3Format(form, onUpdateForm)
                    WizardStep.SETTINGS -> Step4Settings(form, errors, onUpdateForm)
                    WizardStep.PREVIEW -> Step5Preview(form, sports, locations)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Bottom bar
            WizardBottomBar(
                currentStep = currentStep,
                isSaving = isSaving,
                saveError = (saveState as? UiState.Error)?.message,
                onPrev = onPrev,
                onNext = { onNext() },
                onSaveDraft = { onSave(true) },
                onPublish = { onSave(false) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP 1: ОСНОВНОЕ
// ═══════════════════════════════════════════════════════════

@Composable
private fun Step1Basic(
    form: TournamentFormData,
    errors: Map<String, String>,
    sports: List<SportDto>,
    onUpdate: (TournamentFormData.() -> TournamentFormData) -> Unit
) {
    DarkFormField(
        label = "Название турнира",
        value = form.name,
        onValueChange = { onUpdate { copy(name = it) } },
        placeholder = "Введите название",
        error = errors["name"]
    )

    FormDropdown(
        label = "Вид спорта",
        selectedValue = form.sportId,
        placeholder = "Выберите вид спорта",
        items = sports.map { it.id to it.name },
        onItemSelected = { onUpdate { copy(sportId = it) } },
        error = errors["sportId"]
    )

    DarkFormField(
        label = "Описание",
        value = form.description,
        onValueChange = { onUpdate { copy(description = it) } },
        placeholder = "Описание турнира",
        singleLine = false,
        minLines = 3
    )

    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        DatePickerField(
            label = "Дата начала",
            value = form.startDate,
            onValueChange = { onUpdate { copy(startDate = it) } },
            error = errors["startDate"],
            modifier = Modifier.weight(1f)
        )
        DatePickerField(
            label = "Дата окончания",
            value = form.endDate,
            onValueChange = { onUpdate { copy(endDate = it) } },
            error = errors["endDate"],
            modifier = Modifier.weight(1f)
        )
    }

    // Visibility
    SectionLabel("Видимость")
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        DarkToggleCard(
            title = "Публичный",
            description = "Виден всем",
            icon = Icons.Default.Public,
            selected = form.visibility == "public",
            onClick = { onUpdate { copy(visibility = "public") } },
            modifier = Modifier.weight(1f)
        )
        DarkToggleCard(
            title = "Приватный",
            description = "По коду/ссылке",
            icon = Icons.Default.Lock,
            selected = form.visibility == "private",
            onClick = { onUpdate { copy(visibility = "private") } },
            modifier = Modifier.weight(1f)
        )
    }

    if (form.visibility == "private") {
        AccessCodeSection(form.accessCode) { onUpdate { copy(accessCode = it) } }
    }

    DarkFormField(
        label = "URL фото турнира",
        value = form.imageUrl,
        onValueChange = { onUpdate { copy(imageUrl = it) } },
        placeholder = "https://..."
    )
}

@Composable
private fun AccessCodeSection(code: String, onRegenerate: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    DarkCardPadded(padding = 12.dp) {
        Text("Код доступа", fontSize = 13.sp, color = DarkTheme.TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                code,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTheme.Accent,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                clipboard.setText(AnnotatedString(code))
                copied = true
            }, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    null, tint = DarkTheme.TextMuted, modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = {
                onRegenerate(generateAccessCode())
                copied = false
            }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Refresh, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP 2: ЛОКАЦИЯ
// ═══════════════════════════════════════════════════════════

@Composable
private fun Step2Location(
    form: TournamentFormData,
    errors: Map<String, String>,
    locations: List<LocationDto>,
    onUpdate: (TournamentFormData.() -> TournamentFormData) -> Unit
) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        DarkToggleCard(
            title = "Существующая",
            description = "Выбрать из локаций",
            icon = Icons.Default.LocationOn,
            selected = form.locationMode == "existing",
            onClick = { onUpdate { copy(locationMode = "existing") } },
            modifier = Modifier.weight(1f)
        )
        DarkToggleCard(
            title = "Новая",
            description = "Создать локацию",
            icon = Icons.Default.Add,
            selected = form.locationMode == "new",
            onClick = { onUpdate { copy(locationMode = "new") } },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(8.dp))

    if (form.locationMode == "existing") {
        FormDropdown(
            label = "Локация",
            selectedValue = form.locationId,
            placeholder = "Выберите локацию",
            items = locations.map { (it.id ?: "") to "${it.name} — ${it.city ?: ""}" },
            onItemSelected = { onUpdate { copy(locationId = it) } },
            error = errors["locationId"]
        )
    } else {
        DarkCardPadded {
            DarkFormField(
                label = "Название",
                value = form.newLocationName,
                onValueChange = { onUpdate { copy(newLocationName = it) } },
                placeholder = "Автодром \"Скорость\"",
                error = errors["newLocationName"]
            )
            Spacer(Modifier.height(12.dp))

            val locationTypes = listOf(
                "karting" to "Автодром", "shooting" to "Тир",
                "tennis" to "Теннисный корт", "stadium" to "Стадион",
                "arena" to "Арена", "other" to "Другое"
            )
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                FormDropdown(
                    label = "Тип",
                    selectedValue = form.newLocationType,
                    placeholder = "Тип",
                    items = locationTypes,
                    onItemSelected = { onUpdate { copy(newLocationType = it) } },
                    modifier = Modifier.weight(1f)
                )
                DarkFormField(
                    label = "Город",
                    value = form.newLocationCity,
                    onValueChange = { onUpdate { copy(newLocationCity = it) } },
                    placeholder = "Алматы",
                    error = errors["newLocationCity"],
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            DarkFormField(
                label = "Адрес",
                value = form.newLocationAddress,
                onValueChange = { onUpdate { copy(newLocationAddress = it) } },
                placeholder = "ул. Спортивная, 1"
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP 3: ФОРМАТ
// ═══════════════════════════════════════════════════════════

@Composable
private fun Step3Format(
    form: TournamentFormData,
    onUpdate: (TournamentFormData.() -> TournamentFormData) -> Unit
) {
    val hasGroups = form.format == "groups_single_elim" || form.format == "groups_double_elim"

    SectionLabel("Формат турнира")
    val formats = listOf(
        Triple("single_elimination", "Одиночное выбывание", "Проиграл — вылетел"),
        Triple("double_elimination", "Двойное выбывание", "Два поражения — вылет"),
        Triple("round_robin", "Круговая система", "Все играют со всеми"),
        Triple("groups_single_elim", "Группы + Плей-офф", "Группы → выбывание"),
        Triple("groups_double_elim", "Группы + Двойная сетка", "Группы → двойное выбывание")
    )
    val formatIcons = listOf(Icons.Default.Bolt, Icons.Default.AccountTree, Icons.Default.Sync, Icons.Default.GridView, Icons.Default.ViewModule)

    formats.chunked(2).forEachIndexed { rowIdx, row ->
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
            row.forEachIndexed { colIdx, (value, label, desc) ->
                DarkToggleCard(
                    title = label,
                    description = desc,
                    icon = formatIcons[rowIdx * 2 + colIdx],
                    selected = form.format == value,
                    onClick = { onUpdate { copy(format = value) } },
                    modifier = Modifier.weight(1f)
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }

    // Group count
    if (hasGroups) {
        Spacer(Modifier.height(8.dp))
        SectionLabel("Количество групп")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4).forEach { count ->
                DarkFilterChip(
                    "$count группы",
                    form.groupCount == count,
                    { onUpdate { copy(groupCount = count) } }
                )
            }
        }
    }

    // Match format
    Spacer(Modifier.height(8.dp))
    SectionLabel("Формат матчей")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
        listOf("BO1" to "BO1", "BO2" to "BO2", "BO3" to "BO3", "BO5" to "BO5").forEach { (value, label) ->
            DarkFilterChip(label, form.matchFormat == value, { onUpdate { copy(matchFormat = value) } })
        }
    }

    // Stage formats
    Spacer(Modifier.height(8.dp))
    DarkSwitchField(
        label = "Разный формат по стадиям",
        description = "Например: BO2 в группах, BO3 в финале",
        checked = form.useStageFormats,
        onCheckedChange = { onUpdate { copy(useStageFormats = it) } }
    )

    if (form.useStageFormats) {
        Spacer(Modifier.height(8.dp))
        DarkCardPadded(padding = 12.dp) {
            StageFormatRow("Обычные раунды", form.stageFormatsDefault) { onUpdate { copy(stageFormatsDefault = it) } }
            if (hasGroups) {
                Spacer(Modifier.height(8.dp))
                StageFormatRow("Групповой этап", form.stageFormatsGroupStage) { onUpdate { copy(stageFormatsGroupStage = it) } }
            }
            Spacer(Modifier.height(8.dp))
            StageFormatRow("Полуфинал", form.stageFormatsSemiFinal) { onUpdate { copy(stageFormatsSemiFinal = it) } }
            Spacer(Modifier.height(8.dp))
            StageFormatRow("Финал", form.stageFormatsFinal) { onUpdate { copy(stageFormatsFinal = it) } }
            if (form.hasThirdPlaceMatch) {
                Spacer(Modifier.height(8.dp))
                StageFormatRow("За 3-е место", form.stageFormatsThirdPlace) { onUpdate { copy(stageFormatsThirdPlace = it) } }
            }
            if (form.format == "double_elimination" || form.format == "groups_double_elim") {
                Spacer(Modifier.height(8.dp))
                StageFormatRow("Grand Final", form.stageFormatsGrandFinal) { onUpdate { copy(stageFormatsGrandFinal = it) } }
            }
        }
    }

    // Seeding type
    Spacer(Modifier.height(8.dp))
    SectionLabel("Тип посева")
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        DarkToggleCard(
            "Случайный", "Случайное", Icons.Default.Shuffle,
            form.seedingType == "random", { onUpdate { copy(seedingType = "random") } }, Modifier.weight(1f)
        )
        DarkToggleCard(
            "По рейтингу", "Сильнейшие разводятся", Icons.Default.BarChart,
            form.seedingType == "rating", { onUpdate { copy(seedingType = "rating") } }, Modifier.weight(1f)
        )
        DarkToggleCard(
            "Ручной", "Вы определяете", Icons.Default.PanTool,
            form.seedingType == "manual", { onUpdate { copy(seedingType = "manual") } }, Modifier.weight(1f)
        )
    }

    // Third place match
    if (form.format == "single_elimination") {
        Spacer(Modifier.height(8.dp))
        DarkSwitchField(
            label = "Матч за 3-е место",
            description = "Проигравшие полуфиналов играют за бронзу",
            checked = form.hasThirdPlaceMatch,
            onCheckedChange = { onUpdate { copy(hasThirdPlaceMatch = it) } }
        )
    }
}

@Composable
private fun StageFormatRow(label: String, selected: String, onChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = DarkTheme.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("BO1", "BO2", "BO3", "BO5").forEach { v ->
                DarkFilterChip(v, selected == v, { onChange(v) })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP 4: НАСТРОЙКИ
// ═══════════════════════════════════════════════════════════

@Composable
private fun Step4Settings(
    form: TournamentFormData,
    errors: Map<String, String>,
    onUpdate: (TournamentFormData.() -> TournamentFormData) -> Unit
) {
    // Participants
    SectionLabel("Участники")
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        DarkFormField(
            label = "Мин. участников",
            value = form.minParticipants,
            onValueChange = { onUpdate { copy(minParticipants = it) } },
            placeholder = "4",
            error = errors["minParticipants"],
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number
        )
        DarkFormField(
            label = "Макс. участников",
            value = form.maxParticipants,
            onValueChange = { onUpdate { copy(maxParticipants = it) } },
            placeholder = "16",
            error = errors["maxParticipants"],
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number
        )
    }

    DatePickerField(
        label = "Дедлайн регистрации",
        value = form.registrationDeadline,
        onValueChange = { onUpdate { copy(registrationDeadline = it) } },
        error = errors["registrationDeadline"]
    )

    // Check-in
    DarkSwitchField(
        label = "Check-in перед турниром",
        description = "Участники подтверждают участие",
        checked = form.hasCheckIn,
        onCheckedChange = { onUpdate { copy(hasCheckIn = it) } }
    )

    if (form.hasCheckIn) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
            listOf(30, 60, 120).forEach { mins ->
                DarkFilterChip(
                    "$mins мин",
                    form.checkInStartsBefore == mins,
                    { onUpdate { copy(checkInStartsBefore = mins) } }
                )
            }
        }
    }

    // Age category & Prize
    val ageCategories = listOf(
        "" to "Любая",
        "children" to "Детская (до 12)",
        "youth" to "Юношеская (12-17)",
        "adult" to "Взрослая (18+)"
    )
    FormDropdown(
        label = "Возрастная категория",
        selectedValue = form.ageCategory,
        placeholder = "Любая",
        items = ageCategories,
        onItemSelected = { onUpdate { copy(ageCategory = it) } }
    )

    DarkFormField(
        label = "Призовой фонд",
        value = form.prize,
        onValueChange = { onUpdate { copy(prize = it) } },
        placeholder = "500 000 ₸"
    )

    // Dynamic lists
    DynamicListField(
        label = "Категории",
        items = form.categories,
        onItemsChange = { onUpdate { copy(categories = it) } },
        placeholder = "Например: Senior (16+)"
    )

    DynamicListField(
        label = "Призы",
        items = form.prizes,
        onItemsChange = { onUpdate { copy(prizes = it) } },
        placeholder = "Например: 1 место — 200 000 ₸"
    )

    DynamicListField(
        label = "Требования к участникам",
        items = form.requirements,
        onItemsChange = { onUpdate { copy(requirements = it) } },
        placeholder = "Например: Гоночная лицензия"
    )

    // Schedule
    ScheduleSection(form.schedule) { onUpdate { copy(schedule = it) } }
}

@Composable
private fun ScheduleSection(
    items: List<ScheduleItemDto>,
    onChange: (List<ScheduleItemDto>) -> Unit
) {
    var newTime by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Расписание", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextSecondary)

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkTheme.CardBg, RoundedCornerShape(10.dp))
                    .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, null, tint = DarkTheme.Accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(item.time, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text(item.title, Modifier.weight(1f), fontSize = 13.sp, color = DarkTheme.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(
                    onClick = { onChange(items.toMutableList().also { it.removeAt(index) }) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, "Удалить", tint = DarkTheme.TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.Bottom) {
            DarkFormField(
                label = "Время",
                value = newTime,
                onValueChange = { newTime = it },
                placeholder = "10:00",
                modifier = Modifier.width(80.dp)
            )
            DarkFormField(
                label = "Событие",
                value = newTitle,
                onValueChange = { newTitle = it },
                placeholder = "Регистрация участников",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newTime.isNotBlank() && newTitle.isNotBlank()) {
                        onChange(items + ScheduleItemDto(newTime.trim(), newTitle.trim()))
                        newTime = ""
                        newTitle = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP 5: ПРЕВЬЮ
// ═══════════════════════════════════════════════════════════

@Composable
private fun Step5Preview(
    form: TournamentFormData,
    sports: List<SportDto>,
    locations: List<LocationDto>
) {
    val sportName = sports.find { it.id == form.sportId }?.name ?: "—"
    val locationName = if (form.locationMode == "existing") {
        locations.find { it.id == form.locationId }?.let { "${it.name}, ${it.city ?: ""}" } ?: "—"
    } else if (form.newLocationName.isNotBlank()) {
        "${form.newLocationName}, ${form.newLocationCity}"
    } else "—"

    val formatLabels = mapOf(
        "single_elimination" to "Одиночное выбывание",
        "double_elimination" to "Двойное выбывание",
        "round_robin" to "Круговая система",
        "groups_single_elim" to "Группы + Плей-офф",
        "groups_double_elim" to "Группы + Двойная сетка"
    )
    val seedingLabels = mapOf("random" to "Случайный", "rating" to "По рейтингу", "manual" to "Ручной")
    val ageLabels = mapOf("children" to "Детская", "youth" to "Юношеская", "adult" to "Взрослая")

    DarkCardPadded {
        PreviewRow("Название", form.name.ifEmpty { "—" })
        PreviewRow("Вид спорта", sportName)
        PreviewRow("Локация", locationName)
        PreviewRow("Даты", if (form.startDate.isNotEmpty()) "${form.startDate} — ${form.endDate}" else "—")
        PreviewRow("Видимость", if (form.visibility == "private") "Приватный" else "Публичный")
        if (form.visibility == "private") PreviewRow("Код доступа", form.accessCode)
    }

    DarkCardPadded {
        PreviewRow("Формат", formatLabels[form.format] ?: form.format)
        PreviewRow("Матчи", form.matchFormat)
        PreviewRow("Посев", seedingLabels[form.seedingType] ?: form.seedingType)
        val participantsText = buildString {
            if (form.minParticipants.isNotBlank()) append("${form.minParticipants} — ")
            append(form.maxParticipants.ifBlank { "—" })
        }
        PreviewRow("Участники", participantsText)
        PreviewRow("Check-in", if (form.hasCheckIn) "Да, за ${form.checkInStartsBefore} мин" else "Нет")
        PreviewRow("Призовой фонд", form.prize.ifEmpty { "—" })
        if (form.ageCategory.isNotBlank()) PreviewRow("Возраст", ageLabels[form.ageCategory] ?: form.ageCategory)
    }

    if (form.description.isNotBlank()) {
        DarkCardPadded {
            Text("Описание", fontSize = 13.sp, color = DarkTheme.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(form.description, fontSize = 14.sp, color = DarkTheme.TextPrimary, lineHeight = 20.sp)
        }
    }

    if (form.categories.isNotEmpty()) {
        PreviewListSection("Категории", form.categories)
    }
    if (form.requirements.isNotEmpty()) {
        PreviewListSection("Требования", form.requirements)
    }
    if (form.prizes.isNotEmpty()) {
        PreviewListSection("Призы", form.prizes)
    }
    if (form.schedule.isNotEmpty()) {
        DarkCardPadded {
            Text("Расписание", fontSize = 13.sp, color = DarkTheme.TextSecondary)
            Spacer(Modifier.height(8.dp))
            form.schedule.forEach { item ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text(item.time, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                    Spacer(Modifier.width(12.dp))
                    Text(item.title, Modifier.weight(1f), fontSize = 13.sp, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = DarkTheme.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
    }
}

@Composable
private fun PreviewListSection(title: String, items: List<String>) {
    DarkCardPadded {
        Text(title, fontSize = 13.sp, color = DarkTheme.TextSecondary)
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(Modifier.padding(vertical = 2.dp)) {
                Text("•", fontSize = 13.sp, color = DarkTheme.Accent)
                Spacer(Modifier.width(8.dp))
                Text(item, fontSize = 13.sp, color = DarkTheme.TextPrimary)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM BAR
// ═══════════════════════════════════════════════════════════

@Composable
private fun WizardBottomBar(
    currentStep: WizardStep,
    isSaving: Boolean,
    saveError: String?,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    Column {
        if (saveError != null) {
            Text(
                saveError,
                color = ILeaderColors.LightRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider(color = DarkTheme.CardBorder, thickness = 0.5.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .background(DarkTheme.Bg)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep != WizardStep.BASIC) {
                TextButton(onClick = onPrev) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp), tint = DarkTheme.TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("Назад", color = DarkTheme.TextSecondary)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            if (currentStep == WizardStep.PREVIEW) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSaveDraft,
                        enabled = !isSaving,
                        border = BorderStroke(1.dp, DarkTheme.CardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = DarkTheme.TextSecondary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Черновик", color = DarkTheme.TextSecondary, fontSize = 13.sp)
                    }
                    Button(
                        onClick = onPublish,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.RocketLaunch, null, Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Опубликовать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Далее", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
}
