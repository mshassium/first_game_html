#!/usr/bin/env python3
"""Рисует иконку сетевой игры — два сцепленных кольца на тёмном медальоне.

Остальные иконки интерфейса нарисованы нейросетью по промпт-буку, но здесь
нужен был простой геометрический знак: свободного смысла в атласе не осталось —
глобус занят языком, скрещённые топоры обычной игрой, круговая стрелка
«Продолжить». Сцепленные звенья читаются как связь и ни с чем не путаются.

Цвета и размеры сняты с `icon_duel.png`, чтобы иконка встала в общий ряд:
тёмный медальон, бронзовая кайма, глиф со светом сверху.

    python3 tools/artgen/make_icon_online.py
"""

from __future__ import annotations

import math
import pathlib
import random

from PIL import Image, ImageDraw

SIZE = 172
TILT_DEGREES = 28  # наклон пары звеньев
SUPERSAMPLE = 4  # рисуем крупнее и уменьшаем: края колец должны быть гладкими

RIM = (120, 92, 54)         # бронзовая кайма медальона
RIM_INNER = (58, 44, 28)    # её тёмная внутренняя грань
FIELD_OUTER = (26, 25, 23)  # тёмное поле у края
FIELD_INNER = (40, 37, 33)  # и чуть светлее к центру
GOLD = (201, 162, 74)
GOLD_LIGHT = (242, 223, 166)
SHADOW = (12, 10, 8)

OUT = pathlib.Path(__file__).resolve().parents[2] / "assets_src" / "ui" / "icon_online.png"


def medallion(size: int) -> Image.Image:
    """Тёмный круг с бронзовой каймой — подложка, общая у всех иконок."""
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    center = size / 2

    # Поле: радиальный градиент кольцами от края к центру.
    steps = 90
    for step in range(steps):
        part = step / steps
        radius = center * (0.97 - 0.97 * part)
        color = tuple(
            round(FIELD_OUTER[i] + (FIELD_INNER[i] - FIELD_OUTER[i]) * part) for i in range(3)
        )
        draw.ellipse(
            [center - radius, center - radius, center + radius, center + radius],
            fill=color + (255,),
        )

    # Лёгкая крупка на поле: у соседних иконок фон не идеально гладкий.
    speckle = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(speckle)
    rnd = random.Random(17)
    for _ in range(size * 3):
        x = rnd.uniform(0, size)
        y = rnd.uniform(0, size)
        if (x - center) ** 2 + (y - center) ** 2 > (center * 0.9) ** 2:
            continue
        shade = rnd.randint(0, 26)
        dot = rnd.uniform(size * 0.002, size * 0.006)
        sdraw.ellipse([x, y, x + dot, y + dot], fill=(shade + 30, shade + 28, shade + 25, 90))
    image.alpha_composite(speckle)

    # Кайма в три слоя: тёмный кант снаружи, бронза, тёмная грань внутри —
    # так край читается скруглённым, как на остальных иконках.
    draw.ellipse([1, 1, size - 2, size - 2], outline=SHADOW + (255,), width=round(size * 0.03))
    draw.ellipse(
        [size * 0.03, size * 0.03, size * 0.97, size * 0.97],
        outline=RIM + (255,),
        width=round(size * 0.05),
    )
    draw.ellipse(
        [size * 0.085, size * 0.085, size * 0.915, size * 0.915],
        outline=RIM_INNER + (255,),
        width=round(size * 0.018),
    )
    return image


def link(draw: ImageDraw.ImageDraw, cx: float, cy: float, radius: float, width: float, color) -> None:
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], outline=color, width=round(width))


def glyph(size: int) -> Image.Image:
    """
    Два сцепленных звена.

    Кольца рисуются по очереди, а потом дуга левого повторяется поверх правого:
    без этого перехлёста два круга сливаются в знак бесконечности, а не читаются
    как сцепление.
    """
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    center = size / 2
    radius = size * 0.175
    width = size * 0.058
    # Звенья должны заметно заходить друг за друга: касающиеся кольца читаются
    # как два отдельных круга, а не как сцепление.
    offset = radius * 0.72

    tilt = math.radians(TILT_DEGREES)
    left = (center - offset * math.cos(tilt), center + offset * math.sin(tilt))
    right = (center + offset * math.cos(tilt), center - offset * math.sin(tilt))

    def box(point, r):
        return [point[0] - r, point[1] - r, point[0] + r, point[1] + r]

    # Тень под звеньями: без неё знак плоский и сливается с полем.
    for point in (left, right):
        draw.ellipse(
            box((point[0] + size * 0.008, point[1] + size * 0.012), radius),
            outline=SHADOW + (190,), width=round(width),
        )

    draw.ellipse(box(left, radius), outline=GOLD + (255,), width=round(width))
    draw.ellipse(box(right, radius), outline=GOLD + (255,), width=round(width))
    # Левое звено проходит поверх правого — только в зоне пересечения.
    draw.arc(box(left, radius), start=-52, end=52, fill=GOLD + (255,), width=round(width))

    # Блик сверху: свет в игре падает сверху, как на остальных иконках.
    for point in (left, right):
        draw.arc(
            box((point[0], point[1] - size * 0.006), radius),
            start=200, end=340, fill=GOLD_LIGHT + (255,), width=round(width * 0.42),
        )
    return image


def main() -> None:
    big = SIZE * SUPERSAMPLE
    icon = medallion(big)
    icon.alpha_composite(glyph(big))
    icon = icon.resize((SIZE, SIZE), Image.LANCZOS)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    icon.save(OUT)
    print(f"готово: {OUT} ({icon.size[0]}×{icon.size[1]})")


if __name__ == "__main__":
    main()
