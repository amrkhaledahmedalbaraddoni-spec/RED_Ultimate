#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

command -v java >/dev/null 2>&1 || fail "Java 21 is required. Set JAVA_HOME before running this script."
command -v python3 >/dev/null 2>&1 || fail "python3 is required for repository checks."
command -v node >/dev/null 2>&1 || fail "Node.js is required for the admin dashboard."
command -v docker >/dev/null 2>&1 || fail "Docker is required for the local integration stack."
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 is required."

java -version

echo "[1/5] Building Android and backend..."
./gradlew buildAll --stacktrace

echo "[2/5] Building the admin dashboard..."
(
  cd admin_dashboard
  npm ci --no-audit --no-fund
  npm run build
)

echo "[3/5] Validating Docker configuration..."
docker compose config --quiet

echo "[4/5] Running repository and API checks..."
python3 audit_check.py
python3 api_contract_test.py
python3 integration_test.py

echo "[5/5] Verifying local stack health..."
docker compose up -d --build
for attempt in $(seq 1 30); do
  if curl --fail --silent http://127.0.0.1:8080/actuator/health >/tmp/red-health.json; then
    cat /tmp/red-health.json
    echo "All local RED checks passed."
    exit 0
  fi
  sleep 2
done

docker compose ps
fail "Backend health endpoint did not become ready within 60 seconds."
