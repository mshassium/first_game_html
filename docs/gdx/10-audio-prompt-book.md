# 10 — Аудио-промпт-бук: генерация музыки и звука в Suno

Аналог [05-prompt-book.md](05-prompt-book.md), только для звука. Каждый промпт — **цельный блок,
который копируется целиком**. Всё, что должно попасть в Suno, оформлено кодовыми блоками
с пометкой поля (`Styles` / `Lyrics` / `Exclude Styles`). Русский текст вокруг — инструкции
для вас, в промпт он не идёт.

Список того, что нужно получить, берётся из [07-audio-spec.md](07-audio-spec.md) §2–3.
ID (`M-01`, `S-14`) — оттуда же, не переименовывать.

> **Статус относительно 07-audio-spec.md.** Та спека в §1 требует брать всё из CC0-библиотек
> и явно запрещает нейросетевую генерацию. Этот документ реализует другое решение —
> генерацию в Suno. Пока §1 в 07-й спеке не переписан, источником истины по **списку и формату**
> файлов остаётся 07, а источником по **способу получения** — этот файл.

---

## 0. Что Suno умеет и чего не умеет

Честная граница применимости, чтобы не потратить неделю впустую.

| Задача | Suno | Вердикт |
|---|---|---|
| M-01, M-02 — зацикленные инструментальные подложки | Прямое назначение модели | ✅ Делаем |
| M-03, S-18, S-19, S-20 — стингеры и гонг (0.7–4 с музыкального материала) | Генерируется трек, из него вырезается нужный такт | ✅ Делаем, с нарезкой |
| S-01…S-17, S-21, S-22 — короткий фоли (шорох карты, стук кубика, щелчок) | Модель обучена на музыке. Она не умеет выдавать изолированный сухой звук длиной 0.15 с | ⚠️ Обходной путь ниже, выход годного материала низкий |

**Почему фоли в Suno плохо получается.** Модель всегда стремится к музыкальной структуре:
добавляет реверб, подкладывает тон, выстраивает ритм. Для «глухого деревянного щелчка 0.12 с»
это ровно те три вещи, которых быть не должно. Раздел 3 даёт рабочий обходной путь
(генерация «фоли-полигона» с последующей нарезкой), но рассчитывайте, что из одной генерации
пригодных фрагментов будет один-два, и часть позиций всё равно придётся закрыть иначе.

**Что рекомендую по факту:** музыку и стингеры (M-01, M-02, M-03, S-18, S-19, S-20) — Suno,
он тут объективно хорош. Остальные 19 коротких SFX — либо CC0 по исходной 07-спеке
(быстрее и чище), либо специализированный text-to-SFX сервис, который умеет короткие
изолированные звуки. Если решение «всё через Suno» окончательное — раздел 3 написан
именно под него.

### Настройки интерфейса

| Поле | Значение |
|---|---|
| **Instrumental** | Всегда **включён**. Без него Suno почти гарантированно добавит вокал |
| **Styles** | Туда идёт блок «Styles» из промптов ниже |
| **Exclude Styles** | Туда идёт блок «Exclude» — общий для всех, см. §1.3 |
| **Lyrics** | При включённом Instrumental используется для структурных тегов (`[Intro]`, `[Loop]`). Если поле заблокировано — просто пропускаем |
| **Формат скачивания** | **WAV**, если план позволяет. MP3 из Suno — уже сжатый; конвертировать сжатое в сжатое (см. §4) значит терять качество дважды |

Поле Styles в разных версиях интерфейса вмещает разное число символов. Если ваш блок
обрезается — оставляйте **первые два предложения**, они несут инструменты и настроение;
остальное детализация.

### Лицензия — проверить до релиза

Право на коммерческое использование сгенерированного в Suno зависит от тарифа
(на бесплатном его нет) и от текущей редакции Terms of Service. Условия меняются.
**Перед публикацией в сторах откройте актуальные Terms и убедитесь, что ваш тариф
разрешает коммерческое использование в игре.** Результат проверки — датой и цитатой —
занесите в `assets_src/audio/CREDITS.md` рядом с записями о файлах: 07-спека §1 требует
фиксировать происхождение каждого файла, и для сгенерированного это тем более важно.

---

## 1. Звуковой стиль F!RST

### 1.1 Целевое ощущение

Прямой аудио-перевод [03-art-style-bible.md](03-art-style-bible.md): подземное святилище
Ордена Первых, каменный алтарный стол, свечи, жилы светящегося кристалла. Тепло, старина,
вес физических предметов. Не гримдарк, не героика-марш, не эпик-трейлер.

- **Акустика:** большой каменный зал, длинный натуральный хвост реверберации.
- **Инструменты:** арфа, кельтская арфа, челеста, струнные *con sordino*, соло-виолончель,
  низкие деревянные духовые, литавры мягкой колотушкой, чашечные гонги, тибетские чаши,
  безсловесный женский хор далеко в миксе.
- **Чего нет:** ударной установки, синтезаторов, электрогитары, брасс-стабов в стиле трейлера,
  вокала со словами.
- **Динамика:** музыка — фон. Она не должна перетягивать внимание с доски.

### 1.2 Технические договорённости (соблюдать во всех треках)

| Параметр | Значение | Зачем |
|---|---|---|
| Тональность | **D minor** (D aeolian) для M-01 и M-02, **D major** для M-03 | Кроссфейд меню↔бой (1.2 с по 07-спеке) не даст диссонанса, а победный стингер сядет на тот же тональный центр |
| Темп | M-01 — **68 BPM**, M-02 — **88 BPM**, M-03 — свободный | |
| Метр | 4/4 | |
| Громкость | −20 LUFS для музыки, −16 LUFS для SFX | 07-спека §4, выставляется постобработкой |

### 1.3 Общий блок Exclude Styles

Вставляется в поле **Exclude Styles** для **каждой** генерации без изменений.

```
vocals, lyrics, singing, rap, spoken word, drum kit, drum machine, trap beat, EDM, dubstep, synthwave, lo-fi hip hop, chiptune, electric guitar, distorted bass, brass trailer stabs, cinematic braams, applause, crowd noise, vinyl crackle, tape hiss, fade out ending, key change, tempo change, sudden dynamic drop
```

---

## 2. Музыка

### M-01 — `music_menu.mp3` · меню, правила, настройки

Спокойный фэнтези-эмбиент, ощущение древнего зала. Петля 60–90 с.

**Styles:**
```
Slow, calm dark-fantasy ambient underscore for a game main menu. 68 BPM, D minor, 4/4. Solo celtic harp arpeggios lead, answered by soft muted strings and a distant wordless female choir pad. Sparse celesta notes, one low sustained cello drone, occasional soft bowl gong. Recorded in a large stone hall: long natural reverb tail, air, no dryness. Warm, ancient, patient, quietly mysterious, never sad and never heroic. Steady dynamics from start to finish, no build-up, no climax, no drop. Acoustic orchestral instruments only, low volume background music that never demands attention.
```

**Lyrics:**
```
[Intro: solo harp, sparse]
[Main loop: harp with muted strings and distant choir pad, steady, no build]
[Main loop continues, same dynamics]
[Outro: harp only, same volume]
```

**Что оставить.** Из готового трека берём средний фрагмент, где фактура уже полная и ещё
не меняется. Нарезка и склейка петли — §4.2.

> **Сделано.** Трек принят: 4:20, из него собрана петля 80 с (участок 90–170 с, где
> уровни на границах совпадают). Разбор и команды — в
> [assets_src/music/CREDITS.md](../../assets_src/music/CREDITS.md).

---

### M-02 — `music_battle.mp3` · игровой экран

Сдержанное напряжение, без выраженной мелодии — трек не должен надоесть за 20 партий.
Петля 90–120 с.

**Styles:**
```
Tense but restrained dark-fantasy underscore for a slow tabletop card duel. 88 BPM, D minor, 4/4. Foundation is a pulsing low cello ostinato on a single repeated note with soft timpani heartbeat underneath. Above it only sparse textures: occasional muted string swells, a rare harp harmonic, faint bowed metal shimmer. Deliberately no lead melody and no memorable theme. Large stone hall reverb, warm and dark. Focused, patient, faintly ominous, controlled. Flat dynamics throughout, no build-up, no climax, no percussion fills. Acoustic orchestral instruments only, quiet background music designed to be looped for twenty minutes without becoming annoying.
```

**Lyrics:**
```
[Intro: low cello ostinato alone]
[Loop A: ostinato with soft timpani pulse, sparse string swells]
[Loop B: same ostinato, one harp harmonic added, same dynamics]
[Outro: ostinato alone, same volume]
```

**Критерий приёмки, специфичный для этого трека:** запустите фрагмент по кругу и займитесь
чем-то другим на 10 минут. Если ловите себя на том, что напеваете мотив — трек не подходит,
в нём появилась мелодия. Генерируйте заново.

---

### M-03 — `music_victory_sting.mp3` · экран победы

Короткий стингер, не зацикливается. ~4 с.

**Styles:**
```
Short triumphant orchestral sting for a fantasy game victory screen. D major, free tempo, about six seconds total. One rising harp glissando into a warm sustained major chord held by strings, low brass and wordless choir, crowned by a single bright bell and a soft cymbal swell that decays into a long stone-hall reverb tail. Noble, warm, golden, earned. Not bombastic, not a fanfare march, no drums. Acoustic orchestral instruments only. Single gesture, no repetition, no loop.
```

**Lyrics:**
```
[Sting: harp glissando into sustained major chord with choir and bell, long decay]
```

**Один материал на три позиции.** Тот же генерат закрывает и **S-19** `sfx_victory.mp3`
(2.5 с по 07-спеке) — просто другой длины вырез из того же файла. Так победный экран и звук
победы гарантированно звучат как одна вещь.

---

### M-04 (опционально) — фоновая атмосфера святилища

В 07-спеке этой позиции нет, добавлять её не обязательно. Но тихий слой «капли, треск свечей,
гул зала» под музыку сильно оживляет сцену, и Suno умеет такое лучше, чем короткие SFX.
Если решите добавить — заведите в спеке ID `M-04` и файл `music_ambience.mp3`.

**Styles:**
```
Pure atmospheric ambience of an ancient underground stone sanctuary, no music, no melody, no rhythm, no instruments. Continuous quiet low air rumble of a large cavern, slow sparse water drips echoing far away, faint crackle of candle flames, distant deep resonant hum of a crystal vein. Dark, still, spacious, warm. Constant unchanging texture from beginning to end, no events, no build, no transitions. Field recording character, very low volume.
```

**Exclude Styles** — общий блок из §1.3 плюс: `melody, chords, instruments, music`.

---

## 3. SFX

Прочитайте §0 перед тем, как начинать: короткий фоли — не сильная сторона Suno.

### 3.1 Метод «фоли-полигон»

Просить у Suno один звук длиной 0.3 с бессмысленно — модель отдаёт трек на минуты.
Поэтому просим **серию однотипных ударов через паузы**, а потом режем результат на куски.

1. Генерируем «полигон» — трек, где нужный звук повторяется 8–12 раз с паузой ~2 с.
2. Скачиваем WAV.
3. Открываем в Audacity, слушаем, отбираем 1–3 самых чистых повтора.
4. Вырезаем, обрезаем тишину, обрабатываем по §4.1.

Требование «no reverb» в промпте будет соблюдено частично — Suno почти всегда добавит хвост.
Немного помогает финальная строка о сухой близкой записи, но полностью хвост не убрать.
Это главная причина, по которой звуки из Suno на фоне CC0-фоли слышны как «мокрые».

**Общий Exclude для всего раздела 3** — блок из §1.3 плюс:
```
melody, harmony, chords, musical instruments, rhythm, beat, tempo, background music, pad, drone, ambience, reverb tail, hall reverb
```

---

### 3.2 Группа A — карты (S-01, S-02, S-03, S-04, S-05)

Шорох добора, хлопок карты о камень, щелчок переворота, тихий шорох подъёма, стук в урну.

**Styles:**
```
Dry close-miked foley recording session, absolutely no music. A series of isolated card handling sounds, each separated by two seconds of pure silence: a single thick cardboard card sliding out of a deck with a short paper rasp, a card slapping flat down onto a cold stone slab with a soft low thud under the paper snap, a quick sharp card flip click, a very quiet card lifting rasp, a card fluttering down into a hollow stone urn with a dull knock at the end. Real physical objects, thick premium cardstock, granite surface. Close microphone, dry, tight, no reverb, no room tone, no melody, no rhythm.
```

**Lyrics:**
```
[silence]
[card slides out of deck]
[silence]
[card slaps onto stone]
[silence]
[card flip click]
[silence]
[quiet card lift]
[silence]
[card drops into stone urn]
[silence]
```

---

### 3.3 Группа B — школа F, Печать Запрета (S-06, S-07)

Лёд, цепи, отказ. Цвет школы — лазурный, образ — ледяные оковы.

**Styles:**
```
Dry magic sound design foley, no music. Isolated ice and chain impacts separated by two seconds of silence: thick ice sheet cracking and freezing over with a crystalline crunch, heavy iron chain links clinking and tightening, a deep low ominous swell locking shut, then a sharp brittle frost snap breaking with fine crystal shards scattering. Cold, heavy, final, forbidding. Real physical materials, ice and iron. Close dry recording, tight transients, no melody, no rhythm, no musical instruments.
```

**Lyrics:**
```
[silence]
[ice cracks and spreads, chain tightens, low lock]
[silence]
[sharp frost snap, crystal shards]
[silence]
```

---

### 3.4 Группа C — школы I и R (S-08, S-09)

I — Родник Изобилия, мягкий восходящий шиммер. R — Реликварий, тёплый реверсивный свуш
с колокольчиком.

**Styles:**
```
Dry magic sound design, no music. Isolated magical gestures separated by two seconds of silence: a soft rising shimmer of tiny bell-like crystal particles growing upward like sprouting life, then a warm reversed whoosh sucking inward and resolving into one gentle small bell chime. Gentle, warm, benevolent, organic. Subtle water and glass textures. Close dry recording, no melody, no chord progression, no rhythm, no background music.
```

**Lyrics:**
```
[silence]
[soft rising shimmer, growing]
[silence]
[warm reversed whoosh into single small bell]
[silence]
```

---

### 3.5 Группа D — школы S и T (S-10, S-11, S-12)

S — Тень Похитителя: резкий свист-рывок с дымом. T — Капкан Чародея: взвод и щелчок челюстей.

**Styles:**
```
Dry mechanical and magic foley, no music. Isolated impacts separated by two seconds of silence: a fast aggressive whoosh snatching past with a smoky hiss trailing, a metal mechanism being cocked with a ratcheting spring tension click, then a violent steel trap snapping shut with a hard metallic clack and brief ring. Sharp, dangerous, mechanical, sudden. Real steel and springs. Close dry recording, tight fast transients, no melody, no rhythm, no musical instruments.
```

**Lyrics:**
```
[silence]
[fast whoosh with smoky hiss]
[silence]
[metal mechanism cocking, spring tension]
[silence]
[steel trap snaps shut, hard clack]
[silence]
```

---

### 3.6 Группа E — кубики (S-13, S-14)

Каменные d6 по каменному столу.

**Styles:**
```
Dry close foley recording, no music. Two heavy carved stone dice tumbling and rolling across a rough granite slab for about one second, clattering against each other, then settling; after two seconds of silence, a single stone die landing with one dull heavy knock. Real stone on stone, weighty, granular, no plastic and no wooden character. Close microphone, dry, tight, no reverb, no melody, no rhythm, no background music.
```

**Lyrics:**
```
[silence]
[two stone dice tumble and settle]
[silence]
[single stone die lands, one knock]
[silence]
```

---

### 3.7 Группа F — интерфейс (S-15, S-16, S-17, S-21, S-22)

Кнопка, отмена, открытие модалки, переполнение руки, недопустимое действие.

**Styles:**
```
Dry close foley, no music. Isolated short interface sounds separated by two seconds of silence: a muted wooden knock click on solid oak, the same wooden knock pitched noticeably lower, a soft cloth whoosh with a short creak of old wood, a short dull negative wooden double-tap, and one very quiet muted rejection thud. Small, tactile, understated, low volume. Real oak and leather, no synthetic beeps and no electronic UI sounds. Close microphone, dry, tight, no reverb, no melody, no rhythm.
```

**Lyrics:**
```
[silence]
[wooden click]
[silence]
[lower wooden click]
[silence]
[soft whoosh with wood creak]
[silence]
[dull negative double-tap]
[silence]
[very quiet rejection thud]
[silence]
```

---

### 3.8 Группа G — стингеры (S-18, S-20)

S-18 — начало вашего хода, тихий гонг. S-20 — поражение, нисходящий минорный аккорд.
S-19 (победа) берётся из M-03, см. выше.

Здесь Suno в своей стихии — это музыкальный материал, а не фоли. Генерируйте отдельно
от групп A–F: у стингеров реверб **нужен**, поэтому Exclude-хвост про reverb из §3.1 сюда
не добавляем, берём только общий блок §1.3.

**Styles:**
```
Two short solo orchestral gestures in a large stone hall, separated by four seconds of silence. First: one single soft bowl gong struck gently, D minor, warm and quiet, ringing out into a long natural reverb tail, calling attention without alarm. Second: a slow descending minor chord on muted low strings and bass clarinet, dark and heavy, sinking downward and dying away into the hall, resigned and final. Acoustic instruments only, no percussion kit, no melody beyond these two gestures, no rhythm.
```

**Lyrics:**
```
[Gong: single soft strike, long decay]
[silence]
[Defeat: slow descending minor chord, dark, dying away]
```

---

## 4. Постобработка

Формат целевых файлов задан в 07-спеке §2–3: **MP3 44.1 кГц, SFX — моно 128 кбит/с,
музыка — стерео 128 кбит/с**. Всё ниже — доведение генерата до этого формата.

### 4.1 SFX: вырезать, обрезать, нормализовать

```bash
# 1. Вырезать фрагмент из полигона (с 12.40 с, длительность 0.45 с)
ffmpeg -i suno_foley_cards.wav -ss 12.40 -t 0.45 cut.wav

# 2. Обрезать тишину по краям, HPF 60 Гц, привести к -16 LUFS, экспорт в моно MP3
ffmpeg -i cut.wav \
  -af "silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.01,areverse,silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.01,areverse,highpass=f=60,loudnorm=I=-16:TP=-1:LRA=11" \
  -ar 44100 -ac 1 -b:a 128k gdx/assets/audio/sfx_card_place.mp3
```

### 4.2 Музыка: собрать бесшовную петлю

Suno не выдаёт зацикливаемый материал — трек начинается и заканчивается. Петля собирается
вручную. Идея: взять тело петли **A** и следующие за ним 2 секунды **B**, затем наложить
затухающее B на нарастающее начало A. Тогда начало петли — уже сшитый переход, а её конец
и её начало стыкуются потому, что в оригинале B шло сразу за A.

```bash
# 1. A — тело петли: 92 с начиная с 24-й секунды трека
ffmpeg -i suno_battle.wav -ss 24 -t 92 A.wav

# 2. B — ровно те 2 с, что идут сразу после A (24 + 92 = 116)
ffmpeg -i suno_battle.wav -ss 116 -t 2 B.wav

# 3. B кроссфейдом ложится на начало A → петля длиной ровно 92 с
ffmpeg -i B.wav -i A.wav -filter_complex "[0][1]acrossfade=d=2:c1=tri:c2=tri" loop.wav

# 4. Финальный экспорт: -20 LUFS, стерео MP3
ffmpeg -i loop.wav -af "loudnorm=I=-20:TP=-1:LRA=11" \
  -ar 44100 -ac 2 -b:a 128k gdx/assets/audio/music_battle.mp3
```

Обе команды из §4.1 и §4.2 прогнаны на тестовом материале: длительность петли выходит
ровно 92 с, SFX-цепочка отдаёт моно 44.1 кГц / 128 кбит/с.

**Проверка петли обязательна на слух:** три круга подряд, стык не должен выдавать себя ни
щелчком, ни провалом громкости, ни ощущением «музыка началась заново». Если слышно —
двигайте точку выреза на такт вперёд и повторяйте.

### 4.3 Куда класть файлы

Исходники генерации (полные треки из Suno) — в `assets_src/music/`, происхождение
каждого файла записывается в `assets_src/music/CREDITS.md`. В сборку идут только
обработанные петли.

При вырезании обязателен ключ `-vn`: Suno вкладывает в MP3 обложку отдельным
видеопотоком, и без него она утаскивается в результат.

`SoundManager.kt` ждёт файлы по путям `audio/sfx_*.mp3` и `audio/music_*.mp3` внутри
assets — то есть `gdx/assets/audio/`. Имена — строго из 07-спеки: любой промах в имени
означает, что звук молча не загрузится (менеджер намеренно пропускает отсутствующие файлы,
падения не будет — и вы не заметите пропажу).

Обратите внимание: в `SoundManager.Sfx` сейчас заведено **16 позиций**, а в 07-спеке — 22.
Не заведены `S-04 card_hover`, `S-14 dice_land`, `S-16 ui_back`, `S-17 modal_open`,
`S-21 hand_overflow`, `S-22 error`. Если генерируете все 22 — enum нужно дополнить,
иначе шесть файлов просто не будут использоваться.

---

## 5. Приёмка

Дополняет чек-лист 07-спеки §7, не заменяет его.

- [ ] Все треки в одной тональности по §1.2 — кроссфейд меню↔бой не диссонирует
- [ ] M-02 прослушан 10 минут по кругу, мелодия не запоминается и не раздражает
- [ ] Стык петли не слышен на трёх кругах подряд ни у M-01, ни у M-02
- [ ] S-19 и M-03 звучат как один материал (взяты из одного генерата)
- [ ] Ни в одном SFX нет музыкального тона и слышимого ритма
- [ ] SFX из Suno не выделяются «мокростью» на фоне остальных — иначе сушить или заменять
- [ ] Каждый файл записан в `assets_src/audio/CREDITS.md`: Suno, дата, тариф, промпт
- [ ] Проверено и зафиксировано право на коммерческое использование по актуальным Terms
- [ ] Имена файлов побайтно совпадают с 07-спекой, все загружаются в игре
