-- Fix topic_id values to be continuous (1-15) instead of discontinuous (1,2,3,6,7,8,11,12,13,16,17,18,21,22,23)
-- Field 2 (Backend): Remap topics 6→4, 7→5, 8→6
-- Field 3 (Data Science): Remap topics 11→7, 12→8, 13→9
-- Field 4 (Machine Learning): Remap topics 16→10, 17→11, 18→12
-- Field 5 (DevOps): Remap topics 21→13, 22→14, 23→15

BEGIN;

-- Field 2: Backend (topics 6,7,8 → 4,5,6)
UPDATE questions SET topic_id = 4 WHERE topic_id = 6;
UPDATE questions SET topic_id = 5 WHERE topic_id = 7;
UPDATE questions SET topic_id = 6 WHERE topic_id = 8;

-- Field 3: Data Science (topics 11,12,13 → 7,8,9)
UPDATE questions SET topic_id = 7 WHERE topic_id = 11;
UPDATE questions SET topic_id = 8 WHERE topic_id = 12;
UPDATE questions SET topic_id = 9 WHERE topic_id = 13;

-- Field 4: Machine Learning (topics 16,17,18 → 10,11,12)
UPDATE questions SET topic_id = 10 WHERE topic_id = 16;
UPDATE questions SET topic_id = 11 WHERE topic_id = 17;
UPDATE questions SET topic_id = 12 WHERE topic_id = 18;

-- Field 5: DevOps (topics 21,22,23 → 13,14,15)
UPDATE questions SET topic_id = 13 WHERE topic_id = 21;
UPDATE questions SET topic_id = 14 WHERE topic_id = 22;
UPDATE questions SET topic_id = 15 WHERE topic_id = 23;

-- Verify the changes
SELECT topic_id, COUNT(*) as question_count 
FROM questions 
GROUP BY topic_id 
ORDER BY topic_id;

COMMIT;
