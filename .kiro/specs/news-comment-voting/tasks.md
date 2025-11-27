# Implementation Plan

- [ ] 1. Create social-service project structure
  - [ ] 1.1 Create social-service directory and Spring Boot project
    - Copy structure from existing service (exam-service or news-service)
    - Update pom.xml with groupId: com.abc, artifactId: social-service
    - Add dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, postgresql, lombok, mapstruct, eureka-client, validation, oauth2-resource-server, springdoc-openapi
    - _Requirements: 5.1, 5.2_
  
  - [ ] 1.2 Create application.yml configuration
    - Configure server port (8090)
    - Configure PostgreSQL datasource (socialdb)
    - Configure Eureka client registration
    - Configure JWT secret
    - Configure Swagger/OpenAPI
    - Configure JPA (ddl-auto: update, show-sql: true)
    - _Requirements: 5.1, 5.2_
  
  - [ ] 1.3 Create main application class
    - Create SocialServiceApplication.java with @SpringBootApplication
    - Add @EnableDiscoveryClient for Eureka
    - _Requirements: 5.1, 5.2_
  
  - [ ] 1.4 Create package structure
    - Create packages: entity, dto, repository, service, controller, exception, mapper, config
    - _Requirements: 5.1_

- [ ] 2. Set up database schema and entities
  - [ ] 2.1 Create Post entity
    - Create Post.java entity class with id, userId, title, content, lockTime, createdAt, updatedAt
    - Add JPA annotations (@Entity, @Table, @Id, @GeneratedValue)
    - Use Lombok annotations (@Getter, @Setter, @NoArgsConstructor)
    - _Requirements: 3.1_
  
  - [ ] 2.2 Create Comment entity
    - Create Comment.java entity class with id, postId, userId, content, voteCount, createdAt
    - Add JPA annotations and foreign key relationship to Post
    - _Requirements: 1.1, 1.3, 1.5_
  
  - [ ] 2.3 Create Vote entity
    - Create Vote.java entity class with id, commentId, userId, votedAt
    - Add unique constraint on (commentId, userId) using @Table annotation
    - Add foreign key relationship to Comment
    - _Requirements: 2.2_

- [ ] 3. Create DTOs and mappers
  - [ ] 3.1 Create PostRequest and PostResponse DTOs
    - Create PostRequest.java with validation annotations (@NotBlank, @Size)
    - Create PostResponse.java with all post fields
    - _Requirements: 3.1_
  
  - [ ] 3.2 Create CommentRequest and CommentResponse DTOs
    - Create CommentRequest.java with validation annotations
    - Create CommentResponse.java with all comment fields
    - _Requirements: 1.1, 1.4, 6.3_
  
  - [ ] 3.3 Create VoteRequest and VoteResponse DTOs
    - Create VoteRequest.java with commentId and userId
    - Create VoteResponse.java with commentId, voteCount, message
    - _Requirements: 2.1, 2.4_
  
  - [ ] 3.4 Create PostMapper using MapStruct
    - Create mapper interface for Post entity to DTO conversions
    - _Requirements: 3.1_
  
  - [ ] 3.5 Create CommentMapper using MapStruct
    - Create mapper interface for Comment entity to DTO conversions
    - _Requirements: 1.1_

- [ ] 4. Implement repositories
  - [ ] 4.1 Create PostRepository interface
    - Extend JpaRepository<Post, Long>
    - Add methods: findByUserId, findAllByOrderByCreatedAtDesc
    - _Requirements: 3.1_
  
  - [ ] 4.2 Create CommentRepository interface
    - Extend JpaRepository<Comment, Long>
    - Add methods: findByPostId, findByPostIdOrderByVoteCountDesc, findByPostIdOrderByCreatedAtAsc
    - _Requirements: 4.1, 4.2, 6.1_
  
  - [ ] 4.3 Create VoteRepository interface
    - Extend JpaRepository<Vote, Long>
    - Add methods: existsByCommentIdAndUserId, countByCommentId
    - _Requirements: 2.2, 7.2_

- [ ] 5. Implement PostService
  - [ ] 5.1 Create PostService with CRUD operations
    - Implement createPost, getPostById, getAllPosts, updatePost, deletePost, setLockTime methods
    - _Requirements: 3.1, 3.3_
  
  - [ ] 5.2 Add isLocked helper method
    - Implement method to check if current time exceeds lock time
    - _Requirements: 3.5_
  
  - [ ] 5.3 Write property test for lock time validation
    - **Property 9: Lock time comparison is accurate**
    - **Validates: Requirements 3.5**

- [ ] 6. Implement CommentService
  - [ ] 6.1 Create CommentService with comment creation logic
    - Implement createComment method with lock time validation
    - _Requirements: 1.1, 1.2, 1.3, 1.5_
  
  - [ ] 6.2 Write property test for comment creation
    - **Property 1: Comment creation increases comment count**
    - **Validates: Requirements 1.1**
  
  - [ ] 6.3 Write property test for locked post rejection
    - **Property 2: Locked posts reject new comments**
    - **Validates: Requirements 1.2, 3.2**
  
  - [ ] 6.4 Write property test for empty comment rejection
    - **Property 8: Empty comments are rejected**
    - **Validates: Requirements 1.4**
  
  - [ ] 6.5 Implement getCommentsByPostId with conditional sorting
    - Sort by vote count DESC if locked, by created time ASC if not locked
    - _Requirements: 4.1, 4.2, 4.3, 6.1, 6.5_
  
  - [ ] 6.6 Write property test for comment sorting
    - **Property 5: Locked post comments are sorted by votes**
    - **Property 6: Unlocked post comments are sorted by time**
    - **Validates: Requirements 4.1, 4.2**
  
  - [ ] 6.7 Implement getCommentById and deleteComment methods
    - _Requirements: 6.1, 7.1, 7.2, 7.4_
  
  - [ ] 6.8 Write property test for comment deletion
    - **Property 7: Comment deletion removes associated votes**
    - **Validates: Requirements 7.2**

- [ ] 7. Implement VoteService
  - [ ] 7.1 Create VoteService with vote creation logic
    - Implement voteOnComment method with duplicate check
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ] 7.2 Write property test for vote increment
    - **Property 3: Vote count increments correctly**
    - **Validates: Requirements 2.1**
  
  - [ ] 7.3 Write property test for duplicate vote prevention
    - **Property 4: Duplicate votes are prevented**
    - **Validates: Requirements 2.2**

- [ ] 8. Create REST controllers
  - [ ] 8.1 Create PostController with all endpoints
    - POST /posts, GET /posts, GET /posts/{id}, PUT /posts/{id}, DELETE /posts/{id}, PUT /posts/{id}/lock
    - _Requirements: 3.1, 3.3_
  
  - [ ] 8.2 Create CommentController with all endpoints
    - POST /comments, GET /comments/post/{postId}, GET /comments/{id}, DELETE /comments/{id}, POST /comments/{id}/vote
    - _Requirements: 1.1, 2.1, 6.1, 7.1_
  
  - [ ] 8.3 Add request validation and OpenAPI documentation
    - _Requirements: 1.4, 2.5, 6.2, 7.3_

- [ ] 9. Implement error handling
  - [ ] 9.1 Create custom exceptions
    - PostLockedException, DuplicateVoteException, PostNotFoundException, CommentNotFoundException
    - _Requirements: 1.2, 2.2, 6.2_
  
  - [ ] 9.2 Create GlobalExceptionHandler
    - Handle all custom exceptions with appropriate HTTP status codes
    - _Requirements: All error scenarios_

- [ ] 10. Add pagination support
  - [ ] 10.1 Update repositories with Pageable support
    - _Requirements: 6.4_
  
  - [ ] 10.2 Update services to use pagination
    - _Requirements: 6.4_
  
  - [ ] 10.3 Update controllers to accept pagination parameters
    - _Requirements: 6.4_

- [ ] 11. Add security configuration
  - [ ] 11.1 Create SecurityConfig with JWT authentication
    - _Requirements: All_
  
  - [ ] 11.2 Add role-based authorization for admin endpoints
    - _Requirements: 7.1, 3.3_

- [ ] 12. Create Dockerfile and update docker-compose
  - [ ] 12.1 Create Dockerfile for social-service
    - _Requirements: 5.1_
  
  - [ ] 12.2 Update docker-compose.yml
    - Add social-service and socialdb
    - _Requirements: 5.1, 5.3_
  
  - [ ] 12.3 Update build scripts
    - _Requirements: 5.1_

- [ ] 13. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

