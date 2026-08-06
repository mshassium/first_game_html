#!/usr/bin/env python3
"""Проверки сгенерированного ассета против требований его же промпта.

Пороги не задаются здесь: они читаются из текста промпта в буке. Фразы вроде
«Leave 5% empty margin on all sides» и «Bright highlights occupy less than 8%»
написаны для модели, но одновременно являются измеримыми требованиями.

Что проверяется механически:
  формат      — размер и соотношение сторон;
  фон         — чисто белый (или чисто чёрный для VFX) по периметру;
  поля        — объект не подходит к краю ближе, чем требует промпт;
  яркость     — доля светлых пикселей внутри объекта;
  насыщенность— средняя насыщенность нейтральных зон;
  объекты     — сколько отдельных элементов на листе (для листов под нарезку).

Чего здесь нет и быть не может: оценки живописи и «та же ли это колода».
Это остаётся человеку и vision-судье.
"""

from __future__ import annotations

import re
from collections import deque
from dataclasses import dataclass, field

from PIL import Image


# --- требования из текста промпта -------------------------------------------

@dataclass
class Requirements:
    aspect_ratio: str | None = None
    width: int | None = None
    height: int | None = None
    background: str | None = None        # "white" | "black" | None
    margin_percent: float | None = None
    highlight_percent: float | None = None
    object_count: int | None = None
    grid: tuple[int, int] | None = None  # строк x столбцов, если промпт задаёт сетку
    quiet_centre: bool = False           # промпт требует ровный центр под текст/NinePatch


GRID_WORDS = {
    "one": 1, "two": 2, "three": 3, "four": 4, "five": 5,
    "six": 6, "seven": 7, "eight": 8, "nine": 9, "ten": 10,
    "sixteen": 16,
}


def requirements_from_prompt(prompt: str, slice_names: list[str] | None = None) -> Requirements:
    req = Requirements()

    match = re.search(r"aspect ratio (\d+:\d+),\s*size (\d+)x(\d+)", prompt)
    if match:
        req.aspect_ratio = match.group(1)
        req.width, req.height = int(match.group(2)), int(match.group(3))

    if re.search(r"pure-black background", prompt):
        req.background = "black"
    elif re.search(r"pure-white background", prompt):
        req.background = "white"

    match = re.search(r"Leave (\d+)% empty margin", prompt)
    if match:
        req.margin_percent = float(match.group(1))

    match = re.search(r"[Bb]right highlights occupy less than (\d+)%", prompt)
    if match:
        req.highlight_percent = float(match.group(1))

    # Раскладка листа. Сетка надёжнее подсчёта связных областей: элемент вроде
    # рунического кольца по заданию состоит из разрозненных глифов и в одну область
    # не сложится никогда, а в свою ячейку сетки попадёт.
    for pattern in (re.search(r"(\w+)[- ]by[- ](\w+) grid", prompt),
                    re.search(r"(\w+) rows by (\w+) columns", prompt),
                    re.search(r"(\w+) rows of (\w+)", prompt)):
        if pattern:
            a = GRID_WORDS.get(pattern.group(1).lower())
            b = GRID_WORDS.get(pattern.group(2).lower())
            if a and b:
                req.grid = (a, b)
                break

    if slice_names:
        req.object_count = len(slice_names)
    elif req.grid:
        req.object_count = req.grid[0] * req.grid[1]

    req.quiet_centre = bool(re.search(r"central \d+%|centre 70%|nearly uniform", prompt))
    return req


# --- измерения --------------------------------------------------------------

@dataclass
class Finding:
    level: str      # "fail" | "warn" | "info"
    code: str
    message: str


@dataclass
class Report:
    findings: list[Finding] = field(default_factory=list)
    metrics: dict = field(default_factory=dict)

    def add(self, level: str, code: str, message: str) -> None:
        self.findings.append(Finding(level, code, message))

    @property
    def failed(self) -> bool:
        return any(f.level == "fail" for f in self.findings)


def _background_mask(image: Image.Image, background: str) -> tuple[list[list[bool]], int, int]:
    """Заливка от краёв: так уходит и градиент фона, и мягкая тень под объектом.

    Тот же приём, что в SheetSlicer.kt — фон не выбивается по абсолютному цвету,
    иначе светлые части самого объекта считаются фоном.
    """
    scale = 512 / max(image.width, image.height)
    small = image.convert("RGB")
    if scale < 1:
        small = small.resize((max(1, int(image.width * scale)), max(1, int(image.height * scale))))
    width, height = small.size
    pixels = small.load()

    def is_bg(x: int, y: int) -> bool:
        r, g, b = pixels[x, y]
        luma = 0.299 * r + 0.587 * g + 0.114 * b
        spread = max(r, g, b) - min(r, g, b)
        if background == "black":
            return luma < 24 and spread < 20
        return luma > 228 and spread < 26

    mask = [[False] * width for _ in range(height)]
    queue: deque[tuple[int, int]] = deque()
    for x in range(width):
        for y in (0, height - 1):
            if is_bg(x, y) and not mask[y][x]:
                mask[y][x] = True
                queue.append((x, y))
    for y in range(height):
        for x in (0, width - 1):
            if is_bg(x, y) and not mask[y][x]:
                mask[y][x] = True
                queue.append((x, y))

    while queue:
        x, y = queue.popleft()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < width and 0 <= ny < height and not mask[ny][nx] and is_bg(nx, ny):
                mask[ny][nx] = True
                queue.append((nx, ny))
    return mask, width, height


def _components(mask: list[list[bool]], width: int, height: int, min_area: int) -> int:
    """Число связных областей переднего плана — сколько объектов на листе."""
    seen = [[False] * width for _ in range(height)]
    count = 0
    for y in range(height):
        for x in range(width):
            if mask[y][x] or seen[y][x]:
                continue
            area = 0
            queue = deque([(x, y)])
            seen[y][x] = True
            while queue:
                cx, cy = queue.popleft()
                area += 1
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = cx + dx, cy + dy
                    if (0 <= nx < width and 0 <= ny < height
                            and not seen[ny][nx] and not mask[ny][nx]):
                        seen[ny][nx] = True
                        queue.append((nx, ny))
            if area >= min_area:
                count += 1
    return count


def inspect(path, req: Requirements) -> Report:
    report = Report()
    image = Image.open(path).convert("RGB")
    report.metrics["size"] = f"{image.width}x{image.height}"

    # 1. Формат.
    if req.aspect_ratio:
        want_w, want_h = (int(v) for v in req.aspect_ratio.split(":"))
        want = want_w / want_h
        got = image.width / image.height
        report.metrics["aspect"] = round(got, 3)
        if abs(got - want) / want > 0.02:
            report.add("fail", "aspect",
                       f"соотношение {got:.3f} вместо {want:.3f} ({req.aspect_ratio})")
    if req.width and image.width < req.width * 0.9:
        report.add("warn", "resolution",
                   f"ширина {image.width} меньше заказанных {req.width}")

    if not req.background:
        return report

    mask, mw, mh = _background_mask(image, req.background)
    report.metrics["background"] = req.background

    # 2. Чистота фона по периметру: сколько краевых пикселей не признаны фоном.
    border = [(x, y) for x in range(mw) for y in (0, mh - 1)]
    border += [(x, y) for y in range(mh) for x in (0, mw - 1)]
    dirty = sum(1 for x, y in border if not mask[y][x])
    share = dirty / len(border)
    report.metrics["border_dirty"] = round(share, 3)
    if share > 0.02:
        report.add("fail", "background",
                   f"{share:.0%} периметра — не чистый {req.background} фон "
                   "(объект упирается в край или фон залит не тем цветом)")

    # 3. Поля вокруг объекта.
    xs = [x for y in range(mh) for x in range(mw) if not mask[y][x]]
    ys = [y for y in range(mh) for x in range(mw) if not mask[y][x]]
    if not xs:
        report.add("fail", "empty", "на изображении не найден объект — весь холст считается фоном")
        return report
    left, right, top, bottom = min(xs), mw - 1 - max(xs), min(ys), mh - 1 - max(ys)
    margins = {
        "left": 100 * left / mw, "right": 100 * right / mw,
        "top": 100 * top / mh, "bottom": 100 * bottom / mh,
    }
    report.metrics["margins"] = {k: round(v, 1) for k, v in margins.items()}
    if req.margin_percent is not None:
        worst = min(margins.values())
        if worst < req.margin_percent - 1.5:
            side = min(margins, key=margins.get)
            report.add("fail", "margin",
                       f"поле {side} {worst:.1f}% при требуемых {req.margin_percent:.0f}%")

    # 4. Доля ярких пикселей — считается только внутри объекта, иначе белый фон
    #    сам по себе завалил бы метрику.
    small = image.resize((mw, mh))
    pixels = small.load()
    object_pixels = 0
    highlights = 0
    saturation_sum = 0.0
    luma_sum = 0.0
    for y in range(mh):
        for x in range(mw):
            if mask[y][x]:
                continue
            r, g, b = pixels[x, y]
            object_pixels += 1
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            luma_sum += luma
            if luma > 216:
                highlights += 1
            top_c, bottom_c = max(r, g, b), min(r, g, b)
            saturation_sum += 0 if top_c == 0 else (top_c - bottom_c) / top_c
    if object_pixels:
        highlight_share = 100 * highlights / object_pixels
        report.metrics["highlights_percent"] = round(highlight_share, 1)
        report.metrics["mean_saturation"] = round(saturation_sum / object_pixels, 3)
        # Средняя яркость самого объекта. Абсолютного порога у неё нет: бук в §11
        # просит сравнивать ассет с соседями по набору, чем и занимается команда audit.
        report.metrics["mean_luma"] = round(luma_sum / object_pixels, 1)
        if req.highlight_percent is not None and highlight_share > req.highlight_percent:
            report.add("fail", "highlights",
                       f"светлых пикселей {highlight_share:.1f}% при пределе "
                       f"{req.highlight_percent:.0f}% — ассет ярче, чем задумано")

    # 5. Раскладка листа. Если промпт задаёт сетку — проверяется занятость её ячеек;
    #    ориентация принимается любая, формулировки «two rows of four» и «two-by-four»
    #    в буке означают разное расположение.
    if req.grid:
        options = {req.grid, (req.grid[1], req.grid[0])}
        best = None
        for rows, cols in options:
            empty = _empty_cells(mask, mw, mh, rows, cols)
            if best is None or len(empty) < len(best[1]):
                best = ((rows, cols), empty)
        (rows, cols), empty = best
        report.metrics["grid"] = f"{rows}x{cols}, пустых ячеек {len(empty)}"
        if empty:
            report.add("fail", "grid",
                       f"в сетке {rows}x{cols} пусто ячеек: {len(empty)} "
                       f"({', '.join(f'{r + 1}:{c + 1}' for r, c in empty[:5])}) — "
                       "нарезка встанет неправильно")
    elif req.object_count:
        found = _components(mask, mw, mh, min_area=int(0.0015 * mw * mh))
        report.metrics["objects"] = found
        if found != req.object_count:
            report.add("fail", "objects",
                       f"на листе {found} объектов вместо {req.object_count} — "
                       "нарезка встанет неправильно")

    return report


def _empty_cells(mask, width: int, height: int, rows: int, cols: int) -> list[tuple[int, int]]:
    """Ячейки сетки, в которых почти нет содержимого.

    Порог низкий: тусклая пылинка или дымок занимают доли процента ячейки,
    но ячейка при этом не пустая.
    """
    empty = []
    for row in range(rows):
        for col in range(cols):
            x0, x1 = col * width // cols, (col + 1) * width // cols
            y0, y1 = row * height // rows, (row + 1) * height // rows
            filled = sum(1 for y in range(y0, y1) for x in range(x0, x1) if not mask[y][x])
            if filled < 0.002 * (x1 - x0) * (y1 - y0):
                empty.append((row, col))
    return empty


def format_report(report: Report) -> str:
    lines = []
    for key, value in report.metrics.items():
        lines.append(f"    {key}: {value}")
    for finding in report.findings:
        marker = {"fail": "ПРОВАЛ", "warn": "внимание", "info": "инфо"}[finding.level]
        lines.append(f"    [{marker}] {finding.code}: {finding.message}")
    if not report.findings:
        lines.append("    [ок] все механические проверки пройдены")
    return "\n".join(lines)
