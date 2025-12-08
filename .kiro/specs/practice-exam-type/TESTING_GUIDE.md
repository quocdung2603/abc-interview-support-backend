# Testing Guide - PRACTICE Exam Type

## Prerequisites

### 1. Database Setup
Ensure PostgreSQL is running with the exam_db database:

```bash
# Using Docker Compose (recommended)
docker-compose up -d postgres

# Or start manually
# Check docker-compose.yml for connection details
```

### 2. Application Configuration
Verify `exam-service/src/main/resources/application.yml` has correct database settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/exam_db
    username: postgres
    password: your_password
```

## Running Tests

### Run All Tests
```bash
cd exam-service
mvn test
```

### Run Specific Test Class
```bash
# Property test for exam type identification
mvn test -Dtest=ExamTypeRegistrationRequirementPropertyTest

# Property test for auto-publish
mvn test -Dtest=PracticeExamAutoPublishPropertyTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=PracticeExamAutoPublishPropertyTest#practiceExamShouldBeAutoPublished
```

## Test Categories

### 1. Property-Based Tests ✅

**ExamTypeRegistrationRequirementPropertyTest**
- Location: `exam-service/src/test/java/com/abc/exam_service/service/`
- Tests: Exam type identification and registration requirements
- Iterations: 100 per test
- Status: ✅ Written, needs database

**PracticeExamAutoPublishPropertyTest**
- Location: `exam-service/src/test/java/com/abc/exam_service/service/`
- Tests: Auto-publish behavior for PRACTICE exams
- Iterations: 100 per test
- Status: ✅ Written, needs database

### 2. Property Tests To Be Written

The following property tests should be created following the same pattern:

#### Exam Metadata Tests
```java
// Property 2: PRACTICE exam metadata persistence
@Test
public void practiceExamShouldPersistAllMetadata() {
    // For any PRACTICE exam with random metadata
    // When created and retrieved
    // Then all fields should match
}

// Property 3: PRACTICE exam creator tracking
@Test
public void practiceExamShouldTrackCreator() {
    // For any userId
    // When creating PRACTICE exam
    // Then createdBy should equal userId
}

// Property 4: PRACTICE exam timestamp generation
@Test
public void practiceExamShouldHaveTimestamp() {
    // For any PRACTICE exam
    // When created
    // Then createdAt should be within 1 second of now
}
```

#### Answer Submission Tests
```java
// Property 6: PRACTICE exam answer submission without registration
@Test
public void practiceExamShouldAcceptAnswerWithoutRegistration() {
    // For any PRACTICE exam and any user
    // When submitting answer without registration
    // Then answer should be saved successfully
}

// Property 7: Non-PRACTICE exam answer validation
@Test
public void virtualExamShouldRequireRegistrationForAnswer() {
    // For any VIRTUAL/RECRUITER exam
    // When submitting answer without registration
    // Then should throw exception
}

// Property 8: Answer data persistence
@Test
public void answerShouldPersistAllFields() {
    // For any answer submission
    // When saved and retrieved
    // Then all fields should match
}
```

#### Result Submission Tests
```java
// Property 9: PRACTICE exam result submission without registration
@Test
public void practiceExamShouldAcceptResultWithoutRegistration() {
    // For any PRACTICE exam and any user
    // When submitting result without registration
    // Then result should be saved successfully
}

// Property 10: Non-PRACTICE exam result validation
@Test
public void virtualExamShouldRequireRegistrationForResult() {
    // For any VIRTUAL/RECRUITER exam
    // When submitting result without registration
    // Then should throw exception
}

// Property 11: Result data persistence
@Test
public void resultShouldPersistAllFields() {
    // For any result submission
    // When saved and retrieved
    // Then all fields should match
}
```

### 3. Unit Tests for Error Handling

Create `PracticeExamErrorHandlingTest.java`:

```java
@Test
public void shouldThrowExceptionForNonExistentExam() {
    // When submitting answer for non-existent exam
    // Then should throw RuntimeException with message
}

@Test
public void shouldThrowExceptionForInvalidRegistration() {
    // When submitting answer for VIRTUAL exam without registration
    // Then should throw RuntimeException
}

@Test
public void shouldThrowExceptionForCancelledRegistration() {
    // When submitting with CANCELLED registration
    // Then should throw "Registration is not active"
}

@Test
public void shouldThrowExceptionWhenNoQuestionsFound() {
    // When creating exam with criteria matching no questions
    // Then should throw "No questions found"
}
```

### 4. Integration Tests

Create `PracticeExamIntegrationTest.java`:

```java
@Test
public void completeP racticeExamFlow() {
    // 1. Create PRACTICE exam
    // 2. Verify status is PUBLISHED
    // 3. Submit multiple answers (no registration)
    // 4. Submit result (no registration)
    // 5. Verify all data persisted correctly
}

@Test
public void virtualExamBackwardCompatibility() {
    // 1. Create VIRTUAL exam
    // 2. Verify status is DRAFT
    // 3. Try submit answer without registration → expect error
    // 4. Register for exam
    // 5. Submit answer → expect success
}

@Test
public void practiceExamWithRandomQuestions() {
    // 1. Create PRACTICE exam with random questions
    // 2. Verify exam created with PUBLISHED status
    // 3. Verify questions added
    // 4. Verify question count matches request
}
```

## Manual Testing with Postman

### 1. Create PRACTICE Exam

**POST** `/api/exams`
```json
{
  "userId": 1,
  "examType": "PRACTICE",
  "title": "Java Practice Test",
  "position": "Backend Developer",
  "fieldId": 1,
  "topicIds": [1, 2],
  "levelId": 2,
  "questionTypeIds": [1],
  "questionCount": 10,
  "duration": 60,
  "language": "en"
}
```

**Expected Response:**
```json
{
  "id": 123,
  "status": "PUBLISHED",  // ✅ Auto-published
  "examType": "PRACTICE",
  // ... other fields
}
```

### 2. Submit Answer (No Registration)

**POST** `/api/exams/answers`
```json
{
  "examId": 123,
  "userId": 1,
  "questionId": 456,
  "answerContent": "My answer here"
}
```

**Expected:** ✅ Success (no registration check)

### 3. Submit Result (No Registration)

**POST** `/api/exams/results`
```json
{
  "examId": 123,
  "userId": 1,
  "score": 85.5,
  "passStatus": true
}
```

**Expected:** ✅ Success (no registration check)

### 4. Verify VIRTUAL Exam Still Requires Registration

**POST** `/api/exams`
```json
{
  "userId": 1,
  "examType": "VIRTUAL",
  // ... other fields
}
```

**Expected Response:**
```json
{
  "status": "DRAFT"  // ✅ Not auto-published
}
```

**POST** `/api/exams/answers` (without registration)
**Expected:** ❌ Error "User must register for this exam before submitting"

## Test Execution Checklist

- [ ] Database is running
- [ ] Application properties configured
- [ ] Run `ExamTypeRegistrationRequirementPropertyTest` - should pass
- [ ] Run `PracticeExamAutoPublishPropertyTest` - should pass
- [ ] Create remaining property tests
- [ ] Create unit tests for error handling
- [ ] Create integration tests
- [ ] Run full test suite: `mvn test`
- [ ] Manual testing with Postman
- [ ] Verify backward compatibility with VIRTUAL/RECRUITER exams

## Troubleshooting

### Database Connection Error
```
Unable to determine Dialect without JDBC metadata
```
**Solution:** Start PostgreSQL database and verify connection settings

### Test Fails with "Exam not found"
**Solution:** Ensure `@Transactional` annotation is present on test class

### Registration Validation Fails
**Solution:** Check that `ExamRegistrationRepository.findByExamIdAndUserId()` is working

## Success Criteria

All tests should pass with:
- ✅ PRACTICE exams auto-published
- ✅ PRACTICE exams accept answers without registration
- ✅ PRACTICE exams accept results without registration
- ✅ VIRTUAL/RECRUITER exams still require registration
- ✅ All error cases handled properly
- ✅ No regression in existing functionality
