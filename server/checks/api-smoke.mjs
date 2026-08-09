/**
 * Партия целиком через HTTP: два игрока, комната с паролем, ходы, конец партии.
 *
 * По умолчанию поднимает серверную функцию прямо здесь, без Vercel: то же самое,
 * что развёрнутая, но без деплоя и сети. Чтобы проверить настоящий деплой:
 *
 *   API_BASE=https://<проект>.vercel.app node server/checks/api-smoke.mjs
 *
 * Нужны переменные окружения из server/.env.
 */
import http from 'node:http';
import { ANON_KEY, URL_BASE, api, reporter, signInAnonymously } from './lib.mjs';

const { check, finish } = reporter();

let base = process.env.API_BASE;
let server = null;

if (!base) {
  const { default: handler } = await import('../api/index.js');
  server = http.createServer((req, res) => handler(req, res));
  await new Promise((resolve) => server.listen(0, resolve));
  base = `http://127.0.0.1:${server.address().port}`;
  console.log(`функция поднята локально: ${base}`);
} else {
  console.log(`проверяю развёрнутый API: ${base}`);
}

async function call(path, { token, method = 'GET', body } = {}) {
  const response = await fetch(`${base}/api${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  let parsed;
  try {
    parsed = text ? JSON.parse(text) : null;
  } catch {
    parsed = text;
  }
  return { status: response.status, body: parsed };
}

const cleanup = [];

try {
  console.log('1. Два игрока и ники');
  const host = await signInAnonymously();
  const guest = await signInAnonymously();
  cleanup.push(async () => {
    await api(`/rest/v1/profiles?id=in.(${host.id},${guest.id})`, { method: 'DELETE' });
  });

  const noNickname = await call('/rooms', { token: host.token, method: 'POST', body: { name: 'без ника' } });
  check(noNickname.status === 403 && noNickname.body.error === 'nickname_required', 'без ника комнату не создать');

  const named = await call('/profile', { token: host.token, method: 'POST', body: { nickname: 'Хозяин' } });
  check(named.status === 200 && named.body.nickname === 'Хозяин', 'ник хозяина записан');
  await call('/profile', { token: guest.token, method: 'POST', body: { nickname: 'Гость' } });

  const short = await call('/profile', { token: guest.token, method: 'POST', body: { nickname: 'я' } });
  check(short.status === 400, 'слишком короткий ник отбит');

  console.log('2. Комната с паролем');
  const created = await call('/rooms', {
    token: host.token,
    method: 'POST',
    body: { name: 'проверка API', password: 'тайна' },
  });
  check(created.status === 200 && created.body.hasPassword === true, 'комната создана с замком');
  const roomId = created.body.id;
  const roomCode = created.body.code;
  cleanup.push(async () => { await api(`/rest/v1/rooms?id=eq.${roomId}`, { method: 'DELETE' }); });

  const list = await call('/rooms', { token: guest.token });
  const listed = list.body.rooms.find((room) => room.id === roomId);
  check(!!listed, 'комната видна в списке');
  check(listed?.host === 'Хозяин', 'в списке виден ник хозяина');
  check(listed?.hasPassword === true, 'замок виден');
  check(!('password_hash' in (listed ?? {})), 'хеш пароля наружу не уходит');

  console.log('3. Вход');
  const wrong = await call('/rooms/join', {
    token: guest.token,
    method: 'POST',
    body: { code: roomCode, password: 'не тайна' },
  });
  check(wrong.status === 403 && wrong.body.error === 'wrong_password', 'неверный пароль не пускает');

  const ownRoom = await call('/rooms/join', {
    token: host.token,
    method: 'POST',
    body: { roomId, password: 'тайна' },
  });
  check(ownRoom.body.error === 'own_room', 'в свою комнату войти нельзя');

  const joined = await call('/rooms/join', {
    token: guest.token,
    method: 'POST',
    body: { code: roomCode, password: 'тайна' },
  });
  check(joined.status === 200 && !!joined.body.matchId, 'гость вошёл, партия началась');
  const matchId = joined.body.matchId;
  cleanup.push(async () => { await api(`/rest/v1/matches?id=eq.${matchId}`, { method: 'DELETE' }); });

  const second = await call('/rooms/join', {
    token: guest.token,
    method: 'POST',
    body: { code: roomCode, password: 'тайна' },
  });
  check(second.body.error === 'room_not_found', 'занятая комната больше не пускает');

  console.log('4. Посторонний');
  const stranger = await signInAnonymously();
  cleanup.push(async () => { await api(`/rest/v1/profiles?id=eq.${stranger.id}`, { method: 'DELETE' }); });
  await call('/profile', { token: stranger.token, method: 'POST', body: { nickname: 'Прохожий' } });

  const peek = await call(`/rooms/${roomId}`, { token: stranger.token });
  check(peek.body.error === 'room_not_found', 'чужая комната не показывается');

  const intrude = await call(`/matches/${matchId}/command`, {
    token: stranger.token,
    method: 'POST',
    body: { version: 0, kind: 'play', index: 0 },
  });
  check(intrude.body.error === 'not_your_match', 'в чужую партию не походить');

  const noToken = await call('/rooms', { method: 'POST', body: { name: 'без токена' } });
  check(noToken.status === 401 && noToken.body.error === 'unauthorized', 'без токена не пускает');

  // Заголовки HTTP ходят только в ASCII, поэтому подделка тоже латиницей.
  const badToken = await call('/matches/current', { token: 'not-a-real-token' });
  check(badToken.status === 401, 'поддельный токен не проходит');

  console.log('5. Партия');
  const players = {
    A: { token: host.token, name: 'Хозяин' },
    B: { token: guest.token, name: 'Гость' },
  };

  const first = await call('/matches/current', { token: host.token });
  check(first.body.match?.matchId === matchId, 'реконнект возвращает активную партию');
  check(first.body.match?.opponent === 'Гость', 'виден ник соперника');
  check(first.body.match?.state?.includes('\n'), 'состояние пришло');

  let moves = 0;
  let winner = null;
  let checkedStale = false;
  let checkedForeign = false;

  while (moves < 400) {
    const state = await call('/matches/current', { token: players.A.token });
    if (!state.body.match) break;

    const view = state.body.match;
    const acting = actingSeatOf(view.state) === 'YOU' ? view.seat : other(view.seat);
    const mover = players[acting];
    const moverView = acting === view.seat ? view : (await call('/matches/current', { token: mover.token })).body.match;

    // Чужой ход и устаревшая версия — по разу за партию, дальше только мешают.
    if (!checkedForeign) {
      const idle = players[other(acting)];
      const foreign = await call(`/matches/${matchId}/command`, {
        token: idle.token,
        method: 'POST',
        body: { version: moverView.version, kind: 'play', index: 0 },
      });
      check(foreign.body.error === 'not_your_turn', 'ходить за соперника нельзя');
      checkedForeign = true;
    }
    if (!checkedStale) {
      const stale = await call(`/matches/${matchId}/command`, {
        token: mover.token,
        method: 'POST',
        body: { version: moverView.version - 1, kind: 'play', index: 0 },
      });
      check(stale.body.error === 'stale_version', 'ход по старой версии отбит');
      checkedStale = true;
    }

    const pending = moverView.state.split('\n')[6];
    const move = pending.length > 0 ? { kind: 'choose', index: 0 } : { kind: 'play', index: 0 };
    const applied = await call(`/matches/${matchId}/command`, {
      token: mover.token,
      method: 'POST',
      body: { version: moverView.version, ...move },
    });

    if (applied.status !== 200) {
      check(false, `ход ${moves + 1} места ${acting} отбит: ${JSON.stringify(applied.body)}`);
      break;
    }
    moves++;

    const outcome = applied.body.state.split('\n')[7];
    if (outcome) {
      winner = outcome.split(';')[0];
      break;
    }
  }

  check(moves > 5, `партия сыграна: ${moves} ходов`);
  check(winner === 'YOU' || winner === 'AI', `партия завершилась победой (${winner})`);

  const afterEnd = await call(`/matches/${matchId}/command`, {
    token: host.token,
    method: 'POST',
    body: { version: 999, kind: 'play', index: 0 },
  });
  check(afterEnd.body.error === 'match_finished' || afterEnd.body.error === 'stale_version',
    `в законченную партию ходить нельзя (${afterEnd.body.error})`);

  // Законченная партия ещё отдаётся: иначе игрок, у которого не поднялся сокет,
  // не узнает исход — его последний ход был чужим.
  const afterMatch = await call('/matches/current', { token: host.token });
  check(afterMatch.body.match?.status === 'finished', 'после конца партии виден её исход');
  check(afterMatch.body.match?.matchId === matchId, 'и это та самая партия');

  console.log('6. Тайм-аут');
  const timeout = await makeTimedOutMatch(players, call, cleanup);
  check(timeout.claim.status === 200, `просрочка засчитана (${timeout.claim.status})`);
  check(timeout.finished.status === 'finished', 'партия закрыта');
  check(timeout.finished.end_reason === 'TIMEOUT', 'причина — тайм-аут');
  check(timeout.finished.winner_seat === timeout.waiting, `победил тот, кто ждал (${timeout.finished.winner_seat})`);
} finally {
  for (const step of cleanup.reverse()) await step().catch(() => {});
  if (server) server.close();
}

finish('проверка API');

function actingSeatOf(state) {
  // В своей перспективе игрок всегда YOU; строка 4 — чей ход и кто ходил первым.
  const pendingSide = state.split('\n')[6].split(';')[0];
  return pendingSide || state.split('\n')[3].split(';')[0];
}

function other(seat) {
  return seat === 'A' ? 'B' : 'A';
}

/** Партия, у которой время хода уже вышло: срок сдвигается прямо в базе. */
async function makeTimedOutMatch(players, call, cleanup) {
  const created = await call('/rooms', {
    token: players.A.token,
    method: 'POST',
    body: { name: 'проверка тайм-аута' },
  });
  if (!created.body?.id) throw new Error(`комната не создалась: ${JSON.stringify(created.body)}`);
  const roomId = created.body.id;
  cleanup.push(async () => { await api(`/rest/v1/rooms?id=eq.${roomId}`, { method: 'DELETE' }); });

  const joined = await call('/rooms/join', {
    token: players.B.token,
    method: 'POST',
    body: { roomId },
  });
  if (!joined.body?.matchId) throw new Error(`вход не удался: ${JSON.stringify(joined.body)}`);
  const matchId = joined.body.matchId;
  cleanup.push(async () => { await api(`/rest/v1/matches?id=eq.${matchId}`, { method: 'DELETE' }); });

  const match = await api(`/rest/v1/matches?id=eq.${matchId}&select=acting_seat`);
  const acting = match.body[0].acting_seat;
  await api(`/rest/v1/matches?id=eq.${matchId}`, {
    method: 'PATCH',
    body: { turn_deadline: new Date(Date.now() - 1000).toISOString() },
  });

  const waiting = acting === 'A' ? 'B' : 'A';
  const claim = await call(`/matches/${matchId}/claim-timeout`, {
    token: players[waiting].token,
    method: 'POST',
  });
  const finished = (await api(`/rest/v1/matches?id=eq.${matchId}&select=status,end_reason,winner_seat`)).body[0];
  return { claim, finished, waiting };
}
