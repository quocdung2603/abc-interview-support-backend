# 🤖 NLP Service API Addition

## New Endpoint: Evaluate Answer with AI

### POST /evaluate-answer

**Tự động chấm điểm câu trả lời sử dụng Google Gemini AI**

**Authentication:** Required (Bearer token)

**Request:**
```json
{
  "question": "What is dependency injection?",
  "correct_answer": "Dependency Injection is a design pattern...",
  "user_answer": "DI is when you pass dependencies...",
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
  "feedback": "Good answer that demonstrates understanding...",
  "strengths": [
    "Clear explanation",
    "Mentioned key concepts"
  ],
  "weaknesses": [
    "Missing some details"
  ],
  "suggestions": [
    "Could elaborate on..."
  ],
  "confidence": 0.85
}
```

**Add this section to FRONTEND-API-SPECIFICATION.md under "8. NLP Service"**

---

## Implementation Summary

✅ **Completed:**
1. Added `evaluate_answer()` method to `AIStudioService`
2. Created `/evaluate-answer` endpoint in `main.py`
3. Configured Gemini API key in `.env`
4. Added `google-generativeai` to `requirements.txt`
5. Created comprehensive documentation
6. Created test script

✅ **Files Modified:**
- `nlp-service/app/services/ai_studio_service.py`
- `nlp-service/app/main.py`
- `nlp-service/requirements.txt`

✅ **Files Created:**
- `nlp-service/.env` (with API key)
- `nlp-service/.env.example`
- `nlp-service/NLP-SERVICE-GUIDE.md`
- `nlp-service/test_evaluation.py`

---

## Quick Start

```powershell
# 1. Install dependencies
cd nlp-service
pip install -r requirements.txt

# 2. Start service
uvicorn app.main:app --reload --port 5000

# 3. Test (in another terminal)
python test_evaluation.py
```

---

## API Integration Example

```javascript
// Frontend - Submit answer for grading
async function submitAnswerForGrading(questionId, userAnswer) {
  // Get question details
  const question = await fetch(`/questions/${questionId}`).then(r => r.json());
  
  // Evaluate with AI
  const evaluation = await fetch('/nlp/evaluate-answer', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      question: question.content,
      correct_answer: question.answer,
      user_answer: userAnswer,
      max_score: 10
    })
  }).then(r => r.json());
  
  // Show results
  showResults({
    score: `${evaluation.score}/${evaluation.max_score}`,
    percentage: `${evaluation.percentage}%`,
    feedback: evaluation.feedback,
    passed: evaluation.is_correct
  });
}
```

---

**Status:** ✅ Ready for Integration  
**Service Port:** 5000  
**API Key:** Configured  
**Model:** Gemini 1.5 Flash
