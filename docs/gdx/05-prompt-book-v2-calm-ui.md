# 05 — Промпт-бук v2: спокойный премиальный интерфейс

Этот файл заменяет прежний промпт-бук. Его задача — сохранить живописную фэнтезийную
атмосферу карт, но перестать переносить максимальную яркость, золото, свечение и глубокий
рельеф на каждый элемент интерфейса.

Главный принцип новой версии:

> **Карты и магические эффекты — актёры. Интерфейс — тёмная спокойная сцена.**

Каждый английский промпт ниже является цельным блоком. Его можно копировать целиком.
Русский текст вокруг блоков — инструкция и в запрос не входит.

---

## 0. Что изменилось относительно v1

В первой версии один и тот же набор формулировок повторялся почти в каждом запросе:
`rich warm lighting`, `heavy ornate carved wood`, `polished gold`, `deep bevels`,
`cool blue fill`, `glowing rune veins`, `deep shadows`. По отдельности эти признаки
выглядят эффектно, но вместе заставляют **каждый** ассет конкурировать за внимание.
В результате экран становится резким, пёстрым и визуально шумным.

В v2 интенсивность разделена по уровням:

| Уровень | Что относится | Контраст и насыщенность |
|---|---|---|
| 1 — фон | стол, стена, затемнение | очень низкие |
| 2 — UI | панели, кнопки, слоты, рамки | низкие или средние |
| 3 — контент | карты, портреты | средние или высокие |
| 4 — событие | выбранная карта, заклинание, победа | самые высокие, кратковременно |

### Правила, которые нельзя нарушать

1. **Не использовать старую карту F как единственный референс для UI.** Она остаётся
   референсом живописи и карточной конструкции, но не референсом яркости всего экрана.
2. **Для интерфейса главным референсом является концепт 16:9**, нарисованный после разбора
   текущего экрана. Сохраните его как:
   `assets_src/anchor/ui_direction_reference_16x9.png`.
3. **Текст не генерируется картинкой.** Все заголовки, числа, буквы карт, подсказки и
   подписи рисуются в LibGDX через шрифты. В ассетах остаются пустые поверхности.
4. **Цвет школы живёт только внутри карты, в маленьком самоцвете и во временном состоянии
   выбора.** Нейтральные панели, слоты, кнопки и рамки не светятся синим, зелёным или красным.
5. **Золото — не базовый цвет.** Основа металла — тёмная состаренная бронза. Светлое золото
   появляется только тонкой кромкой, на активной кнопке или маленькой иконке.
6. **Один экран — один яркий фокус.** На боевом экране это рука карт или активная карта.
   В меню — дальний освещённый алтарь. В модальном окне — заголовок или выбранный контрол.
7. **Фактура низкочастотная.** Никаких контрастных полос дерева, ряби камня и мелкого
   орнамента в центре панелей: там должен хорошо читаться текст.
8. **Рельеф неглубокий.** Тонкая рамка, мягкая внутренняя тень, один световой кант.
   Не делать рамку внутри рамки внутри рамки.

---

## 1. Референсы и порядок их приоритета

### 1.1 Файлы-референсы

| Файл | Роль |
|---|---|
| `assets_src/anchor/ui_direction_reference_16x9.png` | главный референс композиции, иерархии, темноты, толщины рамок и спокойствия UI |
| `assets_src/anchor/card_F_anchor.png` | старый референс сюжета карты F и живописной манеры |
| `assets_src/anchor/card_F_anchor_v2.png` | новый основной референс всех карт после шага 2.1 |
| `assets_src/anchor/ui_style_anchor.png` | новый основной референс всех панелей и контролов после шага 2.2 |

В запросах с двумя референсами всегда явно указано, какой из них главный.
Из концепта 16:9 нельзя копировать текст, русские надписи, цифры и готовую раскладку
пиксель-в-пиксель. Он задаёт только дизайн-систему.

### 1.2 Палитра-направление

Hex-коды нужны как ориентир, а не как требование абсолютной точности модели.

| Роль | Пример |
|---|---|
| фон почти чёрный | `#090D11` |
| тёмный сине-графитовый камень | `#171D22` |
| поверхность панели | `#22282B` |
| тёмный ненасыщенный дуб | `#2B211A` |
| патинированная бронза | `#5F452C` |
| приглушённый золотой кант | `#A9844F` |
| основной светлый текст в игре | `#D6C9AA` |
| вторичный текст | `#9F957F` |
| спокойный пергамент | `#B6A57E` |

Цвета школ должны быть темнее и спокойнее, чем в v1. Они могут становиться яркими только
в центре иллюстрации карты или в коротком VFX:

- azure: `#4A8FB8`;
- jade: `#5E9B69`;
- amber: `#B77A32`;
- crimson: `#A84B4E`;
- amethyst: `#76609B`.

### 1.3 Форматы

| Формат | Ассеты |
|---|---|
| 2:3, 1024×1536 | карты и рубашка |
| 1:1 | якорь стиля, иконки, портреты, рамка, эмблема |
| 4:3 | листы кнопок, контролов, кубиков и тематических VFX |
| 16:9 | боевой фон, меню, загрузка, проверочные мокапы |
| 9:16 | вертикальный фон стола |

### 1.4 Один чат — одна группа

- Шаг 2.1 выполняется в отдельном чате с двумя приложенными референсами.
- Шаг 2.2 — в новом чате с концептом и новой картой F v2.
- Все карты — в одном чате с `card_F_anchor_v2.png`.
- Все UI-ассеты — в другом чате с `ui_style_anchor.png` и концептом 16:9.
- Фоны — в отдельном чате с концептом 16:9.

---

## 2. Новые эталоны

### 2.1 Перерисовать карту F в спокойной версии

Приложить **два файла**:

1. `assets_src/anchor/card_F_anchor.png`;
2. `assets_src/anchor/ui_direction_reference_16x9.png`.

```text
Two reference images are attached. The first is the existing F card whose subject, overall proportions and hand-painted fantasy character must be preserved. The second is the approved 16:9 UI direction for the game and is the primary reference for restraint, colour hierarchy, material darkness and border thickness. Ignore every word, number and label visible in the second reference and do not reproduce any text from it.

Redesign the same F card as a calmer production-ready version for this interface. Preserve the central subject: a heavy wooden padlock tightly wrapped in pale frozen chains, hovering above a low stone pedestal inside an arched artwork window. Preserve the same card zones and the same premium hand-painted fantasy illustration style, but reduce the visual intensity of the frame.

The card is seen perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

Required redesign:
— make the outer wooden frame approximately 35% narrower than in the old card;
— use dark desaturated walnut-brown oak, not orange or honey-coloured wood;
— use small dark patinated-bronze corner fittings instead of large bright gold ornaments;
— use shallow bevels and soft ambient shadows, not deep embossed relief;
— remove the bright cyan line running around the whole inner frame;
— allow only a very thin low-intensity azure accent line around the artwork window and a small glow in the bottom gemstone;
— keep the strongest blue light inside the frozen-lock illustration only;
— reduce ice particles and bloom by about half;
— make the top cartouche, lower description plate and surrounding neutral surfaces charcoal grey stone with low-contrast texture;
— make the parchment ribbon narrower, darker, more desaturated and visually subordinate;
— keep large clean shapes that remain readable when the card is displayed small in a hand of five cards.

Layout from top to bottom:
— top 5%: narrow outer frame;
— 5% to 18%: EMPTY recessed top cartouche, dark charcoal stone, blank and smooth;
— 18% to 72%: arched artwork window with the frozen chained padlock;
— 72% to 79%: narrow EMPTY parchment ribbon, blank;
— 79% to 94%: EMPTY dark stone description plate, blank;
— bottom centre: one small smooth azure gemstone in a restrained bronze setting.

Lighting is soft and directional from the upper left, with very subtle cool ambient fill from below. The frame itself is mostly matte. Bright highlights occupy less than 8% of the card surface. No hard black outlines. No glow on neutral wood or bronze.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on every side. No text, no letters, no numbers, no glyphs, no captions, no logo, no watermark, no signature.

Avoid: bright orange wood, shiny yellow gold, thick cyan borders, glow around the entire card, excessive bloom, deep bevels, nested frames, high-contrast wood grain, noisy stone, micro-ornament, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/anchor/card_F_anchor_v2.png` и скопировать тот же файл в
`assets_src/cards/card_F.png` как игровую карту F.
После этого старый `card_F_anchor.png` больше не использовать как основной карточный эталон.

### 2.2 Создать эталон материалов UI

Приложить:

1. `assets_src/anchor/ui_direction_reference_16x9.png` — главный референс;
2. `assets_src/anchor/card_F_anchor_v2.png` — вторичный референс общей живописной манеры.

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for darkness, hierarchy, border thickness, spacing and restraint. The redesigned F card is a SECONDARY reference only for hand-painted material language. Ignore and do not reproduce any text, numbers or labels visible in either reference.

Create a production style anchor sheet for a calm premium fantasy card-game interface. The result must show eight separate empty UI material samples arranged in a strict two-by-four grid on a plain pure-white background. All samples share the same soft upper-left lighting, dark muted palette and restrained hand-painted brushwork. Nothing overlaps.

Reading order:
1. a wide charcoal stone panel with a very thin dark-bronze border and tiny corner caps;
2. a wide dark desaturated-oak panel with the same border language;
3. a warm muted parchment information panel held by four small dark-bronze clips;
4. a compact dark status plaque with a thin bronze rim;
5. a wide raised button made of charcoal stone and dark oak, with a subtle bronze edge;
6. the same button pressed, only slightly darker and lower, without a dramatic lighting reversal;
7. a minimal vertical card-slot outline: thin bronze corners, a faint inner shadow and no solid slab;
8. a narrow slider track with a small diamond-shaped bronze knob and one restrained blue enamel inset.

Design language:
— matte charcoal-blue stone, dark desaturated oak, dark patinated bronze and very limited muted gold;
— broad clean surfaces and low-frequency texture;
— shallow relief, thin borders, soft shadows;
— no bright gold, no orange wood, no blue rim light on neutral objects;
— no glowing runes, no magical aura, no dramatic bloom;
— the centre 70% of every panel and button must be quiet and nearly uniform for future text;
— ornaments may appear only at the corners and must occupy less than 10% of each element;
— all edges must remain readable at small UI sizes.

Every face is completely blank: no text, no icons, no symbols, no letters, no numbers and no decorative scribbles in the centre.

Plain flat pure-white background, equal spacing, no object touches the canvas edge, no shadow reaches the canvas edge. No mockup screen and no border around the whole sheet.

Avoid: bright polished gold, orange wood, thick frames, stacked borders, heavy embossing, high-contrast scratches, noisy stone, glowing blue outlines, saturated school colours, photorealism, 3D render, CGI, anime, pixel art, flat vector art, text of any kind.

Square image, aspect ratio 1:1, size 1536x1536.
```

Сохранить целиком как `assets_src/anchor/ui_style_anchor.png`.
Этот лист служит референсом. Необязательно использовать его фрагменты напрямую в игре.

### 2.3 Перерисовать карту F под медальон

Причина замены: в раскладке §2.1 под иллюстрацию отдано 54% высоты, а 27% занимают
картуш, лента и плашка описания. В игре они остаются пустыми — движок рисует только
букву, и та попадает на стык картуша и арки. Поэтому плашки убираются, иллюстрация
занимает всю карту, а буква получает собственное место: пустой бронзовый медальон
в левом верхнем углу.

**Координаты медальона и самоцвета заданы числами и продублированы в `CardActor`.**
Если менять их здесь, надо менять и там, иначе буква съедет с медальона.

Приложить **два файла**:

1. `assets_src/anchor/card_F_anchor_v2.png` — манера письма и материалы;
2. `assets_src/anchor/ui_direction_reference_16x9.png` — сдержанность и темнота.

```text
Two reference images are attached. The first is the current F card of this deck: keep its hand-painted fantasy manner, its dark walnut-brown oak, its dark patinated bronze, its restrained lighting and its subject exactly. The second is the approved 16:9 UI direction and governs overall darkness and restraint. Ignore every word, number and label visible in either reference and do not reproduce any text.

Redesign the same F card with a new layout that gives the illustration the whole card. The card is seen perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

Keep the subject: a heavy wooden padlock tightly wrapped in pale frozen chains, hovering above a low stone pedestal.

Required layout:
— a very thin dark desaturated walnut-brown oak frame around the card, no wider than 2.5% of the card width, about a third as wide as in the attached card, with tiny dark patinated-bronze corner fittings;
— the illustration fills the entire area inside that frame, edge to edge. There is NO top cartouche, NO parchment ribbon and NO description plate. Those bands are removed completely and the artwork takes their space;
— the frozen padlock is drawn correspondingly larger and fills the composition;
— top left: one small EMPTY circular medallion of dark patinated bronze with a thin muted-gold rim. Its centre sits at 16% of the card width and 13% of the card height, its outer diameter is 22% of the card width. Its face is plain dark charcoal and completely blank: no letter, no digit, no symbol, no engraving, no ornament. The game prints a letter into this medallion, so it must stay empty and evenly lit;
— bottom right: one small azure gemstone in a restrained bronze setting, its centre at 87% of the width and 92% of the height, its diameter about 9% of the card width;
— nothing else overlays the illustration.

Lighting is soft and directional from the upper left with a subtle cool fill from below. The strongest blue light stays inside the frozen lock. The frame and the medallion remain matte and quiet, and neither glows. Bright highlights occupy less than 8% of the card surface. No hard black outlines.

Keep large clean shapes: the card must stay readable when shown small in a hand of five.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on every side. No text, no letters, no numbers, no glyphs, no captions, no logo, no watermark, no signature.

Avoid: any lettering inside the medallion, bright orange wood, shiny yellow gold, thick frames, a cartouche or ribbon or description plate, glow around the whole card, excessive bloom, deep bevels, nested frames, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/anchor/card_F_anchor_v3.png` и скопировать тот же файл в
`assets_src/cards/card_F.png`. После этого эталоном всех карт становится v3.

---
## 3. Карты

Карты остаются самым насыщенным постоянным элементом экрана, но их яркость сосредоточена
в иллюстрации, а не в рамке. К каждому запросу этого раздела прикладывайте только
`assets_src/anchor/card_F_anchor_v2.png`.

### 3.1 Карта I — Родник Изобилия

```text
A reference image of the F card of this deck is attached. Match its exact outer proportions, its very thin dark-oak frame, its tiny patinated-bronze corner fittings, its empty circular bronze medallion in the top left corner, its small gemstone in the bottom right corner, its restrained lighting, its hand-painted brushwork and its level of detail. This new card must look as if it was printed in the same deck. Only the illustration, the local accent colour and the gemstone colour change.

Create one vertical fantasy spell card, perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

The frame is dark desaturated walnut-brown oak, not orange. Metal is dark patinated bronze with only tiny muted-gold highlights. The neutral frame remains matte and quiet. Bright colour is confined to the artwork window, one very thin accent line around that window and the small bottom gemstone. There is no glowing line around the whole card.

Layout:
— a very thin dark desaturated walnut-brown oak frame, no wider than 2.5% of the card width, with tiny dark patinated-bronze corner fittings;
— the illustration fills the entire area inside that frame, edge to edge. There is NO top cartouche, NO parchment ribbon and NO description plate;
— the illustration shows a carved stone basin overflowing with luminous emerald water, a young green sprout breaking through the cracked rim, the basin hovering above a low round stone pedestal, a few glowing leaves and droplets in the air, drawn large enough to fill the composition;
— top left: one small EMPTY circular medallion of dark patinated bronze with a thin muted-gold rim, its centre at 16% of the card width and 13% of the card height, its outer diameter 22% of the card width. Its face is plain dark charcoal and completely blank: no letter, no digit, no symbol, no engraving. The game prints a letter into it, so it must stay empty and evenly lit;
— bottom right: one small jade-green gemstone in a restrained bronze setting, its centre at 87% of the width and 92% of the height, its diameter about 9% of the card width;
— nothing else overlays the illustration.

The emerald magical light is strongest around the water and sprout. It fades before reaching the wooden frame. Reduce particle count and bloom; use large readable shapes. Accent colour direction: muted jade #5E9B69, becoming brighter only at the central magical source. The wood and bronze must not be tinted green.

Soft warm light from the upper left, subtle cool ambient fill from below. Bright highlights occupy less than 8% of the whole card. No hard black outlines and no high-contrast micro-detail.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on all sides. No text, no letters, no numbers, no glyphs, no words, no logo, no watermark, no signature.

Avoid: any lettering inside the medallion, a cartouche or ribbon or description plate, bright orange wood, shiny yellow gold, thick glowing borders, glow on neutral materials, excessive bloom, excessive particles, deep bevels, nested frames, noisy stone, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_I.png`.

### 3.2 Карта R — Реликварий

```text
A reference image of the F card of this deck is attached. Match its exact outer proportions, its very thin dark-oak frame, its tiny patinated-bronze corner fittings, its empty circular bronze medallion in the top left corner, its small gemstone in the bottom right corner, its restrained lighting, its hand-painted brushwork and its level of detail. This new card must look as if it was printed in the same deck. Only the illustration, the local accent colour and the gemstone colour change.

Create one vertical fantasy spell card, perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

The frame is dark desaturated walnut-brown oak, not orange. Metal is dark patinated bronze with only tiny muted-gold highlights. The neutral frame remains matte and quiet. Bright colour is confined to the artwork window, one very thin accent line around that window and the small bottom gemstone. There is no glowing line around the whole card.

Layout:
— a very thin dark desaturated walnut-brown oak frame, no wider than 2.5% of the card width, with tiny dark patinated-bronze corner fittings;
— the illustration fills the entire area inside that frame, edge to edge. There is NO top cartouche, NO parchment ribbon and NO description plate;
— the illustration shows an open bronze reliquary casket with its lid raised, hovering above a low round stone pedestal, warm amber light rising from inside, a restrained stream of embers and ash forming one phoenix-feather silhouette, drawn large enough to fill the composition;
— top left: one small EMPTY circular medallion of dark patinated bronze with a thin muted-gold rim, its centre at 16% of the card width and 13% of the card height, its outer diameter 22% of the card width. Its face is plain dark charcoal and completely blank: no letter, no digit, no symbol, no engraving. The game prints a letter into it, so it must stay empty and evenly lit;
— bottom right: one small amber gemstone in a restrained bronze setting, its centre at 87% of the width and 92% of the height, its diameter about 9% of the card width;
— nothing else overlays the illustration.

The amber magical light is clearly brighter than the brown wood but remains local to the casket and feather. It fades before reaching the frame. Use a muted amber direction #B77A32, becoming brighter only at the central source. Keep the frame dark enough that the magical subject remains readable. Reduce ember count and bloom; use large shapes.

Soft warm light from the upper left, subtle cool ambient fill from below. Bright highlights occupy less than 8% of the whole card. No hard black outlines and no high-contrast micro-detail.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on all sides. No text, no letters, no numbers, no glyphs, no words, no logo, no watermark, no signature.

Avoid: bright orange wood, shiny yellow gold, amber tint across the whole frame, thick glowing borders, excessive bloom, excessive sparks, deep bevels, nested frames, noisy stone, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_R.png`.

### 3.3 Карта S — Тень Похитителя

```text
A reference image of the F card of this deck is attached. Match its exact outer proportions, its very thin dark-oak frame, its tiny patinated-bronze corner fittings, its empty circular bronze medallion in the top left corner, its small gemstone in the bottom right corner, its restrained lighting, its hand-painted brushwork and its level of detail. This new card must look as if it was printed in the same deck. Only the illustration, the local accent colour and the gemstone colour change.

Create one vertical fantasy spell card, perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

The frame is dark desaturated walnut-brown oak, not orange. Metal is dark patinated bronze with only tiny muted-gold highlights. The neutral frame remains matte and quiet. Bright colour is confined to the artwork window, one very thin accent line around that window and the small bottom gemstone. There is no glowing line around the whole card.

Layout:
— a very thin dark desaturated walnut-brown oak frame, no wider than 2.5% of the card width, with tiny dark patinated-bronze corner fittings;
— the illustration fills the entire area inside that frame, edge to edge. There is NO top cartouche, NO parchment ribbon and NO description plate;
— the illustration shows a spectral claw-shaped hand made of restrained crimson smoke reaching from darkness and stealing a small golden amulet whose chain has just snapped, the amulet hovering above a low round stone pedestal, only a few broken links suspended in the air, drawn large enough to fill the composition;
— top left: one small EMPTY circular medallion of dark patinated bronze with a thin muted-gold rim, its centre at 16% of the card width and 13% of the card height, its outer diameter 22% of the card width. Its face is plain dark charcoal and completely blank: no letter, no digit, no symbol, no engraving. The game prints a letter into it, so it must stay empty and evenly lit;
— bottom right: one small crimson gemstone in a restrained bronze setting, its centre at 87% of the width and 92% of the height, its diameter about 9% of the card width;
— nothing else overlays the illustration.

The spectral hand is an abstract smoke silhouette, not a realistic anatomical hand. There is no blood and no gore. Crimson light is local to the hand and amulet and fades before reaching the frame. Accent direction: muted crimson #A84B4E, brighter only at the magical centre. The wood and bronze must not be tinted red. Reduce smoke volume, particles and bloom; preserve a clear silhouette.

Soft warm light from the upper left, subtle cool ambient fill from below. Bright highlights occupy less than 8% of the whole card. No hard black outlines and no high-contrast micro-detail.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on all sides. No text, no letters, no numbers, no glyphs, no words, no logo, no watermark, no signature.

Avoid: bright orange wood, shiny yellow gold, red tint across the frame, thick glowing borders, excessive smoke, excessive bloom, gore, blood, deep bevels, nested frames, noisy stone, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_S.png`.

### 3.4 Карта T — Капкан Чародея

```text
A reference image of the F card of this deck is attached. Match its exact outer proportions, its very thin dark-oak frame, its tiny patinated-bronze corner fittings, its empty circular bronze medallion in the top left corner, its small gemstone in the bottom right corner, its restrained lighting, its hand-painted brushwork and its level of detail. This new card must look as if it was printed in the same deck. Only the illustration, the local accent colour and the gemstone colour change.

Create one vertical fantasy spell card, perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

The frame is dark desaturated walnut-brown oak, not orange. Metal is dark patinated bronze with only tiny muted-gold highlights. The neutral frame remains matte and quiet. Bright colour is confined to the artwork window, one very thin accent line around that window and the small bottom gemstone. There is no glowing line around the whole card.

Layout:
— a very thin dark desaturated walnut-brown oak frame, no wider than 2.5% of the card width, with tiny dark patinated-bronze corner fittings;
— the illustration fills the entire area inside that frame, edge to edge. There is NO top cartouche, NO parchment ribbon and NO description plate;
— the illustration shows a circular rune-etched steel snare trap with open toothed jaws, tilted only enough for the circular shape to read, hovering above a low round stone pedestal, thin spectral threads stretched across the ring and restrained violet light between the teeth, drawn large enough to fill the composition;
— top left: one small EMPTY circular medallion of dark patinated bronze with a thin muted-gold rim, its centre at 16% of the card width and 13% of the card height, its outer diameter 22% of the card width. Its face is plain dark charcoal and completely blank: no letter, no digit, no symbol, no engraving. The game prints a letter into it, so it must stay empty and evenly lit;
— bottom right: one small amethyst gemstone in a restrained bronze setting, its centre at 87% of the width and 92% of the height, its diameter about 9% of the card width;
— nothing else overlays the illustration.

The trap runes are abstract invented glyphs and do not resemble real letters. Violet light stays local to the trap and fades before reaching the frame. Accent direction: muted amethyst #76609B, brighter only at the magical source. The wood and bronze must not be tinted violet. Use only a few sparks and a clear readable ring silhouette.

Soft warm light from the upper left, subtle cool ambient fill from below. Bright highlights occupy less than 8% of the whole card. No hard black outlines and no high-contrast micro-detail.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on all sides. No text, no letters, no numbers, no real runes, no words, no logo, no watermark, no signature.

Avoid: bright orange wood, shiny yellow gold, violet tint across the frame, thick glowing borders, excessive sparks, excessive bloom, deep bevels, nested frames, noisy stone, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_T.png`.

### 3.5 Рубашка карты

Приложить `assets_src/anchor/card_F_anchor_v2.png`.

```text
A reference image of the F card of this deck is attached. Create the BACK of that exact card. Match its outer proportions, its very thin dark-oak frame no wider than 2.5% of the card width, its tiny dark patinated-bronze corner fittings, its shallow relief, its quiet lighting, its hand-painted brushwork and its restrained colour grading exactly. The back carries no medallion and no gemstone in the corners.

The card back is seen perfectly flat and straight on, orthographic, with no perspective and no visible side thickness.

The outer frame is the same very thin dark desaturated walnut-brown oak, not orange. Bronze is dark and matte with only tiny muted-gold edge highlights. The entire inner field is dark navy-black tooled leather with very low-contrast embossed ornament. The ornament must remain broad and quiet rather than intricate.

In the exact centre is a compact heraldic seal: one dark patinated-bronze ring with a thin muted-gold inner edge and a simple abstract interlaced arcane sigil. Five very small gemstone cabochons sit around the seal: azure, jade, amber, crimson and amethyst. Their glow is minimal and local, illuminating only a few millimetres of surrounding metal and leather. The gems must not become five competing light sources.

The composition is symmetrical, calm and low contrast. It must read clearly as a card back at small size and reveal nothing about card identity. Bright highlights occupy less than 5% of the surface.

Isolated object centred on a plain flat pure-white background. Leave 5% empty margin on all sides. No text, no letters, no numbers, no glyphs that resemble writing, no captions, no logo, no watermark, no signature.

Avoid: bright orange wood, shiny yellow gold, large glowing gems, strong coloured halos, bright blue leather, dense filigree, deep embossing, high-contrast scratches, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_back.png`.

---
## 4. Интерфейс

Если в конкретном пункте не сказано иначе, прикладывайте два файла:

1. `assets_src/anchor/ui_direction_reference_16x9.png` — главный референс;
2. `assets_src/anchor/ui_style_anchor.png` — референс материалов.

Во всех запросах ниже текст, цифры и иконки из концепта нужно игнорировать. Генерируются
только пустые поверхности. Центральные области панелей должны быть пригодны для NinePatch.

### 4.1 Основная тёмная каменная панель

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for restraint, darkness, border thickness and hierarchy. The UI material anchor is the reference for hand-painted stone and bronze. Ignore and do not reproduce any text, numbers, icons or labels visible in the references.

Create one empty wide horizontal user-interface panel for the same game, seen perfectly flat and straight on with no perspective. It will be used as a scalable NinePatch panel behind rules, settings, status information and modal content.

Design:
— a matte charcoal blue-black stone face with broad low-frequency texture;
— a single very thin border of dark patinated bronze;
— four small restrained corner caps, each occupying no more than 7% of the panel width and height;
— shallow bevels and a soft inner shadow;
— one narrow warm highlight on the upper-left metal edge and no bright highlight elsewhere;
— the central 75% of the panel is nearly uniform, quiet and free from scratches, seams, runes or ornament;
— border thickness is no more than 4% of the panel height;
— no wood layer, no frame inside a frame, no coloured glow.

The panel is dark enough to support warm ivory text, but its interior is not pure black. It has subtle depth and natural hand-painted material variation without visible noise.

The panel face is completely blank. No text, no icons, no symbols, no runes, no numbers, no writing-like marks.

Isolated object centred on a plain flat pure-white background. Leave 8% empty margin on all sides. No shadow reaches the canvas edges. No full-screen mockup and no border around the whole image.

Avoid: bright gold, orange wood, cyan rim light, glowing runes, deep relief, thick corners, multiple nested borders, high-contrast scratches, noisy marbling, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Wide landscape image, aspect ratio 16:9, size 1536x864.
```

Сохранить как `assets_src/ui/panel_stone.png`.

### 4.2 Тёмная деревянная панель

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for restraint, darkness, border thickness and hierarchy. The UI material anchor is the reference for hand-painted oak and bronze. Ignore and do not reproduce any text, numbers, icons or labels visible in the references.

Create one empty wide horizontal user-interface panel for the same game, seen perfectly flat and straight on with no perspective. It will be used as a scalable NinePatch panel for the hand area and selected secondary surfaces.

Design:
— dark desaturated walnut-brown oak, almost charcoal in the shadows, never orange or honey-coloured;
— broad vertical wood grain with very low contrast and no bright streaks;
— a single thin dark patinated-bronze border;
— four small restrained corner caps occupying no more than 7% of the panel;
— shallow bevels, soft inner shadow and one subtle upper-left highlight;
— the central 75% is quiet, nearly uniform and free from knots, cracks, ornaments or metal studs;
— border thickness no more than 4% of panel height;
— no coloured glow and no polished yellow gold.

The wooden panel must be darker and less visually active than the cards placed over it. Cards remain the focal objects.

The face is completely blank. No text, no icons, no symbols, no runes, no numbers, no writing-like marks.

Isolated object centred on a plain flat pure-white background. Leave 8% empty margin on all sides. No shadow reaches the canvas edges. No full-screen mockup and no border around the whole image.

Avoid: orange wood, high-contrast grain, shiny gold, cyan rim light, glowing runes, deep relief, thick corners, nested frames, scratches in the centre, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Wide landscape image, aspect ratio 16:9, size 1536x864.
```

Сохранить как `assets_src/ui/panel_wood.png`.

### 4.3 Информационная панель из пергамента

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for restraint, hierarchy and readable information panels. The UI material anchor is the reference for hand-painted parchment and bronze. Ignore and do not reproduce any text, numbers, icons or labels visible in the references.

Create one empty vertical information panel made of muted aged parchment, seen perfectly flat and straight on with no perspective. It will hold a battle log, objective or short help text.

Design:
— warm desaturated parchment in a subdued beige-grey tone, not bright yellow or cream;
— subtle fibre variation and gentle edge darkening, with no strong stains behind future text;
— four very small dark-bronze corner clips and a thin dark-bronze outer line;
— shallow physical depth and a soft ambient shadow;
— the central 80% is smooth, quiet and evenly lit for readable dark text;
— a slightly darker header zone may be suggested only through a subtle tonal shift, without a separate heavy banner;
— no torn scroll curls, no large ornaments and no glowing edges.

The panel is completely blank. No text, no icons, no symbols, no lines imitating handwriting, no numbers and no decorative glyphs.

Isolated object centred on a plain flat pure-white background. Leave 8% empty margin on all sides. No shadow reaches the canvas edges.

Avoid: bright yellow parchment, burned black edges, heavy stains, high-contrast fibres, thick golden frame, scroll rolls, curled banners, glowing runes, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Portrait image, aspect ratio 3:4, size 1152x1536.
```

Сохранить как `assets_src/ui/panel_parchment.png`.

### 4.4 Кнопки — шесть состояний

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for button proportions, restraint and readable hierarchy. The UI material anchor is the reference for hand-painted dark oak, charcoal stone and bronze. Ignore and do not reproduce any text, numbers or icons visible in the references.

Create a strict sheet of six separate wide horizontal fantasy UI buttons arranged in two rows of three on a plain pure-white background. All six buttons have exactly the same size, proportions, light direction and alignment. They are seen perfectly flat and straight on with no perspective. Nothing overlaps.

Shared construction:
— a very dark charcoal-stone or desaturated-oak face;
— one thin dark patinated-bronze rim;
— tiny corner details only;
— shallow relief and low-contrast hand-painted texture;
— the central 75% is completely empty and nearly uniform for text;
— no icon socket and no generated writing.

Reading order:
1. PRIMARY UP: slightly lighter face, subtle warm upper-left rim and one restrained muted-gold accent line;
2. PRIMARY HOVER: only about 12% brighter than up, with a soft local warm highlight, no glow halo;
3. PRIMARY DOWN: slightly darker and visually lowered by a small inner shadow, not by reversing all light;
4. SECONDARY UP: darker and quieter than primary, bronze rim without gold accent;
5. SECONDARY DOWN: slightly darker with a small inner shadow;
6. DISABLED: desaturated charcoal-grey, low contrast, no bright rim, still readable as the same object.

No state may use blue, green, red or violet school colour. The difference between states must be subtle enough that the interface remains calm, but clear enough for interaction.

Every button face is completely blank. No text, no letters, no numbers, no icons, no symbols and no decorative scribbles.

Plain flat pure-white background, equal spacing, no button touches another or the canvas edges, no shadow reaches the edges. No labels under the buttons and no border around the sheet.

Avoid: bright yellow gold, orange wood, dramatic glow, neon hover, deeply pressed button, hard black outline, thick bevel, large corner ornaments, inconsistent sizes, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Landscape image, aspect ratio 4:3, size 1536x1152.
```

Нарезать в порядке чтения:

- `btn_primary_up`;
- `btn_primary_hover`;
- `btn_primary_down`;
- `btn_secondary_up`;
- `btn_secondary_down`;
- `btn_primary_disabled`.

### 4.5 Контролы настроек

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the settings screen and control hierarchy. The UI material anchor is the reference for dark bronze, charcoal stone and restrained blue enamel. Ignore and do not reproduce any text, numbers or labels visible in the references.

Create a strict sheet of nine separate empty fantasy UI controls arranged in a three-by-three grid on a plain pure-white background. Every object is seen perfectly flat and straight on with no perspective. Objects do not overlap and each remains centred in its cell.

Reading order:
1. a wide closed dropdown field: charcoal face, thin bronze rim, empty centre and a tiny downward triangle at the far right;
2. the same dropdown in an open or focused state, only slightly brighter along the rim, without a glow halo;
3. a long narrow slider track, dark bronze with a subtle recessed centre and no knob;
4. a small diamond-shaped slider knob, dark bronze with one restrained deep-blue enamel inset;
5. a round icon button in the raised state, empty centre, dark bronze rim and charcoal face;
6. the same round button pressed, slightly darker and lower;
7. a small empty checkbox, square, dark bronze edge and charcoal centre;
8. the same checkbox checked with a simple muted-gold tick mark;
9. a thin ornamental divider line with one small diamond motif in the centre.

Design language:
— dark matte materials, shallow relief and thin borders;
— muted gold only on the tick and very small highlights;
— blue enamel appears only inside the slider knob and is not luminous;
— no school-colour glow, no runes and no decorative clutter;
— every empty field is smooth enough for text or icons rendered by the game.

No generated text, letters, numbers, words, captions or labels anywhere.

Plain flat pure-white background, equal cell spacing, no shadows reaching the canvas edges, no grid lines and no frame around the whole sheet.

Avoid: shiny polished gold, orange wood, neon blue, glow halos, thick borders, deep bevels, inconsistent materials, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Landscape image, aspect ratio 4:3, size 1536x1152.
```

Нарезать в порядке чтения:

- `dropdown_closed`;
- `dropdown_focus`;
- `slider_track`;
- `slider_knob`;
- `btn_round_up`;
- `btn_round_down`;
- `checkbox_empty`;
- `checkbox_checked`;
- `divider_ornament`.

### 4.6 Слот под карту

```text
Two reference images are attached. The approved 16:9 battle-screen direction is the PRIMARY reference for the subtle card-slot outlines inside the shared play field. The UI material anchor is the reference for restrained bronze and stone. Ignore and do not reproduce any text, numbers or labels visible in the references.

Create one minimal empty vertical card slot for a dark fantasy game board, seen perfectly flat and straight on with no perspective.

This is NOT a separate stone slab and NOT a miniature framed panel. It is only a quiet recessed slot outline intended to sit directly on a larger shared charcoal-stone play surface.

Construction:
— four thin dark-bronze corner brackets connected by extremely subtle dark lines;
— a faint soft inner shadow suggesting a shallow recess;
— an almost transparent charcoal centre with no independent texture;
— border thickness no more than 2% of slot width;
— corner ornaments occupy no more than 6% of the slot area;
— no bright upper-left gold edge, only a restrained warm bronze highlight;
— no coloured glow and no solid background slab.

The slot must remain readable underneath a card but nearly disappear when empty, exactly like the quiet slots in the approved battle-screen direction.

Isolated object centred on a plain flat pure-white background. Leave 10% empty margin on every side. No text, no letters, no numbers, no icons, no symbols, no watermark or signature.

Avoid: thick frame, full granite plaque, bright gold, cyan outline, glowing runes, deep recess, dramatic shadow, ornate corners, high-contrast texture, perspective, tilt, photorealism, CGI, 3D render, anime, pixel art, vector art.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/ui/slot_card.png`.

### 4.7 Урна сброса

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for scale and restraint. The UI material anchor is the reference for warm charcoal stone and dark bronze. Ignore and do not reproduce any text, numbers or labels visible in the references.

Create one small neutral discard urn for the same game board. It must be a secondary utility object, not a focal prop.

Subject: a low squat carved-stone urn with a wide open oval mouth, viewed only slightly from above. The body is simple and compact, made of warm charcoal-grey stone. One narrow dark patinated-bronze band wraps around the upper body and one tiny bronze clasp sits at the front. The interior falls into soft shadow.

Design restraint:
— broad simple silhouette readable at 48 to 96 pixels high;
— low-contrast stone texture;
— no bright gold, no gemstones, no runes, no magical glow;
— no tall handles, lid, smoke, cards or decorative sculpture;
— soft upper-left light and a small contact shadow only.

Isolated object centred on a plain flat pure-white background. Leave 12% empty margin on all sides. No text, no letters, no numbers, no symbols, no watermark or signature.

Avoid: large heroic prop, bright metal, blue cast, coloured glow, intricate carving, skulls, excessive chips, strong perspective, photorealism, CGI, 3D render, anime, pixel art, vector art.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/discard_urn.png`.

### 4.8 Стопка колоды

Приложить **три файла**:

1. `assets_src/anchor/ui_direction_reference_16x9.png`;
2. `assets_src/anchor/ui_style_anchor.png`;
3. `assets_src/cards/card_back.png`.

```text
Three reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for scale, darkness and restraint. The UI material anchor is the reference for lighting. The third image is the exact card back that must appear on the stack. Ignore and do not reproduce any text or labels from the references.

Create a compact neat stack of about ten identical face-down cards. The top card must use the third reference exactly: same dark navy leather, same restrained central seal, same narrow dark-oak frame and same proportions. Do not invent another back.

View the stack from only slightly above, tilted no more than 8 degrees so it still reads as a flat game-board object. Show thin warm-brown worn card edges underneath the top card, with a very faint muted-gold edge line. Keep the stack compact and low.

The object must be darker and quieter than the face-up cards in the player's hand. There is no glow, no floating card, no particles, no blue light cast on the table and no dramatic drop shadow. Use soft upper-left light and one small contact shadow.

Isolated object centred on a plain flat pure-white background. Leave 10% empty margin on all sides. No text, no letters, no numbers, no logo, no watermark or signature.

Avoid: a different card back, strong perspective, tall stack, bright gilded edges, glowing gems, blue aura, orange wood, large shadow, photorealism, CGI, 3D render, anime, pixel art, vector art.

Landscape image, aspect ratio 4:3, size 1536x1152.
```

Сохранить как `assets_src/ui/deck_stack.png`.

### 4.9 Иконки — спокойный лист 4×4

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the small restrained bronze icons used in the top bar and menu. The UI material anchor is the reference for hand-painted bronze and charcoal surfaces. Ignore and do not reproduce any text, numbers or labels visible in the references.

Create a strict grid of sixteen interface icons, four rows by four columns, on a plain pure-white background. Every icon is the same size, seen perfectly flat and straight on, evenly spaced and centred in its cell.

Each icon is a compact round medallion with:
— a dark charcoal face;
— a thin dark patinated-bronze rim;
— one simple pictogram in shallow muted-bronze relief;
— one tiny warm upper-left highlight;
— no bright gold disc, no gemstone, no glow and no deep embossed texture.

The pictogram must occupy about 55% of the medallion diameter and remain readable at 24 to 48 pixels. Use broad clean strokes and negative space, not detailed illustrations.

Symbols in reading order:
row 1 — cogwheel; horn with three sound waves; horn with a diagonal slash; small harp;
row 2 — harp with a diagonal slash; open book; three thick horizontal menu bars; two straight bars crossing as an X close symbol;
row 3 — circular arrow; arrow pointing left; hourglass; neat stack of cards seen from the side;
row 4 — fan of three cards; lightning bolt; two crossed pennant flags; simple teardrop-shaped arcane sigil.

The menu bars and X are plain geometric UI symbols. Do not replace them with books, swords, scrolls or other objects. There must be no alphabet letters, digits, real runes or inscriptions anywhere.

Plain pure-white background, equal spacing, no icon touches another or the canvas edge, no shadows reach the edge, no grid lines, no labels and no frame around the sheet.

Avoid: bright shiny gold, orange bronze, thick rims, complex miniature scenes, coloured glow, gemstones, inconsistent sizes, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Square image, aspect ratio 1:1, size 1536x1536.
```

Нарезать в прежнем порядке. Если абстрактные menu/close снова заменяются предметами,
использовать исправляющую фразу из раздела 9.

### 4.10 Кубики

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for dark restrained tabletop objects. The UI material anchor is the reference for charcoal stone and patinated bronze. Ignore and do not reproduce any text or labels visible in the references.

Create six identical fantasy dice arranged in two rows of three on a plain pure-white background. All dice have exactly the same size, angle and lighting. Each is seen from a mild elevated three-quarter angle, and the six differ only in the number of pips on the top face.

Material:
— warm charcoal-grey carved stone, not blue-grey;
— very small dark-bronze corner caps;
— shallow round recessed pips filled with muted warm ivory or dim amber enamel, not bright glowing light;
— broad clean faces with low-contrast texture and softly worn edges.

Reading order: top values one, two, three, four, five and six using standard pip layouts. Side faces are plain and unmarked. There are no written numerals.

The dice must remain secondary board objects: no bloom, no aura, no sparks, no bright gold and no dramatic shadows.

Plain pure-white background, equal spacing, no die touches another or the canvas edge, no labels or frame around the sheet.

Avoid: cool blue stone, glowing pips, bright gold, different angles, different sizes, excessive chips, photorealism, CGI, 3D render, anime, pixel art, vector art, letters or numerals.

Landscape image, aspect ratio 4:3, size 1536x1152.
```

Нарезать на `die_1` … `die_6`.

### 4.11 Портрет игрока

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for portrait scale, darkness and restrained contrast. The UI material anchor is the reference for painterly lighting. Ignore and do not reproduce any text or labels from the references.

Create a chest-up portrait of a young human sorcerer for a small circular UI portrait. He wears deep muted-blue robes with dark bronze clasps and a high collar. He has short dark hair and a calm confident friendly expression. Three-quarter view, body turned slightly to the viewer's right, face looking toward the viewer.

Lighting is soft warm light from the upper left with a subtle cool reflection on the opposite shoulder. The eyes are natural and readable; at most there is a tiny blue catchlight, not glowing eyes. A thin blue embroidered line may appear on the collar but it is not luminous.

Background is a simple near-black neutral radial falloff with no scenery, particles, runes or props. Keep the face as the clear focal point and preserve readable values at a final crop of 96 to 160 pixels.

Compose for circular cropping: head centred in the upper half, shoulders fully inside the safe area, 10% empty margin. No frame is drawn around the portrait.

No text, letters, numbers, captions, logo, watermark or signature.

Avoid: bright blue aura, glowing eyes, saturated robes, hard rim light, busy background, weapon, spell effect, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/portrait_player.png`.

### 4.12 Портрет оппонента

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for portrait scale, darkness and restrained contrast. The UI material anchor is the reference for painterly lighting. Ignore and do not reproduce any text or labels from the references.

Create a chest-up portrait of an old human archmage for a small circular UI portrait. He wears deep muted-crimson robes with dark tarnished-gold trim and a hood that leaves the face visible. He has a long grey beard, narrowed intelligent eyes and a controlled knowing half-smile. Three-quarter view, body turned slightly to the viewer's left so he faces opposite the player portrait, face looking toward the viewer.

Lighting is soft warm light from the upper right with a subtle cool reflection on the opposite shoulder. The eyes are natural; at most there is a tiny crimson catchlight, not glowing eyes. He is imposing but not monstrous.

Background is a simple near-black neutral radial falloff with no scenery, particles, runes or props. Keep the face as the focal point and preserve readable values at a final crop of 96 to 160 pixels.

Compose for circular cropping: head centred in the upper half, shoulders fully inside the safe area, 10% empty margin. No frame is drawn around the portrait.

No text, letters, numbers, captions, logo, watermark or signature.

Avoid: bright red aura, glowing eyes, saturated robes, hard rim light, skulls, weapon, busy background, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/portrait_ai.png`.

### 4.13 Рама портрета

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the thin circular portrait frames on the left side of the battle screen. The UI material anchor is the reference for dark bronze. Ignore and do not reproduce any text or labels from the references.

Create one empty circular portrait frame, seen perfectly flat and straight on.

Design:
— a narrow ring of dark patinated bronze with a very thin muted-gold inner edge;
— shallow relief and one soft upper-left highlight;
— four tiny restrained junction details at the cardinal points, not large studs;
— one small EMPTY gemstone socket centred at the bottom, with no gem and no coloured glow, so the game can insert a player-colour indicator later;
— ring thickness approximately 12% of the outer diameter;
— centre completely empty and transparent after background removal.

The frame must remain elegant and readable at 100 to 180 pixels, without dominating the portrait.

Isolated on a plain pure-white background. Leave 8% empty margin. No text, letters, numbers, icons, logo, watermark or signature. Nothing may be drawn inside the ring.

Avoid: thick ornate ring, bright yellow gold, gemstones, glow, spikes, large corner ornaments, deep bevel, photorealism, CGI, 3D render, anime, pixel art, vector art.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/frame_portrait.png`.

### 4.14 Эмблема Ордена

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the small restrained order emblem used in the main menu. The UI material anchor is the reference for dark bronze and muted gold. Ignore and do not reproduce any text or labels from the references.

Create one compact heraldic seal for a fantasy wizard order, seen perfectly flat and straight on. The silhouette is a pointed shield with very restrained side scrollwork. The face is dark patinated bronze with a thin muted-gold border. In the centre is one broad readable abstract interlaced arcane sigil.

Five very small gemstone cabochons sit around the sigil: azure at top, jade upper left, amber upper right, crimson lower right and amethyst lower left. Their light is minimal and local; the emblem must still read primarily as dark bronze, not as a cluster of five glowing lamps.

At the bottom is one small blank plaque with no writing. The whole emblem is symmetrical left to right. Use large simple shapes that remain recognisable at 96 to 160 pixels.

Isolated on a plain pure-white background with 10% empty margin. No text, letters, numbers, real runes, captions, logo, watermark or signature.

Avoid: bright polished gold, large gems, strong coloured halos, dense filigree, excessive scrollwork, bright blue cast, photorealism, CGI, 3D render, anime, pixel art, vector art, text of any kind.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/emblem_first.png`.

### 4.15 Рамка модального окна

Этого ассета не было в первой редакции v2, но игра его использует: `Theme.kt` берёт
`modal_frame` как NinePatch со стретч-зонами 0.22 и 0.30. Требования к модальному окну
заданы в §10.4 — одна внешняя рамка без второй рамки внутри.

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the modal dialogs shown in it: their darkness, thin borders and restraint. The UI material anchor is the reference for hand-painted charcoal stone and dark bronze. Ignore and do not reproduce any text, numbers, icons or labels visible in the references.

Create one empty modal window frame for the same game, seen perfectly flat and straight on with no perspective. It will be stretched as a NinePatch, so the whole central area must be uniform and free of detail.

Design:
— a single outer border of dark patinated bronze over a narrow dark desaturated-oak edge;
— four small restrained corner fittings, each no larger than 9% of the window width;
— one thin muted-gold inner line following the border, no thicker than one pixel at final size;
— shallow relief, a soft inner shadow along the top edge and one subtle upper-left highlight;
— the interior is matte charcoal blue-black stone, quiet and nearly uniform across the central 80%;
— border thickness no more than 6% of the window height;
— NO second frame inside the first, no header banner, no divider bars, no ornament in the middle of any edge.

The middle of all four edges must be plain and repeatable so the frame can stretch in both directions without visible seams or repeated ornament.

The window face is completely blank. No text, no title plate, no icons, no symbols, no runes, no numbers and no close button.

Isolated object centred on a plain flat pure-white background. Leave 8% empty margin on all sides. No shadow reaches the canvas edges.

Avoid: bright gold, orange wood, nested frames, heavy header cartouche, thick corners, deep bevels, glowing edges, scratches in the centre, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Landscape image, aspect ratio 4:3, size 1024x768.
```

Сохранить как `assets_src/ui/modal_frame.png`.

### 4.16 Иконка смены языка

В листе §4.9 шестнадцать символов, и языка среди них нет, а меню его использует.
Генерируется отдельной иконкой в той же медальонной конструкции.

```text
Two reference images are attached. The approved 16:9 UI direction is the PRIMARY reference for the small restrained bronze icons in the menu. The UI material anchor is the reference for hand-painted bronze and charcoal. Ignore and do not reproduce any text, numbers or labels visible in the references.

Create one single interface icon, seen perfectly flat and straight on. It is a compact round medallion with a dark charcoal face, a thin dark patinated-bronze rim, one tiny warm upper-left highlight and no gemstone.

The pictogram is a simple globe in shallow muted-bronze relief: one circle crossed by one vertical meridian and two gently curved horizontal parallels. Broad clean strokes and generous negative space. The pictogram occupies about 55% of the medallion diameter and must stay readable at 24 pixels.

The globe is a plain geometric symbol. Do not add continents, do not add a stand or axis, and do not replace it with a scroll, book, flag or speech bubble.

No letters, no digits, no writing of any kind anywhere on the icon.

Isolated object centred on a plain flat pure-white background. Leave 12% empty margin on all sides. No shadow reaches the canvas edges.

Avoid: bright shiny gold, orange bronze, thick rim, detailed map, coloured glow, gemstones, deep embossing, photorealism, CGI, 3D render, anime, pixel art, flat vector art, text of any kind.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/icon_lang.png`.

---
## 5. Фоны

Для всех фонов прикладывайте `assets_src/anchor/ui_direction_reference_16x9.png`.
При желании вторым референсом можно приложить `ui_style_anchor.png`, но концепт 16:9
всегда имеет более высокий приоритет. Текст из концепта не копируется.

### 5.1 Боевой фон, горизонтальный

```text
The approved 16:9 UI direction for the game is attached. Use it as the primary reference for darkness, negative space, material restraint and the distribution of visual interest. Ignore and do not reproduce any text, numbers, cards, panels, portraits or labels visible in the reference.

Create a full-bleed widescreen background for a fantasy card-game battle screen. It is the quiet layer underneath all gameplay panels, slots, cards and text.

Subject: a broad ancient duelling table and surrounding darkness, viewed from a mild elevated angle that reads almost orthographically. The central 75% is matte charcoal blue-black stone with broad low-contrast slabs, a few subtle cracks and almost no small detail. A dark desaturated-oak edge may appear near the outer perimeter with sparse dark-bronze fittings.

Lighting and hierarchy:
— overall exposure is low and calm;
— soft warm candlelight enters only from the extreme far-left edge and a very small amount from the far-right edge;
— cool ambient light is faint and never creates bright blue surfaces;
— no glowing rune network across the centre;
— at most two or three tiny dim blue fissures may appear near the extreme outer edges, never behind cards or text;
— the middle play area remains evenly dark and uncluttered;
— very gentle natural vignette, without crushing the centre to black.

Composition:
— all candles, stands, carved objects and architectural detail stay inside the outer 12% of the frame;
— the centre contains no props, cards, dice, urns, books, emblems or built-in UI frames;
— top and bottom bands are especially quiet because status bars and the hand panel will cover them;
— no bright brass lines crossing the table.

Painterly premium fantasy illustration with soft visible brushwork and low-frequency texture. Materials feel physical but not photorealistic. No hard black outlines and no dramatic bloom.

No text, letters, numbers, symbols, logo, watermark, signature, characters or creatures.

Avoid: bright blue granite, glowing rune veins in the centre, dozens of candles, orange overall tint, shiny gold inlays, high-contrast cracks, busy tabletop, central altar object, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Full-bleed widescreen landscape, aspect ratio 16:9, size 1920x1080.
```

Сохранить как `assets_src/bg/bg_table_landscape.jpg`.

### 5.2 Боевой фон, вертикальный

```text
The approved 16:9 UI direction for the game is attached. Use it as the primary reference for darkness, negative space, material restraint and warm-versus-cool balance. Ignore and do not reproduce any text, numbers, cards, panels, portraits or labels visible in the reference.

Create a full-bleed vertical background for the portrait layout of the same fantasy card-game battle screen.

Subject: the same ancient duelling table recomposed for a tall frame. A matte charcoal blue-black stone surface runs from top to bottom. It has broad low-contrast slabs, a few subtle cracks and almost no small detail. A dark desaturated-oak edge and sparse dark-bronze fittings may appear only near the outer perimeter.

Lighting and hierarchy:
— low calm exposure;
— very restrained warm candlelight only near the extreme top and bottom edges;
— faint cool ambient fill without turning the stone blue;
— no glowing rune network;
— at most two tiny dim blue fissures near outer edges;
— the central vertical 70% is evenly dark, open and uncluttered for cards and panels.

All props and architectural detail remain within the outer 12% of the frame. No cards, dice, urns, books, emblems or built-in UI frames. No bright brass lines crossing the play area.

Painterly premium fantasy illustration with soft brushwork and low-frequency texture. No text, letters, numbers, logo, watermark, signature, characters or creatures.

Avoid: bright blue stone, glowing central runes, many candles, orange overall tint, shiny gold, busy centre, strong perspective, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Full-bleed tall portrait, aspect ratio 9:16, size 1080x1920.
```

Сохранить как `assets_src/bg/bg_table_portrait.jpg`.

### 5.3 Фон главного меню

```text
The approved 16:9 UI direction for the game is attached. Match its menu composition, darkness, restrained colour hierarchy and painterly fantasy atmosphere. Ignore and do not reproduce any text, buttons, logo, icons or labels visible in the reference.

Create a full-bleed main-menu background for the same game.

Subject: the interior of an ancient underground arcane sanctum. Massive dark stone arches recede toward a distant small altar in the right half of the image. The altar is illuminated by one soft warm golden light. A limited number of candles on simple iron stands create small warm pools along the path. The floor is dark charcoal stone with a few faint cool-blue mineral fissures, subtle enough not to compete with the menu.

Composition is essential:
— the left 38% is very dark, simple and low-detail for a game title and four menu buttons;
— no bright candle, crack, column edge or ornament sits behind the future left-side text;
— the brightest point is the distant altar around 70% of the image width and 45% of the height;
— the right half contains the architectural depth;
— the eye travels from the dark menu area toward the warm altar;
— no second competing bright focal point;
— natural vignette and soft atmospheric depth, not foggy bloom.

Palette: near-black blue-charcoal stone, dark bronze, tiny warm candle accents and one distant muted-gold focal light. The image remains mostly dark and desaturated.

Painterly premium fantasy illustration with visible but soft brushwork. No characters, creatures, weapons, cards, UI frames or built-in buttons.

No text, letters, numbers, logo, watermark or signature.

Avoid: bright left side, dozens of candles, uniformly lit cathedral, strong cyan runes everywhere, shiny gold architecture, orange overall colour cast, clutter behind menu area, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Full-bleed widescreen landscape, aspect ratio 16:9, size 1920x1080.
```

Сохранить как `assets_src/bg/bg_menu.jpg`.

### 5.4 Фон загрузки

```text
The approved 16:9 UI direction for the game is attached. Use it as the reference for darkness, restraint and the same blue-charcoal stone atmosphere. Ignore and do not reproduce any text, buttons, logo, icons or labels visible in the reference.

Create a very quiet full-bleed loading-screen background for the same fantasy game.

Subject: a dark charcoal stone wall seen straight on, with broad low-contrast blocks and one or two extremely faint cool mineral fissures near the far edge. One small distant candle flame appears at the extreme right side and provides a tiny warm accent. Most of the frame is undisturbed darkness.

Composition:
— central 70% is smooth, quiet and low-detail for a logo and progress bar;
— no architecture, altar, props, characters or ornament;
— no bright centre;
— very soft natural vignette;
— no visible repeated texture pattern.

Painterly hand-made texture, matte materials and minimal bloom only on the tiny candle flame.

No text, letters, numbers, logo, watermark or signature.

Avoid: busy wall, strong blue glow, rune network, many candles, bright golden light, high-contrast cracks, photorealism, CGI, 3D render, anime, pixel art, vector art.

Full-bleed widescreen landscape, aspect ratio 16:9, size 1920x1080.
```

Сохранить как `assets_src/bg/bg_loading.jpg`.

---

## 6. Визуальные эффекты

VFX остаются яркими только в момент действия. Их ореолы должны быть компактнее, чем в v1,
чтобы эффект не превращал половину экрана в цветное пятно. Генерировать на чистом чёрном
фоне для аддитивного блендинга.

### 6.1 Нейтральные VFX

```text
Create a strict sheet of eight separate neutral magical visual-effect elements on a perfectly uniform pure-black background, arranged in two rows of four, evenly spaced. Every element is isolated and nothing overlaps.

Reading order:
1. a compact soft radial glow, white centre fading smoothly to nothing, with the visible halo occupying no more than 70% of its cell;
2. a clean symmetrical star burst with four long rays and four short rays, narrow and crisp at the centre;
3. a thin circular ring of abstract invented arcane glyphs seen flat from above, with large gaps and no real letters;
4. a thin expanding shockwave ring, dark centre and one bright narrow outer edge;
5. one small bright spark with a compact halo;
6. one soft ash mote with blurred edges;
7. one restrained translucent smoke puff, wispy and low opacity;
8. one narrow vertical beam, brightest near the upper end and fading smoothly downward.

Use white and very light grey only. Keep bloom compact and preserve large areas of pure black between effects. These elements will be tinted by the game engine.

No text, letters, numbers, captions, labels, logo, watermark, signature, frame or grid lines.

Avoid: coloured tint, huge haze, full-cell glow, background gradient, vignette, overlapping elements, photorealism, CGI, 3D render, anime, pixel art, vector art.

Square image, aspect ratio 1:1, size 1536x1536.
```

Нарезать пресетом `vfx_neutral`.

### 6.2 Тематические VFX школ

```text
Create a strict sheet of six separate magical visual-effect elements on a perfectly uniform pure-black background, arranged in two rows of three, evenly spaced. Every element is isolated and nothing overlaps.

Reading order:
1. one heavy link of a pale-azure frozen chain, crystalline but compact, with a restrained halo;
2. one small jade-green leaf with luminous veins and almost no surrounding haze;
3. one warm amber ember with a short upward spark trail;
4. three parallel crimson smoky claw slashes, clean tapered shapes, no blood;
5. one circular steel snare trap seen directly from above, open jaws and restrained amethyst light between the teeth, abstract invented glyphs only;
6. one jagged crack with a narrow warm light visible inside, designed so the dark stone disappears under additive blending.

Each effect uses its own school colour, but the glow must stay close to the object. The visible halo occupies no more than 25% beyond the solid shape. Use painterly edges rather than flat gradients.

The background remains pure black everywhere else. No text, letters, numbers, captions, labels, logo, watermark, signature, frame or grid lines.

Avoid: huge bloom, coloured fog filling a cell, overlapping elements, gore, blood, real alphabet characters, background gradient, vignette, photorealism, CGI, 3D render, anime, pixel art, vector art.

Landscape image, aspect ratio 4:3, size 1536x1152.
```

Нарезать пресетом `vfx_schools`.

---
## 7. Контрольные мокапы перед внедрением

Эти изображения не идут в игру. Они нужны, чтобы проверить, что новые ассеты действительно
работают как единая система. Мокапы генерируются **после** основных ассетов.

### 7.1 Контрольный мокап боевого экрана

Приложить концепт 16:9 и по одному готовому примеру: `panel_stone`, `panel_wood`,
`panel_parchment`, `slot_card`, `deck_stack`, оба портрета и две карты.

```text
Use the attached approved 16:9 direction and the attached production assets to create one clean battle-screen validation mockup for the same fantasy card game. This is a design check, not a final screenshot. Preserve the calm hierarchy and use the attached assets faithfully rather than redesigning them.

Canvas and layout:
— widescreen 16:9;
— a narrow top status bar using small restrained bronze icons and warm ivory numbers;
— left column with two compact circular portraits and small dark status plaques;
— central area with two large shared charcoal-stone play fields, each containing five subtle empty card-slot outlines and one small deck stack at the far right;
— bottom hand area made from the dark wood panel, containing five colourful cards as the main focal point;
— right information rail occupying about 19% of the width, with one parchment battle-log panel and one smaller objective/help panel;
— generous spacing and no element touching the screen edge.

Visual hierarchy:
— background and empty play fields are very dark and low contrast;
— UI borders are thin dark bronze;
— cards are the most saturated persistent objects;
— only one card may have a subtle selected-state lift and local glow;
— no bright gold frame around every panel;
— no blue glow on neutral slots or controls;
— no duplicated decorative frame layers.

Do not generate readable prose. Use either blank fields or short neutral placeholder bars where text would go. Do not copy any Russian words from the reference. No watermark or logo.

Avoid: bright overall exposure, orange wood, shiny gold, thick frames, many candles, coloured glows on every object, cluttered slots, oversized portraits, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Widescreen landscape, aspect ratio 16:9, size 1920x1080.
```

Проверка считается успешной, если при уменьшении мокапа до 50% всё ещё сразу видны:
рука карт, чей сейчас ход, два игровых поля и правая информационная колонка — а фактура
рамок не превращается в рябь.

### 7.2 Контрольный мокап главного меню

Приложить `bg_menu.jpg`, `emblem_first.png`, `btn_primary_up.png` и концепт 16:9.

```text
Use the attached approved menu direction and production assets to create one clean main-menu validation mockup for the same fantasy card game. Use the attached background, emblem and button material faithfully.

Layout:
— full widescreen 16:9 background;
— emblem and title zone in the upper-left quadrant;
— four equal-width menu buttons stacked vertically below it with generous gaps;
— a small row of secondary round icons at the bottom-left;
— the distant warm altar remains visible in the right half;
— the left side remains dark enough for clear typography.

The menu is calm, elegant and spacious. Buttons use dark faces and thin bronze rims. Only the hovered primary button receives a restrained warm highlight. The background provides the atmosphere; buttons do not need heavy decoration.

Do not generate readable menu words. Use blank buttons or short neutral placeholder bars. Do not copy text from any reference. No watermark.

Avoid: giant logo, bright left background, glowing outlines around all buttons, orange wood, shiny gold, crowded icon row, thick decorative frames, photorealism, CGI, 3D render, anime, pixel art, flat vector art.

Widescreen landscape, aspect ratio 16:9, size 1920x1080.
```

---

## 8. Порядок генерации

1. Сохранить концепт как `assets_src/anchor/ui_direction_reference_16x9.png`.
2. Перерисовать карту F по §2.1 → `card_F_anchor_v2.png`.
3. Создать UI-якорь по §2.2 → `ui_style_anchor.png`.
4. Сгенерировать карты I, R, S, T и новую рубашку.
5. Сгенерировать три основные панели: stone, wood, parchment.
6. Сгенерировать лист кнопок и лист контролов.
7. Сгенерировать слот, урну и стопку колоды.
8. Сгенерировать иконки, кубики, портреты, раму и эмблему.
9. Сгенерировать четыре фона.
10. Сгенерировать VFX.
11. Собрать контрольный боевой мокап и меню.
12. Только после успешной проверки нарезать листы, делать NinePatch и подключать в LibGDX.

После каждой группы проверяйте ассеты не на белом фоне, а поверх реального тёмного экрана.
На белом фоне тёмная бронза часто выглядит слишком тускло, а в игре оказывается правильной.

---

## 9. Исправляющие запросы

Отправлять в том же чате сразу после неудачной генерации.

| Проблема | Исправляющая фраза |
|---|---|
| Всё слишком яркое | `Reduce the overall exposure and contrast by about 25%. Keep the object readable, but make the neutral surfaces matte, dark and subordinate. Bright highlights must occupy less than 8% of the image.` |
| Слишком много золота | `Replace most of the bright yellow-gold metal with dark patinated bronze. Keep muted gold only as a very thin upper-left edge highlight and remove all large shiny gold areas.` |
| Дерево опять оранжевое | `Darken and desaturate the wood. It must be deep walnut-brown with charcoal shadows, never orange, honey-coloured or reddish. Reduce wood-grain contrast by half.` |
| Камень стал синим | `Remove the blue cast from the neutral stone. Use charcoal grey with only a faint cool ambient undertone; the surface must not read as blue.` |
| Панель шумная | `Make the central 75% of the panel quiet and nearly uniform. Remove scratches, seams, marbling, runes, knots and high-contrast texture from the text-safe area.` |
| Слишком толстая рамка | `Reduce the border thickness by 40%. Use one thin bronze rim and tiny corner caps only. Remove nested frames and secondary border layers.` |
| Слишком объёмно и резко | `Make the relief shallower. Replace deep bevels and hard black shadows with a soft inner shadow and one subtle upper-left highlight.` |
| Появилось цветное свечение на UI | `Remove all coloured magical glow from this neutral interface object. It is shared by every school. Keep only matte charcoal, dark oak, patinated bronze and muted gold.` |
| Слот превратился в плиту | `This must be only a minimal card-slot outline placed on a shared play surface, not a separate stone slab or framed panel. Remove the solid background and keep thin corner brackets plus a faint inner shadow.` |
| Кнопки отличаются слишком сильно | `Make the interaction states more subtle. Hover is only about 10–12% brighter, pressed is only slightly darker and lower, and disabled is desaturated without changing the basic construction.` |
| Панель не подходит под NinePatch | `Clear the centre and the middle sections of all four edges. Keep ornament only in small corners so the asset can be stretched without visible distortion or repeated detail.` |
| Иконки слишком сложные | `Simplify every pictogram to broad clean strokes and negative space. It must remain readable at 24 pixels. Remove miniature scenes, texture and decorative micro-detail.` |
| Menu/Close заменены предметами | `The menu symbol is exactly three thick horizontal geometric bars. The close symbol is exactly two straight bars crossing as an X. Do not replace either symbol with a book, scroll, sword or any object.` |
| Модель скопировала текст из концепта | `Remove every word, letter, number and label copied from the reference. Keep only empty surfaces and visual materials. The game engine will render all typography.` |
| Фон стола занят деталями | `Push every candle, prop, crack and ornament into the outer 12% of the frame. The central 75% must be empty, evenly dark and low contrast for gameplay UI.` |
| Левая часть меню слишком светлая | `Darken and simplify the left 38% of the image. Remove candles, cracks and bright edges from behind the future menu. Keep the only strong focal light at the distant altar in the right half.` |
| Карта опять светится по контуру | `Remove the glowing line around the whole card. Keep school colour only inside the artwork window, one extremely thin local accent line and the small bottom gemstone.` |
| Карточная рамка ярче иллюстрации | `Darken and desaturate the wood and bronze frame by about 20%. Keep the strongest saturation and brightness inside the central magical artwork.` |
| Самоцветы стали прожекторами | `Reduce every gemstone halo by at least 70%. The gems are small coloured indicators, not independent light sources.` |
| Получилась 3D-модель | `Redo as a hand-painted 2D fantasy game asset with visible brushwork and simplified material cues. No photorealistic rendering, ray tracing, PBR or CGI.` |

---

## 10. Правила сборки в LibGDX

Генерация хороших ассетов не исправит экран, если при сборке снова дать каждому элементу
максимальную яркость. Эти правила являются частью дизайн-системы.

### 10.1 Иерархия яркости

На обычном боевом экране:

- фон рисуется примерно на 65–75% исходной яркости;
- нейтральные панели — на 80–90%;
- неактивные иконки — с `alpha` около 0.65–0.75;
- карты в руке — на 100%;
- выбранная карта — на 100%, плюс небольшой подъём и локальная тень;
- глобальный glow не используется;
- disabled-контролы — около 40–50% визуального контраста, но не полностью прозрачные.

Не накладывайте одновременно яркий ассет, белый `Color.WHITE`, внешний glow и ещё одну
светящуюся рамку. Состояние выбора должно строиться максимум из двух признаков:
небольшой подъём + тонкий локальный акцент.

### 10.2 Текст

- Основной текст: тёплая слоновая кость, ориентир `#D6C9AA`.
- Заголовки и активные значения: приглушённое золото, ориентир `#B08A52`.
- Вторичный текст: серо-бежевый, ориентир `#9A927F`.
- Не использовать ярко-жёлтый цвет для всего текста.
- Не обводить каждую букву толстой чёрной линией.
- Достаточно мягкой тени 1–2 px с невысокой непрозрачностью.
- Для заголовков использовать выразительный шрифт с засечками и полной поддержкой
  нужных языков; для длинных правил — более спокойный читаемый шрифт.
- Все числа ресурсов желательно выводить табличными цифрами, чтобы ширина не прыгала.

Ориентиры для 1920×1080, затем масштабировать вместе с viewport:

| Элемент | Размер |
|---|---|
| заголовок модального окна | 34–42 px |
| кнопка меню | 28–34 px |
| обычный текст | 22–27 px |
| подпись/ресурс | 20–24 px |
| крупный логотип | 56–72 px |

### 10.3 Компоновка боевого экрана 16:9

Ориентиры, близкие к утверждённому концепту:

- внешний безопасный отступ: 0.8–1.2% ширины экрана;
- правая информационная колонка: 18–20% ширины;
- левая колонка портретов: 10–12% ширины игрового блока;
- центральные игровые поля: остальная ширина;
- верхняя статусная строка: 5–7% высоты;
- рука игрока: 20–23% высоты;
- промежуток между двумя игровыми полями: 1.2–1.8% высоты;
- визуальный зазор между слотами: не меньше толщины двух рамок слота;
- портрет не должен быть выше примерно 14% высоты экрана;
- карточки в руке не касаются верхней рамки панели и не перекрывают друг друга без причины.

Правая колонка должна быть информационной, а не декоративной. На ней достаточно одной
пергаментной панели журнала и одного компактного блока цели/подсказки. Не помещайте каждый
абзац в отдельную тяжёлую рамку.

### 10.4 Модальные окна

- затемнение фона: чёрный scrim с `alpha` примерно 0.68–0.76;
- окно занимает около 48–58% ширины экрана, а не почти весь экран;
- одна внешняя рамка, без дополнительной деревянной рамки внутри;
- внутренний padding: 5–7% ширины окна;
- кнопка закрытия маленькая и не ярче заголовка;
- заголовок отделяется расстоянием или тонким divider, а не огромной плашкой;
- фон правил — тёмный камень; пергамент использовать только когда нужен действительно
  светлый информационный носитель.

### 10.5 Кнопки и состояния

- обычная кнопка тёмная;
- hover повышает яркость только на 8–12% и может добавить тонкий тёплый кант;
- pressed смещается внутрь на 1–2% своей высоты;
- selected не должен одновременно светиться, увеличиваться, менять цвет и пульсировать;
- в одном меню только одна primary-кнопка может быть заметно теплее остальных;
- иконка слева от текста меньше высоты текста или равна ей, но не крупнее.

### 10.6 NinePatch и атлас

- У каждой растягиваемой панели сохраняйте чистую центральную область.
- Stretch-зоны не должны пересекать уголки, трещины, заклёпки и сильные полосы древесины.
- Перед упаковкой в atlas проверяйте ассет на 50%, 100% и 200% размера.
- Не уменьшайте готовый UI-ассет до размера, где его угловой орнамент становится
  однопиксельным шумом; используйте отдельный более простой вариант для маленьких размеров.
- Цвет школы лучше добавлять в коде через отдельный overlay/indicator, а не запекать в
  нейтральный NinePatch.

### 10.7 Финальный тест

Экран считается удачным, если выполняются четыре проверки:

1. При размытии изображения до крупных пятен первым читается игровой контент, а не рамки.
2. В чёрно-белом режиме сохраняется иерархия: карты → ход/цель → панели → фон.
3. При уменьшении скриншота до 960×540 текст и иконки остаются читаемыми, а фактуры не рябят.
4. Если временно убрать все цветные акценты школ, интерфейс всё равно выглядит цельным.

---

## 11. Учёт

Каждый принятый ассет записывать в `assets_src/GENERATION-LOG.md`:

- дата;
- модель;
- приложенные референсы;
- номер попытки;
- что пришлось исправлять;
- итоговая яркость/насыщенность относительно соседних ассетов;
- имя файла в проекте.

Отдельно отметить, что `card_F_anchor_v2.png` и `ui_style_anchor.png` являются двумя
разными эталонами: первый отвечает за карты, второй — за спокойный интерфейс.
