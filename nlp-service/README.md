# NLP Service

Natural Language Processing service for the Interview Microservice ABC platform.

## Features

- **Question Similarity Detection**: Check for duplicate or similar questions
- **Essay Grading**: Automated grading of open-ended questions
- **Text Analysis**: Sentiment analysis, keyword extraction, and complexity analysis
- **Integration**: Seamless integration with Question Service and Exam Service

## API Endpoints

### Health Check
- `GET /health` - Service health status

### Similarity Detection
- `POST /similarity/check` - Check similarity between two texts
- `POST /questions/similarity/check` - Check if a question is similar to existing questions

### Essay Grading
- `POST /grading/essay` - Grade an essay answer
- `POST /exams/{exam_id}/questions/{question_id}/grade` - Grade a specific exam answer
- `POST /exams/{exam_id}/grade-all` - Grade all open-ended questions in an exam

### Analytics
- `GET /questions/{question_id}/analytics` - Get analytics for a specific question

## Usage

### Check Question Similarity
```bash
curl -X POST "http://localhost:8088/questions/similarity/check" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question_text": "What is machine learning?",
    "exclude_id": 123
  }'
```

### Grade Essay
```bash
curl -X POST "http://localhost:8088/grading/essay" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Explain the concept of machine learning",
    "answer": "Machine learning is a subset of artificial intelligence...",
    "max_score": 100
  }'
```

### Grade Exam Answer
```bash
curl -X POST "http://localhost:8088/exams/1/questions/5/grade" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "exam_id": 1,
    "question_id": 5,
    "answer_text": "Student answer here...",
    "max_score": 100
  }'
```

## Grading Criteria

The service evaluates essays based on four main criteria:

1. **Content (40%)**: Relevance and completeness of the answer
2. **Structure (20%)**: Organization and flow of ideas
3. **Language (20%)**: Vocabulary and sentence complexity
4. **Relevance (20%)**: How well the answer addresses the question

## Configuration

Environment variables can be set in the `.env` file:

```env
NLP_SERVICE_PORT=8088

# API Gateway Configuration (Required)
# All service-to-service calls go through the gateway
GATEWAY_URL=http://gateway-service:8080

# JWT Configuration for authentication
JWT_SECRET=your_jwt_secret

# NLP Model Configuration
SIMILARITY_THRESHOLD=0.7
GRADING_CONFIDENCE_THRESHOLD=0.6

# Legacy service URLs (deprecated - do not use)
# QUESTION_SERVICE_URL=http://question-service:8085
# EXAM_SERVICE_URL=http://exam-service:8086
```

### Gateway-Based Architecture

This service now routes all API calls through the API Gateway for:
- **Centralized Authentication**: JWT tokens are validated at the gateway
- **Rate Limiting**: Consistent rate limiting across all services
- **Monitoring**: Unified logging and metrics collection
- **Security**: Single point of entry for all external requests

**Authentication Setup:**

The NLP service can authenticate with the gateway in two ways:

1. **Token Pass-Through** (Recommended for user requests):
   - Extract JWT token from incoming request
   - Pass it through when calling other services
   - Maintains user context throughout the call chain

2. **Service Account** (For background jobs):
   - Use a dedicated service account token
   - Authenticate with auth-service to get a long-lived token
   - Use this token for all gateway calls

Example of setting request token:
```python
from app.services.integration_service import IntegrationService

service = IntegrationService()
# Set token from incoming request
service.gateway_client.set_request_token(user_jwt_token)
# Make calls - token will be included automatically
result = await service.check_question_duplicates(question_text)
```

## Docker

Build and run with Docker:

```bash
docker build -t nlp-service .
docker run -p 8088:8088 nlp-service
```

## Dependencies

- FastAPI for the web framework
- Sentence Transformers for semantic similarity
- spaCy for NLP processing
- scikit-learn for machine learning utilities
- NLTK for natural language processing
- Transformers for pre-trained models

## Development

Install dependencies:

```bash
pip install -r requirements.txt
```

Run locally:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8088 --reload
```

## Integration

This service integrates with other microservices through the API Gateway:

- **Question Service** (via `/questions/**`): For fetching question details and checking duplicates
- **Exam Service** (via `/exams/**`): For grading exam answers and saving results
- **API Gateway**: All service-to-service calls are routed through the gateway

### Gateway Routing

All external service calls use the following pattern:
- Question Service: `{GATEWAY_URL}/questions/*`
- Exam Service: `{GATEWAY_URL}/exams/*`
- User Service: `{GATEWAY_URL}/users/*`

The gateway handles:
- JWT token validation
- Rate limiting (5 requests/second burst, 10 capacity)
- Service discovery and load balancing
- CORS configuration
- Request/response logging
