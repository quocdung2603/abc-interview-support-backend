package com.abc.question_service.service;

import com.abc.question_service.dto.QuestionContent;
import com.abc.question_service.entity.Field;
import com.abc.question_service.entity.Level;
import com.abc.question_service.entity.QuestionType;
import com.abc.question_service.entity.Topic;
import com.abc.question_service.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of QuestionContentGenerator.
 * Uses template-based generation with topic-specific concepts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionContentGeneratorImpl implements QuestionContentGenerator {
    
    private final QuestionRepository questionRepository;
    private final Random random = new Random();
    
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    
    // Template patterns for different question types
    private static final List<String> SINGLE_CHOICE_TEMPLATES = Arrays.asList(
        "What is the primary purpose of {concept} in {topic}?",
        "Which of the following best describes {concept} in {topic}?",
        "In {topic}, what does {concept} primarily handle?",
        "What is the main advantage of using {concept} in {topic}?",
        "Which statement is true about {concept} in {topic}?",
        "What is the correct way to implement {concept} in {topic}?",
        "In {topic}, {concept} is primarily used for what purpose?",
        "Which of the following is a key feature of {concept} in {topic}?",
        "What problem does {concept} solve in {topic}?",
        "How does {concept} improve {aspect} in {topic}?"
    );
    
    private static final List<String> MULTIPLE_CHOICE_TEMPLATES = Arrays.asList(
        "Which of the following are benefits of {concept} in {topic}? (Select all that apply)",
        "Select all correct statements about {concept} in {topic}:",
        "Which of these are valid use cases for {concept} in {topic}? (Select all that apply)",
        "What are the key characteristics of {concept} in {topic}? (Select all that apply)",
        "Which of the following best practices apply to {concept} in {topic}? (Select all that apply)",
        "Select all the ways {concept} can be used in {topic}:",
        "Which statements about {concept} in {topic} are accurate? (Select all that apply)",
        "What are the advantages of using {concept} in {topic}? (Select all that apply)",
        "Which of these are common patterns when working with {concept} in {topic}? (Select all that apply)",
        "Select all the features provided by {concept} in {topic}:"
    );
    
    private static final List<String> FILL_IN_BLANK_TEMPLATES = Arrays.asList(
        "Explain how {concept} works in {topic} and provide an example of its usage.",
        "Describe the role of {concept} in {topic} applications and when you would use it.",
        "Discuss the implementation of {concept} in {topic} and its impact on {aspect}.",
        "Explain the difference between {concept} and alternative approaches in {topic}.",
        "Describe how {concept} improves {aspect} in {topic} applications.",
        "Explain the best practices for using {concept} in {topic} development.",
        "Discuss the advantages and disadvantages of {concept} in {topic}.",
        "Describe a real-world scenario where {concept} is essential in {topic}.",
        "Explain how to optimize {concept} for better {aspect} in {topic}.",
        "Discuss common pitfalls when implementing {concept} in {topic} and how to avoid them."
    );
    
    // Aspects for variation
    private static final List<String> ASPECTS = Arrays.asList(
        "performance", "scalability", "maintainability", "security",
        "user experience", "code quality", "reliability", "efficiency",
        "flexibility", "testability", "readability", "modularity"
    );
    
    @Override
    public QuestionContent generateQuestion(
            Field field,
            Topic topic,
            Level level,
            QuestionType questionType,
            Set<String> existingContent) {
        
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            try {
                QuestionContent content = generateQuestionAttempt(
                    field, topic, level, questionType, attempt
                );
                
                if (isUnique(content.getQuestionText(), existingContent)) {
                    return content;
                }
                
                log.debug("Generated duplicate content on attempt {}, retrying...", attempt + 1);
                
            } catch (Exception e) {
                log.warn("Error generating question on attempt {}: {}", attempt + 1, e.getMessage());
            }
        }
        
        throw new RuntimeException(
            "Unable to generate unique question after " + MAX_GENERATION_ATTEMPTS + " attempts"
        );
    }
    
    @Override
    public boolean isUnique(String content, Set<String> existingContent) {
        if (existingContent.contains(content)) {
            return false;
        }
        
        return !questionRepository.existsByQuestionContent(content);
    }
    
    /**
     * Generates a single question attempt.
     */
    private QuestionContent generateQuestionAttempt(
            Field field,
            Topic topic,
            Level level,
            QuestionType questionType,
            int attempt) {
        
        // Select template based on question type
        String template = selectTemplate(questionType);
        
        // Get topic-specific concepts
        List<String> concepts = getConceptsForTopic(topic.getName());
        String concept = concepts.get(random.nextInt(concepts.size()));
        
        // Add variation based on attempt number
        if (attempt > 0) {
            concept = concept + " " + getVariationSuffix(attempt);
        }
        
        // Select random aspect
        String aspect = ASPECTS.get(random.nextInt(ASPECTS.size()));
        
        // Generate question text
        String questionText = template
            .replace("{topic}", topic.getName())
            .replace("{concept}", concept)
            .replace("{level}", level.getName())
            .replace("{aspect}", aspect);
        
        // Generate answer based on question type
        String answerText = generateAnswer(questionType, topic, concept, level);
        
        return QuestionContent.builder()
            .questionText(questionText)
            .answerText(answerText)
            .build();
    }
    
    /**
     * Selects a template based on question type.
     */
    private String selectTemplate(QuestionType questionType) {
        String typeName = questionType.getName();
        
        if (typeName.contains("Single")) {
            return SINGLE_CHOICE_TEMPLATES.get(random.nextInt(SINGLE_CHOICE_TEMPLATES.size()));
        } else if (typeName.contains("Multiple")) {
            return MULTIPLE_CHOICE_TEMPLATES.get(random.nextInt(MULTIPLE_CHOICE_TEMPLATES.size()));
        } else {
            return FILL_IN_BLANK_TEMPLATES.get(random.nextInt(FILL_IN_BLANK_TEMPLATES.size()));
        }
    }
    
    /**
     * Generates an appropriate answer based on question type.
     */
    private String generateAnswer(QuestionType questionType, Topic topic, String concept, Level level) {
        String typeName = questionType.getName();
        
        if (typeName.contains("Single")) {
            return generateSingleChoiceAnswer(topic, concept, level);
        } else if (typeName.contains("Multiple")) {
            return generateMultipleChoiceAnswer(topic, concept, level);
        } else {
            return generateFillInBlankAnswer(topic, concept, level);
        }
    }
    
    /**
     * Generates a single choice answer.
     */
    private String generateSingleChoiceAnswer(Topic topic, String concept, Level level) {
        List<String> options = Arrays.asList(
            "A) " + concept + " is used for managing state and data flow",
            "B) " + concept + " is primarily for styling and layout",
            "C) " + concept + " is used for database connections",
            "D) " + concept + " is for network communication"
        );
        
        return "Correct Answer: A\n\nExplanation: " + concept + " in " + topic.getName() + 
               " is primarily used for managing state and data flow, which is essential for " +
               level.getName() + " level understanding.";
    }
    
    /**
     * Generates a multiple choice answer.
     */
    private String generateMultipleChoiceAnswer(Topic topic, String concept, Level level) {
        return "Correct Answers: A, C, D\n\n" +
               "A) Improves code organization and maintainability\n" +
               "C) Enhances performance through optimization\n" +
               "D) Provides better error handling capabilities\n\n" +
               "Explanation: " + concept + " in " + topic.getName() + " offers multiple benefits " +
               "that are important for " + level.getName() + " level developers to understand.";
    }
    
    /**
     * Generates a fill in the blank answer.
     */
    private String generateFillInBlankAnswer(Topic topic, String concept, Level level) {
        return concept + " in " + topic.getName() + " is a fundamental concept that enables developers " +
               "to build robust and scalable applications. It works by providing a structured approach " +
               "to handling complex scenarios. For " + level.getName() + " level developers, understanding " +
               concept + " is crucial because it forms the foundation for advanced techniques. " +
               "A typical implementation involves setting up the necessary configuration, " +
               "implementing the core logic, and handling edge cases appropriately. " +
               "Best practices include following established patterns, writing comprehensive tests, " +
               "and documenting the implementation for future maintenance.";
    }
    
    /**
     * Gets topic-specific concepts for content generation.
     */
    private List<String> getConceptsForTopic(String topicName) {
        // Map topics to relevant concepts
        Map<String, List<String>> topicConcepts = new HashMap<>();
        
        // Frontend concepts
        topicConcepts.put("ReactJS", Arrays.asList(
            "hooks", "components", "state management", "props", "virtual DOM",
            "lifecycle methods", "context API", "JSX", "refs", "memoization"
        ));
        topicConcepts.put("Vue.js", Arrays.asList(
            "directives", "computed properties", "watchers", "components", "Vuex",
            "lifecycle hooks", "templates", "reactivity", "mixins", "slots"
        ));
        topicConcepts.put("Angular", Arrays.asList(
            "modules", "components", "services", "dependency injection", "directives",
            "pipes", "routing", "observables", "forms", "HTTP client"
        ));
        topicConcepts.put("TypeScript", Arrays.asList(
            "types", "interfaces", "generics", "decorators", "enums",
            "type inference", "union types", "modules", "namespaces", "type guards"
        ));
        
        // Backend concepts
        topicConcepts.put("Spring Boot", Arrays.asList(
            "dependency injection", "auto-configuration", "REST controllers", "JPA", "transactions",
            "security", "actuator", "profiles", "beans", "annotations"
        ));
        topicConcepts.put("Node.js", Arrays.asList(
            "event loop", "callbacks", "promises", "async/await", "streams",
            "modules", "middleware", "buffers", "clusters", "child processes"
        ));
        topicConcepts.put("Express.js", Arrays.asList(
            "routing", "middleware", "request handling", "response methods", "error handling",
            "template engines", "static files", "sessions", "cookies", "authentication"
        ));
        
        // Database concepts
        topicConcepts.put("PostgreSQL", Arrays.asList(
            "indexes", "transactions", "ACID properties", "joins", "views",
            "stored procedures", "triggers", "constraints", "normalization", "query optimization"
        ));
        topicConcepts.put("MongoDB", Arrays.asList(
            "documents", "collections", "aggregation", "indexing", "sharding",
            "replication", "queries", "schema design", "transactions", "operators"
        ));
        topicConcepts.put("Redis", Arrays.asList(
            "data structures", "caching", "pub/sub", "persistence", "transactions",
            "pipelining", "Lua scripting", "replication", "clustering", "expiration"
        ));
        
        // Default concepts if topic not found
        List<String> concepts = topicConcepts.get(topicName);
        if (concepts == null) {
            concepts = Arrays.asList(
                "core functionality", "best practices", "design patterns", "optimization techniques",
                "error handling", "testing strategies", "configuration", "integration", "deployment", "monitoring"
            );
        }
        
        return concepts;
    }
    
    /**
     * Gets a variation suffix to add uniqueness.
     */
    private String getVariationSuffix(int attempt) {
        String[] suffixes = {
            "patterns", "techniques", "strategies", "approaches", "methods",
            "implementations", "practices", "principles", "concepts", "features"
        };
        return suffixes[attempt % suffixes.length];
    }
}
