-- Операции, которые обязаны быть атомарными: вход в комнату и тайм-аут хода.
--
-- Вход — гонка по определению: двое жмут на одну строчку списка одновременно,
-- и место достаётся кому-то одному. Проверка пароля и занятие места, разнесённые
-- по двум запросам, оставили бы окно, в котором оба гостя видят комнату свободной.
-- Поэтому и то и другое делает одна функция под блокировкой строки.
--
-- Пароль сверяется прямо в базе через pgcrypto: он не покидает её в открытом
-- виде, и серверу не нужна своя реализация bcrypt.

-- Кого ждёт партия. Чей ход — записано в state, но это строка формата StateCodec,
-- и разбирать её умеет только движок. Тайм-аут же должен уметь считать сама база:
-- проигрывает тот, чьего хода ждали.
alter table public.matches
    add column acting_seat char(1) not null default 'A' check (acting_seat in ('A', 'B'));

comment on column public.matches.acting_seat is 'Чьей команды ждём. Проставляет сервер после каждого хода.';

-- ------------------------------------------------------------- создание

create or replace function public.create_room(
    p_host     uuid,
    p_name     text,
    p_code     text,
    p_password text default null
)
returns public.rooms
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    room public.rooms;
begin
    -- Один игрок — одна открытая комната: прежнюю закрываем, иначе уникальный
    -- индекс rooms_one_open_per_host_idx не даст создать новую.
    update public.rooms
       set status = 'closed'
     where host_id = p_host
       and status <> 'closed';

    insert into public.rooms (code, name, host_id, password_hash)
    values (
        p_code,
        btrim(p_name),
        p_host,
        case when p_password is null or p_password = '' then null
             else crypt(p_password, gen_salt('bf')) end
    )
    returning * into room;

    return room;
end;
$$;

comment on function public.create_room is 'Создаёт комнату, закрывая прежнюю открытую комнату хозяина.';

-- ----------------------------------------------------------------- вход

/**
 * Занимает второе место в комнате.
 *
 * Возвращает jsonb с полем outcome:
 *   ok             — место занято, в ответе room_id и host_id;
 *   not_found      — комнаты нет или она уже не в лобби;
 *   wrong_password — пароль не подошёл, счётчик попыток увеличен;
 *   too_many_tries — похоже на подбор, вход закрыт до конца окна;
 *   room_full      — место уже занято;
 *   own_room       — это своя комната.
 */
create or replace function public.join_room(
    p_guest    uuid,
    p_room_id  uuid default null,
    p_code     text default null,
    p_password text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    room  public.rooms;
    tries int;
begin
    -- Блокируем строку: дальше идут проверки, и между ними никто не должен
    -- занять место.
    select * into room
      from public.rooms
     where (p_room_id is not null and id = p_room_id)
        or (p_room_id is null and p_code is not null and code = upper(btrim(p_code)))
       for update;

    if room.id is null or room.status <> 'lobby' then
        return jsonb_build_object('outcome', 'not_found');
    end if;

    if room.host_id = p_guest then
        return jsonb_build_object('outcome', 'own_room');
    end if;

    if room.guest_id is not null then
        return jsonb_build_object('outcome', 'room_full');
    end if;

    if room.password_hash is not null then
        -- Окно подбора: десять попыток на десять минут. Счётчик общий на комнату,
        -- потому что анонимных пользователей можно наштамповать сколько угодно.
        if room.tries_since < now() - interval '10 minutes' then
            update public.rooms
               set failed_tries = 0, tries_since = now()
             where id = room.id
            returning * into room;
        end if;

        if room.failed_tries >= 10 then
            return jsonb_build_object('outcome', 'too_many_tries');
        end if;

        if p_password is null
           or room.password_hash <> crypt(p_password, room.password_hash) then
            update public.rooms
               set failed_tries = failed_tries + 1
             where id = room.id
            returning failed_tries into tries;
            return jsonb_build_object('outcome', 'wrong_password', 'tries', tries);
        end if;
    end if;

    update public.rooms
       set guest_id = p_guest,
           failed_tries = 0,
           last_activity = now()
     where id = room.id
    returning * into room;

    return jsonb_build_object('outcome', 'ok', 'room_id', room.id, 'host_id', room.host_id);
end;
$$;

comment on function public.join_room is 'Атомарно проверяет пароль и занимает второе место в комнате.';

-- ------------------------------------------------------------- тайм-аут

/**
 * Завершает партии, у которых вышло время хода, и закрывает их комнаты.
 * Возвращает завершённые партии с победителем.
 *
 * Считает база, а не сервер: правила здесь не нужны — проигрывает тот, чьего
 * хода ждали. Зато нужна атомарность, иначе двое клиентов, одновременно
 * заметивших просрочку, засчитают тайм-аут дважды.
 */
create or replace function public.expire_matches(p_match_id uuid default null)
returns table (match_id uuid, winner_seat char(1))
language sql
security definer
set search_path = public, extensions
as $$
    with expired as (
        update public.matches m
           set status      = 'finished',
               end_reason  = 'TIMEOUT',
               winner_seat = case m.acting_seat when 'A' then 'B' else 'A' end,
               updated_at  = now()
         where m.status = 'playing'
           and m.turn_deadline < now()
           and (p_match_id is null or m.id = p_match_id)
        returning m.id, m.room_id, m.winner_seat
    ),
    closed as (
        update public.rooms r
           set status = 'closed', last_activity = now()
          from expired e
         where r.id = e.room_id
        returning r.id
    )
    select e.id, e.winner_seat from expired e;
$$;

comment on function public.expire_matches is 'Завершает просроченные партии: проигрывает тот, чьего хода ждали.';

/** Уборка брошенных лобби: комната без гостя, в которой давно ничего не происходило. */
create or replace function public.close_stale_rooms(p_older_than interval default interval '1 hour')
returns integer
language sql
security definer
set search_path = public, extensions
as $$
    with closed as (
        update public.rooms
           set status = 'closed'
         where status = 'lobby'
           and last_activity < now() - p_older_than
        returning id
    )
    select count(*)::int from closed;
$$;

-- Функции зовёт только серверная функция сервисным ключом. Игроку они не нужны:
-- он ходит через HTTP-эндпоинты, где проверяются права.
revoke all on function public.create_room(uuid, text, text, text) from public, anon, authenticated;
revoke all on function public.join_room(uuid, uuid, text, text) from public, anon, authenticated;
revoke all on function public.expire_matches(uuid) from public, anon, authenticated;
revoke all on function public.close_stale_rooms(interval) from public, anon, authenticated;
