-- Migration: Copy data from deprecated 'topics' and 'questionTypes' columns 
-- to new 'topicIds' and 'questionTypeIds' columns for backward compatibility

-- Update topicIds from topics where topicIds is null or empty
UPDATE exams 
SET topic_ids = topics 
WHERE (topic_ids IS NULL OR topic_ids = '' OR topic_ids = '[]') 
  AND topics IS NOT NULL 
  AND topics != '' 
  AND topics != '[]';

-- Update questionTypeIds from questionTypes where questionTypeIds is null or empty
UPDATE exams 
SET question_type_ids = question_types 
WHERE (question_type_ids IS NULL OR question_type_ids = '' OR question_type_ids = '[]') 
  AND question_types IS NOT NULL 
  AND question_types != '' 
  AND question_types != '[]';
