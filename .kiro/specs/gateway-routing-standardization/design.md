# Design Document

## Overview

Hệ thống hiện tại có kiến trúc microservices với API Gateway làm điểm vào duy nhất. Tuy nhiên, một số components (NLP service, Postman collections) đang gọi trực tiếp đến service URLs, bỏ qua Gateway. Design này sẽ chuẩn hóa tất cả API calls để đi qua Gateway, đảm bảo authentication, rate limiting, và monitoring được áp dụng nhất quán.

## Architecture

### Current Architecture Issues

```
External Client → Direct Service URL → Service
NLP Service → Direct Service URL → Exam/Question Service
```

**Problems:**
- Bỏ qua authentication filters
- Bỏ qua rate limiting
- Không có centralized monitoring
- Khó maintain và troubleshoot

### Target Architecture

```
External Client → API Gateway → Service Discovery → Service
NLP Service → API Gateway → Service Discovery → Target Service
```

**Benefits:**
- Centralized authentication
- Consistent rate limiting
- Unified monitoring và logging
- Single point of configuration

## Components and Interfaces

### 1. API Gateway Configuration

**File:** `config-repo/api-gateway.yml`

**Current Routes:**
- `/auth/**` → AUTH-SERVICE
- `/users/**` → USER-SERVICE
- `/exams/**` → EXAM-SERVICE
- `/questions/**` → QUESTION-SERVICE
- `/career/**` → CAREER-SERVICE
- `/news/**` → NEWS-SERVICE
- `/posts/**` → SOCIAL-SERVICE
- `/comments/**` → SOCIAL-SERVICE

**Filters Applied:**
- Authentication (AddUserInfoToHeader)
- Rate Limiting (RequestRateLimiter)
- CORS (globalcors)

### 2. NLP Service Integration

**Current Configuration:**
```python
QUESTION_SERVICE_URL = "http://question-service:8085"
EXAM_SERVICE_URL = "http://exam-service:8086"
```

**Target Configuration:**
```python
GATEWAY_URL = "http://gateway-service:8080"
# All calls go through gateway
# /questions/** → gateway → question-service
# /exams/** → gateway → exam-service
```

**Authentication:**
- NLP service cần JWT token để authenticate với Gateway
- Token có thể được lấy từ service account hoặc passed through từ original request

### 3. Postman Collection Updates

**Current Variables:**
```json
{
  "exam_service_url": "http://localhost:8086",
  "question_service_url": "http://localhost:8085",
  "user_service_url": "http://localhost:8082"
}
```

**Target Variables:**
```json
{
  "gateway_url": "http://localhost:8080"
}
```

**URL Pattern Changes:**
- `{{exam_service_url}}/exams/{{examId}}/history` → `{{gateway_url}}/exams/{{examId}}/history`
- `{{question_service_url}}/questions` → `{{gateway_url}}/questions`
- `{{user_service_url}}/users` → `{{gateway_url}}/users`

## Data Models

### Gateway Request Flow

```
Request {
  method: HTTP Method
  path: /exams/{examId}/history
  headers: {
    Authorization: Bearer <JWT_TOKEN>
    Content-Type: application/json
  }
  queryParams: {
    userId: Long
  }
}

Gateway Processing:
1. Validate JWT token
2. Apply rate limiting
3. Add user info to header
4. Route to EXAM-SERVICE via service discovery
5. Return response

Response {
  statusCode: 200
  body: ExamHistoryResponse
  headers: {
    X-RateLimit-Remaining: 4
  }
}
```

### NLP Service HTTP Client Configuration

```python
class GatewayClient:
    def __init__(self):
        self.gateway_url = os.getenv("GATEWAY_URL", "http://gateway-service:8080")
        self.service_token = self._get_service_token()
    
    def _get_service_token(self):
        # Get JWT token for service-to-service auth
        # Option 1: Use service account
        # Option 2: Pass through original request token
        pass
    
    async def get(self, path: str, **kwargs):
        headers = kwargs.get('headers', {})
        headers['Authorization'] = f'Bearer {self.service_token}'
        
        async with httpx.AsyncClient() as client:
            return await client.get(
                f"{self.gateway_url}{path}",
                headers=headers,
                **kwargs
            )
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Gateway routing consistency
*For any* valid API endpoint path (e.g., /exams/*, /questions/*, /users/*), when a request is made to the gateway with that path, the gateway should route it to the correct service based on the path prefix using service discovery
**Validates: Requirements 1.1, 1.2**

### Property 2: Authentication enforcement
*For any* protected endpoint request through the gateway, the gateway should validate the JWT token and reject requests with invalid or missing tokens with HTTP 401 status
**Validates: Requirements 1.4**

### Property 3: Rate limiting application
*For any* client making requests through the gateway, when the client exceeds the configured rate limit, subsequent requests should be rejected with HTTP 429 status
**Validates: Requirements 1.5**

### Property 4: NLP service gateway routing
*For any* API call from NLP service to other services (exam-service, question-service), the call should go through the gateway URL and include valid JWT authentication headers
**Validates: Requirements 2.1, 2.2, 2.3**

### Property 5: NLP service error handling
*For any* failed service-to-service call from NLP service, the service should handle the error gracefully without crashing and log appropriate error messages
**Validates: Requirements 2.5**

### Property 6: Postman collection URL pattern consistency
*For any* API request in the Postman collection, the request URL should use the {{gateway_url}} variable and follow the pattern {{gateway_url}}/service-path (e.g., {{gateway_url}}/exams, {{gateway_url}}/questions, {{gateway_url}}/users)
**Validates: Requirements 3.1, 3.2, 3.3**

### Property 7: Postman collection variable cleanup
*For any* variable defined in the Postman collection, the variable should not be a direct service URL (e.g., exam_service_url, question_service_url should not exist)
**Validates: Requirements 3.5**

## Error Handling

### Gateway Errors

**Authentication Failures:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing JWT token",
  "path": "/exams/123/history"
}
```

**Rate Limit Exceeded:**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later.",
  "retryAfter": 60
}
```

**Service Unavailable:**
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "EXAM-SERVICE is currently unavailable",
  "path": "/exams/123/history"
}
```

### NLP Service Error Handling

```python
async def call_through_gateway(self, path: str, method: str = "GET", **kwargs):
    try:
        response = await self.gateway_client.request(method, path, **kwargs)
        return response
    except httpx.HTTPStatusError as e:
        if e.response.status_code == 401:
            # Token expired, refresh and retry
            self.service_token = self._refresh_token()
            return await self.gateway_client.request(method, path, **kwargs)
        elif e.response.status_code == 429:
            # Rate limited, wait and retry
            await asyncio.sleep(1)
            return await self.gateway_client.request(method, path, **kwargs)
        else:
            raise
    except httpx.RequestError as e:
        # Network error, log and return error response
        logger.error(f"Gateway request failed: {str(e)}")
        return {"error": f"Failed to call {path} through gateway"}
```

## Testing Strategy

### Unit Tests

**Gateway Configuration Tests:**
- Test route matching for each service path
- Test filter application (auth, rate limiting)
- Test CORS configuration

**NLP Service Client Tests:**
- Test gateway URL construction
- Test authentication header injection
- Test error handling and retries

### Integration Tests

**End-to-End Gateway Flow:**
1. Make request to gateway endpoint
2. Verify authentication is checked
3. Verify request is routed to correct service
4. Verify response is returned correctly

**NLP Service Integration:**
1. NLP service calls exam-service through gateway
2. Verify JWT token is included
3. Verify request reaches exam-service
4. Verify response is processed correctly

### Property-Based Tests

We will use **pytest** with **hypothesis** library for property-based testing in Python components, and **JUnit 5** with **jqwik** for Java components.

**Configuration:**
- Each property test should run minimum 100 iterations
- Each test must reference the correctness property it validates
- Tag format: `# Feature: gateway-routing-standardization, Property {number}: {property_text}`

**Test Coverage:**
- Property 1: Test gateway routing with random valid paths
- Property 2: Test authentication with random valid/invalid tokens
- Property 3: Test rate limiting with random request patterns
- Property 4: Test NLP service calls with random endpoints
- Property 5: Validate Postman collection URLs programmatically

### Manual Testing

**Postman Collection Verification:**
1. Import updated collection
2. Set gateway_url environment variable
3. Execute all requests
4. Verify all requests go through gateway (check logs)
5. Verify authentication works
6. Verify rate limiting triggers after threshold

**NLP Service Verification:**
1. Deploy NLP service with new configuration
2. Trigger NLP operations (grading, duplicate check)
3. Monitor gateway logs to confirm routing
4. Verify authentication headers are present
5. Test error scenarios (service down, rate limit)

## Implementation Notes

### NLP Service Changes

**Files to modify:**
- `nlp-service/app/config.py` - Update service URLs to gateway URL
- `nlp-service/app/services/integration_service.py` - Update HTTP client to use gateway
- `nlp-service/.env.example` - Update environment variable examples
- `nlp-service/README.md` - Update documentation

**Authentication Strategy:**
For NLP service to call through gateway, we have two options:

**Option 1: Service Account Token**
- Create a service account in auth-service
- NLP service authenticates and gets a long-lived token
- Use this token for all gateway calls

**Option 2: Token Pass-Through**
- When NLP service is called with a user request, extract the JWT token
- Pass this token through when calling other services via gateway
- Maintains user context throughout the call chain

**Recommendation:** Use Option 2 (Token Pass-Through) for user-initiated requests, and Option 1 (Service Account) for background jobs.

### Postman Collection Changes

**Files to modify:**
- `ABC-Interview-VERIFIED-Complete.postman_collection.backup.json`
- `postman-collections/ABC-Interview-Verified-Complete.postman_collection.json`

**Changes needed:**
1. Replace all service-specific URL variables with single `gateway_url`
2. Update all request URLs to use `{{gateway_url}}/path`
3. Ensure authentication headers are present in all protected requests
4. Update environment variable documentation

### Docker Compose Considerations

NLP service is not currently in docker-compose.yml. When adding it:

```yaml
nlp-service:
  build: ./nlp-service
  container_name: interview-nlp-service
  ports:
    - "8088:8088"
  environment:
    GATEWAY_URL: http://gateway-service:8080
    JWT_SECRET: ${JWT_SECRET}
  networks:
    - interview-network
  depends_on:
    gateway-service:
      condition: service_healthy
```

### Monitoring and Observability

**Gateway Metrics to Monitor:**
- Request count per service
- Authentication success/failure rate
- Rate limit trigger frequency
- Response times per route
- Error rates per service

**Logging:**
- Log all gateway routing decisions
- Log authentication failures with reason
- Log rate limit triggers
- Log service discovery failures

**Alerts:**
- Alert on high authentication failure rate
- Alert on service unavailability
- Alert on excessive rate limiting
- Alert on slow response times

## Migration Strategy

### Phase 1: Update NLP Service
1. Add gateway client implementation
2. Update configuration to use gateway URL
3. Deploy and test in development
4. Monitor logs to confirm gateway routing

### Phase 2: Update Postman Collections
1. Create new environment with gateway_url
2. Update all request URLs
3. Test all endpoints
4. Document changes in README

### Phase 3: Validation
1. Run integration tests
2. Monitor production metrics
3. Verify all requests go through gateway
4. Document any issues and resolutions

### Rollback Plan

If issues occur:
1. NLP Service: Revert environment variables to direct service URLs
2. Postman: Keep old collection version available
3. Monitor for any cascading failures
4. Document root cause and fix before retry
