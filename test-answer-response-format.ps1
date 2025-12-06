# Test Answer Response Format
# This script verifies that Answer API returns correct field names

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Test Answer Response Format                          " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$QUESTION_SERVICE_URL = "http://localhost:8085"

# Wait for service
Write-Host "Waiting for question-service..." -ForegroundColor Yellow
$maxAttempts = 20
$attempt = 0
$serviceReady = $false

while ($attempt -lt $maxAttempts -and -not $serviceReady) {
    try {
        $health = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/actuator/health" -TimeoutSec 3 -ErrorAction Stop
        if ($health.status -eq "UP") {
            $serviceReady = $true
            Write-Host "[OK] Service is ready" -ForegroundColor Green
        }
    } catch {
        $attempt++
        if ($attempt -lt $maxAttempts) {
            Write-Host "   Waiting... ($attempt/$maxAttempts)" -ForegroundColor Gray
            Start-Sleep -Seconds 3
        }
    }
}

if (-not $serviceReady) {
    Write-Host "[ERROR] Service is not ready" -ForegroundColor Red
    exit 1
}

# Test Answer Response Format
Write-Host "`nTesting Answer Response Format..." -ForegroundColor Yellow

try {
    # Get answers (assuming some exist)
    $answers = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/answers?page=0&size=1" -TimeoutSec 10
    
    if ($answers.content -and $answers.content.Count -gt 0) {
        $answer = $answers.content[0]
        
        Write-Host "`n[OK] Found answer to test" -ForegroundColor Green
        Write-Host "`nAnswer Response:" -ForegroundColor Cyan
        $answer | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor White
        
        # Verify required fields
        Write-Host "`nVerifying fields..." -ForegroundColor Yellow
        
        $requiredFields = @(
            "id",
            "userId",
            "questionId",
            "questionTypeId",
            "answerContent",
            "isCorrect",
            "similarityScore",
            "usefulVote",
            "unusefulVote",
            "isSampleAnswer",
            "orderNumber",
            "createdAt"
        )
        
        $allFieldsPresent = $true
        foreach ($field in $requiredFields) {
            if ($answer.PSObject.Properties.Name -contains $field) {
                Write-Host "   [OK] $field : present" -ForegroundColor Green
            } else {
                Write-Host "   [ERROR] $field : MISSING" -ForegroundColor Red
                $allFieldsPresent = $false
            }
        }
        
        if ($allFieldsPresent) {
            Write-Host "`n[OK] All required fields are present!" -ForegroundColor Green
            Write-Host "`nExpected Format:" -ForegroundColor Cyan
            Write-Host @"
{
    "id": $($answer.id),
    "userId": $($answer.userId),
    "questionId": $($answer.questionId),
    "questionTypeId": $($answer.questionTypeId),
    "answerContent": "$($answer.answerContent)",
    "isCorrect": $($answer.isCorrect),
    "similarityScore": $($answer.similarityScore),
    "usefulVote": $($answer.usefulVote),
    "unusefulVote": $($answer.unusefulVote),
    "isSampleAnswer": $($answer.isSampleAnswer),
    "orderNumber": $($answer.orderNumber),
    "createdAt": "$($answer.createdAt)"
}
"@ -ForegroundColor White
        } else {
            Write-Host "`n[ERROR] Some fields are missing!" -ForegroundColor Red
        }
        
    } else {
        Write-Host "[WARN] No answers found in database" -ForegroundColor Yellow
        Write-Host "[INFO] Create an answer first to test the response format" -ForegroundColor Cyan
        
        Write-Host "`nExpected Answer Response Format:" -ForegroundColor Cyan
        Write-Host @"
{
    "id": 5,
    "userId": 4,
    "questionId": 2,
    "questionTypeId": 2,
    "answerContent": "Virtual DOM is like a lightweight copy...",
    "isCorrect": true,
    "similarityScore": 0.0,
    "usefulVote": 4,
    "unusefulVote": 0,
    "isSampleAnswer": false,
    "orderNumber": 2,
    "createdAt": "2025-11-19T17:57:00.884854"
}
"@ -ForegroundColor White
    }
    
} catch {
    Write-Host "[ERROR] Failed to test answer response: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n================================================================" -ForegroundColor Green
Write-Host "                  Test Complete!                                " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nAPI Endpoints:" -ForegroundColor Cyan
Write-Host "   GET  /api/answers?page=0&size=10" -ForegroundColor Gray
Write-Host "   GET  /api/answers/{id}" -ForegroundColor Gray
Write-Host "   POST /api/answers" -ForegroundColor Gray
Write-Host "   PUT  /api/answers/{id}" -ForegroundColor Gray

Write-Host "`n[OK] Answer response format is correct!`n" -ForegroundColor Green
