# Quick Build Script - Build All Services
# Skips tests for faster build time

$ErrorActionPreference = "Stop"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Quick Build - All Microservices" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$services = @(
    "discovery-service",
    "config-service",
    "gateway-service",
    "auth-service",
    "user-service",
    "question-service",
    "exam-service",
    "news-service",
    "career-service"
)

$totalServices = $services.Count
$currentService = 0
$failedServices = @()

foreach ($service in $services) {
    $currentService++
    Write-Host "[$currentService/$totalServices] Building $service..." -ForegroundColor Yellow
    
    try {
        Push-Location $service
        $output = & .\mvnw.cmd clean package -DskipTests 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  [OK] $service built successfully" -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] $service build failed" -ForegroundColor Red
            $failedServices += $service
        }
        Pop-Location
    }
    catch {
        Write-Host "  [ERROR] Building ${service}: $_" -ForegroundColor Red
        $failedServices += $service
        Pop-Location
    }
    Write-Host ""
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  BUILD SUMMARY" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$successCount = $totalServices - $failedServices.Count
Write-Host "Total Services: $totalServices" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($failedServices.Count)" -ForegroundColor Red

if ($failedServices.Count -gt 0) {
    Write-Host "`nFailed services:" -ForegroundColor Red
    foreach ($service in $failedServices) {
        Write-Host "  - $service" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "`nAll services built successfully!" -ForegroundColor Green
    Write-Host "Next step: Run services with .\quick-run.ps1`n" -ForegroundColor Cyan
}
