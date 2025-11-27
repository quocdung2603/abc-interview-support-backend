# =============================================
# Quick Database Import Script
# Interview Microservice ABC - Sample Data Import
# =============================================

param(
    [switch]$NoWait = $false,
    [switch]$UseDocker = $true
)

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Interview Microservice ABC - Database Import" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$CONTAINER_NAME = "interview-postgres"
$PG_HOST = "localhost"
$PG_PORT = "5432"
$PG_USER = "postgres"
$PG_PASSWORD = "password"

# Database list in import order
$DATABASES = @(
    @{Name="authdb"; File="authdb-sample-data.sql"; Description="Authentication Service"},
    @{Name="userdb"; File="userdb-sample-data.sql"; Description="User Management Service"},
    @{Name="questiondb"; File="questiondb-sample-data.sql"; Description="Question Service"},
    @{Name="careerdb"; File="careerdb-sample-data.sql"; Description="Career Service"},
    @{Name="examdb"; File="examdb-sample-data.sql"; Description="Exam Service"},
    @{Name="newsdb"; File="newsdb-sample-data.sql"; Description="News Service"},
    @{Name="socialdb"; File="socialdb-sample-data.sql"; Description="Social Service"}
)

# Function to execute SQL using Docker
function Import-SqlFileDocker {
    param(
        [string]$Database,
        [string]$SqlFile,
        [string]$Description
    )

    Write-Host "Importing $Description..." -ForegroundColor Yellow

    try {
        # Use Get-Content and pipe to docker exec
        $output = Get-Content $SqlFile -Raw | docker exec -i $CONTAINER_NAME psql -U $PG_USER -d $Database 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Success: $Description imported!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  Failed: $Description (exit code $LASTEXITCODE)" -ForegroundColor Red
            if ($output -and $output.Length -lt 500) {
                Write-Host "  Output: $output" -ForegroundColor Gray
            }
            return $false
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to execute SQL file (direct psql)
function Import-SqlFile {
    param(
        [string]$Database,
        [string]$SqlFile,
        [string]$Description
    )

    Write-Host "Importing $Description..." -ForegroundColor Yellow

    $env:PGPASSWORD = $PG_PASSWORD
    
    try {
        $output = & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d $Database -f $SqlFile -q 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Success: $Description imported!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  Failed: $Description (exit code $LASTEXITCODE)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to check if database exists using Docker
function Test-DatabaseExistsDocker {
    param([string]$Database)
    
    try {
        $query = "SELECT 1 FROM pg_database WHERE datname='$Database'"
        $result = docker exec $CONTAINER_NAME psql -U $PG_USER -d postgres -tAc $query 2>&1
        
        if ($LASTEXITCODE -eq 0 -and $result -match '1') {
            return $true
        }
        return $false
    } catch {
        return $false
    }
}

# Function to check if database exists (direct psql)
function Test-DatabaseExists {
    param([string]$Database)

    $env:PGPASSWORD = $PG_PASSWORD
    
    try {
        $query = "SELECT 1 FROM pg_database WHERE datname='$Database'"
        $result = & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d postgres -tAc $query 2>&1
        
        if ($LASTEXITCODE -eq 0 -and $result -match '1') {
            return $true
        }
        return $false
    } catch {
        return $false
    }
}

# Main execution
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Determine which method to use
if ($UseDocker) {
    # Check if Docker is available
    try {
        $null = Get-Command docker -ErrorAction Stop
        $containerCheck = docker ps --filter "name=$CONTAINER_NAME" --format "{{.Names}}" 2>&1
        
        if ($containerCheck -eq $CONTAINER_NAME) {
            Write-Host "  Using Docker container: $CONTAINER_NAME" -ForegroundColor Cyan
            $USE_DOCKER_MODE = $true
        } else {
            Write-Host "  Warning: Container '$CONTAINER_NAME' not found or not running" -ForegroundColor Yellow
            Write-Host "  Trying direct psql connection..." -ForegroundColor Yellow
            $USE_DOCKER_MODE = $false
        }
    } catch {
        Write-Host "  Docker not available, trying direct psql..." -ForegroundColor Yellow
        $USE_DOCKER_MODE = $false
    }
} else {
    $USE_DOCKER_MODE = $false
}

# If not using Docker, check psql
if (-not $USE_DOCKER_MODE) {
    try {
        $null = Get-Command psql -ErrorAction Stop
        Write-Host "  Using direct psql connection" -ForegroundColor Cyan
    } catch {
        Write-Host "  Error: Neither Docker nor psql is available!" -ForegroundColor Red
        Write-Host "  Please either:" -ForegroundColor Yellow
        Write-Host "    1. Start Docker with: docker-compose up -d" -ForegroundColor Yellow
        Write-Host "    2. Install PostgreSQL client tools (psql)" -ForegroundColor Yellow
        if (-not $NoWait) {
            Write-Host "`nPress any key to exit..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
        }
        exit 1
    }
}

# Test connection
try {
    if ($USE_DOCKER_MODE) {
        $testResult = docker exec $CONTAINER_NAME psql -U $PG_USER -d postgres -tAc 'SELECT 1' 2>&1
    } else {
        $env:PGPASSWORD = $PG_PASSWORD
        $testResult = & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d postgres -tAc 'SELECT 1' 2>&1
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  Error: Cannot connect to PostgreSQL!" -ForegroundColor Red
        Write-Host "  Make sure PostgreSQL is running (docker-compose up -d)" -ForegroundColor Yellow
        if (-not $NoWait) {
            Write-Host "`nPress any key to exit..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
        }
        exit 1
    }
} catch {
    Write-Host "  Error: PostgreSQL is not accessible!" -ForegroundColor Red
    Write-Host "  Exception: $($_.Exception.Message)" -ForegroundColor Gray
    if (-not $NoWait) {
        Write-Host "`nPress any key to exit..." -ForegroundColor Gray
        $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
    }
    exit 1
}

Write-Host "  Success: PostgreSQL connection OK!" -ForegroundColor Green
Write-Host ""

# Check if all databases exist
Write-Host "Checking databases..." -ForegroundColor Yellow
$missingDatabases = @()

foreach ($db in $DATABASES) {
    Write-Host "  Checking $($db.Name)..." -NoNewline
    
    $dbExists = if ($USE_DOCKER_MODE) {
        Test-DatabaseExistsDocker -Database $db.Name
    } else {
        Test-DatabaseExists -Database $db.Name
    }
    
    if ($dbExists) {
        Write-Host " Found" -ForegroundColor Green
    } else {
        Write-Host " Missing" -ForegroundColor Red
        $missingDatabases += $db.Name
    }
}

if ($missingDatabases.Count -gt 0) {
    Write-Host "`n  Error: Missing databases: $($missingDatabases -join ', ')" -ForegroundColor Red
    Write-Host "  Please ensure all services are running (docker-compose up -d)" -ForegroundColor Yellow
    Write-Host "  Services create their databases automatically on first start." -ForegroundColor Yellow
    if (-not $NoWait) {
        Write-Host "`nPress any key to exit..." -ForegroundColor Gray
        $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
    }
    exit 1
}

Write-Host "  Success: All databases exist!" -ForegroundColor Green
Write-Host ""

# Import data
Write-Host "Starting data import..." -ForegroundColor Yellow
Write-Host ""

# Clear existing user data to avoid conflicts with hashed passwords
Write-Host "Clearing existing user data..." -ForegroundColor Yellow
if ($USE_DOCKER_MODE) {
    docker exec $CONTAINER_NAME psql -U $PG_USER -d userdb -c "TRUNCATE TABLE elo_history, users RESTART IDENTITY CASCADE;" 2>&1 | Out-Null
    docker exec $CONTAINER_NAME psql -U $PG_USER -d authdb -c "TRUNCATE TABLE users RESTART IDENTITY CASCADE;" 2>&1 | Out-Null
} else {
    $env:PGPASSWORD = $PG_PASSWORD
    & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d userdb -c "TRUNCATE TABLE elo_history, users RESTART IDENTITY CASCADE;" 2>&1 | Out-Null
    & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d authdb -c "TRUNCATE TABLE users RESTART IDENTITY CASCADE;" 2>&1 | Out-Null
}
Write-Host "  User data cleared!" -ForegroundColor Green
Write-Host ""

$successCount = 0
$totalCount = $DATABASES.Count

foreach ($db in $DATABASES) {
    $sqlFile = Join-Path $PSScriptRoot $db.File
    
    if (-not (Test-Path $sqlFile)) {
        Write-Host "  Error: SQL file not found: $($db.File)" -ForegroundColor Red
        continue
    }
    
    $importSuccess = if ($USE_DOCKER_MODE) {
        Import-SqlFileDocker -Database $db.Name -SqlFile $sqlFile -Description $db.Description
    } else {
        Import-SqlFile -Database $db.Name -SqlFile $sqlFile -Description $db.Description
    }
    
    if ($importSuccess) {
        $successCount++
    }
}

# Summary
Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Import Summary" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

if ($successCount -eq $totalCount) {
    Write-Host "Status: SUCCESS - All $totalCount databases imported!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Test Credentials (Password: admin123 for all):" -ForegroundColor Cyan
    Write-Host "  Admin:     admin@example.com" -ForegroundColor White
    Write-Host "  Recruiter: recruiter@example.com" -ForegroundColor White  
    Write-Host "  User:      user@example.com" -ForegroundColor White
    Write-Host ""
    Write-Host "Gateway URL: http://localhost:8080" -ForegroundColor Green
    Write-Host "You can now test the APIs!" -ForegroundColor Green
} else {
    Write-Host "Status: PARTIAL - $successCount/$totalCount databases imported" -ForegroundColor Yellow
    Write-Host "Please check the error messages above." -ForegroundColor Yellow
}

Write-Host ""

if (-not $NoWait) {
    Write-Host "Press any key to continue..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
}

