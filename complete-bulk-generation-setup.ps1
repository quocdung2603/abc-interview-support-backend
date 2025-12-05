# Complete Bulk Question Generation Setup Script
# This script handles: Build -> Run -> Database Reset -> Initialize -> Generate Questions
# Usage: .\complete-bulk-generation-setup.ps1 [-SkipBuild] [-SkipTests] [-QuestionCount 12000] [-BatchSize 100]

param(
    [switch]$SkipBuild = $false,
    [switch]$SkipTests = $false,
    [int]$QuestionCount = 12000,
    [int]$BatchSize = 100,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$rootDir = Get-Location

# Configuration
$QUESTION_SERVICE_URL = "http://localhost:8085"
$GATEWAY_URL = "http://localhost:8080"
$MAX_WAIT_TIME = 300  # 5 minutes
$HEALTH_CHECK_INTERVAL = 5  # seconds

# Services to build (in order)
$services = @(
    "discovery-service",
    "config-service",
    "gateway-service",
    "auth-service",
    "user-service",
    "question-service",
    "exam-service",
    "career-service",
    "news-service",
    "social-service"
)

# Helper Functions
function Write-Step {
    param([string]$Message, [int]$Step)
    Write-Host "`n[$Step] $Message" -ForegroundColor Cyan
    Write-Host ("-" * 70) -ForegroundColor Gray
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Test-ServiceHealth {
    param([string]$Url, [string]$ServiceName)
    
    try {
        $response = Invoke-RestMethod -Uri "$Url/actuator/health" -TimeoutSec 5 -ErrorAction Stop
        if ($response.status -eq "UP") {
            return $true
        }
    } catch {
        return $false
    }
    return $false
}

function Wait-ForService {
    param([string]$Url, [string]$ServiceName, [int]$MaxWaitSeconds = 300)
    
    Write-Host "Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    $elapsed = 0
    
    while ($elapsed -lt $MaxWaitSeconds) {
        if (Test-ServiceHealth -Url $Url -ServiceName $ServiceName) {
            Write-Success "$ServiceName is healthy"
            return $true
        }
        Start-Sleep -Seconds $HEALTH_CHECK_INTERVAL
        $elapsed += $HEALTH_CHECK_INTERVAL
        
        if ($elapsed % 30 -eq 0) {
            Write-Host "   Still waiting... ($elapsed/$MaxWaitSeconds seconds)" -ForegroundColor Gray
        }
    }
    
    Write-Error "Timeout waiting for $ServiceName"
    return $false
}

# Banner
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   ABC Interview - Complete Bulk Question Generation Setup     " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Configuration:" -ForegroundColor Gray
Write-Host "  Root Directory: $rootDir" -ForegroundColor Gray
Write-Host "  Skip Build: $SkipBuild" -ForegroundColor Gray
Write-Host "  Skip Tests: $SkipTests" -ForegroundColor Gray
Write-Host "  Question Count: $QuestionCount" -ForegroundColor Gray
Write-Host "  Batch Size: $BatchSize" -ForegroundColor Gray

$totalStartTime = Get-Date

# ============================================================================
# STEP 1: Stop existing containers
# ============================================================================
Write-Step "Stopping existing Docker containers" 1

try {
    $existingContainers = docker ps -q 2>$null
    if ($existingContainers) {
        docker-compose down -v 2>$null | Out-Null
        Write-Success "Existing containers stopped and volumes removed"
    } else {
        Write-Info "No existing containers to stop"
    }
} catch {
    Write-Warning "No existing containers to stop"
}

# ============================================================================
# STEP 2: Build services (optional)
# ============================================================================
if (-not $SkipBuild) {
    Write-Step "Building all services" 2
    
    $buildResults = @()
    $buildStartTime = Get-Date
    
    foreach ($service in $services) {
        $serviceDir = Join-Path $rootDir $service
        
        if (-Not (Test-Path $serviceDir)) {
            Write-Warning "Service directory not found: $service (skipping)"
            $buildResults += [PSCustomObject]@{
                Service = $service
                Status = "SKIPPED"
                Duration = "0s"
            }
            continue
        }
        
        Write-Host "Building $service..." -ForegroundColor Yellow
        $startTime = Get-Date
        
        try {
            Push-Location $serviceDir
            
            # Check for Maven wrapper
            $mvnWrapper = if ($IsWindows -or $env:OS -eq "Windows_NT") { ".\mvnw.cmd" } else { "./mvnw" }
            if (-Not (Test-Path $mvnWrapper)) {
                throw "Maven wrapper not found"
            }
            
            # Build Maven command
            $mvnArgs = @("clean", "package")
            if ($SkipTests) {
                $mvnArgs += "-DskipTests"
            }
            if (-Not $Verbose) {
                $mvnArgs += "-q"
            }
            
            # Execute build
            if ($Verbose) {
                & $mvnWrapper @mvnArgs
            } else {
                $output = & $mvnWrapper @mvnArgs 2>&1
                if ($LASTEXITCODE -ne 0) {
                    throw "Build failed with exit code $LASTEXITCODE"
                }
            }
            
            $duration = "{0:F1}s" -f ((Get-Date) - $startTime).TotalSeconds
            Write-Success "$service built successfully ($duration)"
            
            $buildResults += [PSCustomObject]@{
                Service = $service
                Status = "SUCCESS"
                Duration = $duration
            }
            
        } catch {
            $duration = "{0:F1}s" -f ((Get-Date) - $startTime).TotalSeconds
            Write-Error "$service build failed ($duration)"
            Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
            
            $buildResults += [PSCustomObject]@{
                Service = $service
                Status = "FAILED"
                Duration = $duration
            }
            
            Pop-Location
            exit 1
        } finally {
            Pop-Location
        }
    }
    
    $buildDuration = "{0:F1}s" -f ((Get-Date) - $buildStartTime).TotalSeconds
    $successCount = ($buildResults | Where-Object { $_.Status -eq "SUCCESS" }).Count
    
    Write-Host ""
    Write-Success "All services built successfully ($successCount/$($services.Count)) in $buildDuration"
    
} else {
    Write-Step "Skipping build (using existing JARs)" 2
    Write-Info "Using existing JAR files from previous builds"
}

# ============================================================================
# STEP 3: Start Docker containers
# ============================================================================
Write-Step "Starting Docker containers" 3

try {
    Write-Host "Starting containers with docker-compose..." -ForegroundColor Yellow
    docker-compose up -d
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start Docker containers"
    }
    
    Write-Success "Docker containers started"
    
    # Show running containers
    Write-Host "`nRunning containers:" -ForegroundColor Gray
    docker-compose ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}" | Out-String | Write-Host -ForegroundColor Gray
    
} catch {
    Write-Error "Failed to start Docker containers: $($_.Exception.Message)"
    Write-Info "Make sure Docker Desktop is running"
    Write-Info "Try: docker-compose down; docker-compose up -d"
    exit 1
}

# ============================================================================
# STEP 4: Wait for services to be ready
# ============================================================================
Write-Step "Waiting for services to be ready" 4

Write-Info "This may take 2-3 minutes for all services to start..."

# Wait for question-service (most important)
if (-not (Wait-ForService -Url $QUESTION_SERVICE_URL -ServiceName "Question Service" -MaxWaitSeconds $MAX_WAIT_TIME)) {
    Write-Error "Question Service failed to start"
    Write-Info "Check logs with: docker-compose logs question-service"
    exit 1
}

# Give other services time to stabilize
Write-Host "Waiting for other services to stabilize (10 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# ============================================================================
# STEP 5: Reset and initialize database
# ============================================================================
Write-Step "Resetting and initializing database" 5

# Reset database
Write-Host "Resetting database (dropping all questions and answers)..." -ForegroundColor Yellow
try {
    $resetResponse = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions/reset-database" `
        -Method POST `
        -ContentType "application/json" `
        -TimeoutSec 30
    
    Write-Success "Database reset complete"
    Write-Host "   Questions deleted: $($resetResponse.questionsDeleted)" -ForegroundColor Gray
    Write-Host "   Answers deleted: $($resetResponse.answersDeleted)" -ForegroundColor Gray
    
} catch {
    Write-Warning "Failed to reset database: $($_.Exception.Message)"
    Write-Info "Continuing with initialization..."
}

# Initialize reference data
Write-Host "`nInitializing reference data..." -ForegroundColor Yellow
try {
    $initResponse = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions/initialize-reference-data" `
        -Method POST `
        -ContentType "application/json" `
        -TimeoutSec 60
    
    Write-Success "Reference data initialized"
    Write-Host "   Fields created: $($initResponse.fieldsCreated)" -ForegroundColor Gray
    Write-Host "   Topics created: $($initResponse.topicsCreated)" -ForegroundColor Gray
    Write-Host "   Levels created: $($initResponse.levelsCreated)" -ForegroundColor Gray
    Write-Host "   Question Types created: $($initResponse.questionTypesCreated)" -ForegroundColor Gray
    
} catch {
    Write-Error "Failed to initialize reference data: $($_.Exception.Message)"
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   HTTP Status: $statusCode" -ForegroundColor Red
    }
    
    Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
    Write-Host "   1. Check if question-service is running: docker ps" -ForegroundColor Gray
    Write-Host "   2. Check service logs: docker-compose logs question-service" -ForegroundColor Gray
    Write-Host "   3. Verify database connection: docker-compose logs postgres" -ForegroundColor Gray
    exit 1
}

# ============================================================================
# STEP 6: Generate bulk questions
# ============================================================================
Write-Step "Generating $QuestionCount questions" 6

Write-Info "This may take 10-20 minutes depending on your system"
Write-Host "   Batch size: $BatchSize questions per batch" -ForegroundColor Gray
Write-Host "   Estimated batches: $([Math]::Ceiling($QuestionCount / $BatchSize))" -ForegroundColor Gray

$requestBody = @{
    targetCount = $QuestionCount
    batchSize = $BatchSize
    defaultUserId = 1
    defaultApproverId = 1
    dryRun = $false
} | ConvertTo-Json

$generationStartTime = Get-Date
Write-Host "`nStarting bulk generation at $(Get-Date -Format 'HH:mm:ss')..." -ForegroundColor Yellow

try {
    $generationResponse = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions/bulk-generate" `
        -Method POST `
        -Headers @{"Content-Type" = "application/json"} `
        -Body $requestBody `
        -TimeoutSec 1800  # 30 minutes timeout
    
    $generationDuration = (Get-Date) - $generationStartTime
    
    Write-Host ""
    Write-Success "Bulk generation completed!"
    Write-Host "   Completed at: $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Generation Results:" -ForegroundColor Cyan
    Write-Host "   Requested: $($generationResponse.requestedCount)" -ForegroundColor White
    Write-Host "   Generated: $($generationResponse.generatedCount)" -ForegroundColor White
    Write-Host "   Failed: $($generationResponse.failedCount)" -ForegroundColor White
    Write-Host "   Duration: $($generationDuration.ToString('mm\:ss'))" -ForegroundColor White
    Write-Host "   Speed: $([Math]::Round($generationResponse.generatedCount / $generationDuration.TotalMinutes, 0)) questions/minute" -ForegroundColor White
    
    # Distribution by field
    if ($generationResponse.distributionByField) {
        Write-Host "`nDistribution by Field:" -ForegroundColor Cyan
        $generationResponse.distributionByField.PSObject.Properties | Sort-Object Value -Descending | ForEach-Object {
            $percentage = [Math]::Round(($_.Value / $generationResponse.generatedCount) * 100, 1)
            Write-Host "   $($_.Name): $($_.Value) ($percentage%)" -ForegroundColor Gray
        }
    }
    
    # Errors
    if ($generationResponse.errors -and $generationResponse.errors.Count -gt 0) {
        Write-Host "`nErrors encountered:" -ForegroundColor Yellow
        $generationResponse.errors | Select-Object -First 5 | ForEach-Object {
            Write-Host "   - $_" -ForegroundColor Red
        }
        if ($generationResponse.errors.Count -gt 5) {
            Write-Host "   ... and $($generationResponse.errors.Count - 5) more errors" -ForegroundColor Gray
        }
    }
    
} catch {
    $generationDuration = (Get-Date) - $generationStartTime
    Write-Error "Bulk generation failed after $($generationDuration.ToString('mm\:ss'))"
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   HTTP Status: $statusCode" -ForegroundColor Red
    }
    
    exit 1
}

# ============================================================================
# STEP 7: Verify results
# ============================================================================
Write-Step "Verifying results" 7

try {
    # Get total question count
    $allQuestions = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions?page=0&size=1" -TimeoutSec 10
    $totalQuestions = $allQuestions.totalElements
    
    Write-Host "Total questions in database: $totalQuestions" -ForegroundColor Cyan
    
    if ($totalQuestions -ge $QuestionCount) {
        Write-Success "Generated $totalQuestions questions (target: $QuestionCount)"
    } else {
        Write-Warning "Generated $totalQuestions questions (target was $QuestionCount)"
    }
    
    # Get sample questions
    Write-Host "`nSample questions:" -ForegroundColor Cyan
    $sampleQuestions = Invoke-RestMethod -Uri "$QUESTION_SERVICE_URL/api/questions?page=0&size=3" -TimeoutSec 10
    
    $sampleQuestions.content | ForEach-Object {
        Write-Host "   - [$($_.fieldName) / $($_.topicName) / $($_.levelName)]" -ForegroundColor Gray
        $contentPreview = $_.questionContent.Substring(0, [Math]::Min(80, $_.questionContent.Length))
        Write-Host "     $contentPreview..." -ForegroundColor White
    }
    
} catch {
    Write-Warning "Could not verify results: $($_.Exception.Message)"
}

# ============================================================================
# Final Summary
# ============================================================================
$totalDuration = (Get-Date) - $totalStartTime

Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                    SETUP COMPLETE!                             " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nTotal Duration: $($totalDuration.ToString('mm\:ss'))" -ForegroundColor Cyan

Write-Host "`nSystem Status:" -ForegroundColor Cyan
Write-Host "   [OK] Docker containers running" -ForegroundColor Green
Write-Host "   [OK] Database initialized with reference data" -ForegroundColor Green
Write-Host "   [OK] $QuestionCount questions generated" -ForegroundColor Green

Write-Host "`nUseful Endpoints:" -ForegroundColor Cyan
Write-Host "   - Question Service: $QUESTION_SERVICE_URL" -ForegroundColor Gray
Write-Host "   - Gateway: $GATEWAY_URL" -ForegroundColor Gray
Write-Host "   - Swagger UI: $QUESTION_SERVICE_URL/swagger-ui.html" -ForegroundColor Gray
Write-Host "   - Health Check: $QUESTION_SERVICE_URL/actuator/health" -ForegroundColor Gray

Write-Host "`nQuick Commands:" -ForegroundColor Cyan
Write-Host "   - View questions:" -ForegroundColor Gray
Write-Host "     Invoke-RestMethod `"$QUESTION_SERVICE_URL/api/questions?page=0&size=10`"" -ForegroundColor DarkGray
Write-Host "   - View logs:" -ForegroundColor Gray
Write-Host "     docker-compose logs -f question-service" -ForegroundColor DarkGray
Write-Host "   - Stop services:" -ForegroundColor Gray
Write-Host "     docker-compose down" -ForegroundColor DarkGray
Write-Host "   - Restart service:" -ForegroundColor Gray
Write-Host "     docker-compose restart question-service" -ForegroundColor DarkGray

Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "   1. Test the API endpoints using Postman or curl" -ForegroundColor Gray
Write-Host "   2. Verify question uniqueness and distribution" -ForegroundColor Gray
Write-Host "   3. Run property-based tests to validate correctness" -ForegroundColor Gray

Write-Host "`nHappy interviewing!`n" -ForegroundColor Magenta
