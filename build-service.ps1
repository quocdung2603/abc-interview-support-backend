# Quick build script for individual service
# Usage: .\build-service.ps1 <service-name> [-Clean] [-SkipTests] [-Verbose]
# Example: .\build-service.ps1 auth-service -Clean -SkipTests

param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$ServiceName,
    [switch]$SkipTests = $false,
    [switch]$Clean = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$rootDir = Get-Location
$serviceDir = Join-Path $rootDir $ServiceName

Write-Host "=== Building $ServiceName ===" -ForegroundColor Cyan

# Validate service directory
if (-Not (Test-Path $serviceDir)) {
    Write-Host "ERROR: Service directory not found: $ServiceName" -ForegroundColor Red
    Write-Host "Available services:" -ForegroundColor Yellow
    Get-ChildItem -Directory | Where-Object { Test-Path (Join-Path $_.FullName "pom.xml") } | ForEach-Object {
        Write-Host "  - $($_.Name)" -ForegroundColor White
    }
    exit 1
}

try {
    # Navigate to service directory
    Push-Location $serviceDir
    Write-Host "Directory: $serviceDir" -ForegroundColor Gray
    
    # Check for Maven wrapper
    $mvnWrapper = if ($IsWindows -or $env:OS -eq "Windows_NT") { ".\mvnw.cmd" } else { "./mvnw" }
    if (-Not (Test-Path $mvnWrapper)) {
        Write-Host "WARNING: Maven wrapper not found, trying 'mvn'..." -ForegroundColor Yellow
        $mvnWrapper = "mvn"
    }
    
    # Build command arguments
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
    Write-Host "Command: $command" -ForegroundColor Yellow
    
    $startTime = Get-Date
    
    if ($Verbose) {
        & $mvnWrapper @mvnArgs
    } else {
        $output = & $mvnWrapper @mvnArgs 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERROR: Build output:" -ForegroundColor Red
            Write-Host $output -ForegroundColor Red
            throw "Build failed with exit code $LASTEXITCODE"
        }
    }
    
    $duration = "{0:F1}s" -f ((Get-Date) - $startTime).TotalSeconds
    
    # Check if JAR was created
    $jarFile = Get-ChildItem -Path "target" -Filter "*$ServiceName*.jar" -Exclude "*original*" | Select-Object -First 1
    if ($jarFile) {
        $jarSize = "{0:F1} MB" -f ($jarFile.Length / 1MB)
        Write-Host "SUCCESS: $ServiceName built successfully!" -ForegroundColor Green
        Write-Host "   Duration: $duration" -ForegroundColor Gray
        Write-Host "   JAR: $($jarFile.Name) ($jarSize)" -ForegroundColor Gray
    } else {
        Write-Host "WARNING: Build completed but JAR file not found in target/" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "ERROR: Build failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Next steps for $ServiceName:" -ForegroundColor Yellow
Write-Host "  - Run 'docker-compose build $ServiceName' to update Docker image" -ForegroundColor White
Write-Host "  - Run 'docker-compose restart $ServiceName' to deploy changes" -ForegroundColor White