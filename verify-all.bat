@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo [1/5] Checking Java, Node, Python and Docker...
if not defined JAVA_HOME if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
if not defined JAVA_HOME (
  echo ERROR: JDK 21 is required. Set JAVA_HOME.
  exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo ERROR: JAVA_HOME does not point to a valid JDK.
  exit /b 1
)
where node >nul 2>&1 || (echo ERROR: Node.js is required.& exit /b 1)
where python >nul 2>&1 || (echo ERROR: Python is required.& exit /b 1)
where docker >nul 2>&1 || (echo ERROR: Docker is required.& exit /b 1)
docker compose version >nul 2>&1 || (echo ERROR: Docker Compose v2 is required.& exit /b 1)

set JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -Duser.variant= -Dfile.encoding=UTF-8

java -version

echo [2/5] Building Android and backend...
call gradlew.bat buildAll --stacktrace
if errorlevel 1 exit /b 1

echo [3/5] Building admin dashboard...
pushd admin_dashboard
call npm ci --no-audit --no-fund
if errorlevel 1 (popd & exit /b 1)
call npm run build
if errorlevel 1 (popd & exit /b 1)
popd

echo [4/5] Validating Docker and repository checks...
docker compose config --quiet
if errorlevel 1 exit /b 1
python audit_check.py
if errorlevel 1 exit /b 1
python api_contract_test.py
if errorlevel 1 exit /b 1
python integration_test.py
if errorlevel 1 exit /b 1

echo [5/5] Starting local stack...
docker compose up -d --build
if errorlevel 1 exit /b 1

echo All RED checks completed. Verify http://127.0.0.1:8080/actuator/health
endlocal
