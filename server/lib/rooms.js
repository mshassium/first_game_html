/**
 * Комнаты: список, создание, вход, выход.
 *
 * Всё, что должно быть атомарным, живёт функциями в базе (`create_room`,
 * `join_room`): вход в комнату — гонка по определению, и решать её на стороне
 * сервера двумя запросами нельзя.
 */
import { Errors, fail } from './http.js';
import { rpc, select, selectOne, update } from './db.js';
import { startMatch } from './matches.js';

/** Без похожих начертаний: ноль и O, единица и I в коде не встречаются. */
const CODE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
const CODE_LENGTH = 6;

function newCode() {
  let code = '';
  for (let i = 0; i < CODE_LENGTH; i++) {
    code += CODE_ALPHABET[Math.floor(Math.random() * CODE_ALPHABET.length)];
  }
  return code;
}

/**
 * Открытые комнаты.
 *
 * Хеш пароля наружу не уходит: клиент получает только признак «с замком».
 * Ради этого список и отдаёт функция, а не прямой запрос к базе — политики RLS
 * умеют скрывать строки, но не столбцы.
 */
export async function listRooms() {
  const rows = await select(
    'rooms',
    'status=eq.lobby&guest_id=is.null&select=id,code,name,created_at,password_hash,host_id&order=last_activity.desc&limit=50',
  );
  if (!rows?.length) return [];

  const hosts = await select('profiles', `id=in.(${rows.map((r) => r.host_id).join(',')})&select=id,nickname`);
  const nicknames = new Map(hosts.map((h) => [h.id, h.nickname]));

  return rows.map((room) => ({
    id: room.id,
    code: room.code,
    name: room.name,
    host: nicknames.get(room.host_id) ?? null,
    hasPassword: room.password_hash !== null,
    createdAt: room.created_at,
  }));
}

export async function createRoom(player, { name, password }) {
  const title = typeof name === 'string' ? name.trim() : '';
  if (title.length < 1 || title.length > 24) {
    fail(Errors.BAD_REQUEST, { reason: 'название комнаты: от 1 до 24 символов' });
  }
  if (password != null && typeof password !== 'string') {
    fail(Errors.BAD_REQUEST, { reason: 'пароль строкой' });
  }

  // Код случайный, поэтому совпадения возможны: пробуем ещё раз, а не падаем.
  for (let attempt = 0; attempt < 5; attempt++) {
    try {
      const room = await rpc('create_room', {
        p_host: player.id,
        p_name: title,
        p_code: newCode(),
        p_password: password || null,
      });
      return publicRoom(room, player.nickname);
    } catch (error) {
      if (attempt === 4) throw error;
    }
  }
  fail(Errors.SERVER_ERROR);
}

/** Комната глазами хозяина, ждущего гостя. */
export async function roomState(player, roomId) {
  const room = await selectOne('rooms', `id=eq.${roomId}&select=*`);
  if (!room) fail(Errors.ROOM_NOT_FOUND);
  if (room.host_id !== player.id && room.guest_id !== player.id) fail(Errors.ROOM_NOT_FOUND);

  const guest = room.guest_id
    ? await selectOne('profiles', `id=eq.${room.guest_id}&select=nickname`)
    : null;

  return {
    ...publicRoom(room, null),
    status: room.status,
    matchId: room.match_id,
    guest: guest?.nickname ?? null,
  };
}

/**
 * Вход в комнату. Как только место занято, партия начинается сразу — отдельной
 * кнопки «начать» нет, ждать второму игроку нечего.
 */
export async function joinRoom(player, { roomId, code, password }) {
  if (!roomId && !code) fail(Errors.BAD_REQUEST, { reason: 'нужен roomId или code' });

  const result = await rpc('join_room', {
    p_guest: player.id,
    p_room_id: roomId ?? null,
    p_code: code ?? null,
    p_password: password ?? null,
  });

  switch (result?.outcome) {
    case 'ok':
      break;
    case 'not_found':
      fail(Errors.ROOM_NOT_FOUND);
      break;
    case 'room_full':
      fail(Errors.ROOM_FULL);
      break;
    case 'own_room':
      fail(Errors.OWN_ROOM);
      break;
    case 'wrong_password':
      fail(Errors.WRONG_PASSWORD, { tries: result.tries });
      break;
    case 'too_many_tries':
      fail(Errors.TOO_MANY_TRIES);
      break;
    default:
      fail(Errors.SERVER_ERROR);
  }

  const room = await selectOne('rooms', `id=eq.${result.room_id}&select=*`);
  const match = await startMatch(room);
  return { matchId: match.id, seat: 'B', roomId: room.id };
}

/** Выход из лобби. Ушёл хозяин — комната закрывается. */
export async function leaveRoom(player, roomId) {
  const room = await selectOne('rooms', `id=eq.${roomId}&select=*`);
  if (!room) fail(Errors.ROOM_NOT_FOUND);

  if (room.host_id === player.id) {
    await update('rooms', `id=eq.${roomId}`, { status: 'closed', last_activity: new Date().toISOString() });
    return { closed: true };
  }
  if (room.guest_id === player.id) {
    await update('rooms', `id=eq.${roomId}`, { guest_id: null, last_activity: new Date().toISOString() });
    return { closed: false };
  }
  fail(Errors.ROOM_NOT_FOUND);
}

function publicRoom(room, hostNickname) {
  return {
    id: room.id,
    code: room.code,
    name: room.name,
    host: hostNickname,
    hasPassword: room.password_hash !== null,
    createdAt: room.created_at,
  };
}
