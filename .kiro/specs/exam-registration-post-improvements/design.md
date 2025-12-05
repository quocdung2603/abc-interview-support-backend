# Design Document

## Overview

This design addresses three key improvements to the exam and social services:

1. **Exam Registration API Enhancement**: Fix the missing examId field in registration responses and ensure registrations appear correctly when queried by exam ID
2. **Social Post DTO Refactoring**: Align the Post response structure with the frontend interface requirements by adding missing fields (fieldId, topicId, levelId, postType, status)
3. **Role-Based Post Creation**: Implement a workflow where administrators can publish posts immediately while regular users create drafts requiring approval

The design maintains backward compatibility where possible and follows the existing microservice architecture patterns.

## Architecture

### Service Layer Changes

**Exam Service**:
- Modify the `ExamRegistrationService` to properly map examId from the entity relationship
- Update repository queries to ensure bidirectional relationship integrity
- Enhance DTO mapping to include examId in all registration responses

**Social Service**:
- Extend the `Post` entity to include fieldId, topicId, levelId, postType, and status fields
- Implement role-based status determination in `PostService`
- Add filtering logic for post queries based on user role and post status
- Create an approval endpoint for administrators to publish draft posts

### Authentication Integration

The Social Service will need to extract user role information from the JWT token or authentication context to determine whether a post should be created as DRAFT or PUBLISHED.

## Components and Interfaces

### Exam Service Components

#### ExamRegistrationResponse DTO
```java
public class ExamRegistrationResponse {
    private Long id;
    private Long examId;  // Already exists, needs proper mapping
    private Long userId;
    private String registrationStatus;
    private LocalDateTime registeredAt;
}
```

#### ExamRegistrationService
- **Method**: `mapToResponse(ExamRegistration registration)`
  - Extract examId from `registration.getExam().getId()`
  - Handle null exam gracefully
  
- **Method**: `getRegistrationsByExamId(Long examId)`
  - Use repository method to fetch all registrations for an exam
  - Ensure proper entity relationship loading

### Social Service Components

#### Enhanced Post Entity
```java
@Entity
public class Post {
    private Long id;
    private Long userId;
    private Long fieldId;      // NEW: Reference to field/category
    private Long topicId;      // NEW: Reference to topic
    private Long levelId;      // NEW: Optional difficulty level
    private String postType;   // NEW: DISCUSSION or QUESTION
    private String status;     // NEW: DRAFT, PUBLISHED, or LOCKED
    private String title;
    private String content;
    private LocalDateTime lockTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### PostResponse DTO
```java
public class PostResponse {
    private Long id;
    private Long userId;
    private Long fieldId;
    private Long topicId;
    private Long levelId;      // Nullable
    private String postType;   // DISCUSSION | QUESTION
    private String status;     // DRAFT | PUBLISHED | LOCKED
    private String title;
    private String content;
    private String lockTime;   // ISO 8601 format
    private String createdAt;  // ISO 8601 format
    private String updatedAt;  // ISO 8601 format
}
```

#### PostRequest DTO
```java
public class PostRequest {
    private Long userId;       // Can be extracted from auth context
    private Long fieldId;
    private Long topicId;
    private Long levelId;      // Optional
    private String postType;   // DISCUSSION | QUESTION
    private String title;
    private String content;
    private LocalDateTime lockTime;  // Optional
}
```

#### PostService Methods

- **Method**: `createPost(PostRequest request, String userRole)`
  - Determine status based on userRole (ADMIN → PUBLISHED, USER → DRAFT)
  - Validate required fields
  - Save post with appropriate status
  
- **Method**: `approvePost(Long postId, String userRole)`
  - Verify user is admin
  - Update post status from DRAFT to PUBLISHED
  - Return updated post
  
- **Method**: `getPosts(String userRole, Long userId)`
  - If admin: return all posts
  - If regular user: return PUBLISHED posts + own DRAFT posts
  - Apply filtering based on status

#### PostController Endpoints

- `POST /api/posts` - Create post (status determined by role)
- `PUT /api/posts/{id}/approve` - Approve draft post (admin only)
- `GET /api/posts` - List posts (filtered by role and status)
- `GET /api/posts/{id}` - Get single post (with access control)

## Data Models

### Database Schema Changes

#### Posts Table (social-service)
```sql
ALTER TABLE posts 
ADD COLUMN field_id BIGINT,
ADD COLUMN topic_id BIGINT,
ADD COLUMN level_id BIGINT,
ADD COLUMN post_type VARCHAR(20) NOT NULL DEFAULT 'DISCUSSION',
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Add indexes for filtering
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_user_status ON posts(user_id, status);
CREATE INDEX idx_posts_field ON posts(field_id);
CREATE INDEX idx_posts_topic ON posts(topic_id);
```

### Entity Relationships

**ExamRegistration ↔ Exam**:
- ManyToOne relationship already exists
- Ensure proper fetch strategy (LAZY) to avoid N+1 queries
- Map examId explicitly in service layer from relationship

**Post → Field/Topic/Level**:
- Store as Long IDs (foreign keys to other services)
- No JPA relationships (microservice boundary)
- Validation should verify IDs exist in respective services

## Correctnes
s Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, several redundancies were identified:
- Properties 1.1 and 1.2 both validate examId presence and can be combined into a single comprehensive property
- Property 1.4 is redundant with the combined 1.1/1.2 property
- Property 3.2 is redundant with 3.1 (field completeness)
- Property 4.4 is redundant with 4.1 (admin posts are published)
- Property 5.5 is redundant with 5.2 and 5.3 (filtering behavior)

The following properties represent the unique, non-redundant validation requirements:

### Property 1: Registration responses include examId
*For any* exam registration retrieved from the system (whether by user ID or exam ID), the response DTO should contain a non-null examId field that matches the associated exam's ID.
**Validates: Requirements 1.1, 1.2, 1.4**

### Property 2: Registration persistence maintains exam relationship
*For any* registration created with an exam, retrieving that registration from the database should return a registration with the correct exam relationship intact.
**Validates: Requirements 1.3**

### Property 3: Registration creates bidirectional relationship
*For any* registration created for an exam, querying registrations by that exam ID should include the newly created registration.
**Validates: Requirements 2.1, 2.3**

### Property 4: Exam query returns all registrations
*For any* exam with multiple registrations, querying registrations by exam ID should return exactly the set of registrations associated with that exam (no more, no less).
**Validates: Requirements 2.2**

### Property 5: Post response contains all required fields
*For any* post returned by the API, the response should include all fields specified in the Post interface: id, userId, fieldId, topicId, levelId, postType, status, title, content, lockTime, createdAt, and updatedAt.
**Validates: Requirements 3.1, 3.2**

### Property 6: Timestamps are ISO 8601 formatted
*For any* post response, the lockTime, createdAt, and updatedAt fields (when non-null) should be valid ISO 8601 formatted strings.
**Validates: Requirements 3.4, 3.5**

### Property 7: Admin posts are published immediately
*For any* post creation request made by an administrator, the resulting post should have status PUBLISHED.
**Validates: Requirements 4.1, 4.4**

### Property 8: Admin posts are visible to all users
*For any* post created by an administrator, querying posts as any user (admin or regular) should include that post in the results.
**Validates: Requirements 4.2**

### Property 9: Regular user posts are drafts
*For any* post creation request made by a regular user (non-admin), the resulting post should have status DRAFT.
**Validates: Requirements 5.1**

### Property 10: Draft posts are excluded from public listings
*For any* DRAFT post, querying posts as a user who is neither the creator nor an administrator should not include that post in the results.
**Validates: Requirements 5.2**

### Property 11: Draft posts are visible to creator and admins
*For any* DRAFT post, querying posts as either the post creator or an administrator should include that post in the results.
**Validates: Requirements 5.3**

### Property 12: Approval changes draft to published
*For any* post with DRAFT status, calling the approve endpoint (as an administrator) should result in the post status changing to PUBLISHED.
**Validates: Requirements 5.4**

## Error Handling

### Exam Service Error Scenarios

1. **Missing Exam Relationship**: If a registration entity has a null exam reference, the service should handle gracefully by either:
   - Returning null for examId in the response
   - Throwing a DataIntegrityException if this represents corrupted data

2. **Exam Not Found**: When creating a registration with an invalid examId, return 404 with message "Exam not found"

3. **Duplicate Registration**: If a user attempts to register for the same exam twice, return 409 with message "User already registered for this exam"

### Social Service Error Scenarios

1. **Invalid Field/Topic/Level IDs**: When creating a post with non-existent fieldId, topicId, or levelId:
   - Return 400 Bad Request with specific field validation error
   - Consider implementing validation against respective services

2. **Unauthorized Approval**: When a non-admin attempts to approve a post:
   - Return 403 Forbidden with message "Only administrators can approve posts"

3. **Invalid Status Transition**: When attempting to approve a post that is not in DRAFT status:
   - Return 400 Bad Request with message "Only draft posts can be approved"

4. **Post Not Found**: When accessing a non-existent post:
   - Return 404 with message "Post not found with id: {id}"

5. **Access Denied**: When a regular user attempts to access another user's draft post:
   - Return 403 Forbidden with message "Access denied to this post"

## Testing Strategy

### Unit Testing Approach

**Exam Service**:
- Test DTO mapping logic with mock entities
- Test repository query methods with in-memory database
- Test error handling for null exam relationships
- Test duplicate registration prevention

**Social Service**:
- Test role-based status determination logic
- Test post filtering logic for different user roles
- Test approval workflow state transitions
- Test timestamp formatting in DTOs
- Test access control logic for draft posts

### Property-Based Testing Approach

We will use **jqwik** (already in use in the social-service) for property-based testing in Java.

**Configuration**:
- Each property test should run a minimum of 100 iterations
- Each test must be tagged with a comment referencing the design document property

**Test Tag Format**: `// Feature: exam-registration-post-improvements, Property {number}: {property_text}`

**Generators Needed**:

*Exam Service*:
- `ExamGenerator`: Generate random Exam entities with valid IDs
- `ExamRegistrationGenerator`: Generate registrations with exam relationships
- `UserIdGenerator`: Generate valid user IDs

*Social Service*:
- `PostGenerator`: Generate posts with all fields including optional levelId
- `UserRoleGenerator`: Generate ADMIN or USER roles
- `PostStatusGenerator`: Generate DRAFT, PUBLISHED, or LOCKED statuses
- `ISO8601DateGenerator`: Generate valid ISO 8601 date strings

**Key Property Tests**:

1. **Registration ExamId Property**: Generate random registrations, retrieve them, verify examId is present and correct
2. **Bidirectional Relationship Property**: Create registrations, verify they appear in exam's registration list
3. **Post Field Completeness Property**: Generate random posts, serialize to DTO, verify all fields present
4. **ISO 8601 Format Property**: Generate posts with timestamps, verify format compliance
5. **Role-Based Status Property**: Generate post requests with random roles, verify status matches role
6. **Draft Visibility Property**: Generate draft posts, query as different users, verify access control
7. **Approval Workflow Property**: Generate draft posts, approve them, verify status change

### Integration Testing

- Test complete registration flow: create exam → register user → query registrations
- Test complete post creation flow: authenticate → create post → verify visibility
- Test approval workflow: create draft → approve → verify published
- Test cross-service validation: verify fieldId/topicId exist in respective services

## Implementation Notes

### Migration Strategy

1. **Database Migration**: Run schema changes on social-service database first
2. **Backward Compatibility**: Existing posts without new fields should default to:
   - `postType`: 'DISCUSSION'
   - `status`: 'PUBLISHED' (assume existing posts are published)
   - `fieldId`, `topicId`, `levelId`: NULL (to be populated later)
3. **API Versioning**: Consider versioning the POST /api/posts endpoint if breaking changes are needed

### Authentication Context

The Social Service needs access to user role information. Options:
1. Extract from JWT token claims (preferred)
2. Call User Service to fetch role (adds latency)
3. Include role in request body (less secure)

Recommended: Extract role from JWT token's "authorities" or "roles" claim.

### Performance Considerations

1. **N+1 Query Prevention**: Use `@EntityGraph` or JOIN FETCH when loading registrations with exams
2. **Post Query Optimization**: Add database indexes on status and userId for filtering
3. **Caching**: Consider caching field/topic/level validation results

### Security Considerations

1. **Authorization**: Implement method-level security with `@PreAuthorize` annotations
2. **Input Validation**: Validate all IDs to prevent injection attacks
3. **Rate Limiting**: Consider rate limiting post creation to prevent spam
4. **Audit Logging**: Log all post approvals and status changes
