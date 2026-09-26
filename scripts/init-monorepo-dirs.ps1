$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

# 1. Astrology Engine module directories (Section 5)
$astroModules = @(
    "astronomy", "coordinates", "timezone", "planets", "houses", "ascendant",
    "nakshatra", "divisional", "dasha", "transit", "drishti", "yoga",
    "strength", "rules", "models", "api", "tests"
)
foreach ($mod in $astroModules) {
    $dir = Join-Path $root "astrology-engine\$mod"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $initFile = Join-Path $dir "__init__.py"
    if (-not (Test-Path $initFile)) {
        Set-Content -Path $initFile -Value "# $mod module`n"
    }
}

# 2. Backend Java domain package directories (Section 5)
$javaPackages = @(
    "auth", "user", "birth", "location", "chart", "planet", "house",
    "nakshatra", "divisional", "dasha", "transit", "drishti", "yoga",
    "strength", "analysis", "evidence", "reasoning", "ai", "common", "config"
)
foreach ($pkg in $javaPackages) {
    $dir = Join-Path $root "backend\src\main\java\com\astroai\$pkg"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $pkgInfo = Join-Path $dir "package-info.java"
    if (-not (Test-Path $pkgInfo)) {
        Set-Content -Path $pkgInfo -Value "/**`n * Domain package: com.astroai.$pkg`n */`npackage com.astroai.$pkg;`n"
    }
}

# 3. Frontend component & architecture directories (Section 5)
$frontendDirs = @(
    "components/charts", "components/kundli", "components/planets", "components/houses",
    "components/nakshatra", "components/dashas", "components/transits", "components/drishti",
    "components/yogas", "components/strengths", "components/reasoning", "components/chat",
    "pages", "layouts", "services", "hooks", "types", "utils", "store"
)
foreach ($fdir in $frontendDirs) {
    $dir = Join-Path $root "frontend\src\$($fdir -replace '/','\')"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $keepFile = Join-Path $dir ".gitkeep"
    if (-not (Test-Path $keepFile)) {
        Set-Content -Path $keepFile -Value ""
    }
}

Write-Output "All Phase 1 monorepo directories and package markers initialized successfully."
