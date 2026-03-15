#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${BACKEND_DIR}/infra/docker-compose.local.yml"

compose_cmd() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

usage() {
  cat <<EOF
Usage:
  ./scripts/local-infra.sh up [service ...]
  ./scripts/local-infra.sh down [--volumes]
  ./scripts/local-infra.sh restart [service ...]
  ./scripts/local-infra.sh status
  ./scripts/local-infra.sh logs [service]
  ./scripts/local-infra.sh list
  ./scripts/local-infra.sh wait [timeoutSeconds]
EOF
}

ensure_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "docker command not found."
    exit 1
  fi
  if ! docker compose version >/dev/null 2>&1; then
    echo "docker compose command not available."
    exit 1
  fi
}

wait_ready() {
  local timeout="${1:-90}"
  local started
  started="$(date +%s)"

  while true; do
    mapfile -t container_ids < <(compose_cmd ps -q)
    if [[ ${#container_ids[@]} -eq 0 ]]; then
      echo "No infra containers are running yet."
    else
      local all_ready=true
      for cid in "${container_ids[@]}"; do
        local inspect_out name status health
        inspect_out="$(docker inspect --format '{{.Name}} {{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "${cid}")"
        read -r name status health <<< "${inspect_out}"
        name="${name#/}"

        if [[ "${status}" != "running" ]]; then
          all_ready=false
          break
        fi
        if [[ "${health}" != "none" && "${health}" != "healthy" ]]; then
          all_ready=false
          break
        fi
      done

      if [[ "${all_ready}" == "true" ]]; then
        echo "Infra is ready."
        compose_cmd ps
        return 0
      fi
    fi

    local now
    now="$(date +%s)"
    if (( now - started >= timeout )); then
      echo "Timed out waiting for infra readiness after ${timeout}s."
      compose_cmd ps || true
      return 1
    fi
    sleep 2
  done
}

ensure_docker

command="${1:-}"
shift || true

case "${command}" in
  up)
    compose_cmd up -d "$@"
    ;;
  down)
    if [[ "${1:-}" == "--volumes" ]]; then
      compose_cmd down --volumes --remove-orphans
    else
      compose_cmd down --remove-orphans
    fi
    ;;
  restart)
    if [[ $# -eq 0 ]]; then
      compose_cmd restart
    else
      compose_cmd restart "$@"
    fi
    ;;
  status)
    compose_cmd ps
    ;;
  logs)
    if [[ $# -eq 0 ]]; then
      compose_cmd logs -f
    else
      compose_cmd logs -f "$1"
    fi
    ;;
  list)
    compose_cmd config --services
    ;;
  wait)
    wait_ready "${1:-90}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
