#!/usr/bin/env python3
"""E2: режет листы «пять карт в ряд на чёрном» на карты и собирает тесты читаемости.

    python3 tools/artlab/cardtest.py docs/gdx/12-art-direction-lab/e2-letter-hero

Для каждого out/<ID>_*.png:
  cards/<ID>_F.png … <ID>_T.png        — нарезанные карты (по столбцам яркости)
  tests/<ID>_hand960.jpg               — рука из 7 карт на коврике при 960×540 (как на телефоне)
  tests/<ID>_table960.jpg              — стол: два ряда SPACE + рука при 960×540
  tests/<ID>_stack.jpg                 — стопка ×3 крупно и мелко
  tests/all_variants.jpg               — все варианты рядом, крупно
"""
from __future__ import annotations

import glob
import pathlib
import re
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = pathlib.Path(__file__).resolve().parents[2]
LETTERS = "FIRST"
MAT = ROOT / "docs/gdx/12-art-direction-lab/e1-living-card/out/MAT_gpt-5.4-image-2_1.png"


def slice_sheet(path: pathlib.Path) -> list[Image.Image]:
    im = Image.open(path).convert("RGB")
    g = im.convert("L")
    w, h = g.size
    px = g.load()
    thr = 40
    cols = [sum(1 for y in range(0, h, 2) if px[x, y] > thr) for x in range(w)]
    runs, inrun, start = [], False, 0
    for x, v in enumerate(cols + [0]):
        on = v > 3
        if on and not inrun:
            inrun, start = True, x
        elif not on and inrun:
            inrun = False
            if x - start > w * 0.06:
                runs.append((start, x))
    if len(runs) != 5:
        print(f"  ! {path.name}: найдено {len(runs)} колонок вместо 5 — режу равномерно")
        step = w / 5
        runs = [(int(i * step + step * 0.06), int((i + 1) * step - step * 0.06)) for i in range(5)]
    cards = []
    for x0, x1 in runs:
        rows = [sum(1 for x in range(x0, x1, 2) if px[x, y] > thr) for y in range(h)]
        ys = [y for y, v in enumerate(rows) if v > 3]
        y0, y1 = (min(ys), max(ys) + 1) if ys else (0, h)
        c = im.crop((x0, y0, x1, y1))
        # привести к 2:3, обрезая лишнее по центру
        cw, ch = c.size
        if cw / ch > 2 / 3:
            nw = int(ch * 2 / 3); c = c.crop(((cw - nw) // 2, 0, (cw - nw) // 2 + nw, ch))
        else:
            nh = int(cw * 3 / 2); c = c.crop((0, (ch - nh) // 2, cw, (ch - nh) // 2 + nh))
        cards.append(c)
    return cards


def rounded(card: Image.Image, w: int) -> Image.Image:
    h = int(w * 1.5)
    c = card.resize((w, h), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, w - 1, h - 1), radius=int(w * 0.06), fill=255)
    c.putalpha(mask)
    return c


def shadow_paste(base: Image.Image, card: Image.Image, x: int, y: int, angle: float = 0.0) -> None:
    c = card.rotate(angle, expand=True, resample=Image.BICUBIC)
    sh = Image.new("RGBA", c.size, (0, 0, 0, 0))
    sh.paste((0, 0, 0, 150), (0, 0), c.getchannel("A"))
    sh = sh.filter(ImageFilter.GaussianBlur(int(c.width * 0.05)))
    base.alpha_composite(sh, (x + int(c.width * 0.02), y + int(c.width * 0.05)))
    base.alpha_composite(c, (x, y))


def font(size: int) -> ImageFont.FreeTypeFont:
    for name in ("/System/Library/Fonts/Supplemental/Arial Bold.ttf", "/System/Library/Fonts/Helvetica.ttc"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def mat(w: int, h: int) -> Image.Image:
    return Image.open(MAT).convert("RGBA").resize((w, h), Image.LANCZOS)


def hand_test(cards: dict[str, Image.Image], out: pathlib.Path, W=960, H=540) -> None:
    base = mat(W, H)
    seq = ["F", "I", "R", "S", "T", "F", "I"]
    cw = int(W * 0.095); step = int(cw * 0.86)
    x0 = W // 2 - (len(seq) * step) // 2
    for i, l in enumerate(seq):
        shadow_paste(base, rounded(cards[l], cw), x0 + i * step, int(H * 0.68) + abs(i - 3) * 3, (3 - i) * 2.0)
    d = ImageDraw.Draw(base); d.text((12, 10), "рука из 7 · 960×540", fill=(230, 220, 200), font=font(16))
    base.convert("RGB").save(out, quality=88)


def table_test(cards: dict[str, Image.Image], out: pathlib.Path, W=960, H=540) -> None:
    base = mat(W, H)
    cw = int(min(H * 0.17, (W - 40) / 9.6)); ch = int(cw * 1.5)
    rowW = cw * 5 + int(cw * 0.22) * 4; x0 = int(W * 0.5 - rowW / 2)
    d = ImageDraw.Draw(base)
    for fy, row in ((0.28, ["F", "I", "R", None, None]), (0.56, ["S", "T", None, None, None])):
        for i in range(5):
            x = x0 + i * int(cw * 1.22); y = int(H * fy - ch / 2)
            d.rounded_rectangle((x, y, x + cw, y + ch), radius=int(cw * 0.08), outline=(214, 196, 160, 70), width=2)
            if row[i]:
                shadow_paste(base, rounded(cards[row[i]], cw), x, y)
    seq = ["F", "I", "R", "S", "T"]
    hw = int(cw * 1.1); step = int(hw * 0.95); hx0 = W // 2 - (len(seq) * step) // 2
    for i, l in enumerate(seq):
        shadow_paste(base, rounded(cards[l], hw), hx0 + i * step, int(H - hw * 1.5 * 0.92) + abs(i - 2) * 3, (2 - i) * 2.5)
    d.text((12, 10), "стол · 960×540", fill=(230, 220, 200), font=font(16))
    base.convert("RGB").save(out, quality=88)


def stack_test(cards: dict[str, Image.Image], out: pathlib.Path) -> None:
    W, H = 960, 420
    base = mat(W, H)
    for k, cw in enumerate((220, 110, 70)):
        x = 60 + k * 300; y = 40
        for j in range(3):
            shadow_paste(base, rounded(cards["R"], cw), x + j * 6, y + j * 6)
        d = ImageDraw.Draw(base)
        f = font(int(cw * 0.22)); txt = "×3"
        tw = d.textlength(txt, font=f)
        d.rounded_rectangle((x + cw - tw - 14, y + 4, x + cw + 10, y + int(cw * 0.3)), radius=6, fill=(20, 16, 12, 220))
        d.text((x + cw - tw - 4, y + 6), txt, fill=(240, 226, 190), font=f)
    ImageDraw.Draw(base).text((12, 390), "стопка ×3: крупно · как на столе · как в руке телефона", fill=(230, 220, 200), font=font(16))
    base.convert("RGB").save(out, quality=88)


def main() -> None:
    folder = pathlib.Path(sys.argv[1]).resolve()
    (folder / "cards").mkdir(exist_ok=True); (folder / "tests").mkdir(exist_ok=True)
    variants: dict[str, dict[str, Image.Image]] = {}
    for sheet in sorted(glob.glob(str(folder / "out" / "*.png"))):
        vid = re.match(r"([A-Za-z0-9]+)_", pathlib.Path(sheet).name).group(1)
        cs = slice_sheet(pathlib.Path(sheet))
        variants[vid] = dict(zip(LETTERS, cs))
        for l, c in variants[vid].items():
            c.save(folder / "cards" / f"{vid}_{l}.png")
        hand_test(variants[vid], folder / "tests" / f"{vid}_hand960.jpg")
        table_test(variants[vid], folder / "tests" / f"{vid}_table960.jpg")
        stack_test(variants[vid], folder / "tests" / f"{vid}_stack.jpg")
        print(f"✓ {vid}: {len(cs)} карт → tests/{vid}_*.jpg")
    # общий лист
    ids = list(variants); cw = 200; ch = 300; pad = 24
    W = pad + len(LETTERS) * (cw + 12) + pad + 60; H = pad + len(ids) * (ch + 40)
    sheet = Image.new("RGBA", (W, H), (21, 18, 14, 255)); d = ImageDraw.Draw(sheet)
    for r, vid in enumerate(ids):
        y = pad + r * (ch + 40)
        d.text((pad, y + ch // 2 - 10), vid, fill=(211, 165, 74), font=font(20))
        for i, l in enumerate(LETTERS):
            shadow_paste(sheet, rounded(variants[vid][l], cw), pad + 60 + i * (cw + 12), y)
    sheet.convert("RGB").save(folder / "tests" / "all_variants.jpg", quality=88)
    print("✓ tests/all_variants.jpg")


if __name__ == "__main__":
    main()
