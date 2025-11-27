# Design Document

## Overview

This design enhances the Social Service to support two distinct post types with different voting and commenting behaviors. The system implements weighted voting based on user ELO rankings, enforces comment limitations for locked posts, and provides accurate vote percentage calculations capped at 100%.

## Architecture

### High-Level Architecture

```
┌─────────────┐         ┌──────────────────┐         ┌──────────────┐
│   Client    │────────▶│  Social Service  │────────▶│ User Service │
└─────────────┘         └──────────────────┘         └──────────────┘
                               │
                               ▼
                        ┌──────────────┐
                        │  PostgreSQL  │
                        └──────────────┘
```

### Component Interaction

1. **Client** sends requests to Social Service
2. **Social Service** processes business logic and fetches ELO ranks from User Service
3. **User Service** provides user information including ELO rankings
4. **PostgreSQL** stores posts, comments, and votes with weighted scores

## Components and Interfaces

### 1. Enhanced Entity Models

#### Comment Entity
```java
@Entity
@Table(name = "comments")
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer voteCount;        // Deprecated - kept for backward compatibility
    private Double weightedVoteScore; // NEW: Sum of weighted votes
    private Integer editCount;        // NEW: Track number of edits
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;  // NEW: Track last edit time
}
```

#### Vote Entity
```java
@Entity
@Table(name = "comment_votes")
public class Vote {
    private Long id;
    private Long commentId;
    private Long userId;
    private String voteType;          // NEW: "USEFUL" or "NOT_USEFUL"
    private Double voteWeight;        // NEW: Weight based on ELO at vote time
    private LocalDateTime votedAt;
}
```

### 2. User Service Client

```java
public interface UserServiceClient {
    /**
     * Fetches user ELO rank from User Service
     * @param userId The user ID
     * @return ELO rank value, or default if unavailable
     */
    Integer getUserEloRank(Long userId);
    
    /**
     * Checks if User Service is available
     * @return true if service is reachable
     */
    boolean isAvailable();
}
```

### 3. Vote Weight Calculator

```java
public interface VoteWeightCalculator {
    /**
     * Calculates vote weight based on ELO rank
     * Formula: weight = 1.0 + (eloRank - 1000) / 1000.0
     * - ELO 1000 = weight 1.0 (baseline)
     * - ELO 1500 = weight 1.5
     * - ELO 2000 = weight 2.0
     * 
     * @param eloRank User's ELO ranking
     * @return Vote weight (minimum 0.5, maximum 3.0)
     */
    Double calculateWeight(Integer eloRank);
}
```

### 4. Comment Limit Validator

```java
public interface CommentLimitValidator {
    /**
     * Checks if user can comment on a locked post
     * @param postId The post ID
     * @param userId The user ID
     * @return true if user hasn't commented yet
     */
    boolean canComment(Long postId, Long userId);
    
    /**
     * Checks if user can edit their comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @return true if user owns comment and hasn't exceeded edit limit
     */
    boolean canEdit(Long commentId, Long userId);
}
```

## Data Models

### Database Schema Changes

```sql
-- Add new columns to comments table
ALTER TABLE comments 
ADD COLUMN weighted_vote_score DOUBLE PRECISION DEFAULT 0.0,
ADD COLUMN edit_count INTEGER DEFAULT 0,
ADD COLUMN updated_at TIMESTAMP;

-- Add new columns to comment_votes table
ALTER TABLE comment_votes
ADD COLUMN vote_type VARCHAR(20) NOT NULL DEFAULT 'USEFUL',
ADD COLUMN vote_weight DOUBLE PRECISION NOT NULL DEFAULT 1.0;

-- Add index for locked post comment queries
CREATE INDEX idx_comments_post_weighted_score 
ON comments(post_id, weighted_vote_score DESC, created_at ASC);

-- Add index for user comment count on posts
CREATE INDEX idx_comments_post_user 
ON comments(post_id, user_id);
```

### Vote Percentage Calculation

```
votePercentage = min(100, max(0, weightedVoteScore))
```

Where `weightedVoteScore` is the sum of all weighted votes:
- Useful votes add positive weight
- Not useful votes subtract weight

## Correctn
ess Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Property 1: Normal post comment sorting
*For any* normal post with multiple comments, retrieving comments should return them sorted by creation time in ascending order
**Validates: Requirements 1.2**

Property 2: Unlimited comments on normal posts
*For any* normal post and any user, the system should accept multiple comments from the same user without rejection
**Validates: Requirements 1.3**

Property 3: Vote weight calculation
*For any* user with an ELO rank, when voting on a comment, the calculated vote weight should follow the formula: weight = 1.0 + (eloRank - 1000) / 1000.0, bounded between 0.5 and 3.0
**Validates: Requirements 4.2**

Property 4: Locked post creation
*For any* post created with a lock time, the post should be marked as locked when the current time equals or exceeds the lock time
**Validates: Requirements 2.1, 2.2**

Property 5: Locked post comment sorting
*For any* locked post with multiple comments, retrieving comments should return them sorted by weighted vote score in descending order
**Validates: Requirements 2.3**

Property 6: Single comment per user on locked posts
*For any* locked post and any user, after the user creates one comment, subsequent comment attempts should be rejected with a 409 Conflict error
**Validates: Requirements 2.4, 2.5**

Property 7: Edit count tracking
*For any* comment, when edited, the edit count should increment and the updated timestamp should be set to the current time
**Validates: Requirements 3.1, 3.3**

Property 8: Edit limit enforcement
*For any* comment on a locked post, after one edit, subsequent edit attempts should be rejected with a 409 Conflict error
**Validates: Requirements 3.2**

Property 9: Vote type acceptance
*For any* vote request with vote type "USEFUL" or "NOT_USEFUL", the system should accept the vote; for any other vote type, the system should reject it
**Validates: Requirements 5.1**

Property 10: Useful vote score calculation
*For any* comment, when a useful vote with weight W is cast, the weighted vote score should increase by W
**Validates: Requirements 5.2**

Property 11: Not useful vote score calculation
*For any* comment, when a not useful vote with weight W is cast, the weighted vote score should decrease by W
**Validates: Requirements 5.3**

Property 12: Duplicate vote prevention
*For any* comment and any user, after the user votes once, subsequent vote attempts should be rejected with a 409 Conflict error
**Validates: Requirements 5.4**

Property 13: Vote percentage normalization
*For any* comment with weighted vote score S, the vote percentage should be min(100, max(0, S))
**Validates: Requirements 5.5, 8.1**

Property 14: Vote weight persistence
*For any* vote, the vote weight calculated at the time of voting should be stored in the database and retrievable
**Validates: Requirements 4.5**

Property 15: Comment response includes vote percentage
*For any* comment retrieved from the API, the response should include the calculated vote percentage field
**Validates: Requirements 8.4**

## Error Handling

### Exception Types

1. **CommentLimitExceededException** (409 Conflict)
   - Thrown when user attempts to comment more than once on a locked post
   - Message: "User has already commented on this locked post"

2. **EditLimitExceededException** (409 Conflict)
   - Thrown when user attempts to edit more than once
   - Message: "Comment has already been edited once and cannot be edited again"

3. **DuplicateVoteException** (409 Conflict)
   - Thrown when user attempts to vote twice on the same comment
   - Message: "User has already voted on this comment"

4. **InvalidVoteTypeException** (400 Bad Request)
   - Thrown when vote type is not USEFUL or NOT_USEFUL
   - Message: "Vote type must be USEFUL or NOT_USEFUL"

5. **PostLockedException** (400 Bad Request)
   - Thrown when attempting operations not allowed on locked posts
   - Message: "This post is locked and does not allow this operation"

### Fallback Strategies

1. **User Service Unavailable**
   - Default ELO rank: 1000
   - Default vote weight: 1.0
   - Log warning with trace ID
   - Continue processing request

2. **User Service Timeout**
   - Timeout after 2 seconds
   - Apply default values
   - Log timeout event
   - Return success with default weight

3. **Database Connection Issues**
   - Retry up to 3 times with exponential backoff
   - If all retries fail, return 503 Service Unavailable
   - Log error with full stack trace

## Testing Strategy

### Unit Testing

Unit tests will cover:
- Vote weight calculation with various ELO ranks
- Vote percentage normalization (including edge cases: negative scores, scores > 100)
- Comment limit validation logic
- Edit limit validation logic
- Post lock status determination
- Error response formatting

### Property-Based Testing

We will use **JUnit 5** with **jqwik** for property-based testing in Java.

Property-based tests will:
- Generate random posts with various lock times
- Generate random users with ELO ranks from 500 to 2500
- Generate random comments with various vote scores
- Generate random vote sequences (useful/not useful)
- Verify all correctness properties hold across generated inputs
- Run a minimum of 100 iterations per property test

Each property-based test will be tagged with a comment referencing the design document:
```java
// Feature: social-service-improvements, Property 1: Normal post comment sorting
```

### Integration Testing

Integration tests will verify:
- End-to-end comment creation and voting flows
- User Service integration with circuit breaker
- Database transaction handling
- Error responses match API specification

### Test Data Generators

Custom generators for property-based testing:
- `EloRankGenerator`: Generates realistic ELO ranks (500-2500)
- `PostGenerator`: Generates posts with/without lock times
- `CommentGenerator`: Generates comments with various content
- `VoteSequenceGenerator`: Generates sequences of votes with different types and weights

## Performance Considerations

1. **Vote Score Denormalization**
   - Store `weightedVoteScore` directly on Comment entity
   - Update incrementally on each vote (no need to recalculate from all votes)
   - Trade-off: Slight increase in write complexity for significant read performance gain

2. **Database Indexing**
   - Index on `(post_id, weighted_vote_score DESC, created_at ASC)` for locked post queries
   - Index on `(post_id, user_id)` for comment limit checks
   - Index on `(comment_id, user_id)` for duplicate vote checks (already exists via unique constraint)

3. **User Service Caching**
   - Cache ELO ranks for 5 minutes using Spring Cache
   - Reduces User Service load
   - Acceptable staleness for vote weight calculation

4. **Batch Operations**
   - When fetching comments, include vote percentage in single query
   - Avoid N+1 queries for vote calculations

## Security Considerations

1. **Authorization**
   - Only post owner or admin can set lock time
   - Only comment owner can edit their comment
   - All users can vote (once per comment)

2. **Input Validation**
   - Validate vote type enum
   - Validate ELO rank range (if provided manually)
   - Sanitize comment content to prevent XSS
   - Limit comment length (max 5000 characters)

3. **Rate Limiting**
   - Limit comment creation to 10 per minute per user
   - Limit vote creation to 30 per minute per user
   - Prevent abuse of edit functionality

## Migration Strategy

1. **Database Migration**
   - Add new columns with default values
   - Backfill existing data:
     - Set `weighted_vote_score` = `vote_count` for existing comments
     - Set `edit_count` = 0 for existing comments
     - Set `vote_type` = 'USEFUL' for existing votes
     - Set `vote_weight` = 1.0 for existing votes

2. **API Versioning**
   - Maintain backward compatibility
   - Old vote endpoint continues to work (assumes USEFUL vote type)
   - New vote endpoint requires vote type parameter
   - Deprecation notice for old endpoint

3. **Rollout Plan**
   - Phase 1: Deploy database changes
   - Phase 2: Deploy service with feature flag disabled
   - Phase 3: Enable weighted voting for new posts only
   - Phase 4: Enable for all posts after monitoring

## Monitoring and Observability

1. **Metrics**
   - Vote weight distribution histogram
   - User Service call success rate
   - User Service response time
   - Comment limit rejection rate
   - Edit limit rejection rate
   - Vote percentage distribution

2. **Logging**
   - Log all User Service failures with trace ID
   - Log comment/edit limit violations
   - Log vote weight calculations for audit
   - Log abnormal vote patterns (potential abuse)

3. **Alerts**
   - User Service availability < 95%
   - User Service response time > 3 seconds
   - Comment rejection rate > 10%
   - Abnormal vote patterns detected
