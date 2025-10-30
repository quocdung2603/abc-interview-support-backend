# =============================================
# Quick Database Import Script
# Interview Microservice ABC - Sample Data Import
# =============================================

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Interview Microservice ABC - Database Import" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$PG_HOST = "localhost"
$PG_PORT = "5432"
$PG_USER = "postgres"
$PG_PASSWORD = "password"  # Change this to your PostgreSQL password

# Database list in import order
$DATABASES = @(
    @{Name="authdb"; File="authdb-sample-data.sql"; Description="Authentication Service"},
    @{Name="userdb"; File="userdb-sample-data.sql"; Description="User Management Service"},
    @{Name="questiondb"; File="questiondb-sample-data.sql"; Description="Question Service"},
    @{Name="careerdb"; File="careerdb-sample-data.sql"; Description="Career Service"},
    @{Name="examdb"; File="examdb-sample-data.sql"; Description="Exam Service"},
    @{Name="newsdb"; File="newsdb-sample-data.sql"; Description="News Service"}
)

# Function to execute SQL file
function Import-SqlFile {
    param(
        [string]$Database,
        [string]$SqlFile,
        [string]$Description
    )

    Write-Host "Importing $Description..." -ForegroundColor Yellow

    $env:PGPASSWORD = $PG_PASSWORD
    # Build psql argument list to avoid quoting issues
    $args = @('-h', $PG_HOST, '-p', $PG_PORT, '-U', $PG_USER, '-d', $Database, '-f', $SqlFile)

    try {
        & psql @args
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ $Description imported successfully!" -ForegroundColor Green
        } else {
            Write-Host "✗ Failed to import $Description (exit code $LASTEXITCODE)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ Error importing $Description : $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }

    return $true
}

# Function to check if database exists
function Test-DatabaseExists {
    param([string]$Database)

    $env:PGPASSWORD = $PG_PASSWORD
    # Query pg_database in a portable way (no grep/cut required)
    $query = "SELECT 1 FROM pg_database WHERE datname='$Database'"
    try {
        $result = & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -tAc $query 2>$null
        if ($LASTEXITCODE -ne 0) { return $false }
        return ($result.Trim() -eq '1')
    } catch {
        return $false
    }
}

# Main execution
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Check if PostgreSQL is accessible
$env:PGPASSWORD = $PG_PASSWORD
try {
    # Use -tAc to get a simple scalar result
    & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -tAc 'SELECT 1' | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Cannot connect to PostgreSQL. Please check your connection settings." -ForegroundColor Red
        Write-Host "Host: $PG_HOST, Port: $PG_PORT, User: $PG_USER" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "✗ PostgreSQL is not accessible. Please ensure PostgreSQL is running." -ForegroundColor Red
    exit 1
}

Write-Host "✓ PostgreSQL connection successful!" -ForegroundColor Green
Write-Host ""

# Check if all databases exist
Write-Host "Checking if all databases exist..." -ForegroundColor Yellow
$missingDatabases = @()

foreach ($db in $DATABASES) {
    if (-not (Test-DatabaseExists -Database $db.Name)) {
        $missingDatabases += $db.Name
    }
}

if ($missingDatabases.Count -gt 0) {
    Write-Host "✗ Missing databases: $($missingDatabases -join ', ')" -ForegroundColor Red
    Write-Host "Please run init.sql first to create all databases." -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ All databases exist!" -ForegroundColor Green
Write-Host ""

# Import data
Write-Host "Starting data import..." -ForegroundColor Yellow
Write-Host ""

$successCount = 0
$totalCount = $DATABASES.Count

foreach ($db in $DATABASES) {
    $sqlFile = Join-Path $PSScriptRoot $db.File
    
    if (-not (Test-Path $sqlFile)) {
        Write-Host "✗ SQL file not found: $sqlFile" -ForegroundColor Red
        continue
    }
    
    if (Import-SqlFile -Database $db.Name -SqlFile $sqlFile -Description $db.Description) {
        $successCount++
    }
    
    Write-Host ""
}

# Summary
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Import Summary" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Successfully imported: $successCount/$totalCount databases" -ForegroundColor $(if ($successCount -eq $totalCount) { "Green" } else { "Yellow" })

if ($successCount -eq $totalCount) {
    Write-Host ""
    Write-Host "🎉 All sample data imported successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Test Users:" -ForegroundColor Cyan
    Write-Host "  Admin: admin@example.com / password123" -ForegroundColor White
    Write-Host "  Recruiter: recruiter@example.com / password123" -ForegroundColor White
    Write-Host "  User: user@example.com / password123" -ForegroundColor White
    Write-Host "  Test: test@example.com / password123" -ForegroundColor White
    Write-Host ""
    Write-Host "You can now start the microservices and test the APIs!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  Some imports failed. Please check the error messages above." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
