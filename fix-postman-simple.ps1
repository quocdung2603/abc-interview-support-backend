# Simple fix for Postman encoding
$file = "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json"

Write-Host "Reading file as bytes..."
$bytes = [System.IO.File]::ReadAllBytes($file)

Write-Host "Converting to string..."
$content = [System.Text.Encoding]::UTF8.GetString($bytes)

Write-Host "Fixing unicode escapes..."
$content = $content.Replace('\u0027', "'")
$content = $content.Replace('\u0026', '&')

Write-Host "Saving file..."
$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($file, $content, $utf8)

Write-Host "Done! Fixed unicode escape sequences."
