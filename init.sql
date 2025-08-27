CREATE DATABASE userservice;
CREATE DATABASE authdb;
CREATE DATABASE examdb;
CREATE DATABASE questiondb;
CREATE DATABASE careerdb;
-- Tạo bảng roles
CREATE TABLE IF NOT EXISTS roles (
                                     id          BIGSERIAL PRIMARY KEY,
                                     role_name   VARCHAR(32) NOT NULL UNIQUE,
    description TEXT
    );

-- Seed dữ liệu (bỏ qua nếu đã tồn tại)
INSERT INTO roles(role_name, description) VALUES
                                              ('STUDENT',   'Role cho sinh viên'),
                                              ('RECRUITER', 'Role cho nhà tuyển dụng'),
                                              ('ADMIN',     'Role cho quản trị viên')
    ON CONFLICT (role_name) DO NOTHING;
