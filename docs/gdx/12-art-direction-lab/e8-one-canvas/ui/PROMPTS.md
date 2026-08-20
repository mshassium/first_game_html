# E8 · UI-предметы к единому холсту

Предметы, которых нет на мастер-холсте, но которые нужны интерфейсу. Рисуются **по референсу холста**
(тот же свет, та же манера, камера почти строго сверху) на плоском зелёном фоне под хромакей:

    python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e8-one-canvas/ui/PROMPTS.md --aspect 1:1 --n 2
    python3 tools/artlab/keyout.py  docs/gdx/12-art-direction-lab/e8-one-canvas/ui

### NOTEPAD — блокнот счёта (меню и итог партии)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M4_gpt-5.4-image-2_1.png
A single object on a FLAT PURE GREEN background (chroma key, RGB 0,255,0), nothing else in frame, no shadow on the background.
Painted in exactly the style, palette and lighting of the attached reference tabletop scene: warm desk lamp from the upper left, soft painterly brushwork, slightly worn real objects.
The object: an open spiral-bound pocket notepad lying flat on the table, seen from almost straight above with only a slight tilt, both pages visible, the left page blank cream paper with faint ruled lines, the right page blank, a metal spiral along the fold, corners softly bent from use, a couple of faint pencil smudges. No text, no letters, no numbers anywhere.
```

### PUNCH — деревянный жетон-выбивка (выбор буквы)

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M4_gpt-5.4-image-2_1.png
A single object on a FLAT PURE GREEN background (chroma key, RGB 0,255,0), nothing else in frame, no shadow on the background.
Painted in exactly the style, palette and lighting of the attached reference tabletop scene: warm desk lamp from the upper left, soft painterly brushwork.
The object: one round punch-board token of thick cardboard-and-wood, about the size of a large coin, lying flat, seen from straight above, plain cream-and-walnut face with a thin engraved rune ring near the rim and an EMPTY smooth centre (no symbol, no letter, no number at all), rough punched edge, slight bevel, painted like the wooden tokens in the reference.
```

### CHIP — фишка-счётчик

```prompt
refs: docs/gdx/12-art-direction-lab/e8-one-canvas/out/M4_gpt-5.4-image-2_1.png
A single object on a FLAT PURE GREEN background (chroma key, RGB 0,255,0), nothing else in frame, no shadow on the background.
Painted in exactly the style, palette and lighting of the attached reference tabletop scene: warm desk lamp from the upper left, soft painterly brushwork.
The object: one small cream poker chip with a worn indigo rim pattern, lying flat, seen from straight above, smooth EMPTY centre with no symbol and no number, faint scuffs on the surface, exactly like the poker chip lying on the table in the reference but larger and cleanly lit.
```
