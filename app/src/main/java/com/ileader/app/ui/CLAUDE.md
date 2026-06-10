# ui/ — UI-слой

Контекст для Claude-чатов, работающих с UI (тема, навигация, провайдеры, ViewModels).
Корневой контекст: `/CLAUDE.md`. Сабконтекст экранов: `ui/screens/CLAUDE.md`.

## Структура

```
ui/
├── components/      ~50 переиспользуемых composable (DarkThemeComponents.kt, AnimatedBackground, ErrorScreen, ...)
├── navigation/      NavGraph.kt (корневой граф: Welcome → Login/Register/Forgot → Onboarding/Main), BottomNavItems.kt (по ролям)
├── providers/       UserProvider + LocalCurrentUser (Composition-local с текущим пользователем)
├── screens/         71 экран, фичевая таксономия (см. отдельный CLAUDE.md)
├── theme/           Color.kt (light+dark палитры), Theme.kt (ILeaderTheme, AppColorScheme), Type.kt, Shadows.kt
└── viewmodels/      24 VM. Плоская папка, не сгруппирована по фиче
```

## Тема

`ILeaderTheme(themeMode = ThemeMode.LIGHT/DARK/SYSTEM)` оборачивает всё. Внутри:
- `MaterialTheme` (Material 3 colorScheme)
- `LocalAppColors` (CompositionLocal с `AppColorScheme` — семантические цвета: bg, cardBg, border, textPrimary, ...)
- `DarkTheme` object (в `components/DarkThemeComponents.kt`) — backward-compatible proxy: `DarkTheme.Bg`, `DarkTheme.Accent`, ... читают из `LocalAppColors.current`. **68+ экранов используют `DarkTheme.X`** — менять не надо.

Цвета синхронизированы с веб-сайтом:
- light: bg `#f5f5f7`, cards `white`, border `#e5e5e5`
- dark: bg `#0a0a0a`, cards `#18181b`, border `#27272a`
- accent: `#E53535` (красный, primary)

**Тени** — отключены (плоский дизайн): `cardShadow()` / `floatingShadow()` — no-op. `DarkCard` рисует только border, без тени.

## Текущий пользователь — UserSession + LocalCurrentUser

Единый поток данных о пользователе:

```
client.auth (Supabase)
       ↓
AuthViewModel.signIn / signUp / init (session restore)
       ↓
UserSession.setUser(user)        ← singleton в data/session/
       ↓
UserProvider { content() }       ← обёртка в MainActivity
       ↓
LocalCurrentUser.current         ← @Composable доступ из любого места
```

**Правила**:
- В Composable → `val user = LocalCurrentUser.current` (не передавай user через параметры если можно избежать)
- В ViewModel/Repository → `UserSession.userId` или `UserSession.currentUser.value`
- НЕ читай `client.auth.currentUserOrNull()` нигде кроме AuthViewModel
- НЕ создавай свой StateFlow с пользователем в каждом VM — подпишись на `UserSession.currentUser`

Существующие экраны принимают `user: User` как параметр (legacy паттерн). Новые экраны — читай из `LocalCurrentUser` напрямую.

## Навигация

`NavGraph.kt` — корневой граф (Welcome / Login / Register / ForgotPassword / Onboarding / Main).
`MainScreen.kt` — внутренний "сайт" с floating bottom bar. Табы определяются ролью через `BottomNavItems.kt`:
- viewer (`USER`) — Home, Sport, Profile
- athlete — Home, Sport, MyTournaments, Profile
- trainer — Home, Sport, MyTournaments, Profile
- organizer — Home, Sport, MyTournaments, Profile
- referee — Home, Sport, MyTournaments, Profile
- sponsor — Home, Sport, Sponsor, Profile
- media — Home, Sport, Media, Profile
- admin — те же что organizer + админ-разделы внутри Profile

## ViewModels

24 VM в плоской `ui/viewmodels/`. Принцип:
- `class FooViewModel : ViewModel()`
- `private val _state = MutableStateFlow(...)` / `val state: StateFlow<...>`
- Бизнес-логика в `viewModelScope.launch { ... }`
- Создание Repository прямо внутри: `private val repo = FooRepository()` — DI пока нет
- Можно переиспользовать UserSession.userId если userId нужен (вместо параметра в `load(userId)`)

**Когда менять signature на `LocalCurrentUser`/`UserSession`**:
- Новый VM/экран — сразу читай из UserSession
- Мигрируешь существующий → не каскадь по всем VM, только если уже редактируешь файл

## Compose-конвенции

- Все экраны используют `DarkTheme.X` (через palette aliases в начале файла: `private val Bg: Color @Composable get() = DarkTheme.Bg`)
- Не использовать `@Composable getter`'ы внутри Canvas/DrawScope — хоистить в `val`
- Карточки: `RoundedCornerShape(12-16.dp)`
- Анимации входа: FadeIn staggered
- Русские строки прямо в коде (нет strings.xml)
- Иконки: `Icons.AutoMirrored.Filled.X` вместо `Icons.Filled.X` для RTL-чувствительных (ArrowBack, TrendingUp, Article, Undo)

## Файлы которые трогать осторожно

- `MainActivity.kt` — точка входа, обёртки UserProvider + ILeaderTheme + Surface
- `navigation/NavGraph.kt` — корневой граф, LaunchedEffect на auth state для routing
- `theme/Theme.kt`, `theme/Color.kt` — затрагивают весь UI
- `components/DarkThemeComponents.kt` — 50+ composable, изменение ломает много экранов
