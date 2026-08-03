#!/usr/bin/env bash
# ── RED Ultimate — Generate self-signed CA + server cert for sovereign TLS ──
# Usage:  ./scripts/generate-local-cert.sh [HOST_IP]
# Default HOST_IP=192.168.1.50
set -euo pipefail

HOST_IP="${1:-192.168.1.50}"
CERTS_DIR="$(cd "$(dirname "$0")/.." && pwd)/certs"
CA_DIR="$CERTS_DIR/ca"
BKS_PASS="red_sovereign_2024"

echo "► Generating sovereign TLS certs for $HOST_IP …"
mkdir -p "$CERTS_DIR" "$CA_DIR"

# 1. CA key + cert
if [ ! -f "$CA_DIR/ca.key" ]; then
  openssl req -x509 -new -nodes -newkey rsa:4096 -keyout "$CA_DIR/ca.key" \
    -out "$CA_DIR/ca.crt" -days 3650 -sha256 \
    -subj "/CN=RED-Sovereign-CA/O=RED-Ultimate"
  echo "  ✓ CA created"
else
  echo "  ✓ CA exists"
fi

# 2. Server key + CSR + cert (with SAN for the IP)
if [ ! -f "$CERTS_DIR/server.key" ]; then
  openssl req -new -newkey rsa:2048 -nodes -keyout "$CERTS_DIR/server.key" \
    -out "$CERTS_DIR/server.csr" \
    -subj "/CN=$HOST_IP/O=RED-Ultimate"

  openssl x509 -req -in "$CERTS_DIR/server.csr" -CA "$CA_DIR/ca.crt" -CAkey "$CA_DIR/ca.key" \
    -CAcreateserial -out "$CERTS_DIR/server.crt" -days 365 -sha256 \
    -extfile <(printf "subjectAltName=IP:$HOST_IP\nbasicConstraints=CA:FALSE")
  echo "  ✓ Server cert created"
else
  echo "  ✓ Server cert exists"
fi

# 3. BKS keystore for Android app (requires keytool + BouncyCastle provider)
#    This step is optional — if keytool fails, copy the CA manually.
CA_BKS="$CERTS_DIR/red-local-ca.bks"
if ! command -v keytool &>/dev/null; then
  echo "  ⚠ keytool not found; skipping BKS keystore. Copy ca.crt to app/src/main/assets/ manually."
else
  if [ ! -f "$CA_BKS" ]; then
    keytool -importcert -noprompt -alias red-ca \
      -file "$CA_DIR/ca.crt" -keystore "$CA_BKS" \
      -storetype BKS -storepass "$BKS_PASS" \
      -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
      -providerpath /dev/null 2>/dev/null || \
    echo "  ⚠ BKS generation failed (BouncyCastle not on classpath). Copy ca.crt to app/src/main/assets/ manually."
  else
    echo "  ✓ BKS keystore exists"
  fi
fi

echo ""
echo "✅ Certificates generated in $CERTS_DIR/"
echo ""
echo "Next steps:"
echo "  1. For docker-compose:  docker compose up -d (mounts ./certs/ into nginx)"
echo "  2. For Android:         cp $CA_DIR/ca.crt  app/src/main/assets/red-local-ca.bks"
echo "     (or use the BKS file if generated above)"
echo "  3. Set USE_TLS = true in DevelopedServerConfig.java"
echo "  4. Wire REDLocalTrustStore in NetworkDependenciesModule (see SECURITY.md)"
