-- Migration script to add fieldId, topicId, and levelId columns to exams table
-- These columns store numeric IDs instead of text strings to avoid Unicode encoding issues

-- Add new columns as nullable for backward compatibility
ALTER TABLE exams ADD COLUMN IF NOT EXISTS field_id BIGINT;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS topic_id BIGINT;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS level_id BIGINT;

-- Add comments to document the purpose
COMMENT ON COLUMN exams.field_id IS 'Numeric ID reference to field (replaces text-based field name)';
COMMENT ON COLUMN exams.topic_id IS 'Numeric ID reference to topic (replaces text-based topic name)';
COMMENT ON COLUMN exams.level_id IS 'Numeric ID reference to level (replaces text-based level name)';

-- Note: Foreign key constraints are not added because question-service owns those tables
-- In a production environment with shared database, you would add:
-- ALTER TABLE exams ADD CONSTRAINT fk_exam_field FOREIGN KEY (field_id) REFERENCES fields(id);
-- ALTER TABLE exams ADD CONSTRAINT fk_exam_topic FOREIGN KEY (topic_id) REFERENCES topics(id);
-- ALTER TABLE exams ADD CONSTRAINT fk_exam_level FOREIGN KEY (level_id) REFERENCES levels(id);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_exams_field_id ON exams(field_id);
CREATE INDEX IF NOT EXISTS idx_exams_topic_id ON exams(topic_id);
CREATE INDEX IF NOT EXISTS idx_exams_level_id ON exams(level_id);
