# ТЗ БД: Чат #1 — Athlete (Спортсмен)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Athlete к Supabase. Заменить `AthleteMockData` на реальные запросы.

---

## Таблицы (чтение / запись)

| Таблица | Чтение | Запись | Описание |
|---------|:------:|:------:|----------|
| `profiles` | R | W | Профиль спортсмена |
| `user_sports` | R | W | Спорт + рейтинг |
| `sports` | R | — | Справочник видов спорта |
| `tournaments` | R | — | Турниры |
| `tournament_participants` | R | W | Регистрация на турниры |
| `tournament_results` | R | — | Результаты |
| `athlete_goals` | R | W | Цели спортсмена |
| `teams` | R | — | Команда (если есть teamId) |
| `team_members` | R | — | Участники команды |
| `team_requests` | R | W | Заявки в команду |
| `tournament_invites` | R | W | Приглашения на турниры |
| `tournament_invite_codes` | R | — | Инвайт-коды |
| `v_tournament_with_counts` | R | — | View с турнирами + counts |
| `v_user_sport_stats` | R | — | Статистика по спортам |

---

## Файлы для создания

### 1. `data/repository/AthleteRepository.kt`

```kotlin
class AthleteRepository {
    private val client = SupabaseModule.client

    // ── ПРОФИЛЬ ──

    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)
    suspend fun getSports(userId: String): List<UserSportWithNameDto>
    suspend fun updateSports(userId: String, sportIds: List<String>)

    // ── ТУРНИРЫ ──

    suspend fun getMyTournaments(userId: String): List<TournamentWithDetailsDto>
    suspend fun getAvailableTournaments(): List<TournamentWithDetailsDto>
    suspend fun getTournamentDetail(tournamentId: String): TournamentFullDto
    suspend fun registerForTournament(tournamentId: String, userId: String)
    suspend fun cancelRegistration(tournamentId: String, userId: String)
    suspend fun joinByInviteCode(code: String): String // returns tournament_id

    // ── РЕЗУЛЬТАТЫ ──

    suspend fun getMyResults(userId: String): List<TournamentResultDto>
    suspend fun getResultsBySport(userId: String, sportId: String): List<TournamentResultDto>
    suspend fun getStats(userId: String): AthleteStatsData

    // ── ЦЕЛИ ──

    suspend fun getGoals(userId: String): List<GoalDto>
    suspend fun createGoal(goal: GoalInsertDto)
    suspend fun updateGoal(goalId: String, data: GoalUpdateDto)
    suspend fun deleteGoal(goalId: String)

    // ── КОМАНДА ──

    suspend fun getTeam(teamId: String): TeamWithMembersDto
    suspend fun getTeamRequests(userId: String): List<TeamRequestDto>
    suspend fun respondToTeamRequest(requestId: String, accept: Boolean)

    // ── УВЕДОМЛЕНИЯ ──

    suspend fun getTournamentInvites(userId: String): List<TournamentInviteDto>
    suspend fun respondToInvite(inviteId: String, accept: Boolean)
}
```

### 2. DTO (в `data/remote/dto/` или в начале Repository)

```kotlin
@Serializable
data class ProfileDto(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    @SerialName("age_category") val ageCategory: String? = null,
    val verification: String? = null,
    val status: String? = null,
    @SerialName("role_ids") val roleIds: List<String>? = null,
    @SerialName("primary_role_id") val primaryRoleId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class GoalDto(
    val id: String,
    @SerialName("athlete_id") val athleteId: String,
    val type: String, // rating/tournament/points
    val title: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null, // athlete/trainer
    val status: String, // active/completed/failed/paused
    val progress: Int = 0,
    val deadline: String? = null,
    @SerialName("target_rating") val targetRating: Int? = null,
    @SerialName("target_wins") val targetWins: Int? = null,
    @SerialName("target_podiums") val targetPodiums: Int? = null,
    @SerialName("target_points") val targetPoints: Int? = null,
    @SerialName("current_wins") val currentWins: Int? = null,
    @SerialName("current_podiums") val currentPodiums: Int? = null,
    @SerialName("current_points") val currentPoints: Int? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class GoalInsertDto(
    @SerialName("athlete_id") val athleteId: String,
    val type: String,
    val title: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String = "athlete",
    @SerialName("created_by_id") val createdById: String,
    val status: String = "active",
    val progress: Int = 0,
    val deadline: String? = null,
    @SerialName("target_rating") val targetRating: Int? = null,
    @SerialName("target_wins") val targetWins: Int? = null,
    @SerialName("target_podiums") val targetPodiums: Int? = null,
    @SerialName("target_points") val targetPoints: Int? = null,
    @SerialName("sport_id") val sportId: String? = null
)

@Serializable
data class TournamentResultDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    val position: Int,
    val points: Int? = null,
    val time: String? = null,
    val penalty: String? = null,
    val category: String? = null,
    val notes: String? = null
)
```

### 3. ViewModels

Создай по одному ViewModel на группу экранов:

```
ui/viewmodels/
  AthleteDashboardViewModel.kt   → Dashboard
  AthleteTournamentsViewModel.kt  → Tournaments + TournamentDetail
  AthleteResultsViewModel.kt      → Results
  AthleteGoalsViewModel.kt        → Goals
  AthleteProfileViewModel.kt      → Profile
  AthleteTeamViewModel.kt         → Team
  AthleteNotificationsViewModel.kt → Notifications
```

---

## Экран → Запросы (маппинг)

### AthleteDashboardScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.stats` | `v_user_sport_stats` WHERE user_id + агрегация по всем видам спорта |
| `AthleteMockData.upcomingTournaments` | `tournament_participants` JOIN `tournaments` WHERE athlete_id = userId AND status IN ('registration_open', 'in_progress') |
| `AthleteMockData.recentResults` | `tournament_results` JOIN `tournaments` WHERE athlete_id = userId ORDER BY created_at DESC LIMIT 5 |
| `AthleteMockData.ratingHistory` | `v_user_sport_stats` (только текущий рейтинг; историю можно пока хардкодить или пропустить) |

**Запросы:**
```kotlin
// Статистика
val stats = client.from("v_user_sport_stats")
    .select { filter { eq("user_id", userId) } }
    .decodeList<UserSportStatsDto>()

// Мои турниры (предстоящие)
val upcoming = client.from("tournament_participants")
    .select(Columns.raw("*, tournaments(*, sports(name), locations(name, city))"))
    {
        filter {
            eq("athlete_id", userId)
            neq("status", "cancelled")
        }
    }
    .decodeList<ParticipantWithTournamentDto>()
    .filter { it.tournament.status in listOf("registration_open", "registration_closed", "check_in", "in_progress") }

// Недавние результаты
val results = client.from("tournament_results")
    .select(Columns.raw("*, tournaments(name, sport_id, sports(name))"))
    {
        filter { eq("athlete_id", userId) }
        order("tournament_id", Order.DESCENDING)
        limit(5)
    }
    .decodeList<ResultWithTournamentDto>()
```

### AthleteTournamentsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.tournaments` (мои) | `tournament_participants` JOIN `v_tournament_with_counts` WHERE athlete_id = userId |
| Все турниры (доступные) | `v_tournament_with_counts` WHERE status = 'registration_open' AND visibility = 'public' |

### AthleteTournamentDetailScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| Инфо о турнире | `tournaments` JOIN `sports`, `locations`, `profiles!organizer_id` WHERE id = tournamentId |
| Участники | `tournament_participants` JOIN `profiles` WHERE tournament_id |
| Мой статус | `tournament_participants` WHERE tournament_id AND athlete_id = userId |
| Расписание | `tournaments.schedule` (jsonb поле) |
| Документы | `tournaments.documents` (jsonb поле) |
| Bracket | `bracket_matches` WHERE tournament_id |

**Регистрация на турнир:**
```kotlin
client.from("tournament_participants")
    .insert(mapOf(
        "tournament_id" to tournamentId,
        "athlete_id" to userId,
        "status" to "pending"
    ))
```

**Регистрация по инвайт-коду:**
```kotlin
val tournamentId = client.postgrest.rpc("use_invite_code", mapOf("code" to code))
    .decodeSingle<String>()
// Затем INSERT в tournament_participants
```

### AthleteResultsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.results` | `tournament_results` JOIN `tournaments(name, sport_id, sports(name))` WHERE athlete_id = userId |
| Фильтр по спорту | + `tournaments.sport_id = selectedSportId` |
| Общая статистика | `v_user_sport_stats` WHERE user_id = userId |

### AthleteGoalsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.goals` | `athlete_goals` WHERE athlete_id = userId |
| Создать цель | INSERT INTO `athlete_goals` |
| Обновить прогресс | UPDATE `athlete_goals` WHERE id |
| Удалить | DELETE FROM `athlete_goals` WHERE id |

```kotlin
// Получить цели
val goals = client.from("athlete_goals")
    .select { filter { eq("athlete_id", userId) } }
    .decodeList<GoalDto>()

// Создать цель
client.from("athlete_goals").insert(GoalInsertDto(
    athleteId = userId,
    type = "rating",
    title = "Достичь рейтинга 2000",
    targetRating = 2000,
    createdById = userId,
    deadline = "2026-12-31"
))
```

### AthleteProfileScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `user` параметр | `profiles` WHERE id = userId |
| Виды спорта | `user_sports` JOIN `sports` WHERE user_id |
| Подтип | `profiles.athlete_subtype` |
| Обновить профиль | UPDATE `profiles` WHERE id |
| Загрузить аватар | Storage `avatars/$userId/avatar.jpg` |

**Обновление профиля:**
```kotlin
client.from("profiles")
    .update({
        set("name", newName)
        set("phone", newPhone)
        set("city", newCity)
        set("bio", newBio)
        set("athlete_subtype", newSubtype)
    }) {
        filter { eq("id", userId) }
    }
```

**Загрузка аватара:**
```kotlin
val bucket = client.storage.from("avatars")
bucket.upload("$userId/avatar.jpg", imageBytes, upsert = true)
val url = bucket.publicUrl("$userId/avatar.jpg")
client.from("profiles").update({ set("avatar_url", url) }) {
    filter { eq("id", userId) }
}
```

### AthleteTeamScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.team` | `teams` JOIN `team_members(profiles(name, ...))` WHERE id = user.teamId |
| Статистика команды | Агрегация из `v_user_sport_stats` для всех team_members |
| Запросы в команду | `team_requests` WHERE user_id = userId |

### AthleteNotificationsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `AthleteMockData.notifications` → invites | `tournament_invites` WHERE user_id = userId |
| `AthleteMockData.notifications` → team | `team_requests` WHERE user_id = userId |

**Принять приглашение:**
```kotlin
client.from("tournament_invites")
    .update({ set("status", "accepted") }) {
        filter { eq("id", inviteId) }
    }
```

### AthleteRacingLicenseScreen

Лицензии пока НЕТ в БД как отдельная таблица. Можно хранить в `profiles.role_data` (jsonb).

**Временное решение:** оставить mock-данные для лицензий, добавить TODO.

---

## Порядок работы

1. Создать `AthleteRepository.kt` с методами
2. Создать DTO если нужны (доп. к общим)
3. Создать ViewModels
4. Обновить каждый Screen:
   - Добавить `val viewModel: XxxViewModel = viewModel()`
   - Добавить `val state by viewModel.state.collectAsState()`
   - Добавить `LaunchedEffect(user.id) { viewModel.load(user.id) }`
   - Обернуть контент в `when (state)` — Loading / Error / Success
   - В Success-ветке использовать `state.data` вместо MockData
5. НЕ удалять AthleteMockData.kt
