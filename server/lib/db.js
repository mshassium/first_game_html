/**
 * Доступ к базе секретным ключом.
 *
 * Сервер — единственный, кто пишет в базу: у клиента нет ни одной политики на
 * запись, а читать он может только свой вид партии. Поэтому весь доступ идёт
 * отсюда и только с секретным ключом, который живёт в переменных окружения.
 */
import { ApiError, Errors } from './http.js';

const URL_BASE = process.env.SUPABASE_URL;
const SECRET_KEY = process.env.SUPABASE_SECRET_KEY;

if (!URL_BASE || !SECRET_KEY) {
  console.error('нет SUPABASE_URL или SUPABASE_SECRET_KEY в переменных окружения');
}

async function request(pathname, { method = 'GET', body, prefer, headers = {} } = {}) {
  const response = await fetch(`${URL_BASE}${pathname}`, {
    method,
    headers: {
      apikey: SECRET_KEY,
      Authorization: `Bearer ${SECRET_KEY}`,
      'Content-Type': 'application/json',
      ...(prefer ? { Prefer: prefer } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const text = await response.text();
  const parsed = text ? safeParse(text) : null;

  if (!response.ok) {
    console.error('база ответила', response.status, text.slice(0, 500));
    throw new ApiError(Errors.SERVER_ERROR);
  }
  return parsed;
}

function safeParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/** Выборка из таблицы: query — готовая строка параметров PostgREST. */
export function select(table, query = '') {
  return request(`/rest/v1/${table}${query ? `?${query}` : ''}`);
}

/** Первая строка выборки или null. */
export async function selectOne(table, query) {
  const rows = await select(table, query);
  return Array.isArray(rows) ? rows[0] ?? null : rows;
}

export function insert(table, rows, { returning = true } = {}) {
  return request(`/rest/v1/${table}`, {
    method: 'POST',
    body: rows,
    prefer: returning ? 'return=representation' : 'return=minimal',
  });
}

export function upsert(table, rows) {
  return request(`/rest/v1/${table}`, {
    method: 'POST',
    body: rows,
    prefer: 'resolution=merge-duplicates,return=representation',
  });
}

export function update(table, query, patch) {
  return request(`/rest/v1/${table}?${query}`, {
    method: 'PATCH',
    body: patch,
    prefer: 'return=representation',
  });
}

/** Вызов функции базы. Атомарные операции живут там, а не здесь. */
export function rpc(name, args) {
  return request(`/rest/v1/rpc/${name}`, { method: 'POST', body: args });
}
