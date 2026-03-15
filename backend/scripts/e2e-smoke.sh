#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
SMOKE_SCRIPT="${SCRIPT_DIR}/smoke.sh"
DEFAULT_ENV_FILE="${BACKEND_DIR}/infra/local-dev.env"
DEFAULT_ENV_TEMPLATE="${BACKEND_DIR}/infra/local-dev.env.example"
RUN_ROOT="${BACKEND_DIR}/.run/e2e"
E2E_ENV_FILE="${RUN_ROOT}/local-dev.e2e.env"
INGESTION_ROOT_DEFAULT="${BACKEND_DIR}/.run/ingestion-local-source"

AUTO_UP=true
ENV_FILE="${LOCAL_DEV_ENV_FILE:-${DEFAULT_ENV_FILE}}"
USER_ID="${E2E_USER_ID:-user-e2e}"
BOOK_ID="${E2E_BOOK_ID:-book-e2e-$(date +%s)}"
FILE_NAME="${E2E_FILE_NAME:-e2e-sample.pdf}"
TIMEOUT_SECONDS="${E2E_TIMEOUT_SECONDS:-180}"

LAST_STATUS=""
LAST_BODY=""
INGESTION_LOCAL_SOURCE_ROOT_VALUE=""

SERVICES=(
  auth-service
  library-service
  ingestion-service
  reader-service
  activity-service
  recommendation-service
)

declare -A SERVICE_PORT=(
  [auth-service]=8081
  [library-service]=8082
  [ingestion-service]=8083
  [reader-service]=8084
  [activity-service]=8085
  [recommendation-service]=8086
)

usage() {
  cat <<EOF
Usage:
  ./scripts/e2e-smoke.sh [--no-up] [--env-file <path>] [--user-id <id>] [--book-id <id>] [--timeout-seconds <n>]

Examples:
  ./scripts/e2e-smoke.sh
  ./scripts/e2e-smoke.sh --user-id user-42 --book-id book-42
  ./scripts/e2e-smoke.sh --no-up
EOF
}

log() {
  printf '[e2e] %s\n' "$*"
}

die() {
  printf '[e2e] ERROR: %s\n' "$*" >&2
  exit 1
}

require_cmd() {
  local cmd="$1"
  command -v "${cmd}" >/dev/null 2>&1 || die "Required command missing: ${cmd}"
}

urlencode_segment() {
  local raw="$1"
  local encoded
  encoded="$(printf '%s' "${raw}" \
    | sed -e 's/%/%25/g' -e 's/ /%20/g' -e 's|/|%2F|g' -e 's/:/%3A/g' -e 's/+/%2B/g')"
  printf '%s' "${encoded}"
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --no-up)
        AUTO_UP=false
        shift
        ;;
      --env-file)
        [[ $# -ge 2 ]] || die "--env-file requires a value"
        ENV_FILE="$2"
        shift 2
        ;;
      --user-id)
        [[ $# -ge 2 ]] || die "--user-id requires a value"
        USER_ID="$2"
        shift 2
        ;;
      --book-id)
        [[ $# -ge 2 ]] || die "--book-id requires a value"
        BOOK_ID="$2"
        shift 2
        ;;
      --timeout-seconds)
        [[ $# -ge 2 ]] || die "--timeout-seconds requires a value"
        TIMEOUT_SECONDS="$2"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        die "Unknown argument: $1"
        ;;
    esac
  done
}

upsert_env_value() {
  local file="$1"
  local key="$2"
  local value="$3"
  if rg -q "^${key}=" "${file}"; then
    sed -i "s|^${key}=.*|${key}=${value}|" "${file}"
  else
    printf '\n%s=%s\n' "${key}" "${value}" >> "${file}"
  fi
}

prepare_env() {
  mkdir -p "${RUN_ROOT}"

  if [[ ! -f "${ENV_FILE}" ]]; then
    if [[ -f "${DEFAULT_ENV_TEMPLATE}" ]]; then
      cp "${DEFAULT_ENV_TEMPLATE}" "${ENV_FILE}"
      log "Created missing env file from template: ${ENV_FILE}"
    else
      die "Env file not found: ${ENV_FILE}"
    fi
  fi

  cp "${ENV_FILE}" "${E2E_ENV_FILE}"

  upsert_env_value "${E2E_ENV_FILE}" "INGESTION_LOCAL_SOURCE_ROOT" "${INGESTION_ROOT_DEFAULT}"
  upsert_env_value "${E2E_ENV_FILE}" "INGESTION_MIN_EXTRACTED_CHARS" "20"
  upsert_env_value "${E2E_ENV_FILE}" "INGESTION_OCR_ENABLED" "false"

  export LOCAL_DEV_ENV_FILE="${E2E_ENV_FILE}"
  set -a
  # shellcheck disable=SC1090
  source "${E2E_ENV_FILE}"
  set +a

  INGESTION_LOCAL_SOURCE_ROOT_VALUE="${INGESTION_LOCAL_SOURCE_ROOT:-}"
  if [[ -z "${INGESTION_LOCAL_SOURCE_ROOT_VALUE}" ]]; then
    die "INGESTION_LOCAL_SOURCE_ROOT is empty after env load"
  fi
  mkdir -p "${INGESTION_LOCAL_SOURCE_ROOT_VALUE}"
}

http_json() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local tmp
  tmp="$(mktemp)"

  if [[ -n "${body}" ]]; then
    LAST_STATUS="$(curl -sS -o "${tmp}" -w "%{http_code}" -X "${method}" \
      -H "Content-Type: application/json" \
      --data "${body}" \
      "${url}" || true)"
  else
    LAST_STATUS="$(curl -sS -o "${tmp}" -w "%{http_code}" -X "${method}" "${url}" || true)"
  fi

  LAST_BODY="$(cat "${tmp}")"
  rm -f "${tmp}"
}

json_string_from_last() {
  local key="$1"
  printf '%s' "${LAST_BODY}" | tr -d '\n' | sed -n "s/.*\"${key}\":\"\\([^\"]*\\)\".*/\\1/p"
}

json_number_from_last() {
  local key="$1"
  printf '%s' "${LAST_BODY}" | tr -d '\n' | sed -n "s/.*\"${key}\":\\([0-9.][0-9.]*\\).*/\\1/p"
}

escape_pdf_text() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//(/\\(}"
  value="${value//)/\\)}"
  printf '%s' "${value}"
}

generate_pdf() {
  local target="$1"
  local text="$2"
  local escaped stream length
  escaped="$(escape_pdf_text "${text}")"
  stream="$(printf 'BT\n/F1 14 Tf\n72 760 Td\n(%s) Tj\n0 -24 Td\n(%s) Tj\n0 -24 Td\n(%s) Tj\nET\n' \
    "${escaped}" "${escaped}" "${escaped}")"
  length="$(printf '%s' "${stream}" | wc -c | tr -d ' ')"

  mkdir -p "$(dirname "${target}")"
  : > "${target}"

  printf '%%PDF-1.4\n' >> "${target}"
  local o1 o2 o3 o4 o5 xref
  o1="$(wc -c < "${target}")"
  printf '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n' >> "${target}"
  o2="$(wc -c < "${target}")"
  printf '2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n' >> "${target}"
  o3="$(wc -c < "${target}")"
  printf '3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n' >> "${target}"
  o4="$(wc -c < "${target}")"
  printf '4 0 obj\n<< /Length %s >>\nstream\n%sendstream\nendobj\n' "${length}" "${stream}" >> "${target}"
  o5="$(wc -c < "${target}")"
  printf '5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n' >> "${target}"
  xref="$(wc -c < "${target}")"

  printf 'xref\n0 6\n0000000000 65535 f \n' >> "${target}"
  printf '%010d 00000 n \n' "${o1}" >> "${target}"
  printf '%010d 00000 n \n' "${o2}" >> "${target}"
  printf '%010d 00000 n \n' "${o3}" >> "${target}"
  printf '%010d 00000 n \n' "${o4}" >> "${target}"
  printf '%010d 00000 n \n' "${o5}" >> "${target}"
  printf 'trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n%s\n%%%%EOF\n' "${xref}" >> "${target}"
}

start_stack() {
  if [[ "${AUTO_UP}" == "true" ]]; then
    log "Starting infra + selected services"
    "${SMOKE_SCRIPT}" up "${SERVICES[@]}"
  else
    log "Skipping stack start (--no-up), running fast health checks"
    for svc in "${SERVICES[@]}"; do
      local port="${SERVICE_PORT[${svc}]}"
      local url="http://127.0.0.1:${port}/internal/service/ping"
      if ! curl -fsS --max-time 3 "${url}" >/dev/null 2>&1; then
        die "Service not reachable: ${svc} (${url}). Start stack first with ./scripts/smoke.sh up"
      fi
    done
  fi
}

try_upload_intent() {
  local intent_url="http://127.0.0.1:8082/api/library/books/upload-intent"
  local body
  body="$(printf '{"userId":"%s","bookId":"%s","fileName":"%s","contentType":"application/pdf"}' \
    "${USER_ID}" "${BOOK_ID}" "${FILE_NAME}")"
  http_json POST "${intent_url}" "${body}"
}

mark_uploaded() {
  local object_key="$1"
  local uploaded_url="http://127.0.0.1:8082/api/library/books/uploaded"
  local body
  body="$(printf '{"bookId":"%s","userId":"%s","objectKey":"%s","contentType":"application/pdf"}' \
    "${BOOK_ID}" "${USER_ID}" "${object_key}")"
  http_json POST "${uploaded_url}" "${body}"

  if [[ "${LAST_STATUS}" -lt 200 || "${LAST_STATUS}" -ge 300 ]]; then
    die "mark uploaded failed (HTTP ${LAST_STATUS}): ${LAST_BODY}"
  fi
}

poll_ingestion_completed() {
  local book_id_encoded status_url status message
  book_id_encoded="$(urlencode_segment "${BOOK_ID}")"
  status_url="http://127.0.0.1:8083/api/ingestion/books/${book_id_encoded}/status"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))

  while (( SECONDS < deadline )); do
    http_json GET "${status_url}"
    status="$(json_string_from_last "status")"
    message="$(json_string_from_last "message")"

    if [[ "${status}" == "COMPLETED" ]]; then
      log "Ingestion completed: ${message}"
      return 0
    fi
    if [[ "${status}" == "FAILED" ]]; then
      die "Ingestion failed: ${message}"
    fi
    sleep 2
  done

  die "Timed out waiting for ingestion completion (${TIMEOUT_SECONDS}s)"
}

post_progress() {
  local progress_url="http://127.0.0.1:8084/api/reader/progress"
  local body
  body="$(printf '{"userId":"%s","bookId":"%s","page":12,"progressPercent":62.5}' \
    "${USER_ID}" "${BOOK_ID}")"
  http_json POST "${progress_url}" "${body}"
  if [[ "${LAST_STATUS}" -lt 200 || "${LAST_STATUS}" -ge 300 ]]; then
    die "reader progress update failed (HTTP ${LAST_STATUS}): ${LAST_BODY}"
  fi
}

poll_activity_observed() {
  local user_id_encoded url count
  user_id_encoded="$(urlencode_segment "${USER_ID}")"
  url="http://127.0.0.1:8085/api/activity/users/${user_id_encoded}/events"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))

  while (( SECONDS < deadline )); do
    http_json GET "${url}"
    count="$(json_number_from_last "count")"
    if [[ -n "${count}" ]] && (( ${count%.*} > 0 )); then
      log "Activity observed for user ${USER_ID}: count=${count%.*}"
      return 0
    fi
    sleep 2
  done

  die "Timed out waiting for activity events (${TIMEOUT_SECONDS}s)"
}

poll_recommendations() {
  local user_id_encoded url deadline
  user_id_encoded="$(urlencode_segment "${USER_ID}")"
  url="http://127.0.0.1:8086/api/recommendations/${user_id_encoded}"
  deadline=$((SECONDS + TIMEOUT_SECONDS))

  while (( SECONDS < deadline )); do
    http_json GET "${url}"
    if printf '%s' "${LAST_BODY}" | grep -Fq "\"bookId\":\"${BOOK_ID}\""; then
      local score
      score="$(printf '%s' "${LAST_BODY}" | tr -d '\n' | sed -n "s/.*\"bookId\":\"${BOOK_ID}\",\"score\":\\([0-9.][0-9.]*\\).*/\\1/p")"
      if [[ -n "${score}" ]]; then
        log "Recommendation produced for ${BOOK_ID} with score=${score}"
      else
        log "Recommendation produced for ${BOOK_ID}"
      fi
      return 0
    fi
    sleep 2
  done

  die "Timed out waiting for recommendation containing ${BOOK_ID} (${TIMEOUT_SECONDS}s)"
}

main() {
  parse_args "$@"
  require_cmd curl
  require_cmd rg
  require_cmd sed

  prepare_env
  start_stack

  local long_text
  long_text="The Dragon Mystery by Ada Lovelace. Magic and detective clues across the galaxy. "
  long_text="${long_text}${long_text}${long_text}${long_text}${long_text}${long_text}${long_text}${long_text}"

  local object_key=""
  try_upload_intent
  if [[ "${LAST_STATUS}" -ge 200 && "${LAST_STATUS}" -lt 300 ]]; then
    object_key="$(json_string_from_last "objectKey")"
    log "Upload intent created for objectKey=${object_key}"
  else
    object_key="users/${USER_ID}/books/${BOOK_ID}/${FILE_NAME}"
    log "Upload intent unavailable (HTTP ${LAST_STATUS}), using fallback objectKey=${object_key}"
  fi

  local pdf_path="${INGESTION_LOCAL_SOURCE_ROOT_VALUE%/}/${object_key}"
  generate_pdf "${pdf_path}" "${long_text}"
  log "Generated local PDF mirror: ${pdf_path}"

  mark_uploaded "${object_key}"
  log "Uploaded event accepted for bookId=${BOOK_ID}"

  poll_ingestion_completed
  post_progress
  log "Reader progress accepted"

  poll_activity_observed
  poll_recommendations

  log "E2E smoke passed"
  printf '\nSummary:\n'
  printf '  userId: %s\n' "${USER_ID}"
  printf '  bookId: %s\n' "${BOOK_ID}"
  printf '  objectKey: %s\n' "${object_key}"
  printf '  pdf: %s\n' "${pdf_path}"
  printf '  env: %s\n' "${E2E_ENV_FILE}"
}

main "$@"
