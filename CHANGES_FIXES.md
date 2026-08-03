# RED Ultimate — Changes & Fixes Log

## v1.1.1 — Build Stabilization (2026-08-03)

Fixes that unblock `assemblePlayProdDebug` on a clean JDK 21 / Windows or Linux machine
(previously applied only locally and never committed):

- **Hilt 2.52 → 2.59.2** (`gradle/libs.versions.toml`): Hilt 2.52 fails on AGP 9.x with
  `Android BaseExtension not found` (dagger issue #4944). 2.59.2 is the confirmed fix.
- **Migrated annotation processing from kapt to KSP** (`app/build.gradle.kts`): AGP 9 ships
  with built-in Kotlin, so the KGP `org.jetbrains.kotlin.kapt` plugin is incompatible
  (`Cannot add extension with name 'kapt'`). Google's alternative `com.android.legacy-kapt`
  does not work with the Hilt Gradle plugin (dagger issue #4756), and KSP 2.3.x is the
  supported path for AGP 9 built-in Kotlin (ksp issue #2615). Hilt 2.59.2, androidx.hilt
  1.2.0 and Room 2.6.1 all support KSP, so `kapt(...)` dependencies and the `kapt {}` block
  were replaced with the KSP plugin (`com.google.devtools.ksp` 2.3.2) and `ksp(...)`.
- **Moved the Hilt application entry point to Kotlin** (new `com.red.RedHiltApplication`,
  manifest `android:name` updated): Hilt's KSP processor only processes Kotlin sources, so
  `@HiltAndroidApp`, the `@Inject` fields and `Configuration.Provider` were removed from the
  Java class `org.thoughtcrime.securesms.ApplicationContext`. `RedHiltApplication` extends
  `ApplicationContext`, keeping all Signal initialization in the same class hierarchy/APK.
- **Added `backend-server/gradle.properties`**: `backend-server` is an included (composite)
  build with its own Gradle properties; without this file it did not inherit the root
  `org.gradle.dependency.verification=lenient` setting and the whole root build failed
  during configuration with `Dependency verification failed for configuration 'classpath'`.
- **`build-windows.bat` / `verify-all.bat`**: also auto-detect Android Studio secondary
  installs (e.g. `C:\Program Files\Android\Android Studio2\jbr`) and validate `JAVA_HOME`.
- **QR code identity**: the QR screen now uses the authenticated RED user id/name
  (`SettingsViewModel.userId` + server profile) instead of the hardcoded
  `"current-user"` / `"Me"` placeholders.

Verification: `audit_check.py` 90/90, `integration_test.py` 446/446, `api_contract_test.py`
68/68, admin dashboard `npm ci && npm run build` successful; static import resolution over
the whole `com.red` / Signal-developed surface passes with no missing internal references.

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
