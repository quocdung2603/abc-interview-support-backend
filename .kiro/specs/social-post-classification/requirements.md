# Requirements Document

## Introduction

This document specifies requirements for enhancing the Social Service Post classification system by adding validation, filtering, and integration with the Question Service's Field, Topic, and Level taxonomy. The Post entity already contains fieldId, topicId, and levelId fields, but these need proper validation and API support to ensure data integrity and enable users to filter and search posts by classification.

## Glossary

- **Social Service**: The microservice responsible for managing posts, comments, and votes
- **Question Service**: The microservice that manages questions and maintains the Field, Topic, and Level taxonomy
- **Post**: A user-generated content item that can be either a DISCUSSION or QUESTION type
- **Field**: A broad subject area (e.g., "Mathematics", "Computer Science")
- **Topic**: A specific subject within a Field (e.g., "Calculus" within Mathematics)
- **Level**: A difficulty level (e.g., "Beginner", "Intermediate", "Advanced")
- **Classification**: The process of assigning Field, Topic, and Level to a Post

## Requirements

### Requirement 1

**User Story:** As a user creating a post, I want the system to validate that my selected field, topic, and level exist, so that I can be confident my post is properly categorized.

#### Acceptance Criteria

1. WHEN a user creates a post with a fieldId THEN the Social Service SHALL verify the fieldId exists in the Question Service before creating the post
2. WHEN a user creates a post with a topicId THEN the Social Service SHALL verify the topicId exists in the Question Service and belongs to the specified fieldId
3. WHEN a user creates a post with a levelId THEN the Social Service SHALL verify the levelId exists in the Question Service
4. IF the fieldId does not exist THEN the Social Service SHALL reject the post creation and return an error message indicating invalid field
5. IF the topicId does not exist or does not belong to the specified fieldId THEN the Social Service SHALL reject the post creation and return an error message indicating invalid topic
6. IF the levelId is provided and does not exist THEN the Social Service SHALL reject the post creation and return an error message indicating invalid level

### Requirement 2

**User Story:** As a user browsing posts, I want to filter posts by field, topic, or level, so that I can find content relevant to my interests and skill level.

#### Acceptance Criteria

1. WHEN a user requests posts with a fieldId parameter THEN the Social Service SHALL return only posts that have the specified fieldId
2. WHEN a user requests posts with a topicId parameter THEN the Social Service SHALL return only posts that have the specified topicId
3. WHEN a user requests posts with a levelId parameter THEN the Social Service SHALL return only posts that have the specified levelId
4. WHEN a user requests posts with multiple classification parameters THEN the Social Service SHALL return only posts that match all specified parameters
5. WHEN a user requests posts without classification parameters THEN the Social Service SHALL return all posts according to the default sorting

### Requirement 3

**User Story:** As a user viewing a post, I want to see the field, topic, and level names (not just IDs), so that I can understand the post's classification without additional lookups.

#### Acceptance Criteria

1. WHEN the Social Service returns a post response THEN the response SHALL include field name, topic name, and level name in addition to their IDs
2. WHEN the Social Service retrieves classification names THEN it SHALL cache the results to minimize calls to the Question Service
3. IF the Question Service is unavailable THEN the Social Service SHALL return the post with IDs only and log a warning
4. WHEN classification data is cached THEN the cache SHALL expire after a configurable time period

### Requirement 4

**User Story:** As a user updating a post, I want the system to validate my new classification choices, so that I maintain data integrity when changing categories.

#### Acceptance Criteria

1. WHEN a user updates a post with a new fieldId THEN the Social Service SHALL verify the new fieldId exists in the Question Service
2. WHEN a user updates a post with a new topicId THEN the Social Service SHALL verify the new topicId exists and belongs to the current or new fieldId
3. WHEN a user updates a post with a new levelId THEN the Social Service SHALL verify the new levelId exists in the Question Service
4. IF validation fails during update THEN the Social Service SHALL reject the update and return an appropriate error message
5. WHEN a post is successfully updated with new classification THEN the updatedAt timestamp SHALL be updated

### Requirement 5

**User Story:** As a system administrator, I want classification validation to be resilient to Question Service failures, so that the Social Service remains available even when dependencies are down.

#### Acceptance Criteria

1. WHEN the Question Service is unavailable during post creation THEN the Social Service SHALL apply a circuit breaker pattern and fail gracefully
2. WHEN the circuit breaker is open THEN the Social Service SHALL return an error indicating the classification service is temporarily unavailable
3. WHEN the Question Service becomes available again THEN the circuit breaker SHALL automatically close and resume normal validation
4. WHEN validation requests timeout THEN the Social Service SHALL log the timeout and return an appropriate error to the user
5. THE Social Service SHALL configure reasonable timeout values for Question Service calls to prevent cascading failures

### Requirement 6

**User Story:** As a developer, I want clear API documentation for classification parameters, so that I can integrate the filtering features correctly.

#### Acceptance Criteria

1. THE Social Service SHALL document all classification query parameters in the OpenAPI specification
2. THE Social Service SHALL provide example requests and responses for filtering by field, topic, and level
3. THE Social Service SHALL document error responses for invalid classification IDs
4. THE Social Service SHALL include field, topic, and level information in the API response schema documentation
