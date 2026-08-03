#!/usr/bin/env python3
"""
RED Ultimate — API Contract Verification
Verifies that every Android API call matches a backend endpoint exactly
"""

import re
import sys
from pathlib import Path
from collections import defaultdict

ROOT = Path("/home/user/RED_Ultimate")
# RED APIs are compiled as part of the single root :app module.
app_dir = ROOT / "app" / "src" / "main" / "java" / "com" / "red"
backend_dir = ROOT / "backend-server" / "src" / "main" / "kotlin"

print("="*60)
print("API CONTRACT VERIFICATION")
print("="*60)

# Extract all backend endpoints
backend_endpoints = {}
for ctrl in backend_dir.rglob("*Controller.kt"):
    content = ctrl.read_text()
    base_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
    if not base_match:
        continue
    base = base_match.group(1)
    ctrl_name = ctrl.stem
    
    # Extract individual method mappings
    for match in re.finditer(r'@(Get|Post|Put|Delete|Patch)Mapping(?:\("([^"]*)"\)|\(\))?', content):
        method = match.group(1).upper()
        path = match.group(2) or ""
        full_path = f"{base}{path}"
        # Normalize path variables
        normalized = re.sub(r'\{[^}]+\}', '{param}', full_path)
        key = f"{method} {normalized.lstrip('/')}"
        backend_endpoints[key] = ctrl_name

print(f"\nBackend endpoints found: {len(backend_endpoints)}")
for key, ctrl in sorted(backend_endpoints.items()):
    print(f"  {key}  [{ctrl}]")

# Extract all Android API calls
android_calls = {}
for api_file in app_dir.rglob("*Api.kt"):
    content = api_file.read_text()
    iface_match = re.search(r'interface\s+(\w+)', content)
    if not iface_match:
        continue
    iface_name = iface_match.group(1)
    
    for match in re.finditer(r'@(GET|POST|PUT|DELETE|PATCH)\("([^"]+)"\)', content):
        method = match.group(1)
        path = match.group(2)
        # Normalize path variables
        normalized = re.sub(r'\{[^}]+\}', '{param}', path)
        key = f"{method} {normalized.lstrip('/')}"
        android_calls[key] = iface_name

print(f"\nAndroid API calls found: {len(android_calls)}")
for key, api in sorted(android_calls.items()):
    print(f"  {key}  [{api}]")

# Verify alignment
print("\n" + "="*60)
print("VERIFICATION RESULTS")
print("="*60)

errors = 0
matched = 0

for key, api_name in sorted(android_calls.items()):
    if key in backend_endpoints:
        matched += 1
        print(f"  ✅ {key}  [{api_name} → {backend_endpoints[key]}]")
    else:
        # Try partial match
        method, path = key.split(" ", 1)
        found_partial = False
        for bk, bv in backend_endpoints.items():
            bm, bp = bk.split(" ", 1)
            if bm == method and bp == path:
                found_partial = True
                break
        
        if found_partial:
            matched += 1
            print(f"  ✅ {key}  [{api_name} → matched]")
        else:
            errors += 1
            print(f"  ❌ {key}  [{api_name} → NO BACKEND MATCH]")

print(f"\nMatched: {matched}/{len(android_calls)}")
print(f"Errors: {errors}")

# Check for backend endpoints not called by Android
print("\n" + "="*60)
print("BACKEND ENDPOINTS NOT CALLED BY ANDROID")
print("="*60)

for key, ctrl in sorted(backend_endpoints.items()):
    if key not in android_calls:
        method, path = key.split(" ", 1)
        found = False
        for ak, av in android_calls.items():
            am, ap = ak.split(" ", 1)
            if am == method and ap == path:
                found = True
                break
        if not found:
            print(f"  ℹ️  {key}  [{ctrl}] (not called by Android)")

sys.exit(errors)
