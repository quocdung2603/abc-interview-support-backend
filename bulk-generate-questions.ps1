# PowerShell script to generate questions in bulk
# Generates 12,000 unique IT interview questions

param(
    [int]$TargetCount = 12000,
    [int]$BatchSize = 100,
    [long]$UserId = 1,
    [long]$ApproverId = 1,
    [switch]$DryRun
)

Write-Host "🎯 Bulk Question Generation" -ForegroundColor Magenta
Write-Host "===========================" -ForegroundColor Magenta
Write-Host ""

$QUESTION_SERVICE_URL = "http://localhost:8085"

# Check if question service is available
Write-Host "🔍 Checking if question-service is available..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/actuator/health" -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host "✅ Question service is ready!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Question service is not healthy: $($health.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Question service is not available at $QUESTION_SERVICE_URL" -ForegroundColor Red
    Write-Host "💡 Make sure the service is running (docker-compose up or mvn spring-boot:run)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Prepare request body
$requestBody = @{
    targetCount = $TargetCount
    batchSize = $BatchSize
    defaultUserId = $UserId
    defaultApproverId = $ApproverId
    dryRun = $DryRun.IsPresent
} | ConvertTo-Json

Write-Host "📝 Generation Parameters:" -ForegroundColor Cyan
Write-Host "   Target Count: $TargetCount questions" -ForegroundColor White
Write-Host "   Batch Size: $BatchSize questions per batch" -ForegroundColor White
Write-Host "   User ID: $UserId" -ForegroundColor White
Write-Host "   Approver ID: $ApproverId" -ForegroundColor White
Write-Host "   Dry Run: $($DryRun.IsPresent)" -ForegroundColor White
Write-Host ""

# Start generation
Write-Host "🚀 Starting bulk generation..." -ForegroundColor Yellow
Write-Host "⏳ This may take several minutes depending on the target count..." -ForegroundColor Yellow
Write-Host ""

$startTime = Get-Date

try {
    $result = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions/bulk-generate" `
        -Method POST `
        -ContentType "application/json" `
        -Body $requestBody `
        -TimeoutSec 1800  # 30 minutes timeout
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    if ($result.success) {
        Write-Host "✅ Bulk generation completed successfully!" -ForegroundColor Green
        Write-Host ""
        
        # Display results
        Write-Host "📊 Generation Results:" -ForegroundColor Cyan
        Write-Host "   Requested: $($result.requestedCount) questions" -ForegroundColor White
        Write-Host "   Generated: $($result.generatedCount) questions" -ForegroundColor Green
        Write-Host "   Failed: $($result.failedCount) questions" -ForegroundColor $(if ($result.failedCount -gt 0) { "Yellow" } else { "White" })
        Write-Host "   Duration: $($result.duration)" -ForegroundColor White
        Write-Host "   Job ID: $($result.jobId)" -ForegroundColor Gray
        Write-Host ""
        
        # Distribution by field
        if ($result.distributionByField -and $result.distributionByField.Count -gt 0) {
            Write-Host "📈 Distribution by Field:" -ForegroundColor Cyan
            $result.distributionByField.PSObject.Properties | Sort-Object Value -Descending | ForEach-Object {
                $percentage = [math]::Round(($_.Value / $result.generatedCount) * 100, 1)
                Write-Host "   $($_.Name): $($_.Value) questions ($percentage%)" -ForegroundColor White
            }
            Write-Host ""
        }
        
        # Distribution by level
        if ($result.distributionByLevel -and $result.distributionByLevel.Count -gt 0) {
            Write-Host "📈 Distribution by Level:" -ForegroundColor Cyan
            $result.distributionByLevel.PSObject.Properties | Sort-Object Value -Descending | ForEach-Object {
                $percentage = [math]::Round(($_.Value / $result.generatedCount) * 100, 1)
                Write-Host "   $($_.Name): $($_.Value) questions ($percentage%)" -ForegroundColor White
            }
            Write-Host ""
        }
        
        # Distribution by question type
        if ($result.distributionByQuestionType -and $result.distributionByQuestionType.Count -gt 0) {
            Write-Host "📈 Distribution by Question Type:" -ForegroundColor Cyan
            $result.distributionByQuestionType.PSObject.Properties | Sort-Object Value -Descending | ForEach-Object {
                $percentage = [math]::Round(($_.Value / $result.generatedCount) * 100, 1)
                Write-Host "   $($_.Name): $($_.Value) questions ($percentage%)" -ForegroundColor White
            }
            Write-Host ""
        }
        
        # Errors
        if ($result.errors -and $result.errors.Count -gt 0) {
            Write-Host "⚠️ Errors encountered:" -ForegroundColor Yellow
            $result.errors | ForEach-Object {
                Write-Host "   - $_" -ForegroundColor Red
            }
            Write-Host ""
        }
        
        # Performance stats
        $questionsPerSecond = [math]::Round($result.generatedCount / $duration.TotalSeconds, 2)
        Write-Host "⚡ Performance:" -ForegroundColor Cyan
        Write-Host "   Generation speed: $questionsPerSecond questions/second" -ForegroundColor White
        Write-Host "   Total time: $($duration.ToString('mm\:ss'))" -ForegroundColor White
        Write-Host ""
        
        # Sample questions
        Write-Host "📋 Fetching sample questions..." -ForegroundColor Cyan
        try {
            $sampleQuestions = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions?page=0&size=3" -TimeoutSec 10
            if ($sampleQuestions.content -and $sampleQuestions.content.Count -gt 0) {
                Write-Host "📝 Sample generated questions:" -ForegroundColor Green
                $sampleQuestions.content | ForEach-Object {
                    Write-Host "   • [$($_.fieldName) / $($_.topicName) / $($_.levelName)]" -ForegroundColor Gray
                    Write-Host "     $($_.questionContent)" -ForegroundColor White
                    Write-Host ""
                }
            }
        } catch {
            Write-Host "⚠️ Could not fetch sample questions" -ForegroundColor Yellow
        }
        
    } else {
        Write-Host "❌ Bulk generation failed!" -ForegroundColor Red
        Write-Host "   Generated: $($result.generatedCount) questions" -ForegroundColor Yellow
        Write-Host "   Failed: $($result.failedCount) questions" -ForegroundColor Red
        Write-Host ""
        
        if ($result.errors -and $result.errors.Count -gt 0) {
            Write-Host "Errors:" -ForegroundColor Red
            $result.errors | ForEach-Object {
                Write-Host "   - $_" -ForegroundColor Red
            }
        }
        
        exit 1
    }
    
} catch {
    Write-Host "❌ Failed to generate questions: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error details: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response" -ForegroundColor Red
        }
    }
    
    exit 1
}

Write-Host "🎉 Bulk question generation completed!" -ForegroundColor Green
Write-Host ""
Write-Host "🔗 Useful endpoints:" -ForegroundColor Cyan
Write-Host "   • View questions: $QUESTION_SERVICE_URL/api/questions?page=0&size=10" -ForegroundColor Gray
Write-Host "   • Swagger UI: $QUESTION_SERVICE_URL/swagger-ui.html" -ForegroundColor Gray
Write-Host "   • Health check: $QUESTION_SERVICE_URL/actuator/health" -ForegroundColor Gray
