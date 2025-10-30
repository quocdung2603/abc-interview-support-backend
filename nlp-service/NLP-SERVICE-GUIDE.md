# 🤖 NLP Service - AI Answer Evaluation Guide

**Complete guide for using Gemini AI to evaluate answers**

---

## 📋 Quick Reference

**Service URL:** `http://localhost:5000`  
**Main Endpoint:** `POST /evaluate-answer`  
**API Key:** Already configured in `.env`  
**Model:** Gemini 1.5 Flash (Google AI Studio)

---

## 🚀 Usage Examples

### Example 1: Simple Question

**Request:**
```json
POST http://localhost:5000/evaluate-answer
{
  "question": "What is 2 + 2?",
  "correct_answer": "4",
  "user_answer": "4",
  "max_score": 10
}
```

**Response:**
```json
{
  "score": 10,
  "max_score": 10,
  "percentage": 100.0,
  "is_correct": true,
  "feedback": "Perfect answer!",
  "strengths": ["Correct answer"],
  "weaknesses": [],
  "suggestions": [],
  "confidence": 0.95
}
```

### Example 2: Technical Question (Spring Framework)

**Request:**
```json
{
  "question": "Explain dependency injection in Spring",
  "correct_answer": "Dependency Injection is a design pattern where objects receive their dependencies from external sources rather than creating them internally. Spring uses @Autowired, constructor injection, or setter injection to inject beans managed by IoC container.",
  "user_answer": "DI means passing dependencies to classes instead of creating them inside. Spring uses @Autowired annotation.",
  "max_score": 10
}
```

**Response:**
```json
{
  "score": 7,
  "max_score": 10,
  "percentage": 70.0,
  "is_correct": true,
  "feedback": "Good understanding of the basic concept. Answer correctly explains that DI involves passing dependencies rather than creating them, and mentions @Autowired. However, missing details about IoC container and different injection types.",
  "strengths": [
    "Correct core concept explained",
    "Mentioned @Autowired annotation",
    "Clear and concise"
  ],
  "weaknesses": [
    "Missing IoC container explanation",
    "No mention of constructor/setter injection",
    "Could explain benefits"
  ],
  "suggestions": [
    "Add explanation of Spring IoC container role",
    "Mention constructor and setter injection methods",
    "Explain benefits: testability, loose coupling"
  ],
  "confidence": 0.85
}
```

---

## 🎯 Integration with Exam Service

### Step 1: Add to ExamService

```java
@Service
public class ExamGradingService {
    private static final String NLP_SERVICE_URL = "http://localhost:5000";
    
    @Autowired
    private RestTemplate restTemplate;
    
    public EvaluationResult evaluateAnswer(
        String question,
        String correctAnswer,
        String userAnswer,
        int maxScore
    ) {
        String url = NLP_SERVICE_URL + "/evaluate-answer";
        
        Map<String, Object> request = Map.of(
            "question", question,
            "correct_answer", correctAnswer,
            "user_answer", userAnswer,
            "max_score", maxScore
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAuthToken());
        
        HttpEntity<Map<String, Object>> entity = 
            new HttpEntity<>(request, headers);
        
        ResponseEntity<EvaluationResult> response = 
            restTemplate.postForEntity(url, entity, EvaluationResult.class);
        
        return response.getBody();
    }
}
```

### Step 2: Add DTO

```java
@Data
public class EvaluationResult {
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private Boolean isCorrect;
    private String feedback;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private Double confidence;
}
```

### Step 3: Use in Controller

```java
@PostMapping("/exams/{examId}/answers/{answerId}/evaluate")
public ResultResponse evaluateExamAnswer(
    @PathVariable Long examId,
    @PathVariable Long answerId
) {
    // Get answer details
    UserAnswer userAnswer = examService.getUserAnswer(answerId);
    Question question = questionService.getQuestion(userAnswer.getQuestionId());
    
    // Evaluate with AI
    EvaluationResult evaluation = gradingService.evaluateAnswer(
        question.getContent(),
        question.getAnswer(),
        userAnswer.getAnswerContent(),
        10
    );
    
    // Save result
    Result result = new Result();
    result.setExamId(examId);
    result.setQuestionId(question.getId());
    result.setScore(evaluation.getScore());
    result.setFeedback(evaluation.getFeedback());
    result.setAutoGraded(true);
    
    return resultRepository.save(result);
}
```

---

## 📊 Grading Criteria

AI evaluates based on:

1. **Content Accuracy** (40%)
   - Correct facts and concepts
   - Technical accuracy
   
2. **Completeness** (30%)
   - Covers all key points
   - No missing critical information
   
3. **Clarity** (20%)
   - Clear explanation
   - Easy to understand
   
4. **Relevance** (10%)
   - Stays on topic
   - Answers the question asked

---

## 🔧 Configuration

### Change API Key

```powershell
# Edit .env file
notepad nlp-service\.env

# Update:
GOOGLE_API_KEY=your_new_key_here
```

### Adjust Strictness

```env
# Strict grading (consistent, less forgiving)
GEMINI_TEMPERATURE=0.1

# Balanced (recommended)
GEMINI_TEMPERATURE=0.2

# Lenient (more forgiving)
GEMINI_TEMPERATURE=0.4
```

---

## 🧪 Testing

```powershell
# Start service
cd nlp-service
uvicorn app.main:app --reload --port 5000

# Test in another terminal
curl -X POST http://localhost:5000/evaluate-answer `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer test-token" `
  -d '{\"question\":\"What is REST?\",\"correct_answer\":\"REST is an architectural style\",\"user_answer\":\"REST is about web services\",\"max_score\":10}'
```

---

**For complete documentation, see: `nlp-service/README.md`**

**Last Updated:** October 22, 2025
