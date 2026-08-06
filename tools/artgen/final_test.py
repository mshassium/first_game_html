#!/usr/bin/env python3
"""Финальный тест экрана из §10.7 бука, выполненный над мокапом.

Бук формулирует четыре проверки словами. Три из них можно провести механически,
получив одно изображение с четырьмя вариантами одного экрана:

  1. размытие до крупных пятен — первым должен читаться игровой контент, а не рамки;
  2. чёрно-белый режим — иерархия карты → ход/цель → панели → фон должна сохраниться;
  3. уменьшение до 960x540 — текстуры не должны превращаться в рябь;
  4. снятие цветов школ — интерфейс должен остаться цельным.

Скрипт не выносит вердикт: он готовит четыре картинки, по которым вердикт выносит глаз.

    python3 tools/artgen/final_test.py мокап.png тест.png
"""

from __future__ import annotations

import pathlib
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT = pathlib.Path(__file__).resolve().parents[2] / "assets_src/fonts/PT_Sans-Web-Bold.ttf"


def desaturate_schools(image: Image.Image) -> Image.Image:
    """Гасит только цветные пиксели, оставляя нейтральные как есть.

    Так проверяется четвёртый тест: если убрать акценты школ, экран должен
    остаться собранным, а не рассыпаться на невнятные тёмные прямоугольники.
    """
    result = image.convert("RGB").copy()
    pixels = result.load()
    for y in range(result.height):
        for x in range(result.width):
            r, g, b = pixels[x, y]
            top, bottom = max(r, g, b), min(r, g, b)
            if top and (top - bottom) / top > 0.25:
                grey = int(0.299 * r + 0.587 * g + 0.114 * b)
                pixels[x, y] = (grey, grey, grey)
    return result


def main() -> None:
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    source = Image.open(sys.argv[1]).convert("RGB")
    width = 760
    height = round(source.height * width / source.width)
    base = source.resize((width, height), Image.LANCZOS)

    panels = [
        ("исходный мокап", base),
        ("размытие: что читается первым", base.filter(ImageFilter.GaussianBlur(width / 55))),
        ("чёрно-белый: сохранилась ли иерархия", base.convert("L").convert("RGB")),
        ("960x540 и без цветов школ",
         desaturate_schools(source.resize((960, 540), Image.LANCZOS)).resize((width, height), Image.LANCZOS)),
    ]

    pad, label = 18, 26
    sheet = Image.new("RGB", (2 * width + 3 * pad, 2 * (height + label) + 3 * pad), (240, 240, 240))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype(str(FONT), 16) if FONT.is_file() else ImageFont.load_default()
    for index, (title, panel) in enumerate(panels):
        col, row = index % 2, index // 2
        x = pad + col * (width + pad)
        y = pad + row * (height + label + pad)
        draw.text((x, y), title, fill=(20, 20, 20), font=font)
        sheet.paste(panel, (x, y + label))
    sheet.save(sys.argv[2])
    print(f"{sys.argv[2]} — {sheet.width}x{sheet.height}")


if __name__ == "__main__":
    main()
