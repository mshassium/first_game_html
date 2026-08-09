/** Разбор запроса и ответы: всё, что не про игру. */

/** Коды ошибок API. Клиент разбирает именно их, а не текст сообщения. */
export const Errors = {
  UNAUTHORIZED: 'unauthorized',
  NICKNAME_REQUIRED: 'nickname_required',
  BAD_REQUEST: 'bad_request',
  NOT_FOUND: 'not_found',
  ROOM_NOT_FOUND: 'room_not_found',
  ROOM_FULL: 'room_full',
  OWN_ROOM: 'own_room',
  WRONG_PASSWORD: 'wrong_password',
  TOO_MANY_TRIES: 'too_many_tries',
  MATCH_NOT_FOUND: 'match_not_found',
  NOT_YOUR_MATCH: 'not_your_match',
  NOT_YOUR_TURN: 'not_your_turn',
  STALE_VERSION: 'stale_version',
  ILLEGAL_COMMAND: 'illegal_command',
  MATCH_FINISHED: 'match_finished',
  NOT_EXPIRED: 'not_expired',
  SERVER_ERROR: 'server_error',
};

/** Каким кодом HTTP отвечать на ошибку. */
const STATUS = {
  [Errors.UNAUTHORIZED]: 401,
  [Errors.NICKNAME_REQUIRED]: 403,
  [Errors.BAD_REQUEST]: 400,
  [Errors.NOT_FOUND]: 404,
  [Errors.ROOM_NOT_FOUND]: 404,
  [Errors.MATCH_NOT_FOUND]: 404,
  [Errors.NOT_YOUR_MATCH]: 403,
  [Errors.ROOM_FULL]: 409,
  [Errors.OWN_ROOM]: 409,
  [Errors.WRONG_PASSWORD]: 403,
  [Errors.TOO_MANY_TRIES]: 429,
  [Errors.NOT_YOUR_TURN]: 409,
  [Errors.STALE_VERSION]: 409,
  [Errors.ILLEGAL_COMMAND]: 422,
  [Errors.MATCH_FINISHED]: 409,
  [Errors.NOT_EXPIRED]: 409,
  [Errors.SERVER_ERROR]: 500,
};

/** Ошибка, которую обработчик бросает, а роутер превращает в ответ. */
export class ApiError extends Error {
  constructor(code, details) {
    super(code);
    this.code = code;
    this.details = details;
  }
}

export function fail(code, details) {
  throw new ApiError(code, details);
}

/**
 * Игра открывается и с домена, и с локального сервера при отладке, а десктопная
 * сборка про CORS вообще не знает. Заголовки в запросе — только Authorization,
 * то есть куки в игре не участвуют, и звёздочка ничего не открывает лишнего.
 */
export function cors(res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,OPTIONS');
  // apikey — на случай, если клиент пришлёт ключ проекта Supabase заодно:
  // браузер отклоняет весь запрос, если заголовок не перечислен здесь.
  res.setHeader('Access-Control-Allow-Headers', 'Authorization,Content-Type,apikey');
  res.setHeader('Access-Control-Max-Age', '86400');
}

export function sendJson(res, status, body) {
  res.statusCode = status;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.end(JSON.stringify(body));
}

export function sendError(res, error) {
  const code = error instanceof ApiError ? error.code : Errors.SERVER_ERROR;
  const status = STATUS[code] ?? 500;
  if (!(error instanceof ApiError)) {
    // Неожиданное — в лог функции целиком, наружу только код.
    console.error('необработанная ошибка:', error);
  }
  sendJson(res, status, error?.details ? { error: code, ...error.details } : { error: code });
}

/** Тело запроса как объект. Пустое тело — пустой объект, а не ошибка. */
export async function readJson(req) {
  if (req.body !== undefined && req.body !== null) {
    return typeof req.body === 'string' ? parse(req.body) : req.body;
  }
  const chunks = [];
  for await (const chunk of req) chunks.push(chunk);
  return parse(Buffer.concat(chunks).toString('utf8'));
}

function parse(raw) {
  if (!raw) return {};
  try {
    return JSON.parse(raw);
  } catch {
    fail(Errors.BAD_REQUEST, { reason: 'тело запроса не разбирается как JSON' });
  }
}

/**
 * Путь запроса как массив кусков.
 *
 * Все эндпоинты обслуживает одна функция: у бесплатного тарифа Vercel их всего
 * двенадцать на проект, а маршрутов у нас больше. Vercel переписывает `/api/x/y`
 * в `/api/index?path=x&path=y`, отсюда и разбор.
 */
export function routeOf(req) {
  const url = new URL(req.url, 'http://localhost');
  const fromQuery = url.searchParams.getAll('path');
  if (fromQuery.length > 0) {
    return fromQuery.flatMap((part) => part.split('/')).filter(Boolean);
  }
  return url.pathname.replace(/^\/api\/?/, '').split('/').filter(Boolean);
}
