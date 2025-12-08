# Implementation Plan

- [x] 1. Add helper methods to ExamService




  - Add `requiresRegistration(String examType)` method to check if exam type requires registration
  - Add `validateRegistration(Long examId, Long userId)` method to validate user registration


  - Add `findByExamIdAndUserId(Long examId, Long userId)` query method to ExamRegistrationRepository
  - _Requirements: 4.1, 4.2, 4.3_



- [-] 1.1 Write property test for registration requirement logic

  - **Property 12: Exam type identification**


  - **Validates: Requirements 4.1**

- [ ] 2. Modify createExam method to auto-publish PRACTICE exams
  - Update `ExamService.createExam()` to check examType
  - Set status="PUBLISHED" for PRACTICE exams
  - Set status="DRAFT" for VIRTUAL and RECRUITER exams
  - _Requirements: 1.1, 1.5_

- [ ] 2.1 Write property test for PRACTICE exam auto-publish
  - **Property 1: PRACTICE exam auto-publish**
  - **Validates: Requirements 1.1, 5.3**

- [ ] 2.2 Write property test for non-PRACTICE exam status
  - **Property 5: Non-PRACTICE exam backward compatibility**
  - **Validates: Requirements 1.5**

- [ ] 2.3 Write property test for exam metadata persistence
  - **Property 2: PRACTICE exam metadata persistence**
  - **Validates: Requirements 1.2**



- [ ] 2.4 Write property test for creator tracking
  - **Property 3: PRACTICE exam creator tracking**
  - **Validates: Requirements 1.3**

- [ ] 2.5 Write property test for timestamp generation
  - **Property 4: PRACTICE exam timestamp generation**
  - **Validates: Requirements 1.4**

- [ ] 3. Modify submitAnswer method to conditionally validate registration
  - Update `ExamService.submitAnswer()` to fetch exam and check type
  - Call `validateRegistration()` only for non-PRACTICE exams
  - Ensure answer is saved with all required fields
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 3.1 Write property test for PRACTICE exam answer submission
  - **Property 6: PRACTICE exam answer submission without registration**
  - **Validates: Requirements 2.1, 4.2**

- [ ] 3.2 Write property test for non-PRACTICE exam answer validation
  - **Property 7: Non-PRACTICE exam answer submission requires registration**
  - **Validates: Requirements 2.2, 4.3**

- [ ] 3.3 Write property test for answer data persistence
  - **Property 8: Answer data persistence**
  - **Validates: Requirements 2.3**

- [ ] 4. Modify submitResult method to conditionally validate registration
  - Update `ExamService.submitResult()` to fetch exam and check type
  - Call `validateRegistration()` only for non-PRACTICE exams
  - Ensure result is saved with all required fields
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 4.1 Write property test for PRACTICE exam result submission
  - **Property 9: PRACTICE exam result submission without registration**
  - **Validates: Requirements 3.1, 4.2**

- [ ] 4.2 Write property test for non-PRACTICE exam result validation
  - **Property 10: Non-PRACTICE exam result submission requires registration**
  - **Validates: Requirements 3.2, 4.3**

- [ ] 4.3 Write property test for result data persistence
  - **Property 11: Result data persistence**
  - **Validates: Requirements 3.3**

- [ ] 5. Modify createExamWithRandomQuestions to auto-publish PRACTICE exams
  - Update `ExamService.createExamWithRandomQuestions()` to check examType
  - Set status="PUBLISHED" for PRACTICE exams
  - Set status="DRAFT" for other exam types
  - Ensure backward compatibility with existing behavior
  - _Requirements: 5.1, 5.2, 5.3_

- [ ] 5.1 Write property test for random question fetching
  - **Property 13: Random question fetching for PRACTICE exam**
  - **Validates: Requirements 5.1**

- [ ] 5.2 Write property test for question count
  - **Property 14: Random question selection and count**
  - **Validates: Requirements 5.2**

- [ ] 6. Write unit tests for error handling
  - Test invalid exam type validation
  - Test non-existent exam error
  - Test registration required error
  - Test invalid registration status error
  - Test no questions found error
  - _Requirements: 2.4, 3.4, 4.4, 5.4_

- [ ] 7. Write integration tests for end-to-end flows
  - Test complete PRACTICE exam flow (create → submit answers → submit result)
  - Test backward compatibility flow for VIRTUAL exam
  - Test random question generation flow
  - _Requirements: All_

- [ ] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
