# ==============================================================================
# Astro-AI Multi-Service Health Check Script (health-check.ps1)
# Probes each service layer across the deployment stack using PowerShell.
# ==============================================================================

[CmdletBinding()]
param(
    [string]$GatewayUrl = "http://localhost",
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AstroUrl = "http://localhost:8000"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Running Astro-AI Full-Stack Health Audit" -ForegroundColor Cyan
Write-Host " Gateway: $GatewayUrl" -ForegroundColor Gray
Write-Host " Backend: $BackendUrl" -ForegroundColor Gray
Write-Host " Astrology Engine: $AstroUrl" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan

$failed = 0

# 1. Astrology Calculation Engine
Write-Host -NoNewline "[1/3] Testing Python Astrology Engine (/health)... "
try {
    $resp = Invoke-RestMethod -Uri "$AstroUrl/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
    if ($resp.status -eq "ok") {
        Write-Host "OK (status: ok)" -ForegroundColor Green
    } else {
        Write-Host "OK" -ForegroundColor Green
    }
} catch {
    Write-Host "FAILED ($($_.Exception.Message))" -ForegroundColor Red
    $failed++
}

# 2. Spring Boot Core Backend
Write-Host -NoNewline "[2/3] Testing Spring Boot Backend (/actuator/health)... "
$backendOk = $false
try {
    $resp = Invoke-RestMethod -Uri "$BackendUrl/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
    if ($resp.status -eq "UP") {
        Write-Host "OK (status: UP)" -ForegroundColor Green
        $backendOk = $true
    }
} catch {
    # Try through gateway
    try {
        $resp = Invoke-RestMethod -Uri "$GatewayUrl/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
        if ($resp.status -eq "UP") {
            Write-Host "OK via Gateway (status: UP)" -ForegroundColor Green
            $backendOk = $true
        }
    } catch {
        # Fall through
    }
}

if (-not $backendOk) {
    Write-Host "FAILED (Backend health check did not return UP)" -ForegroundColor Red
    $failed++
}

# 3. Frontend Web Gateway
Write-Host -NoNewline "[3/3] Testing Frontend Web Gateway (/)... "
try {
    $resp = Invoke-WebRequest -Uri "$GatewayUrl/" -Method Get -TimeoutSec 5 -ErrorAction Stop
    if ($resp.StatusCode -eq 200) {
        Write-Host "OK (HTTP 200)" -ForegroundColor Green
    } else {
        Write-Host "FAILED (HTTP $($resp.StatusCode))" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "FAILED ($($_.Exception.Message))" -ForegroundColor Red
    $failed++
}

Write-Host "==========================================================" -ForegroundColor Cyan
if ($failed -eq 0) {
    Write-Host " All Astro-AI Services are Healthy and Operational! " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host " Detected $failed failing service(s). Please inspect logs." -ForegroundColor Red
    Write-Host "==========================================================" -ForegroundColor Cyan
    exit 1
}
