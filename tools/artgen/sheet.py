#!/usr/bin/env python3
"""Контактный лист из попыток: всё в одном масштабе, с подписями.

Нужен для приёмки глазами — метрики не показывают, «та же ли это колода».

    python3 tools/artgen/sheet.py out.png a.png=подпись b.png=подпись
    python3 tools/artgen/sheet.py out.png --cols 4 assets_src/raw/attempts/4.*try1.png
"""

from __future__ import annotations

import pathlib
import sys

from PIL import Image, ImageDraw, ImageFont

TILE = 620
PAD = 20
LABEL = 26

# Встроенный шрифт PIL — только латиница, подписи по-русски превращаются в квадраты.
FONT_PATH = pathlib.Path(__file__).resolve().parents[2] / "assets_src/fonts/PT_Sans-Web-Bold.ttf"


def label_font() -> ImageFont.ImageFont:
    if FONT_PATH.is_file():
        return ImageFont.truetype(str(FONT_PATH), 17)
    return ImageFont.load_default()


def main() -> None:
    args = [a for a in sys.argv[1:]]
    cols = 0
    if "--cols" in args:
        index = args.index("--cols")
        cols = int(args[index + 1])
        del args[index:index + 2]
    out = pathlib.Path(args[0])
    items = []
    for raw in args[1:]:
        path, _, label = raw.partition("=")
        file = pathlib.Path(path)
        if not file.is_file():
            print(f"пропущен: {path}")
            continue
        items.append((label or file.stem, Image.open(file).convert("RGB")))
    if not items:
        sys.exit("нечего собирать")

    cols = cols or len(items)
    rows = (len(items) + cols - 1) // cols
    scaled = []
    for label, image in items:
        ratio = TILE / max(image.width, image.height)
        scaled.append((label, image.resize(
            (max(1, int(image.width * ratio)), max(1, int(image.height * ratio))), Image.LANCZOS)))

    cell_w = max(t.width for _, t in scaled) + PAD
    cell_h = max(t.height for _, t in scaled) + PAD + LABEL
    sheet = Image.new("RGB", (cols * cell_w + PAD, rows * cell_h + PAD), (242, 242, 242))
    draw = ImageDraw.Draw(sheet)
    font = label_font()
    for index, (label, tile) in enumerate(scaled):
        col, row = index % cols, index // cols
        x = PAD + col * cell_w + (cell_w - PAD - tile.width) // 2
        y = PAD + row * cell_h + LABEL
        sheet.paste(tile, (x, y))
        draw.text((PAD + col * cell_w, PAD + row * cell_h + 4), label, fill=(25, 25, 25), font=font)
    sheet.save(out)
    print(f"{out} — {sheet.width}x{sheet.height}, плиток {len(scaled)}")


if __name__ == "__main__":
    main()
