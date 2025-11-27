# Requirements Document

## Introduction

This document specifies the requirements for improving the Social Service to support two distinct post types with different commenting and voting behaviors. The system shall enable weighted voting based on user ELO rankings and enforce strict comment limitations for admin-created locked posts.

## Glossary

- **Social Service**: The microservice responsible for managing posts, comments, and voting functionality
- **Post**: A discussion thread created by users or administrators
- **Normal Post**: A post where users can comment multiple times and vote on comments
- **Locked Post**: An admin-created post where each user can comment once with one edit, and comments are sorted by vote count
- **Comment**: User-generated content in response to a post
- **Vote**: A user's indication that a comment is useful or not useful
- **ELO Rank**: A numerical rating representing a user's reputation or expertise level
- **Vote Weight**: The influence a user's vote has, calculated based on their ELO rank
- **Vote Percentage**: The normalized vote score for a comment, capped at 100%
- **User Service**: External microservice that provides user information including ELO rankings

## Requirements

### Requirement 1

**User Story:** As a user, I want to create and view normal posts, so that I can participate in open discussions with multiple comments.

#### Acceptance Criteria

1. WHEN a user creates a post without a lock time THEN the Social Service SHALL create a normal post
2. WHEN a user views a normal post THEN the Social Service SHALL display comments sorted by creation time ascending
3. WHEN a user comments on a normal post THEN the Social Service SHALL allow unlimited comments from the same user
4. WHEN a user votes on a comment in a normal post THEN the Social Service SHALL apply vote weighting based on the user's ELO rank
5. WHEN calculating vote percentage for a normal post comment THEN the Social Service SHALL cap the total at 100%

### Requirement 2

**User Story:** As an administrator, I want to create locked posts for gathering focused feedback, so that each user provides one well-considered comment.

#### Acceptance Criteria

1. WHEN an administrator creates a post with a lock time THEN the Social Service SHALL create a locked post
2. WHEN the current time reaches or exceeds the lock time THEN the Social Service SHALL treat the post as locked
3. WHEN a user views a locked post THEN the Social Service SHALL display comments sorted by vote count descending
4. WHEN a user attempts to comment on a locked post THEN the Social Service SHALL check if the user has already commented
5. IF a user has already commented on a locked post THEN the Social Service SHALL reject additional comments

### Requirement 3

**User Story:** As a user, I want to edit my comment on a locked post once, so that I can refine my contribution without spamming.

#### Acceptance Criteria

1. WHEN a user edits their comment on a locked post THEN the Social Service SHALL track the edit count
2. WHEN a user has already edited their comment once on a locked post THEN the Social Service SHALL reject further edit attempts
3. WHEN a comment is edited THEN the Social Service SHALL update the content and record the edit timestamp
4. WHEN displaying a comment THEN the Social Service SHALL show whether it has been edited

### Requirement 4

**User Story:** As a user, I want my votes to carry weight based on my ELO ranking, so that experienced users have more influence on comment quality.

#### Acceptance Criteria

1. WHEN a user votes on a comment THEN the Social Service SHALL fetch the user's ELO rank from the User Service
2. WHEN calculating vote weight THEN the Social Service SHALL apply a formula based on ELO rank
3. WHEN a user with higher ELO rank votes THEN the Social Service SHALL apply greater weight to their vote
4. WHEN the User Service is unavailable THEN the Social Service SHALL apply a default weight of 1.0
5. WHEN storing a vote THEN the Social Service SHALL record the vote weight at the time of voting

### Requirement 5

**User Story:** As a user, I want to vote comments as useful or not useful, so that I can indicate comment quality.

#### Acceptance Criteria

1. WHEN a user votes on a comment THEN the Social Service SHALL accept vote type as useful or not useful
2. WHEN a useful vote is cast THEN the Social Service SHALL add the weighted vote to the comment's score
3. WHEN a not useful vote is cast THEN the Social Service SHALL subtract the weighted vote from the comment's score
4. WHEN a user has already voted on a comment THEN the Social Service SHALL reject duplicate votes
5. WHEN calculating vote percentage THEN the Social Service SHALL normalize the score to a 0-100% range

### Requirement 6

**User Story:** As a developer, I want the system to handle User Service failures gracefully, so that voting continues even when external services are down.

#### Acceptance Criteria

1. WHEN the User Service is unreachable THEN the Social Service SHALL log the error and continue processing
2. WHEN ELO rank cannot be fetched THEN the Social Service SHALL use a default rank value
3. WHEN the User Service returns an error THEN the Social Service SHALL apply default vote weight
4. WHEN the User Service times out THEN the Social Service SHALL proceed with default values after 2 seconds
5. WHEN the User Service recovers THEN the Social Service SHALL resume fetching ELO ranks

### Requirement 7

**User Story:** As a system administrator, I want comprehensive error handling, so that users receive clear feedback when operations fail.

#### Acceptance Criteria

1. WHEN a user attempts an invalid operation THEN the Social Service SHALL return an appropriate HTTP status code
2. WHEN a user exceeds comment limits on a locked post THEN the Social Service SHALL return a 409 Conflict error
3. WHEN a user attempts to edit beyond the limit THEN the Social Service SHALL return a 409 Conflict error with a descriptive message
4. WHEN a duplicate vote is attempted THEN the Social Service SHALL return a 409 Conflict error
5. WHEN a resource is not found THEN the Social Service SHALL return a 404 Not Found error

### Requirement 8

**User Story:** As a developer, I want the vote percentage calculation to be accurate and performant, so that comment rankings reflect community consensus.

#### Acceptance Criteria

1. WHEN calculating vote percentage THEN the Social Service SHALL sum all weighted votes for the comment
2. WHEN the weighted vote sum exceeds 100 THEN the Social Service SHALL cap the percentage at 100%
3. WHEN the weighted vote sum is negative THEN the Social Service SHALL set the percentage to 0%
4. WHEN displaying comments THEN the Social Service SHALL include the vote percentage in the response
5. WHEN sorting locked post comments THEN the Social Service SHALL use the vote percentage as the primary sort key
