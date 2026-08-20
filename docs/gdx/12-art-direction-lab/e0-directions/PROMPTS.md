# E0 — промпты концепт-бордов направлений

Каждый борд — **один и тот же экран боя** в одной и той же раскладке, чтобы сравнивать
стиль, а не композицию. Раскладка (общий блок, вставлен в каждый промпт):

- слева — два портрета (противник сверху, игрок снизу);
- по центру — две горизонтальные зоны SPACE по 5 слотов (верхняя противника, нижняя игрока), в них лежат по 2–3 карты;
- внизу — рука игрока из 5 карт веером;
- справа — колода и сброс;
- на картах — крупные буквы F, I, R, S, T (буква — главное на карте, иллюстрация — второстепенна).

Рендер: `python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e0-directions/PROMPTS.md`

---

### D1 — Светлое фэнтези-таверна (Hearthstone как он есть)

```prompt
Concept art of a complete game screen for a two-player fantasy card duel, aspect ratio 16:9, painterly digital illustration in the spirit of a warm, playful, family-friendly tavern card game. The whole screen is one cohesive painting.

Layout, strictly: on the left edge two round hero portraits in chunky carved wooden frames (an old bearded wizard at top, a young dark-haired mage at bottom). In the centre, two horizontal rows of five card slots carved into a bright honey-coloured oak table (opponent's row above, player's row below); each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a face-down draw pile and a discard pile.

Cards: portrait 2:3, thick rounded golden-bronze frames, and a HUGE ornate capital letter dominating each card — one of F, I, R, S, T — painted like an illuminated manuscript initial, glowing in its school colour (F icy blue, I jade green, R amber, S crimson, T violet). Beneath the letter a small painterly emblem: a padlock in chains for F, a sprouting cup for I, a phoenix feather for R, a shadowy hand for S, a rune trap for T.

Mood: cosy, bright, welcoming, saturated warm light like a tavern at golden hour, candles, mugs, a purring cat asleep on the table corner, wood grain, brass tacks, exaggerated chunky proportions, soft painterly brushwork, playful not grim. Every element is readable at a glance. No user interface text other than the five letters. No watermark.
```

### D2 — Кабинет типографа: буквы как физические предметы

```prompt
Concept art of a complete game screen for a two-player card duel about collecting the letters of the word FIRST, aspect ratio 16:9, painterly digital illustration. The setting is a cosy old printer-alchemist's study at night: a large ink-stained oak desk seen from above at a slight angle, a green-shaded brass lamp casting a warm pool of light, ink bottles, wax seals, a typesetter's tray of metal letters, scrolls, a steaming cup of tea.

Layout, strictly: on the left edge two round portraits set in brass locket frames (an elderly typesetter with spectacles at top, a young apprentice at bottom). In the centre, two horizontal rows of five rectangular recesses in the desk like a compositor's case (opponent's row above, player's row below), each holding 2–3 letter cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a stack of face-down cards and a discard tray.

Cards: portrait 2:3, made of thick cream letterpress card stock with deckled edges, each dominated by ONE huge embossed capital letter — F, I, R, S, T — printed with tactile ink and gilded, each letter in its own ink colour (F ice blue, I jade green, R amber, S crimson, T violet), with a small wax seal at the bottom bearing a tiny emblem (padlock, sprout, phoenix feather, shadow hand, rune trap). Letters look like real physical type: pressed, glossy ink, subtle relief.

Mood: intimate, warm, tactile, paper and brass and lamplight, quiet night work, gentle dust in the lamp beam. Everything readable at a glance. No user interface text other than the five letters. No watermark.
```

### D3 — Яркий мультяшный настольный (bold cartoon)

```prompt
Concept art of a complete game screen for a two-player card duel game, aspect ratio 16:9, bold stylised cartoon illustration with thick clean outlines, flat cel shading, saturated candy colours and chunky exaggerated shapes — the look of a modern mobile board game, cheerful and instantly readable on a small screen.

Layout, strictly: on the left edge two round character portraits with thick outlines (a grumpy old wizard at top, a grinning young apprentice at bottom). In the centre, two horizontal rows of five card slots on a friendly green felt table with rounded wooden edges (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a draw pile and a discard pile.

Cards: portrait 2:3, thick rounded frames with a bold coloured border per school, and one GIANT chunky capital letter filling most of the card — F, I, R, S, T — in a playful heavy display typeface with a dark outline and a bright highlight, letter colour per school (F ice blue, I jade green, R amber, S crimson, T violet), with a small cute icon peeking from behind the letter (padlock, sprout, phoenix feather, shadow hand, bear trap).

Mood: bright, cheerful, energetic, toy-like, cosy afternoon light, confetti-like sparkles, big friendly shapes, zero clutter. No user interface text other than the five letters. No watermark.
```

### D4 — Мистическая лавка таро: тёмный уют, золотая линия

```prompt
Concept art of a complete game screen for a two-player occult card duel, aspect ratio 16:9, elegant painterly illustration in the manner of a mysterious tarot reader's parlour: a round table draped in deep plum velvet, candlelight, dried herbs, a brass astrolabe, a black cat, warm dark cosy atmosphere, gold leaf accents.

Layout, strictly: on the left edge two round portraits in thin gold-line filigree frames (a veiled old fortune teller at top, a young curious visitor at bottom). In the centre, two horizontal rows of five card slots marked as gold-embroidered rectangles on the velvet (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a draw pile and a discard pile.

Cards: portrait 2:3, matte black card stock with fine gold-foil line-art borders like tarot arcana, each card dominated by ONE huge elegant capital letter — F, I, R, S, T — drawn in glowing gold-foil line art with a subtle coloured aura per school (F ice blue, I jade green, R amber, S crimson, T violet), and a small delicate line-art emblem below the letter (padlock in chains, sprouting cup, phoenix feather, shadow hand, rune trap).

Mood: intimate, mysterious yet cosy and warm, refined, minimal clutter, high contrast between gold lines and dark velvet, candle glow. Everything readable at a glance. No user interface text other than the five letters. No watermark.
```

---

## Раунд 2 — D5 «Стол гика» (после вердикта 18.08)

Судья: подача D1 (живопись, тёплый свет, толстые формы) — да, мир — стол настольщика, за которым F!RST играют между партиями в большие ККИ. Атрибутика — *отсылки*, не копии брендов: карты ККИ рубашкой вверх или размытые, «космический рыцарь» вместо конкретной фигурки, никаких логотипов. Общий блок стиля во всех трёх:

> painterly, chunky, warm — как D1; НЕ фотореализм (D2 отвергнут за это); буква на карте — гигантская.

### D5a — Кухонный стол дома, вечер после большой партии

```prompt
Concept art of a complete game screen for a quick two-player card game played on a real gamer's kitchen table between rounds of a long trading-card-game night, aspect ratio 16:9. Painterly digital illustration with warm chunky friendly stylisation, visible brushwork, exaggerated cosy proportions — NOT photorealistic, NOT 3D render.

Layout, strictly: on the left edge two round player portraits framed as enamel pins on a fantasy playmat (a bearded guy in a wizard-hat hoodie at top, a young woman with headphones at bottom). In the centre, a fantasy-art rubber playmat with two horizontal rows of five card-sized zones printed on it (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a face-down draw pile and a discard pile.

Cards: portrait 2:3, simple thick cream card stock in clear sleeves, each dominated by ONE huge hand-lettered capital letter — F, I, R, S, T — painted boldly in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the playmat, tabletop-gamer clutter as loving background references, no logos: stacks of face-down trading cards in sleeves, two deck boxes, scattered d20 and d6 dice, a spin-down life counter, a hand-painted armoured space-knight miniature standing guard, a mug of tea, a bowl of chips, a warm desk lamp pooling golden light. Mood: cosy Friday-night geek den, warm, lived-in, humorous. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

### D5b — Стол в игровом клубе / магазине настолок

```prompt
Concept art of a complete game screen for a quick two-player card game played at a table in a friendly local tabletop-game store, aspect ratio 16:9. Painterly digital illustration, warm chunky stylisation, visible brushwork, cosy exaggerated proportions — NOT photorealistic, NOT 3D render.

Layout, strictly: on the left edge two round player portraits shaped like collectible tokens (a grinning store regular with glasses at top, a focused kid in a dragon t-shirt at bottom). In the centre, a black neoprene playmat with a subtle glowing rune print, with two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a draw pile and a discard pile.

Cards: portrait 2:3, thick cream card stock, each dominated by ONE huge bold capital letter — F, I, R, S, T — painted in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the mat, the store's atmosphere as background: shelves of board-game boxes blurred behind, a rack of booster packs without logos, a painted fantasy miniature and a small dragon figure on the table edge, dice trays, deck boxes, a card binder, a tournament pairing sheet, warm pendant lamps overhead. Mood: welcoming geek clubhouse, warm amber light, buzzing but cosy. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

### D5c — Стол настольщика в фэнтези-таверне (гибрид с D1)

```prompt
Concept art of a complete game screen for a quick two-player card game, aspect ratio 16:9, painterly digital illustration in the warm playful chunky style of a fantasy tavern card game — but the table belongs to modern tabletop geeks who happen to be sitting in that tavern: their real-world hobby gear is scattered around. Visible brushwork, warm golden candle-and-lamp light, cosy exaggerated proportions — NOT photorealistic.

Layout, strictly: on the left edge two round hero portraits in chunky carved wooden frames (an old bearded wizard at top, a young mage at bottom). In the centre, a honey-oak tavern table with a fantasy playmat laid on it, two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a draw pile and a discard pile.

Cards: portrait 2:3, thick cream card stock with a thin worn gold edge, each dominated by ONE huge bold illuminated capital letter — F, I, R, S, T — in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small emblem beneath (padlock, sprout, phoenix feather, shadow hand, rune trap). The letter fills most of the card.

Around the mat, tabletop-gamer references mixed with tavern life, no logos: stacks of sleeved face-down trading cards, deck boxes, scattered d20 dice, a hand-painted armoured space-knight miniature next to a candle, a spin-down life counter, a tankard, a bowl of chips. Mood: warm, humorous, cosy — a geek's dream table inside a fantasy tavern. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

---

## Раунд 3 — посадка портретов в мире «гик-берлога» (после вердикта по D5)

Принято: D5a (домашний стол) + окружение с полками из D5b + портреты как предметы на столе (D5c). Общий мир для всех трёх:

> A cosy home game room at night: big wooden table, warm desk lamp, shelves of board-game boxes and painted miniatures softly blurred behind, gamer clutter without logos. Painterly, chunky, warm — NOT photoreal.

### P1 — Портреты в деревянных рамках, стоящих на столе

```prompt
Concept art of a complete game screen for a quick two-player card game on a tabletop geek's home game table, aspect ratio 16:9. Painterly digital illustration, warm chunky friendly stylisation, visible brushwork, exaggerated cosy proportions — NOT photorealistic, NOT 3D render.

Scene: a big wooden table in a cosy home game room at night, a warm desk lamp pooling golden light, and behind the table softly blurred shelves full of board-game boxes, painted miniatures and dice jars. On the table a dark plain neoprene playmat with a subtle rune print only along its edges (the centre is calm and dark).

Layout, strictly: on the left edge, two portraits of the players are small carved wooden standing picture frames placed on the table like desk photo frames (a bearded guy in a wizard-hat hoodie at top, a young woman with headphones at bottom), each frame with a tiny brass plate. In the centre of the mat, two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a face-down draw pile and a discard pile.

Cards: portrait 2:3, thick cream card stock in clear sleeves, each dominated by ONE huge hand-lettered capital letter — F, I, R, S, T — painted boldly in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the mat, gamer clutter as loving references, no logos: stacks of sleeved face-down trading cards, deck boxes, scattered d20 and d6 dice, a spin-down life counter, a hand-painted armoured space-knight miniature, a mug of tea, a bowl of chips. Mood: warm, humorous, lived-in Friday-night geek den. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

### P2 — Портреты как раскрашенные миниатюры игроков

```prompt
Concept art of a complete game screen for a quick two-player card game on a tabletop geek's home game table, aspect ratio 16:9. Painterly digital illustration, warm chunky friendly stylisation, visible brushwork, exaggerated cosy proportions — NOT photorealistic, NOT 3D render.

Scene: a big wooden table in a cosy home game room at night, a warm desk lamp pooling golden light, and behind the table softly blurred shelves full of board-game boxes, painted miniatures and dice jars. On the table a dark plain neoprene playmat with a subtle rune print only along its edges (the centre is calm and dark).

Layout, strictly: on the left edge, the two players are represented by two hand-painted tabletop miniatures standing on round bases at the mat's edge — a chunky caricature miniature of a bearded guy in a wizard-hat hoodie at top, and of a young woman with headphones at bottom — each mini lit by the lamp and casting a small shadow. In the centre of the mat, two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a face-down draw pile and a discard pile.

Cards: portrait 2:3, thick cream card stock in clear sleeves, each dominated by ONE huge hand-lettered capital letter — F, I, R, S, T — painted boldly in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the mat, gamer clutter as loving references, no logos: stacks of sleeved face-down trading cards, deck boxes, scattered d20 and d6 dice, a spin-down life counter, a hand-painted armoured space-knight miniature, a mug of tea, a bowl of chips. Mood: warm, humorous, lived-in Friday-night geek den. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

### P3 — Портреты как жетоны в карманах коврика

```prompt
Concept art of a complete game screen for a quick two-player card game on a tabletop geek's home game table, aspect ratio 16:9. Painterly digital illustration, warm chunky friendly stylisation, visible brushwork, exaggerated cosy proportions — NOT photorealistic, NOT 3D render.

Scene: a big wooden table in a cosy home game room at night, a warm desk lamp pooling golden light, and behind the table softly blurred shelves full of board-game boxes, painted miniatures and dice jars. On the table a dark plain neoprene playmat with a subtle rune print only along its edges (the centre is calm and dark).

Layout, strictly: on the left edge, the two players are represented by two thick round painted cardboard player tokens lying flat in two round token slots printed on the playmat — a token with a bearded guy in a wizard-hat hoodie at top, and a token with a young woman with headphones at bottom — each token with a chunky coloured rim. In the centre of the mat, two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out. On the right, a face-down draw pile and a discard pile.

Cards: portrait 2:3, thick cream card stock in clear sleeves, each dominated by ONE huge hand-lettered capital letter — F, I, R, S, T — painted boldly in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the mat, gamer clutter as loving references, no logos: stacks of sleeved face-down trading cards, deck boxes, scattered d20 and d6 dice, a spin-down life counter, a hand-painted armoured space-knight miniature, a mug of tea, a bowl of chips. Mood: warm, humorous, lived-in Friday-night geek den. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

---

## Раунд 4 — якорь стиля и тизер эффектов

Все решения р.1–3 в одном промпте: камера почти сверху, карты противника «лицом к игроку», миниатюры на толстых жетонах-подставках, спокойный коврик, лоток сброса, атрибутика без брендов.

### A1 — Якорь: финальный стол «гик-берлога»

```prompt
Concept art of a complete game screen for a quick two-player card game on a tabletop geek's home game table, aspect ratio 16:9. Painterly digital illustration, warm chunky friendly stylisation, visible brushwork, exaggerated cosy proportions — NOT photorealistic, NOT 3D render.

Camera: almost straight top-down, orthographic feel, only a very slight tilt — both rows of cards must be equally large and equally readable, no perspective foreshortening on the far row. All cards, including the opponent's, are oriented upright towards the viewer.

Scene: a big wooden table in a cosy home game room at night, a warm desk lamp at the top-right pooling golden light onto the mat, and along the top edge softly blurred shelves full of board-game boxes, painted miniatures and dice jars. On the table a dark plain neoprene playmat with a subtle rune print only along its border — the centre is calm and dark so cards pop.

Layout, strictly: on the left, the two players are two hand-painted chunky caricature miniatures standing on thick round painted cardboard token bases with a bold coloured rim (opponent — a bearded guy in a wizard-hat hoodie — at top with a blue rim; player — a young woman with headphones — at bottom with a green rim); the miniatures are large, about 1.3 card heights tall. In the centre of the mat, two horizontal rows of five card zones (opponent's row above, player's row below), each row holds 2–3 played cards. Along the bottom edge, the player's hand of five cards fanned out, slightly larger. On the right, a face-down draw pile and, below it, a wooden dice-tray used as the discard tray with a couple of cards in it.

Cards: portrait 2:3, thick cream card stock in clear sleeves, each dominated by ONE huge hand-lettered capital letter — F, I, R, S, T — painted boldly in its school colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle emblem beneath (padlock, sprout, phoenix feather, shadow hand, bear trap). The letter fills most of the card.

Around the mat, gamer clutter as loving references, no logos: stacks of sleeved face-down trading cards, deck boxes, scattered d20 and d6 dice, a spin-down life counter, a hand-painted armoured space-knight miniature and a green orc-chief miniature on the shelf edge, a mug of tea, a bowl of chips. Mood: warm, humorous, lived-in Friday-night geek den. Everything readable at a glance. No user interface text other than the five letters. No watermark, no brand names.
```

### FX — Тизер эффектов: запрет F и сброс

```prompt
Concept art, close-up detail of a tabletop card game moment on a dark neoprene playmat under a warm desk lamp, aspect ratio 16:9. Painterly digital illustration, warm chunky friendly stylisation, visible brushwork — NOT photorealistic, NOT 3D render. Camera almost straight top-down.

Left half: an opponent's card zone on the mat holding a cream sleeved card with a huge ice-blue hand-lettered capital letter F and a small padlock doodle; on top of that zone a chunky physical FORBID token has just been snapped shut — a small brass-and-iron padlock token with a frosty ice-blue rim, tiny frost crystals spreading on the mat around it, a soft cold glow. Next to it a small blue pennant token showing the forbidden letter.

Right half: a wooden dice tray used as the discard tray, and a cream sleeved card with a huge crimson capital letter S mid-motion sliding into the tray, slightly tilted, with a soft motion smear and two small dust puffs where it lands; two other cards already lie in the tray. A d20 and a mug edge in the corner for scale.

Mood: warm, tactile, humorous, everything readable at a glance. No user interface text other than the letters F and S. No watermark, no brand names.
```
