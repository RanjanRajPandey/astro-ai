# ==============================================================================
# Astro-AI PostgreSQL Automated Backup Script (backup-db.ps1)
# PowerShell script to dump PostgreSQL database with timestamping.
# ==============================================================================

[CmdletBinding()]
param(
    [string]$ContainerName = "astro-ai-prod-postgres",
    [string]$DbUser = "astro_admin",
    [string]$DbName = "astro_ai_prod"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
$BackupDir = Join-Path $RootDir "database\backups"

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = Join-Path $BackupDir "astro_ai_backup_$timestamp.sql"

Write-Host "[INFO] Starting database backup for '$DbName' from container '$ContainerName'..." -ForegroundColor Cyan

# Check if target container is running
$running = & docker ps --format '{{.Names}}' | Where-Object { $_ -eq $ContainerName }
if (-not $running) {
    # Check fallback dev container
    $devRunning = & docker ps --format '{{.Names}}' | Where-Object { $_ -eq "astro-ai-postgres" }
    if ($devRunning) {
        $ContainerName = "astro-ai-postgres"
        $DbUser = "astro_user"
        $DbName = "astro_ai"
        Write-Host "[INFO] Falling back to dev container: $ContainerName" -ForegroundColor Yellow
    } else {
        Write-Error "PostgreSQL container '$ContainerName' is not running."
        exit 1
    }
}

& docker exec -i $ContainerName pg_dump -U $DbUser $DbName > $backupFile
if ($LASTEXITCODE -ne 0) {
    Write-Error "Database dump failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

$fileInfo = Get-Item $backupFile
$sizeKb = [math]::Round($fileInfo.Length / 1KB, 2)
Write-Host "[SUCCESS] Backup completed successfully: $backupFile ($sizeKb KB)" -ForegroundColor Green

# Retention policy: remove backups older than 30 days
$retentionDate = (Get-Date).AddDays(-30)
Get-ChildItem -Path $BackupDir -Filter "astro_ai_backup_*.sql" | Where-Object { $_.LastWriteTime -lt $retentionDate } | Remove-Item -Force
Write-Host "[INFO] Cleaned up backups older than 30 days." -ForegroundColor Gray
