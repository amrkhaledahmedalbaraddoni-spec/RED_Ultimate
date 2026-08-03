# 📋 التقرير التقني الشامل لمشروع RED Ultimate

> **ملاحظة حالة:** هذا التقرير لقطة تاريخية قبل دمج تطبيق RED المستقل. تم لاحقًا نقل
> كود `app-android` إلى وحدة `app` الرئيسية، وأصبح مشروع Android ينتج تطبيقًا واحدًا فقط.
> البنود التي تصف `app-android` كمشروع مستقل تصف الحالة السابقة وليست بنية المستودع الحالية.

> تاريخ التحليل: 2026-08-02  
> منهجية الفحص: قراءة وفحص يدوي لكل ملف وسطر برمجي في المكوّنات المخصّصة، مع التحقق المتقاطع عبر `grep` وبنية المجلدات وملفات البناء (Gradle / Docker) — لا الاعتماد على أي ادّعاء ضمني في الملفات.
> النطاق: تم التركيز على **المكوّنات المخصّصة** المُضافة فوق قاعدة Signal-Android الأصلية (وهي: `app-android`, `backend-server`, `server`, `admin-dashboard`, `admin_dashboard`, `media-sfu`, `pstn-asterisk`, `shared-proto`, `wire-handler`, والكود المُحقن داخل `app/.../developed/`)، لأنها تمثّل «القيمة المضافة» للمشروع. قاعدة Signal الأصلية (4216 ملف Kotlin/Java) سليمة البنية كإطار عام لكنها **لا تتكامل فعلًا** مع الإضافات.

---

## 0. الخلاصة التنفيذية (اقرأ هذا أولاً)

المشروع يقدّم نفسه على أنه «إمبراطورية اتصالات سيادية 100% محلية» تدمج تطبيق Signal-Android مع خادم Spring Boot ولوحة تحكم React ومحرك وسائط Mediasoup وبوابة PSTN/Asterisk — في 9 أنظمة تعمل بأمر واحد (`docker-compose up`).

**بعد الفحص المعمّق لكل سطر، النتيجة الحاسمة هي: المشروع في حالته الحالية لا يُبنى (لا يُcompile) ولا يُنشر ولا يعمل إطلاقاً.** الأسباب الجوهرية:

| # | العيب القاتل | الأثر |
|---|---|---|
| 1 | الكود المُحقن في تطبيق Signal الأصلي (`app/.../developed/`) يحتوي على **أخطاء تجميع قاتلة**: تضارب اسم ملف/اسم صنف Java، واستيراد رموز غير موجودة في مسار صنوف وحدة `app`. | **تعطّل بناء تطبيق الأندرويد بالكامل** — لا يمكن إنتاج APK. |
| 2 | `backend-server` **لا يحتوي على أي صنف `@SpringBootApplication` أو `main()`**، ولا إصدار (version)، ولا يولّد صنوف protobuf، واسم الـ JAR في Dockerfile خاطئ. | الخادم لا يُبنى ولا ينفّذ. |
| 3 | مكوّن `app-android` **بلا `build.gradle.kts` أو `AndroidManifest.xml`**، وبتبعيات مفقودة (Hilt/Room/Proto غير مُعرّفة). | لا يمكن تجميعه إطلاقاً رغم أن `DEPLOY.md` يطلب «بناء APK منه». |
| 4 | `docker-compose.yml` يشير إلى مسارات غير موجودة (`nginx.conf`، Dockerfile لـ `media-sfu` و`admin_dashboard`)، ويعتمد على خدمتين مكررتين متناقضتين. | `docker-compose up` يفشل مباشرة. |
| 5 | كل «المنطق الفعلي» (تسليم مضمون، VoIP 4K/AV1، بوابة Dumin، اعتماد المدير) هو **stub يطبع `println` فقط** ولا يحقق أي وظيفة حقيقية. | ادّعاءات تسويقية بلا أساس تقني. |

درجة الجاهزية للإنتاج: **0%**.

---

## 1. بنية المشروع العامة

```
RED_Ultimate/
├── (قاعدة Signal-Android الأصلية) app/, core/, lib/, feature/, demo/, build-logic/, ...
│   └── داخلها حقن مخصص: app/src/main/java/org/thoughtcrime/securesms/developed/  ← الكود المُضاف
│       └── app/src/main/java/org/thoughtcrime/securesms/dependencies/DevelopedServerConfig.java
├── app-android/          ← «تطبيق بديل» بـ Kotlin/Compose (بلا نظام بناء)
├── backend-server/       ← Spring Boot 3.4 (بلا main، بلا توليد proto)
├── server/               ← Spring Boot ثانٍ مهجور (بلا build.gradle/Dockerfile)
├── admin-dashboard/      ← React+TS ناقص (بلا package.json/entry)
├── admin_dashboard/      ← React+JS+TS مكرّر (بلا Dockerfile/build tooling)
├── media-sfu/            ← Node/Mediasoup (stub بلا Dockerfile)
├── pstn-asterisk/        ← Asterisk config + Dockerfile (يفتقد pjsip.conf)
├── shared-proto/         ← تعريف protobuf واحد
├── wire-handler/         ← وحدة Wire حقيقية من Signal (سليمة)
├── infrastructure/       ← سكربت تهيئة
├── docker-compose.yml    ← منسّق الحاويات (مكسور)
├── build-and-run.sh, DEPLOY.md, MASTER_CHECKLIST.txt, audit_check.py
└── .kilo/, .artifacts/   ← ملفات تخطيط/مخرجات سابقة (تجاهلتها واعتمدت فحصي الخاص)
```

**ملاحظة هيكلية خطيرة:** يوجد **تكرار وتناقض** في كل طبقة:
- خادمان: `backend-server` (له build/Dockerfile لكن بلا main) و`server` (له main لكن بلا build/Dockerfile).
- لوحتان: `admin-dashboard` (TS ناقص) و`admin_dashboard` (JS/TS مكرّر).
- ملفّا docker-compose: `docker-compose.yml` و`temp-dc.yml` بمسارات وخدمات مختلفة متناقضة.
- ملفّا UUID-v7 وعملاء WebSocket و`ChatWebSocketHandler` بنسختين متناقضتين في `com.developedchat` و`com.red.server`.

هذا التضاعف يدل على غياب تصميم موحّد ودمج سلسلسي متعدد غير مُنجَز.

---

## 2. التحليل التفصيلي حسب المكوّن

### 2.1 تطبيق Signal-Android الأصلي (app, core, lib, feature, ...)

- البنية الأساسية سليمة (إعدادات Gradle حديثة: AGP 9.x، Kotlin، Compose، Wire 6.4، R8 deterministic في `gradle.properties`).
- `settings.gradle.kts` و`build.gradle.kts` الجذريين هما إعدادات Signal الحقيقية ولا يشيران إلى أي وحدة من الوحدات المخصّصة (`app-android`, `backend-server`, ...). أي أن **هذه الوحدات معزولة تماماً عن نظام البناء الرسمي ولن تُبنى معه**.
- ⚠️ تم تعديل `ApplicationContext.java` (داخل `onCreate()`) لإضافة:
  ```java
  // RED Master Initialization
  org.thoughtcrime.securesms.developed.REDCore.INSTANCE.initializeEverything();
  ```
  هذا التعديل يربط كود الـ stub ببداية تشغيل التطبيق، لكنه لا يضيف أي قيمة (انظر 2.7).

### 2.2 الكود المُحقن داخل التطبيق: `app/src/main/java/org/thoughtcrime/securesms/developed/`

هذا أهم جزء لأنه **الوحيد المرتبط فعلياً ببناء تطبيق الأندرويد**، وهو ما يكسر البناء.

الملفات:
- `developed/delivery/GuaranteedDelivery.kt`
- `developed/pstn/DuminManager.kt`
- `developed/voip/UltraHDCall.kt`
- `developed/voip/DevelopedVoipController.java`
- `developed/voip/QualityController.kt`
- `developed/DevelopedChatCore.kt` (object `REDCore`)
- `developed/MasterIntegration.kt` (object `MasterIntegration`)
- `developed/DevelopedChatInitialization.java` (public class `REDInitialization`)
- `dependencies/DevelopedServerConfig.java`

#### 🔴 الأخطاء القاتلة (Compile errors تُسقِط بناء التطبيق بالكامل)

**(أ) تضارب اسم الملف مع اسم الصنف في Java — مخالفة قاطعة لمواصفات Java:**
`DevelopedChatInitialization.java` يحتوي:
```java
public class REDInitialization {   // الملف اسمه DevelopedChatInitialization.java
```
مترجم Java يرفض ذلك: *“class REDInitialization is public, should be declared in a file named REDInitialization.java”*. → **فشل البناء**.

**(ب) استيراد رموز غير قابلة للحل في وحدة `app`:**
`MasterIntegration.kt` يستورد:
```kotlin
import com.red.core.delivery.DeliveryEngine   // غير موجود في مسار أصناف وحدة app
import com.red.features.pstn.PstnEngine        // غير موجود
```
هاتان الحزمتان (`com.red.*`) توجدان فقط كملفات حرة داخل `app-android/` التي **ليست وحدة مُضمَّنة في بناء `app`** ولا معتمَدة في `app/build.gradle.kts`. → *unresolved reference* → **فشل البناء**.

**(ج) استيراد غير مستخدم:** `QualityController.kt` يستورد `org.signal.ringrtc.CallManager` ولا يستخدمه إطلاقاً (تحذير → خطأ مع ktlint الصارم في المشروع).

#### ⚠️ عيوب منطقية ومغالطات (حتى لو أُصلحت أخطاء الترجمة)

- **`REDCore.checkApprovalStatus()` تُرجع دائماً `true`** (سطر: `return true`). أي أن «ببوابة اعتماد المدير» المزعومة لا تمنع شيئاً. بينما `MasterIntegration.checkAdminApproval()` تُرجع `false` دائماً — تناقض داخلي، وكلاهما dead code ثابت.
- **`GuaranteedDelivery.generateMsgId()` ليس UUID v7** بل سلسلة عشوائية:
  ```kotlin
  "${System.currentTimeMillis()}-${UUID.randomUUID()}"  // هذا v4 مسبوق بطابع زمني
  ```
  ادّعاء «UUID v7 لضمان عدم ضياع الرسائل» باطل تقنياً.
- **`DevelopedServerConfig.java`** يضع ثوابت URL فقط (`http://192.168.1.50:8080`) لكن **لا شيء في شبكة Signal الأصلية يقرؤها**. الادعاء «إعادة توجيه كل خدمات Signal إلى الخادم المحلي» غير صحيح: شبكة Signal تُبنى عبر `SignalServiceConfiguration`/`SignalServiceNetworkAccess` ولم تُمَس. كذلك `System.setProperty("signal.service.url", ...)` في `REDInitialization.initialize()` مفتاح وهمي لا يقرؤه Signal.
- **`UltraHDCall`, `DevelopedVoipController`, `QualityController`** كلها تطبع نصاً عبر `System.out.println` ولا تلمس WebRTC/RingRTC/الترميز فعلياً. خريطة المعاملات في `QualityController.setUltraHighQuality()` تُبنى ثم تُنسى (لا تُمرَّر لأي `CallManager`).
- **`DuminManager.connect()`** يطبع سطراً واحداً فقط — لا اتصال فعلي ببوابة GSM.
- **عناوين IP مُصلَّبة (hardcoded):** `192.168.1.50`, `192.168.1.100`, `192.168.1.100` — لا يمكن نشرها في بيئة حقيقية دون إعادة بناء.

### 2.3 مجلد `app-android/` (التطبيق البديل)

مجموعة من 24 ملف Kotlin/Compose **بلا أي ملف بناء** (لا `build.gradle.kts`، لا `settings.gradle.kts`، لا `AndroidManifest.xml`). لذلك:
- لا يمكن تجميعها أو إنتاج APK منها إطلاقاً، رغم أن `DEPLOY.md` يقول «ابنِ الـ APK من مجلد app-android».
- كودها يبدو معقولاً كـ UI Compose (شاشات Auth/Login/Register/Pending/Chat/PSTN/Stories)، لكنه **مرتبط بتبعيات لم تُعرَّف**.

#### عيوب برمجية دقيقة في ملفات app-android

- **تبعيات/أصناف مفقودة (لن تُترجم):**
  - `NetworkModule.kt` يوفّر `AuthApi`, `DuminApi`, `REDDatabase` — لا توجد `REDDatabase` ولا `DuminApi` في المشروع كله (يوجد `PstnDao` فقط).
  - `DevelopedWebSocketClientImpl.kt` يطبّق واجهة `DevelopedWebSocketClient` ويستخدم `MessageDeliveryManager` — **كلاهما غير معرّف** إطلاقاً.
  - `ChatViewModel.kt` و`ChatDetailScreen.kt` يستخدمان `MessageDao`, `MessageDeliveryManager`, `MessageEntity`, `MessageStatus` من `com.red.core.delivery` — **لا توجد**.
  - `PstnViewModel.kt` يستخدم `DuminApi`, `PstnCallRequest`, `PstnCallState` — غير معرّفة (يوجد `PstnCallLog`/`PstnDao` فقط).
  - `StoryCleanupWorker.kt` يستخدم `REDDatabase.storyDao()` — غير معرّف.
- **تضارب الحزمة مع مسار الملف** (Kotlin يتسامح لكنه يربك المسح ويسبب أخطاء IDE/بناء عند الدمج): ملفات في `com/developedchat/...` تعلن `package com.red....`.
- **منطق بـ UI مكسور:**
  - `AppNavigation.kt` يستدعي `MainActivity()` كأنه Composable، بينما `MainActivity` هو `ComponentActivity`. → لا يُترجم/يُنفّذ.
  - `MainActivity.kt` يستخدم `@AndroidEntryPoint` (Hilt) و`hiltViewModel()` في كل الشاشات، لكن لا يوجد `Application` مع `@HiltAndroidApp` ولا إعداد Hilt.
  - ثلاثة ملفات تنقّل (Navigation) متضاربة: `app/MainAppNavigation.kt`, `features/app/NavGraph.kt`, `features/app/.../AppNavigation.kt` — routes مختلفة وstartDestinations مختلفة.
  - `CameraCaptureScreen.kt`: تبديل الكاميرا (lensFacing) لا يعيد ربط الكاميرا (AndroidView factory يُستدعى مرة واحدة فقط)؛ و`Executors.newSingleThreadExecutor()` لا يُغلَق أبداً (تسريب خيوط/ذاكرة).
  - `StoryViewerScreen.kt`: إذا كانت قائمة القصص فارغة يرمي `IndexOutOfBoundsException`.
  - `PstnViewModel.makeCall`: يُصلِّب `duminIp = "192.168.1.100"`.
- **بيانات وهمية (mock):** `ChatListScreen`، `ChatDetailScreen` ("me", "12:00 PM")، وكل `MonitorController`/الشاشات تستخدم قيماً ثابتة.

### 2.4 `backend-server/` (Spring Boot المُفترَض)

#### 🔴 أخطاء قاتلة (لا يُبنى ولا يعمل)

1. **لا يوجد صنف `@SpringBootApplication` ولا دالة `main()` في كامل المجلد** (تحققت بـ `grep`). فالـ JAR الناتج من `bootJar` لا يحوي نقطة دخول، وأمر Docker `java -jar build/libs/backend-1.0.0.jar` سيفشل.
2. **اسم الـ JAR خاطئ:** لا يوجد `version` في `build.gradle.kts`، فينتج `bootJar` اسم `backend-server.jar` (وليس `backend-1.0.0.jar`). → Dockerfile لا يجد الملف.
3. **لا يوجد توليد لأكواد Protobuf:** لا plugin اسمه `com.google.protobuf` في `build.gradle.kts` (موجود فقط الاعتمادية runtime `protobuf-java:3.25.1`)، وملف `shared-proto/messages.proto` **ليس** تحت `src/main/proto`. بالتالي **الصنف `com.red.proto.ChatProtos` لا يتولّد**، وكل ملف يستورده يفشل في الترجمة.
4. **تباين الحزمة vs مسار الملف:** كل الملفات تحت `src/main/kotlin/com/developedchat/...` تعلن `package com.red....`. عند فحص مكوّنات Spring من حزمة الجذر، لن يُكتَشف ما هو خارج شجرة الحزمة المُعلنة.
5. **تبعية مفقودة:** `RedisConfig.kt` يستخدم `@Bean` دون استيراده، ويحقن `RedisMessageSubscriber` (موجود، جيد).

#### عيوب منطقية في خدمات backend

- **`MessageService.processIncomingMessage`:**
  - `mongoTemplate.save(msg)` حيث `msg` هو `ChatProtos.ChatMessage` (protobuf) — ليست `@Document`؛ المحوّل الافتراضي لـ Spring Data Mongo لا يعرف كيف يُسلسل رسالة protobuf → فشل/تخزين مشوّه. الكيان الصحيح `MessageDocument` **مُعرَّف لكنه غير مستخدم إطلاقاً**.
  - استعلام `Criteria.where("conversation_id")` و`"receiver_id"` و`"sequence_number"` يستخدم أسماء حقول **snake_case**، بينما `MessageDocument` يعرّف خصائص camelCase (`conversationId`, `sequenceNumber`) التي تُخزَّن بنفس الاسم. → الاستعلامات لا تطابق شيئاً.
  - مفتاح إزالة التكرار `msg_dedup:` بـ TTL 24 ساعة جيد، لكنه لا يُعالَج عند تعذّر الكتابة.
- **`ChatWebSocketHandler` (النسخة developedchat):**
  - يستخدم `chatMsg.receiver_id` و`chatMsg.sequence_number` (snake_case) — وهذه **لا تُترجَم** في Kotlin (الأكسسرز camelCase: `receiverId`). خطأ تجميع.
  - يُرسِل ACK **قبل** فحص التكرار والتخزين (ترتيب خاطئ للخوارزمية المعلنة).
  - `processedMessages` مجموعة ConcurrentHashMap غير محدودة النمو (لا تنظيف) → **تسرّب ذاكرة** دائم.
  - `saveToMongo` يطبع فقط؛ `sendPushNotification` فارغة.
- **`MonitorController`:** يُرجع قيماً ثابتة (`"cpu_usage":"15%"`, `142` اتصال، `"dumin_status":"CONNECTED"`) — مراقبة وهمية تماماً.
- **`SecurityConfig`:** يحمي `/api/admin/**` بدور `ADMIN`، لكن لا توجد آلية مصادقة/JWT فعليّة تمنح الدور → كل طلبات الإدارة سترفض بـ 403. كذلك `csrf().disable()` بدون استبدال بآلية حماية (CSRF token) لجلسات الويب.
- **`DuminGatewayService.initiatePstnCall`:** `response.body?.get("call_id") as String` — إذا كان null يرمي `NullPointerException`. ولا توجد معالجة مهلة/أخطاء.
- **`PstnRelayService`:** يبني URL هاتفياً دون ترميز (`?number=$phoneNumber`) → قابل لحقن (URL/parameter manipulation)، ويطبع `responseCode` دون التحقق منه ولا يقرأ الاستجابة (تسرّب اتصال).
- **`StorageController`:** «الرفع إلى MinIO» مُحاكى بطباعة سطر وإرجاع URL وهمي؛ لا تخزين فعلي.
- **`UserEntity` (JPA) مقابل `User` (Moshi in app-android):** تعريفات مستخدم متعددة متضاربة (حتى قيم `UserStatus` تختلف: `UserEntity` فيها `REJECTED`+`BANNED`، و`Models.kt` نفسها لكن بحزم مختلفة)؛ `AuthController` الـ developedchat يستورد `com.red.core.models.User` الذي **لا وجود له في backend-server** (موجود فقط في app-android). → خطأ تجميع.

### 2.5 مجلد `server/` (الخادم الثاني المهجور)

- يحتوي الوحيد على `@SpringBootApplication` (`RedMasterServer`) لكن:
  - **لا `build.gradle.kts` ولا `Dockerfile`** → لا يُبنى ولا يُنشر، و`docker-compose` يتجاهله (يبني `./backend-server`).
  - يستورد رموزاً غير موجودة: `com.red.sovereign.proto.ChatProtos` و`CallProtos` (الحزمة الفعلية `com.red.proto`، ولا يوجد `CallProtos` إطلاقاً في أي `.proto`)؛ `com.red.server.messaging.MessageService` (الموجود في `com.red.core.delivery`)؛ `com.red.sovereign.features.chat.GroupEntity` و`features.stories.StoryEntity` (غير معرّفة).
  - `application.properties`: منفذ `8443` مع مخزن مفاتيح `red-keystore.p12` **غير موجود** ← يفشل الإقلاع؛ و`server.address=0.0.0.0` مع تعليق «Force local-only» وهذا تناقض (0.0.0.0 يربط كل الواجهات، عكس «محلي فقط»). منفذ 8443 لا يتطابق مع تعيين 8080 في docker-compose.
- هذا المجلد كله **dead/orphan** ويضيف تشويشاً فقط.

### 2.6 لوحتا الإدارة `admin-dashboard/` و`admin_dashboard/`

#### `admin-dashboard/` (TypeScript)
- صفحتان فقط (`Dashboard.tsx`, `UserApproval.tsx`) بـ Ant Design+ECharts.
- **بلا `package.json`، بلا `index.html`، بلا نقطة دخول/App، بلا routing.** → غير قابل للتشغيل.
- بيانات وهمية ثابتة داخل المكوّنات.

#### `admin_dashboard/` (JS + TSX مختلط)
- `package.json` يفتقر إلى `react-scripts`/أداة بناء، و`main: "index.js"` غير موجود، ولا `start`/`build` scripts. لا Dockerfile رغم أن `docker-compose` يبني منه.
- يخلط `.js` و`.tsx` بلا `tsconfig.json`/babel.
- **تباين مسارات الـ API مع الخادم** (تحققت):
  - `LiveMonitor` يستدعي `/api/admin/monitor/stats` ويتوقع حقول `voip/pstn/msgs` — لكن `MonitorController` يُرجع `messages_24h/calls_24h/new_users_24h/stories_active`. ← أسماء حقول مختلفة.
  - `Diagnostics` يقرأ `data.media_sfu_status` — غير موجود في الاستجابة.
  - `Approvals` يستدعي `/api/admin/pending-users` و`/api/admin/approve/{id}?status=` — **لا يوجد endpoint كهذا** (يوجد `/api/admin/approve/{userId}` بلا status، وبلا قائمة pending).
  - `UserApproval.tsx` يستدعي `/api/auth/admin/pending-list` و`/api/auth/admin/approve?email=&status=` — **لا يوجد**.
- نتيجة: حتى لو شغّلت اللوحة، كل استدعاءاتها تفشل 404/field mismatch.

### 2.7 `media-sfu/` (Node/Mediasoup)

- `server.js` ينشئ worker وrouter ثم **يتوقف** — لا WebSocket signaling رغم اعتمادية `ws`، ولا transports/producers/consumers. عديم الفائدة عملياً.
- الترميز الوحيد المُعدّ هو **VP9 بدقة 1080p** (التعليق صريح: «VP9 1080p») و4Mbps. ومع ذلك `MASTER_CHECKLIST.txt` يدّعي «AV1/VP9 at 4K» — **ادعاء خاطئ**.
- **لا Dockerfile** رغم `docker-compose` يبني من `./media-sfu` → فشل البناء.
- لا معالجة أخطاء، لا سجل، لا graced shutdown.

### 2.8 `pstn-asterisk/` (Asterisk)

- `Dockerfile` ينفّذ `COPY pjsip.conf /etc/asterisk/pjsip.conf` لكن **`pjsip.conf` غير موجود** في المجلد → فشل البناء.
- `extensions.conf` يحيل إلى trunk باسم `dumin-trunk` وendpoint `webrtc-client` يلزم تعريفهما في `pjsip.conf` المفقود.
- `manager.conf`: ربط `0.0.0.0`، وسرّ ضعيف ثابت `red_secret_123` بصلاحيات `read=all, write=all` واسم مستخدم `red_admin` — وهو نفسه مُصلَّب في `backend-server/.../PstnManager.kt` (`red_admin`/`red_secret_123`). **سرّ تشغيلي مكشوف في المصدر.**
- **تناقض النشر:** `docker-compose.yml` يستخدم `image: andrius/asterisk` مع mount للمجلد (بدون بناء)، بينما `pstn-asterisk/Dockerfile` يتبنى نهج النسخ. المنطقان متناقضان.

### 2.9 `shared-proto/messages.proto`

- تعريف سليم لـ `ChatMessage`, `MessageAck`, `MessageType`, `AckStatus`. لكن:
  - لا يوجد تعريف لـ `CallSignal`/`CallProtos` رغم استخدامه في `server/.../CallWebSocketHandler`.
  - حقل `sequence_number` في `ChatMessage` موجود، لكن **غير موجود في `MessageAck`** — ومع ذلك `red/server/ChatWebSocketHandler.sendAck` يستدعي `.setSequenceNumber(seq)` → خطأ تجميع.
  - الـ proto لا يُولّد أكواداً لأي وحدة (لا plugin، ولا موضعة تحت src/main/proto). فهو ملف توثيقي فقط في الواقع.

### 2.10 `wire-handler/`

- الوحدة الوحيدة **السليمة فعلياً** (هي وحدة Wire حقيقية من Signal: تستبدل `countNonNull` بـ `countNonDefa` عبر `SchemaHandler`)، مرتبطة بالـ JAR في `build.gradle.kts` الجذري. لا علاقة لها بمنطق RED، لكنها لا تضر.

### 2.11 البنية التحتية والسكربتات

- `infrastructure/setup-env.sh`:
  - ينشئ bucket عاماً `mc policy set public local/red-media` → **وسائط قابلة للقراءة عمومياً** بدون تحكم وصول (إشكالية خصوصية جسيمة لتطبيق يتباهى بالتشفير).
  - يضع بيانات اعتماد MinIO النصية `admin password` في سطر الأوامر (تظهر في `ps`/history).
  - ينشئ قاعدة بيانات بأمر `psql` لكن دون معالجة إذا كانت موجودة (يفشل).
- `docker-compose.yml`:
  - سرّ `MINIO_ROOT_PASSWORD=redsecret123` و`POSTGRES_PASSWORD=password` و`SECRET=redturnsecret` كلها نصية في المصدر.
  - خدمة `nginx` تشير إلى `./nginx.conf` **غير موجود** → فشل.
  - `admin-panel` يبني `./admin_dashboard` الذي **بلا Dockerfile** → فشل.
  - `media-sfu` مبني من مجلد **بلا Dockerfile** → فشل.
  - لا service للـ SFU تتعامل مع UDP RTC رغم تعيين المنافذ.
- `build-and-run.sh`: يفحص `docker-compose` (الأمر القديم) بدل `docker compose`.
- `temp-dc.yml`: مسارات خاطئة (`./mediasoup-sfu` بينما المجلد `media-sfu`، `./asterisk/config`، `./admin`). ملف مهجور/تجميعي غير مكتمل.
- `audit_check.py`: يبحث عن المسار `RED_Ultimate/app/...` (تكرار اسم الجذر)، ويتحقق فقط من **وجود نصوص** مثل `"4K"` و`"AV1"` داخل ملفات — وهي موجودة كـ string literals داخل stubs تطبع، فينجح الفحص زوراً. أي أن سكربت «المراجعة» يُصدّق ادعاءات غير محققة.
- `.gitignore`: كتلة `.kotlin/` مكررة مرتين.
- `MASTER_CHECKLIST.txt`: ينتهي بعبارة «VERIFICATION: COMPLETE. NO FEATURES MISSING. ZERO CLOUD DEPENDENCY» — وهي **غير صحيحة** بحسب كل ما سبق.

---

## 3. الثغرات والمخاطر الأمنية

1. **اعتماد/مصادقة وهمية:** login يُرجع `"mock-jwt-token"` / `"red-jwt-<uuid>"` بدون توقيع أو تحقق. لا password hashing (حقل `passwordHash` موجود لكن لا يُستخدم)، لا انتهاء صلاحية، لا تخزين آمن للجلسة.
2. **بوابة اعتماد المدير معطّلة:** `checkApprovalStatus()` تُرجع `true` دوماً. أي مستخدم يصل للنظام دون اعتماد — عكس الادعاء الأساسي للمشروع.
3. **أسرار مُصلَّبة في المصدر:** كلمات مرور PostgreSQL/MinIO/Coturn، وسرّ Asterisk Manager، وIPs داخلية — كلها مكشوفة في git.
4. **CSRF معطّل** بلا تعويض في `SecurityConfig`.
5. **MinIO bucket عام** → تسريب الوسائط.
6. **حقن عبر URL** في `PstnRelayService.relayToDumin` و`DuminGatewayService` (رقم الهاتف مباشرة في query string بلا ترميز/تحقق).
7. **ربط Asterisk Manager وSpring على `0.0.0.0`** رغم ادعاء «محلي فقط».
8. **Kill-Switch وهمي:** `SecurityController` يطبع سطراً فقط ولا يقطع جلسات فعلياً ولا يمحو بيانات.
9. **شهادة TLS مفقودة:** `application.properties` يشير لـ `red-keystore.p12` غير موجود → إما فشل الإقلاع أو إيقاف TLS ضمناً.
10. لا تحقق من صحة الإدخال (input validation) في كل الـ controllers (تعتمد على `Map<String,String>` وفحص `null` فقط).

---

## 4. الأخطاء البرمجية المُجمَّعة (سريعة المرجع)

| الملف | الخطأ |
|---|---|
| `app/.../developed/DevelopedChatInitialization.java` | `public class REDInitialization` لا يطابق اسم الملف → رفض الترجمة. |
| `app/.../developed/MasterIntegration.kt` | استيراد `com.red.core.delivery.DeliveryEngine` و`com.red.features.pstn.PstnEngine` غير قابلة للحل في وحدة `app`. |
| `backend-server/.../developedchat/ChatWebSocketHandler.kt` | `chatMsg.receiver_id` / `sequence_number` snake_case غير صالحة في Kotlin. |
| `backend-server/.../developedchat/auth/AuthController.kt` | استيراد `com.red.core.models.User/UserStatus` غير موجود في backend. |
| `backend-server/.../red/server/websocket/ChatWebSocketHandler.kt` | استيراد `com.red.sovereign.proto.ChatProtos` و`com.red.server.messaging.MessageService` خاطئ؛ `setStatus(String)` بدل enum؛ `setSequenceNumber` غير موجود في `MessageAck`. |
| `backend-server/.../red/server/websocket/CallWebSocketHandler.kt` | `com.red.sovereign.proto.CallProtos` غير معرّف إطلاقاً. |
| `backend-server/.../red/server/services/CoreService.kt` | `GroupEntity`/`StoryEntity` من `com.red.sovereign.features.*` غير معرّفة. |
| `backend-server/.../red/server/auth/AuthController.kt` | تعارض mapping `/api/auth` مع `developedchat/auth/AuthController` إن دُمجا. |
| `app-android/.../di/NetworkModule.kt` | `REDDatabase`, `DuminApi` غير معرّفة. |
| `app-android/.../delivery/DevelopedWebSocketClientImpl.kt` | `DevelopedWebSocketClient`, `MessageDeliveryManager` غير معرّفة. |
| `app-android/.../chat/ChatViewModel.kt` + `ChatDetailScreen.kt` | `MessageDao`, `MessageDeliveryManager`, `MessageEntity`, `MessageStatus` غير معرّفة. |
| `app-android/.../pstn/PstnViewModel.kt` + `PstnCallScreen.kt` | `DuminApi`, `PstnCallRequest`, `PstnCallState` غير معرّفة. |
| `app-android/.../app/.../AppNavigation.kt` | استدعاء `MainActivity()` كـ Composable (هو Activity). |
| كل مراجع `ChatProtos` في backend | الصنف لا يتولّد (لا protobuf plugin، ولا src/main/proto). |
| `backend-server` ككل | بلا `@SpringBootApplication`/`main()`، وبلا `version`. |
| `app-android` ككل | بلا `build.gradle.kts`/`AndroidManifest.xml`. |
| `media-sfu`, `admin_dashboard` | بلا Dockerfile رغم الإشارة إليها في docker-compose. |
| `pstn-asterisk/Dockerfile` | `COPY pjsip.conf` لملف غير موجود. |

---

## 5. عيوب الأداء والموثوقية

- **تسرّبات ذاكرة:** `processedMessages` في WebSocket handler غير محدودة؛ `Executors.newSingleThreadExecutor()` في Camera غير مغلق.
- **عدم أمان الخيوط:** بعض الخرائط `ConcurrentHashMap` تُعدَّل دون atomicity في قرارات منطقية (مثلاً check-then-act في dedup).
- **عدم وجود إعادة محاولة/طابور حقيقي** للتسليم رغم ادعاء «Guaranteed Delivery».
- ** reconnect() في WebSocket العميل يطبع فقط** (لا backoff فعلي).
- **Polling ثقيل** كل ثانية في `PstnViewModel.startPollingStatus` بلا إلغاء منضبط عند إتلاف ViewModel.
- **مراقبة وهمية:** كل قياسات الأداء/CPU/Disk ثابتة، فلا يمكن الاعتماد عليها للتنبيهات.

---

## 6. المغالطات بين الادّعاء والتطبيق

| الادعاء (في MASTER_CHECKLIST/README) | الواقع المتحقق |
|---|---|
| «VoIP 4K/AV1» | Stub يطبع فقط؛ SFU يضبط VP9 1080p. |
| «Guaranteed delivery، UUID v7، ACK» | `timestamp+UUIDv4` وليس v7؛ ACK قبل التخزين؛ dedup بدون تنظيف. |
| «Admin Approval enforced» | `checkApprovalStatus()` تُرجع `true` دوماً. |
| «PSTN معزول منطقياً» | `verifySystemIsolation()` جسم فارغ. |
| «ZERO CLOUD dependency / 100% Local» | يعتمد على maven central/cloudsmith/build-artifacts.signal.org وmobilecoin وandrius/asterisk وpostgres/mongo/redis/minio الرسمية — لكن «محلي» يقصد به التشغيل لا البناء. |
| «Docker يطلق النظام بأمر واحد» | يفشل بسبب المسارات/الـ Dockerfiles المفقودة. |
| «VERIFICATION: COMPLETE» | سكربت `audit_check.py` يتحقق من وجود نصوص حرفية فقط. |

---

## 7. توصيات التحسين (مرتّبة حسب الأولوية)

### 🔴 أولوية حرجة (لا قيمة للمشروع قبل حلّها)

1. **حلّ أخطاء التجميع في `app/.../developed/`:** إعادة تسمية الملف لتطابق `public class`، أو حذف `MasterIntegration.kt`/`DevelopedChatInitialization.java` (dead code)، وإصلاح كل الاستيرادات غير المحلولة. **بدون هذا لا يُبنى تطبيق Signal.**
2. **إضافة نقطة دخول لـ `backend-server`:** صنف `@SpringBootApplication` + `main()`، وتعيين `version = "1.0.0"`، وتعديل Dockerfile ليعكس اسم الـ JAR الصحيح، أو استخدام `bootJar.archiveFileName`.
3. **تفعيل توليد protobuf:** إضافة plugin `com.google.protobuf` ووضع `messages.proto` تحت `src/main/proto` (لكل من backend وأي وحدة Android تحتاجه)، وإضافة تعريف `CallSignal`.
4. **توحيد الحزم (packages):** جعل مسار الملف يتطابق مع `package`، واختيار اسم نطاق واحد (`com.red.*` أو `com.developedchat.*`) وحذف التكرار بين `backend-server` و`server` (دمجهما أو حذف أحدهما).
5. **إضافة نظام بناء لـ `app-android`:** `build.gradle.kts` (AGP+Compose+Hilt+Room+Retrofit+Moshi+Protobuf) و`AndroidManifest.xml` و`settings.gradle.kts`، أو حذفه كلياً والاكتفاء بحقن `developed/*` في Signal.
6. **إصلاح `docker-compose.yml`:** إضافة `nginx.conf`، وDockerfiles لـ `media-sfu` و`admin_dashboard` (أو استبدالها بصور جاهزة)، وتعديل `temp-dc.yml` أو حذفه، واستخدام `docker compose` (v2).
7. **إصلاح `pstn-asterisk`:** إضافة `pjsip.conf` وتعريف الـ trunks والـ endpoints.

### 🟠 أولوية عالية (أمان/صحة)

8. **مصادقة حقيقية:** JWT موقّع + خوارزمية هاش كلمة المرور (BCrypt/Argon2) + تخزين آمن للجلسات + انتهاء صلاحية.
9. **تفعيل بوابة اعتماد المدير فعلياً:** ربط `checkApprovalStatus` بقاعدة البيانات + middleware يرفض المستخدمين غير المعتمدين.
10. **نقل كل الأسرار إلى متغيرات بيئة/أسرار** (`.env` غير مُلتزم، Vault)؛ إزالة كلمات المرور من git وتدويرها.
11. **إعادة تمكين حماية CSRF/ CORS محدود**، وتفعيل TLS بشهادة فعلية.
12. **إصلاح تسرّبات الذاكرة/الخيوط** وتحقيق منطق إعادة المحاولة/backoff في WebSocket.
13. **توحيد أسماء حقول الـ API** بين الواجهات (dashboards) والخادم (`voip/pstn/msgs` مقابل `*_24h`).
14. **تطبيق منطق إزالة التكرار/التسليم فعلياً** عبر طابور (مثل Kafka/Redis Streams) بدل stubs، واستخدام `MessageDocument` الحقيقي في Mongo وتوحيد أسماء الحقول.
15. **جعل MinIO buckets خاصة** مع روابط موقّعة (presigned URLs).

### 🟡 أولوية متوسطة (جودة/قابلية صيانة)

16. حذف الكود المكرر/المهجور: `server/`، إحدى اللوحتين، `temp-dc.yml`، `audit_check.py` المُضلِّل، `MASTER_CHECKLIST.txt` «الكاذب».
17. إضافة اختبارات (Unit/Integration) — لا يوجد أي اختبار في المكوّنات المخصّصة.
18. استبدال `println` بـ إطار تسجيل (SLF4J/Logback) مع مستويات وملفات.
19. إضافة `react-scripts`/Vite + Dockerfile لـ `admin_dashboard` و إكمال `admin-dashboard`.
20. توثيق حقيقي للأخطاء بدل عبارات «COMPLETE/NO FEATURES MISSING».

### 🟢 أولوية منخفضة (تحسينات)

21. مركزة الإعدادات (IPs، رموز الترميز، TTL) في ملف config واحد.
22. تنفيذ UUID v7 فعلي (توجد مكتبات) أو توضيح أن المعرف زمني.
23. مراقبة فعلية (Micrometer/Prometheus) بدل القيم الثابتة.
24. إضافة healthchecks حقيقية وgraceful shutdown في كل خدمة.

---

## 8. الخلاصة

مشروع **RED Ultimate** في واجهته يبدو طموحاً متكاملاً (تطبيق + خادم + لوحة + وسائط + PSTN + نشر بأمر واحد)، لكن **الفحص الدقيق لكل ملف وسطر يكشف أنه نموذج «واجهة بلا أساس»: كل قدراته المُعلَنة عبارة عن stubs تطبع نصاً، وكل طبقات النشر مكسورة بمسارات/ملفات مفقودة، وحقن الكود في تطبيق Signal الأصلي يكسر بناءه بالكامل بأخطاء تجميع قاطعة (اسم ملف/صنف، واستيرادات غير قابلة للحل)، فضلاً عن ثغرات أمنية جوهرية (مصادقة وهمية، أسرار مكشوفة، اعتماد مدير معطّل).**

التوصية: المشروع **غير جاهز للإنتاج ولا حتى للتطوير التجريبي المتكامل**، ويحتاج إلى إعادة هيكلة جذرية تبدأ بـ (1) إصلاح أخطاء التجميع، (2) إضافة أنظمة بناء صحيحة، (3) استبدال الـ stubs بمنطق حقيقي مع اختبارات، (4) معالجة الثغرات الأمنية — بالترتيب الوارد في قسم التوصيات.

---

*تم إعداد هذا التقرير بالاعتماد الحصري على الفحص المباشر لكود المصدر وملفات البناء والإعداد، مع التحقق المتقاطع عبر أدوات البحث في المستودع — لا على أي ادّعاءات داخلية أو ملفات تخطيط سابقة.*
