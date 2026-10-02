<#
.SYNOPSIS
    Starts all three AstroAI microservices and opens the web application.
#>

$RepoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "       ASTRO-AI PLATFORM - FULL STACK LAUNCHER" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

if (-not $env:JAVA_HOME -and (Test-Path 'C:\Program Files\Java\jdk-25')) {
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'
}

Write-Host "[1/3] Starting Python FastAPI Astrology Engine (Port 8000)..." -ForegroundColor Yellow
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd '$RepoRoot\astrology-engine'; .\.venv\Scripts\Activate.ps1; uvicorn api.main:app --port 8000 --host 0.0.0.0"

Write-Host "[2/3] Starting Spring Boot Core Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'; cd '$RepoRoot\backend'; .\mvnw.cmd spring-boot:run"

Write-Host "[3/3] Starting React Vite Frontend UI (Port 5173)..." -ForegroundColor Yellow
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd '$RepoRoot\frontend'; npm run dev"

Write-Host ""
Write-Host "Waiting 8 seconds for services to initialize..." -ForegroundColor Green
Start-Sleep -Seconds 8

Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " Platform is accessible at:" -ForegroundColor White
Write-Host "   - Frontend Dashboard:        http://localhost:5173" -ForegroundColor Green
Write-Host "   - Spring Boot Backend:       http://localhost:8080/api/health" -ForegroundColor Green
Write-Host "   - Python Calculation Engine: http://127.0.0.1:8000/api/v1/health" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
