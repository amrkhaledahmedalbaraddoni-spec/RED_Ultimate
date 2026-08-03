# RED Ultimate — Architecture

## Overview

RED Ultimate is a sovereign communication platform built on a hardened Signal-Android
fork with four custom sub-systems:

| System | Role | Key Components |
|--------|------|----------------|
| A | Ultra HD VoIP | `UltraHDCall` (AV1/4K), `QualityController`, Mediasoup SFU |
| B | PSTN/Dumin | `DuminManager`, `PstnManager`, Asterisk gateway |
| C | Guaranteed Delivery | `DeliveryEngine` (UUIDv7), `MessageDeliveryManager`, Redis dedup |

## Components

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Android App     │────▶│  RED Backend     │────▶│  PostgreSQL     │
│  (Signal fork +  │WS   │  (Spring Boot)   │     │  (users)        │
│   app-android)   │     │                  │────▶│  MongoDB        │
└─────────────────┘     │                  │     │  (messages,      │
                        │                  │────▶│   stories)       │
┌─────────────────┐     │                  │     └─────────────────┘
│  Admin Dashboard│────▶│                  │────▶│  Redis           │
│  (React)        │HTTP │                  │     │  (dedup, seq,    │
└─────────────────┘     └──────────────────┘     │   presence,      │
                        ┌──────────────────┐     │   rate-limit)    │
                        │  Mediasoup SFU   │     └─────────────────┘
                        │  (Node.js)       │
                        └──────────────────┘     ┌─────────────────┐
                        ┌──────────────────┐     │  MinIO          │
                        │  Asterisk PSTN   │     │  (media)        │
                        │  (Docker)        │     └─────────────────┘
                        └──────────────────┘
```

## Data Flow

### Message Delivery (System C)

1. **Sender** creates a `ChatFrame` with UUIDv7 id → persists to Room as SENDING
2. **WebSocket** sends the frame to the backend
3. **Backend** deduplicates (Redis SETNX, 24h TTL) → sequences (Redis INCR) → persists (MongoDB)
4. **Backend** ACKs the sender (STORED + sequence number)
5. **Backend** relays to the recipient's WebSocket session (or stores for offline sync)
6. **Recipient** client parses the frame → persists to Room as DELIVERED
7. **Sender** receives ACK → updates Room status to SENT/DELIVERED

### Auth Flow

1. **Register** → user created with PENDING status (BCrypt password)
2. **Admin approves** via `/api/admin/users/{id}/approve`
3. **Login** → returns JWT if APPROVED; rate-limited (5/5min per IP+email)
4. **Token persistence** → `TokenStore` (SharedPreferences) + auto-refresh on app start

### Stories

1. **Capture** → camera screen → upload to MinIO via `/api/media/upload`
2. **Create** → POST `/api/stories` with mediaUrl + TTL (default 24h)
3. **List** → GET `/api/stories` returns active stories (expiresAt > now)
4. **Auto-cleanup** → `StoryCleanupScheduler` runs every hour, deletes expired

## Build & Deploy

```bash
# Backend
cd backend-server && gradle bootJar

# Android
cd app-android && gradle :app:assembleDebug

# Full stack
docker compose up -d --build
```

See `SECURITY.md` for the TLS workflow and `DEPLOY.md` for detailed instructions.
