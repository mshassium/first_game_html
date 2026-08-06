#!/usr/bin/env python3
"""Проверка связи с OpenRouter перед тем, как писать генератор ассетов.

Отвечает ровно на три вопроса:
  1. принимает ли модель картинку-референс вместе с запросом на генерацию;
  2. в каком поле ответа лежит готовое изображение;
  3. сколько стоит один такой запрос.

Ключ берётся из переменной окружения OPENROUTER_API_KEY либо из файла
~/.config/openrouter/key. В репозиторий он не попадает.

Запуск:
    python3 tools/artgen/smoke.py
    python3 tools/artgen/smoke.py --model openai/gpt-5-image --ref assets_src/anchor/card_F_anchor.png
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import pathlib
import sys
import time
import urllib.error
import urllib.request

API = "https://openrouter.ai/api/v1"
ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "tools" / "artgen" / "smoke_out"

# Намеренно дешёвый и короткий запрос: цель — плумбинг, а не картинка.
SMOKE_PROMPT = (
    "Create one small dark bronze circular medallion with a plain charcoal face, "
    "seen perfectly flat and straight on, isolated on a plain pure-white background. "
    "No text, no letters, no numbers, no watermark. Square image, aspect ratio 1:1."
)


def read_key() -> str:
    key = os.environ.get("OPENROUTER_API_KEY", "").strip()
    if key:
        return key
    path = pathlib.Path.home() / ".config" / "openrouter" / "key"
    if path.is_file():
        return path.read_text(encoding="utf-8").strip()
    sys.exit(
        "Ключ не найден. Задай OPENROUTER_API_KEY или положи его в "
        f"{path} (chmod 600)."
    )


def post(path: str, payload: dict, key: str, timeout: int = 300) -> dict:
    request = urllib.request.Request(
        f"{API}{path}",
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://github.com/first-game",
            "X-Title": "F!RST asset generation",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        body = error.read().decode("utf-8", "replace")
        sys.exit(f"HTTP {error.code} от OpenRouter:\n{body}")


def get(path: str, key: str) -> dict:
    request = urllib.request.Request(
        f"{API}{path}", headers={"Authorization": f"Bearer {key}"}
    )
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        return {"error": f"HTTP {error.code}: {error.read().decode('utf-8', 'replace')}"}


def data_url(path: pathlib.Path) -> str:
    suffix = path.suffix.lower()
    mime = "image/jpeg" if suffix in (".jpg", ".jpeg") else "image/png"
    return f"data:{mime};base64," + base64.b64encode(path.read_bytes()).decode("ascii")


def collect_images(message: dict) -> list[str]:
    """Изображение может приехать в message.images или внутри content-массива."""
    found: list[str] = []
    for item in message.get("images") or []:
        url = item.get("image_url", {}).get("url") if isinstance(item, dict) else item
        if isinstance(url, str):
            found.append(url)
    content = message.get("content")
    if isinstance(content, list):
        for part in content:
            if isinstance(part, dict) and part.get("type") in ("image_url", "image"):
                url = part.get("image_url", {}).get("url") or part.get("url")
                if isinstance(url, str):
                    found.append(url)
    return found


def save(url: str, target: pathlib.Path) -> pathlib.Path:
    if url.startswith("data:"):
        payload = url.split(",", 1)[1]
        target.write_bytes(base64.b64decode(payload))
    else:
        with urllib.request.urlopen(url, timeout=120) as response:
            target.write_bytes(response.read())
    return target


def describe(path: pathlib.Path) -> str:
    try:
        from PIL import Image
    except ImportError:
        return f"{path.stat().st_size / 1024:.0f} КБ"
    with Image.open(path) as image:
        return f"{image.width}x{image.height} {image.mode}, {path.stat().st_size / 1024:.0f} КБ"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", default="openai/gpt-5-image-mini")
    parser.add_argument("--ref", action="append", default=[],
                        help="путь к референсу; можно повторять")
    parser.add_argument("--prompt", default=SMOKE_PROMPT)
    args = parser.parse_args()

    key = read_key()
    OUT.mkdir(parents=True, exist_ok=True)

    content: list[dict] = [{"type": "text", "text": args.prompt}]
    for raw in args.ref:
        path = pathlib.Path(raw)
        if not path.is_absolute():
            path = ROOT / path
        if not path.is_file():
            sys.exit(f"Референс не найден: {path}")
        content.append({"type": "image_url", "image_url": {"url": data_url(path)}})
        print(f"референс: {path.relative_to(ROOT)} ({describe(path)})")

    payload = {
        "model": args.model,
        "messages": [{"role": "user", "content": content}],
        "modalities": ["image", "text"],
        "usage": {"include": True},
    }

    print(f"модель:   {args.model}")
    started = time.monotonic()
    response = post("/chat/completions", payload, key)
    elapsed = time.monotonic() - started

    raw_dump = OUT / "response.json"
    trimmed = json.loads(json.dumps(response))
    for choice in trimmed.get("choices") or []:
        for image in choice.get("message", {}).get("images") or []:
            url = image.get("image_url", {}).get("url", "")
            if url.startswith("data:"):
                image["image_url"]["url"] = url[:80] + f"...<{len(url)} символов>"
    raw_dump.write_text(json.dumps(trimmed, ensure_ascii=False, indent=2), encoding="utf-8")

    choices = response.get("choices") or []
    if not choices:
        sys.exit(f"В ответе нет choices. Полный ответ в {raw_dump}")
    message = choices[0].get("message", {})
    print(f"\nответ за {elapsed:.1f} с")
    print(f"поля message: {sorted(message.keys())}")
    print(f"finish_reason: {choices[0].get('finish_reason')}")

    images = collect_images(message)
    if not images:
        text = message.get("content")
        print("\nИЗОБРАЖЕНИЯ НЕТ. Текст ответа:")
        print(text if isinstance(text, str) else json.dumps(text, ensure_ascii=False)[:800])
        print(f"\nПолный ответ: {raw_dump}")
    else:
        for index, url in enumerate(images):
            target = save(url, OUT / f"smoke_{index}.png")
            print(f"картинка -> {target.relative_to(ROOT)} ({describe(target)})")

    usage = response.get("usage") or {}
    print(f"\nusage: {json.dumps(usage, ensure_ascii=False)}")
    cost = usage.get("cost")
    if cost is not None:
        print(f"стоимость запроса: ${cost:.4f}")

    generation_id = response.get("id")
    if generation_id:
        time.sleep(2)
        detail = get(f"/generation?id={generation_id}", key).get("data") or {}
        if detail:
            print(
                "детально: total_cost=${:.4f}, tokens_prompt={}, tokens_completion={}".format(
                    detail.get("total_cost") or 0.0,
                    detail.get("tokens_prompt"),
                    detail.get("tokens_completion"),
                )
            )


if __name__ == "__main__":
    main()
