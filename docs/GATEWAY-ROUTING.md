# API Gateway Routing Documentation

## Overview

All external API requests to the Interview Microservice ABC platform must go through the API Gateway. The gateway provides centralized authentication, rate limiting, monitoring, and service discovery.

**Gateway URL:** `http://localhost:8080` (development) or `http://gateway-service:8080` (Docker)

## Architecture

```
External Client → API Gateway → Service Discovery (Eureka) → Target Microservice
```

### Benefits

- **Centralized Authentication**: JWT tokens validated once at gateway
- **Rate Limiting**: Consistent rate limits across all services
- **Monitoring**: Unified logging and metrics collection
- **Security**: Single point of entry for all external requests
- **Service Discovery**: Automatic routing to healthy service instances

## Routing Rules

All routes are configured in `config-repo/api-gateway.yml`.

### Service Routes

| Path Pattern | Target Service | Port | Authentication Required |
|-------------|----------------|------|------------------------|
| `/auth/**` | AUTH-SERVICE | 8081 | No (public endpoints) |
| `/users/**` | USER-SERVICE | 8082 | Yes |
| `/career/**` | CAREER-SERVICE | 8084 | Yes |
| `/questions/**` | QUESTION-SERVICE | 8085 | Yes |
| `/exams/**` | EXAM-SERVICE | 8086 | Yes |
| `/news/**` | NEWS-SERVICE | 8087 | Yes |
| `/recruitments/**` | NEWS-SERVICE | 8087 | Yes |
| `/posts/**` | SOCIAL-SERVICE | 8090 | Yes |
| `/comments/**` | SOCIAL-SERVICE | 8090 | Yes |

### Route Configuration Example

```yaml
- id: exam-service
  uri: lb://EXAM-SERVICE
  predicates: [ Path=/exams/** ]
  filters:
    - AddUserInfoToHeader
    - name: RequestRateLimiter
      args:
        redis-rate-limiter.replenishRate: 5
        redis-rate-limiter.burstCapacity: 10
        key-resolver: "#{@remoteAddrKeyResolver}"
```

## Authentication

### Protected Endpoints

All endpoints except `/auth/**` require JWT authentication.

**Request Header:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Authentication Flow

1. Client sends request with JWT token in Authorization header
2. Gateway validates token signature using shared JWT secret
3. If valid, gateway adds user info to request headers
4. Request is forwarded to target service
5. If invalid, gateway returns 401 Unauthorized

### Example Authenticated Request

```bash
curl -X GET "http://localhost:8080/exams/123/history?userId=456" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Rate Limiting

### Configuration

- **Replenish Rate**: 5 requests per second
- **Burst Capacity**: 10 requests
- **Key Resolver**: Remote IP address

### Rate Limit Headers

Responses include rate limit information:

```
X-RateLimit-Remaining: 4
X-RateLimit-Burst-Capacity: 10
X-RateLimit-Replenish-Rate: 5
```

### Rate Limit Exceeded Response

**Status Code:** 429 Too Many Requests

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later.",
  "retryAfter": 60
}
```

## Service-to-Service Communication

### NLP Service Integration

The NLP service calls other services through the gateway:

**Configuration:**
```python
GATEWAY_URL=http://gateway-service:8080
```

**Example Call:**
```python
from app.services.integration_service import IntegrationService

service = IntegrationService()
# Set user token from incoming request
service.gateway_client.set_request_token(user_jwt_token)

# Call exam service through gateway
result = await service.grade_exam_answer(exam_id, question_id, answer_text)
```

### Authentication for Service-to-Service Calls

**Option 1: Token Pass-Through** (Recommended for user requests)
- Extract JWT token from incoming request
- Pass it through when calling other services
- Maintains user context throughout the call chain

**Option 2: Service Account** (For background jobs)
- Use a dedicated service account token
- Authenticate with auth-service to get a long-lived token
- Use this token for all gateway calls

## Error Responses

### 401 Unauthorized

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing JWT token",
  "path": "/exams/123/history"
}
```

### 403 Forbidden

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions",
  "path": "/exams/123/delete"
}
```

### 429 Too Many Requests

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later.",
  "retryAfter": 60
}
```

### 503 Service Unavailable

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "EXAM-SERVICE is currently unavailable",
  "path": "/exams/123/history"
}
```

## CORS Configuration

The gateway is configured to allow cross-origin requests:

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
      allow-credentials: false
```

## Monitoring and Observability

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Gateway Metrics

Available at `/actuator/gateway`:
- Route information
- Filter chains
- Global filters
- Route predicates

### Prometheus Metrics

Available at `/actuator/prometheus`:
- Request count per route
- Response times
- Error rates
- Rate limit triggers

## Troubleshooting

### Common Issues

**Issue: 401 Unauthorized**
- Check JWT token is valid and not expired
- Verify Authorization header format: `Bearer <token>`
- Ensure JWT secret matches between services

**Issue: 503 Service Unavailable**
- Check target service is running
- Verify service is registered with Eureka
- Check service health endpoint

**Issue: 429 Too Many Requests**
- Reduce request rate
- Implement exponential backoff
- Consider increasing rate limits if legitimate traffic

**Issue: Connection Refused**
- Verify gateway is running on port 8080
- Check Docker network configuration
- Ensure all services are in same network

### Debugging

**Enable Debug Logging:**

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: DEBUG
```

**Check Gateway Logs:**

```bash
docker logs interview-gateway-service
```

**Verify Service Registration:**

```bash
curl http://localhost:8761/eureka/apps
```

## Best Practices

1. **Always use gateway URL** for external requests
2. **Include JWT token** in all authenticated requests
3. **Handle rate limiting** with exponential backoff
4. **Monitor gateway metrics** for performance issues
5. **Use service discovery names** (e.g., EXAM-SERVICE) in gateway config
6. **Keep JWT secrets synchronized** across all services
7. **Implement circuit breakers** for resilience
8. **Log all gateway errors** for debugging

## Migration from Direct Service URLs

If you have code calling services directly:

**Before:**
```bash
curl http://exam-service:8086/exams/123/history
```

**After:**
```bash
curl http://localhost:8080/exams/123/history \
  -H "Authorization: Bearer <token>"
```

**Update Environment Variables:**

```bash
# Old
EXAM_SERVICE_URL=http://exam-service:8086
QUESTION_SERVICE_URL=http://question-service:8085

# New
GATEWAY_URL=http://gateway-service:8080
```

## References

- Gateway Configuration: `config-repo/api-gateway.yml`
- Service Discovery: `http://localhost:8761` (Eureka)
- Gateway Health: `http://localhost:8080/actuator/health`
- Gateway Metrics: `http://localhost:8080/actuator/prometheus`
