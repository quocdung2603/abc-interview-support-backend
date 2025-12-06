# Start pgAdmin for PostgreSQL Database Management
# Usage: .\start-pgadmin.ps1

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Starting pgAdmin for Database Management            " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
try {
    docker ps | Out-Null
    Write-Host "[OK] Docker is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker is not running!" -ForegroundColor Red
    Write-Host "[INFO] Please start Docker Desktop first" -ForegroundColor Yellow
    exit 1
}

# Start pgAdmin container
Write-Host "`nStarting pgAdmin container..." -ForegroundColor Yellow
try {
    docker-compose up -d pgadmin
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start pgAdmin"
    }
    
    Write-Host "[OK] pgAdmin container started" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to start pgAdmin: $_" -ForegroundColor Red
    exit 1
}

# Wait for pgAdmin to be ready
Write-Host "`nWaiting for pgAdmin to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$maxAttempts = 12
$attempt = 0
$pgAdminReady = $false

while ($attempt -lt $maxAttempts -and -not $pgAdminReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5050" -TimeoutSec 3 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $pgAdminReady = $true
        }
    } catch {
        $attempt++
        if ($attempt -lt $maxAttempts) {
            Write-Host "   Waiting... ($attempt/$maxAttempts)" -ForegroundColor Gray
            Start-Sleep -Seconds 5
        }
    }
}

if ($pgAdminReady) {
    Write-Host "[OK] pgAdmin is ready!" -ForegroundColor Green
} else {
    Write-Host "[WARN] pgAdmin may still be starting up" -ForegroundColor Yellow
}

# Display connection information
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                   pgAdmin is Running!                          " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nAccess Information:" -ForegroundColor Cyan
Write-Host "   URL: http://localhost:5050" -ForegroundColor White
Write-Host "   Email: admin@admin.com" -ForegroundColor White
Write-Host "   Password: admin" -ForegroundColor White

Write-Host "`nPostgreSQL Connection Details:" -ForegroundColor Cyan
Write-Host "   Host: postgres (or localhost from your machine)" -ForegroundColor White
Write-Host "   Port: 5432" -ForegroundColor White
Write-Host "   Username: postgres" -ForegroundColor White
Write-Host "   Password: 123456" -ForegroundColor White

Write-Host "`nAvailable Databases:" -ForegroundColor Cyan
Write-Host "   - authdb (Auth Service)" -ForegroundColor Gray
Write-Host "   - userdb (User Service)" -ForegroundColor Gray
Write-Host "   - questiondb (Question Service)" -ForegroundColor Gray
Write-Host "   - examdb (Exam Service)" -ForegroundColor Gray
Write-Host "   - careerdb (Career Service)" -ForegroundColor Gray
Write-Host "   - newsdb (News Service)" -ForegroundColor Gray
Write-Host "   - socialdb (Social Service)" -ForegroundColor Gray

Write-Host "`nHow to Add Server in pgAdmin:" -ForegroundColor Cyan
Write-Host "   1. Open http://localhost:5050 in your browser" -ForegroundColor Gray
Write-Host "   2. Login with email: admin@admin.com, password: admin" -ForegroundColor Gray
Write-Host "   3. Right-click 'Servers' -> Register -> Server" -ForegroundColor Gray
Write-Host "   4. General tab:" -ForegroundColor Gray
Write-Host "      - Name: ABC Interview DB" -ForegroundColor DarkGray
Write-Host "   5. Connection tab:" -ForegroundColor Gray
Write-Host "      - Host: postgres" -ForegroundColor DarkGray
Write-Host "      - Port: 5432" -ForegroundColor DarkGray
Write-Host "      - Username: postgres" -ForegroundColor DarkGray
Write-Host "      - Password: 123456" -ForegroundColor DarkGray
Write-Host "   6. Click 'Save'" -ForegroundColor Gray

Write-Host "`nQuick Commands:" -ForegroundColor Cyan
Write-Host "   - Open pgAdmin: Start-Process http://localhost:5050" -ForegroundColor Gray
Write-Host "   - View logs: docker-compose logs pgadmin" -ForegroundColor Gray
Write-Host "   - Stop pgAdmin: docker-compose stop pgadmin" -ForegroundColor Gray
Write-Host "   - Restart pgAdmin: docker-compose restart pgadmin" -ForegroundColor Gray

Write-Host "`nOpening pgAdmin in browser..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
Start-Process "http://localhost:5050"

Write-Host "`n[OK] pgAdmin setup complete!`n" -ForegroundColor Green
