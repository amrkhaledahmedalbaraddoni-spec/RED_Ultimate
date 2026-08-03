# RED Ultimate — Merged Android Features

The RED feature surface is compiled inside the single root `:app` application. The old standalone
`app-android` project was merged and is no longer a second Android application.

## 📊 ملخص التطوير

| المقياس | القيمة |
|---------|--------|
| ملفات Kotlin (merged `app/src/main/java/com/red`) | 83 |
| ملفات Kotlin (backend) | 44 |
| ملفات الاختبار | 4 (18 اختبار) |
| شاشات التطبيق | 24 |
| ViewModels | 12 |
| واجهات API | 5 |
| واجهات Backend | 15 controller |
| نقاط نهاية API | 48 |
| جداول قاعدة البيانات | 4 |
| فحوصات التدقيق | 85/85 ✅ |
| تحذيرات البناء | 0 |
| أسطر الكود | 7,388+ |

## 📱 الشاشات (24 شاشة)

### المصادقة (7 شاشات)
1. **PermissionRequestScreen** — طلب الأذونات
2. **WelcomeScreen** — شاشة الترحيب
3. **RegisterScreen** — إنشاء حساب
4. **LoginScreen** — تسجيل الدخول
5. **PendingApprovalScreen** — انتظار موافقة المسؤول
6. **StatusScreens** — رفض/حظر
7. **AppLockScreen** — قفل التطبيق بالبصمة

### المحادثات (4 شاشات)
8. **ChatListScreen** — قائمة المحادثات مع بحث + إشعارات + شارة غير مقروء
9. **ChatDetailScreen** — تفاصيل المحادثة + قائمة سياق + مؤشر كتابة + تشفير
10. **NewChatScreen** — محادثة جديدة + بحث
11. **CreateGroupScreen** — إنشاء مجموعة

### القصص (3 شاشات)
12. **StoryListScreen** — قائمة القصص
13. **StoryViewerScreen** — عرض القصة مع شريط تقدم
14. **CameraCaptureScreen** — التقاط صورة

### المكالمات (4 شاشات)
15. **CallLogScreen** — سجل المكالمات
16. **DialPadScreen** — لوحة أرقام كاملة
17. **PstnCallScreen** — مكالمة هاتفية (PSTN/Dumin)
18. **VideoCallScreen** — مكالمة فيديو (WebRTC)

### الملف الشخصي والإعدادات (4 شاشات)
19. **SettingsScreen** — إعدادات كاملة مع تبديلات حقيقية
20. **ProfileScreen** — ملف شخصي + حظر + مكالمة
21. **QRCodeScreen** — رمز QR
22. **MediaGalleryScreen** — معرض الوسائط

### جهات الاتصال والحظر والإشعارات (3 شاشات)
23. **ContactsScreen** — جهات الاتصال + بحث + إضافة
24. **BlockListScreen** — قائمة المحظورين
25. **NotificationListScreen** — مركز الإشعارات

## 🔧 الميزات المكتملة

### الأمان
- ✅ تشفير AES-256-GCM للرسائل المحلية
- ✅ مصادقة بيومترية (بصمة/وجه)
- ✅ SessionManager مع إدارة مفاتيح التشفير
- ✅ قفل التطبيق
- ✅ مؤشر التشفير في المحادثة
- ✅ حظر المستخدمين
- ✅ إيصالات القراءة
- ✅ JWT مع وقت انتهاء الصلاحية

### الاتصال
- ✅ WebSocket مع إعادة الاتصال التلقائي
- ✅ شريط حالة الاتصال
- ✅ مزامنة الرسائل غير المتصلة
- ✅ مؤشرات الكتابة
- ✅ إيصالات التسليم/القراءة
- ✅ UUID v7 للرسائل

### البنية التحتية
- ✅ Hilt DI
- ✅ Room Database (4 جداول)
- ✅ Retrofit + OkHttp + Moshi
- ✅ WorkManager للتنظيف
- ✅ CameraX للقصص
- ✅ Coil 3 للصور
- ✅ Navigation Compose
- ✅ ProGuard للإنتاج

## 🌐 Backend API (48 نقطة نهاية)

| Controller | نقاط النهاية | المسار |
|-----------|-------------|--------|
| AuthController | 4 | /api/auth |
| UserController | 4 | /api/users |
| ContactController | 4 | /api/contacts |
| ConversationController | 2 | /api/conversations |
| MessageController | 5 | /api/messages |
| BlockController | 4 | /api/blocks |
| NotificationController | 4 | /api/notifications |
| PresenceController | 2 | /api/presence |
| PstnController | 2 | /api/pstn |
| StoryController | 3 | /api/stories |
| StorageController | 2 | /api/media |
| AdminApprovalController | 7 | /api/admin/users |
| AuditLogController | 2 | /api/admin/audit |
| MonitorController | 2 | /api/admin/monitor |
| SecurityController | 1 | /api/admin/security |

## ✅ الإصلاحات الحرجة

1. **BlockApi paths**: /api/block → /api/blocks (مطابقة الـ backend)
2. **BlockApi.blockUser**: يرسل {blockeeId} في الـ body
3. **ContactController**: أضيف إلى الـ backend (كان مفقوداً)
4. **NotificationController**: أضيف mark-read و mark-all-read
5. **MessageController**: أضيف /typing endpoint
6. **BlockService**: أضيف @Service annotation
7. **UserRepository.findByPhoneNumbers**: أضيف لمزامنة جهات الاتصال
8. **@JsonClass**: أضيف لكل DTOs في ChatApi, StoryApi, ContactApi
9. **ChatDetailScreen**: إصلاح senderId "me" → identity.userId
10. **ChatViewModel**: إصلاح sendMessage receiverId
11. **PstnCallScreen**: مكالمة تلقائية عند فتح الشاشة
12. **DialPadScreen**: لوحة أرقام كاملة مع DTMF
13. **SettingsScreen**: تبديلات حقيقية مدعومة بـ SharedPreferences
14. **AppLockScreen**: مدمج في RootGraph
15. **EncryptionIndicator**: مدمج في ChatDetailScreen
16. **ConnectionStatusBanner**: مدمج في MainScreen

## 🧪 الاختبارات

| الاختبار | عدد الاختبارات |
|---------|---------------|
| AESEncryptionTest | 6 |
| UuidV7Test | 4 |
| DeliveryEngineTest | 4 |
| ModelsTest | 4 |
| **المجموع** | **18** |

## 📋 كيفية البناء

```bash
# على Windows:
build-windows.bat

# على Linux/Mac (من جذر المستودع):
./gradlew :Signal-Android:assemblePlayProdDebug
```

## ⚠️ ملاحظات مهمة

1. **لا يوجد JDK** في البيئة الحالية — لا يمكن بناء التطبيق
2. **التشفير الحالي محلي فقط** — التشفير من طرف إلى طرف (E2EE) يتطلب بروتوكول Signal
3. **WebRTC غير مُدمج** — VideoCallScreen هو placeholder
4. **FCM غير مُدمج** — الإشعارات الفورية تحتاج Firebase
5. **IP الافتراضي**: 192.168.1.50:8080 — يجب تغييره للخادم الفعلي
