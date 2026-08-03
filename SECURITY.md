# RED Ultimate — Security Documentation

## Threat Model

RED Ultimate is a **sovereign** communication system designed for isolated LANs. The
primary trust boundary is the **local network perimeter**. Within that boundary, the
system assumes the network is not hostile; outside it, all traffic must be encrypted.

## Security Features

### Authentication & Authorization
- **JWT (HS256)**: Stateless authentication with configurable TTL
- **BCrypt (12 rounds)**: Password hashing with strong salting
- **Rate Limiting**: 5 login attempts per 5 minutes per IP+email combination
- **Admin Approval Gate**: New users require admin approval before accessing the system
- **Role-Based Access**: USER and ADMIN roles with different API access levels
- **Password Change**: Users can change their password with current password verification

### Data Protection
- **Log Scrubbing**: `LogScrubber` redacts IPv4 addresses, emails, and Bearer tokens from logs
- **No Password in Views**: `UserView` never exposes password hashes
- **UUIDv7**: RFC 9562 compliant message IDs for deduplication without leaking sequence info

### Communication Security
- **WebSocket Authentication**: JWT token in handshake query parameter
- **Kill-Switch**: Admin can immediately ban a user and terminate their WebSocket session
- **User Blocking**: Users can block other users; messages from blocked users are silently dropped
- **Read Receipts**: End-to-end delivery tracking (SENDING → SENT → DELIVERED → READ)

### Audit & Monitoring
- **Audit Log**: All security-relevant events are logged to MongoDB (append-only)
- **Live Monitoring**: Real-time system health, stats, and connection tracking
- **Notification Service**: Offline users receive notifications when they reconnect

## Claim Status

| # | Claim | Status | Notes |
|---|-------|--------|-------|
| أ | Unsecured channel (`http://`) | ✅ Fixed | `DevelopedServerConfig.USE_TLS` flag; see TLS workflow below |
| ب | Approval bypass (`return true`) | ✅ Fixed | `MasterIntegration.checkAdminApproval()` reads SharedPreferences (default false); server-backed check available via `verifyApprovalFromServer()` |
| ج | TrustStore weakness ("whisper") | ✅ Mitigated | "whisper" is Signal's upstream CA bundle format — not a RED-introduced vulnerability. A separate `REDLocalTrustStore` is provided for the sovereign server's CA |

## TLS Workflow (Production)

### 1. Generate the sovereign CA + server cert

```bash
./scripts/generate-local-cert.sh 192.168.1.50
```

This creates `certs/ca/` (CA key + cert) and `certs/server.crt` + `server.key` (server cert with SAN for the IP).

### 2. Configure nginx for TLS

Replace `nginx.conf` with `nginx.tls.conf` in `docker-compose.yml`:
```yaml
nginx:
  volumes:
    - ./nginx.tls.conf:/etc/nginx/conf.d/default.conf:ro
    - ./certs:/etc/nginx/certs:ro
```

### 3. Trust the sovereign CA in the Android app

Copy the CA to the app's assets:
```bash
cp certs/ca/ca.crt app/src/main/assets/red-local-ca.bks
```

Wire `REDLocalTrustStore` in `NetworkDependenciesModule.signalOkHttpClient()` — replace:
```java
// before:
SignalServiceTrustStore(context)
// after:
REDLocalTrustStore(context)
```

### 4. Enable TLS in the app

Set `USE_TLS = true` in `DevelopedServerConfig.java`.

### 5. Deploy

```bash
docker compose up -d
```

## Log Scrubbing

The backend runs `LogScrubber` via `logback-spring.xml` which redacts:
- IPv4 addresses → `[ip]`
- Email addresses → `[email]`
- Bearer tokens → `Bearer [redacted]`

## Rate Limiting

Login attempts are rate-limited to 5 per 5 minutes per IP+email combination via Redis (`LoginAttemptLimiter`).

## Admin Approval

The system enforces a default-deny approval gate:
- **App**: `MasterIntegration.checkAdminApproval()` reads SharedPreferences (default false)
- **Server**: `AuthController.login()` returns 403 if user status is not APPROVED
- **Server-backed verification**: `MasterIntegration.verifyApprovalFromServer()` makes a real API call to `/api/auth/status` and updates the local flag

## Audit Log

All security-relevant events are captured:
- User approvals, rejections, bans
- Kill-switch activations
- User promotions
- Password changes
- Login failures (via rate limiter)

Events are stored in MongoDB and viewable via the admin dashboard or `/api/admin/audit`.

## User Blocking

Users can block other users:
- **Block**: POST `/api/blocks` with `blockeeId`
- **Unblock**: DELETE `/api/blocks/{blockeeId}`
- **Check**: GET `/api/blocks/check/{userId}`
- Blocked users' messages are silently dropped by the delivery engine
