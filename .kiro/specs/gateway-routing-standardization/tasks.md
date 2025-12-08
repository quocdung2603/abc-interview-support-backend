# Implementation Plan

- [ ] 1. Update NLP Service Configuration
  - [x] 1.1 Update config.py to use gateway URL


    - Replace QUESTION_SERVICE_URL and EXAM_SERVICE_URL with GATEWAY_URL
    - Update default values to point to gateway-service:8080
    - _Requirements: 2.4_


  - [x] 1.2 Create gateway client wrapper

    - Implement GatewayClient class with authentication support
    - Add methods for GET, POST, PUT, DELETE with automatic auth header injection
    - Implement token management (get, refresh, cache)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 1.3 Update integration_service.py to use gateway client


    - Replace direct httpx calls with gateway client calls
    - Update all service URLs to use gateway paths (/exams/*, /questions/*)
    - Maintain backward compatibility during transition
    - _Requirements: 2.1, 2.2_

  - [x] 1.4 Implement error handling for gateway calls


    - Add retry logic for 401 (token refresh)
    - Add backoff for 429 (rate limiting)
    - Add proper logging for all error scenarios
    - _Requirements: 2.5_



  - [ ] 1.5 Write property test for NLP service gateway routing
    - **Property 4: NLP service gateway routing**


    - **Validates: Requirements 2.1, 2.2, 2.3**



  - [ ] 1.6 Write property test for NLP service error handling
    - **Property 5: NLP service error handling**
    - **Validates: Requirements 2.5**

  - [x] 1.7 Update NLP service environment files


    - Update .env.example with GATEWAY_URL
    - Update README.md with new configuration instructions
    - Document authentication setup
    - _Requirements: 2.4_



- [ ] 2. Update Postman Collections
  - [ ] 2.1 Update ABC-Interview-VERIFIED-Complete collection
    - Replace all {{exam_service_url}} with {{gateway_url}}/exams

    - Replace all {{question_service_url}} with {{gateway_url}}/questions
    - Replace all {{user_service_url}} with {{gateway_url}}/users
    - Update all other service-specific URLs to use gateway
    - _Requirements: 3.1, 3.2, 3.3_



  - [ ] 2.2 Update postman-collections/ABC-Interview-Verified-Complete collection
    - Apply same URL updates as 2.1


    - Ensure consistency between both collection files
    - _Requirements: 3.1, 3.2, 3.3_


  - [ ] 2.3 Update environment variables in collections
    - Remove exam_service_url, question_service_url, user_service_url variables
    - Add gateway_url variable with default value
    - Update variable descriptions
    - _Requirements: 3.4, 3.5_


  - [ ] 2.4 Write property test for Postman URL patterns
    - **Property 6: Postman collection URL pattern consistency**

    - **Validates: Requirements 3.1, 3.2, 3.3**

  - [x] 2.5 Write property test for Postman variable cleanup

    - **Property 7: Postman collection variable cleanup**
    - **Validates: Requirements 3.5**

- [x] 3. Verify Gateway Configuration

  - [ ] 3.1 Review gateway routing rules
    - Verify all service routes are properly configured
    - Ensure authentication filters are applied to protected endpoints
    - Verify rate limiting configuration
    - _Requirements: 1.1, 1.2, 1.4, 1.5_


  - [ ] 3.2 Write property test for gateway routing
    - **Property 1: Gateway routing consistency**
    - **Validates: Requirements 1.1, 1.2**

  - [ ] 3.3 Write property test for authentication enforcement
    - **Property 2: Authentication enforcement**
    - **Validates: Requirements 1.4**



  - [ ] 3.4 Write property test for rate limiting
    - **Property 3: Rate limiting application**
    - **Validates: Requirements 1.5**


- [ ] 4. Update Docker Compose Configuration (Optional)
  - [ ] 4.1 Add NLP service to docker-compose.yml
    - Define nlp-service container configuration
    - Set GATEWAY_URL environment variable
    - Configure network and dependencies
    - Add health check

    - _Requirements: 2.1, 2.2_

  - [ ] 4.2 Update NLP service Dockerfile if needed
    - Ensure all dependencies are installed

    - Verify Python environment setup
    - _Requirements: 2.4_

- [ ] 5. Create Documentation
  - [ ] 5.1 Create gateway routing documentation
    - Document all routing rules in centralized location
    - List all endpoints and their target services

    - Specify which endpoints require authentication
    - Document rate limiting rules per service
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ] 5.2 Create API call examples documentation
    - Provide examples of correct API calls through gateway
    - Show authentication header format
    - Include error response examples

    - Document common troubleshooting scenarios
    - _Requirements: 4.5_

  - [ ] 5.3 Update NLP service README
    - Document new gateway-based architecture
    - Explain authentication setup


    - Provide configuration examples
    - _Requirements: 2.4_

- [ ] 6. Integration Testing
  - [ ] 6.1 Test NLP service integration
    - Deploy NLP service with gateway configuration
    - Test check_question_duplicates through gateway
    - Test grade_exam_answer through gateway
    - Test batch_grade_exam through gateway
    - Verify authentication headers are sent
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 6.2 Test Postman collection
    - Import updated collection
    - Set gateway_url environment variable
    - Execute all exam-related requests
    - Execute all question-related requests
    - Execute all user-related requests
    - Verify all requests succeed
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ] 6.3 Test gateway routing and filters
    - Test requests with valid JWT tokens
    - Test requests with invalid/missing tokens (should fail)
    - Test rate limiting by making rapid requests
    - Verify CORS headers are present
    - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
