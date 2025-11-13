<#
Push images for services to Docker Hub (or any Docker registry).
Usage examples:
  # Quick push all services with tag "latest" (assumes JARs exist or build step requested)
  .\push-images.ps1 -HubUser mydockerhubuser -Tag latest -Build -SkipTests

  # Push specific services without rebuilding
  .\push-images.ps1 -HubUser mydockerhubuser -Tag v1.2 -Services @("auth-service","exam-service")

Notes:
 - You must be logged in to Docker Hub: `docker login` before running this script.
 - The script builds images from each service directory using the Dockerfile present inside.
 - Each image is tagged as <HubUser>/<service>:<Tag>
 - If you pass -Build, the script runs `build-all-services.ps1 -SkipTests` first to create the JARs.
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$HubUser,

    [string]$Tag = "latest",

    [string[]]$Services = @(
        "discovery-service",
        "config-service",
        "gateway-service",
        "auth-service",
        "user-service",
        "question-service",
        "exam-service",
        "career-service",
        "news-service"
    ),

    [switch]$Build = $false,
    [switch]$SkipTests = $false,
    [switch]$VerboseOutput = $false
)

$ErrorActionPreference = 'Stop'
$root = Get-Location

function Ensure-DockerAvailable {
    try {
        docker version > $null 2>&1
    } catch {
        throw "Docker is not available or not running. Please install/start Docker and try again."
    }
}

function Ensure-LoggedIn {
    # We attempt to read docker config to see if credentials exist for Docker Hub, but this is best-effort.
    $cfg = "$env:USERPROFILE/.docker/config.json"
    if (-Not (Test-Path $cfg)) {
        Write-Host "Could not find Docker credentials file. Please run 'docker login' and retry." -ForegroundColor Yellow
        return
    }
    try {
        $json = Get-Content $cfg -Raw | ConvertFrom-Json
        if (-Not $json.auths) {
            Write-Host "Docker config doesn't contain auths. Please run 'docker login'." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Unable to parse Docker config; ensure you're logged in (docker login)." -ForegroundColor Yellow
    }
}

# Start
Write-Host "Publish images to Docker Hub: $HubUser, tag: $Tag" -ForegroundColor Cyan
Ensure-DockerAvailable
Ensure-LoggedIn

if ($Build) {
    Write-Host "Building all services before creating images..." -ForegroundColor Yellow
    $buildArgs = @()
    if ($SkipTests) { $buildArgs += "-SkipTests" }
    if ($VerboseOutput) { $buildArgs += "-Verbose" }
    $buildCmd = Join-String -InputObject (".\build-all-services.ps1" + " " + ($buildArgs -join ' ')) -Separator ''
    # Invoke the build script
    & .\build-all-services.ps1 @($buildArgs) 
}

foreach ($svc in $Services) {
    $svcPath = Join-Path $root $svc
    if (-Not (Test-Path $svcPath)) {
        Write-Host "Skipping ${svc}: directory not found" -ForegroundColor Yellow
        continue
    }

    # Derive image name
    $imageName = "$HubUser/${svc}:$Tag"
    Write-Host "\n--- Processing $svc -> $imageName" -ForegroundColor Cyan

    # Build Docker image
    Write-Host "Building Docker image for ${svc}..." -ForegroundColor Gray
    $buildCmd = "docker build -t $imageName $svc"
    if ($VerboseOutput) { Write-Host "  Command: $buildCmd" -ForegroundColor Gray }
    
    # Suppress PowerShell error handling for docker output
    $ErrorActionPreference = 'Continue'
    try {
        $buildOutput = docker build -t $imageName $svc 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error building image for ${svc}:" -ForegroundColor Red
            Write-Host $buildOutput -ForegroundColor Red
            continue
        }
        Write-Host "Built: $imageName" -ForegroundColor Green
    } finally {
        $ErrorActionPreference = 'Stop'
    }

    # Push image
    Write-Host "Pushing image $imageName to registry..." -ForegroundColor Gray
    
    # Suppress PowerShell error handling for docker output
    $ErrorActionPreference = 'Continue'
    try {
        $pushOutput = docker push $imageName 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error pushing image ${imageName}:" -ForegroundColor Red
            Write-Host $pushOutput -ForegroundColor Red
            continue
        }
        Write-Host "Pushed: $imageName" -ForegroundColor Green
    } finally {
        $ErrorActionPreference = 'Stop'
    }
}

Write-Host "\nAll done. Images pushed to Docker Hub under '$HubUser' with tag '$Tag'." -ForegroundColor Cyan
Write-Host "Other users can pull images with: docker pull <user>/<service>:<tag>" -ForegroundColor Green
