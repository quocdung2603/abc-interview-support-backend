CREATE DATABASE userservice;
CREATE DATABASE authdb;
CREATE DATABASE examdb;
CREATE DATABASE questiondb;
CREATE DATABASE careerdb;
INSERT INTO role (role_name, description)
VALUES
    ('STUDENT', 'Role cho sinh viên'),
    ('RECRUITER', 'Role cho nhà tuyển dụng'),
    ('ADMIN', 'Role cho quản trị viên');
