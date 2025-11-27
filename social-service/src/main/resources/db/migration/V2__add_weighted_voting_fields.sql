-- Add new columns to comments table for weighted voting and edit tracking
ALTER TABLE comments 
ADD COLUMN IF NOT EXISTS weighted_vote_score DOUBLE PRECISION DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS edit_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill existing comments
UPDATE comments 
SET weighted_vote_score = COALESCE(vote_count, 0)::DOUBLE PRECISION,
    edit_count = 0,
    updated_at = created_at
WHERE weighted_vote_score IS NULL;

-- Add new columns to comment_votes table for vote types and weights
ALTER TABLE comment_votes
ADD COLUMN IF NOT EXISTS vote_type VARCHAR(20) NOT NULL DEFAULT 'USEFUL',
ADD COLUMN IF NOT EXISTS vote_weight DOUBLE PRECISION NOT NULL DEFAULT 1.0;

-- Backfill existing votes
UPDATE comment_votes
SET vote_type = 'USEFUL',
    vote_weight = 1.0
WHERE vote_type IS NULL OR vote_weight IS NULL;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_comments_post_weighted_score 
ON comments(post_id, weighted_vote_score DESC, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_comments_post_user 
ON comments(post_id, user_id);

-- Add comments for documentation
COMMENT ON COLUMN comments.weighted_vote_score IS 'Sum of all weighted votes (useful adds, not useful subtracts)';
COMMENT ON COLUMN comments.edit_count IS 'Number of times this comment has been edited';
COMMENT ON COLUMN comments.updated_at IS 'Timestamp of last edit';
COMMENT ON COLUMN comment_votes.vote_type IS 'Type of vote: USEFUL or NOT_USEFUL';
COMMENT ON COLUMN comment_votes.vote_weight IS 'Weight of vote based on user ELO rank at time of voting';
