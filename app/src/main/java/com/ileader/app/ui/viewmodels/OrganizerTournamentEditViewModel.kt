package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

data class TournamentEditData(
    val tournament: TournamentDto?,
    val sports: List<SportDto>,
    val locations: List<LocationDto>
)

enum class WizardStep(val title: String) {
    BASIC("Основное"),
    LOCATION("Локация"),
    FORMAT("Формат"),
    SETTINGS("Настройки"),
    PREVIEW("Превью")
}

data class TournamentFormData(
    // Step 1: Basic
    val name: String = "",
    val sportId: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val visibility: String = "public",
    val accessCode: String = generateAccessCode(),
    val imageUrl: String = "",
    // Step 2: Location
    val locationMode: String = "existing",
    val locationId: String = "",
    val newLocationName: String = "",
    val newLocationType: String = "",
    val newLocationCity: String = "",
    val newLocationAddress: String = "",
    // Step 3: Format
    val format: String = "single_elimination",
    val matchFormat: String = "BO1",
    val seedingType: String = "rating",
    val groupCount: Int = 4,
    val hasThirdPlaceMatch: Boolean = false,
    val useStageFormats: Boolean = false,
    val stageFormatsDefault: String = "BO1",
    val stageFormatsSemiFinal: String = "BO1",
    val stageFormatsFinal: String = "BO1",
    val stageFormatsThirdPlace: String = "BO1",
    val stageFormatsGrandFinal: String = "BO1",
    val stageFormatsGroupStage: String = "BO1",
    // Step 4: Settings
    val minParticipants: String = "",
    val maxParticipants: String = "",
    val registrationDeadline: String = "",
    val hasCheckIn: Boolean = false,
    val checkInStartsBefore: Int = 60,
    val ageCategory: String = "",
    val prize: String = "",
    val requirements: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val prizes: List<String> = emptyList(),
    val schedule: List<ScheduleItemDto> = emptyList()
)

fun generateAccessCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = java.security.SecureRandom()
    return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
}

class OrganizerTournamentEditViewModel : ViewModel() {

    private val repo = OrganizerRepository()
    private var userId: String = ""

    private val _loadState = MutableStateFlow<UiState<TournamentEditData>>(UiState.Loading)
    val loadState: StateFlow<UiState<TournamentEditData>> = _loadState.asStateFlow()

    private val _form = MutableStateFlow(TournamentFormData())
    val form: StateFlow<TournamentFormData> = _form.asStateFlow()

    private val _currentStep = MutableStateFlow(WizardStep.BASIC)
    val currentStep: StateFlow<WizardStep> = _currentStep.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Boolean>?>(null)
    val saveState: StateFlow<UiState<Boolean>?> = _saveState.asStateFlow()

    fun updateForm(update: TournamentFormData.() -> TournamentFormData) {
        _form.value = _form.value.update()
        _errors.value = emptyMap()
    }

    fun goNext(): Boolean {
        if (!validateStep(_currentStep.value)) return false
        val next = _currentStep.value.ordinal + 1
        if (next < WizardStep.entries.size) {
            _currentStep.value = WizardStep.entries[next]
        }
        return true
    }

    fun goPrev() {
        val prev = _currentStep.value.ordinal - 1
        if (prev >= 0) {
            _currentStep.value = WizardStep.entries[prev]
        }
    }

    fun goToStep(step: WizardStep) {
        if (step.ordinal < _currentStep.value.ordinal) {
            _currentStep.value = step
        }
    }

    fun validateStep(step: WizardStep): Boolean {
        val errs = mutableMapOf<String, String>()
        val f = _form.value
        when (step) {
            WizardStep.BASIC -> {
                if (f.name.isBlank()) errs["name"] = "Введите название"
                if (f.sportId.isBlank()) errs["sportId"] = "Выберите вид спорта"
                if (f.startDate.isBlank()) errs["startDate"] = "Укажите дату начала"
                if (f.endDate.isBlank()) errs["endDate"] = "Укажите дату окончания"
                if (f.startDate.isNotBlank() && f.endDate.isNotBlank() && f.endDate < f.startDate) {
                    errs["endDate"] = "Дата окончания раньше начала"
                }
            }
            WizardStep.LOCATION -> {
                if (f.locationMode == "existing" && f.locationId.isBlank()) {
                    errs["locationId"] = "Выберите локацию"
                }
                if (f.locationMode == "new") {
                    if (f.newLocationName.isBlank()) errs["newLocationName"] = "Введите название"
                    if (f.newLocationCity.isBlank()) errs["newLocationCity"] = "Укажите город"
                }
            }
            WizardStep.FORMAT -> { /* no required fields */ }
            WizardStep.SETTINGS -> {
                val max = f.maxParticipants.toIntOrNull()
                if (max == null || max < 2) errs["maxParticipants"] = "Минимум 2 участника"
                val min = f.minParticipants.toIntOrNull()
                if (min != null && max != null && min > max) {
                    errs["minParticipants"] = "Мин. > макс. участников"
                }
                if (f.registrationDeadline.isNotBlank() && f.startDate.isNotBlank() && f.registrationDeadline > f.startDate) {
                    errs["registrationDeadline"] = "Дедлайн не может быть позже даты начала"
                }
                val hasGroups = f.format == "groups_single_elim" || f.format == "groups_double_elim"
                if (hasGroups && max != null) {
                    val groupMin = f.groupCount * 2
                    if (max < groupMin) errs["maxParticipants"] = "Для ${f.groupCount} групп нужно мин. $groupMin участников"
                }
            }
            WizardStep.PREVIEW -> { /* no validation */ }
        }
        _errors.value = errs
        return errs.isEmpty()
    }

    fun load(tournamentId: String?, userId: String) {
        this.userId = userId
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            try {
                val sports = repo.getSports()
                val locations = repo.getMyLocations(userId)
                val tournament = if (tournamentId != null) {
                    repo.getTournamentDetail(tournamentId)
                } else null

                if (tournament != null) {
                    _form.value = mapTournamentToForm(tournament)
                }

                _loadState.value = UiState.Success(
                    TournamentEditData(tournament, sports, locations)
                )
            } catch (e: Exception) {
                _loadState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    private fun mapTournamentToForm(t: TournamentDto): TournamentFormData {
        // Parse stageMatchFormats from JsonElement
        var stageDefault = "BO1"
        var stageSemi = "BO1"
        var stageFinal = "BO1"
        var stageThird = "BO1"
        var stageGrand = "BO1"
        var stageGroup = "BO1"
        var useStage = false

        try {
            val smf = t.stageMatchFormats
            if (smf != null) {
                val obj = smf.jsonObject
                useStage = true
                stageDefault = obj["default"]?.jsonPrimitive?.content ?: "BO1"
                stageSemi = obj["semiFinal"]?.jsonPrimitive?.content ?: "BO1"
                stageFinal = obj["final"]?.jsonPrimitive?.content ?: "BO1"
                stageThird = obj["thirdPlace"]?.jsonPrimitive?.content ?: "BO1"
                stageGrand = obj["grandFinal"]?.jsonPrimitive?.content ?: "BO1"
                stageGroup = obj["groupStage"]?.jsonPrimitive?.content ?: "BO1"
            }
        } catch (_: Exception) {}

        // Parse schedule from JsonElement
        val scheduleItems: List<ScheduleItemDto> = try {
            if (t.schedule != null) {
                Json.decodeFromJsonElement(t.schedule)
            } else emptyList()
        } catch (_: Exception) { emptyList() }

        return TournamentFormData(
            name = t.name,
            sportId = t.sportId ?: "",
            description = t.description ?: "",
            startDate = t.startDate ?: "",
            endDate = t.endDate ?: "",
            visibility = t.visibility ?: "public",
            accessCode = t.accessCode ?: generateAccessCode(),
            imageUrl = t.imageUrl ?: "",
            locationMode = if (t.locationId != null) "existing" else "none",
            locationId = t.locationId ?: "",
            format = t.format ?: "single_elimination",
            matchFormat = t.matchFormat ?: "BO1",
            seedingType = t.seedingType ?: "rating",
            groupCount = t.groupCount ?: 4,
            hasThirdPlaceMatch = t.hasThirdPlaceMatch ?: false,
            useStageFormats = useStage,
            stageFormatsDefault = stageDefault,
            stageFormatsSemiFinal = stageSemi,
            stageFormatsFinal = stageFinal,
            stageFormatsThirdPlace = stageThird,
            stageFormatsGrandFinal = stageGrand,
            stageFormatsGroupStage = stageGroup,
            minParticipants = t.minParticipants?.toString() ?: "",
            maxParticipants = t.maxParticipants?.toString() ?: "",
            registrationDeadline = t.registrationDeadline ?: "",
            hasCheckIn = t.hasCheckIn ?: false,
            checkInStartsBefore = t.checkInStartsBefore ?: 60,
            ageCategory = t.ageCategory ?: "",
            prize = t.prize ?: "",
            requirements = t.requirements ?: emptyList(),
            categories = t.categories ?: emptyList(),
            prizes = t.prizes ?: emptyList(),
            schedule = scheduleItems
        )
    }

    fun save(tournamentId: String?, asDraft: Boolean) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                val f = _form.value

                // Create new location if needed
                var finalLocationId = if (f.locationMode == "existing") f.locationId.ifEmpty { null } else null
                if (f.locationMode == "new" && f.newLocationName.isNotBlank()) {
                    val locId = repo.createLocation(
                        LocationInsertDto(
                            name = f.newLocationName,
                            type = f.newLocationType.ifEmpty { "other" },
                            city = f.newLocationCity.ifEmpty { null },
                            address = f.newLocationAddress.ifEmpty { null },
                            ownerId = userId
                        )
                    )
                    finalLocationId = locId
                }

                // Build schedule JsonElement
                val scheduleJson: JsonElement? = if (f.schedule.isNotEmpty()) {
                    Json.encodeToJsonElement(f.schedule)
                } else null

                // Build stageMatchFormats JsonElement
                val stageFormatsJson: JsonElement? = if (f.useStageFormats) {
                    buildJsonObject {
                        put("default", f.stageFormatsDefault)
                        put("semiFinal", f.stageFormatsSemiFinal)
                        put("final", f.stageFormatsFinal)
                        if (f.hasThirdPlaceMatch) put("thirdPlace", f.stageFormatsThirdPlace)
                        val hasGroups = f.format == "groups_single_elim" || f.format == "groups_double_elim"
                        if (f.format == "double_elimination" || f.format == "groups_double_elim") {
                            put("grandFinal", f.stageFormatsGrandFinal)
                        }
                        if (hasGroups) put("groupStage", f.stageFormatsGroupStage)
                    }
                } else null

                val dto = TournamentInsertDto(
                    name = f.name,
                    sportId = f.sportId,
                    locationId = finalLocationId,
                    organizerId = userId,
                    status = if (asDraft) "draft" else "registration_open",
                    startDate = f.startDate,
                    endDate = f.endDate.ifEmpty { null },
                    description = f.description.ifEmpty { null },
                    format = f.format,
                    matchFormat = if (f.useStageFormats) f.stageFormatsDefault else f.matchFormat,
                    seedingType = f.seedingType,
                    visibility = f.visibility,
                    accessCode = if (f.visibility == "private") f.accessCode else null,
                    imageUrl = f.imageUrl.ifEmpty { null },
                    maxParticipants = f.maxParticipants.toIntOrNull(),
                    minParticipants = f.minParticipants.toIntOrNull(),
                    prize = f.prize.ifEmpty { null },
                    prizes = f.prizes.ifEmpty { null },
                    requirements = f.requirements.ifEmpty { null },
                    categories = f.categories.ifEmpty { null },
                    ageCategory = f.ageCategory.ifEmpty { null },
                    groupCount = if (f.format.startsWith("groups_")) f.groupCount else null,
                    hasThirdPlaceMatch = f.hasThirdPlaceMatch,
                    hasCheckIn = f.hasCheckIn,
                    registrationDeadline = f.registrationDeadline.ifEmpty { null },
                    checkInStartsBefore = if (f.hasCheckIn) f.checkInStartsBefore else null,
                    schedule = scheduleJson,
                    stageMatchFormats = stageFormatsJson
                )

                if (tournamentId != null) {
                    repo.updateTournament(tournamentId, dto)
                } else {
                    repo.createTournament(dto)
                }
                _saveState.value = UiState.Success(true)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = null
    }
}
