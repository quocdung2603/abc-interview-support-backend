# Design Document

## Overview

The Question Bulk Generation system is a utility service that generates large volumes of unique, realistic IT interview questions for the ABC Interview platform. The system will create questions distributed across all valid combinations of fields, topics, levels, and question types while ensuring complete uniqueness of question content.

The generator will be implemented as a Spring Boot service component that can be invoked via a REST API endpoint or command-line interface. It will leverage the existing question-service data model and repositories while adding specialized generation logic.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Question Service                          │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │         Bulk Generation Controller                  │    │
│  │  - POST /api/questions/bulk-generate               │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐    │
│  │      Question Generator Service                     │    │
│  │  - Orchestrates generation process                  │    │
│  │  - Validates parameters                             │    │
│  │  - Manages batch persistence                        │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐    │
│  │      Question Content Generator                     │    │
│  │  - Generates unique question content                │    │
│  │  - Creates contextually appropriate answers         │    │
│  │  - Ensures content uniqueness                       │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐    │
│  │      Distribution Strategy                          │    │
│  │  - Calculates question distribution                 │    │
│  │  - Ensures coverage of all combinations             │    │
│  └────────────────┬───────────────────────────────────┘    │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐    │
│  │      Existing Repositories                          │    │
│  │  - QuestionRepository                               │    │
│  │  - FieldRepository                                  │    │
│  │  - TopicRepository                                  │    │
│  │  - LevelRepository                                  │    │
│  │  - QuestionTypeRepository                           │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

1. Administrator invokes bulk generation endpoint with target count
2. Generator Service loads all fields, topics, levels, and question types from database
3. Distribution Strategy calculates how many questions per combination
4. Question Content Generator creates unique questions in batches
5. Each batch is validated for uniqueness and persisted to database
6. Process continues until target count is reached
7. Final report is returned to administrator

## Components and Interfaces

### 1. BulkGenerationController

REST controller that exposes the bulk generation functionality.

```java
@RestController
@RequestMapping("/api/questions")
public class BulkGenerationController {
    
    @PostMapping("/bulk-generate")
    public ResponseEntity<GenerationReport> generateQuestions(
        @RequestBody GenerationRequest request
    );
}
```

**Request DTO:**
```java
public class GenerationRequest {
    private Integer targetCount;      // Number of questions to generate
    private Integer batchSize;        // Optional: batch size for persistence
    private Long defaultUserId;       // User ID to assign to questions
    private Long defaultApproverId;   // Approver ID for questions
}
```

**Response DTO:**
```java
public class GenerationReport {
    private Integer requestedCount;
    private Integer generatedCount;
    private Integer failedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private Map<String, Integer> distributionByField;
    private List<String> errors;
}
```

### 2. QuestionGeneratorService

Core service that orchestrates the generation process.

```java
@Service
public class QuestionGeneratorService {
    
    public GenerationReport generateQuestions(GenerationRequest request);
    
    private List<Field> loadFields();
    private List<Topic> loadTopics();
    private List<Level> loadLevels();
    private List<QuestionType> loadQuestionTypes();
    
    private Map<String, Integer> calculateDistribution(
        int targetCount,
        List<Field> fields,
        List<Topic> topics,
        List<Level> levels,
        List<QuestionType> questionTypes
    );
    
    private List<Question> generateBatch(
        Map<String, Integer> distribution,
        int batchSize
    );
    
    private void persistBatch(List<Question> questions);
}
```

### 3. QuestionContentGenerator

Generates unique question content and answers based on context.

```java
@Component
public class QuestionContentGenerator {
    
    public String generateQuestionContent(
        Field field,
        Topic topic,
        Level level,
        QuestionType questionType,
        int sequenceNumber
    );
    
    public String generateQuestionAnswer(
        String questionContent,
        QuestionType questionType
    );
    
    private String generateSingleChoiceQuestion(
        Topic topic,
        Level level,
        int sequenceNumber
    );
    
    private String generateMultipleChoiceQuestion(
        Topic topic,
        Level level,
        int sequenceNumber
    );
    
    private String generateFillInBlankQuestion(
        Topic topic,
        Level level,
        int sequenceNumber
    );
}
```

### 4. DistributionStrategy

Calculates how questions should be distributed across combinations.

```java
@Component
public class DistributionStrategy {
    
    public Map<CombinationKey, Integer> calculateDistribution(
        int targetCount,
        List<Field> fields,
        List<Topic> topics,
        List<Level> levels,
        List<QuestionType> questionTypes
    );
    
    private int calculateMinimumQuestions(
        int totalCombinations
    );
    
    private Map<CombinationKey, Integer> distributeRemaining(
        int remainingCount,
        Map<CombinationKey, Integer> baseDistribution
    );
}

public class CombinationKey {
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    private Long questionTypeId;
}
```

## Data Models

### Question Entity (Existing)

The existing Question entity will be used without modification:

```java
@Entity
@Table(name = "questions")
public class Question {
    private Long id;
    private Long userId;
    private Topic topic;
    private Field field;
    private Level level;
    private QuestionType questionType;
    private String questionContent;
    private String questionAnswer;
    private Double similarityScore;
    private String status;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private Integer usefulVote;
    private Integer unusefulVote;
}
```

### Supporting Entities (Existing)

- **Field**: IT domain categories (Frontend, Backend, etc.)
- **Topic**: Specific technologies within fields (ReactJS, Spring Boot, etc.)
- **Level**: Experience levels (Intern, Junior, Senior, etc.)
- **QuestionType**: Question formats (Single Choice, Multiple Choice, Fill in the Blank)

### Question Content Templates

The generator will use template-based content generation with variations:

**Single Choice Templates:**
- "What is the primary purpose of {concept} in {topic}?"
- "Which of the following best describes {concept} in {topic}?"
- "In {topic}, what happens when {scenario}?"
- "Which {topic} feature is used to {action}?"

**Multiple Choice Templates:**
- "Select all correct statements about {concept} in {topic}:"
- "Which of the following are valid {concept} in {topic}? (Select all that apply)"
- "In {topic}, which approaches can be used to {action}? (Multiple answers)"

**Fill in the Blank Templates:**
- "Explain how {concept} works in {topic} and provide an example."
- "Describe the process of {action} in {topic}."
- "What are the key differences between {concept1} and {concept2} in {topic}?"

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Generated count matches target count
*For any* valid target count, when the generator completes, the number of questions created should equal the target count.
**Validates: Requirements 1.1**

### Property 2: All question content is unique
*For any* set of generated questions, no two questions should have identical questionContent values.
**Validates: Requirements 1.2**

### Property 3: Topic-field relationship integrity
*For any* generated question, the topic's fieldId should match the question's fieldId.
**Validates: Requirements 2.2**

### Property 4: All foreign keys reference existing entities
*For any* generated question, the fieldId, topicId, levelId, and questionTypeId should all reference existing entities in their respective tables.
**Validates: Requirements 2.3, 2.4, 2.5, 2.6**

### Property 5: Timestamp ordering
*For any* generated question, the createdAt timestamp should be less than or equal to the approvedAt timestamp, and both should be non-null.
**Validates: Requirements 1.5**

### Property 6: Minimum distribution coverage
*For any* generation run with sufficient target count, each valid combination of field, topic, level, and questionType should have at least 10 unique questions.
**Validates: Requirements 2.1**

### Property 7: Topic name appears in question content
*For any* generated question, the questionContent should contain the topic name as a substring.
**Validates: Requirements 3.4**

### Property 8: Initial field values are correctly set
*For any* generated question, usefulVote should equal 0, unusefulVote should equal 0, similarityScore should equal 0.0, status should equal "APPROVED", and language should equal "en".
**Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**

### Property 9: Valid user and approver assignment
*For any* generated question, userId and approvedBy should be non-null and positive.
**Validates: Requirements 1.4**

### Property 10: Generation report accuracy
*For any* generation run, the GenerationReport's generatedCount should equal the number of questions actually persisted to the database.
**Validates: Requirements 4.5**

## Error Handling

### Validation Errors

1. **Invalid Target Count**: If target count is less than or equal to zero, return HTTP 400 with error message
2. **Insufficient Combinations**: If target count exceeds possible unique combinations, return HTTP 400 with warning
3. **Missing Reference Data**: If fields, topics, levels, or question types are not found in database, return HTTP 500 with error details

### Database Errors

1. **Constraint Violations**: Catch and log constraint violations, skip the problematic question, continue with generation
2. **Connection Failures**: Retry up to 3 times with exponential backoff, then fail the entire batch
3. **Transaction Failures**: Roll back the current batch, report partial success in GenerationReport

### Content Generation Errors

1. **Duplicate Content**: If duplicate content is detected, regenerate with different sequence number
2. **Template Errors**: Log template rendering errors and use fallback template

### Error Response Format

```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Target count must be greater than zero",
  "path": "/api/questions/bulk-generate"
}
```

## Testing Strategy

### Unit Testing

The system will use JUnit 5 for unit testing with the following focus areas:

1. **Content Generation Logic**
   - Test template rendering with various inputs
   - Verify content uniqueness within small batches
   - Test answer generation for each question type

2. **Distribution Calculation**
   - Test distribution algorithm with various target counts
   - Verify minimum coverage requirements
   - Test edge cases (small counts, large counts)

3. **Validation Logic**
   - Test referential integrity checks
   - Test field initialization
   - Test timestamp validation

### Property-Based Testing

The system will use **JUnit QuickCheck** (already included in pom.xml) for property-based testing. Each property test will run a minimum of 100 iterations.

Each property-based test will be tagged with a comment explicitly referencing the correctness property from this design document using the format: `**Feature: question-bulk-generation, Property {number}: {property_text}**`

Property tests will focus on:

1. **Uniqueness Properties** (Property 2)
   - Generate random batches of questions
   - Verify no duplicate content exists

2. **Referential Integrity Properties** (Properties 3, 4)
   - Generate questions with random entity references
   - Verify all references are valid

3. **Distribution Properties** (Property 6)
   - Generate with random target counts
   - Verify minimum coverage across combinations

4. **Initialization Properties** (Properties 8, 9)
   - Generate random questions
   - Verify all fields are correctly initialized

5. **Count Properties** (Properties 1, 10)
   - Generate with random target counts
   - Verify output matches input

### Integration Testing

Integration tests will verify:

1. **End-to-End Generation Flow**
   - Invoke REST endpoint with test database
   - Verify questions are persisted correctly
   - Verify report accuracy

2. **Batch Processing**
   - Test with various batch sizes
   - Verify transaction boundaries
   - Test rollback scenarios

3. **Error Handling**
   - Test with invalid inputs
   - Test with database failures
   - Verify error responses

### Test Data Strategy

- Use H2 in-memory database for unit and property tests
- Seed test database with minimal reference data (2 fields, 4 topics, 3 levels, 3 question types)
- Use test containers for integration tests with PostgreSQL

## Performance Considerations

### Batch Processing

- Default batch size: 100 questions per transaction
- Configurable via request parameter
- Larger batches improve performance but increase memory usage

### Memory Management

- Generate questions in batches to avoid loading all in memory
- Use streaming for large result sets
- Clear entity manager cache between batches

### Database Optimization

- Use batch inserts for better performance
- Disable unnecessary validation triggers during bulk insert
- Create indexes on foreign key columns if not already present

### Estimated Performance

- Target: 1000 questions per minute
- Memory usage: ~50MB for 10,000 questions
- Database size: ~1KB per question

## Security Considerations

### Authentication and Authorization

- Endpoint requires ADMIN role
- JWT token validation required
- Rate limiting: 1 request per minute per user

### Input Validation

- Validate target count range (1 to 100,000)
- Validate batch size range (10 to 1,000)
- Sanitize all user inputs

### Audit Logging

- Log all generation requests with user ID and timestamp
- Log generation results (success/failure counts)
- Log any errors or constraint violations

## Deployment Considerations

### Configuration

Add to application.yml:

```yaml
question:
  generation:
    max-target-count: 100000
    default-batch-size: 100
    max-batch-size: 1000
    default-user-id: 1
    default-approver-id: 1
```

### Database Migration

No schema changes required - uses existing tables.

### Monitoring

- Expose metrics for generation requests
- Track generation duration
- Monitor database connection pool usage during bulk operations

## Future Enhancements

1. **AI-Powered Content Generation**: Integrate with LLM for more realistic question content
2. **Content Quality Scoring**: Add automated quality assessment for generated questions
3. **Parallel Generation**: Support multi-threaded generation for better performance
4. **Resume Capability**: Support resuming interrupted generation processes
5. **Custom Templates**: Allow administrators to provide custom question templates
6. **Language Support**: Generate questions in multiple languages
