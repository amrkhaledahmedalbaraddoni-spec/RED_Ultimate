#!/usr/bin/env bash
set -euo pipefail

# Optional helper: pre-creates MinIO buckets (private) and verifies the database.
# The backend auto-creates the "red-media" bucket on startup, so this script is only needed
# if you want to pre-provision additional buckets before the first boot.

MINIO_HOST="${MINIO_HOST:-localhost}"
MINIO_PORT="${MINIO_PORT:-9000}"
MINIO_USER="${MINIO_ROOT_USER:-redadmin}"
MINIO_PASS="${MINIO_ROOT_PASSWORD:-redsecret123}"
DB_HOST="${DB_HOST:-localhost}"
DB_USER="${DB_USER:-red}"
DB_NAME="${DB_NAME:-red_sovereign}"

if ! command -v mc >/dev/null 2>&1; then
  echo "Skipping MinIO setup: 'mc' client not installed (backend will create buckets automatically)."
else
  mc alias set local "http://${MINIO_HOST}:${MINIO_PORT}" "${MINIO_USER}" "${MINIO_PASS}"
  mc mb --ignore-existing "local/red-media"
  # IMPORTANT: keep media private — never publish the bucket.
  echo "MinIO bucket 'red-media' is ready (private)."
fi

if command -v psql >/dev/null 2>&1; then
  PGPASSWORD="${DB_PASSWORD:-password}" psql -h "${DB_HOST}" -U "${DB_USER}" -d postgres -tc \
    "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'" | grep -q 1 || \
    PGPASSWORD="${DB_PASSWORD:-password}" psql -h "${DB_HOST}" -U "${DB_USER}" -d postgres -c "CREATE DATABASE ${DB_NAME};"
  echo "Database '${DB_NAME}' is ready."
else
  echo "Skipping DB check: 'psql' not installed."
fi

echo "✅ RED: environment setup complete."
