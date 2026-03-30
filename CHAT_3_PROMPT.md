Мы перестраиваем приложение iLeader Android на 4 таба. Полный план лежит в файле:
C:\Users\zhami\.claude\plans\immutable-greeting-pinwheel.md — прочитай его.

Также прочитай CLAUDE.md в корне проекта — там полный контекст по проекту, БД, конвенциям.

Ключевые правила:
- Русские строки в UI
- Используй DarkTheme.X для цветов (из DarkThemeComponents.kt)
- Используй существующие компоненты: DarkCard, SectionHeader, DarkSearchField, DarkSegmentedControl, EmptyState, LoadingScreen, ErrorScreen, FadeIn, MiniStat, RoleBadge, StatusBadge, DarkFormField, ILeaderInputField, FormDropdown, DarkFilterChip и т.д.
- ViewModel создаёт Repository напрямую (без Hilt): private val repo = SomeRepository()
- UiState<T> — sealed class: Loading, Success(data), Error(message)
- Запросы к Supabase через существующие Repository классы
- Palette aliases в экранах: private val Bg: Color @Composable get() = DarkTheme.Bg

---

# Твоя задача: Таб "Спорт" (Поиск и Фильтры)

Ты создаёшь ТОЛЬКО НОВЫЕ файлы. Не модифицируй существующие файлы.

## Твои файлы (все создать с нуля)

```
app/src/main/java/com/ileader/app/ui/screens/sport/SportTab.kt
app/src/main/java/com/ileader/app/ui/screens/sport/SportScreen.kt
app/src/main/java/com/ileader/app/ui/screens/sport/FilterPopupScreen.kt
app/src/main/java/com/ileader/app/ui/viewmodels/SportViewModel.kt
```

## Что делать

### SportTab.kt

Обёртка с внутренней навигацией. Sealed class `SportNavState` с вариантами: Search, TournamentDetail(id), ArticleDetail(id), PublicProfile(id).

Сигнатура: `@Composable fun SportTab(user: User)` — именно такая, другой чат импортирует её в MainScreen.

Для Detail-экранов импортируй из `com.ileader.app.ui.screens.detail.*` — их создаёт другой чат параллельно. Используй эти сигнатуры:
- `TournamentDetailScreen(tournamentId: String, user: User, onBack: () -> Unit)`
- `ArticleDetailScreen(articleId: String, onBack: () -> Unit)`
- `PublicProfileScreen(userId: String, onBack: () -> Unit)`

### SportScreen.kt

Сигнатура: `@Composable fun SportScreen(user: User, onTournamentClick: (String) -> Unit, onArticleClick: (String) -> Unit, onProfileClick: (String) -> Unit)`

Layout:
- Верх: Row с `DarkSearchField` (Modifier.weight(1f), onChange → `viewModel.search(query)`) + IconButton с иконкой фильтра (Icons.Default.Tune)
- Под ним: `DarkSegmentedControl` с 3 вкладками: "Турниры", "Люди", "Новости". onChange → `viewModel.setTab(tab)`
- Контент: `LazyColumn` с карточками по активной вкладке:
  - **Турниры**: DarkCard с — название, спорт (StatusBadge), дата, город, кол-во участников, статус. Tap → onTournamentClick
  - **Люди**: DarkCard с — аватар (48dp), имя, спорт, рейтинг, город. Tap → onProfileClick
  - **Новости**: DarkCard с — cover image (AsyncImage, 80x80dp), title, excerpt (maxLines=2), категория, дата. Tap → onArticleClick
- Внизу списка (last item): если `viewModel.state.hasMore*` → TextButton "Загрузить ещё" → `viewModel.loadMore()`
- Пустые результаты: `EmptyState` с соответствующим текстом

Клик на иконку фильтра → `showFilterPopup = true`. FilterPopupScreen показывается как `AnimatedVisibility` overlay или `Dialog(usePlatformDefaultWidth = false)`.

### FilterPopupScreen.kt

Сигнатура: `@Composable fun FilterPopupScreen(activeTab: SportSubTab, sports: List<SportDto>, filters: SportFilterState, onApply: (SportFilterState) -> Unit, onReset: () -> Unit, onDismiss: () -> Unit)`

Full-screen overlay (Dialog с fillMaxSize или Box с fillMaxSize + background):
- BackHeader("Фильтры", onBack = onDismiss)
- Контент зависит от activeTab:

**Турниры**:
- Спорт: FormDropdown из списка sports
- Статус: Row из DarkFilterChip — "Регистрация", "Идёт", "Завершён" (values: registration_open, in_progress, completed)
- Город: ILeaderInputField текстовое поле
- Возрастная категория: FormDropdown — "Дети (6-12)", "Юноши (12-17)", "Взрослые (18+)" (values: children, youth, adult)

**Люди**:
- Спорт: FormDropdown
- Город: ILeaderInputField
- Роль: Row из DarkFilterChip — "Спортсмены", "Тренеры", "Судьи" (values: athlete, trainer, referee)

**Новости**:
- Спорт: FormDropdown
- Категория: Row/FlowRow из DarkFilterChip — "Новости", "Отчёт", "Интервью", "Аналитика", "Обзор" (values: news, report, interview, analytics, review)

Внизу: Row из двух кнопок — "Сбросить" (outlined) и "Применить" (filled, accent)

### SportViewModel.kt

```kotlin
class SportViewModel : ViewModel() {
    private val repo = ViewerRepository()

    data class SportState(
        val activeTab: SportSubTab = SportSubTab.TOURNAMENTS,
        val searchQuery: String = "",
        val filters: SportFilterState = SportFilterState(),
        val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
        val people: UiState<List<CommunityProfileDto>> = UiState.Loading,
        val news: UiState<List<ArticleDto>> = UiState.Loading,
        val sports: List<SportDto> = emptyList(),
        val hasMoreTournaments: Boolean = true,
        val hasMorePeople: Boolean = true,
        val hasMoreNews: Boolean = true
    )

    enum class SportSubTab { TOURNAMENTS, PEOPLE, NEWS }

    data class SportFilterState(
        val sportId: String? = null,
        val status: String? = null,
        val city: String? = null,
        val ageCategory: String? = null,
        val role: String? = null,
        val category: String? = null
    )
}
```

Логика:
- init: загружает все три списка с limit 50 + загружает список спортов для фильтра
- Хранит полные загруженные списки (allTournaments, allPeople, allNews) и отфильтрованные (в UiState)
- `search(query)`: фильтрует клиент-сайд по name/title (contains, ignoreCase)
- `setTab(tab)`: переключает activeTab
- `applyFilters(filters)`: сохраняет фильтры, пересчитывает отфильтрованные списки
- `resetFilters()`: сбрасывает filters к SportFilterState(), пересчитывает
- `loadMore()`: увеличивает offset, загружает следующие 50, добавляет к существующим. Если вернулось меньше 50 → hasMore = false

Repository методы: `repo.getPublicTournaments()`, `repo.getAthletes()`, `repo.getTrainers()`, `repo.getReferees()`, `repo.getPublishedArticles()`, `repo.getSports()`

Для людей: загружай все три типа (athletes, trainers, referees) и объединяй в один список. Фильтр по role разделяет обратно.

## НЕ ТРОГАЙ

- MainScreen.kt, NavGraph.kt, BottomNavItems.kt — зона Чата 1
- Файлы в ui/screens/home/, ui/screens/detail/ — зона Чата 2
- Файлы в ui/screens/mytournaments/, ui/screens/profile/ — зона Чата 4
- Существующие файлы — не модифицируй

Приступай к реализации.
