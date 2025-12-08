# Design Document

## Overview

Tính năng PRACTICE exam type mở rộng hệ thống exam-service hiện tại để hỗ trợ một loại exam mới dành cho việc tự luyện tập. Khác với VIRTUAL và RECRUITER exam yêu cầu đăng ký và phê duyệt, PRACTICE exam cho phép user tạo và làm bài ngay lập tức mà không cần qua bất kỳ quy trình phê duyệt nào.

Thiết kế này tập trung vào việc:
- Tự động publish PRACTICE exam khi tạo
- Bỏ qua validation đăng ký khi submit answer/result cho PRACTICE exam
- Duy trì backward compatibility với VIRTUAL và RECRUITER exam
- Tái sử dụng tối đa code hiện có

## Architecture

### High-Level Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│      ExamController                 │
│  - createExam()                     │
│  - createExamWithRandomQuestions()  │
│  - submitAnswer()                   │
│  - submitResult()                   │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│      ExamService                    │
│  + createExam()                     │
│  + createExamWithRandomQuestions()  │
│  + submitAnswer()                   │
│  + submitResult()                   │
│  - requiresRegistration()  [NEW]   │
│  - validateRegistration()  [NEW]   │
└──────┬──────────────────────────────┘
       │
       ├──────────────┬─────────────────┐
       ▼              ▼                 ▼
┌─────────────┐ ┌──────────────┐ ┌────────────────┐
│ExamRepo     │ │UserAnswerRepo│ │ResultRepo      │
└─────────────┘ └──────────────┘ └────────────────┘
```

### Component Interaction Flow

**Flow 1: Tạo PRACTICE Exam**
```
Client → ExamController.createExam(examType="PRACTICE")
       → ExamService.createExam()
       → [Check examType]
       → [If PRACTICE: set status="PUBLISHED"]
       → [If VIRTUAL/RECRUITER: set status="DRAFT"]
       → ExamRepository.save()
       → Return ExamResponse
```

**Flow 2: Submit Answer cho PRACTICE Exam**
```
Client → ExamController.submitAnswer(examId, userId, answer)
       → ExamService.submitAnswer()
       → ExamRepository.findById(examId)
       → [Check exam.examType]
       → [If PRACTICE: skip registration check]
       → [If VIRTUAL/RECRUITER: validate registration]
       → UserAnswerRepository.save()
       → Return UserAnswerResponse
```

## Components and Interfaces

### 1. ExamService (Modified)

**New Helper Methods:**

```java
/**
 * Kiểm tra xem exam type có yêu cầu registration hay không
 * @param examType loại exam (PRACTICE, VIRTUAL, RECRUITER)
 * @return true nếu yêu cầu registration, false nếu không
 */
private boolean requiresRegistration(String examType) {
    return !"PRACTICE".equalsIgnoreCase(examType);
}

/**
 * Validate registration cho exam nếu cần thiết
 * @param examId ID của exam
 * @param userId ID của user
 * @throws RuntimeException nếu registration không hợp lệ
 */
private void validateRegistration(Long examId, Long userId) {
    if (!examRegistrationRepository.existsByExamIdAndUserId(examId, userId)) {
        throw new RuntimeException("User must register for this exam before submitting");
    }
    
    // Kiểm tra registration status
    ExamRegistration registration = examRegistrationRepository
        .findByExamIdAndUserId(examId, userId)
        .orElseThrow(() -> new RuntimeException("Registration not found"));
    
    if (!"REGISTERED".equals(registration.getRegistrationStatus())) {
        throw new RuntimeException("Registration is not active");
    }
}
```

**Modified Methods:**

```java
@Transactional
public ExamResponse createExam(ExamRequest req) {
    Exam exam = mappers.toEntity(req);
    
    // Set status based on exam type
    if ("PRACTICE".equalsIgnoreCase(req.getExamType())) {
        exam.setStatus("PUBLISHED");
    } else {
        exam.setStatus("DRAFT");
    }
    
    exam.setCreatedAt(LocalDateTime.now());
    exam.setCreatedBy(req.getUserId());
    return mappers.toResponse(examRepository.save(exam));
}

@Transactional
public CreateExamWithQuestionsResponse createExamWithRandomQuestions(
        CreateExamWithQuestionsRequest req, Long userId) {
    // ... existing question fetching logic ...
    
    // Create exam
    Exam exam = new Exam();
    // ... set other fields ...
    exam.setExamType(req.getExamType() != null ? req.getExamType() : "PRACTICE");
    
    // Auto-publish PRACTICE exams
    if ("PRACTICE".equalsIgnoreCase(exam.getExamType())) {
        exam.setStatus("PUBLISHED");
    } else {
        exam.setStatus("DRAFT");
    }
    
    // ... rest of the method ...
}

public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    // Fetch exam to check type
    Exam exam = examRepository.findById(req.getExamId())
        .orElseThrow(() -> new RuntimeException("Exam not found"));
    
    // Validate registration only for non-PRACTICE exams
    if (requiresRegistration(exam.getExamType())) {
        validateRegistration(req.getExamId(), req.getUserId());
    }
    
    // Save answer
    UserAnswer answer = mappers.toEntity(req);
    answer.setExam(exam);
    answer.setCreatedAt(LocalDateTime.now());
    return mappers.toResponse(userAnswerRepository.save(answer));
}

public ResultResponse submitResult(ResultRequest req) {
    // Fetch exam to check type
    Exam exam = examRepository.findById(req.getExamId())
        .orElseThrow(() -> new RuntimeException("Exam not found"));
    
    // Validate registration only for non-PRACTICE exams
    if (requiresRegistration(exam.getExamType())) {
        validateRegistration(req.getExamId(), req.getUserId());
    }
    
    // Save result
    Result result = mappers.toEntity(req);
    result.setExam(exam);
    result.setCompletedAt(LocalDateTime.now());
    return mappers.toResponse(resultRepository.save(result));
}
```

### 2. ExamRegistrationRepository (New Query Method)

```java
/**
 * Tìm registration theo examId và userId
 * @param examId ID của exam
 * @param userId ID của user
 * @return Optional chứa ExamRegistration nếu tìm thấy
 */
Optional<ExamRegistration> findByExamIdAndUserId(Long examId, Long userId);
```

## Data Models

### Exam Entity (No Changes Required)

Entity hiện tại đã hỗ trợ đầy đủ:
- `examType`: String field có thể chứa "PRACTICE", "VIRTUAL", "RECRUITER"
- `status`: String field có thể chứa "DRAFT", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED"

### ExamRequest DTO (No Changes Required)

DTO hiện tại đã hỗ trợ `examType` field với validation `@NotBlank`.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property Reflection

Sau khi phân tích prework, tôi nhận thấy một số properties bị trùng lặp:
- Property 4.2 trùng với 2.1 và 3.1 (PRACTICE exams skip registration)
- Property 4.3 trùng với 2.2 và 3.2 (VIRTUAL/RECRUITER exams require registration)
- Property 5.3 trùng với 1.1 (PRACTICE exams auto-published)

Các properties này sẽ được gộp lại để tránh redundancy.

### Correctness Properties

**Property 1: PRACTICE exam auto-publish**
*For any* exam creation request with examType="PRACTICE", the created exam SHALL have status="PUBLISHED"
**Validates: Requirements 1.1, 5.3**

**Property 2: PRACTICE exam metadata persistence**
*For any* PRACTICE exam creation request, all metadata fields (title, position, fieldId, topicIds, levelId, questionTypeIds, questionCount, duration, language) SHALL be stored and retrievable
**Validates: Requirements 1.2**

**Property 3: PRACTICE exam creator tracking**
*For any* PRACTICE exam creation request with userId, the created exam SHALL have createdBy field equal to that userId
**Validates: Requirements 1.3**

**Property 4: PRACTICE exam timestamp generation**
*For any* PRACTICE exam creation, the createdAt field SHALL be set to a timestamp within 1 second of the creation time
**Validates: Requirements 1.4**

**Property 5: Non-PRACTICE exam backward compatibility**
*For any* exam creation request with examType="VIRTUAL" or "RECRUITER", the created exam SHALL have status="DRAFT"
**Validates: Requirements 1.5**

**Property 6: PRACTICE exam answer submission without registration**
*For any* PRACTICE exam and any user, submitting an answer SHALL succeed without requiring a registration record
**Validates: Requirements 2.1, 4.2**

**Property 7: Non-PRACTICE exam answer submission requires registration**
*For any* VIRTUAL or RECRUITER exam and any user without valid registration, submitting an answer SHALL fail with an error
**Validates: Requirements 2.2, 4.3**

**Property 8: Answer data persistence**
*For any* successful answer submission, all required fields (examId, userId, questionId, answerContent, createdAt) SHALL be stored and retrievable
**Validates: Requirements 2.3**

**Property 9: PRACTICE exam result submission without registration**
*For any* PRACTICE exam and any user, submitting a result SHALL succeed without requiring a registration record
**Validates: Requirements 3.1, 4.2**

**Property 10: Non-PRACTICE exam result submission requires registration**
*For any* VIRTUAL or RECRUITER exam and any user without valid registration, submitting a result SHALL fail with an error
**Validates: Requirements 3.2, 4.3**

**Property 11: Result data persistence**
*For any* successful result submission, all required fields (examId, userId, score, completedAt) SHALL be stored and retrievable
**Validates: Requirements 3.3**

**Property 12: Exam type identification**
*For any* exam operation, the system SHALL correctly identify the examType from the exam record
**Validates: Requirements 4.1**

**Property 13: Random question fetching for PRACTICE exam**
*For any* PRACTICE exam creation with random questions, the system SHALL fetch questions from Question Service using the specified fieldId, topicIds, levelId, and questionTypeId
**Validates: Requirements 5.1**

**Property 14: Random question selection and count**
*For any* PRACTICE exam creation with random questions requesting N questions, the created exam SHALL contain exactly N questions (or fewer if insufficient questions available)
**Validates: Requirements 5.2**

## Error Handling

### Error Scenarios

1. **Invalid Exam Type**
   - Condition: examType is null, empty, or not one of the valid values
   - Response: HTTP 400 Bad Request with validation error message
   - Example: "Exam type must be one of: PRACTICE, VIRTUAL, RECRUITER"

2. **Non-existent Exam**
   - Condition: Attempting to submit answer/result for an exam that doesn't exist
   - Response: HTTP 404 Not Found
   - Example: "Exam not found with id: 123"

3. **Registration Required**
   - Condition: Attempting to submit answer/result for VIRTUAL/RECRUITER exam without registration
   - Response: HTTP 403 Forbidden
   - Example: "User must register for this exam before submitting"

4. **Invalid Registration Status**
   - Condition: User has registration but status is CANCELLED
   - Response: HTTP 403 Forbidden
   - Example: "Registration is not active"

5. **No Questions Found**
   - Condition: Creating PRACTICE exam with random questions but Question Service returns no matches
   - Response: HTTP 404 Not Found
   - Example: "No questions found matching the criteria"

6. **Question Service Unavailable**
   - Condition: Question Service is down or unreachable
   - Response: HTTP 503 Service Unavailable
   - Example: "Question Service is temporarily unavailable"

### Error Handling Strategy

```java
// Example error handling in submitAnswer
public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    try {
        Exam exam = examRepository.findById(req.getExamId())
            .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + req.getExamId()));
        
        if (requiresRegistration(exam.getExamType())) {
            try {
                validateRegistration(req.getExamId(), req.getUserId());
            } catch (RuntimeException e) {
                throw new RegistrationRequiredException(e.getMessage());
            }
        }
        
        // ... save answer ...
    } catch (ExamNotFoundException e) {
        throw e; // HTTP 404
    } catch (RegistrationRequiredException e) {
        throw e; // HTTP 403
    } catch (Exception e) {
        log.error("Unexpected error submitting answer", e);
        throw new InternalServerException("Failed to submit answer");
    }
}
```

## Testing Strategy

### Unit Testing

Unit tests sẽ tập trung vào các trường hợp cụ thể và edge cases:

1. **Exam Creation Tests**
   - Test tạo PRACTICE exam với status="PUBLISHED"
   - Test tạo VIRTUAL exam với status="DRAFT"
   - Test tạo RECRUITER exam với status="DRAFT"
   - Test validation khi examType null hoặc invalid

2. **Answer Submission Tests**
   - Test submit answer cho PRACTICE exam không cần registration
   - Test submit answer cho VIRTUAL exam yêu cầu registration
   - Test submit answer với exam không tồn tại
   - Test submit answer với registration bị cancelled

3. **Result Submission Tests**
   - Test submit result cho PRACTICE exam không cần registration
   - Test submit result cho VIRTUAL exam yêu cầu registration
   - Test submit result với exam không tồn tại

4. **Helper Method Tests**
   - Test requiresRegistration() với các exam types khác nhau
   - Test validateRegistration() với các trạng thái registration khác nhau

### Property-Based Testing

Property-based tests sẽ sử dụng **jqwik** framework (đã có trong social-service) để verify các correctness properties trên nhiều inputs ngẫu nhiên. Mỗi property test sẽ chạy tối thiểu 100 iterations.

**Test Configuration:**
```java
@Property(tries = 100)
```

**Property Test Examples:**

1. **Property 1 Test: PRACTICE exam auto-publish**
```java
@Property
void practiceExamShouldBeAutoPublished(
    @ForAll @StringLength(min = 1, max = 200) String title,
    @ForAll @IntRange(min = 1, max = 100) int questionCount,
    @ForAll @IntRange(min = 1, max = 300) int duration) {
    
    ExamRequest request = createExamRequest("PRACTICE", title, questionCount, duration);
    ExamResponse response = examService.createExam(request);
    
    assertThat(response.getStatus()).isEqualTo("PUBLISHED");
}
```

2. **Property 6 Test: PRACTICE exam answer submission without registration**
```java
@Property
void practiceExamAnswerSubmissionShouldNotRequireRegistration(
    @ForAll @Positive long examId,
    @ForAll @Positive long userId,
    @ForAll @Positive long questionId,
    @ForAll String answerContent) {
    
    // Setup: Create PRACTICE exam
    Exam exam = createPracticeExam(examId);
    
    // Execute: Submit answer without registration
    UserAnswerRequest request = new UserAnswerRequest();
    request.setExamId(examId);
    request.setUserId(userId);
    request.setQuestionId(questionId);
    request.setAnswerContent(answerContent);
    
    // Verify: Should succeed
    assertDoesNotThrow(() -> examService.submitAnswer(request));
}
```

3. **Property 7 Test: Non-PRACTICE exam requires registration**
```java
@Property
void virtualExamAnswerSubmissionShouldRequireRegistration(
    @ForAll("examTypes") String examType,
    @ForAll @Positive long examId,
    @ForAll @Positive long userId) {
    
    Assume.that(!examType.equals("PRACTICE"));
    
    // Setup: Create non-PRACTICE exam without registration
    Exam exam = createExam(examId, examType);
    
    // Execute & Verify: Should throw exception
    UserAnswerRequest request = new UserAnswerRequest();
    request.setExamId(examId);
    request.setUserId(userId);
    
    assertThrows(RuntimeException.class, () -> examService.submitAnswer(request));
}

@Provide
Arbitrary<String> examTypes() {
    return Arbitraries.of("VIRTUAL", "RECRUITER");
}
```

### Integration Testing

Integration tests sẽ verify toàn bộ flow từ controller đến database:

1. **End-to-End PRACTICE Exam Flow**
   - Tạo PRACTICE exam qua API
   - Verify status="PUBLISHED"
   - Submit answers qua API (không đăng ký)
   - Submit result qua API (không đăng ký)
   - Verify tất cả data được lưu đúng

2. **Backward Compatibility Flow**
   - Tạo VIRTUAL exam qua API
   - Verify status="DRAFT"
   - Thử submit answer không đăng ký → expect 403
   - Đăng ký exam
   - Submit answer → expect success

3. **Random Question Generation Flow**
   - Tạo PRACTICE exam với random questions
   - Verify exam được tạo với status="PUBLISHED"
   - Verify questions được add vào exam
   - Verify question count đúng

## Implementation Notes

### Backward Compatibility

Thiết kế này đảm bảo backward compatibility hoàn toàn:
- Không thay đổi database schema
- Không thay đổi API contracts
- VIRTUAL và RECRUITER exams hoạt động như cũ
- Chỉ thêm logic mới cho PRACTICE exam type

### Performance Considerations

1. **Registration Check Optimization**
   - Sử dụng `existsByExamIdAndUserId()` thay vì `findByExamIdAndUserId()` khi chỉ cần check existence
   - Cache exam type trong memory nếu cần thiết

2. **Database Queries**
   - Không có thêm N+1 query issues
   - Sử dụng lazy loading cho exam relationships

### Security Considerations

1. **Authorization**
   - User chỉ có thể tạo PRACTICE exam cho chính mình
   - User chỉ có thể submit answer/result cho exam mà mình có quyền

2. **Validation**
   - Validate examType ở DTO level với `@NotBlank`
   - Validate exam existence trước khi submit
   - Validate registration cho non-PRACTICE exams

### Monitoring and Logging

```java
// Log exam creation with type
log.info("Creating exam: type={}, userId={}, title={}", 
    req.getExamType(), req.getUserId(), req.getTitle());

// Log registration validation skip
log.debug("Skipping registration validation for PRACTICE exam: examId={}", examId);

// Log registration validation enforcement
log.debug("Validating registration for {} exam: examId={}, userId={}", 
    examType, examId, userId);
```

## Migration Plan

### Phase 1: Code Changes
1. Modify `ExamService.createExam()` to auto-publish PRACTICE exams
2. Add `requiresRegistration()` helper method
3. Add `validateRegistration()` helper method
4. Modify `submitAnswer()` to conditionally validate registration
5. Modify `submitResult()` to conditionally validate registration
6. Modify `createExamWithRandomQuestions()` to auto-publish PRACTICE exams

### Phase 2: Testing
1. Write unit tests for new logic
2. Write property-based tests for correctness properties
3. Write integration tests for end-to-end flows
4. Verify backward compatibility with existing tests

### Phase 3: Deployment
1. Deploy to staging environment
2. Run smoke tests
3. Monitor logs for errors
4. Deploy to production
5. Monitor metrics (exam creation rate, answer submission rate)

### Rollback Plan

Nếu có issues:
1. Revert code changes
2. PRACTICE exams đã tạo sẽ vẫn tồn tại với status="PUBLISHED"
3. Có thể manually update status về "DRAFT" nếu cần
4. Không có data loss vì không thay đổi schema
