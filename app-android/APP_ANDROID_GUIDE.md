# RED Ultimate — Android App

## نظرة عامة
تطبيق RED Ultimate هو تطبيق اتصالات آمن وموحد يعمل على أندرويد. يوفر:
- **رسائل مشفرة** — محادثات فورية مع تشفير محلي AES-256-GCM
- **مكالمات PSTN** — مكالمات هاتفية حقيقية عبر بوابة Dumin/GSM
- **مكالمات فيديو** — (قيد التطوير) مكالمات فيديو عبر WebRTC
- **قصص** — مشاركة صور ومقاطع فيديو تختفي بعد 24 ساعة
- **جهات الاتصال** — إدارة جهات الاتصال والبحث عن أصدقاء
- **الإشعارات** — إشعارات فورية للرسائل والمكالمات والقصص
- **الحظر** — حظر المستخدمين غير المرغوب فيهم
- **الخصوصية** — إعدادات الخصوصية (آخر ظهور، إيصالات القراءة)
- **الأمان** — قفل التطبيق بالبصمة، تشفير محلي

## البنية التقنية

### التقنيات المستخدمة
| التقنية | الإصدار | الاستخدام |
|---------|---------|-----------|
| Kotlin | 2.1.0 | اللغة الرئيسية |
| Compose | BOM 2024.12.01 | واجهة المستخدم |
| Hilt | 2.52 | حقن التبعيات |
| Room | 2.6.1 | قاعدة البيانات المحلية |
| Retrofit | 2.11.0 | اتصال HTTP |
| OkHttp | 4.12.0 | WebSocket و HTTP |
| Moshi | 1.15.1 | JSON serialization |
| CameraX | 1.4.1 | التصوير |
| Coil 3 | 3.0.4 | تحميل الصور |
| WorkManager | 2.10.0 | المهام الخلفية |
| Biometric | 1.1.0 | المصادقة البيومترية |

### بنية المشروع
```
app-android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/strings.xml
│       │   ├── values/themes.xml
│       │   └── xml/network_security_config.xml
│       └── java/com/red/
│           ├── MainActivity.kt          # النشاط الرئيسي
│           ├── RedApplication.kt        # نقطة دخول التطبيق
│           ├── app/
│           │   └── AppNavigation.kt     # مسار المصادقة
│           ├── core/
│           │   ├── auth/TokenStore.kt    # تخزين الرمز المميز
│           │   ├── crypto/               # التشفير
│           │   │   ├── AESEncryption.kt  # AES-256-GCM
│           │   │   └── EncryptionIndicator.kt
│           │   ├── database/            # قاعدة البيانات
│           │   │   ├── RedDatabase.kt
│           │   │   ├── ContactEntity.kt
│           │   │   ├── ConversationEntity.kt
│           │   │   └── StoryEntity.kt
│           │   ├── delivery/            # نظام التوصيل
│           │   │   ├── DeliveryEngine.kt
│           │   │   ├── DevelopedWebSocketClient.kt
│           │   │   ├── DevelopedWebSocketClientImpl.kt
│           │   │   ├── Dtos.kt
│           │   │   ├── MessageDeliveryManager.kt
│           │   │   ├── MessageEntity.kt
│           │   │   ├── NotificationHelper.kt
│           │   │   ├── OfflineSyncService.kt
│           │   │   ├── UuidV7.kt
│           │   │   └── ConnectionStatusIndicator.kt
│           │   ├── di/                  # حقن التبعيات
│           │   │   ├── DatabaseModule.kt
│           │   │   └── NetworkModule.kt
│           │   ├── models/Models.kt     # نماذج البيانات
│           │   ├── security/            # الأمان
│           │   │   ├── BiometricHelper.kt
│           │   │   └── SessionManager.kt
│           │   ├── utils/DevelopedLogger.kt
│           │   └── workers/StoryCleanupWorker.kt
│           └── feature/                # الميزات
│               ├── auth/               # المصادقة
│               │   ├── AuthApi.kt
│               │   ├── AuthViewModel.kt
│               │   ├── LoginScreen.kt
│               │   ├── RegisterScreen.kt
│               │   ├── WelcomeScreen.kt
│               │   ├── PendingApprovalScreen.kt
│               │   ├── PermissionRequestScreen.kt
│               │   ├── StatusScreens.kt
│               │   └── AppLockScreen.kt
│               ├── block/              # الحظر
│               │   ├── BlockListScreen.kt
│               │   └── BlockListViewModel.kt
│               ├── calls/              # المكالمات
│               │   ├── CallLogScreen.kt
│               │   ├── CallLogViewModel.kt
│               │   └── VideoCallScreen.kt
│               ├── chat/               # المحادثات
│               │   ├── ChatApi.kt
│               │   ├── ChatDetailScreen.kt
│               │   ├── ChatListScreen.kt
│               │   ├── ChatListViewModel.kt
│               │   ├── ChatViewModel.kt
│               │   ├── ContactApi.kt
│               │   ├── CreateGroupScreen.kt
│               │   └── NewChatScreen.kt
│               ├── contacts/           # جهات الاتصال
│               │   ├── ContactsScreen.kt
│               │   └── ContactsViewModel.kt
│               ├── media/              # الوسائط
│               │   ├── MediaGalleryScreen.kt
│               │   └── MediaGalleryViewModel.kt
│               ├── notifications/      # الإشعارات
│               │   ├── NotificationListScreen.kt
│               │   └── NotificationListViewModel.kt
│               ├── profile/            # الملف الشخصي
│               │   ├── ProfileScreen.kt
│               │   ├── ProfileViewModel.kt
│               │   ├── QRCodeScreen.kt
│               │   ├── SettingsScreen.kt
│               │   └── SettingsViewModel.kt
│               ├── pstn/               # مكالمات PSTN
│               │   ├── DialPadScreen.kt
│               │   ├── DuminApi.kt
│               │   ├── PstnCallScreen.kt
│               │   ├── PstnModels.kt
│               │   └── PstnViewModel.kt
│               └── stories/            # القصص
│                   ├── CameraCaptureScreen.kt
│                   ├── StoryApi.kt
│                   ├── StoryListScreen.kt
│                   ├── StoryViewerScreen.kt
│                   └── StoryViewModel.kt
```

## شاشات التطبيق (22 شاشة)

| الشاشة | الملف | الوصف |
|--------|-------|-------|
| Root Graph | MainActivity.kt | التبديل بين المصادقة والشاشة الرئيسية |
| Permission Request | PermissionRequestScreen.kt | طلب الأذونات |
| Welcome | WelcomeScreen.kt | شاشة الترحيب |
| Register | RegisterScreen.kt | إنشاء حساب |
| Login | LoginScreen.kt | تسجيل الدخول |
| Pending Approval | PendingApprovalScreen.kt | انتظار موافقة المسؤول |
| Chat List | ChatListScreen.kt | قائمة المحادثات |
| Chat Detail | ChatDetailScreen.kt | تفاصيل المحادثة |
| New Chat | NewChatScreen.kt | محادثة جديدة |
| Create Group | CreateGroupScreen.kt | إنشاء مجموعة |
| Contacts | ContactsScreen.kt | جهات الاتصال |
| Stories | StoryListScreen.kt | القصص |
| Story Viewer | StoryViewerScreen.kt | عرض القصة |
| Camera Capture | CameraCaptureScreen.kt | التقاط صورة |
| Call Log | CallLogScreen.kt | سجل المكالمات |
| Video Call | VideoCallScreen.kt | مكالمة فيديو |
| Dial Pad | DialPadScreen.kt | لوحة الاتصال |
| PSTN Call | PstnCallScreen.kt | مكالمة هاتفية |
| Settings | SettingsScreen.kt | الإعدادات |
| Profile | ProfileScreen.kt | الملف الشخصي |
| Block List | BlockListScreen.kt | قائمة المحظورين |
| Notifications | NotificationListScreen.kt | الإشعارات |
| QR Code | QRCodeScreen.kt | رمز الاستجابة السريعة |
| Media Gallery | MediaGalleryScreen.kt | معرض الوسائط |
| App Lock | AppLockScreen.kt | قفل التطبيق |

## واجهات API (8 واجهات)

| الواجهة | نقاط النهاية | الوصف |
|---------|--------------|-------|
| AuthApi | 9 | المصادقة وإدارة الحساب |
| ChatApi | 11 | الرسائل والمحادثات والحضور |
| ContactApi | 4 | إدارة جهات الاتصال |
| BlockApi | 4 | حظر وإلغاء حظر المستخدمين |
| NotificationApi | 4 | إدارة الإشعارات |
| DuminApi | 4 | مكالمات PSTN |
| StoryApi | 3 | القصص والوسائط |

## التشفير
- **AES-256-GCM** — تشفير محلي للرسائل على الجهاز
- **UUID v7** — معرفات فريدة مرتبة زمنياً للرسائل
- **JWT** — رمز المصادقة المميز
- **البصمة** — قفل التطبيق بالبصمة

## كيفية البناء
```bash
# على Windows مع إعدادات اللغة العربية:
build-windows.bat

# على Linux/Mac:
./gradlew assemblePlayProdDebug
```

## المتطلبات
- Android SDK 35
- JDK 17
- Kotlin 2.1.0
- Min SDK 26 (Android 8.0)
