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
│  (Signal + RED   │WS   │  (Spring Boot)   │     │  (users)        │
│   in :app)       │     │                  │────▶│  MongoDB        │
└─────────────────┘     │                  │     │  (messages,      │
                        │                  │     │   stories,       │
┌─────────────────┐     │                  │     │   blocks,        │
│  Admin Dashboard│────▶│                  │────▶│   audit_log)     │
│  (React)        │HTTP │                  │     └─────────────────┘
└─────────────────┘     │                  │────▶│  Redis           │
                        ┌──────────────────┐     │  (dedup, seq,    │
                        │  Mediasoup SFU   │     │   presence,      │
                        │  (Node.js)       │     │   rate-limit,    │
                        └──────────────────┘     │   notifications) │
                        ┌──────────────────┐     └─────────────────┘
                        │  Asterisk PSTN   │     ┌─────────────────┐
                        │  (Docker)        │     │  MinIO          │
                        └──────────────────┘     │  (media)        │
                                                 └─────────────────┘
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

### Read Receipts

1. **Recipient** marks messages as READ → sends `READ` frame via WebSocket
2. **Backend** `ReadReceiptService` notifies the sender via WebSocket
3. **Sender** updates message status to READ

### Typing Indicators

1. **User** starts typing → sends `TYPING` frame via WebSocket
2. **Backend** `ChatWebSocketHandler` forwards to the conversation peer
3. **Peer** receives and displays typing indicator

### User Blocking

1. **Blocker** calls `/api/blocks` → `BlockService` creates a `BlockDocument` in MongoDB
2. Future messages from the blocked user are silently dropped
3. Blocked user cannot see blocker's presence

### Auth Flow

1. **Register** → user created with PENDING status (BCrypt password)
2. **Admin approves** via `/api/admin/users/{id}/approve`
3. **Login** → returns JWT if APPROVED; rate-limited (5/5min per IP+email)
4. **Token persistence** → `TokenStore` (SharedPreferences) + auto-refresh on app start
5. **Password change** → `/api/auth/change-password` (requires current password)
6. **Profile update** → `/api/users/me` (name, phone number)

### Stories

1. **Capture** → camera screen → upload to MinIO via `/api/media/upload`
2. **Create** → POST `/api/stories` with mediaUrl + TTL (default 24h)
3. **List** → GET `/api/stories` returns active stories (expiresAt > now)
4. **Auto-cleanup** → `StoryCleanupScheduler` runs every minute, deletes expired
5. **Delete** → DELETE `/api/stories/{id}` (owner or admin only)

### Offline Notifications

1. When a message arrives for an offline user, `NotificationService` enqueues it in Redis
2. On reconnect, the client retrieves pending notifications via `/api/notifications/pending`
3. Notifications have a 7-day TTL in Redis

### Audit Log

1. All security-relevant events are logged to MongoDB via `AuditLogService`
2. Events include: user approvals, bans, promotions, kill-switch activations, status changes
3. Admin can view audit log via `/api/admin/audit` endpoint
4. Audit log is append-only and never deleted

## API Reference

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user (PENDING status) |
| POST | `/api/auth/login` | Login (returns JWT if APPROVED) |
| GET | `/api/auth/status` | Get current user status |
| POST | `/api/auth/change-password` | Change password (requires current) |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get own profile |
| PUT | `/api/users/me` | Update own profile |
| GET | `/api/users/{id}` | Get public profile |
| GET | `/api/users/search?q=...` | Search users by name/email |

### Conversations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/conversations` | List conversations |
| GET | `/api/messages/conversation?conversationId=...&since=...` | Get messages |
| GET | `/api/messages/pending?since=...` | Get pending messages |
| DELETE | `/api/messages/{messageId}` | Delete own message |

### Stories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stories` | List active stories |
| POST | `/api/stories` | Create story |
| DELETE | `/api/stories/{id}` | Delete story |

### Blocking
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/blocks` | Block a user |
| DELETE | `/api/blocks/{blockeeId}` | Unblock a user |
| GET | `/api/blocks` | List blocked users |
| GET | `/api/blocks/check/{userId}` | Check if user is blocked |

### Media
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/media/upload` | Upload file to MinIO |
| GET | `/api/media/{objectName}` | Download file |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/pending` | List pending users |
| POST | `/api/admin/users/{id}/approve` | Approve user |
| POST | `/api/admin/users/{id}/reject` | Reject user |
| POST | `/api/admin/users/{id}/ban` | Ban user |
| POST | `/api/admin/users/{id}/promote` | Promote to admin |
| GET | `/api/admin/monitor/health` | System health |
| GET | `/api/admin/monitor/stats` | System stats |
| GET | `/api/admin/audit` | View audit log |
| POST | `/api/admin/security/kill-switch/{userId}` | Emergency revoke |

### PSTN
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/pstn/dial?number=...` | Dial PSTN number |
| GET | `/api/pstn/sim` | Get SIM status |

## Build & Deploy

```bash
# Backend
cd backend-server && gradle bootJar

# Android (the single merged application)
./gradlew :Signal-Android:assemblePlayProdDebug

# Windows (Arabic locale)
build-windows.bat

# Full stack
docker compose up -d --build
```

See `SECURITY.md` for the TLS workflow and `DEPLOY.md` for detailed instructions.
