#!/usr/bin/env python3
"""Рендер концепт-бордов для арт-лаборатории (docs/gdx/12-art-direction-lab).

В отличие от artgen, который делает *ассеты* по промпт-буку с проверками, здесь
рисуются *концепты*: целые экраны, настроение, направления стиля. Проверок нет —
судья смотрит глазами. Транспорт, ключ и сохранение переиспользуются из artgen.

Промпты берутся из markdown-файла эксперимента: каждый блок вида

    ### D2 — Кабинет типографа
    ```prompt
    ...английский промпт целиком...
    ```

становится заданием с идентификатором `D2`. Строка `refs: a.png, b.png` внутри
блока (первая строка) — референсы из assets_src/ или от корня репозитория.

Запуск:
    python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e0-directions/PROMPTS.md
    python3 tools/artlab/concept.py …/PROMPTS.md --only D2,D3 --n 2 --aspect 16:9
    python3 tools/artlab/concept.py …/PROMPTS.md --model google/gemini-3-pro-image

Результат ложится рядом с файлом промптов в `out/<ID>_<модель>_<номер>.png`,
каждый запрос пишется строкой в `out/runs.jsonl` (модель, цена, секунды).
"""

from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys
import time
import urllib.error

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "artgen"))
import artgen  # noqa: E402  (ключ, post, data_url, save_image, resolve_ref)

DEFAULT_MODEL = artgen.DEFAULT_MODEL
BLOCK = re.compile(r"^###\s+(?P<id>[A-Za-z0-9._-]+)\s*[—-]\s*(?P<title>.+?)\s*$\n(?:.*?\n)*?```prompt\n(?P<body>.*?)```",
                   re.M | re.S)


def parse_prompts(path: pathlib.Path) -> list[dict]:
    text = path.read_text(encoding="utf-8")
    tasks = []
    for match in BLOCK.finditer(text):
        body = match.group("body").strip()
        refs: list[str] = []
        first, _, rest = body.partition("\n")
        if first.lower().startswith("refs:"):
            refs = [r.strip() for r in first[5:].split(",") if r.strip()]
            body = rest.strip()
        tasks.append({"id": match.group("id"), "title": match.group("title").strip(),
                      "prompt": body, "refs": refs})
    if not tasks:
        sys.exit(f"В {path} не найдено ни одного блока ```prompt под заголовком ### ID — Название")
    return tasks


def render(task: dict, model: str, aspect: str, resolution: str, out: pathlib.Path,
           key: str, index: int) -> dict:
    payload: dict = {
        "model": model,
        "prompt": task["prompt"],
        "n": 1,
        "aspect_ratio": aspect,
        "resolution": resolution,
        "output_format": "png",
    }
    if task["refs"]:
        payload["input_references"] = [
            {"type": "image_url", "image_url": {"url": artgen.data_url(artgen.resolve_ref(r), 1024)}}
            for r in task["refs"]
        ]
    slug = model.split("/")[-1]
    target = out / f"{task['id']}_{slug}_{index}.png"
    started = time.monotonic()
    try:
        response = artgen.post(payload, key)
    except urllib.error.HTTPError as error:
        return {"id": task["id"], "model": model, "ok": False,
                "error": f"HTTP {error.code}: {error.read().decode('utf-8', 'replace')[:400]}"}
    except urllib.error.URLError as error:
        return {"id": task["id"], "model": model, "ok": False, "error": str(error)}
    images = response.get("data") or []
    record = {
        "id": task["id"], "title": task["title"], "model": model, "aspect": aspect,
        "resolution": resolution, "refs": task["refs"], "index": index,
        "seconds": round(time.monotonic() - started, 1),
        "cost": (response.get("usage") or {}).get("cost"), "ok": bool(images),
    }
    if images:
        artgen.save_image(images[0], target)
        record["file"] = str(target.relative_to(ROOT))
    else:
        record["error"] = json.dumps(response, ensure_ascii=False)[:400]
    return record


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("prompts", help="markdown-файл с блоками ```prompt")
    parser.add_argument("--only", help="ID через запятую")
    parser.add_argument("--model", default=DEFAULT_MODEL)
    parser.add_argument("--aspect", default="16:9")
    parser.add_argument("--resolution", default="2K")
    parser.add_argument("--n", type=int, default=1, help="сколько вариантов на промпт")
    parser.add_argument("--start", type=int, default=1, help="с какого номера нумеровать файлы")
    args = parser.parse_args()

    path = pathlib.Path(args.prompts).resolve()
    tasks = parse_prompts(path)
    if args.only:
        wanted = {w.strip() for w in args.only.split(",")}
        tasks = [t for t in tasks if t["id"] in wanted]
        missing = wanted - {t["id"] for t in tasks}
        if missing:
            sys.exit(f"Нет таких ID в файле: {', '.join(sorted(missing))}")
    out = path.parent / "out"
    out.mkdir(exist_ok=True)
    key = artgen.read_key()
    log = out / "runs.jsonl"
    total = 0.0
    for task in tasks:
        for i in range(args.start, args.start + args.n):
            record = render(task, args.model, args.aspect, args.resolution, out, key, i)
            with log.open("a", encoding="utf-8") as handle:
                handle.write(json.dumps(record, ensure_ascii=False) + "\n")
            if record["ok"]:
                total += record.get("cost") or 0
                print(f"✓ {task['id']} #{i}: {record['file']}  {record['seconds']} с, ${record.get('cost')}")
            else:
                print(f"✗ {task['id']} #{i}: {record['error']}")
    print(f"Итого ≈ ${total:.3f}")


if __name__ == "__main__":
    main()
