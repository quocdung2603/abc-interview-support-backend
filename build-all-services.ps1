# Build all microservices individually
# This script navigates to each service directory and builds them one by one
# Usage: .\build-all-services.ps1

param(
    [switch]$SkipTests = $false,
    [switch]$Clean = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$rootDir = Get-Location

# Define services in build order (infrastructure first, then business services)
$services = @(
    "discovery-service",
    "config-service", 
    "gateway-service",
    "auth-service",
    "user-service",
    "question-service",
    "exam-service",
    "career-service",
    "news-service"
)

$buildResults = @()
$totalStartTime = Get-Date

Write-Host "=== Building All Microservices ===" -ForegroundColor Cyan
Write-Host "Root Directory: $rootDir" -ForegroundColor Gray
Write-Host "Skip Tests: $SkipTests" -ForegroundColor Gray
Write-Host "Clean Build: $Clean" -ForegroundColor Gray
Write-Host ""

foreach ($service in $services) {
    $serviceDir = Join-Path $rootDir $service
    
    if (-Not (Test-Path $serviceDir)) {
        Write-Host "WARNING: Service directory not found: $service" -ForegroundColor Yellow
        $buildResults += [PSCustomObject]@{
            Service = $service
            Status = "SKIPPED"
            Duration = "0s"
            Error = "Directory not found"
        }
        continue
    }
    
    Write-Host "Building $service..." -ForegroundColor Yellow
    $startTime = Get-Date
    
    try {
        # Navigate to service directory
        Push-Location $serviceDir
        
        # Check if Maven wrapper exists
        $mvnWrapper = if ($IsWindows -or $env:OS -eq "Windows_NT") { ".\mvnw.cmd" } else { "./mvnw" }
        if (-Not (Test-Path $mvnWrapper)) {
            throw "Maven wrapper not found in $service"
        }
        
        # Build Maven command
        $mvnArgs = @()
        if ($Clean) {
            $mvnArgs += "clean"
        }
        $mvnArgs += "package"
        if ($SkipTests) {
            $mvnArgs += "-DskipTests"
        }
        if (-Not $Verbose) {
            $mvnArgs += "-q"
        }
        
        # Execute build
        $command = "$mvnWrapper $($mvnArgs -join ' ')"
        Write-Host "  Command: $command" -ForegroundColor Gray
        
        if ($Verbose) {
            & $mvnWrapper @mvnArgs
        } else {
            $output = & $mvnWrapper @mvnArgs 2>&1
            if ($LASTEXITCODE -ne 0) {
                throw "Build failed with exit code $LASTEXITCODE`n$output"
            }
        }
        
        $duration = "{0:F1}s" -f ((Get-Date) - $startTime).TotalSeconds
        Write-Host "SUCCESS: $service built successfully ($duration)" -ForegroundColor Green
        
        $buildResults += [PSCustomObject]@{
            Service = $service
            Status = "SUCCESS"
            Duration = $duration
            Error = ""
        }
        
    } catch {
        $duration = "{0:F1}s" -f ((Get-Date) - $startTime).TotalSeconds
        Write-Host "FAILED: $service build failed ($duration)" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        
        $buildResults += [PSCustomObject]@{
            Service = $service
            Status = "FAILED"
            Duration = $duration
            Error = $_.Exception.Message
        }
    } finally {
        # Return to root directory
        Pop-Location
    }
    
    Write-Host ""
}

# Summary
$totalDuration = "{0:F1}s" -f ((Get-Date) - $totalStartTime).TotalSeconds
$successCount = ($buildResults | Where-Object { $_.Status -eq "SUCCESS" }).Count
$failedCount = ($buildResults | Where-Object { $_.Status -eq "FAILED" }).Count
$skippedCount = ($buildResults | Where-Object { $_.Status -eq "SKIPPED" }).Count

Write-Host "=== Build Summary ===" -ForegroundColor Cyan
Write-Host "Total Duration: $totalDuration" -ForegroundColor Gray
Write-Host ""

# Results table
$buildResults | Format-Table -Property Service, Status, Duration, Error -AutoSize

# Overall status
if ($failedCount -eq 0 -and $skippedCount -eq 0) {
    Write-Host "SUCCESS: All services built successfully! ($successCount/$($services.Count))" -ForegroundColor Green
} elseif ($failedCount -eq 0) {
    Write-Host "SUCCESS: All available services built successfully! ($successCount built, $skippedCount skipped)" -ForegroundColor Green
} else {
    Write-Host "ERROR: Some services failed to build! ($successCount success, $failedCount failed, $skippedCount skipped)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  - Run docker-compose build to create Docker images" -ForegroundColor White
Write-Host "  - Run docker-compose up -d to start all services" -ForegroundColor White
Write-Host "  - Check service health with docker-compose ps" -ForegroundColor White