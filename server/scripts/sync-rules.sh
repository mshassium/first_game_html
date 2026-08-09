#!/usr/bin/env bash
# Кладёт свежую сборку правил рядом с серверными функциями.
#
# Библиотека собирается Gradle из модуля gdx/rules, а Vercel деплоит только то,
# что лежит в репозитории: JVM и Gradle там нет. Поэтому собранные файлы
# коммитятся, и после любой правки правил надо прогнать этот скрипт.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$here/../.." && pwd)"
dist="$root/gdx/rules/build/dist/js/productionLibrary"
target="$here/../lib/rules"

echo "== собираю библиотеку правил"
"$root/gdx/gradlew" -p "$root/gdx" :rules:jsNodeProductionLibraryDistribution -q

if [ ! -f "$dist/first-game-rules.js" ]; then
    echo "нет собранной библиотеки в $dist" >&2
    exit 1
fi

mkdir -p "$target"
rm -f "$target"/*.js
cp "$dist"/*.js "$target/"

# Kotlin/JS выдаёт модули в формате CommonJS, а сервер объявлен как ESM.
# Свой package.json в этой папке возвращает её файлам прежнюю трактовку —
# иначе Node пытается читать UMD-обёртку как ES-модуль и падает на загрузке
# зависимости stdlib.
cat > "$target/package.json" <<'JSON'
{
  "type": "commonjs",
  "//": "Собрано из gdx/rules скриптом server/scripts/sync-rules.sh. Руками не править."
}
JSON

echo "== скопировано в server/lib/rules:"
ls -la "$target" | awk 'NR>1 {print "   " $9, $5}'

echo "== дымовая проверка фасада"
node "$root/gdx/rules/smoke/facade-smoke.cjs"
