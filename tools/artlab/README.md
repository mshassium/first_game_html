# artlab — инструменты арт-лаборатории

Обслуживает [docs/gdx/12-art-direction-lab](../../docs/gdx/12-art-direction-lab/00-plan.md).
В отличие от `artgen` (финальные ассеты по промпт-буку с проверками), здесь рисуются
концепты и собираются страницы для судьи. Ключ OpenRouter — тот же, что у artgen.

```bash
python3 tools/artlab/concept.py docs/gdx/12-art-direction-lab/e0-directions/PROMPTS.md            # рендер концептов
python3 tools/artlab/concept.py …/PROMPTS.md --only D2 --n 2 --start 3                             # ещё два варианта D2
python3 tools/artlab/gallery.py docs/gdx/12-art-direction-lab/e0-directions                        # gallery.html рядом
python3 tools/artlab/gallery.py …/e0-directions --embed /tmp/e0.html                               # с картинками внутри — для публикации
python3 tools/artlab/gallery.py …/e0-directions --pdf …/e0-directions/gallery.pdf                  # PDF, A4 альбом, страница на вариант
```

Формат `PROMPTS.md` описан в шапке `concept.py`, формат `GALLERY.md` — в шапке `gallery.py`.
Каждый запрос пишется в `out/runs.jsonl` эксперимента (модель, секунды, цена).

```bash
python3 tools/artlab/embed.py docs/gdx/12-art-direction-lab/e1-living-card/lab.html /tmp/e1.html   # автономный прототип с встроенными картинками
```

```bash
python3 tools/artlab/cardtest.py docs/gdx/12-art-direction-lab/e2-letter-hero   # нарезка листов карт + тесты читаемости 960×540
python3 tools/artlab/keyout.py   docs/gdx/12-art-direction-lab/e4-effects        # хромакей предметов с зелёного фона → props/*.png
```

`embed.py … --full BG_,TOP_` — не ужимать фон и фигурки (иначе фон мылится).
Встраиваются только пути, записанные в одинарных кавычках (`'cut/plate.png'`), — поэтому в прототипах
картинки задаются из JS, а не в атрибуте `src`.

```bash
python3 tools/artlab/canvascut.py docs/gdx/12-art-direction-lab/e8-one-canvas/out/M3_….png \
        docs/gdx/12-art-direction-lab/e8-one-canvas/cut     # мастер-холст → плита + спрайты + index.json
```

`canvascut.py` режет единый холст сцены (E8): находит коврик, вырезает лежащие на нём предметы вместе с их
тенью, залечивает коврик текстурой чистых участков. Рядом с исходником можно положить `stamp.json`
(вернуть печатную разметку под вырезанным) и `extra.json` (ручные области для предметов, которые
автоматика не берёт).
