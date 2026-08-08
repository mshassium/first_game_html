/**
 * Дымовая проверка JS-фасада: партия целиком через MatchFacade.
 *
 * Тесты домена гоняются и на Node, но границу Kotlin -> JavaScript они не
 * пересекают: экспортируемые имена, свойства результата и работа с null видны
 * только из настоящего JS. Именно этим кодом будет пользоваться серверная
 * функция, поэтому проверяется он так же, как она.
 *
 * Запуск:
 *   ./gradlew :rules:jsNodeProductionLibraryDistribution
 *   node rules/smoke/facade-smoke.cjs
 */
const path = require('path');

const dist = path.resolve(__dirname, '../build/dist/js/productionLibrary/first-game-rules.js');
const lib = require(dist);

const facade = lib.com?.first?.game?.domain?.js?.MatchFacade;
if (!facade) {
  console.error('MatchFacade не экспортирован; верхний уровень:', Object.keys(lib));
  process.exit(1);
}

let failures = 0;
function check(condition, message) {
  if (condition) return;
  console.error('  ПРОВАЛ:', message);
  failures++;
}

const SEED = 'дымовая партия';

// --- партия целиком ---------------------------------------------------------

const start = facade.newMatch(SEED);
check(start.ok === true, 'newMatch должен возвращать ok');
check(start.state.length > 0, 'newMatch должен вернуть состояние');
check(start.events.split('\n').length > 10, 'в раздаче должно быть много событий');

let state = start.state;
let version = 0;
let moves = 0;

while (!facade.isOver(state)) {
  const seat = facade.actingSeat(state);
  check(seat === 'A' || seat === 'B', `место должно быть A или B, получено ${seat}`);

  const view = facade.viewFor(state, seat);
  check(typeof view === 'string' && view.length > 0, 'вид места не построен');

  // Ход по виду: первая карта руки или первый вариант выбора. Строки вида
  // разбираются здесь так же грубо, как это делал бы чужой клиент.
  const lines = view.split('\n');
  const pending = lines[6];
  const command = pending.length > 0 ? 'choose;0' : 'play;0';

  const result = facade.apply(state, seat, command, `${SEED}#${++version}`);
  check(result.ok === true, `ход ${version} места ${seat} отбит: ${result.error}`);
  if (!result.ok) break;
  check(result.events.length > 0, `ход ${version} прошёл без событий`);

  // Событиями обоим местам: у соперника буквы прихода карт должны быть срезаны.
  check(facade.eventsFor(result.events, 'A') !== null, 'события для места A не построены');
  check(facade.eventsFor(result.events, 'B') !== null, 'события для места B не построены');

  state = result.state;
  moves++;
  if (moves > 500) {
    check(false, 'партия не заканчивается');
    break;
  }
}

const winner = facade.winnerSeat(state);
check(winner === 'A' || winner === 'B', `партия кончилась без победителя: ${winner}`);
check(facade.actingSeat(state) === null, 'в законченной партии ходить некому');

// --- отбой недопустимого ----------------------------------------------------

const fresh = facade.newMatch(SEED);
const acting = facade.actingSeat(fresh.state);
const idle = acting === 'A' ? 'B' : 'A';

const foreign = facade.apply(fresh.state, idle, 'play;0', `${SEED}#1`);
check(foreign.ok === false && foreign.error === 'NOT_YOUR_TURN', 'чужой ход должен отбиваться');

const illegal = facade.apply(fresh.state, acting, 'play;99', `${SEED}#1`);
check(illegal.ok === false && illegal.error === 'ILLEGAL_COMMAND', 'невозможный ход должен отбиваться');

const garbage = facade.apply('мусор', acting, 'play;0', SEED);
check(garbage.ok === false && garbage.error === 'BAD_STATE', 'битое состояние должно отбиваться');

const badSeat = facade.apply(fresh.state, 'Z', 'play;0', SEED);
check(badSeat.ok === false && badSeat.error === 'BAD_SEAT', 'неизвестное место должно отбиваться');

check(facade.viewFor('мусор', 'A') === null, 'вид из битого состояния должен быть null');

// --- скрытая информация -----------------------------------------------------

const viewA = facade.viewFor(fresh.state, 'A');
const viewB = facade.viewFor(fresh.state, 'B');
const handOf = (view) => view.split('\n')[2].split(';')[1]; // рука соперника в виде
check(handOf(viewA) === 'F,F,F,F,F', `рука соперника в виде A не скрыта: ${handOf(viewA)}`);
check(handOf(viewB) === 'F,F,F,F,F', `рука соперника в виде B не скрыта: ${handOf(viewB)}`);

if (failures > 0) {
  console.error(`\nдымовая проверка фасада: ${failures} провалов`);
  process.exit(1);
}
console.log(`дымовая проверка фасада пройдена: ${moves} ходов, победило место ${winner}`);
