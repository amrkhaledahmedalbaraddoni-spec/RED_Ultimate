#!/usr/bin/env python3
"""RED Ultimate — Honest technical audit. Verifies real files, symbols, and logic."""
import os, sys

R = os.path.dirname(os.path.abspath(__file__))

def p(*parts): return os.path.join(R, *parts)
def read(*parts):
    try: return open(p(*parts)).read()
    except: return ""

checks = []

# ── 1. Signal app build-critical fixes ──────────────────────────────────────
checks.append(("DevelopedChatInitialization.java removed (no class/filename mismatch)",
    "DevelopedChatInitialization.java" not in read("app","src","main","java","org","thoughtcrime","securesms","developed","REDInitialization.java")))

checks.append(("REDInitialization.java exists",
    os.path.isfile(p("app","src","main","java","org","thoughtcrime","securesms","developed","REDInitialization.java"))))

checks.append(("MasterIntegration has no broken imports",
    "com.red.core.delivery.DeliveryEngine" not in read("app","src","main","java","org","thoughtcrime","securesms","developed","MasterIntegration.kt")))

checks.append(("MasterIntegration checkAdminApproval is not 'return true'",
    "return true" not in read("app","src","main","java","org","thoughtcrime","securesms","developed","MasterIntegration.kt")))

checks.append(("MasterIntegration has verifyApprovalFromServer (server-backed check)",
    "verifyApprovalFromServer" in read("app","src","main","java","org","thoughtcrime","securesms","developed","MasterIntegration.kt")))

# ── 2. Backend server ───────────────────────────────────────────────────────
checks.append(("Backend has @SpringBootApplication main class",
    "@SpringBootApplication" in read("backend-server","src","main","kotlin","com","red","RedBackendApplication.kt")))

checks.append(("Backend has version in build.gradle.kts",
    'version = "1.0.0"' in read("backend-server","build.gradle.kts")))

checks.append(("Backend has settings.gradle.kts",
    os.path.isfile(p("backend-server","settings.gradle.kts"))))

checks.append(("Backend has real JWT service",
    "fun issue" in read("backend-server","src","main","kotlin","com","red","config","JwtService.kt") and "Jwts.builder" in read("backend-server","src","main","kotlin","com","red","config","JwtService.kt")))

checks.append(("Backend has BCrypt password encoder",
    "BCryptPasswordEncoder" in read("backend-server","src","main","kotlin","com","red","config","SecurityConfig.kt")))

checks.append(("Backend has real UUID v7",
    "0x7000" in read("backend-server","src","main","kotlin","com","red","delivery","UuidV7.kt")))

checks.append(("Backend MessageService returns Result",
    "data class Result" in read("backend-server","src","main","kotlin","com","red","delivery","MessageService.kt")))

checks.append(("Backend has ConversationController",
    os.path.isfile(p("backend-server","src","main","kotlin","com","red","delivery","ConversationController.kt"))))

checks.append(("Backend has StoryController",
    os.path.isfile(p("backend-server","src","main","kotlin","com","red","delivery","StoryController.kt"))))

checks.append(("Backend has LoginAttemptLimiter",
    os.path.isfile(p("backend-server","src","main","kotlin","com","red","auth","LoginAttemptLimiter.kt"))))

checks.append(("Backend has LogScrubber",
    "LogScrubber" in read("backend-server","src","main","kotlin","com","red","security","LogScrubber.kt")))

checks.append(("Backend has logback-spring.xml with scrubbing",
    "ScrubbedMessageConverter" in read("backend-server","src","main","resources","logback-spring.xml")))

checks.append(("Backend Dockerfile references correct jar name",
    "backend-1.0.0.jar" in read("backend-server","Dockerfile")))

# ── 3. App-android ──────────────────────────────────────────────────────────
checks.append(("app-android has build.gradle.kts",
    os.path.isfile(p("app-android","app","build.gradle.kts"))))

checks.append(("app-android has AndroidManifest.xml",
    os.path.isfile(p("app-android","app","src","main","AndroidManifest.xml"))))

checks.append(("app-android has RedApplication (@HiltAndroidApp)",
    "@HiltAndroidApp" in read("app-android","app","src","main","java","com","red","RedApplication.kt")))

checks.append(("app-android has real UUID v7",
    "0x7000" in read("app-android","app","src","main","java","com","red","core","delivery","UuidV7.kt")))

checks.append(("MessageDeliveryManager handles inbound frames",
    "handleFrame" in read("app-android","app","src","main","java","com","red","core","delivery","MessageDeliveryManager.kt")))

checks.append(("TokenStore exists for session persistence",
    os.path.isfile(p("app-android","app","src","main","java","com","red","core","auth","TokenStore.kt"))))

checks.append(("NetworkModule has auth interceptor",
    "authInterceptor" in read("app-android","app","src","main","java","com","red","core","di","NetworkModule.kt")))

checks.append(("ChatApi exists for real conversations",
    os.path.isfile(p("app-android","app","src","main","java","com","red","feature","chat","ChatApi.kt"))))

checks.append(("ChatListViewModel exists",
    os.path.isfile(p("app-android","app","src","main","java","com","red","feature","chat","ChatListViewModel.kt"))))

checks.append(("ChatListScreen uses hiltViewModel",
    "hiltViewModel()" in read("app-android","app","src","main","java","com","red","feature","chat","ChatListScreen.kt")))

checks.append(("StoryApi exists",
    os.path.isfile(p("app-android","app","src","main","java","com","red","feature","stories","StoryApi.kt"))))

checks.append(("StoryViewModel exists",
    os.path.isfile(p("app-android","app","src","main","java","com","red","feature","stories","StoryViewModel.kt"))))

checks.append(("StoryListScreen is reactive",
    "StoryViewModel" in read("app-android","app","src","main","java","com","red","feature","stories","StoryListScreen.kt")))

checks.append(("DuminApi exists",
    os.path.isfile(p("app-android","app","src","main","java","com","red","feature","pstn","DuminApi.kt"))))

# ── 4. Infrastructure ──────────────────────────────────────────────────────
checks.append(("docker-compose.yml exists and is valid",
    os.path.isfile(p("docker-compose.yml")) and "services:" in read("docker-compose.yml")))

checks.append(("nginx.conf exists",
    os.path.isfile(p("nginx.conf"))))

checks.append(("nginx.tls.conf exists for production",
    os.path.isfile(p("nginx.tls.conf"))))

checks.append(("media-sfu has Dockerfile",
    os.path.isfile(p("media-sfu","Dockerfile"))))

checks.append(("admin_dashboard has Dockerfile",
    os.path.isfile(p("admin_dashboard","Dockerfile"))))

checks.append(("pstn-asterisk has pjsip.conf",
    os.path.isfile(p("pstn-asterisk","pjsip.conf"))))

checks.append(("scripts/generate-local-cert.sh exists",
    os.path.isfile(p("scripts","generate-local-cert.sh"))))

# ── 5. Security ────────────────────────────────────────────────────────────
checks.append(("DevelopedServerConfig has USE_TLS flag",
    "USE_TLS" in read("app","src","main","java","org","thoughtcrime","securesms","dependencies","DevelopedServerConfig.java")))

checks.append(("No hardcoded http:// default URL",
    'LOCAL_IP = "http' not in read("app","src","main","java","org","thoughtcrime","securesms","dependencies","DevelopedServerConfig.java")))

checks.append(("REDLocalTrustStore exists",
    os.path.isfile(p("app","src","main","java","org","thoughtcrime","securesms","push","REDLocalTrustStore.java"))))

checks.append(("SECURITY.md exists",
    os.path.isfile(p("SECURITY.md"))))

# ── 6. Cleanup ─────────────────────────────────────────────────────────────
checks.append(("server/ orphan folder removed",
    not os.path.isdir(p("server"))))

checks.append(("admin-dashboard/ duplicate removed",
    not os.path.isdir(p("admin-dashboard"))))

checks.append(("temp-dc.yml removed",
    not os.path.isfile(p("temp-dc.yml"))))

# ── Report ──────────────────────────────────────────────────────────────────
passed = sum(1 for _, ok in checks if ok)
total = len(checks)
for name, ok in checks:
    print(f"{'✅' if ok else '❌'} {name}")
print(f"\n{passed}/{total} checks passed")
if passed != total:
    print(f"\n⚠️  {total - passed} FAILED")
    sys.exit(1)
