# E8 — «Единый холст»: вся сцена партии одной картиной, из неё режутся ассеты

Референс сочетаемости — `refs/judge_P2_cohesion.png` (P2 из E0, выбран судьёй как эталон: «каждый элемент идеально сочетается с остальным»).
Рендер: `python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e8-one-canvas/PROMPTS.md --aspect 16:9 --n 2`

### M1 — мастер-холст: партия в нашей раскладке

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/refs/judge_P2_cohesion.png
Paint EXACTLY in the style, palette, lighting and level of detail of the attached reference — the same painterly tabletop-geek scene — but with this precise layout, aspect ratio 16:9, camera almost straight top-down (slight tilt only):

A large dark neoprene playmat with a subtle rune border fills the middle of a wooden table; the mat is calm and dark in the centre. Around the mat, the same cosy clutter as the reference: a brass desk lamp at the top-left, sleeved card stacks and deck boxes at the top, a painted space-knight miniature, jars of dice, a mug, a bowl of chips, a spiral notebook with a pencil, scattered dice — nothing on the mat itself except what is listed below.

On the mat, left side: two hand-painted caricature miniatures on round bases — at the top a bearded mage in a wizard-hat hoodie (base rim painted ICE BLUE), at the bottom a young woman with headphones and a purple hoodie (base rim painted JADE GREEN). Right of each miniature, printed on the mat, two small empty circles in a vertical column (token sockets). Under the top miniature's base sits a small brass hourglass on a wooden token.

In the centre of the mat, two horizontal rows of five card-sized rectangles printed on the mat (opponent's row above, player's row below). The top row holds, left to right, three cards then two empty rectangles; the bottom row holds, left to right, two empty rectangles then three cards. The five cards on the rows together show all five letters exactly once: top row F, I, R; bottom row S, T. Each card: portrait 2:3, thick cream card stock in a clear sleeve, ONE huge hand-painted brush-stroke capital letter in its colour (F ice blue, I jade green, R amber, S crimson, T violet) with a small doodle beneath (padlock, sprout, feather, shadowy hand, bear trap) — exactly like the reference cards. Along the bottom edge, the player's hand: five cards fanned, letters F I R S T.

Right of the rows: a face-down deck (dark indigo back with a cream rune emblem) at the height of the top row and a second face-down deck at the height of the bottom row; a small wooden tray with two discarded cards above the top deck. Further right, on the wood: a tall lit candle in a brass holder near the top, and a large round brass button on a wooden disc near the bottom. A small cream poker chip lies beside the bottom deck.

In the top-left token socket of the opponent lies a round wooden token with a painted padlock (ice-blue rim) with a tiny blue pennant flag pinned beside it; in the top socket of the player lies a wooden token with a small iron bear trap (violet rim).

Everything readable at a glance, no text other than the card letters, no watermark, no logos.
```

### M3 — мастер-холст без руки (для нарезки)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/refs/judge_P2_cohesion.png, docs/gdx/12-art-direction-lab/e8-one-canvas/out/M1_gpt-5.4-image-2_2.png
Repaint the SECOND attached image (same scene, same style, same palette, same lighting, same camera, same objects in the same places) with exactly these changes and nothing else: (1) the fanned hand of five cards along the bottom edge is REMOVED — the bottom of the mat and the wooden table edge below it are empty, showing only the mat border and the notebook, pencil and dice that are already there; (2) all five letter cards remain on the two printed rows exactly as they are (top row F, I, R; bottom row S, T), each clearly separate; (3) keep both face-down decks, the small wooden discard tray with two cards, the two miniatures on round bases, the hourglass, the padlock token with the tiny pennant in the opponent's top socket, the bear-trap token in the player's top socket, the candle, the brass button, the poker chip. Aspect ratio 16:9. No text other than card letters, no watermark.
```

### M4 — мастер-холст без фигурок и жетонов (плита раунда 2)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M3_gpt-5.4-image-2_1.png
Repaint the attached image — same scene, same painterly style, same palette, same lighting, same camera, every object in exactly the same place — with these changes and nothing else:
(1) the two hand-painted miniatures on round bases at the left of the playmat are REMOVED; that area of the mat is empty dark neoprene, and the printed circular token sockets stay exactly where they are;
(2) the round wooden token with a padlock and the small blue pennant flag in the upper socket is REMOVED — that socket is empty;
(3) the round wooden token with the bear trap in the lower socket is REMOVED — that socket is empty;
(4) the brass hourglass is REMOVED from the mat.
Everything else stays untouched and identical: the five letter cards on the two printed rows (top row F, I, R; bottom row S, T), both face-down decks on the right, the wooden discard tray with two cards, the brass desk lamp, the lit candle, the mug, the bowl of chips, the scattered dice, the spiral notebook with pencil, the deck boxes and the space-knight miniature along the far edge, the printed rune border of the mat.
Aspect ratio 16:9. No text other than the card letters, no watermark, no logos.
```

### M5 — мастер-холст с фигурками E3, нарисованными внутри картины

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M4_gpt-5.4-image-2_1.png, docs/gdx/12-art-direction-lab/e3-living-table/props/TOP_OPP.png, docs/gdx/12-art-direction-lab/e3-living-table/props/TOP_ME.png
Repaint the FIRST attached image — same scene, same painterly style, same palette, same warm desk-lamp lighting, same camera almost straight above, every object in exactly the same place — adding only two hand-painted tabletop miniatures standing on the left part of the dark playmat, left of the printed circular token sockets:
(1) the upper one, level with the top card row: the hooded mage from the SECOND attached image — a slim figure in a deep blue hooded cloak with grey trim and a round silver clasp, glowing pale-blue crystal shards floating over the open palm, blue tattooed forearm, brown boots, standing on a round base of wooden planks with an ICE-BLUE painted rim;
(2) the lower one, level with the bottom card row and slightly larger because it is nearer the camera: the red-bearded dwarf from the THIRD attached image — braided ginger beard, green sleeves, brown leather apron with a belt and a big iron key, raising a wooden tankard of foaming ale, standing on a round base of wooden planks with a JADE-GREEN painted rim.
Paint both miniatures as painted plastic models photographed on this very table: lit by the same warm lamp from the upper left, same soft painterly brushwork and level of detail as the rest of the scene, each about 1.3 card widths tall, seen from the same steep top-down angle as everything else, casting the same soft shadow on the mat. Do not change anything else in the picture.
Aspect ratio 16:9. No text other than the card letters, no watermark, no logos.
```

### M6 — мастер-холст с исходными фигурками, без жетонов и часов

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M3_gpt-5.4-image-2_1.png
Repaint the attached image — same scene, same painterly style, same palette, same warm desk-lamp lighting, same camera, every object in exactly the same place — with these changes and nothing else:
(1) the brass hourglass on the mat is REMOVED;
(2) the round wooden token with a padlock and the small blue pennant flag is REMOVED — its printed circular socket stays empty;
(3) the round wooden token with the bear trap is REMOVED — its printed socket stays empty.
KEEP the two hand-painted miniatures on round bases exactly as they are and exactly where they are: the bearded mage in a wizard-hat hoodie on a base with an ICE-BLUE rim, and the young woman with headphones in a purple hoodie on a base with a JADE-GREEN rim. Keep the five letter cards on the printed rows (top row F, I, R; bottom row S, T), both face-down decks, the wooden discard tray with two cards, the lamp, candle, mug, bowl of chips, dice, spiral notebook with pencil, deck boxes and the space-knight miniature along the far edge, and the printed rune border and empty token sockets of the mat.
Aspect ratio 16:9. No text other than the card letters, no watermark, no logos.
```

### M7 — «инвентарь»: предметы интерфейса, разложенные на том же столе (для нарезки)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M4_gpt-5.4-image-2_1.png
Repaint the attached image — same table, same painterly style, same palette, same warm desk-lamp lighting from the upper left, same camera almost straight above — with the playmat COMPLETELY EMPTY: no cards, no tokens, no miniatures, no hourglass, no decks and no discard tray on the mat.
Lying alone on the empty mat, well separated from each other, seen from straight above and lit by the same lamp:
(1) in the middle-left of the mat, an OPEN spiral-bound pocket notepad, both pages blank cream paper with faint ruled lines, a metal spiral along the fold, corners softly bent from use — about twice as wide as one of the letter cards used to be;
(2) to its right, a round punch-board token of wood and thick card, the size of a large coin, with a thin engraved rune ring near the rim and a completely EMPTY smooth centre;
(3) to the right of the token, a cream poker chip with a worn indigo rim pattern and an empty smooth centre, the size of a real chip.
Everything around the mat stays exactly as in the reference: lamp, candle, mug, bowl of chips, dice, notebook and pencil, deck boxes, the space-knight miniature and the wooden table.
Aspect ratio 16:9. No text, no letters, no numbers anywhere, no watermark, no logos.
```

### M8 — «инвентарь-2»: жетоны пяти эффектов на том же столе

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M6_gpt-5.4-image-2_1.png, docs/gdx/12-art-direction-lab/e4-effects/props/TRAP.png
Repaint the FIRST attached image — same table, same painterly style, same palette, same warm desk-lamp lighting from the upper left, same camera almost straight above — with the playmat COMPLETELY EMPTY: no cards, no miniatures, no decks, no discard tray, nothing but the bare dark mat with its printed rune border.
Lying alone on the empty mat in one row, well separated from each other, seen from straight above:
five round tokens of dark walnut wood, each the size of a large coin, each with a painted metal rim in its own colour and a small sculpted object resting on its face — exactly in the manner of the SECOND attached image (a wooden token with an iron bear trap on it):
(1) ICE-BLUE rim — a small iron padlock, closed;
(2) JADE-GREEN rim — a young sprout with two leaves pushing out of dark soil;
(3) AMBER rim — a single quill feather;
(4) CRIMSON rim — a small shadowy hand reaching up;
(5) VIOLET rim — an iron bear trap with open jaws.
Same worn painted-model finish and the same soft shadow on the mat for every token, all five the same size.
Everything around the mat stays exactly as in the reference: lamp, candle, mug, bowl of chips, dice, notebook and pencil, deck boxes, the space-knight miniature and the wooden table.
Aspect ratio 16:9. No text, no letters, no numbers anywhere, no watermark, no logos.
```

### P1 — портретный мастер-холст (телефон)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M6_gpt-5.4-image-2_1.png
Paint the SAME scene as the attached reference — the same geek's table, the same painterly style, the same palette, the same warm desk lamp glowing from the upper left, the same camera almost straight down onto the table — but recomposed for a TALL VERTICAL frame, aspect ratio 9:16.
A dark neoprene playmat with a printed rune border fills the middle of the vertical frame. On the mat, printed in pale line work: an upper row of five card-sized rectangles (the opponent's) and, below it, a lower row of five card-sized rectangles (the player's); to the left of each row a column of two small printed circles (token sockets).
Left of the upper row stands a hand-painted miniature of a bearded mage in a wizard-hat hoodie on a round base with an ICE-BLUE rim; left of the lower row a miniature of a young woman with headphones in a purple hoodie on a base with a JADE-GREEN rim.
On the right edge of the mat: two face-down decks with a cream rune emblem on dark indigo backs, one level with each row, and a small wooden discard tray with two cards above the upper deck.
Above the mat, along the top of the frame, the far edge of the table: a brass desk lamp leaning in from the upper left, stacked sleeved card boxes, jars of dice, a painted space-knight miniature, a lit candle.
Below the mat, along the bottom of the frame, the near edge of the table: bare wood with a spiral notebook and pencil, a mug of tea, a bowl of chips, scattered dice and a large round brass button on a wooden disc.
The mat is EMPTY — no cards, no tokens, no hourglass on it.
Aspect ratio 9:16. No text, no letters, no numbers anywhere, no watermark, no logos.
```

### M9 — мастер-холст в светлом игровом клубе (по мотивам D5b)

```prompt
refs: docs/gdx/12-art-direction-lab/e0-directions/out/D5b_gpt-5.4-image-2_1.png
Painterly digital illustration in exactly the manner of the attached reference: warm chunky stylisation, visible brushwork, cosy exaggerated proportions, NOT photorealistic, NOT a 3D render.
A table in a friendly local tabletop-game store, and the whole scene is BRIGHT AND AIRY: honey-toned oak table, daylight from a shop window mixing with warm pendant lamps overhead, light walls, no dark corners, no heavy vignette.
The camera looks almost straight DOWN onto the table, only a slight tilt, so the tabletop fills the frame and the shop is a soft blurred band along the top edge: shelves of colourful board-game boxes, a rack of booster packs, other players at their tables, hanging lamps.
On the table, filling the middle of the frame, a dark neoprene playmat with a subtle glowing rune border. Printed on the mat in pale line work: an upper row of five card-sized rectangles (the opponent's) and a lower row of five card-sized rectangles (the player's); to the left of each row a vertical column of two small printed circles (token sockets). The mat is COMPLETELY EMPTY — no cards, no tokens, nothing lying on it.
Left of the mat, standing on the table, two hand-painted tabletop miniatures on round bases: at the top a bearded mage in a wizard-hat hoodie on a base with an ICE-BLUE rim, at the bottom a young woman with headphones in a purple hoodie on a base with a JADE-GREEN rim.
Right of the mat: two face-down decks with a cream rune emblem on dark indigo backs, one level with each row, and a shallow wooden discard tray above the upper deck.
Around the mat, the store's clutter: an open dice tray with bright polyhedral dice, a couple of deck boxes, a card binder, a small red dragon figure, a spiral notepad with a pen, a paper cup of coffee, a bowl of snacks.
Aspect ratio 16:9. No text, no letters, no numbers anywhere, no logos, no watermark.
```

### M10 — карты F·I·R·S·T крупно на светлом столе (инвентарный кадр)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M9_gpt-5.4-image-2_3.png, docs/gdx/12-art-direction-lab/e2-letter-hero/out/C1F_gpt-5.4-image-2_1.png
Painterly digital illustration in exactly the style, palette and lighting of the FIRST attached image — the same bright tabletop-game store, the same honey-toned oak table, the same daylight plus warm pendant lamps, the same visible brushwork.
The camera looks straight DOWN at the empty dark playmat, close in, so that the frame is filled by SIX large playing cards lying flat in a row on the mat, well separated from each other, none of them overlapping, all the same size and all seen from directly above without perspective distortion.
Five of them are face-up letter cards, drawn exactly like the SECOND attached image: portrait 2:3, thick cream card stock with a thin gold corner ornament, dominated by ONE huge hand-painted brush-stroke capital letter filling most of the card, with a small painted emblem beneath it —
F in ice blue with a padlock, I in jade green with a sprout, R in amber with a quill feather, S in crimson with a shadowy hand, T in violet with a bear trap.
The sixth card is face-DOWN: dark indigo back with a cream rune compass emblem and a thin cream border.
Each card casts a soft short shadow on the mat. No other objects in the frame.
Aspect ratio 16:9. No text other than the five letters, no numbers, no logos, no watermark.
```

### M11 — жетоны пяти эффектов на светлом столе

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M9_gpt-5.4-image-2_3.png, docs/gdx/12-art-direction-lab/e4-effects/props/TRAP.png
Painterly digital illustration in exactly the style, palette and lighting of the FIRST attached image — the same bright tabletop-game store, the same honey-toned oak table, daylight plus warm pendant lamps, visible brushwork.
The camera looks straight DOWN at the empty dark playmat, close in. Lying alone on the mat in one row, well separated, all the same size, seen from directly above:
five round tokens of dark walnut wood, each the size of a large coin, each with a painted metal rim in its own colour and a small sculpted object resting on its face — in the manner of the SECOND attached image (a wooden token with an iron bear trap):
(1) ICE-BLUE rim — a closed iron padlock; (2) JADE-GREEN rim — a young sprout with two leaves in dark soil; (3) AMBER rim — a single quill feather; (4) CRIMSON rim — a small shadowy hand reaching up; (5) VIOLET rim — an iron bear trap with open jaws.
Same worn painted-model finish and the same soft shadow on the mat for every token. No other objects in the frame.
Aspect ratio 16:9. No text, no letters, no numbers, no logos, no watermark.
```

### M12 — предметы интерфейса на светлом столе

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M9_gpt-5.4-image-2_3.png
Painterly digital illustration in exactly the style, palette and lighting of the attached image — the same bright tabletop-game store, the same honey-toned oak table, daylight plus warm pendant lamps, visible brushwork.
The camera looks straight DOWN at the empty dark playmat, close in. Lying alone on the mat, well separated from each other, seen from directly above:
(1) on the left, an OPEN spiral-bound pocket notepad, both pages blank cream paper with faint ruled lines, a metal spiral along the fold, corners softly bent from use — about twice as wide as a playing card;
(2) in the middle, a round punch-board token of wood and thick card, the size of a large coin, with a thin engraved rune ring near the rim and a completely EMPTY smooth centre;
(3) on the right, a shallow wooden discard tray, empty, seen from directly above, its inner felt visible.
No other objects in the frame.
Aspect ratio 16:9. No text, no letters, no numbers, no logos, no watermark.
```

### M13 — та же сцена, коврик без печатной разметки (плита для движка)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M9_gpt-5.4-image-2_3.png
Repaint the attached image — same scene, same bright tabletop-game store, same painterly style, same palette, same lighting, same camera, every object in exactly the same place — with one change and nothing else: the playmat is now COMPLETELY BLANK. Remove all printed line work from the mat: the ten card-sized rectangles and the four small circles are gone. Keep the mat's outer rune border, its corner ornaments and its dark neoprene texture exactly as they are.
Everything else stays untouched and identical: the two miniatures on round bases at the left, the two face-down decks and the wooden tray at the right, the dice tray, deck boxes, card binder, notepad and pen, coffee cup, snack bowl, the red dragon figure, the honey-toned oak table and the blurred store behind.
Aspect ratio 16:9. No text, no letters, no numbers, no logos, no watermark.
```
