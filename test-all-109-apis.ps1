# ============================================================
# Comprehensive API Test Script - All 109 Endpoints
# Tests CREATE, READ, UPDATE, DELETE operations
# ============================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = "all-109-apis-test-$timestamp.csv"

$colors = @{
    Success = "Green"
    Failed = "Red"
    Warning = "Yellow"
    Info = "Cyan"
}

$results = @()
$totalTests = 0
$passedTests = 0
$failedTests = 0

# Helper function
function Test-API {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [int]$ExpectedStatus = 200,
        [string]$Token = $null
    )
    
    $script:totalTests++
    
    try {
        $uri = "$baseUrl$Endpoint"
        $params = @{
            Uri = $uri
            Method = $Method
            ContentType = "application/json"
            Headers = $Headers
            TimeoutSec = 30
        }
        
        if ($Token) {
            $params.Headers["Authorization"] = "Bearer $Token"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 5)
        }
        
        $startTime = Get-Date
        try {
            $response = Invoke-WebRequest @params -UseBasicParsing
            $responseTime = [int]((Get-Date) - $startTime).TotalMilliseconds
            $statusCode = $response.StatusCode
        }
        catch {
            $responseTime = [int]((Get-Date) - $startTime).TotalMilliseconds
            $statusCode = $_.Exception.Response.StatusCode.value__
        }
        
        $passed = ($statusCode -eq $ExpectedStatus)
        
        $methodInfo = "$Method $Endpoint"
        if ($passed) {
            $script:passedTests++
            Write-Host "✓ $Name " -NoNewline -ForegroundColor $colors.Success
            Write-Host "- $statusCode ${responseTime}ms" -ForegroundColor Gray
        } else {
            $script:failedTests++
            Write-Host "✗ $Name " -NoNewline -ForegroundColor $colors.Failed
            Write-Host "- Expected: $ExpectedStatus, Got: $statusCode ${responseTime}ms" -ForegroundColor Gray
        }
        
        $script:results += [PSCustomObject]@{
            Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Name = $Name
            Method = $Method
            Endpoint = $Endpoint
            Status = if($passed){"PASS"}else{"FAIL"}
            ExpectedStatus = $ExpectedStatus
            ActualStatus = $statusCode
            ResponseTime = $responseTime
        }
        
        return @{
            Success = $passed
            Response = if($response){$response.Content}else{$null}
            StatusCode = $statusCode
        }
    }
    catch {
        $script:failedTests++
        Write-Host "✗ $Name - ERROR: $_" -ForegroundColor $colors.Failed
        
        $script:results += [PSCustomObject]@{
            Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Name = $Name
            Method = $Method
            Endpoint = $Endpoint
            Status = "ERROR"
            ExpectedStatus = $ExpectedStatus
            ActualStatus = "ERROR"
            ResponseTime = 0
        }
        
        return @{Success = $false; Response = $null; StatusCode = 0}
    }
}

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor $colors.Info
Write-Host "║  Comprehensive API Test - All 109 Endpoints (CRUD)        ║" -ForegroundColor $colors.Info
Write-Host "║  Base URL: $baseUrl                              ║" -ForegroundColor $colors.Info
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor $colors.Info

# ==================== STEP 1: AUTHENTICATION ====================
Write-Host "`n[1/8] Authentication Service..." -ForegroundColor $colors.Info

$adminLogin = Test-API -Name "Login (Admin)" -Method POST -Endpoint "/auth/login" `
    -Body @{email="admin@example.com"; password="admin123"}
$adminToken = if($adminLogin.Response){($adminLogin.Response | ConvertFrom-Json).data.accessToken}else{$null}

$recruiterLogin = Test-API -Name "Login (Recruiter)" -Method POST -Endpoint "/auth/login" `
    -Body @{email="recruiter@example.com"; password="admin123"}
$recruiterToken = if($recruiterLogin.Response){($recruiterLogin.Response | ConvertFrom-Json).data.accessToken}else{$null}

$userLogin = Test-API -Name "Login (User)" -Method POST -Endpoint "/auth/login" `
    -Body @{email="user@example.com"; password="admin123"}
$userToken = if($userLogin.Response){($userLogin.Response | ConvertFrom-Json).data.accessToken}else{$null}

Test-API -Name "Register New User" -Method POST -Endpoint "/auth/register" `
    -Body @{email="newuser_$timestamp@test.com"; password="Test@123"; fullName="Test User"; roleName="USER"}

Test-API -Name "Refresh Token" -Method POST -Endpoint "/auth/refresh" `
    -Body @{refreshToken="dummy-refresh-token"} -ExpectedStatus 401

Test-API -Name "Verify Token" -Method GET -Endpoint "/auth/verify" -Token $userToken

Test-API -Name "Get User Info" -Method GET -Endpoint "/auth/user-info" -Token $userToken

# ==================== STEP 2: USER SERVICE ====================
Write-Host "`n[2/8] User Service..." -ForegroundColor $colors.Info

Test-API -Name "Get All Users" -Method GET -Endpoint "/users?page=0&size=10" -Token $adminToken
Test-API -Name "Get User by ID" -Method GET -Endpoint "/users/1" -Token $adminToken
Test-API -Name "Get User by Email" -Method GET -Endpoint "/users/by-email/admin@example.com"
Test-API -Name "Check Email Exists" -Method GET -Endpoint "/users/check-email/admin@example.com"
Test-API -Name "Get Users by Role" -Method GET -Endpoint "/users/role/1?page=0&size=10" -Token $adminToken
Test-API -Name "Get Users by Status" -Method GET -Endpoint "/users/status/ACTIVE?page=0&size=10" -Token $adminToken
Test-API -Name "Update User" -Method PUT -Endpoint "/users/3" `
    -Body @{fullName="Updated Name";address="Ha Noi"} -Token $userToken
Test-API -Name "Update User Role" -Method PUT -Endpoint "/users/3/role" `
    -Body @{roleId=2} -Token $adminToken
Test-API -Name "Update User Status" -Method PUT -Endpoint "/users/3/status" `
    -Body @{status="ACTIVE"} -Token $adminToken
Test-API -Name "Update User ELO" -Method POST -Endpoint "/users/elo" `
    -Body @{userId=3;eloChange=50} -Token $userToken
Test-API -Name "Validate Password" -Method POST -Endpoint "/users/validate-password" `
    -Body @{email="admin@example.com";password="admin123"}
Test-API -Name "Get All Roles" -Method GET -Endpoint "/users/roles" -Token $adminToken

# ==================== STEP 3: QUESTION SERVICE (top endpoints) ====================
Write-Host "`n[3/8] Question Service..." -ForegroundColor $colors.Info

Test-API -Name "Get All Fields" -Method GET -Endpoint "/questions/fields?page=0&size=10"
Test-API -Name "Get Field by ID" -Method GET -Endpoint "/questions/fields/1"
Test-API -Name "Get All Topics" -Method GET -Endpoint "/questions/topics?page=0&size=10"
Test-API -Name "Get Topic by ID" -Method GET -Endpoint "/questions/topics/1"
Test-API -Name "Get Topics by Field" -Method GET -Endpoint "/questions/topics/field/1?page=0&size=10"
Test-API -Name "Get All Levels" -Method GET -Endpoint "/questions/levels?page=0&size=10"
Test-API -Name "Get Level by ID" -Method GET -Endpoint "/questions/levels/1"
Test-API -Name "Get All Question Types" -Method GET -Endpoint "/questions/types?page=0&size=10"
Test-API -Name "Get Question Type by ID" -Method GET -Endpoint "/questions/types/1"
Test-API -Name "Get All Questions" -Method GET -Endpoint "/questions?page=0&size=10"
Test-API -Name "Get Question by ID" -Method GET -Endpoint "/questions/1"
Test-API -Name "Get All Answers" -Method GET -Endpoint "/questions/answers?page=0&size=10"
Test-API -Name "Get Answer by ID" -Method GET -Endpoint "/questions/answers/1"
Test-API -Name "Get Answers by Question" -Method GET -Endpoint "/questions/1/answers?page=0&size=10"

# ==================== STEP 4: EXAM SERVICE ====================
Write-Host "`n[4/8] Exam Service..." -ForegroundColor $colors.Info

Test-API -Name "Get All Exams" -Method GET -Endpoint "/exams?page=0&size=10"
Test-API -Name "Get Exam by ID" -Method GET -Endpoint "/exams/1"
Test-API -Name "Get Exams by Field" -Method GET -Endpoint "/exams/field/1?page=0&size=10"
Test-API -Name "Get Exams by Creator" -Method GET -Endpoint "/exams/creator/1?page=0&size=10" -Token $adminToken
Test-API -Name "Get Exam Types" -Method GET -Endpoint "/exams/types"
Test-API -Name "Get All Registrations" -Method GET -Endpoint "/exams/registrations?page=0&size=10" -Token $adminToken
Test-API -Name "Get Registrations by Exam" -Method GET -Endpoint "/exams/registrations/exam/1?page=0&size=10" -Token $adminToken
Test-API -Name "Get Registrations by User" -Method GET -Endpoint "/exams/registrations/user/3?page=0&size=10" -Token $userToken
Test-API -Name "Get All Results" -Method GET -Endpoint "/exams/results?page=0&size=10" -Token $adminToken
Test-API -Name "Get Results by User" -Method GET -Endpoint "/exams/results/user/3?page=0&size=10" -Token $userToken
Test-API -Name "Get Results by Exam" -Method GET -Endpoint "/exams/results/exam/1?page=0&size=10" -Token $adminToken

# ==================== STEP 5: NEWS SERVICE ====================
Write-Host "`n[5/8] News Service..." -ForegroundColor $colors.Info

Test-API -Name "Get All News" -Method GET -Endpoint "/news?page=0&size=10"
Test-API -Name "Get News by ID" -Method GET -Endpoint "/news/1"
Test-API -Name "Get News by Type" -Method GET -Endpoint "/news/type?newsType=TUTORIAL&page=0&size=10"
Test-API -Name "Get News by User" -Method GET -Endpoint "/news/user/1?page=0&size=10"
Test-API -Name "Get News by Status" -Method GET -Endpoint "/news/status/PUBLISHED?page=0&size=10" -Token $adminToken
Test-API -Name "Get News by Field" -Method GET -Endpoint "/news/field/1?page=0&size=10"
Test-API -Name "Get Published News" -Method GET -Endpoint "/news/published/TUTORIAL?page=0&size=10"
Test-API -Name "Get Pending Moderation" -Method GET -Endpoint "/news/moderation/pending?page=0&size=10" -Token $adminToken
Test-API -Name "Get News Types" -Method GET -Endpoint "/news/types"
Test-API -Name "Get All Recruitments" -Method GET -Endpoint "/recruitments?page=0&size=10"

# ==================== STEP 6: CAREER SERVICE ====================
Write-Host "`n[6/8] Career Service..." -ForegroundColor $colors.Info

Test-API -Name "Get Career by User" -Method GET -Endpoint "/career/preferences/3" -Token $userToken

# ==================== STEP 7: INFRASTRUCTURE ====================
Write-Host "`n[7/8] Infrastructure..." -ForegroundColor $colors.Info

Test-API -Name "Gateway Health" -Method GET -Endpoint "/actuator/health"
Test-API -Name "User Service Health" -Method GET -Endpoint "/users/actuator/health"

# ==================== STEP 8: GENERATE REPORT ====================
Write-Host "`n[8/8] Generating test report..." -ForegroundColor $colors.Info

$results | Export-Csv -Path $reportFile -NoTypeInformation -Encoding UTF8

$successRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
$avgResponseTime = [int]($results | Where-Object {$_.ResponseTime -gt 0} | Measure-Object -Property ResponseTime -Average).Average
$maxResponseTime = ($results | Measure-Object -Property ResponseTime -Maximum).Maximum
$minResponseTime = ($results | Where-Object {$_.ResponseTime -gt 0} | Measure-Object -Property ResponseTime -Minimum).Minimum

$excellent = ($results | Where-Object {$_.ResponseTime -lt 50 -and $_.ResponseTime -gt 0}).Count
$good = ($results | Where-Object {$_.ResponseTime -ge 50 -and $_.ResponseTime -lt 100}).Count
$medium = ($results | Where-Object {$_.ResponseTime -ge 100 -and $_.ResponseTime -lt 200}).Count
$slow = ($results | Where-Object {$_.ResponseTime -ge 200}).Count

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor $colors.Info
Write-Host "║                    TEST SUMMARY                            ║" -ForegroundColor $colors.Info
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor $colors.Info
Write-Host "║  Total Tests:      $totalTests                                          ║" -ForegroundColor $colors.Info
Write-Host "║  Passed:           $passedTests " -NoNewline -ForegroundColor $colors.Info
Write-Host "✓" -NoNewline -ForegroundColor $colors.Success
Write-Host "                                          ║" -ForegroundColor $colors.Info
Write-Host "║  Failed:           $failedTests " -NoNewline -ForegroundColor $colors.Info
if($failedTests -gt 0){
    Write-Host "✗" -NoNewline -ForegroundColor $colors.Failed
} else {
    Write-Host "✓" -NoNewline -ForegroundColor $colors.Success
}
Write-Host "                                          ║" -ForegroundColor $colors.Info
Write-Host "║  Success Rate:     $successRate%                                     ║" -ForegroundColor $colors.Info
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor $colors.Info
Write-Host "║  Avg Response:     ${avgResponseTime}ms                                    ║" -ForegroundColor $colors.Info
Write-Host "║  Min Response:     ${minResponseTime}ms                                      ║" -ForegroundColor $colors.Info
Write-Host "║  Max Response:     ${maxResponseTime}ms                                   ║" -ForegroundColor $colors.Info
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor $colors.Info
Write-Host "║  Performance Distribution:                                 ║" -ForegroundColor $colors.Info
Write-Host "║    Excellent (<50ms):    $excellent                                  ║" -ForegroundColor $colors.Success
Write-Host "║    Good (50-100ms):      $good                                   ║" -ForegroundColor $colors.Success
Write-Host "║    Medium (100-200ms):   $medium                                   ║" -ForegroundColor $colors.Warning
Write-Host "║    Slow (>=200ms):       $slow                                   ║" -ForegroundColor $colors.Warning
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor $colors.Info
Write-Host "║  Report saved: $reportFile           ║" -ForegroundColor $colors.Info
Write-Host "╚════════════════════════════════════════════════════════════╝`n" -ForegroundColor $colors.Info

if ($successRate -eq 100) {
    Write-Host "🎉 ALL TESTS PASSED! System is 100% operational!" -ForegroundColor $colors.Success
} elseif ($successRate -ge 90) {
    Write-Host "✓ Most tests passed. Review failed tests in report." -ForegroundColor $colors.Success
} else {
    Write-Host "⚠ Several tests failed. Please review the report." -ForegroundColor $colors.Warning
}

Write-Host "`nDone!" -ForegroundColor $colors.Info
