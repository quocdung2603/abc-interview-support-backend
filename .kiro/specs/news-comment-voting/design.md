# Design Document

## Overview

This design implements a **social-service** - a unified microservice that manages posts, comments, and voting functionality. The system allows users to create posts (articles/news), comment on posts, vote on comments, and provides automatic ranking of comments based on votes after a post is locked by an admin.

**Note**: The existing **news-service** remains unchanged and continues to handle recruitment-related functionality only. Social-service is for general social posts and discussions.

## Architecture

### Microservice Architecture

```
Gateway Service
    ↓
Social Service (New)                    News Service (Existing - Recruitment)
    ↓                                       ↓
SocialDB (PostgreSQL)                   NewsDB (PostgreSQL)
```

### Social Service Internal Architecture

```
Controller Layer (REST API)
    ↓
    PostController          CommentController
    ↓                       ↓
Service Layer (Business Logic)
    ↓                       ↓
    PostService            CommentService + VoteService
    ↓                       ↓
Repository Layer (Data Access)
    ↓                       ↓
    PostRepository         CommentRepository + VoteRepository
    ↓                       ↓
Database (PostgreSQL - socialdb)
    ↓                       ↓
    posts table            comments + comment_votes tables
```

### Key Components:

- **PostController**: Handles HTTP requests for post operations (CRUD, lock management)
- **PostService**: Implements business logic for posts
- **PostRepository**: Provides data access for posts
- **Post Entity**: Represents a post with lockTime field

- **CommentController**: Handles HTTP requests for comment and voting operations
- **CommentService**: Implements business logic for comments (create, retrieve, delete, sorting)
- **VoteService**: Handles voting logic (vote creation, duplicate prevention)
- **CommentRepository**: Provides data access for comments
- **VoteRepository**: Provides data access for votes
- **Comment Entity**: Represents a comment on a post
- **Vote Entity**: Represents a vote on a comment

### Service Benefits:
- **Single Database Transaction**: Post and Comment operations can be in same transaction
- **No Network Overhead**: Direct method calls instead of HTTP/Feign calls
- **Simpler Deployment**: One service to deploy and manage
- **Data Consistency**: Foreign key constraints work within same database
- **Easier Testing**: No need to mock inter-service calls

## Components and Interfaces

### 1. Post Entity

```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;        // User who created the post
    private String title;
    
    @Column(columnDefinition = "text")
    private String content;
    
    private LocalDateTime lockTime;  // Time when post becomes locked
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2. Comment Entity

```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long postId;        // Foreign key to posts table
    private Long userId;        // User who created the comment
    
    @Column(columnDefinition = "text")
    private String content;
    
    private Integer voteCount;  // Denormalized vote count for performance
    private LocalDateTime createdAt;
}
```

### 3. Vote Entity

```java
@Entity
@Table(name = "comment_votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long commentId;     // Foreign key to comments table
    private Long userId;        // User who voted
    private LocalDateTime votedAt;
    
    // Unique constraint on (commentId, userId) to prevent duplicate votes
}
```

### 4. DTOs

**PostRequest:**
```java
{
    "userId": Long,
    "title": String (required, max 200 chars),
    "content": String (required, max 10000 chars),
    "lockTime": LocalDateTime (optional)
}
```

**PostResponse:**
```java
{
    "id": Long,
    "userId": Long,
    "title": String,
    "content": String,
    "lockTime": LocalDateTime,
    "createdAt": LocalDateTime,
    "updatedAt": LocalDateTime
}
```

**CommentRequest:**
```java
{
    "postId": Long,
    "userId": Long,
    "content": String (required, max 1000 chars)
}
```

**CommentResponse:**
```java
{
    "id": Long,
    "postId": Long,
    "userId": Long,
    "content": String,
    "voteCount": Integer,
    "createdAt": LocalDateTime
}
```

**VoteRequest:**
```java
{
    "commentId": Long,
    "userId": Long
}
```

**VoteResponse:**
```java
{
    "commentId": Long,
    "voteCount": Integer,
    "message": String
}
```

### 5. REST API Endpoints

**Post Endpoints:**
```
POST   /posts                       - Create a new post
GET    /posts                       - Get all posts (with pagination)
GET    /posts/{id}                  - Get a single post by ID
PUT    /posts/{id}                  - Update a post
DELETE /posts/{id}                  - Delete a post (admin only)
PUT    /posts/{id}/lock             - Set lock time for a post (admin only)
```

**Comment Endpoints:**
```
POST   /comments                    - Create a new comment
GET    /comments/post/{postId}      - Get all comments for a post (with sorting)
GET    /comments/{id}               - Get a single comment by ID
DELETE /comments/{id}               - Delete a comment (admin only)
POST   /comments/{id}/vote          - Vote on a comment
```

## Data Models

### Database Schema

**Social Service Database (socialdb):**

**posts table:**
```sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    lock_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_lock_time ON posts(lock_time);
```

**comments table:**
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    vote_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_vote_count ON comments(vote_count DESC);
CREATE INDEX idx_comments_created_at ON comments(created_at);
```

**comment_votes table:**
```sql
CREATE TABLE comment_votes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_votes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT uk_comment_user_vote UNIQUE (comment_id, user_id)
);

CREATE INDEX idx_votes_comment_id ON comment_votes(comment_id);
CREATE INDEX idx_votes_user_id ON comment_votes(user_id);
```

### Relationships

- One Post has Many Comments (1:N) - enforced by foreign key
- One Comment has Many Votes (1:N) - enforced by foreign key
- One User can have Many Votes, but only one Vote per Comment (N:1 with unique constraint)

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Comment creation increases comment count
*For any* post and valid comment content, creating a comment should result in the comment count for that post increasing by one
**Validates: Requirements 1.1**

### Property 2: Locked posts reject new comments
*For any* post where current time exceeds lock time, attempting to create a comment should be rejected with an error
**Validates: Requirements 1.2, 3.2**

### Property 3: Vote count increments correctly
*For any* comment and user who hasn't voted yet, voting should increase the vote count by exactly one
**Validates: Requirements 2.1**

### Property 4: Duplicate votes are prevented
*For any* comment and user who has already voted, attempting to vote again should not change the vote count
**Validates: Requirements 2.2**

### Property 5: Locked post comments are sorted by votes
*For any* locked post with comments, retrieving comments should return them ordered by vote count in descending order
**Validates: Requirements 4.1**

### Property 6: Unlocked post comments are sorted by time
*For any* unlocked post with comments, retrieving comments should return them ordered by creation time in ascending order
**Validates: Requirements 4.2**

### Property 7: Comment deletion removes associated votes
*For any* comment with votes, deleting the comment should also remove all votes associated with that comment
**Validates: Requirements 7.2**

### Property 8: Empty comments are rejected
*For any* comment submission with empty or whitespace-only content, the system should reject it and the comment count should remain unchanged
**Validates: Requirements 1.4**

### Property 9: Lock time comparison is accurate
*For any* post with a lock time, checking if the post is locked should return true if and only if the current time is greater than or equal to the lock time
**Validates: Requirements 3.5**

### Property 10: Comment retrieval includes all required fields
*For any* comment retrieved from the system, the response should contain comment ID, user ID, content, vote count, and creation time
**Validates: Requirements 6.3**

## Error Handling

### Error Scenarios:

1. **Comment on locked post**: Return 400 Bad Request with message "Post is locked, no new comments allowed"
2. **Duplicate vote**: Return 409 Conflict with message "User has already voted on this comment"
3. **Non-existent post**: Return 404 Not Found with message "Post not found with id: {id}"
4. **Non-existent comment**: Return 404 Not Found with message "Comment not found with id: {id}"
5. **Empty comment**: Return 400 Bad Request with message "Comment content cannot be empty"
6. **Comment too long**: Return 400 Bad Request with message "Comment exceeds maximum length of 1000 characters"
7. **Empty post title**: Return 400 Bad Request with message "Post title cannot be empty"
8. **Post title too long**: Return 400 Bad Request with message "Post title exceeds maximum length of 200 characters"
9. **Database constraint violation**: Return 500 Internal Server Error with generic message

### Error Response Format:

```json
{
    "timestamp": "2025-11-26T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Post is locked, no new comments allowed",
    "path": "/comments"
}
```

## Testing Strategy

### Unit Tests

Unit tests will cover:
- Post creation, update, and deletion
- Comment creation with valid and invalid data
- Vote creation and duplicate vote prevention
- Lock time validation logic
- Comment sorting logic (by votes vs by time)
- Edge cases: empty comments, non-existent posts, null values

### Property-Based Tests

We will use **JUnit 5** with **jqwik** for property-based testing in Java.

Each property-based test will:
- Run a minimum of 100 iterations with random inputs
- Be tagged with a comment referencing the design document property
- Use the format: `**Feature: news-comment-voting, Property {number}: {property_text}**`

Example property test structure:
```java
@Property
@Label("Feature: news-comment-voting, Property 1: Comment creation increases comment count")
void testCommentCreationIncreasesCount(@ForAll Long postId, @ForAll @StringLength(min=1, max=1000) String content) {
    // Test implementation
}
```

### Integration Tests

Integration tests will verify:
- End-to-end post creation and retrieval flow
- End-to-end comment creation flow
- Vote counting across multiple users
- Comment sorting after post lock
- Cascade deletion of votes when comment is deleted
- Cascade deletion of comments when post is deleted

### Performance Considerations

- Use denormalized `voteCount` field to avoid counting votes on every query
- Create database indexes on `post_id`, `vote_count`, and `created_at` for fast sorting
- Implement pagination for posts and comments
- Use database-level unique constraint to prevent duplicate votes
- Use database-level foreign key constraints with CASCADE for data integrity

## Implementation Notes

1. **Vote Count Denormalization**: Store vote count directly in the comment table and update it transactionally when votes are added. This avoids expensive COUNT queries.

2. **Lock Time Check**: Implement as a simple comparison in the service layer before allowing comment creation. Check if `post.lockTime != null && LocalDateTime.now().isAfter(post.lockTime)`.

3. **Sorting Strategy**: CommentService retrieves post to check lock status, then applies appropriate sorting:
   - If locked (lockTime < now): ORDER BY vote_count DESC, created_at ASC
   - If not locked: ORDER BY created_at ASC

4. **Cascade Deletion**: Use database-level ON DELETE CASCADE to automatically:
   - Remove votes when comments are deleted
   - Remove comments when posts are deleted

5. **Transaction Management**: Use `@Transactional` annotation for operations that modify both comments and votes to ensure data consistency.

6. **Service Registration**: Social-service registers with Eureka for service discovery and load balancing.

7. **Security**: Use JWT authentication. Admin-only endpoints (delete, lock) require ADMIN role.

8. **Pagination**: Implement pagination for both posts and comments to handle large datasets efficiently.

