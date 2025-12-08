# Fix Social Service Database Schema
# This script creates missing tables in socialdb

$ErrorActionPreference = "Stop"

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "           Fix Social Service Database Schema                   " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

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

# SQL script to create missing tables
$createTablesSQL = @"
-- Create comments table if not exists
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    vote_count INTEGER DEFAULT 0,
    weighted_vote_score DOUBLE PRECISION DEFAULT 0.0,
    edit_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Create index on post_id for faster queries
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

-- Create votes table if not exists
CREATE TABLE IF NOT EXISTS votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT,
    comment_id BIGINT,
    vote_type VARCHAR(20) NOT NULL CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE', 'USEFUL', 'UNUSEFUL')),
    weight DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_votes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT chk_vote_target CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL)),
    CONSTRAINT uk_user_post_vote UNIQUE (user_id, post_id),
    CONSTRAINT uk_user_comment_vote UNIQUE (user_id, comment_id)
);

-- Create indexes on votes
CREATE INDEX IF NOT EXISTS idx_votes_user_id ON votes(user_id);
CREATE INDEX IF NOT EXISTS idx_votes_post_id ON votes(post_id);
CREATE INDEX IF NOT EXISTS idx_votes_comment_id ON votes(comment_id);

-- Create post_tags table if not exists
CREATE TABLE IF NOT EXISTS post_tags (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Create index on post_tags
CREATE INDEX IF NOT EXISTS idx_post_tags_post_id ON post_tags(post_id);
CREATE INDEX IF NOT EXISTS idx_post_tags_tag_name ON post_tags(tag_name);

-- Verify tables exist
SELECT 'posts' as table_name, COUNT(*) as exists FROM information_schema.tables WHERE table_name = 'posts'
UNION ALL
SELECT 'comments', COUNT(*) FROM information_schema.tables WHERE table_name = 'comments'
UNION ALL
SELECT 'votes', COUNT(*) FROM information_schema.tables WHERE table_name = 'votes'
UNION ALL
SELECT 'post_tags', COUNT(*) FROM information_schema.tables WHERE table_name = 'post_tags';
"@

# Save SQL to temp file
$tempSqlFile = "temp-fix-social-schema.sql"
$createTablesSQL | Out-File -FilePath $tempSqlFile -Encoding UTF8

Write-Host "Creating missing tables in socialdb..." -ForegroundColor Yellow

try {
    # Copy SQL file to container
    docker cp $tempSqlFile interview-postgres:/tmp/fix-schema.sql
    
    # Execute SQL
    $result = docker exec interview-postgres psql -U postgres -d socialdb -f /tmp/fix-schema.sql
    
    Write-Host "[OK] Schema fixed successfully" -ForegroundColor Green
    Write-Host "`nResult:" -ForegroundColor Cyan
    $result | Write-Host -ForegroundColor Gray
    
    # Clean up
    docker exec interview-postgres rm /tmp/fix-schema.sql
    Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
    
} catch {
    Write-Host "[ERROR] Failed to fix schema: $_" -ForegroundColor Red
    Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
    exit 1
}

# Verify tables
Write-Host "`nVerifying tables..." -ForegroundColor Yellow
try {
    $verifySQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('posts', 'comments', 'votes', 'post_tags') ORDER BY table_name;"
    $tables = docker exec interview-postgres psql -U postgres -d socialdb -t -c $verifySQL
    
    Write-Host "[OK] Tables in socialdb:" -ForegroundColor Green
    $tables | Write-Host -ForegroundColor White
    
} catch {
    Write-Host "[WARN] Could not verify tables" -ForegroundColor Yellow
}

# Restart social-service
Write-Host "`nRestarting social-service..." -ForegroundColor Yellow
try {
    docker-compose restart social-service | Out-Null
    Write-Host "[OK] Social service restarted" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Could not restart social-service" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "                  Schema Fix Complete!                          " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

Write-Host "`nTables Created/Verified:" -ForegroundColor Cyan
Write-Host "   - posts" -ForegroundColor White
Write-Host "   - comments" -ForegroundColor White
Write-Host "   - votes" -ForegroundColor White
Write-Host "   - post_tags" -ForegroundColor White

Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "   1. Wait for social-service to restart (30 seconds)" -ForegroundColor Gray
Write-Host "   2. Test API: Invoke-RestMethod http://localhost:8090/actuator/health" -ForegroundColor Gray
Write-Host "   3. Check logs: docker-compose logs social-service" -ForegroundColor Gray

Write-Host "`n[OK] Database schema fixed!`n" -ForegroundColor Green
