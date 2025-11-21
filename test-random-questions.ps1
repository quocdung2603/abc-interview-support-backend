# Test Random Questions API
$baseUrl = "http://localhost:8080"

# First, login to get token
Write-Host "1. Login to get token..." -ForegroundColor Cyan
$loginBody = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "   ✓ Login successful, token: $($token.Substring(0,20))..." -ForegroundColor Green
} catch {
    Write-Host "   ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test random questions endpoint
Write-Host "`n2. Testing random questions endpoint..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$requestBody = @{
    examId = 1
    numberOfQuestions = 5
    field = "Lập trình viên"
    topics = @("ReactJS")
    level = "Junior"
    questionType = "Multiple Choice"
} | ConvertTo-Json -Depth 10

Write-Host "   Request body:" -ForegroundColor Yellow
Write-Host $requestBody

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/exams/questions/random" -Method POST -Headers $headers -Body $requestBody -ContentType "application/json; charset=utf-8"
    Write-Host "`n   ✓ Success!" -ForegroundColor Green
    Write-Host "   Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "`n   ✗ Failed!" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "   Error Message:" -ForegroundColor Red
    
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $reader.DiscardBufferedData()
    $responseBody = $reader.ReadToEnd()
    Write-Host $responseBody -ForegroundColor Red
}
