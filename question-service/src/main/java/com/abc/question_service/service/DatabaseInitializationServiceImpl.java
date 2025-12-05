package com.abc.question_service.service;

import com.abc.question_service.dto.InitializationResult;
import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.repository.AnswerRepository;
import com.abc.question_service.repository.FieldRepository;
import com.abc.question_service.repository.LevelRepository;
import com.abc.question_service.repository.QuestionRepository;
import com.abc.question_service.repository.QuestionTypeRepository;
import com.abc.question_service.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DatabaseInitializationService.
 * Handles database reset and reference data initialization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializationServiceImpl implements DatabaseInitializationService {
    
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final FieldRepository fieldRepository;
    private final TopicRepository topicRepository;
    private final LevelRepository levelRepository;
    private final QuestionTypeRepository questionTypeRepository;
    
    @Override
    @Transactional
    public InitializationResult resetDatabase() {
        log.info("Starting database reset...");
        
        InitializationResult result = InitializationResult.builder()
                .success(true)
                .errors(new ArrayList<>())
                .build();
        
        try {
            // Count before deletion
            long questionCount = questionRepository.count();
            long answerCount = answerRepository.count();
            
            log.info("Deleting {} questions and {} answers", questionCount, answerCount);
            
            // Delete all questions (answers will be cascade deleted)
            questionRepository.deleteAll();
            
            result.setQuestionsDeleted((int) questionCount);
            result.setAnswersDeleted((int) answerCount);
            result.setMessage("Database reset completed successfully");
            
            log.info("Database reset completed: {} questions and {} answers deleted", 
                    questionCount, answerCount);
            
        } catch (Exception e) {
            log.error("Error during database reset", e);
            result.setSuccess(false);
            result.getErrors().add("Failed to reset database: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public InitializationResult initializeReferenceData() {
        log.info("Starting reference data initialization...");
        
        InitializationResult result = InitializationResult.builder()
                .success(true)
                .errors(new ArrayList<>())
                .fieldsCreated(0)
                .topicsCreated(0)
                .levelsCreated(0)
                .questionTypesCreated(0)
                .build();
        
        try {
            // Initialize fields
            int fieldsCreated = initializeFields();
            result.setFieldsCreated(fieldsCreated);
            log.info("Created {} fields", fieldsCreated);
            
            // Initialize topics (depends on fields)
            int topicsCreated = initializeTopics();
            result.setTopicsCreated(topicsCreated);
            log.info("Created {} topics", topicsCreated);
            
            // Initialize levels
            int levelsCreated = initializeLevels();
            result.setLevelsCreated(levelsCreated);
            log.info("Created {} levels", levelsCreated);
            
            // Initialize question types
            int questionTypesCreated = initializeQuestionTypes();
            result.setQuestionTypesCreated(questionTypesCreated);
            log.info("Created {} question types", questionTypesCreated);
            
            // Verify reference data
            if (!verifyReferenceData()) {
                result.setSuccess(false);
                result.getErrors().add("Reference data verification failed");
            } else {
                result.setMessage("Reference data initialized successfully");
            }
            
            log.info("Reference data initialization completed");
            
        } catch (Exception e) {
            log.error("Error during reference data initialization", e);
            result.setSuccess(false);
            result.getErrors().add("Failed to initialize reference data: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean verifyReferenceData() {
        log.info("Verifying reference data...");
        
        try {
            // Check fields
            long fieldCount = fieldRepository.count();
            if (fieldCount < 10) {
                log.warn("Expected at least 10 fields, found {}", fieldCount);
                return false;
            }
            
            // Check topics
            long topicCount = topicRepository.count();
            if (topicCount < 50) {
                log.warn("Expected at least 50 topics, found {}", topicCount);
                return false;
            }
            
            // Check levels
            long levelCount = levelRepository.count();
            if (levelCount != 8) {
                log.warn("Expected exactly 8 levels, found {}", levelCount);
                return false;
            }
            
            // Check question types
            long questionTypeCount = questionTypeRepository.count();
            if (questionTypeCount != 3) {
                log.warn("Expected exactly 3 question types, found {}", questionTypeCount);
                return false;
            }
            
            // Verify each field has at least 5 topics
            List<Field> fields = fieldRepository.findAll();
            for (Field field : fields) {
                long topicsForField = topicRepository.countByFieldId(field.getId());
                if (topicsForField < 5) {
                    log.warn("Field '{}' has only {} topics, expected at least 5", 
                            field.getName(), topicsForField);
                    return false;
                }
            }
            
            log.info("Reference data verification passed");
            return true;
            
        } catch (Exception e) {
            log.error("Error during reference data verification", e);
            return false;
        }
    }
    
    /**
     * Initialize 10 fields with IT domain names.
     */
    private int initializeFields() {
        int created = 0;
        
        String[][] fieldsData = {
            {"Frontend Development", "Client-side web technologies and frameworks"},
            {"Backend Development", "Server-side development and APIs"},
            {"Database", "Database design, management, and optimization"},
            {"DevOps", "Development operations, CI/CD, and deployment"},
            {"Mobile Development", "Mobile application development for iOS and Android"},
            {"Data Science", "Data analysis, machine learning, and AI"},
            {"Cybersecurity", "Information security and protection"},
            {"Cloud Computing", "Cloud platforms and services"},
            {"Software Testing", "Quality assurance and testing methodologies"},
            {"System Design", "Architecture and system design patterns"}
        };
        
        for (String[] fieldData : fieldsData) {
            String name = fieldData[0];
            String description = fieldData[1];
            
            if (!fieldRepository.existsByName(name)) {
                Field field = new Field();
                field.setName(name);
                field.setDescription(description);
                fieldRepository.save(field);
                created++;
                log.debug("Created field: {}", name);
            }
        }
        
        return created;
    }
    
    /**
     * Initialize 50+ topics (5+ per field).
     */
    private int initializeTopics() {
        int created = 0;
        
        // Get all fields
        List<Field> fields = fieldRepository.findAll();
        
        // Topics for each field
        String[][][] topicsData = {
            // Frontend Development
            {
                {"ReactJS", "React library for building user interfaces"},
                {"Vue.js", "Progressive JavaScript framework"},
                {"Angular", "TypeScript-based web application framework"},
                {"TypeScript", "Typed superset of JavaScript"},
                {"HTML/CSS", "Web markup and styling"},
                {"Webpack", "Module bundler for JavaScript"},
                {"Redux", "State management library"}
            },
            // Backend Development
            {
                {"Spring Boot", "Java-based framework for microservices"},
                {"Node.js", "JavaScript runtime for server-side"},
                {"Express.js", "Web framework for Node.js"},
                {"Django", "Python web framework"},
                {"Flask", "Lightweight Python web framework"},
                {"ASP.NET Core", "Cross-platform .NET framework"},
                {"GraphQL", "Query language for APIs"}
            },
            // Database
            {
                {"PostgreSQL", "Advanced open-source relational database"},
                {"MySQL", "Popular open-source relational database"},
                {"MongoDB", "NoSQL document database"},
                {"Redis", "In-memory data structure store"},
                {"Elasticsearch", "Distributed search and analytics engine"},
                {"SQL Optimization", "Query optimization techniques"},
                {"Database Design", "Schema design and normalization"}
            },
            // DevOps
            {
                {"Docker", "Containerization platform"},
                {"Kubernetes", "Container orchestration system"},
                {"Jenkins", "Automation server for CI/CD"},
                {"GitLab CI", "Continuous integration and deployment"},
                {"Terraform", "Infrastructure as code tool"},
                {"Ansible", "Configuration management tool"},
                {"AWS", "Amazon Web Services cloud platform"}
            },
            // Mobile Development
            {
                {"React Native", "Cross-platform mobile framework"},
                {"Flutter", "UI toolkit for mobile apps"},
                {"iOS Swift", "Programming language for iOS"},
                {"Android Kotlin", "Modern language for Android"},
                {"Xamarin", "Cross-platform mobile development"},
                {"Mobile UI/UX", "Mobile user interface design"},
                {"Mobile Performance", "App optimization techniques"}
            },
            // Data Science
            {
                {"Python", "Programming language for data science"},
                {"Pandas", "Data manipulation library"},
                {"NumPy", "Numerical computing library"},
                {"Scikit-learn", "Machine learning library"},
                {"TensorFlow", "Deep learning framework"},
                {"Data Visualization", "Charts and graphs"},
                {"Statistical Analysis", "Statistical methods and tests"}
            },
            // Cybersecurity
            {
                {"Network Security", "Securing network infrastructure"},
                {"Web Security", "Web application security"},
                {"Cryptography", "Encryption and decryption"},
                {"Penetration Testing", "Security testing methods"},
                {"OWASP", "Web security best practices"},
                {"Security Auditing", "Security assessment"},
                {"Incident Response", "Handling security incidents"}
            },
            // Cloud Computing
            {
                {"AWS Services", "Amazon Web Services"},
                {"Azure Services", "Microsoft Azure cloud"},
                {"Google Cloud", "Google Cloud Platform"},
                {"Serverless", "Serverless computing"},
                {"Microservices", "Microservices architecture"},
                {"Cloud Security", "Cloud security practices"},
                {"Cloud Cost Optimization", "Managing cloud costs"}
            },
            // Software Testing
            {
                {"Unit Testing", "Testing individual components"},
                {"Integration Testing", "Testing component interactions"},
                {"Test Automation", "Automated testing frameworks"},
                {"Performance Testing", "Load and stress testing"},
                {"Security Testing", "Testing for vulnerabilities"},
                {"Test-Driven Development", "TDD methodology"},
                {"Quality Assurance", "QA processes and practices"}
            },
            // System Design
            {
                {"Design Patterns", "Software design patterns"},
                {"Scalability", "Designing scalable systems"},
                {"Load Balancing", "Distributing traffic"},
                {"Caching Strategies", "Caching techniques"},
                {"API Design", "RESTful and GraphQL APIs"},
                {"Distributed Systems", "Distributed architecture"},
                {"High Availability", "Fault-tolerant systems"}
            }
        };
        
        for (int i = 0; i < fields.size() && i < topicsData.length; i++) {
            Field field = fields.get(i);
            String[][] topics = topicsData[i];
            
            for (String[] topicData : topics) {
                String name = topicData[0];
                String description = topicData[1];
                
                if (!topicRepository.existsByNameAndFieldId(name, field.getId())) {
                    Topic topic = new Topic();
                    topic.setName(name);
                    topic.setDescription(description);
                    topic.setField(field);
                    topicRepository.save(topic);
                    created++;
                    log.debug("Created topic: {} for field: {}", name, field.getName());
                }
            }
        }
        
        return created;
    }
    
    /**
     * Initialize 8 levels from Intern to Architect.
     */
    private int initializeLevels() {
        int created = 0;
        
        String[][] levelsData = {
            {"Intern", "Entry level position", "0", "20"},
            {"Fresher", "Fresh graduate", "21", "35"},
            {"Junior", "Junior developer", "36", "50"},
            {"Middle", "Mid-level developer", "51", "65"},
            {"Senior", "Senior developer", "66", "80"},
            {"Lead", "Tech lead", "81", "90"},
            {"Principal Engineer", "Principal engineer", "91", "95"},
            {"Architect", "Solution architect", "96", "100"}
        };
        
        for (String[] levelData : levelsData) {
            String name = levelData[0];
            String description = levelData[1];
            int minScore = Integer.parseInt(levelData[2]);
            int maxScore = Integer.parseInt(levelData[3]);
            
            if (!levelRepository.existsByName(name)) {
                Level level = new Level();
                level.setName(name);
                level.setDescription(description);
                level.setMinScore(minScore);
                level.setMaxScore(maxScore);
                levelRepository.save(level);
                created++;
                log.debug("Created level: {}", name);
            }
        }
        
        return created;
    }
    
    /**
     * Initialize 3 question types.
     */
    private int initializeQuestionTypes() {
        int created = 0;
        
        String[][] questionTypesData = {
            {"Single Choice", "One correct answer from multiple options"},
            {"Multiple Choice", "Multiple correct answers from options"},
            {"Fill in the Blank", "Open-ended descriptive answer"}
        };
        
        for (String[] questionTypeData : questionTypesData) {
            String name = questionTypeData[0];
            String description = questionTypeData[1];
            
            if (!questionTypeRepository.existsByName(name)) {
                QuestionType questionType = new QuestionType();
                questionType.setName(name);
                questionType.setDescription(description);
                questionTypeRepository.save(questionType);
                created++;
                log.debug("Created question type: {}", name);
            }
        }
        
        return created;
    }
}
