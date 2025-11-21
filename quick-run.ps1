# Quick Run Script - Start All Services with Docker Compose

$ErrorActionPreference = "Continue"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Quick Run - Start All Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    $dockerCheck = docker ps 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[FAIL] Docker is not running" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Docker is running" -ForegroundColor Green
}
catch {
    Write-Host "[ERROR] Docker not available" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Write-Host "[OK] Cleaned up" -ForegroundColor Green
Write-Host ""

Write-Host "Starting all services..." -ForegroundColor Yellow
Write-Host "(This may take 2-3 minutes)" -ForegroundColor Gray
Write-Host ""

docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[OK] All services started!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Waiting for initialization (15 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    Write-Host "`nService Status:" -ForegroundColor Cyan
    docker-compose ps
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  SERVICES READY" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
    
    Write-Host "Service URLs:" -ForegroundColor Yellow
    Write-Host "  Gateway:  http://localhost:8080" -ForegroundColor White
    Write-Host "  Eureka:   http://localhost:8761" -ForegroundColor White
    
    Write-Host "`nNext Steps:" -ForegroundColor Cyan
    Write-Host "  Test APIs: .\test-all-109-apis.ps1" -ForegroundColor Gray
    Write-Host "  Stop:      docker-compose down`n" -ForegroundColor Gray
} else {
    Write-Host "`n[FAIL] Failed to start services" -ForegroundColor Red
    exit 1
}
