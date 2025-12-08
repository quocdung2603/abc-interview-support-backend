-- =============================================
-- NEWS SERVICE DATABASE - SAMPLE DATA
-- =============================================

-- Connect to newsdb
\c newsdb;

-- Insert sample news articles
INSERT INTO news(user_id, title, content, field_id, exam_id, news_type, status, created_at, published_at, expired_at, approved_by, useful_vote, interest_vote, company_name, location, salary, experience, position, working_hours, deadline, application_method) VALUES
(1, 'React Server Components Overview', 'An introduction to React Server Components and how they improve performance.', 1, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '10 days', NULL, NULL, 1, 12, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'Tailwind CSS 4.0 Preview', 'Tailwind CSS 4.0 brings new utilities and performance improvements.', 1, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '9 days', NULL, NULL, 1, 14, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'Next.js Routing Enhancements', 'Deep dive into the new routing features introduced in Next.js.', 1, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '8 days', NULL, NULL, 1, 10, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'Frontend Performance Checklist 2025', 'A modern checklist for optimizing frontend applications.', 1, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '7 days', NULL, NULL, 1, 18, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'TypeScript 6.0 Released', 'The latest TypeScript update introduces advanced type inference.', 1, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '6 days', NULL, NULL, 1, 20, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

(2, 'Node.js 24 Performance Boost', 'Node.js 24 introduces major speed improvements.', 2, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '10 days', NULL, NULL, 1, 16, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'Spring Security Best Practices', 'Enhance backend security with updated Spring Security techniques.', 2, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '9 days', NULL, NULL, 1, 11, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'Express.js Middleware Patterns', 'Understanding reusable middleware patterns in Express.js.', 2, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '8 days', NULL, NULL, 1, 9, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'Microservices Authentication Models', 'A guide to authentication and authorization in microservices.', 2, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '7 days', NULL, NULL, 1, 21, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'API Rate Limiting Strategies', 'Techniques for preventing abuse and ensuring API stability.', 2, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '6 days', NULL, NULL, 1, 13, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

(1, 'Data Cleaning Techniques', 'Popular data cleaning strategies for real-world datasets.', 3, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '10 days', NULL, NULL, 1, 17, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'Exploratory Data Analysis Guide', 'How to perform EDA efficiently using Python.', 3, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '9 days', NULL, NULL, 1, 14, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'Pandas Advanced Tricks', 'Lesser-known but powerful Pandas operations.', 3, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '8 days', NULL, NULL, 1, 19, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'NumPy Optimization Tips', 'Speed up your NumPy computations using vectorization.', 3, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '7 days', NULL, NULL, 1, 12, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'Data Visualization Best Practices', 'Best visualization techniques for different datasets.', 3, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '6 days', NULL, NULL, 1, 22, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

(2, 'Gradient Descent Variants Explained', 'Overview of popular GD optimization algorithms.', 4, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '10 days', NULL, NULL, 1, 20, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'Decision Trees vs Random Forests', 'Comparison of two fundamental ML algorithms.', 4, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '9 days', NULL, NULL, 1, 17, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'Neural Network Initialization Methods', 'How initialization affects training performance.', 4, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '8 days', NULL, NULL, 1, 11, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'ML Model Evaluation Metrics', 'Understanding accuracy, precision, recall, and F1-score.', 4, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '7 days', NULL, NULL, 1, 24, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'Ethical AI Considerations', 'Important ethical issues when deploying ML models.', 4, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '6 days', NULL, NULL, 1, 15, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),

(1, 'Kubernetes Autoscaling Guide', 'How Kubernetes handles horizontal and vertical autoscaling.', 5, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '10 days', NULL, NULL, 1, 18, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'Docker Image Optimization Tips', 'Ways to reduce image size and improve CI performance.', 5, NULL, 'NEWS', 'PENDING', NOW() - INTERVAL '9 days', NULL, NULL, 1, 11, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'CI/CD Pipeline Templates', 'Reusable pipeline templates for fast deployment.', 5, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '8 days', NULL, NULL, 1, 16, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'Monitoring with Prometheus', 'How to monitor applications effectively using Prometheus.', 5, NULL, 'NEWS', 'PUBLISHED', NOW() - INTERVAL '7 days', NULL, NULL, 1, 14, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'DevOps Security Essentials', 'Best practices to secure CI/CD pipelines and cloud environments.', 5, NULL, 'NEWS', 'APPROVED', NOW() - INTERVAL '6 days', NULL, NULL, 1, 21, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);



-- Insert sample recruitment posts
INSERT INTO news(user_id, title, content, field_id, exam_id, news_type, status, created_at, published_at, expired_at, approved_by, useful_vote, interest_vote, company_name, location, salary, experience, position, working_hours, deadline, application_method) VALUES
-- FIELD 1: FRONTEND
(2, 'Frontend Developer - ABC Solutions', 'Seeking a Frontend Developer skilled in ReactJS, TypeScript, and UI development.', 1, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() + INTERVAL '25 days', 1, 10, 22, 'ABC Solutions', 'Ho Chi Minh City', '1200-1800 USD', '1-3 years', 'Frontend Developer', '9AM-6PM', '2024-12-28', 'Send CV to hr@abcsolutions.com'),
(2, 'ReactJS Engineer - WebPlus', 'Hiring ReactJS Engineer to build responsive web applications using modern frontend stacks.', 1, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() + INTERVAL '22 days', 1, 15, 30, 'WebPlus', 'Hanoi', '1500-2200 USD', '2-4 years', 'ReactJS Engineer', 'Flexible', '2024-12-25', 'Apply at: webplus.vn/careers'),
(2, 'UI Engineer - CreativeSoft', 'We need a UI Engineer with strong skills in HTML/CSS, responsive design, and component libraries.', 1, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', 1, 18, 28, 'CreativeSoft', 'Da Nang', '1300-1900 USD', '1-3 years', 'UI Engineer', '8AM-5PM', '2024-12-20', 'Email: jobs@creativesoft.com'),
(2, 'Senior Frontend Developer - NextTech', 'Looking for a Senior Frontend Developer experienced in performance optimization and React architecture.', 1, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() + INTERVAL '18 days', 1, 22, 35, 'NextTech', 'Ho Chi Minh City', '2000-3000 USD', '3-5 years', 'Senior Frontend Developer', '9AM-6PM', '2024-12-18', 'Apply via LinkedIn'),
(2, 'Frontend Web Engineer - CodeWave', 'Join our frontend team to build modern web interfaces using ReactJS and TailwindCSS.', 1, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', NOW() + INTERVAL '15 days', 1, 12, 20, 'CodeWave', 'Hanoi', '1400-2000 USD', '2-3 years', 'Frontend Web Engineer', '9AM-6PM', '2024-12-15', 'Apply at: codewave.com/careers'),

-- FIELD 2: BACKEND
(2, 'Backend Developer - TechHub', 'Hiring Backend Developer with experience in Node.js and Express.js for scalable services.', 2, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() + INTERVAL '25 days', 1, 11, 24, 'TechHub', 'Ho Chi Minh City', '1500-2300 USD', '2-4 years', 'Backend Developer', '8AM-5PM', '2024-12-28', 'Send CV to hr@techhub.vn'),
(2, 'Java Backend Engineer - FinCorp', 'We need Java Backend Engineers skilled in Spring Boot for financial platforms.', 2, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() + INTERVAL '22 days', 1, 20, 32, 'FinCorp', 'Hanoi', '1800-2600 USD', '2-4 years', 'Java Backend Engineer', 'Flexible', '2024-12-25', 'Apply at: fincorp.com/jobs'),
(2, 'API Developer - SoftSphere', 'Looking for API Developer with strong knowledge of REST and backend architecture.', 2, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', 1, 14, 21, 'SoftSphere', 'Da Nang', '1400-2000 USD', '1-3 years', 'API Developer', '9AM-6PM', '2024-12-20', 'Email: careers@softsphere.com'),
(2, 'Backend Engineer - DataPay', 'Seeking Backend Engineers for building secure payment services using Node.js.', 2, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() + INTERVAL '17 days', 1, 18, 29, 'DataPay', 'Ho Chi Minh City', '1700-2400 USD', '2-3 years', 'Backend Engineer', '9AM-6PM', '2024-12-17', 'LinkedIn: DataPay'),
(2, 'Senior Backend Developer - CoreLogic', 'Recruiting Senior Backend Developer with microservices architecture expertise.', 2, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', NOW() + INTERVAL '15 days', 1, 25, 40, 'CoreLogic', 'Hanoi', '2200-3200 USD', '4-6 years', 'Senior Backend Developer', '8AM-5PM', '2024-12-15', 'Apply at: corelogic.vn'),

-- FIELD 3: DATA SCIENCE
(2, 'Data Analyst - DataVision', 'Hiring Data Analyst with strong proficiency in Python and SQL.', 3, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() + INTERVAL '25 days', 1, 8, 18, 'DataVision', 'Ho Chi Minh City', '1200-1800 USD', '1-3 years', 'Data Analyst', '8AM-5PM', '2024-12-28', 'Send CV to hr@datavision.com'),
(2, 'Junior Data Scientist - InsightLab', 'Looking for Data Scientist to work on statistical models and ML pipelines.', 3, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() + INTERVAL '22 days', 1, 16, 25, 'InsightLab', 'Hanoi', '1500-2200 USD', '1-2 years', 'Junior Data Scientist', 'Flexible', '2024-12-25', 'Apply via website'),
(2, 'ML Engineer - DeepThink', 'Join our ML team to build predictive models and automation systems using Python.', 3, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', 1, 20, 33, 'DeepThink', 'Da Nang', '1800-2600 USD', '2-3 years', 'ML Engineer', '9AM-6PM', '2024-12-20', 'Email: careers@deepthink.ai'),
(2, 'Data Engineer - StreamFlow', 'Seeking Data Engineer with experience in ETL pipelines and cloud data storage.', 3, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() + INTERVAL '18 days', 1, 14, 27, 'StreamFlow', 'Ho Chi Minh City', '2000-2800 USD', '3-4 years', 'Data Engineer', '9AM-6PM', '2024-12-18', 'LinkedIn: StreamFlow'),
(2, 'Senior Data Scientist - Quantify', 'Looking for Senior Data Scientist with deep ML and statistical modeling experience.', 3, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', NOW() + INTERVAL '15 days', 1, 22, 35, 'Quantify', 'Hanoi', '2800-3800 USD', '4-6 years', 'Senior Data Scientist', '9AM-6PM', '2024-12-15', 'Apply at: quantify.ai/careers'),

-- FIELD 4: MACHINE LEARNING
(2, 'ML Research Engineer - AI Labs', 'Hiring ML Engineer to research supervised and unsupervised learning methods.', 4, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() + INTERVAL '25 days', 1, 12, 21, 'AI Labs', 'Ho Chi Minh City', '2000-3000 USD', '2-4 years', 'ML Research Engineer', 'Flexible', '2024-12-28', 'Send CV to hr@ailabs.com'),
(2, 'Deep Learning Engineer - VisionTech', 'Looking for DL Engineer specializing in neural networks and computer vision.', 4, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() + INTERVAL '22 days', 1, 18, 29, 'VisionTech', 'Hanoi', '2200-3200 USD', '3-5 years', 'Deep Learning Engineer', '9AM-6PM', '2024-12-25', 'Apply at: visiontech.ai'),
(2, 'AI Engineer - SmartAI', 'Join our AI team to implement ML models for automation systems.', 4, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', 1, 15, 26, 'SmartAI', 'Da Nang', '1800-2600 USD', '2-4 years', 'AI Engineer', '9AM-6PM', '2024-12-20', 'Email: jobs@smartai.com'),
(2, 'ML Operations Engineer - MLWorks', 'We need MLOps Engineer to manage ML pipelines and deployment in production.', 4, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() + INTERVAL '18 days', 1, 20, 32, 'MLWorks', 'Ho Chi Minh City', '2100-2900 USD', '3-5 years', 'MLOps Engineer', '8AM-5PM', '2024-12-18', 'Apply via LinkedIn'),
(2, 'Research Scientist - NeuralCore', 'Hiring Research Scientist experienced in neural architectures and model optimization.', 4, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', NOW() + INTERVAL '15 days', 1, 25, 40, 'NeuralCore', 'Hanoi', '3000-4000 USD', '5+ years', 'Research Scientist', 'Flexible', '2024-12-15', 'Email: careers@neuralcore.ai'),

-- FIELD 5: DEVOPS
(2, 'DevOps Engineer - CloudMatrix', 'Seeking DevOps Engineer with strong Docker and Kubernetes experience.', 5, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() + INTERVAL '25 days', 1, 14, 27, 'CloudMatrix', 'Ho Chi Minh City', '2000-2800 USD', '2-4 years', 'DevOps Engineer', '9AM-6PM', '2024-12-28', 'Send CV to hr@cloudmatrix.com'),
(2, 'Cloud Engineer - SkyNet', 'Hiring Cloud Engineer to manage infrastructure on AWS and automate deployments.', 5, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', NOW() + INTERVAL '22 days', 1, 18, 30, 'SkyNet', 'Hanoi', '2200-3200 USD', '3-5 years', 'Cloud Engineer', 'Flexible', '2024-12-25', 'Apply on website'),
(2, 'SRE Engineer - OpsForce', 'Looking for SRE Engineer with monitoring and CI/CD pipeline management experience.', 5, NULL, 'RECRUITMENT', 'PENDING', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', 1, 12, 22, 'OpsForce', 'Da Nang', '1800-2600 USD', '2-4 years', 'SRE Engineer', '9AM-6PM', '2024-12-20', 'Email: career@opsforce.dev'),
(2, 'Platform Engineer - InfraTech', 'Hiring Platform Engineer to maintain scalable cloud-native systems.', 5, NULL, 'RECRUITMENT', 'APPROVED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NOW() + INTERVAL '18 days', 1, 20, 35, 'InfraTech', 'Ho Chi Minh City', '2300-3300 USD', '3-5 years', 'Platform Engineer', '9AM-6PM', '2024-12-18', 'Apply via LinkedIn'),
(2, 'DevOps Specialist - CloudOne', 'We need DevOps Specialist experienced in Kubernetes and CI/CD to support production systems.', 5, NULL, 'RECRUITMENT', 'PUBLISHED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '12 hours', NOW() + INTERVAL '15 days', 1, 22, 38, 'CloudOne', 'Hanoi', '2400-3500 USD', '4-6 years', 'DevOps Specialist', 'Flexible', '2024-12-15', 'Apply at: cloudone.io/careers');



-- Success message
SELECT 'News database sample data inserted successfully!' as message;