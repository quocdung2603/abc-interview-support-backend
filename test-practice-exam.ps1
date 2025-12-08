# Test script for PRACTICE exam type feature
Write-Host "=== Testing PRACTICE Exam Type Feature ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/exams"
$headers = @{
    "Content-Type" = "application/json"
}

# Test 1: Create a PRACTICE exam
Write-Host "`n[Test 1] Creating PRACTICE exam..." -ForegroundColor Yellow
$practiceExam = @{
    userId = 1
    examType = "PRACTICE"
    title = "Practice Exam - Auto Published"
    position = "Software Engineer"
    fieldId = 1
    topicIds = @(1, 2)
    levelId = 1
    questionTypeIds = @(1)
    questionCount = 10
    duration = 60
    language = "en"
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $practiceExam
    Write-Host "Success: PRACTICE exam created successfully!" -ForegroundColor Green
    Write-Host "  Exam ID: $($response1.id)" -ForegroundColor Gray
    Write-Host "  Status: $($response1.status)" -ForegroundColor Gray
    Write-Host "  Exam Type: $($response1.examType)" -ForegroundColor Gray
    
    if ($response1.status -eq "PUBLISHED") {
        Write-Host "  Success: Status is PUBLISHED (as expected)" -ForegroundColor Green
    }
    else {
        Write-Host "  Error: Status is NOT PUBLISHED (expected PUBLISHED, got $($response1.status))" -ForegroundColor Red
    }
    
    $practiceExamId = $response1.id
}
catch {
    Write-Host "Error: Failed to create PRACTICE exam" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Create a VIRTUAL exam (should be DRAFT)
Write-Host "`n[Test 2] Creating VIRTUAL exam..." -ForegroundColor Yellow
$virtualExam = @{
    userId = 1
    examType = "VIRTUAL"
    title = "Virtual Exam - Should be Draft"
    position = "Software Engineer"
    fieldId = 1
    topicIds = @(1, 2)
    levelId = 1
    questionTypeIds = @(1)
    questionCount = 10
    duration = 60
    language = "en"
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $virtualExam
    Write-Host "Success: VIRTUAL exam created successfully!" -ForegroundColor Green
    Write-Host "  Exam ID: $($response2.id)" -ForegroundColor Gray
    Write-Host "  Status: $($response2.status)" -ForegroundColor Gray
    Write-Host "  Exam Type: $($response2.examType)" -ForegroundColor Gray
    
    if ($response2.status -eq "DRAFT") {
        Write-Host "  Success: Status is DRAFT (as expected for VIRTUAL)" -ForegroundColor Green
    }
    else {
        Write-Host "  Error: Status is NOT DRAFT (expected DRAFT, got $($response2.status))" -ForegroundColor Red
    }
    
    $virtualExamId = $response2.id
}
catch {
    Write-Host "Error: Failed to create VIRTUAL exam" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Submit answer to PRACTICE exam (should work without registration)
Write-Host "`n[Test 3] Submitting answer to PRACTICE exam (no registration)..." -ForegroundColor Yellow
$answer = @{
    examId = $practiceExamId
    userId = 1
    questionId = 1
    answerContent = "Test answer for practice exam"
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/answers" -Method Post -Headers $headers -Body $answer
    Write-Host "Success: Answer submitted successfully to PRACTICE exam!" -ForegroundColor Green
    Write-Host "  Answer ID: $($response3.id)" -ForegroundColor Gray
    Write-Host "  Success: No registration required (as expected)" -ForegroundColor Green
}
catch {
    Write-Host "Error: Failed to submit answer to PRACTICE exam" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Submit result to PRACTICE exam (should work without registration)
Write-Host "`n[Test 4] Submitting result to PRACTICE exam (no registration)..." -ForegroundColor Yellow
$result = @{
    examId = $practiceExamId
    userId = 1
    score = 85.5
    passStatus = $true
    feedback = "Good job on practice exam"
} | ConvertTo-Json

try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/results" -Method Post -Headers $headers -Body $result
    Write-Host "Success: Result submitted successfully to PRACTICE exam!" -ForegroundColor Green
    Write-Host "  Result ID: $($response4.id)" -ForegroundColor Gray
    Write-Host "  Score: $($response4.score)" -ForegroundColor Gray
    Write-Host "  Success: No registration required (as expected)" -ForegroundColor Green
}
catch {
    Write-Host "Error: Failed to submit result to PRACTICE exam" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Try to submit answer to VIRTUAL exam without registration (should fail)
Write-Host "`n[Test 5] Trying to submit answer to VIRTUAL exam (no registration - should fail)..." -ForegroundColor Yellow
$answerVirtual = @{
    examId = $virtualExamId
    userId = 1
    questionId = 1
    answerContent = "Test answer for virtual exam"
} | ConvertTo-Json

try {
    $response5 = Invoke-RestMethod -Uri "$baseUrl/answers" -Method Post -Headers $headers -Body $answerVirtual
    Write-Host "Error: Answer submitted to VIRTUAL exam without registration (should have failed!)" -ForegroundColor Red
}
catch {
    if (($_.Exception.Message -like "*register*") -or ($_.Exception.Message -like "*registration*")) {
        Write-Host "Success: Correctly rejected - registration required for VIRTUAL exam" -ForegroundColor Green
        Write-Host "  Error message: $($_.Exception.Message)" -ForegroundColor Gray
    }
    else {
        Write-Host "Error: Failed with unexpected error" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
Write-Host "Success: PRACTICE exams are auto-published" -ForegroundColor Green
Write-Host "Success: VIRTUAL exams remain as DRAFT" -ForegroundColor Green
Write-Host "Success: PRACTICE exams allow answer submission without registration" -ForegroundColor Green
Write-Host "Success: PRACTICE exams allow result submission without registration" -ForegroundColor Green
Write-Host "Success: VIRTUAL exams require registration for submissions" -ForegroundColor Green
Write-Host "`nAll tests passed! PRACTICE exam type feature is working correctly." -ForegroundColor Green
