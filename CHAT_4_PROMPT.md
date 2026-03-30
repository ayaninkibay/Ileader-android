Мы перестраиваем приложение iLeader Android на 4 таба. Полный план лежит в файле:
C:\Users\zhami\.claude\plans\immutable-greeting-pinwheel.md — прочитай его.

Также прочитай CLAUDE.md в корне проекта — там полный контекст по проекту, БД, конвенциям.

Ключевые правила:
- Русские строки в UI
- Используй DarkTheme.X для цветов (из DarkThemeComponents.kt)
- Используй существующие компоненты: DarkCard, SectionHeader, DarkSearchField, DarkSegmentedControl, EmptyState, LoadingScreen, ErrorScreen, FadeIn, MiniStat, RoleBadge, StatusBadge, DarkFormField, ILeaderInputField, BackHeader, ThemeSwitcherCard и т.д.
- ViewModel создаёт Repository напрямую (без Hilt): private val repo = SomeRepository()
- UiState<T> — sealed class: Loading, Success(data), Error(message)
- Запросы к Supabase через существующие Repository классы
- Palette aliases в экранах: private val Bg: Color @Composable get() = DarkTheme.Bg

---

# Твоя задача: Табы "Мои турниры" + "Профиль"

Ты создаёшь ТОЛЬКО НОВЫЕ файлы. Не модифицируй существующие файлы.

## Твои файлы (все создать с нуля)

```
app/src/main/java/com/ileader/app/ui/screens/mytournaments/MyTournamentsTab.kt
app/src/main/java/com/ileader/app/ui/screens/mytournaments/MyTournamentsScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/MyTournamentsViewModel.kt

app/src/main/java/com/ileader/app/ui/screens/profile/ProfileTab.kt
app/src/main/java/com/ileader/app/ui/screens/profile/ProfileScreen.kt
app/src/main/java/com/ileader/app/ui/screens/profile/EditProfileScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/ProfileViewModel.kt
```

## Часть 1: Мои турниры

### MyTournamentsTab.kt

Обёртка с внутренней навигацией. Sealed class `MyTournamentsNavState`: List, TournamentDetail(id), QrScanner(tournamentId).

Сигнатура: `@Composable fun MyTournamentsTab(user: User, onSignOut: () -> Unit)` — именно такая.

Для Detail-экрана импортируй `TournamentDetailScreen` из `com.ileader.app.ui.screens.detail` (создаёт другой чат). Сигнатура: `TournamentDetailScreen(tournamentId: String, user: User, onBack: () -> Unit)`.

Для QR-сканера используй существующий `QrScannerScreen` из `com.ileader.app.ui.screens.common`.

### MyTournamentsScreen.kt

Сигнатура: `@Composable fun MyTournamentsScreen(user: User, onTournamentClick: (String) -> Unit, onQrScan: (String) -> Unit)`

Экран в LazyColumn:
1. Сначала `when (user.role)` → показываем основную ролевую секцию
2. Потом ДОПОЛНИТЕЛЬНО проверяем helper-назначения — если есть, показываем секцию "Помощник" ПОД основным контентом

**ВАЖНО**: Helper — это НЕ роль в profiles.role. Это запись в таблице `tournament_helpers`. Пользователь может быть athlete И helper одновременно. Поэтому секция хелпера показывается В ДОПОЛНЕНИЕ к ролевой секции.

Контент по ролям:

**USER / SPONSOR / ADMIN / CONTENT_MANAGER**:
- SectionHeader("Мои турниры")
- `ViewerRepository().getMySpectatorRegistrations(user.id)` → список турниров как зритель
- Карточка: название турнира, спорт, дата, статус check-in
- EmptyState если пусто: "Вы пока не зарегистрированы как зритель"

**ATHLETE**:
- SectionHeader("Мои турниры")
- `AthleteRepository().getMyTournaments(user.id)` → зарегистрированные турниры
- Карточка: название, спорт, дата, статус участия. Tap → onTournamentClick
- Ниже: SectionHeader("Мои цели")
- `AthleteRepository().getGoals(user.id)` → цели
- Карточка цели: title, прогресс (DarkProgressBar), дедлайн, статус

**TRAINER**:
- SectionHeader("Турниры команды")
- `TrainerRepository().getMyTeams(user.id)` → команды → их турниры
- Карточка: название турнира, команда, дата

**ORGANIZER**:
- SectionHeader("Мои турниры")
- `OrganizerRepository().getMyTournaments(user.id)` → турниры организатора
- НЕТ кнопки создания турнира! Только просмотр списка.
- Карточка: название, статус, участники, дата. Tap → onTournamentClick (в detail можно редактировать ТОЛЬКО базовые поля: название, описание, даты, maxParticipants)
- На карточке: IconButton "QR" → onQrScan(tournamentId)

**REFEREE**:
- SectionHeader("Назначенные турниры")
- `RefereeRepository().getAssignedTournaments(user.id)` → назначенные
- Карточка: название, спорт, дата, роль судьи, матчи (total/completed). Tap → onTournamentClick

**MEDIA**:
- SectionHeader("Аккредитации")
- `MediaRepository().getMediaInvites(user.id)` → аккредитации
- Карточка: название турнира, статус аккредитации (pending/accepted/declined), дата

**Секция хелпера** (показывается ВСЕМ ролям если есть назначения):
- Загружается: `HelperRepository().getMyAssignments(user.id)`
- Если список не пуст:
  - SectionHeader("Помощник")
  - Карточки турниров-назначений
  - На каждой карточке: кнопки "QR Check-in" и "Ручной Check-in"

### MyTournamentsViewModel.kt

```kotlin
class MyTournamentsViewModel : ViewModel() {
    // Репозитории создаются по необходимости в зависимости от роли
    private val helperRepo = HelperRepository()

    data class MyTournamentsState(
        val roleTournaments: UiState<List<Any>> = UiState.Loading,
        val goals: UiState<List<AthleteGoal>>? = null, // только для athlete
        val helperAssignments: UiState<List<TournamentHelperDto>> = UiState.Loading
    )
}
```

Метод `load(userId: String, role: UserRole)`:
- when(role) → вызывает соответствующий Repository
- ПАРАЛЛЕЛЬНО загружает helperAssignments для ВСЕХ ролей
- Для ATHLETE дополнительно загружает goals

## Часть 2: Профиль

### ProfileTab.kt

Обёртка с sub-навигацией. Sealed class `ProfileNavState`: Main, Edit, Tickets, Notifications.

Сигнатура: `@Composable fun ProfileTab(user: User, onSignOut: () -> Unit)` — именно такая.

Навигация:
- Main → ProfileScreen
- Edit → EditProfileScreen (onBack → Main)
- Tickets → MyTicketsScreen из `com.ileader.app.ui.screens.common`
- Notifications → NotificationsScreen из `com.ileader.app.ui.screens.common`

### ProfileScreen.kt

Сигнатура: `@Composable fun ProfileScreen(user: User, onSignOut: () -> Unit, onEditProfile: () -> Unit, onTickets: () -> Unit, onNotifications: () -> Unit)`

Layout (LazyColumn или Column + verticalScroll):

1. **Header card** (DarkCard):
   - Аватар: AsyncImage 80dp круглый (или инициалы на accent фоне если нет аватара)
   - Имя: 24sp bold
   - Row: RoleBadge(user.role) + город (muted)

2. **Stats row** (DarkCard, Row с Arrangement.SpaceEvenly):
   - Зависит от роли:
   - athlete: MiniStat("Турниры", count), MiniStat("Победы", wins), MiniStat("Рейтинг", rating)
   - trainer: MiniStat("Команда", memberCount), MiniStat("Турниры", count)
   - referee: MiniStat("Матчи", count), MiniStat("Нарушения", count)
   - organizer: MiniStat("Турниры", count), MiniStat("Участники", count)
   - user и другие: MiniStat("Посещения", count)

3. **Quick Actions** (Column, каждый action — DarkCard-строка):
   Каждый action = Row(icon + text + Spacer + chevron right), clickable:
   - Icons.Default.Edit + "Редактировать профиль" → onEditProfile
   - Icons.Default.ConfirmationNumber + "Мои билеты" → onTickets
   - Icons.Default.Notifications + "Уведомления" → onNotifications
   - Icons.Default.SportsScore + "Виды спорта" → открыть BottomSheet с повторным выбором спорта (как онбординг, но ModalBottomSheet)
   - ThemeSwitcherCard() — вставить как есть, это существующий composable
   - Icons.Default.ExitToApp + "Выйти" (красный текст) → onSignOut, с confirmation AlertDialog

### EditProfileScreen.kt

Сигнатура: `@Composable fun EditProfileScreen(user: User, onBack: () -> Unit)`

Layout:
- BackHeader("Редактирование профиля", onBack)
- Аватар секция: AsyncImage 100dp + кнопка "Изменить фото" под ним. Для загрузки используй AvatarViewModel из `com.ileader.app.ui.viewmodels.AvatarViewModel`
- Форма (Column):
  - DarkFormField("Имя") + ILeaderInputField(value = name)
  - DarkFormField("Никнейм") + ILeaderInputField(value = nickname)
  - DarkFormField("Телефон") + ILeaderInputField(value = phone, keyboardType = Phone)
  - DarkFormField("Город") + ILeaderInputField(value = city)
  - DarkFormField("Страна") + ILeaderInputField(value = country)
  - DarkFormField("О себе") + ILeaderInputField(value = bio, singleLine = false, maxLines = 4)
- Кнопка "Сохранить" (accent, fillMaxWidth) — disabled если нет изменений
- Загрузка данных: `ViewerRepository().getProfile(user.id)`
- Сохранение: `ViewerRepository().updateProfile(user.id, ProfileUpdateDto(name, phone, city, country, bio, avatarUrl))`
- После успеха: Snackbar "Профиль обновлён" + onBack

### ProfileViewModel.kt

```kotlin
class ProfileViewModel : ViewModel() {
    private val viewerRepo = ViewerRepository()

    data class ProfileState(
        val profile: UiState<ProfileDto> = UiState.Loading,
        val stats: List<UserSportStatsDto> = emptyList(),
        val userSports: List<UserSportDto> = emptyList()
    )
}
```

Метод `load(userId: String)`:
- `viewerRepo.getProfile(userId)` → profile
- `viewerRepo.getUserSportStats(userId)` → stats (для athlete — wins, tournaments, rating)
- `viewerRepo.getUserSports(userId)` → userSports

## НЕ ТРОГАЙ

- MainScreen.kt, NavGraph.kt, BottomNavItems.kt — зона Чата 1
- Файлы в ui/screens/home/, ui/screens/detail/ — зона Чата 2
- Файлы в ui/screens/sport/ — зона Чата 3
- Существующие файлы — не модифицируй, только читай для справки

Приступай к реализации.
