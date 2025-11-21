-- =============================================
-- AUTH SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to authdb
\c authdb;

-- Insert sample roles
INSERT INTO roles(role_name, description) VALUES 
('USER', 'Role cho sinh viên/người tìm việc'),
('RECRUITER', 'Role cho nhà tuyển dụng'),
('ADMIN', 'Role cho quản trị viên')
ON CONFLICT (role_name) DO NOTHING;

-- Sample data for testing authentication
-- Note: In production, passwords should be properly hashed
-- These are just for testing purposes

-- Sample users for testing (password: 'admin123' hashed with BCrypt)
INSERT INTO users(email, password, full_name, role_id, status, created_at) VALUES 
('admin@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Admin User', 3, 'ACTIVE', NOW()),
('recruiter@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Recruiter User', 2, 'ACTIVE', NOW()),
('user@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Regular User', 1, 'ACTIVE', NOW()),
('test@example.com', '$2a$10$.ox16FHE5rz4MiHNChR0jeJWy40xPDCIuL/ShGaP.jhLSV2kH0/7.', 'Test User', 1, 'PENDING', NOW())
ON CONFLICT (email) DO NOTHING;

-- Sample verification tokens
UPDATE users SET verify_token = 'sample-verify-token-123' WHERE email = 'test@example.com';

-- Success message
SELECT 'Auth database sample data inserted successfully!' as message;
