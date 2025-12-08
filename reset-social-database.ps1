# Reset Social Service Database
# This script drops and recreates the socialdb database

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Reset Social Service Database                        " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$POSTGRES_CONTAINER = "interview-postgres"
$POSTGRES_USER = "postgres"
$DATABASE_NAME = "socialdb"

# Check if PostgreSQL container is running
Write-Host "Checking PostgreSQL container..." -ForegroundColor Yellow
try {
    $containerStatus = docker ps --filter "name=$POSTGRES_CONTAINER" --format "{{.Status}}"
    if (-not $containerStatus) {
        throw "PostgreSQL container is not running"
    }
    Write-Host "[OK] PostgreSQL container is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] PostgreSQL container is not running!" -ForegroundColor Red
    Write-Host "[INFO] Start it with: docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

# Stop social-service first
Write-Host "`nStopping social-service..." -ForegroundColor Yellow
try {
    docker-compose stop social-service 2>$null | Out-Null
    Write-Host "[OK] Social service stopped" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Social service was not running" -ForegroundColor Yellow
}

# Drop existing database
Write-Host "`nDropping database '$DATABASE_NAME'..." -ForegroundColor Yellow
try {
    # Terminate all connections to the database
    $terminateQuery = @"
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = '$DATABASE_NAME'
  AND pid <> pg_backend_pid();
"@
    
    docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c $terminateQuery 2>$null | Out-Null
    
    # Drop database
    docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c "DROP DATABASE IF EXISTS $DATABASE_NAME;" | Out-Null
    
    Write-Host "[OK] Database dropped" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Database may not exist or already dropped" -ForegroundColor Yellow
}

# Create new database
Write-Host "`nCreating database '$DATABASE_NAME'..." -ForegroundColor Yellow
try {
    docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c "CREATE DATABASE $DATABASE_NAME;" | Out-Null
    Write-Host "[OK] Database created" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to create database: $_" -ForegroundColor Red
    exit 1
}

# Grant privileges
Write-Host "`nGranting privileges..." -ForegroundColor Yellow
try {
    docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c "GRANT ALL PRIVILEGES ON DATABASE $DATABASE_NAME TO $POSTGRES_USER;" | Out-Null
    Write-Host "[OK] Privileges granted" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Could not grant privileges" -ForegroundColor Yellow
}

# Verify database
Write-Host "`nVerifying database..." -ForegroundColor Yellow
try {
    $result = docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c "\l $DATABASE_NAME"
    if ($result -match $DATABASE_NAME) {
        Write-Host "[OK] Database verified" -ForegroundColor Green
    }
} catch {
    Write-Host "[WARN] Could not verify database" -ForegroundColor Yellow
}

# Start social-service (it will create tables automatically via JPA)
Write-Host "`nStarting social-service..." -ForegroundColor Yellow
try {
    docker-compose up -d social-service | Out-Null
    Write-Host "[OK] Social service started" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to start social service: $_" -ForegroundColor Red
    exit 1
}

# Wait for service to initialize
Write-Host "`nWaiting for service to initialize tables..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$maxAttempts = 12
$attempt = 0
$serviceReady = $false

while ($attempt -lt $maxAttempts -and -not $serviceReady) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8090/actuator/health" -TimeoutSec 3 -ErrorAction Stop
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
    Write-Host "[OK] Social service is ready!" -ForegroundColor Green
} else {
    Write-Host "[WARN] Service may still be starting up" -ForegroundColor Yellow
}

# Check tables
Write-Host "`nChecking created tables..." -ForegroundColor Yellow
try {
    $tables = docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $DATABASE_NAME -c "\dt"
    Write-Host $tables -ForegroundColor Gray
} catch {
    Write-Host "[WARN] Could not list tables" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  Database Reset Complete!                      " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nDatabase Info:" -ForegroundColor Cyan
Write-Host "   Database: $DATABASE_NAME" -ForegroundColor White
Write-Host "   Host: localhost (or postgres from Docker)" -ForegroundColor White
Write-Host "   Port: 5432" -ForegroundColor White
Write-Host "   Username: $POSTGRES_USER" -ForegroundColor White
Write-Host "   Password: 123456" -ForegroundColor White

Write-Host "`nService Info:" -ForegroundColor Cyan
Write-Host "   URL: http://localhost:8090" -ForegroundColor White
Write-Host "   Health: http://localhost:8090/actuator/health" -ForegroundColor White

Write-Host "`nQuick Commands:" -ForegroundColor Cyan
Write-Host "   - View logs: docker-compose logs -f social-service" -ForegroundColor Gray
Write-Host "   - Connect to DB: docker exec -it $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $DATABASE_NAME" -ForegroundColor Gray
Write-Host "   - List tables: docker exec $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $DATABASE_NAME -c '\dt'" -ForegroundColor Gray

Write-Host "`n[OK] Social database reset successfully!`n" -ForegroundColor Green
