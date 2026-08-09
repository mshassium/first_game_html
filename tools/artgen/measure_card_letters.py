#!/usr/bin/env python3
"""Меряет, насколько буква карты смещена в своих габаритах.

Буквы карт печёт FontBaker из Cinzel Decorative. У заглавной R там длинный
росчерк: он занимает правую треть глифа, и центрирование строки по габаритам
сажает в середину медальона хвост, а тело буквы уводит влево — она выбивается
из ряда с F, I, S, T.

Скрипт считает для каждой буквы центр габаритов и центр тела (тело берётся по
верхней половине глифа, где росчерка ещё нет) и печатает готовую константу для
`CardLetter.kt`. Перемерить после перепечки шрифта (`./gradlew tools:bakeFonts`).

    python3 tools/artgen/measure_card_letters.py
"""

from __future__ import annotations

import pathlib
import re
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[2] / "gdx" / "assets" / "fonts"
LETTERS = "FIRST"


def glyphs(fnt: pathlib.Path) -> dict[str, dict[str, int]]:
    found = {}
    for line in fnt.read_text(encoding="utf-8").splitlines():
        if line.startswith("char id="):
            fields = {k: int(v) for k, v in re.findall(r"(\w+)=(-?\d+)", line)}
            found[chr(fields["id"])] = fields
    return found


def cap_height(atlas: Image.Image, chars: dict[str, dict[str, int]]) -> float:
    """Высота прописной: libGDX берёт её по первой найденной букве из своего списка."""
    for char in "MNBDCEFKLPRTVWXYZ":
        if char in chars:
            return float(chars[char]["height"])
    raise SystemExit("в шрифте нет ни одной буквы из списка capChars libGDX")


def main() -> int:
    fnt = ROOT / "card_letter.fnt"
    png = ROOT / "card_letter.png"
    if not fnt.exists():
        print(f"нет {fnt}", file=sys.stderr)
        return 1

    chars = glyphs(fnt)
    atlas = Image.open(png).convert("RGBA")
    cap = cap_height(atlas, chars)

    print(f"{'буква':>6} {'габариты':>9} {'тело':>7} {'сдвиг':>7} {'в долях cap':>12}")
    shifts = {}
    for letter in LETTERS:
        info = chars[letter]
        glyph = atlas.crop(
            (info["x"], info["y"], info["x"] + info["width"], info["y"] + info["height"])
        )
        box = glyph.getbbox()
        # Тело — верхняя половина глифа: росчерк у R уходит вниз-вправо и сюда не попадает.
        body = glyph.crop((0, 0, glyph.width, glyph.height // 2)).getbbox()
        if box is None or body is None:
            continue
        box_center = (box[0] + box[2]) / 2
        body_center = (body[0] + body[2]) / 2
        shift = box_center - body_center
        shifts[letter] = shift / cap
        print(
            f"{letter:>6} {box_center:>9.1f} {body_center:>7.1f} "
            f"{shift:>7.1f} {shift / cap:>12.3f}"
        )

    worst = max(shifts, key=lambda letter: abs(shifts[letter]))
    print()
    print("Поправка нужна там, где сдвиг заметно больше нуля. Для CardLetter.kt:")
    print(f'    private const val TAIL_SHIFT = {shifts[worst]:.3f}f   // буква {worst}')
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
