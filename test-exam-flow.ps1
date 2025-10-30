# Test script for exam-service flow
$baseUrl = "http://localhost:8080"
$examServiceUrl = "http://localhost:8086"

Write-Host "=== Testing Exam Service Flow ===" -ForegroundColor Cyan

# Step 1: Create an exam
Write-Host "`n1. Creating a new exam..." -ForegroundColor Yellow
$createExamBody = @{
    userId = 1
    examType = "VIRTUAL"
    title = "Test Exam - Result Verification"
    position = "Software Engineer"
    topics = @(1, 2)
    questionTypes = @(1, 2)
    questionCount = 5
    duration = 30
    language = "Vietnamese"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$examServiceUrl/exams" -Method Post -Body $createExamBody -ContentType "application/json"
    $examId = $createResponse.id
    Write-Host "Success: Exam created with ID $examId" -ForegroundColor Green
    Write-Host "  Status: $($createResponse.status)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to create exam: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Publish the exam
Write-Host "`n2. Publishing exam..." -ForegroundColor Yellow
try {
    $publishResponse = Invoke-RestMethod -Uri "$examServiceUrl/exams/$examId/publish?userId=1" -Method Post
    Write-Host "Success: Exam published" -ForegroundColor Green
    Write-Host "  Status: $($publishResponse.status)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to publish exam: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 3: Start the exam
Write-Host "`n3. Starting exam..." -ForegroundColor Yellow
try {
    $startResponse = Invoke-RestMethod -Uri "$examServiceUrl/exams/$examId/start" -Method Post
    Write-Host "Success: Exam started" -ForegroundColor Green
    Write-Host "  Status: $($startResponse.status)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to start exam: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Submit a result
Write-Host "`n4. Submitting exam result..." -ForegroundColor Yellow
$resultBody = @{
    examId = $examId
    userId = 100
    score = 85.5
    passStatus = $true
    feedback = "Good performance on test exam"
} | ConvertTo-Json

try {
    $resultResponse = Invoke-RestMethod -Uri "$examServiceUrl/exams/results" -Method Post -Body $resultBody -ContentType "application/json"
    Write-Host "Success: Result submitted" -ForegroundColor Green
    Write-Host "  Result ID: $($resultResponse.id)" -ForegroundColor Gray
    Write-Host "  Exam ID: $($resultResponse.examId)" -ForegroundColor Gray
    Write-Host "  User ID: $($resultResponse.userId)" -ForegroundColor Gray
    Write-Host "  Score: $($resultResponse.score)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to submit result: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Red
}

# Step 5: Submit another result (different user)
Write-Host "`n5. Submitting second result..." -ForegroundColor Yellow
$result2Body = @{
    examId = $examId
    userId = 101
    score = 72.0
    passStatus = $true
    feedback = "Passed with good understanding"
} | ConvertTo-Json

try {
    $result2Response = Invoke-RestMethod -Uri "$examServiceUrl/exams/results" -Method Post -Body $result2Body -ContentType "application/json"
    Write-Host "Success: Second result submitted" -ForegroundColor Green
    Write-Host "  Result ID: $($result2Response.id)" -ForegroundColor Gray
    Write-Host "  Exam ID: $($result2Response.examId)" -ForegroundColor Gray
    Write-Host "  Score: $($result2Response.score)" -ForegroundColor Gray
} catch {
    Write-Host "Failed to submit second result: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 6: Retrieve results by exam
Write-Host "`n6. Retrieving results for exam $examId..." -ForegroundColor Yellow
try {
    $uri = "$examServiceUrl/exams/$examId/results?page=0&size=10"
    $getResultsResponse = Invoke-RestMethod -Uri $uri -Method Get
    Write-Host "Success: Results retrieved" -ForegroundColor Green
    Write-Host "  Total Elements: $($getResultsResponse.totalElements)" -ForegroundColor Gray
    Write-Host "  Number of Elements: $($getResultsResponse.numberOfElements)" -ForegroundColor Gray
    Write-Host "  Page: $($getResultsResponse.number) of $($getResultsResponse.totalPages)" -ForegroundColor Gray
    
    if ($getResultsResponse.content.Count -gt 0) {
        Write-Host "`n  Results:" -ForegroundColor Cyan
        foreach ($result in $getResultsResponse.content) {
            Write-Host "    - Result ID: $($result.id), User: $($result.userId), Score: $($result.score), ExamId: $($result.examId)" -ForegroundColor White
        }
    } else {
        Write-Host "  WARNING: No results found in response!" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Failed to retrieve results: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Red
}

# Step 7: Retrieve results by user
Write-Host "`n7. Retrieving results for user 100..." -ForegroundColor Yellow
try {
    $uri = "$examServiceUrl/exams/results/user/100?page=0&size=10"
    $userResultsResponse = Invoke-RestMethod -Uri $uri -Method Get
    Write-Host "Success: User results retrieved" -ForegroundColor Green
    Write-Host "  Total Elements: $($userResultsResponse.totalElements)" -ForegroundColor Gray
    
    if ($userResultsResponse.content.Count -gt 0) {
        foreach ($result in $userResultsResponse.content) {
            Write-Host "    - Exam: $($result.examId), Score: $($result.score)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Failed to retrieve user results: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
Write-Host "`nTest Exam ID: $examId" -ForegroundColor Green
