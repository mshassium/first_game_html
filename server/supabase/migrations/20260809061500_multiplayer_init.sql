-- Мультиплеер: комнаты, партии и виды партий.
-- Спецификация: docs/gdx/11-multiplayer-spec.md §7.
--
-- Главное правило схемы: клиент не пишет сюда ничего и почти ничего не читает.
-- Пишет только серверная функция сервисным ключом, а игрок видит единственную
-- таблицу match_views — и в ней лежит уже отредактированное состояние, без карт
-- соперника. Полное состояние партии живёт в matches, куда клиенту хода нет.

-- Хеширование паролей комнат: crypt/gen_salt из pgcrypto. Так пароль не покидает
-- базу в открытом виде и серверу не нужна своя реализация bcrypt.
create extension if not exists pgcrypto with schema extensions;

-- ---------------------------------------------------------------- профили

create table public.profiles (
    id         uuid primary key references auth.users (id) on delete cascade,
    nickname   text not null check (char_length(btrim(nickname)) between 2 and 16),
    created_at timestamptz not null default now()
);

comment on table public.profiles is 'Ник поверх анонимного пользователя auth.users.';

-- ---------------------------------------------------------------- комнаты

create type public.room_status as enum ('lobby', 'playing', 'closed');

create table public.rooms (
    id            uuid primary key default gen_random_uuid(),
    -- Короткий код для входа без списка: шесть символов без похожих (0/O, 1/I).
    code          text not null unique check (code ~ '^[A-HJ-NP-Z2-9]{6}$'),
    name          text not null check (char_length(btrim(name)) between 1 and 24),
    host_id       uuid not null references public.profiles (id),
    guest_id      uuid references public.profiles (id),
    -- bcrypt-хеш; null означает комнату без пароля.
    password_hash text,
    status        public.room_status not null default 'lobby',
    match_id      uuid,
    -- Счётчик неудачных попыток пароля и начало текущего окна: защита от подбора.
    failed_tries  int not null default 0,
    tries_since   timestamptz not null default now(),
    created_at    timestamptz not null default now(),
    last_activity timestamptz not null default now(),
    constraint rooms_guest_not_host check (guest_id is null or guest_id <> host_id)
);

comment on column public.rooms.password_hash is 'crypt(пароль, gen_salt(''bf'')); null — комната открыта.';

-- Список открытых комнат: только они показываются игроку, свежие сверху.
create index rooms_lobby_idx on public.rooms (status, last_activity desc);
-- Один игрок — одна открытая комната: прежняя закрывается при создании новой.
create unique index rooms_one_open_per_host_idx
    on public.rooms (host_id) where status <> 'closed';

-- ---------------------------------------------------------------- партии

create table public.matches (
    id            uuid primary key default gen_random_uuid(),
    room_id       uuid not null references public.rooms (id) on delete cascade,
    seat_a        uuid not null references public.profiles (id),
    seat_b        uuid not null references public.profiles (id),
    -- Полное состояние в формате StateCodec, в системе координат места A.
    state         text not null,
    -- База случайности; в зерно каждой команды подмешивается version.
    seed          text not null,
    version       int not null default 0,
    turn_deadline timestamptz not null,
    status        text not null default 'playing' check (status in ('playing', 'finished')),
    winner_seat   char(1) check (winner_seat in ('A', 'B')),
    end_reason    text check (
        end_reason in ('FIRST_SET', 'FIVE_OF_A_KIND', 'DECK_OUT', 'TIMEOUT', 'SURRENDER')
    ),
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    constraint matches_two_players check (seat_a <> seat_b),
    constraint matches_finished_has_winner check (
        (status = 'playing' and winner_seat is null and end_reason is null)
        or (status = 'finished' and winner_seat is not null and end_reason is not null)
    )
);

comment on table public.matches is 'Полное состояние партии. Клиенту недоступно: здесь лежат обе руки.';

-- Уборка просроченных партий и поиск активной партии игрока при реконнекте.
create index matches_live_idx on public.matches (status, turn_deadline);
create index matches_seats_idx on public.matches (seat_a, seat_b) where status = 'playing';

alter table public.rooms
    add constraint rooms_match_fk foreign key (match_id)
    references public.matches (id) on delete set null;

-- ------------------------------------------------------------ виды партий

-- Ровно две строки на партию — по одной на игрока. Именно их читает клиент и
-- на них подписан Realtime.
create table public.match_views (
    match_id   uuid not null references public.matches (id) on delete cascade,
    player_id  uuid not null references public.profiles (id),
    seat       char(1) not null check (seat in ('A', 'B')),
    version    int not null,
    -- Состояние в перспективе игрока и без скрытой информации.
    state      text not null,
    -- События последнего применения: их экран проигрывает анимацией.
    events     text not null default '',
    deadline   timestamptz not null,
    updated_at timestamptz not null default now(),
    primary key (match_id, player_id)
);

comment on table public.match_views is 'То, что игрок имеет право видеть. Пишет только сервер.';

-- Реконнект: активная партия игрока ищется по этому индексу.
create index match_views_player_idx on public.match_views (player_id, updated_at desc);

-- ---------------------------------------------------------------------- RLS

alter table public.profiles    enable row level security;
alter table public.rooms       enable row level security;
alter table public.matches     enable row level security;
alter table public.match_views enable row level security;

-- Профиль: свой можно прочитать и завести. Ник ставит сервер, но чтение
-- своего профиля клиенту нужно, чтобы понять, спрашивать ли ник.
create policy profiles_select_own on public.profiles
    for select to authenticated using (id = (select auth.uid()));

create policy profiles_insert_own on public.profiles
    for insert to authenticated with check (id = (select auth.uid()));

-- Вид партии: только свой. Эта же политика ограничивает Realtime.
create policy match_views_select_own on public.match_views
    for select to authenticated using (player_id = (select auth.uid()));

-- Для rooms и matches политик нет намеренно: в rooms лежит хеш пароля, в
-- matches — обе руки. Список комнат отдаёт серверная функция, которая ходит
-- сервисным ключом и RLS обходит. Политик на запись нет ни у одной таблицы.

-- ----------------------------------------------------------------- Realtime

-- Клиент подписывается на изменения своей строки в match_views с фильтром
-- player_id=eq.<uid>; RLS выше не даст подписаться на чужую.
alter publication supabase_realtime add table public.match_views;
