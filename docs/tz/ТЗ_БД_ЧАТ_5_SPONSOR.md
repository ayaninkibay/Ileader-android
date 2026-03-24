# ТЗ БД: Чат #5 — Sponsor (Спонсор)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Sponsor к Supabase. Заменить `SponsorMockData` на реальные запросы.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль спонсора |
| `sponsorships` | R/W | Спонсорские контракты |
| `teams` | R | Команды (для спонсирования) |
| `team_members` | R | Участники команд |
| `tournaments` | R | Турниры |
| `tournament_participants` | R | Участники турниров |
| `tournament_results` | R | Результаты |
| `tournament_invites` | R/W | Приглашения (incoming/outgoing) |
| `sports` | R | Справочник спортов |
| `v_tournament_with_counts` | R | View турниров |

---

## Файлы для создания

### 1. `data/repository/SponsorRepository.kt`

```kotlin
class SponsorRepository {
    private val client = SupabaseModule.client

    // ── ДАШБОРД ──
    suspend fun getDashboardStats(userId: String): SponsorDashboardStats
    suspend fun getActiveSponsorships(userId: String): List<SponsorshipWithDetailsDto>

    // ── КОМАНДЫ ──
    suspend fun getAvailableTeams(sportFilter: String? = null): List<TeamWithStatsDto>
    suspend fun getSponsoredTeams(userId: String): List<SponsorshipWithTeamDto>
    suspend fun getTeamMembers(teamId: String): List<TeamMemberWithProfileDto>
    suspend fun requestTeamSponsorship(sponsorId: String, teamId: String, amount: Double?)

    // ── ТУРНИРЫ ──
    suspend fun getAvailableTournaments(): List<TournamentWithDetailsDto>
    suspend fun getSponsoredTournaments(userId: String): List<SponsorshipWithTournamentDto>
    suspend fun getTournamentDetail(tournamentId: String): TournamentFullDto
    suspend fun requestTournamentSponsorship(sponsorId: String, tournamentId: String, tier: String, amount: Double?)

    // ── ПРИГЛАШЕНИЯ ──
    suspend fun getInvites(userId: String): List<TournamentInviteWithDetailsDto>
    suspend fun respondToInvite(inviteId: String, accept: Boolean)

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)
}
```

### 2. Специфичные DTO

```kotlin
@Serializable
data class SponsorshipDto(
    val id: String? = null,
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    val tier: String = "partner", // platinum/gold/silver/bronze/partner
    val amount: Double? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val status: String = "pending", // pending/active/expired/cancelled
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SponsorshipInsertDto(
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    val tier: String = "partner",
    val amount: Double? = null,
    val status: String = "pending"
)
```

### 3. ViewModels

```
ui/viewmodels/
  SponsorDashboardViewModel.kt
  SponsorTeamsViewModel.kt
  SponsorTournamentsViewModel.kt
  SponsorTournamentDetailViewModel.kt
  SponsorNotificationsViewModel.kt
  SponsorProfileViewModel.kt
```

---

## Экран → Запросы

### SponsorDashboardScreen

```kotlin
// Мои спонсорства
val sponsorships = client.from("sponsorships")
    .select(Columns.raw("*, teams(name, sports(name)), tournaments(name, sports(name))"))
    { filter { eq("sponsor_id", userId) } }
    .decodeList<SponsorshipWithDetailsDto>()

// Статистика
val stats = SponsorDashboardStats(
    totalSponsorships = sponsorships.size,
    teamSponsorships = sponsorships.count { it.teamId != null },
    tournamentSponsorships = sponsorships.count { it.tournamentId != null },
    totalInvested = sponsorships.sumOf { it.amount ?: 0.0 },
    activeSponsorships = sponsorships.count { it.status == "active" }
)

// Приглашения (pending)
val pendingInvites = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date)"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "sponsor")
            eq("status", "pending")
        }
    }
    .decodeList<TournamentInviteWithDetailsDto>()
```

### SponsorTeamsScreen

```kotlin
// Доступные команды для спонсирования
val teams = client.from("teams")
    .select(Columns.raw("*, sports(name), profiles!owner_id(name), team_members(count)"))
    { filter { eq("is_active", true) } }
    .decodeList<TeamWithStatsDto>()

// Фильтрация по спорту на клиенте

// Мои спонсированные команды
val myTeams = client.from("sponsorships")
    .select(Columns.raw("*, teams(*, sports(name), profiles!owner_id(name))"))
    {
        filter {
            eq("sponsor_id", userId)
            not("team_id", "is", "null")
        }
    }
    .decodeList<SponsorshipWithTeamDto>()
```

**Спонсировать команду:**
```kotlin
// ВАЖНО: запрос идёт тренеру (owner) на подтверждение
// Создаём sponsorship со статусом "pending"
client.from("sponsorships").insert(SponsorshipInsertDto(
    sponsorId = userId,
    teamId = teamId,
    amount = amount,
    tier = "partner",
    status = "pending"
))
// Тренер увидит запрос в своих уведомлениях и может принять/отклонить
```

### SponsorTournamentsScreen

```kotlin
// Доступные турниры (public, registration_open или in_progress)
val tournaments = client.from("v_tournament_with_counts")
    .select {
        filter {
            eq("visibility", "public")
            or {
                eq("status", "registration_open")
                eq("status", "in_progress")
            }
        }
    }
    .decodeList<TournamentWithCountsDto>()

// Мои спонсированные турниры
val myTournaments = client.from("sponsorships")
    .select(Columns.raw("*, tournaments(*, sports(name), locations(name))"))
    {
        filter {
            eq("sponsor_id", userId)
            not("tournament_id", "is", "null")
        }
    }
    .decodeList<SponsorshipWithTournamentDto>()
```

### SponsorTournamentDetailScreen

```kotlin
// Детали турнира
val tournament = client.from("tournaments")
    .select(Columns.raw("*, sports(name), locations(*), profiles!organizer_id(name)"))
    { filter { eq("id", tournamentId) } }
    .decodeSingle<TournamentFullDto>()

// Участники
val participants = client.from("tournament_participants")
    .select(Columns.raw("*, profiles(name, avatar_url)"))
    { filter { eq("tournament_id", tournamentId); eq("status", "confirmed") } }
    .decodeList<ParticipantWithProfileDto>()

// Результаты (если completed)
val results = client.from("tournament_results")
    .select(Columns.raw("*, profiles!athlete_id(name)"))
    { filter { eq("tournament_id", tournamentId) }; order("position") }
    .decodeList<ResultWithAthleteDto>()

// Мое спонсорство этого турнира
val mySponsorship = client.from("sponsorships")
    .select {
        filter {
            eq("sponsor_id", userId)
            eq("tournament_id", tournamentId)
        }
    }
    .decodeSingleOrNull<SponsorshipDto>()
```

### SponsorNotificationsScreen

```kotlin
// Приглашения от организаторов
val invites = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date, sports(name))"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "sponsor")
        }
        order("created_at", Order.DESCENDING)
    }
    .decodeList<TournamentInviteWithDetailsDto>()

// Статусы спонсорств (ответы на мои заявки)
val sponsorships = client.from("sponsorships")
    .select(Columns.raw("*, teams(name), tournaments(name)"))
    {
        filter { eq("sponsor_id", userId) }
        order("created_at", Order.DESCENDING)
    }
    .decodeList<SponsorshipWithDetailsDto>()
```

### SponsorProfileScreen

Аналогично — `profiles` + Storage для аватара/лого.

---

## Бизнес-логика: Спонсорство команд

1. Спонсор нажимает "Спонсировать команду"
2. Создаётся `sponsorships` с `status = "pending"`
3. Тренер (owner команды) видит pending запросы
4. Тренер принимает → `status = "active"` или отклоняет → `status = "cancelled"`

**Тиеры (platinum/gold/silver/bronze/partner) скрыты из UI — всё "partner" по умолчанию.**

---

## Порядок работы

1. Создать `SponsorRepository.kt`
2. Создать DTO: `SponsorshipDto`, `SponsorshipInsertDto`
3. Создать ViewModels (6 штук)
4. Обновить экраны
5. НЕ удалять SponsorMockData.kt
