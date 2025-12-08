# Fix topic_id values in questiondb-sample-data.sql
# Remap discontinuous topic IDs (1,2,3,6,7,8,11,12,13,16,17,18,21,22,23) to continuous (1-15)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$inputFile = Join-Path $scriptDir "questiondb-sample-data.sql"
$backupFile = Join-Path $scriptDir "questiondb-sample-data.sql.bak"

# Create backup
if (-not (Test-Path $backupFile)) {
    Copy-Item $inputFile $backupFile
    Write-Host "[OK] Backup created: $backupFile" -ForegroundColor Green
}

# Topic mapping: old -> new
$topicMap = @{
    23 = 15; 22 = 14; 21 = 13
    18 = 12; 17 = 11; 16 = 10
    13 = 9; 12 = 8; 11 = 7
    8 = 6; 7 = 5; 6 = 4
}

Write-Host "Processing file..." -ForegroundColor Yellow

# Read file as array of lines to preserve formatting
$lines = [System.IO.File]::ReadAllLines($inputFile, [System.Text.UTF8Encoding]::new($false))
$processedLines = @()
$changeCount = 0

foreach ($line in $lines) {
    # Match pattern: (user_id, topic_id, field_id, level_id, question_type_id,
    if ($line -match '^\((\d+), (\d+), (\d+), (\d+), (\d+),') {
        $userId = $matches[1]
        $topicId = [int]$matches[2]
        $fieldId = $matches[3]
        $levelId = $matches[4]
        $questionTypeId = $matches[5]
        
        # Check if remapping needed
        if ($topicMap.ContainsKey($topicId)) {
            $newTopicId = $topicMap[$topicId]
            # Replace the topic_id in the line
            $newLine = $line -replace "^(\(\d+), $topicId,", "`$1, $newTopicId,"
            $processedLines += $newLine
            $changeCount++
        } else {
            $processedLines += $line
        }
    } else {
        $processedLines += $line
    }
}

# Write back to file without BOM
[System.IO.File]::WriteAllLines($inputFile, $processedLines, [System.Text.UTF8Encoding]::new($false))

Write-Host "[OK] Fixed $changeCount topic IDs" -ForegroundColor Green

# Verify the changes
Write-Host "`nVerifying changes..." -ForegroundColor Yellow
$topicCounts = @{}
$totalQuestions = 0

foreach ($line in $processedLines) {
    if ($line -match '^\((\d+), (\d+), (\d+), (\d+), (\d+),') {
        $topicId = [int]$matches[2]
        if (-not $topicCounts.ContainsKey($topicId)) {
            $topicCounts[$topicId] = 0
        }
        $topicCounts[$topicId]++
        $totalQuestions++
    }
}

Write-Host "`nTopic distribution:"
$topicCounts.Keys | Sort-Object | ForEach-Object {
    Write-Host "  Topic $($_): $($topicCounts[$_]) questions" -ForegroundColor Cyan
}

$minTopic = ($topicCounts.Keys | Measure-Object -Minimum).Minimum
$maxTopic = ($topicCounts.Keys | Measure-Object -Maximum).Maximum

Write-Host "`nTotal questions: $totalQuestions" -ForegroundColor Green
Write-Host "Topic range: $minTopic to $maxTopic" -ForegroundColor Green

if ($maxTopic -le 15 -and $minTopic -ge 1) {
    Write-Host "`n[OK] All topic IDs are now in range 1-15!" -ForegroundColor Green
} else {
    Write-Host "`n[WARNING] Topic IDs outside expected range 1-15" -ForegroundColor Yellow
}
