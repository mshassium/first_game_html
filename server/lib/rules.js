/**
 * Правила игры — та же библиотека, что и в клиенте.
 *
 * Модуль собран из `gdx/rules` (Kotlin Multiplatform) в JS и лежит рядом
 * готовым файлом: у Vercel нет ни JVM, ни Gradle, собрать его при деплое нечем.
 * Обновляется скриптом `scripts/sync-rules.sh`.
 */
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);

export const facade = require('./rules/first-game-rules.js').com.first.game.domain.js.MatchFacade;

/** Сколько даётся на ход. Истёк — партия засчитывается сопернику. */
export const TURN_SECONDS = 60;

export function deadlineFrom(now = Date.now()) {
  return new Date(now + TURN_SECONDS * 1000).toISOString();
}
