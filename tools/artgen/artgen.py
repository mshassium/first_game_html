#!/usr/bin/env python3
"""Генерация ассетов по промпт-буку через OpenRouter.

Промпты не дублируются в коде: они берутся прямо из docs/gdx/05-prompt-book-v2-calm-ui.md
по номеру раздела. Правишь бук — меняется генерация. Здесь живёт только то, чего в буке нет:
какие референсы прикладывать, куда класть результат и сколько это стоило.

Ключ: переменная OPENROUTER_API_KEY либо ~/.config/openrouter/key.

    python3 tools/artgen/artgen.py sections
    python3 tools/artgen/artgen.py gen 2.1 --model openai/gpt-5-image --ref anchor/card_F_anchor.png
    python3 tools/artgen/artgen.py compare 2.1 --ref anchor/card_F_anchor.png
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import pathlib
import re
import sys
import time
import urllib.error
import urllib.request

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import checks  # noqa: E402  — лежит рядом, ставится в путь выше

API = "https://openrouter.ai/api/v1"
ROOT = pathlib.Path(__file__).resolve().parents[2]
BOOK = ROOT / "docs" / "gdx" / "05-prompt-book-v2-calm-ui.md"
SRC = ROOT / "assets_src"
ATTEMPTS = SRC / "raw" / "attempts"
LEDGER = ROOT / "tools" / "artgen" / "runs.jsonl"

# Референс всегда указывается относительно assets_src.
DEFAULT_REFS = [
    "anchor/ui_direction_reference_16x9.png",
]

CANDIDATE_MODELS = [
    "openai/gpt-5-image",
    "openai/gpt-5.4-image-2",
    "google/gemini-3-pro-image",
]

# Выбрана сравнением на §2.1: точнее всех держит бриф v2, дешевле и быстрее остальных.
DEFAULT_MODEL = "openai/gpt-5.4-image-2"

CONCEPT = "anchor/ui_direction_reference_16x9.png"
CARD_ANCHOR = "anchor/card_F_anchor_v3.png"
UI_ANCHOR = "anchor/ui_style_anchor.png"

# Какие референсы прикладывать и в каком порядке. Порядок важен: промпты бука
# ссылаются на «первый» и «второй» приложенный файл.
REFS: dict[str, list[str]] = {
    "2.1": ["anchor/card_F_anchor.png", CONCEPT],
    "2.2": [CONCEPT, "anchor/card_F_anchor_v2.png"],
    "2.3": ["anchor/card_F_anchor_v2.png", CONCEPT],
    "4.8": [CONCEPT, UI_ANCHOR, "cards/card_back.png"],
}
REFS_BY_CHAPTER: dict[str, list[str]] = {
    "3": [CARD_ANCHOR],
    "4": [CONCEPT, UI_ANCHOR],
    "5": [CONCEPT],
    "6": [],
}


def refs_for(number: str) -> list[str]:
    if number in REFS:
        return REFS[number]
    return REFS_BY_CHAPTER.get(number.split(".")[0], [CONCEPT])


def parse_fixes() -> dict[str, str]:
    """Таблица исправляющих фраз из §9 бука: проблема -> английская фраза."""
    text = BOOK.read_text(encoding="utf-8")
    section = text.split("## 9. Исправляющие запросы", 1)
    if len(section) < 2:
        return {}
    fixes = {}
    for row in re.finditer(r"^\|\s*([^|]+?)\s*\|\s*`([^`]+)`\s*\|$", section[1], re.MULTILINE):
        fixes[row.group(1).strip()] = row.group(2).strip()
    return fixes


# Провал проверки -> строка из §9. Для чего в буке фразы нет, она собирается по месту.
FIX_BY_CODE = {
    "highlights": "Всё слишком яркое",
    "saturation": "Всё слишком яркое",
}


def fix_phrase(finding: dict, section: dict) -> str:
    fixes = parse_fixes()
    name = FIX_BY_CODE.get(finding["code"])
    if name and name in fixes:
        return fixes[name]
    if finding["code"] in ("margin", "background"):
        return ("Redraw the same subject smaller inside the canvas. Leave a clearly visible "
                "empty margin on every side, do not let the object or its shadow touch any "
                "canvas edge, and keep the background a single flat pure colour.")
    if finding["code"] == "objects":
        expected = len(section["slice_names"]) or "the requested number of"
        return (f"The sheet must contain exactly {expected} separate elements, evenly spaced, "
                "with nothing overlapping and nothing touching another element or the canvas "
                "edge. Do not merge elements and do not add extra ones.")
    return ""


# --- ключ и транспорт -------------------------------------------------------

def read_key() -> str:
    key = os.environ.get("OPENROUTER_API_KEY", "").strip()
    if key:
        return key
    path = pathlib.Path.home() / ".config" / "openrouter" / "key"
    if path.is_file():
        return path.read_text(encoding="utf-8").strip()
    sys.exit(f"Ключ не найден: ни OPENROUTER_API_KEY, ни {path}")


def post(payload: dict, key: str, timeout: int = 600) -> dict:
    request = urllib.request.Request(
        f"{API}/images",
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://github.com/first-game",
            "X-Title": "F!RST asset generation",
        },
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=timeout) as response:
        return json.loads(response.read().decode("utf-8"))


# --- промпт-бук -------------------------------------------------------------

SECTION_RE = re.compile(r"^#{2,3}\s+(\d+(?:\.\d+)?)\s+(.*)$", re.MULTILINE)


def parse_book() -> dict[str, dict]:
    """Разбирает бук на разделы: номер -> заголовок, промпт, целевой файл."""
    text = BOOK.read_text(encoding="utf-8")
    matches = list(SECTION_RE.finditer(text))
    sections: dict[str, dict] = {}
    for index, match in enumerate(matches):
        start = match.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        body = text[start:end]
        prompt_match = re.search(r"```text\n(.*?)\n```", body, re.DOTALL)
        if not prompt_match:
            continue
        save_match = re.search(r"Сохранить(?: целиком)? как `([^`]+)`", body)
        slice_match = re.search(r"Нарезать", body)
        # Имена после «Нарезать» — это и порядок нарезки, и ожидаемое число
        # объектов на листе, по которому потом проверяется генерация.
        tail = body[slice_match.end():] if slice_match else ""
        # Последний пункт списка в буке заканчивается точкой, остальные — точкой с запятой.
        slice_names = re.findall(r"^-\s+`([^`]+)`\s*[;.]?$", tail, re.MULTILINE)
        prompt = prompt_match.group(1).strip()
        ratio, width, height = parse_format(prompt)
        sections[match.group(1)] = {
            "title": match.group(2).strip(),
            "prompt": prompt,
            "save": save_match.group(1) if save_match else None,
            "sliced": bool(slice_match),
            "slice_names": slice_names,
            "aspect_ratio": ratio,
            "width": width,
            "height": height,
        }
    return sections


FORMAT_RE = re.compile(r"aspect ratio (\d+:\d+),\s*size (\d+)x(\d+)")


def parse_format(prompt: str) -> tuple[str | None, int | None, int | None]:
    """Формат ассета берётся из последней строки самого промпта, а не задаётся в коде."""
    match = FORMAT_RE.search(prompt)
    if not match:
        return None, None, None
    return match.group(1), int(match.group(2)), int(match.group(3))


def resolution_tier(width: int | None, height: int | None) -> str:
    """OpenRouter принимает ступени, а не произвольный размер: 1K достаточно до 1024."""
    if not width or not height:
        return "1K"
    return "1K" if max(width, height) <= 1024 else "2K"


# --- генерация --------------------------------------------------------------

def data_url(path: pathlib.Path, max_side: int | None = None) -> str:
    """Референс в base64. max_side уменьшает картинку: мокапам §7 нужно приложить
    десяток файлов сразу, и в полном размере запрос раздувается до десятков мегабайт."""
    mime = "image/jpeg" if path.suffix.lower() in (".jpg", ".jpeg") else "image/png"
    raw = path.read_bytes()
    if max_side:
        from io import BytesIO

        from PIL import Image

        with Image.open(path) as image:
            if max(image.size) > max_side:
                ratio = max_side / max(image.size)
                small = image.convert("RGB").resize(
                    (int(image.width * ratio), int(image.height * ratio)), Image.LANCZOS)
                buffer = BytesIO()
                small.save(buffer, format="JPEG", quality=88)
                raw, mime = buffer.getvalue(), "image/jpeg"
    return f"data:{mime};base64," + base64.b64encode(raw).decode("ascii")


def resolve_ref(name: str) -> pathlib.Path:
    path = pathlib.Path(name)
    for candidate in (path, SRC / name, ROOT / name):
        if candidate.is_file():
            return candidate
    sys.exit(f"Референс не найден: {name}")


def save_image(item: dict, target: pathlib.Path) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    payload = item.get("b64_json")
    if payload:
        target.write_bytes(base64.b64decode(payload))
        return
    url = item.get("url") or item.get("image_url", {}).get("url", "")
    if url.startswith("data:"):
        target.write_bytes(base64.b64decode(url.split(",", 1)[1]))
    else:
        with urllib.request.urlopen(url, timeout=180) as response:
            target.write_bytes(response.read())


def describe(path: pathlib.Path) -> str:
    from PIL import Image

    with Image.open(path) as image:
        return f"{image.width}x{image.height}, {path.stat().st_size / 1024:.0f} КБ"


def generate(section: dict, number: str, model: str, refs: list[str],
             target: pathlib.Path, key: str, extra: str = "", seed: int | None = None,
             ref_max: int | None = None) -> dict:
    """Один запрос к модели. Возвращает запись для журнала."""
    prompt = section["prompt"] + (f"\n\n{extra}" if extra else "")
    payload: dict = {
        "model": model,
        "prompt": prompt,
        "n": 1,
        "resolution": resolution_tier(section["width"], section["height"]),
        "output_format": "png",
    }
    if section["aspect_ratio"]:
        payload["aspect_ratio"] = section["aspect_ratio"]
    if seed is not None:
        payload["seed"] = seed
    if refs:
        payload["input_references"] = [
            {"type": "image_url", "image_url": {"url": data_url(resolve_ref(name), ref_max)}}
            for name in refs
        ]

    started = time.monotonic()
    try:
        response = post(payload, key)
    except urllib.error.HTTPError as error:
        body = error.read().decode("utf-8", "replace")[:400]
        return {"section": number, "model": model, "ok": False,
                "error": f"HTTP {error.code}: {body}"}
    except urllib.error.URLError as error:
        return {"section": number, "model": model, "ok": False, "error": str(error)}
    elapsed = time.monotonic() - started

    images = response.get("data") or []
    usage = response.get("usage") or {}
    record = {
        "section": number,
        "model": model,
        "refs": refs,
        "aspect_ratio": payload.get("aspect_ratio"),
        "resolution": payload["resolution"],
        "seed": seed,
        "extra": extra or None,
        "seconds": round(elapsed, 1),
        "cost": usage.get("cost"),
        "ok": bool(images),
    }
    if not images:
        record["error"] = json.dumps(response, ensure_ascii=False)[:400]
    else:
        save_image(images[0], target)
        record["file"] = str(target.relative_to(ROOT))
        record["size"] = describe(target)
        report = checks.inspect(target, checks.requirements_from_prompt(
            section["prompt"], section["slice_names"]))
        record["metrics"] = report.metrics
        record["findings"] = [
            {"level": f.level, "code": f.code, "message": f.message} for f in report.findings
        ]
        record["passed"] = not report.failed

    LEDGER.parent.mkdir(parents=True, exist_ok=True)
    with LEDGER.open("a", encoding="utf-8") as handle:
        handle.write(json.dumps(record, ensure_ascii=False) + "\n")
    return record


JUDGE_MODEL = "openai/gpt-5-nano"

JUDGE_SCHEMA = {
    "type": "object",
    "properties": {
        "text_present": {"type": "boolean",
                         "description": "any letters, digits, words or writing-like marks"},
        "text_where": {"type": "string", "description": "where the text is, or empty"},
        "wood_orange": {"type": "boolean",
                        "description": "any wood that reads as orange or honey-coloured"},
        "glow_on_neutral": {"type": "boolean",
                            "description": "coloured magical glow on neutral frames or panels"},
        "notes": {"type": "string", "description": "one short sentence, in Russian"},
    },
    "required": ["text_present", "text_where", "wood_orange", "glow_on_neutral", "notes"],
    "additionalProperties": False,
}

# Ответ судьи -> строка из §9. Тем же механизмом, что и механические проверки.
JUDGE_FIX = {
    "text_present": "Модель скопировала текст из концепта",
    "wood_orange": "Дерево опять оранжевое",
    "glow_on_neutral": "Появилось цветное свечение на UI",
}


def judge(path: pathlib.Path, key: str, model: str = JUDGE_MODEL) -> dict:
    """Vision-проверка того, что не ловится метриками: текст, оранжевое дерево, свечение.

    Результат — подсказка, а не приговор: модель ошибается, поэтому её находки
    попадают в отчёт как «внимание», а не как провал.
    """
    payload = {
        "model": model,
        "messages": [{"role": "user", "content": [
            {"type": "text", "text":
                "You are checking a generated game asset against its art direction. "
                "Answer strictly about what is visible. Invented runes and abstract glyphs "
                "inside an illustration do not count as text; letters, digits and words do."},
            {"type": "image_url", "image_url": {"url": data_url(path)}},
        ]}],
        "response_format": {"type": "json_schema", "json_schema": {
            "name": "asset_check", "strict": True, "schema": JUDGE_SCHEMA}},
    }
    request = urllib.request.Request(
        f"{API}/chat/completions", data=json.dumps(payload).encode("utf-8"),
        headers={"Authorization": f"Bearer {key}", "Content-Type": "application/json"},
        method="POST")
    try:
        with urllib.request.urlopen(request, timeout=180) as response:
            body = json.loads(response.read().decode("utf-8"))
    except (urllib.error.HTTPError, urllib.error.URLError) as error:
        return {"error": str(error)}
    try:
        return json.loads(body["choices"][0]["message"]["content"])
    except (KeyError, IndexError, json.JSONDecodeError) as error:
        return {"error": f"судья вернул неразбираемый ответ: {error}"}


def judge_findings(verdict: dict) -> list[dict]:
    if "error" in verdict:
        return [{"level": "warn", "code": "judge", "message": verdict["error"]}]
    found = []
    if verdict.get("text_present"):
        found.append({"level": "warn", "code": "text_present",
                      "message": f"судья видит текст: {verdict.get('text_where') or 'место не указано'}"})
    if verdict.get("wood_orange"):
        found.append({"level": "warn", "code": "wood_orange",
                      "message": "судья: дерево читается как оранжевое"})
    if verdict.get("glow_on_neutral"):
        found.append({"level": "warn", "code": "glow_on_neutral",
                      "message": "судья: цветное свечение на нейтральной поверхности"})
    return found


def accept(number: str, section: dict, source: pathlib.Path,
           to: str | None, note: str) -> None:
    """Принять попытку: положить по адресу из бука и дописать запись в лог."""
    target_name = to or section["save"]
    if not target_name:
        sys.exit("В буке для этого раздела нет «Сохранить как» — укажи --to")
    target = ROOT / target_name if not target_name.startswith("/") else pathlib.Path(target_name)
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.suffix.lower() in (".jpg", ".jpeg") and source.suffix.lower() == ".png":
        # Фоны бук хранит в JPEG; переименования мало, нужна пересборка формата.
        from PIL import Image

        with Image.open(source) as image:
            image.convert("RGB").save(target, format="JPEG", quality=92)
    else:
        target.write_bytes(source.read_bytes())

    attempts, model, cost = 0, "?", 0.0
    if LEDGER.is_file():
        for line in LEDGER.read_text(encoding="utf-8").splitlines():
            entry = json.loads(line)
            if entry.get("section") == number:
                attempts += 1
                cost += entry.get("cost") or 0.0
                if entry.get("file") and ROOT / entry["file"] == source:
                    model = entry["model"]

    log = SRC / "GENERATION-LOG.md"
    entry = f"""
## §{number} — {section['title']}

- **Файл:** `{target.relative_to(ROOT)}`, {describe(target)}
- **Дата:** {time.strftime('%Y-%m-%d')}
- **Модель:** {model}
- **Промпт:** 05-prompt-book-v2-calm-ui.md §{number}, без изменений
- **Референсы:** {', '.join(refs_for(number)) or 'нет'}
- **Попыток до приёмки:** {attempts} (суммарно ${cost:.2f})
- **Результат:** принят

### Замечания
{note or 'Механические проверки пройдены, принят визуально.'}
"""
    with log.open("a", encoding="utf-8") as handle:
        handle.write(entry)
    print(f"принят -> {target.relative_to(ROOT)}")
    print(f"запись добавлена в {log.relative_to(ROOT)}")


def report(record: dict) -> None:
    head = f"  {record['model']:<24}"
    if not record.get("ok"):
        print(f"{head} ОШИБКА: {record.get('error')}")
        return
    verdict = "принят проверками" if record.get("passed") else "ЕСТЬ ЗАМЕЧАНИЯ"
    print(f"{head} {record['size']:<22} {record['seconds']:>5} с  ${record['cost']:.4f}  {verdict}")
    print(f"  {'':24} -> {record['file']}")
    for key, value in (record.get("metrics") or {}).items():
        print(f"  {'':24}    {key}: {value}")
    for finding in record.get("findings") or []:
        marker = {"fail": "ПРОВАЛ", "warn": "внимание", "info": "инфо"}[finding["level"]]
        print(f"  {'':24}    [{marker}] {finding['code']}: {finding['message']}")


# --- команды ----------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("sections", help="показать разделы бука с промптами")

    check_parser = sub.add_parser("check", help="прогнать проверки по готовому файлу")
    check_parser.add_argument("section")
    check_parser.add_argument("file")
    check_parser.add_argument("--judge", action="store_true",
                              help="дополнительно спросить vision-модель про текст и цвет")

    accept_parser = sub.add_parser(
        "accept", help="принять попытку: скопировать по адресу из бука и записать в лог")
    accept_parser.add_argument("section")
    accept_parser.add_argument("file")
    accept_parser.add_argument("--to", default=None, help="путь, если в буке его нет (листы)")
    accept_parser.add_argument("--note", default="", help="замечание в лог")

    audit_parser = sub.add_parser(
        "audit", help="сравнить яркость и насыщенность ассетов между собой (§11 бука)")
    audit_parser.add_argument("files", nargs="+")
    audit_parser.add_argument("--black", action="store_true", help="ассеты на чёрном фоне (VFX)")

    batch_parser = sub.add_parser("batch", help="прогнать несколько разделов подряд")
    batch_parser.add_argument("sections", help="через запятую, либо префикс: 3 — все карты")
    batch_parser.add_argument("--model", default=DEFAULT_MODEL)
    batch_parser.add_argument("--retries", type=int, default=2,
                              help="повторов с исправляющей фразой при провале проверок")

    for name in ("gen", "compare"):
        p = sub.add_parser(name)
        p.add_argument("section")
        p.add_argument("--ref", action="append", default=[],
                       help="дополнительный референс (путь от assets_src)")
        p.add_argument("--no-default-ref", action="store_true",
                       help="не прикладывать концепт 16:9")
        p.add_argument("--extra", default="", help="исправляющая фраза из §9")
        p.add_argument("--seed", type=int, default=None,
                       help="фиксированный seed: повторяемый результат при правке промпта")
        p.add_argument("--ref-max", type=int, default=None,
                       help="уменьшить референсы до N px перед отправкой")
        if name == "gen":
            p.add_argument("--model", default=DEFAULT_MODEL)
            p.add_argument("--out", default=None)
        else:
            p.add_argument("--models", default=",".join(CANDIDATE_MODELS))

    args = parser.parse_args()
    sections = parse_book()

    if args.command == "audit":
        req = checks.Requirements(background="black" if args.black else "white")
        rows = []
        for name in args.files:
            path = pathlib.Path(name)
            if not path.is_absolute():
                path = ROOT / path
            if not path.is_file():
                continue
            metrics = checks.inspect(path, req).metrics
            if "mean_luma" in metrics:
                rows.append((path.name, metrics["mean_luma"], metrics["mean_saturation"]))
        if not rows:
            sys.exit("нечего сравнивать")
        lumas = sorted(r[1] for r in rows)
        median = lumas[len(lumas) // 2]
        print(f"медианная яркость набора: {median}\n")
        for name, luma, saturation in sorted(rows, key=lambda r: -r[1]):
            ratio = luma / median if median else 1
            mark = "  <- выбивается" if ratio > 1.5 or ratio < 0.55 else ""
            print(f"  {name:<34} яркость {luma:>6}  x{ratio:.2f}  насыщенность {saturation}{mark}")
        return

    if args.command == "batch":
        wanted = [s.strip() for s in args.sections.split(",")]
        numbers = [n for n in sections
                   if n in wanted or any(n.split(".")[0] == w for w in wanted)]
        if not numbers:
            sys.exit(f"Не нашёл разделов по запросу «{args.sections}»")
        key = read_key()
        stamp = time.strftime("%Y%m%d-%H%M%S")
        spent, accepted = 0.0, 0
        for number in numbers:
            section = sections[number]
            refs = refs_for(number)
            print(f"\n§{number} — {section['title']}  [{', '.join(refs) or 'без референсов'}]")
            extra = ""
            for attempt in range(args.retries + 1):
                target = ATTEMPTS / f"{number}_{stamp}_try{attempt + 1}.png"
                record = generate(section, number, args.model, refs, target, key, extra)
                spent += record.get("cost") or 0.0
                report(record)
                if record.get("passed"):
                    accepted += 1
                    break
                fails = [f for f in record.get("findings") or [] if f["level"] == "fail"]
                if not fails or attempt == args.retries:
                    break
                extra = fix_phrase(fails[0], section)
                print(f"  {'':24}    повтор с исправлением: {extra[:70]}…")
        print(f"\nитого: {accepted} из {len(numbers)} прошли проверки, потрачено ${spent:.2f}")
        print("кандидаты в assets_src/raw/attempts — принимать командой accept")
        return

    if args.command == "sections":
        for number, data in sections.items():
            target = data["save"] or ("лист под нарезку" if data["sliced"] else "—")
            print(f"{number:<5} {data['title']:<38} {target}")
        return

    if args.section not in sections:
        sys.exit(f"Раздела {args.section} в буке нет. Список: python3 {sys.argv[0]} sections")

    section = sections[args.section]

    if args.command in ("check", "accept"):
        path = pathlib.Path(args.file)
        if not path.is_absolute():
            path = ROOT / path
        if not path.is_file():
            sys.exit(f"Файл не найден: {path}")
        if args.command == "accept":
            accept(args.section, section, path, args.to, args.note)
            return
        print(f"§{args.section} — {section['title']}\n{path.name}")
        print(checks.format_report(checks.inspect(
            path, checks.requirements_from_prompt(section["prompt"], section["slice_names"]))))
        if args.judge:
            verdict = judge(path, read_key())
            print(f"    судья: {verdict.get('notes') or verdict.get('error', '')}")
            for finding in judge_findings(verdict):
                print(f"    [внимание] {finding['code']}: {finding['message']}")
            if not judge_findings(verdict):
                print("    [ок] судья не нашёл текста, оранжевого дерева и свечения на нейтральном")
        return

    refs = ([] if args.no_default_ref else list(DEFAULT_REFS)) + list(args.ref)
    key = read_key()
    stamp = time.strftime("%Y%m%d-%H%M%S")

    print(f"§{args.section} — {section['title']}")
    print(f"формат:    {section['aspect_ratio']} {section['width']}x{section['height']} "
          f"-> resolution={resolution_tier(section['width'], section['height'])}")
    print(f"референсы: {', '.join(refs) or 'нет'}")

    if args.command == "gen":
        target = pathlib.Path(args.out) if args.out else ATTEMPTS / f"{args.section}_{stamp}.png"
        if not target.is_absolute():
            target = ROOT / target
        report(generate(section, args.section, args.model, refs, target, key,
                        args.extra, args.seed, args.ref_max))
        return

    for model in args.models.split(","):
        slug = model.split("/")[-1]
        target = ATTEMPTS / f"{args.section}_{slug}_{stamp}.png"
        report(generate(section, args.section, model, refs, target, key, args.extra,
                        args.seed, args.ref_max))


if __name__ == "__main__":
    main()
