#!/usr/bin/env python3
"""E8 «Единый холст»: режет мастер-холст сцены на игровые слои.

    python3 tools/artlab/canvascut.py docs/gdx/12-art-direction-lab/e8-one-canvas/out/M1_gpt-5.4-image-2_2.png \
        docs/gdx/12-art-direction-lab/e8-one-canvas/cut

Делает:
  1. находит коврик (большая тёмная малонасыщенная область в центре);
  2. внутри коврика находит объекты (светлые/насыщенные пятна): карты, фигурки, жетоны, часы, колоды, лоток;
  3. каждый объект вырезает с мягкой альфой в `sprites/NN.png` + `sprites/index.json` (bbox, центр, размер);
  4. «залечивает» коврик под объектами текстурой из пустых участков → `plate.png` (фон игры без подвижных предметов);
  5. если рядом с исходником лежит `stamp.json` — возвращает в плиту печатную разметку,
     которую закрывали предметы (контур пустого слота копируется на места вырезанных карт);
  6. `sheet.jpg` — контрольный лист: холст, маска, плита, спрайты.

Спрайты именуются вручную после просмотра (index.json → letter/kind) — автоматика не знает, что есть что.
"""
from __future__ import annotations

import json
import pathlib
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy import ndimage as ndi


def sidecar(src: pathlib.Path, name: str) -> pathlib.Path:
    """`<исходник>.<name>.json` рядом с холстом, иначе общий `<name>.json`."""
    special = src.with_name(f"{src.stem}.{name}.json")
    return special if special.exists() else src.with_name(f"{name}.json")


def main() -> None:
    src = pathlib.Path(sys.argv[1]).resolve()
    out = pathlib.Path(sys.argv[2]).resolve(); (out / "sprites").mkdir(parents=True, exist_ok=True)
    im = Image.open(src).convert("RGB"); W, H = im.size
    rgb = np.asarray(im).astype(np.float32)
    hsv = np.asarray(im.convert("HSV")).astype(np.float32)
    lum = rgb.mean(axis=2); sat = hsv[..., 1]

    # 1. коврик: тёмно и малонасыщенно; берём крупнейшую компоненту вокруг центра
    matlike = (lum < 70) & (sat < 110)
    matlike = ndi.binary_opening(matlike, iterations=2)
    lab, n = ndi.label(matlike)
    cy, cx = H // 2, W // 2
    # компонента, содержащая больше всего пикселей в центральном окне
    win = lab[int(H * .3):int(H * .7), int(W * .3):int(W * .7)]
    ids, counts = np.unique(win[win > 0], return_counts=True)
    mat_id = ids[counts.argmax()]
    raw_mat = lab == mat_id
    # контур коврика — выпуклая оболочка его компоненты (тогда фигурки у края тоже «внутри»)
    from scipy.spatial import ConvexHull
    ys, xs = np.where(raw_mat); pts = np.column_stack([xs, ys])
    hull = ConvexHull(pts); poly = [tuple(map(int, pts[v])) for v in hull.vertices]
    hm = Image.new("L", (W, H), 0); ImageDraw.Draw(hm).polygon(poly, fill=255)
    mat = np.asarray(hm) > 0
    mat = ndi.binary_erosion(mat, iterations=3)     # чуть внутрь, чтобы не цеплять дерево по краю
    raw_mat_hull = mat.copy()
    mat_box = (int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max()))

    # 2. объекты на коврике: заметно светлее или насыщеннее коврика (печатные линии зон и кант — тусклые, не проходят)
    cand = mat & ((lum > 95) | (sat > 150)) & ~raw_mat
    cand = ndi.binary_opening(cand, iterations=2)
    cand = ndi.binary_closing(cand, iterations=4)
    cand = ndi.binary_fill_holes(cand)
    olab, on = ndi.label(cand)
    sizes = ndi.sum(cand, olab, range(1, on + 1))
    skip = sidecar(src, "skip")
    skip_boxes = json.loads(skip.read_text())["boxes"] if skip.exists() else []
    inner = ndi.binary_erosion(mat, iterations=6)
    keep = []
    for i, s in enumerate(sizes):
        if s < (W * H) * 0.0004:
            continue
        m = olab == i + 1
        if (m & inner).sum() / s < 0.985:      # цепляет край коврика/дерево — оставляем в плите (фигурки, лоток на краю)
            continue
        ys_, xs_ = np.where(m)
        bw, bh = xs_.max() - xs_.min() + 1, ys_.max() - ys_.min() + 1
        if s / (bw * bh) < 0.35:               # тонкие огрызки (блики по краю колод) — не предметы
            continue
        if any(bx0 <= xs_.mean() <= bx1 and by0 <= ys_.mean() <= by1 for bx0, by0, bx1, by1 in skip_boxes):
            continue                           # предмет из skip.json: остаётся частью плиты
        keep.append(i + 1)
    objects = np.isin(olab, keep)
    index = []
    plate = rgb.copy()

    # 3. базовый цвет коврика: ближайший чистый пиксель (сильно размыт)
    clean0 = raw_mat & ~ndi.binary_dilation(objects, iterations=12)
    dist, (iy, ix) = ndi.distance_transform_edt(~clean0, return_indices=True)
    base = rgb[iy, ix]
    base = np.stack([ndi.gaussian_filter(base[..., c], 18) for c in range(3)], axis=-1)

    # 3a. дорастить каждый объект его тёмным ободком и собственной тенью:
    #     всё, что рядом заметно отличается от чистого коврика, — часть предмета, а не коврик
    delta = np.abs(rgb - base).mean(axis=2)
    halo = (delta > 13) & raw_mat_hull
    _, (oy, ox) = ndi.distance_transform_edt(~objects, return_indices=True)
    owner = olab[oy, ox]                             # ближайший объект: тень достаётся своему хозяину,
    masks = {}                                       # чтобы соседняя карта не утащила чужой край
    # 3a-bis. ручные области (extra.json рядом с исходником): предметы, которые автоматика не берёт —
    #         слишком тёмные (жетон в гнезде) или касающиеся края коврика
    extra = sidecar(src, "extra")
    if extra.exists():
        for j, reg in enumerate(json.loads(extra.read_text())["regions"]):
            if "clone" in reg:                 # клон-регионы режутся отдельно, ниже
                continue
            bx0, by0, bx1, by1 = reg["box"]
            box = np.zeros((H, W), bool); box[by0:by1, bx0:bx1] = True
            m = ndi.binary_fill_holes(ndi.binary_closing((delta > 13) & box, iterations=3))
            lb, ln = ndi.label(m)
            if ln:
                sz = ndi.sum(m, lb, range(1, ln + 1))
                m = np.isin(lb, [k + 1 for k, v in enumerate(sz) if v > 150])
            masks[f"x{j}"] = m
            keep.append(f"x{j}")
            raw_mat_hull = raw_mat_hull | box
    for i in [k for k in keep if not isinstance(k, str)]:
        m = olab == i
        near = ndi.binary_dilation(m, iterations=22) & raw_mat_hull & (owner == i)
        grown = ndi.binary_fill_holes(ndi.binary_closing(m | (halo & near), iterations=2))
        lb, _ = ndi.label(grown)                     # только та часть, что срослась с объектом
        ids = np.unique(lb[m])
        masks[i] = np.isin(lb, ids[ids > 0])
    objects = np.zeros_like(objects)
    for m in masks.values():
        objects |= m

    # 3b. мелкая текстура коврика: высокие частоты чистой плитки, размноженные тайлом
    clean = raw_mat & ~ndi.binary_dilation(objects, iterations=10)
    cys, cxs = np.where(clean); rng = np.random.default_rng(7); T = 96
    tile = None
    for _ in range(4000):
        k = rng.integers(len(cys)); ty, tx = int(cys[k]), int(cxs[k])
        if ty + T < H and tx + T < W and clean[ty:ty + T, tx:tx + T].all():
            tile = rgb[ty:ty + T, tx:tx + T]; break
    if tile is None:                                  # чистой плитки нет — зерном по статистике коврика
        sigma = float(rgb[clean].std(axis=0).mean()) * 0.35
        texture = ndi.gaussian_filter(rng.normal(0, sigma, (H, W, 1)), (1.1, 1.1, 0)) * np.ones(3)
    else:
        hp = tile - np.stack([ndi.gaussian_filter(tile[..., c], 6) for c in range(3)], axis=-1)
        reps = (H // T + 2, W // T + 2, 1); texture = np.tile(hp, reps)[:H, :W]
    healed_img = np.clip(base + texture * 0.9, 0, 255)

    for idx, i in enumerate(keep):
        m = masks[i]
        ys, xs = np.where(m); x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
        pad = 6; x0p, y0p = max(0, x0 - pad), max(0, y0 - pad); x1p, y1p = min(W, x1 + pad), min(H, y1 + pad)
        # мягкая альфа: маска + лёгкое размытие по краю
        a = (m[y0p:y1p, x0p:x1p].astype(np.float32) * 255)
        a = np.asarray(Image.fromarray(a.astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.2))).astype(np.float32)
        spr = np.dstack([rgb[y0p:y1p, x0p:x1p], a]).astype(np.uint8)
        Image.fromarray(spr, "RGBA").save(out / "sprites" / f"{idx:02d}.png")
        index.append({"id": idx, "bbox": [int(x0p), int(y0p), int(x1p), int(y1p)], "w": int(x1p - x0p), "h": int(y1p - y0p),
                      "cx": int((x0p + x1p) / 2), "cy": int((y0p + y1p) / 2), "area": int(m.sum())})
        # 4. залечивание области под объектом (с запасом) плавной заплаткой
        dil = ndi.binary_dilation(m, iterations=8) & raw_mat_hull
        plate[dil] = healed_img[dil]
    # 4a. вернуть печатную разметку под вырезанными предметами (stamp.json рядом с исходником)
    stamp = sidecar(src, "stamp")
    if stamp.exists():
        for job in json.loads(stamp.read_text())["stamps"]:
            x0, y0, x1, y1 = job["src"]
            patch = plate[y0:y1, x0:x1]
            hp = patch - np.stack([ndi.gaussian_filter(patch[..., c], 8) for c in range(3)], axis=-1)
            ph, pw = hp.shape[:2]
            for cx_, cy_ in job["dst"]:
                dx, dy = int(cx_ - pw / 2), int(cy_ - ph / 2)
                plate[dy:dy + ph, dx:dx + pw] = np.clip(plate[dy:dy + ph, dx:dx + pw] + hp, 0, 255)

    # мягкий край заплаток: смешиваем по перу 4 px
    healed = (ndi.binary_dilation(objects, iterations=8) & raw_mat_hull).astype(np.float32)
    feather = ndi.gaussian_filter(healed, 3)[..., None]
    plate = rgb * (1 - feather) + plate * feather
    # 4a-bis. клон-регионы (extra.json, поле clone): предмет вне коврика — например латунная кнопка на дереве.
    #         Соседний участок стола копируется на его место; что от него отличается — и есть предмет.
    if extra.exists():
        for reg in json.loads(extra.read_text())["regions"]:
            if "clone" not in reg:
                continue
            x0, y0, x1, y1 = reg["box"]; dx, dy = reg["clone"]
            patch = rgb[y0 + dy:y1 + dy, x0 + dx:x1 + dx]
            here = rgb[y0:y1, x0:x1]
            diff = ndi.gaussian_filter(np.abs(here - patch).mean(axis=2), 2)
            m = ndi.binary_opening(ndi.binary_closing(diff > 18, iterations=4), iterations=4)
            if "core" in reg:                          # где искать сам предмет (бокс шире — он нужен для мягкого шва)
                cx0, cy0, cx1, cy1 = reg["core"]; box = np.zeros_like(m)
                box[cy0 - y0:cy1 - y0, cx0 - x0:cx1 - x0] = True; m &= box
            lb, ln = ndi.label(m)
            if ln:                                     # только самая крупная связная часть — сам предмет
                sz = ndi.sum(m, lb, range(1, ln + 1)); m = lb == (int(np.argmax(sz)) + 1)
            m = ndi.binary_fill_holes(ndi.binary_dilation(m, iterations=3))
            a = np.asarray(Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.4))).astype(np.float32)
            Image.fromarray(np.dstack([here, a]).astype(np.uint8), "RGBA").save(out / "sprites" / f"{reg['name']}.png")
            index.append({"id": reg["name"], "bbox": [x0, y0, x1, y1], "w": x1 - x0, "h": y1 - y0,
                          "cx": (x0 + x1) // 2, "cy": (y0 + y1) // 2, "area": int(m.sum())})
            ring = ndi.binary_dilation(m, iterations=14) & ~ndi.binary_dilation(m, iterations=6)
            patch = patch + (here[ring].mean(axis=0) - patch[ring].mean(axis=0))   # свести яркость к соседям
            feather = ndi.gaussian_filter(ndi.binary_dilation(m, iterations=6).astype(np.float32), 7)[..., None]
            plate[y0:y1, x0:x1] = np.clip(here * (1 - feather) + patch * feather, 0, 255)

    # 4b. карты режутся строго по телу (яркий картон в рукаве), без тени и без краёв соседей:
    #     тень при движении рисует движок, а тело карты нужно ровным прямоугольником — тогда
    #     его можно проективно вписать в любой слот и карта получит ракурс стола.
    cards_cfg = sidecar(src, "cards")
    if cards_cfg.exists():
        cards_dir = out / "cards"; cards_dir.mkdir(exist_ok=True)
        cardidx = []
        for c in json.loads(cards_cfg.read_text())["cards"]:
            if "rect" in c:                     # рубашка и прочие тёмные карты — прямоугольником вручную
                bx0, by0, bx1, by1 = c["rect"]
                Image.fromarray(rgb[by0:by1, bx0:bx1].astype(np.uint8)).save(cards_dir / f"{c['name']}.png")
                cardidx.append({"name": c["name"], "rect": c["rect"], "slot": c["slot"]})
                continue
            x0, y0, x1, y1 = c["box"]
            sub = rgb[y0:y1, x0:x1]
            body = ndi.binary_opening(sub.mean(axis=2) > c.get("lum", 95), iterations=3)
            if not body.any():
                continue
            rows = np.where(body.mean(axis=1) > 0.45)[0]; cols = np.where(body.mean(axis=0) > 0.45)[0]
            bx0, bx1, by0, by1 = x0 + cols.min(), x0 + cols.max() + 1, y0 + rows.min(), y0 + rows.max() + 1
            Image.fromarray(rgb[by0:by1, bx0:bx1].astype(np.uint8)).save(cards_dir / f"{c['name']}.png")
            cardidx.append({"name": c["name"], "rect": [int(bx0), int(by0), int(bx1), int(by1)],
                            "slot": c["slot"]})
        (out / "cards.json").write_text(json.dumps({"cards": cardidx}, ensure_ascii=False, indent=1))
        print("карты:", ", ".join(f"{c['name']} {c['rect'][2] - c['rect'][0]}×{c['rect'][3] - c['rect'][1]}" for c in cardidx))

    # 4b. простые копии кусков холста (crops.json): предмет остаётся и в плите, и становится спрайтом
    crops = sidecar(src, "crops")
    if crops.exists():
        for c in json.loads(crops.read_text())["crops"]:
            x0, y0, x1, y1 = c["box"]
            im.crop((x0, y0, x1, y1)).save(out / "sprites" / f"{c['name']}.png")
            index.append({"id": c["name"], "bbox": [x0, y0, x1, y1], "w": x1 - x0, "h": y1 - y0,
                          "cx": (x0 + x1) // 2, "cy": (y0 + y1) // 2, "area": (x1 - x0) * (y1 - y0)})

    Image.fromarray(plate.astype(np.uint8)).save(out / "plate.png")
    (out / "index.json").write_text(json.dumps({"source": str(src), "size": [W, H], "mat_box": mat_box, "sprites": index}, ensure_ascii=False, indent=1))

    # 5. контрольный лист
    sheet = Image.new("RGB", (W * 2, H * 2), (30, 28, 24))
    sheet.paste(im, (0, 0))
    mk = Image.fromarray((objects * 255).astype(np.uint8)).convert("RGB"); sheet.paste(mk, (W, 0))
    sheet.paste(Image.open(out / "plate.png"), (0, H))
    d = ImageDraw.Draw(sheet); x, y = W + 10, H + 10; rowh = 0
    for s in index:
        name = s["id"] if isinstance(s["id"], str) else f"{s['id']:02d}"
        sp = Image.open(out / "sprites" / f"{name}.png"); k = min(1.0, 180 / max(sp.size))
        sp = sp.resize((max(1, int(sp.width * k)), max(1, int(sp.height * k))))
        if x + sp.width > 2 * W - 10: x = W + 10; y += rowh + 24; rowh = 0
        sheet.paste(sp, (x, y), sp if sp.mode == "RGBA" else None); d.text((x, y + sp.height + 2), str(s["id"]), fill=(230, 220, 200)); x += sp.width + 12; rowh = max(rowh, sp.height)
    sheet.save(out / "sheet.jpg", quality=85)
    print(f"коврик {mat_box}, объектов {len(index)} → {out}")


if __name__ == "__main__":
    main()
