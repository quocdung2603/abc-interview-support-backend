-- Migration script for bulk question generation feature
-- Adds unique constraint on question_content and performance indexes

-- Add unique constraint on question_content to ensure no duplicate questions
ALTER TABLE questions 
ADD CONSTRAINT IF NOT EXISTS unique_question_content 
UNIQUE (question_content);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_questions_field_id ON questions(field_id);
CREATE INDEX IF NOT EXISTS idx_questions_topic_id ON questions(topic_id);
CREATE INDEX IF NOT EXISTS idx_questions_level_id ON questions(level_id);
CREATE INDEX IF NOT EXISTS idx_questions_question_type_id ON questions(question_type_id);
CREATE INDEX IF NOT EXISTS idx_questions_status ON questions(status);

-- Create index on topics.field_id for referential integrity queries
CREATE INDEX IF NOT EXISTS idx_topics_field_id ON topics(field_id);

-- Add comment to document the purpose
COMMENT ON CONSTRAINT unique_question_content ON questions IS 'Ensures all questions have unique content for bulk generation';
