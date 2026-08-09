# Сервер мультиплеера

Бэкенд сетевых партий: база и права доступа в Supabase, серверные функции на
Vercel (появятся на этапе M9.4). Правила игры сюда не переписываются — сервер
зовёт ту же библиотеку, что и клиент, собранную из модуля `gdx/rules` в JS.

Спецификация целиком: [docs/gdx/11-multiplayer-spec.md](../docs/gdx/11-multiplayer-spec.md).

## Что уже работает

| Часть | Состояние |
|---|---|
| Проект Supabase `first-game` (eu-central-1) | Поднят |
| Схема: `profiles`, `rooms`, `matches`, `match_views` | Применена миграцией |
| RLS: игрок видит только свой вид партии | Проверено `checks/rls-smoke.mjs` |
| Realtime на `match_views` | Проверено `checks/realtime-smoke.mjs` |
| Анонимный вход | Включён |
| API на Vercel | Этап M9.4, не начат |

## Настройка

```bash
cp server/.env.example server/.env    # заполнить своими ключами
set -a && . server/.env && set +a     # подхватить их в оболочку
```

Ключи берутся в Supabase → Project Settings → API Keys. Секретный ключ обходит
RLS: он живёт только здесь и в переменных окружения Vercel, в клиент игры не
попадает никогда.

## Миграции

```bash
cd server
supabase link --project-ref "$SUPABASE_PROJECT_REF" --password "$SUPABASE_DB_PASS"
supabase db push --db-url "postgresql://postgres.$SUPABASE_PROJECT_REF:$SUPABASE_DB_PASS@aws-0-eu-central-1.pooler.supabase.com:5432/postgres"
```

Строка подключения именно через пул, а не через `db.<ref>.supabase.co`: прямое
подключение у новых проектов Supabase работает только по IPv6, и `db push` без
неё падает с `tls error (EOF)`.

## Проверки

Обе проверки создают свои данные и убирают их за собой, так что их можно гонять
по живому проекту. Перед запуском нужна собранная библиотека правил.

```bash
./gradlew -p gdx :rules:jsNodeProductionLibraryDistribution
node server/checks/rls-smoke.mjs        # права: свой вид виден, чужой нет
node server/checks/realtime-smoke.mjs   # ход доезжает по сокету, чужой не протекает
```

`realtime-smoke.mjs` заодно рабочий прототип протокола: Supabase Realtime
говорит кадрами Phoenix, и на этапе M9.5 такой же обмен пишется руками на Kotlin —
библиотеки Supabase в TeaVM не живут.

## Как устроен доступ к данным

- Клиент **не пишет** в базу вообще. Все изменения делает серверная функция
  секретным ключом.
- Клиент читает ровно одну таблицу — `match_views`, и только свою строку в ней:
  политика RLS сравнивает `player_id` с `auth.uid()`.
- В `match_views` лежит уже отредактированное состояние: рука соперника и обе
  колоды заменены заглушками ещё до записи в базу.
- Полное состояние партии живёт в `matches`, куда клиенту хода нет ни на чтение,
  ни на запись. В `rooms` лежит хеш пароля комнаты, поэтому список комнат тоже
  отдаёт функция, а не прямой запрос.
