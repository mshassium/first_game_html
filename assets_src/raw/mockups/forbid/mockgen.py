#!/usr/bin/env python3
"""Мокапы визуализации запрета (карта F) через OpenRouter.

Одноразовый скрипт: берёт реальные скриншоты стола как референсы и просит модель
дорисовать на них варианты индикатора запрета. Промпты здесь, а не в промпт-буке:
это эскизы для выбора, а не ассеты.
"""
import base64
import concurrent.futures
import json
import pathlib
import sys
import time
import urllib.error
import urllib.request
from io import BytesIO

from PIL import Image

API = "https://openrouter.ai/api/v1/images"
MODEL = "openai/gpt-5.4-image-2"
HERE = pathlib.Path(__file__).resolve().parent
OUT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else HERE / "mockups"

REF_LANDSCAPE = HERE / "refL" / "03.png"
REF_PORTRAIT = HERE / "refP" / "06.png"
REF_CARD = HERE / "ref_card.png"


def key() -> str:
    return (pathlib.Path.home() / ".config" / "openrouter" / "key").read_text().strip()


def data_url(path: pathlib.Path, max_side: int = 1400) -> str:
    with Image.open(path) as image:
        ratio = min(1.0, max_side / max(image.size))
        small = image.convert("RGB").resize(
            (int(image.width * ratio), int(image.height * ratio)), Image.LANCZOS)
        buffer = BytesIO()
        small.save(buffer, format="JPEG", quality=90)
    return "data:image/jpeg;base64," + base64.b64encode(buffer.getvalue()).decode()


STYLE = """Ты рисуешь МОКАП интерфейса карточной игры F!RST.

Приложенные файлы — реальные скриншоты игрового стола: первый в горизонтальной
раскладке (десктоп), второй в вертикальной (мобильной), третий — крупный план
карты F в гнезде зоны SPACE.

Стиль держать один в один по референсам: тёмное каменное подземелье, панели из
камня и дерева, бронзовые резные рамы с уголками, приглушённая палитра, свечи,
живописный фэнтези-арт. Карта F — синяя, с окованным цепями замком и медальоном
буквы в левом верхнем углу. Цвет школы F — бледно-лазурный #9CC8FF.

ЗАДАЧА. По правилам игры карта F накладывает запрет: сторона называет букву, и
первая же попытка оппонента сыграть её сжигает карту. Запрет висит сколько угодно
ходов, и игроку нужно постоянное напоминание прямо на столе: какая буква названа
(для своего запрета) или хотя бы сам факт запрета (для чужого — буква скрыта).

Перерисуй ФРАГМЕНТ стола по референсу как можно точнее — тот же ряд SPACE, те же
пустые гнёзда, та же рама панели, та же карта F в первом гнезде — и добавь на него
ровно один новый элемент интерфейса, описанный ниже. Ничего другого не менять и не
придумывать. Никакого текста, кроме одной латинской буквы на самом индикаторе.
Названная буква в мокапе — S.

ЭЛЕМЕНТ: """

VARIANTS = {
    "01_corner_medallion": """бронзовая печать-медальон, посаженная на правый нижний
угол карты F и выступающая за её край примерно на треть своего диаметра. Медальон
круглый, того же литья, что бронзовые уголки панелей: витой ободок, тёмное поле,
на нём крупно выгравирована буква S с бледно-лазурным внутренним свечением. По
ободку — тонкий лазурный ореол, будто металл только что остыл. Иллюстрация карты
почти не закрыта.""",

    "02_frost_ribbon": """узкая обледеневшая лента-картуш поперёк нижней трети карты F,
во всю её ширину и чуть выступающая за боковые края. Лента бронзовая, с резными
концами, покрыта инеем; по центру ленты — буква S бледно-лазурного цвета. От ленты
вверх и вниз по карте расходится лёгкая изморозь. Верхние две трети иллюстрации
остаются открытыми.""",

    "03_hanging_lock": """массивный навесной замок на короткой цепи, свисающий с нижней
кромки карты F в пустоту гнезда под ней. Замок железный, покрыт инеем, на его корпусе
выбита буква S; цепь уходит вверх и обвивает нижний угол карты. Замок висит ниже
карты и её иллюстрацию не закрывает, лёгкое лазурное свечение снизу.""",

    "04_panel_plaque": """бронзовая табличка, врезанная в раму панели зоны SPACE прямо
под гнездом карты F: узкий прямоугольник с резными краями, тёмное поле, на нём буква S
и мелкий орнамент цепи по бокам. Табличка выглядит частью самой рамы, а не наклейкой
поверх неё; вокруг буквы слабое лазурное свечение. Сама карта F не тронута совсем,
только её рамка чуть подсвечена лазурным.""",

    "05_rune_banner": """вертикальный тканевый вымпел цвета выцветшей лазури с бронзовой
окантовкой, подвешенный на кольце к левому краю карты F и свисающий вдоль неё снаружи,
в зазор между картой и краем панели. На вымпеле сверху вниз — руническая вязь и крупная
буква S. Вымпел уже карты примерно втрое, иллюстрацию не перекрывает.""",

    "06_frozen_card": """сама карта F закована: по её периметру нарос иней и толстая
ледяная кромка, поверх иллюстрации протянуты две скрещённые ледяные цепи, а в правом
верхнем углу карты — маленький ледяной кристалл-огранка с буквой S внутри. Никаких
отдельных элементов вне габарита карты.""",
}


def request(name: str, prompt: str, layout: str, api_key: str) -> str:
    ratio, size = ("3:2", "2K") if layout == "landscape" else ("2:3", "2K")
    payload = {
        "model": MODEL,
        "prompt": prompt + (
            "\n\nФОРМАТ: горизонтальный фрагмент десктопной раскладки, "
            "aspect ratio 3:2, size 1536x1024."
            if layout == "landscape" else
            "\n\nФОРМАТ: вертикальный фрагмент мобильной раскладки, "
            "aspect ratio 2:3, size 1024x1536."),
        "n": 1,
        "aspect_ratio": ratio,
        "resolution": size,
        "output_format": "png",
        "input_references": [
            {"type": "image_url", "image_url": {"url": data_url(REF_LANDSCAPE)}},
            {"type": "image_url", "image_url": {"url": data_url(REF_PORTRAIT)}},
            {"type": "image_url", "image_url": {"url": data_url(REF_CARD, 700)}},
        ],
    }
    started = time.monotonic()
    req = urllib.request.Request(
        API, data=json.dumps(payload).encode(), method="POST",
        headers={"Authorization": f"Bearer {api_key}",
                 "Content-Type": "application/json",
                 "X-Title": "F!RST forbid mockups"})
    try:
        with urllib.request.urlopen(req, timeout=600) as response:
            body = json.loads(response.read().decode())
    except urllib.error.HTTPError as error:
        return f"{name}/{layout}: HTTP {error.code} {error.read().decode()[:200]}"
    images = body.get("data") or []
    if not images:
        return f"{name}/{layout}: пусто {json.dumps(body)[:200]}"
    item = images[0]
    raw = item.get("b64_json")
    if raw:
        blob = base64.b64decode(raw)
    else:
        url = item.get("url") or item.get("image_url", {}).get("url", "")
        if url.startswith("data:"):
            blob = base64.b64decode(url.split(",", 1)[1])
        else:
            with urllib.request.urlopen(url, timeout=180) as response:
                blob = response.read()
    OUT.mkdir(parents=True, exist_ok=True)
    target = OUT / f"{name}_{layout}.png"
    target.write_bytes(blob)
    cost = (body.get("usage") or {}).get("cost")
    return (f"{name}/{layout}: {target.name} "
            f"{len(blob)//1024} КБ, {time.monotonic() - started:.0f} с, ${cost}")


def main() -> None:
    api_key = key()
    jobs = [(name, STYLE + text, layout)
            for name, text in VARIANTS.items()
            for layout in ("landscape", "portrait")]
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        futures = [pool.submit(request, *job, api_key) for job in jobs]
        for future in concurrent.futures.as_completed(futures):
            print(future.result(), flush=True)


if __name__ == "__main__":
    main()
