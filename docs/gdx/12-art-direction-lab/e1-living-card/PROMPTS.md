# E1 — ассеты для прототипа живой карты

Прототипу нужны настоящие картинки в новом стиле (якорь A1). Это не финальные ассеты —
финальные будут после E2 (буква-герой) через artgen; здесь — рабочие болванки для движения.

Рендер: `python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e1-living-card/PROMPTS.md --only CARD_F,CARD_S,CARD_BACK --aspect 2:3` и `--only MAT --aspect 16:9`.

### CARD_F — лицо карты F

```prompt
A single playing card face seen perfectly straight on, filling the entire image edge to edge — the image IS the card, no background, no table, no perspective, no shadow. Thick cream card stock with a soft paper texture and slightly worn corners, a thin painted dark-blue border line inset from the edge. ONE huge hand-lettered capital letter F painted in ice-blue gouache with visible brush strokes, filling about 65% of the card height, centred slightly above the middle. Below it, small and centred, a simple ice-blue padlock doodle. Painterly, chunky, warm, friendly — NOT photorealistic. No other text, no numbers, no watermark.
```

### CARD_S — лицо карты S

```prompt
A single playing card face seen perfectly straight on, filling the entire image edge to edge — the image IS the card, no background, no table, no perspective, no shadow. Thick cream card stock with a soft paper texture and slightly worn corners, a thin painted dark-red border line inset from the edge. ONE huge hand-lettered capital letter S painted in crimson gouache with visible brush strokes, filling about 65% of the card height, centred slightly above the middle. Below it, small and centred, a simple crimson shadowy-hand doodle. Painterly, chunky, warm, friendly — NOT photorealistic. No other text, no numbers, no watermark.
```

### CARD_BACK — рубашка

```prompt
The back of a playing card seen perfectly straight on, filling the entire image edge to edge — the image IS the card back, no background, no table, no perspective. Deep indigo-blue painted card stock with a hand-drawn cream-coloured geometric rune border and a small central emblem of five tiny stacked cards; slightly worn edges, visible brush texture. Painterly, chunky, warm — NOT photorealistic. No text, no letters, no watermark.
```

### MAT — коврик, вид сверху, пустой

```prompt
Top-down view, aspect ratio 16:9, of an empty dark neoprene tabletop playmat lying on a warm wooden table, seen almost perfectly straight from above with no perspective. The mat is plain dark charcoal-blue with a subtle painted rune pattern only along its border; the centre is calm and empty. A warm desk lamp out of frame at the top-right pools soft golden light across the mat with a gentle falloff toward the bottom-left. Along the very edges of the image the wooden table shows, with a d20 die at the bottom-left corner and the base of a tea mug at the top-right corner. Painterly, chunky, warm — NOT photorealistic. No text, no letters, no cards, no watermark.
```
