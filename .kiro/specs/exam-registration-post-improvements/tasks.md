# Implementation Plan

- [ ] 1. Fix exam registration examId mapping
  - [x] 1.1 Update ExamRegistrationService to extract examId from exam relationship



    - Modify the `mapToResponse` method to safely extract examId from `registration.getExam().getId()`
    - Handle null exam references gracefully


    - _Requirements: 1.1, 1.2, 1.4_



  - [ ] 1.2 Write property test for registration examId presence
    - **Property 1: Registration responses include examId**
    - **Validates: Requirements 1.1, 1.2, 1.4**



  - [ ] 1.3 Write property test for registration persistence
    - **Property 2: Registration persistence maintains exam relationship**

    - **Validates: Requirements 1.3**

- [ ] 2. Fix exam registration bidirectional relationship
  - [x] 2.1 Update ExamRegistrationService to ensure bidirectional relationship


    - Verify that creating a registration properly sets the exam reference
    - Ensure the exam's registration collection is updated


    - _Requirements: 2.1, 2.3_

  - [ ] 2.2 Update repository query methods for fetching registrations by exam
    - Implement or verify `findByExamId` method in ExamRegistrationRepository
    - Use appropriate fetch strategy to avoid N+1 queries
    - _Requirements: 2.2_



  - [ ] 2.3 Write property test for bidirectional relationship
    - **Property 3: Registration creates bidirectional relationship**
    - **Validates: Requirements 2.1, 2.3**



  - [ ] 2.4 Write property test for exam query completeness
    - **Property 4: Exam query returns all registrations**
    - **Validates: Requirements 2.2**



- [ ] 3. Checkpoint - Verify exam registration fixes
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Extend Post entity with new fields


  - [ ] 4.1 Create database migration script for posts table
    - Add columns: field_id, topic_id, level_id, post_type, status
    - Add indexes for filtering: status, user_id+status, field_id, topic_id
    - Set default values for existing records


    - _Requirements: 3.1, 4.1, 5.1_



  - [ ] 4.2 Update Post entity class
    - Add fields: fieldId, topicId, levelId, postType, status
    - Add validation annotations


    - Update @PrePersist to set default status based on context
    - _Requirements: 3.1, 3.2_

- [x] 5. Update Post DTOs


  - [ ] 5.1 Update PostRequest DTO
    - Add fields: fieldId, topicId, levelId, postType
    - Add validation for required fields
    - Remove userId from request (extract from auth context)
    - _Requirements: 3.1_




  - [ ] 5.2 Update PostResponse DTO
    - Add fields: fieldId, topicId, levelId, postType, status
    - Change timestamp fields to String type for ISO 8601 format
    - Update mapper to format timestamps correctly
    - _Requirements: 3.1, 3.4, 3.5_

  - [ ] 5.3 Write property test for post response completeness
    - **Property 5: Post response contains all required fields**
    - **Validates: Requirements 3.1, 3.2**

  - [ ] 5.4 Write property test for ISO 8601 timestamp formatting
    - **Property 6: Timestamps are ISO 8601 formatted**
    - **Validates: Requirements 3.4, 3.5**

- [ ] 6. Implement role-based post creation
  - [ ] 6.1 Create utility to extract user role from authentication context
    - Implement method to read JWT token claims
    - Extract role/authorities from token
    - Handle missing or invalid tokens
    - _Requirements: 4.3_

  - [ ] 6.2 Update PostService.createPost to determine status by role
    - Accept user role as parameter
    - Set status to PUBLISHED for admin role
    - Set status to DRAFT for regular user role
    - Extract userId from authentication context
    - _Requirements: 4.1, 5.1_

  - [ ] 6.3 Update PostController.createPost endpoint
    - Extract user role from authentication
    - Pass role to service layer
    - Return appropriate HTTP status codes
    - _Requirements: 4.1, 5.1_

  - [ ] 6.4 Write property test for admin post status
    - **Property 7: Admin posts are published immediately**
    - **Validates: Requirements 4.1, 4.4**

  - [ ] 6.5 Write property test for regular user post status
    - **Property 9: Regular user posts are drafts**
    - **Validates: Requirements 5.1**

- [ ] 7. Implement post visibility filtering
  - [ ] 7.1 Update PostService.getPosts with role-based filtering
    - If admin: return all posts
    - If regular user: return PUBLISHED posts + own DRAFT posts
    - Add parameters for userId and role
    - _Requirements: 5.2, 5.3, 5.5_

  - [ ] 7.2 Update PostRepository with custom query methods
    - Add `findByStatus` method
    - Add `findByStatusOrUserId` method for user-specific filtering
    - Optimize queries with proper indexing
    - _Requirements: 5.2, 5.3_

  - [ ] 7.3 Update PostController.getPosts endpoint
    - Extract user role and userId from authentication
    - Pass to service layer for filtering
    - _Requirements: 4.2, 5.2, 5.3_

  - [ ] 7.4 Write property test for admin post visibility
    - **Property 8: Admin posts are visible to all users**
    - **Validates: Requirements 4.2**

  - [ ] 7.5 Write property test for draft post exclusion
    - **Property 10: Draft posts are excluded from public listings**
    - **Validates: Requirements 5.2**

  - [ ] 7.6 Write property test for draft post visibility to creator and admins
    - **Property 11: Draft posts are visible to creator and admins**
    - **Validates: Requirements 5.3**

- [ ] 8. Implement post approval workflow
  - [ ] 8.1 Create PostService.approvePost method
    - Verify user is admin
    - Verify post exists and is in DRAFT status
    - Update status to PUBLISHED
    - Return updated post
    - _Requirements: 5.4_

  - [ ] 8.2 Create PostController.approvePost endpoint
    - Add PUT /api/posts/{id}/approve endpoint
    - Implement authorization check
    - Handle error cases (not found, invalid status, unauthorized)
    - _Requirements: 5.4_

  - [ ] 8.3 Write property test for approval workflow
    - **Property 12: Approval changes draft to published**
    - **Validates: Requirements 5.4**

  - [ ] 8.4 Write unit tests for approval error cases
    - Test unauthorized access (non-admin)
    - Test invalid status transition (non-draft post)
    - Test post not found
    - _Requirements: 5.4_

- [ ] 9. Add error handling and validation
  - [ ] 9.1 Implement field/topic/level ID validation
    - Add validation in PostService to check IDs exist
    - Return appropriate error messages
    - _Requirements: 3.1_

  - [ ] 9.2 Add access control for viewing individual posts
    - Update PostService.getPostById with access checks
    - Verify user can access draft posts (creator or admin only)
    - _Requirements: 5.3_

  - [ ] 9.3 Implement exception handlers
    - Handle DataIntegrityException for exam service
    - Handle AccessDeniedException for social service
    - Handle InvalidStatusTransitionException
    - Return appropriate HTTP status codes and messages

- [ ] 10. Final checkpoint - Verify all functionality
  - Ensure all tests pass, ask the user if questions arise.
