# Лог генерации ассетов

Что здесь: какой ассет, когда, чем и с какого раза получен, и что при этом выяснилось.
Нужен ровно для одного — чтобы через полгода можно было догенерировать одну карту
в том же стиле, не восстанавливая контекст заново.

Промпты живут в [docs/gdx/05-prompt-book.md](../docs/gdx/05-prompt-book.md) и оттуда
копируются целиком. Здесь дублируется только промпт эталона: он задаёт стиль всему
набору, и его важно иметь под рукой в неизменном виде.

## Правила ведения

- Строка добавляется, когда ассет **принят**, а не когда сгенерирован.
- Если пришлось отклониться от промпта из бука — записать, как именно, и обновить бук.
- Столбец «Попыток» — сколько раз пришлось перезапускать до приёмки. По нему видно,
  какие промпты стоит переписать.

## Сводка

| # | Ассет | Файл | Дата | Модель | Попыток | Статус |
|---|---|---|---|---|---|---|
| C-01 | Карта F — Печать Запрета (эталон) | `anchor/card_F_anchor.png` | 2026-07-26 | OpenAI | — | **Принят** |
| C-02 | Карта I — Родник Изобилия | `cards/card_I.png` | 2026-07-26 | OpenAI | — | **Принят** |
| C-03 | Карта R — Реликварий | `cards/card_R.png` | 2026-07-26 | OpenAI | — | **Принят** |
| C-04 | Карта S — Тень Похитителя | `cards/card_S.png` | 2026-07-26 | OpenAI | — | **Принят** |
| C-05 | Карта T — Капкан Чародея | `cards/card_T.png` | 2026-07-26 | OpenAI | — | **Принят** |
| C-06 | Рубашка карты | `cards/card_back.png` | 2026-07-26 | OpenAI | — | **Принят** |
| U-01…U-17 | Панели и кнопки (лист) | `raw/sheet_panels.png` | 2026-07-26 | OpenAI | — | **Принят**, нарезан на 12 файлов |
| U-06 | Слот под карту | `ui/slot_card.png` | 2026-07-26 | OpenAI | 2 | **Принят** со второй попытки |
| U-09 | Урна сброса | `ui/discard_urn.png` | 2026-07-26 | OpenAI | 2 | **Принят** |
| U-08 | Стопка колоды | `ui/deck_stack.png` | 2026-07-26 | OpenAI | 2 | **Принят**, рубашка совпадает с C-06 |
| U-18…U-33 | Иконки (лист 4×4) | `raw/icons.png` | 2026-07-26 | OpenAI | 1 | **Принят**, 14 из 16 по заказу |
| U-44…U-49 | Кубики (лист) | `ui/sheet_dice.png` | — | — | — | Не начат |
| U-40 | Портрет игрока | `ui/portrait_player.png` | — | — | — | Не начат |
| U-41 | Портрет оппонента | `ui/portrait_ai.png` | — | — | — | Не начат |
| U-42 | Рама портрета | `ui/frame_portrait.png` | — | — | — | Не начат |
| U-51 | Эмблема Ордена | `ui/emblem_first.png` | — | — | — | Не начат |
| B-01 | Фон стола, горизонтальный | `bg/bg_table_landscape.png` | — | — | — | Не начат |
| B-02 | Фон стола, вертикальный | `bg/bg_table_portrait.png` | — | — | — | Не начат |
| B-03 | Фон меню | `bg/bg_menu.png` | — | — | — | Не начат |
| B-04 | Фон загрузки | `bg/bg_loading.png` | — | — | — | Не начат |
| V-01…V-08 | VFX, нейтральные (лист) | `vfx/sheet_neutral.png` | — | — | — | Не начат |
| V-09…V-14 | VFX, тематические (лист) | `vfx/sheet_schools.png` | — | — | — | Не начат |

Шрифты и иконки приложения нейросетью не генерируются: первые печёт `./gradlew tools:bakeFonts`
из OFL-шрифтов в `fonts/`, вторые — `./gradlew tools:bakeIcons`.

---

## C-01 — Карта F, эталон стиля

- **Файл:** `anchor/card_F_anchor.png`, PNG, 1024×1536, 3.7 МБ
- **Дата:** 26 июля 2026
- **Модель:** OpenAI (генерация в чате)
- **Промпт:** взят из промпт-бука без единого изменения
- **Результат:** принят без замечаний

### Что этот ассет зафиксировал для всего набора

- тёплая дубовая рамка, четыре бронзово-золотые угловые накладки;
- верхняя гранитная плашка-картуш с тонкой светящейся окантовкой цвета школы — пустая;
- арочный медальон: главный объект парит над низким каменным постаментом, вокруг —
  свечение цвета школы и частицы в воздухе;
- пергаментная лента и нижняя гранитная плашка — пустые;
- самоцвет цвета школы в центре нижней части рамки;
- свет сверху-слева, холодная подсветка снизу, живописный мазок, лёгкая потёртость.

Все последующие промпты написаны так, чтобы это воспроизводилось, и требуют приложить
этот файл как reference.

### Отклонения от промпта, которые приняты как нормальные

| Что просили | Что вышло | Решение |
|---|---|---|
| Идеально белый фон | Светло-серый градиент с мягкой тенью под картой | Принято: фон всё равно удаляется при постобработке. Требование «pure white» в промптах оставлено — оно удерживает модель от рисования сцены вокруг объекта |
| Поля 5 % по периметру | Поля есть, но карта смещена к центру с тенью снизу | Принято, кроп решает |

### Что нужно сделать с файлом перед вставкой в игру

1. Удалить фон и тень, оставить чистую альфу по контуру карты.
2. Обрезать по контуру, привести к 1024×1536.
3. Уменьшить до 512×768 (Lanczos), прогнать через `pngquant --quality=80-95`.
4. Положить как `card_F.png` в набор для упаковки в `cards.atlas`.

Подробности — [04-asset-list.md](../docs/gdx/04-asset-list.md) §7.

### Промпт эталона, дословно

```text
Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.

Layout from top to bottom, occupying the full card:
— top 6% is the outer wooden frame;
— from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint azure inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
— from 30% to 72% there is an arched medallion window containing the artwork: a heavy ancient rune-carved padlock wrapped in glowing pale-azure ice chains, floating above a frost-covered stone pedestal, cold azure light radiating outward, frost crystals in the air;
— from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
— from 82% to 95% there is an EMPTY recessed dark plate for description, blank, with no writing;
— at the bottom centre, embedded in the frame, a smooth glowing azure gemstone cabochon.

A thin glowing azure line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is azure blue (#9CC8FF); the wood and bronze are neutral warm brown.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo.

Avoid: photorealism, 3D render, CGI, anime, pixel art, flat vector, neon, cyberpunk, modern objects, text of any kind, letters, numerals, filled cartouche, engraved symbols inside the empty plates.

Aspect ratio 2:3.
```

---

## C-02…C-06 — остальные карты и рубашка

- **Файлы:** `cards/card_I.png`, `card_R.png`, `card_S.png`, `card_T.png`, `card_back.png`
- **Дата:** 26 июля 2026
- **Модель:** OpenAI, промпты из бука §2.1–2.5 с приложенным эталоном
- **Результат:** приняты, набор визуально консистентен — рамка, компоновка
  и постамент под объектом совпадают с эталоном, различаются только иллюстрация
  и цвет школы

Сырьё лежит в `raw/`, чистые версии с альфой — в `cards/`.

## U-01…U-17 — панели и кнопки

- **Файл сырья:** `raw/sheet_panels.png`, 1024×1024
- **Дата:** 26 июля 2026
- **Модель:** OpenAI, промпт из бука §3.1

### Отклонение от промпта

Промпт просил три ряда: 3 панели, 3 главные кнопки, 4 второстепенных.
Модель нарисовала другую раскладку — **4 панели сеткой 2×2, 5 широких кнопок
и 3 круглых**, всего 12 элементов. Это лучше, чем просили: лишняя панель ушла
под рамку модального окна.

Имена состояний кнопок расставлены не на глаз, а по замеру центральной области:
«up» — самый светлый вариант, «down» — самый тёмный, «disabled» — наименее
насыщенный. Две пары пришлось поменять местами относительно порядка чтения,
итоговый порядок зашит в пресет `panels` нарезчика.

### Как нарезано

```bash
./gradlew tools:sliceSheet -Psheet="../assets_src/raw/sheet_panels.png" -Ppreset=panels -Pout=../assets_src/ui
```

## U-06, U-08, U-09 — слот, урна и колода: разбор первой попытки

- **Дата:** 26 июля 2026
- **Модель:** OpenAI, промпт из бука (тогда — один общий на три объекта)
- **Результат:** слот и колода отклонены, урна принимается с оговоркой

### Замер цветовой температуры

Среднее по материалу, «тепло» = R − B:

| Ассет | Тепло | Яркость |
|---|---|---|
| card_F, рамка (эталон) | +29 | 97 |
| panel_wood (принят) | +54 | 69 |
| panel_stone (принят) | +21 | 100 |
| card_back (принят) | +37 | 63 |
| слот (попытка 1) | **−23** | 61 |
| урна (попытка 1) | **−4** | 53 |
| колода (попытка 1) | **−1** | 48 |

Весь принятый набор тёплый, новые элементы — холодные. По слоту разрыв 44 пункта.

### Почему отклонено

- **Слот.** Холодный синий камень плюс азурное свечение контура. Азур `#9CC8FF` —
  цвет школы F, а слот нейтральный: в него кладут карты любой буквы и обе стороны.
  Игрок прочитает синий контур как «здесь запрет». Плюс подсветку активной зоны
  игра уже рисует сама, в цвет стороны, и своё свечение слота с ней конфликтует.
- **Колода.** Модель нарисовала собственную рубашку — золотая звезда-компас
  с растительным орнаментом вместо принятого бронзового кольца с пятью самоцветами.
  В игре карта вылетает со стопки, и рубашка на глазах игрока сменилась бы на другую.
  Плюс выраженная перспектива три четверти против плоских фронтальных карт.
- **Урна.** Ближе всех: камень, бронзовые обручи, золотой сигил. Холод −4 правится
  тёплым проходом при постобработке.

### Что изменено в промпт-буке

Общий промпт разбит на три (§3.2 слот, §3.3 урна, §3.4 колода). В §0 добавлены
четыре правила: явно требовать тёплую гамму, не ставить цветное свечение на нейтральные
предметы, прикладывать уже принятые элементы вторым референсом, держать плоский ракурс.

## U-18…U-33 — иконки интерфейса

- **Файл сырья:** `raw/icons.png`, 1024×1024
- **Дата:** 26 июля 2026
- **Модель:** OpenAI, промпт из бука §3.5
- **Результат:** принят, 16 иконок нарезаны, тепло +64 — в тон принятому набору

### Отклонение от заказа

Из 16 символов 14 нарисованы верно. Два абстрактных модель заменила иллюстрациями:

| Позиция | Заказано | Нарисовано | Как названо |
|---|---|---|---|
| 6 | раскрытая книга | перечёркнутая книга | `icon_rules_off` |
| 7 | три полосы (меню) | раскрытая книга | `icon_rules` |
| 8 | косой крест (закрыть) | скрещённые мечи | `icon_duel` |

Мечи оказались удачной находкой — ушли на кнопку «Играть». Иконки меню и закрытия
в наборе отсутствуют, догенерируются мини-листом по §3.5.1. Абстрактные символы
в промпте усилены: прописные буквы плюс отдельный абзац с запретом заменять их предметами.

### Что подключено в игре

Меню: `duel` на «Играть», `rules`, `settings`, `lang`. HUD: `hand`, `deck`, `duel`
перед счётчиками и `hourglass` перед таймером.

## U-06, U-08, U-09 — вторая попытка, принято

- **Дата:** 26 июля 2026
- **Промпты:** бук §3.2, §3.3, §3.4 после разбивки

Замер подтвердил исправление: тепло (R−B) слот +55, урна +58, колода +53 против
−23, −4 и −1 в первой попытке. Диапазон принятого набора — +21…+54, то есть новые
объекты попали в тон. Колода показывает ту же рубашку, что и карты.

Файлы пришли с уже вырезанным фоном, поэтому нарезчик их только обрезал по содержимому:
повторное вырезание съело бы тёмные части объектов.

## Шаблон записи

Копировать при добавлении нового ассета.

```markdown
## <код> — <название>

- **Файл:** `<путь>`, <формат>, <размер>, <вес>
- **Дата:** <дата>
- **Модель:** <модель>
- **Промпт:** 05-prompt-book.md §<номер>, <без изменений | с правками: ...>
- **Попыток до приёмки:** <n>
- **Результат:** <принят | принят с оговорками>

### Замечания
<что пришлось править в промпте, что модель делает неправильно раз за разом>
```
