# Implementation Plan

- [x] 1. Set up Question Service client infrastructure


  - Add Spring Cloud OpenFeign dependency to pom.xml
  - Add Resilience4j circuit breaker dependency
  - Configure Feign client properties in application.yml
  - Configure circuit breaker properties for Question Service
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_



- [ ] 2. Create Question Service client and DTOs
  - [ ] 2.1 Create FieldResponse, TopicResponse, LevelResponse DTOs in social-service
    - Mirror the structure from Question Service


    - Add Jackson annotations for JSON serialization
    - _Requirements: 3.1_

  - [ ] 2.2 Create QuestionServiceClient Feign interface
    - Define methods: getFieldById, getTopicById, getLevelById


    - Add @FeignClient annotation with service name and URL
    - Add circuit breaker configuration
    - Add timeout configuration
    - _Requirements: 1.1, 1.2, 1.3, 5.4_



  - [ ] 2.3 Implement fallback handler for circuit breaker
    - Create QuestionServiceClientFallback class
    - Return empty Optional or throw custom exception when service unavailable
    - Log circuit breaker events


    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 3. Implement classification caching
  - [ ] 3.1 Configure Redis cache for classification data
    - Add Spring Data Redis dependency


    - Configure Redis connection in application.yml
    - Set TTL to 1 hour for classification cache
    - _Requirements: 3.2, 3.4_

  - [x] 3.2 Create ClassificationCacheService interface and implementation


    - Implement cache-aside pattern
    - Methods: getCachedField, getCachedTopic, getCachedLevel
    - Methods: cacheField, cacheTopic, cacheLevel, evictAll


    - Use @Cacheable and @CachePut annotations
    - _Requirements: 3.2, 3.4_

  - [ ] 3.3 Write unit tests for cache service
    - Test cache hit returns cached value
    - Test cache miss triggers service call
    - Test cache eviction

    - _Requirements: 3.2, 3.4_

- [x] 4. Implement classification validation

  - [ ] 4.1 Create ValidationResult class
    - Fields: valid, errorMessage, fieldErrors map
    - Builder pattern for easy construction

    - _Requirements: 1.4, 1.5, 1.6_

  - [x] 4.2 Create ClassificationValidator interface and implementation

    - Implement validateClassification method
    - Validate fieldId exists using QuestionServiceClient
    - Validate topicId exists and belongs to fieldId
    - Validate levelId exists (if provided)
    - Use caching to minimize service calls
    - Return detailed ValidationResult
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_



  - [ ] 4.3 Write property test for valid classification acceptance
    - **Property 1: Valid classification IDs are accepted**
    - **Validates: Requirements 1.1, 1.2, 1.3**




  - [ ] 4.4 Write property test for invalid fieldId rejection
    - **Property 2: Invalid fieldId is rejected**
    - **Validates: Requirements 1.4**

  - [ ] 4.5 Write property test for invalid topicId rejection
    - **Property 3: Invalid topicId is rejected**
    - **Validates: Requirements 1.5**



  - [ ] 4.6 Write property test for invalid levelId rejection
    - **Property 4: Invalid levelId is rejected**

    - **Validates: Requirements 1.6**

- [ ] 5. Enhance PostResponse DTO with classification names
  - [ ] 5.1 Add classification name fields to PostResponse
    - Add fieldName, topicName, levelName fields

    - Update mapper to populate these fields
    - _Requirements: 3.1_

  - [x] 5.2 Create PostMapper utility for entity-to-DTO conversion

    - Method: toResponse(Post post, FieldResponse field, TopicResponse topic, LevelResponse level)

    - Handle null values gracefully
    - Format dates to ISO 8601
    - _Requirements: 3.1_

  - [ ] 5.3 Write property test for response enrichment
    - **Property 9: Response includes classification names**
    - **Validates: Requirements 3.1**

- [ ] 6. Update PostService with validation and enrichment
  - [x] 6.1 Inject ClassificationValidator and ClassificationCacheService


    - Add constructor injection
    - _Requirements: 1.1, 1.2, 1.3_


  - [ ] 6.2 Add validation to createPost method
    - Call validator before saving post

    - Throw custom exception if validation fails
    - Include detailed error message
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_


  - [ ] 6.3 Add validation to updatePost method
    - Call validator before updating post

    - Throw custom exception if validation fails
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [ ] 6.4 Add enrichment to response methods
    - Fetch classification data from cache/service
    - Populate fieldName, topicName, levelName in response
    - Handle Question Service unavailability gracefully (return IDs only)
    - Log warning if enrichment fails
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ] 6.5 Write property test for update validation consistency
    - **Property 10: Update validation follows same rules as creation**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**



- [ ] 7. Add filtering capabilities to PostRepository
  - [ ] 7.1 Add query methods to PostRepository
    - findByFieldId(Long fieldId, Pageable pageable)


    - findByTopicId(Long topicId, Pageable pageable)
    - findByLevelId(Long levelId, Pageable pageable)
    - findByFieldIdAndTopicId(Long fieldId, Long topicId, Pageable pageable)
    - findByFieldIdAndTopicIdAndLevelId(Long fieldId, Long topicId, Long levelId, Pageable pageable)
    - findByFieldIdAndLevelId(Long fieldId, Long levelId, Pageable pageable)
    - findByTopicIdAndLevelId(Long topicId, Long levelId, Pageable pageable)


    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [ ] 7.2 Write property test for field filtering
    - **Property 5: Filtering by fieldId returns only matching posts**

    - **Validates: Requirements 2.1**

  - [ ] 7.3 Write property test for topic filtering
    - **Property 6: Filtering by topicId returns only matching posts**
    - **Validates: Requirements 2.2**



  - [ ] 7.4 Write property test for level filtering
    - **Property 7: Filtering by levelId returns only matching posts**
    - **Validates: Requirements 2.3**



  - [ ] 7.5 Write property test for multiple filter combination
    - **Property 8: Multiple filters are combined with AND logic**
    - **Validates: Requirements 2.4**

- [ ] 8. Implement dynamic filtering in PostService
  - [x] 8.1 Create PostFilterRequest DTO

    - Fields: fieldId, topicId, levelId, postType, status
    - Pagination fields: page, size, sortBy, sortDirection
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 8.2 Add filterPosts method to PostService

    - Accept PostFilterRequest parameter
    - Build dynamic query based on non-null filters
    - Call appropriate repository method
    - Enrich responses with classification names

    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 9. Update PostController with filtering endpoints
  - [ ] 9.1 Update GET /posts endpoint to accept filter parameters
    - Add @RequestParam for fieldId, topicId, levelId
    - Call PostService.filterPosts with parameters


    - Return paginated enriched responses
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [ ] 9.2 Add OpenAPI documentation for filter parameters
    - Document fieldId, topicId, levelId query parameters
    - Add example requests and responses



    - Document error responses (400, 503, 504)
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 10. Implement custom exception handling
  - [ ] 10.1 Create custom exceptions
    - InvalidClassificationException (400)



    - ClassificationServiceUnavailableException (503)
    - ClassificationServiceTimeoutException (504)
    - _Requirements: 1.4, 1.5, 1.6, 5.1, 5.4_

  - [ ] 10.2 Create GlobalExceptionHandler
    - Handle InvalidClassificationException → 400 with error details
    - Handle ClassificationServiceUnavailableException → 503
    - Handle ClassificationServiceTimeoutException → 504
    - Handle FeignException → appropriate status codes
    - Return standardized error response format
    - _Requirements: 1.4, 1.5, 1.6, 5.1, 5.4_

  - [ ] 10.3 Write unit tests for exception handling
    - Test each exception type returns correct status code
    - Test error response format
    - Test error message content

- [ ] 11. Add monitoring and observability
  - [ ] 11.1 Add Actuator endpoints for circuit breaker
    - Expose circuit breaker health indicator
    - Expose circuit breaker metrics
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 11.2 Add custom metrics for classification operations
    - Counter: classification_validation_total (with result label)
    - Counter: classification_cache_hits_total
    - Counter: classification_cache_misses_total
    - Timer: classification_validation_duration
    - _Requirements: 3.2_

  - [ ] 11.3 Add logging for key operations
    - Log validation failures with details
    - Log circuit breaker state changes
    - Log Question Service call failures
    - Log cache eviction events
    - _Requirements: 3.3, 5.1, 5.4_

- [ ] 12. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Update API documentation
  - [ ] 13.1 Update OpenAPI specification
    - Document enhanced PostResponse with classification names
    - Document filter parameters for GET /posts
    - Add example requests with filters
    - Add example error responses
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ] 13.2 Create Postman collection for classification features
    - Request: Create post with valid classification
    - Request: Create post with invalid fieldId (expect 400)
    - Request: Create post with invalid topicId (expect 400)
    - Request: Filter posts by fieldId
    - Request: Filter posts by topicId and levelId
    - Request: Get post with enriched classification names
    - _Requirements: 6.1, 6.2_

- [ ] 14. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
