@echo off
title AstroAI Platform Launcher
color 0b
echo ========================================================
echo        ASTRO-AI PLATFORM - FULL STACK LAUNCHER
echo ========================================================
echo.

set REPO_DIR=%~dp0
cd /d "%REPO_DIR%"

if "%JAVA_HOME%"=="" if exist "C:\Program Files\Java\jdk-25" set "JAVA_HOME=C:\Program Files\Java\jdk-25"

echo [1/3] Starting Python FastAPI Astrology Engine (Port 8000)...
start "AstroAI - Python Swiss Ephemeris Engine (Port 8000)" cmd /k "cd /d "%REPO_DIR%astrology-engine" && .\.venv\Scripts\activate.bat && uvicorn api.main:app --port 8000 --host 0.0.0.0"

echo [2/3] Starting Spring Boot Core Backend (Port 8080)...
start "AstroAI - Spring Boot Backend (Port 8080)" cmd /k "cd /d "%REPO_DIR%backend" && .\mvnw.cmd spring-boot:run"

echo [3/3] Starting React Vite Frontend UI (Port 5173)...
start "AstroAI - React Vite UI (Port 5173)" cmd /k "cd /d "%REPO_DIR%frontend" && npm run dev"

echo.
echo All 3 microservices are starting up in separate terminal windows!
echo Waiting 8 seconds before opening browser...
timeout /t 8 /nobreak > nul

start http://localhost:5173

echo.
echo ========================================================
echo  Platform is accessible at:
echo    - Frontend Dashboard:        http://localhost:5173
echo    - Spring Boot Backend:       http://localhost:8080/api/health
echo    - Python Calculation Engine: http://127.0.0.1:8000/api/v1/health
echo ========================================================
echo.
pause
