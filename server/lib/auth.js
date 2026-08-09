/**
 * Кто пришёл с запросом.
 *
 * Токен проверяется у самой Supabase — запросом `/auth/v1/user`. Разбирать JWT
 * на месте было бы быстрее, но новые ключи проекта подписываются асимметрично,
 * и пришлось бы тянуть JWKS с ротацией. Для пошаговой игры лишние полсотни
 * миллисекунд на ход ничего не решают, а ошибиться в проверке подписи — решает.
 *
 * Ответы кэшируются на время жизни экземпляра функции: подряд идущие запросы
 * одного игрока не дёргают Supabase заново.
 */
import { Errors, fail } from './http.js';
import { selectOne } from './db.js';

const URL_BASE = process.env.SUPABASE_URL;
const ANON_KEY = process.env.SUPABASE_PUBLISHABLE_KEY;

/** Токен -> {id, expiresAt}. Живёт, пока жив экземпляр функции. */
const cache = new Map();
const CACHE_TTL_MS = 60_000;

export async function userIdFrom(req) {
  const header = req.headers.authorization ?? '';
  const token = header.startsWith('Bearer ') ? header.slice(7).trim() : '';
  if (!token) fail(Errors.UNAUTHORIZED);

  const cached = cache.get(token);
  if (cached && cached.expiresAt > Date.now()) return cached.id;

  const response = await fetch(`${URL_BASE}/auth/v1/user`, {
    headers: { apikey: ANON_KEY, Authorization: `Bearer ${token}` },
  });
  if (!response.ok) fail(Errors.UNAUTHORIZED);

  const user = await response.json();
  if (!user?.id) fail(Errors.UNAUTHORIZED);

  cache.set(token, { id: user.id, expiresAt: Date.now() + CACHE_TTL_MS });
  // Кэш не должен расти бесконечно: экземпляр функции живёт долго.
  if (cache.size > 500) cache.clear();
  return user.id;
}

/**
 * Игрок вместе с профилем. Без ника в комнаты не пускаем: соперник должен
 * видеть, с кем играет.
 */
export async function playerFrom(req, { requireNickname = true } = {}) {
  const id = await userIdFrom(req);
  const profile = await selectOne('profiles', `id=eq.${id}&select=id,nickname`);
  if (!profile && requireNickname) fail(Errors.NICKNAME_REQUIRED);
  return { id, nickname: profile?.nickname ?? null };
}
