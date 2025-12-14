-- Migration to add new vote percentage tracking fields to comments table
-- This supports the weighted voting percentage system based on ELO ranks

-- Add new columns for tracking vote counts by type
ALTER TABLE comments 
ADD COLUMN useful_vote_count INTEGER DEFAULT 0,
ADD COLUMN not_useful_vote_count INTEGER DEFAULT 0,
ADD COLUMN useful_percentage DOUBLE PRECISION DEFAULT 50.0,
ADD COLUMN not_useful_percentage DOUBLE PRECISION DEFAULT 50.0;

-- Update existing comments to have default values
UPDATE comments 
SET useful_vote_count = 0,
    not_useful_vote_count = 0,
    useful_percentage = 50.0,
    not_useful_percentage = 50.0
WHERE useful_vote_count IS NULL;

-- Add constraints to ensure data integrity
ALTER TABLE comments 
ADD CONSTRAINT check_useful_percentage CHECK (useful_percentage >= 0 AND useful_percentage <= 100),
ADD CONSTRAINT check_not_useful_percentage CHECK (not_useful_percentage >= 0 AND not_useful_percentage <= 100);

-- Add comment for documentation
COMMENT ON COLUMN comments.useful_vote_count IS 'Count of useful votes received';
COMMENT ON COLUMN comments.not_useful_vote_count IS 'Count of not useful votes received';
COMMENT ON COLUMN comments.useful_percentage IS 'Weighted percentage of useful votes (based on voter ELO)';
COMMENT ON COLUMN comments.not_useful_percentage IS 'Weighted percentage of not useful votes (based on voter ELO)';
