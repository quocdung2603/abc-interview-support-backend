# Generate BCrypt password hash using User Service
Write-Host "Generating BCrypt hash for 'admin123' using User Service..." -ForegroundColor Cyan

# Test current hash
Write-Host "`nCurrent hash in database:"
docker exec interview-postgres psql -U postgres -d userdb -c "SELECT password FROM users WHERE email='user@example.com' LIMIT 1;"

# Create temporary endpoint test
$testBody = @"
{
  "email": "user@example.com",
  "password": "admin123"
}
"@

Write-Host "`nTesting validate-password with admin123:"
try {
    $response = Invoke-WebRequest `
        -Uri "http://localhost:8082/users/validate-password" `
        -Method POST `
        -ContentType "application/json" `
        -Body $testBody `
        -UseBasicParsing
    Write-Host "Result: $($response.Content)" -ForegroundColor $(if ($response.Content -eq "true") { "Green" } else { "Red" })
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Try different password
Write-Host "`nTrying different passwords..."
$passwords = @("password", "admin", "123456", "admin123!")

foreach ($pwd in $passwords) {
    $body = "{`"email`":`"user@example.com`",`"password`":`"$pwd`"}"
    try {
        $res = Invoke-WebRequest -Uri "http://localhost:8082/users/validate-password" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
        if ($res.Content -eq "true") {
            Write-Host "  [OK] Password '$pwd' MATCHES!" -ForegroundColor Green
            break
        } else {
            Write-Host "  [X] Password '$pwd' does not match" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  [X] Password '$pwd' error" -ForegroundColor Red
    }
}
