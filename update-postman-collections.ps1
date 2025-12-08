# PowerShell script to update Postman collections to use gateway URL

$files = @(
    "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json",
    "postman-collections/ABC-Interview-Verified-Complete.postman_collection.json"
)

$totalReplacements = 0

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing $file..." -ForegroundColor Cyan
        
        # Read file content
        $content = Get-Content $file -Raw -Encoding UTF8
        
        # Count and replace patterns
        $replacements = @{}
        
        # Replace URL patterns
        $patterns = @(
            @{Pattern = '\{\{exam_service_url\}\}/exams'; Replacement = '{{gateway_url}}/exams'; Key = 'exam_service_url'},
            @{Pattern = '\{\{question_service_url\}\}/questions'; Replacement = '{{gateway_url}}/questions'; Key = 'question_service_url'},
            @{Pattern = '\{\{user_service_url\}\}/users'; Replacement = '{{gateway_url}}/users'; Key = 'user_service_url'},
            @{Pattern = '\{\{auth_service_url\}\}/auth'; Replacement = '{{gateway_url}}/auth'; Key = 'auth_service_url'},
            @{Pattern = '\{\{career_service_url\}\}/career'; Replacement = '{{gateway_url}}/career'; Key = 'career_service_url'},
            @{Pattern = '\{\{news_service_url\}\}/news'; Replacement = '{{gateway_url}}/news'; Key = 'news_service_url'},
            @{Pattern = '\{\{social_service_url\}\}/posts'; Replacement = '{{gateway_url}}/posts'; Key = 'social_service_url'},
            @{Pattern = '\{\{social_service_url\}\}/comments'; Replacement = '{{gateway_url}}/comments'; Key = 'social_service_url'}
        )
        
        foreach ($p in $patterns) {
            $matches = [regex]::Matches($content, $p.Pattern)
            $count = $matches.Count
            if ($count -gt 0) {
                if (-not $replacements.ContainsKey($p.Key)) {
                    $replacements[$p.Key] = 0
                }
                $replacements[$p.Key] += $count
                $content = $content -replace $p.Pattern, $p.Replacement
            }
        }
        
        # Replace host array patterns
        $hostPatterns = @(
            @{Pattern = '"host":\s*\[\s*"\{\{exam_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'exam_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{question_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'question_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{user_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'user_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{auth_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'auth_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{career_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'career_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{news_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'news_service_url'},
            @{Pattern = '"host":\s*\[\s*"\{\{social_service_url\}\}"\s*\]'; Replacement = '"host": ["{{gateway_url}}"]'; Key = 'social_service_url'}
        )
        
        foreach ($p in $hostPatterns) {
            $matches = [regex]::Matches($content, $p.Pattern)
            $count = $matches.Count
            if ($count -gt 0) {
                if (-not $replacements.ContainsKey($p.Key)) {
                    $replacements[$p.Key] = 0
                }
                $replacements[$p.Key] += $count
                $content = $content -replace $p.Pattern, $p.Replacement
            }
        }
        
        # Write updated content
        $content | Set-Content $file -Encoding UTF8 -NoNewline
        
        # Print summary
        Write-Host "`nReplacements made in ${file}:" -ForegroundColor Green
        $fileTotal = 0
        foreach ($key in $replacements.Keys) {
            $count = $replacements[$key]
            Write-Host "  - $key : $count occurrences" -ForegroundColor Yellow
            $fileTotal += $count
        }
        Write-Host "File total: $fileTotal`n" -ForegroundColor Green
        $totalReplacements += $fileTotal
        
    } else {
        Write-Host "Warning: File not found: $file" -ForegroundColor Red
    }
}

Write-Host "`n✅ Update complete! Total replacements across all files: $totalReplacements" -ForegroundColor Green
