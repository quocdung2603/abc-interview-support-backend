-- Test PRACTICE Exam Type Feature
-- Run this in PostgreSQL to verify the implementation

-- 1. Check current exams
SELECT id, exam_type, title, status, created_at 
FROM exams 
ORDER BY created_at DESC 
LIMIT 10;

-- 2. Insert a PRACTICE exam (simulating what the service does)
INSERT INTO exams (user_id, exam_type, title, position, field_id, topic_ids, level_id, question_type_ids, question_count, duration, status, language, created_at, created_by)
VALUES (1, 'PRACTICE', 'SQL Test - Practice Exam', 'Developer', 1, '[1,2]', 1, '[1]', 10, 60, 'PUBLISHED', 'en', NOW(), 1)
RETURNING id, exam_type, status;

-- 3. Insert a VIRTUAL exam for comparison
INSERT INTO exams (user_id, exam_type, title, position, field_id, topic_ids, level_id, question_type_ids, question_count, duration, status, language, created_at, created_by)
VALUES (1, 'VIRTUAL', 'SQL Test - Virtual Exam', 'Developer', 1, '[1,2]', 1, '[1]', 10, 60, 'DRAFT', 'en', NOW(), 1)
RETURNING id, exam_type, status;

-- 4. Verify PRACTICE exams are PUBLISHED
SELECT 
    exam_type,
    status,
    COUNT(*) as count
FROM exams
WHERE exam_type = 'PRACTICE'
GROUP BY exam_type, status;

-- 5. Verify VIRTUAL exams are DRAFT
SELECT 
    exam_type,
    status,
    COUNT(*) as count
FROM exams
WHERE exam_type = 'VIRTUAL'
GROUP BY exam_type, status;

-- 6. Check if we can insert answers without registration for PRACTICE exams
-- Get a PRACTICE exam ID
DO $$
DECLARE
    practice_exam_id BIGINT;
BEGIN
    SELECT id INTO practice_exam_id FROM exams WHERE exam_type = 'PRACTICE' LIMIT 1;
    
    IF practice_exam_id IS NOT NULL THEN
        -- Insert answer without registration
        INSERT INTO user_answers (exam_id, user_id, question_id, answer_content, created_at)
        VALUES (practice_exam_id, 1, 1, 'Test answer for practice exam', NOW());
        
        RAISE NOTICE 'Successfully inserted answer for PRACTICE exam without registration';
    END IF;
END $$;

-- 7. Summary report
SELECT 
    'PRACTICE Exams' as category,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END) as published,
    SUM(CASE WHEN status = 'DRAFT' THEN 1 ELSE 0 END) as draft
FROM exams
WHERE exam_type = 'PRACTICE'
UNION ALL
SELECT 
    'VIRTUAL Exams' as category,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END) as published,
    SUM(CASE WHEN status = 'DRAFT' THEN 1 ELSE 0 END) as draft
FROM exams
WHERE exam_type = 'VIRTUAL'
UNION ALL
SELECT 
    'RECRUITER Exams' as category,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END) as published,
    SUM(CASE WHEN status = 'DRAFT' THEN 1 ELSE 0 END) as draft
FROM exams
WHERE exam_type = 'RECRUITER';
