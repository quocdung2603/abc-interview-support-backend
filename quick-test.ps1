# Quick API Test - Sample Endpoints
# Fast validation of key system components

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║             Quick API Test - Key Endpoints                 ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$passed = 0
$failed = 0

function Test-Endpoint {
    param($Name, $Method, $Url, $Body = $null, $Token = $null)
    
    try {
        $params = @{
            Uri = "$baseUrl$Url"
            Method = $Method
            ContentType = "application/json"
            TimeoutSec = 10
        }
        
        if ($Token) {
            $params.Headers = @{Authorization = "Bearer $Token"}
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $start = Get-Date
        $response = Invoke-WebRequest @params -UseBasicParsing
        $time = [int]((Get-Date) - $start).TotalMilliseconds
        
        Write-Host "✓ " -NoNewline -ForegroundColor Green
        Write-Host "$Name " -NoNewline
        Write-Host "($($response.StatusCode) - ${time}ms)" -ForegroundColor Gray
        $script:passed++
        return $response
    }
    catch {
        $status = $_.Exception.Response.StatusCode.value__
        Write-Host "✗ " -NoNewline -ForegroundColor Red
        Write-Host "$Name " -NoNewline
        Write-Host "($status)" -ForegroundColor Gray
        $script:failed++
        return $null
    }
}

Write-Host "[1/6] Testing Authentication..." -ForegroundColor Yellow

$adminRes = Test-Endpoint "Admin Login" POST "/auth/login" -Body @{
    email = "admin@example.com"
    password = "admin123"
}

$adminToken = $null
if ($adminRes) {
    $data = ($adminRes.Content | ConvertFrom-Json).data
    $adminToken = $data.accessToken
}

$userRes = Test-Endpoint "User Login" POST "/auth/login" -Body @{
    email = "user@example.com"  
    password = "admin123"
}

$userToken = $null
if ($userRes) {
    $data = ($userRes.Content | ConvertFrom-Json).data
    $userToken = $data.accessToken
}

Test-Endpoint "Register User" POST "/auth/register" -Body @{
    email = "test_$timestamp@test.com"
    password = "Test@123"
    fullName = "Test User"
    roleName = "USER"
}

if ($userToken) {
    Test-Endpoint "Verify Token" GET "/auth/verify" -Token $userToken
    Test-Endpoint "Get User Info" GET "/auth/user-info" -Token $userToken
}

Write-Host "`n[2/6] Testing User Service..." -ForegroundColor Yellow

if ($adminToken) {
    Test-Endpoint "Get All Users" GET "/users?page=0&size=10" -Token $adminToken
    Test-Endpoint "Get User by ID" GET "/users/1" -Token $adminToken
}

Test-Endpoint "Get User by Email" GET "/users/by-email/admin@example.com"
Test-Endpoint "Check Email" GET "/users/check-email/test@test.com"

Write-Host "`n[3/6] Testing Question Service..." -ForegroundColor Yellow

Test-Endpoint "Get Fields" GET "/questions/fields?page=0&size=10"
Test-Endpoint "Get Topics" GET "/questions/topics?page=0&size=10"
Test-Endpoint "Get Levels" GET "/questions/levels?page=0&size=10"
Test-Endpoint "Get Question Types" GET "/questions/types?page=0&size=10"
Test-Endpoint "Get Questions" GET "/questions?page=0&size=10"
Test-Endpoint "Get Answers" GET "/questions/answers?page=0&size=10"

Write-Host "`n[4/6] Testing Exam Service..." -ForegroundColor Yellow

Test-Endpoint "Get Exams" GET "/exams?page=0&size=10"
Test-Endpoint "Get Exam Types" GET "/exams/types"

if ($adminToken) {
    Test-Endpoint "Get Registrations" GET "/exams/registrations?page=0&size=10" -Token $adminToken
    Test-Endpoint "Get Results" GET "/exams/results?page=0&size=10" -Token $adminToken
}

Write-Host "`n[5/6] Testing News Service..." -ForegroundColor Yellow

Test-Endpoint "Get News" GET "/news?page=0&size=10"
Test-Endpoint "Get News by ID" GET "/news/1"
Test-Endpoint "Get News Types" GET "/news/types"
Test-Endpoint "Get Recruitments" GET "/recruitments?page=0&size=10"

Write-Host "`n[6/6] Testing Infrastructure..." -ForegroundColor Yellow

Test-Endpoint "Gateway Health" GET "/actuator/health"
# Note: Actuator endpoints are only accessible directly to containers (security best practice)
# Test real API endpoints instead
Test-Endpoint "User Service API" GET "/users/check-email/test@test.com"

# Summary
$total = $passed + $failed
$rate = [math]::Round(($passed / $total) * 100, 1)

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                    TEST SUMMARY                            ║" -ForegroundColor Cyan
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Total:    $total tests                                         ║" -ForegroundColor Cyan
Write-Host "║  Passed:   $passed " -NoNewline -ForegroundColor Cyan
Write-Host "✓" -NoNewline -ForegroundColor Green
Write-Host "                                                ║" -ForegroundColor Cyan
Write-Host "║  Failed:   $failed " -NoNewline -ForegroundColor Cyan
if ($failed -gt 0) {
    Write-Host "✗" -NoNewline -ForegroundColor Red
} else {
    Write-Host "✓" -NoNewline -ForegroundColor Green
}
Write-Host "                                                ║" -ForegroundColor Cyan
Write-Host "║  Rate:     $rate%                                          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

if ($rate -eq 100) {
    Write-Host "🎉 All tests passed!" -ForegroundColor Green
} elseif ($rate -ge 80) {
    Write-Host "✓ Most tests passed" -ForegroundColor Green
} else {
    Write-Host "⚠ Some tests failed" -ForegroundColor Yellow
}

Write-Host "`n💡 For comprehensive testing of all 109 endpoints:" -ForegroundColor Cyan
Write-Host "   Import Postman collection and run full test suite" -ForegroundColor Gray
Write-Host ""
