-- Add indexes for exam grading performance optimization

-- Index for finding user answers by exam and user (used in history retrieval)
CREATE INDEX IF NOT EXISTS idx_user_answers_exam_user ON user_answers(exam_id, user_id);

-- Index for finding results by exam and user (used in result retrieval)
CREATE INDEX IF NOT EXISTS idx_results_exam_user ON results(exam_id, user_id);

-- Index for ordering results by completion time (used in finding most recent result)
CREATE INDEX IF NOT EXISTS idx_results_completed_at ON results(completed_at DESC);
