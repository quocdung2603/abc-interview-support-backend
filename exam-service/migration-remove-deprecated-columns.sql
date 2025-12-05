-- Migration: Remove deprecated columns from exams table
-- Date: 2025-11-27
-- Description: Remove 'topics' and 'question_types' columns that have been replaced by 'topic_ids' and 'question_type_ids'

-- Step 1: Backup data from old columns to new columns (if needed)
-- This ensures any data in old columns is migrated to new columns
UPDATE exams 
SET topic_ids = topics 
WHERE topic_ids IS NULL AND topics IS NOT NULL;

UPDATE exams 
SET question_type_ids = question_types 
WHERE question_type_ids IS NULL AND question_types IS NOT NULL;

-- Step 2: Drop the deprecated columns
ALTER TABLE exams DROP COLUMN IF EXISTS topics;
ALTER TABLE exams DROP COLUMN IF EXISTS question_types;

-- Verify the changes
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'exams';
