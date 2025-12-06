# Rebuild Question Service
# This script rebuilds and restarts the question-service container

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Rebuild Question Service                             " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Go to root directory
$rootDir = Split-Path -Parent $PSScriptRoot
Push-Location $rootDir

try {
    # Step 1: Build the service
    Write-Host "[1/4] Building question-service..." -ForegroundColor Yellow
    Push-Location "question-service"
    
    try {
        $mvnWrapper = if ($IsWindows -or $env:OS -eq "Windows_NT") { ".\mvnw.cmd" } else { "./mvnw" }
        
        & $mvnWrapper clean package -DskipTests -q
        
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed"
        }
        
        Write-Host "[OK] Build successful" -ForegroundColor Green
    } finally {
        Pop-Location
    }
    
    # Step 2: Stop the container
    Write-Host "`n[2/4] Stopping question-service container..." -ForegroundColor Yellow
    docker-compose stop question-service
    Write-Host "[OK] Container stopped" -ForegroundColor Green
    
    # Step 3: Rebuild Docker image
    Write-Host "`n[3/4] Rebuilding Docker image..." -ForegroundColor Yellow
    docker-compose build question-service
    
    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed"
    }
    
    Write-Host "[OK] Docker image rebuilt" -ForegroundColor Green
    
    # Step 4: Start the container
    Write-Host "`n[4/4] Starting question-service container..." -ForegroundColor Yellow
    docker-compose up -d question-service
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start container"
    }
    
    Write-Host "[OK] Container started" -ForegroundColor Green
    
    # Wait for service to be ready
    Write-Host "`nWaiting for service to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    $maxAttempts = 12
    $attempt = 0
    $serviceReady = $false
    
    while ($attempt -lt $maxAttempts -and -not $serviceReady) {
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:8085/actuator/health" -TimeoutSec 3 -ErrorAction Stop
            if ($response.status -eq "UP") {
                $serviceReady = $true
            }
        } catch {
            $attempt++
            if ($attempt -lt $maxAttempts) {
                Write-Host "   Waiting... ($attempt/$maxAttempts)" -ForegroundColor Gray
                Start-Sleep -Seconds 5
            }
        }
    }
    
    if ($serviceReady) {
        Write-Host "[OK] Question service is ready!" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Service may still be starting up" -ForegroundColor Yellow
        Write-Host "[INFO] Check logs with: docker-compose logs question-service" -ForegroundColor Cyan
    }
    
    # Summary
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Green
    Write-Host "                  Rebuild Complete!                             " -ForegroundColor Green
    Write-Host "================================================================" -ForegroundColor Green
    
    Write-Host "`nService Info:" -ForegroundColor Cyan
    Write-Host "   URL: http://localhost:8085" -ForegroundColor White
    Write-Host "   Health: http://localhost:8085/actuator/health" -ForegroundColor White
    Write-Host "   Swagger: http://localhost:8085/swagger-ui.html" -ForegroundColor White
    
    Write-Host "`nQuick Commands:" -ForegroundColor Cyan
    Write-Host "   - View logs: docker-compose logs -f question-service" -ForegroundColor Gray
    Write-Host "   - Restart: docker-compose restart question-service" -ForegroundColor Gray
    Write-Host "   - Stop: docker-compose stop question-service" -ForegroundColor Gray
    
    Write-Host "`n[OK] Question service rebuilt successfully!`n" -ForegroundColor Green
    
} catch {
    Write-Host "`n[ERROR] Rebuild failed: $_" -ForegroundColor Red
    Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
    Write-Host "   1. Check if Docker is running" -ForegroundColor Gray
    Write-Host "   2. View logs: docker-compose logs question-service" -ForegroundColor Gray
    Write-Host "   3. Try manual build: cd question-service && mvnw clean package" -ForegroundColor Gray
    exit 1
} finally {
    Pop-Location
}
