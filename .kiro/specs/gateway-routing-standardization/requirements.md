# Requirements Document

## Introduction

Hệ thống hiện tại có một số API calls đang gọi trực tiếp đến service URLs thay vì thông qua API Gateway. Điều này vi phạm kiến trúc microservices và gây ra các vấn đề về bảo mật, rate limiting, và monitoring. Tất cả các external requests phải đi qua API Gateway để đảm bảo tính nhất quán và kiểm soát tập trung.

## Glossary

- **API Gateway**: Điểm vào duy nhất cho tất cả các API requests từ bên ngoài, cung cấp routing, authentication, rate limiting, và monitoring
- **Service URL**: URL trực tiếp đến một microservice cụ thể (ví dụ: http://exam-service:8086)
- **Gateway URL**: URL của API Gateway (ví dụ: http://localhost:8080)
- **NLP Service**: Python service xử lý natural language processing và grading
- **Postman Collection**: Tập hợp các API test requests
- **Service-to-Service Communication**: Giao tiếp giữa các microservices nội bộ

## Requirements

### Requirement 1

**User Story:** Là một system architect, tôi muốn tất cả external API calls đi qua API Gateway, để đảm bảo security, rate limiting, và monitoring được áp dụng nhất quán.

#### Acceptance Criteria

1. WHEN an external client makes an API request THEN the request SHALL route through the API Gateway
2. WHEN the API Gateway receives a request for exam service THEN the Gateway SHALL forward the request to the exam-service using service discovery
3. WHEN the Postman collection is executed THEN all requests SHALL use the gateway_url variable instead of service-specific URLs
4. THE API Gateway SHALL apply authentication filters to all protected endpoints
5. THE API Gateway SHALL apply rate limiting to all incoming requests

### Requirement 2

**User Story:** Là một developer, tôi muốn NLP service gọi các services khác qua API Gateway, để đảm bảo tất cả requests đều được authenticate và monitor.

#### Acceptance Criteria

1. WHEN NLP service calls exam-service THEN the call SHALL route through the API Gateway
2. WHEN NLP service calls question-service THEN the call SHALL route through the API Gateway
3. WHEN NLP service makes authenticated requests THEN the service SHALL include valid JWT tokens in request headers
4. THE NLP service configuration SHALL use gateway URL instead of direct service URLs
5. WHEN service-to-service calls fail THEN the NLP service SHALL handle errors gracefully and log appropriate messages

### Requirement 3

**User Story:** Là một QA engineer, tôi muốn Postman collections sử dụng gateway URL, để test cases phản ánh đúng production architecture.

#### Acceptance Criteria

1. WHEN Postman collection is imported THEN all exam-service endpoints SHALL use {{gateway_url}}/exams pattern
2. WHEN Postman collection is imported THEN all question-service endpoints SHALL use {{gateway_url}}/questions pattern
3. WHEN Postman collection is imported THEN all user-service endpoints SHALL use {{gateway_url}}/users pattern
4. THE Postman collection SHALL define gateway_url as an environment variable
5. THE Postman collection SHALL NOT contain any direct service URL variables like exam_service_url

### Requirement 4

**User Story:** Là một DevOps engineer, tôi muốn có documentation rõ ràng về routing rules, để dễ dàng maintain và troubleshoot.

#### Acceptance Criteria

1. THE system SHALL document all gateway routing rules in a centralized location
2. THE documentation SHALL specify which endpoints require authentication
3. THE documentation SHALL specify rate limiting rules for each service
4. WHEN a new service is added THEN the gateway configuration SHALL be updated with appropriate routing rules
5. THE documentation SHALL include examples of correct API call patterns through gateway
