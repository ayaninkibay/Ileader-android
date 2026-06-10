# Setup — локальный запуск

## Prerequisites

1. **Android Studio** Hedgehog (2023.1.1) или новее — поставит вместе с собой JDK 21 (нужен для Gradle).
2. **Android SDK** API 36 (targetSdk). Studio предложит установить при первом открытии.
3. **Git**.

JDK 21 уже идёт в комплекте с Android Studio как `jbr` (JetBrains Runtime). Отдельно ставить не нужно.

---

## Первичная настройка

### 1. Клонировать репозиторий

```bash
git clone <repo-url> ileader-android
cd ileader-android
```

### 2. Создать `local.properties`

Файл должен лежать в корне проекта (рядом с `gradlew`). В нём — Supabase credentials и путь к Android SDK:

```properties
# Android Studio добавляет эту строку автоматически при первом открытии
sdk.dir=C:\\Users\\<user>\\AppData\\Local\\Android\\Sdk

# Supabase — запроси ключи у тимлида
supabase.url=https://clkbmjsmfzjuqdwnoejv.supabase.co
supabase.anon.key=<anon-key>

# Демо-пароли (нужны для debug-сборки, в release демо-кнопки скрыты)
demo.password=123456
demo.admin.password=demo-admin-2026

# Release signing — нужно только для production-AAB.
# Если не задано, release-сборка получается unsigned; подписать
# можно вручную через Build → Generate Signed Bundle в Android Studio.
release.store.file=release.keystore
release.store.password=<store-password>
release.key.alias=ileader
release.key.password=<key-password>
```

> `local.properties` в `.gitignore` — никогда не коммить ключи и keystore.

### 3. Открыть в Android Studio

`File → Open → <папка проекта>`. Studio сама запустит Gradle sync. Первый раз ~5–10 минут — скачивает зависимости.

---

## Запуск приложения

### Через Android Studio

1. Выбери устройство (физический телефон через USB-debug или эмулятор).
2. Конфигурация запуска `app` уже создана.
3. Кнопка ▶ Run.

### Через консоль

```bash
# Debug build на подключённое устройство
./gradlew installDebug

# Запустить активность
adb shell am start -n com.ileader.app/.MainActivity
```

---

## Gradle из bash / Git Bash (Windows)

Git Bash по умолчанию использует JDK 8, что ломает сборку. Нужно явно указать JDK 21 из Android Studio:

```bash
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"
./gradlew build
```

Можно добавить экспорт в `~/.bashrc` чтобы не повторять каждый раз.

В **PowerShell** и Android Studio это не требуется — там JDK подхватывается автоматически.

---

## Полезные команды

```bash
# Только Kotlin-компиляция (быстро, для проверки правок)
./gradlew :app:compileDebugKotlin

# Чистая сборка
./gradlew clean assembleDebug

# Установить debug-APK
./gradlew installDebug

# Удалить с устройства
adb uninstall com.ileader.app

# Логи приложения (фильтр по тегу)
adb logcat -s iLeader

# Release-APK с минификацией (R8). Если signing настроен в local.properties — будет подписан
./gradlew assembleRelease

# Release-AAB для Google Play (формат загрузки в Play Console)
./gradlew bundleRelease
```

---

## Релиз в Google Play

### 1. Создать keystore (один раз)

```bash
keytool -genkey -v -keystore release.keystore -alias ileader \
    -keyalg RSA -keysize 2048 -validity 10000
```

Положи `release.keystore` в корень проекта (рядом с `gradlew`). Файл **никогда** не коммитится в git — он в `.gitignore`.

> Если потеряешь keystore — обновления в Play Store будут невозможны. Сделай бэкап в защищённом месте (1Password, Bitwarden, Google Drive с 2FA).

### 2. Прописать пути и пароли в `local.properties`

См. блок `release.store.*` и `release.key.*` выше.

### 3. Собрать подписанный AAB

```bash
./gradlew bundleRelease
```

Результат: `app/build/outputs/bundle/release/app-release.aab` — это файл для загрузки в Play Console.

### 4. Проверить размер и содержимое

```bash
# Размер APK (внутри AAB)
./gradlew :app:analyzeReleaseBundle    # если есть AGP-аналайзер

# Или установить unsigned APK на устройство и потестировать
adb install app/build/outputs/apk/release/app-release.apk
```

### 5. Загрузить в Play Console

`play.google.com/console` → твой проект → Production → Create new release → upload AAB.

### Чек-лист перед загрузкой

- [ ] `versionCode` увеличен с предыдущего релиза (если не первый)
- [ ] `versionName` соответствует семантическому версионированию
- [ ] `release.keystore` существует и `local.properties` его видит
- [ ] AAB собирается без ошибок (`./gradlew bundleRelease`)
- [ ] Иконки в `mipmap-*` — финальные, не плейсхолдеры
- [ ] Privacy policy URL готов (Google требует для приложений с авторизацией)
- [ ] Скриншоты для Play Store сделаны на физическом устройстве (не Compose Preview)
- [ ] Описание на русском и казахском подготовлено

---

## Демо-аккаунты

На экране входа есть кнопки «Войти как Athlete», «Войти как Organizer» и т.д. — для быстрого тестирования под разными ролями. Пароли подставляются автоматически из `BuildConfig`.

Если хочешь зайти руками — список логинов в [README.md](README.md#демо-аккаунты-для-тестирования).

---

## Частые проблемы

### `Unsupported class file major version`
Gradle подхватил JDK 8 вместо 21. Проверь:
```bash
./gradlew --version    # должно показать JVM 21
```
Если 8 — экспортируй `JAVA_HOME` (см. выше) или открывай через Android Studio.

### `SUPABASE_URL: null` при старте
Не создан `local.properties` или нет ключей. Скопируй из шаблона выше.

### Дублирующиеся KSP-кеши при сборке
```
Storage for [...kspCaches...] is already registered
```
Это известный артефакт инкрементальной сборки Gradle. Лечится:
```bash
./gradlew clean
```

### `Could not resolve all files for configuration`
Нет сети или firewall блокирует `repo.maven.apache.org` / `dl.google.com`. Проверь VPN.

### Эмулятор не видит устройство
```bash
adb kill-server && adb start-server
adb devices    # должно показать <id> device
```

---

## Дальше

- **Архитектура и конвенции** → [README.md](README.md)
- **Схема БД** — Supabase dashboard проекта `clkbmjsmfzjuqdwnoejv`
- **Бизнес-логика** — веб-проект `C:\Users\zhami\Documents\ileader`, особенно сервисы в `src/lib/services/`
