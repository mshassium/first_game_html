#!/usr/bin/env python3
"""Нарезка листа по ячейкам сетки, а не по связным областям.

`SheetSlicer.kt` ищет связные области и для большинства листов это правильно.
Но элемент вроде рунического кольца по заданию состоит из разрозненных глифов
и распадается на дюжину кусков, а мягкое свечение вообще не имеет чёткой границы.
Раскладка таких листов задана промптом жёстко — «two rows of four», — поэтому
резать их по сетке точнее и не требует подбора порогов.

Для листов на чёрном фоне альфа берётся из яркости: так элемент корректно
складывается аддитивным блендингом, а чёрный фон исчезает.

    python3 tools/artgen/slice_grid.py лист.png 2x4 assets_src/vfx имя1,имя2,...
    python3 tools/artgen/slice_grid.py лист.png 4x4 out имена --white
"""

from __future__ import annotations

import pathlib
import sys

from PIL import Image


def cut_black(cell: Image.Image) -> Image.Image:
    """Чёрный фон -> прозрачность через яркость. Цвет пикселя сохраняется."""
    rgb = cell.convert("RGB")
    alpha = rgb.convert("L")
    out = rgb.convert("RGBA")
    out.putalpha(alpha)
    return out


# Мягкая кромка: между этими яркостями альфа падает плавно, иначе у объекта
# остаётся зубчатый белый ореол на месте мягкой тени.
WHITE_OPAQUE = 214.0
WHITE_CLEAR = 246.0


def cut_white(cell: Image.Image) -> Image.Image:
    """Белый фон -> прозрачность по абсолютной яркости.

    В отличие от заливки от краёв в `SheetSlicer.kt`, порог не протекает внутрь
    тёмного объекта: у урны сброса заливка съедала корпус и оставляла один обод.
    Расплата — светлый объект на белом так не вырезать, для таких нужна заливка.
    """
    rgb = cell.convert("RGB")
    out = rgb.convert("RGBA")
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, _ = pixels[x, y]
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            spread = max(r, g, b) - min(r, g, b)
            if spread >= 18 or luma <= WHITE_OPAQUE:
                continue
            if luma >= WHITE_CLEAR:
                pixels[x, y] = (r, g, b, 0)
            else:
                fade = (luma - WHITE_OPAQUE) / (WHITE_CLEAR - WHITE_OPAQUE)
                pixels[x, y] = (r, g, b, int(255 * (1 - fade)))
    return out


def main() -> None:
    if len(sys.argv) < 5:
        sys.exit(__doc__)
    sheet_path, grid, out_dir, names = sys.argv[1:5]
    white = "--white" in sys.argv
    rows, cols = (int(v) for v in grid.lower().split("x"))
    labels = [n.strip() for n in names.split(",") if n.strip()]
    if len(labels) != rows * cols:
        sys.exit(f"имён {len(labels)}, а ячеек {rows * cols}")

    sheet = Image.open(sheet_path)
    out = pathlib.Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)

    for index, label in enumerate(labels):
        row, col = divmod(index, cols)
        box = (col * sheet.width // cols, row * sheet.height // rows,
               (col + 1) * sheet.width // cols, (row + 1) * sheet.height // rows)
        cell = cut_white(sheet.crop(box)) if white else cut_black(sheet.crop(box))
        bounds = cell.getchannel("A").point(lambda v: 255 if v > 8 else 0).getbbox()
        if bounds is None:
            print(f"  {label:<20} ПУСТО — ячейка без содержимого")
            continue
        cropped = cell.crop(bounds)
        cropped.save(out / f"{label}.png")
        print(f"  {label:<20} {cropped.width}x{cropped.height}")


if __name__ == "__main__":
    main()
