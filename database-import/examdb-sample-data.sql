-- =============================================
-- EXAM SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to examdb
\c examdb;

-- Insert sample exams with updated schema (numeric IDs)
INSERT INTO exams(user_id, exam_type, title, position, field_id, topic_ids, level_id, question_type_ids, question_count, duration, start_time, end_time, status, language, created_at, created_by) VALUES 
(1, 'VIRTUAL', 'ReactJS Developer Assessment', 'Frontend Developer', 1, '[1,2,3]', 3, '[1,2]', 20, 60, NOW() + INTERVAL '7 days', NOW() + INTERVAL '7 days 1 hour', 'PUBLISHED', 'en', NOW() - INTERVAL '5 days', 1),
(1, 'VIRTUAL', 'Java Spring Boot Interview', 'Backend Developer', 1, '[4,7]', 4, '[1,2,6]', 25, 90, NOW() + INTERVAL '10 days', NOW() + INTERVAL '10 days 1.5 hours', 'PUBLISHED', 'en', NOW() - INTERVAL '4 days', 1),
(2, 'VIRTUAL', 'Full Stack Developer Test', 'Full Stack Developer', 1, '[1,2,4,5]', 4, '[1,2,6]', 30, 120, NOW() + INTERVAL '14 days', NOW() + INTERVAL '14 days 2 hours', 'PUBLISHED', 'en', NOW() - INTERVAL '3 days', 2),
(1, 'RECRUITER', 'Soft Skills Assessment', 'Any Position', 2, '[]', 2, '[2]', 10, 30, NOW() + INTERVAL '5 days', NOW() + INTERVAL '5 days 30 minutes', 'DRAFT', 'en', NOW() - INTERVAL '2 days', 1),
(2, 'VIRTUAL', 'DevOps Engineer Interview', 'DevOps Engineer', 4, '[16,17,18,19]', 4, '[1,2,6]', 20, 75, NOW() + INTERVAL '12 days', NOW() + INTERVAL '12 days 1.25 hours', 'PUBLISHED', 'en', NOW() - INTERVAL '1 day', 2),
(1, 'VIRTUAL', 'Data Science Assessment', 'Data Scientist', 5, '[20,21,22]', 5, '[1,2,7]', 25, 90, NOW() + INTERVAL '8 days', NOW() + INTERVAL '8 days 1.5 hours', 'PUBLISHED', 'en', NOW() - INTERVAL '6 hours', 1);

-- Insert sample exam questions
INSERT INTO exam_questions(exam_id, question_id, order_number) VALUES 
(1, 1, 1),  -- ReactJS exam - ReactJS features question
(1, 2, 2),  -- ReactJS exam - Virtual DOM question
(1, 3, 3),  -- ReactJS exam - VueJS vs ReactJS question
(2, 4, 1),  -- Java Spring Boot exam - Spring Boot auto-configuration question
(2, 5, 2),  -- Java Spring Boot exam - System design question
(3, 1, 1),  -- Full Stack exam - ReactJS question
(3, 4, 2),  -- Full Stack exam - Spring Boot question
(3, 5, 3),  -- Full Stack exam - System design question
(5, 8, 1),  -- DevOps exam - Docker question
(6, 9, 1);  -- Data Science exam - Machine learning question

-- Insert sample results
INSERT INTO results(exam_id, user_id, score, pass_status, feedback, completed_at) VALUES 
(1, 3, 85.5, true, 'Good understanding of ReactJS concepts. Strong performance on component lifecycle and state management.', NOW() - INTERVAL '2 days'),
(1, 4, 72.0, true, 'Solid grasp of ReactJS basics. Could improve on advanced concepts like hooks and performance optimization.', NOW() - INTERVAL '1 day'),
(1, 5, 45.0, false, 'Needs more study on ReactJS fundamentals. Focus on component architecture and JSX syntax.', NOW() - INTERVAL '12 hours'),
(2, 3, 92.0, true, 'Excellent knowledge of Spring Boot. Demonstrates strong understanding of auto-configuration and dependency injection.', NOW() - INTERVAL '1 day'),
(2, 6, 78.5, true, 'Good Spring Boot knowledge. Shows understanding of core concepts but could improve on advanced features.', NOW() - INTERVAL '6 hours'),
(3, 3, 88.0, true, 'Strong full-stack capabilities. Good understanding of both frontend and backend technologies.', NOW() - INTERVAL '3 days'),
(5, 6, 95.0, true, 'Outstanding DevOps knowledge. Excellent understanding of containerization and orchestration.', NOW() - INTERVAL '2 days'),
(6, 3, 67.5, true, 'Good understanding of data science concepts. Could improve on practical implementation details.', NOW() - INTERVAL '1 day');

-- Insert sample user answers
INSERT INTO user_answers(exam_id, question_id, user_id, answer_content, is_correct, similarity_score, created_at) VALUES 
(1, 1, 3, 'ReactJS is a JavaScript library for building user interfaces with features like Virtual DOM, components, and JSX.', true, 0.95, NOW() - INTERVAL '2 days'),
(1, 2, 3, 'Virtual DOM is a JavaScript representation of the real DOM that React uses to optimize updates and improve performance.', true, 0.92, NOW() - INTERVAL '2 days'),
(1, 3, 3, 'VueJS uses templates and two-way data binding, while React uses JSX and one-way data binding.', true, 0.88, NOW() - INTERVAL '2 days'),
(1, 1, 4, 'ReactJS is a frontend library for creating interactive UIs with components and state management.', true, 0.85, NOW() - INTERVAL '1 day'),
(1, 2, 4, 'Virtual DOM helps React update the browser DOM efficiently by comparing virtual and real DOM.', true, 0.78, NOW() - INTERVAL '1 day'),
(1, 3, 4, 'VueJS is easier to learn than React and has two-way data binding.', true, 0.82, NOW() - INTERVAL '1 day'),
(1, 1, 5, 'React is a JavaScript library for web development.', false, 0.45, NOW() - INTERVAL '12 hours'),
(1, 2, 5, 'Virtual DOM is used for styling in React.', false, 0.30, NOW() - INTERVAL '12 hours'),
(2, 4, 3, 'Spring Boot auto-configuration automatically sets up beans based on classpath dependencies and configuration properties.', true, 0.94, NOW() - INTERVAL '1 day'),
(2, 5, 3, 'A scalable e-commerce system needs load balancers, microservices, database sharding, caching, and monitoring.', true, 0.91, NOW() - INTERVAL '1 day');

-- Insert sample exam registrations
INSERT INTO exam_registrations(exam_id, user_id, registration_status, registered_at) VALUES 
(1, 3, 'REGISTERED', NOW() - INTERVAL '3 days'),
(1, 4, 'REGISTERED', NOW() - INTERVAL '2 days'),
(1, 5, 'REGISTERED', NOW() - INTERVAL '1 day'),
(2, 3, 'REGISTERED', NOW() - INTERVAL '2 days'),
(2, 6, 'REGISTERED', NOW() - INTERVAL '1 day'),
(3, 3, 'REGISTERED', NOW() - INTERVAL '4 days'),
(5, 6, 'REGISTERED', NOW() - INTERVAL '3 days'),
(6, 3, 'REGISTERED', NOW() - INTERVAL '2 days'),
(1, 6, 'CANCELLED', NOW() - INTERVAL '2 days'),  -- Cancelled registration
(2, 4, 'REGISTERED', NOW() - INTERVAL '1 day');

-- Success message
SELECT 'Exam database sample data inserted successfully!' as message;
