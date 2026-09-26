$root = Split-Path -Parent $PSScriptRoot

Write-Host "=== [1/3] Running Python Astrology Engine Tests ===" -ForegroundColor Cyan
Push-Location (Join-Path $root "astrology-engine")
.\.venv\Scripts\pytest -v
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

Write-Host "`n=== [2/3] Running Spring Boot Backend Tests ===" -ForegroundColor Cyan
Push-Location (Join-Path $root "backend")
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $ErrorActionPreference = "Continue"
    $javaHomeLine = (cmd /c "java -XshowSettings:properties -version 2>&1" | Select-String "java.home =").ToString()
    $env:JAVA_HOME = ($javaHomeLine -split "=")[1].Trim()
}
.\mvnw.cmd test
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

Write-Host "`n=== [3/3] Running React Frontend Tests ===" -ForegroundColor Cyan
Push-Location (Join-Path $root "frontend")
npm test
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

Write-Host "`nALL MONOREPO TEST SUITES PASSED!" -ForegroundColor Green
