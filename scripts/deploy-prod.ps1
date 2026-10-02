# ==============================================================================
# Astro-AI Production Deployment Automation Script (deploy-prod.ps1)
# PowerShell script for pre-flight checks, building, launching, and health checks.
# ==============================================================================

[CmdletBinding()]
param(
    [string]$EnvFile = ".env.production"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Starting Astro-AI Production Deployment Pipeline (PowerShell)" -ForegroundColor Cyan
Write-Host " Working Directory: $RootDir" -ForegroundColor Gray
Write-Host " Timestamp: $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ssZ')" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan

Set-Location $RootDir

# 1. Environment Verification
$TargetEnv = Join-Path $RootDir $EnvFile
if (-not (Test-Path $TargetEnv)) {
    if (Test-Path (Join-Path $RootDir ".env")) {
        Write-Host "[INFO] $EnvFile not found, falling back to .env" -ForegroundColor Yellow
        $TargetEnv = Join-Path $RootDir ".env"
    } else {
        Write-Error "Neither $EnvFile nor .env exists. Copy .env.production.example to .env.production and configure."
        exit 1
    }
}
Write-Host "[INFO] Using environment file: $TargetEnv" -ForegroundColor Green

# 2. Secret Integrity Checks
$envContent = Get-Content $TargetEnv -Raw
if ($envContent -match "REPLACE_WITH" -or $envContent -match "change_me_in_production") {
    Write-Warning "Found placeholder credentials in $TargetEnv! Replace with cryptographically strong secrets."
}

# 3. Docker Compose Command Resolution
$composeCmd = "docker"
$composeArgs = @("compose")

try {
    & docker compose version | Out-Null
} catch {
    $composeCmd = "docker-compose"
    $composeArgs = @()
}

# 4. Build Containers
Write-Host "[INFO] Building production containers..." -ForegroundColor Cyan
& $composeCmd @composeArgs --env-file $TargetEnv -f docker-compose.prod.yml build --pull
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# 5. Launch Stack
Write-Host "[INFO] Launching production stack..." -ForegroundColor Cyan
& $composeCmd @composeArgs --env-file $TargetEnv -f docker-compose.prod.yml up -d --remove-orphans
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker compose up failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# 6. Service Health Verification
Write-Host "[INFO] Awaiting container health and Flyway migrations..." -ForegroundColor Cyan
$maxAttempts = 30
$attempt = 1
$backendHealthy = $false

while ($attempt -le $maxAttempts) {
    Write-Host "  Attempt $attempt/$maxAttempts: Checking stack health..." -ForegroundColor Gray
    
    $checkResult = & docker exec astro-ai-prod-backend wget --spider -q http://127.0.0.1:8080/actuator/health 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] Spring Boot Backend is UP and Healthy!" -ForegroundColor Green
        $backendHealthy = $true
        break
    }
    
    Start-Sleep -Seconds 5
    $attempt++
}

if (-not $backendHealthy) {
    Write-Host "[ERROR] Health check timed out. Dumping container logs:" -ForegroundColor Red
    & $composeCmd @composeArgs -f docker-compose.prod.yml logs --tail=50
    exit 1
}

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " Astro-AI Production Deployment Completed Successfully!   " -ForegroundColor Green
Write-Host " Gateway URL: http://localhost:80                         " -ForegroundColor Cyan
Write-Host " Health Check: http://localhost:80/actuator/health        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Green
