# تشغيل RED على خادم محلي

لا يحتاج المشروع إلى Dumin أو أي خدمة خارجية لكي يعمل. Dumin/PSTN معطل افتراضيًا، وتعمل الميزات الأساسية عبر خادم RED المحلي.

## تشغيل الخادم

من جذر المشروع:

```bash
cp .env.example .env
docker compose up -d --build
curl http://127.0.0.1:8080/actuator/health
```

تم نشر Backend على المنفذ `8080` في جهاز الخادم.

## إعداد تطبيق Android

### Android Emulator

أضف إلى `local.properties` في جذر المشروع:

```properties
red.server.url=http://10.0.2.2:8080
red.dumin.enabled=false
```

العنوان `10.0.2.2` يعني جهاز الكمبيوتر المضيف من داخل Android Emulator.

### جهاز Android فعلي

استبدل العنوان بعنوان LAN لجهاز الخادم:

```properties
red.server.url=http://192.168.1.50:8080
red.dumin.enabled=false
```

يجب السماح بالمنفذ 8080 في Firewall، والتأكد من أن Backend يستمع على `0.0.0.0` وليس `localhost` فقط.

### البناء

```bash
./gradlew :Signal-Android:assemblePlayProdDebug
```

أو يمكن تمرير الخصائص مباشرة:

```bash
./gradlew \
  -Pred.server.url=http://192.168.1.50:8080 \
  -Pred.dumin.enabled=false \
  :Signal-Android:assemblePlayProdDebug
```

## تفعيل Dumin لاحقًا

لا تفعل ذلك إلا بعد توفر Gateway محلي قابل للوصول. خدمة Asterisk لا تعمل افتراضيًا:

```bash
docker compose --profile dumin up -d pstn-gateway
```

ثم ابنِ التطبيق مع إعداد Dumin:

```properties
red.dumin.enabled=true
red.dumin.ip=192.168.1.100
red.dumin.gateway.url=http://192.168.1.100:5060
```

بدون Gateway سيبقى التطبيق مستقرًا، وسيتم إخفاء تبويبات PSTN/Dumin بدل عرض أخطاء اتصال مضللة.
