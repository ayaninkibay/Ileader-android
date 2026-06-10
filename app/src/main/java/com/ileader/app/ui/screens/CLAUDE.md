# ui/screens/ — экраны приложения

Контекст для Claude-чатов, работающих с экранами. ~71 файл в 24 фичевых папках.
Родительский контекст: `ui/CLAUDE.md`. Корневой: `/CLAUDE.md`.

## Таксономия — по фичам, не по ролям

История: было смесь "по ролям" (`athlete/`, `media/`, `admin/`) и "по фичам" (`detail/`, `common/`). В 2026-05 разнесли `common/` и `detail/` по фичам. Сейчас правило простое:

**Имя папки = имя фичи** (`tournaments/`, `articles/`, `courses/`, `tickets/`, ...). Папки, носящие имена ролей (`athlete/`, `media/`, `admin/`, `sponsor/`), содержат экраны эксклюзивно для этой роли. Если экран используется несколькими ролями — кладём в фичу, не в роль.

| Папка | Содержит | Комментарии |
|---|---|---|
| `auth/` | Welcome, Login, Register, ForgotPassword, MainPlaceholder, AuthViewModel | AuthViewModel здесь, а не в `viewmodels/` |
| `onboarding/` | OnboardingSportScreen | Первый вход — выбор спорта |
| `main/` | MainScreen | Контейнер с floating bottom bar, role-based |
| `home/` | HomeScreen, HomeTab | HomeScreen — собственно вёрстка, HomeTab — навигационный wrapper + sub-routes |
| `tournaments/` | TournamentDetailScreen | Просмотр одного турнира (публичный) |
| `mytournaments/` | MyTournamentsTab + create/edit + helper/referee/team/invites/locations management | Это «организаторская» админка турнира, не «мои регистрации» |
| `articles/` | ArticleDetailScreen | Один экран. Создание/редактирование статей — в `media/` |
| `media/` | MediaTab, MediaArticlesScreen, MediaArticleEditorScreen, MediaInterviews*, MediaAccreditations | Эксклюзивно для роли media |
| `profile/` | Profile, ProfileTab, EditProfile, Family, GoalCreate, GoalDetail + AthleteProfilePage, TrainerProfilePage, RefereeProfilePage, PublicProfileScreen | Личный кабинет + публичные профили |
| `teams/` | TeamDetailScreen | Один экран |
| `athlete/` | RacingLicense, RatingHistory, LapTimes, Achievements, ResultsHistory | Эксклюзивно для спортсменов |
| `sport/` | SportScreen, SportTab, RankingsScreen, FilterPopup, LeagueDetailScreen | LeagueDetailScreen здесь — это вью из спорт-таба (480 строк) |
| `leagues/` | LeaguesListScreen, LeagueDetailScreen | LeagueDetailScreen здесь — другая вью (277 строк), используется из HomeTab. Две отдельные импл, см. ниже |
| `chat/` | ConversationsListScreen, ChatScreen | |
| `courses/` | CoursesListScreen, CourseDetailScreen | |
| `notifications/` | NotificationsScreen | |
| `tickets/` | MyTicketsScreen, QrTicketScreen, QrScannerScreen | QR сканер для check-in здесь же |
| `checkin/` | ManualCheckInScreen | Ручной чек-ин (поиск по имени) |
| `referee/` | RefereeMatchesScreen | Экран матчей для судейства |
| `location/` | LocationDetailScreen, LocationReviewFormScreen | |
| `sponsor/` | SponsorTab, SponsorshipsScreen, SponsorTournamentSearchScreen | Эксклюзивно для роли sponsor |
| `admin/` | AdminSettingsScreen, AdminUsersScreen, AdminVerificationsScreen, AdminSportRequestsScreen | Эксклюзивно admin |
| `verification/` | VerificationRequestScreen | Запрос верификации (для organizer/referee/sponsor/media) |
| `common/` | PlaceholderScreen | Только то, что реально общее. Не свалка — клади в фичу |

## Дубликат имён: `LeagueDetailScreen` × 2

Файлы:
- `sport/LeagueDetailScreen.kt` (480 строк) — вызывается из `sport/SportTab.kt` через same-package resolution
- `leagues/LeagueDetailScreen.kt` (277 строк) — вызывается из `home/HomeTab.kt` через explicit import

Это **разные импл**, отображают одну сущность по-разному в зависимости от контекста (sport-таб vs home-таб). Не объединять без явного запроса.

## Шаблон таб-файла

Папки вроде `home/`, `sport/`, `media/`, `mytournaments/`, `profile/`, `sponsor/` содержат пару:
- `XxxScreen.kt` — собственно вёрстка таба (отображает данные)
- `XxxTab.kt` — навигационный wrapper, держит state с sub-route'ом (sealed class XxxNavState), переключает между основным экраном и под-экранами (детальные страницы из других папок)

`XxxTab` импортирует детальные экраны из других папок (`tournaments/TournamentDetailScreen`, `articles/ArticleDetailScreen`, `profile/PublicProfileScreen`, ...).

## Шаринг helper'ов между экранами

В `profile/AthleteProfilePage.kt` определены `internal fun`:
- `SectionCard(title, content)` — карточка-секция с заголовком
- `StatColumn(value, label)` — колонка статистики
- `ContactRow(icon, label, value)` — строка контактной информации
- `InfoChip(label, value)` — чип
- `TournamentRow(name, sport, ...)`
- `ResultRow(name, sport, position, points, date)`

Эти helper'ы переиспользуются:
- внутри `profile/` (все profile-pages в одном package) — без импорта
- в `tournaments/TournamentDetailScreen.kt` — через `import com.ileader.app.ui.screens.profile.SectionCard`
- в `teams/TeamDetailScreen.kt` — через imports

Если хочется добавить новый общий helper — клади рядом с существующими в `profile/AthleteProfilePage.kt` или вынеси в `ui/components/`.

## Конвенции экранов

- Сигнатура: `@Composable fun XxxScreen(navParam, onBack, vm: XxxViewModel = viewModel())`
- В начале файла — palette aliases как composable getter'ы:
  ```kotlin
  private val Bg: Color @Composable get() = DarkTheme.Bg
  private val CardBg: Color @Composable get() = DarkTheme.CardBg
  ```
- UiState: `when (val s = vm.state) { is Loading -> ...; is Error -> ...; is Success -> Content(s.data) }`
- FadeIn staggered анимации на вход
- Русские строки в коде

## Что НЕ должно появляться

- Папка `detail/` или `common/` (кроме `PlaceholderScreen` в common/) — раскладывай по фичам
- Создание/редактирование турниров/статей внутри `tournaments/`/`articles/` — это веб-only (но статьи редактируются в `media/`, потому что media — это специальная роль)
- Прямые вызовы `client.auth` — читай `UserSession.userId` / `LocalCurrentUser.current`
- Принятие `user: User` параметра в новых экранах — используй `LocalCurrentUser.current`. Существующие можно не трогать (legacy)
