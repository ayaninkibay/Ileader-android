# ТЗ БД: Чат #7 — Admin (Администратор)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Admin к Supabase. Заменить `AdminMockData` на реальные запросы. Админ имеет полный доступ ко всем данным платформы.

**ВАЖНО:** Админ-функции (создание юзеров, смена ролей, блокировка) требуют серверных API-вызовов. В вебе это сделано через API routes (`/api/admin/*`). В Android нужно вызывать Supabase Edge Functions или те же API endpoints.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Все пользователи платформы |
| `roles` | R | Справочник ролей |
| `sports` | R/W | CRUD видов спорта |
| `tournaments` | R/W | Все турниры (удаление, смена статуса) |
| `tournament_participants` | R | Участники |
| `locations` | R/W | Все локации |
| `teams` | R | Все команды |
| `team_requests` | R/W | Заявки |
| `tournament_invites` | R/W | Все приглашения |
| `sponsorships` | R | Все спонсорства |
| `user_sports` | R/W | Виды спорта пользователей |
| `v_tournament_with_counts` | R | View турниров |
| `v_organizer_stats` | R | Статистика организаторов |

---

## Файлы для создания

### 1. `data/repository/AdminRepository.kt`

```kotlin
class AdminRepository {
    private val client = SupabaseModule.client

    // ── ДАШБОРД ──
    suspend fun getPlatformStats(): AdminPlatformStats
    suspend fun getRecentUsers(): List<ProfileDto>
    suspend fun getUsersByRole(): Map<String, Int>

    // ── ПОЛЬЗОВАТЕЛИ ──
    suspend fun getAllUsers(): List<ProfileWithRoleDto>
    suspend fun getUserDetail(userId: String): ProfileWithFullDataDto
    suspend fun updateUser(userId: String, data: AdminUserUpdateDto)
    suspend fun blockUser(userId: String)
    suspend fun unblockUser(userId: String)
    suspend fun verifyUser(userId: String)
    suspend fun rejectVerification(userId: String)
    suspend fun createUser(data: AdminCreateUserDto): String // через Edge Function

    // ── ВЕРИФИКАЦИЯ ──
    suspend fun getPendingVerifications(): List<ProfileDto>
    suspend fun approveVerification(userId: String)
    suspend fun declineVerification(userId: String)

    // ── ТУРНИРЫ ──
    suspend fun getAllTournaments(): List<TournamentWithDetailsDto>
    suspend fun deleteTournament(tournamentId: String)
    suspend fun updateTournamentStatus(tournamentId: String, status: String)

    // ── ЛОКАЦИИ ──
    suspend fun getAllLocations(): List<LocationDto>
    suspend fun deleteLocation(locationId: String)

    // ── ВИДЫ СПОРТА ──
    suspend fun getAllSports(): List<SportDto>
    suspend fun createSport(data: SportInsertDto): String
    suspend fun updateSport(sportId: String, data: SportUpdateDto)
    suspend fun toggleSportActive(sportId: String, isActive: Boolean)

    // ── ЗАЯВКИ / ПРИГЛАШЕНИЯ ──
    suspend fun getAllRequests(): AdminRequestsData
    suspend fun respondToRequest(type: String, requestId: String, approve: Boolean)

    // ── НАСТРОЙКИ ──
    suspend fun getPlatformSettings(): PlatformSettingsDto
    suspend fun updatePlatformSettings(data: PlatformSettingsDto)
}
```

### 2. Специфичные DTO

```kotlin
@Serializable
data class ProfileWithRoleDto(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val verification: String? = null,
    val status: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN с roles
    @SerialName("primary_role_id") val primaryRoleId: String? = null,
    val roles: RoleDto? = null // from JOIN
)

@Serializable
data class RoleDto(
    val id: String,
    val name: String,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class AdminUserUpdateDto(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val status: String? = null, // active/blocked
    val verification: String? = null, // verified/rejected
    @SerialName("primary_role_id") val primaryRoleId: String? = null
)

@Serializable
data class SportInsertDto(
    val name: String,
    val slug: String,
    @SerialName("athlete_label") val athleteLabel: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class SportUpdateDto(
    val name: String? = null,
    @SerialName("athlete_label") val athleteLabel: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

@Serializable
data class AdminPlatformStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val blockedUsers: Int,
    val pendingVerifications: Int,
    val totalTournaments: Int,
    val activeTournaments: Int,
    val totalSports: Int,
    val totalLocations: Int
)
```

### 3. ViewModels

```
ui/viewmodels/
  AdminDashboardViewModel.kt
  AdminUsersViewModel.kt
  AdminUserEditViewModel.kt
  AdminTournamentsViewModel.kt
  AdminRequestsViewModel.kt
  AdminSportsViewModel.kt
  AdminSettingsViewModel.kt
```

---

## Экран → Запросы

### AdminDashboardScreen

```kotlin
// Все пользователи (для подсчёта)
val users = client.from("profiles")
    .select(Columns.raw("id, status, verification, primary_role_id"))
    .decodeList<ProfileMinimalDto>()

val stats = AdminPlatformStats(
    totalUsers = users.size,
    activeUsers = users.count { it.status == "active" },
    blockedUsers = users.count { it.status == "blocked" },
    pendingVerifications = users.count { it.verification == "pending" },
    totalTournaments = client.from("tournaments").select(Columns.raw("id")).decodeList<IdOnlyDto>().size,
    activeTournaments = client.from("tournaments")
        .select(Columns.raw("id")) { filter { eq("status", "in_progress") } }
        .decodeList<IdOnlyDto>().size,
    totalSports = client.from("sports").select(Columns.raw("id")).decodeList<IdOnlyDto>().size,
    totalLocations = client.from("locations").select(Columns.raw("id")).decodeList<IdOnlyDto>().size
)

// Недавние регистрации
val recentUsers = client.from("profiles")
    .select(Columns.raw("*, roles!primary_role_id(name, display_name)"))
    {
        order("created_at", Order.DESCENDING)
        limit(10)
    }
    .decodeList<ProfileWithRoleDto>()

// Распределение по ролям
val roles = client.from("roles").select().decodeList<RoleDto>()
val roleDistribution = roles.associate { role ->
    role.name to users.count { it.primaryRoleId == role.id }
}
```

### AdminUsersScreen

```kotlin
// Все пользователи с ролью
val users = client.from("profiles")
    .select(Columns.raw("*, roles!primary_role_id(name, display_name)"))
    { order("created_at", Order.DESCENDING) }
    .decodeList<ProfileWithRoleDto>()

// Фильтрация на клиенте по role/status/verification/search
```

**Заблокировать пользователя:**
```kotlin
client.from("profiles")
    .update({ set("status", "blocked") })
    { filter { eq("id", userId) } }
```

**Разблокировать:**
```kotlin
client.from("profiles")
    .update({ set("status", "active") })
    { filter { eq("id", userId) } }
```

**Верифицировать:**
```kotlin
client.from("profiles")
    .update({ set("verification", "verified") })
    { filter { eq("id", userId) } }
```

### AdminUserCreateScreen

**Создание пользователя через Edge Function:**

В веб-проекте это делается через `/api/admin/users` POST, который вызывает Edge Function `create-user`. В Android нужно вызвать эту функцию напрямую:

```kotlin
// Вариант 1: Вызов Edge Function
val response = client.functions.invoke("create-user", buildJsonObject {
    put("email", email)
    put("password", password)
    put("name", name)
    put("role", role)
    put("phone", phone)
    put("city", city)
})

// Вариант 2: Вызов API напрямую через Ktor
// POST https://clkbmjsmfzjuqdwnoejv.supabase.co/functions/v1/create-user
```

**ВАЖНО:** Обычный `auth.signUpWith(Email)` НЕ подходит для админского создания пользователей — он зарегистрирует нового юзера и разлогинит текущего админа. Нужна серверная функция.

### AdminTournamentsScreen

```kotlin
// Все турниры
val tournaments = client.from("v_tournament_with_counts")
    .select()
    .decodeList<TournamentWithCountsDto>()

// Удалить турнир
client.from("tournaments")
    .delete { filter { eq("id", tournamentId) } }

// Изменить статус
client.from("tournaments")
    .update({ set("status", newStatus) })
    { filter { eq("id", tournamentId) } }
```

### AdminRequestsScreen

```kotlin
// Заявки на верификацию
val verifications = client.from("profiles")
    .select(Columns.raw("*, roles!primary_role_id(name, display_name)"))
    { filter { eq("verification", "pending") } }
    .decodeList<ProfileWithRoleDto>()

// Заявки в команды
val teamRequests = client.from("team_requests")
    .select(Columns.raw("*, profiles!user_id(name), teams(name)"))
    { filter { eq("status", "pending") } }
    .decodeList<TeamRequestWithDetailsDto>()

// Приглашения на турниры
val invites = client.from("tournament_invites")
    .select(Columns.raw("*, profiles!user_id(name), tournaments(name)"))
    { filter { eq("status", "pending") } }
    .decodeList<TournamentInviteWithDetailsDto>()

// Спонсорства pending
val sponsorships = client.from("sponsorships")
    .select(Columns.raw("*, profiles!sponsor_id(name), teams(name), tournaments(name)"))
    { filter { eq("status", "pending") } }
    .decodeList<SponsorshipWithDetailsDto>()
```

### AdminSportsScreen (если есть)

```kotlin
// Все виды спорта
val sports = client.from("sports")
    .select()
    .decodeList<SportDto>()

// Создать
client.from("sports").insert(SportInsertDto(
    name = "Шахматы",
    slug = "chess",
    athleteLabel = "Шахматист"
))

// Обновить
client.from("sports")
    .update({ set("name", newName) })
    { filter { eq("id", sportId) } }

// Вкл/выкл
client.from("sports")
    .update({ set("is_active", isActive) })
    { filter { eq("id", sportId) } }
```

### AdminSettingsScreen

Настройки платформы пока хранятся в mock-данных. В будущем можно создать таблицу `platform_settings` или хранить в отдельном конфиге. **Пока оставить на MockData.**

---

## Порядок работы

1. Создать `AdminRepository.kt`
2. Создать DTO: `ProfileWithRoleDto`, `RoleDto`, `AdminUserUpdateDto`, `SportInsertDto`, `AdminPlatformStats`
3. Создать ViewModels (7 штук)
4. Обновить экраны: Dashboard, Users, UserEdit, Tournaments, Requests, Settings
5. Для AdminUserCreateScreen — использовать Edge Function (или временно mock)
6. AdminSettingsScreen — оставить на MockData
7. НЕ удалять AdminMockData.kt
