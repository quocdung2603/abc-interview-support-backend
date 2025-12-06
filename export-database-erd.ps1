# Export Database Schema and Generate ERD
# This script exports database schema to SQL and generates documentation

param(
    [string]$Database = "questiondb",
    [string]$OutputDir = "database-docs"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Export Database Schema and ERD                       " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$POSTGRES_HOST = "localhost"
$POSTGRES_PORT = "5432"
$POSTGRES_USER = "postgres"
$POSTGRES_PASSWORD = "123456"

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
    Write-Host "[OK] Created output directory: $OutputDir" -ForegroundColor Green
}

# Check if PostgreSQL container is running
Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
try {
    $containerStatus = docker ps --filter "name=interview-postgres" --format "{{.Status}}"
    if (-not $containerStatus) {
        throw "PostgreSQL container is not running"
    }
    Write-Host "[OK] PostgreSQL is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] PostgreSQL container is not running!" -ForegroundColor Red
    Write-Host "[INFO] Start it with: docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

# Export schema to SQL
Write-Host "`nExporting database schema to SQL..." -ForegroundColor Yellow
$schemaFile = Join-Path $OutputDir "$Database-schema.sql"

try {
    # Use docker exec to run pg_dump
    $dumpCommand = "pg_dump -U $POSTGRES_USER -d $Database --schema-only --no-owner --no-privileges"
    
    docker exec interview-postgres $dumpCommand > $schemaFile
    
    if (Test-Path $schemaFile) {
        $fileSize = (Get-Item $schemaFile).Length
        Write-Host "[OK] Schema exported: $schemaFile ($fileSize bytes)" -ForegroundColor Green
    } else {
        throw "Schema file was not created"
    }
} catch {
    Write-Host "[ERROR] Failed to export schema: $_" -ForegroundColor Red
    exit 1
}

# Export table list
Write-Host "`nExporting table list..." -ForegroundColor Yellow
$tableListFile = Join-Path $OutputDir "$Database-tables.txt"

try {
    $query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"
    $result = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -t -c $query
    
    $result | Out-File -FilePath $tableListFile -Encoding UTF8
    Write-Host "[OK] Table list exported: $tableListFile" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Could not export table list" -ForegroundColor Yellow
}

# Export relationships
Write-Host "`nExporting foreign key relationships..." -ForegroundColor Yellow
$relationshipsFile = Join-Path $OutputDir "$Database-relationships.txt"

try {
    $query = @"
SELECT
    tc.table_name as source_table,
    kcu.column_name as source_column,
    ccu.table_name AS target_table,
    ccu.column_name AS target_column,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, kcu.column_name;
"@
    
    $result = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -c $query
    $result | Out-File -FilePath $relationshipsFile -Encoding UTF8
    Write-Host "[OK] Relationships exported: $relationshipsFile" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Could not export relationships" -ForegroundColor Yellow
}

# Generate Markdown documentation
Write-Host "`nGenerating Markdown documentation..." -ForegroundColor Yellow
$mdFile = Join-Path $OutputDir "$Database-ERD.md"

$mdContent = @"
# Database ERD: $Database

Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## Database Overview

- **Database Name**: $Database
- **PostgreSQL Version**: 15
- **Host**: $POSTGRES_HOST
- **Port**: $POSTGRES_PORT

## Tables

"@

# Get table information
try {
    $query = @"
SELECT 
    t.table_name,
    (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_name = t.table_name) as column_count,
    obj_description((quote_ident(t.table_schema)||'.'||quote_ident(t.table_name))::regclass, 'pg_class') as table_comment
FROM information_schema.tables t
WHERE t.table_schema = 'public'
ORDER BY t.table_name;
"@
    
    $tables = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -t -c $query
    
    $mdContent += "`n### Table List`n`n"
    $mdContent += "| Table Name | Columns | Description |`n"
    $mdContent += "|------------|---------|-------------|`n"
    
    $tables -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
        $parts = $_ -split '\|'
        if ($parts.Count -ge 2) {
            $tableName = $parts[0].Trim()
            $columnCount = $parts[1].Trim()
            $mdContent += "| $tableName | $columnCount | |`n"
        }
    }
    
} catch {
    Write-Host "[WARN] Could not generate table list in markdown" -ForegroundColor Yellow
}

# Add relationships section
$mdContent += "`n## Relationships (Foreign Keys)`n`n"
$mdContent += "| Source Table | Source Column | Target Table | Target Column | Constraint Name |`n"
$mdContent += "|--------------|---------------|--------------|---------------|-----------------|`n"

try {
    $query = @"
SELECT
    tc.table_name || ' | ' ||
    kcu.column_name || ' | ' ||
    ccu.table_name || ' | ' ||
    ccu.column_name || ' | ' ||
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
"@
    
    $relationships = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -t -c $query
    
    $relationships -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
        $mdContent += "| $($_.Trim()) |`n"
    }
    
} catch {
    Write-Host "[WARN] Could not generate relationships in markdown" -ForegroundColor Yellow
}

# Add Mermaid ERD diagram
$mdContent += "`n## ERD Diagram (Mermaid)`n`n"
$mdContent += "``````mermaid`n"
$mdContent += "erDiagram`n"

try {
    # Get all tables
    $query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"
    $tables = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -t -c $query
    
    $tableList = $tables -split "`n" | Where-Object { $_.Trim() } | ForEach-Object { $_.Trim() }
    
    # Add relationships
    $query = @"
SELECT
    tc.table_name || ' ||--o{ ' || ccu.table_name || ' : ' || tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
"@
    
    $relationships = docker exec interview-postgres psql -U $POSTGRES_USER -d $Database -t -c $query
    
    $relationships -split "`n" | Where-Object { $_.Trim() } | ForEach-Object {
        $mdContent += "    $($_.Trim())`n"
    }
    
} catch {
    Write-Host "[WARN] Could not generate Mermaid diagram" -ForegroundColor Yellow
}

$mdContent += "``````n`n"

# Add instructions
$mdContent += @"

## How to View ERD

### Option 1: View in GitHub/GitLab
Upload this markdown file to GitHub or GitLab - they will render the Mermaid diagram automatically.

### Option 2: Use Mermaid Live Editor
1. Copy the Mermaid code above
2. Go to https://mermaid.live
3. Paste the code to see the diagram

### Option 3: Use VS Code
Install "Markdown Preview Mermaid Support" extension in VS Code.

### Option 4: Use pgAdmin
1. Open pgAdmin
2. Connect to database: $Database
3. Right-click database → "ERD For Database"

## Files Generated

- **$Database-schema.sql**: Complete database schema
- **$Database-tables.txt**: List of all tables
- **$Database-relationships.txt**: Foreign key relationships
- **$Database-ERD.md**: This documentation file

## Database Connection Info

``````
Host: $POSTGRES_HOST
Port: $POSTGRES_PORT
Database: $Database
Username: $POSTGRES_USER
Password: $POSTGRES_PASSWORD
``````

---
Generated by export-database-erd.ps1
"@

$mdContent | Out-File -FilePath $mdFile -Encoding UTF8
Write-Host "[OK] Markdown documentation: $mdFile" -ForegroundColor Green

# Summary
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  Export Complete!                              " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nGenerated Files:" -ForegroundColor Cyan
Write-Host "   - $schemaFile" -ForegroundColor White
Write-Host "   - $tableListFile" -ForegroundColor White
Write-Host "   - $relationshipsFile" -ForegroundColor White
Write-Host "   - $mdFile" -ForegroundColor White

Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "   1. Open $mdFile to view ERD documentation" -ForegroundColor Gray
Write-Host "   2. Copy Mermaid code to https://mermaid.live for visualization" -ForegroundColor Gray
Write-Host "   3. Or use pgAdmin: Right-click database -> 'ERD For Database'" -ForegroundColor Gray

Write-Host "`nOpening documentation..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
Start-Process $mdFile

Write-Host "`n[OK] Export complete!`n" -ForegroundColor Green
