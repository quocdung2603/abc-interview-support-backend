# Script để xóa tất cả dữ liệu trong tất cả database
Write-Host "=========================================" -ForegroundColor Green
Write-Host "CLEAR ALL DATABASE DATA" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# Danh sách các database
$databases = @("authdb", "userdb", "careerdb", "questiondb", "examdb", "newsdb", "socialdb")

foreach ($db in $databases) {
    Write-Host "Processing database: $db" -ForegroundColor Yellow

    # Lấy danh sách tables và truncate từng table
    $tables = docker exec -i interview-postgres psql -U postgres -d $db -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename NOT LIKE 'flyway_%';" -t -A

    $tables | ForEach-Object {
        if ($_.Trim() -ne "") {
            Write-Host "  Truncating table: $_" -ForegroundColor Gray
            docker exec -i interview-postgres psql -U postgres -d $db -c "TRUNCATE TABLE $_ RESTART IDENTITY CASCADE;" 2>$null | Out-Null
        }
    }

    Write-Host "  Database $db cleared successfully" -ForegroundColor Green
    Write-Host ""
}

Write-Host "=========================================" -ForegroundColor Green
Write-Host "ALL DATABASE DATA CLEARED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green