# iLeader Android - Project Context for Claude Chats

> Этот файл содержит полный контекст проекта для всех Claude-чатов, работающих над приложением.
> Обновлено: 2026-03-16

---

## 1. Обзор проекта

**iLeader** — спортивная платформа для управления турнирами, рейтингами и командами.

- **Веб-сайт** (источник истины): `C:\Users\zhami\Documents\ileader` — Next.js 16 + React 19 + TypeScript + Supabase
- **Android-приложение** (этот проект): `C:\Users\zhami\Documents\ileader-android` — Kotlin + Jetpack Compose + Material 3 + Supabase
- **Общая БД**: Supabase PostgreSQL (`clkbmjsmfzjuqdwnoejv.supabase.co`)
- **Домен сайта**: `ileader.kz`

**Принцип**: Приложение — мобильный клиент с **только необходимым** функционалом. Не копия сайта. Без лишнего контента, без сложных CRUD-операций (создание турниров, статей — остаётся на сайте).

---

## 2. Роли пользователей (9 ролей)

| Роль | Описание | Верификация |
|------|----------|-------------|
| `user` (Зритель) | Просмотр турниров, новостей, рейтингов | Не нужна |
| `athlete` (Спортсмен) | Участие в турнирах, отслеживание результатов, цели | Не нужна |
| `trainer` (Тренер) | Управление командой и спортсменами | Не нужна |
| `organizer` (Организатор) | Управление турнирами, локациями | Нужна (admin) |
| `referee` (Судья) | Судейство турниров, ввод результатов | Нужна (admin) |
| `sponsor` (Спонсор) | Спонсирование команд/турниров | Нужна (admin) |
| `media` (Медиа) | Написание статей, аккредитации | Нужна (admin) |
| `content_manager` | Управление контентом (курсы, спорты) | Нужна (admin) |
| `admin` | Полный доступ к платформе | — |

---

## 3. Стек приложения

```
Kotlin 2.0.21 + Jetpack Compose + Material 3
├── Supabase 3.0.2 (PostGREST, Auth, Storage)
├── Ktor 3.0.1 (HTTP client)
├── Kotlinx Serialization 1.7.3
├── Jetpack Navigation Compose 2.8.4
├── Coil 2.7.0 (загрузка изображений)
├── DataStore 1.1.1 (настройки)
├── Accompanist 0.34.0
├── minSdk=26, targetSdk=36, Java 11
```

**Архитектура**: MVVM (ViewModel → Repository → SupabaseModule)

---

## 4. Структура проекта

```
app/src/main/java/com/ileader/app/
├── MainActivity.kt                    # Точка входа, тема, навигация
├── data/
│   ├── bracket/                       # BracketGenerator.kt, BracketUtils.kt
│   ├── mock/                          # 8 файлов с моковыми данными (УБРАТЬ)
│   ├── models/                        # Domain-модели (User, Tournament, Team, etc.)
│   ├── preferences/                   # ThemePreference.kt (DataStore)
│   ├── remote/
│   │   ├── SupabaseModule.kt          # Singleton клиент Supabase
│   │   ├── UiState.kt                 # Sealed class: Loading/Success/Error
│   │   └── dto/                       # 20+ DTO-файлов для сериализации
│   └── repository/                    # 9 репозиториев по ролям
├── ui/
│   ├── components/                    # DarkThemeComponents.kt (50+ composable)
│   ├── navigation/                    # NavGraph.kt, BottomNavItems.kt
│   ├── screens/                       # 85 экранов по ролям + auth + common
│   ├── theme/                         # Color.kt, Theme.kt, Type.kt, Shadows.kt
│   └── viewmodels/                    # 58 ViewModel'ов
```

---

## 5. Тема и UI-система

### Цвета
- **Light**: bg=`#f5f5f7`, cards=`white`, border=`#e5e5e5`
- **Dark**: bg=`#0a0a0a`, cards=`#18181b`, border=`#27272a`
- **Primary accent**: `#E53535` (красный)

### Архитектура темы
- `AppColorScheme` — data class с семантическими цветами
- `LocalAppColors` — CompositionLocal (light/dark палитры)
- `DarkTheme` object в `DarkThemeComponents.kt` — backward-compatible proxy, читает из `LocalAppColors.current`
- **68+ экранов** используют `DarkTheme.X` — менять не нужно
- `ThemePreference` (DataStore) → LIGHT/DARK/SYSTEM

### Тени (Shadows.kt)
- `coloredShadow()` — кастомный модификатор (iOS-стиль)
- `cardShadow(isDark)` — для карточек
- `floatingShadow(isDark)` — для плавающих элементов
- Bottom bar: floating, rounded (20dp), inset 12dp

### Важные правила
- `DarkTheme.X` — `@Composable` getters, нельзя использовать внутри Canvas/DrawScope — надо хоистить в `val`
- Palette aliases в экранах: `private val Bg: Color @Composable get() = DarkTheme.Bg`
- Русские UI-строки везде
- `RoundedCornerShape(12-16.dp)` для карточек
- FadeIn staggered анимации на вход экранов

---

## 6. База данных Supabase — Полная схема

### Пользователи и роли
- **profiles** — id, name, email, role, avatarUrl, nickname, kzNumber, phone, birthDate, city, country, bio, athleteSubtype, ageCategory, verification, status, roleData (JSONB), settings, profileVisibility, createdAt, updatedAt
- **roles** — id, name, description
- **user_sports** — userId, sportId, rating, tournaments, wins, podiums, totalPoints, isPrimary

### Спорт
- **sports** — id, name, slug, athleteLabel, description, rules, iconUrl, isActive
- **sport_exercises** — id, sportId, name, slug, description, rules, difficulty, videoUrl, isActive, sortOrder (43 упражнения на 8 видов спорта)

### Команды
- **teams** — id, name, sportId, ownerId (trainer), description, logoUrl, city, isActive, foundedYear
- **team_members** — teamId, userId, roleInTeam (captain/member/reserve)
- **team_requests** — id, teamId, userId, status (pending/accepted/declined), message

### Турниры (основная сущность)
- **tournaments** — id, name, sportId, locationId, organizerId, status, startDate, endDate, registrationDeadline, maxParticipants, minParticipants, prize, description, imageUrl, format, matchFormat, seedingType, visibility, accessCode, groupCount, hasThirdPlaceMatch, hasCheckIn, checkInStartsBefore, ageCategory, skillLevel, genderCategory, region, discipline, exerciseId, entryFee, documents, requirements, categories, prizes, schedule, stageMatchFormats
- **tournament_participants** — tournamentId, athleteId, teamId, number, status, seed, checkInStatus, groupId
- **tournament_results** — id, tournamentId, athleteId, position, points, time, penalty, category, notes
- **tournament_matches** — id, tournamentId, matchNumber, title, status, startTime, endTime
- **tournament_referees** — tournamentId, refereeId, role
- **tournament_documents** — id, name, fileName, fileSize, fileType, path
- **tournament_sponsorships** — sponsorId, tournamentId, tier, amount
- **tournament_schedule** — id, tournamentId, time, title, description, sortOrder

### Турнирная сетка
- **bracket_matches** — id, tournamentId, round, matchNumber, bracketType (upper/lower/grand_final/third_place), participant1/2 Id/Name/Seed/Score, games[], winnerId, loserId, status, nextMatchId, loserNextMatchId, groupId, isBye
- **tournament_groups** — id, tournamentId, name, standings[]

### Локации
- **locations** — id, name, type, address, city, country, capacity, facilities[], imageUrls[], description, rating, phone, email, website, sportIds[], ownerId
- **location_reviews** — id, locationId, userId, overall, criteria (11 параметров), comment

### Лиги
- **leagues** — многоэтапные серии турниров
- **league_stages** — этапы лиги
- **league_standings** — рейтинг спортсменов в лиге

### Инвайты и заявки
- **tournament_invite_codes** — code, type (athlete/referee/sponsor), maxUses, usedCount, expiresAt
- **tournament_invites** — direction (outgoing/incoming), role (referee/sponsor/media), status
- **referee_requests** — приглашения судей от организаторов
- **referee_applications** — заявки судей на турниры
- **sport_requests** — заявки на новые виды спорта

### Цели и рейтинг
- **athlete_goals** — type (rating/tournament/points), title, status, progress, targets, deadlines
- **rating_history** — userId, sportId, rating, ratingChange, reason, tournamentId
- **achievements** — title, description, rarity (legendary/epic/rare/common)
- **licenses** — number, category, class, federation, status, dates
- **lap_times** — для картинга (timeSeconds, conditions, equipment)

### Контент и медиа
- **articles** — title, content, excerpt, coverImageUrl, sportId, tournamentId, status, category (12 типов), views, tags
- **media_accreditations** — mediaUserId, tournamentId, status
- **interviews** — mediaUserId, athleteId, title, status, scheduledDate

### Академия (курсы)
- **courses** — title, description, sportId, authorId, isFree, price, status
- **course_lessons** — title, textContent, videoUrl, audioUrl, durationMinutes, isFreePreview
- **user_courses** — userId, courseId, grantedBy

### Сообщения
- **conversations** — id
- **conversation_participants** — conversationId, userId, lastReadAt
- **messages** — conversationId, senderId, content

### Уведомления
- **notifications** — userId, type, title, message, data (JSONB), isRead

### Нарушения
- **violations** — tournamentId, participantId, refereeId, severity, category, description, matchNumber, time, penaltyApplied

### Семейные связи
- **family_links** — parent ↔ child для несовершеннолетних спортсменов

### Системное
- **platform_settings** — key, value (JSONB)

### Ключевые Enums в БД
`tournament_status`, `participant_status`, `check_in_status`, `match_status`, `bracket_type`, `tournament_format`, `match_format`, `seeding_type`, `user_role`, `user_status`, `verification_status`, `goal_type`, `goal_status`, `violation_severity`, `violation_category`, `sponsorship_tier`, `team_role`, `referee_tournament_role`, `content_status`, `news_category`, `license_status`, `accreditation_status`, `interview_status`, `media_type`, `location_type`, `invite_direction`, `invite_role`

---

## 7. Текущее состояние приложения — ПРОБЛЕМЫ

### Критические проблемы

1. **28 экранов на моковых данных** вместо реальной БД
   - Файлы: `data/mock/` (8 файлов)
   - Нужно: заменить все моки на реальные запросы к Supabase

2. **Нет DI (Dependency Injection)**
   - Каждый ViewModel создаёт свой экземпляр Repository: `private val repo = AthleteRepository()`
   - Нужно: синглтон-репозитории или Hilt DI

3. **0 тестов** — ни unit, ни UI, ни integration

4. **Нет обработки ошибок** — 1 вызов `Log.e` на весь проект (38K строк)

5. **Захардкоженные ключи Supabase** в `build.gradle.kts` как fallback значения

6. **Раздутая архитектура** — 85 экранов, 58 ViewModel'ов для "минимального" приложения

### Отсутствующий функционал (есть на сайте, нет в приложении)

| Функционал | Статус в приложении |
|-----------|-------------------|
| Лиги (leagues) | Полностью отсутствуют |
| Академия/Курсы | Полностью отсутствуют |
| Чат/Сообщения | Полностью отсутствуют |
| QR check-in | Отсутствует |
| Invite-коды на турниры | Отсутствует |
| Family links (несовершеннолетние) | Отсутствует |
| Верификация ролей | Отсутствует |
| Рейтинги/Rankings глобальные | Отсутствуют |
| Отзывы на локации | Отсутствуют |
| Нарушения (violations) | Отсутствуют |
| Push-уведомления (FCM) | Отсутствуют |
| Offline-режим / кэширование | Отсутствует |
| Deep linking | Отсутствует |

### Экраны на моках (нужно подключить к БД)

- Athlete: RacingLicense (нет таблицы licenses в DTO)
- Media: весь контент (articles таблица существует, но DTO/Repository не подключены)
- Admin: Settings (platform_settings не сохраняются)
- Viewer: большинство экранов (Home, News, Community) частично на моках
- Sponsor: MyTeam, Tournaments детали на моках

### DTO vs Веб-схема — расхождения

Приложение НЕ имеет DTO для:
- `leagues`, `league_stages`, `league_standings`
- `courses`, `course_lessons`, `user_courses`
- `conversations`, `messages`
- `achievements`
- `family_links`
- `location_reviews`
- `media_accreditations`, `interviews`
- `notifications` (есть модель, нет DTO для запросов)
- `violations` (есть DTO, но экран не использует реальные данные)
- `rating_history`

---

## 8. Что должно быть в приложении (Essential Features)

### MUST HAVE (ядро)

1. **Аутентификация** — вход/регистрация, выбор роли, восстановление пароля
2. **Дашборды по ролям** — ключевая статистика, быстрые действия
3. **Просмотр турниров** — каталог, фильтры, детали, регистрация
4. **Результаты турниров** — таблица результатов, позиции, очки
5. **Турнирная сетка** — визуализация brackets (single/double elim, round robin, groups)
6. **Профиль** — просмотр/редактирование, аватар
7. **Команды** — просмотр состава, вступление/выход
8. **Уведомления** — in-app уведомления
9. **Рейтинги** — глобальный рейтинг спортсменов по видам спорта

### SHOULD HAVE (важно, но второй приоритет)

10. **Цели спортсмена** — установка и отслеживание
11. **Push-уведомления** (FCM)
12. **Чат/Сообщения** — переписка между пользователями
13. **Судейство** — просмотр назначений, ввод результатов
14. **Offline-кэширование** основных данных

### NICE TO HAVE (на сайте есть, в приложении опционально)

15. **Создание турниров** (для организаторов) — сложная форма, лучше на сайте
16. **Написание статей** (для медиа) — лучше на сайте
17. **Академия/Курсы** — просмотр курсов (создание на сайте)
18. **Admin-панель** — управление пользователями/спортами (лучше на сайте)
19. **Лиги** — просмотр
20. **QR check-in** — для организаторов

---

## 9. Архитектурные решения для переконструкции

### Рекомендуемая новая архитектура

```
data/
├── local/          # Room DB для кэширования (NEW)
├── remote/
│   ├── dto/        # DTO классы (сериализация)
│   ├── api/        # Supabase API вызовы
│   └── SupabaseModule.kt
├── repository/     # Единые репозитории (singleton)
├── models/         # Domain модели
└── preferences/    # DataStore

domain/             # Use Cases (опционально, для сложной логики)

di/                 # Hilt модули (NEW)

ui/
├── theme/
├── components/     # Переиспользуемые composable
├── navigation/
├── screens/
│   ├── auth/
│   ├── athlete/
│   ├── trainer/
│   ├── organizer/
│   ├── referee/
│   ├── sponsor/
│   ├── media/
│   ├── admin/
│   ├── viewer/
│   └── common/
└── viewmodels/
```

### Ключевые изменения
1. **Добавить Hilt** для DI — Repository как `@Singleton`
2. **Убрать все моки** — только реальные данные из Supabase
3. **Сократить экраны** — убрать лишние, оставить essential
4. **Добавить Room** для offline-кэширования (опционально)
5. **Добавить FCM** для push-уведомлений
6. **Нормальная обработка ошибок** — логирование, retry, user-friendly сообщения

---

## 10. Конвенции кода

### Общие
- Язык UI: **русский**
- Все строки прямо в коде (нет strings.xml i18n)
- Kotlin style: camelCase для переменных, PascalCase для классов
- Composable: `@Composable fun ScreenName(navController, viewModel)`

### Compose
- Все экраны используют `DarkTheme.X` для цветов
- Palette aliases: `private val Bg: Color @Composable get() = DarkTheme.Bg`
- `DarkCard`, `DarkButton`, `DarkTextField` — кастомные компоненты из `DarkThemeComponents.kt`
- Тени через `Modifier.cardShadow(isDark)` и `Modifier.floatingShadow(isDark)`

### Supabase
- Все запросы через `SupabaseModule.client`
- DTO → Domain маппинг в `DtoMappers.kt` или в самих DTO (`.toDomain()`)
- Колонки указывать явно (`select("id, name, ...")`) — без `select("*")`
- JOINы через `Columns.raw()`

### Навигация
- `NavGraph.kt` — центральный граф
- `BottomNavItems.kt` — элементы нижней навигации (по ролям)
- Floating bottom bar: rounded, inset, с indicator dots

### Build
- Bash shell использует Java 8 по умолчанию; для Gradle нужен `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"` (JDK 21)
- Supabase URL и ключ из `local.properties` (fallback в build.gradle.kts)
- Demo-пароли: athlete/trainer/etc = "123456", admin = "demo-admin-2026"

---

## 11. Ключевые файлы

### Конфигурация
- `app/build.gradle.kts` — зависимости, SDK, BuildConfig
- `gradle/libs.versions.toml` — версии библиотек
- `local.properties` — Supabase credentials (не в git)

### Точки входа
- `MainActivity.kt` — тема, навигация
- `ui/navigation/NavGraph.kt` — все маршруты
- `ui/navigation/BottomNavItems.kt` — bottom nav по ролям
- `data/remote/SupabaseModule.kt` — клиент Supabase

### Тема
- `ui/theme/Color.kt` — палитры light/dark
- `ui/theme/Theme.kt` — ILeaderTheme, AppColorScheme, LocalAppColors
- `ui/theme/Type.kt` — типографика
- `ui/theme/Shadows.kt` — cardShadow, floatingShadow, coloredShadow

### Компоненты
- `ui/components/DarkThemeComponents.kt` — 50+ переиспользуемых composable (DarkCard, DarkButton, DarkTextField, DarkTopBar, etc.)

### Data Layer
- `data/remote/dto/` — 20+ DTO файлов
- `data/models/` — 8 файлов доменных моделей
- `data/repository/` — 9 репозиториев
- `data/remote/UiState.kt` — sealed class для состояний загрузки

### Auth
- `ui/screens/auth/LoginScreen.kt`
- `ui/screens/auth/RegisterScreen.kt`
- `ui/screens/auth/WelcomeScreen.kt`
- `ui/viewmodels/AuthViewModel.kt` — вход, регистрация, демо-аккаунты

---

## 12. Демо-аккаунты для тестирования

| Роль | Email | Пароль |
|------|-------|--------|
| Admin | admin@mail.ru | demo-admin-2026 |
| Athlete | athlete@demo.com | 123456 |
| Trainer | trainer@demo.com | 123456 |
| Organizer | organizer@demo.com | 123456 |
| Referee | referee@demo.com | 123456 |
| Sponsor | sponsor@demo.com | 123456 |
| Media | media@demo.com | 123456 |
| Content Manager | content@demo.com | 123456 |
| User (Зритель) | user@demo.com | 123456 |

---

## 13. Веб-сайт — справочная информация

Сайт (Next.js 16) — полноценная платформа с ~140 страницами. Ключевые отличия от приложения:

### Что есть на сайте, но НЕ нужно в приложении
- Полный CRUD турниров (5-шаговая форма создания) → мобилка только просмотр
- Полный CRUD статей/медиа → мобилка только просмотр
- Admin панель для управления платформой → веб-only
- Создание курсов → веб-only
- SEO, серверный рендеринг → не применимо

### Что есть на сайте и НУЖНО синхронизировать в приложении
- Схема данных (таблицы, поля, enums) — должны совпадать
- Бизнес-логика (расчёт рейтингов, очков, bracket generation)
- UI/UX паттерны (цвета, карточки, навигация по ролям)
- 8 видов спорта: картинг, стрельба, теннис, футбол, бокс, плавание, лёгкая атлетика, гребля

### Сервисы сайта (21 файл в `src/lib/services/`)
Эти сервисы содержат всю бизнес-логику запросов к Supabase. При реализации аналогичных запросов в Android-репозиториях — сверяйтесь с ними:
- `athletes.service.ts`, `tournaments.service.ts`, `bracket.service.ts`, `teams.service.ts`
- `rankings.service.ts`, `goals.service.ts`, `referee.service.ts`, `sponsor.service.ts`
- `media.service.ts`, `locations.service.ts`, `courses.service.ts`, `leagues.service.ts`
- `notifications.service.ts`, `community.service.ts`, `invites.service.ts`
- `admin.service.ts`, `organizer.service.ts`, `trainer.service.ts`
- `finances.service.ts`, `parental.service.ts`, `exercises.service.ts`

---

## 14. Правила для Claude-чатов

### При работе с кодом
1. **Всегда сверяйтесь с веб-сайтом** (`C:\Users\zhami\Documents\ileader`) для понимания бизнес-логики
2. **Используйте реальные данные** из Supabase — никаких моков
3. **Не раздувайте** — минимум кода для решения задачи
4. **Русские строки** в UI
5. **Используйте DarkTheme.X** для цветов, не создавайте новые Color()
6. **Используйте существующие компоненты** из `DarkThemeComponents.kt`
7. **Запросы к Supabase** — указывайте колонки явно, используйте JOINы
8. **Не меняйте тему/компоненты** без явного запроса

### При планировании
1. **Приложение ≠ копия сайта** — только essential функционал
2. **Организатор/Admin/Media** — минимальный функционал в мобилке
3. **Athlete/Trainer/Referee/Viewer** — основной фокус мобилки
4. **Сложные формы** (создание турниров, статей) → оставить на сайте

### Приоритеты
1. Работающая аутентификация и навигация по ролям
2. Просмотр и регистрация на турниры
3. Результаты и рейтинги
4. Профили и команды
5. Уведомления
6. Всё остальное

---

## 15. TODO — известные блокеры

- [ ] Таблица `articles` существует в БД, но в приложении нет DTO/Repository для неё
- [ ] Таблица `licenses` существует, но RacingLicense экран использует моки
- [ ] `platform_settings` — AdminSettings не сохраняет в БД
- [ ] Edge Function `create-user` — для создания пользователей из Admin-панели
- [ ] 28 экранов на моковых данных нужно переподключить
- [ ] Нет Hilt/DI — Repository создаются в каждом ViewModel отдельно
- [ ] Нет тестов
- [ ] Нет обработки ошибок и логирования
