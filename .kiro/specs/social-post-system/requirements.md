# Requirements Document

## Introduction

This document specifies the requirements for a Social Post System with two distinct post types: Normal Posts and Admin Feedback Posts. The system supports commenting, voting mechanisms with ELO-based weighting, and different interaction rules based on post type.

## Glossary

- **Social Post System**: The complete system managing posts, comments, and voting
- **Normal Post**: A post type where users can comment freely and vote on comment usefulness
- **Admin Feedback Post**: A post type created by administrators where each user has limited comment/edit rights
- **Comment**: A user-generated response to a post (non-hierarchical, single-level only)
- **Vote**: A user action indicating comment usefulness (useful/unuseful)
- **ELO Rank**: A numerical rating system representing user expertise level
- **Vote Weight**: The influence of a vote based on the voter's ELO rank
- **Vote Percentage**: The normalized useful vote score (0-100%) for a comment
- **User**: Any authenticated person who can create posts, comment, and vote
- **Admin**: A privileged user who can create Admin Feedback Posts

## Requirements

### Requirement 1: Post Management

**User Story:** As a user, I want to create and manage posts, so that I can share content and gather feedback from the community.

#### Acceptance Criteria

1. WHEN a user creates a post, THE Social Post System SHALL store the post with user ID, content, post type, and creation timestamp
2. WHEN a user specifies post type during creation, THE Social Post System SHALL validate that the type is either "NORMAL" or "ADMIN_FEEDBACK"
3. WHEN an admin creates a post, THE Social Post System SHALL allow setting post type to "ADMIN_FEEDBACK"
4. WHEN a non-admin user creates a post, THE Social Post System SHALL restrict post type to "NORMAL" only
5. WHEN a user requests to view a post, THE Social Post System SHALL return the post with all associated comments sorted by vote score

### Requirement 2: Normal Post Commenting

**User Story:** As a user, I want to comment on normal posts without restrictions, so that I can participate in open discussions.

#### Acceptance Criteria

1. WHEN a user adds a comment to a normal post, THE Social Post System SHALL create the comment without limiting the number of comments per user
2. WHEN a user edits their comment on a normal post, THE Social Post System SHALL update the comment content and record the edit timestamp
3. WHEN a user deletes their comment on a normal post, THE Social Post System SHALL remove the comment and all associated votes
4. WHEN retrieving comments for a normal post, THE Social Post System SHALL return all comments in a flat structure (non-hierarchical)
5. WHEN a comment is created, THE Social Post System SHALL initialize its vote score to zero

### Requirement 3: Admin Feedback Post Commenting

**User Story:** As a user, I want to contribute to admin feedback posts with controlled participation, so that feedback collection is structured and fair.

#### Acceptance Criteria

1. WHEN a user attempts to comment on an admin feedback post, THE Social Post System SHALL verify the user has not already commented on that post
2. WHEN a user has already commented on an admin feedback post, THE Social Post System SHALL reject additional comment attempts with an error message
3. WHEN a user edits their comment on an admin feedback post, THE Social Post System SHALL verify the user has not already edited once
4. WHEN a user has already edited their comment once on an admin feedback post, THE Social Post System SHALL reject further edit attempts
5. WHEN a user edits their comment on an admin feedback post, THE Social Post System SHALL record the edit timestamp and increment the edit count

### Requirement 4: Comment Voting with ELO Weighting

**User Story:** As a user, I want to vote on comment usefulness with my vote weighted by my expertise, so that experienced users have more influence on comment rankings.

#### Acceptance Criteria

1. WHEN a user votes on a comment, THE Social Post System SHALL record the vote type (useful or unuseful) and the voter's ELO rank
2. WHEN calculating a comment's vote score, THE Social Post System SHALL apply vote weight based on the voter's ELO rank
3. WHEN a user changes their vote on a comment, THE Social Post System SHALL update the existing vote record and recalculate the comment score
4. WHEN a user removes their vote on a comment, THE Social Post System SHALL delete the vote record and recalculate the comment score
5. WHEN calculating vote percentage, THE Social Post System SHALL normalize the weighted useful votes to a scale of 0-100%

### Requirement 5: Vote Weight Calculation

**User Story:** As a system administrator, I want votes weighted by ELO rank, so that expert opinions have appropriate influence on comment rankings.

#### Acceptance Criteria

1. WHEN calculating vote weight, THE Social Post System SHALL use the formula: weight = (ELO rank / 1000)
2. WHEN a user with ELO rank 1000 votes, THE Social Post System SHALL apply a weight of 1.0
3. WHEN a user with ELO rank 2000 votes, THE Social Post System SHALL apply a weight of 2.0
4. WHEN a user with ELO rank 500 votes, THE Social Post System SHALL apply a weight of 0.5
5. WHEN aggregating votes for a comment, THE Social Post System SHALL sum all weighted useful votes and all weighted unuseful votes separately

### Requirement 6: Vote Percentage Normalization

**User Story:** As a user, I want to see comment usefulness as a percentage, so that I can quickly understand community consensus.

#### Acceptance Criteria

1. WHEN calculating vote percentage for a comment, THE Social Post System SHALL use the formula: percentage = (weighted_useful_votes / (weighted_useful_votes + weighted_unuseful_votes)) * 100
2. WHEN a comment has only useful votes, THE Social Post System SHALL return a vote percentage of 100%
3. WHEN a comment has only unuseful votes, THE Social Post System SHALL return a vote percentage of 0%
4. WHEN a comment has no votes, THE Social Post System SHALL return a vote percentage of 50% (neutral)
5. WHEN displaying vote percentage, THE Social Post System SHALL cap the maximum value at 100%

### Requirement 7: Comment Sorting for Admin Feedback Posts

**User Story:** As a user viewing an admin feedback post, I want to see the most useful comments first, so that I can quickly find valuable feedback.

#### Acceptance Criteria

1. WHEN retrieving comments for an admin feedback post, THE Social Post System SHALL sort comments by vote percentage in descending order
2. WHEN two comments have equal vote percentage, THE Social Post System SHALL sort by total weighted votes in descending order
3. WHEN two comments have equal vote percentage and total votes, THE Social Post System SHALL sort by creation timestamp in ascending order (oldest first)
4. WHEN a comment's votes change, THE Social Post System SHALL recalculate its position in the sorted list
5. WHEN displaying sorted comments, THE Social Post System SHALL include the vote percentage with each comment

### Requirement 8: Data Integrity and Validation

**User Story:** As a system administrator, I want data validation and integrity checks, so that the system maintains consistent and valid data.

#### Acceptance Criteria

1. WHEN a user attempts to vote on their own comment, THE Social Post System SHALL reject the vote with an error message
2. WHEN a user attempts to vote on a non-existent comment, THE Social Post System SHALL return an error indicating the comment does not exist
3. WHEN a user attempts to comment on a non-existent post, THE Social Post System SHALL return an error indicating the post does not exist
4. WHEN storing ELO rank for vote weighting, THE Social Post System SHALL validate that the rank is a positive number
5. WHEN a post is deleted, THE Social Post System SHALL cascade delete all associated comments and votes

### Requirement 9: User ELO Rank Management

**User Story:** As a system administrator, I want to track and update user ELO ranks, so that vote weighting reflects current user expertise.

#### Acceptance Criteria

1. WHEN a new user is created, THE Social Post System SHALL initialize their ELO rank to 1000 (default)
2. WHEN a user's ELO rank is updated, THE Social Post System SHALL validate that the new rank is between 0 and 10000
3. WHEN retrieving a user's voting history, THE Social Post System SHALL include the ELO rank at the time of each vote
4. WHEN calculating vote weights, THE Social Post System SHALL use the voter's current ELO rank (not historical)
5. WHEN a user's ELO rank changes, THE Social Post System SHALL recalculate vote scores for all comments they have voted on

### Requirement 10: Comment Edit History

**User Story:** As a user, I want to see when comments were edited, so that I can understand the evolution of feedback.

#### Acceptance Criteria

1. WHEN a comment is edited, THE Social Post System SHALL record the edit timestamp
2. WHEN a comment is edited on an admin feedback post, THE Social Post System SHALL increment the edit count
3. WHEN displaying a comment, THE Social Post System SHALL show the original creation time and last edit time if edited
4. WHEN a comment has been edited, THE Social Post System SHALL display an "edited" indicator
5. WHEN retrieving comment details, THE Social Post System SHALL include the edit count for admin feedback post comments

### Requirement 11: Post Type Validation

**User Story:** As a system administrator, I want post type rules enforced, so that admin feedback posts maintain their special characteristics.

#### Acceptance Criteria

1. WHEN validating post type, THE Social Post System SHALL accept only "NORMAL" or "ADMIN_FEEDBACK" as valid values
2. WHEN a user attempts to change post type after creation, THE Social Post System SHALL reject the change with an error
3. WHEN retrieving posts, THE Social Post System SHALL include the post type in the response
4. WHEN filtering posts by type, THE Social Post System SHALL return only posts matching the specified type
5. WHEN an admin creates an admin feedback post, THE Social Post System SHALL apply admin feedback commenting rules to that post

### Requirement 12: Concurrent Vote Handling

**User Story:** As a user, I want my votes to be processed correctly even when multiple users vote simultaneously, so that vote counts remain accurate.

#### Acceptance Criteria

1. WHEN multiple users vote on the same comment simultaneously, THE Social Post System SHALL process each vote atomically
2. WHEN recalculating vote scores, THE Social Post System SHALL lock the comment record to prevent race conditions
3. WHEN a vote is being processed, THE Social Post System SHALL ensure the vote count update completes before releasing the lock
4. WHEN concurrent votes occur, THE Social Post System SHALL maintain accurate weighted vote totals
5. WHEN vote calculation fails, THE Social Post System SHALL rollback the transaction and return an error
