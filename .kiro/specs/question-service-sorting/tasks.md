# Implementation Plan

- [ ] 1. Update getAllLevels method to add default sorting



  - Modify `getAllLevels()` in QuestionService to check if pageable is unsorted
  - If unsorted, create new PageRequest with Sort.by("id").ascending()
  - Follow the same pattern as getAllFields() and getAllTopics()
  - _Requirements: 1.3, 2.3_





- [ ] 1.1 Write property test for Level sorting
  - **Property 1: Default sorting applies when no sort specified**
  - **Validates: Requirements 1.3**

- [x] 2. Update getAllQuestionTypes method to add default sorting


  - Modify `getAllQuestionTypes()` in QuestionService to check if pageable is unsorted


  - If unsorted, create new PageRequest with Sort.by("id").ascending()
  - Follow the same pattern as getAllFields() and getAllTopics()
  - _Requirements: 1.4, 2.4_

- [x] 2.1 Write property test for QuestionType sorting


  - **Property 1: Default sorting applies when no sort specified**


  - **Validates: Requirements 1.4**

- [ ] 3. Update getAllQuestions method to add default sorting
  - Modify `getAllQuestions()` in QuestionService to check if pageable is unsorted
  - If unsorted, create new PageRequest with Sort.by("id").ascending()


  - Follow the same pattern as getAllFields() and getAllTopics()


  - _Requirements: 1.5, 2.5_

- [ ] 3.1 Write property test for Question sorting
  - **Property 1: Default sorting applies when no sort specified**
  - **Validates: Requirements 1.5**

- [ ] 4. Update getAllAnswers method to add default sorting
  - Modify `getAllAnswers()` in QuestionService to check if pageable is unsorted
  - If unsorted, create new PageRequest with Sort.by("id").ascending()
  - Follow the same pattern as getAllFields() and getAllTopics()
  - _Requirements: 1.6, 2.6_

- [ ] 4.1 Write property test for Answer sorting
  - **Property 1: Default sorting applies when no sort specified**
  - **Validates: Requirements 1.6**

- [ ] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Write property test for explicit sorting preservation
  - **Property 2: Explicit sorting is preserved**
  - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**

- [ ] 7. Write property test for element preservation
  - **Property 3: Sorting preserves all elements**
  - **Validates: Requirements 3.1, 3.2**

- [ ] 8. Write property test for ordering consistency
  - **Property 4: ID ordering is consistent**
  - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**

- [x] 9. Write unit tests for edge cases

  - Test empty result set with sorting
  - Test single element with sorting
  - Test multiple elements in ascending ID order
  - Test explicit sort parameter overrides default
  - Test pagination with sorting across multiple pages
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [ ] 10. Write integration tests for all endpoints
  - Test GET /fields returns sorted by ID
  - Test GET /topics returns sorted by ID
  - Test GET /levels returns sorted by ID
  - Test GET /question-types returns sorted by ID
  - Test GET /questions returns sorted by ID
  - Test GET /answers returns sorted by ID
  - Test pagination with sorting
  - Test custom sort parameter overrides default
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
