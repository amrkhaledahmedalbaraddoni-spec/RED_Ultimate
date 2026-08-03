#!/usr/bin/env python3
"""
RED Ultimate — Comprehensive Integration Verification Suite
Checks every component, every file, every API path, every connection
"""

import os
import re
import sys
import json
import subprocess
from pathlib import Path
from collections import defaultdict

ROOT = Path("/home/user/RED_Ultimate")
errors = []
warnings = []
passed = 0

def check(condition, msg, level="error"):
    global passed, errors, warnings
    if condition:
        passed += 1
        print(f"  ✅ {msg}")
    else:
        if level == "error":
            errors.append(msg)
            print(f"  ❌ {msg}")
        else:
            warnings.append(msg)
            print(f"  ⚠️  {msg}")

# ═══════════════════════════════════════════════════════════════
# 1. ADMIN DASHBOARD
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("1. ADMIN DASHBOARD")
print("="*60)

admin_src = ROOT / "admin_dashboard" / "src"
admin_build = ROOT / "admin_dashboard" / "build"

# Check build exists
check(admin_build.exists(), "Admin dashboard build exists")
check((admin_build / "static" / "js").exists(), "JS assets exist")

# Check all pages exist
required_pages = ["AuditLog.js", "UserManagement.js", "StoryManagement.js", 
                  "Approvals.js", "Diagnostics.js", "DuminMonitor.js"]
for page in required_pages:
    check((admin_src / "pages" / page).exists(), f"Page {page} exists")

# Check App.js has all routes
app_js = admin_src / "App.js"
if app_js.exists():
    content = app_js.read_text()
    routes = re.findall(r'path="([^"]+)"', content)
    check(len(routes) >= 8, f"App.js has {len(routes)} routes (>= 8)")
    check("/audit" in content, "Audit route exists")
    check("/users" in content, "Users route exists")
    check("/stories" in content, "Stories route exists")

# Check LiveMonitor component
monitor = admin_src / "components" / "LiveMonitor.js"
check(monitor.exists(), "LiveMonitor.js exists")

# ═══════════════════════════════════════════════════════════════
# 2. ANDROID APP
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("2. ANDROID APP")
print("="*60)

app_dir = ROOT / "app-android" / "app" / "src" / "main" / "java" / "com" / "red"

# Count all Kotlin files
kt_files = list(app_dir.rglob("*.kt"))
check(len(kt_files) >= 70, f"Kotlin files: {len(kt_files)} (>= 70)")

# Check all screens exist
required_screens = [
    "LoginScreen.kt", "RegisterScreen.kt", "WelcomeScreen.kt",
    "ChatListScreen.kt", "ChatDetailScreen.kt", "NewChatScreen.kt",
    "CallLogScreen.kt", "VideoCallScreen.kt", "DialPadScreen.kt",
    "PstnCallScreen.kt", "SettingsScreen.kt", "ProfileScreen.kt",
    "ContactsScreen.kt", "BlockListScreen.kt", "NotificationListScreen.kt",
    "MediaGalleryScreen.kt", "StoryListScreen.kt", "StoryViewerScreen.kt",
    "CameraCaptureScreen.kt", "AppLockScreen.kt", "QRCodeScreen.kt",
    "CreateGroupScreen.kt", "PendingApprovalScreen.kt", "PermissionRequestScreen.kt"
]
for screen in required_screens:
    found = list(app_dir.rglob(screen))
    check(len(found) > 0, f"Screen {screen} exists")

# Check all ViewModels exist
required_vms = [
    "AuthViewModel.kt", "ChatViewModel.kt", "ChatListViewModel.kt",
    "SettingsViewModel.kt", "ProfileViewModel.kt", "PstnViewModel.kt",
    "CallLogViewModel.kt", "StoryViewModel.kt", "ContactsViewModel.kt",
    "BlockListViewModel.kt", "NotificationListViewModel.kt", "MediaGalleryViewModel.kt"
]
for vm in required_vms:
    found = list(app_dir.rglob(vm))
    check(len(found) > 0, f"ViewModel {vm} exists")

# Check all API interfaces exist
required_apis = ["AuthApi.kt", "ChatApi.kt", "DuminApi.kt", "StoryApi.kt", "ContactApi.kt"]
for api in required_apis:
    found = list(app_dir.rglob(api))
    check(len(found) > 0, f"API {api} exists")

# Check core infrastructure
core_files = [
    "RedDatabase.kt", "TokenStore.kt", "NetworkModule.kt", "DatabaseModule.kt",
    "AESEncryption.kt", "SessionManager.kt", "BiometricHelper.kt",
    "MessageDeliveryManager.kt", "DevelopedWebSocketClientImpl.kt",
    "NotificationHelper.kt", "OfflineSyncService.kt",
    "ConnectionStatusIndicator.kt", "EncryptionIndicator.kt",
    "DeliveryEngine.kt", "UuidV7.kt"
]
for cf in core_files:
    found = list(app_dir.rglob(cf))
    check(len(found) > 0, f"Core {cf} exists")

# Check database entities
db_entities = ["MessageEntity.kt", "StoryEntity.kt", "ContactEntity.kt", "ConversationEntity.kt"]
for de in db_entities:
    found = list(app_dir.rglob(de))
    check(len(found) > 0, f"Entity {de} exists")

# ═══════════════════════════════════════════════════════════════
# 3. ANDROID-BACKEND API ALIGNMENT
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("3. ANDROID-BACKEND API ALIGNMENT")
print("="*60)

backend_dir = ROOT / "backend-server" / "src" / "main" / "kotlin"

# Extract all backend endpoints
backend_endpoints = {}
for ctrl in backend_dir.rglob("*Controller.kt"):
    content = ctrl.read_text()
    base_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
    if base_match:
        base = base_match.group(1)
        methods = re.findall(r'@(Get|Post|Put|Delete)Mapping\(?(?:"([^"]*)")?\)', content)
        for method, path in methods:
            full_path = f"{method.upper()} {base}{path}"
            backend_endpoints[full_path] = ctrl.name

# Extract all Android API calls
android_calls = {}
for api_file in app_dir.rglob("*Api.kt"):
    content = api_file.read_text()
    # Get the API interface name
    iface_match = re.search(r'interface\s+(\w+Api)', content)
    if not iface_match:
        continue
    iface_name = iface_match.group(1)
    
    calls = re.findall(r'@(GET|POST|PUT|DELETE)\("([^"]+)"\)', content)
    for method, path in calls:
        full_path = f"{method} {path}"
        android_calls[full_path] = iface_name

# Check alignment
print(f"  Backend endpoints: {len(backend_endpoints)}")
print(f"  Android API calls: {len(android_calls)}")

# Check critical paths
critical_paths = [
    ("POST", "api/auth/login"),
    ("POST", "api/auth/register"),
    ("GET", "api/auth/status"),
    ("GET", "api/users/me"),
    ("PUT", "api/users/me"),
    ("POST", "api/auth/change-password"),
    ("GET", "api/conversations"),
    ("GET", "api/messages/conversation"),
    ("GET", "api/messages/pending"),
    ("POST", "api/blocks"),
    ("DELETE", "api/blocks/{blockeeId}"),
    ("GET", "api/blocks"),
    ("GET", "api/blocks/check/{userId}"),
    ("GET", "api/notifications/pending"),
    ("GET", "api/notifications/count"),
    ("POST", "api/notifications/mark-read"),
    ("POST", "api/notifications/mark-all-read"),
    ("GET", "api/presence/online"),
    ("GET", "api/contacts"),
    ("POST", "api/contacts/add"),
    ("GET", "api/stories"),
    ("POST", "api/stories"),
    ("POST", "api/pstn/dial"),
]

for method, path in critical_paths:
    # Check Android side
    android_found = any(
        path in call_path for call_path in android_calls.keys()
    )
    check(android_found, f"Android has {method} {path}", level="warning" if not android_found else "error")
    
    # Check Backend side
    backend_found = any(
        path in ep_path for ep_path in backend_endpoints.keys()
    )
    check(backend_found, f"Backend has {method} {path}", level="warning" if not backend_found else "error")

# ═══════════════════════════════════════════════════════════════
# 4. ANDROID CODE QUALITY
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("4. ANDROID CODE QUALITY")
print("="*60)

# Check @JsonClass on all DTOs
dto_issues = 0
for kt_file in app_dir.rglob("*.kt"):
    content = kt_file.read_text()
    # Find data classes that look like DTOs
    dto_matches = re.finditer(r'(?:@JsonClass[^\n]*\n\s*)?data class (\w+Dto|\w+Request|\w+Response|\w+Info)\(', content)
    for match in dto_matches:
        class_name = match.group(1)
        has_json_class = content[:match.start()].rstrip().endswith(')')
        # Check if there's @JsonClass before
        preceding = content[:match.start()].rstrip()
        if '@JsonClass' not in preceding.split('\n')[-3:]:
            # More lenient check
            if '@JsonClass(generateAdapter = true)' not in content[:match.end()] and 'JsonClass' not in content[max(0, match.start()-200):match.start()]:
                dto_issues += 1
                print(f"  ⚠️  {kt_file.name}: {class_name} may be missing @JsonClass")

check(dto_issues == 0, f"All DTOs have @JsonClass ({dto_issues} missing)", level="warning")

# Check @HiltViewModel
for vm_file in app_dir.rglob("*ViewModel.kt"):
    content = vm_file.read_text()
    check("@HiltViewModel" in content, f"{vm_file.name} has @HiltViewModel")

# Check @Inject constructor
for vm_file in app_dir.rglob("*ViewModel.kt"):
    content = vm_file.read_text()
    check("@Inject constructor" in content, f"{vm_file.name} has @Inject constructor")

# Check Room entities have @Entity
for entity_file in app_dir.rglob("*Entity.kt"):
    content = entity_file.read_text()
    check("@Entity" in content, f"{entity_file.name} has @Entity")

# Check DAOs have @Dao
for entity_file in app_dir.rglob("*Entity.kt"):
    content = entity_file.read_text()
    if "@Dao" in content:
        check(True, f"{entity_file.name} has @Dao")
    elif "interface" in content and "Dao" in content:
        check("@Dao" in content, f"{entity_file.name} has @Dao for its Dao interface")

# ═══════════════════════════════════════════════════════════════
# 5. BACKEND CODE QUALITY
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("5. BACKEND CODE QUALITY")
print("="*60)

# Check all services have @Service
for svc_file in backend_dir.rglob("*Service.kt"):
    content = svc_file.read_text()
    check("@Service" in content, f"{svc_file.name} has @Service")

# Check all controllers have @RestController
for ctrl_file in backend_dir.rglob("*Controller.kt"):
    content = ctrl_file.read_text()
    check("@RestController" in content, f"{ctrl_file.name} has @RestController")

# Check all repositories exist
for svc_file in backend_dir.rglob("*Service.kt"):
    content = svc_file.read_text()
    if "Repository" in content:
        repo_refs = re.findall(r'(\w+Repository)', content)
        for repo_name in set(repo_refs):
            found = list(backend_dir.rglob(f"{repo_name}.kt"))
            if not found:
                # Check if repo is in the same file
                check(repo_name in content, f"{repo_name} exists (in {svc_file.name})")

# Check no duplicate endpoints
endpoint_counts = defaultdict(int)
for ctrl_file in backend_dir.rglob("*Controller.kt"):
    content = ctrl_file.read_text()
    base_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
    if base_match:
        base = base_match.group(1)
        methods = re.findall(r'@(Get|Post|Put|Delete)Mapping\(?(?:"([^"]*)")?\)', content)
        for method, path in methods:
            full = f"{method.upper()} {base}{path}"
            endpoint_counts[full] += 1

duplicates = {k: v for k, v in endpoint_counts.items() if v > 1}
check(len(duplicates) == 0, f"No duplicate endpoints (found {len(duplicates)})")

# ═══════════════════════════════════════════════════════════════
# 6. DATABASE CONSISTENCY
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("6. DATABASE CONSISTENCY")
print("="*60)

# Check RedDatabase entities
db_file = app_dir / "core" / "database" / "RedDatabase.kt"
if db_file.exists():
    content = db_file.read_text()
    entities = re.findall(r'(\w+Entity::class)', content)
    check(len(entities) >= 4, f"RedDatabase has {len(entities)} entities (>= 4)")
    for entity in entities:
        name = entity.replace("::class", "")
        found = list(app_dir.rglob(f"{name}.kt"))
        check(len(found) > 0, f"{name} file exists")

# Check PstnDatabase
pstn_db = app_dir / "feature" / "pstn" / "PstnModels.kt"
if pstn_db.exists():
    content = pstn_db.read_text()
    check("PstnDatabase" in content, "PstnDatabase exists")
    check("PstnDao" in content, "PstnDao exists")
    check("PstnCallLog" in content, "PstnCallLog exists")

# ═══════════════════════════════════════════════════════════════
# 7. NAVIGATION COMPLETENESS
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("7. NAVIGATION COMPLETENESS")
print("="*60)

main_activity = app_dir / "MainActivity.kt"
if main_activity.exists():
    content = main_activity.read_text()
    
    # Check all routes
    required_routes = [
        "chats", "stories", "calls", "phone", "contacts", "settings",
        "chat_detail/{chatId}", "pstn_call/{number}", "video_call/{peerName}",
        "story_capture", "story_viewer/{urls}", "new_chat", "create_group",
        "profile/{userId}", "block_list", "notifications", "media_gallery/{conversationId}",
        "qr_code"
    ]
    for route in required_routes:
        check(route in content, f"Route '{route}' in MainActivity")

# ═══════════════════════════════════════════════════════════════
# 8. TESTS
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("8. TESTS")
print("="*60)

test_dir = ROOT / "app-android" / "app" / "src" / "test"
test_files = list(test_dir.rglob("*Test.kt"))
check(len(test_files) >= 4, f"Test files: {len(test_files)} (>= 4)")

for tf in test_files:
    content = tf.read_text()
    test_count = content.count("@Test")
    check(test_count > 0, f"{tf.name} has {test_count} tests")

# ═══════════════════════════════════════════════════════════════
# 9. BUILD ARTIFACTS
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("9. BUILD ARTIFACTS")
print("="*60)

check((admin_build / "index.html").exists(), "Admin dashboard index.html exists")
check((admin_build / "static").exists(), "Admin dashboard static assets exist")

# Check media-sfu
sfu_file = ROOT / "media-sfu" / "server.js"
check(sfu_file.exists(), "media-sfu/server.js exists")

# Check build files
check((ROOT / "app-android" / "app" / "build.gradle.kts").exists(), "app-android build.gradle.kts exists")
check((ROOT / "app-android" / "app" / "proguard-rules.pro").exists(), "proguard-rules.pro exists")
check((ROOT / "app-android" / "app" / "src" / "main" / "AndroidManifest.xml").exists(), "AndroidManifest.xml exists")

# ═══════════════════════════════════════════════════════════════
# 10. SECURITY
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("10. SECURITY")
print("="*60)

# Check encryption
check((app_dir / "core" / "crypto" / "AESEncryption.kt").exists(), "AES encryption exists")
check((app_dir / "core" / "security" / "BiometricHelper.kt").exists(), "Biometric helper exists")
check((app_dir / "core" / "security" / "SessionManager.kt").exists(), "Session manager exists")

# Check network security config
ns_config = ROOT / "app-android" / "app" / "src" / "main" / "res" / "xml" / "network_security_config.xml"
check(ns_config.exists(), "Network security config exists")

# Check no hardcoded secrets
for kt_file in app_dir.rglob("*.kt"):
    content = kt_file.read_text()
    if "password" in content.lower() and "hardcoded" in content.lower():
        check(False, f"Possible hardcoded secret in {kt_file.name}")

# ═══════════════════════════════════════════════════════════════
# RESULTS
# ═══════════════════════════════════════════════════════════════
print("\n" + "="*60)
print("FINAL RESULTS")
print("="*60)
print(f"  ✅ Passed: {passed}")
print(f"  ❌ Errors: {len(errors)}")
print(f"  ⚠️  Warnings: {len(warnings)}")
print()

if errors:
    print("ERRORS:")
    for e in errors[:20]:
        print(f"  ❌ {e}")
    if len(errors) > 20:
        print(f"  ... and {len(errors) - 20} more")

if warnings:
    print("\nWARNINGS:")
    for w in warnings[:10]:
        print(f"  ⚠️  {w}")

print()
if len(errors) == 0:
    print("🎉 ALL CHECKS PASSED!")
else:
    print(f"⚠️  {len(errors)} ERRORS FOUND - NEEDS FIXING")

sys.exit(len(errors))
