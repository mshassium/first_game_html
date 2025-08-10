#!/usr/bin/env bash
#
# Safely refresh Cursor telemetry IDs in storage.json on macOS
# - Makes a timestamped backup
# - Generates new machineId/macMachineId/devDeviceId/sqmId
# - Tries to quit Cursor gracefully before editing
# - No NVRAM, no immutable flags, no random websites
#
set -Eeuo pipefail

# -------- Logging --------
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log()      { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()     { echo -e "${YELLOW}[WARN]${NC} $*"; }
err()      { echo -e "${RED}[ERROR]${NC} $*"; }
debug()    { echo -e "${BLUE}[DEBUG]${NC} $*"; }

# -------- Preconditions --------
if [[ "$(uname -s)" != "Darwin" ]]; then
  err "Этот скрипт рассчитан на macOS."
  exit 1
fi

need() { command -v "$1" >/dev/null 2>&1 || { err "Не найдено: $1"; MISSING=1; }; }
MISSING=0
need jq
need uuidgen
need openssl
if [[ "${MISSING}" == "1" ]]; then
  err "Установи отсутствующие зависимости (например, через Homebrew: brew install jq openssl)."
  exit 1
fi

# -------- Resolve user/home --------
# Если запущено с sudo — правим у реального пользователя, не у root.
TARGET_USER="${SUDO_USER:-$USER}"
TARGET_HOME="$(eval echo "~${TARGET_USER}")"

if [[ ! -d "${TARGET_HOME}" ]]; then
  err "Не удалось определить домашнюю папку пользователя ${TARGET_USER}"
  exit 1
fi

# -------- Paths --------
STORAGE_DIR="${TARGET_HOME}/Library/Application Support/Cursor/User/globalStorage"
STORAGE_FILE="${STORAGE_DIR}/storage.json"
BACKUP_DIR="${STORAGE_DIR}/backups"

if [[ ! -f "${STORAGE_FILE}" ]]; then
  err "Не найден файл: ${STORAGE_FILE}"
  warn "Запусти и закрой Cursor хотя бы один раз, затем повтори."
  exit 1
fi

# -------- Try to quit Cursor gracefully --------
quit_cursor() {
  # Пытаемся закрыть приложение штатно через AppleScript
  if pgrep -f "/Applications/Cursor.app/Contents/MacOS/Cursor" >/dev/null 2>&1; then
    warn "Обнаружен запущенный Cursor — пытаюсь закрыть аккуратно…"
    if command -v osascript >/dev/null 2>&1; then
      osascript -e 'tell application "Cursor" to quit' || true
      sleep 2
    fi
  fi
  # Если всё ещё жив — отправим TERM именно бинарю Cursor
  if pgrep -f "/Applications/Cursor.app/Contents/MacOS/Cursor" >/dev/null 2>&1; then
    warn "Cursor всё ещё работает — отправляю SIGTERM…"
    pkill -TERM -f "/Applications/Cursor.app/Contents/MacOS/Cursor" || true
    sleep 2
  fi
  # Последняя попытка: SIGKILL только целевому бинарю
  if pgrep -f "/Applications/Cursor.app/Contents/MacOS/Cursor" >/dev/null 2>&1; then
    warn "Принудительное завершение Cursor…"
    pkill -KILL -f "/Applications/Cursor.app/Contents/MacOS/Cursor" || true
    sleep 1
  fi
  if pgrep -f "/Applications/Cursor.app/Contents/MacOS/Cursor" >/dev/null 2>&1; then
    err "Не удалось закрыть Cursor. Закрой его вручную и запусти скрипт снова."
    exit 1
  fi
  log "Cursor остановлен."
}

# -------- Backup --------
timestamp() { date +"%Y%m%d_%H%M%S"; }
make_backup() {
  mkdir -p "${BACKUP_DIR}"
  local backup_file="${BACKUP_DIR}/storage.json.backup_$(timestamp)"
  cp -p "${STORAGE_FILE}" "${backup_file}"
  # Владелец — исходный пользователь
  chown "${TARGET_USER}":"$(id -gn "${TARGET_USER}")" "${backup_file}" 2>/dev/null || true
  chmod 0644 "${backup_file}" 2>/dev/null || true
  log "Резервная копия: ${backup_file}"
}

# -------- ID generation --------
rand_hex_32() { openssl rand -hex 32; }                                  # 64 hex chars
uuid_lc()     { uuidgen | tr '[:upper:]' '[:lower:]'; }
uuid_uc()     { uuidgen | tr '[:lower:]' '[:upper:]'; }

generate_ids() {
  MACHINE_ID="authuser_$(rand_hex_32)"
  MAC_MACHINE_ID="$(rand_hex_32)"
  DEVICE_ID="$(uuid_lc)"
  SQM_ID="{$(uuid_uc)}"
  debug "machineId=${MACHINE_ID}"
  debug "macMachineId=${MAC_MACHINE_ID}"
  debug "devDeviceId=${DEVICE_ID}"
  debug "sqmId=${SQM_ID}"
}

# -------- Apply changes --------
apply_changes() {
  # Сохраним права, чтобы не портить их
  local perms owner group
  perms="$(stat -f '%Op' "${STORAGE_FILE}")"
  owner="$(stat -f '%Su' "${STORAGE_FILE}")"
  group="$(stat -f '%Sg' "${STORAGE_FILE}")"

  # Временно разрешим запись текущему пользователю
  chmod u+w "${STORAGE_FILE}" || true

  local tmp
  tmp="$(mktemp)"
  # Обновляем/создаём нужные ключи
  jq --arg mid "${MACHINE_ID}" \
     --arg mmid "${MAC_MACHINE_ID}" \
     --arg did "${DEVICE_ID}" \
     --arg sid "${SQM_ID}" \
     '
     .["telemetry.machineId"]      = $mid  |
     .["telemetry.macMachineId"]   = $mmid |
     .["telemetry.devDeviceId"]    = $did  |
     .["telemetry.sqmId"]          = $sid
     ' "${STORAGE_FILE}" > "${tmp}"

  mv "${tmp}" "${STORAGE_FILE}"

  # Вернём исходные права/владельца
  chown "${owner}:${group}" "${STORAGE_FILE}" 2>/dev/null || true
  chmod "${perms}" "${STORAGE_FILE}" 2>/dev/null || true

  log "Обновлено: ${STORAGE_FILE}"
}

# -------- Main --------
log "Путь к storage.json: ${STORAGE_FILE}"
quit_cursor
make_backup
generate_ids
apply_changes
log "Готово. Запусти Cursor — должны подхватиться новые идентификаторы."
