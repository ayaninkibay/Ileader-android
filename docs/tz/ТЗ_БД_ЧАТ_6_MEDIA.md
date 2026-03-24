# ТЗ БД: Чат #6 — Media (СМИ)

> Прочитай `ОБЩЕЕ_ТЗ_БД.md` перед началом работы!

## Обзор

Подключить все экраны Media к Supabase. Заменить `MediaMockData` на реальные запросы.

**ВАЖНО:** В текущей БД НЕТ отдельных таблиц для статей/контента СМИ (articles, content). Контент СМИ пока существует только в mock-данных. Есть два варианта:

**Вариант А (рекомендуемый):** Оставить контент-экраны (Content, ContentDetail, ContentEdit, Analytics) на mock-данных. Подключить к БД только: Dashboard (статистика), Tournaments (аккредитация), Profile, Notifications.

**Вариант Б:** Использовать `profiles.role_data` (jsonb поле) для хранения статей. Не рекомендуется — jsonb не подходит для полноценного контент-менеджмента.

**Принимаем Вариант А.** Таблица `articles` будет добавлена в БД позже.

---

## Таблицы (чтение / запись)

| Таблица | R/W | Описание |
|---------|:---:|----------|
| `profiles` | R/W | Профиль СМИ |
| `tournaments` | R | Турниры (для аккредитации) |
| `tournament_invites` | R/W | Аккредитации (role = 'media') |
| `tournament_invite_codes` | R | Инвайт-коды |
| `tournament_participants` | R | Участники (для контента) |
| `tournament_results` | R | Результаты (для контента) |
| `sports` | R | Справочник спортов |
| `teams` | R | Команды (для контента) |
| `user_sports` | R | Виды спорта |
| `v_tournament_with_counts` | R | View турниров |

---

## Файлы для создания

### 1. `data/repository/MediaRepository.kt`

```kotlin
class MediaRepository {
    private val client = SupabaseModule.client

    // ── ДАШБОРД ──
    suspend fun getDashboardStats(userId: String): MediaDashboardStats
    suspend fun getUpcomingTournaments(): List<TournamentWithDetailsDto>

    // ── ТУРНИРЫ ──
    suspend fun getAvailableTournaments(): List<TournamentWithAccreditationDto>
    suspend fun getMyAccreditations(userId: String): List<TournamentInviteWithDetailsDto>
    suspend fun requestAccreditation(tournamentId: String, userId: String, message: String?)
    suspend fun joinByInviteCode(code: String, userId: String)

    // ── ПРОФИЛЬ ──
    suspend fun getProfile(userId: String): ProfileDto
    suspend fun updateProfile(userId: String, data: ProfileUpdateDto)

    // ── УВЕДОМЛЕНИЯ ──
    suspend fun getInvites(userId: String): List<TournamentInviteWithDetailsDto>
    suspend fun respondToInvite(inviteId: String, accept: Boolean)
}
```

### 2. ViewModels

```
ui/viewmodels/
  MediaDashboardViewModel.kt     → Dashboard
  MediaTournamentsViewModel.kt    → Tournaments (аккредитации)
  MediaProfileViewModel.kt        → Profile
  MediaNotificationsViewModel.kt  → Notifications
  // Content, ContentDetail, ContentEdit, Analytics — оставить на MockData
```

---

## Экран → Запросы

### MediaDashboardScreen

```kotlin
// Статистика аккредитаций
val accreditations = client.from("tournament_invites")
    .select {
        filter {
            eq("user_id", userId)
            eq("role", "media")
        }
    }
    .decodeList<TournamentInviteDto>()

val stats = MediaDashboardStats(
    totalAccreditations = accreditations.size,
    approvedAccreditations = accreditations.count { it.status == "accepted" },
    pendingAccreditations = accreditations.count { it.status == "pending" },
    // Статьи — пока из MockData
    totalArticles = MediaMockData.totalArticles,
    publishedArticles = MediaMockData.publishedArticles
)

// Предстоящие турниры для освещения
val tournaments = client.from("v_tournament_with_counts")
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
```

### MediaTournamentsScreen

```kotlin
// Все публичные турниры + мой статус аккредитации
val tournaments = client.from("v_tournament_with_counts")
    .select { filter { eq("visibility", "public") } }
    .decodeList<TournamentWithCountsDto>()

// Мои аккредитации
val myAccreditations = client.from("tournament_invites")
    .select {
        filter {
            eq("user_id", userId)
            eq("role", "media")
        }
    }
    .decodeList<TournamentInviteDto>()

// Маппинг: для каждого турнира проверить, есть ли аккредитация
// tournaments.map { t ->
//     val accr = myAccreditations.find { it.tournamentId == t.id }
//     TournamentWithAccreditation(t, accr?.status)
// }
```

**Запросить аккредитацию:**
```kotlin
client.from("tournament_invites").insert(mapOf(
    "tournament_id" to tournamentId,
    "user_id" to userId,
    "role" to "media",
    "direction" to "incoming", // заявка от СМИ
    "status" to "pending",
    "message" to message
))
```

### MediaContentScreen / ContentDetailScreen / ContentEditScreen

**Оставить на MockData.** Добавить комментарий:
```kotlin
// TODO: Подключить к БД когда будет создана таблица articles
// Сейчас используются данные из MediaMockData
```

### MediaAnalyticsScreen

**Оставить на MockData.** Аналитика привязана к контенту, которого нет в БД.

### MediaProfileScreen

```kotlin
// Профиль
val profile = client.from("profiles")
    .select { filter { eq("id", userId) } }
    .decodeSingle<ProfileDto>()

// Виды спорта
val sports = client.from("user_sports")
    .select(Columns.raw("*, sports(name, slug)"))
    { filter { eq("user_id", userId) } }
    .decodeList<UserSportWithNameDto>()

// role_data содержит доп. данные (media_type, organization, etc.)
// Парсить из profile.roleData (jsonb)
```

**Обновить профиль:**
```kotlin
client.from("profiles")
    .update({
        set("name", newName)
        set("phone", newPhone)
        set("city", newCity)
        set("bio", newBio)
        // Обновить role_data для медиа-специфичных полей
        set("role_data", buildJsonObject {
            put("media_type", mediaType) // newspaper/tv/online/radio/blog
            put("organization", organization)
        }.toString())
    }) { filter { eq("id", userId) } }
```

### MediaNotificationsScreen

```kotlin
// Ответы на заявки аккредитации
val invites = client.from("tournament_invites")
    .select(Columns.raw("*, tournaments(name, start_date, sports(name))"))
    {
        filter {
            eq("user_id", userId)
            eq("role", "media")
        }
        order("updated_at", Order.DESCENDING)
    }
    .decodeList<TournamentInviteWithDetailsDto>()
```

### MediaTeamScreen

```kotlin
// Команда (если есть teamId)
val membership = client.from("team_members")
    .select(Columns.raw("*, teams(*, sports(name), profiles!owner_id(name))"))
    { filter { eq("user_id", userId) } }
    .decodeSingleOrNull<TeamMembershipDto>()
// Если null → у пользователя нет команды
```

---

## Порядок работы

1. Создать `MediaRepository.kt` (небольшой — ~10 методов)
2. Создать ViewModels только для: Dashboard, Tournaments, Profile, Notifications
3. Обновить экраны Dashboard, Tournaments, Profile, Notifications
4. Content/ContentDetail/ContentEdit/Analytics — оставить на MockData, добавить TODO-комментарии
5. НЕ удалять MediaMockData.kt
