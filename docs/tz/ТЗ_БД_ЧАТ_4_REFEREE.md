# ТЗ БД: Чат #4 — Referee (Судья)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Referee к Supabase. Заменить `RefereeMockData` на реальные запросы.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль судьи |
| `tournament_referees` | R | Назначения на турниры |
| `tournaments` | R | Турниры |
| `tournament_participants` | R | Участники турниров |
| `tournament_results` | R/W | Ввод результатов матчей |
| `bracket_matches` | R/W | Управление матчами bracket |
| `violations` | R/W | Запись нарушений |
| `tournament_invites` | R/W | Приглашения (incoming от организаторов) |
| `tournament_invite_codes` | R | Инвайт-коды (для самостоятельной записи) |
| `sports` | R | Справочник спортов |
| `user_sports` | R | Виды спорта судьи |
| `v_tournament_with_counts` | R | View турниров |

---

## Файлы для создания

### 1. `data/repository/RefereeRepository.kt`

```kotlin
class RefereeRepository {
    private val client = SupabaseModule.client

    // ── ДАШБОРД ──
    suspend fun getDashboardStats(userId: String): RefereeDashboardStats
    suspend fun getUpcomingAssignments(userId: String): List<RefereeAssignmentWithTournamentDto>

    // ── МОИ ТУРНИРЫ ──
    suspend fun getAssignedTournaments(userId: String): List<TournamentWithRefereeRoleDto>
    suspend fun getTournamentDetail(tournamentId: String, userId: String): RefereeTournamentDetailData

    // ── МАТЧИ ──
    suspend fun getMatches(tournamentId: String): List<BracketMatchWithParticipantsDto>
    suspend fun updateMatchResult(matchId: String, data: MatchResultUpdateDto)
    suspend fun startMatch(matchId: String)

    // ── РЕЗУЛЬТАТЫ ──
    suspend fun getParticipants(tournamentId: String): List<ParticipantWithProfileDto>
    suspend fun saveResults(tournamentId: String, results: List<ResultInsertDto>)

    // ── НАРУШЕНИЯ ──
    suspend fun getViolations(tournamentId: String): List<ViolationDto>
    suspend fun createViolation(data: ViolationInsertDto)
    suspend fun getMyViolationsHistory(userId: String): List<ViolationDto>

    // ── ПРИГЛАШЕНИЯ ──
    suspend fun getInvites(userId: String): List<TournamentInviteWithDetailsDto>
    suspend fun respondToInvite(inviteId: String, accept: Boolean, message: String?)
    suspend fun applyToTournament(tournamentId: String, userId: String, refereeRole: String, message: String?)
    suspend fun joinByInviteCode(code: String, userId: String)

    // ── ИСТОРИЯ ──
    suspend fun getCompletedTournaments(userId: String): List<TournamentWithStatsDto>
    suspend fun getMonthlyStats(userId: String): List<MonthlyStatsDto>

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)
}
```

### 2. Специфичные DTO

```kotlin
@Serializable
data class ViolationDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("referee_id") val refereeId: String,
    val severity: String, // warning/penalty/disqualification
    val category: String,
    val description: String? = null,
    @SerialName("match_number") val matchNumber: Int? = null,
    val time: String? = null,
    @SerialName("penalty_applied") val penaltyApplied: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ViolationInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("referee_id") val refereeId: String,
    val severity: String,
    val category: String,
    val description: String? = null,
    @SerialName("match_number") val matchNumber: Int? = null,
    val time: String? = null,
    @SerialName("penalty_applied") val penaltyApplied: String? = null
)

@Serializable
data class BracketMatchDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val round: Int,
    @SerialName("match_number") val matchNumber: Int,
    @SerialName("bracket_type") val bracketType: String? = null,
    @SerialName("participant1_id") val participant1Id: String? = null,
    @SerialName("participant2_id") val participant2Id: String? = null,
    val scores: String? = null,
    val games: String? = null, // JSON
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("loser_id") val loserId: String? = null,
    val status: String, // scheduled/in_progress/completed/cancelled
    @SerialName("scheduled_at") val scheduledAt: String? = null,
    @SerialName("is_bye") val isBye: Boolean? = null
)

@Serializable
data class MatchResultUpdateDto(
    val scores: String? = null,
    val games: String? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("loser_id") val loserId: String? = null,
    val status: String = "completed"
)
```

### 3. ViewModels

```
ui/viewmodels/
  RefereeDashboardViewModel.kt
  RefereeTournamentsViewModel.kt
  RefereeTournamentDetailViewModel.kt
  RefereeTournamentResultsViewModel.kt
  RefereeRequestsViewModel.kt
  RefereeHistoryViewModel.kt
  RefereeProfileViewModel.kt
```

---

## Экран → Запросы

### RefereeDashboardScreen

```kotlin
// Мои назначения
val assignments = client.from("tournament_referees")
    .select(Columns.raw("*, tournaments(*, sports(name), locations(name, city))"))
    { filter { eq("referee_id", userId) } }
    .decodeList<RefereeAssignmentWithTournamentDto>()

// Статистика
// Кол-во турниров — assignments.size
// Кол-во матчей — bracket_matches через tournament_ids
val tournamentIds = assignments.map { it.tournamentId }
val matchCount = client.from("bracket_matches")
    .select(Columns.raw("id")) {
        filter { isIn("tournament_id", tournamentIds) }
    }
    .decodeList<IdOnlyDto>().size

// Нарушения
val violationCount = client.from("violations")
    .select(Columns.raw("id")) {
        filter { eq("referee_id", userId) }
    }
    .decodeList<IdOnlyDto>().size

// Ожидающие приглашения
val pendingInvites = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date)"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "referee")
            eq("status", "pending")
        }
    }
    .decodeList<TournamentInviteWithDetailsDto>()
```

### RefereeTournamentsScreen

```kotlin
// Все назначенные турниры с ролью судьи
val tournaments = client.from("tournament_referees")
    .select(Columns.raw("role, tournaments(*, sports(name), locations(name, city))"))
    { filter { eq("referee_id", userId) } }
    .decodeList<TournamentWithRefereeRoleDto>()
// Фильтр по статусу на клиенте
```

### RefereeTournamentDetailScreen

```kotlin
// Турнир
val tournament = client.from("tournaments")
    .select(Columns.raw("*, sports(name, slug), locations(*), profiles!organizer_id(name)"))
    { filter { eq("id", tournamentId) } }
    .decodeSingle<TournamentFullDto>()

// Мая роль
val myRole = client.from("tournament_referees")
    .select { filter { eq("tournament_id", tournamentId); eq("referee_id", userId) } }
    .decodeSingle<RefereeAssignmentDto>()

// Участники
val participants = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name, avatar_url)"))
    { filter { eq("tournament_id", tournamentId); eq("status", "confirmed") } }
    .decodeList<ParticipantWithProfileDto>()

// Матчи bracket
val matches = client.from("bracket_matches")
    .select(Columns.raw("*"))
    { filter { eq("tournament_id", tournamentId) }; order("round"); order("match_number") }
    .decodeList<BracketMatchDto>()

// Нарушения
val violations = client.from("violations")
    .select(Columns.raw("*, profiles!athlete_id(name)"))
    { filter { eq("tournament_id", tournamentId) } }
    .decodeList<ViolationWithAthleteDto>()
```

### RefereeTournamentResultsScreen

```kotlin
// Внести результат матча
client.from("bracket_matches")
    .update({
        set("scores", scoresJson)
        set("winner_id", winnerId)
        set("loser_id", loserId)
        set("status", "completed")
    }) { filter { eq("id", matchId) } }

// Записать нарушение
client.from("violations").insert(ViolationInsertDto(
    tournamentId = tournamentId,
    athleteId = athleteId,
    refereeId = userId,
    severity = "warning",
    category = "FALSE_START",
    description = "Фальстарт на старте"
))

// Финальные результаты (при завершении турнира)
results.forEach { result ->
    client.from("tournament_results").upsert(result)
}
```

### RefereeRequestsScreen

```kotlin
// Входящие приглашения
val incoming = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date, sports(name), locations(name))"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "referee")
            eq("direction", "outgoing") // outgoing от организатора = incoming для судьи
        }
        order("created_at", Order.DESCENDING)
    }
    .decodeList<TournamentInviteWithDetailsDto>()

// Мои заявки
val outgoing = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date)"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "referee")
            eq("direction", "incoming") // incoming от судьи = outgoing заявка
        }
    }
    .decodeList<TournamentInviteWithDetailsDto>()

// Принять приглашение
client.from("tournament_invites")
    .update({ set("status", "accepted") })
    { filter { eq("id", inviteId) } }

// После принятия — организатор добавляет в tournament_referees
// (или можно автоматизировать через DB trigger)

// Присоединиться по инвайт-коду
val tournamentId = client.postgrest.rpc("use_invite_code", mapOf("code" to code))
    .decodeSingle<String>()
```

**ВАЖНО:** Направление приглашений:
- `direction = "outgoing"` + `user_id = referee` = организатор пригласил судью
- `direction = "incoming"` + `user_id = referee` = судья подал заявку

### RefereeHistoryScreen

```kotlin
// Завершённые турниры
val completed = client.from("tournament_referees")
    .select(Columns.raw("role, tournaments(*, sports(name))"))
    {
        filter {
            eq("referee_id", userId)
            eq("tournaments.status", "completed")
        }
    }
    .decodeList<TournamentWithRefereeRoleDto>()

// Статистика по месяцам — агрегация на клиенте из violations + matches
val allViolations = client.from("violations")
    .select { filter { eq("referee_id", userId) } }
    .decodeList<ViolationDto>()
// Группировка по месяцу created_at
```

### RefereeProfileScreen

Аналогично другим профилям — `profiles` + `user_sports` + Storage.

---

## Порядок работы

1. Создать `RefereeRepository.kt`
2. Создать DTO: `ViolationDto`, `ViolationInsertDto`, `BracketMatchDto`, `MatchResultUpdateDto`
3. Создать ViewModels (7 штук)
4. Обновить экраны по очереди: Dashboard → Tournaments → Detail → Results → Requests → History → Profile
5. НЕ удалять RefereeMockData.kt
