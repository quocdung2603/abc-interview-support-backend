-- Add question_type_ids column to store array of question type IDs as JSON
ALTER TABLE exams ADD COLUMN IF NOT EXISTS question_type_ids TEXT;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_exams_question_type_ids ON exams(question_type_ids);

-- Add comment
COMMENT ON COLUMN exams.question_type_ids IS 'JSON array of question type IDs for encoding-independent storage';
