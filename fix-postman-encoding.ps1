# Fix Postman Collection Encoding Issues
# This script fixes UTF-8 encoding problems in Postman collection files

Write-Host "Fixing Postman Collection Encoding..." -ForegroundColor Cyan

$file = "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json"

if (-not (Test-Path $file)) {
    Write-Host "File not found: $file" -ForegroundColor Red
    exit 1
}

Write-Host "Reading file with correct encoding..." -ForegroundColor Yellow

# Read the file content
$content = Get-Content $file -Raw -Encoding UTF8

Write-Host "Applying fixes..." -ForegroundColor Yellow

# Count fixes
$fixCount = 0

# Fix malformed emoji patterns (these appear as mojibake)
$patterns = @{
    'ðŸ"' = '🔒'  # Lock
    'ðŸ'¤' = '👤'  # User
    'ðŸ"œ' = '📜'  # Scroll
    'ðŸ"' = '📁'  # Folder
    'ðŸ"š' = '📚'  # Books
    'ðŸ"Š' = '📊'  # Chart
    'ðŸ"' = '📝'  # Memo
    'ðŸ"°' = '📰'  # Newspaper
    'ðŸ'¬' = '💬'  # Speech
    'ðŸ"¢' = '📢'  # Megaphone
    'ðŸŽ‰' = '🎉'  # Party
    'âœ…' = '✅'  # Check mark
    'âœ"' = '✔️'  # Check
    'â„¹' = 'ℹ️'  # Info
    'âš ' = '⚠️'  # Warning
    'â­' = '⭐'  # Star
}

foreach ($pattern in $patterns.GetEnumerator()) {
    $oldContent = $content
    $content = $content -replace [regex]::Escape($pattern.Key), $pattern.Value
    if ($oldContent -ne $content) {
        $matches = ([regex]::Matches($oldContent, [regex]::Escape($pattern.Key))).Count
        $fixCount += $matches
        Write-Host "  Fixed $matches instances of $($pattern.Key) -> $($pattern.Value)" -ForegroundColor Gray
    }
}

# Fix unicode escape sequences
$unicodePatterns = @{
    '\\u0027' = "'"
    '\\u0026' = '&'
}

foreach ($pattern in $unicodePatterns.GetEnumerator()) {
    $oldContent = $content
    $content = $content -replace $pattern.Key, $pattern.Value
    if ($oldContent -ne $content) {
        $matches = ([regex]::Matches($oldContent, $pattern.Key)).Count
        $fixCount += $matches
        Write-Host "  Fixed $matches instances of $($pattern.Key) -> $($pattern.Value)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Saving fixed file..." -ForegroundColor Yellow

# Save with UTF-8 encoding (no BOM)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($file, $content, $utf8NoBom)

Write-Host ""
Write-Host "SUCCESS! Fixed $fixCount encoding issues" -ForegroundColor Green
Write-Host ""
Write-Host "The Postman collection is now ready to import." -ForegroundColor Cyan
