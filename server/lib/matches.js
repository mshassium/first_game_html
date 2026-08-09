/**
 * Партия на сервере: старт, ход, сдача, тайм-аут, реконнект.
 *
 * Правил здесь нет ни строчки — их считает библиотека из `rules.js`, та же, что
 * стоит в игре. Здесь только права, очерёдность и запись в базу.
 */
import { Errors, fail } from './http.js';
import { insert, rpc, select, selectOne, update, upsert } from './db.js';
import { TURN_SECONDS, deadlineFrom, facade } from './rules.js';

/** Место игрока в партии или null, если он в ней не участвует. */
export function seatOf(match, playerId) {
  if (match.seat_a === playerId) return 'A';
  if (match.seat_b === playerId) return 'B';
  return null;
}

/**
 * Новая партия для заполненной комнаты.
 *
 * Хозяин комнаты садится на место A. Зерно партии складывается из её кода и
 * времени старта; в зерно каждой команды потом подмешивается номер версии,
 * иначе два хода подряд дали бы одинаковый расклад.
 */
export async function startMatch(room) {
  const seed = `${room.code}-${Date.now()}`;
  const started = facade.newMatch(seed);
  if (!started.ok) fail(Errors.SERVER_ERROR);

  const deadline = deadlineFrom();
  const [match] = await insert('matches', {
    room_id: room.id,
    seat_a: room.host_id,
    seat_b: room.guest_id,
    state: started.state,
    seed,
    version: 0,
    acting_seat: facade.actingSeat(started.state) ?? 'A',
    turn_deadline: deadline,
  });

  await writeViews(match, started.state, started.events, deadline);
  await update('rooms', `id=eq.${room.id}`, {
    match_id: match.id,
    status: 'playing',
    last_activity: new Date().toISOString(),
  });

  return match;
}

/**
 * Раскладывает состояние по двум видам.
 *
 * Каждый игрок получает свою перспективу и только то, что имеет право видеть:
 * рука соперника и обе колоды в видах уже заменены заглушками.
 */
export async function writeViews(match, state, events, deadline) {
  const now = new Date().toISOString();
  const rows = [
    { player: match.seat_a, seat: 'A' },
    { player: match.seat_b, seat: 'B' },
  ].map(({ player, seat }) => ({
    match_id: match.id,
    player_id: player,
    seat,
    version: match.version,
    state: facade.viewFor(state, seat),
    events: facade.eventsFor(events ?? '', seat) ?? '',
    deadline,
    updated_at: now,
  }));
  await upsert('match_views', rows);
}

/** Вид партии глазами игрока — то же, что придёт ему по сокету. */
export async function viewOf(matchId, playerId) {
  const view = await selectOne(
    'match_views',
    `match_id=eq.${matchId}&player_id=eq.${playerId}&select=seat,version,state,events,deadline`,
  );
  if (!view) fail(Errors.MATCH_NOT_FOUND);
  return view;
}

/** Партия по id с проверкой, что игрок в ней участвует. */
async function matchFor(matchId, playerId) {
  const match = await selectOne('matches', `id=eq.${matchId}&select=*`);
  if (!match) fail(Errors.MATCH_NOT_FOUND);
  const seat = seatOf(match, playerId);
  if (!seat) fail(Errors.NOT_YOUR_MATCH);
  return { match, seat };
}

/**
 * Просрочку снимаем лениво, при любом обращении к партии: бесплатный тариф
 * Vercel даёт всего один запуск cron в сутки, так что расписанию доверять нечего.
 */
async function expireIfDue(match) {
  if (match.status !== 'playing') return match;
  if (new Date(match.turn_deadline).getTime() > Date.now()) return match;

  // Пометить партию просроченной должна база: клиентов, заметивших просрочку
  // одновременно, бывает двое, и засчитать тайм-аут нужно ровно один раз.
  const expired = await rpc('expire_matches', { p_match_id: match.id });
  if (!Array.isArray(expired) || expired.length === 0) {
    // Кто-то успел раньше — перечитываем и работаем с тем, что в базе.
    return (await selectOne('matches', `id=eq.${match.id}&select=*`)) ?? match;
  }
  const winner = expired[0].winner_seat;
  return endMatch(match, winner, 'TIMEOUT');
}

/**
 * Записать конец партии в само состояние.
 *
 * Отметки в строке матча мало: клиент читает вид, а там партия по-прежнему идёт
 * и версия прежняя — значит обновление он просто отбросит и останется ждать
 * хода, которого не будет. Поэтому исход кладётся в состояние и версия растёт.
 */
async function endMatch(match, winnerSeat, reason) {
  const result = facade.finish(match.state, winnerSeat, reason);
  if (!result.ok) {
    // Состояние уже законченное — значит кто-то опередил; берём как есть.
    return (await selectOne('matches', `id=eq.${match.id}&select=*`)) ?? match;
  }

  const [saved] = await update('matches', `id=eq.${match.id}`, {
    state: result.state,
    version: match.version + 1,
    status: 'finished',
    winner_seat: winnerSeat,
    end_reason: reason,
    updated_at: new Date().toISOString(),
  }) ?? [];
  const finished = saved ?? match;

  await writeViews(finished, result.state, result.events, finished.turn_deadline);
  await closeRoom(finished.room_id);
  return finished;
}

/**
 * Ход игрока.
 *
 * Порядок проверок важен: сначала то, что видно по состоянию, и только потом
 * движок. «Не твой ход» бывает от гонки клиентов и это нормально, а вот
 * «так ходить нельзя» означает либо баг клиента, либо попытку сжульничать.
 */
export async function applyCommand(playerId, matchId, { version, kind, index }) {
  if (!Number.isInteger(version) || !Number.isInteger(index)) {
    fail(Errors.BAD_REQUEST, { reason: 'нужны целые version и index' });
  }
  if (kind !== 'play' && kind !== 'choose') {
    fail(Errors.BAD_REQUEST, { reason: 'kind: play или choose' });
  }

  const { match: found, seat } = await matchFor(matchId, playerId);
  const match = await expireIfDue(found);

  if (match.status !== 'playing') fail(Errors.MATCH_FINISHED);
  if (match.version !== version) fail(Errors.STALE_VERSION, { version: match.version });

  const result = facade.apply(match.state, seat, `${kind};${index}`, `${match.seed}#${match.version + 1}`);
  if (!result.ok) {
    if (result.error === 'NOT_YOUR_TURN') fail(Errors.NOT_YOUR_TURN);
    if (result.error === 'MATCH_FINISHED') fail(Errors.MATCH_FINISHED);
    fail(Errors.ILLEGAL_COMMAND, { reason: result.error });
  }

  const over = facade.isOver(result.state);
  const deadline = over ? match.turn_deadline : deadlineFrom();

  // Захват версии заодно решает гонку: если клиент прислал два одинаковых хода
  // подряд, второй не найдёт строку с прежней версией и получит stale_version.
  const [saved] = await update('matches', `id=eq.${matchId}&version=eq.${version}`, {
    state: result.state,
    version: match.version + 1,
    acting_seat: facade.actingSeat(result.state) ?? match.acting_seat,
    turn_deadline: deadline,
    updated_at: new Date().toISOString(),
    ...(over
      ? {
          status: 'finished',
          winner_seat: facade.winnerSeat(result.state),
          end_reason: endReasonOf(result.state),
        }
      : {}),
  }) ?? [];

  if (!saved) fail(Errors.STALE_VERSION, { version: match.version });

  await writeViews(saved, result.state, result.events, deadline);
  if (over) await closeRoom(saved.room_id);

  return viewOf(matchId, playerId);
}

/** Сдаться: партия заканчивается в пользу соперника. */
export async function surrender(playerId, matchId) {
  const { match: found, seat } = await matchFor(matchId, playerId);
  const match = await expireIfDue(found);
  if (match.status !== 'playing') fail(Errors.MATCH_FINISHED);

  await endMatch(match, seat === 'A' ? 'B' : 'A', 'SURRENDER');
  return viewOf(matchId, playerId);
}

/**
 * «Соперник просрочил ход».
 *
 * Просьбу присылает тот, кто ждёт, и это основной путь: заинтересованная
 * сторона в этот момент как раз онлайн. Время сверяет база, а не клиент.
 */
export async function claimTimeout(playerId, matchId) {
  const { match: found } = await matchFor(matchId, playerId);
  const match = await expireIfDue(found);
  if (match.status !== 'finished' || match.end_reason !== 'TIMEOUT') {
    if (match.status === 'playing') fail(Errors.NOT_EXPIRED, { deadline: match.turn_deadline });
  }
  return viewOf(matchId, playerId);
}

/**
 * Партия игрока — для возврата после перезагрузки или обрыва.
 *
 * Только что законченная тоже считается: иначе игрок, у которого не поднялся
 * сокет, никогда не узнает, чем кончилось дело — его последний ход был чужим,
 * а спросить, кроме как здесь, негде.
 */
export async function currentMatch(playerId) {
  const mine = `or=(seat_a.eq.${playerId},seat_b.eq.${playerId})`;
  const rows = await select(
    'matches',
    `${mine}&status=eq.playing&select=*&order=updated_at.desc&limit=1`,
  );
  const found = rows?.[0] ?? (await recentlyFinished(mine));
  if (!found) return null;

  const match = await expireIfDue(found);
  const view = await viewOf(match.id, playerId);
  const opponentId = match.seat_a === playerId ? match.seat_b : match.seat_a;
  const opponent = await selectOne('profiles', `id=eq.${opponentId}&select=nickname`);

  return {
    matchId: match.id,
    status: match.status,
    seat: seatOf(match, playerId),
    opponent: opponent?.nickname ?? null,
    ...view,
  };
}

/** Партия, законченная недавно: показать исход и предложить выйти в список. */
async function recentlyFinished(mineFilter) {
  const since = new Date(Date.now() - RESULT_WINDOW_MINUTES * 60_000).toISOString();
  const rows = await select(
    'matches',
    `${mineFilter}&status=eq.finished&updated_at=gte.${since}&select=*&order=updated_at.desc&limit=1`,
  );
  return rows?.[0] ?? null;
}

/** Сколько после конца партии её ещё отдаёт `/matches/current`. */
const RESULT_WINDOW_MINUTES = 10;

function endReasonOf(state) {
  // Причина победы читается из состояния: последняя строка — исход партии.
  const outcome = state.split('\n')[7] ?? '';
  return outcome.split(';')[1] || 'FIRST_SET';
}

async function closeRoom(roomId) {
  await update('rooms', `id=eq.${roomId}`, {
    status: 'closed',
    last_activity: new Date().toISOString(),
  });
}

export { TURN_SECONDS };
