# 05 — Промпт-бук: готовые промпты на все ассеты

Каждый промпт ниже — **цельный блок, который копируется целиком и вставляется как есть**.
Ничего дописывать, склеивать и подставлять не нужно: стиль, технические требования и
запреты уже вшиты в каждый промпт.

Промпты на английском: image-модели точнее понимают английские описания материалов и света.
Русский текст вокруг блоков — инструкции для вас, в промпт он не идёт.

---

## 0. Как этим пользоваться

**Модель.** Эталонная карта F сгенерирована моделью OpenAI и принята без замечаний —
дальше работаем ею же. Промпты не привязаны к конкретной модели, но менять её посреди
набора не стоит: у каждой свой характер мазка и свой рендер металла.

**Вложение.** К каждому промпту из раздела 2 прикладывайте файл
`assets_src/anchor/card_F_anchor.png`. Это единственный способ удержать один стиль на
всём наборе. В промптах уже написано, что делать с приложенной картинкой.

**Формат кадра.** Задаётся двумя способами сразу — так надёжнее:

1. **Селектором «Формат изображения»** в интерфейсе: доступны 1:1, 3:4, 9:16, 4:3 и 16:9.
2. **Последней строкой промпта** — она есть в каждом блоке ниже. Текстом модель берёт и те
   соотношения, которых нет в селекторе: эталонная карта запрошена как 2:3 и вышла ровно
   1024×1536.

Какой формат для чего:

| Формат | Что генерируем |
|---|---|
| 2:3 (текстом), 1024×1536 | Карты и рубашка |
| 1:1 | Иконки, панели, портреты, рама, эмблема, нейтральные VFX |
| 4:3 | Кубики, тематические VFX |
| 16:9 | Фоны стола и меню, фон загрузки, слот с урной и колодой |
| 9:16 | Вертикальный фон стола |

Обрезать после генерации ничего не нужно — все фоны сразу в тех пропорциях,
в которых их ждёт игра.

**Фон и тень.** Модель почти всегда рисует не идеально белый фон, а светло-серый с мягкой
тенью под объектом — как на эталоне. Это нормально и ожидаемо: фон всё равно удаляется
при постобработке (см. [04-asset-list.md](04-asset-list.md) §7). Требование «pure white»
в промпте оставлено намеренно — оно удерживает модель от рисования сцены вокруг объекта.

**Один чат — одна группа.** Внутри диалога модель держит стиль лучше. Начали новую группу
ассетов — новый чат, и в него снова прикладываем эталон.

### Четыре правила, выведенные из брака

Первый лист «слот, урна, колода» пришлось забраковать. Разбор дал четыре правила,
которые теперь вшиты во все промпты — но если будете писать свой, держите их в голове.

**1. Требуйте тёплую гамму явно.** Без прямого указания модель уводит камень в холодный
синий. Замер по каналам (среднее R−B, «тепло»):

| Ассет | Тепло (R−B) |
|---|---|
| card_F, рамка (эталон) | +29 |
| panel_wood (принят) | +54 |
| panel_stone (принят) | +21 |
| card_back (принят) | +37 |
| слот из забракованного листа | **−23** |
| урна из забракованного листа | **−4** |
| колода из забракованного листа | **−1** |

Разрыв в 44 пункта между принятой каменной панелью и новым слотом виден невооружённым
глазом: рядом они выглядят предметами из разных игр. Поэтому в каждом промпте на предмет
интерфейса стоит строка «The palette is warm throughout» и запрет холодного тона.

**2. Никакого цветного свечения на нейтральных предметах.** Азур, нефрит, янтарь, багрянец
и аметист в этой игре означают конкретные школы магии. Слот, урна, колода и панели —
общие для обеих сторон, и цветное свечение на них читается как ложная подсказка. Свечение
активной зоны игра рисует сама, в цвет стороны.

**3. Уже принятое не перерисовываем — прикладываем.** Если в новом ассете должен появиться
элемент, который уже утверждён (рубашка карты на стопке колоды), приложите этот файл вторым
референсом и прямо запретите выдумывать свой вариант. Иначе стопка окажется с одной рубашкой,
а вылетающая с неё карта — с другой.

**4. Плоский ракурс.** Всё, что лежит на столе рядом с картами, рисуется фронтально или
с наклоном не больше 10 градусов. Выраженная перспектива спорит с плоскими картами.

**Учёт.** Каждый принятый ассет записывайте в [assets_src/GENERATION-LOG.md](../../assets_src/GENERATION-LOG.md) —
файл уже создан, эталон в нём отмечен.

---

## 1. Эталон — готов

`assets_src/anchor/card_F_anchor.png`, 1024×1536, принят без замечаний.

Промпт, которым он получен, сохранён в логе генерации. Повторно его запускать не нужно —
он приведён там только для того, чтобы можно было догенерировать карту в том же стиле
через полгода.

Что этот эталон зафиксировал для всего набора:

- тёплая дубовая рамка с четырьмя бронзово-золотыми угловыми накладками;
- верхняя гранитная плашка-картуш с тонкой светящейся окантовкой цвета школы — **пустая**;
- арочный медальон: главный объект парит над низким каменным постаментом, вокруг —
  свечение цвета школы и частицы в воздухе;
- пергаментная лента под медальоном — **пустая**;
- нижняя тёмная гранитная плашка описания — **пустая**;
- самоцвет цвета школы в центре нижней части рамки;
- свет сверху-слева, холодная подсветка снизу, живописный мазок.

Все промпты ниже написаны так, чтобы это воспроизводилось.

---

## 2. Карты

Четыре карты генерируем **по одной**, а не листом: портретный кадр 2:3 — родной для модели,
поэтому каждая карта выходит в полном разрешении и её не нужно резать.

К каждому промпту прикладываем `card_F_anchor.png`.

### 2.1 Карта I — Родник Изобилия

```text
A reference image of the "F" card from the same fantasy card set is attached. Match its art style, framing, proportions, wood tone, bronze corner fittings, lighting direction, colour grading, brushwork and level of detail exactly. This new card must look like it came from the same printed deck. Only the artwork inside the arched medallion window, the accent glow colour and the bottom gemstone colour change.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.

Layout from top to bottom, occupying the full card:
— top 6% is the outer wooden frame;
— from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint jade-green inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
— from 30% to 72% there is an arched medallion window containing the artwork: a carved stone basin overflowing with glowing emerald water, a young green sprout breaking through the cracked stone rim, the basin floating above a low round stone pedestal exactly like in the reference, emerald light radiating outward, glowing leaves and water droplets floating in the air;
— from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
— from 82% to 95% there is an EMPTY recessed dark granite plate for description, blank, with no writing;
— at the bottom centre, embedded in the frame, a smooth glowing jade-green gemstone cabochon.

A thin glowing jade-green line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is jade green (#A9FFCF); the wood and bronze stay neutral warm brown, exactly as in the reference, and must not be tinted green.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail, filled cartouche, engraved symbols inside the empty plates.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_I.png`.

### 2.2 Карта R — Реликварий

```text
A reference image of the "F" card from the same fantasy card set is attached. Match its art style, framing, proportions, wood tone, bronze corner fittings, lighting direction, colour grading, brushwork and level of detail exactly. This new card must look like it came from the same printed deck. Only the artwork inside the arched medallion window, the accent glow colour and the bottom gemstone colour change.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.

Layout from top to bottom, occupying the full card:
— top 6% is the outer wooden frame;
— from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint amber-gold inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
— from 30% to 72% there is an arched medallion window containing the artwork: an open bronze reliquary casket with its lid raised, floating above a low round stone pedestal exactly like in the reference, warm amber light and glowing embers pouring upward out of the casket, drifting ash gathering into the shape of a single rising phoenix feather above it;
— from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
— from 82% to 95% there is an EMPTY recessed dark granite plate for description, blank, with no writing;
— at the bottom centre, embedded in the frame, a smooth glowing amber gemstone cabochon.

A thin glowing amber line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is amber gold (#FFD195); the wood and bronze stay neutral warm brown, exactly as in the reference, and the accent must stay clearly brighter and more saturated than the wood so it reads as magical light and not as more wood.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail, filled cartouche, engraved symbols inside the empty plates.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_R.png`.

> У этой карты акцент близок к цвету дерева. Если янтарное свечение сливается с рамкой —
> повторите запрос, добавив в конец: `Increase the contrast between the amber magical glow and the brown wooden frame: the glow must be luminous and clearly separated from the wood.`

### 2.3 Карта S — Тень Похитителя

```text
A reference image of the "F" card from the same fantasy card set is attached. Match its art style, framing, proportions, wood tone, bronze corner fittings, lighting direction, colour grading, brushwork and level of detail exactly. This new card must look like it came from the same printed deck. Only the artwork inside the arched medallion window, the accent glow colour and the bottom gemstone colour change.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.

Layout from top to bottom, occupying the full card:
— top 6% is the outer wooden frame;
— from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint crimson-rose inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
— from 30% to 72% there is an arched medallion window containing the artwork: a ghostly clawed hand made of crimson smoke reaching out of the darkness and snatching a golden amulet whose chain has just snapped, the amulet hanging above a low round stone pedestal exactly like in the reference, crimson light and swirling dark smoke around them, broken chain links scattering in the air;
— from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
— from 82% to 95% there is an EMPTY recessed dark granite plate for description, blank, with no writing;
— at the bottom centre, embedded in the frame, a smooth glowing crimson gemstone cabochon.

A thin glowing crimson line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is crimson rose (#FF9AA4); the wood and bronze stay neutral warm brown, exactly as in the reference, and must not be tinted red. The hand is a spectral silhouette of smoke, not a realistic anatomical hand, and there is no blood and no gore anywhere.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail, filled cartouche, engraved symbols inside the empty plates.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_S.png`.

### 2.4 Карта T — Капкан Чародея

```text
A reference image of the "F" card from the same fantasy card set is attached. Match its art style, framing, proportions, wood tone, bronze corner fittings, lighting direction, colour grading, brushwork and level of detail exactly. This new card must look like it came from the same printed deck. Only the artwork inside the arched medallion window, the accent glow colour and the bottom gemstone colour change.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.

Layout from top to bottom, occupying the full card:
— top 6% is the outer wooden frame;
— from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint amethyst-violet inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
— from 30% to 72% there is an arched medallion window containing the artwork: a circular rune-etched steel snare trap with open toothed jaws, tilted towards the viewer so its ring shape reads clearly, hovering above a low round stone pedestal exactly like in the reference, violet magical light glowing between the teeth, thin spectral threads stretched across the open ring like a web, violet sparks in the air;
— from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
— from 82% to 95% there is an EMPTY recessed dark granite plate for description, blank, with no writing;
— at the bottom centre, embedded in the frame, a smooth glowing amethyst gemstone cabochon.

A thin glowing amethyst line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is amethyst violet (#C6B3FF); the wood and bronze stay neutral warm brown, exactly as in the reference, and must not be tinted violet. The runes on the trap ring are abstract invented glyphs, not letters of any real alphabet.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail, filled cartouche, engraved symbols inside the empty plates.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_T.png`.

### 2.5 Рубашка карты

```text
A reference image of the "F" card from the same fantasy card set is attached. Match its outer shape, size, proportions, wooden border, bronze corner fittings, lighting direction, colour grading and brushwork exactly. This is the BACK of the same card, so the outer frame must be identical to the reference.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, dark navy tooled leather, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: the back of a vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The outer carved dark-oak border and bronze corner fittings are identical to the reference card. There are no windows, no plates and no banners on this side: the whole inner area is filled with dark navy blue tooled leather with subtle embossed ornament.

In the exact centre of the leather field there is a raised heraldic seal: a circular tarnished bronze ring with a polished gold inner rim and an ornate abstract arcane sigil in the middle. Five small glowing gemstone cabochons are set evenly around the ring, in clockwise order starting from the top: azure blue, jade green, amber gold, crimson rose, amethyst violet. Each gem glows softly and casts a faint coloured light onto the leather around it.

The design is perfectly symmetrical from left to right, calm and ornamental, with no focal scene and no illustration — it must read as the reverse side of a card and give away nothing about the card's identity.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail, asymmetry.

Portrait orientation, aspect ratio 2:3, size 1024x1536.
```

Сохранить как `assets_src/cards/card_back.png`.

---

## 3. Интерфейс

### 3.1 Панели и кнопки (один лист)

```text
A reference image of a fantasy spell card is attached. Match its art style, material treatment, wood tone, bronze patina, gold accents, lighting direction from the upper left, colour grading and brushwork exactly. These are user-interface elements for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite, aged parchment. Muted rich palette, deep shadows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a sheet of nine separate user-interface elements arranged in three rows on a plain flat pure-white background, evenly spaced, seen perfectly flat and straight on with no perspective, all sharing identical material treatment and lighting. Elements must not overlap and must not touch each other.

Row 1, three wide horizontal rectangular panels with rounded corners, each with a completely empty flat interior:
— a panel of dark carved oak with a tarnished bronze border and small bronze corner fittings;
— a panel of grey-blue granite with a recessed centre and a thin bronze edge;
— a panel of aged parchment held at the corners by bronze clips.

Row 2, three wide horizontal buttons with rounded corners and completely empty faces:
— a raised button with a dark oak body and a polished gold rim, lit from the upper left;
— the same button in a pressed-in state, darker, with the highlight moved to the lower edge;
— the same button desaturated, dull and greyed out, as a disabled state.

Row 3, four smaller elements:
— a raised wide button with a bronze rim and no gold, empty face;
— the same bronze button pressed in;
— a small round bronze button, raised, empty face;
— the same round button pressed in.

Every element is completely empty: no text, no icons, no symbols, no ornament in the middle of the faces.

Plain flat pure-white background, elements do not touch the canvas edges, no shadows reaching the canvas edges, so the background can be removed cleanly. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup screenshot, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, cluttered micro-detail.

Square image, aspect ratio 1:1, size 1024x1024.
```

Нарезать на `panel_wood`, `panel_stone`, `panel_parchment`, `btn_primary_up/down/disabled`,
`btn_secondary_up/down`, `btn_round_up/down` — имена из [04-asset-list.md](04-asset-list.md) §3.

### 3.2 Слот под карту

```text
A reference image of a fantasy spell card is attached. Match its warm colour grading, stone and bronze material treatment, lighting from the upper left and painterly brushwork exactly. This object sits on the same game board as that card.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Materials: warm grey-brown granite with worn chipped edges, tarnished bronze with polished gold highlights. Muted rich warm palette, deep shadows. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single empty vertical card-shaped niche carved into a slab of warm grey-brown granite, seen perfectly flat and straight on with no perspective. The niche is recessed inward with a soft inner shadow, and a thin engraved bronze outline runs around its inner edge, catching a warm highlight along its upper left side. The interior of the niche is empty, flat and slightly darker than the surrounding stone, because a card will be placed into it later.

The palette is warm throughout: the stone is grey-brown with a warm cast, the metal is tarnished bronze with polished gold highlights. There is no blue, no cyan and no coloured magical glow anywhere — this slot is neutral, cards of every school are placed into it, and a coloured glow would read as one particular school.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 8% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, cool blue tint, cyan or azure glow, coloured magical light, perspective, tilt, text of any kind, letters, numerals, watermarks.

Portrait orientation, aspect ratio 2:3.
```

Сохранить как `assets_src/ui/slot_card.png`.

### 3.3 Урна сброса

```text
A reference image of a fantasy spell card is attached. Match its warm colour grading, stone and bronze material treatment, lighting from the upper left and painterly brushwork exactly. This object sits on the same game board as that card.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along the upper edges. Materials: warm grey-brown granite with worn chipped edges, tarnished bronze banding with polished gold highlights. Muted rich warm palette, deep shadows. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a squat carved stone urn with a wide round opening, standing upright and seen from slightly above so the opening reads as an oval. Two tarnished bronze bands wrap around its body, and a small ornamental bronze boss sits at the front where the bands cross. The urn is empty inside, its interior falling into shadow. Discarded cards will be dropped into it, so it looks sturdy, low and open.

The palette is warm throughout: warm grey-brown stone, tarnished bronze, gold highlights. There is no blue, no cyan and no coloured magical glow anywhere — the urn is a neutral board object shared by both players, and a coloured glow would tie it to one school of magic.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 8% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, cool blue tint, cyan or azure glow, coloured magical light, text of any kind, letters, numerals, watermarks.

Square image, aspect ratio 1:1.
```

Сохранить как `assets_src/ui/discard_urn.png`.

### 3.4 Стопка колоды

Прикладываем **два** файла: `card_F_anchor.png` и `card_back.png`. Второй обязателен —
без него модель придумает свою рубашку, и стопка перестанет совпадать с картами,
которые с неё вылетают.

```text
Two reference images are attached: a fantasy spell card, and the BACK of that same card. The deck in this image must use exactly the card back from the second reference — the same dark navy tooled leather, the same bronze ring with five coloured gemstones, the same ornament and the same proportions. Do not invent a different back design.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Materials: dark navy tooled leather, tarnished bronze, warm polished gold, aged card edges. Muted rich warm palette, deep shadows. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a neat stack of about ten identical face-down cards, seen from slightly above and almost straight on, tilted no more than 10 degrees, so the cards still read as flat objects rather than a perspective render. The top card shows the card back from the second reference exactly. Below it, the edges of the remaining cards are visible as thin worn warm-brown layers with faint gilded edges. The corners of the stack are slightly worn and chipped.

The palette is warm throughout, matching the references. Any blue in the image comes only from the navy leather of the card back itself, never from the lighting or the card edges.

Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 8% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, strong perspective, a card back different from the reference, cool blue tint, text of any kind, letters, numerals, watermarks.

Landscape orientation, aspect ratio 4:3.
```

Сохранить как `assets_src/ui/deck_stack.png`.

### 3.5 Иконки — лист 4×4

```text
A reference image of a fantasy spell card is attached. Match its bronze and gold material treatment, lighting direction from the upper left, colour grading and painterly brushwork exactly. These are interface icons for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool fill light from below, narrow warm rim light along top edges. Materials: tarnished patinated bronze with a warm polished gold rim, deeply engraved recesses with soft inner shadow. Muted rich palette, deep shadows. Bold readable silhouettes, physical tabletop-object feel with worn edges. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a strict grid of 16 game interface icons, 4 rows by 4 columns, on a plain flat pure-white background, evenly spaced with equal gaps, all exactly the same size and all seen perfectly flat and straight on. Each icon is a single symbol engraved and embossed into a small round tarnished bronze medallion with a warm polished gold rim, lit from the upper left, with a soft shadow inside the engraving.

The 16 symbols in reading order, left to right and top to bottom:
row 1 — a cogwheel; a horn with three curved sound waves; the same horn with a diagonal slash across it; a small harp;
row 2 — the same harp with a diagonal slash across it; an open book; THREE THICK HORIZONTAL BARS stacked one above another with equal gaps between them, like the menu button of an application, and nothing else on the medallion; TWO THICK STRAIGHT BARS crossing each other in an X shape, like a close button, and nothing else on the medallion;
row 3 — a circular arrow bent into a loop; an arrow pointing left; an hourglass; a neat stack of cards seen from the side;
row 4 — a fan of three cards; a lightning bolt; two small crossed pennant flags on poles; a teardrop-shaped arcane sigil.

The two abstract symbols in row 2 — the three bars and the X — must be plain geometric shapes. Do not replace them with objects, books, scrolls, swords or any other illustration.

Every symbol is a pictogram only. There must be absolutely no written characters anywhere: no alphabet letters, no digits, no runes that resemble letters, no inscriptions on the medallions.

Plain flat pure-white background, icons do not touch the canvas edges, no shadows reaching the canvas edges, so the background can be removed cleanly. No text, no captions, no watermark, no signature, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, cluttered micro-detail, icons of different sizes.

Square image, aspect ratio 1:1, size 1024x1024.
```

Нарезать по сетке пресетом `icons`. Имена в порядке чтения зашиты в нарезчике.

> **Что уже случилось на этом промпте.** Модель нарисовала 14 символов из 16 верно,
> а два абстрактных заменила иллюстрациями: вместо трёх полос меню — раскрытую книгу,
> вместо косого креста — скрещённые мечи. Плюс на шестой позиции вместо книги оказалась
> перечёркнутая книга. Принятые файлы названы по факту (`icon_rules_off`, `icon_duel`),
> мечи пригодились под кнопку «Играть». Абстрактные символы в промпте усилены прописными
> буквами и отдельным абзацем — при повторной генерации должно сработать.

### 3.5.1 Догенерация недостающих иконок

Меню (три полосы) и закрытие (косой крест) в наборе отсутствуют. Приложите любую
принятую иконку, например `assets_src/ui/icon_settings.png`, и запросите две штуки:

```text
A reference image of a game interface icon is attached: a symbol engraved and embossed on a small round tarnished bronze medallion with a warm polished gold rim. Match its material, lighting from the upper left, warm colour grading, size and painterly brushwork exactly.

Subject: two icons of exactly that kind, side by side on a plain flat pure-white background, evenly spaced, not touching, both the same size as each other and as the reference.

The left icon carries THREE THICK HORIZONTAL BARS stacked one above another with equal gaps between them, filling most of the medallion — the universal menu symbol. The right icon carries TWO THICK STRAIGHT BARS crossing each other in an X shape — the universal close symbol.

Both symbols are plain geometric shapes engraved into the bronze. Do not turn them into books, scrolls, swords, banners or any other object. Nothing else is drawn on the medallions.

Plain flat pure-white background, icons do not touch the canvas edges, no shadows reaching the canvas edges, so the background can be removed cleanly. No text, no letters, no numbers, no watermark, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, anime, pixel art, vector art, flat design, cool blue tint, text of any kind, letters, numerals, watermarks, replacing the abstract symbols with objects.

Square image, aspect ratio 1:1.
```

Нарезать: `./gradlew tools:sliceSheet -Psheet=... -Pnames=icon_menu,icon_close -Pout=../assets_src/ui`

> Промпт сработал: оба символа остались геометрическими фигурами. Иконки вышли крупнее
> и золотистее первого набора, но рядом читаются как один комплект.

### 3.6 Кубики — лист из шести граней

```text
A reference image of a fantasy spell card is attached. Match its stone and bronze material treatment, lighting direction from the upper left, colour grading and painterly brushwork exactly. These are dice for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Materials: carved grey-blue granite with worn chipped edges, small bronze caps on the corners, recessed pips filled with glowing warm amber light. Muted rich palette, deep shadows, subtle bloom only on the glowing pips. Bold readable silhouette, physical tabletop-object feel. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: six identical carved granite dice arranged in two rows of three on a plain flat pure-white background, evenly spaced, not touching each other. Every die is the same cube seen from exactly the same slightly-elevated three-quarter angle, at exactly the same size, with exactly the same lighting — the six images differ only in the number of pips on the visible top face.

Reading order: the first die shows one pip, the second two pips, the third three pips, the fourth four pips, the fifth five pips, the sixth six pips. The pips are round recesses filled with glowing warm amber light, arranged in the standard dice layout. The side faces of the dice are left plain and unmarked so the top face reads clearly.

There are no numerals anywhere — quantity is shown only by round pips.

Plain flat pure-white background, dice do not touch the canvas edges, no shadows reaching the canvas edges, so the background can be removed cleanly. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no border frame around the whole image.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, cluttered micro-detail, dice of different sizes or angles.

Landscape orientation, aspect ratio 4:3.
```

Нарезать пресетом `dice` на `die_1` … `die_6`.

> **Что случилось на этом промпте.** Значения и ракурс модель выдержала точно, но камень
> нарисовала холодным (тепло R−B от −1 до +9 при диапазоне набора +21…+64). Вылечено
> постобработкой: `./gradlew tools:warmGrade -Pdir=assets_src/ui -Pprefix=die_ -Ptarget=20`.
> Если генерируете заново — добавьте в промпт строку
> `The stone is warm grey-brown, not blue-grey. There is no cool blue cast anywhere.`

### 3.7 Портрет игрока

```text
A reference image of a fantasy spell card is attached. Match its painterly style, warm lighting from the upper left, cool fill light from below, colour grading and brushwork exactly. This is a character portrait for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along the top edges of the figure. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, heroic and friendly. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a chest-up portrait of a young human sorcerer, the player's hero. Deep blue robes with tarnished bronze clasps and a high collar, short dark hair, a calm confident friendly expression, a faint azure glow in the eyes, a thin azure rune-light tracing along the collar. Three-quarter view, body turned slightly to the viewer's right, face looking towards the viewer.

The figure is lit warmly from the upper left with a cool blue rim light on the right shoulder. Behind the figure there is a plain dark neutral background with a soft radial falloff, no scenery, no props and no other characters.

The portrait is composed to be cropped into a circle later, so the head is centred in the upper half of the frame and nothing important touches the edges of the canvas. Leave 8% empty margin on all sides. No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo, no frame or border drawn around the portrait.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, weapons, text of any kind, letters, numerals, watermarks.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/portrait_player.png`.

### 3.8 Портрет оппонента

```text
A reference image of a fantasy spell card is attached. Match its painterly style, warm lighting, cool fill light, colour grading and brushwork exactly. This is a character portrait for the same game and it must sit next to a portrait of a young sorcerer as its counterpart.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper right, cool blue fill light from below, narrow warm rim light along the top edges of the figure. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, imposing but not horrifying. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a chest-up portrait of an old human archmage, the opponent. Deep crimson robes with tarnished gold trim, a deep hood pulled over the head leaving the face visible, a long grey beard, narrowed cunning eyes with a faint crimson glow, a knowing half-smile. Three-quarter view, body turned slightly to the viewer's left so that he faces the opposite direction from the young sorcerer, face looking towards the viewer.

The figure is lit warmly from the upper right with a cool blue rim light on the left shoulder. Behind the figure there is a plain dark neutral background with a soft radial falloff, no scenery, no props and no other characters.

The portrait is composed to be cropped into a circle later, so the head is centred in the upper half of the frame and nothing important touches the edges of the canvas. Leave 8% empty margin on all sides. No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo, no frame or border drawn around the portrait.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, skulls, weapons, text of any kind, letters, numerals, watermarks.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/portrait_ai.png`.

### 3.9 Рама портрета

```text
A reference image of a fantasy spell card is attached. Match its bronze and gold material treatment, lighting direction from the upper left, colour grading and painterly brushwork exactly. This is a portrait frame for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along the top edge. Materials: tarnished patinated bronze with a polished warm gold inner rim, worn edges and small chips. Muted rich palette, deep shadows. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single empty circular portrait frame, seen perfectly flat and straight on, centred in the canvas. The ring is made of tarnished bronze with a polished gold inner rim and carries four small ornamental studs at the top, bottom, left and right positions. The ring is thick and clearly three-dimensional, with bevels catching the light.

The centre of the ring is completely empty — nothing is drawn inside it, no portrait, no glass, no fill, no colour, because a character portrait will be placed under it later. Only the ring itself is painted.

Plain flat pure-white background, the frame does not touch the canvas edges, no shadow reaching the canvas edges, so the background can be removed cleanly. Leave 6% empty margin on all sides. No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, anything drawn inside the ring.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/frame_portrait.png`.

### 3.10 Эмблема Ордена

```text
A reference image of a fantasy spell card is attached. Match its bronze and gold material treatment, lighting direction from the upper left, colour grading and painterly brushwork exactly. This is the heraldic emblem of the wizard order that the game is about.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm light from the upper left, cool blue fill light from below, narrow warm rim light along the top edges. Materials: tarnished patinated bronze, warm polished gold, five smooth glowing gemstone cabochons. Muted rich palette, deep shadows, subtle bloom only on the gem glows. Bold readable silhouette that stays recognisable at small size. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a single heraldic seal, seen perfectly flat and straight on, centred in the canvas, shaped like a pointed shield with ornamental scrollwork along its left and right edges. Its face is tarnished bronze with a polished gold border. In the centre of the shield there is an ornate abstract arcane sigil made of interlaced curved lines.

Five glowing gemstone cabochons are set into the shield around the sigil, evenly spaced, in clockwise order starting from the top: azure blue, jade green, amber gold, crimson rose, amethyst violet. Each gem glows softly and lights the metal around it.

At the bottom of the shield there is a small blank banner shape, completely empty and smooth, with no writing and no engraving on it.

The emblem is perfectly symmetrical from left to right, mirrored around the vertical centre line.

Plain flat pure-white background, the emblem does not touch the canvas edges, no shadow reaching the canvas edges, so the background can be removed cleanly. Leave 6% empty margin on all sides. No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, asymmetry, cluttered micro-detail.

Square image, aspect ratio 1:1, size 1024x1024.
```

Сохранить как `assets_src/ui/emblem_first.png`. Название игры пишется поверх шрифтом,
в самой эмблеме текста быть не должно.

---

## 4. Фоны

### 4.1 Стол, горизонтальная раскладка

```text
A reference image of a fantasy spell card is attached. Match its painterly style, stone and bronze material treatment, warm-versus-cool lighting, colour grading and brushwork exactly. This is the game board background of the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm candlelight from the left and right edges, cool blue ambient light from above, deep shadows. Materials: grey-blue granite with faintly glowing rune veins, worn brass inlays, dark carved oak, dripping wax candles with warm flames. Muted rich palette, subtle bloom only on the candle flames and rune glow. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: an ancient stone duelling altar table filling the whole frame, seen from a slightly elevated angle so its surface reads as a wide flat playing field. The surface is grey-blue granite with faint glowing rune veins running through it and worn brass inlays along the edges. The table is bordered by dark carved oak with bronze fittings.

Several burning candles on iron stands stand at the far left and far right edges of the frame, throwing warm light inward across the stone. Behind and around the table there is deep darkness falling off into a heavy natural vignette.

Composition requirement: the central two thirds of the image are open, empty and uncluttered table surface with absolutely nothing standing on it, because game cards and panels will be drawn on top of this background. All detail and interest is pushed to the outer edges, and the very top and very bottom of the frame stay dark and quiet so that interface panels can sit over them.

Full-bleed background illustration filling the entire canvas, no isolated object, no drawn frame or border around the image. No text, no letters, no numbers, no words, no watermark, no signature, no logo, no characters, no creatures, no cards, no dice and no objects lying on the table.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, busy central area.

Widescreen landscape orientation, aspect ratio 16:9.
```

Сохранить как `assets_src/bg/bg_table_landscape.jpg`.

### 4.2 Стол, вертикальная раскладка

```text
A reference image of a fantasy spell card is attached. Match its painterly style, stone and bronze material treatment, warm-versus-cool lighting, colour grading and brushwork exactly. This is the vertical version of the game board background of the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm candlelight from the top and bottom edges, cool blue ambient light, deep shadows. Materials: grey-blue granite with faintly glowing rune veins, worn brass inlays, dark carved oak, dripping wax candles with warm flames. Muted rich palette, subtle bloom only on the candle flames and rune glow. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: the same ancient stone duelling altar table as in a landscape version, but recomposed for a tall vertical frame. The granite surface with glowing rune veins and brass inlays runs from top to bottom and fills the whole frame. Burning candles on iron stands stand near the top edge and near the bottom edge, throwing warm light towards the centre. Deep darkness and a heavy natural vignette surround the table.

Composition requirement: the tall central area of the image is open, empty and uncluttered table surface with absolutely nothing standing on it, because game cards and panels will be drawn on top of this background. All detail is pushed to the top and bottom edges.

Full-bleed background illustration filling the entire canvas, no isolated object, no drawn frame or border around the image. No text, no letters, no numbers, no words, no watermark, no signature, no logo, no characters, no creatures, no cards, no dice and no objects lying on the table.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, busy central area.

Tall portrait orientation, aspect ratio 9:16.
```

Сохранить как `assets_src/bg/bg_table_portrait.jpg`.

### 4.3 Фон меню

```text
A reference image of a fantasy spell card is attached. Match its painterly style, stone material treatment, warm-versus-cool lighting, colour grading and brushwork exactly. This is the main menu background of the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, warm candlelight, cool blue ambient light in the depths, deep shadows, dust motes floating in the light beams. Materials: massive carved grey-blue stone with glowing rune veins, iron candle stands, warm wax candles, distant gold light. Muted rich palette, subtle bloom only on flames and rune glow. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: the interior of an underground arcane sanctum. Massive carved stone arches recede into darkness towards a distant altar that is lit by a soft golden glow. Glowing rune veins run through the stone walls and floor. Dozens of candles on iron stands line the walls, their light falling in warm pools. Dust motes drift through the beams of light. The mood is deep, mysterious and inviting rather than threatening.

Composition requirement: the left third of the image is noticeably darker, simpler and less detailed than the rest, because a menu with buttons will be placed over it. The distant altar and the brightest light sit in the right half of the frame. There are no characters and no creatures anywhere.

Full-bleed background illustration filling the entire canvas, no isolated object, no drawn frame or border around the image. No text, no letters, no numbers, no words, no watermark, no signature, no logo.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, skulls, text of any kind, letters, numerals, watermarks.

Widescreen landscape orientation, aspect ratio 16:9.
```

Сохранить как `assets_src/bg/bg_menu.jpg`.

### 4.4 Фон экрана загрузки

```text
A reference image of a fantasy spell card is attached. Match its painterly style, stone material treatment, lighting and colour grading exactly. This is a very quiet loading-screen background for the same game.

Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted texture with visible brushwork, one small warm candle flame as the only light source, cool blue darkness everywhere else, very deep shadows and a heavy natural vignette. Materials: rough grey-blue stone wall with faint glowing rune veins. Muted rich palette, minimal detail. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.

Subject: a dark stone wall seen straight on, with faint glowing rune veins running through it and a single distant candle burning at the far edge of the frame. Most of the image is quiet darkness. There is nothing else in the scene — no objects, no characters, no architecture details competing for attention — because a logo and a progress bar will be drawn over this image.

Full-bleed background illustration filling the entire canvas, no isolated object, no drawn frame or border around the image. No text, no letters, no numbers, no words, no watermark, no signature, no logo.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, text of any kind, letters, numerals, watermarks, busy composition.

Widescreen landscape orientation, aspect ratio 16:9.
```

Сохранить как `assets_src/bg/bg_loading.jpg`.

---

## 5. Визуальные эффекты

Эти два листа генерируются **на чёрном фоне** и без эталона — стиль тут не нужен, нужна
чистая светящаяся форма. Чёрный убирается аддитивным блендингом прямо в движке, поэтому
вырезать альфу вручную не придётся.

### 5.1 Нейтральные элементы

```text
Subject: a sheet of eight separate magical visual-effect elements on a plain flat pure-black background, arranged in two rows of four, evenly spaced, each element isolated with nothing touching it and nothing overlapping.

Reading order, left to right and top to bottom:
1. a soft round radial glow, pure white in the centre, fading smoothly and evenly to nothing at its edges;
2. a multi-rayed star burst flare, white, symmetrical, with four long rays and several short ones;
3. a thin circular ring of glowing arcane runes seen flat from directly above, the runes being abstract invented glyphs and not letters of any real alphabet;
4. a thin expanding shockwave ring, dark in the middle and brightest along its outer edge;
5. a single small bright spark with a soft halo;
6. a small soft ash mote with blurred edges;
7. a soft translucent puff of smoke, wispy and uneven;
8. a vertical beam of light, brightest and widest at the top, fading to nothing at the bottom.

All eight elements are painted in white and very light grey only, with no colour of their own, because they will be tinted inside a game engine. They glow additively against the pure black background, which stays absolutely uniform and pure black everywhere else.

No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo, no frame or border around the image, no grid lines, no labels under the elements.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, pixel art, vector art, coloured tints, dark elements, elements touching each other, background gradients, vignettes.

Square image, aspect ratio 1:1, size 1024x1024.
```

Нарезать пресетом `vfx_neutral`. Порог прозрачности и минимальная площадь для листов
эффектов подставляются автоматически: у свечений мягкий край, и обычный порог срезал бы
ореол, а мелкая пылинка отсеялась бы как мусор.

> Лист прошёл с первого раза без замечаний.

### 5.2 Тематические элементы школ

```text
Subject: a sheet of six separate magical visual-effect elements on a plain flat pure-black background, arranged in two rows of three, evenly spaced, each element isolated with nothing touching it and nothing overlapping.

Reading order, left to right and top to bottom:
1. a single heavy link of a glowing pale-azure ice chain, thick and crystalline, seen at a slight angle;
2. a small glowing emerald-green leaf with luminous veins;
3. a single glowing amber ember with a faint upward trail of sparks;
4. a diagonal triple claw slash mark made of crimson light and smoke, three parallel tapering gashes;
5. a circular rune-etched steel snare trap with open toothed jaws seen from directly above, violet light glowing between the teeth, the runes being abstract invented glyphs and not letters of any real alphabet;
6. a jagged crack spreading across dark stone with warm light shining out from inside the crack.

Every element glows against the pure black background, which stays absolutely uniform and pure black everywhere else. Each element keeps its own colour as described, painted with visible brushwork rather than flat gradients.

No text, no letters, no numbers, no words, no captions, no watermark, no signature, no logo, no frame or border around the image, no grid lines, no labels under the elements.

Avoid: photorealism, 3D render, CGI, ray tracing, anime, pixel art, vector art, elements touching each other, background gradients, vignettes, gore, blood.

Landscape orientation, aspect ratio 4:3.
```

Нарезать пресетом `vfx_schools`.

> Лист прошёл с первого раза. Трещина по камню вырезается так, что тёмная порода уходит
> в прозрачность и остаются светящиеся линии — для аддитивной отрисовки это правильно.

---

## 6. Порядок работы

1. ~~Эталон — карта F~~ — **сделано**.
2. Карты I, R, S, T — четыре отдельных запроса (§2.1–2.4).
3. Рубашка (§2.5).
4. Панели и кнопки (§3.1) → нарезать.
5. Слот (§3.2), урна (§3.3), колода (§3.4) — тремя отдельными запросами.
   Для колоды обязательно приложить `card_back.png` вторым референсом.
6. Иконки (§3.5) → нарезать по сетке.
7. Кубики (§3.6) → нарезать.
8. Портреты и рама (§3.7–3.9).
9. Эмблема (§3.10).
10. Фоны (§4.1–4.4).
11. VFX (§5.1–5.2).

После каждой группы — вставьте полученное в игру и посмотрите вживую. Ассет, красивый
сам по себе, часто разваливается рядом с остальными; в отрыве от экрана это не видно.

---

## 7. Если получилось не то

Отправляйте эти фразы **в том же чате** следующим сообщением — модель правит свою же
картинку точнее, чем генерирует заново.

| Симптом | Что написать |
|---|---|
| В картуше, ленте или нижней плашке появились закорючки | `Regenerate the exact same image, but the rectangular plate at the top, the ribbon banner and the bottom plate must be completely empty, smooth and blank — no engraving, no glyphs, no scribbles, no ornament inside them. Everything else stays identical.` |
| Стиль уехал от эталона | `This is off-style. Match the attached reference exactly: same wood tone, same bronze patina, same light direction from the upper left, same brush texture, same colour grading, same level of detail. Redo.` |
| Карта под углом или в перспективе | `Show the card perfectly flat and straight on, orthographic, no perspective, no tilt, no rotation, no card thickness visible.` |
| Рамка несимметричная | `Make the frame perfectly symmetrical left to right, mirrored around the vertical centre line.` |
| Фон не белый или тень уходит за край | `Place the object on a completely plain flat pure-white background. The object must not touch the canvas edges and must cast no shadow that reaches the edges.` |
| Слишком много мелких деталей | `Reduce detail density by half. Keep large readable shapes, remove micro-ornaments and clutter. The silhouette must read clearly at thumbnail size.` |
| Акцентный цвет ушёл | `The accent glow, the inner frame line and the bottom gemstone must all be exactly this colour: #FFD195. The wood and bronze stay neutral warm brown and must not be tinted by the accent colour.` |
| Элементы на листе разного размера | `All items on the sheet must be exactly the same size, seen from exactly the same angle, and aligned on a strict grid with equal spacing.` |
| Появилась рамка вокруг всего изображения | `Do not draw any border or frame around the whole image. Only the objects themselves on a plain background.` |
| Ассет вышел холодным, не в тон принятым | `The palette must be warm throughout: warm grey-brown stone, tarnished bronze, warm gold highlights. Remove the cool blue cast entirely. Match the warm colour grading of the attached reference.` |
| Модель нарисовала свою рубашку карты | `The card back must be exactly the one in the attached reference: same navy tooled leather, same bronze ring, same five gemstones, same ornament. Do not design a new back.` |
| На нейтральном предмете появилось цветное свечение | `Remove all coloured magical glow. This object is neutral and shared by both players, so any school colour on it would be misleading. Keep only warm bronze and gold highlights.` |
| Элементы на листе слиплись | `Increase the spacing between the elements so that none of them touch or overlap, and none of them touch the canvas edges.` |

---

## 8. Учёт

Каждый принятый ассет — строкой в [assets_src/GENERATION-LOG.md](../../assets_src/GENERATION-LOG.md):
дата, модель, номер попытки, замечания. Через месяц, когда понадобится догенерировать
одну карту в том же стиле, этот файл сэкономит час.
