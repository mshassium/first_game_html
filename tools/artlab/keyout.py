#!/usr/bin/env python3
"""Хромакей: вырезает объект с плоского зелёного фона в PNG с альфой.
    python3 tools/artlab/keyout.py docs/gdx/12-art-direction-lab/e4-effects   # все out/*.png → props/*.png
"""
import glob, pathlib, sys
from PIL import Image
folder = pathlib.Path(sys.argv[1]).resolve(); (folder / "props").mkdir(exist_ok=True)
KEY = sys.argv[2] if len(sys.argv) > 2 else "green"   # green | magenta — для зелёных предметов фон пурпурный
for f in sorted(glob.glob(str(folder / "out" / "*.png"))):
    im = Image.open(f).convert("RGB"); px = im.load(); w, h = im.size
    out = Image.new("RGBA", (w, h)); op = out.load()
    for y in range(h):
        for x in range(w):
            r, g, b = px[x, y]
            if KEY == "magenta":
                green = min(r, b) - g           # насколько пиксель «пурпурнее» остального
            else:
                green = g - max(r, b)           # насколько пиксель «зелёнее» остального
            if green > 90: a = 0
            elif green > 30: a = int(255 * (1 - (green - 30) / 60))
            else: a = 255
            if a < 255:                          # убрать ореол фона
                if KEY == "magenta":
                    k = max(0, min(r, b) - g - 10); op[x, y] = (max(0, r - k), g, max(0, b - k), a)
                else:
                    g2 = min(g, max(r, b) + 10); op[x, y] = (r, g2, b, a)
            else:
                op[x, y] = (r, g, b, 255)
    bbox = out.getbbox(); out = out.crop(bbox)
    name = pathlib.Path(f).name.split("_gpt")[0]
    out.save(folder / "props" / f"{name}.png"); print("✓", name, out.size)
