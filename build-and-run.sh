#!/usr/bin/env bash
set -euo pipefail

echo "🔴 RED Master Build Sequence Starting..."

# 1. Check dependencies
if ! docker compose version >/dev/null 2>&1 && ! command -v docker-compose >/dev/null 2>&1; then
  echo "Error: 'docker compose' (or docker-compose) is not installed." >&2
  exit 1
fi

# Prefer the modern `docker compose` plugin; fall back to docker-compose.
if docker compose version >/dev/null 2>&1; then
  DC="docker compose"
else
  DC="docker-compose"
fi

# 2. Build all artifacts
echo "📦 Building Backend, SFU, Admin Panel..."
$DC build

# 3. Launch the stack
echo "🚀 Launching RED Sovereign stack..."
$DC up -d

echo "✅ All systems are ONLINE."
echo "🌐 Entry point:        http://localhost"
echo "📡 REST API:           http://localhost/api"
echo "🔌 WebSocket (chat):   ws://localhost/ws/chat"
echo "🎬 Media SFU:          http://localhost:4000  (udp 40000-40100)"
echo "🔧 MinIO console:      http://localhost:9001"
echo "----------------------------------------"
echo "Default admin: \${RED_ADMIN_EMAIL:-admin@red.local} / \${RED_ADMIN_PASSWORD:-changeme123}"
echo "Project RED is now operational."
