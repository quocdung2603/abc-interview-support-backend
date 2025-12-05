# Requirements Document

## Introduction

This document specifies requirements for improving the exam registration APIs and social post system. The system needs to properly return exam registration data with complete information, refactor the post response DTOs, and implement a role-based post creation workflow where administrators can publish posts immediately while regular users create drafts that require approval.

## Glossary

- **Exam Service**: The microservice responsible for managing exams and exam registrations
- **Social Service**: The microservice responsible for managing social posts, discussions, and questions
- **Registration**: A record indicating a user has registered for an exam
- **Post**: A social content item that can be either a discussion or question
- **Admin**: A user with administrative privileges who can publish posts immediately
- **User**: A regular user whose posts require administrative approval
- **Draft Status**: A post state indicating content is not yet publicly visible
- **Published Status**: A post state indicating content is publicly visible
- **Locked Status**: A post state indicating content cannot be modified

## Requirements

### Requirement 1

**User Story:** As a developer consuming the exam registration API, I want to receive complete registration data including the examId, so that I can properly display and process exam registrations.

#### Acceptance Criteria

1. WHEN the system retrieves registrations by user ID THEN the Exam Service SHALL include the examId field in each registration response
2. WHEN the system retrieves registrations by exam ID THEN the Exam Service SHALL include the examId field in each registration response
3. WHEN a registration is created THEN the Exam Service SHALL persist the examId with the registration record
4. WHEN the registration response is serialized THEN the Exam Service SHALL map the examId field from the entity to the DTO

### Requirement 2

**User Story:** As a user who has registered for an exam, I want to see my registration appear in the exam's registration list, so that I can confirm my registration was successful.

#### Acceptance Criteria

1. WHEN a user registers for an exam THEN the Exam Service SHALL add the registration to the exam's registration list immediately
2. WHEN retrieving registrations by exam ID THEN the Exam Service SHALL return all registrations associated with that exam
3. WHEN a registration is created THEN the Exam Service SHALL establish the bidirectional relationship between exam and registration entities
4. WHEN querying registrations by exam THEN the Exam Service SHALL use the correct repository method to fetch associated registrations

### Requirement 3

**User Story:** As a frontend developer, I want the post response DTO to match the expected interface structure, so that I can properly consume the API without type mismatches.

#### Acceptance Criteria

1. WHEN the Social Service returns a post response THEN the system SHALL include all fields specified in the Post interface
2. WHEN serializing a post entity THEN the Social Service SHALL map userId, fieldId, topicId, and levelId to the response DTO
3. WHEN a post has no level assigned THEN the Social Service SHALL return levelId as null
4. WHEN a post has a lock time THEN the Social Service SHALL serialize lockTime in ISO 8601 format
5. WHEN timestamps are included THEN the Social Service SHALL format createdAt and updatedAt in ISO 8601 format

### Requirement 4

**User Story:** As an administrator, I want to create posts that are immediately published, so that I can quickly share announcements and gather feedback without requiring approval.

#### Acceptance Criteria

1. WHEN an administrator creates a post THEN the Social Service SHALL set the post status to PUBLISHED
2. WHEN an administrator creates a post THEN the Social Service SHALL make the post immediately visible to all users
3. WHEN determining post status THEN the Social Service SHALL check the creator's role from the authentication context
4. WHEN an administrator creates a post THEN the Social Service SHALL not require additional approval workflow

### Requirement 5

**User Story:** As a regular user, I want my posts to be created as drafts, so that administrators can review and approve them before they become publicly visible.

#### Acceptance Criteria

1. WHEN a regular user creates a post THEN the Social Service SHALL set the post status to DRAFT
2. WHEN a post has DRAFT status THEN the Social Service SHALL exclude it from public post listings
3. WHEN a regular user creates a post THEN the Social Service SHALL make it visible only to the creator and administrators
4. WHEN an administrator approves a draft post THEN the Social Service SHALL update the status to PUBLISHED
5. WHEN querying posts THEN the Social Service SHALL filter results based on user role and post status
