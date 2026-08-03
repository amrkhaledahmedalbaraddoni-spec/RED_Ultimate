# RED Ultimate — Deployment Guide

## Prerequisites

- Docker 24+ and Docker Compose v2+
- 4 GB RAM minimum (8 GB recommended for production)
- 20 GB disk space minimum
- Network access between all containers

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/your-org/RED_Ultimate.git
cd RED_Ultimate

# 2. Configure environment
cp .env.example .env
# Edit .env with your settings (JWT secret, admin credentials, etc.)

# 3. Generate TLS certificates (optional for LAN, required for production)
./scripts/generate-local-cert.sh 192.168.1.50

# 4. Start all services
docker compose up -d --build

# 5. Verify all services are healthy
docker compose ps
```

## Service Architecture

| Service | Port | Description |
|---------|------|-------------|
| Backend (Spring Boot) | 8080 (internal) | API, WebSocket, auth |
| Nginx | 80/443 | Reverse proxy |
| PostgreSQL | 5432 (internal) | Users, metadata |
| MongoDB | 27017 (internal) | Messages, stories, audit |
| Redis | 6379 (internal) | Dedup, sequencing, presence |
| MinIO | 9000/9001 | Media storage |
| Media SFU | 4000 | WebRTC/4K VoIP |
| Asterisk | 5060/8088 | PSTN/Dumin gateway |
| Admin Dashboard | 80 (via nginx) | Admin panel |

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `RED_JWT_SECRET` | (change-me) | JWT signing secret (≥32 bytes) |
| `RED_ADMIN_EMAIL` | admin@red.local | Bootstrap admin email |
| `RED_ADMIN_PASSWORD` | changeme123 | Bootstrap admin password |
| `PUBLIC_IP` | 127.0.0.1 | Public IP for SFU |
| `TURN_SECRET` | redturnsecret | COTURN secret |

### TLS Configuration

For production deployments, enable TLS:

1. Generate certificates: `./scripts/generate-local-cert.sh <YOUR_IP>`
2. Update `docker-compose.yml` to use `nginx.tls.conf`
3. Set `USE_TLS = true` in `DevelopedServerConfig.java`
4. Copy CA cert to Android app assets

### Redis Configuration

Redis is configured with:
- 256 MB max memory
- LRU eviction policy
- Used for: dedup (24h TTL), sequencing, presence (60s TTL), rate limiting (5min TTL), notifications (7d TTL)

## Building the Android App

The RED backend is local-first. Dumin/PSTN is disabled by default and does not need to be
running. Start the backend locally with Docker, then point the Android build at it.

For an Android Emulator, the host machine is `10.0.2.2`:

### On Linux/macOS
```bash
docker compose up -d --build
curl http://127.0.0.1:8080/actuator/health

# The Signal and RED features are now one Android application module.
./gradlew :Signal-Android:assemblePlayProdDebug
```

For a physical device, replace `10.0.2.2` with the computer's LAN IP and make sure port 8080 is
reachable from the device:

```bash
./gradlew \
  -Pred.server.url=http://192.168.1.50:8080 \
  -Pred.dumin.enabled=false \
  :Signal-Android:assemblePlayProdDebug
```

Enable Dumin only when a local gateway actually exists:

```bash
./gradlew \
  -Pred.server.url=http://192.168.1.50:8080 \
  -Pred.dumin.enabled=true \
  -Pred.dumin.ip=192.168.1.100 \
  :Signal-Android:assemblePlayProdDebug
```

### On Windows (Arabic locale)
```batch
build-windows.bat
```

This script:
1. Sets `JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8` to prevent Arabic-Indic digits and generated-source encoding corruption
2. Runs `gradlew clean` to remove stale generated code
3. Runs `gradlew assemblePlayProdDebug`

## Monitoring

### Health Checks
All services have health checks configured in `docker-compose.yml`.

### Admin Dashboard
Access the admin dashboard at `http://<YOUR_IP>/`

Features:
- **Dashboard**: Real-time system stats (messages, users, stories)
- **Approvals**: Manage user registration requests
- **Users**: Full user management (CRUD, promote, ban)
- **Stories**: View and moderate stories
- **Diagnostics**: System health checks
- **Audit Log**: Security event viewer
- **Dumin/PSTN**: Hardware gateway monitoring

### API Endpoints
- Health: `GET /actuator/health`
- Stats: `GET /api/admin/monitor/stats`
- System health: `GET /api/admin/monitor/health`

## Backup Strategy

### PostgreSQL
```bash
docker exec red-db-sql pg_dump -U red red_sovereign > backup_$(date +%Y%m%d).sql
```

### MongoDB
```bash
docker exec red-db-nosql mongodump --db red_messages --out /tmp/backup
docker cp red-db-nosql:/tmp/backup ./mongodb_backup_$(date +%Y%m%d)
```

### Redis
Redis data is ephemeral (cache only). No backup needed.

### MinIO
```bash
docker exec red-storage mc mirror local/red-media /tmp/backup
```

## Scaling

### Horizontal Scaling
The backend can be scaled horizontally. Key considerations:
- **WebSocket**: Session affinity via nginx (IP hash)
- **Redis**: Shared state across instances (presence, dedup, sequences)
- **MongoDB**: Shared message store
- **PostgreSQL**: Shared user store

### Vertical Scaling
- Backend: Increase `JAVA_OPTS` in docker-compose.yml (default: `-Xms256m -Xmx512m`)
- Redis: Increase `maxmemory` in docker-compose.yml command
- PostgreSQL: Increase `shared_buffers` and `work_mem`

## Troubleshooting

### Common Issues

1. **Arabic-Indic digits in resource directories** (Windows)
   - Use `build-windows.bat` or set `JAVA_TOOL_OPTIONS`

2. **DeviceName.kt Wire generation errors**
   - Run `gradlew clean` before building

3. **Dependency verification failure**
   - Check `gradle/verification-metadata.xml` for aapt2 version

4. **WebSocket connection fails**
   - Check JWT token in handshake query parameter
   - Verify nginx WebSocket proxy configuration

5. **Messages not delivered**
   - Check Redis connectivity
   - Verify MongoDB connection
   - Check WebSocket session in PresenceService

### Logs

```bash
# Backend logs
docker compose logs -f backend

# All service logs
docker compose logs -f

# Specific service
docker compose logs -f cache-redis
```
