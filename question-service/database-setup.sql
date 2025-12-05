-- Database Setup Script for Bulk Question Generation
-- Run this script to add constraints and indexes for optimal performance

-- ============================================================================
-- UNIQUE CONSTRAINTS
-- ============================================================================

-- Add unique constraint on question_content to prevent duplicate questions
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'unique_question_content'
    ) THEN
        ALTER TABLE questions 
        ADD CONSTRAINT unique_question_content 
        UNIQUE (question_content);
        
        RAISE NOTICE 'Added unique constraint on questions.question_content';
    ELSE
        RAISE NOTICE 'Unique constraint on questions.question_content already exists';
    END IF;
END $$;

-- ============================================================================
-- PERFORMANCE INDEXES
-- ============================================================================

-- Index on questions.field_id for filtering by field
CREATE INDEX IF NOT EXISTS idx_questions_field_id ON questions(field_id);

-- Index on questions.topic_id for filtering by topic
CREATE INDEX IF NOT EXISTS idx_questions_topic_id ON questions(topic_id);

-- Index on questions.level_id for filtering by level
CREATE INDEX IF NOT EXISTS idx_questions_level_id ON questions(level_id);

-- Index on questions.question_type_id for filtering by question type
CREATE INDEX IF NOT EXISTS idx_questions_question_type_id ON questions(question_type_id);

-- Index on questions.status for filtering approved questions
CREATE INDEX IF NOT EXISTS idx_questions_status ON questions(status);

-- Index on topics.field_id for referential integrity checks
CREATE INDEX IF NOT EXISTS idx_topics_field_id ON topics(field_id);

-- Composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_questions_composite ON questions(field_id, topic_id, level_id, question_type_id);

-- ============================================================================
-- VERIFY SETUP
-- ============================================================================

-- Display constraint information
SELECT 
    'Constraint: ' || conname as info,
    'Table: ' || tablename as detail
FROM pg_constraint c
JOIN pg_tables t ON c.conrelid = t.tablename::regclass
WHERE conname = 'unique_question_content';

-- Display index information
SELECT 
    'Index: ' || indexname as info,
    'Table: ' || tablename as detail,
    'Columns: ' || indexdef as definition
FROM pg_indexes
WHERE tablename IN ('questions', 'topics')
AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- Display statistics
SELECT 
    'Questions table' as table_name,
    COUNT(*) as row_count,
    pg_size_pretty(pg_total_relation_size('questions')) as total_size
FROM questions
UNION ALL
SELECT 
    'Topics table' as table_name,
    COUNT(*) as row_count,
    pg_size_pretty(pg_total_relation_size('topics')) as total_size
FROM topics;

RAISE NOTICE 'Database setup completed successfully!';
