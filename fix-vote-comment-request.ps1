# Fix Vote Comment Request in Postman Collection
# Add missing voteType field to the request body

Write-Host "Fixing Vote Comment Request..." -ForegroundColor Cyan

$file = "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json"

if (-not (Test-Path $file)) {
    Write-Host "File not found: $file" -ForegroundColor Red
    exit 1
}

Write-Host "Reading file..." -ForegroundColor Yellow
$content = Get-Content $file -Raw -Encoding UTF8

Write-Host "Fixing vote request body..." -ForegroundColor Yellow

# Find and replace the incomplete vote request body
$oldBody = '"raw":  "{\n  \"userId\": 3\n}"'
$newBody = '"raw":  "{\n  \"userId\": 3,\n  \"voteType\": \"USEFUL\"\n}"'

if ($content -match [regex]::Escape($oldBody)) {
    $content = $content.Replace($oldBody, $newBody)
    Write-Host "  Fixed vote request body - added voteType field" -ForegroundColor Green
} else {
    Write-Host "  Vote request pattern not found or already fixed" -ForegroundColor Yellow
}

Write-Host "Saving file..." -ForegroundColor Yellow
$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($file, $content, $utf8)

Write-Host ""
Write-Host "SUCCESS! Vote comment request has been fixed." -ForegroundColor Green
Write-Host ""
Write-Host "Request body now includes:" -ForegroundColor Cyan
Write-Host '  {' -ForegroundColor White
Write-Host '    "userId": 3,' -ForegroundColor White
Write-Host '    "voteType": "USEFUL"' -ForegroundColor White
Write-Host '  }' -ForegroundColor White
Write-Host ""
Write-Host "Valid voteType values: USEFUL, NOT_USEFUL" -ForegroundColor Gray
Write-Host "Alternative: use 'useful': true or 'unuseful': true" -ForegroundColor Gray
