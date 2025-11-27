-- =============================================
-- SOCIAL SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to socialdb
\c socialdb;

-- Insert sample posts
INSERT INTO posts(user_id, title, content, lock_time, created_at, updated_at) VALUES 
(1, 'Welcome to Social Platform!', 'This is the first post on our new social platform. Feel free to comment and share your thoughts!', NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(2, 'Best Practices for Microservices', 'Let''s discuss the best practices for building microservices architecture. What are your experiences?', NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(1, 'Spring Boot Tips and Tricks', 'Share your favorite Spring Boot tips and tricks here. I''ll start: Always use @Transactional for database operations!', NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(3, 'ReactJS vs Angular - Discussion', 'What do you prefer for frontend development? Let''s have a healthy discussion about ReactJS and Angular.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(2, 'Docker Compose Best Practices', 'I''ve been working with Docker Compose for a while. Here are some best practices I''ve learned...', NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(1, 'Database Design Patterns', 'What are your favorite database design patterns? Share your knowledge!', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(3, 'Locked Post Example', 'This post is locked for commenting. You can only view existing comments.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(2, 'API Security Best Practices', 'Security is crucial for APIs. Let''s discuss JWT, OAuth2, and other security measures.', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(1, 'Testing Strategies for Microservices', 'How do you test your microservices? Unit tests, integration tests, or end-to-end tests?', NULL, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
(3, 'Cloud Deployment Options', 'AWS, Azure, or Google Cloud? What''s your preferred cloud platform and why?', NULL, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours');

-- Insert sample comments for post 1
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(1, 2, 'Great initiative! Looking forward to engaging discussions here.', 5, NOW() - INTERVAL '9 days'),
(1, 3, 'This platform looks promising. Can''t wait to see more features!', 3, NOW() - INTERVAL '9 days'),
(1, 4, 'Welcome everyone! Let''s build a great community together.', 7, NOW() - INTERVAL '8 days'),
(1, 5, 'Excited to be part of this community!', 2, NOW() - INTERVAL '8 days');

-- Insert sample comments for post 2
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(2, 1, 'Service discovery is crucial. I recommend using Eureka or Consul.', 8, NOW() - INTERVAL '7 days'),
(2, 3, 'Don''t forget about API Gateway patterns. They''re essential for microservices.', 6, NOW() - INTERVAL '7 days'),
(2, 4, 'Circuit breaker pattern saved my production system multiple times!', 10, NOW() - INTERVAL '6 days'),
(2, 5, 'Database per service is a must. Shared databases create tight coupling.', 4, NOW() - INTERVAL '6 days');

-- Insert sample comments for post 3
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(3, 2, 'Use @Async for background tasks. It improves performance significantly.', 12, NOW() - INTERVAL '5 days'),
(3, 4, 'Spring Boot Actuator is amazing for monitoring and health checks.', 9, NOW() - INTERVAL '5 days'),
(3, 5, 'Don''t forget to configure proper logging with Logback or Log4j2.', 7, NOW() - INTERVAL '4 days');

-- Insert sample comments for post 4 (locked post)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(4, 2, 'I prefer ReactJS for its simplicity and large ecosystem.', 15, NOW() - INTERVAL '5 days'),
(4, 5, 'Angular is better for large enterprise applications with TypeScript.', 8, NOW() - INTERVAL '5 days'),
(4, 1, 'Both are great! It depends on your project requirements.', 20, NOW() - INTERVAL '4 days'),
(4, 3, 'ReactJS has better performance with virtual DOM.', 12, NOW() - INTERVAL '4 days');

-- Insert sample comments for post 5
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(5, 1, 'Always use named volumes for persistent data!', 6, NOW() - INTERVAL '3 days'),
(5, 3, 'Health checks are essential for production deployments.', 5, NOW() - INTERVAL '3 days'),
(5, 4, 'Use .env files for environment-specific configurations.', 8, NOW() - INTERVAL '2 days');

-- Insert sample comments for post 6
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(6, 2, 'Normalization is key for data integrity.', 4, NOW() - INTERVAL '2 days'),
(6, 5, 'Sometimes denormalization is necessary for performance.', 6, NOW() - INTERVAL '2 days'),
(6, 3, 'Use indexes wisely. They can make or break performance.', 9, NOW() - INTERVAL '1 day');

-- Insert sample comments for post 7 (locked post)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(7, 1, 'This is a comment on a locked post.', 3, NOW() - INTERVAL '2 days'),
(7, 2, 'No more comments can be added after lock time.', 5, NOW() - INTERVAL '2 days');

-- Insert sample comments for post 8
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(8, 3, 'JWT tokens should have short expiration times.', 11, NOW() - INTERVAL '20 hours'),
(8, 4, 'Always validate tokens on the server side!', 8, NOW() - INTERVAL '18 hours'),
(8, 5, 'Use HTTPS for all API communications.', 7, NOW() - INTERVAL '16 hours');

-- Insert sample comments for post 9
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(9, 2, 'I use a combination of all three. Each has its place.', 5, NOW() - INTERVAL '10 hours'),
(9, 4, 'Integration tests are most valuable for microservices.', 6, NOW() - INTERVAL '8 hours');

-- Insert sample comments for post 10
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES 
(10, 1, 'AWS has the most mature ecosystem.', 4, NOW() - INTERVAL '5 hours'),
(10, 2, 'Azure integrates well with Microsoft technologies.', 3, NOW() - INTERVAL '4 hours'),
(10, 5, 'Google Cloud has excellent Kubernetes support.', 6, NOW() - INTERVAL '3 hours');

-- Insert sample votes
INSERT INTO comment_votes(comment_id, user_id, voted_at) VALUES 
-- Votes for post 1 comments
(1, 1, NOW() - INTERVAL '9 days'),
(1, 3, NOW() - INTERVAL '9 days'),
(1, 4, NOW() - INTERVAL '8 days'),
(1, 5, NOW() - INTERVAL '8 days'),
(1, 6, NOW() - INTERVAL '8 days'),
(2, 1, NOW() - INTERVAL '9 days'),
(2, 2, NOW() - INTERVAL '9 days'),
(2, 4, NOW() - INTERVAL '8 days'),
(3, 1, NOW() - INTERVAL '8 days'),
(3, 2, NOW() - INTERVAL '8 days'),
(3, 3, NOW() - INTERVAL '8 days'),
(3, 5, NOW() - INTERVAL '7 days'),
(3, 6, NOW() - INTERVAL '7 days'),
(3, 7, NOW() - INTERVAL '7 days'),
(3, 8, NOW() - INTERVAL '7 days'),
(4, 1, NOW() - INTERVAL '8 days'),
(4, 3, NOW() - INTERVAL '8 days'),

-- Votes for post 2 comments
(5, 2, NOW() - INTERVAL '7 days'),
(5, 3, NOW() - INTERVAL '7 days'),
(5, 4, NOW() - INTERVAL '6 days'),
(5, 5, NOW() - INTERVAL '6 days'),
(5, 6, NOW() - INTERVAL '6 days'),
(5, 7, NOW() - INTERVAL '6 days'),
(5, 8, NOW() - INTERVAL '6 days'),
(5, 9, NOW() - INTERVAL '6 days'),
(6, 1, NOW() - INTERVAL '7 days'),
(6, 2, NOW() - INTERVAL '7 days'),
(6, 4, NOW() - INTERVAL '6 days'),
(6, 5, NOW() - INTERVAL '6 days'),
(6, 6, NOW() - INTERVAL '6 days'),
(6, 7, NOW() - INTERVAL '6 days'),
(7, 1, NOW() - INTERVAL '6 days'),
(7, 2, NOW() - INTERVAL '6 days'),
(7, 3, NOW() - INTERVAL '5 days'),
(7, 4, NOW() - INTERVAL '5 days'),
(7, 5, NOW() - INTERVAL '5 days'),
(7, 6, NOW() - INTERVAL '5 days'),
(7, 7, NOW() - INTERVAL '5 days'),
(7, 8, NOW() - INTERVAL '5 days'),
(7, 9, NOW() - INTERVAL '5 days'),
(7, 10, NOW() - INTERVAL '5 days'),
(8, 1, NOW() - INTERVAL '6 days'),
(8, 2, NOW() - INTERVAL '6 days'),
(8, 3, NOW() - INTERVAL '5 days'),
(8, 4, NOW() - INTERVAL '5 days');

-- Success message
SELECT 'Social database sample data inserted successfully!' as message;
SELECT COUNT(*) as total_posts FROM posts;
SELECT COUNT(*) as total_comments FROM comments;
SELECT COUNT(*) as total_votes FROM comment_votes;
