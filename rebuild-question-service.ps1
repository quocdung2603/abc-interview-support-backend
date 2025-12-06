# Rebuild and Restart Question Service
# This script rebuilds question-service and restarts the Docker container

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "        Rebuild and Restart Question Service                    " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build service
Write-Host "[1/3] Building question-service..." -ForegroundColor Yellow
try {
    Push-Location question-service
    
    Write-Host "   Running Maven build..." -ForegroundColor Gray
    mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed"
    }
    
    Write-Host "[OK] Build successful" -ForegroundColor Green
    Pop-Location
} catch {
    Write-Host "[ERROR] Build failed: $_" -ForegroundColor Red
    Pop-Location
    exit 1
}

# Step 2: Rebuild Docker image
Write-Host "`n[2/3] Rebuilding Docker image..." -ForegroundColor Yellow
try {
    docker-compose build question-service
    
    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed"
    }
    
    Write-Host "[OK] Docker image rebuilt" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker build failed: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Restart container
Write-Host "`n[3/3] Restarting container..." -ForegroundColor Yellow
try {
    docker-compose up -d question-service
    
    if ($LASTEXITCODE -ne 0) {
        throw "Container restart failed"
    }
    
    Write-Host "[OK] Container restarted" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Container restart failed: $_" -ForegroundColor Red
    exit 1
}

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
    Write-Host "[INFO] Check logs: docker-compose logs question-service" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  Rebuild Complete!                             " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nService Status:" -ForegroundColor Cyan
Write-Host "   URL: http://localhost:8085" -ForegroundColor White
Write-Host "   Health: http://localhost:8085/actuator/health" -ForegroundColor White
Write-Host "   Swagger: http://localhost:8085/swagger-ui.html" -ForegroundColor White

Write-Host "`nTest Answer API:" -ForegroundColor Cyan
Write-Host "   GET http://localhost:8085/api/answers" -ForegroundColor Gray

Write-Host "`n[OK] Complete!`n" -ForegroundColor Green
