#!/usr/bin/env python3
"""Режет «инвентарный лист» на отдельные ассеты с альфой.

    python3 tools/artlab/sheetcut.py лист.png F,I,R,S,T,BACK --out .../assets

Лист — это кадр, где нужные предметы разложены на пустом коврике, крупно и порознь
(см. промпты M10–M12). В отличие от `canvascut.py`, который вынимает предметы из боевой
сцены и залечивает место под ними, здесь ничего лечить не надо: фон просто отбрасывается.

Предмет находится как связная область, отличающаяся от фона коврика (по яркости или по
цветности), очищается морфологией и вырезается с мягкой альфой. Тень на коврик в ассет
НЕ попадает — её рисует движок, иначе тень «едет» вместе с предметом.

  --names   имена по порядку слева направо (или через `--rows`, построчно)
  --min     минимальная доля площади кадра для предмета (по умолчанию 0.002)
  --pad     поля вокруг предмета в пикселях
  --debug   сохранить контрольный лист с найденными областями
"""
from __future__ import annotations

import argparse
import json
import pathlib

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy import ndimage as ndi


def find_objects(rgb: np.ndarray, min_area: int) -> list[np.ndarray]:
    """Маски предметов: всё, что заметно отличается от фона коврика."""
    lum = rgb.mean(axis=2)
    hsv = np.asarray(Image.fromarray(rgb.astype(np.uint8)).convert("HSV")).astype(np.float32)
    # фон коврика — самая частая тёмная малонасыщенная область; берём её медиану как эталон
    dark = lum < np.percentile(lum, 55)
    base = np.median(rgb[dark], axis=0)
    diff = np.abs(rgb - base).mean(axis=2)
    sat = hsv[..., 1]
    mask = (diff > 26) | (lum > np.percentile(lum, 88)) | (sat > 120)
    mask = ndi.binary_opening(mask, iterations=3)
    mask = ndi.binary_closing(mask, iterations=6)
    mask = ndi.binary_fill_holes(mask)
    # края кадра (стол, кайма коврика) отбрасываем — предметы лежат внутри
    h, w = mask.shape
    frame = np.zeros_like(mask)
    frame[int(h * .04):int(h * .96), int(w * .02):int(w * .98)] = True
    mask &= frame
    lab, n = ndi.label(mask)
    out = []
    for i in range(1, n + 1):
        m = lab == i
        if m.sum() < min_area:
            continue
        ys, xs = np.where(m)
        if xs.min() == 0 or ys.min() == 0 or xs.max() == w - 1 or ys.max() == h - 1:
            continue                                   # прилип к краю кадра — это фон
        out.append(m)
    return out


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("sheet")
    ap.add_argument("names", help="имена предметов по порядку, через запятую")
    ap.add_argument("--out", default=None, help="папка назначения (по умолчанию assets/ рядом с листом)")
    ap.add_argument("--min", type=float, default=0.002, help="мин. доля площади кадра")
    ap.add_argument("--pad", type=int, default=3)
    ap.add_argument("--rows", type=int, default=1, help="во сколько рядов разложены предметы")
    ap.add_argument("--debug", action="store_true")
    a = ap.parse_args()

    src = pathlib.Path(a.sheet).resolve()
    out = pathlib.Path(a.out).resolve() if a.out else src.parent / "assets"
    out.mkdir(parents=True, exist_ok=True)
    im = Image.open(src).convert("RGB")
    rgb = np.asarray(im).astype(np.float32)
    H, W = rgb.shape[:2]

    masks = find_objects(rgb, int(W * H * a.min))
    # порядок: сверху вниз по рядам, внутри ряда слева направо
    def key(m):
        ys, xs = np.where(m)
        return (round(ys.mean() / (H / max(1, a.rows))), xs.mean())
    masks.sort(key=key)

    names = [s.strip() for s in a.names.split(",") if s.strip()]
    if len(masks) != len(names):
        print(f"⚠ найдено предметов {len(masks)}, имён {len(names)} — проверьте лист (--debug)")
    index = []
    for i, m in enumerate(masks):
        name = names[i] if i < len(names) else f"obj{i}"
        ys, xs = np.where(m)
        x0, y0 = max(0, xs.min() - a.pad), max(0, ys.min() - a.pad)
        x1, y1 = min(W, xs.max() + 1 + a.pad), min(H, ys.max() + 1 + a.pad)
        alpha = (m[y0:y1, x0:x1] * 255).astype(np.uint8)
        alpha = np.asarray(Image.fromarray(alpha).filter(ImageFilter.GaussianBlur(0.8)))
        Image.fromarray(np.dstack([rgb[y0:y1, x0:x1], alpha]).astype(np.uint8), "RGBA").save(out / f"{name}.png")
        index.append({"name": name, "box": [int(x0), int(y0), int(x1), int(y1)],
                      "w": int(x1 - x0), "h": int(y1 - y0)})
        print(f"✓ {name}: {x1 - x0}×{y1 - y0}")
    (out / "index.json").write_text(json.dumps({"sheet": str(src), "items": index}, ensure_ascii=False, indent=1))

    if a.debug:
        dbg = im.copy(); d = ImageDraw.Draw(dbg)
        for it in index:
            d.rectangle(it["box"], outline=(255, 210, 120), width=3)
            d.text((it["box"][0] + 6, it["box"][1] + 6), it["name"], fill=(255, 230, 170))
        dbg.save(out / f"_debug_{src.stem}.jpg", quality=85)


if __name__ == "__main__":
    main()
