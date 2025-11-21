# Test script for POST /exams/with-random-questions endpoint

Write-Host ""
Write-Host "=== Testing Create Exam with Random Questions ===" -ForegroundColor Cyan
Write-Host ""

# Login
Write-Host "1. Logging in..." -ForegroundColor Yellow
$loginHeaders = @{ 'Content-Type' = 'application/json' }
$loginBody = '{"email":"admin@example.com","password":"admin123"}'

try {
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -Headers $loginHeaders -Body $loginBody
    $token = $loginResponse.access_token
    Write-Host "   Login successful" -ForegroundColor Green
} catch {
    Write-Host "   Login failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Creating exams with random questions..." -ForegroundColor Yellow

# Test 1: ReactJS Junior
Write-Host ""
Write-Host "   Test 1: ReactJS Junior Multiple Choice" -ForegroundColor Magenta
$body1 = '{"title":"ReactJS Junior Practice","position":"Frontend Dev","duration":60,"language":"vi","field":"Lap trinh vien","topics":["ReactJS"],"level":"Junior","questionType":"Multiple Choice","numberOfQuestions":2}'
$headers1 = @{ 'Content-Type' = 'application/json; charset=utf-8'; 'X-User-Id' = '1' }

try {
    $r1 = Invoke-RestMethod -Uri 'http://localhost:8086/exams/with-random-questions' -Method POST -Headers $headers1 -Body ([System.Text.Encoding]::UTF8.GetBytes($body1)) -ContentType 'application/json; charset=utf-8'
    Write-Host "   Success!" -ForegroundColor Green
    Write-Host "     Exam ID: $($r1.examId) | Status: $($r1.status) | Questions: $($r1.questionCount)" -ForegroundColor White
} catch {
    Write-Host "   Failed" -ForegroundColor Red
}

# Test 2: Spring Boot Middle
Write-Host ""
Write-Host "   Test 2: Spring Boot Middle Open Ended" -ForegroundColor Magenta
$body2 = '{"title":"Spring Boot Advanced","position":"Backend Dev","duration":90,"language":"vi","field":"Lap trinh vien","topics":["Spring Boot"],"level":"Middle","questionType":"Open Ended","numberOfQuestions":1}'
$headers2 = @{ 'Content-Type' = 'application/json; charset=utf-8'; 'X-User-Id' = '1' }

try {
    $r2 = Invoke-RestMethod -Uri 'http://localhost:8086/exams/with-random-questions' -Method POST -Headers $headers2 -Body ([System.Text.Encoding]::UTF8.GetBytes($body2)) -ContentType 'application/json; charset=utf-8'
    Write-Host "   Success!" -ForegroundColor Green
    Write-Host "     Exam ID: $($r2.examId) | Status: $($r2.status) | Questions: $($r2.questionCount)" -ForegroundColor White
} catch {
    Write-Host "   Failed" -ForegroundColor Red
}

# Test 3: QA Tester
Write-Host ""
Write-Host "   Test 3: Automated Testing for Tester" -ForegroundColor Magenta
$body3 = '{"title":"QA Testing Practice","position":"QA Engineer","duration":45,"language":"vi","field":"Tester","topics":["Automated Testing"],"level":"Fresher","questionType":"Multiple Choice","numberOfQuestions":1}'
$headers3 = @{ 'Content-Type' = 'application/json; charset=utf-8'; 'X-User-Id' = '1' }

try {
    $r3 = Invoke-RestMethod -Uri 'http://localhost:8086/exams/with-random-questions' -Method POST -Headers $headers3 -Body ([System.Text.Encoding]::UTF8.GetBytes($body3)) -ContentType 'application/json; charset=utf-8'
    Write-Host "   Success!" -ForegroundColor Green
    Write-Host "     Exam ID: $($r3.examId) | Status: $($r3.status) | Questions: $($r3.questionCount)" -ForegroundColor White
} catch {
    Write-Host "   Failed" -ForegroundColor Red
}

# Test 4: Invalid criteria (should fail)
Write-Host ""
Write-Host "   Test 4: Invalid criteria (should fail)" -ForegroundColor Magenta
$body4 = '{"title":"Invalid Test","position":"Dev","duration":30,"language":"en","field":"Invalid","topics":["Invalid"],"level":"Senior","questionType":"Multiple Choice","numberOfQuestions":10}'
$headers4 = @{ 'Content-Type' = 'application/json; charset=utf-8'; 'X-User-Id' = '1' }

try {
    $r4 = Invoke-RestMethod -Uri 'http://localhost:8086/exams/with-random-questions' -Method POST -Headers $headers4 -Body ([System.Text.Encoding]::UTF8.GetBytes($body4)) -ContentType 'application/json; charset=utf-8'
    Write-Host "   Error: Should have failed!" -ForegroundColor Red
} catch {
    Write-Host "   Success: Correctly rejected invalid criteria" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Test Summary ===" -ForegroundColor Cyan
Write-Host "Endpoint: POST /exams/with-random-questions" -ForegroundColor White
Write-Host "Permission: USER, ADMIN, or RECRUITER" -ForegroundColor White
Write-Host "Status: DRAFT (not published)" -ForegroundColor White
Write-Host "Type: PRACTICE (for self-study)" -ForegroundColor White
Write-Host ""
Write-Host "All tests completed!" -ForegroundColor Cyan
Write-Host ""
