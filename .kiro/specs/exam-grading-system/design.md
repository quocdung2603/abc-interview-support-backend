# Design Document: Exam Grading System

## Overview

The Exam Grading System is a comprehensive solution for automatically evaluating user exam submissions within the existing exam-service microservice. The system calculates scores based on exact answer matching, determines pass/fail status, and maintains detailed history of all user answers for review and learning purposes.

The system integrates with the existing Question Service to fetch correct answers and question metadata, ensuring grading is always based on the latest question data. All grading logic is self-contained within the exam-service, with no external dependencies on NLP or similarity calculation services.

## Architecture

### High-Level Architecture

```
┌─────────────────┐
│   API Gateway   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         Exam Service                     │
│  ┌────────────────────────────────────┐ │
│  │   Grading Controller               │ │
│  └──────────┬─────────────────────────┘ │
│             │                            │
│             ▼                            │
│  ┌────────────────────────────────────┐ │
│  │   Grading Service                  │ │
│  │  - Submit & Grade Exam             │ │
│  │  - Calculate Scores                │ │
│  │  - Determine Pass/Fail             │ │
│  │  - Retrieve History                │ │
│  └──────────┬─────────────────────────┘ │
│             │                            │
│             ▼                            │
│  ┌────────────────────────────────────┐ │
│  │   Question Service Client          │ │
│  │  - Fetch Question Details          │ │
│  │  - Get Correct Answers             │ │
│  └────────────────────────────────────┘ │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │   Repositories                     │ │
│  │  - UserAnswerRepository            │ │
│  │  - ResultRepository                │ │
│  │  - ExamRepository                  │ │
│  │  - ExamRegistrationRepository      │ │
│  └────────────────────────────────────┘ │
└──────────────┬───────────────────────────┘
               │
               ▼
        ┌─────────────┐
        │  PostgreSQL │
        └─────────────┘

External Service:
┌──────────────────┐
│ Question Service │
│  - GET /questions/{id}
│  - Provides correct answers
└──────────────────┘
```

### Component Interaction Flow

**Exam Submission & Grading Flow:**
1. User submits answers via POST /exams/{examId}/submit
2. GradingController receives request and validates authentication
3. GradingService validates exam and registration status
4. For each question, GradingService:
   - Fetches correct answer from Question Service
   - Compares user answer with correct answer (case-insensitive, trimmed)
   - Marks answer as correct/incorrect
   - Stores UserAnswer entity
5. GradingService calculates total score
6. GradingService determines pass/fail based on threshold
7. GradingService stores Result entity
8. Response returned with score, pass status, and detailed breakdown

**Exam History Retrieval Flow:**
1. User requests history via GET /exams/{examId}/history
2. GradingController validates user has taken the exam
3. GradingService retrieves all UserAnswer records for exam and user
4. For each answer, GradingService fetches question details from Question Service
5. Response includes question content, user answer, correct answer, and correctness status

## Components and Interfaces

### 1. GradingController

REST controller handling HTTP requests for exam grading operations.

**Endpoints:**

```java
POST /exams/{examId}/submit
- Request: SubmitExamRequest (userId, answers: List<AnswerSubmission>)
- Response: ExamGradingResponse (score, passStatus, totalQuestions, correctAnswers, details)
- Auth: USER, ADMIN
- Description: Submit exam answers and receive immediate grading

GET /exams/{examId}/history
- Query Params: userId (Long)
- Response: ExamHistoryResponse (examId, userId, answers: List<AnswerHistoryItem>)
- Auth: USER (own history), ADMIN (any user)
- Description: Retrieve detailed exam history with question-by-question breakdown

GET /exams/{examId}/results/{userId}
- Response: ResultResponse (score, passStatus, completedAt, feedback)
- Auth: USER (own result), ADMIN, RECRUITER
- Description: Get overall exam result for a specific user
```

### 2. GradingService

Core business logic for exam grading and history management.

**Methods:**

```java
ExamGradingResponse submitAndGradeExam(Long examId, Long userId, List<AnswerSubmission> answers)
- Validates exam exists and is in valid status (PUBLISHED/COMPLETED)
- Validates user is registered for exam
- Validates all submitted question IDs belong to the exam
- Fetches correct answers from Question Service
- Grades each answer using exact matching
- Calculates total score
- Determines pass/fail status (threshold: 70%)
- Stores UserAnswer and Result entities
- Returns detailed grading response

ExamHistoryResponse getExamHistory(Long examId, Long userId)
- Retrieves all UserAnswer records for exam and user
- Fetches question details from Question Service
- Orders answers by question order number
- Returns comprehensive history with question content

ResultResponse getExamResult(Long examId, Long userId)
- Retrieves most recent Result for exam and user
- Returns score, pass status, completion time, feedback
```

### 3. AnswerGrader (Utility Class)

Utility class for answer comparison logic.

**Methods:**

```java
boolean isCorrect(String userAnswer, String correctAnswer)
- Normalizes both answers (lowercase, trim whitespace)
- Performs case-insensitive exact match
- Returns true if answers match, false otherwise

double calculateScore(int correctCount, int totalCount)
- Calculates percentage score
- Returns value between 0.0 and 100.0

boolean determinePassStatus(double score, double threshold)
- Compares score against threshold (default 70%)
- Returns true if passed, false if failed
```

### 4. QuestionServiceClient (Enhanced)

Enhanced client for fetching question data including correct answers.

**Methods:**

```java
QuestionDTO getQuestionWithAnswer(Long questionId)
- Fetches question details including correct answer
- Implements retry logic (3 attempts with exponential backoff)
- Caches question data during grading operation
- Throws exception if question not found or service unavailable

Map<Long, QuestionDTO> getQuestionsWithAnswers(List<Long> questionIds)
- Batch fetches multiple questions
- Returns map of questionId -> QuestionDTO
- More efficient than individual calls
```

### 5. Repositories

**UserAnswerRepository:**
```java
List<UserAnswer> findByExamIdAndUserIdOrderByCreatedAtAsc(Long examId, Long userId)
Optional<UserAnswer> findByExamIdAndUserIdAndQuestionId(Long examId, Long userId, Long questionId)
void deleteByExamId(Long examId)
```

**ResultRepository:**
```java
Optional<Result> findTopByExamIdAndUserIdOrderByCompletedAtDesc(Long examId, Long userId)
List<Result> findByExamId(Long examId)
List<Result> findByUserId(Long userId)
```

**ExamRegistrationRepository:**
```java
boolean existsByExamIdAndUserIdAndRegistrationStatus(Long examId, Long userId, String status)
```

## Data Models

### Existing Entities (No Changes)

**UserAnswer** (already exists):
```java
@Entity
@Table(name = "user_answers")
class UserAnswer {
    Long id;
    Exam exam;              // ManyToOne relationship
    Long questionId;
    Long userId;
    String answerContent;   // User's submitted answer
    Boolean isCorrect;      // Grading result
    Double similarityScore; // Not used (kept for backward compatibility)
    LocalDateTime createdAt;
}
```

**Result** (already exists):
```java
@Entity
@Table(name = "results")
class Result {
    Long id;
    Exam exam;              // ManyToOne relationship
    Long userId;
    Double score;           // Percentage score (0-100)
    Boolean passStatus;     // true if passed, false if failed
    String feedback;        // Optional feedback text
    LocalDateTime completedAt;
}
```

### New DTOs

**SubmitExamRequest:**
```java
class SubmitExamRequest {
    Long userId;
    List<AnswerSubmission> answers;
}

class AnswerSubmission {
    Long questionId;
    String answerContent;
}
```

**ExamGradingResponse:**
```java
class ExamGradingResponse {
    Long examId;
    Long userId;
    Double score;              // Percentage (0-100)
    Boolean passStatus;
    Integer totalQuestions;
    Integer correctAnswers;
    Integer incorrectAnswers;
    LocalDateTime completedAt;
    List<AnswerGradingDetail> details;
}

class AnswerGradingDetail {
    Long questionId;
    String userAnswer;
    Boolean isCorrect;
    String correctAnswer;    // Only shown after submission
}
```

**ExamHistoryResponse:**
```java
class ExamHistoryResponse {
    Long examId;
    Long userId;
    String examTitle;
    Double score;
    Boolean passStatus;
    LocalDateTime completedAt;
    List<AnswerHistoryItem> answers;
}

class AnswerHistoryItem {
    Long questionId;
    Integer orderNumber;
    String questionContent;
    String userAnswer;
    String correctAnswer;
    Boolean isCorrect;
    QuestionMetadata metadata;
}

class QuestionMetadata {
    Long fieldId;
    String fieldName;
    List<Long> topicIds;
    List<String> topicNames;
    Long levelId;
    String levelName;
    Long questionTypeId;
    String questionTypeName;
}
```

## Data Flow

### Submit and Grade Exam

```
User → Controller → Service
                      ↓
                   Validate Exam (PUBLISHED/COMPLETED)
                      ↓
                   Validate Registration (REGISTERED)
                      ↓
                   Validate Question IDs
                      ↓
                   For Each Answer:
                      ├→ Fetch Question from Question Service
                      ├→ Extract Correct Answer
                      ├→ Compare with User Answer (case-insensitive)
                      ├→ Mark as Correct/Incorrect
                      └→ Save UserAnswer Entity
                      ↓
                   Calculate Total Score
                      ↓
                   Determine Pass/Fail (threshold: 70%)
                      ↓
                   Save Result Entity
                      ↓
                   Build Response with Details
                      ↓
Service → Controller → User
```

### Retrieve Exam History

```
User → Controller → Service
                      ↓
                   Fetch UserAnswers (exam + user)
                      ↓
                   For Each Answer:
                      ├→ Fetch Question Details from Question Service
                      ├→ Extract Question Content
                      ├→ Extract Metadata (field, topics, level, type)
                      └→ Build AnswerHistoryItem
                      ↓
                   Fetch Result (score, pass status)
                      ↓
                   Order by Question Order Number
                      ↓
                   Build ExamHistoryResponse
                      ↓
Service → Controller → User
```



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Answer storage completeness
*For any* valid exam submission with N answers, storing the submission should result in exactly N UserAnswer records being created in the database
**Validates: Requirements 1.2**

### Property 2: Score calculation accuracy
*For any* set of graded answers, the calculated score should equal (correct answers / total answers) * 100
**Validates: Requirements 1.3**

### Property 3: Pass status consistency
*For any* calculated score and threshold, if score >= threshold then passStatus should be true, otherwise false
**Validates: Requirements 1.4**

### Property 4: Result persistence
*For any* completed grading operation, a Result record should exist with the calculated score, pass status, and completion timestamp
**Validates: Requirements 1.5**

### Property 5: History completeness
*For any* exam and user combination where answers exist, retrieving history should return all UserAnswer records for that exam and user
**Validates: Requirements 2.1**

### Property 6: History content accuracy
*For any* answer in exam history, the response should include question content, user answer, correct answer, and correctness status
**Validates: Requirements 2.2**

### Property 7: History ordering
*For any* exam history response, answers should be ordered by their question order number in ascending order
**Validates: Requirements 2.3**

### Property 8: Empty history handling
*For any* exam and user combination where no answers exist, retrieving history should return an empty list with appropriate status
**Validates: Requirements 2.4**

### Property 9: Exact matching correctness
*For any* two normalized strings (lowercase, trimmed), they should be marked as matching if and only if they are identical
**Validates: Requirements 3.1, 3.5**

### Property 10: Correct answer scoring
*For any* answer that matches exactly, the answer should be marked as correct (isCorrect = true) and contribute 1 point to the score
**Validates: Requirements 3.2**

### Property 11: Incorrect answer scoring
*For any* answer that does not match, the answer should be marked as incorrect (isCorrect = false) and contribute 0 points to the score
**Validates: Requirements 3.3**

### Property 12: Result retrieval accuracy
*For any* exam and user with multiple results, retrieving the result should return the most recent one based on completion timestamp
**Validates: Requirements 4.5**

### Property 13: Question data retrieval
*For any* grading operation, correct answers should be fetched from Question Service before comparison
**Validates: Requirements 5.1, 5.2**

### Property 14: Exam validation
*For any* submission, if the exam does not exist or is not in PUBLISHED/COMPLETED status, the submission should be rejected
**Validates: Requirements 6.1**

### Property 15: Registration validation
*For any* submission, if the user is not registered for the exam with status REGISTERED, the submission should be rejected
**Validates: Requirements 6.2**

### Property 16: Question ID validation
*For any* submission containing question IDs not in the exam, the submission should be rejected with a validation error
**Validates: Requirements 6.3**

### Property 17: Empty answer handling
*For any* empty or null answer, it should be treated as incorrect with zero points
**Validates: Requirements 6.4**

### Property 18: History metadata inclusion
*For any* exam history item, the response should include question metadata (topic, level, question type)
**Validates: Requirements 7.1**

## Error Handling

### Validation Errors

**Invalid Exam Status:**
- HTTP 400 Bad Request
- Message: "Exam is not available for submission. Current status: {status}"
- Occurs when exam is in DRAFT or CANCELLED status

**User Not Registered:**
- HTTP 403 Forbidden
- Message: "User is not registered for this exam"
- Occurs when no active registration exists

**Invalid Question IDs:**
- HTTP 400 Bad Request
- Message: "Invalid question IDs: {ids}. These questions are not part of the exam"
- Occurs when submitted question IDs don't match exam questions

**Missing Answers:**
- HTTP 400 Bad Request
- Message: "Incomplete submission. Expected {expected} answers, received {actual}"
- Occurs when not all exam questions are answered

### Service Errors

**Question Service Unavailable:**
- HTTP 503 Service Unavailable
- Message: "Unable to fetch question data. Please try again later"
- Occurs after 3 failed retry attempts
- Grading operation is aborted

**Database Errors:**
- HTTP 500 Internal Server Error
- Message: "Failed to save grading results. Please contact support"
- Transaction is rolled back
- No partial data is saved

### Not Found Errors

**Exam Not Found:**
- HTTP 404 Not Found
- Message: "Exam not found with id: {examId}"

**Result Not Found:**
- HTTP 404 Not Found
- Message: "No result found for exam {examId} and user {userId}"

**Empty History:**
- HTTP 200 OK
- Empty answers list with message: "No submission found for this exam"

### Retry Logic

**Question Service Calls:**
- Initial attempt
- Retry 1: Wait 1 second
- Retry 2: Wait 2 seconds
- Retry 3: Wait 4 seconds
- After 3 failures: Return 503 error

## Testing Strategy

### Unit Testing

The system will use **JUnit 5** and **Mockito** for unit testing.

**Test Coverage:**

1. **AnswerGrader Tests:**
   - Test exact matching with various cases (uppercase, lowercase, mixed)
   - Test whitespace handling (leading, trailing, multiple spaces)
   - Test empty and null inputs
   - Test score calculation with different correct/total ratios
   - Test pass/fail determination with boundary values (69.9%, 70%, 70.1%)

2. **GradingService Tests:**
   - Test validation logic (exam status, registration, question IDs)
   - Test grading flow with mocked Question Service
   - Test score calculation with various answer combinations
   - Test Result and UserAnswer entity creation
   - Test error handling for service failures

3. **GradingController Tests:**
   - Test request validation
   - Test authentication and authorization
   - Test response formatting
   - Test error response structure

4. **Repository Tests:**
   - Test query methods with test data
   - Test ordering and filtering
   - Test cascade operations

### Property-Based Testing

The system will use **jqwik** (Java property-based testing library) for property-based tests.

**Configuration:**
- Each property test will run a minimum of 100 iterations
- Tests will use random data generators for comprehensive coverage

**Property Test Coverage:**

Each correctness property listed in the Correctness Properties section will be implemented as a property-based test. Tests will be tagged with comments referencing the design document property number.

**Example Property Test Structure:**
```java
@Property
// Feature: exam-grading-system, Property 1: Answer storage completeness
void answerStorageCompleteness(@ForAll @IntRange(min = 1, max = 50) int answerCount) {
    // Generate random exam with N questions
    // Submit N answers
    // Verify exactly N UserAnswer records exist
}
```

### Integration Testing

**Test Scenarios:**

1. **End-to-End Grading Flow:**
   - Create exam with questions
   - Register user
   - Submit answers
   - Verify Result and UserAnswer records
   - Retrieve history
   - Verify response completeness

2. **Question Service Integration:**
   - Test with real Question Service
   - Test retry logic with simulated failures
   - Test caching behavior

3. **Database Transaction Tests:**
   - Test rollback on errors
   - Test concurrent submissions
   - Test data consistency

### Performance Testing

**Metrics to Monitor:**

- Grading time for exams with 10, 50, 100 questions
- Question Service call latency
- Database query performance
- Memory usage during batch operations

**Acceptance Criteria:**

- Grade 50-question exam in < 5 seconds
- Support 100 concurrent grading operations
- History retrieval in < 2 seconds for 100-question exam

## Security Considerations

### Authentication & Authorization

- All endpoints require valid JWT token
- Users can only submit/view their own exams (except ADMIN)
- ADMIN and RECRUITER can view any user's results
- Registration validation prevents unauthorized exam access

### Data Privacy

- Correct answers only shown after submission
- User answers are private (not visible to other users)
- Results include feedback field for teacher comments

### Input Validation

- All request DTOs use Jakarta Validation annotations
- Question IDs validated against exam questions
- User IDs validated against registrations
- Answer content sanitized to prevent injection

## Deployment Considerations

### Database Migrations

**New Indexes:**
```sql
CREATE INDEX idx_user_answers_exam_user ON user_answers(exam_id, user_id);
CREATE INDEX idx_results_exam_user ON results(exam_id, user_id);
CREATE INDEX idx_results_completed_at ON results(completed_at DESC);
```

### Configuration

**Application Properties:**
```yaml
grading:
  pass-threshold: 70.0  # Percentage required to pass
  retry:
    max-attempts: 3
    initial-delay: 1000  # milliseconds
    multiplier: 2.0
  cache:
    question-ttl: 300  # seconds (5 minutes)
```

### Monitoring

**Metrics to Track:**

- Grading success rate
- Average grading time
- Question Service call failures
- Database transaction failures
- Cache hit rate

**Alerts:**

- Question Service unavailable for > 5 minutes
- Grading failure rate > 5%
- Average grading time > 10 seconds
- Database connection pool exhaustion

## Future Enhancements

1. **Partial Credit:** Support for answers that are partially correct
2. **Question Weights:** Different questions worth different points
3. **Time Tracking:** Track time spent on each question
4. **Analytics:** Aggregate statistics on question difficulty
5. **Batch Grading:** Grade multiple exams simultaneously
6. **Manual Review:** Flag answers for manual review by teachers
7. **Feedback System:** Allow teachers to add detailed feedback per question
8. **Regrading:** Support for regrading exams when answers change
