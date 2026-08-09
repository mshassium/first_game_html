/**
 * Проверка прав доступа к базе мультиплеера.
 *
 * Заводит двух анонимных игроков, комнату и настоящую партию (состояние считает
 * та же библиотека правил, что пойдёт в серверную функцию), после чего смотрит
 * на базу глазами игрока: свой вид партии виден, чужой нет, полное состояние и
 * комнаты не читаются вовсе.
 *
 * Запуск:
 *   ./gradlew -p gdx :rules:jsNodeProductionLibraryDistribution
 *   node server/checks/rls-smoke.mjs
 *
 * Нужны переменные окружения: SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, SUPABASE_SECRET_KEY.
 */
import { createRequire } from 'node:module';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const require = createRequire(import.meta.url);

const URL_BASE = need('SUPABASE_URL');
const ANON_KEY = need('SUPABASE_PUBLISHABLE_KEY');
const SECRET_KEY = need('SUPABASE_SECRET_KEY');

function need(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`нет переменной окружения ${name}`);
    process.exit(1);
  }
  return value;
}

let failures = 0;
function check(condition, message) {
  if (condition) {
    console.log('  ok  ', message);
    return;
  }
  console.error('  ПРОВАЛ', message);
  failures++;
}

async function api(pathname, { token = SECRET_KEY, key = SECRET_KEY, method = 'GET', body, prefer } = {}) {
  const headers = { apikey: key, Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
  if (prefer) headers.Prefer = prefer;
  const response = await fetch(`${URL_BASE}${pathname}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  let parsed = null;
  try {
    parsed = text ? JSON.parse(text) : null;
  } catch {
    parsed = text;
  }
  return { status: response.status, body: parsed };
}

/** Анонимный вход: тот же вызов, что будет делать игра. */
async function signInAnonymously() {
  const response = await fetch(`${URL_BASE}/auth/v1/signup`, {
    method: 'POST',
    headers: { apikey: ANON_KEY, 'Content-Type': 'application/json' },
    body: '{}',
  });
  const data = await response.json();
  if (!data.access_token) throw new Error(`анонимный вход не удался: ${JSON.stringify(data)}`);
  return { token: data.access_token, id: data.user.id };
}

const rules = require(path.join(HERE, '../../gdx/rules/build/dist/js/productionLibrary/first-game-rules.js'));
const facade = rules.com.first.game.domain.js.MatchFacade;

const code = Array.from({ length: 6 }, () => 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'[Math.floor(Math.random() * 32)]).join('');
const stamp = Date.now();

console.log('1. Два анонимных игрока');
const host = await signInAnonymously();
const guest = await signInAnonymously();
check(host.id !== guest.id, 'выданы разные пользователи');

console.log('2. Профили, комната и партия — сервисным ключом');
await api('/rest/v1/profiles', {
  method: 'POST',
  body: [{ id: host.id, nickname: `хозяин${stamp % 1000}` }, { id: guest.id, nickname: `гость${stamp % 1000}` }],
});

const room = await api('/rest/v1/rooms', {
  method: 'POST',
  prefer: 'return=representation',
  body: {
    code,
    name: 'проверка прав',
    host_id: host.id,
    guest_id: guest.id,
    status: 'playing',
    // Пароль хешируется в базе: серверу не нужна своя реализация bcrypt.
    password_hash: null,
  },
});
check(room.status === 201, `комната создана (${room.status})`);
const roomId = room.body?.[0]?.id;

const started = facade.newMatch(`проверка-${stamp}`);
const match = await api('/rest/v1/matches', {
  method: 'POST',
  prefer: 'return=representation',
  body: {
    room_id: roomId,
    seat_a: host.id,
    seat_b: guest.id,
    state: started.state,
    seed: `проверка-${stamp}`,
    version: 0,
    turn_deadline: new Date(stamp + 60_000).toISOString(),
  },
});
check(match.status === 201, `партия создана (${match.status})`);
const matchId = match.body?.[0]?.id;

const views = await api('/rest/v1/match_views', {
  method: 'POST',
  body: [
    {
      match_id: matchId, player_id: host.id, seat: 'A', version: 0,
      state: facade.viewFor(started.state, 'A'),
      events: facade.eventsFor(started.events, 'A'),
      deadline: new Date(stamp + 60_000).toISOString(),
    },
    {
      match_id: matchId, player_id: guest.id, seat: 'B', version: 0,
      state: facade.viewFor(started.state, 'B'),
      events: facade.eventsFor(started.events, 'B'),
      deadline: new Date(stamp + 60_000).toISOString(),
    },
  ],
});
check(views.status === 201, `виды партии записаны (${views.status})`);

console.log('3. База глазами игрока');
const asHost = { token: host.token, key: ANON_KEY };

const ownViews = await api('/rest/v1/match_views?select=seat,state', asHost);
check(ownViews.status === 200 && ownViews.body.length === 1, `виден ровно один вид партии (${ownViews.body?.length})`);
check(ownViews.body?.[0]?.seat === 'A', 'и это вид своего места');

const foreignView = await api(`/rest/v1/match_views?player_id=eq.${guest.id}`, asHost);
check(foreignView.status === 200 && foreignView.body.length === 0, 'вид соперника не отдаётся');

const fullMatch = await api('/rest/v1/matches?select=state', asHost);
check(fullMatch.status === 200 && fullMatch.body.length === 0, 'полное состояние партии недоступно');

const roomList = await api('/rest/v1/rooms?select=code,password_hash', asHost);
check(roomList.status === 200 && roomList.body.length === 0, 'комнаты напрямую не читаются');

const ownProfile = await api('/rest/v1/profiles?select=nickname', asHost);
check(ownProfile.status === 200 && ownProfile.body.length === 1, 'свой профиль виден');

console.log('4. Запись игроком запрещена');
const write = await api('/rest/v1/match_views', {
  ...asHost,
  method: 'PATCH',
  body: { version: 999 },
});
check(write.status >= 400 || write.status === 404, `правка своего вида отбита (${write.status})`);

const forge = await api('/rest/v1/matches', {
  ...asHost,
  method: 'POST',
  body: { room_id: roomId, seat_a: host.id, seat_b: guest.id, state: 'подделка', seed: 'x', turn_deadline: new Date().toISOString() },
});
check(forge.status >= 400, `подделка партии отбита (${forge.status})`);

console.log('5. Скрытая информация в видах');
const stateA = facade.viewFor(started.state, 'A');
const stateB = facade.viewFor(started.state, 'B');
const handOf = (state) => state.split('\n')[2].split(';')[1];
check(handOf(stateA) === 'F,F,F,F,F' && handOf(stateB) === 'F,F,F,F,F', 'рука соперника скрыта в обоих видах');

console.log('6. Уборка');
await api(`/rest/v1/matches?id=eq.${matchId}`, { method: 'DELETE' });
await api(`/rest/v1/rooms?id=eq.${roomId}`, { method: 'DELETE' });
await api(`/rest/v1/profiles?id=in.(${host.id},${guest.id})`, { method: 'DELETE' });

if (failures > 0) {
  console.error(`\nпроверка прав: ${failures} провалов`);
  process.exit(1);
}
console.log('\nпроверка прав пройдена');
