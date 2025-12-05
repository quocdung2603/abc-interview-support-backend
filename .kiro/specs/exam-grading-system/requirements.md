# Requirements Document

## Introduction

This document specifies the requirements for an Exam Grading System that automatically evaluates user exam submissions, calculates scores, determines pass/fail status, and maintains detailed history of user answers for each question in an exam. The system will use exact matching to compare user answers with correct answers.

## Glossary

- **Exam Grading System**: The system component responsible for evaluating user exam submissions and calculating scores
- **User Answer**: A response submitted by a user for a specific question in an exam
- **Correct Answer**: The reference answer stored in the question database used for comparison
- **Pass Status**: A boolean indicator showing whether a user passed or failed an exam based on the score threshold
- **Exam History**: A detailed record of all user answers for an exam, including correctness and scores
- **Question Service**: External service that provides question details including correct answers

## Requirements

### Requirement 1

**User Story:** As a student, I want to submit my exam answers and receive an automatic grade, so that I can immediately know my performance without waiting for manual evaluation.

#### Acceptance Criteria

1. WHEN a user submits exam answers THEN the Exam Grading System SHALL validate that all required questions are answered
2. WHEN a user submits exam answers THEN the Exam Grading System SHALL store each answer with the associated exam ID, question ID, and user ID
3. WHEN exam answers are submitted THEN the Exam Grading System SHALL calculate the total score based on individual question scores
4. WHEN the total score is calculated THEN the Exam Grading System SHALL determine pass status by comparing the score against a predefined threshold
5. WHEN grading is complete THEN the Exam Grading System SHALL store the result with score, pass status, and completion timestamp

### Requirement 2

**User Story:** As a student, I want to see which questions I answered correctly or incorrectly, so that I can understand my mistakes and learn from them.

#### Acceptance Criteria

1. WHEN a user requests exam history THEN the Exam Grading System SHALL retrieve all user answers for that specific exam
2. WHEN displaying exam history THEN the Exam Grading System SHALL include the question content, user's answer, correct answer, and correctness status for each question
3. WHEN exam history is requested THEN the Exam Grading System SHALL order questions by their order number in the exam
4. WHEN a user has not taken an exam THEN the Exam Grading System SHALL return an empty history with appropriate status message
5. WHEN displaying each answer THEN the Exam Grading System SHALL show whether the answer was marked correct or incorrect

### Requirement 3

**User Story:** As a system administrator, I want all questions to be graded with exact matching, so that scoring is objective and consistent.

#### Acceptance Criteria

1. WHEN grading any question THEN the Exam Grading System SHALL compare the user answer with the correct answer using case-insensitive exact matching after trimming whitespace
2. WHEN a user answer matches exactly THEN the Exam Grading System SHALL mark the answer as correct and assign full points for that question
3. WHEN a user answer does not match THEN the Exam Grading System SHALL mark the answer as incorrect and assign zero points
4. WHEN storing an answer result THEN the Exam Grading System SHALL set the isCorrect field to true or false based on the comparison
5. WHEN comparing answers THEN the Exam Grading System SHALL normalize both answers by converting to lowercase and removing leading/trailing whitespace

### Requirement 4

**User Story:** As a teacher, I want to retrieve detailed exam results for any user, so that I can review their performance and provide feedback.

#### Acceptance Criteria

1. WHEN a teacher requests exam results by exam ID and user ID THEN the Exam Grading System SHALL return the overall result including score and pass status
2. WHEN exam results are retrieved THEN the Exam Grading System SHALL include the completion timestamp
3. WHEN exam results include feedback THEN the Exam Grading System SHALL return the feedback text
4. WHEN no result exists for the given exam and user THEN the Exam Grading System SHALL return a not found error with appropriate message
5. WHEN multiple results exist for the same exam and user THEN the Exam Grading System SHALL return the most recent result

### Requirement 5

**User Story:** As a developer, I want the grading system to fetch correct answers from the Question Service, so that grading is based on the latest question data.

#### Acceptance Criteria

1. WHEN grading begins THEN the Exam Grading System SHALL retrieve question details including correct answers from the Question Service
2. WHEN the Question Service returns question data THEN the Exam Grading System SHALL extract the correct answer field for comparison
3. WHEN the Question Service is unavailable THEN the Exam Grading System SHALL retry the request up to three times with exponential backoff
4. WHEN all retries fail THEN the Exam Grading System SHALL return an error indicating the grading cannot be completed
5. WHEN question data is retrieved THEN the Exam Grading System SHALL cache the data for the duration of the grading operation

### Requirement 6

**User Story:** As a system administrator, I want exam submissions to be validated before grading, so that incomplete or invalid submissions are rejected.

#### Acceptance Criteria

1. WHEN a user submits exam answers THEN the Exam Grading System SHALL verify that the exam exists and is in PUBLISHED or COMPLETED status
2. WHEN a user submits exam answers THEN the Exam Grading System SHALL verify that the user is registered for the exam
3. WHEN a user submits answers for questions not in the exam THEN the Exam Grading System SHALL reject the submission with a validation error
4. WHEN a user submits empty or null answers THEN the Exam Grading System SHALL treat them as incorrect answers with zero points
5. WHEN validation fails THEN the Exam Grading System SHALL return a detailed error message indicating which validation rule was violated

### Requirement 7

**User Story:** As a student, I want to see my exam history with detailed question-by-question breakdown, so that I can review my performance on each topic.

#### Acceptance Criteria

1. WHEN retrieving exam history THEN the Exam Grading System SHALL include question metadata such as topic, level, and question type
2. WHEN displaying question details THEN the Exam Grading System SHALL fetch and include the question content from the Question Service
3. WHEN a question has multiple topics THEN the Exam Grading System SHALL display all associated topics
4. WHEN exam history includes timestamps THEN the Exam Grading System SHALL format timestamps in ISO 8601 format
5. WHEN the Question Service cannot provide question details THEN the Exam Grading System SHALL still return the user answer data with question ID only
