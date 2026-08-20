#!/usr/bin/env python3
"""Галерея эксперимента арт-лаборатории: одна HTML-страница для судьи.

Читает `out/runs.jsonl` (что отрендерено), `GALLERY.md` (тексты к вариантам)
и собирает `gallery.html` рядом. Тёмный нейтральный фон намеренно: концепты
судятся на нейтральной подложке, как в просмотровой.

    python3 tools/artlab/gallery.py docs/gdx/12-art-direction-lab/e0-directions
    python3 tools/artlab/gallery.py …/e0-directions --embed out.html   # картинки внутри (для публикации)
    python3 tools/artlab/gallery.py …/e0-directions --pdf gallery.pdf   # то же в PDF (headless Chrome, A4 альбом)

GALLERY.md — простой формат:

    # Заголовок страницы
    > одна строка подзаголовка
    ## intro            ← абзацы до первого варианта
    …
    ## D1 · Название    ← блок варианта: ID до « · », далее заголовок
    thesis: одна фраза
    refs: что смотреть
    note: комментарий арт-директора (можно несколько строк)
    ## outro            ← что после вариантов (таблица судьи и т.п.)
"""

from __future__ import annotations

import argparse
import base64
import html
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]

CSS = """
:root{--bg:#15120e;--panel:#1e1913;--ink:#eadfcd;--muted:#a3957f;--gold:#d3a54a;--rule:#332a1f;--mono:ui-monospace,Menlo,monospace}
*{box-sizing:border-box}html{color-scheme:dark}
body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.55 -apple-system,"Helvetica Neue",Arial,sans-serif}
main{max-width:1240px;margin:0 auto;padding:32px 20px 80px}
h1,h2,h3{font-family:Georgia,"Iowan Old Style","Times New Roman",serif;font-weight:400;letter-spacing:.01em;text-wrap:balance;margin:0}
h1{font-size:2.2rem;line-height:1.15}h2{font-size:1.6rem}h3{font-size:1.15rem;color:var(--gold)}
.sub{color:var(--muted);margin:.5rem 0 0;max-width:62ch}
header{padding-bottom:24px;border-bottom:1px solid var(--rule)}
.intro p,.outro p{max-width:72ch}
.intro p,.outro p,.note p{margin:.6rem 0}
section.v{padding:36px 0;border-bottom:1px solid var(--rule)}
.vhead{display:flex;align-items:baseline;gap:14px;flex-wrap:wrap;margin-bottom:6px}
.id{font-family:var(--mono);color:var(--gold);font-size:.95rem;letter-spacing:.08em}
.thesis{color:var(--ink);font-size:1.05rem;margin:0 0 16px;max-width:72ch}
.shots{display:grid;gap:14px;grid-template-columns:repeat(auto-fit,minmax(min(100%,520px),1fr))}
figure{margin:0;background:var(--panel);border-radius:6px;overflow:hidden}
figure img{display:block;width:100%;height:auto}
figcaption{padding:6px 10px;font-family:var(--mono);font-size:.78rem;color:var(--muted)}
.meta{display:grid;grid-template-columns:auto 1fr;gap:6px 16px;margin-top:16px;font-size:.95rem}
.meta dt{color:var(--muted);text-transform:uppercase;letter-spacing:.08em;font-size:.72rem;padding-top:.25rem}
.meta dd{margin:0}
.note{background:var(--panel);border-left:3px solid var(--gold);padding:10px 16px;margin-top:14px;max-width:80ch}
table{border-collapse:collapse;width:100%;font-size:.95rem;margin-top:12px}
th,td{border:1px solid var(--rule);padding:8px 10px;text-align:left;vertical-align:top}
th{color:var(--muted);font-weight:500;font-size:.8rem;text-transform:uppercase;letter-spacing:.06em}
td:not(:first-child){font-family:var(--mono);color:var(--muted)}
.wrap{overflow-x:auto}
.compare{display:grid;gap:14px;grid-template-columns:repeat(auto-fit,minmax(min(100%,360px),1fr));margin-top:20px}
a{color:var(--gold)}
@media (prefers-reduced-motion:no-preference){figure img{transition:transform .25s ease}figure:hover img{transform:scale(1.01)}}
@page{size:A4 landscape;margin:12mm;background:#15120e}
@media print{
  html,body{background:var(--bg) !important;-webkit-print-color-adjust:exact;print-color-adjust:exact}
  body{font-size:15px}
  main{max-width:none;padding:0}
  header{border:0;padding-bottom:12px}
  section.v{break-before:page;border-bottom:0;padding:0;display:grid;grid-template-columns:1fr 1fr;gap:6px 28px;
    grid-template-areas:"head head" "thesis thesis" "shots shots" "refs refs" "note note";align-content:start}
  .vhead{grid-area:head}.thesis{grid-area:thesis;margin-bottom:6px}.shots{grid-area:shots;grid-template-columns:1fr 1fr}
  .shots figure img{max-height:26vh;width:auto;max-width:100%;margin:0 auto}
  .note .compare figure img{max-height:30vh;width:auto;max-width:100%;margin:0 auto}
  .meta{grid-area:refs;margin-top:6px}.note{grid-area:note;max-width:none;margin-top:8px}
  .note .compare{grid-template-columns:1fr 1fr 1fr;margin-top:10px}
  .compare{grid-template-columns:1fr 1fr 1fr}
  section.outro{break-before:page}
  figure,.note,table,.compare figure{break-inside:avoid}
  figure img{transition:none}
  a{text-decoration:none}
}
"""


def parse_md(path: pathlib.Path) -> dict:
    text = path.read_text(encoding="utf-8")
    title = re.search(r"^#\s+(.+)$", text, re.M)
    sub = re.search(r"^>\s+(.+)$", text, re.M)
    parts = re.split(r"^##\s+", text, flags=re.M)[1:]
    doc = {"title": title.group(1).strip() if title else path.parent.name,
           "sub": sub.group(1).strip() if sub else "", "intro": "", "outro": "", "variants": []}
    for part in parts:
        head, _, body = part.partition("\n")
        head = head.strip()
        if head in ("intro", "outro"):
            doc[head] = body.strip()
            continue
        vid, _, name = head.partition("·")
        variant = {"id": vid.strip(), "name": name.strip(), "thesis": "", "refs": "", "note": ""}
        current = None
        for line in body.splitlines():
            m = re.match(r"^(thesis|refs|note):\s*(.*)$", line)
            if m:
                current = m.group(1)
                variant[current] = m.group(2)
            elif current:
                variant[current] += "\n" + line
        doc["variants"].append(variant)
    return doc


IMG = None  # резолвер картинок, выставляется в build()


def paragraphs(text: str) -> str:
    out = []
    for block in re.split(r"\n\s*\n", text.strip()):
        block = block.strip()
        if block.startswith("!["):
            figs = []
            for alt, src in re.findall(r"!\[(.*?)\]\((.+?)\)", block):
                figs.append(f"<figure><img src='{IMG(src)}' alt='{html.escape(alt)}' ><figcaption>{html.escape(alt)}</figcaption></figure>")
            out.append("<div class='compare'>" + "".join(figs) + "</div>")
            continue
        block = html.escape(block)
        block = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", block)
        block = re.sub(r"\[(.+?)\]\((.+?)\)", r'<a href="\2">\1</a>', block)
        if block.startswith("|"):
            rows = [r.strip().strip("|").split("|") for r in block.splitlines() if not re.match(r"^\|[-| ]+\|$", r.strip())]
            head, *rest = rows
            out.append('<div class="wrap"><table><tr>' + "".join(f"<th>{c.strip()}</th>" for c in head) + "</tr>"
                       + "".join("<tr>" + "".join(f"<td>{c.strip()}</td>" for c in r) + "</tr>" for r in rest)
                       + "</table></div>")
        else:
            out.append(f"<p>{block.replace(chr(10), '<br>')}</p>")
    return "\n".join(out)


def img_src(path: pathlib.Path, base: pathlib.Path, embed: bool) -> str:
    if not embed:
        return html.escape(str(path.relative_to(base)) if path.is_relative_to(base) else str(path))
    from io import BytesIO

    from PIL import Image

    with Image.open(path) as image:
        image = image.convert("RGB")
        if image.width > 1400:
            image = image.resize((1400, int(image.height * 1400 / image.width)), Image.LANCZOS)
        buffer = BytesIO()
        image.save(buffer, format="JPEG", quality=84)
    return "data:image/jpeg;base64," + base64.b64encode(buffer.getvalue()).decode("ascii")


def build(folder: pathlib.Path, embed: bool, out: pathlib.Path) -> None:
    doc = parse_md(folder / "GALLERY.md")
    runs = [json.loads(l) for l in (folder / "out" / "runs.jsonl").read_text(encoding="utf-8").splitlines() if l.strip()]
    files: dict[str, list[dict]] = {}
    for r in runs:
        if r.get("ok"):
            files.setdefault(r["id"], []).append(r)
    base = out.parent
    global IMG
    IMG = lambda src: img_src((folder / src).resolve(), base, embed)  # noqa: E731
    parts = [f"<meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>{html.escape(doc['title'])}</title><style>{CSS}</style><main>",
             f"<header><h1>{html.escape(doc['title'])}</h1>",
             f"<p class='sub'>{html.escape(doc['sub'])}</p></header>" if doc["sub"] else "</header>"]
    if doc["intro"]:
        parts.append(f"<section class='intro'>{paragraphs(doc['intro'])}</section>")
    for v in doc["variants"]:
        parts.append(f"<section class='v' id='{html.escape(v['id'])}'><div class='vhead'><span class='id'>{html.escape(v['id'])}</span><h2>{html.escape(v['name'])}</h2></div>")
        if v["thesis"]:
            parts.append(f"<p class='thesis'>{html.escape(v['thesis'])}</p>")
        shots = files.get(v["id"], [])
        if shots:
            parts.append("<div class='shots'>")
            for r in shots:
                p = ROOT / r["file"]
                cap = f"{r['model']} · #{r.get('index', '')} · {r.get('seconds', '?')} с · ${r.get('cost') or 0:.3f}"
                parts.append(f"<figure><img src='{img_src(p, base, embed)}' alt='{html.escape(v['name'])}' ><figcaption>{html.escape(cap)}</figcaption></figure>")
            parts.append("</div>")
        else:
            parts.append("<p class='sub'>Рендеров пока нет.</p>")
        if v["refs"]:
            parts.append(f"<dl class='meta'><dt>Референсы</dt><dd>{html.escape(v['refs'])}</dd></dl>")
        if v["note"]:
            parts.append(f"<div class='note'><h3>Арт-директор</h3>{paragraphs(v['note'])}</div>")
        parts.append("</section>")
    if doc["outro"]:
        parts.append(f"<section class='outro'>{paragraphs(doc['outro'])}</section>")
    parts.append("</main>")
    out.write_text("\n".join(parts), encoding="utf-8")
    print(out, f"{out.stat().st_size / 1024:.0f} КБ")


CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"


def to_pdf(folder: pathlib.Path, pdf: pathlib.Path) -> None:
    """Собирает встроенную галерею и печатает её headless Chrome в PDF."""
    import subprocess
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        page = pathlib.Path(tmp) / "print.html"
        build(folder, True, page)
        body = page.read_text(encoding="utf-8")
        head, _, rest = body.partition("<main>")
        page.write_text(f'<!doctype html><html lang="ru"><head>{head}</head><body><main>{rest}</body></html>', encoding="utf-8")
        subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
                        "--virtual-time-budget=10000", f"--print-to-pdf={pdf}", page.as_uri()],
                       check=True, capture_output=True)
    print(pdf, f"{pdf.stat().st_size / 1024:.0f} КБ")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("folder")
    parser.add_argument("--embed", metavar="OUT_HTML", help="встроить картинки и записать по этому пути")
    parser.add_argument("--pdf", metavar="OUT_PDF", help="напечатать в PDF через headless Chrome")
    args = parser.parse_args()
    folder = pathlib.Path(args.folder).resolve()
    if not (folder / "GALLERY.md").is_file():
        sys.exit(f"Нет {folder / 'GALLERY.md'}")
    if args.pdf:
        if not pathlib.Path(CHROME).exists():
            sys.exit(f"Для PDF нужен Chrome: {CHROME}")
        to_pdf(folder, pathlib.Path(args.pdf).resolve())
    elif args.embed:
        build(folder, True, pathlib.Path(args.embed).resolve())
    else:
        build(folder, False, folder / "gallery.html")


if __name__ == "__main__":
    main()
