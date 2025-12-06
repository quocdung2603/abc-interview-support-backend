# Export Complete System ERD - All Microservices
# This script creates a comprehensive ERD for the entire system

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "     Export Complete System ERD - All Microservices             " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$POSTGRES_HOST = "localhost"
$POSTGRES_PORT = "5432"
$POSTGRES_USER = "postgres"
$POSTGRES_PASSWORD = "123456"
$OutputDir = "database-docs"
$OutputFile = Join-Path $OutputDir "COMPLETE-SYSTEM-ERD.md"

# All databases
$databases = @(
    @{Name="authdb"; Service="Auth Service"; Color="#FF6B6B"},
    @{Name="userdb"; Service="User Service"; Color="#4ECDC4"},
    @{Name="questiondb"; Service="Question Service"; Color="#45B7D1"},
    @{Name="examdb"; Service="Exam Service"; Color="#FFA07A"},
    @{Name="careerdb"; Service="Career Service"; Color="#98D8C8"},
    @{Name="newsdb"; Service="News Service"; Color="#F7DC6F"},
    @{Name="socialdb"; Service="Social Service"; Color="#BB8FCE"}
)

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# Check PostgreSQL
Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
try {
    $containerStatus = docker ps --filter "name=interview-postgres" --format "{{.Status}}"
    if (-not $containerStatus) {
        throw "PostgreSQL container is not running"
    }
    Write-Host "[OK] PostgreSQL is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] PostgreSQL container is not running!" -ForegroundColor Red
    exit 1
}

# Start building the comprehensive markdown
$mdContent = @"
# ABC Interview System - Complete Database ERD

**Generated:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## System Overview

This document contains the complete Entity Relationship Diagram for the ABC Interview microservices system.

### Microservices Architecture

The system consists of 7 microservices, each with its own database:

| Service | Database | Description |
|---------|----------|-------------|
| Auth Service | authdb | Authentication and authorization |
| User Service | userdb | User management and profiles |
| Question Service | questiondb | Question bank management |
| Exam Service | examdb | Exam creation and management |
| Career Service | careerdb | Career paths and job postings |
| News Service | newsdb | News and articles |
| Social Service | socialdb | Social features and interactions |

---

"@

# Process each database
foreach ($db in $databases) {
    $dbName = $db.Name
    $serviceName = $db.Service
    
    Write-Host "`nProcessing $serviceName ($dbName)..." -ForegroundColor Yellow
    
    $mdContent += "`n## $serviceName ($dbName)`n`n"
    
    # Get table count
    try {
        $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';"
        $tableCount = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
        $tableCount = $tableCount.Trim()
        
        $mdContent += "**Total Tables:** $tableCount`n`n"
    } catch {
        Write-Host "[WARN] Could not get table count for $dbName" -ForegroundColor Yellow
    }
    
    # Get tables with column counts
    try {
        $query = @"
SELECT 
    t.table_name,
    (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE t.table_schema = 'public'
ORDER BY t.table_name;
"@
        
        $tables = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
        
        $mdContent += "### Tables`n`n"
        $mdContent += "| Table Name | Columns |`n"
        $mdContent += "|------------|---------|`n"
        
        $tables -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
            $parts = $_ -split '\|'
            if ($parts.Count -ge 2) {
                $tableName = $parts[0].Trim()
                $columnCount = $parts[1].Trim()
                $mdContent += "| ``$tableName`` | $columnCount |`n"
            }
        }
        
        $mdContent += "`n"
        
    } catch {
        Write-Host "[WARN] Could not get tables for $dbName" -ForegroundColor Yellow
    }
    
    # Get relationships
    try {
        $query = @"
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table,
    ccu.column_name AS foreign_column
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
"@
        
        $relationships = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
        
        if ($relationships.Trim()) {
            $mdContent += "### Relationships`n`n"
            $mdContent += "| From Table | Column | To Table | Column |`n"
            $mdContent += "|------------|--------|----------|--------|`n"
            
            $relationships -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
                $parts = $_ -split '\|'
                if ($parts.Count -ge 4) {
                    $fromTable = $parts[0].Trim()
                    $fromCol = $parts[1].Trim()
                    $toTable = $parts[2].Trim()
                    $toCol = $parts[3].Trim()
                    $mdContent += "| ``$fromTable`` | ``$fromCol`` | ``$toTable`` | ``$toCol`` |`n"
                }
            }
            
            $mdContent += "`n"
        }
        
    } catch {
        Write-Host "[WARN] Could not get relationships for $dbName" -ForegroundColor Yellow
    }
    
    $mdContent += "---`n`n"
    
    Write-Host "[OK] $serviceName processed" -ForegroundColor Green
}

# Add comprehensive Mermaid ERD
Write-Host "`nGenerating comprehensive Mermaid ERD..." -ForegroundColor Yellow

$mdContent += @"
## Complete System ERD (Mermaid)

This diagram shows all tables across all microservices with their relationships.

``````mermaid
erDiagram

"@

# Add each database's tables and relationships
foreach ($db in $databases) {
    $dbName = $db.Name
    $serviceName = $db.Service
    
    $mdContent += "`n    %% $serviceName ($dbName)`n"
    
    try {
        # Get relationships for Mermaid
        $query = @"
SELECT
    tc.table_name || ' ||--o{ ' || ccu.table_name || ' : "' || kcu.column_name || '"'
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
"@
        
        $relationships = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
        
        $relationships -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
            $rel = $_.Trim()
            if ($rel) {
                # Prefix table names with database name for uniqueness
                $rel = $rel -replace '(\w+) \|\|--o\{', "${dbName}_`$1 ||--o{"
                $rel = $rel -replace '\|\|--o\{ (\w+)', "||--o{ ${dbName}_`$1"
                $mdContent += "    $rel`n"
            }
        }
        
    } catch {
        Write-Host "[WARN] Could not generate Mermaid for $dbName" -ForegroundColor Yellow
    }
}

$mdContent += "``````n`n"

# Add detailed table schemas
$mdContent += @"
---

## Detailed Table Schemas

"@

foreach ($db in $databases) {
    $dbName = $db.Name
    $serviceName = $db.Service
    
    Write-Host "`nExporting detailed schema for $serviceName..." -ForegroundColor Yellow
    
    $mdContent += "`n### $serviceName Tables`n`n"
    
    try {
        # Get all tables
        $query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"
        $tables = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
        
        $tableList = $tables -split "`n" | Where-Object { $_.Trim() } | ForEach-Object { $_.Trim() }
        
        foreach ($table in $tableList) {
            if (-not $table) { continue }
            
            $mdContent += "#### ``$table```n`n"
            
            # Get columns
            $query = @"
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = '$table'
ORDER BY ordinal_position;
"@
            
            $columns = docker exec interview-postgres psql -U $POSTGRES_USER -d $dbName -t -c $query
            
            $mdContent += "| Column | Type | Nullable | Default |`n"
            $mdContent += "|--------|------|----------|---------|`n"
            
            $columns -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
                $parts = $_ -split '\|'
                if ($parts.Count -ge 4) {
                    $colName = $parts[0].Trim()
                    $colType = $parts[1].Trim()
                    $maxLen = $parts[2].Trim()
                    $nullable = $parts[3].Trim()
                    $default = if ($parts.Count -ge 5) { $parts[4].Trim() } else { "" }
                    
                    if ($maxLen) {
                        $colType += "($maxLen)"
                    }
                    
                    $mdContent += "| ``$colName`` | $colType | $nullable | $default |`n"
                }
            }
            
            $mdContent += "`n"
        }
        
    } catch {
        Write-Host "[WARN] Could not get detailed schema for $dbName" -ForegroundColor Yellow
    }
}

# Add footer
$mdContent += @"

---

## How to Use This Document

### View ERD Diagram

1. **GitHub/GitLab**: Upload this file - Mermaid diagrams render automatically
2. **Mermaid Live Editor**: Copy Mermaid code to https://mermaid.live
3. **VS Code**: Install "Markdown Preview Mermaid Support" extension
4. **pgAdmin**: Right-click any database → "ERD For Database"

### Database Connection

``````bash
Host: $POSTGRES_HOST
Port: $POSTGRES_PORT
Username: $POSTGRES_USER
Password: $POSTGRES_PASSWORD
``````

### Access Databases

``````bash
# Connect to specific database
docker exec -it interview-postgres psql -U postgres -d questiondb

# List all databases
docker exec -it interview-postgres psql -U postgres -c "\l"

# List tables in database
docker exec -it interview-postgres psql -U postgres -d questiondb -c "\dt"
``````

### Export Individual Database

``````powershell
# Export single database
.\export-database-erd.ps1 -Database questiondb

# Export all databases
.\export-all-databases-erd.ps1
``````

---

**Generated by:** export-complete-system-erd.ps1  
**Date:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")  
**System:** ABC Interview Microservices Platform

"@

# Write to file
$mdContent | Out-File -FilePath $OutputFile -Encoding UTF8
Write-Host "[OK] Complete system ERD generated: $OutputFile" -ForegroundColor Green

# Also export SQL schemas for all databases
Write-Host "`nExporting SQL schemas for all databases..." -ForegroundColor Yellow

foreach ($db in $databases) {
    $dbName = $db.Name
    $schemaFile = Join-Path $OutputDir "$dbName-schema.sql"
    
    try {
        docker exec interview-postgres pg_dump -U $POSTGRES_USER -d $dbName --schema-only --no-owner --no-privileges > $schemaFile
        Write-Host "[OK] $dbName schema exported" -ForegroundColor Green
    } catch {
        Write-Host "[WARN] Could not export $dbName schema" -ForegroundColor Yellow
    }
}

# Summary
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "            Complete System ERD Export Finished!                " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nMain File:" -ForegroundColor Cyan
Write-Host "   $OutputFile" -ForegroundColor White

Write-Host "`nThis file contains:" -ForegroundColor Cyan
Write-Host "   - Overview of all 7 microservices" -ForegroundColor Gray
Write-Host "   - Complete table listings" -ForegroundColor Gray
Write-Host "   - All relationships (Foreign Keys)" -ForegroundColor Gray
Write-Host "   - Comprehensive Mermaid ERD diagram" -ForegroundColor Gray
Write-Host "   - Detailed table schemas with columns" -ForegroundColor Gray
Write-Host "   - Connection and usage instructions" -ForegroundColor Gray

Write-Host "`nAdditional Files:" -ForegroundColor Cyan
foreach ($db in $databases) {
    Write-Host "   - $($db.Name)-schema.sql" -ForegroundColor Gray
}

Write-Host "`nOpening documentation..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
Start-Process $OutputFile

Write-Host "`n[OK] Complete!`n" -ForegroundColor Green
