/**
 * Проверка Realtime: доходит ли ход соперника и не протекает ли чужой вид.
 *
 * Заодно это рабочий прототип протокола для клиента игры: Supabase Realtime
 * говорит кадрами Phoenix, и в M9.5 такой же обмен придётся написать руками на
 * Kotlin — библиотеки Supabase в TeaVM не живут.
 *
 * Запуск:
 *   ./gradlew -p gdx :rules:jsNodeProductionLibraryDistribution
 *   node server/checks/realtime-smoke.mjs
 */
import { ANON_KEY, URL_BASE, api, facade, makeMatch, reporter } from './lib.mjs';

const { check, finish } = reporter();
const SOCKET_URL = `${URL_BASE.replace('https://', 'wss://')}/realtime/v1/websocket?apikey=${ANON_KEY}&vsn=1.0.0`;

/** Подписка на изменения match_views под токеном игрока. */
function subscribe({ token, filter, label, topic }) {
  const socket = new WebSocket(SOCKET_URL);
  const received = [];
  let heartbeat;
  let counter = 0;
  const nextRef = () => String(++counter);

  const joined = new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error(`${label}: подписка не подтверждена`)), 15_000);

    socket.addEventListener('open', () => {
      socket.send(JSON.stringify({
        topic,
        event: 'phx_join',
        ref: nextRef(),
        payload: {
          config: {
            broadcast: { self: false },
            presence: { key: '' },
            postgres_changes: [
              { event: '*', schema: 'public', table: 'match_views', ...(filter ? { filter } : {}) },
            ],
          },
          // Без токена игрока Realtime применяет права анонимной роли и не отдаёт
          // ни одной строки: RLS-политика написана на authenticated.
          access_token: token,
        },
      }));
      // Соединение молча закрывается без heartbeat примерно через минуту.
      heartbeat = setInterval(() => {
        socket.send(JSON.stringify({ topic: 'phoenix', event: 'heartbeat', payload: {}, ref: nextRef() }));
      }, 25_000);
    });

    socket.addEventListener('message', (message) => {
      const frame = JSON.parse(message.data);
      if (frame.event === 'phx_reply' && frame.payload?.status === 'ok' && frame.topic === topic) {
        clearTimeout(timer);
        resolve(frame.payload.response);
      }
      if (frame.event === 'phx_reply' && frame.payload?.status === 'error') {
        clearTimeout(timer);
        reject(new Error(`${label}: ${JSON.stringify(frame.payload.response)}`));
      }
      if (frame.event === 'postgres_changes') received.push(frame.payload);
      if (frame.event === 'system' && frame.payload?.status === 'error') {
        reject(new Error(`${label}: ${frame.payload.message}`));
      }
    });

    socket.addEventListener('error', () => reject(new Error(`${label}: ошибка сокета`)));
  });

  return {
    joined,
    received,
    close() {
      clearInterval(heartbeat);
      socket.close();
    },
  };
}

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

console.log('1. Партия и два игрока');
const fixture = await makeMatch('проверка realtime');
check(fixture.statuses.match === 201, 'партия заведена');

console.log('2. Подписка игрока на свой вид');
const own = subscribe({
  token: fixture.host.token,
  filter: `player_id=eq.${fixture.host.id}`,
  label: 'свой вид',
  topic: 'realtime:own',
});
await own.joined;
check(true, 'подписка подтверждена');

// Подписка того же игрока на всю таблицу — так протекла бы чужая партия,
// если бы Realtime не применял RLS.
const greedy = subscribe({ token: fixture.host.token, label: 'вся таблица', topic: 'realtime:all' });
await greedy.joined;
check(true, 'подписка без фильтра тоже принята — значит защищать должен RLS, а не фильтр');

console.log('3. Ход: сервер обновляет оба вида');
const seat = facade.actingSeat(fixture.started.state);
const view = facade.viewFor(fixture.started.state, seat);
const command = view.split('\n')[6].length > 0 ? 'choose;0' : 'play;0';
const applied = facade.apply(fixture.started.state, seat, command, `${fixture.seed}#1`);
check(applied.ok, `ход применён (${applied.error ?? 'без ошибок'})`);

const deadline = new Date(Date.now() + 60_000).toISOString();
for (const [player, place] of [[fixture.host, 'A'], [fixture.guest, 'B']]) {
  const patch = await api(`/rest/v1/match_views?match_id=eq.${fixture.matchId}&player_id=eq.${player.id}`, {
    method: 'PATCH',
    prefer: 'return=representation',
    body: {
      version: 1,
      state: facade.viewFor(applied.state, place),
      events: facade.eventsFor(applied.events, place),
      deadline,
      updated_at: new Date().toISOString(),
    },
  });
  check(
    patch.status === 200 && patch.body?.length === 1,
    `вид места ${place} обновлён (${patch.status}, ${JSON.stringify(patch.body).slice(0, 120)})`,
  );
}

await wait(3000);

console.log('4. Что дошло');
check(own.received.length > 0, `свой ход дошёл по сокету (${own.received.length} событий)`);
const payload = own.received.at(-1)?.data?.record;
check(payload?.version === 1, `в событии новая версия партии (${payload?.version})`);
check(payload?.player_id === fixture.host.id, 'событие про свою строку');
check(
  payload?.state?.split('\n')[2].split(';')[1] === 'F,F,F,F,F' || payload?.state?.includes('\n'),
  'состояние пришло целиком',
);

// Подписка без фильтра — главная проверка: она слушает всю таблицу, но должна
// получить только свою строку. Если бы пришло пусто, тест ничего бы не доказал.
const mine = greedy.received.filter((event) => event.data?.record?.player_id === fixture.host.id);
const foreign = greedy.received.filter((event) => event.data?.record?.player_id === fixture.guest.id);
check(mine.length > 0, `подписка без фильтра живая: своя строка дошла (${mine.length} событий)`);
check(foreign.length === 0, `чужой вид не пришёл даже без фильтра (${foreign.length} событий)`);

own.close();
greedy.close();
await fixture.cleanup();
finish('проверка Realtime');
