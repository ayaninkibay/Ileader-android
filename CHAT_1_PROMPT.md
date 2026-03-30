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

# Твоя задача: Фундамент — Навигация + Онбординг

Ты единственный кто МОДИФИЦИРУЕТ существующие файлы. Другие 3 чата работают параллельно и создают только новые файлы в своих директориях. Не трогай их зоны.

## Файлы для создания

- `app/src/main/java/com/ileader/app/data/preferences/SportPreference.kt`
- `app/src/main/java/com/ileader/app/ui/screens/onboarding/OnboardingSportScreen.kt`
- `app/src/main/java/com/ileader/app/ui/viewmodels/OnboardingViewModel.kt`

## Файлы для модификации

- `app/src/main/java/com/ileader/app/data/preferences/ThemePreference.kt`
- `app/src/main/java/com/ileader/app/ui/navigation/BottomNavItems.kt`
- `app/src/main/java/com/ileader/app/ui/screens/main/MainScreen.kt`
- `app/src/main/java/com/ileader/app/ui/navigation/NavGraph.kt`

## Что делать

1. **ThemePreference.kt**: Замени `private val Context.dataStore` на `val Context.ileaderDataStore` (public). Замени все обращения `context.dataStore` на `context.ileaderDataStore`. Это нужно чтобы SportPreference использовал тот же DataStore.

2. **SportPreference.kt**: DataStore хранит `selected_sport_ids` (List<String> сериализованный в JSON строку) и `selected_sport_names`. Использует `context.ileaderDataStore` из ThemePreference. Методы: `setSports(ids: List<String>, names: List<String>)`, `clearSports()`. Flow: `selectedSportIds: Flow<List<String>>`, `selectedSportNames: Flow<List<String>>`.

3. **OnboardingViewModel.kt**: Загружает спорты через `ViewerRepository().getSports()`. State: `UiState<List<SportDto>>` для списка спортов, `selectedSportIds: Set<String>` для выбранных (1-3 штуки), `isSaving: Boolean`. Метод `saveSports(userId: String, context: Context)` — сохраняет в `user_sports` таблицу через Supabase + SportPreference.

4. **OnboardingSportScreen.kt**: Полноэкранный экран выбора спорта. Заголовок "Выберите вид спорта" + подзаголовок "Выберите 1-3 вида спорта". Сетка 2x4 карточек спорта (используй sportIcon/sportEmoji из DarkThemeComponents + name). Выбранные подсвечиваются accent цветом. Кнопка "Продолжить" (disabled если ничего не выбрано). После сохранения вызывает `onComplete()`.

5. **BottomNavItems.kt**: Убрать ВСЮ функцию `getBottomNavItems(role, ...)` со всеми параметрами. Заменить на простую:
```kotlin
fun getBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem("home", "Главная", Icons.Default.Home),
    BottomNavItem("sport", "Спорт", Icons.Default.Search),
    BottomNavItem("my_tournaments", "Мои турниры", Icons.Default.EmojiEvents),
    BottomNavItem("profile", "Профиль", Icons.Default.Person)
)
```

6. **MainScreen.kt**: Убрать ВСЕ импорты ролевых экранов (athlete, trainer, organizer, referee, media, viewer, helper). Убрать `RoleScreenRouter` целиком. Убрать `HelperViewModel`, `TicketsViewModel` и всю связанную логику. `bottomNavItems` теперь просто `getBottomNavItems()` без параметров. Новый TabRouter вместо RoleScreenRouter:
```kotlin
when (selectedRoute) {
    "home" -> HomeTab(user = user)
    "sport" -> SportTab(user = user)
    "my_tournaments" -> MyTournamentsTab(user = user, onSignOut = onSignOut)
    "profile" -> ProfileTab(user = user, onSignOut = onSignOut)
}
```
Другие чаты создадут реальные HomeTab, SportTab, MyTournamentsTab, ProfileTab. Пока добавь временные placeholder'ы в конце файла:
```kotlin
// Временные placeholder'ы — будут заменены реальными экранами из других чатов
@Composable fun HomeTab(user: User) { PlaceholderScreen("Главная", user, {}) }
@Composable fun SportTab(user: User) { PlaceholderScreen("Спорт", user, {}) }
@Composable fun MyTournamentsTab(user: User, onSignOut: () -> Unit) { PlaceholderScreen("Мои турниры", user, onSignOut) }
@Composable fun ProfileTab(user: User, onSignOut: () -> Unit) { PlaceholderScreen("Профиль", user, onSignOut) }
```

7. **NavGraph.kt**: Добавить `data object Onboarding : Screen("onboarding")` в sealed class. В `LaunchedEffect(authState.isAuthenticated)` — после auth проверять `authState.currentUser?.sportIds.isNullOrEmpty()`. Если пустые → navigate to Onboarding, иначе → Main. Добавить `composable(Screen.Onboarding.route)` с OnboardingSportScreen, где onComplete навигирует к Main.

## НЕ ТРОГАЙ

- Файлы auth экранов (Login, Register, Welcome, ForgotPassword)
- DarkThemeComponents.kt
- Любые файлы в ui/screens/athlete/, trainer/, organizer/, referee/, media/, viewer/, helper/
- Любые файлы в ui/screens/home/, ui/screens/sport/, ui/screens/mytournaments/, ui/screens/profile/, ui/screens/detail/ — это зоны других чатов
- Repository и DTO файлы

Приступай к реализации.
