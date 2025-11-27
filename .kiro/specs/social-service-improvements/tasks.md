# Implementation Plan

- [x] 1. Update database schema and entities


  - Add new columns to Comment entity (weightedVoteScore, editCount, updatedAt)
  - Add new columns to Vote entity (voteType, voteWeight)
  - Create database migration scripts
  - Update entity classes with new fields
  - _Requirements: 3.1, 3.3, 4.5, 5.1_







- [ ] 1.1 Write property test for vote weight persistence
  - **Property 14: Vote weight persistence**
  - **Validates: Requirements 4.5**

- [ ] 2. Implement User Service client
  - Create UserServiceClient interface


  - Implement REST client to fetch ELO ranks
  - Add circuit breaker for fault tolerance
  - Implement caching for ELO ranks (5 minute TTL)
  - Add timeout configuration (2 seconds)



  - _Requirements: 4.1, 6.1, 6.4_

- [ ] 2.1 Write unit tests for User Service client
  - Test successful ELO rank fetch
  - Test service unavailable scenario
  - Test timeout scenario






  - Test circuit breaker behavior
  - _Requirements: 4.1, 6.1, 6.4_

- [ ] 3. Implement vote weight calculator
  - Create VoteWeightCalculator service

  - Implement weight formula: weight = 1.0 + (eloRank - 1000) / 1000.0
  - Apply bounds: minimum 0.5, maximum 3.0
  - Handle null/invalid ELO ranks with default weight 1.0

  - _Requirements: 4.2, 4.4_



- [ ] 3.1 Write property test for vote weight calculation
  - **Property 3: Vote weight calculation**
  - **Validates: Requirements 4.2**

- [ ] 4. Implement comment limit validator
  - Create CommentLimitValidator service

  - Implement canComment method (check if user already commented on locked post)
  - Implement canEdit method (check edit count and ownership)
  - Add repository methods to count user comments per post

  - _Requirements: 2.4, 2.5, 3.2_

- [x] 4.1 Write property test for single comment per user on locked posts

  - **Property 6: Single comment per user on locked posts**
  - **Validates: Requirements 2.4, 2.5**


- [ ] 4.2 Write property test for edit limit enforcement
  - **Property 8: Edit limit enforcement**

  - **Validates: Requirements 3.2**

- [ ] 5. Update VoteService with weighted voting
  - Modify voteOnComment to accept voteType parameter
  - Fetch user ELO rank from UserServiceClient
  - Calculate vote weight using VoteWeightCalculator
  - Store voteType and voteWeight in Vote entity
  - Update comment's weightedVoteScore (add for USEFUL, subtract for NOT_USEFUL)
  - _Requirements: 5.1, 5.2, 5.3, 5.4_


- [ ] 5.1 Write property test for vote type acceptance
  - **Property 9: Vote type acceptance**

  - **Validates: Requirements 5.1**

- [x] 5.2 Write property test for useful vote score calculation

  - **Property 10: Useful vote score calculation**
  - **Validates: Requirements 5.2**

- [ ] 5.3 Write property test for not useful vote score calculation
  - **Property 11: Not useful vote score calculation**
  - **Validates: Requirements 5.3**

- [ ] 5.4 Write property test for duplicate vote prevention
  - **Property 12: Duplicate vote prevention**

  - **Validates: Requirements 5.4**

- [x] 6. Update CommentService with edit tracking and limits


  - Add updateComment method
  - Check edit limit using CommentLimitValidator
  - Increment editCount on each edit
  - Update updatedAt timestamp
  - Throw EditLimitExceededException if limit exceeded
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 6.1 Write property test for edit count tracking
  - **Property 7: Edit count tracking**
  - **Validates: Requirements 3.1, 3.3**


- [ ] 7. Update CommentService with comment limits for locked posts
  - Modify createComment to check if post is locked

  - Use CommentLimitValidator to check if user can comment
  - Throw CommentLimitExceededException if user already commented
  - _Requirements: 2.4, 2.5_

- [ ] 8. Update comment sorting logic
  - Modify getCommentsByPostId to check if post is locked
  - For locked posts: sort by weightedVoteScore DESC, then createdAt ASC

  - For normal posts: sort by createdAt ASC
  - Update repository methods with correct sorting
  - _Requirements: 1.2, 2.3_

- [ ] 8.1 Write property test for normal post comment sorting
  - **Property 1: Normal post comment sorting**
  - **Validates: Requirements 1.2**


- [ ] 8.2 Write property test for locked post comment sorting
  - **Property 5: Locked post comment sorting**
  - **Validates: Requirements 2.3**

- [x] 9. Implement vote percentage calculation

  - Add getVotePercentage method to Comment entity
  - Calculate: min(100, max(0, weightedVoteScore))

  - Update CommentResponse DTO to include votePercentage field
  - Update CommentMapper to include votePercentage in response
  - _Requirements: 5.5, 8.1, 8.4_

- [ ] 9.1 Write property test for vote percentage normalization
  - **Property 13: Vote percentage normalization**


  - **Validates: Requirements 5.5, 8.1**

- [ ] 9.2 Write property test for comment response includes vote percentage
  - **Property 15: Comment response includes vote percentage**
  - **Validates: Requirements 8.4**


- [ ] 10. Add new exception classes
  - Create CommentLimitExceededException
  - Create EditLimitExceededException
  - Create InvalidVoteTypeException
  - Update GlobalExceptionHandler to handle new exceptions
  - Return appropriate HTTP status codes (409, 400)

  - _Requirements: 7.2, 7.3, 7.4_

- [ ] 11. Update API controllers
  - Add voteType parameter to vote endpoint
  - Add updateComment endpoint to CommentController
  - Update API documentation (Swagger annotations)


  - Add validation for voteType enum
  - _Requirements: 5.1, 3.3_

- [ ] 12. Update DTOs
  - Add voteType field to VoteRequest
  - Add voteWeight field to VoteResponse
  - Add votePercentage field to CommentResponse
  - Add editCount and updatedAt fields to CommentResponse
  - Create CommentUpdateRequest DTO
  - _Requirements: 3.4, 5.1, 8.4_

- [ ] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Write integration tests
  - Test end-to-end comment creation on normal posts
  - Test end-to-end comment creation on locked posts with limits
  - Test end-to-end voting with weighted scores
  - Test edit functionality with limits
  - Test User Service integration with fallback
  - _Requirements: All_

- [ ] 15. Create database migration scripts
  - Write Flyway/Liquibase migration for schema changes
  - Write data backfill script for existing records
  - Test migration on development database
  - _Requirements: All_

- [ ] 16. Add monitoring and logging
  - Add metrics for vote weight distribution
  - Add metrics for User Service call success rate
  - Add logging for User Service failures
  - Add logging for comment/edit limit violations
  - Add alerts for abnormal patterns
  - _Requirements: 6.1_

- [ ] 17. Update API documentation
  - Update Swagger/OpenAPI specification
  - Add examples for new vote types
  - Document error responses
  - Add migration guide for API consumers
  - _Requirements: All_

- [ ] 18. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
