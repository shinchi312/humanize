#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
SERVICES_SCRIPT="${SCRIPT_DIR}/local-services.sh"
INFRA_SCRIPT="${SCRIPT_DIR}/local-infra.sh"
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
)

declare -A SERVICE_PORT=(
  [api-gateway]=8080
  [auth-service]=8081
  [library-service]=8082
  [ingestion-service]=8083
  [reader-service]=8084
  [activity-service]=8085
  [recommendation-service]=8086
  [notification-service]=8087
  [ai-service]=8088
)

usage() {
  cat <<EOF
Usage:
  ./scripts/smoke.sh up [service ...]
  ./scripts/smoke.sh down [service ...]
  ./scripts/smoke.sh restart [service ...]
  ./scripts/smoke.sh status [service ...]
  ./scripts/smoke.sh check [service ...]
  ./scripts/smoke.sh logs <infra|service> [infraService]
  ./scripts/smoke.sh list

Notes:
  - No service arguments -> default set:
    ${DEFAULT_SERVICES[*]}
  - Use "all" to target all backend services.
  - 'down' without services stops all backend services and infra.
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
      echo "Run './scripts/smoke.sh list' for valid names."
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

wait_for_ping() {
  local service="$1"
  local timeout="${2:-180}"
  local port="${SERVICE_PORT[${service}]}"
  local url="http://127.0.0.1:${port}/internal/service/ping"
  local started
  started="$(date +%s)"

  while true; do
    if curl -fsS --max-time 2 "${url}" >/dev/null 2>&1; then
      echo "${service}: ping OK (${url})"
      return 0
    fi

    local now
    now="$(date +%s)"
    if (( now - started >= timeout )); then
      echo "${service}: ping timeout after ${timeout}s (${url})"
      echo "Hint: check logs with ./scripts/local-services.sh logs ${service}"
      return 1
    fi
    sleep 2
  done
}

check_services() {
  local failures=0
  while IFS= read -r svc; do
    if ! wait_for_ping "${svc}" 120; then
      failures=$((failures + 1))
    fi
  done < <(resolve_services "$@")

  if (( failures > 0 )); then
    echo "Smoke check failed for ${failures} service(s)."
    return 1
  fi
  echo "Smoke check passed."
}

up_flow() {
  ensure_env_file
  local selected=("$@")
  "${INFRA_SCRIPT}" up
  "${INFRA_SCRIPT}" wait 120
  "${SERVICES_SCRIPT}" start "${selected[@]}"
  "${SERVICES_SCRIPT}" status "${selected[@]}"
  check_services "${selected[@]}"
}

down_flow() {
  local selected=("$@")
  if [[ ${#selected[@]} -eq 0 ]]; then
    "${SERVICES_SCRIPT}" stop all
    "${INFRA_SCRIPT}" down
  else
    "${SERVICES_SCRIPT}" stop "${selected[@]}"
  fi
}

restart_flow() {
  local selected=("$@")
  if [[ ${#selected[@]} -eq 0 ]]; then
    down_flow
    up_flow
  else
    "${SERVICES_SCRIPT}" restart "${selected[@]}"
    check_services "${selected[@]}"
  fi
}

status_flow() {
  local selected=("$@")
  "${INFRA_SCRIPT}" status
  "${SERVICES_SCRIPT}" status "${selected[@]}"
}

command="${1:-}"
shift || true

case "${command}" in
  up|start)
    up_flow "$@"
    ;;
  down|stop)
    down_flow "$@"
    ;;
  restart)
    restart_flow "$@"
    ;;
  status)
    status_flow "$@"
    ;;
  check)
    check_services "$@"
    ;;
  logs)
    target="${1:-}"
    if [[ -z "${target}" ]]; then
      echo "logs requires <infra|service>."
      exit 1
    fi
    shift || true
    if [[ "${target}" == "infra" ]]; then
      "${INFRA_SCRIPT}" logs "${1:-}"
    elif contains_service "${target}"; then
      "${SERVICES_SCRIPT}" logs "${target}"
    else
      echo "Unknown target for logs: ${target}"
      exit 1
    fi
    ;;
  list)
    printf "%s\n" "${ALL_SERVICES[@]}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
