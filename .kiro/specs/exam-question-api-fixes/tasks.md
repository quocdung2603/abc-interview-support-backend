# Implementation Plan

- [x] 1. Remove deprecated fields from ExamResponse DTO



  - Remove `topics` field declaration from ExamResponse.java
  - Remove `questionTypes` field declaration from ExamResponse.java
  - Verify that only `topicIds` and `questionTypeIds` remain
  - _Requirements: 1.3, 1.4, 2.1, 2.2_





- [ ] 1.1 Write property test for response field exclusion
  - **Property 2: Response excludes deprecated fields**
  - **Validates: Requirements 1.3, 1.4**

- [ ] 2. Remove deprecated fields from ExamRequest DTO
  - Remove `topics` field declaration from ExamRequest.java


  - Remove `questionTypes` field declaration from ExamRequest.java

  - Remove validation annotations from deprecated fields
  - Update validation to ensure `topicIds` and `questionTypeIds` are required
  - _Requirements: 2.5_

- [ ] 2.1 Write property test for request deserialization
  - **Property 3: Request deserialization accepts new fields**
  - **Validates: Requirements 2.5**



- [ ] 3. Update Mappers to remove deprecated field mappings
  - Remove `@Mapping` annotations for `topics` and `questionTypes` in `toEntity` method


  - Remove `@Mapping` annotations for `topics` and `questionTypes` in `toResponse` method


  - Remove `getTopicIds()` fallback method that reads from deprecated `topics` field
  - Remove `getQuestionTypeIds()` fallback method that reads from deprecated `questionTypes` field
  - Simplify mapping to only use `topicIds` and `questionTypeIds`
  - _Requirements: 1.1, 1.2, 3.1_


- [ ] 3.1 Write property test for response field presence
  - **Property 1: Response contains required fields**
  - **Validates: Requirements 1.1, 1.2**


- [ ] 3.2 Write property test for entity to response mapping
  - **Property 4: Entity to response mapping preserves data**

  - **Validates: Requirements 3.1**

- [ ] 4. Remove deprecated fields from Exam entity
  - Remove `topics` field declaration from Exam.java
  - Remove `questionTypes` field declaration from Exam.java
  - Remove getter and setter methods for deprecated fields
  - _Requirements: 2.3, 2.4_



- [x] 5. Update ExamService to remove deprecated field handling

  - Remove code in `updateExam()` that sets deprecated `topics` and `questionTypes` fields
  - Verify that only `topicIds` and `questionTypeIds` are used throughout the service
  - _Requirements: 3.3_

- [ ] 5.1 Write property test for new exam creation
  - **Property 5: New exam creation uses correct fields**
  - **Validates: Requirements 3.3**



- [ ] 6. Write unit tests for edge cases
  - Test empty topicIds list handling
  - Test empty questionTypeIds list handling
  - Test null values in entity fields
  - Test malformed JSON in entity fields
  - Test single item lists
  - Test large lists (50+ items)
  - _Requirements: 1.1, 1.2, 3.1_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Update integration tests
  - Update existing integration tests to use only new field names
  - Verify POST /exams with topicIds/questionTypeIds works
  - Verify GET /exams returns only new fields
  - Verify PUT /exams with new fields works
  - Verify GET /exams (list) returns all exams with new format
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
