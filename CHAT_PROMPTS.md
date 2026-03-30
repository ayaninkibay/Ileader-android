# Задания для 4 параллельных Claude-чатов

> **Правило**: каждый чат работает ТОЛЬКО со своими файлами. Файлы другого чата — НЕ ТРОГАТЬ.
> **Порядок**: Чат 1 запускается первым (или одновременно). Чаты 2-4 создают только НОВЫЕ файлы, конфликтов нет.
> **Сборка**: после завершения всех чатов — один `assembleDebug` для проверки.

---

## Общий контекст (вставить в каждый чат перед заданием)

```
Мы перестраиваем приложение iLeader Android на 4 таба. Полный план лежит в файле:
C:\Users\zhami\.claude\plans\immutable-greeting-pinwheel.md — прочитай его.

Также прочитай CLAUDE.md в корне проекта — там полный контекст по проекту, БД, конвенциям.

Ключевые правила:
- Русские строки в UI
- Используй DarkTheme.X для цветов (из DarkThemeComponents.kt)
- Используй существующие компоненты: DarkCard, SectionHeader, DarkSearchField, DarkSegmentedControl, EmptyState, LoadingScreen, ErrorScreen, FadeIn, MiniStat, RoleBadge, StatusBadge, DarkFormField, ILeaderInputField и т.д.
- ViewModel создаёт Repository напрямую (без Hilt): private val repo = SomeRepository()
- UiState<T> — sealed class: Loading, Success(data), Error(message)
- Запросы к Supabase через существующие Repository классы
- Palette aliases в экранах: private val Bg: Color @Composable get() = DarkTheme.Bg

Работай ТОЛЬКО со своими файлами. Не трогай файлы из чужих зон.
```

---

## ЧАТ 1: Фундамент — Навигация + Онбординг

**Зона ответственности** — ты единственный кто МОДИФИЦИРУЕТ существующие файлы:

### Твои файлы

**Создать:**
- `app/src/main/java/com/ileader/app/data/preferences/SportPreference.kt`
- `app/src/main/java/com/ileader/app/ui/screens/onboarding/OnboardingSportScreen.kt`
- `app/src/main/java/com/ileader/app/ui/viewmodels/OnboardingViewModel.kt`

**Модифицировать:**
- `app/src/main/java/com/ileader/app/data/preferences/ThemePreference.kt` — вынести DataStore в `val Context.ileaderDataStore` (public), чтобы SportPreference мог его использовать
- `app/src/main/java/com/ileader/app/ui/navigation/BottomNavItems.kt` — переписать на 4 фиксированных таба
- `app/src/main/java/com/ileader/app/ui/screens/main/MainScreen.kt` — убрать RoleScreenRouter, сделать TabRouter на 4 таба
- `app/src/main/java/com/ileader/app/ui/navigation/NavGraph.kt` — добавить маршрут Onboarding

### Задание

1. **SportPreference.kt**: DataStore хранит `selected_sport_ids` (List<String> сериализованный в JSON) и `selected_sport_names`. Использует тот же DataStore что и ThemePreference (`ileader_settings`). Для этого в ThemePreference.kt замени `private val Context.dataStore` на `val Context.ileaderDataStore` (public). Методы: `setSports(ids: List<String>, names: List<String>)`, `clearSports()`, `selectedSportIds: Flow<List<String>>`.

2. **OnboardingViewModel.kt**: Загружает спорты через `ViewerRepository().getSports()`. State: `UiState<List<SportDto>>` для списка спортов, `selectedSportIds: Set<String>` для выбранных (1-3 штуки), `isSaving: Boolean`. Метод `saveSports(userId: String, context: Context)` — сохраняет в `user_sports` таблицу + SportPreference.

3. **OnboardingSportScreen.kt**: Полноэкранный экран выбора спорта. Заголовок "Выберите вид спорта" + подзаголовок "Выберите 1-3 вида спорта". Сетка 2x4 карточек спорта (sportIcon/sportEmoji + name). Выбранные подсвечиваются accent цветом. Кнопка "Продолжить" (disabled если ничего не выбрано). После сохранения вызывает `onComplete()`.

4. **BottomNavItems.kt**: Убрать ВСЮ функцию `getBottomNavItems(role, ...)`. Заменить на:
```kotlin
fun getBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem("home", "Главная", Icons.Default.Home),
    BottomNavItem("sport", "Спорт", Icons.Default.Search),
    BottomNavItem("my_tournaments", "Мои турниры", Icons.Default.EmojiEvents),
    BottomNavItem("profile", "Профиль", Icons.Default.Person)
)
```

5. **MainScreen.kt**: Убрать все импорты ролевых экранов. Убрать `RoleScreenRouter`. Убрать `HelperViewModel`, `TicketsViewModel`. Новый `TabRouter`:
```kotlin
when (selectedRoute) {
    "home" -> HomeTab(user = user)
    "sport" -> SportTab(user = user)
    "my_tournaments" -> MyTournamentsTab(user = user, onSignOut = onSignOut)
    "profile" -> ProfileTab(user = user, onSignOut = onSignOut)
}
```
**ВАЖНО**: Чаты 2-4 создадут эти composable функции (HomeTab, SportTab, MyTournamentsTab, ProfileTab). Пока они не готовы, используй временные placeholder'ы:
```kotlin
// Временные placeholder'ы — будут заменены реальными экранами
@Composable fun HomeTab(user: User) { PlaceholderScreen("Главная", user, {}) }
@Composable fun SportTab(user: User) { PlaceholderScreen("Спорт", user, {}) }
@Composable fun MyTournamentsTab(user: User, onSignOut: () -> Unit) { PlaceholderScreen("Мои турниры", user, onSignOut) }
@Composable fun ProfileTab(user: User, onSignOut: () -> Unit) { PlaceholderScreen("Профиль", user, onSignOut) }
```
Размести их прямо в MainScreen.kt в конце файла, потом чаты 2-4 удалят их и заменят своими.

6. **NavGraph.kt**: Добавить `Screen.Onboarding` в sealed class. В `LaunchedEffect(authState.isAuthenticated)` — после auth проверять `authState.currentUser?.sportIds.isNullOrEmpty()`. Если true → navigate to Onboarding, иначе → Main. Добавить `composable(Screen.Onboarding.route)` с OnboardingSportScreen.

### НЕ трогай
- Файлы auth экранов (Login, Register, Welcome, ForgotPassword)
- DarkThemeComponents.kt
- Любые файлы в ui/screens/athlete/, trainer/, organizer/, referee/, media/, viewer/, helper/
- Repository и DTO файлы

---

## ЧАТ 2: Таб "Главная" + Детальные экраны

**Зона ответственности** — ТОЛЬКО НОВЫЕ файлы:

### Твои файлы (все создать с нуля)

```
app/src/main/java/com/ileader/app/ui/screens/home/HomeTab.kt
app/src/main/java/com/ileader/app/ui/screens/home/HomeScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/HomeViewModel.kt

app/src/main/java/com/ileader/app/ui/screens/detail/TournamentDetailScreen.kt
app/src/main/java/com/ileader/app/ui/screens/detail/ArticleDetailScreen.kt
app/src/main/java/com/ileader/app/ui/screens/detail/PublicProfileScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/TournamentDetailViewModel.kt
app/src/main/java/com/ileader/app/ui/viewmodels/ArticleDetailViewModel.kt
app/src/main/java/com/ileader/app/ui/viewmodels/PublicProfileViewModel.kt
```

### Задание

**HomeTab.kt** — обёртка с внутренней навигацией:
```kotlin
@Composable
fun HomeTab(user: User) {
    // Внутренняя навигация: Home → Detail screens
    var navState by remember { mutableStateOf<HomeNavState>(HomeNavState.Home) }
    when (val state = navState) {
        HomeNavState.Home -> HomeScreen(user, onArticleClick = { navState = HomeNavState.ArticleDetail(it) }, ...)
        is HomeNavState.ArticleDetail -> ArticleDetailScreen(articleId = state.id, onBack = { navState = HomeNavState.Home })
        is HomeNavState.TournamentDetail -> TournamentDetailScreen(tournamentId = state.id, user = user, onBack = { navState = HomeNavState.Home })
        is HomeNavState.PublicProfile -> PublicProfileScreen(userId = state.id, onBack = { navState = HomeNavState.Home })
    }
}
```

**HomeScreen.kt** — три секции в `LazyColumn`:
1. **Новости**: `SectionHeader("Новости iLeader", "Все")` + `LazyRow` карточек. Источник: `ViewerRepository.getRecentArticles(5)`. Карточка: AsyncImage (cover), title, excerpt, дата. Tap → onArticleClick(id).
2. **Турниры**: `SectionHeader("Ближайшие турниры")` + `LazyRow`. Источник: `ViewerRepository.getUpcomingTournaments(10)`, фильтр клиент-сайд по sportIds из SportPreference. Сортировка по startDate ASC, только status = registration_open или in_progress. Карточка: название, спорт, дата, локация, статус-бадж. Tap → onTournamentClick(id).
3. **Люди**: `SectionHeader("Люди")` + `LazyRow`. Источник: `ViewerRepository.getAthletes()` (take 10). Карточка: аватар (или инициалы), имя, спорт, рейтинг. Tap → onProfileClick(id).

**HomeViewModel.kt**:
- `private val repo = ViewerRepository()`
- Загрузка через `viewModelScope.launch { coroutineScope { async { ... } } }` — все три секции параллельно.
- State: `data class HomeState(news: UiState<List<ArticleDto>>, tournaments: UiState<List<TournamentWithCountsDto>>, people: UiState<List<CommunityProfileDto>>)`
- Метод `load(sportIds: List<String>)` — загрузка + фильтрация турниров по sportIds.

**TournamentDetailScreen.kt** — адаптация из существующего `ViewerTournamentDetailScreen`. Прочитай его для вдохновления, но пиши новый файл. Показывает:
- Инфо (название, спорт, даты, локация, описание, организатор)
- Список участников
- Bracket (если есть) — используй существующие компоненты из `ui/components/bracket/`
- Результаты (если completed)
- **Action-кнопки** зависят от роли + статуса (см. таблицу в плане):
  - athlete + registration_open + не зарегистрирован → "Зарегистрироваться"
  - athlete + зарегистрирован → "Отменить регистрацию"
  - user + registration_open → "Зарегистрироваться как зритель"
  - organizer (свой) → "Редактировать"
  - referee (назначен) + in_progress → "Судейство"
  - Иначе → нет кнопки

**TournamentDetailViewModel.kt**: Загружает через ViewerRepository: tournament detail, participants, bracket, results, groups. Проверяет регистрацию пользователя. Методы: `registerAsParticipant()`, `registerAsSpectator()`, `unregister()`.

**ArticleDetailScreen.kt**: Cover image (AsyncImage, fillMaxWidth), заголовок, автор + дата, контент. BackHeader сверху.

**ArticleDetailViewModel.kt**: `ViewerRepository.getArticleDetail(articleId)`. State: `UiState<ArticleDto>`.

**PublicProfileScreen.kt**: Header с аватаром + имя + роль + город. Спортивная статистика (UserSportStatsDto). Результаты турниров (ResultDto). Команда (если есть). BackHeader.

**PublicProfileViewModel.kt**: Загружает через ViewerRepository: profile, userSports, sportStats, results, teamMembership.

### SportPreference — чтение
Для чтения sportIds в HomeScreen используй:
```kotlin
val context = LocalContext.current
val sportPref = remember { SportPreference(context) }
val sportIds by sportPref.selectedSportIds.collectAsState(initial = emptyList())
```

### НЕ трогай
- MainScreen.kt, NavGraph.kt, BottomNavItems.kt (это Чат 1)
- Файлы в sport/, mytournaments/, profile/ (это Чаты 3 и 4)
- Существующие экраны в viewer/, athlete/ и т.д. (они будут удалены позже)

---

## ЧАТ 3: Таб "Спорт" (Поиск/Фильтры)

**Зона ответственности** — ТОЛЬКО НОВЫЕ файлы:

### Твои файлы (все создать с нуля)

```
app/src/main/java/com/ileader/app/ui/screens/sport/SportTab.kt
app/src/main/java/com/ileader/app/ui/screens/sport/SportScreen.kt
app/src/main/java/com/ileader/app/ui/screens/sport/FilterPopupScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/SportViewModel.kt
```

### Задание

**SportTab.kt** — обёртка с внутренней навигацией (аналогично HomeTab):
```kotlin
@Composable
fun SportTab(user: User) {
    var navState by remember { mutableStateOf<SportNavState>(SportNavState.Search) }
    when (val state = navState) {
        SportNavState.Search -> SportScreen(user, onTournamentClick = {...}, onArticleClick = {...}, onProfileClick = {...})
        is SportNavState.TournamentDetail -> TournamentDetailScreen(...)
        is SportNavState.ArticleDetail -> ArticleDetailScreen(...)
        is SportNavState.PublicProfile -> PublicProfileScreen(...)
    }
}
```
Для Detail-экранов импортируй из `com.ileader.app.ui.screens.detail.*` — их создаёт Чат 2.

**SportScreen.kt**:
- Верх: `DarkSearchField` (onChange → viewModel.search(query)) + кнопка-иконка фильтра (Settings или FilterList)
- `DarkSegmentedControl` с 3 вкладками: "Турниры", "Люди", "Новости"
- Контент: `LazyColumn` по активной вкладке
  - Турниры: карточки TournamentWithCountsDto (название, спорт, дата, статус, город, участники)
  - Люди: карточки CommunityProfileDto (аватар, имя, спорт, рейтинг, город)
  - Новости: карточки ArticleDto (cover, title, excerpt, категория, дата)
- Внизу списка: кнопка "Загрузить ещё" (если есть ещё данные)
- Клик на иконку фильтра → `showFilterPopup = true`

**FilterPopupScreen.kt** — full-screen popup (Dialog fillMaxSize или отдельный composable):
- Фильтры динамические по активной вкладке SportScreen:
  - **Турниры**: Спорт (dropdown из ViewerRepository.getSports()), Статус (chips: registration_open, in_progress, completed), Город (текстовое поле), Возрастная категория (dropdown: children, youth, adult)
  - **Люди**: Спорт (dropdown), Город (текст), Роль (chips: Спортсмен, Тренер, Судья)
  - **Новости**: Спорт (dropdown), Категория (chips из news categories)
- Кнопки "Применить" и "Сбросить"
- Используй DarkFormField, FormDropdown, DarkFilterChip из DarkThemeComponents

**SportViewModel.kt**:
- State:
```kotlin
data class SportState(
    val activeTab: SportSubTab = SportSubTab.TOURNAMENTS,
    val searchQuery: String = "",
    val filters: SportFilterState = SportFilterState(),
    val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
    val people: UiState<List<CommunityProfileDto>> = UiState.Loading,
    val news: UiState<List<ArticleDto>> = UiState.Loading,
    val sports: List<SportDto> = emptyList(), // для фильтра
    val hasMoreTournaments: Boolean = true,
    val hasMorePeople: Boolean = true,
    val hasMoreNews: Boolean = true
)
enum class SportSubTab { TOURNAMENTS, PEOPLE, NEWS }
data class SportFilterState(val sportId: String? = null, val status: String? = null, val city: String? = null, val ageCategory: String? = null, val role: String? = null, val category: String? = null)
```
- Загрузка: все три списка с **limit 50**. Пагинация через offset.
- Поиск: фильтрация клиент-сайд по name/title (icontains).
- Фильтры: клиент-сайд фильтрация по загруженным данным.
- Методы: `search(query)`, `setTab(tab)`, `applyFilters(filters)`, `resetFilters()`, `loadMore()`.
- Repository: `ViewerRepository()` — методы `getPublicTournaments()`, `getAthletes()`, `getTrainers()`, `getReferees()`, `getPublishedArticles()`, `getSports()`.

### НЕ трогай
- MainScreen.kt, NavGraph.kt, BottomNavItems.kt (Чат 1)
- Файлы в home/, detail/ (Чат 2)
- Файлы в mytournaments/, profile/ (Чат 4)

---

## ЧАТ 4: Табы "Мои турниры" + "Профиль"

**Зона ответственности** — ТОЛЬКО НОВЫЕ файлы:

### Твои файлы (все создать с нуля)

```
app/src/main/java/com/ileader/app/ui/screens/mytournaments/MyTournamentsTab.kt
app/src/main/java/com/ileader/app/ui/screens/mytournaments/MyTournamentsScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/MyTournamentsViewModel.kt

app/src/main/java/com/ileader/app/ui/screens/profile/ProfileTab.kt
app/src/main/java/com/ileader/app/ui/screens/profile/ProfileScreen.kt
app/src/main/java/com/ileader/app/ui/screens/profile/EditProfileScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/ProfileViewModel.kt
```

### Задание — Мои турниры

**MyTournamentsTab.kt** — обёртка с sub-навигацией:
```kotlin
@Composable
fun MyTournamentsTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<MyTournamentsNavState>(MyTournamentsNavState.List) }
    when (val state = navState) {
        MyTournamentsNavState.List -> MyTournamentsScreen(user, onTournamentClick = {...}, ...)
        is MyTournamentsNavState.TournamentDetail -> TournamentDetailScreen(...)  // из com.ileader.app.ui.screens.detail
        is MyTournamentsNavState.QrScanner -> QrScannerScreen(...)  // из common/
    }
}
```

**MyTournamentsScreen.kt** — контент зависит от роли:
- `when (user.role)` → показываем основную ролевую секцию
- **ДОПОЛНИТЕЛЬНО** (не вместо!) проверяем `HelperRepository.getMyAssignments(userId)` — если есть назначения, показываем секцию "Помощник" ПОД основным контентом

Контент по ролям:
| Роль | Что показываем |
|------|---------------|
| `USER` | `ViewerRepository.getMySpectatorRegistrations(userId)` → список турниров как зритель |
| `ATHLETE` | `AthleteRepository.getMyTournaments(userId)` → зарегистрированные турниры. Ниже — `AthleteRepository.getGoals(userId)` → секция "Мои цели" |
| `TRAINER` | `TrainerRepository.getMyTeams(userId)` → турниры команд |
| `ORGANIZER` | `OrganizerRepository.getMyTournaments(userId)` → мои турниры. Нет кнопки создания! Карточка → TournamentDetail (с возможностью редактирования базовых полей: название, описание, даты, maxParticipants). Кнопка "QR Check-in" на карточке турнира |
| `REFEREE` | `RefereeRepository.getAssignedTournaments(userId)` → назначенные турниры |
| `MEDIA` | `MediaRepository.getMediaInvites(userId)` → аккредитации |
| `SPONSOR`, `ADMIN`, `CONTENT_MANAGER` | То же что USER |

**Секция хелпера** (дополнительная, показывается если есть назначения):
- SectionHeader("Помощник")
- Список турниров из `HelperRepository.getMyAssignments(userId)`
- На каждой карточке кнопки: "QR Check-in" и "Ручной Check-in"
- Используй `QrScannerScreen` и `ManualCheckInScreen` из `ui/screens/common/`

**MyTournamentsViewModel.kt**:
- Принимает `userId` и `role`
- Загружает данные из соответствующего Repository по роли
- Параллельно загружает helper-назначения для ВСЕХ ролей
- State: `data class MyTournamentsState(tournaments: UiState<List<Any>>, goals: UiState<List<AthleteGoal>>? = null, helperAssignments: UiState<List<TournamentHelperDto>> = UiState.Loading)`

### Задание — Профиль

**ProfileTab.kt** — обёртка с sub-навигацией:
```kotlin
@Composable
fun ProfileTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<ProfileNavState>(ProfileNavState.Main) }
    when (val state = navState) {
        ProfileNavState.Main -> ProfileScreen(user, onSignOut, onEditProfile = { navState = ProfileNavState.Edit }, onTickets = { navState = ProfileNavState.Tickets }, onNotifications = { navState = ProfileNavState.Notifications })
        ProfileNavState.Edit -> EditProfileScreen(user, onBack = { navState = ProfileNavState.Main })
        ProfileNavState.Tickets -> MyTicketsScreen(user)  // из common/
        ProfileNavState.Notifications -> NotificationsScreen(user)  // из common/
    }
}
```

**ProfileScreen.kt**:
1. **Header card** (DarkCard с gradient или accent-оттенком):
   - Аватар (AsyncImage или InitialsAvatar) — 80dp
   - Имя (24sp bold)
   - `RoleBadge(user.role)` + город
2. **Stats row** (Row из MiniStat):
   - athlete: "Турниры" / "Победы" / "Рейтинг"
   - trainer: "Команда" / "Турниры"
   - referee: "Матчи" / "Нарушения"
   - organizer: "Турниры" / "Участники"
   - user: "Посещения"
3. **Quick Actions** (Column из DarkCard-строк с иконкой + текст + chevron):
   - "Редактировать профиль" (Icons.Default.Edit) → onEditProfile
   - "Мои билеты" (Icons.Default.ConfirmationNumber) → onTickets
   - "Уведомления" (Icons.Default.Notifications) → onNotifications
   - "Выбор вида спорта" (Icons.Default.SportsScore) → открыть BottomSheet с выбором спорта (аналог OnboardingSportScreen но как Sheet)
   - ThemeSwitcherCard() — существующий composable
   - "Выйти" (Icons.Default.ExitToApp, красный текст) → onSignOut

**EditProfileScreen.kt**:
- BackHeader("Редактирование профиля")
- Аватар с кнопкой "Изменить" — через AvatarViewModel (import из существующего ui/viewmodels/)
- Поля (DarkFormField + ILeaderInputField): имя, nickname, телефон, город, страна, bio (многострочное)
- Загрузка данных: `ViewerRepository.getProfile(userId)`
- Сохранение: `ViewerRepository.updateProfile(userId, ProfileUpdateDto(...))`
- Кнопка "Сохранить" (DarkButton) — disabled если нет изменений
- Обработка: loading → success (snackbar "Профиль обновлён") / error

**ProfileViewModel.kt**:
- Загружает профиль + ролевые статы
- Для athlete: `ViewerRepository.getUserSportStats(userId)` → wins, tournaments, rating
- Для organizer: данные из профиля (countable через tournaments list)
- State: `data class ProfileState(profile: UiState<ProfileDto>, stats: List<UserSportStatsDto>, userSports: List<UserSportDto>)`

### НЕ трогай
- MainScreen.kt, NavGraph.kt, BottomNavItems.kt (Чат 1)
- Файлы в home/, detail/ (Чат 2)
- Файлы в sport/ (Чат 3)

---

## После завершения всех 4 чатов

1. В MainScreen.kt — удалить placeholder'ы (HomeTab, SportTab и т.д.) которые создал Чат 1, заменить импортами из реальных файлов:
```kotlin
import com.ileader.app.ui.screens.home.HomeTab
import com.ileader.app.ui.screens.sport.SportTab
import com.ileader.app.ui.screens.mytournaments.MyTournamentsTab
import com.ileader.app.ui.screens.profile.ProfileTab
```

2. Запустить билд: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

3. Исправить ошибки компиляции (если есть несовпадения сигнатур между чатами)

4. Фаза 7 (очистка) — удалить старые экраны поэтапно (см. план)
