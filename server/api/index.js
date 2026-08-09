/**
 * Единственная серверная функция: все маршруты API.
 *
 * Отдельными файлами было бы нагляднее, но бесплатный тариф Vercel даёт
 * двенадцать функций на проект, а маршрутов у нас больше. Заодно холодный
 * старт теперь один на всё API, а не свой у каждого эндпоинта.
 *
 * Маршруты:
 *   POST /api/profile                     ник
 *   GET  /api/rooms                       список открытых комнат
 *   POST /api/rooms                       создать комнату
 *   GET  /api/rooms/{id}                  состояние комнаты
 *   POST /api/rooms/join                  войти по id или коду
 *   POST /api/rooms/{id}/leave            выйти из лобби
 *   GET  /api/matches/current             активная партия (реконнект)
 *   POST /api/matches/{id}/command        ход
 *   POST /api/matches/{id}/surrender      сдаться
 *   POST /api/matches/{id}/claim-timeout  засчитать просрочку соперника
 *   GET  /api/cron/reap                   уборка (по расписанию)
 */
import { Errors, cors, fail, readJson, routeOf, sendError, sendJson } from '../lib/http.js';
import { playerFrom, userIdFrom } from '../lib/auth.js';
import { rpc, upsert } from '../lib/db.js';
import { createRoom, joinRoom, leaveRoom, listRooms, roomState } from '../lib/rooms.js';
import { applyCommand, claimTimeout, currentMatch, surrender } from '../lib/matches.js';
import { TURN_SECONDS } from '../lib/rules.js';

export default async function handler(req, res) {
  cors(res);
  if (req.method === 'OPTIONS') {
    res.statusCode = 204;
    res.end();
    return;
  }

  try {
    const route = routeOf(req);
    const result = await dispatch(req, route);
    sendJson(res, result?.status ?? 200, result?.body ?? result ?? {});
  } catch (error) {
    sendError(res, error);
  }
}

async function dispatch(req, route) {
  const [head, second, third] = route;
  const isGet = req.method === 'GET';
  const isPost = req.method === 'POST';

  // Проверка живости и заодно способ узнать длину хода, не зашивая её в клиент.
  if (route.length === 0 || head === 'health') {
    return { turnSeconds: TURN_SECONDS, ok: true };
  }

  if (head === 'profile' && isPost) return setNickname(req);

  if (head === 'rooms') {
    if (!second && isGet) return { rooms: await listRooms() };
    if (!second && isPost) return createRoom(await playerFrom(req), await readJson(req));
    if (second === 'join' && isPost) return joinRoom(await playerFrom(req), await readJson(req));
    if (second && third === 'leave' && isPost) return leaveRoom(await playerFrom(req), second);
    if (second && !third && isGet) return roomState(await playerFrom(req), second);
  }

  if (head === 'matches') {
    if (second === 'current' && isGet) {
      const match = await currentMatch((await playerFrom(req)).id);
      return { match };
    }
    if (second && third === 'command' && isPost) {
      const player = await playerFrom(req);
      return applyCommand(player.id, second, await readJson(req));
    }
    if (second && third === 'surrender' && isPost) {
      return surrender((await playerFrom(req)).id, second);
    }
    if (second && third === 'claim-timeout' && isPost) {
      return claimTimeout((await playerFrom(req)).id, second);
    }
  }

  if (head === 'cron' && second === 'reap') return reap(req);

  fail(Errors.NOT_FOUND, { route: route.join('/') });
}

/**
 * Ник. Профиль заводится под тем же id, что и анонимный пользователь, поэтому
 * повторный вызов просто переименовывает.
 */
async function setNickname(req) {
  const id = await userIdFrom(req);
  const { nickname } = await readJson(req);
  const trimmed = typeof nickname === 'string' ? nickname.trim() : '';
  if (trimmed.length < 2 || trimmed.length > 16) {
    fail(Errors.BAD_REQUEST, { reason: 'ник: от 2 до 16 символов' });
  }
  const [profile] = await upsert('profiles', { id, nickname: trimmed });
  return { id: profile.id, nickname: profile.nickname };
}

/**
 * Уборка по расписанию: просроченные партии и брошенные лобби.
 *
 * Основную работу по тайм-аутам делает не она, а ленивая проверка при обращении
 * к партии: на бесплатном тарифе cron запускается раз в сутки. Здесь — мусор.
 */
async function reap(req) {
  const secret = process.env.CRON_SECRET;
  if (secret) {
    const header = req.headers.authorization ?? '';
    if (header !== `Bearer ${secret}`) fail(Errors.UNAUTHORIZED);
  }
  const expired = await rpc('expire_matches', { p_match_id: null });
  const closed = await rpc('close_stale_rooms', {});
  return { expiredMatches: expired?.length ?? 0, closedRooms: closed ?? 0 };
}
