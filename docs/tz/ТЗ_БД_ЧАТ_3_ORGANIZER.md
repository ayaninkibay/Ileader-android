# ТЗ БД: Чат #3 — Organizer (Организатор)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Organizer к Supabase. Заменить `OrganizerMockData` на реальные запросы. Организатор — самая сложная роль по количеству CRUD операций.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль организатора |
| `tournaments` | R/W | CRUD турниров |
| `tournament_participants` | R/W | Управление участниками |
| `tournament_results` | R/W | Ввод результатов |
| `tournament_referees` | R | Назначенные судьи |
| `tournament_invite_codes` | R/W | Создание/управление инвайт-кодами |
| `tournament_invites` | R/W | Приглашения (outgoing) |
| `tournament_groups` | R/W | Группы турнира |
| `bracket_matches` | R/W | Сетка/bracket |
| `locations` | R/W | CRUD локаций |
| `sports` | R/W | Управление видами спорта |
| `user_sports` | R | Рейтинги |
| `v_tournament_with_counts` | R | View турниров |
| `v_organizer_stats` | R | Статистика организатора |

---

## Файлы для создания

### 1. `data/repository/OrganizerRepository.kt`

```kotlin
class OrganizerRepository {
    private val client = SupabaseModule.client

    // ── ДАШБОРД ──
    suspend fun getStats(userId: String): OrganizerStatsDto
    suspend fun getRecentRegistrations(userId: String): List<RecentRegistrationDto>

    // ── ТУРНИРЫ ──
    suspend fun getMyTournaments(userId: String): List<TournamentWithDetailsDto>
    suspend fun getTournamentDetail(tournamentId: String): TournamentFullDto
    suspend fun createTournament(data: TournamentInsertDto): String // returns id
    suspend fun updateTournament(tournamentId: String, data: TournamentUpdateDto)
    suspend fun deleteTournament(tournamentId: String)
    suspend fun updateTournamentStatus(tournamentId: String, status: String)

    // ── УЧАСТНИКИ ──
    suspend fun getParticipants(tournamentId: String): List<ParticipantWithProfileDto>
    suspend fun approveParticipant(tournamentId: String, athleteId: String)
    suspend fun declineParticipant(tournamentId: String, athleteId: String)
    suspend fun disqualifyParticipant(tournamentId: String, athleteId: String)

    // ── РЕЗУЛЬТАТЫ ──
    suspend fun getResults(tournamentId: String): List<TournamentResultDto>
    suspend fun saveResults(tournamentId: String, results: List<ResultInsertDto>)
    suspend fun updateResult(resultId: String, data: ResultUpdateDto)

    // ── ИНВАЙТЫ ──
    suspend fun createInviteCode(data: InviteCodeInsertDto): String
    suspend fun getInviteCodes(tournamentId: String): List<InviteCodeDto>
    suspend fun deactivateInviteCode(codeId: String)
    suspend fun sendInvite(data: TournamentInviteInsertDto)

    // ── BRACKET ──
    suspend fun getBracket(tournamentId: String): List<BracketMatchDto>
    suspend fun updateMatch(matchId: String, data: MatchUpdateDto)

    // ── ЛОКАЦИИ ──
    suspend fun getMyLocations(userId: String): List<LocationDto>
    suspend fun getLocationDetail(locationId: String): LocationDto
    suspend fun createLocation(data: LocationInsertDto): String
    suspend fun updateLocation(locationId: String, data: LocationUpdateDto)
    suspend fun deleteLocation(locationId: String)

    // ── ВИДЫ СПОРТА ──
    suspend fun getSports(): List<SportDto>
    suspend fun getMySports(userId: String): List<SportDto> // через user_sports

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)
}
```

### 2. Специфичные DTO

```kotlin
@Serializable
data class TournamentInsertDto(
    val name: String,
    @SerialName("sport_id") val sportId: String,
    @SerialName("location_id") val locationId: String? = null,
    @SerialName("organizer_id") val organizerId: String,
    val status: String = "draft",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    val description: String? = null,
    val format: String? = null,
    @SerialName("match_format") val matchFormat: String? = null,
    @SerialName("seeding_type") val seedingType: String? = null,
    val visibility: String = "public",
    @SerialName("max_participants") val maxParticipants: Int? = null,
    @SerialName("min_participants") val minParticipants: Int? = null,
    @SerialName("prize_pool") val prizePool: String? = null,
    val rules: String? = null,
    val requirements: String? = null,
    val categories: List<String>? = null,
    val ageCategory: String? = null,
    val region: String? = null,
    val discipline: String? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("has_third_place_match") val hasThirdPlaceMatch: Boolean? = null,
    @SerialName("has_check_in") val hasCheckIn: Boolean? = null,
    val schedule: String? = null, // JSON string
    val documents: String? = null // JSON string
)

@Serializable
data class LocationInsertDto(
    val name: String,
    val type: String, // location_type enum
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val facilities: List<String>? = null,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null
)

@Serializable
data class InviteCodeInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    val code: String, // генерировать: "ATH-" + random 6 chars
    val type: String, // "athlete", "referee", "sponsor"
    @SerialName("referee_role") val refereeRole: String? = null,
    @SerialName("max_uses") val maxUses: Int? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_by") val createdBy: String
)

@Serializable
data class ResultInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    val position: Int,
    val points: Int? = null,
    val time: String? = null,
    val penalty: String? = null,
    val category: String? = null,
    val notes: String? = null
)

@Serializable
data class OrganizerStatsDto(
    @SerialName("organizer_id") val organizerId: String? = null,
    @SerialName("total_tournaments") val totalTournaments: Int = 0,
    @SerialName("active_tournaments") val activeTournaments: Int = 0,
    @SerialName("total_locations") val totalLocations: Int = 0,
    @SerialName("total_participants") val totalParticipants: Int = 0
)
```

### 3. ViewModels

```
ui/viewmodels/
  OrganizerDashboardViewModel.kt
  OrganizerTournamentsViewModel.kt
  OrganizerTournamentDetailViewModel.kt
  OrganizerTournamentEditViewModel.kt
  OrganizerTournamentResultsViewModel.kt
  OrganizerLocationsViewModel.kt
  OrganizerLocationDetailViewModel.kt
  OrganizerSportsViewModel.kt
  OrganizerProfileViewModel.kt
```

---

## Экран → Запросы

### OrganizerDashboardScreen

```kotlin
// Статистика (из View)
val stats = client.from("v_organizer_stats")
    .select { filter { eq("organizer_id", userId) } }
    .decodeSingle<OrganizerStatsDto>()

// Предстоящие турниры
val upcoming = client.from("tournaments")
    .select(Columns.raw("*, sports(name), locations(name, city)"))
    {
        filter {
            eq("organizer_id", userId)
            neq("status", "completed")
            neq("status", "cancelled")
        }
        order("start_date", Order.ASCENDING)
        limit(5)
    }
    .decodeList<TournamentWithDetailsDto>()

// Недавние регистрации (последние 10 участников по моим турнирам)
// Сначала получить IDs моих турниров, затем:
val registrations = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name, avatar_url), tournaments(name)"))
    {
        filter { isIn("tournament_id", myTournamentIds) }
        order("registered_at", Order.DESCENDING)
        limit(10)
    }
    .decodeList<RecentRegistrationDto>()
```

### OrganizerTournamentsScreen

```kotlin
// Все мои турниры
val tournaments = client.from("v_tournament_with_counts")
    .select { filter { eq("organizer_id", userId) } }
    .decodeList<TournamentWithCountsDto>()
// Фильтрация по status на клиенте
```

### OrganizerTournamentDetailScreen

```kotlin
// Турнир
val tournament = client.from("tournaments")
    .select(Columns.raw("*, sports(name, slug), locations(*), profiles!organizer_id(name)"))
    { filter { eq("id", tournamentId) } }
    .decodeSingle<TournamentFullDto>()

// Участники
val participants = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name, email, avatar_url, athlete_subtype)"))
    { filter { eq("tournament_id", tournamentId) } }
    .decodeList<ParticipantWithProfileDto>()

// Судьи
val referees = client.from("tournament_referees")
    .select(Columns.raw("*, profiles(name)"))
    { filter { eq("tournament_id", tournamentId) } }
    .decodeList<RefereeAssignmentDto>()

// Инвайт-коды
val codes = client.from("tournament_invite_codes")
    .select { filter { eq("tournament_id", tournamentId) } }
    .decodeList<InviteCodeDto>()
```

**Одобрить участника:**
```kotlin
client.from("tournament_participants")
    .update({ set("status", "confirmed") })
    { filter { eq("tournament_id", tid); eq("athlete_id", aid) } }
```

### OrganizerTournamentEditScreen / CreateScreen

**Создание:**
```kotlin
val result = client.from("tournaments")
    .insert(tournamentInsertDto) { select() }
    .decodeSingle<TournamentDto>()
// result.id — ID нового турнира
```

**Обновление:**
```kotlin
client.from("tournaments")
    .update({
        set("name", data.name)
        set("start_date", data.startDate)
        set("status", data.status)
        // ... все поля
    }) { filter { eq("id", tournamentId) } }
```

**Загрузка изображения турнира:**
```kotlin
val bucket = client.storage.from("tournament-images")
bucket.upload("$tournamentId/cover.jpg", imageBytes, upsert = true)
val url = bucket.publicUrl("$tournamentId/cover.jpg")
client.from("tournaments").update({ set("image_url", url) }) {
    filter { eq("id", tournamentId) }
}
```

**Загрузка документов:**
```kotlin
val bucket = client.storage.from("tournament-docs")
bucket.upload("$tournamentId/$filename", docBytes)
```

### OrganizerTournamentResultsScreen

```kotlin
// Получить участников (для заполнения)
val participants = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name)"))
    { filter { eq("tournament_id", tid); eq("status", "confirmed") } }
    .decodeList<ParticipantWithProfileDto>()

// Сохранить результаты (UPSERT)
results.forEach { result ->
    client.from("tournament_results").upsert(result)
}

// Обновить статус турнира → completed
client.from("tournaments")
    .update({ set("status", "completed") })
    { filter { eq("id", tid) } }
```

### OrganizerLocationsScreen + Detail + Edit + Create

```kotlin
// Мои локации
val locations = client.from("locations")
    .select { filter { eq("owner_id", userId) } }
    .decodeList<LocationDto>()

// Создать
val newLocation = client.from("locations")
    .insert(locationInsertDto) { select() }
    .decodeSingle<LocationDto>()

// Обновить
client.from("locations")
    .update({ /* поля */ }) { filter { eq("id", locationId) } }

// Удалить
client.from("locations")
    .delete { filter { eq("id", locationId) } }

// Загрузить фото локации
val bucket = client.storage.from("location-images")
bucket.upload("$locationId/main.jpg", imageBytes, upsert = true)
```

### OrganizerSportsScreen

```kotlin
// Все виды спорта
val sports = client.from("sports")
    .select { filter { eq("is_active", true) } }
    .decodeList<SportDto>()

// Мои виды спорта (через user_sports)
val mySports = client.from("user_sports")
    .select(Columns.raw("*, sports(*)"))
    { filter { eq("user_id", userId) } }
    .decodeList<UserSportWithDetailsDto>()

// Добавить вид спорта к себе
client.from("user_sports").insert(mapOf(
    "user_id" to userId,
    "sport_id" to sportId,
    "is_primary" to false
))
```

### OrganizerProfileScreen

Аналогично AthleteProfileScreen — `profiles` READ/UPDATE + Storage для аватара.

---

## Порядок работы

1. Создать `OrganizerRepository.kt` (самый большой — ~30 методов)
2. Создать DTO: `TournamentInsertDto`, `LocationInsertDto`, `InviteCodeInsertDto`, `ResultInsertDto`, `OrganizerStatsDto`, etc.
3. Создать ViewModels (9 штук)
4. Обновить экраны (начать с Dashboard, потом Tournaments, Locations)
5. НЕ удалять OrganizerMockData.kt
