# 08 — Сборка, платформы, деплой

## 1. Требования к окружению

| Инструмент | Версия | Для чего |
|---|---|---|
| JDK | **17** (Temurin) | Все модули. libGDX 1.13 требует ≥ 11, Android — 17 |
| Gradle | 8.x (wrapper в репозитории) | Сборка |
| Android SDK | Platform 34, Build-Tools 34, minSdk 21 | Android |
| Xcode | 15+ | iOS (только на macOS) |
| Node.js | не требуется | TeaVM собирается Gradle'ом |

Версии libGDX/KTX/TeaVM/RoboVM зафиксировать в `gradle.properties` и **проверить актуальность на момент генерации проекта** через gdx-liftoff.

## 2. Генерация каркаса

Проект создаётся утилитой **gdx-liftoff** (актуальная замена gdx-setup):

- Package: `com.first.game`, Main class: `FirstGame`
- Language: **Kotlin**
- Platforms: `Desktop (LWJGL3)`, `Android`, `iOS (RoboVM)`, `Web (TeaVM)`
- Extensions: только `gdx-freetype` **не подключаем** (см. [02-architecture.md](02-architecture.md) §7)
- Third-party: `KTX` (app, actors, assets-async, scene2d, graphics, collections)

Результат кладём в `gdx/` внутри существующего репозитория.

## 3. Команды сборки

```bash
cd gdx

# Desktop — запуск
./gradlew lwjgl3:run

# Desktop — исполняемый jar
./gradlew lwjgl3:jar
# → lwjgl3/build/libs/first-game-<ver>.jar   (кроссплатформенный fat jar)

# Android — debug APK
./gradlew android:assembleDebug
# → android/build/outputs/apk/debug/android-debug.apk

# Android — release AAB (для Google Play)
./gradlew android:bundleRelease

# Web — сборка
./gradlew teavm:build
# → teavm/build/dist/    (index.html + .js + assets)

# Web — локальный запуск с автообновлением
./gradlew teavm:run

# iOS — симулятор
./gradlew ios:launchIPhoneSimulator

# iOS — сборка ipa (нужен профиль подписи)
./gradlew ios:createIPA

# Упаковка атласов (если подключён gdx-texturepacker-gradle-plugin)
./gradlew packTextures

# Тесты домена
./gradlew core:test
```

## 4. Особенности каждой платформы

### 4.1 Desktop (LWJGL3)
- Стартовое окно 1280×720, ресайз разрешён, минимум 800×450.
- Полный экран по `F11`, выход по `Esc` (с подтверждением во время партии).
- Иконка окна в трёх размерах (16/32/128).
- Для распространения без JVM — опционально `jpackage` или `Packr` (не входит в M8).

### 4.2 Android
- `minSdk 21`, `targetSdk 34`.
- Ориентация: **`sensor`** — поддерживаем и портрет, и ландшафт (раскладка адаптируется).
- Обязательно обработать `onPause/onResume` — при сворачивании музыка глушится, таймер партии останавливается.
- Adaptive icon: foreground (эмблема) + background (тёмное дерево).
- Вырезы экрана: `layoutInDisplayCutoutMode = shortEdges` + учёт insets в раскладке.
- Release-подпись: keystore **не коммитить**, путь и пароли — через переменные окружения / GitHub Secrets.

### 4.3 iOS (RoboVM)
- Требует macOS + Xcode. Симулятор — бесплатно; реальное устройство и App Store — платный Apple Developer.
- `Info.plist`: поддержка обеих ориентаций, `UIRequiresFullScreen`, safe-area.
- Иконки всех требуемых размеров + launch screen (storyboard).
- Ограничение по размеру бинарника: следить, что атласы не раздувают IPA.
- **В CI собираем только компиляцию** (без подписи), фактическая публикация — вручную.

### 4.4 Web (TeaVM)
- Главный ограничитель — **вес** и **время первой загрузки**.
- Обязательно: экран загрузки с прогрессом, показывающийся до старта GL.
- `index.html` перекрыть своим шаблоном: тёмный фон, центрирование canvas, `<meta viewport>` с `viewport-fit=cover`, запрет масштабирования жестами.
- Аудио — только после первого жеста (см. [07-audio-spec.md](07-audio-spec.md) §6).
- Проверять сборку `:teavm:build` **на каждом этапе**, а не в конце: TeaVM спотыкается на отдельных конструкциях Java/Kotlin-стандартной библиотеки, и найти виновника проще на маленьком дельта-изменении.
- Кэширование: файлы ассетов с хэшем в имени + `Cache-Control: immutable`; `index.html` — `no-cache`.

## 5. CI/CD (GitHub Actions)

Три workflow в `.github/workflows/`:

### 5.1 `ci.yml` — на каждый push и PR
```
jobs:
  test:     ubuntu → ./gradlew core:test
  desktop:  ubuntu → ./gradlew lwjgl3:jar          (артефакт)
  web:      ubuntu → ./gradlew teavm:build         (артефакт)
  android:  ubuntu → ./gradlew android:assembleDebug (артефакт)
  ios:      macos  → ./gradlew ios:createIPA -PskipSigning  (только компиляция)
```
Матрица кэширует `~/.gradle` — иначе сборка занимает минуты.

### 5.2 `deploy-web.yml` — на push в `main` и на теги `v*`
1. `./gradlew teavm:build`
2. Скопировать `teavm/build/dist/*` в `v2/` рабочего дерева ветки `gh-pages`
3. Сохранить в `gh-pages` корневые файлы старой версии **без изменений** (`index.html`, `first_v*.html`, `images/`, `CNAME`)
4. Закоммитить и запушить `gh-pages`

Итог: `https://first-game.com/` — старая игра, `https://first-game.com/v2/` — новая.

> Важно: `CNAME` должен присутствовать в ветке, которую раздаёт Pages, иначе кастомный домен отвалится. Проверить это первым же деплоем.

### 5.3 `release.yml` — на тег `v*`
Собирает desktop-jar и Android-AAB/APK, создаёт GitHub Release, прикладывает артефакты и ссылку на веб-версию.

## 6. Ветки и версионирование

- Разработка — в `feature/libgdx-remake`, мердж в `main` по завершении M8 (или раньше, если веб-деплой в `/v2/` не мешает основной версии — он не мешает).
- Версионирование: `v2.0.0` для первого релиза ремейка. Старая HTML-версия остаётся как «v1».
- Номер версии — единственный источник в `gradle.properties` (`version=2.0.0`), пробрасывается в Android `versionName`, iOS `CFBundleShortVersionString` и в экран настроек.

## 7. Чек-лист релиза

- [ ] `./gradlew core:test` зелёный
- [ ] Веб-версия открывается в Chrome, Safari, Firefox; на iPhone и Android-телефоне в браузере
- [ ] Время до игрового меню в вебе на среднем канале ≤ 8 с
- [ ] APK ставится и играется на реальном Android, обе ориентации
- [ ] iOS-сборка запускается на симуляторе, обе ориентации
- [ ] Desktop-jar запускается на macOS и Windows
- [ ] Обе локали (EN/RU) без обрезанного текста ни на одном экране
- [ ] Звук выключается и включается, настройки переживают перезапуск
- [ ] Партия проходится до победы и до поражения, обе концовки корректны
- [ ] `first-game.com/` (старая версия) не пострадал
