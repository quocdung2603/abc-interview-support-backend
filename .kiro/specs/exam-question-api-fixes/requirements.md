# Requirements Document

## Introduction

This specification addresses the cleanup of deprecated fields in the Exam API response format. The system currently returns both new fields (`topicIds`, `questionTypeIds`) and deprecated fields (`topics`, `questionTypes`) for backward compatibility. This requirement focuses on removing the deprecated fields to standardize the API response format.

## Glossary

- **Exam Service**: The microservice responsible for managing exam creation, retrieval, and lifecycle
- **ExamResponse**: The Data Transfer Object (DTO) that defines the structure of exam data returned by the API
- **topicIds**: A list of numeric identifiers representing the topics covered in an exam
- **questionTypeIds**: A list of numeric identifiers representing the types of questions in an exam
- **Deprecated Fields**: The old field names (`topics`, `questionTypes`) that duplicate the functionality of the new fields

## Requirements

### Requirement 1

**User Story:** As an API consumer, I want to receive exam data with a clean, standardized response format, so that I can integrate with the API without confusion about which fields to use.

#### Acceptance Criteria

1. WHEN the Exam Service returns an exam response, THE Exam Service SHALL include the `topicIds` field as a list of numeric identifiers
2. WHEN the Exam Service returns an exam response, THE Exam Service SHALL include the `questionTypeIds` field as a list of numeric identifiers
3. WHEN the Exam Service returns an exam response, THE Exam Service SHALL NOT include the deprecated `topics` field
4. WHEN the Exam Service returns an exam response, THE Exam Service SHALL NOT include the deprecated `questionTypes` field
5. WHEN the Exam Service serializes an exam to JSON, THE Exam Service SHALL produce output matching the standardized format with only `topicIds` and `questionTypeIds`

### Requirement 2

**User Story:** As a developer maintaining the codebase, I want to remove unused deprecated fields from the data model, so that the code is cleaner and easier to maintain.

#### Acceptance Criteria

1. WHEN the ExamResponse DTO is defined, THE ExamResponse DTO SHALL NOT contain a `topics` field declaration
2. WHEN the ExamResponse DTO is defined, THE ExamResponse DTO SHALL NOT contain a `questionTypes` field declaration
3. WHEN the Exam entity is defined, THE Exam entity SHALL NOT contain a `topics` field for database persistence
4. WHEN the Exam entity is defined, THE Exam entity SHALL NOT contain a `questionTypes` field for database persistence
5. WHEN the ExamRequest DTO is defined, THE ExamRequest DTO SHALL accept input using `topicIds` and `questionTypeIds` fields

### Requirement 3

**User Story:** As a system administrator, I want existing exam data to continue working after the API changes, so that no data is lost during the migration.

#### Acceptance Criteria

1. WHEN existing exams are retrieved from the database, THE Exam Service SHALL successfully map the data to the new response format
2. WHEN the database contains exams with old field names, THE Exam Service SHALL handle the migration transparently
3. WHEN new exams are created, THE Exam Service SHALL store data using only the new field names (`topicIds`, `questionTypeIds`)
