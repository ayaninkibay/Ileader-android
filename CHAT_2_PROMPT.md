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

---

# Твоя задача: Таб "Главная" + Детальные экраны

Ты создаёшь ТОЛЬКО НОВЫЕ файлы. Не модифицируй существующие файлы — другой чат этим занимается.

## Твои файлы (все создать с нуля)

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

## Что делать

### HomeTab.kt

Обёртка с внутренней навигацией. Sealed class `HomeNavState` с вариантами: Home, ArticleDetail(id), TournamentDetail(id), PublicProfile(id). State-based роутинг через `var navState by remember { mutableStateOf(...) }`. Каждый Detail-экран получает `onBack = { navState = HomeNavState.Home }`.

Сигнатура: `@Composable fun HomeTab(user: User)` — именно такая, другой чат импортирует её в MainScreen.

### HomeScreen.kt

Три секции в `LazyColumn` (не Column + LazyRows — а LazyColumn с item{} для каждой секции и LazyRow внутри):

1. **Новости iLeader**: `SectionHeader("Новости iLeader", "Все")` + горизонтальный LazyRow карточек. Источник: `viewModel.state.news`. Карточка: AsyncImage (cover, 200x130dp, RoundedCornerShape(12.dp)), title (maxLines=2), excerpt (maxLines=1, muted), дата. Tap → `onArticleClick(article.id)`.

2. **Ближайшие турниры**: `SectionHeader("Ближайшие турниры")` + LazyRow. Источник: `viewModel.state.tournaments`. Показываем только турниры с status = registration_open или in_progress, сортировка по startDate ASC. Фильтруем клиент-сайд по sportIds пользователя (из SportPreference). Карточка: название, спорт-бадж, дата, город, StatusBadge(status). Tap → `onTournamentClick(tournament.id)`.

3. **Люди**: `SectionHeader("Люди")` + LazyRow. Источник: `viewModel.state.people`. Карточка: аватар 56dp (AsyncImage или инициалы на accent фоне), имя, основной спорт, рейтинг. Tap → `onProfileClick(profile.id)`.

Для чтения sportIds из SportPreference:
```kotlin
val context = LocalContext.current
val sportPref = remember { SportPreference(context) }
val sportIds by sportPref.selectedSportIds.collectAsState(initial = emptyList())
```
SportPreference создаётся другим чатом, но файл будет в `com.ileader.app.data.preferences.SportPreference`.

### HomeViewModel.kt

```kotlin
class HomeViewModel : ViewModel() {
    private val repo = ViewerRepository()
    // State с тремя UiState для независимой загрузки
    data class HomeState(
        val news: UiState<List<ArticleDto>> = UiState.Loading,
        val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
        val people: UiState<List<CommunityProfileDto>> = UiState.Loading
    )
}
```
Метод `load(sportIds: List<String>)` — загружает все три секции параллельно через `coroutineScope { async {} }`. Новости: `repo.getRecentArticles(5)`. Турниры: `repo.getUpcomingTournaments(10)` → фильтр по sportIds клиент-сайд. Люди: `repo.getAthletes()` take 10.

### TournamentDetailScreen.kt

Прочитай существующий `ui/screens/viewer/ViewerTournamentDetailScreen.kt` для вдохновления — там уже есть хорошая верстка. Но пиши НОВЫЙ файл в `detail/`.

Сигнатура: `@Composable fun TournamentDetailScreen(tournamentId: String, user: User, onBack: () -> Unit)`

Содержимое:
- BackHeader с названием турнира
- Инфо-карточка: спорт, даты, локация, организатор, описание, maxParticipants
- Секция участников (LazyColumn или Column)
- Bracket (если формат elimination) — используй компоненты из `ui/components/bracket/` (BracketView и т.д.)
- Результаты (если status = completed) — таблица позиций

**Action-кнопки внизу** зависят от роли И статуса турнира:
- athlete + registration_open + не зарегистрирован → "Зарегистрироваться" (accent кнопка)
- athlete + registration_open + зарегистрирован → "Отменить регистрацию" (outlined, красный)
- athlete + другой статус → нет кнопки
- user + registration_open → "Зарегистрироваться как зритель"
- user + уже зрегистрирован → нет кнопки
- organizer (если tournament.organizerId == user.id) → "Редактировать"
- referee (если назначен) + in_progress → "Судейство"
- Все остальные случаи → нет кнопки

### TournamentDetailViewModel.kt

Загружает через ViewerRepository: `getTournamentDetail(id)`, `getTournamentParticipants(id)`, `getTournamentBracket(id)`, `getTournamentResults(id)`, `getTournamentGroups(id)`. Проверяет регистрацию: `AthleteRepository().getMyParticipation(tournamentId, userId)`. Для зрителей: `ViewerRepository().getMySpectatorRegistration(tournamentId, userId)`.

Методы: `registerAsParticipant(tournamentId, userId)` через AthleteRepository, `registerAsSpectator(tournamentId, userId)` через ViewerRepository, `unregister(tournamentId, userId)` через AthleteRepository.

### ArticleDetailScreen.kt

Сигнатура: `@Composable fun ArticleDetailScreen(articleId: String, onBack: () -> Unit)`

- BackHeader("Статья")
- Cover image (AsyncImage, fillMaxWidth, height 220dp, RoundedCornerShape bottom)
- Заголовок (22sp bold)
- Автор + дата (Row: имя автора, "•", дата публикации, muted text)
- Спорт-бадж (если есть sportId)
- Контент (Text, полный текст статьи)

### ArticleDetailViewModel.kt

`ViewerRepository().getArticleDetail(articleId)`. State: `UiState<ArticleDto>`. Загрузка в init по articleId.

### PublicProfileScreen.kt

Сигнатура: `@Composable fun PublicProfileScreen(userId: String, onBack: () -> Unit)`

- BackHeader("Профиль")
- Аватар (80dp) + имя + RoleBadge + город
- Спортивная статистика: Row из MiniStat (турниры, победы, рейтинг)
- Секция результатов: LazyColumn карточек TournamentResult
- Команда (если есть): DarkCard с названием команды и ролью

### PublicProfileViewModel.kt

Загружает: `ViewerRepository().getPublicProfile(userId)`, `getUserSports(userId)`, `getUserSportStats(userId)`, `getAthleteResults(userId)`, `getAthleteMembership(userId)`.

## НЕ ТРОГАЙ

- MainScreen.kt, NavGraph.kt, BottomNavItems.kt — это зона Чата 1
- Файлы в ui/screens/sport/ — это зона Чата 3
- Файлы в ui/screens/mytournaments/, ui/screens/profile/ — это зона Чата 4
- Существующие файлы в viewer/, athlete/ и т.д. — не модифицируй, только читай для справки

Приступай к реализации.
