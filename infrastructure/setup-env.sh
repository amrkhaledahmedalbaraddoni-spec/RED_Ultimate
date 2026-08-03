#!/usr/bin/env bash
# ── RED Ultimate — Environment Setup Script ────────────────────────────────
# Creates .env from .env.example with sensible defaults for development.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "🔴 RED Ultimate — Environment Setup"
echo "====================================="
echo ""

# Generate a random JWT secret
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')

# Check if .env already exists
if [ -f "$ROOT_DIR/.env" ]; then
    echo "⚠️  .env already exists. Backing up to .env.bak"
    cp "$ROOT_DIR/.env" "$ROOT_DIR/.env.bak"
fi

# Create .env
cat > "$ROOT_DIR/.env" << EOF
# RED Ultimate — Environment Configuration
# Generated on $(date -I)

# ── JWT Authentication ──────────────────────────────────────────────────────
RED_JWT_SECRET=$JWT_SECRET

# ── Bootstrap Admin ─────────────────────────────────────────────────────────
RED_ADMIN_EMAIL=admin@red.local
RED_ADMIN_PASSWORD=changeme123

# ── Network ─────────────────────────────────────────────────────────────────
PUBLIC_IP=127.0.0.1

# ── COTURN ──────────────────────────────────────────────────────────────────
TURN_SECRET=redturnsecret

# ── Database ────────────────────────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:postgresql://db-postgres:5432/red_sovereign
SPRING_DATASOURCE_USERNAME=red
SPRING_DATASOURCE_PASSWORD=password

SPRING_DATA_MONGODB_URI=mongodb://db-mongo:27017/red_messages

SPRING_DATA_REDIS_HOST=cache-redis
SPRING_DATA_REDIS_PORT=6379

# ── Storage (MinIO) ────────────────────────────────────────────────────────
MINIO_ROOT_USER=redadmin
MINIO_ROOT_PASSWORD=redsecret123
RED_STORAGE_ENDPOINT=http://minio:9000

# ── PSTN / Dumin ───────────────────────────────────────────────────────────
RED_DUMIN_BASE_URL=http://192.168.1.100:5060
RED_DUMIN_API_TOKEN=
EOF

echo "✅ .env created with:"
echo "   JWT_SECRET: $JWT_SECRET (auto-generated)"
echo "   ADMIN_EMAIL: admin@red.local"
echo "   ADMIN_PASSWORD: changeme123"
echo ""
echo "⚠️  IMPORTANT: Change the admin password before deploying!"
echo ""
echo "Next steps:"
echo "  1. Edit .env with your custom settings"
echo "  2. Run: docker compose up -d --build"
