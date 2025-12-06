# Export ERD for All Databases
# This script exports ERD for all microservice databases

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "        Export ERD for All Microservice Databases               " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$databases = @("authdb", "userdb", "questiondb", "examdb", "careerdb", "newsdb", "socialdb")

$successCount = 0
$failCount = 0

foreach ($db in $databases) {
    Write-Host "`n--- Exporting $db ---" -ForegroundColor Yellow
    
    try {
        & .\export-database-erd.ps1 -Database $db -ErrorAction Stop
        $successCount++
        Write-Host "[OK] $db exported successfully" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Failed to export $db : $_" -ForegroundColor Red
        $failCount++
    }
    
    Write-Host ""
}

Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  All Exports Complete!                         " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "   Success: $successCount databases" -ForegroundColor Green
Write-Host "   Failed: $failCount databases" -ForegroundColor Red

Write-Host "`nAll files are in: database-docs\" -ForegroundColor Cyan
Write-Host "`nOpening folder..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
Start-Process "database-docs"

Write-Host "`n[OK] Complete!`n" -ForegroundColor Green
