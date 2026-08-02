# خطة تنفيذية: إزالة الاعتماديات على Signal وجعل المشروع مستقلاً

## حالة الحالية (Current Status)

**الملخص:** مشروع monorepo (RED_Ultimate) يحتوي على:
- **تطبيق أندرويد** (Signal-Android fork): pp/ + 50+ Gradle modules
- **خادم خلفي** (Spring Boot): ackend-server/ (20 ملف Kotlin)
- **Media SFU**: media-sfu/ (Node.js + Mediasoup, 29 سطر)
- **بوابة PSTN**: pstn-asterisk/ (Asterisk configs)
- **خوادم موزعة**: docker-compose.yml (9 خدمات)

**ملاحظة مهمة:** مجلد server/ (4 ملفات) غير مكتمل - لا يحتوي على uild.gradle.kts. ackend-server/ هو الخادم الرئيسي.

---

## جواب على سؤالك: هل يمكن تشغيل الخادم بدون Docker؟

**الإجابة: نعم** - ackend-server/ يمكنه التشغيل بدون Docker:

### المتطلبات لتشغيل backend-server بدون Docker:
1. **Java 21** (مطلوب من libs.versions.toml: javaVersion = "21")
2. **PostgreSQL** (محلي أو خادم منفصل)
3. **MongoDB 8** (محلي أو خادم منفصل)
4. **Redis 7** (محلي أو خادم منفصل)
5. **Asterisk** (للـ PSTN - اختياري)

### كيفية التشغيل:
\\\ash
cd backend-server
./gradlew bootRun  # أو gradle bootRun
\\\

### ملفات مطلوب تعديلها للتشغيل المحلي:
- ackend-server/src/main/resources/application.properties → إنشاء pplication-local.properties
- أو إنشاء docker-compose.dev.yml للقواعد فقط مع تشغيل backend-server محلياً

---

## القرارات المطلوبة (Unresolved Design Decisions)

### 1. Domain Name
**الحالة:** غير محدد
**توصية:** استخدام \localhost\ + ports للتطوير المحلي في Phase 0-2، وتحديد domain لاحقاً في Phase 4.

### 2. libsignal Strategy
**الحالة:** مرتبط بـ \org.signal:libsignal-*\
**توصية:** الخيار B (build from source مع الحفاظ على package الأصلي) كخطوة مؤقتة.

### 3. Media SFU
**الحالة:** 29 سطر (غير مكتمل)
**توصية:** الخيار B (RingRTC) للحصول على مكالمات عاملة بسرعة.

---

## الخطة التنفيذية (Phased Implementation Plan)

### Phase 0: الأساسيات (Week 1)
**الهدف:** بيئة تطوير محلية عاملة، إزالة أسرار مكشوفة

#### Task 0.1: Create dev Docker Compose
**الملف:** \.kilo/plans/docker-compose.dev.yml\ (جديد)
**Acceptance Criteria:**
- [ ] يحتوي على PostgreSQL 16, MongoDB 7, Redis 7, MinIO, COTURN
- [ ] كل خدمة تستخدم default credentials
- [ ] يعمل مع \docker-compose -f docker-compose.dev.yml up -d\
- [ ] كل الخدمات على \ed-net\ network

#### Task 0.2: Create .env.example template
**الملف:** \.env.example\ (جديد)
**Acceptance Criteria:**
- [ ] يحتوي على جميع env vars من \docker-compose.yml\
- [ ] كل قيمة placeholder
- [ ] يوثق كل متغير بـ comment

#### Task 0.3: Local Backend Setup
**الملف:** \ackend-server/src/main/resources/application-local.properties\ (جديد)
**Acceptance Criteria:**
- [ ] يستخدم \localhost\ بدلاً من Docker hostnames
- [ ] \./gradlew :backend-server:bootRun\ ينجح
- [ ] Health check على \/actuator/health\ يعيد 200

#### Task 0.4: Remove hardcoded secrets
**الملفات:**
- \ackend-server/.../PstnManager.kt\ (line 9)
- \pstn-asterisk/manager.conf\ (line 7)
**Acceptance Criteria:**
- [ ] استخدام environment variables
- [ ] لا توجد قيم hardcoded

### Phase 1: التطبيق الأساسي (Week 2-3)

#### Task 1.1: Create local.properties.example
**الملف:** \local.properties.example\
**Acceptance Criteria:**
- [ ] يحتوي على \sdk.dir\ و \quickstart.credentials.dir\
- [ ] يوضح كيف يتم إنشاء \local.properties\

#### Task 1.2: Simplify build variants
**الملف:** \pp/build.gradle.kts\ (lines 69-93)
**Acceptance Criteria:**
- [ ] تقليل إلى ≤15 variants
- [ ] \./gradlew :app:assembleDebug\ ينجح

#### Task 1.3: Verify local build
**Acceptance Criteria:**
- [ ] \./gradlew :app:assembleDebug\ يكتمل بنجاح
- [ ] APK يمكن تثقيفه على جهاز/محاكي

### Phase 2: إزالة التبعيات الخارجية (Week 4-5)

#### Task 2.1: Remove MobileCoin
**الملف:** \pp/build.gradle.kts\ (line 767, 356)
**Acceptance Criteria:**
- [ ] إزالة dependency و ProGuard rules
- [ ] بناء Debug ينجح

#### Task 2.2: Replace FCM → UnifiedPush
**الملفات:** \pp/build.gradle.kts\, \demo/registration/build.gradle.kts\
**Acceptance Criteria:**
- [ ] إزالة Firebase dependencies
- [ ] إضافة UnifiedPush library
- [ ] بناء Debug ينجح

#### Task 2.3: Replace Giphy/Map API keys
**Acceptance Criteria:**
- [ ] إزالة hardcoded API keys من BuildConfig
- [ ] إضافة placeholders للـ environment variables

#### Task 2.4: Replace Google Maps → MapLibre
**Acceptance Criteria:**
- [ ] إزالة Google Play Services Maps
- [ ] إضافة MapLibre + OpenStreetMap
- [ ] Maps يعمل في التطبيق

### Phase 3: التكامل (Week 6-7)

#### Task 3.1: Unify ChatWebSocketHandler
**الملفات:**
- \ackend-server/.../developedchat/.../ChatWebSocketHandler.kt\
- \ackend-server/.../red/server/websocket/ChatWebSocketHandler.kt\
**Acceptance Criteria:**
- [ ] نسخة واحدة فقط باستخدام MessageService
- [ ] Redis-based dedup (ليس in-memory)
- [ ] WebSocket auth مضافة

#### Task 3.2: Update Signal URLs في BuildConfig
**الملف:** \pp/build.gradle.kts\
**Acceptance Criteria:**
- [ ] جميع URLs تشير إلى \localhost:8443\ أو domain محلي
- [ ] بناء ناجح

#### Task 3.3: Test end-to-end messaging
**Acceptance Criteria:**
- [ ] رسالة تُرسل من التطبيق → تصل الـ backend
- [ ] رسالة تُطلق عبر WebSocket للمتلقي
- [ ] رسائل مخزنة في MongoDB

### Phase 4: الأمان والإنتاج (Week 8+)

#### Task 4.1: JWT Authentication
**Acceptance Criteria:**
- [ ] REST endpoints محمية بـ JWT
- [ ] WebSocket connections مصادقة
- [ ] Token refresh آلية

#### Task 4.2: TLS/SSL configuration
**Acceptance Criteria:**
- [ ] self-signed certificate للتطوير
- [ ] HTTPS يعمل على الـ backend
- [ ] WebSocket over WSS

#### Task 4.3: Monitoring stack
**Acceptance Criteria:**
- [ ] Spring Boot Actuator مفعل
- [ ] Prometheus metrics endpoint
- [ ] Grafana dashboard مرفق

---
## المخاطر (Risks & Mitigations)

| المخاطرة | الاحتمال | التأثير | التخفيف |
|----------|----------|---------|---------|
| كسر البناء بعد إزالة dependencies | عالي | متوسط | Commit كل تغيير dependencies منفرداً + build check |
| libsignal منقرصر للـ compilation | متوسط | عالي | الاحتفاظ بنسخة APK النهائية من Signal APK كـ fallback |
| Media SFU غير قابل للتشغيل | متوسط | عالي | استخدام RingRTC كـ fallback |

---

*الحالة: جاهز للتنفيذ - ينتظر تأكيد Domain Name و libsignal Strategy*
*الإصدار: 1.1*
