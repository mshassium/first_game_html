#!/usr/bin/env python3
"""Находит центр бронзового медальона на каждой карте.

Промпт задаёт медальон в долях карты, но модель ставит его с разбросом, а вырезание
фона у каждой карты срезает свой отступ — в итоге доли разъезжаются. Буква, поставленная
по номиналу из промпта, садится мимо. Скрипт меряет фактический центр и печатает готовые
строки для карты в `CardActor`.

    python3 tools/artgen/measure_medallion.py assets_src/cards/card_*.png
"""

from __future__ import annotations

import pathlib
import sys
from collections import deque

from PIL import Image


def medallion(path: pathlib.Path) -> tuple[float, float, float] | None:
    """Центр и диаметр медальона в долях ширины и высоты карты."""
    image = Image.open(path).convert("RGBA")
    width, height = image.size
    pixels = image.load()

    # Медальон живёт в левом верхнем углу. Ищем тёплую бронзу обода: она заметно
    # ярче тёмного поля и всегда сдвинута в красный относительно синего.
    def is_rim(x: int, y: int) -> bool:
        r, g, b, a = pixels[x, y]
        return a > 200 and r > 80 and r - b > 30 and r >= g >= b

    region = [(x, y)
              for y in range(int(height * 0.02), int(height * 0.32))
              for x in range(int(width * 0.02), int(width * 0.38))
              if is_rim(x, y)]
    if not region:
        return None

    members = set(region)
    groups: list[set[tuple[int, int]]] = []
    while members:
        seed = members.pop()
        group = {seed}
        queue = deque([seed])
        while queue:
            cx, cy = queue.popleft()
            for dx in (-2, -1, 0, 1, 2):
                for dy in (-2, -1, 0, 1, 2):
                    neighbour = (cx + dx, cy + dy)
                    if neighbour in members:
                        members.discard(neighbour)
                        group.add(neighbour)
                        queue.append(neighbour)
        groups.append(group)

    # Отсеиваем угловую накладку рамки: она тоже бронзовая, но это уголок,
    # прижатый к самому краю карты. Медальон круглый и отстоит от края.
    def plausible(group: set[tuple[int, int]]) -> bool:
        xs = [p[0] for p in group]
        ys = [p[1] for p in group]
        box_w, box_h = max(xs) - min(xs), max(ys) - min(ys)
        if box_w < width * 0.10 or box_h < width * 0.10:
            return False
        if not 0.75 <= box_w / max(box_h, 1) <= 1.33:
            return False
        return min(xs) > width * 0.03 and min(ys) > height * 0.02

    candidates = [g for g in groups if plausible(g)]
    if not candidates:
        return None
    best = max(candidates, key=len)

    xs = [p[0] for p in best]
    ys = [p[1] for p in best]
    centre_x = (min(xs) + max(xs)) / 2
    centre_y = (min(ys) + max(ys)) / 2
    diameter = max(max(xs) - min(xs), max(ys) - min(ys))
    return centre_x / width, centre_y / height, diameter / width


def main() -> None:
    paths = [pathlib.Path(p) for p in sys.argv[1:]]
    if not paths:
        sys.exit(__doc__)
    rows = []
    for path in sorted(paths):
        if path.stem == "card_back":
            continue
        found = medallion(path)
        if not found:
            print(f"{path.name}: медальон не найден")
            continue
        x, y, d = found
        letter = path.stem.replace("card_", "")
        rows.append((letter, x, y, d))
        print(f"{path.name:<14} центр {x:.3f} / {y:.3f}, диаметр {d:.3f}")

    if rows:
        print("\nдля CardActor:")
        for letter, x, y, _ in sorted(rows):
            print(f'            Letter.{letter} to Medallion({x:.3f}f, {y:.3f}f),')


if __name__ == "__main__":
    main()
