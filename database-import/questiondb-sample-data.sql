-- =============================================
-- QUESTION SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to questiondb
\c questiondb;

-- Insert sample fields
INSERT INTO fields(name, description) VALUES 
('Lập trình viên', 'Ngành lập trình phần mềm'),
('Business Analyst', 'Phân tích nghiệp vụ'),
('Tester', 'Kiểm thử phần mềm'),
('DevOps', 'Vận hành và triển khai'),
('Data Science', 'Khoa học dữ liệu'),
('UI/UX Design', 'Thiết kế giao diện người dùng')
ON CONFLICT DO NOTHING;

-- Insert sample topics
INSERT INTO topics(field_id, name, description) VALUES 
(1, 'ReactJS', 'Thư viện JavaScript cho UI'),
(1, 'VueJS', 'Framework JavaScript cho UI'),
(1, 'Angular', 'Framework TypeScript cho UI'),
(1, 'Spring Boot', 'Framework Java cho backend'),
(1, 'Node.js', 'Runtime JavaScript cho backend'),
(1, 'Python', 'Ngôn ngữ lập trình Python'),
(1, 'Java', 'Ngôn ngữ lập trình Java'),
(1, 'JavaScript', 'Ngôn ngữ lập trình JavaScript'),
(2, 'Requirements Analysis', 'Phân tích yêu cầu'),
(2, 'Process Modeling', 'Mô hình hóa quy trình'),
(2, 'Stakeholder Management', 'Quản lý các bên liên quan'),
(3, 'Manual Testing', 'Kiểm thử thủ công'),
(3, 'Automated Testing', 'Kiểm thử tự động'),
(3, 'Performance Testing', 'Kiểm thử hiệu suất'),
(3, 'Security Testing', 'Kiểm thử bảo mật'),
(4, 'Docker', 'Containerization'),
(4, 'Kubernetes', 'Container orchestration'),
(4, 'AWS', 'Amazon Web Services'),
(4, 'CI/CD', 'Continuous Integration/Deployment'),
(5, 'Machine Learning', 'Học máy'),
(5, 'Data Analysis', 'Phân tích dữ liệu'),
(5, 'Big Data', 'Dữ liệu lớn'),
(6, 'UI Design', 'Thiết kế giao diện'),
(6, 'UX Research', 'Nghiên cứu trải nghiệm người dùng'),
(6, 'Prototyping', 'Tạo mẫu thử nghiệm')
ON CONFLICT DO NOTHING;

-- Insert sample levels
INSERT INTO levels(name, description) VALUES 
('Fresher', 'Mới ra trường, 0-1 năm kinh nghiệm'),
('Junior', '1-2 năm kinh nghiệm'),
('Middle', '2-4 năm kinh nghiệm'),
('Senior', '4+ năm kinh nghiệm'),
('Lead', '5+ năm kinh nghiệm, có khả năng dẫn dắt team'),
('Architect', '7+ năm kinh nghiệm, thiết kế hệ thống')
ON CONFLICT DO NOTHING;

-- Insert sample question types
INSERT INTO question_types(name, description) VALUES 
('Multiple Choice', 'Câu hỏi trắc nghiệm'),
('Open Ended', 'Câu hỏi tự luận'),
('True/False', 'Câu hỏi đúng/sai'),
('Fill in the Blank', 'Câu hỏi điền vào chỗ trống'),
('Code Review', 'Review code'),
('System Design', 'Thiết kế hệ thống'),
('Algorithm', 'Thuật toán'),
('Database Design', 'Thiết kế cơ sở dữ liệu')
ON CONFLICT DO NOTHING;

-- Insert sample questions
INSERT INTO questions(user_id, topic_id, field_id, level_id, question_type_id, question_content, question_answer, similarity_score, status, language, created_at, approved_at, approved_by, useful_vote, unuseful_vote) VALUES 
(3, 1, 1, 2, 1, 'What is ReactJS and what are its main features?', 'ReactJS is a JavaScript library for building user interfaces. Main features include: Virtual DOM, Component-based architecture, JSX, One-way data binding, and React Hooks.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', 1, 15, 2),
(3, 1, 1, 2, 2, 'Explain the concept of Virtual DOM in ReactJS', 'Virtual DOM is a JavaScript representation of the real DOM. React creates a virtual copy of the DOM, makes changes to it, and then efficiently updates the real DOM only where changes occurred, improving performance.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', 1, 12, 1),
(4, 2, 1, 1, 1, 'What is VueJS and how does it differ from ReactJS?', 'VueJS is a progressive JavaScript framework. Key differences: Vue uses templates vs JSX, Vue has two-way data binding vs React one-way, Vue is easier to learn, React has larger ecosystem.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', 1, 8, 0),
(5, 4, 1, 3, 2, 'Describe the Spring Boot auto-configuration feature', 'Spring Boot auto-configuration automatically configures your Spring application based on the dependencies you have added. It uses @EnableAutoConfiguration annotation and can be customized through application.properties.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', 1, 20, 1),
(6, 1, 1, 4, 6, 'Design a scalable e-commerce system architecture', 'A scalable e-commerce system should include: Load balancers, CDN, Microservices architecture, Database sharding, Caching layers (Redis), Message queues, API Gateway, and monitoring systems.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', 1, 25, 0),
(3, 9, 2, 2, 2, 'What is requirements analysis and why is it important?', 'Requirements analysis is the process of determining user expectations for a new or modified product. It is important because it helps ensure the final product meets user needs and reduces development costs.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', 1, 10, 1),
(4, 13, 3, 1, 1, 'What is the difference between unit testing and integration testing?', 'Unit testing tests individual components in isolation, while integration testing tests how different components work together. Unit tests are faster and more focused, integration tests catch interface issues.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', 1, 6, 0),
(5, 16, 4, 2, 2, 'Explain Docker containers and their benefits', 'Docker containers are lightweight, portable units that package applications and their dependencies. Benefits include: Consistency across environments, resource efficiency, easy scaling, and simplified deployment.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', 1, 18, 2),
(6, 20, 5, 3, 2, 'What is machine learning and give examples of its applications', 'Machine learning is a subset of AI that enables computers to learn from data without explicit programming. Applications include: Image recognition, recommendation systems, fraud detection, and natural language processing.', 0.0, 'APPROVED', 'en', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '6 hours', 1, 14, 1),
(3, 1, 1, 2, 1, 'What are React Hooks and name some commonly used ones?', 'React Hooks are functions that let you use state and other React features in functional components. Common hooks include: useState, useEffect, useContext, useReducer, and useCallback.', 0.0, 'PENDING', 'en', NOW() - INTERVAL '2 hours', NULL, NULL, 0, 0);

-- Insert sample answers
INSERT INTO answers(user_id, question_id, question_type_id, content, is_correct, similarity_score, useful_vote, unuseful_vote, is_sample_answer, order_number, created_at) VALUES
(3, 1, 1, 'ReactJS is a JavaScript library for building user interfaces. Main features include: Virtual DOM, Component-based architecture, JSX, One-way data binding, and React Hooks.', true, 0.0, 8, 1, true, 1, NOW() - INTERVAL '9 days'),
(4, 1, 1, 'ReactJS is a frontend library that helps create interactive UIs. Key features are Virtual DOM for performance, reusable components, and declarative programming.', true, 0.0, 5, 0, false, 2, NOW() - INTERVAL '8 days'),
(5, 1, 1, 'React is a library for building web applications. It uses JSX syntax and has features like state management and component lifecycle methods.', true, 0.0, 3, 1, false, 3, NOW() - INTERVAL '7 days'),
(3, 2, 2, 'Virtual DOM is a JavaScript representation of the real DOM. React creates a virtual copy, makes changes to it, and then efficiently updates the real DOM only where changes occurred, improving performance.', true, 0.0, 10, 0, true, 1, NOW() - INTERVAL '6 days'),
(4, 2, 2, 'Virtual DOM is like a lightweight copy of the real DOM that React uses to optimize updates and improve rendering performance.', true, 0.0, 4, 0, false, 2, NOW() - INTERVAL '5 days'),
(6, 3, 1, 'VueJS is a progressive JavaScript framework. Key differences: Vue uses templates vs JSX, Vue has two-way data binding vs React one-way, Vue is easier to learn, React has larger ecosystem.', true, 0.0, 6, 0, true, 1, NOW() - INTERVAL '4 days'),
(3, 4, 2, 'Spring Boot auto-configuration automatically configures your Spring application based on dependencies. It uses @EnableAutoConfiguration and can be customized through application.properties.', true, 0.0, 12, 1, true, 1, NOW() - INTERVAL '3 days'),
(5, 4, 2, 'Auto-configuration in Spring Boot reduces boilerplate code by automatically setting up beans based on classpath dependencies and configuration properties.', true, 0.0, 7, 0, false, 2, NOW() - INTERVAL '2 days'),
(6, 5, 6, 'A scalable e-commerce system should include: Load balancers, CDN, Microservices architecture, Database sharding, Caching layers (Redis), Message queues, API Gateway, and monitoring systems.', true, 0.0, 15, 0, true, 1, NOW() - INTERVAL '1 day'),
(3, 6, 2, 'Requirements analysis determines user expectations for a product. It is important because it ensures the final product meets user needs and reduces development costs.', true, 0.0, 8, 0, true, 1, NOW() - INTERVAL '12 hours');

-- Success message
SELECT 'Question database sample data inserted successfully!' as message;
