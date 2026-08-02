# خطة جعل مشروع RED_Ultimate مستقلاً بالكامل (Self-Contained & Rebranded)

## ملخص المشروع
تحويل مشروع Signal-Android fork إلى مشروع مستقل تماماً باسم \RED\ (أو الاسم المختار) مع:
- حزمة خاصة: \com.red.*\ بدلاً من \org.thoughtcrime.securesms\ و \org.signal.*\
- بنية تحتية خاصة 100% (لا اعتماد على Signal)
- مفاتيح/خدمات API خاصة
- بروتوكول مشفر خاص أو fork من libsignal

---

## المرحلة 1: تغيير الحزم والتسمية (Package Renaming & Rebranding)

### 1.1 تغيير Package Name الرئيسي
| من | إلى |
|----|-----|
| \org.thoughtcrime.securesms\ | \com.red.app\ |
| \org.signal.*\ (core, libs) | \com.red.core.*\, \com.red.lib.*\ |
| \org.whispersystems.signalservice\ | \com.red.service\ |

**الملفات المؤثرة:**
- \pp/build.gradle.kts\: \
amespace = \"com.red.app\"\
- \settings.gradle.kts\: إعادة تسمية جميع المشاريع
- جميع ملفات \AndroidManifest.xml\
- جميع ملفات Kotlin/Java (آلاف الملفات)
- \wire-handler\ package: \org.signal.wire\ → \com.red.wire\

### 1.2 تغيير Application ID
\\\kotlin
// app/build.gradle.kts
defaultConfig {
    applicationId = \"com.red.app\"
    // إزالة applicationIdSuffix للـ staging أو تغييره
}
\\\

### 1.3 تغيير الأسماء المعروضة
- App name: \"Signal\" → \"RED Messenger\" (أو الاسم المختار)
- Package name في Play Store: \com.red.app\
- جميع strings في \es/values/strings.xml\

---

## المرحلة 2: استبدال البنية التحتية للخوادم (Server Infrastructure)

### 2.1 تحديث BuildConfig URLs
**app/build.gradle.kts** - السطور 265-288, 489-500:

| الحالي (red.local) | الجديد (مجالك) |
|---------------------|----------------|
| \https://chat.red.local\ | \https://api.yourdomain.com\ |
| \https://storage.red.local\ | \https://storage.yourdomain.com\ |
| \https://cdn.red.local\ | \https://cdn.yourdomain.com\ |
| \https://cdn2.red.local\ | \https://cdn2.yourdomain.com\ |
| \https://cdn3.red.local\ | \https://cdn3.yourdomain.com\ |
| \https://cdsi.red.local\ | \https://cdsi.yourdomain.com\ |
| \https://svr2.red.local\ | \https://svr2.yourdomain.com\ |
| \https://sfu.voip.red.local\ | \https://sfu.yourdomain.com\ |
| \uptime.red.local\ | \https://status.yourdomain.com\ |
| \contentproxy.red.local\ | \https://proxy.yourdomain.com\ |

### 2.2 IPs الثابتة (Static IPs)
- \static-ips.properties\ → تحديث بـ IPs خوادمك
- أو إزالة الاعتماد على static IPs واستخدام DNS فقط

### 2.3 Environment Configuration
- \local.properties\ → إضافة إعدادات مجالك
- إزالة \quickstart.credentials.dir\ و \enchmark.backup.file\ أو توجيهها لبيئتك

---

## المرحلة 3: استبدال التبعيات الخارجية (Dependency Replacement)

### 3.1 libsignal (الأهم والأصعب)

**الخيارات:**
| الخيار | الوصف | الجهد |
|---------|---------|-------|
| **A: Fork libsignal** | نسخ repo libsignal، تغيير package إلى \com.red.libsignal\، نشر لمستودعك | عالي جداً |
| **B: استخدام libsignal كما هو** | الاحتفاظ بـ \org.signal.libsignal\ لكن بناء من المصدر | متوسط |
| **C: استبدال ببروتوكول خاص** | تنفيذ Signal Protocol من الصفر أو استخدام مكتبة أخرى | عالي جداً |

**التوصية:** الخيار A (Fork) للاستقلالية الكاملة

**التبديلات المطلوبة:**
\\\kotlin
// settings.gradle.kts - إزالة:
maven { url = uri(\"https://raw.githubusercontent.com/signalapp/maven/master/sqlcipher/release/\") }
maven { url = uri(\"https://raw.githubusercontent.com/signalapp/maven/master/aesgcmprovider/release/\") }

// إضافة مستودعك الخاص:
maven { url = uri(\"https://maven.yourdomain.com/release/\") }

// libsignal-client → com.red.libsignal:libsignal-client
// libsignal-android → com.red.libsignal:libsignal-android
\\\

### 3.2 SQLCipher
- الحالي: \org.signal:android-database-sqlcipher\ من maven.signal.org
- البديل: \
et.zetetic:android-database-sqlcipher:4.5.3\ (الرسمي من Zetetic) أو fork خاص

### 3.3 AES-GCM Provider
- الحالي: \org.signal:aesgcmprovider\ 
- البديل: تنفيذ خاص باستخدام \javax.crypto\ أو BouncyCastle

### 3.4 RingRTC (WebRTC)
- الحالي: \org.signal:ringrtc\ 
- **بديل:** استخدام \media-sfu\ الموجود (Mediasoup) + \libwebrtc\ الرسمي من Google
- إزالة RingRTC بالكامل، استخدام WebRTC Android SDK مباشرة

### 3.5 MobileCoin (المدفوعات)
- الحالي: \com.mobilecoin:mobilecoin\ 
- **الخيارات:**
  - إزالة نظام المدفوعات بالكامل
  - استبدال بنظام مدفوعات خاص (Stripe direct, Crypto الخاص بك, إلخ)
  - Fork MobileCoin مع تغيير الحزمة

### 3.6 Firebase/FCM
**الإزالة الكاملة:**
- \libs.firebase.messaging\ → إزالة
- \FirebaseTestLabTask\ → إزالة
- \google-services.json\ → عدم الحاجة
- **البديل:** UnifiedPush (مفتوح المصدر) أو Push server خاص

### 3.7 خدمات API خارجية

| الخدمة | الحالي | البديل |
|---------|--------|---------|
| **Giphy** | \3o6ZsYH6U6Eri53TXy\ | مفتاح Giphy خاص أو Tenor أو إزالة GIFs |
| **Stripe** | \pk_live_...\ / \pk_test_...\ | مفاتيح Stripe خاصة أو نظام دفع آخر |
| **Google Maps** | \AIzaSyCSx9xea86GwDKGznCAULE9Y5a8b-TfN9U\ | مفتاح Maps خاص أو OpenStreetMap/MapLibre |
| **reCAPTCHA** | \signalcaptchas.org\ | hCaptcha خاص، Turnstile (Cloudflare)، أو CAPTCHA ذاتي |
| **Google Play Services** | Maps, Auth, Sign-in | إبقاء Maps فقط (بمفتاح خاص)، الباقي اختياري |

---

## المرحلة 4: Protobuf Schemas (استبدال Signal Protos)

### 4.1 protobuf الحالي في \lib/libsignal-service/src/main/protowire/\
\\\
CallQualitySurvey.proto
CDSI.proto
DecryptedGroups.proto
Groups.proto
InternalSerialization.proto
MessageProcessing.proto
Provisioning.proto
RegistrationProvisioning.proto
ResumableUploads.proto
SignalService.proto (28KB - الأساسي)
SignalServiceLegacy.proto
StickerResources.proto
StorageService.proto
SVR2.proto
\\\

### 4.2 الخطة
1. **نسخ جميع proto files** إلى \shared-proto/\ أو module جديد \ed-protos\
2. **تغيير package** من \org.signal.*\ إلى \com.red.proto.*\
3. **تعديل الحقول** لتناسب بروتوكولك (إزالة حقول Signal-specific)
4. **إعادة توليد الكود** باستخدام Wire compiler مع \wire-handler\ المحدث
5. **تحديث جميع المستهلكين** (\libsignal-service\, \lib:network\, \lib:archive\, \pp\)

---

## المرحلة 5: Push Notifications (UnifiedPush / Self-hosted)

### 5.1 إزالة FCM
\\\kotlin
// app/build.gradle.kts - إزالة:
implementation(libs.firebase.messaging) { ... }

// Manifest - إزالة:
<service android:name=\"com.google.firebase.messaging.FirebaseMessagingService\">
\\\

### 5.2 تطبيق UnifiedPush
- إضافة \UnifiedPush\ library
- تنفيذ \PushReceiver\ خاص
- إعداد Push Server (يمكن استخدام \
tfy.sh\ self-hosted أو \UnifiedPush Server\)

---

## المرحلة 6: ProGuard & Build Configuration

### 6.1 ملفات ProGuard للتنظيف
**إزالة/تعديل:**
\\\
proguard/proguard-firebase-messaging.pro     → حذف
proguard/proguard-google-play-services.pro   → مراجعة (الإبقاء على Maps فقط)
proguard/proguard-mobilecoin.pro             → حذف أو استبدال
proguard/proguard-sqlite.pro                 → مراجعة
proguard/proguard-retrolambda.pro            → حذف (لم يعد مستخدماً)
\\\

### 6.2 BuildConfig Fields للتنظيف
**إزالة/استبدال:**
- \LIBSIGNAL_NET_ENV\ → \RED_NET_ENV\
- \LIBSIGNAL_LOG_LEVEL\ → \RED_LOG_LEVEL\
- \SVR2_MRENCLAVE_LEGACY\, \SVR2_MRENCLAVE\ → مفاتيح SVR2 الخاصة
- \UNIDENTIFIED_SENDER_TRUST_ROOTS\ → جذور الثقة الخاصة
- \ZKGROUP_SERVER_PUBLIC_PARAMS\ → معاملات ZKGroup الخاصة
- \GENERIC_SERVER_PUBLIC_PARAMS\ → معاملات الخادم العامة
- \BACKUP_SERVER_PUBLIC_PARAMS\ → معاملات خادم النسخ الاحتياطي
- \BADGE_STATIC_ROOT\ → جذر الشارات الخاص
- \SIGNAL_AGENT\ → \RED_AGENT\

---

## المرحلة 7: تحديث Backend Services

### 7.1 backend-server (Spring Boot)
- تحديث \pplication.properties\ / \pplication.yml\ بمجالك
- تغيير package من \com.red.server\ إلى \com.red.backend\
- مراجعة جميع endpoints لاستخدام بروتوكولك الجديد

### 7.2 media-sfu (Mediasoup)
-Already مستقل، مراجعة signaling protocol ليتوافق مع app

### 7.3 pstn-asterisk
- مراجعة \extensions.conf\ و \manager.conf\ لبيئتك
- إضافة TLS/SRTP certificates

### 7.4 Docker Compose
- تحديث جميع domains في \docker-compose.yml\
- إضافة SSL certificates (Let's Encrypt أو الخاصة)
- تحديث passwords/secrets في \.env\ file

---

## المرحلة 8: اختبارات وقواعد Lint

### 8.1 lintchecks module
- تحديث جميع detectors لاستخدام \com.red.*\ packages
- مراجعة \EXEMPTED_CLASSES\ للفئات الجديدة

### 8.2 اختبارات Microbenchmark
- تحديث package من \org.signal.microbenchmark\ إلى \com.red.benchmark\

---

## المرحلة 9: المستندات والعلامة التجارية

### 9.1 README.md
- إعادة كتابة بالكامل لـ RED
- إزالة إشارات Signal

### 9.2 LICENSE
- مراجعة: المشروع مرخص AGPLv3 (مثل Signal)
- قرار: الإبقاء على AGPLv3 أم تغيير الترخيص؟

### 9.3 CONTRIBUTING.md, DEPLOY.md
- تحديث للمساهمة في RED

---

## المرحلة 10: CI/CD و Keystore

### 10.1 Keystore
- إنشاء \keystore.release.properties\ جديد
- إزالة \keystore.debug.properties\ القديم أو تحديثه

### 10.2 Build Signing
\\\kotlin
// app/build.gradle.kts
signingConfigs {
    release {
        storeFile = file(\"keystore.release.jks\")
        // ...
    }
}
\\\

### 10.3 GitHub Actions / CI
- تحديث workflows لبناء ونشر لمستودعك
- إضافة secret scanning لـ API keys

---

## تقديرات أولوية التبعيات (Dependency Graph)

\\\
┌─────────────────────────────────────────────────────────────┐
│                    CRITICAL PATH                            │
├─────────────────────────────────────────────────────────────┤
│  1. Package Rename (أساس كل شيء)                            │
│       ↓                                                     │
│  2. libsignal Fork + Publish إلى Maven خاص                  │
│       ↓                                                     │
│  3. Protobuf Regeneration (يعتمد على 1, 2)                 │
│       ↓                                                     │
│  4. App BuildConfig + URLs (يمكن بالتوازي مع 2, 3)          │
│       ↓                                                     │
│  5. Dependency Replacement (SQLCipher, RingRTC, MobileCoin) │
│       ↓                                                     │
│  6. Push System (FCM → UnifiedPush)                        │
│       ↓                                                     │
│  7. Backend Services Alignment                             │
│       ↓                                                     │
│  8. Testing & Lint                                          │
│       ↓                                                     │
│  9. Release Keystore + CI/CD                                │
└─────────────────────────────────────────────────────────────┘
\\\

---

## المهام القابلة للتوازي (Parallel Tracks)

| المسار | المهام |
|--------|--------|
| **Track A: Core** | Package rename, libsignal fork, protobufs |
| **Track B: App Config** | BuildConfig URLs, API keys, ProGuard, Manifest |
| **Track C: Backend** | Docker Compose, SSL, Domain config, Backend code |
| **Track D: Push** | UnifiedPush integration, Push server setup |
| **Track E: Payments** | MobileCoin removal/replacement, Stripe integration |
| **Track F: QA** | Lint updates, Tests, Benchmarks |

---

## قرارات مطلوبة منك (Decisions Needed)

قبل البدء، أحتاج قرارك في:

1. **الاسم النهائي للحزمة:** \com.red.*\ أم \org.red.*\ أم \com.yourcompany.*\؟
2. **المجال (Domain):** ما هو المجال الرسمي؟ (مثلاً \ed-messenger.com\)
3. **libsignal:** Fork كامل أم بناء من المصدر مع package أصلي؟
4. **MobileCoin/المدفوعات:** إزالة بالكامل أم استبدال بنظام آخر؟
5. **Push:** UnifiedPush (مفتوح) أم Push server خاص؟
6. **الترخيص:** الإبقاء على AGPLv3 أم تغيير؟
7. **Google Maps:** الإبقاء (بمفتاح خاص) أم استبدال بـ MapLibre/OpenStreetMap؟
8. **Giphy/Stickers:** الإبقاء (بمفتاح خاص) أم إزالة أم بديل؟

---

## مخاطر رئيسية (Risks)

| المخاطر | التخفيف |
|----------|---------|
| **كسر البروتوكول مع مستخدمين موجودين** | Version negotiation في البروتوكول، migration path |
| **فشل بناء libsignal fork** | بناء واختبار fork منفصل أولاً قبل الدمج |
| **أخطاء Wire generation** | اختبار protobuf compilation في isolation |
| **فقدان بيانات المستخدمين** | Migration scripts للـ database، backup strategy |
| **مشاكل ProGuard/R8** | اختبار builds release على أجهزة حقيقية مبكراً |

---

## خطة التحقق (Validation Plan)

1. **Unit Tests:** جميع الاختبارات تمر بعد كل تغيير حزمة
2. **Integration Tests:** الاتصال بالـ backend الخاص
3. **E2E Tests:** إرسال/استقبال رسائل، مكالمات، مزامنة
4. **Benchmark:** مقارنة الأداء مع النسخة الأصلية
5. **Security Audit:** مراجعة التشفير، مفاتيح، بروتوكول
6. **Play Store Internal Test:** بناء ونشر نسخة داخلية للاختبار

---

## الخطوات التالية

بمجرد قرارك على الأسئلة أعلاه، سأقوم بإنشاء:
1. **خطة مهام تفصيلية** (Task breakdown مع ملفات محددة)
2. **سكريبتات آلية** للـ package rename (إذا أمكن)
3. **دليل Fork libsignal** خطوة بخطوة
4. **قائمة تحقق (Checklist)** لكل مرحلة

---

*تاريخ الإنشاء: 2026-08-01*
*الحالة: في انتظار القرارات*
