# ТЗ БД: Чат #8 — Viewer / User (Зритель)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Viewer к Supabase. Заменить `ViewerMockData` на реальные запросы. Зритель видит только публичные данные — турниры, новости, сообщество.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль зрителя (свой) + чтение публичных профилей |
| `tournaments` | R | Публичные турниры |
| `tournament_participants` | R | Участники (для публичного просмотра) |
| `tournament_results` | R | Результаты (публичные) |
| `bracket_matches` | R | Bracket (публичный) |
| `sports` | R | Виды спорта |
| `teams` | R | Публичные команды |
| `team_members` | R | Участники команд |
| `user_sports` | R | Рейтинги |
| `v_tournament_with_counts` | R | View турниров |
| `v_user_sport_stats` | R | Статистика спортсменов |

**Зритель НЕ пишет данные** (кроме обновления своего профиля).

---

## Файлы для создания

### 1. `data/repository/ViewerRepository.kt`

```kotlin
class ViewerRepository {
    private val client = SupabaseModule.client

    // ── ГЛАВНАЯ ──
    suspend fun getPlatformStats(): PlatformStatsDto
    suspend fun getSports(): List<SportDto>
    suspend fun getUpcomingTournaments(limit: Int = 10): List<TournamentWithDetailsDto>
    suspend fun getLatestNews(): List<NewsArticleDto> // Пока mock — нет таблицы articles

    // ── ТУРНИРЫ ──
    suspend fun getPublicTournaments(
        sportFilter: String? = null,
        regionFilter: String? = null,
        statusFilter: String? = null,
        search: String? = null
    ): List<TournamentWithDetailsDto>

    suspend fun getTournamentDetail(tournamentId: String): TournamentFullDto
    suspend fun getTournamentParticipants(tournamentId: String): List<ParticipantWithProfileDto>
    suspend fun getTournamentResults(tournamentId: String): List<ResultWithAthleteDto>
    suspend fun getTournamentBracket(tournamentId: String): List<BracketMatchDto>

    // ── НОВОСТИ ──
    // Пока нет таблицы articles в БД → оставить на MockData
    // suspend fun getNews(): List<NewsArticleDto>
    // suspend fun getNewsDetail(articleId: String): NewsArticleDto

    // ── СООБЩЕСТВО ──
    suspend fun getAthletes(
        sportFilter: String? = null,
        search: String? = null
    ): List<CommunityProfileDto>

    suspend fun getTrainers(
        sportFilter: String? = null,
        search: String? = null
    ): List<CommunityProfileDto>

    suspend fun getReferees(
        sportFilter: String? = null,
        search: String? = null
    ): List<CommunityProfileDto>

    suspend fun getTeams(
        sportFilter: String? = null,
        search: String? = null
    ): List<TeamWithStatsDto>

    suspend fun getAthletePublicProfile(athleteId: String): AthletePublicProfileData
    suspend fun getTrainerPublicProfile(trainerId: String): TrainerPublicProfileData
    suspend fun getRefereePublicProfile(refereeId: String): RefereePublicProfileData
    suspend fun getTeamPublicProfile(teamId: String): TeamPublicProfileData

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)
}
```

### 2. Специфичные DTO

```kotlin
@Serializable
data class PlatformStatsDto(
    val totalUsers: Int,
    val totalTournaments: Int,
    val totalSports: Int
)

@Serializable
data class CommunityProfileDto(
    val id: String,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val city: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    // JOIN
    @SerialName("user_sports") val userSports: List<UserSportWithNameDto>? = null
)

@Serializable
data class AthletePublicProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportWithNameDto>,
    val stats: List<UserSportStatsDto>,
    val recentResults: List<ResultWithTournamentDto>,
    val team: TeamDto? = null
)

@Serializable
data class ResultWithAthleteDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    val position: Int,
    val points: Int? = null,
    val time: String? = null,
    val penalty: String? = null,
    val profiles: ProfileMinimalDto? = null // from JOIN
)

@Serializable
data class ProfileMinimalDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
```

### 3. ViewModels

```
ui/viewmodels/
  ViewerHomeViewModel.kt
  ViewerTournamentsViewModel.kt
  ViewerTournamentDetailViewModel.kt
  ViewerNewsViewModel.kt          // пока mock
  ViewerCommunityViewModel.kt
  ViewerPublicProfileViewModel.kt
  ViewerProfileViewModel.kt
```

---

## Экран → Запросы

### ViewerHomeScreen

```kotlin
// Статистика платформы
val usersCount = client.from("profiles")
    .select(Columns.raw("id")) { filter { eq("status", "active") } }
    .decodeList<IdOnlyDto>().size

val tournamentsCount = client.from("tournaments")
    .select(Columns.raw("id"))
    .decodeList<IdOnlyDto>().size

val sportsCount = client.from("sports")
    .select(Columns.raw("id")) { filter { eq("is_active", true) } }
    .decodeList<IdOnlyDto>().size

val platformStats = PlatformStatsDto(usersCount, tournamentsCount, sportsCount)

// Виды спорта
val sports = client.from("sports")
    .select { filter { eq("is_active", true) } }
    .decodeList<SportDto>()

// Предстоящие турниры
val upcoming = client.from("v_tournament_with_counts")
    .select {
        filter {
            eq("visibility", "public")
            or {
                eq("status", "registration_open")
                eq("status", "in_progress")
            }
        }
        order("start_date", Order.ASCENDING)
        limit(10)
    }
    .decodeList<TournamentWithCountsDto>()

// Новости — пока MockData (нет таблицы articles)
val news = ViewerMockData.newsArticles
```

### ViewerTournamentsScreen (Tab)

```kotlin
// Все публичные турниры
val tournaments = client.from("v_tournament_with_counts")
    .select {
        filter { eq("visibility", "public") }
        order("start_date", Order.DESCENDING)
    }
    .decodeList<TournamentWithCountsDto>()

// Фильтрация на клиенте: по sport, region, status, search
```

### ViewerTournamentDetailScreen

```kotlin
// Турнир с деталями
val tournament = client.from("tournaments")
    .select(Columns.raw("*, sports(name, slug), locations(*), profiles!organizer_id(name)"))
    { filter { eq("id", tournamentId) } }
    .decodeSingle<TournamentFullDto>()

// Участники
val participants = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name, avatar_url, city)"))
    {
        filter { eq("tournament_id", tournamentId); eq("status", "confirmed") }
        order("seed")
    }
    .decodeList<ParticipantWithProfileDto>()

// Schedule из tournament.schedule (jsonb)
```

### ViewerTournamentResultsScreen

```kotlin
// Результаты
val results = client.from("tournament_results")
    .select(Columns.raw("*, profiles!athlete_id(name, avatar_url, city)"))
    {
        filter { eq("tournament_id", tournamentId) }
        order("position", Order.ASCENDING)
    }
    .decodeList<ResultWithAthleteDto>()

// Bracket
val bracket = client.from("bracket_matches")
    .select {
        filter { eq("tournament_id", tournamentId) }
        order("round")
        order("match_number")
    }
    .decodeList<BracketMatchDto>()
```

### ViewerNewsScreen / NewsDetailScreen

**Оставить на MockData.** Нет таблицы `articles` в БД.

```kotlin
// TODO: Подключить к БД когда будет создана таблица articles
val news = ViewerMockData.newsArticles
```

### ViewerCommunityScreen

```kotlin
// Спортсмены (athletes)
val athleteRoleId = getRoleId("athlete") // получить ID роли
val athletes = client.from("profiles")
    .select(Columns.raw("id, name, avatar_url, city, athlete_subtype, user_sports(rating, sports(name))"))
    {
        filter {
            eq("primary_role_id", athleteRoleId)
            eq("status", "active")
        }
        limit(50)
    }
    .decodeList<CommunityProfileDto>()

// Тренеры
val trainerRoleId = getRoleId("trainer")
val trainers = client.from("profiles")
    .select(Columns.raw("id, name, avatar_url, city, user_sports(sports(name))"))
    {
        filter {
            eq("primary_role_id", trainerRoleId)
            eq("status", "active")
        }
    }
    .decodeList<CommunityProfileDto>()

// Судьи
val refereeRoleId = getRoleId("referee")
val referees = client.from("profiles")
    .select(Columns.raw("id, name, avatar_url, city, user_sports(sports(name))"))
    {
        filter {
            eq("primary_role_id", refereeRoleId)
            eq("status", "active")
        }
    }
    .decodeList<CommunityProfileDto>()

// Команды
val teams = client.from("teams")
    .select(Columns.raw("*, sports(name), profiles!owner_id(name), team_members(count)"))
    { filter { eq("is_active", true) } }
    .decodeList<TeamWithStatsDto>()
```

**Получить ID роли (вспомогательная функция):**
```kotlin
private suspend fun getRoleId(roleName: String): String {
    return client.from("roles")
        .select { filter { eq("name", roleName) } }
        .decodeSingle<RoleDto>()
        .id
}
// Кэшировать результат — роли не меняются
```

### ViewerAthleteProfileScreen / TrainerProfileScreen / RefereeProfileScreen

```kotlin
// Публичный профиль спортсмена
val profile = client.from("profiles")
    .select { filter { eq("id", athleteId) } }
    .decodeSingle<ProfileDto>()

val sports = client.from("user_sports")
    .select(Columns.raw("*, sports(name, slug)"))
    { filter { eq("user_id", athleteId) } }
    .decodeList<UserSportWithNameDto>()

val stats = client.from("v_user_sport_stats")
    .select { filter { eq("user_id", athleteId) } }
    .decodeList<UserSportStatsDto>()

val results = client.from("tournament_results")
    .select(Columns.raw("*, tournaments(name, start_date, sports(name))"))
    {
        filter { eq("athlete_id", athleteId) }
        order("position", Order.ASCENDING)
        limit(10)
    }
    .decodeList<ResultWithTournamentDto>()

// Команда (если есть)
val teamMembership = client.from("team_members")
    .select(Columns.raw("*, teams(name, sports(name))"))
    { filter { eq("user_id", athleteId) } }
    .decodeSingleOrNull<TeamMembershipDto>()
```

### ViewerTeamProfileScreen

```kotlin
val team = client.from("teams")
    .select(Columns.raw("*, sports(name), profiles!owner_id(name, avatar_url)"))
    { filter { eq("id", teamId) } }
    .decodeSingle<TeamFullDto>()

val members = client.from("team_members")
    .select(Columns.raw("*, profiles(name, avatar_url, city)"))
    { filter { eq("team_id", teamId) } }
    .decodeList<TeamMemberWithProfileDto>()
```

### ViewerProfileScreen

```kotlin
// Свой профиль
val profile = client.from("profiles")
    .select { filter { eq("id", userId) } }
    .decodeSingle<ProfileDto>()

// Обновить
client.from("profiles")
    .update({
        set("name", newName)
        set("phone", newPhone)
        set("city", newCity)
    }) { filter { eq("id", userId) } }
```

---

## Порядок работы

1. Создать `ViewerRepository.kt`
2. Создать DTO: `PlatformStatsDto`, `CommunityProfileDto`, `ResultWithAthleteDto`, etc.
3. Создать ViewModels (7 штук)
4. Обновить экраны: Home, Tournaments, TournamentDetail, TournamentResults, Community, PublicProfiles, Profile
5. News / NewsDetail — оставить на MockData (нет таблицы articles)
6. НЕ удалять ViewerMockData.kt
