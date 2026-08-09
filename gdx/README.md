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
rules/    правила и ИИ: Kotlin Multiplatform, таргеты jvm и js
core/     презентация: экраны, актёры, анимации, ассеты, звук
lwjgl3/   десктопная сборка
teavm/    веб-сборка
android/  Android-сборка
ios/      iOS-сборка на RoboVM
tools/    генераторы ассетов: растровые шрифты и иконки
assets/   то, что попадает в сборку
```

Домен (`rules/.../domain`) не зависит от libGDX — правила проверяются обычными
тестами, включая инварианты на 200 полных партиях. Модуль мультиплатформенный,
потому что тот же движок будет считать сетевые партии на сервере: он собирается
и в JVM-байткод для игры, и в JS для serverless-функции. См.
[docs/gdx/11-multiplayer-spec.md](../docs/gdx/11-multiplayer-spec.md).

## Частые команды

```bash
./gradlew :rules:jvmTest                     # тесты домена и сетевого слоя на JVM
./gradlew :rules:jsNodeTest                  # те же тесты на Node
./gradlew :rules:allTests                    # оба таргета сразу
./gradlew core:test                          # тесты презентационного слоя

# JS-библиотека правил для сервера мультиплеера + дымовая проверка фасада
./gradlew :rules:jsNodeProductionLibraryDistribution
node rules/smoke/facade-smoke.cjs
./gradlew lwjgl3:run                         # запустить игру
./gradlew tools:bakeFonts                    # перепечь шрифты из assets_src/fonts
./gradlew tools:bakeIcons                    # перегенерировать иконки приложения
./gradlew :teavm:gdx_teavm_web_js_build      # веб-сборка в teavm/build/dist/js/webapp
./gradlew :teavm:gdx_teavm_web_js_build -PwebDebug   # то же с читаемыми стек-трейсами
```

### Отладочные ключи десктопа

```bash
./gradlew lwjgl3:run -Pfirst.boot=game              # сразу игровой стол
./gradlew lwjgl3:run -Pfirst.boot=loading           # экран загрузки веб-сборки
./gradlew lwjgl3:run -Pfirst.autoplay=true          # обе стороны ведёт ИИ
./gradlew lwjgl3:run -Pfirst.size=720x1280          # портретное окно
./gradlew lwjgl3:run -Pfirst.shots=/tmp/shots -Pfirst.frames=90,600
./gradlew lwjgl3:run -Pfirst.net=duel               # партия через настоящий сервер
./gradlew lwjgl3:run -Pfirst.net=poll               # то же без сокета: работа опросом
./gradlew lwjgl3:run -Pfirst.boot=online            # сразу список комнат

# Два окна играют друг с другом. Через gradlew run так нельзя: две сборки
# одновременно портят один и тот же build-каталог, поэтому собираем jar.
./gradlew lwjgl3:jar
cd assets
java -XstartOnFirstThread -Dfirst.net=host  -Dfirst.profile=host  -jar ../lwjgl3/build/libs/first-game-2.0.0.jar &
java -XstartOnFirstThread -Dfirst.net=guest -Dfirst.profile=guest -jar ../lwjgl3/build/libs/first-game-2.0.0.jar
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
- **Звук на мобильных не стартует.** Браузер держит AudioContext усыплённым и будит
  его только внутри самого обработчика события — из игрового цикла поздно, жест уже
  «протух». Слушатели вешаются на документ в [WebBrowserHooks](teavm/src/main/java/com/first/game/teavm/WebBrowserHooks.java)
  и не снимаются: контекст возобновляется и после сворачивания вкладки.
- **Поворот экрана в вебе.** Заблокировать ориентацию браузер разрешает только
  в полноэкранном режиме, поэтому по первому касанию запрашивается полный экран,
  затем блокировка. iOS Safari блокировку не поддерживает вовсе — там игра остаётся
  в портретной раскладке, она у неё есть.
- **Чёрный экран в фоновой вкладке — не поломка.** Игровой цикл libGDX в вебе
  висит на `requestAnimationFrame`, а его браузер в скрытой вкладке не вызывает
  вовсе (`document.visibilityState === "hidden"` → ноль кадров). Автоматическая
  проверка через расширение браузера показывает чёрный canvas именно поэтому:
  проверять веб нужно в видимом окне.
- **Лишний заголовок ломает запрос только в вебе.** Клиент слал `apikey` во все
  запросы, включая свой API; браузер отбил их preflight-проверкой, потому что
  сервер не перечислил заголовок в `Access-Control-Allow-Headers`. На десктопе
  CORS не проверяется, поэтому баг живёт до первого открытия в браузере.
- **`Actions.removeActor()` перед `Actions.run{}`.** Снятый со сцены актёр больше
  не тикает, поэтому колбэк после удаления никогда не выполнится — очередь
  анимаций встаёт намертво. Удалять актёра нужно внутри `run`.
- **kotlin-android не видит AGP.** Плагины должны объявляться в корневом
  `build.gradle.kts`, иначе они попадают в разные загрузчики классов.
- **`thumbv7` в robovm.xml.** Современный RoboVM такой архитектуры не знает — только `arm64`.

## Требования

JDK 17, Android SDK 36 (для Android), Xcode (для iOS). Gradle приезжает через wrapper.
