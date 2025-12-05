# Design Document: Social Post Classification Enhancement

## Overview

This design enhances the Social Service's Post classification system by adding validation, filtering, and enrichment capabilities. Posts already contain `fieldId`, `topicId`, and `levelId` fields, but currently lack validation against the Question Service's taxonomy and do not provide human-readable classification names in responses.

The solution integrates Social Service with Question Service through REST API calls, implements caching for performance, adds circuit breaker for resilience, and extends the Post API with filtering capabilities.

## Architecture

### Component Interaction

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Client    │────────>│  Social Service  │────────>│ Question Service│
└─────────────┘         │                  │         │                 │
                        │  - PostService   │         │  - Field API    │
                        │  - Validation    │         │  - Topic API    │
                        │  - Caching       │         │  - Level API    │
                        │  - Circuit       │         └─────────────────┘
                        │    Breaker       │
                        └──────────────────┘
                                │
                                v
                        ┌──────────────────┐
                        │   Redis Cache    │
                        │  (Classification)│
                        └──────────────────┘
```

### Service Communication

- **Synchronous REST**: Social Service calls Question Service REST endpoints
- **Caching Layer**: Redis caches Field/Topic/Level data to reduce load
- **Circuit Breaker**: Resilience4j protects against Question Service failures
- **Feign Client**: Declarative REST client for Question Service integration

## Components and Interfaces

### 1. Question Service Client

**Interface: QuestionServiceClient**
```java
public interface QuestionServiceClient {
    FieldResponse getFieldById(Long fieldId);
    TopicResponse getTopicById(Long topicId);
    LevelResponse getLevelById(Long levelId);
    boolean validateFieldExists(Long fieldId);
    boolean validateTopicBelongsToField(Long topicId, Long fieldId);
    boolean validateLevelExists(Long levelId);
}
```

**Implementation: QuestionServiceClientImpl**
- Uses Spring Cloud OpenFeign for REST calls
- Applies Resilience4j circuit breaker
- Handles timeouts and connection errors
- Returns fallback responses when service unavailable

### 2. Classification Cache Service

**Interface: ClassificationCacheService**
```java
public interface ClassificationCacheService {
    Optional<FieldResponse> getCachedField(Long fieldId);
    Optional<TopicResponse> getCachedTopic(Long topicId);
    Optional<LevelResponse> getCachedLevel(Long levelId);
    void cacheField(Long fieldId, FieldResponse field);
    void cacheTopic(Long topicId, TopicResponse topic);
    void cacheLevel(Long levelId, LevelResponse level);
    void evictAll();
}
```

**Implementation: ClassificationCacheServiceImpl**
- Uses Spring Cache abstraction with Redis backend
- TTL: 1 hour for all classification data
- Cache key format: `classification:field:{id}`, `classification:topic:{id}`, `classification:level:{id}`
- Implements cache-aside pattern

### 3. Classification Validator

**Interface: ClassificationValidator**
```java
public interface ClassificationValidator {
    ValidationResult validateClassification(Long fieldId, Long topicId, Long levelId);
}

public class ValidationResult {
    private boolean valid;
    private String errorMessage;
    private Map<String, String> fieldErrors;
}
```

**Implementation: ClassificationValidatorImpl**
- Validates fieldId exists
- Validates topicId exists and belongs to fieldId
- Validates levelId exists (if provided)
- Returns detailed error messages for each validation failure
- Uses QuestionServiceClient with caching

### 4. Enhanced Post Service

**Updates to PostService**
- Add validation before create/update operations
- Enrich PostResponse with classification names
- Support filtering by fieldId, topicId, levelId
- Handle validation errors gracefully

### 5. Enhanced Post Repository

**New Query Methods**
```java
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByFieldId(Long fieldId, Pageable pageable);
    Page<Post> findByTopicId(Long topicId, Pageable pageable);
    Page<Post> findByLevelId(Long levelId, Pageable pageable);
    Page<Post> findByFieldIdAndTopicId(Long fieldId, Long topicId, Pageable pageable);
    Page<Post> findByFieldIdAndTopicIdAndLevelId(Long fieldId, Long topicId, Long levelId, Pageable pageable);
    Page<Post> findByFieldIdAndLevelId(Long fieldId, Long levelId, Pageable pageable);
    Page<Post> findByTopicIdAndLevelId(Long topicId, Long levelId, Pageable pageable);
}
```

### 6. Enhanced DTOs

**PostResponse Enhancement**
```java
@Data
public class PostResponse {
    private Long id;
    private Long userId;
    
    // Classification IDs
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    
    // Classification Names (enriched)
    private String fieldName;
    private String topicName;
    private String levelName;
    
    private String postType;
    private String status;
    private String title;
    private String content;
    private String lockTime;
    private String createdAt;
    private String updatedAt;
}
```

**PostFilterRequest**
```java
@Data
public class PostFilterRequest {
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    private String postType;
    private String status;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
```

## Data Models

### Existing Post Entity
```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private Long fieldId;       // Already exists
    private Long topicId;       // Already exists
    private Long levelId;       // Already exists
    
    private String postType;
    private String status;
    private String title;
    private String content;
    private LocalDateTime lockTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

No database schema changes required - all fields already exist.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Valid classification IDs are accepted

*For any* post creation request with valid fieldId, topicId, and levelId (that exist in Question Service), the Social Service should successfully create the post and return the post with classification data.

**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Invalid fieldId is rejected

*For any* post creation request with a fieldId that does not exist in Question Service, the Social Service should reject the request and return an error indicating invalid field.

**Validates: Requirements 1.4**

### Property 3: Invalid topicId is rejected

*For any* post creation request with a topicId that does not exist or does not belong to the specified fieldId, the Social Service should reject the request and return an error indicating invalid topic.

**Validates: Requirements 1.5**

### Property 4: Invalid levelId is rejected

*For any* post creation request with a levelId that does not exist in Question Service, the Social Service should reject the request and return an error indicating invalid level.

**Validates: Requirements 1.6**

### Property 5: Filtering by fieldId returns only matching posts

*For any* fieldId and any set of posts in the database, when filtering by that fieldId, all returned posts should have that exact fieldId.

**Validates: Requirements 2.1**

### Property 6: Filtering by topicId returns only matching posts

*For any* topicId and any set of posts in the database, when filtering by that topicId, all returned posts should have that exact topicId.

**Validates: Requirements 2.2**

### Property 7: Filtering by levelId returns only matching posts

*For any* levelId and any set of posts in the database, when filtering by that levelId, all returned posts should have that exact levelId.

**Validates: Requirements 2.3**

### Property 8: Multiple filters are combined with AND logic

*For any* combination of fieldId, topicId, and levelId filters, all returned posts should match ALL specified filter criteria.

**Validates: Requirements 2.4**

### Property 9: Response includes classification names

*For any* post response where classification IDs are present, the response should include the corresponding field name, topic name, and level name retrieved from Question Service or cache.

**Validates: Requirements 3.1**

### Property 10: Update validation follows same rules as creation

*For any* post update request with new classification IDs, the validation rules should be identical to those applied during post creation.

**Validates: Requirements 4.1, 4.2, 4.3, 4.4**

## Error Handling

### Validation Errors

**Invalid Field**
- HTTP Status: 400 Bad Request
- Error Code: `INVALID_FIELD`
- Message: "Field with ID {fieldId} does not exist"

**Invalid Topic**
- HTTP Status: 400 Bad Request
- Error Code: `INVALID_TOPIC`
- Message: "Topic with ID {topicId} does not exist or does not belong to field {fieldId}"

**Invalid Level**
- HTTP Status: 400 Bad Request
- Error Code: `INVALID_LEVEL`
- Message: "Level with ID {levelId} does not exist"

### Service Unavailability

**Question Service Down**
- HTTP Status: 503 Service Unavailable
- Error Code: `CLASSIFICATION_SERVICE_UNAVAILABLE`
- Message: "Classification validation is temporarily unavailable. Please try again later."

**Timeout**
- HTTP Status: 504 Gateway Timeout
- Error Code: `CLASSIFICATION_SERVICE_TIMEOUT`
- Message: "Classification service request timed out"

### Circuit Breaker States

**Open State**
- Reject requests immediately without calling Question Service
- Return 503 Service Unavailable
- Log circuit breaker open event

**Half-Open State**
- Allow limited test requests
- Monitor success rate
- Transition to closed or open based on results

**Closed State**
- Normal operation
- Monitor failure rate
- Open circuit if threshold exceeded

## Testing Strategy

### Unit Tests

Unit tests will verify specific behaviors and edge cases:

1. **ClassificationValidator Tests**
   - Valid classification passes validation
   - Invalid fieldId fails validation
   - Invalid topicId fails validation
   - Topic not belonging to field fails validation
   - Invalid levelId fails validation
   - Null levelId (optional) passes validation

2. **PostService Tests**
   - Create post with valid classification succeeds
   - Create post with invalid classification fails
   - Update post with valid classification succeeds
   - Update post with invalid classification fails
   - Response enrichment includes classification names

3. **PostRepository Tests**
   - Filter by fieldId returns correct posts
   - Filter by topicId returns correct posts
   - Filter by levelId returns correct posts
   - Multiple filters combine correctly

4. **Cache Service Tests**
   - Cache hit returns cached value
   - Cache miss triggers service call
   - Cache eviction works correctly
   - TTL expiration works correctly

5. **Circuit Breaker Tests**
   - Circuit opens after threshold failures
   - Circuit half-opens after wait duration
   - Circuit closes after successful requests
   - Fallback behavior works correctly

### Property-Based Tests

Property-based tests will use **jqwik** (already configured in social-service) to verify universal properties across many random inputs.

**Configuration**: Each property test should run minimum 100 iterations.

**Test Library**: jqwik for Java

1. **Property Test: Valid classification acceptance**
   - Generate random valid fieldId, topicId, levelId
   - Create post with these IDs
   - Verify post is created successfully
   - **Feature: social-post-classification, Property 1: Valid classification IDs are accepted**

2. **Property Test: Invalid fieldId rejection**
   - Generate random invalid fieldId (not in Question Service)
   - Attempt to create post
   - Verify request is rejected with appropriate error
   - **Feature: social-post-classification, Property 2: Invalid fieldId is rejected**

3. **Property Test: Invalid topicId rejection**
   - Generate random invalid topicId or topicId not belonging to fieldId
   - Attempt to create post
   - Verify request is rejected with appropriate error
   - **Feature: social-post-classification, Property 3: Invalid topicId is rejected**

4. **Property Test: Invalid levelId rejection**
   - Generate random invalid levelId
   - Attempt to create post
   - Verify request is rejected with appropriate error
   - **Feature: social-post-classification, Property 4: Invalid levelId is rejected**

5. **Property Test: Field filtering correctness**
   - Generate random set of posts with various fieldIds
   - Filter by random fieldId
   - Verify all returned posts have that fieldId
   - **Feature: social-post-classification, Property 5: Filtering by fieldId returns only matching posts**

6. **Property Test: Topic filtering correctness**
   - Generate random set of posts with various topicIds
   - Filter by random topicId
   - Verify all returned posts have that topicId
   - **Feature: social-post-classification, Property 6: Filtering by topicId returns only matching posts**

7. **Property Test: Level filtering correctness**
   - Generate random set of posts with various levelIds
   - Filter by random levelId
   - Verify all returned posts have that levelId
   - **Feature: social-post-classification, Property 7: Filtering by levelId returns only matching posts**

8. **Property Test: Multiple filter combination**
   - Generate random set of posts
   - Apply random combination of fieldId, topicId, levelId filters
   - Verify all returned posts match ALL filter criteria
   - **Feature: social-post-classification, Property 8: Multiple filters are combined with AND logic**

9. **Property Test: Response enrichment**
   - Generate random post with classification IDs
   - Retrieve post
   - Verify response includes both IDs and names for all classifications
   - **Feature: social-post-classification, Property 9: Response includes classification names**

10. **Property Test: Update validation consistency**
    - Generate random post
    - Update with random new classification IDs
    - Verify validation rules match creation validation
    - **Feature: social-post-classification, Property 10: Update validation follows same rules as creation**

### Integration Tests

Integration tests will verify end-to-end flows:

1. **Full Post Creation Flow**
   - Call Question Service to get valid IDs
   - Create post with valid classification
   - Verify post stored correctly
   - Verify response includes enriched data

2. **Filtering Integration**
   - Create multiple posts with different classifications
   - Test various filter combinations
   - Verify pagination works with filters

3. **Circuit Breaker Integration**
   - Simulate Question Service failures
   - Verify circuit breaker opens
   - Verify graceful degradation
   - Verify recovery when service returns

4. **Cache Integration**
   - Create post (triggers cache population)
   - Verify subsequent requests use cache
   - Verify cache expiration
   - Verify cache eviction

## Configuration

### Application Properties

```yaml
# Question Service Integration
question-service:
  url: http://question-service:8083
  timeout:
    connect: 2000
    read: 3000

# Circuit Breaker Configuration
resilience4j:
  circuitbreaker:
    instances:
      questionService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 3000ms

# Cache Configuration
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour in milliseconds
  redis:
    host: localhost
    port: 6379

# Classification Cache
classification:
  cache:
    ttl: 3600  # 1 hour in seconds
```

## API Endpoints

### Enhanced GET /posts

**Query Parameters:**
- `fieldId` (optional): Filter by field ID
- `topicId` (optional): Filter by topic ID
- `levelId` (optional): Filter by level ID
- `postType` (optional): Filter by post type
- `status` (optional): Filter by status
- `page` (default: 0): Page number
- `size` (default: 20): Page size
- `sort` (default: createdAt,desc): Sort specification

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 123,
      "fieldId": 1,
      "fieldName": "Computer Science",
      "topicId": 5,
      "topicName": "Data Structures",
      "levelId": 2,
      "levelName": "Intermediate",
      "postType": "QUESTION",
      "status": "PUBLISHED",
      "title": "How to implement a binary tree?",
      "content": "I'm learning about binary trees...",
      "lockTime": null,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5
}
```

### POST /posts (Enhanced Validation)

**Request:**
```json
{
  "fieldId": 1,
  "topicId": 5,
  "levelId": 2,
  "postType": "QUESTION",
  "title": "How to implement a binary tree?",
  "content": "I'm learning about binary trees..."
}
```

**Success Response (201):**
```json
{
  "id": 1,
  "userId": 123,
  "fieldId": 1,
  "fieldName": "Computer Science",
  "topicId": 5,
  "topicName": "Data Structures",
  "levelId": 2,
  "levelName": "Intermediate",
  "postType": "QUESTION",
  "status": "DRAFT",
  "title": "How to implement a binary tree?",
  "content": "I'm learning about binary trees...",
  "lockTime": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error Response (400):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_TOPIC",
  "message": "Topic with ID 999 does not exist or does not belong to field 1",
  "path": "/posts"
}
```

**Error Response (503):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 503,
  "error": "Service Unavailable",
  "code": "CLASSIFICATION_SERVICE_UNAVAILABLE",
  "message": "Classification validation is temporarily unavailable. Please try again later.",
  "path": "/posts"
}
```

## Performance Considerations

### Caching Strategy

- **Cache Hit Rate Target**: > 80%
- **Cache Warming**: Pre-populate cache with frequently accessed classifications
- **Cache Invalidation**: Manual eviction endpoint for administrators

### Load Reduction

- Batch validation requests when possible
- Use cache-aside pattern to minimize Question Service calls
- Implement request coalescing for concurrent identical requests

### Monitoring

- Track cache hit/miss rates
- Monitor Question Service call latency
- Alert on circuit breaker state changes
- Track validation failure rates by error type
