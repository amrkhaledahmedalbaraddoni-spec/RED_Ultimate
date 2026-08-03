@echo off
REM ── RED Ultimate — Build script for Windows with Arabic locale fix ──
REM
REM This script sets the Java locale to English before building,
REM which prevents Android resource directory names from using
REM Arabic-Indic digits (e.g. values-sw٣٦٠dp → values-sw360dp).
REM
REM Usage:  build-windows.bat            (debug build)
REM         build-windows.bat release    (release build)

setlocal

REM Force English locale for all Java processes
set JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US

echo ========================================
echo  RED Ultimate - Windows Build
echo  Locale: %JAVA_TOOL_OPTIONS%
echo ========================================
echo.

REM Clean previous build artifacts (fixes stale generated code)
echo [1/3] Cleaning previous build...
call gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Clean failed. Please check the output above.
    pause
    exit /b 1
)

echo.
echo [2/3] Building project...
if "%1"=="release" (
    call gradlew.bat assemblePlayProdRelease
) else (
    call gradlew.bat assemblePlayProdDebug
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo  BUILD FAILED
    echo.
    echo  If you see these errors:
    echo.
    echo  1. "values-swXXXdp" with Arabic numbers:
    echo     - Run this script (build-windows.bat) instead of
    echo       running Gradle directly from Android Studio.
    echo     - Or set JAVA_TOOL_OPTIONS in Windows:
    echo       setx JAVA_TOOL_OPTIONS "-Duser.language=en -Duser.country=US"
    echo.
    echo  2. "DeviceName.kt syntax error":
    echo     - Run: gradlew clean
    echo     - Then rebuild from Android Studio.
    echo.
    echo  3. "Dependency verification failed":
    echo     - Run: gradlew --write-verification-metadata sha256
    echo     - Or temporarily disable in gradle.properties.
    echo ========================================
    pause
    exit /b 1
)

echo.
echo [3/3] Build complete!
echo.
echo APK location:
if "%1"=="release" (
    echo   app\build\outputs\apk\playProd\release\app-play-prod-release.apk
) else (
    echo   app\build\outputs\apk\playProd\debug\app-play-prod-debug.apk
)
echo.
pause
endlocal
