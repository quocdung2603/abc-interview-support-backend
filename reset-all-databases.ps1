# Reset All Databases Script
# This script drops and recreates all databases

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Reset All Databases" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Database names from .env
$databases = @(
    "authdb",
    "userdb", 
    "careerdb",
    "questiondb",
    "examdb",
    "newsdb",
    "socialdb"
)

Write-Host "This will DROP and RECREATE the following databases:" -ForegroundColor Yellow
$databases | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
Write-Host ""

$confirmation = Read-Host "Are you sure you want to continue? (yes/no)"
if ($confirmation -ne "yes") {
    Write-Host "Operation cancelled." -ForegroundColor Red
    exit
}

Write-Host "`nDropping and recreating databases..." -ForegroundColor Cyan

foreach ($db in $databases) {
    Write-Host "Processing $db..." -ForegroundColor Yellow
    
    # Drop database if exists
    docker exec -i interview-postgres psql -U postgres -c "DROP DATABASE IF EXISTS $db;" 2>$null
    
    # Create database
    docker exec -i interview-postgres psql -U postgres -c "CREATE DATABASE $db;" 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Success: $db recreated successfully" -ForegroundColor Green
    } else {
        Write-Host "  Failed: $db" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Database reset complete!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart all services: docker-compose restart" -ForegroundColor White
Write-Host "2. Import sample data: .\import-data.ps1" -ForegroundColor White
Write-Host ""
