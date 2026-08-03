# RED Ultimate — Changes & Fixes Log

## v1.1.0 — Comprehensive Enhancement (2026-08-03)

### Backend Server

#### New Features
- **UserView data class**: Proper JSON-safe projection of UserEntity (never exposes password hash)
- **User search**: `GET /api/users/search?q=...` — search users by name or email
- **Profile update**: `PUT /api/users/me` — update name, phone number
- **Password change**: `POST /api/auth/change-password` — requires current password verification
- **Read receipts**: `ReadReceiptService` — WebSocket READ frames with sender notification
- **Typing indicators**: `ChatWebSocketHandler` — WebSocket TYPING frames forwarded to peers
- **User blocking**: `BlockService` + `BlockController` + `BlockDocument` — silent message drop
- **Message deletion**: `DELETE /api/messages/{messageId}` — own messages only
- **Audit log**: `AuditLogService` + `AuditLogDocument` + `AuditLogController` — append-only security events
- **Notification service**: `NotificationService` — offline message queue in Redis (7-day TTL)
- **Presence last-seen**: `UserEntity.lastSeenAt` updated on WebSocket connect/disconnect
- **User avatar**: `UserEntity.avatarUrl` field added

#### Improvements
- **ChatWebSocketHandler**: Refactored to handle TYPING, READ, and regular message frames
- **PresenceService**: Added `broadcast()` method and `onlineUserIds()` for system announcements
- **MonitorController**: Added `messages_1h`, `online_user_ids`, `ram_usage_percent`, `uptime_ms`, `audit_events_24h`
- **SecurityConfig**: All new endpoints properly secured
- **AdminApprovalController**: Audit logging for all approval/ban/promote actions
- **SecurityController**: Audit logging for kill-switch activations
- **Dockerfile**: Non-root user, JVM tuning for containers, configurable JAVA_OPTS

#### Tests
- `AuthControllerTest`: Password encoding, JWT generation/parsing, UserView mapping, UserEntity equality
- `SecurityTest`: LogScrubber redaction (IPs, emails, Bearer tokens, multiple redactions)
- `MessageServiceTest`: IncomingMessage, StoredMessage, AckStatus, MessageAck
- `BlockServiceTest`: BlockDocument fields
- `NotificationServiceTest`: PendingNotification, ReadReceipt

### App-Android

#### New Features
- **Call log screen**: Real PSTN call history with duration, direction (incoming/outgoing/missed), timestamps
- **CallLogViewModel**: Reactive call log from Room database
- **Settings screen**: Full settings with profile editing, password change, about dialog, logout
- **SettingsViewModel**: Profile loading, updating, password changing
- **User search in chat**: Search users by name/email to start new conversations
- **Typing indicators**: `ChatViewModel.isTyping` state, displayed in chat detail
- **Read receipts**: `ChatViewModel.markAsRead()` method
- **Offline sync service**: `OfflineSyncService` — fetches pending messages on reconnect
- **Notification helper**: `NotificationHelper` — creates notification channels and shows notifications
- **UserView model**: Matches backend UserView for API compatibility

#### Improvements
- **ChatListScreen**: Added search bar, user search results, empty state with guidance
- **ChatDetailScreen**: Added typing indicator, auto-scroll to bottom
- **StoryListScreen**: Added clickable stories, avatar circles, expiry badges
- **StoryViewerScreen**: Now navigable from story list
- **AuthApi**: Added `getMyProfile()`, `updateProfile()`, `changePassword()`
- **ChatApi**: Added `searchUsers()`, `getMessages()`, `getPendingMessages()`
- **Models**: Added `UserView` data class
- **RedApplication**: Creates notification channels on startup

### Admin Dashboard

#### New Features
- **Audit Log page**: Searchable, filterable security event viewer with color-coded action tags
- **User Management page**: Full CRUD with search, detail modal, approve/promote/ban actions
- **Story Management page**: View and moderate stories with expiry tracking and delete

#### Improvements
- **App.js**: Added routes for /users, /stories, /audit, /settings
- **LiveMonitor**: Added messages_1h stat, RAM progress bar, system status timeline, infrastructure overview
- **Navigation**: Added icons for User Management, Story Management, Audit Log, Settings

### Infrastructure

#### Improvements
- **docker-compose.yml**: Health checks for all services, Redis memory limits and LRU eviction, backend JVM memory limits
- **build-windows.bat**: Improved with step-by-step output and error messages
- **.env.example**: Comprehensive with all configuration options
- **infrastructure/setup-env.sh**: Auto-generates JWT secret, creates .env with sensible defaults
- **media-sfu/server.js**: Rate limiting (50 connections per IP), max payload limit
- **backend-server/Dockerfile**: Non-root user, configurable JAVA_OPTS

### Documentation

#### Improvements
- **ARCHITECTURE.md**: Comprehensive with API reference, data flow diagrams, all features documented
- **SECURITY.md**: Added user blocking, audit log, read receipts, typing indicators
- **DEPLOY.md**: Comprehensive with backup strategy, scaling, troubleshooting
- **MASTER_CHECKLIST.txt**: Updated with all new features
- **shared-proto/messages.proto**: Added ReadReceipt, TypingIndicator, PresenceUpdate, Story, UserProfile, BlockAction, PushNotification
- **audit_check.py**: Expanded from 46 to 81 checks

---

## v1.0.0 — Initial Release (2026-08-01)

### Fixed
- Critical Signal app build failures (DevelopedChatInitialization.java removal, REDInitialization.java)
- Backend rebuild (Spring Boot 3.4, JWT, BCrypt, Redis dedup, MongoDB, WebSocket)
- App-android build system (Hilt, Compose, Room, Retrofit)
- Windows build fixes (Arabic locale, aapt2 verification, clean build)
- Security hardening (TLS, admin approval, REDLocalTrustStore, LogScrubber, rate limiting)
