# E4 — предметы-эффекты для прототипа

Все на плоском чистом зелёном фоне (#00FF00) — вырезаются хромакеем `tools/artlab/keyout.py`.
Рендер: `python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e4-effects/PROMPTS.md --aspect 1:1`

### LOCK — замок-жетон (запрет F)
```prompt
A single chunky tabletop game token seen from directly above: a thick round wooden token base painted ice-blue on the rim, and on it a small hand-painted brass-and-iron padlock, closed, with a tiny keyhole; frost crystals painted on the base around the padlock. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TRAP — капкан-жетон (ловушка T)
```prompt
A single chunky tabletop game token seen from directly above: a thick round wooden token base painted violet on the rim, and on it a small hand-painted iron bear trap with open jaws and a short chain. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TRAP_CLOSED — капкан захлопнутый
```prompt
A single chunky tabletop game token seen from directly above: a thick round wooden token base painted violet on the rim, and on it a small hand-painted iron bear trap with its jaws SNAPPED SHUT, teeth interlocked, a short chain. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### PENNANT — вымпел на булавке (буква рисуется движком)
```prompt
A small hand-painted triangular pennant flag on a short brass pin, seen from directly above, lying flat: the flag is plain ice-blue cloth with a cream border, completely blank (no symbol, no letter), the pin has a small round head. Painterly, chunky, warm — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### HAND — «рука тени» для кражи S (полупрозрачная)
```prompt
A stylised painterly silhouette of a reaching open hand made of dark crimson smoke, seen from above, fingers spread, wisps trailing off the wrist, soft edges. Painterly — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no other objects, no text, no watermark.
```

## Раунд 3 — жетоны для I, R, S (единая грамматика: жетон делает всё)

### TOKEN_I — жетон прироста
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/LOCK_gpt-5.4-image-2_1.png
Match the attached token exactly in style, size and camera. A single chunky tabletop game token seen from directly above: a thick round wooden token base painted jade-green on the rim, and on it a small hand-painted young sprout with two leaves growing from a little mound of soil. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TOKEN_R — жетон возврата
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/LOCK_gpt-5.4-image-2_1.png
Match the attached token exactly in style, size and camera. A single chunky tabletop game token seen from directly above: a thick round wooden token base painted warm amber on the rim, and on it a small hand-painted glowing phoenix feather with a few embers. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TOKEN_S — жетон кражи
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/LOCK_gpt-5.4-image-2_1.png
Match the attached token exactly in style, size and camera. A single chunky tabletop game token seen from directly above: a thick round wooden token base painted crimson on the rim, and on it a small hand-painted grabbing hand silhouette in dark crimson, fingers curled. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```

## Раунд 4 — все жетоны в стиле капкана T (принято судьёй)

Референс — `out/TRAP_gpt-5.4-image-2_1.png`: толстая деревянная шайба с натуральной древесиной в центре, крашеный ободок цвета школы, предмет — металлический/расписной, лежит на дереве. Ободки: F лазурь, I нефрит, R янтарь, S багрянец.

### LOCK2 — замок F
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/TRAP_gpt-5.4-image-2_1.png
Match the attached token EXACTLY in style, material, size and camera: the same thick round wooden token with natural warm wood grain on its face and a painted rim — but here the rim is painted ICE BLUE, and lying on the wood is a small hand-painted brass-and-iron padlock, closed, with a tiny keyhole and a few painted frost crystals around it. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TOKI2 — росток I
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/TRAP_gpt-5.4-image-2_1.png
Match the attached token EXACTLY in style, material, size and camera: the same thick round wooden token with natural warm wood grain on its face and a painted rim — but here the rim is painted JADE GREEN, and lying on the wood is a small hand-painted young sprout with two leaves in a tiny clay pot. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TOKR2 — перо R
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/TRAP_gpt-5.4-image-2_1.png
Match the attached token EXACTLY in style, material, size and camera: the same thick round wooden token with natural warm wood grain on its face and a painted rim — but here the rim is painted WARM AMBER, and lying on the wood is a small hand-painted glowing orange phoenix feather with two tiny embers. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
### TOKS2 — рука S
```prompt
refs: docs/gdx/12-art-direction-lab/e4-effects/out/TRAP_gpt-5.4-image-2_1.png
Match the attached token EXACTLY in style, material, size and camera: the same thick round wooden token with natural warm wood grain on its face and a painted rim — but here the rim is painted CRIMSON, and lying on the wood is a small hand-painted iron grabbing claw / gauntlet hand with fingers curled, dark metal with crimson highlights. Painterly, chunky, warm, slightly worn — NOT photorealistic. Isolated, centred, on a flat pure bright green background (#00FF00), no shadow on the background, no other objects, no text, no watermark.
```
