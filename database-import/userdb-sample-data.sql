-- =============================================
-- USER SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to userdb
\c userdb;

-- Insert sample roles
INSERT INTO roles(role_name, description) VALUES 
('USER', 'Role cho sinh viên/người tìm việc'),
('RECRUITER', 'Role cho nhà tuyển dụng'),
('ADMIN', 'Role cho quản trị viên')
ON CONFLICT (role_name) DO NOTHING;

-- Sample users with detailed information
-- Password for all users: admin123
INSERT INTO users(role_id, email, password, full_name, date_of_birth, address, status, is_studying, elo_score, elo_rank, verify_token, created_at) VALUES 
(3, 'admin@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Admin User', '1985-01-15', '123 Admin Street, Ho Chi Minh City', 'ACTIVE', false, 0, 'NEWBIE', NULL, NOW()),
(2, 'recruiter@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Recruiter User', '1988-03-20', '456 Recruiter Avenue, Hanoi', 'ACTIVE', false, 0, 'NEWBIE', NULL, NOW()),
(1, 'user@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'John Doe', '1995-06-10', '789 User Lane, Da Nang', 'ACTIVE', true, 1200, 'LEARNER', NULL, NOW()),
(1, 'test@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Test User', '1998-12-05', '321 Test Road, Can Tho', 'PENDING', true, 800, 'NEWBIE', 'sample-verify-token-123', NOW()),
(1, 'student@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Student User', '2000-08-15', '654 Student Street, Hue', 'ACTIVE', true, 950, 'NEWBIE', NULL, NOW()),
(1, 'developer@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Developer User', '1992-04-22', '987 Developer Boulevard, Hai Phong', 'ACTIVE', false, 1500, 'EXPERT', NULL, NOW())
ON CONFLICT (email) DO NOTHING;

-- Sample ELO history
INSERT INTO elo_history(user_id, action, points, description, created_at) VALUES 
(3, 'EXAM_COMPLETED', 50, 'Completed ReactJS exam successfully', NOW() - INTERVAL '2 days'),
(3, 'QUESTION_APPROVED', 20, 'Question approved by admin', NOW() - INTERVAL '1 day'),
(3, 'ANSWER_VOTED_UP', 5, 'Answer received positive vote', NOW() - INTERVAL '12 hours'),
(5, 'EXAM_COMPLETED', 30, 'Completed basic programming exam', NOW() - INTERVAL '3 days'),
(5, 'QUESTION_CREATED', 10, 'Created new question', NOW() - INTERVAL '1 day'),
(6, 'EXAM_COMPLETED', 100, 'Completed advanced system design exam', NOW() - INTERVAL '5 days'),
(6, 'QUESTION_APPROVED', 25, 'High-quality question approved', NOW() - INTERVAL '2 days'),
(6, 'ANSWER_MARKED_SAMPLE', 15, 'Answer marked as sample by admin', NOW() - INTERVAL '1 day');

-- Success message
SELECT 'User database sample data inserted successfully!' as message;
