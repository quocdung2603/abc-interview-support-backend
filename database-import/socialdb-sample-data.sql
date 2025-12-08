-- =============================================
-- SOCIAL SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to socialdb
\c socialdb;

-- Insert sample posts with field/topic/level references
INSERT INTO posts(user_id, field_id, topic_id, level_id, title, content, post_type, status, lock_time, created_at, updated_at) VALUES
-- Frontend - ReactJS - Intern
(1, 1, 1, 1, 'ReactJS Interview Questions for Interns', 'I''m preparing for my first ReactJS internship interview. What are the most common questions they ask about ReactJS basics? I know about components and JSX, but what else should I focus on?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

-- Frontend - ReactJS - Fresher
(2, 1, 1, 2, 'React Hooks vs Class Components', 'As a fresher React developer, I''m confused about when to use hooks vs class components. Can someone explain the practical differences and when to choose each approach?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),

-- Frontend - ReactJS - Junior
(3, 1, 1, 3, 'State Management in React Applications', 'I''ve been working with React for about a year now. What are your experiences with different state management solutions? Redux, Context API, or Zustand?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),

-- Frontend - TypeScript - Intern
(4, 1, 2, 1, 'TypeScript Basics for Frontend Interns', 'Starting to learn TypeScript for frontend development. What are the most important concepts I should master as an intern? Types, interfaces, generics?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),

-- Frontend - TypeScript - Fresher
(5, 1, 2, 2, 'TypeScript Advanced Types', 'Moving beyond basic types in TypeScript. Union types, intersection types, and conditional types - which ones are most commonly used in real projects?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

-- Frontend - HTML/CSS - Junior
(1, 1, 3, 3, 'CSS Grid vs Flexbox - Real World Usage', 'I''ve been using both CSS Grid and Flexbox in my projects. When do you choose one over the other? Any performance considerations?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),

-- Backend - Node.js - Intern
(2, 2, 4, 1, 'Node.js Event Loop Interview Questions', 'Preparing for Node.js internship interviews. The event loop is always mentioned. Can someone explain it in simple terms and what questions they usually ask?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

-- Backend - Node.js - Fresher
(3, 2, 4, 2, 'Express.js Middleware Patterns', 'As a fresher Node.js developer, I''m learning Express middleware. What are the common patterns and best practices for organizing middleware in large applications?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

-- Backend - Spring Boot - Junior
(4, 2, 5, 3, 'Spring Boot Security Configuration', 'Working on a Spring Boot project with authentication. What are the best practices for configuring Spring Security? JWT vs session-based auth?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Backend - Express.js - Middle
(5, 2, 6, 4, 'Scaling Express.js Applications', 'My Express.js app is getting more traffic. What are the strategies for scaling? Load balancing, caching, database optimization?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Data Science - Python - Intern
(1, 3, 7, 1, 'Python for Data Science - Getting Started', 'New to data science and learning Python. What libraries should I focus on first? NumPy, Pandas, or should I start with basic Python concepts?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),

-- Data Science - Python - Fresher
(2, 3, 7, 2, 'Data Cleaning Techniques in Python', 'Working on my first data science project. What are the essential data cleaning techniques I should know? Handling missing values, outliers, data types?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '10 hours', NOW() - INTERVAL '10 hours'),

-- Data Science - Pandas - Junior
(3, 3, 8, 3, 'Pandas Performance Optimization', 'My Pandas code is running slow on large datasets. What are the best practices for optimizing Pandas operations? Vectorization, chunking, data types?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '8 hours'),

-- Data Science - NumPy - Middle
(4, 3, 9, 4, 'Advanced NumPy Operations', 'Using NumPy for scientific computing. What are some advanced operations that are commonly used in data science? Broadcasting, fancy indexing, memory mapping?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),

-- Machine Learning - Scikit-learn - Intern
(5, 4, 10, 1, 'Scikit-learn Basic Algorithms', 'Learning machine learning with scikit-learn. Which algorithms should I start with? Linear regression, decision trees, or k-means clustering?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),

-- Machine Learning - Scikit-learn - Fresher
(1, 4, 10, 2, 'Model Evaluation Metrics', 'Building my first ML models. Beyond accuracy, what other metrics should I use to evaluate model performance? Precision, recall, F1-score, AUC-ROC?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

-- Machine Learning - TensorFlow - Junior
(2, 4, 11, 3, 'TensorFlow vs PyTorch', 'Considering deep learning frameworks. What are the pros and cons of TensorFlow vs PyTorch? Which one should I learn first?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

-- Machine Learning - Neural Networks - Middle
(3, 4, 12, 4, 'Neural Network Architectures', 'Designing neural networks for different problems. What architectures work best for image classification, NLP, time series? CNN, RNN, Transformer?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),

-- DevOps - Docker - Intern
(4, 5, 13, 1, 'Docker Basics for Beginners', 'Starting with Docker for the first time. What are the essential commands I should learn? build, run, exec, logs, and how do containers differ from images?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),

-- DevOps - Docker - Fresher
(5, 5, 13, 2, 'Docker Compose for Development', 'Setting up development environment with Docker Compose. How do you structure your docker-compose.yml files? Multi-stage builds, volumes, networks?', 'DISCUSSION', 'PUBLISHED', NULL, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes'),

-- DevOps - CI/CD - Junior
(1, 5, 14, 3, 'CI/CD Pipeline Best Practices', 'Implementing CI/CD pipelines. What tools are you using? Jenkins, GitLab CI, GitHub Actions? What are the common pitfalls to avoid?', 'QUESTION', 'DRAFT', NULL, NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '5 minutes'),

-- DevOps - Kubernetes - Middle
(2, 5, 15, 4, 'Kubernetes Production Deployments', 'Managing Kubernetes clusters in production. What are your experiences with scaling, monitoring, and troubleshooting? Helm charts, operators, service meshes?', 'QUESTION', 'DRAFT', NULL, NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '2 minutes'),

-- Senior Level Discussions
(3, 1, 1, 5, 'Senior Frontend Developer Interview Tips', 'Preparing for senior frontend positions. What advanced topics should I focus on? Architecture patterns, performance optimization, team leadership?', 'QUESTION', 'DRAFT', NULL, NOW() - INTERVAL '1 minute', NOW() - INTERVAL '1 minute'),

(4, 2, 5, 5, 'Senior Backend Architect Interview', 'Going for senior backend architect roles. System design, scalability, microservices architecture - what are the key areas to prepare?', 'QUESTION', 'DRAFT', NULL, NOW() - INTERVAL '30 seconds', NOW() - INTERVAL '30 seconds');

-- Insert sample comments for post 1 (ReactJS Intern Interview)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(1, 2, 'For ReactJS interns, focus on component lifecycle, props vs state, and basic hooks like useState and useEffect. JSX syntax is also commonly asked.', 8, NOW() - INTERVAL '9 days'),
(1, 3, 'Don''t forget about virtual DOM and reconciliation concepts. These are fundamental to understanding React''s performance.', 6, NOW() - INTERVAL '9 days'),
(1, 4, 'Practice building simple components and passing data between them. Interviewers love to see practical examples.', 5, NOW() - INTERVAL '8 days');

-- Insert sample comments for post 2 (React Hooks vs Class)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(2, 1, 'Hooks are the future! They make components cleaner and easier to test. Class components are still used in legacy codebases.', 12, NOW() - INTERVAL '8 days'),
(2, 4, 'I prefer hooks for functional programming style. The learning curve is worth it for better code organization.', 9, NOW() - INTERVAL '8 days'),
(2, 5, 'Class components for complex state logic, hooks for simpler components. It depends on the use case.', 7, NOW() - INTERVAL '7 days');

-- Insert sample comments for post 3 (React State Management)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(3, 2, 'Redux Toolkit is my go-to for complex state management. It reduces boilerplate significantly compared to plain Redux.', 15, NOW() - INTERVAL '7 days'),
(3, 5, 'Context API works great for small to medium apps. No need to overcomplicate with Redux unless necessary.', 11, NOW() - INTERVAL '7 days'),
(3, 1, 'Zustand is gaining popularity - it''s simpler than Redux but more powerful than Context API.', 8, NOW() - INTERVAL '6 days');

-- Insert sample comments for post 4 (TypeScript Basics)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(4, 3, 'Start with basic types (string, number, boolean), then move to interfaces and type aliases. Generics come later.', 10, NOW() - INTERVAL '6 days'),
(4, 2, 'Type inference is powerful in TypeScript. You don''t always need to explicitly type everything.', 6, NOW() - INTERVAL '6 days'),
(4, 5, 'Practice with union types and optional properties - these are commonly used in React props.', 9, NOW() - INTERVAL '5 days');

-- Insert sample comments for post 5 (TypeScript Advanced Types)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(5, 1, 'Union types are essential for handling different data shapes. Intersection types for combining interfaces.', 13, NOW() - INTERVAL '5 days'),
(5, 4, 'Conditional types are advanced but powerful. They''re commonly used in utility types like Extract and Exclude.', 8, NOW() - INTERVAL '5 days'),
(5, 3, 'Mapped types are great for transforming existing types. Very useful in real-world TypeScript applications.', 7, NOW() - INTERVAL '4 days');

-- Insert sample comments for post 6 (CSS Grid vs Flexbox)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(6, 2, 'CSS Grid for 2D layouts (rows and columns), Flexbox for 1D layouts. That''s the simple rule I follow.', 18, NOW() - INTERVAL '4 days'),
(6, 5, 'Grid has better performance for complex layouts. Flexbox is simpler for component-level layouts.', 12, NOW() - INTERVAL '4 days'),
(6, 1, 'They work great together! Use Grid at page level, Flexbox inside components.', 15, NOW() - INTERVAL '3 days');

-- Insert sample comments for post 7 (Node.js Event Loop)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(7, 3, 'Think of it as a queue system. Call stack executes synchronously, event loop handles async operations.', 11, NOW() - INTERVAL '3 days'),
(7, 4, 'Common question: "Why is Node.js single-threaded but can handle concurrent requests?"', 9, NOW() - INTERVAL '3 days'),
(7, 2, 'Practice explaining the phases: timers, pending callbacks, idle/prepare, poll, check, close callbacks.', 7, NOW() - INTERVAL '2 days');

-- Insert sample comments for post 8 (Express Middleware)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(8, 5, 'Application-level middleware runs on every request. Router-level for specific routes. Error handling middleware last.', 14, NOW() - INTERVAL '2 days'),
(8, 1, 'Use middleware for authentication, logging, CORS, body parsing. Keep your route handlers clean.', 10, NOW() - INTERVAL '2 days'),
(8, 3, 'Morgan for logging, Helmet for security headers, CORS for cross-origin requests - essential middleware.', 12, NOW() - INTERVAL '1 day');

-- Insert sample comments for post 9 (Spring Boot Security)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(9, 2, 'JWT for stateless authentication, sessions for stateful. JWT scales better in microservices.', 16, NOW() - INTERVAL '1 day'),
(9, 5, 'Spring Security with OAuth2 is powerful. Use @PreAuthorize for method-level security.', 11, NOW() - INTERVAL '1 day'),
(9, 1, 'Never store passwords in plain text! Use BCrypt for hashing. Implement proper password policies.', 13, NOW() - INTERVAL '20 hours');

-- Insert sample comments for post 10 (Scaling Express.js)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(10, 4, 'Start with PM2 for clustering, then move to container orchestration. Redis for caching is essential.', 17, NOW() - INTERVAL '20 hours'),
(10, 3, 'Database connection pooling, query optimization, and CDN for static assets are key scaling factors.', 12, NOW() - INTERVAL '18 hours'),
-- Insert sample comments for post 11 (Python Data Science)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(11, 2, 'Start with NumPy and Pandas basics. Then move to visualization with Matplotlib/Seaborn. Data cleaning is 80% of the work!', 14, NOW() - INTERVAL '11 hours'),
(11, 4, 'Jupyter notebooks are perfect for learning. Practice with real datasets from Kaggle.', 9, NOW() - INTERVAL '11 hours'),
(11, 5, 'Don''t neglect statistics fundamentals. Understanding distributions and hypothesis testing is crucial.', 11, NOW() - INTERVAL '10 hours');

-- Insert sample comments for post 12 (Data Cleaning)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(12, 1, 'Handle missing values: drop, fill with mean/median/mode, or use imputation methods. Context matters!', 16, NOW() - INTERVAL '9 hours'),
(12, 3, 'Outlier detection: IQR method, Z-score, or isolation forests. Always visualize before removing.', 12, NOW() - INTERVAL '9 hours'),
(12, 2, 'Data type conversion is often overlooked. Pandas astype() and to_datetime() are your friends.', 8, NOW() - INTERVAL '8 hours');

-- Insert sample comments for post 13 (Pandas Performance)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(13, 5, 'Use vectorized operations instead of loops. Pandas apply() with lambda can be slow - avoid when possible.', 19, NOW() - INTERVAL '7 hours'),
(13, 1, 'Chunking with read_csv(chunksize=) for large files. Dask for parallel processing on bigger datasets.', 15, NOW() - INTERVAL '7 hours'),
(13, 4, 'Category dtype for string columns, downcast numeric types. Memory usage can drop significantly.', 13, NOW() - INTERVAL '6 hours');

-- Insert sample comments for post 14 (NumPy Advanced)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(14, 2, 'Broadcasting is powerful but can be confusing. Practice with different shape arrays.', 17, NOW() - INTERVAL '5 hours'),
(14, 3, 'Fancy indexing with boolean masks and integer arrays. Much more flexible than basic slicing.', 11, NOW() - INTERVAL '5 hours'),
(14, 5, 'Memory-mapped arrays with np.memmap() for datasets larger than RAM.', 9, NOW() - INTERVAL '4 hours');

-- Insert sample comments for post 15 (Scikit-learn Basics)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(15, 1, 'Start with linear regression and decision trees. They''re intuitive and build understanding.', 20, NOW() - INTERVAL '3 hours'),
(15, 4, 'K-means for unsupervised learning. Elbow method for choosing k. Always scale features first!', 14, NOW() - INTERVAL '3 hours'),
(15, 2, 'Train/test split is crucial. Never evaluate on training data - that''s overfitting waiting to happen.', 16, NOW() - INTERVAL '2 hours');

-- Insert sample comments for post 16 (Model Evaluation)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(16, 3, 'For classification: precision, recall, F1-score. For regression: MAE, MSE, RMSE. Choose based on business impact.', 22, NOW() - INTERVAL '1 hour'),
(16, 5, 'Cross-validation prevents overfitting. K-fold CV gives more reliable estimates than single train/test split.', 18, NOW() - INTERVAL '1 hour'),
(16, 1, 'Confusion matrix is essential. Shows exactly where your model is making mistakes.', 15, NOW() - INTERVAL '45 minutes');

-- Insert sample comments for post 17 (TensorFlow vs PyTorch)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(17, 4, 'TensorFlow has better production deployment tools. PyTorch has cleaner Pythonic API.', 25, NOW() - INTERVAL '45 minutes'),
(17, 2, 'PyTorch for research and experimentation. TensorFlow for production ML engineering.', 19, NOW() - INTERVAL '40 minutes'),
(17, 3, 'Both are excellent. PyTorch has better debugging, TensorFlow has TensorBoard for visualization.', 17, NOW() - INTERVAL '35 minutes');

-- Insert sample comments for post 18 (Neural Network Architectures)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(18, 5, 'CNNs for images, RNNs/LSTMs for sequences, Transformers for everything now!', 28, NOW() - INTERVAL '25 minutes'),
(18, 1, 'Start with feedforward networks, then CNNs. RNNs are trickier with vanishing gradients.', 21, NOW() - INTERVAL '20 minutes'),
(18, 4, 'Attention mechanisms revolutionized NLP. BERT, GPT models are all about attention.', 23, NOW() - INTERVAL '15 minutes');

-- Insert sample comments for post 19 (Docker Basics)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(19, 2, 'Images are blueprints, containers are running instances. docker build creates images, docker run creates containers.', 24, NOW() - INTERVAL '10 minutes'),
(19, 3, 'Dockerfile best practices: use .dockerignore, multi-stage builds, non-root users, minimal base images.', 16, NOW() - INTERVAL '8 minutes'),
(19, 5, 'Volumes for persistent data, bind mounts for development. Never store data in container filesystem.', 12, NOW() - INTERVAL '5 minutes');

-- Insert sample comments for post 20 (Docker Compose)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(20, 1, 'Separate concerns: app service, database service, maybe redis. Use depends_on for startup order.', 18, NOW() - INTERVAL '3 minutes'),
(20, 4, 'Environment variables in .env file, networks for service isolation, volumes for data persistence.', 14, NOW() - INTERVAL '2 minutes'),
(20, 2, 'Docker Compose for development, Kubernetes manifests for production. Different tools for different scales.', 11, NOW() - INTERVAL '1 minute');

-- Insert sample comments for post 21 (CI/CD Pipelines)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(21, 3, 'GitHub Actions is free and integrates perfectly with GitHub. Jenkins is more flexible but requires maintenance.', 20, NOW() - INTERVAL '45 minutes'),
(21, 5, 'GitLab CI/CD is powerful with built-in security scanning. CircleCI has great performance for smaller teams.', 15, NOW() - INTERVAL '40 minutes'),
(21, 1, 'Start simple: lint → test → build → deploy. Add complexity as your team grows.', 17, NOW() - INTERVAL '35 minutes');

-- Insert sample comments for post 22 (Kubernetes Production)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(22, 4, 'Helm charts are essential for managing complex applications. Think of them as package managers for Kubernetes.', 22, NOW() - INTERVAL '25 minutes'),
(22, 2, 'Monitoring with Prometheus + Grafana, logging with ELK stack. Observability is critical in production.', 19, NOW() - INTERVAL '20 minutes'),
(22, 3, 'Service meshes like Istio add traffic management, security, and observability. Great for microservices.', 16, NOW() - INTERVAL '15 minutes');

-- Insert sample comments for post 23 (Senior Frontend Interview)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(23, 5, 'Architecture decisions are key: component design patterns, state management at scale, performance optimization.', 25, NOW() - INTERVAL '1 minute'),
(23, 1, 'Leadership skills matter: mentoring juniors, code reviews, collaborating with designers and backend teams.', 21, NOW() - INTERVAL '45 seconds'),
(23, 4, 'System design questions: how would you build a large-scale React application? Bundle splitting, lazy loading, etc.', 18, NOW() - INTERVAL '30 seconds');

-- Insert sample comments for post 24 (Senior Backend Architect)
INSERT INTO comments(post_id, user_id, content, vote_count, created_at) VALUES
(24, 2, 'Scalability, reliability, and maintainability are the three pillars. Design for failure, monitor everything.', 28, NOW() - INTERVAL '30 seconds'),
(24, 3, 'Microservices vs monolith decisions: organizational structure often dictates the architecture choice.', 23, NOW() - INTERVAL '15 seconds'),
(24, 5, 'API design, database schema evolution, caching strategies, async processing - these are daily concerns.', 20, NOW() - INTERVAL '5 seconds');

-- Insert sample votes for comments
INSERT INTO comment_votes(comment_id, user_id, voted_at) VALUES
-- Votes for post 1-10 comments (keeping existing votes)
(1, 1, NOW() - INTERVAL '9 days'), (1, 3, NOW() - INTERVAL '9 days'), (1, 4, NOW() - INTERVAL '8 days'),
(2, 1, NOW() - INTERVAL '9 days'), (2, 2, NOW() - INTERVAL '9 days'), (2, 4, NOW() - INTERVAL '8 days'),
(3, 1, NOW() - INTERVAL '8 days'), (3, 2, NOW() - INTERVAL '8 days'), (3, 3, NOW() - INTERVAL '8 days'),
(4, 1, NOW() - INTERVAL '8 days'), (4, 3, NOW() - INTERVAL '8 days'), (4, 5, NOW() - INTERVAL '7 days'),
(5, 2, NOW() - INTERVAL '8 days'), (5, 3, NOW() - INTERVAL '8 days'), (5, 5, NOW() - INTERVAL '7 days'),
(6, 1, NOW() - INTERVAL '7 days'), (6, 2, NOW() - INTERVAL '7 days'), (6, 3, NOW() - INTERVAL '7 days'),
(7, 2, NOW() - INTERVAL '7 days'), (7, 4, NOW() - INTERVAL '7 days'), (7, 5, NOW() - INTERVAL '6 days'),
(8, 1, NOW() - INTERVAL '7 days'), (8, 3, NOW() - INTERVAL '7 days'), (8, 4, NOW() - INTERVAL '6 days'),
(9, 1, NOW() - INTERVAL '6 days'), (9, 2, NOW() - INTERVAL '6 days'), (9, 5, NOW() - INTERVAL '6 days'),
(10, 3, NOW() - INTERVAL '6 days'), (10, 5, NOW() - INTERVAL '6 days'), (10, 1, NOW() - INTERVAL '5 days'),
(11, 2, NOW() - INTERVAL '6 days'), (11, 4, NOW() - INTERVAL '6 days'), (11, 5, NOW() - INTERVAL '5 days'),
(12, 1, NOW() - INTERVAL '5 days'), (12, 3, NOW() - INTERVAL '5 days'), (12, 4, NOW() - INTERVAL '5 days'),
(13, 2, NOW() - INTERVAL '5 days'), (13, 3, NOW() - INTERVAL '5 days'), (13, 5, NOW() - INTERVAL '4 days'),
(14, 1, NOW() - INTERVAL '5 days'), (14, 4, NOW() - INTERVAL '5 days'), (14, 2, NOW() - INTERVAL '4 days'),
(15, 1, NOW() - INTERVAL '4 days'), (15, 3, NOW() - INTERVAL '4 days'), (15, 4, NOW() - INTERVAL '4 days'),
(16, 2, NOW() - INTERVAL '4 days'), (16, 5, NOW() - INTERVAL '4 days'), (16, 1, NOW() - INTERVAL '3 days'),
(17, 3, NOW() - INTERVAL '4 days'), (17, 4, NOW() - INTERVAL '4 days'), (17, 2, NOW() - INTERVAL '3 days'),
(18, 1, NOW() - INTERVAL '3 days'), (18, 2, NOW() - INTERVAL '3 days'), (18, 5, NOW() - INTERVAL '2 days'),
(19, 4, NOW() - INTERVAL '3 days'), (19, 2, NOW() - INTERVAL '3 days'), (19, 5, NOW() - INTERVAL '2 days'),
(20, 1, NOW() - INTERVAL '3 days'), (20, 3, NOW() - INTERVAL '3 days'), (20, 4, NOW() - INTERVAL '2 days'),
(21, 1, NOW() - INTERVAL '2 days'), (21, 2, NOW() - INTERVAL '2 days'), (21, 5, NOW() - INTERVAL '2 days'),
(22, 3, NOW() - INTERVAL '2 days'), (22, 4, NOW() - INTERVAL '2 days'), (22, 1, NOW() - INTERVAL '1 day'),
(23, 2, NOW() - INTERVAL '2 days'), (23, 5, NOW() - INTERVAL '2 days'), (23, 3, NOW() - INTERVAL '1 day'),
(24, 1, NOW() - INTERVAL '1 day'), (24, 4, NOW() - INTERVAL '1 day'), (24, 2, NOW() - INTERVAL '1 day'),
(25, 5, NOW() - INTERVAL '1 day'), (25, 3, NOW() - INTERVAL '1 day'), (25, 1, NOW() - INTERVAL '20 hours'),
(26, 4, NOW() - INTERVAL '1 day'), (26, 2, NOW() - INTERVAL '1 day'), (26, 5, NOW() - INTERVAL '20 hours'),
(27, 1, NOW() - INTERVAL '20 hours'), (27, 3, NOW() - INTERVAL '20 hours'), (27, 4, NOW() - INTERVAL '20 hours'),
(28, 2, NOW() - INTERVAL '20 hours'), (28, 5, NOW() - INTERVAL '20 hours'), (28, 3, NOW() - INTERVAL '18 hours'),
(29, 1, NOW() - INTERVAL '20 hours'), (29, 4, NOW() - INTERVAL '20 hours'), (29, 2, NOW() - INTERVAL '18 hours'),
(30, 3, NOW() - INTERVAL '18 hours'), (30, 5, NOW() - INTERVAL '18 hours'), (30, 1, NOW() - INTERVAL '16 hours'),

-- Votes for post 11-24 comments (new posts)
(31, 4, NOW() - INTERVAL '11 hours'), (31, 2, NOW() - INTERVAL '11 hours'), (31, 5, NOW() - INTERVAL '10 hours'),
(32, 1, NOW() - INTERVAL '11 hours'), (32, 3, NOW() - INTERVAL '11 hours'), (32, 4, NOW() - INTERVAL '10 hours'),
(33, 2, NOW() - INTERVAL '10 hours'), (33, 5, NOW() - INTERVAL '10 hours'), (33, 1, NOW() - INTERVAL '9 hours'),
(34, 3, NOW() - INTERVAL '9 hours'), (34, 4, NOW() - INTERVAL '9 hours'), (34, 2, NOW() - INTERVAL '8 hours'),
(35, 1, NOW() - INTERVAL '9 hours'), (35, 5, NOW() - INTERVAL '9 hours'), (35, 3, NOW() - INTERVAL '8 hours'),
(36, 4, NOW() - INTERVAL '8 hours'), (36, 2, NOW() - INTERVAL '8 hours'), (36, 1, NOW() - INTERVAL '7 hours'),
(37, 5, NOW() - INTERVAL '7 hours'), (37, 3, NOW() - INTERVAL '7 hours'), (37, 4, NOW() - INTERVAL '6 hours'),
(38, 1, NOW() - INTERVAL '7 hours'), (38, 2, NOW() - INTERVAL '7 hours'), (38, 5, NOW() - INTERVAL '6 hours'),
(39, 3, NOW() - INTERVAL '6 hours'), (39, 4, NOW() - INTERVAL '6 hours'), (39, 1, NOW() - INTERVAL '5 hours'),
(40, 2, NOW() - INTERVAL '5 hours'), (40, 5, NOW() - INTERVAL '5 hours'), (40, 3, NOW() - INTERVAL '4 hours'),
(41, 1, NOW() - INTERVAL '5 hours'), (41, 4, NOW() - INTERVAL '5 hours'), (41, 2, NOW() - INTERVAL '4 hours'),
(42, 3, NOW() - INTERVAL '4 hours'), (42, 5, NOW() - INTERVAL '4 hours'), (42, 1, NOW() - INTERVAL '3 hours'),
(43, 4, NOW() - INTERVAL '3 hours'), (43, 2, NOW() - INTERVAL '3 hours'), (43, 5, NOW() - INTERVAL '2 hours'),
(44, 1, NOW() - INTERVAL '3 hours'), (44, 3, NOW() - INTERVAL '3 hours'), (44, 4, NOW() - INTERVAL '2 hours'),
(45, 2, NOW() - INTERVAL '2 hours'), (45, 5, NOW() - INTERVAL '2 hours'), (45, 1, NOW() - INTERVAL '1 hour'),
(46, 3, NOW() - INTERVAL '1 hour'), (46, 4, NOW() - INTERVAL '1 hour'), (46, 2, NOW() - INTERVAL '45 minutes'),
(47, 1, NOW() - INTERVAL '1 hour'), (47, 5, NOW() - INTERVAL '1 hour'), (47, 3, NOW() - INTERVAL '45 minutes'),
(48, 4, NOW() - INTERVAL '45 minutes'), (48, 2, NOW() - INTERVAL '45 minutes'), (48, 1, NOW() - INTERVAL '30 minutes'),
(49, 5, NOW() - INTERVAL '45 minutes'), (49, 3, NOW() - INTERVAL '45 minutes'), (49, 4, NOW() - INTERVAL '40 minutes'),
(50, 1, NOW() - INTERVAL '45 minutes'), (50, 2, NOW() - INTERVAL '45 minutes'), (50, 5, NOW() - INTERVAL '40 minutes'),
(51, 3, NOW() - INTERVAL '40 minutes'), (51, 4, NOW() - INTERVAL '40 minutes'), (51, 1, NOW() - INTERVAL '35 minutes'),
(52, 2, NOW() - INTERVAL '25 minutes'), (52, 5, NOW() - INTERVAL '25 minutes'), (52, 3, NOW() - INTERVAL '20 minutes'),
(53, 1, NOW() - INTERVAL '25 minutes'), (53, 4, NOW() - INTERVAL '25 minutes'), (53, 2, NOW() - INTERVAL '20 minutes'),
(54, 5, NOW() - INTERVAL '20 minutes'), (54, 3, NOW() - INTERVAL '20 minutes'), (54, 1, NOW() - INTERVAL '15 minutes'),
(55, 4, NOW() - INTERVAL '10 minutes'), (55, 2, NOW() - INTERVAL '10 minutes'), (55, 5, NOW() - INTERVAL '8 minutes'),
(56, 1, NOW() - INTERVAL '10 minutes'), (56, 3, NOW() - INTERVAL '10 minutes'), (56, 4, NOW() - INTERVAL '8 minutes'),
(57, 2, NOW() - INTERVAL '8 minutes'), (57, 5, NOW() - INTERVAL '8 minutes'), (57, 1, NOW() - INTERVAL '5 minutes'),
(58, 3, NOW() - INTERVAL '3 minutes'), (58, 4, NOW() - INTERVAL '3 minutes'), (58, 2, NOW() - INTERVAL '2 minutes'),
(59, 1, NOW() - INTERVAL '3 minutes'), (59, 5, NOW() - INTERVAL '3 minutes'), (59, 3, NOW() - INTERVAL '2 minutes'),
(60, 4, NOW() - INTERVAL '2 minutes'), (60, 2, NOW() - INTERVAL '2 minutes'), (60, 1, NOW() - INTERVAL '1 minute'),
(61, 5, NOW() - INTERVAL '45 minutes'), (61, 3, NOW() - INTERVAL '45 minutes'), (61, 1, NOW() - INTERVAL '40 minutes'),
(62, 4, NOW() - INTERVAL '45 minutes'), (62, 2, NOW() - INTERVAL '45 minutes'), (62, 5, NOW() - INTERVAL '40 minutes'),
(63, 3, NOW() - INTERVAL '40 minutes'), (63, 1, NOW() - INTERVAL '40 minutes'), (63, 4, NOW() - INTERVAL '35 minutes'),
(64, 2, NOW() - INTERVAL '25 minutes'), (64, 5, NOW() - INTERVAL '25 minutes'), (64, 3, NOW() - INTERVAL '20 minutes'),
(65, 1, NOW() - INTERVAL '25 minutes'), (65, 4, NOW() - INTERVAL '25 minutes'), (65, 2, NOW() - INTERVAL '20 minutes'),
(66, 5, NOW() - INTERVAL '20 minutes'), (66, 3, NOW() - INTERVAL '20 minutes'), (66, 1, NOW() - INTERVAL '15 minutes'),
(67, 4, NOW() - INTERVAL '1 minute'), (67, 2, NOW() - INTERVAL '1 minute'), (67, 5, NOW() - INTERVAL '45 seconds'),
(68, 1, NOW() - INTERVAL '1 minute'), (68, 3, NOW() - INTERVAL '1 minute'), (68, 4, NOW() - INTERVAL '45 seconds'),
(69, 2, NOW() - INTERVAL '45 seconds'), (69, 5, NOW() - INTERVAL '45 seconds'), (69, 1, NOW() - INTERVAL '30 seconds'),
(70, 3, NOW() - INTERVAL '30 seconds'), (70, 4, NOW() - INTERVAL '30 seconds'), (70, 2, NOW() - INTERVAL '15 seconds'),
(71, 1, NOW() - INTERVAL '30 seconds'), (71, 5, NOW() - INTERVAL '30 seconds'), (71, 3, NOW() - INTERVAL '15 seconds'),
(72, 4, NOW() - INTERVAL '15 seconds'), (72, 2, NOW() - INTERVAL '15 seconds'), (72, 1, NOW() - INTERVAL '5 seconds');

-- Success message
SELECT 'Social database sample data inserted successfully!' as message;
SELECT COUNT(*) as total_posts FROM posts;
SELECT COUNT(*) as total_comments FROM comments;
SELECT COUNT(*) as total_votes FROM comment_votes;
