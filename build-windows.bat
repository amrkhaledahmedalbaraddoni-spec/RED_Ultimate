@echo off
REM ============================================================
REM  RED Ultimate — Windows Build Script
REM  Fixes Arabic locale issues on Windows (Arabic-Indic digits
REM  in resource directory names, stale Wire code, dependency
REM  verification).
REM ============================================================

echo.
echo  ========================================
echo   RED Ultimate - Windows Build Script
echo  ========================================
echo.

REM Force English locale for the JVM (prevents Arabic-Indic digits
REM like ٣٦٠ in resource directory names like values-sw360dp)
set JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -Duser.variant= -Dfile.encoding=UTF-8

echo [1/3] Setting locale to English (prevents Arabic-Indic digit issues)...
echo       JAVA_TOOL_OPTIONS=%JAVA_TOOL_OPTIONS%

echo.
echo [2/3] Running clean build (removes stale generated code)...
call gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Clean failed. Please check the output above.
    pause
    exit /b 1
)

echo.
echo [3/3] Building RED Ultimate (PlayProdDebug)...
call gradlew.bat assemblePlayProdDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo  BUILD FAILED
    echo ========================================
    echo.
    echo Common fixes:
    echo   1. Make sure JAVA_TOOL_OPTIONS is set (see above)
    echo   2. Run 'gradlew clean' before building
    echo   3. Check verification-metadata.xml for aapt2 version
    echo   4. If DeviceName.kt has errors, run clean again
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  BUILD SUCCESSFUL
echo ========================================
echo.
echo APK location: app\build\outputs\apk\playProd\debug\
echo.
pause
