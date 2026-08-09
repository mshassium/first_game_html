/**
 * Общее для проверок базы: доступ к REST, анонимный вход и заготовка партии.
 *
 * Нужны переменные окружения: SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, SUPABASE_SECRET_KEY.
 */
import { createRequire } from 'node:module';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const require = createRequire(import.meta.url);

export const URL_BASE = need('SUPABASE_URL');
export const ANON_KEY = need('SUPABASE_PUBLISHABLE_KEY');
export const SECRET_KEY = need('SUPABASE_SECRET_KEY');

function need(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`нет переменной окружения ${name}`);
    process.exit(1);
  }
  return value;
}

/** Правила игры — та же сборка, что пойдёт в серверную функцию. */
export const facade = require(
  path.join(HERE, '../../gdx/rules/build/dist/js/productionLibrary/first-game-rules.js'),
).com.first.game.domain.js.MatchFacade;

export function reporter() {
  const state = { failures: 0 };
  return {
    check(condition, message) {
      if (condition) {
        console.log('  ok  ', message);
      } else {
        console.error('  ПРОВАЛ', message);
        state.failures++;
      }
    },
    finish(title) {
      if (state.failures > 0) {
        console.error(`\n${title}: ${state.failures} провалов`);
        process.exit(1);
      }
      console.log(`\n${title} пройдена`);
    },
  };
}

export async function api(pathname, options = {}) {
  const { token = SECRET_KEY, key = SECRET_KEY, method = 'GET', body, prefer } = options;
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
export async function signInAnonymously() {
  const response = await fetch(`${URL_BASE}/auth/v1/signup`, {
    method: 'POST',
    headers: { apikey: ANON_KEY, 'Content-Type': 'application/json' },
    body: '{}',
  });
  const data = await response.json();
  if (!data.access_token) throw new Error(`анонимный вход не удался: ${JSON.stringify(data)}`);
  return { token: data.access_token, id: data.user.id };
}

export function roomCode() {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  return Array.from({ length: 6 }, () => alphabet[Math.floor(Math.random() * alphabet.length)]).join('');
}

/**
 * Заготовка: два игрока, комната, партия и два вида. Возвращает всё, что нужно
 * для проверок, и функцию уборки.
 */
export async function makeMatch(label) {
  const stamp = Date.now();
  const host = await signInAnonymously();
  const guest = await signInAnonymously();

  await api('/rest/v1/profiles', {
    method: 'POST',
    body: [
      { id: host.id, nickname: `хозяин${stamp % 1000}` },
      { id: guest.id, nickname: `гость${stamp % 1000}` },
    ],
  });

  const room = await api('/rest/v1/rooms', {
    method: 'POST',
    prefer: 'return=representation',
    body: {
      code: roomCode(),
      name: label,
      host_id: host.id,
      guest_id: guest.id,
      status: 'playing',
    },
  });
  const roomId = room.body?.[0]?.id;

  const seed = `${label}-${stamp}`;
  const started = facade.newMatch(seed);
  const deadline = new Date(stamp + 60_000).toISOString();

  const match = await api('/rest/v1/matches', {
    method: 'POST',
    prefer: 'return=representation',
    body: {
      room_id: roomId,
      seat_a: host.id,
      seat_b: guest.id,
      state: started.state,
      seed,
      version: 0,
      turn_deadline: deadline,
    },
  });
  const matchId = match.body?.[0]?.id;

  await api('/rest/v1/match_views', {
    method: 'POST',
    body: [host, guest].map((player, index) => ({
      match_id: matchId,
      player_id: player.id,
      seat: index === 0 ? 'A' : 'B',
      version: 0,
      state: facade.viewFor(started.state, index === 0 ? 'A' : 'B'),
      events: facade.eventsFor(started.events, index === 0 ? 'A' : 'B'),
      deadline,
    })),
  });

  return {
    host,
    guest,
    roomId,
    matchId,
    seed,
    started,
    statuses: { room: room.status, match: match.status },
    async cleanup() {
      await api(`/rest/v1/matches?id=eq.${matchId}`, { method: 'DELETE' });
      await api(`/rest/v1/rooms?id=eq.${roomId}`, { method: 'DELETE' });
      await api(`/rest/v1/profiles?id=in.(${host.id},${guest.id})`, { method: 'DELETE' });
    },
  };
}
