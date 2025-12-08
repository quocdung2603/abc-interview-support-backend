-- =============================================
-- QUESTION SERVICE DATABASE - SAMPLE DATA (ORDERED)
-- =============================================

-- Connect to questiondb
\c questiondb;

-- =============================================
-- STEP 1: INSERT FIELDS (5 fields)
-- =============================================
INSERT INTO fields(name, description) VALUES
('Frontend', 'Field focusing on building and designing user interfaces for web applications.'),
('Backend', 'Field dealing with server-side logic, APIs, and database management.'),
('Data Science', 'Field involving data analysis, processing, and modeling.'),
('Machine Learning', 'Field focused on building predictive models and machine learning algorithms.'),
('DevOps', 'Field related to automation, deployment, CI/CD, and infrastructure management.')
ON CONFLICT DO NOTHING;

-- =============================================
-- STEP 2: INSERT TOPICS (16 topics, 3-4 per field)
-- =============================================
INSERT INTO topics(field_id, name, description) VALUES
-- FIELD 1: Frontend (3 topics)
(1, 'ReactJS', 'A popular JavaScript library for building user interfaces.'),
(1, 'TypeScript', 'A typed superset of JavaScript that adds static typing.'),
(1, 'HTML/CSS', 'Fundamental technologies for structuring and styling web interfaces.'),

-- FIELD 2: Backend (3 topics)
(2, 'Node.js', 'A JavaScript runtime environment for backend development.'),
(2, 'Spring Boot', 'A powerful Java framework for building APIs and microservices.'),
(2, 'Express.js', 'A minimal and flexible web framework for Node.js.'),

-- FIELD 3: Data Science (3 topics)
(3, 'Python for Data', 'Using Python and related libraries for data processing and analysis.'),
(3, 'Pandas', 'A powerful library for handling and manipulating tabular data.'),
(3, 'NumPy', 'A library for numerical computing and matrix operations.'),

-- FIELD 4: Machine Learning (4 topics)
(4, 'Supervised Learning', 'Machine learning techniques that learn from labeled data.'),
(4, 'Unsupervised Learning', 'Algorithms for clustering and dimensionality reduction using unlabeled data.'),
(4, 'Neural Networks', 'Artificial neural network models inspired by the human brain.'),
(4, 'Machine Learning', 'General machine learning concepts and algorithms.'),

-- FIELD 5: DevOps (3 topics)
(5, 'Docker', 'Technology for packaging applications into lightweight containers.'),
(5, 'CI/CD', 'Automated processes for building, testing, and deploying applications.'),
(5, 'Kubernetes', 'A platform for orchestrating and managing containerized applications.')
ON CONFLICT DO NOTHING;

-- =============================================
-- STEP 3: INSERT LEVELS (5 levels)
-- =============================================
INSERT INTO levels(name, description) VALUES
('Intern', 'Entry-level position with basic understanding and fundamental concepts.'),
('Fresher', 'Junior-level position with basic understanding and fundamental concepts.'),
('Junior', 'Mid-level position with intermediate knowledge and practical examples.'),
('Mid-level', 'Senior-level position with advanced understanding and best practices.'),
('Senior', 'Expert-level position with architectural patterns and optimization.')
ON CONFLICT DO NOTHING;

-- =============================================
-- STEP 4: INSERT QUESTION TYPES (3 types)
-- =============================================
INSERT INTO question_types(name, description) VALUES
('Select one', 'Single choice question where only one answer is correct.'),
('Select all that apply', 'Multiple choice question where multiple answers can be correct.'),
('Explain in detail', 'Open-ended question requiring detailed explanation.')
ON CONFLICT DO NOTHING;

-- =============================================
-- STEP 5: INSERT QUESTIONS (900 questions)
-- =============================================
-- [Questions data will be inserted here from the original file]

-- =============================================
-- STEP 6: INSERT ANSWERS (2100 answers)
-- =============================================
-- [Answers data will be inserted here from the original file]