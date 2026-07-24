# 05 — Промпт-бук для nano banana (Gemini Image)

Практическое руководство: как получить 60+ ассетов в одном стиле, генерируя вручную в чате.

Промпты — на английском: image-модели заметно точнее понимают английские описания материалов и света. Русские комментарии — для вас, в промпт их не копируем.

---

## 1. Метод

### 1.1 Три правила, без которых стиль расползётся

1. **Style Anchor.** Первый утверждённый ассет (карта F) становится эталоном. Во **все** последующие запросы прикладываем его как reference-изображение и пишем `Match the art style, lighting, material treatment and colour grading of the attached reference exactly.`
2. **Групповая генерация.** Связанные ассеты просим одним изображением-листом (5 карт, 16 иконок, 6 граней кубика). Внутри одной картинки стиль совпадает почти идеально — резать потом дешевле, чем бороться с дрейфом.
3. **Один чат = одна группа ассетов.** Модель удерживает контекст стиля внутри диалога. Как только начинается новая группа — новый чат, и в него снова кладём Anchor.

### 1.2 Формула промпта

```
[STYLE BLOCK]  — дословно из 03-art-style-bible.md §10, никогда не перефразировать
[SUBJECT]      — что изображено, конкретно и по слоям
[COMPOSITION]  — раскладка, ракурс, поля, что в центре
[TECH]         — фон, прозрачность, соотношение сторон, отсутствие текста
[NEGATIVE]     — что запрещено
```

### 1.3 Технические хвосты (копировать в конец промпта)

**Для элементов с прозрачностью:**
```
Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo, no UI mockup, no border frame around the whole image.
```

**Для фонов:**
```
Full-bleed background illustration, no isolated object, no frame. No text, no letters, no numbers, no watermark, no characters in the centre of the composition.
```

**Универсальный negative:**
```
Avoid: photorealism, 3D render, CGI, ray tracing, anime, manga, chibi, cartoon flash style, pixel art, vector art, flat design, minimalism, neon, cyberpunk, sci-fi, modern objects, technology, gore, blood, text of any kind, letters, numerals, watermarks, duplicated frames, cluttered micro-detail.
```

### 1.4 Соотношения сторон

| Ассет | Aspect ratio |
|---|---|
| Карта | 2:3 |
| Фон landscape | 16:9 |
| Фон portrait | 9:16 |
| Иконка, кубик, портрет, эмблема | 1:1 |
| Кнопка | 16:5 |
| Панель, лист-сетка | 1:1 |

---

## 2. Шаг 0 — Style Anchor (карта F)

Самый важный запрос проекта. Итерируем до полного удовлетворения — всё остальное будет наследовать этот результат.

> Painterly digital illustration in the style of a premium fantasy collectible card game. Hand-painted textures with visible brushwork, rich warm lighting from the upper left, cool blue fill light from below, narrow warm rim light along top edges. Heavy ornate carved dark-oak wood and tarnished bronze framing with deep bevels and soft drop shadows. Materials: aged oak with visible grain, patinated bronze, warm polished gold accents, grey-blue granite with faintly glowing rune veins, smooth glowing gemstones. Muted rich palette, deep shadows, subtle bloom only on magical glows. Slightly stylised exaggerated proportions, bold readable silhouette, physical tabletop-object feel with worn edges and small chips. Not photorealistic, not 3D render, not anime, not pixel art, not flat vector.
>
> Subject: a single vertical fantasy spell card, portrait orientation, seen perfectly flat and straight on with no perspective. The card is a physical object with a thick carved dark-oak border and tarnished bronze corner fittings.
>
> Layout from top to bottom, occupying the full card:
> — top 6% is the outer wooden frame;
> — from 6% to 30% of the height there is a large EMPTY carved rectangular cartouche plate, recessed into the frame, made of grey-blue granite with a faint azure inner glow around its inner edge. This plate must be completely blank and empty — no symbol, no engraving, no ornament inside it;
> — from 30% to 72% there is an arched medallion window containing the artwork: a heavy ancient rune-carved padlock wrapped in glowing pale-azure ice chains, floating above a frost-covered stone pedestal, cold azure light radiating outward, frost crystals in the air;
> — from 72% to 82% there is an EMPTY horizontal parchment ribbon banner stretched across the card, blank, with no writing;
> — from 82% to 95% there is an EMPTY recessed dark plate for description, blank, with no writing;
> — at the bottom centre, embedded in the frame, a smooth glowing azure gemstone cabochon.
>
> A thin glowing azure line runs along the inner perimeter of the wooden frame. The dominant accent colour of this card is azure blue (#9CC8FF); the wood and bronze are neutral warm brown.
>
> Isolated object centred on a plain flat pure-white background with no shadow touching the canvas edges, so the background can be removed cleanly. Leave 5% empty margin on all sides. No text, no letters, no numbers, no digits, no words, no captions, no watermark, no signature, no logo.
>
> Avoid: photorealism, 3D render, CGI, anime, pixel art, flat vector, neon, cyberpunk, modern objects, text of any kind, letters, numerals, filled cartouche, engraved symbols inside the empty plates.
>
> Aspect ratio 2:3.

**Приёмка Anchor.** Проверить по чек-листу [04-asset-list.md](04-asset-list.md) §8 и дополнительно:
- картуш, лента и поле описания **действительно пустые** (частая ошибка модели — вписать «текст» из закорючек);
- карта строго фронтальная, без перспективы и наклона;
- рамка симметричная слева-направо.

Если картуш заполнен — уточняющий запрос по §5.

Утверждённый файл сохраняем как `assets_src/anchor/card_F_anchor.png` и прикладываем ко всем следующим запросам.

---

## 3. Группы ассетов

### 3.1 Карты I, R, S, T (лист)

Новый чат. Приложить Anchor.

> Using the attached reference card as the exact style, framing, layout and lighting template, generate a sheet of four more cards from the same set, arranged in one horizontal row, evenly spaced on a plain flat pure-white background, all identical in size, shape, framing, proportions and camera angle to the reference. Only the artwork inside the arched medallion, the accent glow colour and the bottom gemstone colour differ between cards. The wooden frame and bronze fittings must be pixel-identical in style across all four. Every cartouche plate, ribbon banner and description plate must remain completely EMPTY and blank.
>
> Card 1 — accent colour jade green (#A9FFCF): a carved stone basin overflowing with glowing emerald water, a young sprout breaking through the cracked stone rim, emerald light and floating leaves.
>
> Card 2 — accent colour amber gold (#FFD195): an open bronze reliquary casket on a pedestal, warm amber light and glowing embers rising out of it, ashes reforming into the shape of a rising phoenix feather.
>
> Card 3 — accent colour crimson rose (#FF9AA4): a ghostly clawed hand made of crimson smoke reaching out and snatching a golden amulet, its chain snapped, swirling dark smoke around it.
>
> Card 4 — accent colour amethyst purple (#C6B3FF): a circular rune-etched snare trap with steel jaws seen from above, violet magical light glowing between the teeth, spectral threads stretched across it like a web.
>
> [универсальный technical tail + negative]
>
> Aspect ratio 16:9 for the whole sheet.

Затем — по одной карте на проверку в полном разрешении:

> Reproduce card 3 from the previous sheet alone, at maximum detail, exactly the same design, composition and colours, portrait orientation, isolated on plain white. Aspect ratio 2:3.

### 3.2 Рубашка карты

> [STYLE BLOCK] Subject: the back of a fantasy spell card, same size, proportions and outer frame as the attached reference card. Instead of any window or plate, the entire inner area is dark navy blue tooled leather with a raised symmetrical heraldic emblem in the centre: a circular bronze seal with five small glowing gemstones arranged in a ring around it, one azure, one jade, one amber, one crimson, one amethyst. Perfectly symmetrical left to right, ornamental, calm, no focal artwork. [tech tail] Aspect ratio 2:3.

### 3.3 Фоны

**B-01 стол, landscape:**
> [STYLE BLOCK] Subject: an ancient stone duelling altar table seen from a slightly elevated three-quarter angle, occupying the whole frame. The table surface is grey-blue granite with faintly glowing rune veins, worn brass inlays along the edges, a few burning candles with warm light at the left and right edges, dark carved wooden borders. Behind and around it, deep darkness fading into a heavy vignette. The central two thirds of the image are an open, empty, uncluttered table surface with no objects on it, because game elements will be drawn on top. Warm candlelight from the sides, cool blue ambient from above. [background tech tail + negative] Aspect ratio 16:9.

**B-02 стол, portrait:** тот же промпт, но `Aspect ratio 9:16` и `recompose vertically: the empty table surface fills the tall centre of the frame, candles at top and bottom edges.`

**B-03 меню:**
> [STYLE BLOCK] Subject: the interior of an underground arcane sanctum: massive carved stone arches receding into darkness, glowing rune veins running through the stone walls, dozens of candles on iron stands, a distant altar with a soft golden light behind it, dust motes floating in light beams. Atmospheric, deep, mysterious, inviting. The left third of the image is darker and less detailed so that a menu can be placed over it. [background tech tail + negative] Aspect ratio 16:9.

**B-04 загрузка:**
> [STYLE BLOCK] Subject: a very dark, simple, atmospheric background — a stone wall with faint glowing rune veins and a single distant candle, heavy vignette, minimal detail, mostly darkness. [background tech tail] Aspect ratio 16:9.

### 3.4 Панели и кнопки (лист)

> [STYLE BLOCK] Using the attached reference for style and materials, generate a sheet of user-interface elements for the same fantasy game, arranged in a neat grid on a plain flat pure-white background, evenly spaced, all sharing identical material treatment and lighting:
> Row 1: a horizontal rectangular panel of dark carved oak with a tarnished bronze border and small bronze corner fittings, empty interior; the same panel but made of grey-blue granite with a recessed centre; the same panel but made of aged parchment held by bronze clips.
> Row 2: a wide horizontal button with a dark oak body and a polished gold rim, slightly raised, empty face; the same button pressed down and darker; the same button desaturated and dull.
> Row 3: a wide horizontal button with a bronze rim and no gold, raised, empty face; the same button pressed down; a small round bronze button, raised, empty face; the same round button pressed down.
> All elements are empty — no text, no icons, no symbols on them. [tech tail + negative] Aspect ratio 1:1.

Отдельно — слот и урна:
> [STYLE BLOCK] Two objects side by side on plain white: (1) a vertical recessed empty card-shaped niche carved into grey-blue granite, with a faint glowing rune outline running around the inner edge and soft inner shadow — a slot where a card will be placed; (2) a squat carved stone urn with a wide opening and bronze banding, standing upright, empty, used as a discard container. [tech tail] Aspect ratio 16:9.

### 3.5 Иконки (лист 4×4)

> [STYLE BLOCK] Using the attached reference for material and lighting, generate a 4 by 4 grid of 16 game interface icons on a plain flat pure-white background, evenly spaced, all exactly the same size, all in the same style: each icon is a symbol engraved and embossed on a small round tarnished bronze medallion with a warm gold rim, lit from the upper left, with soft shadow inside the engraving.
> The 16 symbols in reading order: a cogwheel; a sound horn with wave lines; a sound horn crossed out; a lyre; a lyre crossed out; an open book; three stacked horizontal bars; a diagonal cross; a circular arrow; a left arrow; an hourglass; a stack of cards; a fan of cards held in a hand; a lightning bolt; a globe; a letter-free exclamation-style teardrop sigil.
> Every symbol must be a pictogram only — absolutely no written characters, no alphabet letters, no digits. [tech tail + negative] Aspect ratio 1:1.

> Замечание: если модель всё же нарисует буквы на «globe» или «lang» — заменить символ на два пересечённых флажка без рисунка.

### 3.6 Портреты

> [STYLE BLOCK] Subject: a chest-up portrait of a young human sorcerer in deep blue robes with bronze clasps, short dark hair, confident friendly expression, a faint azure glow in the eyes, painted in a heroic fantasy card-game style. Three-quarter view, looking slightly toward the viewer, lit warmly from the upper left, dark neutral background behind the figure. [tech tail] Aspect ratio 1:1.

> [STYLE BLOCK] Subject: a chest-up portrait of an old human archmage in deep crimson robes with tarnished gold trim, long grey beard, hooded, cunning narrowed eyes with a faint crimson glow, painted in a heroic fantasy card-game style. Three-quarter view mirrored to face the opposite direction from a hero portrait, lit warmly from the upper right, dark neutral background. [tech tail] Aspect ratio 1:1.

Рама:
> [STYLE BLOCK] Subject: an empty circular portrait frame made of tarnished bronze with polished gold inner rim and four small ornamental studs at top, bottom, left and right. The centre is completely empty and transparent-looking, only the ring itself is drawn. [tech tail] Aspect ratio 1:1.

### 3.7 Кубики (лист)

> [STYLE BLOCK] Subject: a sheet of six identical carved granite dice shown in two rows of three on a plain flat pure-white background. Each die is a cube seen from exactly the same slightly-above three-quarter angle, same size, same lighting, only the number of pips on the top face differs: one pip, two pips, three pips, four pips, five pips, six pips. The pips are round recesses filled with glowing warm amber light. Worn stone edges, bronze corner caps. No numerals anywhere — only round pips. [tech tail + negative] Aspect ratio 3:2.

### 3.8 Эмблема

> [STYLE BLOCK] Subject: a heraldic emblem for a fantasy wizard order: a vertically symmetrical bronze and gold seal shaped like a pointed shield, with an ornate arcane sigil in the centre and five gemstones set around it — azure, jade, amber, crimson, amethyst — each glowing softly. Ornamental scrollwork on the sides, a small banner shape at the bottom that is completely empty and blank. Perfectly symmetrical, iconic, readable at small size. [tech tail] Aspect ratio 1:1.

### 3.9 VFX — два листа

**Лист 1 (нейтральные элементы):**
> [STYLE BLOCK] Generate a sheet of eight separate magical visual-effect elements on a plain flat pure-black background, arranged in two rows of four, evenly spaced, each isolated with nothing touching: (1) a soft round radial glow, pure white, fading smoothly to nothing at the edges; (2) a multi-rayed star burst flare, white, symmetrical; (3) a thin circular ring of arcane runes glowing white, seen flat from above, the runes are abstract invented glyphs and not letters of any real alphabet; (4) a thin expanding shockwave ring, brightest at its outer edge; (5) a single small bright spark; (6) a small soft ash mote; (7) a soft translucent puff of smoke; (8) a vertical beam of light, brightest at the top, fading downward. All elements are white or very light grey so they can be tinted in the engine. Elements glow additively against pure black. No text, no numbers, no watermark, no frames. Aspect ratio 2:1.

> На чёрном фоне — потому что аддитивные эффекты чище вырезаются: чёрный становится прозрачным при `Blend: Add` либо через «умножение на яркость» в редакторе.

**Лист 2 (тематические элементы):**
> [STYLE BLOCK] Generate a sheet of six separate magical visual-effect elements on a plain flat pure-black background, in two rows of three, evenly spaced, each isolated: (1) a single heavy link of a glowing pale-azure ice chain; (2) a small glowing emerald leaf; (3) a single glowing amber ember with a faint trail; (4) a diagonal triple claw slash mark made of crimson light and smoke; (5) a circular rune-etched steel snare trap with open jaws seen from directly above, glowing violet between the teeth; (6) a jagged crack spreading across stone with warm light shining out of the crack. All glowing against pure black. No text, no numbers, no watermark. Aspect ratio 3:2.

---

## 4. Порядок работы

1. **Anchor** (карта F) → утвердить → сохранить.
2. **Карты** I, R, S, T листом → нарезать → допросить отдельные при недостатке качества.
3. **Рубашка**.
4. **Панели и кнопки** листом → нарезать → разметить 9-patch.
5. **Иконки** листом → нарезать.
6. **Слот, урна, колода**.
7. **Кубики** листом.
8. **Портреты и рама**.
9. **Эмблема**.
10. **Фоны** (4 шт.).
11. **VFX** два листа.

После каждой группы — вставить полученное в игру и посмотреть вживую. Оценивать ассет в отрыве от игры бессмысленно.

---

## 5. Борьба с типовыми проблемами

| Симптом | Что писать |
|---|---|
| В картуше/ленте появился «текст» из закорючек | `Regenerate the exact same image, but the rectangular plate at the top and the ribbon banner must be completely empty, smooth and blank — no engraving, no glyphs, no scribbles, no ornament inside them. Everything else stays identical.` |
| Стиль уехал от Anchor | `This is off-style. Match the attached reference exactly: same wood tone, same bronze patina, same light direction from the upper left, same brush texture, same colour grading. Redo.` |
| Карта нарисована под углом / в перспективе | `Show the card perfectly flat and straight on, orthographic, no perspective, no tilt, no rotation, no thickness visible.` |
| Рамка несимметричная | `Make the frame perfectly symmetrical left to right, mirrored around the vertical centre line.` |
| Фон не белый / есть тень до края | `Place the object on a completely plain flat pure-white background. The object must not touch the edges and must cast no shadow onto the background.` |
| Слишком «шумно», много мелочей | `Reduce detail density by half. Keep large readable shapes, remove micro-ornaments and clutter. The silhouette must read clearly at thumbnail size.` |
| Цвет акцента ушёл | `The accent glow and gemstone must be exactly this colour: #FFD195. Keep the wood and bronze neutral warm brown, unaffected by the accent colour.` |
| Разные размеры элементов на листе | `All items on the sheet must be exactly the same size and aligned on a strict grid with equal spacing.` |
| Появилась «лишняя рамка» вокруг всего изображения | `Do not draw any border or frame around the whole image. Only the object itself.` |

## 6. Учёт сгенерированного

Вести `assets_src/GENERATION-LOG.md`: имя ассета → дата → финальный промпт → номер попытки → замечания. Это спасает, когда через месяц нужно догенерировать одну карту в том же стиле.
