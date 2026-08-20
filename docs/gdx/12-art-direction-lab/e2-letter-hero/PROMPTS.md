# E2 — компоновки карты: буква как герой

Каждый вариант — один лист 3:2 с пятью картами F I R S T в ряд на чистом чёрном фоне (потом режется
на отдельные карты для тестов «рука из 7 на 960×540» и «стопка ×N»). Общая база: кремовый картон в
протекторе, гуашь, живопись, цвета школ (F лазурь, I нефрит, R янтарь, S багрянец, T аметист).

Рендер: `python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e2-letter-hero/PROMPTS.md --aspect 3:2`

### C1 — База E1: гигантская буква + дудл-эмблема внизу

```prompt
A row of five playing cards seen perfectly straight on, evenly spaced with clear gaps, on a pure black background, aspect ratio 3:2, no table, no perspective, no shadows. Each card is portrait 2:3, thick cream card stock with soft paper texture and slightly worn corners, a thin painted border line inset from the edge in the card's own colour. Each card is dominated by ONE huge hand-lettered capital letter painted in gouache with visible brush strokes, filling about 65% of the card height, centred slightly above the middle; below it a small simple doodle emblem in the same colour. Left to right: F in ice blue with a padlock doodle; I in jade green with a sprout doodle; R in warm amber with a phoenix feather doodle; S in crimson with a shadowy hand doodle; T in violet with a bear-trap doodle. Painterly, chunky, warm — NOT photorealistic. No other text, no numbers, no watermark.
```

### C2 — Буква с тёмной обводкой, эмблема внутри буквы

```prompt
A row of five playing cards seen perfectly straight on, evenly spaced with clear gaps, on a pure black background, aspect ratio 3:2, no table, no perspective, no shadows. Each card is portrait 2:3, thick cream card stock with soft paper texture and slightly worn corners, no border line. Each card is dominated by ONE huge heavy hand-lettered capital letter painted in saturated gouache with a bold dark-brown ink outline and a soft inner highlight, filling about 75% of the card height, centred; a small emblem is painted INSIDE the letter's body as a darker silhouette. Left to right: F in deep ice blue with a padlock silhouette; I in jade green with a sprout silhouette; R in warm amber-orange with a phoenix feather silhouette; S in crimson with a shadowy hand silhouette; T in violet with a bear-trap silhouette. Painterly, chunky, warm, high contrast — NOT photorealistic. No other text, no numbers, no watermark.
```

### C3 — Цветное лицо школы, кремовая буква

```prompt
A row of five playing cards seen perfectly straight on, evenly spaced with clear gaps, on a pure black background, aspect ratio 3:2, no table, no perspective, no shadows. Each card is portrait 2:3, thick card stock whose whole face is painted in the school colour with visible gouache brush texture and slightly worn cream edges showing the card stock. Each card is dominated by ONE huge hand-lettered capital letter painted in thick cream-white gouache with a soft shadow, filling about 65% of the card height, centred slightly above the middle; below it a small cream doodle emblem. Left to right: ice-blue card with F and a padlock doodle; jade-green card with I and a sprout doodle; amber card with R and a phoenix feather doodle; crimson card with S and a shadowy hand doodle; violet card with T and a bear-trap doodle. Painterly, chunky, warm — NOT photorealistic. No other text, no numbers, no watermark.
```

### C4 — Буква во всю карту + угловой индекс для стопок и веера

```prompt
A row of five playing cards seen perfectly straight on, evenly spaced with clear gaps, on a pure black background, aspect ratio 3:2, no table, no perspective, no shadows. Each card is portrait 2:3, thick cream card stock with soft paper texture and slightly worn corners, a wide painted band in the school colour along the top edge. Each card is dominated by ONE huge hand-lettered capital letter painted in gouache filling about 80% of the card height, centred; in the top-left corner, small, the same letter repeated as a tiny index inside the coloured band, and a tiny doodle emblem in the bottom-right corner. Left to right: F in ice blue with a padlock; I in jade green with a sprout; R in warm amber with a phoenix feather; S in crimson with a shadowy hand; T in violet with a bear trap. Painterly, chunky, warm — NOT photorealistic. No other text, no numbers, no watermark.
```
