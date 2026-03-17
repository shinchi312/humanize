#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
RUN_DIR="${BACKEND_DIR}/.run"
PID_DIR="${RUN_DIR}/pids"
LOG_DIR="${RUN_DIR}/logs"
ENV_FILE="${LOCAL_DEV_ENV_FILE:-${BACKEND_DIR}/infra/local-dev.env}"

ALL_SERVICES=(
  api-gateway
  auth-service
  library-service
  ingestion-service
  reader-service
  activity-service
  recommendation-service
  notification-service
  ai-service
)

DEFAULT_SERVICES=(
  auth-service
  library-service
  ingestion-service
  reader-service
  activity-service
  recommendation-service
  notification-service
  ai-service
)

usage() {
  cat <<EOF
Usage:
  ./scripts/local-services.sh start [service ...]
  ./scripts/local-services.sh stop [service ...]
  ./scripts/local-services.sh restart [service ...]
  ./scripts/local-services.sh status [service ...]
  ./scripts/local-services.sh logs <service>
  ./scripts/local-services.sh list

Notes:
  - If no service is supplied, defaults are used:
    ${DEFAULT_SERVICES[*]}
  - Use "all" to target all services.
EOF
}

contains_service() {
  local target="$1"
  for svc in "${ALL_SERVICES[@]}"; do
    if [[ "${svc}" == "${target}" ]]; then
      return 0
    fi
  done
  return 1
}

resolve_services() {
  local args=("$@")
  if [[ ${#args[@]} -eq 0 ]]; then
    printf "%s\n" "${DEFAULT_SERVICES[@]}"
    return 0
  fi
  if [[ ${#args[@]} -eq 1 && "${args[0]}" == "all" ]]; then
    printf "%s\n" "${ALL_SERVICES[@]}"
    return 0
  fi
  for svc in "${args[@]}"; do
    if ! contains_service "${svc}"; then
      echo "Unknown service: ${svc}"
      echo "Run './scripts/local-services.sh list' for valid names."
      exit 1
    fi
  done
  printf "%s\n" "${args[@]}"
}

ensure_env_file() {
  if [[ ! -f "${ENV_FILE}" ]]; then
    cat <<EOF
Missing env file: ${ENV_FILE}

Create it from template:
  cp ${BACKEND_DIR}/infra/local-dev.env.example ${BACKEND_DIR}/infra/local-dev.env
EOF
    exit 1
  fi
}

ensure_runtime_dirs() {
  mkdir -p "${PID_DIR}" "${LOG_DIR}"
}

pid_file() {
  local service="$1"
  echo "${PID_DIR}/${service}.pid"
}

log_file() {
  local service="$1"
  echo "${LOG_DIR}/${service}.log"
}

is_running() {
  local service="$1"
  local pidf
  pidf="$(pid_file "${service}")"
  if [[ ! -f "${pidf}" ]]; then
    return 1
  fi
  local pid
  pid="$(cat "${pidf}")"
  if [[ -z "${pid}" ]]; then
    return 1
  fi
  kill -0 "${pid}" 2>/dev/null
}

start_service() {
  local service="$1"
  ensure_runtime_dirs
  ensure_env_file

  if is_running "${service}"; then
    echo "${service}: already running (pid $(cat "$(pid_file "${service}")"))"
    return 0
  fi

  local pidf logf
  pidf="$(pid_file "${service}")"
  logf="$(log_file "${service}")"

  nohup bash -lc "
    set -a
    source '${ENV_FILE}'
    set +a
    cd '${BACKEND_DIR}'
    ./scripts/with-local-jdk.sh -pl services/${service} -am spring-boot:run
  " > "${logf}" 2>&1 &

  local pid=$!
  echo "${pid}" > "${pidf}"
  sleep 1

  if kill -0 "${pid}" 2>/dev/null; then
    echo "${service}: started (pid ${pid}) log=${logf}"
  else
    echo "${service}: failed to start (see ${logf})"
    rm -f "${pidf}"
    return 1
  fi
}

stop_service() {
  local service="$1"
  local pidf
  pidf="$(pid_file "${service}")"
  if [[ ! -f "${pidf}" ]]; then
    echo "${service}: not running"
    return 0
  fi

  local pid
  pid="$(cat "${pidf}")"
  if kill -0 "${pid}" 2>/dev/null; then
    kill "${pid}" || true
    sleep 1
    if kill -0 "${pid}" 2>/dev/null; then
      kill -9 "${pid}" || true
    fi
    echo "${service}: stopped"
  else
    echo "${service}: stale pid file removed"
  fi
  rm -f "${pidf}"
}

status_service() {
  local service="$1"
  local logf
  logf="$(log_file "${service}")"
  if is_running "${service}"; then
    echo "${service}: running (pid $(cat "$(pid_file "${service}")")) log=${logf}"
  else
    echo "${service}: stopped"
  fi
}

command="${1:-}"
shift || true

case "${command}" in
  start)
    while IFS= read -r svc; do
      start_service "${svc}"
    done < <(resolve_services "$@")
    ;;
  stop)
    while IFS= read -r svc; do
      stop_service "${svc}"
    done < <(resolve_services "$@")
    ;;
  restart)
    while IFS= read -r svc; do
      stop_service "${svc}"
      start_service "${svc}"
    done < <(resolve_services "$@")
    ;;
  status)
    while IFS= read -r svc; do
      status_service "${svc}"
    done < <(resolve_services "$@")
    ;;
  logs)
    svc="${1:-}"
    if [[ -z "${svc}" ]]; then
      echo "Service is required for logs."
      exit 1
    fi
    if ! contains_service "${svc}"; then
      echo "Unknown service: ${svc}"
      exit 1
    fi
    tail -f "$(log_file "${svc}")"
    ;;
  list)
    printf "%s\n" "${ALL_SERVICES[@]}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
