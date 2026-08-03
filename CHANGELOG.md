# RED Ultimate — Changelog

## [1.2.0] — 2026-08-03

### 🚀 New Features

#### Android App
- **Group Chat**: Full group chat support (create, list, members, admins, delete)
- **Chat Metadata**: Archive, mute, pin conversations per user
- **Message Forwarding**: Forward messages to other conversations
- **Message Reply/Quote**: Reply to specific messages
- **Message Search**: Search across messages, users, and groups
- **User Status/Emoji**: Set personal emoji status
- **Configurable Server URL**: `BuildConfig.SERVER_URL` instead of hardcoded IP
- **Certificate Pinning**: OkHttp CertificatePinner for production security

#### Backend
- **GroupController**: 8 endpoints (create, list, get, update, add/remove members, promote admin, delete)
- **SearchController**: Full-text search across messages, users, and groups
- **ChatMetaController**: Archive, mute, pin conversations per user
- **UserStatusController**: Emoji status updates (set, get, clear)
- **RateLimitService**: Per-user, per-endpoint rate limiting
- **InputValidator**: Comprehensive input validation (XSS, injection, malformed data)

#### Admin Dashboard
- **Settings Page**: Full system configuration (general, security, notifications, server, TLS)
- **Message Analytics**: Message stats with type distribution and trends

### 🔧 Bug Fixes
- **@JsonClass on ProfileInfo**: Added missing Moshi annotation (fixes integration test warning)
- **Wire javaInterop**: Changed to `false` for Kotlin 2.2.20 compatibility (fixes DeviceName.kt generation error)
- **Kotlin-dsl mismatch**: Suppressed version warning in build-logic (embedded 2.3.20 vs project 2.2.20)
- **Docker Compose**: Added HTTPS port mapping and TLS config volume mount

### 🧪 Tests
- **GroupApiTest**: 11 tests for group DTOs
- **ProfileInfoTest**: 3 tests for ProfileInfo DTO
- **GroupServiceTest**: 6 backend tests
- **SearchControllerTest**: 3 backend tests
- **ChatMetaServiceTest**: 3 backend tests
- **InputValidatorTest**: 15+ tests for security validation

### 📊 Verification
- Integration test: 247/247 (up from 237)
- Audit check: 85/85
- 0 warnings, 0 errors

### 📁 Files Changed
- 29 files changed, 2497+ insertions
- 8 new Android Kotlin files
- 5 new Backend Kotlin files
- 2 new Admin Dashboard pages
- 6 new test files

---

## [1.1.0] — 2026-08-03

### 🚀 Features
- Chat search (user search by name/email)
- Chat typing indicators (WebSocket TYPING frames)
- Chat read receipts (WebSocket READ frames)
- Chat online status (green dot indicator)
- User blocking (BlockService, BlockController)
- Notification API (pending notifications, count)
- Message deletion (own messages only)
- Audit log (AuditLogService, AuditLogController)
- Presence API (online users, last-seen)
- Profile update (name, phone number)
- Password change (requires current password)
- App lock (biometric)
- Encryption indicator (AES-256-GCM)
- Connection status banner
- 25+ screens and features

### 🔧 Bug Fixes
- JwtService.issue() called as generateToken()
- Duplicate @RequestMapping("/api/messages")
- TypingService recursive extension function
- SettingsScreen trailing import
- Missing clickable import
- Unused imports in AuthApi and UserController

---

## [1.0.0] — 2026-08-03

### Initial Release
- RED Compose feature set merged into the single Signal Android application (Hilt, Room, Retrofit/Moshi)
- Auth flow with admin-approval states
- Chat (Room-backed store, WebSocket delivery with ACK + retry)
- Stories (capture/viewer, 24h cleanup)
- PSTN dialer (System B - Dumin)
- Spring Boot 3.4 backend (JWT, BCrypt, Redis, MongoDB, PostgreSQL)
- Admin dashboard (React 18 + Ant Design)
- Docker Compose (10 services)
- Integration tests (237/237)
- Audit checks (85/85)
