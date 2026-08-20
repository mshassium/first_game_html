#!/usr/bin/env python3
"""Делает lab.html автономным: картинки из `out/` встраиваются как data-URI (ужатые JPEG),
чтобы прототип можно было опубликовать одной страницей и открыть с телефона.

    python3 tools/artlab/embed.py docs/gdx/12-art-direction-lab/e1-living-card/lab.html /tmp/e1.html [--max 768]
"""
import argparse, base64, pathlib, re
from io import BytesIO
from PIL import Image

ap = argparse.ArgumentParser(); ap.add_argument("src"); ap.add_argument("dst"); ap.add_argument("--max", type=int, default=1024, help="макс. сторона картинки"); ap.add_argument("--q", type=int, default=84); ap.add_argument("--full", default="", help="подстроки имён файлов, которые оставить в полном размере (через запятую)")
a = ap.parse_args()
src = pathlib.Path(a.src).resolve(); html = src.read_text(encoding="utf-8")
def data_uri(rel: str) -> str:
    p = (src.parent / rel).resolve()
    with Image.open(p) as im:
        alpha = im.mode in ("RGBA", "LA") or (im.mode == "P" and "transparency" in im.info)
        im = im.convert("RGBA" if alpha else "RGB")
        keep = any(s and s in p.name for s in a.full.split(","))
        if max(im.size) > a.max and not keep:
            k = a.max / max(im.size); im = im.resize((round(im.width * k), round(im.height * k)), Image.LANCZOS)
        buf = BytesIO()
        if alpha:                                   # PNG с палитрой: вчетверо легче, на глаз не отличить
            im.quantize(colors=255, method=Image.FASTOCTREE).save(buf, format="PNG", optimize=True)
            return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()
        im.save(buf, format="JPEG", quality=a.q, optimize=True)
    return "data:image/jpeg;base64," + base64.b64encode(buf.getvalue()).decode()
seen = {}
def repl(m):
    rel = m.group(1)
    if rel not in seen: seen[rel] = data_uri(rel)
    return f"'{seen[rel]}'"
html = re.sub(r"'((?:\.\./|out/|props/|cut\d*/|ui/)[^']+\.(?:png|jpg|jpeg))'", repl, html)
pathlib.Path(a.dst).write_text(html, encoding="utf-8")
print(a.dst, f"{pathlib.Path(a.dst).stat().st_size/1024:.0f} КБ, встроено {len(seen)}")
