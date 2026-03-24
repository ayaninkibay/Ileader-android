# ТЗ БД: Чат #2 — Trainer (Тренер)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Trainer к Supabase. Заменить `TrainerMockData` на реальные запросы.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль тренера |
| `teams` | R/W | Команды тренера (owner_id = userId) |
| `team_members` | R/W | Участники команд |
| `team_requests` | R/W | Заявки в команду (принять/отклонить) |
| `user_sports` | R | Рейтинги спортсменов |
| `sports` | R | Справочник спортов |
| `tournaments` | R | Турниры |
| `tournament_participants` | R | Участие спортсменов команды |
| `tournament_results` | R | Результаты спортсменов |
| `athlete_goals` | R/W | Цели (тренер может создавать цели для своих спортсменов) |
| `sponsorships` | R | Спонсорство команд |
| `v_tournament_with_counts` | R | View турниров |
| `v_user_sport_stats` | R | Статистика спортсменов |

---

## Файлы для создания

### 1. `data/repository/TrainerRepository.kt`

```kotlin
class TrainerRepository {
    private val client = SupabaseModule.client

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)

    // ── КОМАНДЫ ──
    suspend fun getMyTeams(userId: String): List<TeamWithMembersDto>
    suspend fun getTeamStats(teamId: String): TeamStatsData
    suspend fun inviteAthlete(teamId: String, email: String)
    suspend fun removeAthleteFromTeam(teamId: String, athleteId: String)

    // ── ЗАЯВКИ ──
    suspend fun getPendingRequests(teamId: String): List<TeamRequestDto>
    suspend fun respondToRequest(requestId: String, accept: Boolean, message: String?)

    // ── СПОРТСМЕН (деталь) ──
    suspend fun getAthleteDetail(athleteId: String): AthleteDetailData
    suspend fun getAthleteResults(athleteId: String): List<TournamentResultDto>
    suspend fun getAthleteGoals(athleteId: String): List<GoalDto>
    suspend fun createGoalForAthlete(goal: GoalInsertDto)

    // ── ТУРНИРЫ ──
    suspend fun getTeamTournaments(teamId: String): List<TournamentWithDetailsDto>

    // ── СТАТИСТИКА ──
    suspend fun getTeamStatistics(teamId: String): List<UserSportStatsDto>

    // ── УВЕДОМЛЕНИЯ ──
    suspend fun getTeamRequests(ownerId: String): List<TeamRequestDto>
    suspend fun getSponsorshipRequests(teamIds: List<String>): List<SponsorshipDto>
}
```

### 2. ViewModels

```
ui/viewmodels/
  TrainerDashboardViewModel.kt
  TrainerTeamViewModel.kt
  TrainerAthleteDetailViewModel.kt
  TrainerTournamentsViewModel.kt
  TrainerStatisticsViewModel.kt
  TrainerProfileViewModel.kt
  TrainerNotificationsViewModel.kt
```

---

## Экран → Запросы

### TrainerDashboardScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `TrainerMockData.teams.size` | `teams` WHERE owner_id = userId → COUNT |
| Кол-во спортсменов | `team_members` WHERE team_id IN (my teams) → COUNT |
| Предстоящие турниры | `tournament_participants` WHERE team_id IN (my teams) JOIN `tournaments` WHERE status != completed |
| Средний результат | `v_user_sport_stats` WHERE user_id IN (my team members) → AVG rating |
| Рейтинг команды | Агрегация из `v_user_sport_stats` |

```kotlin
// Получить команды тренера
val teams = client.from("teams")
    .select(Columns.raw("*, team_members(*, profiles(name, email, avatar_url)), sports(name)"))
    { filter { eq("owner_id", userId) } }
    .decodeList<TeamWithMembersDto>()

// Турниры команды
val teamIds = teams.map { it.id }
val tournaments = client.from("tournament_participants")
    .select(Columns.raw("*, tournaments(*, sports(name), locations(name, city))"))
    {
        filter { isIn("team_id", teamIds) }
    }
    .decodeList<ParticipantWithTournamentDto>()
```

### TrainerTeamScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `TrainerMockData.teams` | `teams` WHERE owner_id = userId JOIN `team_members`, `sports` |
| `TrainerMockData.pendingInvites` | `team_requests` WHERE team_id AND status = 'pending' |
| Клик по спортсмену | → TrainerAthleteDetailScreen(athleteId) |

**Пригласить спортсмена:**
```kotlin
// Найти пользователя по email
val profile = client.from("profiles")
    .select { filter { eq("email", email) } }
    .decodeSingleOrNull<ProfileDto>()
    ?: throw Exception("Пользователь не найден")

// Создать заявку
client.from("team_requests").insert(mapOf(
    "team_id" to teamId,
    "user_id" to profile.id,
    "status" to "pending",
    "message" to "Приглашение от тренера"
))
```

**Принять заявку:**
```kotlin
// Обновить статус заявки
client.from("team_requests")
    .update({ set("status", "accepted"); set("response_message", message) })
    { filter { eq("id", requestId) } }

// Добавить в team_members
client.from("team_members").insert(mapOf(
    "team_id" to teamId,
    "user_id" to athleteId,
    "role" to "member"
))

// Обновить teamId в профиле спортсмена
client.from("profiles")
    .update({ /* profiles не имеет teamId — teamId определяется через team_members */ })
```

**ВАЖНО:** В текущей БД `teamId` нет в profiles — принадлежность определяется через `team_members`. Нужно обновить модель `User` чтобы teamId вычислялся из team_members.

### TrainerAthleteDetailScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| Профиль спортсмена | `profiles` WHERE id = athleteId |
| Результаты | `tournament_results` JOIN `tournaments` WHERE athlete_id |
| Рейтинг | `v_user_sport_stats` WHERE user_id = athleteId |
| Цели | `athlete_goals` WHERE athlete_id |

```kotlin
// Профиль
val profile = client.from("profiles")
    .select { filter { eq("id", athleteId) } }
    .decodeSingle<ProfileDto>()

// Статистика
val stats = client.from("v_user_sport_stats")
    .select { filter { eq("user_id", athleteId) } }
    .decodeList<UserSportStatsDto>()

// Результаты
val results = client.from("tournament_results")
    .select(Columns.raw("*, tournaments(name, start_date, sports(name))"))
    { filter { eq("athlete_id", athleteId) } }
    .decodeList<ResultWithTournamentDto>()

// Цели (тренер видит цели своих спортсменов)
val goals = client.from("athlete_goals")
    .select { filter { eq("athlete_id", athleteId) } }
    .decodeList<GoalDto>()
```

**Создать цель для спортсмена:**
```kotlin
client.from("athlete_goals").insert(GoalInsertDto(
    athleteId = athleteId,
    type = "rating",
    title = "Достичь рейтинга 1500",
    targetRating = 1500,
    createdBy = "trainer",
    createdById = trainerId, // ID тренера
    deadline = "2026-06-30"
))
```

### TrainerTournamentsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| `TrainerMockData.tournaments` | `tournament_participants` WHERE team_id IN (my teams) JOIN `v_tournament_with_counts` |

### TrainerStatisticsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| Рейтинги спортсменов | `v_user_sport_stats` WHERE user_id IN (team members) |
| Распределение результатов | `tournament_results` WHERE athlete_id IN (team members) → агрегация по position |
| Прогресс рейтинга | Исторических данных нет — оставить mock или хардкод |

### TrainerProfileScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| Профиль | `profiles` WHERE id = userId |
| Виды спорта | `user_sports` JOIN `sports` WHERE user_id |
| Обновить | UPDATE `profiles` WHERE id |

### TrainerNotificationsScreen

| Mock-данные | Реальный запрос |
|-------------|-----------------|
| Заявки в команду | `team_requests` WHERE team_id IN (my teams) AND status = 'pending' |
| Спонсорские предложения | `sponsorships` WHERE team_id IN (my teams) AND status = 'pending' |
| Результаты турниров | Нет отдельной таблицы уведомлений — составлять из данных (недавние results) |

---

## Порядок работы

1. Создать `TrainerRepository.kt`
2. Создать доп. DTO если нужны
3. Создать ViewModels (7 штук)
4. Обновить экраны: `TrainerDashboardScreen`, `TrainerTeamScreen`, `TrainerAthleteDetailScreen`, `TrainerTournamentsScreen`, `TrainerStatisticsScreen`, `TrainerProfileScreen`, `TrainerNotificationsScreen`
5. В каждом экране: ViewModel + UiState + LaunchedEffect + when(state)
6. НЕ удалять TrainerMockData.kt
