# Requirements Document

## Introduction

This document specifies the requirements for adding a comment system with voting functionality to the news service. The system allows users to comment on news posts, vote on comments, and provides automatic comment ranking based on votes after a post is locked by admin.

## Glossary

- **News Service**: The microservice responsible for managing news posts and comments
- **Post**: A news article or recruitment announcement created by users
- **Comment**: User-generated text response to a post
- **Vote**: A user action to indicate support for a comment (upvote)
- **Lock Time**: The timestamp when a post becomes locked and no new comments can be added
- **Admin**: A user with administrative privileges who can set lock times for posts
- **Comment Ranking**: The ordering of comments based on vote count, with highest votes first

## Requirements

### Requirement 1

**User Story:** As a user, I want to add comments to news posts, so that I can share my thoughts and engage in discussions.

#### Acceptance Criteria

1. WHEN a user submits a comment with valid content THEN the system SHALL create a new comment and associate it with the specified post
2. WHEN a user attempts to comment on a locked post THEN the system SHALL reject the comment and return an error message
3. WHEN a comment is created THEN the system SHALL record the creation timestamp and user ID
4. WHEN a user submits an empty comment THEN the system SHALL reject the comment and maintain the current state
5. WHEN a comment is created THEN the system SHALL initialize the vote count to zero

### Requirement 2

**User Story:** As a user, I want to vote on comments, so that I can indicate which comments I find valuable or agree with.

#### Acceptance Criteria

1. WHEN a user votes on a comment THEN the system SHALL increment the vote count for that comment by one
2. WHEN a user attempts to vote on the same comment multiple times THEN the system SHALL prevent duplicate votes from the same user
3. WHEN a vote is recorded THEN the system SHALL persist the vote immediately to the database
4. WHEN a user votes on a comment THEN the system SHALL return the updated vote count
5. WHEN a user attempts to vote on a non-existent comment THEN the system SHALL return an error message

### Requirement 3

**User Story:** As an admin, I want to set a lock time for posts, so that I can control when discussions should be closed and comments ranked.

#### Acceptance Criteria

1. WHEN an admin creates a post with a lock time THEN the system SHALL store the lock time with the post
2. WHEN the current time exceeds the lock time THEN the system SHALL prevent new comments from being added
3. WHEN an admin updates a post's lock time THEN the system SHALL update the stored lock time value
4. WHEN a post is created without a lock time THEN the system SHALL allow comments indefinitely
5. WHEN checking if a post is locked THEN the system SHALL compare the current time with the stored lock time

### Requirement 4

**User Story:** As a user, I want to see comments sorted by vote count after a post is locked, so that I can easily find the most valuable contributions.

#### Acceptance Criteria

1. WHEN retrieving comments for a locked post THEN the system SHALL return comments ordered by vote count in descending order
2. WHEN retrieving comments for an unlocked post THEN the system SHALL return comments ordered by creation time in ascending order
3. WHEN two comments have the same vote count THEN the system SHALL order them by creation time with older comments first
4. WHEN a post has no comments THEN the system SHALL return an empty list
5. WHEN comments are retrieved THEN the system SHALL include the vote count for each comment

### Requirement 5

**User Story:** As a developer, I want clear separation between post management and comment management, so that the system is maintainable and extensible.

#### Acceptance Criteria

1. WHEN comment logic is updated THEN the post management logic SHALL remain unaffected
2. WHEN post logic is modified THEN the comment management logic SHALL continue functioning unchanged
3. WHEN the database schema is queried THEN the comments table SHALL have a foreign key relationship to the posts table

### Requirement 6

**User Story:** As a user, I want to retrieve all comments for a specific post, so that I can read the discussion.

#### Acceptance Criteria

1. WHEN requesting comments for a post THEN the system SHALL return all comments associated with that post
2. WHEN requesting comments for a non-existent post THEN the system SHALL return an error message
3. WHEN comments are retrieved THEN the system SHALL include comment ID, user ID, content, vote count, and creation time
4. WHEN a post has many comments THEN the system SHALL support pagination for comment retrieval
5. WHEN retrieving comments THEN the system SHALL apply the appropriate sorting based on post lock status

### Requirement 7

**User Story:** As an admin, I want to delete inappropriate comments, so that I can maintain content quality and enforce community guidelines.

#### Acceptance Criteria

1. WHEN an admin deletes a comment THEN the system SHALL remove the comment from the database
2. WHEN a comment is deleted THEN the system SHALL also remove all associated votes for that comment
3. WHEN an admin attempts to delete a non-existent comment THEN the system SHALL return an error message
4. WHEN a comment is deleted THEN the system SHALL return a success confirmation with the deleted comment ID
5. WHEN deleting a comment THEN the system SHALL maintain referential integrity with the votes table
