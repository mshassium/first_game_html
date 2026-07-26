# F!RST — libGDX-версия

Ремейк карточной игры F!RST на Kotlin + libGDX. Проектные документы — в [../docs/gdx](../docs/gdx).
Старая HTML-версия живёт в корне репозитория и не затрагивается.

## Что уже работает

| Платформа | Состояние | Команда |
|---|---|---|
| Десктоп (LWJGL3) | Играется | `./gradlew lwjgl3:run` |
| Веб (TeaVM) | Играется | `./gradlew :teavm:gdx_teavm_web_js_build` |
| Android | APK собирается, 4.4 МБ | `./gradlew :android:assembleDebug` |
| iOS (RoboVM) | Запускается в симуляторе | `./gradlew :ios:launchIPhoneSimulator` |

Правила игры перенесены полностью, три уровня ИИ, i18n EN/RU, анимации,
адаптивная раскладка landscape/portrait.

**Графика пока программная** — [PlaceholderArt](core/src/main/kotlin/com/first/game/assets/PlaceholderArt.kt)
рисует карты и панели в тех же пропорциях, что описаны в
[04-asset-list.md](../docs/gdx/04-asset-list.md). Когда в `assets/atlas/` появятся
настоящие атласы, `Assets` подхватит их без правок в экранах.

## Структура

```
core/     вся игра: домен (правила, ИИ) + презентация (экраны, актёры, анимации)
lwjgl3/   десктопная сборка
teavm/    веб-сборка
android/  Android-сборка
ios/      iOS-сборка на RoboVM
tools/    генераторы ассетов: растровые шрифты и иконки
assets/   то, что попадает в сборку
```

Домен (`core/.../domain`) не зависит от libGDX — правила проверяются обычными
JVM-тестами, включая инварианты на 200 полных партиях.

## Частые команды

```bash
./gradlew core:test                          # 38 тестов домена
./gradlew lwjgl3:run                         # запустить игру
./gradlew tools:bakeFonts                    # перепечь шрифты из assets_src/fonts
./gradlew tools:bakeIcons                    # перегенерировать иконки приложения
./gradlew :teavm:gdx_teavm_web_js_build      # веб-сборка в teavm/build/dist/js/webapp
./gradlew :teavm:gdx_teavm_web_js_build -PwebDebug   # то же с читаемыми стек-трейсами
```

### Отладочные ключи десктопа

```bash
./gradlew lwjgl3:run -Pfirst.boot=game              # сразу игровой стол
./gradlew lwjgl3:run -Pfirst.autoplay=true          # обе стороны ведёт ИИ
./gradlew lwjgl3:run -Pfirst.size=720x1280          # портретное окно
./gradlew lwjgl3:run -Pfirst.shots=/tmp/shots -Pfirst.frames=90,600
```

Последний снимает кадры в PNG и закрывает приложение — так проверяются экраны
без прав на системный скриншот.

## Грабли, на которые уже наступили

- **Веб чёрный экран.** `WebApplicationConfiguration.preloadListener` обязателен:
  без него бэкенд не запускает загрузку ассетов. Слушатель может быть пустым.
- **Чёрные текстуры в вебе.** Текстуры не степени двойки с мипмапами WebGL1
  считает неполными и рисует чёрным. Наступали дважды: сначала на программных
  заглушках, потом на фонах 1440×810. Проверено опытом: та же текстура рядом,
  с мипмапами и без, даёт `0,0,0` и `93,61,43`. **Мипмапы в проекте не используются
  нигде** — ни в атласах, ни в шрифтах, ни в фонах.
- **`Actions.removeActor()` перед `Actions.run{}`.** Снятый со сцены актёр больше
  не тикает, поэтому колбэк после удаления никогда не выполнится — очередь
  анимаций встаёт намертво. Удалять актёра нужно внутри `run`.
- **kotlin-android не видит AGP.** Плагины должны объявляться в корневом
  `build.gradle.kts`, иначе они попадают в разные загрузчики классов.
- **`thumbv7` в robovm.xml.** Современный RoboVM такой архитектуры не знает — только `arm64`.

## Требования

JDK 17, Android SDK 36 (для Android), Xcode (для iOS). Gradle приезжает через wrapper.
