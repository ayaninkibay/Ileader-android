# iLeader Android

Мобильный клиент спортивной платформы [iLeader](https://ileader.kz). Веб-версия и приложение работают на одной базе данных в Supabase, схема описана в разделе ниже. Приложение — мобильный клиент с **только необходимым** функционалом: просмотр турниров, регистрация, рейтинги, профили. Сложные CRUD-операции (создание турниров, статей, курсов) остаются на сайте.

> **Setup** — см. [SETUP.md](SETUP.md) для локального запуска.

---

## Стек

```
Kotlin 2.0.21 + Jetpack Compose + Material 3
├── Supabase 3.0.2 (PostGREST, Auth, Storage, Realtime)
├── Ktor 3.0.1 (HTTP)
├── Room 2.6.1 (offline-кэш)
├── Kotlinx Serialization 1.7.3
├── Coil 2.7.0 (картинки)
├── DataStore 1.1.1 (настройки)
├── Navigation Compose 2.8.4
└── minSdk=26, targetSdk=36, JDK 21
```

Архитектура: **MVVM** без DI (`viewModel { ... }` создаёт `Repository` напрямую внутри VM).

---

## Структура проекта

```
app/src/main/java/com/ileader/app/
├── MainActivity.kt          Точка входа: ILeaderTheme → UserProvider → AlertHost → NavGraph
├── data/
│   ├── bracket/             Алгоритмы турнирной сетки (single/double elim, RR, groups)
│   ├── local/               Room: AppDatabase, кэш
│   ├── models/              Domain-модели (User, Tournament, Team, …)
│   ├── notifications/       FCM: токен в profiles.fcm_token, каналы, deep-link
│   ├── preferences/         DataStore: тема, язык
│   ├── remote/
│   │   ├── SupabaseModule   Singleton Supabase-клиент. HttpTimeout 15s, retry 2× на 5xx/IOException
│   │   ├── UiState          Sealed: Loading / Success(T) / Error(message)
│   │   └── dto/             ~20 DTO-классов под Postgres-колонки
│   ├── repository/          19 репозиториев — все запросы к Supabase здесь
│   ├── session/             UserSession (текущий пользователь, singleton)
│   └── util/
│       ├── AppLogger        Логирование (Log.x + опц. алерт юзеру)
│       ├── AlertController  Глобальный канал snackbar-уведомлений
│       ├── MemoryCache      Process-wide TTL-кэш с in-flight dedup
│       └── safeApiCall      Логирующая обёртка для read-методов
└── ui/
    ├── components/          ~50 переиспользуемых composable (DarkCard, DarkButton, EmptyState, …)
    ├── navigation/          NavGraph (Welcome → Login/Register → Main), BottomNavItems по ролям
    ├── providers/           UserProvider + LocalCurrentUser (CompositionLocal с юзером)
    ├── screens/             71 экран, организованы по фичам (auth/, home/, tournaments/, profile/, …)
    ├── theme/               Color, Theme (ILeaderTheme, AppColorScheme), Type, Shadows
    └── viewmodels/          24 ViewModel, плоская папка
```

---

## Ключевые сущности

### `UserSession` ([data/session/UserSession.kt](app/src/main/java/com/ileader/app/data/session/UserSession.kt))
Singleton с `currentUser: StateFlow<User?>`. Источник правды о текущем пользователе. Никто не лазит в `client.auth.currentUserOrNull()` напрямую.
- `AuthViewModel` пишет туда после signIn/signUp/restore
- `signOut` → `clear()`
- В Composable: `LocalCurrentUser.current`
- В не-Composable: `UserSession.userId` / `UserSession.currentUser.value`

### `AlertController` + `Alerts` ([data/util/AlertController.kt](app/src/main/java/com/ileader/app/data/util/AlertController.kt))
Глобальный канал snackbar-уведомлений. Работает на любом экране, включая auth.
```kotlin
Alerts.error("Не удалось войти")
Alerts.success("Профиль сохранён")
Alerts.info("Регистрация открыта")
```
Корневой `AlertHost` (в `MainActivity`) подписывается на канал и показывает Snackbar поверх `NavGraph`.

### `AppLogger` ([data/util/AppLogger.kt](app/src/main/java/com/ileader/app/data/util/AppLogger.kt))
Единая точка логирования. `d/i` стрипаются в release, `w/e` остаются.
```kotlin
AppLogger.e("loadTournaments failed", e)
AppLogger.e("registerForTournament failed", e, alert = "Не удалось зарегистрироваться")  // лог + snackbar одной строкой
```
**Правила:** не логируй PII (email, phone, токены) даже на error. Передавай Exception вторым аргументом.

### `MemoryCache` ([data/util/MemoryCache.kt](app/src/main/java/com/ileader/app/data/util/MemoryCache.kt))
Process-wide TTL-кэш с in-flight dedup. TTL по типам данных:
- 30 мин — справочники (sports, licenses)
- 10 мин — профили, команды, локации
- 5 мин — детали турниров, статьи
- 2–3 мин — нотификации, инвайты
- 15–60 сек — live-данные (bracket, check-in)

На write-методах инвалидируй ключи вручную: `MemoryCache.invalidate("profile:$userId")`. На `signOut` весь кэш чистится автоматически.

### `SupabaseModule` ([data/remote/SupabaseModule.kt](app/src/main/java/com/ileader/app/data/remote/SupabaseModule.kt))
Singleton Supabase-клиент. HttpTimeout 15s, retry 2× на 5xx/IOException с exp-backoff. **4xx не ретраит** — это реальные ошибки.

---

## Тема

`ILeaderTheme(themeMode = LIGHT/DARK/SYSTEM)` оборачивает всё. Внутри живёт `LocalAppColors` (CompositionLocal с `AppColorScheme`: bg, cardBg, border, textPrimary, accent, …).

**Старый API**: `DarkTheme.X` объект из `components/DarkThemeComponents.kt` — backward-compat proxy, читает из `LocalAppColors.current`. **68+ экранов уже используют `DarkTheme.X`** — менять не нужно.

Цвета синхронизированы с веб-сайтом:
- light: bg `#f5f5f7`, cards `white`, border `#e5e5e5`
- dark: bg `#0a0a0a`, cards `#18181b`, border `#27272a`
- accent: `#E53535` (красный)

Тени отключены (плоский дизайн): `cardShadow()` / `floatingShadow()` — no-op.

---

## Навигация

```
NavGraph (корень)
├── Welcome → Login / Register / ForgotPassword
├── Onboarding (выбор спорта при первом входе)
└── Main
    └── MainScreen (floating bottom bar)
        ├── Home, Sport, Profile (всегда)
        ├── MyTournaments (athlete/trainer/organizer/referee/admin)
        ├── Media (роль media)
        └── Sponsor (роль sponsor)
```

Конкретные табы для роли — в [BottomNavItems.kt](app/src/main/java/com/ileader/app/ui/navigation/BottomNavItems.kt).

---

## Роли пользователей (9)

`user` (зритель), `athlete`, `trainer`, `organizer`, `referee`, `sponsor`, `media`, `content_manager`, `admin`. Часть ролей требует верификации админом (organizer/referee/sponsor/media).

---

## База данных (Supabase)

Общая с веб-сайтом. Подключение к Supabase: см. dashboard проекта `clkbmjsmfzjuqdwnoejv` для полной схемы. Основные сущности:
- **profiles, roles, user_sports** — пользователи и роли
- **sports, sport_exercises** — спорт и упражнения
- **teams, team_members, team_requests** — команды
- **tournaments, tournament_participants, tournament_results, tournament_matches** — турниры
- **bracket_matches, tournament_groups** — турнирная сетка
- **leagues, league_stages, league_standings** — лиги
- **locations, location_reviews** — локации
- **articles, courses, conversations, messages, notifications** — контент и общение

Запросы — через `SupabaseModule.client` внутри репозиториев. Колонки указывай явно (`Columns.raw("id, name, …")`), без `select("*")`.

---

## Конвенции

- **Язык UI**: русский, прямо в коде (нет `strings.xml`)
- **ViewModel**: `class FooViewModel : ViewModel()`, `_state: MutableStateFlow`, бизнес-логика в `viewModelScope.launch`
- **Repository**: создаётся напрямую в VM (`private val repo = FooRepository()`) — DI пока нет
- **State**: `UiState<T>` (Loading / Success(data) / Error(message))
- **Имена файлов**: фичевая таксономия в `ui/screens/` (`tournaments/`, `articles/`, `tickets/`, …). Папки-роли (`athlete/`, `media/`, `admin/`) — только для эксклюзивно ролевых экранов
- **Composable**: `XxxScreen(navParam, onBack, vm: XxxViewModel = viewModel())`
- **Цвета**: `DarkTheme.X` (не создавай новые `Color(0x...)` без надобности)
- **Анимации входа**: FadeIn staggered
- **Иконки**: `Icons.AutoMirrored.Filled.X` для RTL-чувствительных (ArrowBack, TrendingUp, Article)

---

## Демо-аккаунты для тестирования

| Роль | Email | Пароль |
|---|---|---|
| Admin | admin@mail.ru | demo-admin-2026 |
| Athlete | athlete@demo.com | 123456 |
| Trainer | trainer@demo.com | 123456 |
| Organizer | organizer@demo.com | 123456 |
| Referee | referee@demo.com | 123456 |
| Sponsor | sponsor@demo.com | 123456 |
| Media | media@demo.com | 123456 |
| Content Manager | content@demo.com | 123456 |
| User (зритель) | user@demo.com | 123456 |

Заходи через кнопку «Войти как …» на экране логина — пароли подставляются автоматически.

---

## Известные проблемы / TODO

- **DI отсутствует** — каждый VM создаёт свой Repository. Кандидат на Hilt.
- **Тестов нет** — ни unit, ни UI, ни integration.
- **28 экранов на моках** — `data/mock/` нужно постепенно подключить к Supabase.
- **FCM-уведомления** — боевые: `google-services.json` подключён, токен сохраняется в `profiles.fcm_token`, пуши доставляются через Edge Function `send-push` (триггер на insert в `notifications`).
- **Edge Function `create-user`** — нужна для создания юзеров из admin-панели.
- **`platform_settings`** — Admin Settings экран не сохраняет в БД.

---

## Куда смотреть дальше

- **Запустить локально** → [SETUP.md](SETUP.md)
- **Веб-сайт** (источник истины бизнес-логики, схема Supabase) → `C:\Users\zhami\Documents\ileader` (Next.js 16 + React 19)
- **Сервисы веб-сайта** в `src/lib/services/` — там же вся бизнес-логика запросов к БД, сверяйся с ними при написании репозиториев
