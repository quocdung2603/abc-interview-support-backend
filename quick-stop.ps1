# ============================================================
# Quick Stop Script - Stop All Services
# ============================================================

$ErrorActionPreference = "Continue"

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║          Quick Stop - Shutdown All Services                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Stopping all containers..." -ForegroundColor Yellow
docker-compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ All services stopped successfully!" -ForegroundColor Green
    Write-Host "`n💡 To start again: .\quick-run.ps1" -ForegroundColor Cyan
} else {
    Write-Host "`n✗ Error stopping services" -ForegroundColor Red
    Write-Host "Try: docker-compose down --remove-orphans" -ForegroundColor Yellow
}
