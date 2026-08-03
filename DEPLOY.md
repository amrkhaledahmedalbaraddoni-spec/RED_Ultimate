# Deployment Guide — RED Sovereign Edition

A fully on-prem, containerised stack: backend (Spring Boot), media SFU (Mediasoup), PSTN gateway (Asterisk), PostgreSQL, MongoDB, Redis, MinIO, and a React admin dashboard behind Nginx.

## 1. Prerequisites
- Docker + Docker Compose (`docker compose` v2).
- A physical Dumin/GSM device on the LAN for System B (optional; the gateway reports OFFLINE until present).
- No internet/cloud is required at runtime.

## 2. Configure (optional)
Copy `.env.example` (or set env vars) to override secrets — at minimum rotate the JWT secret and the bootstrap admin password:
```bash
export RED_JWT_SECRET="$(openssl rand -hex 32)"
export RED_ADMIN_EMAIL=admin@red.local
export RED_ADMIN_PASSWORD='a-strong-password'
export PUBLIC_IP=192.168.1.50   # reachable IP for WebRTC ICE candidates
export TURN_SECRET="$(openssl rand -hex 16)"
```

## 3. Launch the whole stack
```bash
./build-and-run.sh
# or:  docker compose up -d --build
```

## 4. Entry points
| Service | URL |
|---|---|
| Reverse proxy / Admin UI | http://localhost |
| REST API | http://localhost/api |
| Chat WebSocket | ws://localhost/ws/chat?token=\<jwt\> |
| Media SFU (signaling) | http://localhost:4000 (UDP 40000–40100) |
| MinIO console | http://localhost:9001 |
| Asterisk AMI | localhost:5038 |

## 5. Build & distribute the Android app
The Android client lives in `app-android/` (a standalone Gradle project):
```bash
cd app-android
./gradlew :app:assembleRelease   # or assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
Configure the server endpoint by editing the `BASE_URL` in `app-android/app/src/main/java/com/red/core/di/NetworkModule.kt` (default `http://192.168.1.50:8080/`).

## 6. Admin workflow
- On first boot, an admin account is auto-created from `RED_ADMIN_EMAIL` / `RED_ADMIN_PASSWORD` (only if none exists).
- Log in via the admin UI or `POST /api/auth/login`, then approve pending users at `POST /api/admin/users/{id}/approve`.
- Use the Kill Switch at `POST /api/admin/security/kill-switch/{userId}` to revoke a lost device.

## 7. Building the Signal integration (optional)
The main Signal-Android app (the `:app` module) is still buildable with its own toolchain:
```bash
./gradlew :Signal-Android:assembleDebug
```
The RED integration lives under `app/src/main/java/org/thoughtcrime/securesms/developed/` and now compiles cleanly.

**Rights Reserved to RED © 2026**
