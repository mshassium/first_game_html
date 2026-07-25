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
| C-02 | Карта I — Родник Изобилия | `cards/card_I.png` | — | — | — | Не начат |
| C-03 | Карта R — Реликварий | `cards/card_R.png` | — | — | — | Не начат |
| C-04 | Карта S — Тень Похитителя | `cards/card_S.png` | — | — | — | Не начат |
| C-05 | Карта T — Капкан Чародея | `cards/card_T.png` | — | — | — | Не начат |
| C-06 | Рубашка карты | `cards/card_back.png` | — | — | — | Не начат |
| U-01…U-17 | Панели и кнопки (лист) | `ui/sheet_panels.png` | — | — | — | Не начат |
| U-06, U-08, U-09 | Слот, урна, колода | `ui/sheet_board.png` | — | — | — | Не начат |
| U-18…U-33 | Иконки (лист 4×4) | `ui/sheet_icons.png` | — | — | — | Не начат |
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
