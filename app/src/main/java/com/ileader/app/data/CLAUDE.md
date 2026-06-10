# data/ — слой данных

Контекст для Claude-чатов, работающих с data-слоем (репозитории, DTO, кэш, сессия).
Корневой контекст: `/CLAUDE.md` в корне проекта.

## Слои

```
data/
├── bracket/         BracketGenerator, BracketUtils — алгоритмы single/double elim, RR, groups
├── local/           Room: AppDatabase, DAO, cached entities. Используется для offline-фолбэка
├── models/          Domain-модели (User, Tournament, Team, ...) — то, что отдаём в UI
├── notifications/   FCM-заглушка. Боевой код закомментирован до подключения Firebase
├── preferences/     DataStore: ThemePreference, LanguagePreference
├── remote/
│   ├── SupabaseModule.kt   Singleton клиент. HttpTimeout 15s req / 10s connect / 15s socket. HttpRequestRetry 2× на 5xx/IOException, exp backoff 2.0 cap 3s. 4xx не ретраит
│   ├── UiState.kt          Sealed: Loading / Success(T) / Error(message)
│   └── dto/                ~20 файлов. Сериализационные типы. Часто имеют .toDomain() в companion
├── repository/      19 репозиториев. Read-методы возвращают DTO или domain; write-методы инвалидируют MemoryCache (см. ниже)
├── session/         UserSession — singleton с currentUser: StateFlow<User?>. AuthViewModel пишет на signIn/signUp/restore, чистит на signOut
└── util/
    ├── AppLogger.kt    Тонкая обёртка над android.util.Log
    ├── MemoryCache.kt  Process-wide TTL cache + in-flight dedup (см. ниже)
    └── safeApiCall.kt  Логирующий wrapper, для read-методов с try-catch
```

## MemoryCache — конвенция использования

`object MemoryCache.cached<T: Any>(key, ttlMs, loader)` → возвращает кешированное значение или вызывает loader и кеширует.

**TTL по типам данных** (агрессивные):
- 30 мин (1 800 000ms) — справочники (sports, licenses, sport_images, legal_pages)
- 10 мин (600 000ms) — профили, team, league details, courses, locations, user goals
- 5 мин (300 000ms) — лица турниров, results, статьи, community lists
- 2–3 мин (120–180 000ms) — league standings, notifications, invites, team requests
- 30–60s — live данные внутри события (results, спектаторы)
- 15–30s — bracket во время live scoring

**Ключи** — `kebab:colon:hierarchical`:
- `sports`, `legal_pages`, `platform_stats` — singletons
- `tournament:$id`, `bracket:$id`, `participants:$id`, `groups:$id` — per-tournament
- `profile:$id` (ProfileDto), `public_profile:$id` (ProfileDto with roles), `athlete:profile_domain:$id` (User domain) — заметь: разные типы → разные ключи (MemoryCache не type-safe across keys, ClassCastException если перепутать)
- `community:athletes`, `community:trainers`, `community:teams`, ... — глобальные списки
- `user:goals:$id`, `user:license:$id`, `user:sports:$id`, ... — per-user

**Инвалидация на write**:
```kotlin
suspend fun updateProfile(userId, data) {
    client.from("profiles").update(...)
    MemoryCache.invalidate("profile:$userId")
    MemoryCache.invalidate("public_profile:$userId")
    MemoryCache.invalidate("athlete:profile_domain:$userId")  // если есть отдельный ключ
}
```

Используй `invalidateMatching("prefix:")` когда write затрагивает много пользователей/турниров и id отдельно неизвестен.

**Nullable значения** — оборачивай в `listOf(it)` / `firstOrNull()` (MemoryCache требует `T: Any`):
```kotlin
suspend fun getLicense(userId: String): License? {
    val cached = MemoryCache.cached("athlete:license:$userId", 1_800_000L) {
        client.from("licenses").select(...).decodeList<LicenseDto>()
    }
    return cached.firstOrNull()?.toDomain()
}
```

**НЕ кешируй**:
- Search-методы (`searchAthletes(query)`, `searchTournaments(query)`) — query меняется по символу
- Live данные где stale = bug (CheckInRepository.getParticipant — флоу check-in)
- Chat messages — должны быть свежими
- Методы, принимающие composable id-list — кеш-ключ зависит от списка, неэффективно

`AuthViewModel.signOut()` вызывает `MemoryCache.clear()` — данные следующего пользователя гарантированно свежие.

## UserSession

Singleton, источник правды о текущем пользователе. Никто не лазит в `client.auth.currentUserOrNull()` напрямую — все читают `UserSession.currentUser` или `UserSession.userId`.

- `AuthViewModel.init { ... }` восстанавливает сессию → `UserSession.setUser(user)`
- `signIn` / `signUp` → `setUser(user)` после успешного входа
- `signOut` → `clear()`
- `UI` подписывается через `LocalCurrentUser.current` (см. `ui/providers/UserProvider`)

Не-Composable сервисы (репозитории, бэкграунд-задачи) могут читать `UserSession.userId` напрямую вместо принятия параметра.

## DTO ↔ Domain

DTO живут в `data/remote/dto/`, имеют поля с `@SerialName` для snake_case Postgres-колонок. Конверсия:
```kotlin
@Serializable data class TournamentDto(...) {
    fun toDomain(): Tournament = Tournament(...)
}
```

`DtoMappers.kt` — общие маппинги между DTO. UI работает с domain-моделями из `data/models/`, репозитории решают что отдавать (DTO для прямой передачи в view, domain для бизнес-логики).

## Supabase запросы

- Колонки указывай явно через `Columns.raw("id, name, ...")` или `select { ... }` — никогда `select("*")` в продовых местах
- JOIN'ы через `Columns.raw("*, sports(id, name), profiles!organizer_id(name)")`
- Параллельные independent reads — `coroutineScope { async { ... } }`
- N+1 — батчуй через `isIn("id", ids)` и потом `associateBy { it.id }`
- Count-only запросы — `count(Count.EXACT)` без декода строк
- Read-методы оборачивай в `safeApiCall("RepoName.methodName") { ... }` для единого логирования
