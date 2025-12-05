# Implementation Plan - Bulk Question Generation

- [x] 1. Set up database schema and constraints


  - Add unique constraint on questions.question_content
  - Create indexes for performance (field_id, topic_id, level_id, question_type_id, status)
  - Create index on topics.field_id for referential queries
  - _Requirements: 2.5, 3.2_



- [ ] 2. Create DTOs and request/response models
  - [ ] 2.1 Create BulkGenerationRequest DTO
    - Fields: targetCount, batchSize, defaultUserId, defaultApproverId, dryRun


    - Add validation annotations
    - _Requirements: 7.1, 7.2, 7.3, 7.4_


  
  - [x] 2.2 Create GenerationResult DTO


    - Fields: requestedCount, generatedCount, failedCount, timestamps, duration, distributions, errors, success
    - _Requirements: 1.3_
  


  - [ ] 2.3 Create InitializationResult DTO
    - Fields: counts for created/deleted entities, success flag, errors list
    - _Requirements: 8.1, 8.2_
  

  - [ ] 2.4 Create GenerationProgress DTO
    - Fields: jobId, totalQuestions, processedQuestions, percentage, status
    - _Requirements: 6.5_

- [ ] 3. Implement Database Initialization Service
  - [ ] 3.1 Create DatabaseInitializationService interface and implementation
    - Method: resetDatabase() - drops all questions and answers
    - Method: initializeReferenceData() - creates/verifies reference data
    - Method: verifyReferenceData() - validates integrity
    - _Requirements: 8.1, 8.2, 8.7_
  
  - [ ] 3.2 Implement reference data creation logic
    - Create 10 Fields with IT domain names and descriptions
    - Create 50+ Topics (5+ per field) with proper fieldId references
    - Create 8 Levels (Intern to Architect) with score ranges


    - Create 3 Question Types (Single Choice, Multiple Choice, Fill in the Blank)
    - _Requirements: 8.3, 8.4, 8.5, 8.6_
  

  - [ ] 3.3 Write property test for reference data initialization
    - **Property 19: Minimum topics per field**
    - **Validates: Requirements 8.4**
  
  - [ ] 3.4 Write property test for topic-field relationships
    - **Property 20: Topic-field referential integrity**
    - **Validates: Requirements 8.7**


- [ ] 4. Implement Question Content Generator
  - [ ] 4.1 Create QuestionContentGenerator interface and implementation
    - Method: generateQuestion() - creates unique question content
    - Method: isUnique() - validates uniqueness

    - _Requirements: 2.1, 2.2, 4.4, 4.5_
  
  - [ ] 4.2 Implement template system
    - Create template maps for each QuestionType
    - Single Choice templates with one correct answer format
    - Multiple Choice templates with multiple correct answers format
    - Fill in the Blank templates with open-ended format
    - Template variables: {topic}, {level}, {concept}, {aspect}
    - _Requirements: 4.1, 4.2, 4.3_
  
  - [ ] 4.3 Implement topic-specific content generation
    - Create concept maps for each topic (e.g., ReactJS → hooks, components, state)
    - Ensure topic name or related terminology appears in question content
    - Generate contextually appropriate answers (minimum 10 characters)
    - _Requirements: 4.4, 4.5_
  
  - [ ] 4.4 Implement uniqueness validation with retry logic
    - Check against in-memory set of generated content
    - Check against database using query
    - Retry up to 10 times if duplicate detected
    - Throw exception if unable to generate unique content


    - _Requirements: 2.2, 2.3_
  
  - [ ] 4.5 Write property test for question format compliance
    - **Property 9: Single choice format compliance**
    - **Property 10: Multiple choice format compliance**

    - **Property 11: Fill in the blank format compliance**
    - **Validates: Requirements 4.1, 4.2, 4.3**
  
  - [ ] 4.6 Write property test for topic-specific terminology
    - **Property 12: Topic-specific terminology**
    - **Validates: Requirements 4.4**
  
  - [ ] 4.7 Write property test for non-empty answers
    - **Property 13: Non-empty answers**
    - **Validates: Requirements 4.5**

- [ ] 5. Implement Distribution Calculator
  - [ ] 5.1 Create DistributionCalculator class
    - Method: calculateDistribution() - computes question allocation
    - Calculate total valid combinations (topics × levels × question types)
    - Assign minimum 10 questions per combination
    - Distribute remaining questions proportionally by topic count per field


    - _Requirements: 3.1, 3.3, 3.5_
  
  - [ ] 5.2 Implement combination validation
    - Ensure topic belongs to correct field

    - Filter out invalid combinations
    - _Requirements: 3.2_
  
  - [ ] 5.3 Write property test for minimum combination coverage
    - **Property 6: Minimum combination coverage**
    - **Validates: Requirements 3.1**

  
  - [ ] 5.4 Write property test for topic-field relationship integrity
    - **Property 7: Topic-field relationship integrity**
    - **Validates: Requirements 3.2**
  
  - [ ] 5.5 Write property test for balanced field distribution
    - **Property 8: Balanced field distribution**

    - **Validates: Requirements 3.3**

- [ ] 6. Implement Bulk Generation Orchestrator
  - [ ] 6.1 Create BulkGenerationOrchestrator class
    - Method: generateQuestions() - main orchestration logic
    - Method: getProgress() - returns current progress
    - Method: cancelGeneration() - stops ongoing generation
    - _Requirements: 1.1, 1.5, 6.5_
  
  - [ ] 6.2 Implement batch processing logic
    - Process questions in configurable batches (default 100)
    - Commit each batch independently using @Transactional(propagation = REQUIRES_NEW)
    - Continue processing if a batch fails (log error and proceed)
    - Track progress and report at regular intervals
    - _Requirements: 1.5, 6.1, 6.2, 6.5_
  
  - [ ] 6.3 Implement metadata assignment
    - Set status = "APPROVED" for all generated questions
    - Set language = "en" for all generated questions
    - Assign userId and approvedBy from request parameters
    - Set createdAt and approvedAt timestamps (approvedAt >= createdAt)
    - Initialize usefulVote = 0 and unusefulVote = 0
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [ ] 6.4 Implement result summary generation
    - Calculate requestedCount, generatedCount, failedCount
    - Record startTime, endTime, and duration

    - Generate distribution maps by field, level, and question type
    - Collect all errors encountered during generation
    - _Requirements: 1.3, 3.4_
  
  - [x] 6.5 Write property test for exact count generation

    - **Property 1: Exact count generation**
    - **Validates: Requirements 1.1**
  
  - [ ] 6.6 Write property test for valid reference data usage
    - **Property 2: Valid reference data usage**
    - **Validates: Requirements 1.2**
  
  - [ ] 6.7 Write property test for batch processing consistency
    - **Property 3: Batch processing consistency**
    - **Validates: Requirements 1.5**
  
  - [ ] 6.8 Write property test for metadata initialization
    - **Property 14: Approved status initialization**


    - **Property 15: English language setting**
    - **Property 16: Valid user assignments**
    - **Property 17: Timestamp initialization**
    - **Property 18: Zero vote initialization**
    - **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**


- [ ] 7. Implement Uniqueness Validator
  - [ ] 7.1 Create UniquenessValidator class
    - Maintain in-memory Set<String> of generated content
    - Method: ensureUnique() - validates and retries if needed
    - Method: existsInDatabase() - checks database for existing content
    - _Requirements: 2.1, 2.2, 2.4_

  
  - [ ] 7.2 Add database query for uniqueness check
    - Create repository method: existsByQuestionContent(String content)
    - Use indexed query for performance
    - _Requirements: 2.2_
  

  - [ ] 7.3 Write property test for complete uniqueness
    - **Property 4: Complete uniqueness**
    - **Validates: Requirements 2.1, 2.4**
  
  - [x] 7.4 Write property test for database uniqueness validation

    - **Property 5: Database uniqueness validation**
    - **Validates: Requirements 2.2**

- [ ] 8. Implement REST Controller and endpoints
  - [x] 8.1 Create BulkQuestionController

    - POST /api/questions/reset-database
    - POST /api/questions/initialize-reference-data
    - POST /api/questions/bulk-generate
    - GET /api/questions/generation-progress/{jobId}
    - Add @PreAuthorize for admin-only access

    - _Requirements: 1.1, 8.1, 8.2_
  
  - [ ] 8.2 Implement request validation
    - Validate targetCount > 0 and <= maxTargetCount (100,000)
    - Validate batchSize > 0 and <= maxBatchSize (1,000)
    - Validate userId and approverId are positive
    - Return 400 Bad Request with error details if invalid


    - _Requirements: 7.5_
  
  - [ ] 8.3 Implement error handling and responses
    - Catch validation errors and return appropriate error responses


    - Catch generation errors and return detailed error information
    - Implement rollback on complete failure
    - Return 500 Internal Server Error for system failures
    - _Requirements: 1.4_



- [ ] 9. Add configuration properties
  - Create application.yml configuration section for bulk-generation
  - Properties: max-target-count, default-batch-size, max-batch-size, uniqueness-check-retries, batch-commit-timeout, progress-report-interval, default-user-id, default-approver-id
  - _Requirements: 7.1, 7.2, 7.3_

- [ ] 10. Implement progress tracking and reporting
  - [ ] 10.1 Create ProgressReporter class
    - Track totalQuestions and processedQuestions using AtomicInteger
    - Method: reportProgress() - logs progress at intervals
    - Calculate percentage completion
    - _Requirements: 6.5_
  
  - [ ] 10.2 Implement job tracking for async operations
    - Generate unique jobId for each generation request
    - Store progress in ConcurrentHashMap<String, GenerationProgress>
    - Allow clients to query progress via GET endpoint
    - _Requirements: 6.5_

- [ ] 11. Add logging and monitoring
  - Add INFO logs for progress updates and batch completions
  - Add WARN logs for uniqueness conflicts and retry attempts
  - Add ERROR logs for batch failures and database errors
  - Add DEBUG logs for individual question generation
  - _Requirements: 6.2, 6.5_

- [ ] 12. Create PowerShell scripts for Docker execution
  - [ ] 12.1 Create complete-bulk-generation-setup.ps1 script
    - Stops existing containers
    - Builds all services (with -SkipBuild and -SkipTests options)
    - Starts Docker containers
    - Waits for services to be ready with health checks
    - Resets database
    - Initializes reference data
    - Generates bulk questions with configurable count and batch size
    - Displays comprehensive results and verification
    - _Requirements: 1.1, 1.3, 8.1, 8.2_
  
  - [ ] 12.2 Create quick-bulk-setup.ps1 script
    - Simplified version with minimal output
    - Always builds and generates 12,000 questions
    - Fast setup for quick testing
    - _Requirements: 1.1, 8.1, 8.2_
  
  - [ ] 12.3 Create reset-and-init-database.ps1 script
    - Checks if question-service is running
    - Resets database (with -SkipReset option)
    - Initializes reference data
    - Verifies reference data with detailed output
    - Calculates statistics (combinations, minimum questions needed)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_
  
  - [ ] 12.4 Create generate-bulk-questions.ps1 script
    - Checks service health and reference data
    - Accepts parameters: QuestionCount, BatchSize, UserId, ApproverId, DryRun
    - Displays generation parameters and estimates
    - Confirms before proceeding (unless DryRun)
    - Generates questions with progress tracking
    - Shows detailed results with distributions by field, level, and type
    - Displays success rate and speed metrics
    - Shows sample generated questions
    - Verifies final database count
    - _Requirements: 1.1, 1.3, 1.5, 6.5, 7.1, 7.2, 7.3, 7.4_
  
  - [ ] 12.5 Create BULK-GENERATION-SCRIPTS-GUIDE.md
    - Document all scripts with usage examples
    - Common workflows (first-time setup, services running, generate more, etc.)
    - Script comparison table
    - Parameters reference
    - Troubleshooting guide
    - Performance tips
    - Useful endpoints
    - Example session
    - Best practices
    - _Requirements: All_

- [ ] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Create documentation
  - [x] 14.1 Create BULK-GENERATION-GUIDE.md


    - Overview of the bulk generation system
    - API endpoint documentation with examples
    - Configuration options
    - Troubleshooting guide
    - _Requirements: All_
  
  - [x] 14.2 Update existing documentation

    - Add bulk generation section to README.md
    - Update API documentation with new endpoints
    - _Requirements: All_

- [ ] 15. Final Checkpoint - Verify complete system
  - Run complete workflow: reset → initialize → generate 12,000 questions
  - Verify all 20 correctness properties hold
  - Verify generation completes within 30 minutes
  - Verify distribution is balanced across all combinations
  - Ensure all tests pass, ask the user if questions arise.
