-- Add new fields to posts table for field, topic, level, post type, and status
ALTER TABLE posts
ADD COLUMN IF NOT EXISTS field_id BIGINT,
ADD COLUMN IF NOT EXISTS topic_id BIGINT,
ADD COLUMN IF NOT EXISTS level_id BIGINT,
ADD COLUMN IF NOT EXISTS post_type VARCHAR(20) NOT NULL DEFAULT 'DISCUSSION',
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED';

-- Set default values for existing records
-- Assume existing posts are DISCUSSION type and PUBLISHED status
UPDATE posts
SET post_type = 'DISCUSSION',
    status = 'PUBLISHED'
WHERE post_type IS NULL OR status IS NULL;

-- Add indexes for filtering performance
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_user_status ON posts(user_id, status);
CREATE INDEX IF NOT EXISTS idx_posts_field ON posts(field_id);
CREATE INDEX IF NOT EXISTS idx_posts_topic ON posts(topic_id);
CREATE INDEX IF NOT EXISTS idx_posts_post_type ON posts(post_type);

-- Add comments for documentation
COMMENT ON COLUMN posts.field_id IS 'Reference to field/category ID from another service';
COMMENT ON COLUMN posts.topic_id IS 'Reference to topic ID from another service';
COMMENT ON COLUMN posts.level_id IS 'Optional reference to difficulty level ID';
COMMENT ON COLUMN posts.post_type IS 'Type of post: DISCUSSION or QUESTION';
COMMENT ON COLUMN posts.status IS 'Post status: DRAFT, PUBLISHED, or LOCKED';
