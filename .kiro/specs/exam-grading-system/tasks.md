# Implementation Plan

## Overview
This implementation plan implements an exam grading system with automatic scoring, pass/fail determination, and detailed answer history tracking. The system uses exact answer matching and integrates with the Question Service to fetch correct answers.

## Current State Analysis
- ✅ Entities (UserAnswer, Result) already exist with all required fields
- ✅ Repositories exist with basic query methods  
- ✅ QuestionServiceClient exists with question fetching capability
- ✅ Basic DTOs (UserAnswerRequest/Response, ResultRequest/Response) exist
- ✅ Property-based testing framework (junit-quickcheck) already configured
- ✅ Task 3.1 and 3.2 completed (SubmitExamRequest, ExamGradingResponse DTOs created)
- ❌ No grading-specific history DTOs (ExamHistoryResponse, AnswerHistoryItem, etc.)
- ❌ No GradingService or GradingController
- ❌ No AnswerGrader utility
- ❌ Missing repository methods for grading queries
- ❌ No grading configuration properties

## Tasks

- [x] 1. Add jqwik dependency for property-based testing


  - Replace junit-quickcheck with jqwik in pom.xml
  - jqwik provides better Java integration and more powerful generators
  - Configure jqwik to run minimum 100 iterations per property test
  - _Requirements: Testing infrastructure_

- [x] 2. Enhance repositories with grading-specific queries


  - [x] 2.1 Add UserAnswerRepository query methods


    - Add findByExamIdAndUserIdOrderByCreatedAtAsc method
    - Add findByExamIdAndUserIdAndQuestionId method

    - _Requirements: 2.1, 2.3_

  - [x] 2.2 Add ResultRepository query methods


    - Add findTopByExamIdAndUserIdOrderByCompletedAtDesc method
    - Add findByExamId method returning List (not Page)
    - Add findByUserId method returning List (not Page)
    - _Requirements: 4.5_

  - [x] 2.3 Add ExamRegistrationRepository query method


    - Add existsByExamIdAndUserIdAndRegistrationStatus method
    - _Requirements: 6.2_

- [x] 3. Create remaining exam grading DTOs


  - [x] 3.1 Create ExamHistoryResponse and AnswerHistoryItem DTOs


    - Define ExamHistoryResponse with exam info and list of answers
    - Define AnswerHistoryItem with question content, answers, and metadata
    - Define QuestionMetadata DTO for field, topics, level, type
    - _Requirements: 2.1, 2.2, 7.1_

- [x] 4. Implement answer grading utility


  - [x] 4.1 Create AnswerGrader utility class


    - Implement isCorrect() method with case-insensitive exact matching
    - Implement answer normalization (lowercase, trim whitespace)
    - Implement calculateScore() method for percentage calculation
    - Implement determinePassStatus() method with configurable threshold
    - _Requirements: 3.1, 3.2, 3.3, 3.5_

  - [x] 4.2 Write property test for answer matching


    - **Property 9: Exact matching correctness**
    - **Validates: Requirements 3.1, 3.5**
    - Test that normalized strings match if and only if identical
    - Test various cases: uppercase, lowercase, whitespace variations

  - [x] 4.3 Write property test for score calculation

    - **Property 2: Score calculation accuracy**
    - **Validates: Requirements 1.3**
    - Test that score = (correct / total) * 100 for various combinations

  - [x] 4.4 Write property test for pass status determination

    - **Property 3: Pass status consistency**
    - **Validates: Requirements 1.4**
    - Test that passStatus is true if score >= threshold, false otherwise

- [x] 5. Enhance QuestionServiceClient


  - [x] 5.1 Add method to fetch question with correct answer


    - Implement getQuestionWithAnswer(Long questionId) method
    - Extract correct answer field from question response
    - Add error handling for question not found
    - Note: getQuestionById already exists, enhance if needed
    - _Requirements: 5.1, 5.2_

  - [x] 5.2 Implement retry logic with exponential backoff


    - Add retry mechanism (3 attempts: 1s, 2s, 4s delays)
    - Handle service unavailable scenarios
    - Log retry attempts for monitoring
    - Apply to getQuestionById and batch methods
    - _Requirements: 5.3, 5.4_

  - [x] 5.3 Add batch question fetching method


    - Implement getQuestionsWithAnswers(List<Long> questionIds)
    - Return Map<Long, QuestionDTO> for efficient lookup
    - Optimize for multiple question fetches
    - _Requirements: 5.1, 5.5_

  - [x] 5.4 Write property test for retry logic

    - **Property 13: Question data retrieval**
    - **Validates: Requirements 5.1, 5.2**
    - Test that questions are fetched before grading
    - Test retry behavior with simulated failures

- [-] 6. Implement GradingService core logic

  - [x] 6.1 Implement exam and registration validation


    - Validate exam exists and is in PUBLISHED/COMPLETED status
    - Validate user is registered with REGISTERED status
    - Validate all question IDs belong to the exam
    - Return detailed validation error messages
    - _Requirements: 6.1, 6.2, 6.3, 6.5, 7.1_

  - [x] 6.2 Implement submitAndGradeExam method


    - Validate submission using validation logic from 6.1
    - Fetch correct answers from Question Service
    - Grade each answer using AnswerGrader
    - Create and save UserAnswer entities for each answer
    - Calculate total score and determine pass/fail
    - Create and save Result entity
    - Build and return ExamGradingResponse
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3_

  - [ ] 6.3 Write property test for answer storage completeness
    - **Property 1: Answer storage completeness**
    - **Validates: Requirements 1.2**
    - Test that N submitted answers result in N UserAnswer records

  - [ ] 6.4 Write property test for result persistence
    - **Property 4: Result persistence**
    - **Validates: Requirements 1.5**
    - Test that completed grading creates Result record with all fields

  - [ ] 6.5 Write property test for validation logic
    - **Property 14: Exam validation**
    - **Property 15: Registration validation**
    - **Property 16: Question ID validation**
    - **Validates: Requirements 6.1, 6.2, 6.3**
    - Test that invalid submissions are rejected

  - [x] 6.6 Implement getExamHistory method


    - Retrieve all UserAnswer records for exam and user
    - Fetch question details from Question Service for each answer
    - Extract question content and metadata
    - Order answers by question order number
    - Build and return ExamHistoryResponse
    - Handle case when no answers exist (empty history)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3_

  - [ ] 6.7 Write property test for history completeness
    - **Property 5: History completeness**
    - **Validates: Requirements 2.1**
    - Test that all submitted answers appear in history

  - [ ] 6.8 Write property test for history ordering
    - **Property 7: History ordering**
    - **Validates: Requirements 2.3**
    - Test that history answers are ordered by question order number

  - [x] 6.9 Implement getExamResult method


    - Retrieve most recent Result for exam and user
    - Return score, pass status, completion time, feedback
    - Handle case when no result exists
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [ ] 6.10 Write property test for result retrieval
    - **Property 12: Result retrieval accuracy**
    - **Validates: Requirements 4.5**
    - Test that most recent result is returned when multiple exist

- [-] 7. Create GradingController REST endpoints

  - [x] 7.1 Implement POST /exams/{examId}/submit endpoint


    - Accept SubmitExamRequest in request body
    - Extract userId from X-User-Id header (set by gateway)
    - Call GradingService.submitAndGradeExam()
    - Return ExamGradingResponse
    - Add @PreAuthorize for USER and ADMIN roles
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 7.2 Implement GET /exams/{examId}/history endpoint

    - Accept userId as query parameter
    - Validate user can only access own history (unless ADMIN)
    - Call GradingService.getExamHistory()
    - Return ExamHistoryResponse
    - Add @PreAuthorize for USER and ADMIN roles
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 7.3 Implement GET /exams/{examId}/results/{userId} endpoint

    - Call GradingService.getExamResult()
    - Return ResultResponse (already exists)
    - Add @PreAuthorize for USER, ADMIN, RECRUITER roles
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [ ] 7.4 Write integration tests for grading endpoints
    - Test complete grading flow end-to-end
    - Test history retrieval with question details
    - Test result retrieval
    - Test error cases (invalid exam, not registered, etc.)

- [x] 8. Add database indexes for performance


  - Create index on user_answers(exam_id, user_id)
  - Create index on results(exam_id, user_id)
  - Create index on results(completed_at DESC)
  - _Requirements: Performance optimization_

- [x] 9. Add configuration properties


  - Add grading.pass-threshold property (default: 70.0)
  - Add grading.retry.max-attempts property (default: 3)
  - Add grading.retry.initial-delay property (default: 1000ms)
  - Add grading.retry.multiplier property (default: 2.0)
  - _Requirements: Configuration management_

- [x] 10. Checkpoint - Verify all tests pass


  - Run all property-based tests
  - Run all integration tests
  - Verify grading endpoints work correctly
  - Test complete flow: create exam → register → submit → grade → view history
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Create Postman collection for grading endpoints






  - Add POST /exams/{examId}/submit request with sample data
  - Add GET /exams/{examId}/history request
  - Add GET /exams/{examId}/results/{userId} request
  - Include authentication headers
  - Add test cases for error scenarios
  - _Requirements: API documentation and testing_
