# Design Document - Bulk Question Generation System

## Overview

The Bulk Question Generation System is designed to create 12,000 unique IT interview questions with proper metadata distribution across Fields, Topics, Levels, and Question Types. The system consists of three main components:

1. **Database Initialization Service** - Resets and seeds reference data
2. **Question Content Generator** - Creates realistic, unique question content
3. **Bulk Generation Orchestrator** - Coordinates batch processing and distribution

The system uses a template-based approach with randomization to ensure uniqueness while maintaining realistic content quality.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    API Layer (REST)                          │
│  /api/questions/bulk-generate                                │
│  /api/questions/reset-database                               │
│  /api/questions/initialize-reference-data                    │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│              Bulk Generation Orchestrator                    │
│  • Calculates distribution strategy                          │
│  • Manages batch processing                                  │
│  • Tracks progress and errors                                │
│  • Validates uniqueness                                      │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
┌───────────────────────┐          ┌──────────────────────────┐
│ Question Content      │          │  Database Service        │
│ Generator             │          │  • Reference data CRUD   │
│ • Template engine     │          │  • Question persistence  │
│ • Uniqueness checker  │          │  • Batch commits         │
│ • Topic-specific      │          │  • Uniqueness validation │
│   content             │          └──────────────────────────┘
└───────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                       │
│  • fields, topics, levels, question_types (reference)        │
│  • questions (with unique constraint on content)             │
│  • answers                                                   │
└─────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

1. **Initialization Phase**:
   - Admin calls `/api/questions/reset-database`
   - System drops all questions and answers
   - System recreates or verifies reference data
   - Returns initialization summary

2. **Generation Phase**:
   - Admin calls `/api/questions/bulk-generate` with parameters
   - Orchestrator calculates distribution across combinations
   - For each batch:
     - Content Generator creates unique questions
     - Uniqueness is validated against database
     - Batch is committed to database
     - Progress is reported
   - Returns generation summary with statistics

## Components and Interfaces

### 1. Database Initialization Service

**Interface:**
```java
public interface DatabaseInitializationService {
    InitializationResult resetDatabase();
    InitializationResult initializeReferenceData();
    boolean verifyReferenceData();
}
```

**Responsibilities:**
- Drop all questions and answers
- Create or verify Fields (10 IT domains)
- Create or verify Topics (5+ per field)
- Create or verify Levels (8 levels)
- Create or verify Question Types (3 types)
- Validate referential integrity

### 2. Question Content Generator

**Interface:**
```java
public interface QuestionContentGenerator {
    QuestionContent generateQuestion(
        Field field,
        Topic topic, 
        Level level,
        QuestionType questionType,
        Set<String> existingContent
    );
    
    boolean isUnique(String content, Set<String> existingContent);
}
```

**Responsibilities:**
- Generate realistic question content using templates
- Ensure topic-specific terminology
- Create appropriate answers based on question type
- Validate uniqueness before returning
- Retry generation if duplicate detected

### 3. Bulk Generation Orchestrator

**Interface:**
```java
public interface BulkGenerationOrchestrator {
    GenerationResult generateQuestions(BulkGenerationRequest request);
    GenerationProgress getProgress(String jobId);
    void cancelGeneration(String jobId);
}
```

**Responsibilities:**
- Calculate distribution strategy
- Manage batch processing
- Track progress and report updates
- Handle errors and continue processing
- Validate final results
- Return comprehensive summary

### 4. REST Controller

**Endpoints:**
```java
@RestController
@RequestMapping("/api/questions")
public class BulkQuestionController {
    
    @PostMapping("/reset-database")
    public ResponseEntity<InitializationResult> resetDatabase();
    
    @PostMapping("/initialize-reference-data")
    public ResponseEntity<InitializationResult> initializeReferenceData();
    
    @PostMapping("/bulk-generate")
    public ResponseEntity<GenerationResult> bulkGenerate(
        @RequestBody BulkGenerationRequest request
    );
    
    @GetMapping("/generation-progress/{jobId}")
    public ResponseEntity<GenerationProgress> getProgress(
        @PathVariable String jobId
    );
}
```

## Data Models

### Request Models

```java
public class BulkGenerationRequest {
    private Integer targetCount;      // Default: 12000
    private Integer batchSize;        // Default: 100
    private Long defaultUserId;       // Default: 1
    private Long defaultApproverId;   // Default: 1
    private Boolean dryRun;           // Default: false
}
```

### Response Models

```java
public class GenerationResult {
    private Integer requestedCount;
    private Integer generatedCount;
    private Integer failedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String duration;
    private Map<String, Integer> distributionByField;
    private Map<String, Integer> distributionByLevel;
    private Map<String, Integer> distributionByQuestionType;
    private List<String> errors;
    private Boolean success;
}

public class InitializationResult {
    private Integer fieldsCreated;
    private Integer topicsCreated;
    private Integer levelsCreated;
    private Integer questionTypesCreated;
    private Integer questionsDeleted;
    private Integer answersDeleted;
    private Boolean success;
    private List<String> errors;
}
```

### Reference Data Structure

```java
// Fields (10 total)
public class Field {
    private Long id;
    private String name;        // "Frontend", "Backend", "Database", etc.
    private String description;
}

// Topics (50+ total, 5+ per field)
public class Topic {
    private Long id;
    private String name;        // "ReactJS", "Spring Boot", "PostgreSQL", etc.
    private String description;
    private Long fieldId;       // Foreign key to Field
}

// Levels (8 total)
public class Level {
    private Long id;
    private String name;        // "Intern", "Fresher", "Junior", ..., "Architect"
    private String description;
    private Integer minScore;
    private Integer maxScore;
}

// Question Types (3 total)
public class QuestionType {
    private Long id;
    private String name;        // "Single Choice", "Multiple Choice", "Fill in the Blank"
    private String description;
}
```

### Question Content Templates

The system uses templates to generate realistic questions:

```java
public class QuestionTemplate {
    private QuestionType type;
    private String template;
    private String answerTemplate;
    
    // Examples:
    // Single Choice: "What is the primary purpose of {concept} in {topic}?"
    // Multiple Choice: "Which of the following are benefits of {concept}? (Select all that apply)"
    // Fill in the Blank: "Explain how {concept} improves {aspect} in {topic} applications."
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Exact count generation
*For any* target count specified in the request, the number of questions generated should equal the target count (unless generation fails).
**Validates: Requirements 1.1**

### Property 2: Valid reference data usage
*For any* generated question, its fieldId, topicId, levelId, and questionTypeId must exist in the database reference tables.
**Validates: Requirements 1.2**

### Property 3: Batch processing consistency
*For any* batch size configuration, questions should be committed in groups of that size, and partial batches should be committed at the end.
**Validates: Requirements 1.5**

### Property 4: Complete uniqueness
*For any* two questions in the generated set, their questionContent must be different.
**Validates: Requirements 2.1, 2.4**

### Property 5: Database uniqueness validation
*For any* generated question, its content must not match any existing question content in the database.
**Validates: Requirements 2.2**

### Property 6: Minimum combination coverage
*For any* valid combination of (Field, Topic, Level, QuestionType), there should be at least 10 questions with that combination.
**Validates: Requirements 3.1**

### Property 7: Topic-field relationship integrity
*For any* generated question, the topic's fieldId must equal the question's fieldId.
**Validates: Requirements 3.2**

### Property 8: Balanced field distribution
*For any* field in the system, the number of questions should be proportional to the number of topics in that field (within reasonable variance).
**Validates: Requirements 3.3**

### Property 9: Single choice format compliance
*For any* question with questionTypeId = 1 (Single Choice), the question content should indicate one correct answer selection.
**Validates: Requirements 4.1**

### Property 10: Multiple choice format compliance
*For any* question with questionTypeId = 2 (Multiple Choice), the question content should indicate multiple correct answer selection.
**Validates: Requirements 4.2**

### Property 11: Fill in the blank format compliance
*For any* question with questionTypeId = 3 (Fill in the Blank), the question should be open-ended and the answer should be descriptive (minimum 20 characters).
**Validates: Requirements 4.3**

### Property 12: Topic-specific terminology
*For any* generated question, the question content should contain the topic name or related terminology.
**Validates: Requirements 4.4**

### Property 13: Non-empty answers
*For any* generated question, the questionAnswer field should be non-null and have minimum length of 10 characters.
**Validates: Requirements 4.5**

### Property 14: Approved status initialization
*For all* generated questions, the status field should equal "APPROVED".
**Validates: Requirements 5.1**

### Property 15: English language setting
*For all* generated questions, the language field should equal "en".
**Validates: Requirements 5.2**

### Property 16: Valid user assignments
*For all* generated questions, userId and approvedBy fields should be non-null and positive.
**Validates: Requirements 5.3**

### Property 17: Timestamp initialization
*For all* generated questions, createdAt and approvedAt fields should be non-null and approvedAt should be >= createdAt.
**Validates: Requirements 5.4**

### Property 18: Zero vote initialization
*For all* generated questions, usefulVote and unusefulVote should equal 0.
**Validates: Requirements 5.5**

### Property 19: Minimum topics per field
*For any* field after initialization, there should be at least 5 topics associated with that field.
**Validates: Requirements 8.4**

### Property 20: Topic-field referential integrity
*For any* topic after initialization, its fieldId must reference an existing field.
**Validates: Requirements 8.7**

## Error Handling

### Error Categories

1. **Validation Errors**
   - Invalid parameters (negative counts, zero batch size)
   - Missing reference data
   - Invalid user IDs

2. **Generation Errors**
   - Unable to generate unique content after retries
   - Database connection failures
   - Constraint violations

3. **System Errors**
   - Out of memory
   - Timeout exceeded
   - Database transaction failures

### Error Handling Strategy

```java
public class ErrorHandlingStrategy {
    // Retry logic for transient failures
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    
    // Continue processing on batch failure
    private boolean continueOnBatchFailure = true;
    
    // Rollback strategy
    private RollbackStrategy rollbackStrategy = RollbackStrategy.BATCH_LEVEL;
}
```

### Error Response Format

```java
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private List<String> details;
    private String suggestion;
}
```

## Testing Strategy

### Unit Testing

Unit tests will cover individual components:

- **QuestionContentGenerator**: Test template rendering, uniqueness checking, topic-specific content
- **DatabaseInitializationService**: Test reference data creation, cleanup operations
- **BulkGenerationOrchestrator**: Test distribution calculation, batch management
- **Validation Logic**: Test parameter validation, constraint checking

### Property-Based Testing

Property-based tests will verify universal properties using **JUnit 5** with **jqwik** library (Java property-based testing framework). Each test will run a minimum of 100 iterations.

**Configuration:**
```java
@Property(tries = 100)
```

**Test Structure:**
Each property-based test must be tagged with a comment referencing the design document property:

```java
/**
 * Feature: bulk-question-generation, Property 1: Exact count generation
 */
@Property(tries = 100)
void testExactCountGeneration(@ForAll @IntRange(min = 100, max = 1000) int targetCount) {
    // Test implementation
}
```

### Integration Testing

Integration tests will verify end-to-end workflows:

- Database reset and initialization
- Full bulk generation process
- Error recovery and rollback
- Progress reporting

### Performance Testing

Performance tests will measure:

- Generation speed (questions per minute)
- Memory usage during bulk operations
- Database query performance
- Batch commit efficiency

Target: 12,000 questions in under 30 minutes

## Implementation Notes

### Distribution Algorithm

```java
public class DistributionCalculator {
    public Map<Combination, Integer> calculateDistribution(
        int targetCount,
        List<Field> fields,
        List<Topic> topics,
        List<Level> levels,
        List<QuestionType> questionTypes
    ) {
        // 1. Calculate total valid combinations
        int totalCombinations = countValidCombinations(fields, topics, levels, questionTypes);
        
        // 2. Assign minimum 10 questions per combination
        int baseQuestions = totalCombinations * 10;
        
        // 3. Distribute remaining questions proportionally
        int remaining = targetCount - baseQuestions;
        
        // 4. Allocate extras based on topic count per field
        return distributeProportionally(remaining, fields, topics, levels, questionTypes);
    }
}
```

### Uniqueness Validation Strategy

```java
public class UniquenessValidator {
    private Set<String> generatedContent = new HashSet<>();
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    
    public String ensureUnique(Supplier<String> generator) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String content = generator.get();
            if (!generatedContent.contains(content) && !existsInDatabase(content)) {
                generatedContent.add(content);
                return content;
            }
        }
        throw new UnableToGenerateUniqueContentException();
    }
}
```

### Template System

```java
public class TemplateEngine {
    private Map<QuestionType, List<String>> templates;
    private Random random = new Random();
    
    public String renderQuestion(Topic topic, Level level, QuestionType type) {
        String template = selectRandomTemplate(type);
        return template
            .replace("{topic}", topic.getName())
            .replace("{level}", level.getName())
            .replace("{concept}", selectRandomConcept(topic))
            .replace("{aspect}", selectRandomAspect());
    }
}
```

### Progress Reporting

```java
public class ProgressReporter {
    private int totalQuestions;
    private AtomicInteger processedQuestions = new AtomicInteger(0);
    
    public void reportProgress() {
        int processed = processedQuestions.get();
        int percentage = (processed * 100) / totalQuestions;
        log.info("Progress: {}/{} ({}%)", processed, totalQuestions, percentage);
    }
}
```

## Database Schema Considerations

### Uniqueness Constraint

```sql
ALTER TABLE questions 
ADD CONSTRAINT unique_question_content 
UNIQUE (question_content);
```

### Indexes for Performance

```sql
CREATE INDEX idx_questions_field_id ON questions(field_id);
CREATE INDEX idx_questions_topic_id ON questions(topic_id);
CREATE INDEX idx_questions_level_id ON questions(level_id);
CREATE INDEX idx_questions_question_type_id ON questions(question_type_id);
CREATE INDEX idx_questions_status ON questions(status);
CREATE INDEX idx_topics_field_id ON topics(field_id);
```

### Reference Data

```sql
-- 10 Fields
INSERT INTO fields (name, description) VALUES
('Frontend Development', 'Client-side web technologies'),
('Backend Development', 'Server-side development and APIs'),
('Database', 'Database design and management'),
('DevOps', 'Development operations and deployment'),
('Mobile Development', 'Mobile application development'),
('Data Science', 'Data analysis and machine learning'),
('Cybersecurity', 'Information security and protection'),
('Cloud Computing', 'Cloud platforms and services'),
('Software Testing', 'Quality assurance and testing'),
('System Design', 'Architecture and system design');

-- 8 Levels
INSERT INTO levels (name, description, min_score, max_score) VALUES
('Intern', 'Entry level', 0, 20),
('Fresher', 'Fresh graduate', 21, 35),
('Junior', 'Junior developer', 36, 50),
('Middle', 'Mid-level developer', 51, 65),
('Senior', 'Senior developer', 66, 80),
('Lead', 'Tech lead', 81, 90),
('Principal Engineer', 'Principal engineer', 91, 95),
('Architect', 'Solution architect', 96, 100);

-- 3 Question Types
INSERT INTO question_types (name, description) VALUES
('Single Choice', 'One correct answer from multiple options'),
('Multiple Choice', 'Multiple correct answers from options'),
('Fill in the Blank', 'Open-ended descriptive answer');
```

## Security Considerations

1. **Authorization**: Only admin users should access bulk generation endpoints
2. **Rate Limiting**: Prevent abuse of generation endpoints
3. **Input Validation**: Sanitize all input parameters
4. **SQL Injection**: Use parameterized queries
5. **Resource Limits**: Enforce maximum target count and batch size

## Monitoring and Logging

### Metrics to Track

- Questions generated per minute
- Uniqueness check failures
- Batch commit success rate
- Average question generation time
- Database query performance
- Memory usage during generation

### Log Levels

- **INFO**: Progress updates, batch completions
- **WARN**: Uniqueness conflicts, retry attempts
- **ERROR**: Batch failures, database errors
- **DEBUG**: Individual question generation, template selection

## Deployment Considerations

### Configuration Properties

```yaml
bulk-generation:
  max-target-count: 100000
  default-batch-size: 100
  max-batch-size: 1000
  uniqueness-check-retries: 10
  batch-commit-timeout: 30s
  progress-report-interval: 1000
  default-user-id: 1
  default-approver-id: 1
```

### Resource Requirements

- **Memory**: 2GB minimum for question-service
- **Database**: 1GB minimum for PostgreSQL
- **CPU**: 2 cores minimum
- **Disk**: 2GB for generated data

### Docker Configuration

```yaml
question-service:
  environment:
    - JAVA_OPTS=-Xmx2g -Xms1g
    - SPRING_PROFILES_ACTIVE=docker
  deploy:
    resources:
      limits:
        memory: 2G
      reservations:
        memory: 1G
```
