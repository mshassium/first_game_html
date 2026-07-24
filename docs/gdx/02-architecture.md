# 02 — Архитектура

## 1. Структура репозитория

Новый проект живёт в подпапке `gdx/`, чтобы корень репозитория остался за старой HTML-версией (GitHub Pages раздаёт `index.html` из корня).

```
first_game_html/
├── index.html, first_v*.html, images/, tests/   ← старая версия, не трогаем
├── docs/gdx/                                    ← этот пакет документов
├── assets_src/                                  ← исходники ассетов (PSD/PNG до упаковки), в git
│   ├── anchor/          # эталонные изображения для reference-генерации
│   ├── cards/           # карты в полном разрешении
│   ├── ui/              # панели, кнопки, иконки
│   ├── vfx/             # элементы эффектов
│   └── raw/             # сырые выдачи нейросети, до кропа (в .gitignore)
└── gdx/
    ├── settings.gradle
    ├── build.gradle
    ├── assets/          # то, что реально идёт в сборку (атласы, шрифты, звук, i18n)
    ├── core/            # вся игра, кроссплатформенно
    ├── lwjgl3/          # desktop
    ├── android/
    ├── ios/             # RoboVM
    └── teavm/           # web
```

Генерация каркаса — **gdx-liftoff** (актуальная замена устаревшему gdx-setup). Проверить на момент старта актуальные версии: libGDX 1.13.x, Kotlin 2.1.x, Gradle 8.x, `gdx-teavm` 1.3.x, RoboVM 2.3.x.

## 2. Модули `core`

```
core/src/main/kotlin/com/first/game/
├── domain/            # ЧИСТЫЙ Kotlin, ноль импортов com.badlogic.*
│   ├── Model.kt       # Letter, Side, SideState, GameState, Traps
│   ├── Command.kt     # PlayCard, ChooseForbid, ChooseRecover, ChooseSteal, DiscardForTrap, RollDice
│   ├── Event.kt       # GameEvent — что произошло, для анимации и лога
│   ├── GameEngine.kt  # (GameState, Command) -> Result(GameState, List<GameEvent>)
│   ├── Rng.kt         # интерфейс + seedable-реализация (для детерминированных тестов)
│   └── ai/
│       ├── AiPolicy.kt        # интерфейс: (GameState) -> Command
│       ├── EasyAi.kt          # текущая эвристика
│       ├── NormalAi.kt        # + защита от победы игрока
│       └── HardAi.kt          # 1-плай перебор с оценкой
├── presentation/
│   ├── screens/       # SplashScreen, MenuScreen, RulesScreen, GameScreen, SettingsScreen
│   ├── actors/        # CardActor, SpaceZoneActor, DiscardPileActor, HandActor, HudActor, DieActor
│   ├── anim/          # AnimationDirector — переводит GameEvent в Scene2D Actions
│   └── layout/        # LandscapeLayout, PortraitLayout
├── assets/            # AssetDescriptors, AssetLoaderService, Fonts, Skins
├── audio/             # SoundManager, MusicManager
├── i18n/              # обёртка над I18NBundle
└── FirstGame.kt       # KtxGame, точка входа
```

**Правило зависимостей:** `presentation` → `domain` (односторонне). `domain` не знает ни о libGDX, ни об экранах. Нарушение этого правила ломает главное преимущество архитектуры и должно ловиться на ревью.

## 3. Движок как редьюсер + поток событий

Главная проблема текущей HTML-версии: `render()` вызывается изнутри анимационных колбэков, состояние и визуал переплетены, из-за чего логика ловушек и запретов уже дважды переписывалась (см. историю коммитов `fix trap logic`).

Новая схема:

```kotlin
data class EngineResult(val state: GameState, val events: List<GameEvent>)

interface GameEngine {
    fun apply(state: GameState, command: Command): EngineResult
}
```

- Команда применяется **мгновенно**, новое состояние доступно сразу.
- Список `GameEvent` описывает, что показать: `CardDrawn`, `CardPlayed`, `CardForbidden`, `CardRecovered`, `CardStolen`, `TrapSet`, `TrapTriggered`, `HandOverflow`, `ChoiceRequired`, `GameEnded`.
- `AnimationDirector` складывает события в очередь и проигрывает их по одному, блокируя ввод. По завершении очереди ввод разблокируется.
- Если игрок нажимает «пропустить» — очередь доигрывается мгновенно (все Actions выполняются с `duration = 0`).

Плюсы: логика тестируется без графики; анимации можно менять, не боясь сломать правила; ИИ может «проиграть» партию в цикле для отладки.

**Интерактивные выборы** (какую букву запретить, что вернуть из сброса) моделируются событием `ChoiceRequired(kind, options)`. Движок при этом переходит в состояние `awaitingChoice` и не принимает других команд, пока не придёт `Choose*`-команда. Это убирает колбэк-ад текущей версии.

## 4. Экраны

| Экран | Содержимое |
|---|---|
| `SplashScreen` | Логотип + прогресс-бар загрузки ассетов (критично для веба) |
| `MenuScreen` | Логотип, 5 карт веером с idle-анимацией, кнопки: Играть / Правила / Настройки |
| `RulesScreen` | Скроллящийся текст правил + карточки-примеры |
| `SettingsScreen` | Язык (EN/RU), громкость музыки, громкость SFX, скорость анимаций, сложность ИИ |
| `GameScreen` | Игровой стол, HUD, лог, модалки |
| Оверлеи | `DiceOverlay`, `ChoiceDialog`, `GameOverDialog`, `PauseMenu` |

Переходы между экранами — кроссфейд 0.25 с через общий `ScreenTransition`.

## 5. Ассеты и загрузка

- `AssetManager` (ktx-assets-async) с явными `AssetDescriptor`.
- Два этапа: **splash-набор** (логотип, шрифт прогресс-бара, фон загрузки — грузится синхронно, ~200 КБ) и **основной набор** (всё остальное, с прогрессом).
- Музыка грузится **лениво**, после входа в меню, чтобы не задерживать первый экран.
- Атласы:
  - `ui.atlas` — панели, кнопки, иконки, рамки (2048×2048)
  - `cards.atlas` — лицевые стороны карт, рубашка (2048×2048)
  - `vfx.atlas` — элементы эффектов и партиклов (1024×1024)
  - Фон стола — отдельная текстура, не в атласе (крупная)
- Формат: PNG с премультиплицированной альфой не используем (усложняет блендинг в Scene2D); обычная straight alpha, `SpriteBatch` в режиме `GL_SRC_ALPHA`.
- Фильтрация: `Linear, Linear` + мипмапы для карт (они сильно масштабируются между рукой и столом).

## 6. Раскладка и адаптивность

- **Viewport:** `ExtendViewport(1280, 720)` с минимальным миром 1280×720 и расширением по длинной стороне. Это покрывает и 16:9, и 20:9 (современные телефоны), и 4:3 (планшеты) без чёрных полос.
- **Две раскладки** (как в текущей версии):
  - **Landscape:** левая колонка — SPACE ИИ / SPACE игрока / рука; правая колонка (фикс. ширина ~300 px мира) — сброс ИИ, сброс игрока, лог боя.
  - **Portrait:** сверху горизонтальный ряд «сброс ИИ | сброс игрока», под ним SPACE ИИ, SPACE игрока, рука. Лог боя скрывается в выдвижную панель по кнопке.
- Переключение раскладки — по `worldWidth / worldHeight < 1.0` в `resize()`.
- Размер карты вычисляется от доступной высоты зоны и от условия «7 карт руки помещаются в ряд без скролла» — портируем логику `computeAdaptiveCardWidth()`, но в мировых координатах, что сильно проще, чем в CSS.
- Safe area на iOS/Android (вырезы) — отступы через `Gdx.graphics.getSafeInsets()` (iOS) и `WindowInsets` (Android), пробрасываются в `core` через интерфейс `PlatformInsets`.

## 7. Шрифты и текст

- Генерация в рантайме через **gdx-freetype** (на TeaVM freetype не работает → для веба используем **предгенерированные `.fnt` + PNG-страницы**). Практичное решение: генерировать `.fnt` заранее для всех платформ (Hiero / `gdx-fontpack`), freetype не тащить вообще. Это упрощает сборку и снимает риск TeaVM.
- Три гарнитуры (см. [04-asset-list.md](04-asset-list.md) §4): декоративная для букв карт, титульная с кириллицей, основная с кириллицей.
- Размеры: генерируем по 2–3 кегля на гарнитуру (например 28/44/72) и масштабируем между ними — так текст остаётся чётким на 4K-десктопе и на телефоне.
- Все строки — в `assets/i18n/strings_en.properties` и `strings_ru.properties`. В коде **ни одной строки в литерале**.

## 8. Настройки и сохранение

`Preferences` (`first_game_prefs`): язык, громкости, скорость анимаций, сложность ИИ, лучшее время партии. Партия между сессиями не сохраняется (D-8).

## 9. Ограничения по платформам

| Платформа | Что учесть |
|---|---|
| Web (TeaVM) | Нет reflection, нет freetype, аудио стартует только после первого пользовательского жеста; вес сборки критичен |
| Android | minSdk 21; текстуры ≤ 2048; корректная обработка `pause/resume` (потеря GL-контекста → `AssetManager` перезагружает) |
| iOS | RoboVM; те же лимиты текстур; safe area; сборка требует macOS + Xcode |
| Desktop | Ресайз окна произвольный, полноэкранный режим по F11 |
