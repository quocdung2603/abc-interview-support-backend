# Quick wrapper to import sample data
# Run this after starting services with docker-compose up -d

Write-Host "`nImporting sample data into databases..." -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

& "$PSScriptRoot\database-import\quick-import-data.ps1"

Write-Host "`nDone! Services are ready to use." -ForegroundColor Green
