#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEFAULT_JDK="${HOME}/jdks/jdk-21"
LOCAL_JDK="${HUMANIZE_JDK_HOME:-${JAVA_HOME:-${DEFAULT_JDK}}}"

if [[ ! -x "${LOCAL_JDK}/bin/javac" ]]; then
  cat <<EOF
Error: javac not found at: ${LOCAL_JDK}/bin/javac

Fix:
1. Put JDK 21 in a user-owned folder (example: ~/jdks/jdk-21)
2. Re-run with one of:
   HUMANIZE_JDK_HOME=~/jdks/jdk-21 ./scripts/with-local-jdk.sh -v
   JAVA_HOME=~/jdks/jdk-21 ./scripts/with-local-jdk.sh -v
EOF
  exit 1
fi

export JAVA_HOME="${LOCAL_JDK}"
export PATH="${JAVA_HOME}/bin:${PATH}"

cd "${BACKEND_DIR}"
exec ./mvnw "$@"
